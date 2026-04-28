CREATE DATABASE QuanLyTaiChinh;
GO
USE QuanLyTaiChinh;
GO
-- 1. Bảng Danh mục: Mỗi user có bộ danh mục riêng (Ăn uống, Lương...)
CREATE TABLE Categories (
    ID INT PRIMARY KEY IDENTITY(1,1),
    TelegramUserID BIGINT NOT NULL,   -- Lưu ID của người dùng Telegram
    Name NVARCHAR(100) NOT NULL,
    Type NVARCHAR(10) CHECK (Type IN ('In', 'Out')),
    INDEX ix_user (TelegramUserID)    -- Tối ưu tốc độ tìm kiếm theo user
);

-- 2. Bảng Giao dịch: Lưu vết thu chi của từng user
CREATE TABLE Transactions (
    ID INT PRIMARY KEY IDENTITY(1,1),
    TelegramUserID BIGINT NOT NULL,   -- Ai là người tạo giao dịch này?
    Amount DECIMAL(18, 2) NOT NULL,
    Note NVARCHAR(255),
    CreatedAt DATETIME DEFAULT GETDATE(),
    CategoryID INT,
    
    FOREIGN KEY (CategoryID) REFERENCES Categories(ID)
);

USE QuanLyTaiChinh;
GO

-- Xóa dữ liệu cũ trong Categories để làm lại ID chuẩn 1 và 2
TRUNCATE TABLE Transactions; -- Xóa giao dịch trước vì nó dính khóa ngoại
DELETE FROM Categories;
DBCC CHECKIDENT ('Categories', RESEED, 0); -- Reset ID về 0 để dòng sau tăng lên 1

-- Chèn 2 dòng chuẩn để khớp với code Java (ID 1 và 2)
INSERT INTO Categories (TelegramUserID, Name, Type) 
VALUES (0, N'Thu Nhập Mặc Định', 'In'); -- Sẽ có ID = 1

INSERT INTO Categories (TelegramUserID, Name, Type) 
VALUES (0, N'Chi Tiêu Mặc Định', 'Out'); -- Sẽ có ID = 2
GO
