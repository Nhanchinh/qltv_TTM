package smartcard;

import javax.smartcardio.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý số dư, điểm thưởng và sách mượn trên thẻ
 * Dựa trên Main.java reference
 */
public class CardBalanceManager {

    private static final byte INS_GET_BALANCE = (byte) 0x53;
    private static final byte INS_DEPOSIT = (byte) 0x54;
    private static final byte INS_PAYMENT = (byte) 0x55;
    private static final byte INS_BORROW_BOOK = (byte) 0x56;
    private static final byte INS_RETURN_BOOK = (byte) 0x57;
    private static final byte INS_GET_BORROWED_BOOKS = (byte) 0x58;
    private static final byte INS_ADD_POINT = (byte) 0x59;
    private static final byte INS_USE_POINT = (byte) 0x5A;
    private static final byte INS_UPGRADE_SILVER = (byte) 0x60;
    private static final byte INS_UPGRADE_GOLD = (byte) 0x61;
    private static final byte INS_UPGRADE_DIAMOND = (byte) 0x62;

    private CardChannel channel;

    public CardBalanceManager(CardChannel channel) {
        this.channel = channel;
    }

    /**
     * Kết quả lấy số dư và điểm
     */
    public static class BalanceInfo {
        public int balance; // Số dư (VNĐ)
        public int points; // Điểm thưởng
        public boolean success;
        public String message;

        @Override
        public String toString() {
            return "Balance: " + balance + " VND, Points: " + points;
        }
    }

    /**
     * Thông tin sách đang mượn
     */
    public static class BorrowedBook {
        public String bookId; // Mã sách (6 bytes)
        public String borrowDate; // Ngày mượn (DDMMYYYY)
        public int duration; // Số ngày mượn
        public int type; // Loại (0=normal, 1=promotion)

        @Override
        public String toString() {
            return "BookID: " + bookId + ", Date: " + borrowDate + ", Duration: " + duration + " days";
        }
    }

    /**
     * Lấy số dư và điểm từ thẻ
     * Response: 8 bytes (4 bytes Balance + 4 bytes Points)
     */
    public BalanceInfo getBalance() {
        BalanceInfo result = new BalanceInfo();
        result.success = false;

        try {
            System.out.println("[BALANCE] Getting Balance & Points...");

            // Expect 8 bytes: 4 bytes Balance + 4 bytes Points
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_GET_BALANCE, 0x00, 0x00, 8));

            System.out.println("[BALANCE] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                byte[] data = r.getData();

                if (data.length >= 8) {
                    result.balance = ByteBuffer.wrap(data, 0, 4).getInt();
                    result.points = ByteBuffer.wrap(data, 4, 4).getInt();
                    result.success = true;
                    result.message = "Lấy số dư thành công";

                    System.out.println("[BALANCE] Balance: " + result.balance + " VND");
                    System.out.println("[BALANCE] Points: " + result.points);
                } else {
                    result.message = "Dữ liệu không đủ: " + data.length + " bytes";
                    System.out.println("[BALANCE] Error: " + result.message);
                }
            } else {
                result.message = "Lỗi: SW=0x" + Integer.toHexString(r.getSW());
                System.out.println("[BALANCE] " + result.message);
            }
        } catch (Exception e) {
            result.message = "Exception: " + e.getMessage();
            System.err.println("[BALANCE] " + result.message);
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy danh sách sách đang mượn từ thẻ
     * Response: 240 bytes (15 slots * 16 bytes mỗi slot)
     * Mỗi slot: 6 bytes ID + 8 bytes Date + 1 byte Duration + 1 byte Type
     */
    public List<BorrowedBook> getBorrowedBooks() {
        List<BorrowedBook> books = new ArrayList<>();

        try {
            System.out.println("[BOOKSHELF] Fetching borrowed books...");

            // 15 slots * 16 bytes = 240 bytes
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_GET_BORROWED_BOOKS, 0x00, 0x00, 240));

            System.out.println("[BOOKSHELF] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                byte[] data = r.getData();
                System.out.println("[BOOKSHELF] Received " + data.length + " bytes");

                // Parse 15 slots
                for (int i = 0; i < 15; i++) {
                    int offset = i * 16;

                    // Check if slot is used (first byte of ID != 0)
                    if (offset < data.length && data[offset] != 0) {
                        BorrowedBook book = new BorrowedBook();

                        // Parse ID (6 bytes)
                        book.bookId = new String(data, offset, 6).trim();

                        // Parse Date (8 bytes) at offset + 6
                        if (offset + 14 <= data.length) {
                            book.borrowDate = new String(data, offset + 6, 8).trim();
                        }

                        // Parse Duration at offset + 14
                        if (offset + 15 <= data.length) {
                            book.duration = data[offset + 14] & 0xFF;
                        }

                        // Parse Type at offset + 15
                        if (offset + 16 <= data.length) {
                            book.type = data[offset + 15] & 0xFF;
                        }

                        books.add(book);
                        System.out.println("[BOOKSHELF] Book " + (i + 1) + ": " + book);
                    }
                }

                System.out.println("[BOOKSHELF] Total borrowed: " + books.size() + " books");
            } else {
                System.out.println("[BOOKSHELF] Failed. SW: 0x" + Integer.toHexString(r.getSW()));
            }
        } catch (Exception e) {
            System.err.println("[BOOKSHELF] Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Nạp tiền vào thẻ
     */
    public boolean deposit(int amount) {
        try {
            System.out.println("[DEPOSIT] Depositing " + amount + " VND...");

            byte[] data = ByteBuffer.allocate(4).putInt(amount).array();
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_DEPOSIT, 0x00, 0x00, data));

            System.out.println("[DEPOSIT] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[DEPOSIT] SUCCESS!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("[DEPOSIT] Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thanh toán từ thẻ
     */
    public boolean payment(int amount) {
        try {
            System.out.println("[PAYMENT] Paying " + amount + " VND...");

            byte[] data = ByteBuffer.allocate(4).putInt(amount).array();
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_PAYMENT, 0x00, 0x00, data));

            System.out.println("[PAYMENT] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[PAYMENT] SUCCESS!");
                return true;
            } else if (r.getSW() == 0x6300) {
                System.out.println("[PAYMENT] FAILED: Insufficient Balance!");
            }
        } catch (Exception e) {
            System.err.println("[PAYMENT] Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thêm điểm thưởng
     */
    public boolean addPoints(int points) {
        try {
            System.out.println("[POINTS] Adding " + points + " points...");

            byte[] data = ByteBuffer.allocate(4).putInt(points).array();
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_ADD_POINT, 0x00, 0x00, data));

            System.out.println("[POINTS] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[POINTS] SUCCESS!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("[POINTS] Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * Sử dụng điểm thưởng
     */
    public boolean usePoints(int points) {
        try {
            System.out.println("[POINTS] Using " + points + " points...");

            byte[] data = ByteBuffer.allocate(4).putInt(points).array();
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_USE_POINT, 0x00, 0x00, data));

            System.out.println("[POINTS] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[POINTS] SUCCESS!");
                return true;
            } else if (r.getSW() == 0x6300) {
                System.out.println("[POINTS] FAILED: Not enough points!");
            }
        } catch (Exception e) {
            System.err.println("[POINTS] Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * Nâng cấp hạng thành viên trên thẻ
     * 
     * @param rank "Silver", "Gold", hoặc "Diamond"
     * @return true nếu thành công
     */
    public boolean upgradeRank(String rank) {
        try {
            byte insCode;
            switch (rank) {
                case "Silver":
                    insCode = INS_UPGRADE_SILVER;
                    break;
                case "Gold":
                    insCode = INS_UPGRADE_GOLD;
                    break;
                case "Diamond":
                    insCode = INS_UPGRADE_DIAMOND;
                    break;
                default:
                    System.err.println("[UPGRADE] Invalid rank: " + rank);
                    return false;
            }

            System.out.println("[UPGRADE] Upgrading to " + rank + "...");
            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, insCode, 0x00, 0x00));

            System.out.println("[UPGRADE] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[UPGRADE] SUCCESS! You are now a " + rank + " member!");
                return true;
            } else {
                System.err.println("[UPGRADE] FAILED. SW: 0x" + Integer.toHexString(r.getSW()));
            }
        } catch (Exception e) {
            System.err.println("[UPGRADE] Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mượn sách - lưu vào thẻ chip
     * 
     * @param bookId   Mã sách (max 6 ký tự)
     * @param duration Số ngày mượn
     * @param type     Loại (0=normal, 1=free promotion)
     * @return true nếu thành công
     */
    public boolean borrowBook(String bookId, int duration, int type) {
        try {
            System.out.println("[BORROW] Borrowing book: " + bookId + ", duration: " + duration + " days");

            // Chuẩn bị bookId 6 bytes
            byte[] bookIdBytes = new byte[6];
            byte[] inputBytes = bookId.getBytes();
            int copyLen = Math.min(inputBytes.length, 6);
            System.arraycopy(inputBytes, 0, bookIdBytes, 0, copyLen);

            // Ngày mượn hiện tại (DDMMYYYY)
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
            String dateStr = today.format(formatter);
            byte[] dateBytes = dateStr.getBytes();

            // Giới hạn duration và type
            if (duration > 255)
                duration = 255;
            if (type > 255)
                type = 255;

            // Build payload: 6 ID + 8 Date + 1 Duration + 1 Type = 16 bytes
            byte[] payload = new byte[16];
            System.arraycopy(bookIdBytes, 0, payload, 0, 6);
            System.arraycopy(dateBytes, 0, payload, 6, 8);
            payload[14] = (byte) duration;
            payload[15] = (byte) type;

            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_BORROW_BOOK, 0x00, 0x00, payload));

            System.out.println("[BORROW] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[BORROW] SUCCESS!");
                return true;
            } else if (r.getSW() == 0x6300) {
                System.err.println("[BORROW] FAILED: Book already borrowed or invalid.");
            } else if (r.getSW() == 0x6A84) {
                System.err.println("[BORROW] FAILED: Max books reached (15).");
            } else {
                System.err.println("[BORROW] FAILED. SW: 0x" + Integer.toHexString(r.getSW()));
            }
        } catch (Exception e) {
            System.err.println("[BORROW] Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * Trả sách - xóa khỏi thẻ chip
     * 
     * @param bookId Mã sách
     * @return true nếu thành công
     */
    public boolean returnBook(String bookId) {
        try {
            System.out.println("[RETURN] Returning book: " + bookId);

            // Chuẩn bị bookId 6 bytes
            byte[] bookIdBytes = new byte[6];
            byte[] inputBytes = bookId.getBytes();
            int copyLen = Math.min(inputBytes.length, 6);
            System.arraycopy(inputBytes, 0, bookIdBytes, 0, copyLen);

            ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, INS_RETURN_BOOK, 0x00, 0x00, bookIdBytes));

            System.out.println("[RETURN] Response SW: 0x" + Integer.toHexString(r.getSW()));

            if (r.getSW() == 0x9000) {
                System.out.println("[RETURN] SUCCESS!");
                return true;
            } else {
                System.err.println("[RETURN] FAILED (Book not found?). SW: 0x" + Integer.toHexString(r.getSW()));
            }
        } catch (Exception e) {
            System.err.println("[RETURN] Exception: " + e.getMessage());
        }
        return false;
    }
}