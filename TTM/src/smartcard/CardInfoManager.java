package smartcard;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
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

    // Expected plaintext response size (match BookstoreClientTest.getInfo)
    // Structure on card: [CardID 16][Name 64][DOB 16][Phone 16][Address 64][RegDate
    // 16][Rank 1] = 193 bytes
    private static final int EXPECTED_RESPONSE_SIZE = 193;

    public CardInfoManager(CardChannel channel, CardKeyManager keyManager) {
        this.channel = channel;
        this.keyManager = keyManager;
    }

    /**
     * Retrieve user information from card (PLAINTEXT)
     * Data format (plaintext, not encrypted):
     * [CardID 16][Name 64][DOB 16][Phone 16][Address 64][RegDate 16][Rank 16] = 208
     * bytes
     */
    public UserInfo getInfo() throws Exception {
        try {
            // Send GET_INFO command (PLAINTEXT), giống BookstoreClientTest.getInfo()
            ResponseAPDU response = channel.transmit(
                    new CommandAPDU(CLA, INS_GET_INFO, 0x00, 0x00, EXPECTED_RESPONSE_SIZE));

            if (response.getSW() != 0x9000) {
                throw new Exception("Get Info failed: " + String.format("0x%04X", response.getSW()));
            }

            byte[] plainData = response.getData();
            System.out.println("Received data length: " + plainData.length);

            if (plainData.length < EXPECTED_RESPONSE_SIZE) {
                throw new Exception("Invalid response: data too short. Expected at least "
                        + EXPECTED_RESPONSE_SIZE + " bytes, got " + plainData.length);
            }

            // Parse plaintext data
            UserInfo userInfo = parseUserData(plainData);
            System.out.println(">>> User info retrieved successfully");

            return userInfo;

        } catch (Exception e) {
            throw new Exception("Get info error: " + e.getMessage(), e);
        }
    }

    /**
     * Parse user data from plaintext
     * Full format from card:
     * [CardID 16][Name 64][DOB 16][Phone 16][Address 64][RegDate 16] = 192 bytes
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

        // Extract Phone (16 bytes)
        String phone = extractString(plainData, offset, 16);
        offset += 16;

        // Extract Address (64 bytes)
        String address = extractString(plainData, offset, 64);
        offset += 64;

        // Extract RegDate (16 bytes)
        String regDate = extractString(plainData, offset, 16);
        offset += 16;

        // Extract Rank (1 byte, sau RegDate)
        String rank = "Unknown";
        if (plainData.length >= 193) {
            byte type = plainData[192];
            switch (type) {
                case 0:
                    rank = "Normal";
                    break;
                case 1:
                    rank = "Silver";
                    break;
                case 2:
                    rank = "Gold";
                    break;
                case 3:
                    rank = "Diamond";
                    break;
                default:
                    rank = "Type " + type;
                    break;
            }
        }
        return new UserInfo(cardId, name, dob, phone, address, regDate, rank);
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
        public final String dob; // DDMMYYYY format
        public final String phone;
        public final String address;
        public final String regDate; // DDMMYYYY format
        public final String rank;

        public UserInfo(String cardId, String name, String dob, String phone, String address, String regDate,
                String rank) {
            this.cardId = cardId;
            this.name = name;
            this.dob = dob;
            this.phone = phone;
            this.address = address;
            this.regDate = regDate;
            this.rank = rank;
        }

        @Override
        public String toString() {
            return String.format(
                    "CardID : %s%n" +
                            "Name   : %s%n" +
                            "DOB    : %s%n" +
                            "Phone  : %s%n" +
                            "Address: %s%n" +
                            "RegDate: %s%n" +
                            "Rank   : %s",
                    cardId, name, dob, phone, address, regDate, rank);
        }
    }
}
