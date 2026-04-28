package Model;

public class DanhMuc extends DoiTuongGoc {
    private String tenDanhMuc;
    private String loai;
    private String tenNguoiDung;

    public DanhMuc(long idTelegram, String ten, String loai, String tenNguoiDung) {
        this.setIdNguoiDungTelegram(idTelegram);
        this.tenNguoiDung = tenNguoiDung;
        this.tenDanhMuc = ten;
        this.loai = loai;
    }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public String getTenNguoiDung() { return tenNguoiDung; }
}
