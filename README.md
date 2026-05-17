# 💼 ManagerWallet

![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Database](https://img.shields.io/badge/Database-SQL%20Server-red.svg)
![UI](https://img.shields.io/badge/UI-Swing-orange.svg)
![Bot](https://img.shields.io/badge/Bot-Telegram-2CA5E0.svg)
![License](https://img.shields.io/badge/License-Unlicensed-lightgrey.svg)

> 🧾 **ManagerWallet** là ứng dụng quản lý tài chính cá nhân kết hợp **Telegram Bot** và **giao diện quản trị Swing**, giúp ghi nhận thu chi nhanh chóng, theo dõi số dư và quản lý người dùng tập trung trên **SQL Server**.

---

## 📚 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Bắt đầu](#-bắt-đầu)
  - [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
  - [Cài đặt](#-cài-đặt)
- [Sử dụng](#-sử-dụng)
- [Đóng góp](#-đóng-góp)
- [Giấy phép](#-giấy-phép)
- [Liên hệ](#-liên-hệ)

---

## ✨ Giới thiệu

**ManagerWallet** là một hệ thống quản lý tài chính cá nhân được xây dựng bằng **Java**, sử dụng **Telegram Bot** để tương tác nhanh với người dùng và **giao diện Admin GUI** để quản trị dữ liệu.

Dự án cho phép:
- ghi nhận giao dịch nạp/rút tiền,
- theo dõi số dư theo từng người dùng Telegram,
- xem báo cáo trong ngày,
- và quản trị danh sách người dùng qua giao diện desktop.

Hệ thống lưu dữ liệu trên **Microsoft SQL Server** và có cơ chế khởi tạo bảng tự động nếu chưa tồn tại.

---

## 🚀 Tính năng

- 🤖 **Tương tác qua Telegram Bot**
  - `/start`
  - `/menu`
  - `nap <so_tien> <ghi_chu_tuy_chon>`
  - `rut <so_tien> <ghi_chu_tuy_chon>`

- 💰 **Quản lý thu chi cá nhân**
  - Nạp tiền
  - Rút tiền
  - Kiểm tra số dư hiện tại
  - Báo cáo giao dịch trong ngày

- 🧑‍💼 **Admin GUI**
  - Xem tổng số user
  - Danh sách người dùng và số dư
  - Xem chi tiết giao dịch của từng user
  - Sửa tên hiển thị
  - Xóa user đã chọn

- 🗄️ **Cơ sở dữ liệu SQL Server**
  - Tự động tạo bảng nếu chưa có
  - Lưu lịch sử giao dịch và thông tin người dùng
  - Đồng bộ dữ liệu theo Telegram User ID

- 🔔 **Thông báo khởi động bot**
  - Khi bot khởi động lại, hệ thống gửi thông báo đến các user đã từng sử dụng

---

## 🛠️ Công nghệ sử dụng

- **Java**
- **Swing**
- **Telegram Bots API**
- **Microsoft SQL Server**
- **T-SQL**

---

## 🎯 Bắt đầu

### ✅ Yêu cầu hệ thống

Trước khi chạy dự án, hãy đảm bảo bạn đã cài đặt:

- **Java 17+**  
- **Microsoft SQL Server**
- **JDBC Driver for SQL Server**
- **Telegram Bot Token**
- IDE như **IntelliJ IDEA** hoặc **Eclipse** để phát triển

---

### 📦 Cài đặt

1. **Clone repository**
   ```bash
   git clone https://github.com/pabotido/ManagerWallet.git
   cd ManagerWallet
   ```

2. **Tạo database**
   Chạy file SQL trong thư mục:
   ```text
   Data_base/database.sql
   ```

3. **Cấu hình kết nối database**
   Trong `Big_project/src/Main.java`, ứng dụng hiện đang dùng cấu hình:
   ```java
   jdbc:sqlserver://localhost:1433;databaseName=QuanLyTaiChinh;encrypt=true;trustServerCertificate=true
   ```
   với:
   ```java
   sa / 123456789
   ```

   > ⚠️ Khuyến nghị: chuyển các thông tin này sang file cấu hình riêng hoặc biến môi trường để bảo mật tốt hơn.

4. **Cấu hình Telegram Bot**
   Trong `Big_project/src/Controller/BotTaiChinh.java`, bot đang sử dụng token và username cố định.  
   Hãy thay thế bằng token thực tế của bạn nếu cần triển khai riêng.

5. **Mở project và build**
   - Mở project bằng IDE
   - Thêm JDBC driver và thư viện Telegram Bots nếu chưa được khai báo
   - Build project

---

## ▶️ Sử dụng

Sau khi chạy class `Main`:

1. Ứng dụng kết nối tới SQL Server
2. Telegram Bot được khởi động
3. Giao diện Admin GUI được mở

### Lệnh Telegram hỗ trợ

#### Mở menu
```text
/start
```
hoặc
```text
/menu
```

#### Nạp tiền
```text
nap 500000 luong thang 5
```

#### Rút tiền
```text
rut 50000 mua do an
```

### Cách dùng nhanh qua nút bấm
Bot hiển thị menu chính với:
- **Nạp tiền**
- **Rút tiền**
- **Tổng kết ngày**

Người dùng chỉ cần bấm nút và nhập số tiền tương ứng.

---

## 🤝 Đóng góp

Mình rất hoan nghênh đóng góp để dự án tốt hơn.

Nếu bạn muốn đóng góp:
1. Fork repository
2. Tạo branch mới
3. Thực hiện thay đổi
4. Gửi Pull Request

### Quy ước gợi ý
- Viết code rõ ràng, dễ đọc
- Giữ đúng cấu trúc package hiện tại
- Kiểm tra kỹ trước khi gửi PR
- Không commit thông tin nhạy cảm như token hoặc mật khẩu

---

## 📄 Giấy phép

Dự án hiện **chưa khai báo license**.

Nếu bạn muốn mở mã nguồn cho cộng đồng, hãy cân nhắc thêm một license phù hợp như:
- MIT
- Apache 2.0
- GPLv3

---

## 📬 Liên hệ

**Tác giả:** `pabotido`  
**GitHub:** [https://github.com/pabotido](https://github.com/pabotido)

Nếu cần, bạn có thể bổ sung thêm:
- Email
- LinkedIn
- Website cá nhân

---

## 🧩 Cấu trúc dự án

```text
ManagerWallet/
├── Big_project/
│   └── src/
│       ├── Controller/
│       ├── Model/
│       ├── View/
│       └── Main.java
├── Data_base/
│   └── database.sql
└── README.md
```

---

## ⚠️ Lưu ý bảo mật

- Token Telegram hiện đang được hard-code trong mã nguồn
- Tài khoản SQL Server đang dùng `sa`
- Mật khẩu database cũng đang được lưu trực tiếp trong code

Đây là các thông tin nên tách ra file cấu hình riêng để an toàn hơn trong môi trường thực tế.
