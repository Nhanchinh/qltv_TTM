package smartcard;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Manages PIN verification on the smart card
 */
public class CardVerifyManager {
    
    private CardChannel channel;
    private CardKeyManager keyManager;
    
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte CLA = 0x00;
    
    public CardVerifyManager(CardChannel channel) {
        this.channel = channel;
        this.keyManager = new CardKeyManager(channel);
    }
    
    /**
     * Verify user PIN
     * @param userPin The user PIN (6 characters)
     * @return true if verification successful
     * @throws Exception if verification fails
     */
    public boolean verifyPin(String userPin) throws Exception {
        // Validate input
        if (userPin == null || userPin.length() != 6) {
            throw new Exception("User PIN must be exactly 6 characters");
        }
        
        // Load app keypair from file (must exist)
        if (!keyManager.loadAppKeyPair()) {
            throw new Exception("No app keypair found. Please initialize a card first.");
        }
        
        try {
            // Get card public key
            keyManager.getPublicKey();
            if (!keyManager.hasCardPublicKey()) {
                throw new Exception("Failed to retrieve card public key");
            }
        } catch (Exception e) {
            // Check if card is blocked (0x6983)
            if (e.getMessage().contains("0x6983")) {
                throw new Exception("CARD_BLOCKED");
            }
            throw e;
        }
        
        System.out.println("\n========== VERIFY USER PIN ==========");
        System.out.println("User PIN: " + userPin);
        
        // Send secure command with hybrid encryption
        return sendSecureCommand(INS_VERIFY_PIN, userPin.getBytes());
    }
    
    /**
     * Send secure command with hybrid encryption
     * (ĐÃ ĐƯỢC ĐƠN GIẢN HÓA): Gửi dữ liệu PLAINTEXT giống BookstoreClientTest.verifyPin()
     */
    private boolean sendSecureCommand(byte ins, byte[] rawData) throws Exception {
        // Gửi trực tiếp dữ liệu raw (PLAINTEXT) xuống thẻ
        if (rawData == null) {
            rawData = new byte[0];
        }

        System.out.println("Sending PLAINTEXT CMD (INS: " + String.format("0x%02X", ins) +
                           "), Data length = " + rawData.length + " bytes");

        // Gửi lệnh giống BookstoreClientTest.verifyPin()
        CommandAPDU command = new CommandAPDU(CLA, ins, 0x00, 0x00, rawData);
        ResponseAPDU response = channel.transmit(command);
        
        System.out.println("Response SW: " + Integer.toHexString(response.getSW()));
        
        if (response.getSW() == 0x9000) {
            System.out.println(">>> SUCCESS");
            System.out.println("=====================================\n");
            return true;
        } else {
            System.out.println(">>> FAILED (SW: " + Integer.toHexString(response.getSW()) + ")");
            
            // Get PIN tries when verification fails
            int remainingTries = -1;
            int sw = response.getSW();
            
            // Check if SW is in range 0x6600-0x66FF (wrong PIN with debug hash)
            // or exactly 0x6300 (SW_VERIFICATION_FAILED)
            boolean isWrongPin = (sw >= 0x6600 && sw <= 0x66FF) || sw == 0x6300;
            
            if (isWrongPin) {
                try {
                    System.out.println("Getting remaining PIN tries...");
                    CommandAPDU getPinTriesCmd = new CommandAPDU(0x00, (byte)0x33, 0x00, 0x00);
                    ResponseAPDU triesResponse = channel.transmit(getPinTriesCmd);
                    
                    if (triesResponse.getSW() == 0x9000 && triesResponse.getData().length > 0) {
                        byte tries = triesResponse.getData()[0];
                        remainingTries = 3 - tries;
                        System.out.println("Failed attempts: " + tries + ", Remaining: " + remainingTries);
                    }
                } catch (Exception e) {
                    System.err.println("Could not get PIN tries: " + e.getMessage());
                }
            }
            
            System.out.println("=====================================\n");
            
            // Provide specific error messages
            if (isWrongPin) {
                if (remainingTries >= 0) {
                    throw new Exception("WRONG_PIN:" + remainingTries);
                } else {
                    throw new Exception("WRONG_PIN");
                }
            } else if (response.getSW() == 0x6983) {
                throw new Exception("CARD_BLOCKED");
            } else {
                throw new Exception("Xác thực thất bại: 0x" + Integer.toHexString(response.getSW()));
            }
        }
    }
}
