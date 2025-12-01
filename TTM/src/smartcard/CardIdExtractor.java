package smartcard;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Arrays;

/**
 * Utility class for extracting and decrypting CardID from smart card
 * 
 * Protocol based on Main.java authenticateUser() method:
 * 1. Send command to card (INS_AUTH_GET_CARD_ID = 0x31)
 * 2. Card responds: [128 bytes RSA encrypted session key][16 bytes AES encrypted CardID]
 * 3. Decrypt session key using app's private key (RSA/ECB/PKCS1Padding)
 * 4. Decrypt CardID using session key (AES/CBC/NoPadding with zero IV)
 */
public class CardIdExtractor {
    
    private static final byte INS_AUTH_GET_CARD_ID = (byte) 0x31;
    private static final int RSA_KEY_SIZE = 128;  // 1024-bit RSA = 128 bytes
    private static final int RESPONSE_SIZE = 144; // 128 RSA + 16 AES
    private static final int IV_SIZE = 16;        // 128-bit IV
    
    /**
     * Extract and decrypt CardID from smart card
     * 
     * @param channel Smart card channel (must be connected)
     * @param keyManager Key manager with app's keypair for RSA decryption
     * @return Decrypted CardID (trimmed)
     * @throws Exception If card communication fails or decryption fails
     */
    public static String extractCardId(CardChannel channel, CardKeyManager keyManager) throws Exception {
        System.out.println("\n--- Extracting CardID from Smart Card ---");
        
        // Step 1: Send command to card to get encrypted CardID
        System.out.println("Step 1: Sending INS_AUTH_GET_CARD_ID command to card...");
        ResponseAPDU response = channel.transmit(new CommandAPDU(0x00, INS_AUTH_GET_CARD_ID, 0x00, 0x00, RESPONSE_SIZE));
        
        if (response.getSW() != 0x9000) {
            System.err.println("Failed: Card returned error. SW: " + String.format("0x%04X", response.getSW()));
            throw new Exception("Get CardID command failed. SW: " + String.format("0x%04X", response.getSW()));
        }
        
        byte[] encData = response.getData();
        System.out.println("Step 1: ✓ Received encrypted data: " + encData.length + " bytes");
        
        if (encData.length < RSA_KEY_SIZE) {
            System.err.println("Error: Invalid response length. Expected at least " + RSA_KEY_SIZE + " bytes, got " + encData.length);
            throw new Exception("Invalid response length. Expected at least " + RSA_KEY_SIZE + " bytes, got " + encData.length);
        }
        
        // Step 2: Extract RSA encrypted session key (first 128 bytes)
        System.out.println("Step 2: Extracting RSA encrypted session key...");
        byte[] encSessionKey = Arrays.copyOfRange(encData, 0, RSA_KEY_SIZE);
        System.out.println("Step 2: ✓ Session key encrypted block: " + RSA_KEY_SIZE + " bytes");
        
        // Step 3: Decrypt session key using app's private key (RSA/ECB/PKCS1Padding)
        System.out.println("Step 3: Decrypting session key with app's private RSA key...");
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, keyManager.getAppKeyPair().getPrivate());
        byte[] sessionKeyBytes = rsaCipher.doFinal(encSessionKey);
        System.out.println("Step 3: ✓ Session key decrypted: " + sessionKeyBytes.length + " bytes");
        
        // Step 4: Extract AES encrypted data (remaining bytes after RSA block)
        System.out.println("Step 4: Extracting AES encrypted CardID data...");
        byte[] encCardId = Arrays.copyOfRange(encData, RSA_KEY_SIZE, encData.length);
        System.out.println("Step 4: ✓ Encrypted CardID data: " + encCardId.length + " bytes");
        
        // Step 5: Decrypt CardID using session key (AES/CBC/NoPadding with zero IV)
        System.out.println("Step 5: Decrypting CardID with AES/CBC...");
        SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
        IvParameterSpec iv = new IvParameterSpec(new byte[IV_SIZE]); // Zero IV
        Cipher aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, iv);
        
        byte[] cardIdBytes = aesCipher.doFinal(encCardId);
        String cardId = new String(cardIdBytes).trim();
        System.out.println("Step 5: ✓ CardID decrypted: " + cardId);
        
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
