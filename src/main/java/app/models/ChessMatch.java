package app.models;

import app.exceptions.TDEException;
import app.utils.TdeUtils;
import org.bson.Document;

public class ChessMatch {
    private Long id;
    private TdeUtils.MATCH_RESULT winner;
    private Integer winnerRating;
    private Integer loserRating;
    private String winnerUsername;

    public ChessMatch(String line) throws TDEException {
        String[] split = line.split(",");

        if (split.length != 5) throw new TDEException("Formato inválido");

        this.id = Long.parseLong(split[0]);
        this.winner = TdeUtils.MATCH_RESULT.getResult(split[1]);
        this.winnerRating = Integer.parseInt(split[2]);
        this.loserRating = Integer.parseInt(split[3]);
        this.winnerUsername = split[4].trim();
    }

    public Document toMongoDocument () {
        Document document = new Document();
        document.put("id", id);
        document.put("winner", winner.getResult());
        document.put("winnerRating", winnerRating);
        document.put("loserRating", loserRating);
        document.put("winnerUsername", winnerUsername);
        return document;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TdeUtils.MATCH_RESULT getWinner() {
        return winner;
    }

    public void setWinner(TdeUtils.MATCH_RESULT winner) {
        this.winner = winner;
    }

    public Integer getWinnerRating() {
        return winnerRating;
    }

    public void setWinnerRating(Integer winnerRating) {
        this.winnerRating = winnerRating;
    }

    public Integer getLoserRating() {
        return loserRating;
    }

    public void setLoserRating(Integer loserRating) {
        this.loserRating = loserRating;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }

    @Override
    public String toString() {
        return "ChessMatch{" +
                "id=" + id +
                ", winner='" + winner.getResult() + '\'' +
                ", winnerRating=" + winnerRating +
                ", loserRating=" + loserRating +
                ", winnerUsername='" + winnerUsername + '\'' +
                '}';
    }
}
