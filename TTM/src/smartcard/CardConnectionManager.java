package smartcard;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 * Manager class for handling smart card connection
 * Provides methods for connecting and disconnecting from a smart card
 */
public class CardConnectionManager {

    private static final Logger LOGGER = Logger.getLogger(CardConnectionManager.class.getName());

    private static final int MAX_CONNECT_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 800L;

    private TerminalFactory factory;
    private CardTerminal terminal;
    private Card card;
    private CardChannel channel;

    public CardConnectionManager() {
        this.factory = null;
        this.terminal = null;
        this.card = null;
        this.channel = null;
    }

    /**
     * Attempts to connect to the first available smart card terminal
     * 
     * @return true if connection successful, false otherwise
     * @throws Exception if connection fails
     */
    public boolean connectCard() throws Exception {
        return connectCardWithRetries(MAX_CONNECT_ATTEMPTS);
    }

    /**
     * Attempts to connect with limited retries.
     * 
     * @param maxAttempts Total attempts (including the first one)
     */
    public boolean connectCardWithRetries(int maxAttempts) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
            try {
                if (attempt > 1) {
                    LOGGER.info(String.format("Thử kết nối lại thẻ (%d/%d)...", attempt, maxAttempts));
                }
                boolean connected = performSingleConnectionAttempt();
                if (connected) {
                    return true;
                }
            } catch (Exception e) {
                lastException = e;
                cleanup();

                Level logLevel = (attempt == maxAttempts) ? Level.SEVERE : Level.WARNING;
                LOGGER.log(logLevel, "Lỗi kết nối thẻ (lần " + attempt + "/" + maxAttempts + "): " + e.getMessage(), e);

                if (attempt == maxAttempts) {
                    throw e;
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new Exception("Thread bị gián đoạn trong khi chờ kết nối lại", interruptedException);
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new Exception("Không thể kết nối thẻ sau nhiều lần thử.");
    }

    /**
     * Thực hiện một lần kết nối (không retry).
     */
    private boolean performSingleConnectionAttempt() throws Exception {
        // Initialize terminal factory
        if (factory == null) {
            factory = TerminalFactory.getDefault();
            if (factory == null) {
                throw new Exception("Terminal Factory mặc định trả về null");
            }
        }

        // Get list of available terminals
        List<CardTerminal> terminals = factory.terminals().list();
        if (terminals.isEmpty()) {
            throw new Exception("Không tìm thấy đầu đọc thẻ (Card terminal)!");
        }

        System.out.println("Số lượng terminal tìm được: " + terminals.size());

        // Prefer a terminal that already has a card present
        Optional<CardTerminal> withCard = terminals.stream()
                .filter(t -> {
                    try {
                        return t.isCardPresent();
                    } catch (CardException ex) {
                        LOGGER.log(Level.WARNING, "Không thể kiểm tra trạng thái thẻ cho terminal " + t, ex);
                        return false;
                    }
                })
                .findFirst();

        terminal = withCard.orElse(terminals.get(0));
        System.out.println("Kết nối tới: " + terminal.getName());

        // Try to connect with T=1 protocol first (Extended APDU support)
        try {
            card = terminal.connect("T=1");
            System.out.println("Đã kết nối với protocol: T=1 (Hỗ trợ Extended APDU)");
        } catch (CardException e) {
            System.out.println(
                    "Cảnh báo: T=1 không được hỗ trợ, thử kết nối với * (T=0 có thể không hỗ trợ Extended APDU)");
            card = terminal.connect("*");
        }

        // Get basic channel
        channel = card.getBasicChannel();
        if (channel == null) {
            throw new Exception("Không thể lấy basic channel!");
        }

        // Select applet using AID
        byte[] aid = hexStringToByteArray("11223344550300");
        ResponseAPDU response = channel.transmit(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid));

        System.out.println("SELECT Applet: " + String.format("%04X", response.getSW()));

        // Check if selection was successful
        if (response.getSW() != 0x9000) {
            channel = null;
            card = null;
            throw new Exception("SELECT Applet thất bại. SW: " + String.format("%04X", response.getSW()));
        }

        System.out.println(">>> Kết nối thành công!");
        return true;
    }

    /**
     * Disconnects from the smart card
     * 
     * @return true if disconnection successful
     */
    public boolean disconnectCard() {
        try {
            if (card != null) {
                card.disconnect(false);
                System.out.println(">>> Đã ngắt kết nối khỏi thẻ!");
            }
            return true;
        } catch (CardException e) {
            String msg = e.getMessage();
            // Ignore invalid handle/parameter errors as they indicate connection is already
            // gone
            if (msg != null && (msg.contains("WINDOWS_ERROR_INVALID_HANDLE") ||
                    msg.contains("SCARD_E_INVALID_HANDLE") ||
                    msg.contains("SCARD_E_INVALID_PARAMETER"))) {
                System.out.println(">>> Đã ngắt kết nối (Tự động - card previously disconnected)");
                return true;
            }
            LOGGER.log(Level.INFO, "Lỗi khi ngắt kết nối (có thể bỏ qua): " + e.getMessage());
            return false;
        } finally {
            cleanup();
        }
    }

    /**
     * Check if currently connected to a smart card
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return card != null && channel != null;
    }

    /**
     * Get ATR (Answer to Reset) of the current card
     * 
     * @return ATR string in hex format
     */
    public String getATR() {
        if (card == null) {
            return "";
        }
        byte[] atrBytes = card.getATR().getBytes();
        return bytesToHex(atrBytes);
    }

    /**
     * Get card channel
     * 
     * @return CardChannel for communication with card
     */
    public CardChannel getChannel() {
        return channel;
    }

    /**
     * Convert hex string to byte array
     * 
     * @param s hex string
     * @return byte array
     */
    private static byte[] hexStringToByteArray(String s) {
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
     * 
     * @param data byte array
     * @return hex string
     */
    private static String bytesToHex(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Clean up resources
     */
    private void cleanup() {
        card = null;
        channel = null;
        terminal = null;
    }
}
