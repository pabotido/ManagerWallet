import Controller.BotTaiChinh;
import Model.KhoTaiChinh;
import View.AdminGuiView;
import View.AppConsole;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLyTaiChinh;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123456789";

    public static void main(String[] args) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> dongKetNoi(connection)));
            AppConsole.println("Ket noi SQL thanh cong!");

            KhoTaiChinh kho = new KhoTaiChinh(connection);
            khoiDongBot(kho);
            chayAdminGui(kho, connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void khoiDongBot(KhoTaiChinh kho) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BotTaiChinh bot = new BotTaiChinh(kho);
            botsApi.registerBot(bot);
            AppConsole.println("Telegram Bot da san sang hoat dong!");
            bot.guiThongBaoKhoiDong();
        } catch (TelegramApiException e) {
            AppConsole.println("Khong dang ky duoc Telegram Bot: " + e.getMessage());
        }
    }

    private static void chayAdminGui(KhoTaiChinh kho, Connection connection) {
        SwingUtilities.invokeLater(() -> {
            AdminGuiView view = new AdminGuiView(kho, () -> {
                AppConsole.println("Dang dong chuong trinh...");
                dongKetNoi(connection);
                System.exit(0);
            });
            view.setVisible(true);
        });
    }

    private static void dongKetNoi(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                AppConsole.println("Da dong ket noi database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
