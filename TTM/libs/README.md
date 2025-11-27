# Thư viện (Libraries)

Thư mục này chứa các file JAR cần thiết cho project.

## SQLite JDBC Driver

**Yêu cầu**: SQLite JDBC Driver

### Cách thêm:

1. Download SQLite JDBC driver từ:
   - https://github.com/xerial/sqlite-jdbc/releases
   - Hoặc https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/
   
2. Tải file `sqlite-jdbc-3.x.x.jar` (ví dụ: `sqlite-jdbc-3.51.0.0.jar`)

3. Đặt file JAR vào thư mục `libs/` này

4. Trong NetBeans:
   - Click chuột phải vào project → Properties
   - Chọn Libraries → Compile
   - Click Add JAR/Folder
   - Chọn file `sqlite-jdbc-3.x.x.jar` trong thư mục `libs/`
   - Click OK

### Phiên bản khuyến nghị:
- sqlite-jdbc-3.45.0.0.jar (hoặc mới hơn)

