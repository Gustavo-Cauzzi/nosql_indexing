package app.helpers;

public class IndexLineDTO {
    private long id;
    private String line;

    public IndexLineDTO(long id, String line) {
        this.id = id;
        this.line = line;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
