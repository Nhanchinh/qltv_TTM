/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BookService;
import services.BorrowService;
import services.CardService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import ui.DBConnect;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Panel Thuê/Trả sách với tích hợp Smart Card
 * Hỗ trợ thuê nhiều sách cùng lúc
 * 
 * @author admin
 */
public class muontra extends javax.swing.JPanel {

    private BookService bookService;
    private BorrowService borrowService;
    private CardService cardService;
    private String currentCardId = "CARD001";

    // Giỏ thuê sách (để thuê nhiều quyển cùng lúc)
    private List<BorrowCartItem> borrowCart = new ArrayList<>();

    // Hằng số theo hạng thành viên
    private static final int MAX_BOOKS_NORMAL = 3;
    private static final int MAX_BOOKS_SILVER = 5;
    private static final int MAX_BOOKS_GOLD = 10;
    private static final int MAX_BOOKS_DIAMOND = 15;

    private static final int FREE_BORROWS_NORMAL = 1;
    private static final int FREE_BORROWS_SILVER = 3;
    private static final int FREE_BORROWS_GOLD = 5;
    private static final int FREE_BORROWS_DIAMOND = 10;

    // Phí thuê và phạt
    private static final int RENTAL_FEE_PER_DAY = 1000; // 1,000đ/ngày
    private static final int LATE_FEE_PER_DAY = 5000; // 5,000đ/ngày trễ hạn
    private static final int FREE_DURATION_DAYS = 14; // Lượt FREE: 14 ngày đầu miễn phí

    // Biến lưu thông tin thành viên
    private String currentMemberType = "Normal";
    private int maxBooksAllowed = MAX_BOOKS_NORMAL;
    private int freeBorrowsPerMonth = FREE_BORROWS_NORMAL;
    private int currentBorrowedCount = 0;
    private int usedFreeBorrowsThisMonth = 0;

    // Class đại diện cho item trong giỏ mượn
    private static class BorrowCartItem {
        String bookId;
        String title;
        double price;
        int days;
        boolean useFreeSlot;

        BorrowCartItem(String bookId, String title, double price, int days, boolean useFreeSlot) {
            this.bookId = bookId;
            this.title = title;
            this.price = price;
            this.days = days;
            this.useFreeSlot = useFreeSlot;
        }

        /**
         * Tính phí thuê dựa trên lượt FREE:
         * - CÓ lượt FREE + ≤14 ngày: phí = 0đ
         * - CÓ lượt FREE + >14 ngày: phí = (ngày - 14) × 1,000đ
         * - HẾT lượt FREE: phí = ngày × 1,000đ
         */
        int getRentalFee() {
            if (useFreeSlot) {
                // Có lượt FREE: 14 ngày đầu miễn phí
                if (days <= FREE_DURATION_DAYS) {
                    return 0;
                } else {
                    return (days - FREE_DURATION_DAYS) * RENTAL_FEE_PER_DAY;
                }
            } else {
                // Hết lượt FREE: tính phí từ ngày 1
                return days * RENTAL_FEE_PER_DAY;
            }
        }

        int getTotalCost() {
            return (int) price + getRentalFee();
        }
    }

    /**
     * Creates new form BorrowPanel
     */
    public muontra() {
        bookService = new BookService();
        borrowService = new BorrowService();
        cardService = new CardService();
        initComponents();
        loadMemberInfo();
        loadAvailableBooks();
        loadBorrowedBooks();
    }

    /**
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            if (cardIdField != null) {
                cardIdField.setText(cardId);
            }
            loadMemberInfo();
            loadAvailableBooks();
            loadBorrowedBooks();
        }
    }

    /**
     * Load thông tin thành viên và đếm lượt free đã dùng trong tháng từ DB
     */
    private void loadMemberInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null && card.memberType != null) {
            String memberType = card.memberType;

            // Xác định hạng và giới hạn
            if (memberType.equalsIgnoreCase("Normal") || memberType.equalsIgnoreCase("ThanhVien")) {
                currentMemberType = "Normal";
                maxBooksAllowed = MAX_BOOKS_NORMAL;
                freeBorrowsPerMonth = FREE_BORROWS_NORMAL;
            } else if (memberType.equalsIgnoreCase("Silver") || memberType.equalsIgnoreCase("Bac")) {
                currentMemberType = "Silver";
                maxBooksAllowed = MAX_BOOKS_SILVER;
                freeBorrowsPerMonth = FREE_BORROWS_SILVER;
            } else if (memberType.equalsIgnoreCase("Gold") || memberType.equalsIgnoreCase("Vang")) {
                currentMemberType = "Gold";
                maxBooksAllowed = MAX_BOOKS_GOLD;
                freeBorrowsPerMonth = FREE_BORROWS_GOLD;
            } else if (memberType.equalsIgnoreCase("Diamond") || memberType.equalsIgnoreCase("KimCuong")) {
                currentMemberType = "Diamond";
                maxBooksAllowed = MAX_BOOKS_DIAMOND;
                freeBorrowsPerMonth = FREE_BORROWS_DIAMOND;
            }
        }

        // Đếm số sách đang mượn từ DB (Status = 'mượn')
        currentBorrowedCount = countBorrowedBooksFromDB();

        // Đếm lượt free đã dùng trong tháng hiện tại từ DB
        usedFreeBorrowsThisMonth = countFreeBorrowsThisMonth();

        // Cập nhật UI hiển thị thông tin
        if (memberInfoLabel != null) {
            memberInfoLabel.setText(String.format("Hạng: %s | Đang mượn: %d/%d | Lượt free: %d/%d",
                    currentMemberType, currentBorrowedCount, maxBooksAllowed,
                    Math.max(0, freeBorrowsPerMonth - usedFreeBorrowsThisMonth), freeBorrowsPerMonth));
        }
    }

    /**
     * Đếm số sách đang mượn từ DB
     */
    private int countBorrowedBooksFromDB() {
        String sql = "SELECT COUNT(*) FROM BorrowHistory WHERE CardID = ? AND Status = 'mượn'";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentCardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MUONTRA] Error counting borrowed books: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số lượt mượn FREE đã sử dụng trong tháng hiện tại
     * Dựa trên cột UsedFreeSlot = 1
     */
    private int countFreeBorrowsThisMonth() {
        // Lấy ngày đầu và cuối tháng hiện tại
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startOfMonth = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        String startOfNextMonth = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

        // Đếm số lượt đã dùng FREE trong tháng này (UsedFreeSlot = 1)
        String sql = "SELECT COUNT(*) FROM BorrowHistory WHERE CardID = ? " +
                "AND BorrowDate >= ? AND BorrowDate < ? " +
                "AND UsedFreeSlot = 1";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentCardId);
            pstmt.setString(2, startOfMonth);
            pstmt.setString(3, startOfNextMonth);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MUONTRA] Error counting free borrows: " + e.getMessage());
        }
        return 0;
    }

    private void loadAvailableBooks() {
        List<BookService.Book> books = bookService.getAllBooks();
        String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Giá", "SL có sẵn" };
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        Object[][] data = new Object[books.size()][5];
        for (int i = 0; i < books.size(); i++) {
            BookService.Book book = books.get(i);
            // Số lượng có sẵn = Stock - BorrowStock (số đang mượn)
            int available = book.stock - book.borrowStock;
            data[i][0] = book.bookId;
            data[i][1] = book.title;
            data[i][2] = book.author;
            data[i][3] = nf.format(book.price) + " đ";
            data[i][4] = available > 0 ? available : "Hết";
        }
        booksTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    /**
     * Load sách đang mượn từ DB
     */
    private void loadBorrowedBooks() {
        List<BorrowService.BorrowRecord> records = borrowService.getBorrowedBooksByCard(currentCardId);
        // Cột: Chọn, ID, Mã sách, Tên sách, Ngày mượn, Hạn trả, Số ngày, Phí thuê,
        // Trạng thái, Tiền cọc, Phí phạt
        String[] columns = { "Chọn", "ID", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Số ngày", "Phí thuê",
                "Trạng thái", "Tiền cọc", "Phí phạt" };
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        Object[][] data = new Object[records.size()][11];

        for (int i = 0; i < records.size(); i++) {
            BorrowService.BorrowRecord record = records.get(i);
            BookService.Book book = bookService.getBookById(record.bookId);

            data[i][0] = false; // Checkbox unchecked by default
            data[i][1] = record.id;
            data[i][2] = record.bookId;
            data[i][3] = book != null ? book.title : record.bookId;

            // Format dates và tính phí
            try {
                Date borrowDate = inputFormat.parse(record.borrowDate);
                Date dueDate = inputFormat.parse(record.dueDate);
                data[i][4] = outputFormat.format(borrowDate);
                data[i][5] = outputFormat.format(dueDate);

                long borrowDays = (dueDate.getTime() - borrowDate.getTime()) / (1000 * 60 * 60 * 24);
                data[i][6] = borrowDays + " ngày";

                int rentalFee = 0;
                if (borrowDays > FREE_DURATION_DAYS) {
                    rentalFee = (int) ((borrowDays - FREE_DURATION_DAYS) * RENTAL_FEE_PER_DAY);
                    data[i][7] = nf.format(rentalFee) + " đ";
                } else {
                    data[i][7] = "Miễn phí";
                }

                Date today = new Date();
                int lateFee = 0;
                if (today.after(dueDate)) {
                    long lateDays = (today.getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24);
                    lateFee = (int) (lateDays * LATE_FEE_PER_DAY);
                    if (book != null && lateFee >= book.price) {
                        data[i][8] = "⚠️ MẤT SÁCH";
                        lateFee = (int) book.price;
                    } else {
                        data[i][8] = "TRỄ " + lateDays + " ngày";
                    }
                } else {
                    data[i][8] = "Đang mượn";
                }
                data[i][10] = lateFee > 0 ? nf.format(lateFee) + " đ" : "0 đ";
            } catch (Exception e) {
                data[i][4] = record.borrowDate;
                data[i][5] = record.dueDate;
                data[i][6] = "--";
                data[i][7] = "--";
                data[i][8] = "Đang mượn";
                data[i][10] = "0 đ";
            }

            data[i][9] = book != null ? nf.format(book.price) + " đ" : "--";
        }

        // Preserve TableModelListener
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        borrowedTable.setModel(model);
        borrowedTable.getModel().addTableModelListener(e -> updateReturnInfo());

        // Căn chỉnh độ rộng cột checkbox
        borrowedTable.getColumnModel().getColumn(0).setMaxWidth(50);

        updateReturnInfo();
        loadMemberInfo();
    }

    private void searchBook() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập mã sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        BookService.Book book = bookService.getBookById(bookId);
        if (book == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách với mã: " + bookId, "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } else {
            // Highlight in table
            for (int i = 0; i < booksTable.getRowCount(); i++) {
                if (booksTable.getValueAt(i, 0).toString().equals(bookId)) {
                    booksTable.setRowSelectionInterval(i, i);
                    booksTable.scrollRectToVisible(booksTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    /**
     * Thêm sách vào giỏ mượn
     */
    private void addToCart() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra đã có trong giỏ chưa
        for (BorrowCartItem item : borrowCart) {
            if (item.bookId.equals(bookId)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Sách đã có trong giỏ!", "Thông báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Kiểm tra giới hạn mượn
        if (currentBorrowedCount + borrowCart.size() >= maxBooksAllowed) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Đã đạt giới hạn " + maxBooksAllowed + " quyển (hạng " + currentMemberType + ")!",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookService.Book book = bookService.getBookById(bookId);
        if (book == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra còn sách không
        int available = book.stock - book.borrowStock;
        if (available <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Sách đã hết!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int days = (Integer) daysSpinner.getValue();

        // Kiểm tra còn lượt free không
        int usedFreeInCart = 0;
        for (BorrowCartItem item : borrowCart) {
            if (item.useFreeSlot)
                usedFreeInCart++;
        }
        boolean canUseFree = (usedFreeBorrowsThisMonth + usedFreeInCart) < freeBorrowsPerMonth;

        borrowCart.add(new BorrowCartItem(bookId, book.title, book.price, days, canUseFree));
        updateCartTable();
        bookIdField.setText("");
    }

    /**
     * Xóa sách khỏi giỏ
     */
    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < borrowCart.size()) {
            borrowCart.remove(row);
            // Tính lại FREE slots cho các item còn lại
            recalculateFreeSlots();
            updateCartTable();
        }
    }

    /**
     * Tính lại FREE slots cho toàn bộ giỏ hàng
     * Đảm bảo các item đầu tiên được ưu tiên dùng FREE
     */
    private void recalculateFreeSlots() {
        // Tính số lượt FREE còn lại
        int freeRemaining = freeBorrowsPerMonth - usedFreeBorrowsThisMonth;

        // Reset tất cả về không FREE
        for (BorrowCartItem item : borrowCart) {
            item.useFreeSlot = false;
        }

        // Gán FREE cho các item đầu tiên (trong giới hạn còn lại)
        int freeAssigned = 0;
        for (BorrowCartItem item : borrowCart) {
            if (freeAssigned < freeRemaining) {
                item.useFreeSlot = true;
                freeAssigned++;
            } else {
                break;
            }
        }
    }

    /**
     * Xóa toàn bộ giỏ
     */
    private void clearCart() {
        borrowCart.clear();
        updateCartTable();
    }

    /**
     * Cập nhật bảng giỏ mượn
     * Hiển thị rõ lượt FREE và phí thuê
     */
    private void updateCartTable() {
        String[] columns = { "Mã sách", "Tên sách", "Số ngày", "Tiền cọc", "Phí thuê", "Tổng" };
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        Object[][] data = new Object[borrowCart.size()][6];
        int grandTotal = 0;

        for (int i = 0; i < borrowCart.size(); i++) {
            BorrowCartItem item = borrowCart.get(i);
            data[i][0] = item.bookId;
            data[i][1] = item.title;

            // Hiển thị số ngày và trạng thái FREE
            if (item.useFreeSlot) {
                data[i][2] = item.days + " ngày FREE";
            } else {
                data[i][2] = item.days + " ngày";
            }

            data[i][3] = nf.format(item.price) + " đ"; // Tiền cọc

            // Hiển thị phí thuê
            int rentalFee = item.getRentalFee();
            if (rentalFee == 0) {
                if (item.useFreeSlot) {
                    data[i][4] = "FREE (14 ngày)";
                } else {
                    data[i][4] = "0 đ";
                }
            } else {
                data[i][4] = nf.format(rentalFee) + " đ";
            }

            data[i][5] = nf.format(item.getTotalCost()) + " đ";
            grandTotal += item.getTotalCost();
        }

        cartTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
        cartTotalLabel.setText("Tổng: " + nf.format(grandTotal) + " đ");
    }

    /**
     * Thuê tất cả sách trong giỏ
     */
    private void borrowAllBooks() {
        if (borrowCart.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giỏ thuê trống!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tính tổng tiền
        int totalAmount = 0;
        for (BorrowCartItem item : borrowCart) {
            totalAmount += item.getTotalCost();
        }

        // Kiểm tra số dư thẻ
        int cardBalance = 0;
        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                if (info.success) {
                    cardBalance = info.balance;
                }
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error reading balance: " + e.getMessage());
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        if (cardBalance < totalAmount) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Số dư thẻ không đủ!\nSố dư: " + nf.format(cardBalance) + " đ\nCần: " + nf.format(totalAmount)
                            + " đ",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xác nhận
        StringBuilder sb = new StringBuilder("Xác nhận thuê " + borrowCart.size() + " quyển sách?\n\n");
        for (BorrowCartItem item : borrowCart) {
            sb.append("• ").append(item.title).append(" (").append(item.days).append(" ngày)");
            if (item.useFreeSlot) {
                sb.append(" ✓FREE");
            }
            sb.append("\n");
        }
        sb.append("\nTổng tiền: ").append(nf.format(totalAmount)).append(" đ");

        int option = javax.swing.JOptionPane.showConfirmDialog(this, sb.toString(), "Xác nhận thuê sách",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Thực hiện thuê sách TRƯỚC (không thanh toán trước)
        // Chỉ thanh toán khi thuê thành công
        boolean success = false;
        int successCount = 0;
        int paidAmount = 0;
        List<BorrowCartItem> successItems = new ArrayList<>();

        CardConnectionManager connManager = null;
        try {
            connManager = CardConnectionManager.getInstance();
            if (!connManager.connectCard()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối thẻ!", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

            // 1. Thử mượn từng sách vào thẻ trước
            boolean needAutoUpgrade = false;
            int booksOnCardCount = 0;

            for (BorrowCartItem item : borrowCart) {
                int bookType = item.useFreeSlot ? 1 : 0;
                System.out.println("[MUONTRA] Trying to borrow: " + item.bookId + " (days=" + item.days + ", type="
                        + bookType + ")");

                boolean borrowOk = balanceManager.borrowBook(item.bookId, item.days, bookType);

                if (borrowOk) {
                    successItems.add(item);
                    successCount++;
                    System.out.println("[MUONTRA] Borrow OK: " + item.bookId);
                } else {
                    System.err.println("[MUONTRA] Borrow FAILED: " + item.bookId);

                    // Log danh sách sách đang có trong thẻ để debug
                    System.out.println("[MUONTRA] === DEBUG: Books currently on card ===");
                    try {
                        List<CardBalanceManager.BorrowedBook> booksOnCard = balanceManager.getBorrowedBooks();
                        booksOnCardCount = booksOnCard.size();
                        if (booksOnCard.isEmpty()) {
                            System.out.println("[MUONTRA] Card is empty (no borrowed books)");
                        } else {
                            System.out.println("[MUONTRA] Total books on card: " + booksOnCard.size());
                            for (int i = 0; i < booksOnCard.size(); i++) {
                                CardBalanceManager.BorrowedBook book = booksOnCard.get(i);
                                System.out.println("[MUONTRA]   " + (i + 1) + ". BookID: " + book.bookId +
                                        ", BorrowDate: " + book.borrowDate +
                                        ", Duration: " + book.duration + " days");
                            }
                        }

                        // Kiểm tra xem có phải thẻ cần upgrade không
                        // Nếu số sách hiện tại < giới hạn theo hạng DB thì có thể thẻ chưa được upgrade
                        if (booksOnCardCount < maxBooksAllowed) {
                            System.out.println("[MUONTRA] Card has " + booksOnCardCount + " books but DB says max is "
                                    + maxBooksAllowed);
                            System.out.println("[MUONTRA] Card rank may not be updated! Will try to auto-upgrade...");
                            needAutoUpgrade = true;
                        }
                    } catch (Exception ex) {
                        System.err.println("[MUONTRA] Cannot get books from card: " + ex.getMessage());
                    }
                    System.out.println("[MUONTRA] ===================================");

                    // Nếu cần auto-upgrade, thực hiện ngay
                    if (needAutoUpgrade && successCount == 0) {
                        System.out.println("[MUONTRA] === AUTO-UPGRADE MODE ===");
                        String cardRank = "Normal";
                        if (currentMemberType.equals("Silver")) {
                            cardRank = "Silver";
                        } else if (currentMemberType.equals("Gold")) {
                            cardRank = "Gold";
                        } else if (currentMemberType.equals("Diamond")) {
                            cardRank = "Diamond";
                        }

                        if (!cardRank.equals("Normal")) {
                            System.out.println("[MUONTRA] Attempting to upgrade card to: " + cardRank);
                            boolean upgraded = balanceManager.upgradeRank(cardRank);

                            if (upgraded) {
                                System.out.println("[MUONTRA] Auto-upgrade SUCCESS! Retrying borrow...");

                                // Retry borrow after upgrade
                                borrowOk = balanceManager.borrowBook(item.bookId, item.days, bookType);
                                if (borrowOk) {
                                    successItems.add(item);
                                    successCount++;
                                    System.out.println("[MUONTRA] Retry borrow OK: " + item.bookId);
                                    needAutoUpgrade = false; // Đã fix, không cần upgrade tiếp
                                } else {
                                    System.err.println("[MUONTRA] Retry borrow still FAILED: " + item.bookId);
                                }
                            } else {
                                System.err.println("[MUONTRA] Auto-upgrade FAILED for rank: " + cardRank);
                            }
                        }
                        System.out.println("[MUONTRA] ===========================");
                    }
                }
            }

            // 2. Nếu có sách mượn được, tính tiền và thanh toán
            if (successCount > 0) {
                // Tính tiền cho các sách đã mượn được
                for (BorrowCartItem item : successItems) {
                    paidAmount += item.getTotalCost();
                }

                // Thanh toán
                boolean paymentOk = balanceManager.payment(paidAmount);
                if (paymentOk) {
                    // Lưu vào DB
                    for (BorrowCartItem item : successItems) {
                        borrowService.borrowBook(currentCardId, item.bookId, item.days, item.useFreeSlot);

                        // Cập nhật BorrowStock trong DB (tăng lên 1)
                        BookService.Book book = bookService.getBookById(item.bookId);
                        if (book != null) {
                            bookService.updateBorrowStock(item.bookId, book.borrowStock + 1);
                        }
                    }
                    success = true;
                } else {
                    // Thanh toán thất bại - cần rollback các sách đã mượn trên thẻ
                    System.err.println("[MUONTRA] Payment failed! Books borrowed on card but not in DB.");
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Thanh toán thất bại! Có thể có lỗi với thẻ.\nVui lòng liên hệ quản trị viên.",
                            "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }

            connManager.disconnectCard();

        } catch (Exception e) {
            System.err.println("[MUONTRA] Error borrowing: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Lỗi khi mượn sách: " + e.getMessage(),
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (connManager != null)
                    connManager.disconnectCard();
            } catch (Exception ignored) {
            }
        }

        // Hiển thị kết quả
        if (success) {
            if (successCount == borrowCart.size()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Thuê sách thành công!\nĐã thanh toán: " + nf.format(paidAmount) + " đ",
                        "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Thuê thành công " + successCount + "/" + borrowCart.size() + " quyển!\n" +
                                "Đã thanh toán: " + nf.format(paidAmount) + " đ\n\n" +
                                "Lưu ý: Một số sách không thuê được, có thể do thẻ đã đầy.",
                        "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
            borrowCart.clear();
            updateCartTable();
            loadMemberInfo();
            loadAvailableBooks();
            loadBorrowedBooks();
        } else if (successCount == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không thể thuê sách!\n\n" +
                            "Nguyên nhân có thể:\n" +
                            "• Thẻ đã thuê tối đa số sách cho phép\n" +
                            "• Lỗi kết nối với thẻ\n" +
                            "• Thẻ đã bị khóa",
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cập nhật thông tin khi chọn sách để trả
     */
    /**
     * Cập nhật thông tin khi chọn sách để trả (hỗ trợ nhiều sách qua checkbox)
     */
    private void updateReturnInfo() {
        List<Integer> checkedRows = new ArrayList<>();
        for (int i = 0; i < borrowedTable.getRowCount(); i++) {
            if ((Boolean) borrowedTable.getValueAt(i, 0)) {
                checkedRows.add(i);
            }
        }

        if (checkedRows.isEmpty()) {
            returnInfoLabel.setText("Chọn sách để trả (tích vào ô vuông bên trái)");
            return;
        }

        long totalRefund = 0;
        long totalDeposit = 0;
        long totalFine = 0;

        // Định dạng tiền tệ
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

        for (int row : checkedRows) {
            // Cột: Check(0), ID(1), Mã sách(2), Tên sách(3), Ngày mượn(4), Hạn trả(5), Số
            // ngày(6),
            // Phí thuê(7), Trạng thái(8), Tiền cọc(9), Phí phạt(10)
            String status = borrowedTable.getValueAt(row, 8).toString();
            String depositStr = borrowedTable.getValueAt(row, 9).toString().replace(" đ", "").replace(".", "");
            String fineStr = borrowedTable.getValueAt(row, 10).toString().replace(" đ", "").replace(".", "");

            long deposit = 0;
            long fine = 0;
            try {
                deposit = Long.parseLong(depositStr);
                fine = Long.parseLong(fineStr);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }

            if (status.contains("MẤT SÁCH")) {
                totalDeposit += deposit;
                // Mất sách -> Phạt = Cọc, Hoàn = 0
                totalFine += deposit;
            } else {
                totalDeposit += deposit;
                totalFine += fine;
                long refund = deposit - fine;
                if (refund > 0)
                    totalRefund += refund;
            }
        }

        if (checkedRows.size() == 1) {
            int row = checkedRows.get(0);
            String status = borrowedTable.getValueAt(row, 8).toString();
            String rentalFee = borrowedTable.getValueAt(row, 7).toString();
            if (status.contains("MẤT SÁCH")) {
                returnInfoLabel.setText("⚠️ Sách coi như đã mất. Tiền cọc bị tịch thu.");
                returnInfoLabel.setForeground(new java.awt.Color(200, 0, 0));
            } else {
                returnInfoLabel.setText("Cọc: " + nf.format(totalDeposit) + " đ | Thuê: " + rentalFee + " | Phạt: "
                        + nf.format(totalFine) + " đ | Hoàn: " + nf.format(totalRefund) + " đ");
                returnInfoLabel
                        .setForeground(totalFine > 0 ? new java.awt.Color(200, 100, 0) : new java.awt.Color(0, 150, 0));
            }
        } else {
            returnInfoLabel.setText("Đang chọn " + checkedRows.size() + " quyển | Tổng hoàn: " + nf.format(totalRefund)
                    + " đ (Cọc: " + nf.format(totalDeposit) + " - Phạt: " + nf.format(totalFine) + ")");
            returnInfoLabel.setForeground(new java.awt.Color(0, 100, 200));
        }
    }

    /**
     * Trả sách (hỗ trợ nhiều sách qua checkbox)
     */
    private void returnBook() {
        List<Integer> checkedRows = new ArrayList<>();
        for (int i = 0; i < borrowedTable.getRowCount(); i++) {
            if ((Boolean) borrowedTable.getValueAt(i, 0)) {
                checkedRows.add(i);
            }
        }

        if (checkedRows.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để trả!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        long totalRefund = 0;
        List<String> bookTitles = new ArrayList<>();

        // Tính toán trước tổng tiền và danh sách sách
        for (int row : checkedRows) {
            String bookId = borrowedTable.getValueAt(row, 2).toString();
            String title = borrowedTable.getValueAt(row, 3).toString();
            String status = borrowedTable.getValueAt(row, 8).toString();

            BookService.Book book = bookService.getBookById(bookId);
            if (book != null) {
                long deposit = (long) book.price;
                long fine = 0;

                if (status.contains("MẤT SÁCH")) {
                    fine = deposit;
                } else if (status.contains("TRỄ")) {
                    try {
                        String[] parts = status.split(" ");
                        int lateDays = Integer.parseInt(parts[1]);
                        fine = lateDays * LATE_FEE_PER_DAY;
                    } catch (Exception e) {
                    }
                }

                long refund = deposit - fine;
                if (refund < 0)
                    refund = 0;

                totalRefund += refund;
                bookTitles.add(title + (refund == 0 ? " (Không hoàn tiền)" : ""));
            }
        }

        // Xác nhận
        StringBuilder confirmMsg = new StringBuilder("Xác nhận trả " + checkedRows.size() + " quyển sách?\n\n");
        for (String title : bookTitles) {
            confirmMsg.append("• ").append(title).append("\n");
        }
        confirmMsg.append("\nTỔNG HOÀN TRẢ: ").append(nf.format(totalRefund)).append(" đ vào thẻ.");

        int option = javax.swing.JOptionPane.showConfirmDialog(this, confirmMsg.toString(), "Xác nhận trả sách",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Thực hiện trả sách
        int successCount = 0;
        long actualRefunded = 0;

        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

                for (int row : checkedRows) {
                    int borrowId = Integer.parseInt(borrowedTable.getValueAt(row, 1).toString());
                    String bookId = borrowedTable.getValueAt(row, 2).toString();
                    String status = borrowedTable.getValueAt(row, 8).toString();
                    BookService.Book book = bookService.getBookById(bookId);

                    if (book == null)
                        continue;

                    // 1. Xóa sách khỏi thẻ
                    boolean returnOk = balanceManager.returnBook(bookId);
                    if (returnOk) {
                        // Tính lại tiền hoàn cho quyển này để cộng dồn
                        long deposit = (long) book.price;
                        long fine = 0;
                        if (status.contains("MẤT SÁCH"))
                            fine = deposit;
                        else if (status.contains("TRỄ")) {
                            try {
                                String[] parts = status.split(" ");
                                int lateDays = Integer.parseInt(parts[1]);
                                fine = lateDays * LATE_FEE_PER_DAY;
                            } catch (Exception e) {
                            }
                        }
                        long refund = Math.max(0, deposit - fine);
                        actualRefunded += refund;

                        // 2. Cập nhật DB
                        borrowService.returnBook(borrowId, currentCardId);

                        // 3. Giảm BorrowStock
                        bookService.updateBorrowStock(bookId, Math.max(0, book.borrowStock - 1));

                        successCount++;
                    } else {
                        System.err.println("[MUONTRA] Failed to remove book from card: " + bookId);
                    }
                }

                // 4. Hoàn tiền tổng vào thẻ (1 giao dịch)
                if (actualRefunded > 0) {
                    balanceManager.deposit((int) actualRefunded);
                }

                connManager.disconnectCard();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối thẻ!", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error returning: " + e.getMessage());
        }

        if (successCount > 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Đã trả thành công " + successCount + "/" + checkedRows.size() + " quyển!\nĐã hoàn: "
                            + nf.format(actualRefunded) + " đ vào thẻ.",
                    "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadBorrowedBooks();
            loadAvailableBooks();
            returnInfoLabel.setText("Chọn sách để trả");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Trả sách thất bại! Vui lòng thử lại.",
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Báo mất sách
     */
    private void reportLostBook() {
        int row = borrowedTable.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int borrowId = Integer.parseInt(borrowedTable.getValueAt(row, 0).toString());
        String bookId = borrowedTable.getValueAt(row, 1).toString();
        BookService.Book book = bookService.getBookById(bookId);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        String confirmMsg = "⚠️ Báo mất sách?\n\n" +
                "Sách: " + book.title + "\n" +
                "Giá: " + nf.format(book.price) + " đ\n\n" +
                "TIỀN CỌC SẼ KHÔNG ĐƯỢC HOÀN TRẢ!";

        int option = javax.swing.JOptionPane.showConfirmDialog(this, confirmMsg, "Xác nhận mất sách",
                javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Xóa khỏi thẻ, không hoàn tiền, giảm BorrowStock
        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                balanceManager.returnBook(bookId);
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error reporting lost: " + e.getMessage());
        }

        // Cập nhật DB
        borrowService.returnBook(borrowId, currentCardId);
        bookService.updateBorrowStock(bookId, Math.max(0, book.borrowStock - 1));

        // Giảm Stock vĩnh viễn (mất sách)
        bookService.updateBookStock(bookId, Math.max(0, book.stock - 1));

        javax.swing.JOptionPane.showMessageDialog(this,
                "Đã ghi nhận sách bị mất.\nTiền cọc đã bị tịch thu.",
                "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);

        loadBorrowedBooks();
        loadAvailableBooks();
    }

    private void refreshBorrowedList() {
        loadBorrowedBooks();
        loadMemberInfo();
    }

    private void initComponents() {
        // Setup Main Panel
        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 0));

        // 1. Header
        add(createHeaderPanel(), java.awt.BorderLayout.NORTH);

        // 2. Tabbed Pane
        tabbedPane = new javax.swing.JTabbedPane();
        tabbedPane.setFont(new java.awt.Font("Segoe UI", 1, 14));
        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        tabbedPane.setUI(new ModernTabbedPaneUI());

        // --- Borrow Tab ---
        borrowPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        borrowPanel.setBackground(new java.awt.Color(248, 250, 252));
        borrowPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Input Section (Top)
        borrowPanel.add(createBorrowInputPanel(), java.awt.BorderLayout.NORTH);

        // Content Section (Center - Split)
        javax.swing.JPanel contentGrid = new javax.swing.JPanel(new java.awt.GridBagLayout());
        contentGrid.setOpaque(false);
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 10);
        gbc.weighty = 1.0;

        // Left: Available Books (60%)
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        contentGrid.add(createAvailableBooksPanel(), gbc);

        // Right: Cart (40%)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new java.awt.Insets(0, 10, 0, 0);
        contentGrid.add(createCartPanel(), gbc);

        borrowPanel.add(contentGrid, java.awt.BorderLayout.CENTER);

        // Bottom: Action
        borrowAllButton = createModernButton("XÁC NHẬN MƯỢN TẤT CẢ", java.awt.Color.WHITE,
                new java.awt.Color(16, 185, 129)); // Emerald 500
        borrowAllButton.setPreferredSize(new java.awt.Dimension(0, 50));
        borrowAllButton.setFont(new java.awt.Font("Segoe UI", 1, 16));
        borrowAllButton.addActionListener(e -> borrowAllBooks());

        javax.swing.JPanel bottomWrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 0, 0, 0));
        bottomWrapper.add(borrowAllButton, java.awt.BorderLayout.CENTER);
        borrowPanel.add(bottomWrapper, java.awt.BorderLayout.SOUTH);

        tabbedPane.addTab("  Thuê Sách  ", borrowPanel);

        // --- Return Tab ---
        returnPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        returnPanel.setBackground(new java.awt.Color(248, 250, 252));
        returnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        returnPanel.add(createReturnContentPanel(), java.awt.BorderLayout.CENTER);
        returnPanel.add(createReturnBottomPanel(), java.awt.BorderLayout.SOUTH);

        tabbedPane.addTab("  Trả Sách  ", returnPanel);
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadBorrowedBooks();
            }
        });

        add(tabbedPane, java.awt.BorderLayout.CENTER);
    }

    // --- Helpers for UI Construction ---

    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(new java.awt.Color(255, 255, 255));
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30)));

        titleLabel = new javax.swing.JLabel("Thuê / Trả Sách");
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 24));
        titleLabel.setForeground(new java.awt.Color(30, 41, 59));

        memberInfoLabel = new javax.swing.JLabel("Đang tải thông tin...");
        memberInfoLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        memberInfoLabel.setForeground(new java.awt.Color(59, 130, 246)); // Blue 500
        memberInfoLabel.setBackground(new java.awt.Color(239, 246, 255));
        memberInfoLabel.setOpaque(true);
        memberInfoLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 15, 8, 15));

        p.add(titleLabel, java.awt.BorderLayout.WEST);
        p.add(memberInfoLabel, java.awt.BorderLayout.EAST);
        return p;
    }

    private javax.swing.JPanel createBorrowInputPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 5, 5, 15);
        gbc.weighty = 0.0;

        // Card ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        p.add(createLabel("Mã thẻ:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        cardIdField = createStyledTextField();
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);
        p.add(cardIdField, gbc);

        // Book ID
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        p.add(createLabel("Mã sách:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.2;
        bookIdField = createStyledTextField();
        p.add(bookIdField, gbc);

        // Search Button
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        gbc.insets = new java.awt.Insets(5, 0, 5, 15);
        searchButton = createModernButton("Tìm kiếm", java.awt.Color.WHITE, new java.awt.Color(59, 130, 246));
        searchButton.setPreferredSize(new java.awt.Dimension(100, 36));
        searchButton.setToolTipText("Tìm sách");
        searchButton.addActionListener(e -> searchBook());
        p.add(searchButton, gbc);

        // Days
        gbc.gridx = 5;
        gbc.weightx = 0.0;
        p.add(createLabel("Số ngày:"), gbc);

        gbc.gridx = 6;
        gbc.weightx = 0.1;
        daysSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(14, 1, 60, 1));
        daysSpinner.setFont(new java.awt.Font("Segoe UI", 0, 14));
        daysSpinner.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        p.add(daysSpinner, gbc);

        // Add Button
        gbc.gridx = 7;
        gbc.weightx = 0.0;
        gbc.insets = new java.awt.Insets(5, 20, 5, 0);
        addToCartButton = createModernButton("Thêm vào giỏ", java.awt.Color.WHITE, new java.awt.Color(59, 130, 246));
        addToCartButton.addActionListener(e -> addToCart());
        p.add(addToCartButton, gbc);

        return p;
    }

    private javax.swing.JPanel createAvailableBooksPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 10));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        javax.swing.JLabel header = new javax.swing.JLabel("Kho sách");
        header.setFont(new java.awt.Font("Segoe UI", 1, 16));
        header.setForeground(new java.awt.Color(30, 41, 59));
        p.add(header, java.awt.BorderLayout.NORTH);

        String[] bookColumns = { "Mã sách", "Tên sách", "Tác giả", "Giá", "SL có sẵn" };
        booksTable = createStyledTable(new Object[][] {}, bookColumns);
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            int row = booksTable.getSelectedRow();
            if (row >= 0) {
                bookIdField.setText(booksTable.getValueAt(row, 0).toString());
            }
        });
        availableBooksTable = new javax.swing.JScrollPane(booksTable);
        availableBooksTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        availableBooksTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(availableBooksTable, java.awt.BorderLayout.CENTER);

        return p;
    }

    private javax.swing.JPanel createCartPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 10));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);
        javax.swing.JLabel title = new javax.swing.JLabel("Giỏ thuê");
        title.setFont(new java.awt.Font("Segoe UI", 1, 16));
        title.setForeground(new java.awt.Color(30, 41, 59));

        cartTotalLabel = new javax.swing.JLabel("Tổng: 0 đ");
        cartTotalLabel.setFont(new java.awt.Font("Segoe UI", 1, 16));
        cartTotalLabel.setForeground(new java.awt.Color(22, 163, 74)); // Green 600

        header.add(title, java.awt.BorderLayout.WEST);
        header.add(cartTotalLabel, java.awt.BorderLayout.EAST);
        p.add(header, java.awt.BorderLayout.NORTH);

        String[] cartColumns = { "Mã sách", "Tên sách", "Số ngày", "Tiền cọc", "Phí thuê", "Tổng" };
        cartTable = createStyledTable(new Object[][] {}, cartColumns);

        borrowCartTable = new javax.swing.JScrollPane(cartTable);
        borrowCartTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        borrowCartTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(borrowCartTable, java.awt.BorderLayout.CENTER);

        // Cart Buttons
        javax.swing.JPanel btnPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        removeFromCartButton = createModernButton("Xóa", java.awt.Color.WHITE, new java.awt.Color(239, 68, 68)); // Red
                                                                                                                 // 500
        removeFromCartButton.setPreferredSize(new java.awt.Dimension(80, 32));
        removeFromCartButton.addActionListener(e -> removeFromCart());

        clearCartButton = createModernButton("Xóa hết", new java.awt.Color(71, 85, 105),
                new java.awt.Color(226, 232, 240)); // Slate 200
        clearCartButton.setPreferredSize(new java.awt.Dimension(90, 32));
        clearCartButton.addActionListener(e -> clearCart());

        btnPanel.add(removeFromCartButton);
        btnPanel.add(clearCartButton);
        p.add(btnPanel, java.awt.BorderLayout.SOUTH);

        return p;
    }

    private javax.swing.JPanel createReturnContentPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 10));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header with Select All
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);

        javax.swing.JLabel title = new javax.swing.JLabel("Sách đang thuê");
        title.setFont(new java.awt.Font("Segoe UI", 1, 16));
        title.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JButton selectAllBtn = createModernButton("Chọn tất cả", java.awt.Color.WHITE,
                new java.awt.Color(59, 130, 246));
        selectAllBtn.setPreferredSize(new java.awt.Dimension(120, 30));
        selectAllBtn.addActionListener(e -> {
            boolean allSelected = true;
            for (int i = 0; i < borrowedTable.getRowCount(); i++) {
                if (!(Boolean) borrowedTable.getValueAt(i, 0)) {
                    allSelected = false;
                    break;
                }
            }
            for (int i = 0; i < borrowedTable.getRowCount(); i++) {
                borrowedTable.setValueAt(!allSelected, i, 0);
            }
        });

        header.add(title, java.awt.BorderLayout.WEST);
        header.add(selectAllBtn, java.awt.BorderLayout.EAST);
        p.add(header, java.awt.BorderLayout.NORTH);

        // Table
        String[] returnColumns = { "Chọn", "ID", "Mã sách", "Tên sách", "Ngày thuê", "Hạn trả", "Số ngày", "Phí thuê",
                "Trạng thái", "Tiền cọc", "Phí phạt" };
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new Object[][] {},
                returnColumns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        borrowedTable = createStyledTable(new Object[][] {}, returnColumns); // Will be replaced by model
        borrowedTable.setModel(model);
        borrowedTable.getModel().addTableModelListener(e -> updateReturnInfo());
        borrowedTable.getColumnModel().getColumn(0).setMaxWidth(50);

        borrowedBooksTable = new javax.swing.JScrollPane(borrowedTable);
        borrowedBooksTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        borrowedBooksTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(borrowedBooksTable, java.awt.BorderLayout.CENTER);

        return p;
    }

    private javax.swing.JPanel createReturnBottomPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 15));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));

        returnInfoLabel = new javax.swing.JLabel("Chọn sách để trả");
        returnInfoLabel.setFont(new java.awt.Font("Segoe UI", 1, 16));
        returnInfoLabel.setForeground(new java.awt.Color(100, 116, 139));
        returnInfoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p.add(returnInfoLabel, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel btnPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        returnButton = createModernButton("Trả sách đã chọn", java.awt.Color.WHITE, new java.awt.Color(22, 163, 74));
        returnButton.setPreferredSize(new java.awt.Dimension(200, 45));
        returnButton.addActionListener(e -> returnBook());

        lostBookButton = createModernButton("Báo mất sách", java.awt.Color.WHITE, new java.awt.Color(220, 38, 38));
        lostBookButton.setPreferredSize(new java.awt.Dimension(180, 45));
        lostBookButton.addActionListener(e -> reportLostBook());

        refreshButton = createModernButton("Làm mới", new java.awt.Color(71, 85, 105),
                new java.awt.Color(241, 245, 249));
        refreshButton.setPreferredSize(new java.awt.Dimension(150, 45));
        refreshButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)));
        refreshButton.addActionListener(e -> refreshBorrowedList());

        btnPanel.add(returnButton);
        btnPanel.add(lostBookButton);
        btnPanel.add(refreshButton);
        p.add(btnPanel, java.awt.BorderLayout.CENTER);

        return p;
    }

    // --- Common Widgets ---

    private javax.swing.JPanel createPanelWithShadow() {
        javax.swing.JPanel p = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private javax.swing.JLabel createLabel(String text) {
        javax.swing.JLabel l = new javax.swing.JLabel(text);
        l.setFont(new java.awt.Font("Segoe UI", 1, 14));
        l.setForeground(new java.awt.Color(100, 116, 139));
        return l;
    }

    private javax.swing.JTextField createStyledTextField() {
        javax.swing.JTextField f = new javax.swing.JTextField();
        f.setFont(new java.awt.Font("Segoe UI", 0, 14));
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        f.setBackground(new java.awt.Color(248, 250, 252));
        return f;
    }

    private javax.swing.JButton createModernButton(String text, java.awt.Color fg, java.awt.Color bg) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font("Segoe UI", 1, 14));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    private javax.swing.JButton createIconButton(String text, java.awt.Color bg) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font("Segoe UI Symbol", 0, 16));
        b.setForeground(java.awt.Color.WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new java.awt.Dimension(40, 36));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    private javax.swing.JTable createStyledTable(Object[][] data, String[] columns) {
        javax.swing.JTable t = new javax.swing.JTable(new javax.swing.table.DefaultTableModel(data, columns));
        t.setFont(new java.awt.Font("Segoe UI", 0, 14));
        t.setRowHeight(35);
        t.setSelectionBackground(new java.awt.Color(239, 246, 255));
        t.setSelectionForeground(new java.awt.Color(15, 23, 42));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new java.awt.Color(241, 245, 249));
        t.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 14));
        t.getTableHeader().setBackground(new java.awt.Color(248, 250, 252));
        t.getTableHeader().setForeground(new java.awt.Color(100, 116, 139));
        t.getTableHeader()
                .setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        return t;
    }

    // Modern ScrollBar
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new java.awt.Color(203, 213, 225);
            this.trackColor = new java.awt.Color(248, 250, 252);
        }

        @Override
        protected javax.swing.JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected javax.swing.JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private javax.swing.JButton createZeroButton() {
            javax.swing.JButton b = new javax.swing.JButton();
            b.setPreferredSize(new java.awt.Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled())
                return;
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    // Modern TabbedPane UI
    private static class ModernTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
        private final java.awt.Color selectedColor = new java.awt.Color(37, 99, 235); // Blue 600
        private final java.awt.Color selectedBgColor = new java.awt.Color(239, 246, 255); // Blue 50
        private final java.awt.Color unselectedColor = new java.awt.Color(100, 116, 139); // Slate 500
        private final java.awt.Color unselectedBgColor = new java.awt.Color(255, 255, 255); // White

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new java.awt.Insets(0, 20, 0, 0);
            contentBorderInsets = new java.awt.Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBorder(java.awt.Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                boolean isSelected) {
            // No border
        }

        @Override
        protected void paintContentBorder(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
            g.setColor(selectedColor);
            int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            g.fillRect(0, tabAreaHeight - 2, tabPane.getWidth(), 2);
        }

        @Override
        protected void paintFocusIndicator(java.awt.Graphics g, int tabPlacement, java.awt.Rectangle[] rects,
                int tabIndex, java.awt.Rectangle iconRect, java.awt.Rectangle textRect, boolean isSelected) {
            // No focus
        }

        @Override
        protected void paintTabBackground(java.awt.Graphics g, int tabPlacement, int tabIndex, int x, int y, int w,
                int h, boolean isSelected) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(selectedBgColor);
                g2.fillRect(x, y, w, h);
                // Indicator
                g2.setColor(selectedColor);
                g2.fillRect(x, y + h - 2, w, 2);
            } else {
                g2.setColor(unselectedBgColor);
                g2.fillRect(x, y, w, h);
            }
            g2.dispose();
        }

        @Override
        protected void paintText(java.awt.Graphics g, int tabPlacement, java.awt.Font font,
                java.awt.FontMetrics metrics, int tabIndex, String title, java.awt.Rectangle textRect,
                boolean isSelected) {
            g.setFont(isSelected ? font.deriveFont(java.awt.Font.BOLD) : font);
            g.setColor(isSelected ? selectedColor : unselectedColor);
            // Draw centered manually to control color easily
            int textX = textRect.x + (textRect.width - metrics.stringWidth(title)) / 2;
            int textY = textRect.y + metrics.getAscent();
            g.drawString(title, textX, textY);
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return 50;
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, java.awt.FontMetrics metrics) {
            return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 30; // Standard width + padding
        }
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel memberInfoLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel borrowPanel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JTextField bookIdField;
    private javax.swing.JSpinner daysSpinner;
    private javax.swing.JButton addToCartButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JScrollPane availableBooksTable;
    private javax.swing.JTable booksTable;
    private javax.swing.JScrollPane borrowCartTable;
    private javax.swing.JTable cartTable;
    private javax.swing.JButton removeFromCartButton;
    private javax.swing.JButton clearCartButton;
    private javax.swing.JButton borrowAllButton;
    private javax.swing.JLabel cartTotalLabel;
    private javax.swing.JPanel returnPanel;
    private javax.swing.JScrollPane borrowedBooksTable;
    private javax.swing.JTable borrowedTable;
    private javax.swing.JLabel returnInfoLabel;
    private javax.swing.JButton returnButton;
    private javax.swing.JButton lostBookButton;
    private javax.swing.JButton refreshButton;
}
