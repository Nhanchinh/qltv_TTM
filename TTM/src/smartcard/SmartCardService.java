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
 * Simple wrapper around javax.smartcardio so Swing panels can check whether a
 * Java Card applet is reachable. The implementation mirrors the sample flow in
 * the provided JCIDE/NetBeans document: connect → SELECT AID → send custom
 * command (CLA B0 / INS 00).
 */
public class SmartCardService {

    private static final Logger LOGGER = Logger.getLogger(SmartCardService.class.getName());

    // AID must match the value that was installed on the Java Card
    public static final byte[] AID_APPLET = {
        (byte) 0x11, (byte) 0x22, (byte) 0x33,
        (byte) 0x44, (byte) 0x55, (byte) 0x09
    };

    private static final byte IDCARD_CLA = (byte) 0xB0;
    private static final byte INS_PRINT = (byte) 0x11;
    private static final int DEMO_RESPONSE_LE = 64;

    private TerminalFactory factory;
    private CardTerminal terminal;
    private Card card;
    private CardChannel channel;
    private ResponseAPDU lastResponse;
    private byte[] lastCommand;

    /**
     * Attempts to connect to the first available terminal and select the target
     * applet. Returns the current card status so the UI can show ATR / SW codes.
     */
    public synchronized SmartCardStatus connect() throws SmartCardServiceException {
        ensureFactory();

        try {
            List<CardTerminal> terminals = factory.terminals().list();
            if (terminals.isEmpty()) {
                throw new SmartCardServiceException("Không tìm thấy đầu đọc (Terminal factory rỗng).");
            }

            // Prefer a terminal that already has a card present, fallback to the first terminal.
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

            card = terminal.connect("*");  // hoặc "T=0|T=1"
            channel = card.getBasicChannel();

            if (channel == null) {
                throw new SmartCardServiceException("Không tạo được kênh giao tiếp mặc định (basic channel).");
            }

            ResponseAPDU selectResponse = transmit(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_APPLET));
            SmartCardStatus status = buildStatus("Đã kết nối và SELECT applet", selectResponse);

            if (!status.isSuccess()) {
                throw new SmartCardServiceException("SELECT applet trả về SW khác 0x9000: " + status.getSw());
            }

            return status;
        } catch (CardException ex) {
            cleanup();
            throw new SmartCardServiceException("Lỗi kết nối đến thẻ: " + ex.getMessage(), ex);
        }
    }

    /**
     * Disconnects from the current card (if any) and returns a status that can
     * be displayed by the UI.
     */
    public synchronized SmartCardStatus disconnect() throws SmartCardServiceException {
        try {
            if (card != null) {
                card.disconnect(false);
            }
        } catch (CardException ex) {
            throw new SmartCardServiceException("Lỗi khi ngắt kết nối: " + ex.getMessage(), ex);
        } finally {
            cleanup();
        }
        return SmartCardStatus.disconnected("Đã ngắt kết nối khỏi thẻ.");
    }

    /**
     * Sends the PRINT command described in the document (CLA B0 / INS 00). The
     * returned status contains the raw response bytes so callers can decode
     * them however they need.
     */
    public synchronized SmartCardStatus readDemoText() throws SmartCardServiceException {
        ensureConnected();
        ResponseAPDU response = transmit(
            new CommandAPDU(IDCARD_CLA & 0xFF, INS_PRINT & 0xFF, 0x00, 0x00, DEMO_RESPONSE_LE));
        return buildStatus("Đã gửi lệnh PRINT", response);
    }

    /**
     * Lightweight ping: re-send SELECT without resetting the connection, useful
     * if the UI just wants to check whether the card is still responding.
     */
    public synchronized SmartCardStatus refreshStatus() throws SmartCardServiceException {
        ensureConnected();
        ResponseAPDU response = transmit(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID_APPLET));
        return buildStatus("SELECT applet lần nữa để kiểm tra trạng thái", response);
    }

    public synchronized boolean isConnected() {
        return card != null && channel != null;
    }

    public synchronized String getAtr() {
        if (card == null) {
            return "";
        }
        byte[] atrBytes = card.getATR().getBytes();
        return bytesToHex(atrBytes);
    }

    private void ensureFactory() throws SmartCardServiceException {
        if (factory == null) {
            factory = TerminalFactory.getDefault();
            if (factory == null) {
                throw new SmartCardServiceException("TerminalFactory mặc định trả về null.");
            }
        }
    }

    private void ensureConnected() throws SmartCardServiceException {
        if (!isConnected()) {
            throw new SmartCardServiceException("Chưa kết nối với thẻ. Hãy bấm \"Kết nối\" trước.");
        }
    }

    private ResponseAPDU transmit(CommandAPDU apdu) throws SmartCardServiceException {
        try {
            lastCommand = apdu.getBytes();
            lastResponse = channel.transmit(apdu);
            return lastResponse;
        } catch (CardException ex) {
            throw new SmartCardServiceException("Không thể gửi APDU: " + ex.getMessage(), ex);
        }
    }

    private void cleanup() {
        card = null;
        channel = null;
        lastCommand = null;
        lastResponse = null;
    }

    private SmartCardStatus buildStatus(String message, ResponseAPDU response) {
        String sw = response != null ? String.format("%04X", response.getSW() & 0xFFFF) : "----";
        String dataHex = response != null ? bytesToHex(response.getData()) : "";
        String atrHex = getAtr();
        return new SmartCardStatus(isConnected(), atrHex, sw, dataHex, message, bytesToHex(lastCommand));
    }

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
     * Immutable DTO describing the most recent card state.
     */
    public static class SmartCardStatus {
        private final boolean connected;
        private final String atr;
        private final String sw;
        private final String data;
        private final String message;
        private final String lastCommand;

        SmartCardStatus(boolean connected, String atr, String sw, String data, String message, String lastCommand) {
            this.connected = connected;
            this.atr = atr;
            this.sw = sw;
            this.data = data;
            this.message = message;
            this.lastCommand = lastCommand;
        }

        public static SmartCardStatus disconnected(String message) {
            return new SmartCardStatus(false, "", "", "", message, "");
        }

        public boolean isConnected() {
            return connected;
        }

        public boolean isSuccess() {
            return "9000".equals(sw);
        }

        public String getAtr() {
            return atr;
        }

        public String getSw() {
            return sw;
        }

        public String getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }

        public String getLastCommand() {
            return lastCommand;
        }
    }

    /**
     * Custom exception to keep javax.smartcardio leakage away from UI code.
     */
    public static class SmartCardServiceException extends Exception {
        public SmartCardServiceException(String message) {
            super(message);
        }

        public SmartCardServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

