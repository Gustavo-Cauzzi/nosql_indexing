package app.helpers;

public class WinnersDTO {
    private final String name;
    private final Integer wins;

    public WinnersDTO(String name, Integer wins) {
        this.name = name;
        this.wins = wins;
    }

    public String getName() {
        return name;
    }

    public Integer getWins() {
        return wins;
    }

    @Override
    public String toString() {
        return "WinnersDTO{" +
                "name='" + name + '\'' +
                ", wins=" + wins +
                '}';
    }
}
