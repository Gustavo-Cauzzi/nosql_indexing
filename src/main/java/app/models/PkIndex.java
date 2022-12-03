package app.models;

public class PkIndex {
    private Long id;
    private Long address;
    private String line;

    public PkIndex(String line) {
        String[] split = line.split(",");

        if (split.length != 2) {
            System.err.println("Formato não identificado");
            return;
        }

        this.id = Long.parseLong(split[0]);
        this.address = Long.parseLong(split[1]);
        this.line = line;
    }

    public PkIndex(Long id, Long address) {
        this.id = id;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAddress() {
        return address;
    }

    public void setAddress(Long address) {
        this.address = address;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "PkIndex{" +
                "id=" + id +
                ", address=" + address +
                '}';
    }
}
