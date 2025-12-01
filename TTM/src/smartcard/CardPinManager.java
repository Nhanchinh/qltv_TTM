package smartcard;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.security.KeyPair;
import java.security.PublicKey;
import smartcard.CryptoUtils;

/**
 * Manages PIN operations on the smart card
 */
public class CardPinManager {
    
    private CardChannel channel;
    private CardKeyManager keyManager;
    
    private static final byte INS_RESET_PIN = (byte) 0x25;
    private static final byte CLA = 0x00;
    
    public CardPinManager(CardChannel channel) {
        this.channel = channel;
        this.keyManager = new CardKeyManager(channel);
    }
    
    /**
     * Reset user PIN using admin PIN
     * @param adminPin The admin PIN (6 characters)
     * @param newUserPin The new user PIN (6 characters)
     * @return true if reset successful
     * @throws Exception if reset fails
     */
    public boolean resetUserPin(String adminPin, String newUserPin) throws Exception {
        // Validate inputs
        if (adminPin == null || adminPin.length() != 6) {
            throw new Exception("Admin PIN must be exactly 6 characters");
        }
        if (newUserPin == null || newUserPin.length() != 6) {
            throw new Exception("New user PIN must be exactly 6 characters");
        }
        
        // Load app keypair from file (must exist)
        if (!keyManager.loadAppKeyPair()) {
            throw new Exception("No app keypair found. Please initialize a card first.");
        }
        
        // Get card public key
        keyManager.getPublicKey();
        if (!keyManager.hasCardPublicKey()) {
            throw new Exception("Failed to retrieve card public key");
        }
        
        System.out.println("\n========== RESET USER PIN ==========");
        System.out.println("Admin PIN: " + adminPin);
        System.out.println("New User PIN: " + newUserPin);
        
        // Prepare 12-byte data: adminPin (6) + newUserPin (6)
        byte[] resetData = new byte[12];
        System.arraycopy(adminPin.getBytes(), 0, resetData, 0, 6);
        System.arraycopy(newUserPin.getBytes(), 0, resetData, 6, 6);
        
        System.out.println("Reset Data (12 bytes): " + CryptoUtils.bytesToHex(resetData));
        
        // Send secure command with hybrid encryption
        return sendSecureCommand(INS_RESET_PIN, resetData);
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
        
        // Pad data to 16-byte blocks (exactly like Main.java)
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
            return true;
        } else {
            System.out.println(">>> FAILED (SW: " + Integer.toHexString(response.getSW()) + ")");
            throw new Exception("Reset PIN failed with SW: 0x" + Integer.toHexString(response.getSW()));
        }
    }
}
