package smartcard;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Manager for updating user information on smart card
 * Sends: Name (64) + DOB (16) + Phone (16) + Address (64) = 160 bytes
 */
public class CardUpdateManager {
    
    private CardChannel channel;
    
    // APDU command instructions
    private static final byte CLA = 0x00;
    private static final byte INS_UPDATE_INFO = (byte) 0x40;
    
    // Field sizes (must match card applet Constants.java)
    private static final int LEN_FULLNAME = 64;
    private static final int LEN_DOB = 16;
    private static final int LEN_PHONE = 16;
    private static final int LEN_ADDRESS = 64;
    private static final int TOTAL_UPDATE_SIZE = 160; // 64 + 16 + 16 + 64
    
    public CardUpdateManager(CardChannel channel) {
        this.channel = channel;
    }
    
    /**
     * Update user information on card
     * @param name Full name (will be padded/truncated to 64 bytes)
     * @param dob Date of birth in DDMMYYYY format (will be padded to 16 bytes)
     * @param phone Phone number (will be padded/truncated to 16 bytes)
     * @param address Address (will be padded/truncated to 64 bytes)
     * @return true if update successful
     * @throws Exception if update fails
     */
    public boolean updateInfo(String name, String dob, String phone, String address) throws Exception {
        System.out.println("[UPDATE_INFO] Preparing data to update card...");
        System.out.println("  Name   : " + name);
        System.out.println("  DOB    : " + dob);
        System.out.println("  Phone  : " + phone);
        System.out.println("  Address: " + address);
        
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Tên không được để trống!");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new Exception("Số điện thoại không được để trống!");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new Exception("Địa chỉ không được để trống!");
        }
        
        // Convert DOB from DD/MM/YYYY to DDMMYYYY if needed
        String dobFormatted = dob;
        if (dob != null && dob.contains("/")) {
            dobFormatted = dob.replace("/", "");
        }
        
        // Validate DOB format (should be 8 digits)
        if (dobFormatted != null && !dobFormatted.isEmpty()) {
            if (dobFormatted.length() != 8 || !dobFormatted.matches("\\d{8}")) {
                throw new Exception("Ngày sinh phải có định dạng DDMMYYYY (8 chữ số)!");
            }
        }
        
        // Build 160-byte data packet
        byte[] updateData = new byte[TOTAL_UPDATE_SIZE];
        int offset = 0;
        
        // 1. Name (64 bytes)
        byte[] nameBytes = padOrTruncate(name, LEN_FULLNAME);
        System.arraycopy(nameBytes, 0, updateData, offset, LEN_FULLNAME);
        offset += LEN_FULLNAME;
        
        // 2. DOB (16 bytes)
        byte[] dobBytes = padOrTruncate(dobFormatted != null ? dobFormatted : "", LEN_DOB);
        System.arraycopy(dobBytes, 0, updateData, offset, LEN_DOB);
        offset += LEN_DOB;
        
        // 3. Phone (16 bytes)
        byte[] phoneBytes = padOrTruncate(phone, LEN_PHONE);
        System.arraycopy(phoneBytes, 0, updateData, offset, LEN_PHONE);
        offset += LEN_PHONE;
        
        // 4. Address (64 bytes)
        byte[] addressBytes = padOrTruncate(address, LEN_ADDRESS);
        System.arraycopy(addressBytes, 0, updateData, offset, LEN_ADDRESS);
        
        System.out.println("[UPDATE_INFO] Sending " + updateData.length + " bytes to card...");
        logBytes("[UPDATE_INFO] Data preview", updateData, 0, 32);
        
        // Send UPDATE_INFO command
        long startTime = System.currentTimeMillis();
        ResponseAPDU response = channel.transmit(
            new CommandAPDU(CLA, INS_UPDATE_INFO, 0x00, 0x00, updateData)
        );
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("[UPDATE_INFO] Response SW: 0x" + 
            String.format("%04X", response.getSW()) + " (Time: " + duration + "ms)");
        
        if (response.getSW() == 0x9000) {
            System.out.println("[UPDATE_INFO] SUCCESS! Card information updated.");
            return true;
        } else {
            String errorMsg = getErrorMessage(response.getSW());
            System.out.println("[UPDATE_INFO] FAILED! " + errorMsg);
            throw new Exception("Cập nhật thông tin thất bại: " + errorMsg);
        }
    }
    
    /**
     * Pad string to specified length with spaces, or truncate if too long
     */
    private byte[] padOrTruncate(String str, int length) {
        byte[] result = new byte[length];
        Arrays.fill(result, (byte) 0x20); // Fill with spaces
        
        if (str != null && !str.isEmpty()) {
            byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
            int copyLen = Math.min(strBytes.length, length);
            System.arraycopy(strBytes, 0, result, 0, copyLen);
        }
        
        return result;
    }
    
    /**
     * Log bytes in hex format for debugging
     */
    private void logBytes(String prefix, byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder(prefix + ": ");
        int end = Math.min(offset + length, data.length);
        int start = Math.max(0, offset);
        for (int i = start; i < end; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        System.out.println(sb.toString());
    }
    
    /**
     * Get human-readable error message from status word
     */
    private String getErrorMessage(int sw) {
        switch (sw) {
            case 0x6300:
                return "Xác thực thất bại";
            case 0x6983:
                return "Thẻ bị khóa";
            case 0x6700:
                return "Độ dài dữ liệu không đúng";
            case 0x6A86:
                return "Tham số P1-P2 không đúng";
            case 0x6D00:
                return "Lệnh không được hỗ trợ";
            case 0x6E00:
                return "Class không được hỗ trợ";
            default:
                return String.format("Lỗi 0x%04X", sw);
        }
    }
}
