package smartcard;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.PublicKey;
import java.security.PrivateKey;

/**
 * Cryptographic utilities for hybrid encryption (RSA + AES)
 */
public class CryptoUtils {
    
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String AES_ALGORITHM = "AES/CBC/NoPadding";
    private static final int AES_KEY_SIZE = 128; // bits
    private static final int BLOCK_SIZE = 16;     // bytes for AES
    
    /**
     * Generate random AES-128 session key
     */
    public static SecretKey generateSessionKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }
    
    /**
     * Encrypt session key with RSA public key
     */
    public static byte[] encryptSessionKeyWithRSA(SecretKey sessionKey, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(sessionKey.getEncoded());
    }
    
    /**
     * Decrypt session key with RSA private key
     */
    public static byte[] decryptSessionKeyWithRSA(byte[] encryptedKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedKey);
    }
    
    /**
     * Encrypt data with AES in CBC mode (zero IV)
     */
    public static byte[] encryptDataWithAES(byte[] data, SecretKey sessionKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(new byte[BLOCK_SIZE]); // Zero IV
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, iv);
        return cipher.doFinal(data);
    }
    
    /**
     * Decrypt data with AES in CBC mode (zero IV)
     */
    public static byte[] decryptDataWithAES(byte[] encryptedData, SecretKey sessionKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(new byte[BLOCK_SIZE]); // Zero IV
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, iv);
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * Pad data to BLOCK_SIZE with zeros
     */
    public static byte[] padData(byte[] data) {
        int paddingLength = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        if (paddingLength == 0) {
            return data; // Already aligned
        }
        byte[] padded = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, padded, 0, data.length);
        return padded;
    }
    
    /**
     * Remove zero-padding from decrypted data
     */
    public static byte[] unpadData(byte[] data) {
        if (data == null || data.length == 0) {
            return data;
        }
        
        int length = data.length;
        while (length > 0 && data[length - 1] == 0) {
            length--;
        }
        
        if (length == data.length) {
            return data;
        }
        
        byte[] unpadded = new byte[length];
        System.arraycopy(data, 0, unpadded, 0, length);
        return unpadded;
    }
    
    /**
     * Create AES key from byte array
     */
    public static SecretKey createAESKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }
    
    /**
     * Convert hex string to byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * Convert byte array to hex string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    /**
     * Create fixed-length byte array from string
     */
    public static byte[] createFixedLengthData(String text, int length) {
        byte[] result = new byte[length];
        byte[] source = text.getBytes();
        int copyLen = Math.min(source.length, length);
        System.arraycopy(source, 0, result, 0, copyLen);
        return result;
    }
}
