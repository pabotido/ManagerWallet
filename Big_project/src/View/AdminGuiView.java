package View;

import Model.KhoTaiChinh;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class AdminGuiView extends JFrame {
    private final KhoTaiChinh kho;
    private final Runnable onExit;
    private final JLabel lblTongUser;
    private final JButton btnXoaUser;
    private final JButton btnSuaUser;
    private final JTextArea txtThongTin;
    private final JTextArea txtLog;
    private final DefaultTableModel userTableModel;
    private final DefaultTableModel giaoDichTableModel;
    private final JTable bangUser;
    private Long selectedUserId;
    private String selectedTenHienThi;

    public AdminGuiView(KhoTaiChinh kho, Runnable onExit) {
        this.kho = kho;
        this.onExit = onExit;

        setTitle("He thong Quan tri Admin");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit.run();
            }
        });

        lblTongUser = new JLabel("Tong so user: 0");
        lblTongUser.setHorizontalAlignment(SwingConstants.LEFT);
        lblTongUser.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnDemUser = new JButton("Cap nhat tong user");
        JButton btnTaiDanhSach = new JButton("Tai danh sach user");
        JButton btnThoat = new JButton("Thoat");
        btnXoaUser = new JButton("Xoa user da chon");
        btnSuaUser = new JButton("Sua ten hien thi");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(lblTongUser);
        topPanel.add(btnDemUser);
        topPanel.add(btnTaiDanhSach);
        topPanel.add(btnThoat);

        userTableModel = new DefaultTableModel(new Object[]{"ID TELEGRAM", "USERNAME", "TEN HIEN THI", "SO DU (VND)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bangUser = new JTable(userTableModel);
        bangUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userTableScroll = new JScrollPane(bangUser);

        txtThongTin = new JTextArea();
        txtThongTin.setEditable(false);
        JScrollPane thongTinScroll = new JScrollPane(txtThongTin);

        giaoDichTableModel = new DefaultTableModel(new Object[]{"THOI GIAN", "LOAI", "SO TIEN", "GHI CHU"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bangGiaoDich = new JTable(giaoDichTableModel);
        JScrollPane giaoDichScroll = new JScrollPane(bangGiaoDich);

        JSplitPane detailSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, thongTinScroll, giaoDichScroll);
        detailSplit.setResizeWeight(0.35);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userTableScroll, detailSplit);
        mainSplit.setResizeWeight(0.45);

        txtLog = new JTextArea(5, 40);
        txtLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(txtLog);

        JPanel thaoTacPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thaoTacPanel.add(btnXoaUser);
        thaoTacPanel.add(btnSuaUser);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(thaoTacPanel, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout(8, 8));
        add(topPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnDemUser.addActionListener(e -> capNhatTongUser());
        btnTaiDanhSach.addActionListener(e -> taiDanhSachUser());
        btnXoaUser.addActionListener(e -> xoaUser());
        btnSuaUser.addActionListener(e -> suaTenHienThi());
        btnThoat.addActionListener(e -> onExit.run());
        bangUser.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                taiChiTietUserDuocChon();
            }
        });

        btnXoaUser.setVisible(false);
        btnSuaUser.setVisible(false);

        capNhatTongUser();
        taiDanhSachUser();
    }

    private void capNhatTongUser() {
        int tong = kho.admin_DemUser();
        lblTongUser.setText("Tong so user: " + tong);
    }

    private void taiDanhSachUser() {
        List<KhoTaiChinh.UserSoDuRow> danhSach = kho.admin_LayTatCaUserVaSoDu();
        userTableModel.setRowCount(0);
        for (KhoTaiChinh.UserSoDuRow row : danhSach) {
            userTableModel.addRow(new Object[]{
                    row.telegramUserId(),
                    row.telegramUsername() == null ? "(chua co)" : "@" + row.telegramUsername(),
                    row.tenHienThi() == null ? "(chua co)" : row.tenHienThi(),
                    String.format("%,.0f", row.soDu())
            });
        }
        selectedUserId = null;
        selectedTenHienThi = null;
        btnXoaUser.setVisible(false);
        btnSuaUser.setVisible(false);
        txtThongTin.setText("Chon mot tai khoan de xem thong tin chi tiet.");
        giaoDichTableModel.setRowCount(0);
        ghiLog("Da tai danh sach user (" + danhSach.size() + " user).");
    }

    private void taiChiTietUserDuocChon() {
        int row = bangUser.getSelectedRow();
        if (row < 0) {
            return;
        }

        selectedUserId = Long.parseLong(String.valueOf(userTableModel.getValueAt(row, 0)));
        Object rawTen = userTableModel.getValueAt(row, 2);
        selectedTenHienThi = rawTen == null ? null : String.valueOf(rawTen);
        if ("(chua co)".equals(selectedTenHienThi)) selectedTenHienThi = null;

        KhoTaiChinh.UserDetail detail = kho.admin_LayThongTinUser(selectedUserId);
        List<KhoTaiChinh.GiaoDichRow> giaoDichRows = kho.admin_LayGiaoDichUser(selectedUserId);

        String username = detail.telegramUsername() == null ? "(chua co)" : "@" + detail.telegramUsername();
        String tenHienThi = detail.tenHienThi() == null ? "(chua co)" : detail.tenHienThi();
        String createdAt = detail.createdAt() == null ? "(khong ro)" : detail.createdAt();
        txtThongTin.setText(
                "ID: " + detail.telegramUserId() + "\n"
                        + "Username: " + username + "\n"
                        + "Ten hien thi: " + tenHienThi + "\n"
                        + "Ngay tao: " + createdAt + "\n"
                        + "So giao dich: " + detail.soGiaoDich() + "\n"
                        + "Tong nap: " + String.format("%,.0f VND", detail.tongNap()) + "\n"
                        + "Tong rut: " + String.format("%,.0f VND", detail.tongRut()) + "\n"
                        + "So du: " + String.format("%,.0f VND", detail.soDu())
        );

        giaoDichTableModel.setRowCount(0);
        for (KhoTaiChinh.GiaoDichRow gd : giaoDichRows) {
            String loai;
            if ("In".equalsIgnoreCase(gd.loai())) loai = "NAP";
            else if ("Out".equalsIgnoreCase(gd.loai())) loai = "RUT";
            else loai = "(khac)";
            giaoDichTableModel.addRow(new Object[]{
                    gd.thoiGian() == null ? "" : gd.thoiGian(),
                    loai,
                    String.format("%,.0f", gd.soTien()),
                    gd.ghiChu()
            });
        }
        btnXoaUser.setVisible(true);
        btnSuaUser.setVisible(true);
    }

    private void xoaUser() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Ban chua chon user.", "Thieu du lieu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ban chac chan muon xoa user " + selectedUserId + "?",
                "Xac nhan xoa",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        kho.admin_XoaUser(selectedUserId);
        ghiLog("Da xoa user: " + selectedUserId);
        capNhatTongUser();
        taiDanhSachUser();
    }

    private void suaTenHienThi() {
        if (selectedUserId == null) {
            JOptionPane.showMessageDialog(this, "Ban chua chon user.", "Thieu du lieu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String current = selectedTenHienThi == null ? "" : selectedTenHienThi;
        String tenMoi = JOptionPane.showInputDialog(this, "Nhap ten hien thi moi:", current);
        if (tenMoi == null) return;
        tenMoi = tenMoi.trim();
        if (tenMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ten hien thi khong duoc de trong.", "Loi du lieu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        kho.admin_CapNhatTenHienThiUser(selectedUserId, tenMoi);
        ghiLog("Da sua ten hien thi user " + selectedUserId + " -> " + tenMoi);
        long userId = selectedUserId;
        capNhatTongUser();
        taiDanhSachUser();
        chonLaiUser(userId);
    }

    private void chonLaiUser(long userId) {
        for (int i = 0; i < userTableModel.getRowCount(); i++) {
            long id = Long.parseLong(String.valueOf(userTableModel.getValueAt(i, 0)));
            if (id == userId) {
                bangUser.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void ghiLog(String text) {
        txtLog.append(text + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }
}
