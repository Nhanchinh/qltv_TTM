package smartcard;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Manages PIN operations on the smart card
 */
public class CardPinManager {

    private CardChannel channel;
    private CardKeyManager keyManager;

    // Đổi lại đúng INS cho reset PIN theo BookstoreClientTest (0x50)
    private static final byte INS_RESET_PIN = (byte) 0x50;
    private static final byte INS_CHANGE_PIN = (byte) 0x25;
    private static final byte CLA = 0x00;

    public CardPinManager(CardChannel channel) {
        this.channel = channel;
        this.keyManager = new CardKeyManager(channel);
    }

    /**
     * Reset user PIN using admin PIN
     * 
     * @param adminPin   The admin PIN (6 characters)
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

        System.out.println("\n========== RESET USER PIN ==========");
        System.out.println("Admin PIN: " + adminPin);
        System.out.println("New User PIN: " + newUserPin);

        // Prepare 12-byte data: adminPin (6) + newUserPin (6)
        byte[] resetData = new byte[12];
        System.arraycopy(adminPin.getBytes(), 0, resetData, 0, 6);
        System.arraycopy(newUserPin.getBytes(), 0, resetData, 6, 6);

        // Gửi lệnh PLAINTEXT giống BookstoreClientTest.resetUserPin()
        System.out.println("Sending Reset PIN Command (PLAINTEXT)...");
        ResponseAPDU response = channel.transmit(new CommandAPDU(CLA, INS_RESET_PIN, 0x00, 0x00, resetData));

        System.out.println("Response SW: " + Integer.toHexString(response.getSW()));
        if (response.getSW() == 0x9000) {
            System.out.println(">>> Reset PIN SUCCESS");
            return true;
        } else {
            System.out.println(">>> Reset PIN FAILED");
            throw new Exception("Reset PIN failed with SW: 0x" + Integer.toHexString(response.getSW()));
        }

    }


    /**
     * Update user PIN (User changing their own PIN)
     * 
     * @param oldPin The current user PIN
     * @param newPin The new user PIN
     * @return true if update successful
     * @throws Exception if update fails
     */
    public boolean updatePin(String oldPin, String newPin) throws Exception {
        if (oldPin == null || oldPin.length() != 6) {
            throw new Exception("Old PIN must be 6 characters");
        }
        if (newPin == null || newPin.length() != 6) {
            throw new Exception("New PIN must be 6 characters");
        }

        System.out.println("\n========== UPDATE USER PIN ==========");
        // Prepare 12-byte data: oldPin (6) + newPin (6)
        byte[] data = new byte[12];
        System.arraycopy(oldPin.getBytes(), 0, data, 0, 6);
        System.arraycopy(newPin.getBytes(), 0, data, 6, 6);

        System.out.println("Sending Update PIN Command (PLAINTEXT)...");
        ResponseAPDU response = channel.transmit(new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, data));

        if (response.getSW() == 0x9000) {
            System.out.println(">>> Update PIN SUCCESS");
            return true;
        } else {
            System.out.println(">>> Update PIN FAILED: " + Integer.toHexString(response.getSW()));
            throw new Exception("Update PIN failed. SW: 0x" + Integer.toHexString(response.getSW()));
        }
        
    }
}
