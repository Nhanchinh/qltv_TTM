# 🚀 HƯỚNG DẪN NHANH - Rebuild Database

## Bước 1: Xóa database cũ (đã làm ✓)
```
fix_database.bat đã chạy thành công
Database cũ đã được backup
```

## Bước 2: Chạy RebuildDatabase.java

**Trong IDE (NetBeans/IntelliJ/Eclipse):**
1. Mở file: `src/ui/RebuildDatabase.java`
2. Right-click → **Run File** (hoặc Shift+F6)
3. Chọn **"yes"** khi được hỏi xác nhận

**Kết quả mong đợi:**
```
========================================
 REBUILD DATABASE
========================================

[1] Creating new database...
[2] Creating tables...
    ✓ Books table created
    ✓ Cards table created
    ✓ BorrowHistory table created
    ✓ PurchaseBookHistory table created
    ✓ Stationery table created
    ✓ StationerySales table created
    ✓ Transactions table created
    ✓ Fines table created
    ✓ Settings table created

========================================
 DATABASE REBUILT SUCCESSFULLY!
========================================
```

## Bước 3: Chạy InsertData.java

**Trong IDE:**
1. Mở file: `src/ui/InsertData.java`
2. Right-click → **Run File**

**Kết quả mong đợi:**
```
Đường dẫn file DB: src\database\library.db
Đang xóa dữ liệu cũ...
>>> Đã xóa dữ liệu cũ!
Đã thêm 20 sách vào Books!
>>> THÊM DỮ LIỆU HOÀN TẤT!
```

## Bước 4: Kiểm tra

Chạy ứng dụng chính và test:
- ✅ Đăng nhập
- ✅ Xem danh sách sách
- ✅ Xem thông tin thẻ
- ✅ Các chức năng khác

---

## ⚠️ Nếu gặp lỗi

### Lỗi: "Database already exists"
- Chọn **"yes"** để xóa và rebuild

### Lỗi: "Table already exists"
- Xóa file `src/database/library.db` thủ công
- Chạy lại RebuildDatabase.java

### Lỗi: "No such table"
- Chắc chắn đã chạy RebuildDatabase.java thành công
- Kiểm tra file database có tồn tại không

---

## 📊 Các bảng đã tạo

1. ✅ **Books** - Danh sách sách (20+ cuốn)
2. ✅ **Cards** - Thẻ thành viên (2 thẻ mẫu)
3. ✅ **BorrowHistory** - Lịch sử mượn sách
4. ✅ **PurchaseBookHistory** - Lịch sử mua sách
5. ✅ **Stationery** - Văn phòng phẩm (4 items)
6. ✅ **StationerySales** - Lịch sử bán VPP
7. ✅ **Transactions** - Giao dịch
8. ✅ **Fines** - Tiền phạt
9. ✅ **Settings** - Cài đặt hệ thống

---

**Tóm tắt:**
1. ✅ fix_database.bat (đã chạy)
2. ⏳ RebuildDatabase.java (chạy ngay)
3. ⏳ InsertData.java (chạy sau)
4. ⏳ Test ứng dụng
