package smartcard;

import javax.crypto.SecretKey;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * Orchestrates complete card setup workflow
 * Step 1: Get public key
 * Step 2: Setup card with PINs (hybrid encryption)
 * Step 3: Initialize user data
 */
public class CardSetupManager {
    
    private CardChannel channel;
    private CardKeyManager keyManager;
    
    // APDU command instructions
    private static final byte INS_SETUP_CARD = (byte) 0x10;
    private static final byte INS_INIT_DATA = (byte) 0x15;
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte INS_GET_PIN_TRIES = (byte) 0x33;
    private static final byte INS_AUTH_GET_CARD_ID = (byte) 0x31;
    private static final byte CLA = 0x00;
    
    public CardSetupManager(CardChannel channel) {
        this.channel = channel;
        this.keyManager = new CardKeyManager(channel);
    }
    
    /**
     * Step 1: Retrieve public key and load/generate app keypair
     * Checks if keypair file exists first - if valid, loads it
     * Otherwise generates new keypair
     */
    public boolean getPublicKey() throws Exception {
        try {
            keyManager.getPublicKey();
            
            // Try to load existing keypair first
            if (!keyManager.loadAppKeyPair()) {
                // No valid keypair found - generate new one
                System.out.println("No valid keypair found, generating new keypair...");
                keyManager.generateAppKeyPair();
            } else {
                System.out.println("Loaded existing keypair from file");
            }
            
            return keyManager.hasCardPublicKey() && keyManager.hasAppKeyPair();
        } catch (Exception e) {
            throw new Exception("Failed to get public key: " + e.getMessage(), e);
        }
    }
    
    /**
     * Step 2: Setup card with user and admin PINs
     * Format: 6 bytes (user PIN) + 6 bytes (admin PIN)
     * Exactly like Main.java sendSecureCommand() method
     */
    public boolean setupCard(String userPin, String adminPin) throws Exception {
        try {
            // Validate PINs
            if (userPin == null || adminPin == null) {
                throw new Exception("PINs cannot be null");
            }
            if (userPin.length() != 6 || adminPin.length() != 6) {
                throw new Exception("PINs must be exactly 6 characters. Got userPin length=" + userPin.length() + 
                                  ", adminPin length=" + adminPin.length());
            }
            
            System.out.println("\n========== SETUP CARD (STEP 2) ==========");
            System.out.println("User PIN: " + userPin + " (length=" + userPin.length() + ")");
            System.out.println("Admin PIN: " + adminPin + " (length=" + adminPin.length() + ")");
            
            // Chuẩn bị payload 12 byte: [6 byte user PIN][6 byte admin PIN]
            byte[] setupData = new byte[12];
            System.arraycopy(userPin.getBytes(), 0, setupData, 0, 6);
            System.arraycopy(adminPin.getBytes(), 0, setupData, 6, 6);

            System.out.println("Sending Setup Data (PLAINTEXT, 12 bytes)...");

            // Gửi APDU dạng plaintext (giống BookstoreClientTest.setupCard)
            ResponseAPDU response = channel.transmit(
                new CommandAPDU(CLA, INS_SETUP_CARD, 0x00, 0x00, setupData)
            );
            
            int sw = response.getSW();
            System.out.println("Response SW: 0x" + String.format("%04X", sw));
            
            if (sw == 0x9000) {
                System.out.println("✓ Setup card SUCCESS");
                System.out.println("=====================================\n");
                return true;
            } else {
                System.out.println("✗ Setup card FAILED");
                System.out.println("=====================================\n");
                throw new Exception("Setup card failed: " + String.format("0x%04X", sw));
            }
            
        } catch (Exception e) {
            throw new Exception("Setup card error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Step 2.5: Verify user PIN (REQUIRED before initUserData)
     * Hiện dùng dạng PLAINTEXT giống BookstoreClientTest.verifyPin()
     */
    public boolean verifyPin(String userPin) throws Exception {
        try {
            if (userPin == null || userPin.length() != 6) {
                throw new Exception("User PIN must be exactly 6 characters");
            }
            
            System.out.println("\n========== VERIFY PIN (STEP 2.5) ==========");
            System.out.println("Verifying User PIN: " + userPin);
            
            // Gửi PIN dạng PLAINTEXT giống BookstoreClientTest.verifyPin()
            byte[] pinData = userPin.getBytes();

            System.out.println("Sending VERIFY_PIN (PLAINTEXT), length = " + pinData.length + " bytes");

            // Send APDU (PLAINTEXT)
            ResponseAPDU response = channel.transmit(
                new CommandAPDU(CLA, INS_VERIFY_PIN, 0x00, 0x00, pinData)
            );
            
            int sw = response.getSW();
            System.out.println("Response SW: 0x" + String.format("%04X", sw));
            
            if (sw == 0x9000) {
                System.out.println("✓ PIN verification SUCCESS");
                System.out.println("==========================================\n");
                return true;
            } else {
                System.out.println("✗ PIN verification FAILED");
                System.out.println("==========================================\n");
                throw new Exception("PIN verification failed: " + String.format("0x%04X", sw));
            }
            
        } catch (Exception e) {
            throw new Exception("Verify PIN error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Step 3: Initialize user data on card
     * Full format (giống BookstoreClientTest.initUserDataExtended & applet hiện tại):
     *   [CardID 16]
     *   [Name 64]
     *   [DOB 16]
     *   [Phone 16]
     *   [Address 64]
     *   [RegDate 16]      --> Tổng phần text: 192 bytes
     *   [App PubKey 128]  --> Modulus 1024-bit
     * Tổng cộng: 320 bytes, gửi RAW (PLAINTEXT, KHÔNG mã hóa)
     */
    public boolean initUserData(String cardId, String userName, String dob, String phone, String address, String regDate) throws Exception {
        if (!keyManager.hasCardPublicKey() || !keyManager.hasAppKeyPair()) {
            throw new Exception("Card setup not completed. Call getPublicKey() and setupCard() first.");
        }
        
        try {
            // Prepare 320-byte payload (RAW, NOT ENCRYPTED)
            byte[] payload = new byte[320];
            int offset = 0;
            
            // 1. CardID (16 bytes) - at offset 0
            System.arraycopy(CryptoUtils.createFixedLengthData(cardId, 16), 0, payload, offset, 16);
            offset += 16;
            
            // 2. User Name (64 bytes) - at offset 16
            System.arraycopy(CryptoUtils.createFixedLengthData(userName, 64), 0, payload, offset, 64);
            offset += 64;
            
            // 3. DOB DDMMYYYY (16 bytes) - at offset 80
            System.arraycopy(CryptoUtils.createFixedLengthData(dob, 16), 0, payload, offset, 16);
            offset += 16;
            
            // 4. Phone (16 bytes) - at offset 96
            System.arraycopy(CryptoUtils.createFixedLengthData(phone, 16), 0, payload, offset, 16);
            offset += 16;
            
            // 5. Address (64 bytes) - at offset 112
            System.arraycopy(CryptoUtils.createFixedLengthData(address, 64), 0, payload, offset, 64);
            offset += 64;
            
            // 6. RegDate DDMMYYYY (16 bytes) - at offset 176
            System.arraycopy(CryptoUtils.createFixedLengthData(regDate, 16), 0, payload, offset, 16);
            offset += 16;
            
            // 7. App Public Key modulus (128 bytes for 1024-bit RSA) - at offset 192
            PublicKey appPubKey = keyManager.getAppKeyPair().getPublic();
            java.security.interfaces.RSAPublicKey rsaPubKey = (java.security.interfaces.RSAPublicKey) appPubKey;
            byte[] modulusBytes = rsaPubKey.getModulus().toByteArray();
            
            // Handle leading zero byte from BigInteger.toByteArray() (giống BookstoreClientTest)
            byte[] modulusFixed = new byte[128];
            if (modulusBytes.length == 129 && modulusBytes[0] == 0) {
                System.arraycopy(modulusBytes, 1, modulusFixed, 0, 128);
            } else if (modulusBytes.length == 128) {
                System.arraycopy(modulusBytes, 0, modulusFixed, 0, 128);
            } else {
                // Pad with zeros at beginning if modulus < 128 bytes
                int padLen = 128 - modulusBytes.length;
                System.arraycopy(modulusBytes, 0, modulusFixed, padLen, modulusBytes.length);
            }
            
            System.arraycopy(modulusFixed, 0, payload, offset, 128);
            
            // DEBUG: Show payload structure before sending
            System.out.println("\n========== INIT USER DATA ==========");
            System.out.println("Payload Structure (320 bytes):");
            System.out.println("  [0-15]   CardID (16): " + new String(Arrays.copyOfRange(payload, 0, 16)).trim());
            System.out.println("  [16-79]  Name (64): " + new String(Arrays.copyOfRange(payload, 16, 80)).trim());
            System.out.println("  [80-95]  DOB (16): " + new String(Arrays.copyOfRange(payload, 80, 96)).trim());
            System.out.println("  [96-111] Phone (16): " + new String(Arrays.copyOfRange(payload, 96, 112)).trim());
            System.out.println("  [112-175] Address (64): " + new String(Arrays.copyOfRange(payload, 112, 176)).trim());
            System.out.println("  [176-191] RegDate (16): " + new String(Arrays.copyOfRange(payload, 176, 192)).trim());
            System.out.println("  [192-319] App PubKey Modulus (128 bytes)");
            System.out.println("Total Payload: " + payload.length + " bytes");
            System.out.println("Sending raw payload (NOT encrypted)...");
            System.out.println("====================================\n");
            
            // Send raw payload directly (NOT encrypted)
            // Exactly like Main.java line 260: channel.transmit(new CommandAPDU(0x00, INS_INIT_DATA, 0x00, 0x00, payload));
            ResponseAPDU response = channel.transmit(new CommandAPDU(CLA, INS_INIT_DATA, 0x00, 0x00, payload));
            
            System.out.println("Init User Data Response SW: " + String.format("0x%04X", response.getSW()));
            
            if (response.getSW() != 0x9000) {
                throw new Exception("Init data failed: " + String.format("0x%04X", response.getSW()));
            }
            
            System.out.println(">>> User data initialized on card (SUCCESS)");
            return true;
            
        } catch (Exception e) {
            throw new Exception("Init user data error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the KeyManager instance
     */
    public CardKeyManager getKeyManager() {
        return keyManager;
    }
    
    /**
     * Change user PIN on card
     * Format: 6 bytes (old PIN) + 6 bytes (new PIN)
     * Hiện gửi PLAINTEXT giống BookstoreClientTest.changePin()
     */
    public boolean changePin(String oldPin, String newPin) throws Exception {
        try {
            // Validate PINs
            if (oldPin == null || newPin == null) {
                throw new Exception("PINs cannot be null");
            }
            if (oldPin.length() != 6 || newPin.length() != 6) {
                throw new Exception("PINs must be exactly 6 characters. Got oldPin length=" + oldPin.length() + 
                                  ", newPin length=" + newPin.length());
            }
            
            System.out.println("\n========== CHANGE PIN ==========");
            System.out.println("Old PIN: " + oldPin + " (length=" + oldPin.length() + ")");
            System.out.println("New PIN: " + newPin + " (length=" + newPin.length() + ")");
            
            // Prepare 12-byte payload (old PIN + new PIN) – PLAINTEXT
            byte[] payload = new byte[12];
            System.arraycopy(oldPin.getBytes(), 0, payload, 0, 6);
            System.arraycopy(newPin.getBytes(), 0, payload, 6, 6);
            
            System.out.println("Sending CHANGE_PIN (PLAINTEXT, 12 bytes)...");
            
            // INS_CHANGE_PIN = 0x25 giống BookstoreClientTest.changePin()
            byte INS_CHANGE_PIN = (byte) 0x25;
            ResponseAPDU response = channel.transmit(
                new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, payload)
            );
            
            int sw = response.getSW();
            System.out.println("Response SW: 0x" + String.format("%04X", sw));
            
            if (sw == 0x9000) {
                System.out.println("✓ Change PIN SUCCESS");
                System.out.println("================================\n");
                return true;
            } else {
                System.out.println("✗ Change PIN FAILED");
                System.out.println("================================\n");
                throw new Exception("Change PIN failed: " + String.format("0x%04X", sw));
            }
            
        } catch (Exception e) {
            throw new Exception("Change PIN error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get PIN tries from card (card status)
     * Instruction: 0x33
     * Returns: byte array with PIN tries count
     */
    public byte[] getPinTries() throws Exception {
        try {
            if (channel == null) {
                throw new Exception("Card not connected!");
            }

            System.out.println("Getting PIN Tries...");
            ResponseAPDU response = channel.transmit(new CommandAPDU(CLA, INS_GET_PIN_TRIES, 0x00, 0x00));

            if (response.getSW() != 0x9000) {
                System.out.println("Failed to get PIN Tries. SW: " + String.format("%04X", response.getSW()));
                return null;
            }

            byte[] data = response.getData();
            if (data.length < 1) {
                System.out.println("Error: Empty response data");
                return null;
            }

            // Get first byte (number of tries)
            byte tries = data[0];
            System.out.println("PIN Tries: " + tries);

            if (tries >= 3) {
                System.out.println(">>> CARD IS BLOCKED!");
            } else {
                System.out.println(">>> Remaining attempts: " + (3 - tries));
            }
            
            return data;

        } catch (Exception e) {
            throw new Exception("Get PIN tries error: " + e.getMessage(), e);
        }
    }
}
