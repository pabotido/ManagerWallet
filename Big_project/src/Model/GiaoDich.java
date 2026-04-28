package Model;

public class GiaoDich extends DoiTuongGoc {
    private double soTien;
    private String ghiChu;
    private int idDanhMuc;

    public GiaoDich(long idTelegram, double soTien, String ghiChu, int idDanhMuc) {
        this.setIdNguoiDungTelegram(idTelegram);
        this.soTien = soTien;
        this.ghiChu = ghiChu;
        this.idDanhMuc = idDanhMuc;
    }

    public double getSoTien() { return soTien; }
    public String getGhiChu() { return ghiChu; }
    public int getIdDanhMuc() { return idDanhMuc; }
}
