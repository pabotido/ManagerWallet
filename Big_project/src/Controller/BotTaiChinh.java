package Controller;

import Model.GiaoDich;
import Model.KhoTaiChinh;
import View.AppConsole;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BotTaiChinh extends TelegramLongPollingBot {
    private static final int LOAI_NAP = 0;
    private static final int LOAI_RUT = 1;
    private static final String BOT_TOKEN = "8688609308:AAEVsXvrnOEpy8_QcHbsAt8hTRI1dUXv7Lw";

    private final KhoTaiChinh kho;
    private final Map<Long, Integer> hanhDongDangCho = new ConcurrentHashMap<>();

    public BotTaiChinh(KhoTaiChinh kho) {
        super(BOT_TOKEN);
        this.kho = kho;
    }

    @Override
    public String getBotUsername() {
        return "pabotidoBot";
    }

    public void guiThongBaoKhoiDong() {
        List<Long> danhSach = kho.layDanhSachNguoiDung();
        AppConsole.println("So nguoi nhan thong bao khoi dong: " + danhSach.size());
        for (long chatId : danhSach) {
            guiTinNhan(chatId, "Bot da hoat dong lai. Ban co the tiep tuc su dung /start.");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            xuLyNutBam(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        User from = update.getMessage().getFrom();
        if (from == null) {
            return;
        }

        String username = from.getUserName();
        String tenHienThi = taoTenHienThi(from);
        String name = tenHienThi == null ? "Ban" : tenHienThi;

        kho.dangKyNguoiDung(chatId, username, tenHienThi);

        String msg = update.getMessage().getText().trim();
        String msgLower = msg.toLowerCase();

        if (msgLower.startsWith("nap ") || msgLower.startsWith("rut ")) {
            thucHienGiaoDich(chatId, msg, name);
        } else if (hanhDongDangCho.containsKey(chatId)) {
            thucHienGiaoDichTuNut(chatId, msg, name);
        } else if (msgLower.equals("/start") || msgLower.equals("/menu")) {
            guiMenuChinh(chatId, name);
        } else {
            guiHuongDan(chatId);
        }
    }

    private void thucHienGiaoDich(long chatId, String msg, String name) {
        hanhDongDangCho.remove(chatId);
        try {
            String[] p = msg.split("\\s+", 3);
            if (p.length < 2) throw new IllegalArgumentException("Thieu so tien");

            String lenh = p[0].toLowerCase();
            double tienYeuCau = Double.parseDouble(p[1]);
            String note = p.length >= 3 ? p[2].trim() : "";

            if (tienYeuCau <= 0) {
                guiTinNhan(chatId, "So tien phai lon hon 0.");
                return;
            }

            if (note.isEmpty()) {
                note = lenh.equals("rut") ? "Rut tien" : "Nap tien";
            }

            if (lenh.equals("rut")) {
                double soDuHienTai = kho.laySoDu(chatId);
                if (tienYeuCau > soDuHienTai) {
                    guiTinNhan(chatId, taoThongBaoKhongDuSoDu(soDuHienTai));
                    return;
                }
                kho.luuGiaoDich(new GiaoDich(chatId, tienYeuCau, note, LOAI_RUT));
            } else {
                kho.luuGiaoDich(new GiaoDich(chatId, tienYeuCau, note, LOAI_NAP));
            }

            guiMenuChinh(chatId, name);
        } catch (NumberFormatException e) {
            guiTinNhan(chatId, "So tien khong hop le. Vi du: nap 500000 luong hoac rut 50000 mua_kem");
        } catch (IllegalStateException e) {
            guiTinNhan(chatId, taoThongBaoKhongDuSoDu(kho.laySoDu(chatId)));
        } catch (Exception e) {
            guiTinNhan(chatId, "Sai cu phap. Vi du: nap 500000 luong hoac rut 50000 mua_kem");
        }
    }

    private void thucHienGiaoDichTuNut(long chatId, String msg, String name) {
        Integer idDanhMuc = hanhDongDangCho.get(chatId);
        if (idDanhMuc == null) return;

        try {
            String[] p = msg.split("\\s+", 2);
            double tienYeuCau = Double.parseDouble(p[0]);
            if (tienYeuCau <= 0) {
                guiTinNhan(chatId, "So tien phai lon hon 0.");
                return;
            }

            String note = p.length >= 2 ? p[1].trim() : "";
            if (note.isEmpty()) {
                note = idDanhMuc == LOAI_RUT ? "Rut tien" : "Nap tien";
            }

            if (idDanhMuc == LOAI_RUT) {
                double soDuHienTai = kho.laySoDu(chatId);
                if (tienYeuCau > soDuHienTai) {
                    guiTinNhan(chatId, taoThongBaoKhongDuSoDu(soDuHienTai));
                    return;
                }
            }

            kho.luuGiaoDich(new GiaoDich(chatId, tienYeuCau, note, idDanhMuc));
            hanhDongDangCho.remove(chatId);
            guiMenuChinh(chatId, name);
        } catch (NumberFormatException e) {
            guiTinNhan(chatId, "Chi can nhap so tien. Vi du: 500000 hoac 500000 luong");
        } catch (IllegalStateException e) {
            guiTinNhan(chatId, taoThongBaoKhongDuSoDu(kho.laySoDu(chatId)));
        } catch (Exception e) {
            guiTinNhan(chatId, "Co loi khi xu ly giao dich, vui long thu lai.");
        }
    }

    private void guiMenuChinh(long chatId, String ten) {
        double soDu = kho.laySoDu(chatId);
        String txt = "QUAN LY TAI CHINH\nChu the: " + ten + "\nSo du: " + String.format("%,.0f", soDu) + " VND";

        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(txt);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(taoNut("Nap tien", "NAP"));
        row1.add(taoNut("Rut tien", "RUT"));
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(taoNut("Tong ket ngay", "TK_NGAY"));
        rows.add(row2);

        markup.setKeyboard(rows);
        sm.setReplyMarkup(markup);

        try {
            execute(sm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xuLyNutBam(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        User from = update.getCallbackQuery().getFrom();
        if (from == null) {
            return;
        }

        String username = from.getUserName();
        String tenHienThi = taoTenHienThi(from);
        kho.dangKyNguoiDung(chatId, username, tenHienThi);
        xacNhanCallback(update);

        if (data.equals("NAP")) {
            hanhDongDangCho.put(chatId, LOAI_NAP);
            guiTinNhan(chatId, "Nhap so tien can nap. Vi du: 500000 hoac 500000 luong");
        } else if (data.equals("RUT")) {
            hanhDongDangCho.put(chatId, LOAI_RUT);
            guiTinNhan(chatId, "Nhap so tien can rut. Vi du: 50000 hoac 50000 mua_do");
        } else if (data.equals("TK_NGAY")) {
            guiTinNhan(chatId, kho.layBaoCaoNgay(chatId));
        }
    }

    private void guiHuongDan(long chatId) {
        guiTinNhan(chatId,
                "Lenh hop le:\n"
                        + "- /start hoac /menu\n"
                        + "- nap <so_tien> <ghi_chu_tuy_chon>\n"
                        + "- rut <so_tien> <ghi_chu_tuy_chon>");
    }

    private String taoThongBaoKhongDuSoDu(double soDuHienTai) {
        return "Khong du so du.\nSo du hien tai: "
                + String.format("%,.0f", soDuHienTai)
                + " VND";
    }

    private void xacNhanCallback(Update update) {
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            execute(answer);
        } catch (Exception e) {
            AppConsole.println("Khong xac nhan duoc callback: " + e.getMessage());
        }
    }

    private InlineKeyboardButton taoNut(String t, String c) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(t);
        b.setCallbackData(c);
        return b;
    }

    private void guiTinNhan(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(text);
        try {
            execute(sm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String taoTenHienThi(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (first + " " + last).trim();
        if (!fullName.isEmpty()) return fullName;

        String username = user.getUserName();
        return username == null || username.isBlank() ? null : username;
    }
}
