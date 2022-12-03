package app;

import app.exceptions.TDERuntimeException;
import app.utils.TdeUtils;
import com.google.common.hash.Hashing;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import app.models.ChessMatch;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

import static com.mongodb.client.model.Indexes.descending;

public class MongoService {
    public void populateMongo() {
        try (MongoClient mongoClient = MongoClients.create()) {
            System.out.println("Transmitindo dados do csv para o mongoDb");
            MongoDatabase db = mongoClient.getDatabase("ucs");
            db.getCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName()).drop();
            db.createCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName());
            MongoCollection<Document> chessCollection = db.getCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName());
            try (RandomAccessFile dataFile = new RandomAccessFile(System.getProperty("user.dir") + "\\src\\main\\java\\app\\data\\chessStats.csv", "rw")) {
                TdeUtils.iterateOverFile(dataFile, (line, lineSize) -> {
                    ChessMatch chessMatch = new ChessMatch(line);
                    chessMatch.setWinnerUsername(encrypt(chessMatch.getWinnerUsername()));
                    chessCollection.insertOne(chessMatch.toMongoDocument());
                });
            } catch (IOException e) {
                throw new TDERuntimeException(e);
            }
        }
    }

    public void searchByPk() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Informe o código da partida:");
        int codigo = scanner.nextInt();

        try (MongoClient mongoClient = MongoClients.create()) {
            long start = System.currentTimeMillis();
            MongoDatabase db = mongoClient.getDatabase("ucs");
            MongoCollection<Document> chessCollection = db.getCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName());
            Document chessMatch = chessCollection.find(Filters.eq("id", codigo)).first();
            long end = System.currentTimeMillis();
            System.out.println(chessMatch == null ? "Nenhum resultado encontrado" : "Resultado encontrado:\n" + chessMatch);
            System.out.println(TdeUtils.calculateEstimatedTimeStr(start, end));
        }
    }

    public void showTopWinners() {
        try (MongoClient mongoClient = MongoClients.create()) {
            long start = System.currentTimeMillis();
            MongoDatabase db = mongoClient.getDatabase("ucs");
            MongoCollection<Document> chessCollection = db.getCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName());
            AggregateIterable<Document> aggregate = chessCollection.aggregate(Arrays.asList(Aggregates.group("$winnerUsername", Accumulators.sum("count", 1)), Aggregates.sort(descending("count"))));
            long end = System.currentTimeMillis();
            MongoCursor<Document> iterator = aggregate.iterator();
            for (int i = 0; i < 10; i++) {
                System.out.println(iterator.next());
            }
            System.out.println("Mostrar todos? (s/N)");
            Scanner scanner = new Scanner(System.in);
            String action = scanner.next();
            if (action.equalsIgnoreCase("s")) {
                aggregate.forEach(System.out::println);
            }
            System.out.println(TdeUtils.calculateEstimatedTimeStr(start, end));
        }
    }

    public void searchByName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o nome a ser procurado");
        String name = scanner.next();

        try (MongoClient mongoClient = MongoClients.create()) {
            long start = System.currentTimeMillis();
            MongoDatabase db = mongoClient.getDatabase("ucs");
            MongoCollection<Document> chessCollection = db.getCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName());
            FindIterable<Document> winnerUsername = chessCollection.find(Filters.eq("winnerUsername", encrypt(name)));
            long end = System.currentTimeMillis();
            winnerUsername.map(o -> {
                o.remove("winnerUsername");
                return o;
            }).forEach(System.out::println);
            System.out.println(TdeUtils.calculateEstimatedTimeStr(start, end));
        }
    }

    private String encrypt (String str) {
        return Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
    }

    public void findTotalMatchResults() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Qual resultado deseja coferir?");
        System.out.println("w - Brancas");
        System.out.println("b - Pretas");
        System.out.println("d - Empates");
        System.out.println("r - Desistências");
        String r = scanner.next().toLowerCase();
        TdeUtils.MATCH_RESULT result = TdeUtils.MATCH_RESULT.getResult(r);
        if (result.equals(TdeUtils.MATCH_RESULT.UNKNOWN)) {
            System.out.println("Resultado não reconhecido");
            return;
        }

        try (MongoClient mongoClient = MongoClients.create()) {
            long start = System.currentTimeMillis();
            MongoDatabase db = mongoClient.getDatabase("ucs");
            MongoCollection<Document> chessCollection = db.getCollection(TdeUtils.MONGO_COLLECTIONS.CHESS.getCollectionName());
            FindIterable<Document> results = chessCollection.find(Filters.eq("winner", result.getResult()));
            long end = System.currentTimeMillis();
            results.forEach(System.out::println);
            System.out.printf("Total: %d partidas%n", results.iterator().available());
            System.out.println(TdeUtils.calculateEstimatedTimeStr(start, end));
        }
    }
}
