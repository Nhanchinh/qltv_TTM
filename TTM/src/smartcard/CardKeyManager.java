package smartcard;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Manages smart card key operations
 * Retrieves public key from card and generates app's RSA keypair
 */
public class CardKeyManager {
    
    private static final byte INS_GET_PUBLIC_KEY = (byte) 0x22;
    private static final byte CLA = 0x00;
    
    private CardChannel channel;
    private PublicKey cardPublicKey;
    private KeyPair appKeyPair;
    
    public CardKeyManager(CardChannel channel) {
        this.channel = channel;
        this.cardPublicKey = null;
        this.appKeyPair = null;
    }
    
    /**
     * Fetch and store card's public key
     */
    public PublicKey getPublicKey() throws Exception {
        if (cardPublicKey != null) {
            return cardPublicKey;
        }
        
        try {
            ResponseAPDU r = channel.transmit(new CommandAPDU(CLA, INS_GET_PUBLIC_KEY, 0x00, 0x00, 256));
            
            // Handle 0x6C response (Wrong Le - request to retry with specific length)
            if (r.getSW1() == 0x6C) {
                r = channel.transmit(new CommandAPDU(CLA, INS_GET_PUBLIC_KEY, 0x00, 0x00, r.getSW2()));
            }
            
            if (r.getSW() != 0x9000) {
                throw new Exception("Get Public Key failed: " + String.format("0x%04X", r.getSW()));
            }
            
            // Parse modulus from response
            byte[] modulusBytes = r.getData();
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = BigInteger.valueOf(65537);
            
            // Create RSA public key
            RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            cardPublicKey = kf.generatePublic(rsaSpec);
            
            System.out.println(">>> Card Public Key retrieved successfully");
            return cardPublicKey;
            
        } catch (Exception e) {
            throw new Exception("Failed to get public key: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate and store app's RSA keypair (1024-bit)
     * Saves to file for persistence across sessions
     */
    public KeyPair generateAppKeyPair() throws Exception {
        if (appKeyPair != null) {
            return appKeyPair;
        }
        
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            appKeyPair = keyGen.generateKeyPair();
            
            System.out.println(">>> App RSA KeyPair generated (1024-bit)");
            
            // Save to file (overwrites old keypair)
            KeyStorage.saveKeyPair(appKeyPair);
            
            return appKeyPair;
            
        } catch (Exception e) {
            throw new Exception("Failed to generate app keypair: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load app keypair from file
     * If file doesn't exist, returns false
     */
    public boolean loadAppKeyPair() throws Exception {
        if (appKeyPair != null) {
            return true; // Already loaded in memory
        }
        
        try {
            appKeyPair = KeyStorage.loadKeyPair();
            
            if (appKeyPair != null) {
                System.out.println(">>> App RSA KeyPair loaded from file");
                return true;
            } else {
                System.out.println(">>> No saved app keypair found");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load app keypair: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get stored card public key
     */
    public PublicKey getCardPublicKey() {
        return cardPublicKey;
    }

    /**
     * Get encoded bytes for card public key (X.509)
     */
    public byte[] getCardPublicKeyEncoded() {
        if (cardPublicKey == null) return null;
        try {
            return cardPublicKey.getEncoded();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get stored app keypair
     */
    public KeyPair getAppKeyPair() {
        return appKeyPair;
    }
    
    /**
     * Check if card public key is available
     */
    public boolean hasCardPublicKey() {
        return cardPublicKey != null;
    }
    
    /**
     * Check if app keypair is available
     */
    public boolean hasAppKeyPair() {
        return appKeyPair != null;
    }
    
    /**
     * Reset all keys
     */
    public void reset() {
        cardPublicKey = null;
        appKeyPair = null;
    }
}
