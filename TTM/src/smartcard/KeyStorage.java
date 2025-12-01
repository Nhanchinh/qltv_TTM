package smartcard;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 * Persistent storage for app's RSA keypair
 * Saves to file: app_keypair.dat
 */
public class KeyStorage {
    
    private static final String KEY_FILE = "app_keypair.dat";
    private static final Path KEY_PATH = Paths.get(KEY_FILE);
    
    /**
     * Save app keypair to file (overwrites existing)
     */
    public static void saveKeyPair(KeyPair keyPair) throws Exception {
        try {
            System.out.println("\n[KeyStorage] Saving app keypair to file...");
            
            // Get encoded keys
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            
            // Write to file
            try (DataOutputStream out = new DataOutputStream(
                    new FileOutputStream(KEY_PATH.toFile()))) {
                
                // Write private key
                out.writeInt(privateKeyBytes.length);
                out.write(privateKeyBytes);
                
                // Write public key
                out.writeInt(publicKeyBytes.length);
                out.write(publicKeyBytes);
            }
            
            System.out.println("[KeyStorage] ✓ Keypair saved successfully");
            System.out.println("[KeyStorage] File: " + KEY_PATH.toAbsolutePath());
            
        } catch (Exception e) {
            throw new Exception("Failed to save keypair: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load app keypair from file
     * Returns null if file doesn't exist
     */
    public static KeyPair loadKeyPair() throws Exception {
        if (!Files.exists(KEY_PATH)) {
            System.out.println("[KeyStorage] No saved keypair found");
            return null;
        }
        
        try {
            System.out.println("\n[KeyStorage] Loading app keypair from file...");
            System.out.println("[KeyStorage] File: " + KEY_PATH.toAbsolutePath());
            
            // Read from file
            try (DataInputStream in = new DataInputStream(
                    new FileInputStream(KEY_PATH.toFile()))) {
                
                // Read private key
                int privateKeyLen = in.readInt();
                byte[] privateKeyBytes = new byte[privateKeyLen];
                in.readFully(privateKeyBytes);
                
                // Read public key
                int publicKeyLen = in.readInt();
                byte[] publicKeyBytes = new byte[publicKeyLen];
                in.readFully(publicKeyBytes);
                
                // Reconstruct keys
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
                
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                
                KeyPair keyPair = new KeyPair(publicKey, privateKey);
                
                System.out.println("[KeyStorage] ✓ Keypair loaded successfully");
                return keyPair;
            }
            
        } catch (Exception e) {
            System.err.println("[KeyStorage] ✗ Failed to load keypair: " + e.getMessage());
            throw new Exception("Failed to load keypair: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if keypair file exists
     */
    public static boolean hasStoredKeyPair() {
        return Files.exists(KEY_PATH);
    }
    
    /**
     * Delete stored keypair file
     */
    public static void deleteKeyPair() throws Exception {
        if (Files.exists(KEY_PATH)) {
            Files.delete(KEY_PATH);
            System.out.println("[KeyStorage] Keypair file deleted");
        }
    }
}
