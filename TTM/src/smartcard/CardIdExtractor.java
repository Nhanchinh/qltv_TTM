package smartcard;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Utility class for extracting CardID from smart card
 * 
 * Giao thức hiện tại giống BookstoreClientTest.authenticateUser() STEP 1:
 * 1. Gửi lệnh INS_AUTH_GET_CARD_ID (0x31) với Le = 16
 * 2. Thẻ trả về CardID dạng PLAINTEXT, 16 byte
 */
public class CardIdExtractor {
    
    private static final byte INS_AUTH_GET_CARD_ID = (byte) 0x31;
    private static final int RESPONSE_SIZE = 16; // 16 bytes CardID plaintext
    
    /**
     * Extract CardID from smart card (PLAINTEXT)
     * 
     * @param channel Smart card channel (must be connected)
     * @return Decrypted CardID (trimmed)
     * @throws Exception If card communication fails or decryption fails
     */
    public static String extractCardId(CardChannel channel, CardKeyManager keyManager) throws Exception {
        System.out.println("\n--- Extracting CardID from Smart Card ---");
        
        // Step 1: Send command to card to get CardID (PLAINTEXT)
        System.out.println("Step 1: Sending INS_AUTH_GET_CARD_ID command to card (PLAINTEXT)...");
        ResponseAPDU response = channel.transmit(new CommandAPDU(0x00, INS_AUTH_GET_CARD_ID, 0x00, 0x00, RESPONSE_SIZE));
        
        if (response.getSW() != 0x9000) {
            System.err.println("Failed: Card returned error. SW: " + String.format("0x%04X", response.getSW()));
            throw new Exception("Get CardID command failed. SW: " + String.format("0x%04X", response.getSW()));
        }
        
        byte[] cardIdBytes = response.getData();
        System.out.println("Step 1: ✓ Received CardID data: " + cardIdBytes.length + " bytes");
        
        String cardId = new String(cardIdBytes).trim();
        System.out.println("Step 2: ✓ CardID (PLAINTEXT): " + cardId);
        
        // Validate CardID format
        if (cardId.isEmpty()) {
            System.err.println("Error: CardID is empty after decryption");
            throw new Exception("Invalid CardID: empty after decryption");
        }
        
        if (cardId.length() < 4) {
            System.err.println("Error: CardID too short: " + cardId);
            throw new Exception("Invalid CardID format: " + cardId + " (too short)");
        }
        
        System.out.println("--- CardID Extraction Completed Successfully ---\n");
        return cardId;
    }
    
    /**
     * Convert byte array to hex string (for debugging)
     * 
     * @param bytes Byte array to convert
     * @return Hex string representation
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
