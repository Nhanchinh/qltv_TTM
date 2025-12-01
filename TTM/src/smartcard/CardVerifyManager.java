package smartcard;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.security.PublicKey;
import smartcard.CryptoUtils;

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
     * Uses RSA to encrypt AES session key, then AES to encrypt data
     */
    private boolean sendSecureCommand(byte ins, byte[] rawData) throws Exception {
        // Get card public key
        PublicKey cardPublicKey = keyManager.getCardPublicKey();
        if (cardPublicKey == null) {
            throw new Exception("Card public key not available");
        }
        
        // Generate AES session key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey sessionKey = keyGen.generateKey();
        
        System.out.println("Session Key: " + CryptoUtils.bytesToHex(sessionKey.getEncoded()));
        
        // Encrypt session key with card's RSA public key
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, cardPublicKey);
        byte[] encryptedSessionKey = rsaCipher.doFinal(sessionKey.getEncoded());
        
        System.out.println("Encrypted Session Key (" + encryptedSessionKey.length + " bytes): " + 
                         CryptoUtils.bytesToHex(encryptedSessionKey));
        
        // Pad data to 16-byte blocks
        int blockSize = 16;
        int paddedLength = ((rawData.length / blockSize) + 1) * blockSize;
        if (rawData.length % blockSize == 0 && rawData.length > 0) {
            paddedLength = rawData.length;
        } else if (rawData.length == 0) {
            paddedLength = 16;
        }
        
        byte[] paddedData = new byte[paddedLength];
        System.arraycopy(rawData, 0, paddedData, 0, rawData.length);
        // Rest is already zero-padded by Java
        
        System.out.println("Padded Data (" + paddedData.length + " bytes): " + 
                         CryptoUtils.bytesToHex(paddedData));
        
        // Encrypt data with AES session key (CBC mode, zero IV)
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        Cipher aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);
        byte[] encryptedData = aesCipher.doFinal(paddedData);
        
        System.out.println("Encrypted Data (" + encryptedData.length + " bytes): " + 
                         CryptoUtils.bytesToHex(encryptedData));
        
        // Combine encrypted session key + encrypted data
        byte[] apduData = new byte[encryptedSessionKey.length + encryptedData.length];
        System.arraycopy(encryptedSessionKey, 0, apduData, 0, encryptedSessionKey.length);
        System.arraycopy(encryptedData, 0, apduData, encryptedSessionKey.length, encryptedData.length);
        
        System.out.println("Total APDU Data (" + apduData.length + " bytes)");
        System.out.println("Sending Secure CMD (INS: " + String.format("0x%02X", ins) + ")...");
        
        // Send command
        CommandAPDU command = new CommandAPDU(CLA, ins, 0x00, 0x00, apduData);
        ResponseAPDU response = channel.transmit(command);
        
        System.out.println("Response SW: " + Integer.toHexString(response.getSW()));
        
        if (response.getSW() == 0x9000) {
            System.out.println(">>> SUCCESS");
            System.out.println("=====================================\n");
            return true;
        } else {
            System.out.println(">>> FAILED (SW: " + Integer.toHexString(response.getSW()) + ")");
            System.out.println("=====================================\n");
            
            // Provide specific error messages
            if (response.getSW() == 0x6602) {
                throw new Exception("WRONG_PIN");
            } else if (response.getSW() == 0x6983) {
                throw new Exception("CARD_BLOCKED");
            } else {
                throw new Exception("Xác thực thất bại: 0x" + Integer.toHexString(response.getSW()));
            }
        }
    }
}
