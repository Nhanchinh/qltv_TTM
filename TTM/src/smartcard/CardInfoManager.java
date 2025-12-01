package smartcard;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.security.KeyPair;
import java.util.Arrays;

/**
 * Manager for retrieving encrypted user information from smart card
 * Uses hybrid decryption: RSA (for session key) + AES (for data)
 */
public class CardInfoManager {
    
    private CardChannel channel;
    private CardKeyManager keyManager;
    
    // APDU command instructions
    private static final byte INS_GET_INFO = (byte) 0x30;
    private static final byte CLA = 0x00;
    
    // Expected response sizes
    private static final int RSA_BLOCK_SIZE = 128;    // RSA-1024
    private static final int EXPECTED_RESPONSE_SIZE = 200; // Le (expected length)
    
    public CardInfoManager(CardChannel channel, CardKeyManager keyManager) {
        this.channel = channel;
        this.keyManager = keyManager;
    }
    
    /**
     * Retrieve and decrypt user information from card
     * Response format: [128 bytes RSA encrypted session key][AES encrypted data]
     * Data: [CardID 16][Name 64][DOB 16][RegDate 16] = 112 bytes
     */
    public UserInfo getInfo() throws Exception {
        if (!keyManager.hasAppKeyPair()) {
            throw new Exception("App KeyPair not available. Call getPublicKey() first.");
        }
        
        try {
            // Send GET_INFO command
            ResponseAPDU response = channel.transmit(
                new CommandAPDU(CLA, INS_GET_INFO, 0x00, 0x00, EXPECTED_RESPONSE_SIZE)
            );
            
            if (response.getSW() != 0x9000) {
                throw new Exception("Get Info failed: " + String.format("0x%04X", response.getSW()));
            }
            
            byte[] encData = response.getData();
            System.out.println("Received encrypted data length: " + encData.length);
            
            if (encData.length < RSA_BLOCK_SIZE) {
                throw new Exception("Invalid response: data too short");
            }
            
            // Step 1: Extract RSA encrypted session key (first 128 bytes)
            byte[] encSessionKey = new byte[RSA_BLOCK_SIZE];
            System.arraycopy(encData, 0, encSessionKey, 0, RSA_BLOCK_SIZE);
            
            // Step 2: Decrypt session key with app's private key
            KeyPair appKeyPair = keyManager.getAppKeyPair();
            byte[] sessionKeyBytes = CryptoUtils.decryptSessionKeyWithRSA(
                encSessionKey, 
                appKeyPair.getPrivate()
            );
            
            System.out.println("Session key decrypted, length: " + sessionKeyBytes.length);
            
            // Step 3: Extract AES encrypted data (remaining bytes)
            int aesDataLen = encData.length - RSA_BLOCK_SIZE;
            byte[] aesEncData = new byte[aesDataLen];
            System.arraycopy(encData, RSA_BLOCK_SIZE, aesEncData, 0, aesDataLen);
            
            // Step 4: Decrypt AES data with session key
            SecretKey sessionKey = CryptoUtils.createAESKeyFromBytes(sessionKeyBytes);
            byte[] plainData = CryptoUtils.decryptDataWithAES(aesEncData, sessionKey);
            
            System.out.println("Data decrypted, length: " + plainData.length);
            
            // Step 5: Parse plaintext
            UserInfo userInfo = parseUserData(plainData);
            System.out.println(">>> User info retrieved successfully");
            
            return userInfo;
            
        } catch (Exception e) {
            throw new Exception("Get info error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse user data from decrypted plaintext
     * Format: [CardID 16][Name 64][DOB 16][RegDate 16]
     */
    private UserInfo parseUserData(byte[] plainData) {
        int offset = 0;
        
        // Extract CardID (16 bytes)
        String cardId = extractString(plainData, offset, 16);
        offset += 16;
        
        // Extract Name (64 bytes)
        String name = extractString(plainData, offset, 64);
        offset += 64;
        
        // Extract DOB (16 bytes) - format: DDMMYYYY
        String dob = extractString(plainData, offset, 16);
        offset += 16;
        
        // Extract RegDate (16 bytes) - format: DDMMYYYY
        String regDate = extractString(plainData, offset, 16);
        
        return new UserInfo(cardId, name, dob, regDate);
    }
    
    /**
     * Extract and trim string from byte array
     */
    private String extractString(byte[] data, int offset, int length) {
        byte[] section = Arrays.copyOfRange(data, offset, offset + length);
        return new String(section).trim();
    }
    
    /**
     * User information data class
     */
    public static class UserInfo {
        public final String cardId;
        public final String name;
        public final String dob;      // DDMMYYYY format
        public final String regDate;  // DDMMYYYY format
        
        public UserInfo(String cardId, String name, String dob, String regDate) {
            this.cardId = cardId;
            this.name = name;
            this.dob = dob;
            this.regDate = regDate;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CardID: %s%nName: %s%nDOB: %s%nRegDate: %s",
                cardId, name, dob, regDate
            );
        }
    }
}
