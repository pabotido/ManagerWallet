package Model;

import View.AppConsole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhoTaiChinh {
    private static final int LOAI_NAP = 0;
    private static final int LOAI_RUT = 1;

    public record UserSoDuRow(long telegramUserId, String telegramUsername, String tenHienThi, double soDu) {}
    public record UserDetail(long telegramUserId, String telegramUsername, String tenHienThi, String createdAt, int soGiaoDich, double tongNap, double tongRut, double soDu) {}
    public record GiaoDichRow(String thoiGian, String loai, double soTien, String ghiChu) {}

    private final Connection ketNoi;

    public KhoTaiChinh(Connection connection) {
        this.ketNoi = connection;
        taoBangDanhMucNeuChuaCo();
        taoBangGiaoDichNeuChuaCo();
        taoBangNguoiDungNeuChuaCo();
        taoDanhMucMacDinhNeuChuaCo();
        dongBoNguoiDungTuLichSuGiaoDich();
    }

    private Connection getKetNoi() {
        return ketNoi;
    }

    private void taoBangDanhMucNeuChuaCo() {
        String sql = "IF OBJECT_ID('dbo.Categories', 'U') IS NULL "
                + "CREATE TABLE Categories ("
                + "ID INT IDENTITY(1,1) PRIMARY KEY, "
                + "TelegramUserID BIGINT NOT NULL DEFAULT 0, "
                + "Name NVARCHAR(255) NOT NULL, "
                + "Type NVARCHAR(50) NOT NULL)";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Khong tao duoc bang Categories", e);
        }
    }

    private void taoBangGiaoDichNeuChuaCo() {
        String sql = "IF OBJECT_ID('dbo.Transactions', 'U') IS NULL "
                + "CREATE TABLE Transactions ("
                + "ID BIGINT IDENTITY(1,1) PRIMARY KEY, "
                + "TelegramUserID BIGINT NOT NULL, "
                + "Amount FLOAT NOT NULL, "
                + "Note NVARCHAR(500) NULL, "
                + "CategoryID INT NOT NULL, "
                + "CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(), "
                + "CONSTRAINT FK_Transactions_Categories FOREIGN KEY (CategoryID) REFERENCES Categories(ID))";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Khong tao duoc bang Transactions", e);
        }
    }

    private void taoBangNguoiDungNeuChuaCo() {
        String sql = "IF OBJECT_ID('dbo.BotUsers', 'U') IS NULL "
                + "CREATE TABLE BotUsers ("
                + "TelegramUserID BIGINT PRIMARY KEY, "
                + "TelegramUsername NVARCHAR(255) NULL, "
                + "TelegramDisplayName NVARCHAR(255) NULL, "
                + "CreatedAt DATETIME2 DEFAULT GETDATE())";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String addUsernameColumn = "IF COL_LENGTH('dbo.BotUsers', 'TelegramUsername') IS NULL "
                + "ALTER TABLE BotUsers ADD TelegramUsername NVARCHAR(255) NULL";
        try (PreparedStatement ps = getKetNoi().prepareStatement(addUsernameColumn)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String addDisplayNameColumn = "IF COL_LENGTH('dbo.BotUsers', 'TelegramDisplayName') IS NULL "
                + "ALTER TABLE BotUsers ADD TelegramDisplayName NVARCHAR(255) NULL";
        try (PreparedStatement ps = getKetNoi().prepareStatement(addDisplayNameColumn)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String addCreatedAtColumn = "IF COL_LENGTH('dbo.BotUsers', 'CreatedAt') IS NULL "
                + "ALTER TABLE BotUsers ADD CreatedAt DATETIME2 NULL";
        try (PreparedStatement ps = getKetNoi().prepareStatement(addCreatedAtColumn)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String backfillCreatedAt = "UPDATE BotUsers SET CreatedAt = GETDATE() WHERE CreatedAt IS NULL";
        try (PreparedStatement ps = getKetNoi().prepareStatement(backfillCreatedAt)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void taoDanhMucMacDinhNeuChuaCo() {
        chenDanhMucNeuChuaCo("Thu Nhap Mac Dinh", "In");
        chenDanhMucNeuChuaCo("Chi Tieu Mac Dinh", "Out");
    }

    private void chenDanhMucNeuChuaCo(String ten, String loai) {
        String sql = "IF NOT EXISTS (SELECT 1 FROM Categories WHERE Type = ?) "
                + "INSERT INTO Categories (TelegramUserID, Name, Type) VALUES (0, ?, ?)";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setString(1, loai);
            ps.setString(2, ten);
            ps.setString(3, loai);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Khong tao duoc danh muc mac dinh " + loai, e);
        }
    }

    public synchronized void dangKyNguoiDung(long idTelegram, String username, String tenHienThi) {
        if (username != null && username.isBlank()) username = null;
        if (tenHienThi != null && tenHienThi.isBlank()) tenHienThi = null;

        String sql = "IF NOT EXISTS (SELECT 1 FROM BotUsers WHERE TelegramUserID = ?) "
                + "INSERT INTO BotUsers (TelegramUserID, TelegramUsername, TelegramDisplayName, CreatedAt) VALUES (?, ?, ?, GETDATE()); "
                + "UPDATE BotUsers SET "
                + "TelegramUsername = CASE WHEN ? IS NOT NULL THEN ? ELSE TelegramUsername END, "
                + "TelegramDisplayName = CASE WHEN ? IS NOT NULL THEN ? ELSE TelegramDisplayName END "
                + "WHERE TelegramUserID = ?";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setLong(1, idTelegram);
            ps.setLong(2, idTelegram);
            ps.setString(3, username);
            ps.setString(4, tenHienThi);
            ps.setString(5, username);
            ps.setString(6, username);
            ps.setString(7, tenHienThi);
            ps.setString(8, tenHienThi);
            ps.setLong(9, idTelegram);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dongBoNguoiDungTuLichSuGiaoDich() {
        String sql = "INSERT INTO BotUsers (TelegramUserID, CreatedAt) "
                + "SELECT DISTINCT t.TelegramUserID, GETDATE() FROM Transactions t "
                + "WHERE NOT EXISTS (SELECT 1 FROM BotUsers b WHERE b.TelegramUserID = t.TelegramUserID)";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized double laySoDu(long idTelegram) {
        return layTongTienTheoLoai(idTelegram, "In") - layTongTienTheoLoai(idTelegram, "Out");
    }

    private double layTongTienTheoLoai(long idTelegram, String loai) {
        String sql = "SELECT COALESCE(SUM(t.Amount), 0) "
                + "FROM Transactions t "
                + "JOIN Categories c ON c.ID = t.CategoryID "
                + "WHERE t.TelegramUserID = ? AND c.Type = ?";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setLong(1, idTelegram);
            ps.setString(2, loai);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public synchronized void luuGiaoDich(GiaoDich gd) {
        if (gd.getIdDanhMuc() == LOAI_RUT) {
            double soDuHienTai = laySoDu(gd.getIdNguoiDungTelegram());
            if (gd.getSoTien() > soDuHienTai) {
                throw new IllegalStateException("Khong du so du");
            }
        }

        int categoryId = layHoacTaoCategoryId(gd.getIdDanhMuc());
        String sql = "INSERT INTO Transactions (TelegramUserID, Amount, Note, CategoryID) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setLong(1, gd.getIdNguoiDungTelegram());
            ps.setDouble(2, gd.getSoTien());
            ps.setString(3, gd.getGhiChu());
            ps.setInt(4, categoryId);
            ps.executeUpdate();
            AppConsole.println("Giao dich " + (gd.getIdDanhMuc() == LOAI_NAP ? "nap" : "rut") + " thanh cong!");
        } catch (SQLException e) {
            throw new RuntimeException("Khong the luu giao dich", e);
        }
    }

    private int layHoacTaoCategoryId(int idDanhMuc) {
        String loai = idDanhMuc == LOAI_NAP ? "In" : "Out";
        String ten = idDanhMuc == LOAI_NAP ? "Thu Nhap Mac Dinh" : "Chi Tieu Mac Dinh";

        Integer existingId = timCategoryIdTheoLoai(loai);
        if (existingId != null) return existingId;

        chenDanhMucNeuChuaCo(ten, loai);
        Integer retryId = timCategoryIdTheoLoai(loai);
        if (retryId != null) return retryId;

        throw new RuntimeException("Khong lay duoc ID category cho loai " + loai);
    }

    private Integer timCategoryIdTheoLoai(String loai) {
        String sql = "SELECT TOP 1 ID FROM Categories WHERE Type = ? ORDER BY ID";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setString(1, loai);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("ID");
        } catch (SQLException e) {
            throw new RuntimeException("Khong the truy van Categories", e);
        }
        return null;
    }

    public synchronized String layBaoCaoNgay(long idTelegram) {
        double tongNap = 0;
        double tongRut = 0;
        StringBuilder chiTietRut = new StringBuilder("\n--- CHI TIET RUT ---\n");

        String sqlNap = "SELECT COALESCE(SUM(t.Amount), 0) "
                + "FROM Transactions t "
                + "JOIN Categories c ON c.ID = t.CategoryID "
                + "WHERE t.TelegramUserID = ? "
                + "AND c.Type = 'In' "
                + "AND CAST(t.CreatedAt AS DATE) = CAST(GETDATE() AS DATE)";
        String sqlRut = "SELECT t.Note, t.Amount "
                + "FROM Transactions t "
                + "JOIN Categories c ON c.ID = t.CategoryID "
                + "WHERE t.TelegramUserID = ? "
                + "AND c.Type = 'Out' "
                + "AND CAST(t.CreatedAt AS DATE) = CAST(GETDATE() AS DATE) "
                + "ORDER BY t.CreatedAt DESC";
        try (PreparedStatement ps1 = getKetNoi().prepareStatement(sqlNap);
             PreparedStatement ps2 = getKetNoi().prepareStatement(sqlRut)) {
            ps1.setLong(1, idTelegram);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) tongNap = rs1.getDouble(1);

            ps2.setLong(1, idTelegram);
            ResultSet rs2 = ps2.executeQuery();
            int stt = 1;
            while (rs2.next()) {
                double amount = rs2.getDouble("Amount");
                String note = rs2.getString("Note");
                chiTietRut.append(stt++)
                        .append(". ")
                        .append(note)
                        .append(": ")
                        .append(String.format("%,.0f", amount))
                        .append("d\n");
                tongRut += amount;
            }
        } catch (SQLException e) {
            return "Loi he thong!";
        }

        StringBuilder baoCao = new StringBuilder();
        baoCao.append("TONG KET HOM NAY\n--------------------------\n");
        baoCao.append("Tong nap: +").append(String.format("%,.0f", tongNap)).append("d\n");
        baoCao.append("Tong rut: -").append(String.format("%,.0f", tongRut)).append("d\n");
        if (tongRut > 0) {
            baoCao.append(chiTietRut);
        } else {
            baoCao.append("\n(Hom nay ban chua rut tien)");
        }
        baoCao.append("\n--------------------------\nSo du thay doi: ")
                .append(String.format("%,.0f", tongNap - tongRut))
                .append("d");
        return baoCao.toString();
    }

    public synchronized int admin_DemUser() {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT TelegramUserID FROM BotUsers "
                + "UNION SELECT TelegramUserID FROM Transactions"
                + ") AS all_users";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public synchronized List<UserSoDuRow> admin_LayTatCaUserVaSoDu() {
        List<UserSoDuRow> ketQua = new ArrayList<>();
        String sql = "WITH AllUsers AS ("
                + "SELECT TelegramUserID FROM BotUsers "
                + "UNION SELECT TelegramUserID FROM Transactions"
                + ") "
                + "SELECT u.TelegramUserID, b.TelegramUsername, "
                + "COALESCE(NULLIF(b.TelegramDisplayName, ''), NULLIF(b.TelegramUsername, '')) AS TenHienThi, "
                + "COALESCE(SUM(CASE "
                + "WHEN c.Type = 'In' THEN t.Amount "
                + "WHEN c.Type = 'Out' THEN -t.Amount "
                + "ELSE 0 END), 0) AS SoDu "
                + "FROM AllUsers u "
                + "LEFT JOIN BotUsers b ON b.TelegramUserID = u.TelegramUserID "
                + "LEFT JOIN Transactions t ON t.TelegramUserID = u.TelegramUserID "
                + "LEFT JOIN Categories c ON c.ID = t.CategoryID "
                + "GROUP BY u.TelegramUserID, b.TelegramUsername, b.TelegramDisplayName "
                + "ORDER BY u.TelegramUserID";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ketQua.add(new UserSoDuRow(
                        rs.getLong("TelegramUserID"),
                        rs.getString("TelegramUsername"),
                        rs.getString("TenHienThi"),
                        rs.getDouble("SoDu")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong lay duoc danh sach user", e);
        }
        return ketQua;
    }

    public synchronized UserDetail admin_LayThongTinUser(long idTelegram) {
        String sql = "SELECT u.TelegramUserID, b.TelegramUsername, "
                + "COALESCE(NULLIF(b.TelegramDisplayName, ''), NULLIF(b.TelegramUsername, '')) AS TenHienThi, "
                + "CONVERT(VARCHAR(19), b.CreatedAt, 120) AS CreatedAtText, "
                + "COUNT(t.TelegramUserID) AS SoGiaoDich, "
                + "COALESCE(SUM(CASE WHEN c.Type = 'In' THEN t.Amount ELSE 0 END), 0) AS TongNap, "
                + "COALESCE(SUM(CASE WHEN c.Type = 'Out' THEN t.Amount ELSE 0 END), 0) AS TongRut "
                + "FROM (SELECT ? AS TelegramUserID) u "
                + "LEFT JOIN BotUsers b ON b.TelegramUserID = u.TelegramUserID "
                + "LEFT JOIN Transactions t ON t.TelegramUserID = u.TelegramUserID "
                + "LEFT JOIN Categories c ON c.ID = t.CategoryID "
                + "GROUP BY u.TelegramUserID, b.TelegramUsername, b.TelegramDisplayName, b.CreatedAt";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setLong(1, idTelegram);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return new UserDetail(idTelegram, null, null, null, 0, 0, 0, 0);
            }

            double tongNap = rs.getDouble("TongNap");
            double tongRut = rs.getDouble("TongRut");
            return new UserDetail(
                    rs.getLong("TelegramUserID"),
                    rs.getString("TelegramUsername"),
                    rs.getString("TenHienThi"),
                    rs.getString("CreatedAtText"),
                    rs.getInt("SoGiaoDich"),
                    tongNap,
                    tongRut,
                    tongNap - tongRut
            );
        } catch (SQLException e) {
            throw new RuntimeException("Khong lay duoc thong tin user", e);
        }
    }

    public synchronized List<GiaoDichRow> admin_LayGiaoDichUser(long idTelegram) {
        List<GiaoDichRow> ketQua = new ArrayList<>();
        String sql = "SELECT TOP 200 "
                + "CONVERT(VARCHAR(19), t.CreatedAt, 120) AS ThoiGian, "
                + "c.Type AS Loai, t.Amount AS SoTien, COALESCE(t.Note, '') AS GhiChu "
                + "FROM Transactions t "
                + "LEFT JOIN Categories c ON c.ID = t.CategoryID "
                + "WHERE t.TelegramUserID = ? "
                + "ORDER BY t.CreatedAt DESC";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setLong(1, idTelegram);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ketQua.add(new GiaoDichRow(
                        rs.getString("ThoiGian"),
                        rs.getString("Loai"),
                        rs.getDouble("SoTien"),
                        rs.getString("GhiChu")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Khong lay duoc giao dich user", e);
        }
        return ketQua;
    }

    public synchronized void admin_CapNhatTenHienThiUser(long idTelegram, String tenHienThiMoi) {
        String sql = "IF EXISTS (SELECT 1 FROM BotUsers WHERE TelegramUserID = ?) "
                + "UPDATE BotUsers SET TelegramDisplayName = ? WHERE TelegramUserID = ? "
                + "ELSE INSERT INTO BotUsers (TelegramUserID, TelegramDisplayName, CreatedAt) VALUES (?, ?, GETDATE())";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ps.setLong(1, idTelegram);
            ps.setString(2, tenHienThiMoi);
            ps.setLong(3, idTelegram);
            ps.setLong(4, idTelegram);
            ps.setString(5, tenHienThiMoi);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Khong cap nhat duoc ten hien thi", e);
        }
    }

    public synchronized List<Long> layDanhSachNguoiDung() {
        List<Long> userIds = new ArrayList<>();
        String sql = "SELECT TelegramUserID FROM BotUsers "
                + "UNION SELECT DISTINCT TelegramUserID FROM Transactions "
                + "ORDER BY TelegramUserID";
        try (PreparedStatement ps = getKetNoi().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) userIds.add(rs.getLong("TelegramUserID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }

    public synchronized void admin_XoaUser(long id) {
        String xoaGiaoDich = "DELETE FROM Transactions WHERE TelegramUserID = ?";
        String xoaUser = "DELETE FROM BotUsers WHERE TelegramUserID = ?";

        try {
            getKetNoi().setAutoCommit(false);

            try (PreparedStatement psXoaGiaoDich = getKetNoi().prepareStatement(xoaGiaoDich);
                 PreparedStatement psXoaUser = getKetNoi().prepareStatement(xoaUser)) {
                psXoaGiaoDich.setLong(1, id);
                int soDongGiaoDich = psXoaGiaoDich.executeUpdate();

                psXoaUser.setLong(1, id);
                int soDongUser = psXoaUser.executeUpdate();

                getKetNoi().commit();
                AppConsole.println("Da xoa User " + id
                        + " (xoa " + soDongGiaoDich + " giao dich, "
                        + soDongUser + " ban ghi user).");
            }
        } catch (SQLException e) {
            try {
                getKetNoi().rollback();
            } catch (SQLException rollbackException) {
                e.addSuppressed(rollbackException);
            }
            throw new RuntimeException("Khong xoa duoc user " + id, e);
        } finally {
            try {
                getKetNoi().setAutoCommit(true);
            } catch (SQLException e) {
                AppConsole.println("Khong khoi phuc duoc auto-commit: " + e.getMessage());
            }
        }
    }
}
