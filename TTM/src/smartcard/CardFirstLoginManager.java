package smartcard;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Manages First Login status operations
 */
public class CardFirstLoginManager {

    private CardChannel channel;

    // INS constants
    private static final byte INS_CHECK_FIRST_LOGIN = (byte) 0x27;
    private static final byte INS_DISABLE_FIRST_LOGIN = (byte) 0x28;
    private static final byte CLA = (byte) 0x00;

    public CardFirstLoginManager(CardChannel channel) {
        this.channel = channel;
    }

    /**
     * Check if this is the first login
     * 
     * @return true if first login (status byte == 1), false otherwise
     * @throws Exception if command fails
     */
    public boolean isFirstLogin() throws Exception {
        System.out.println("Checking First Login Status...");
        ResponseAPDU r = channel.transmit(new CommandAPDU(CLA, INS_CHECK_FIRST_LOGIN, 0x00, 0x00));

        System.out.println("Response SW: " + Integer.toHexString(r.getSW()));

        if (r.getSW() == 0x9000) {
            byte[] data = r.getData();
            if (data.length > 0) {
                byte status = data[0];
                System.out.println(">>> First Login: " + (status == 1 ? "YES" : "NO"));
                return status == 1;
            }
        }

        throw new Exception("Failed to check first login status. SW: " + Integer.toHexString(r.getSW()));
    }

    /**
     * Disable first login flag
     * 
     * @return true if successful
     * @throws Exception if command fails
     */
    public boolean disableFirstLogin() throws Exception {
        System.out.println("Disabling First Login Status...");
        ResponseAPDU r = channel.transmit(new CommandAPDU(CLA, INS_DISABLE_FIRST_LOGIN, 0x00, 0x00));

        System.out.println("Response SW: " + Integer.toHexString(r.getSW()));

        if (r.getSW() == 0x9000) {
            System.out.println(">>> DISABLE SUCCESS");
            return true;
        } else {
            throw new Exception("Failed to disable first login. SW: " + Integer.toHexString(r.getSW()));
        }
    }
}
