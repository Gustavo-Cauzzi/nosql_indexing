package app.utils;

import app.exceptions.TDEException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

public class TdeUtils {
    public enum MONGO_COLLECTIONS {
        CHESS("chess_matches");

        private final String collectionName;

        MONGO_COLLECTIONS(final String collectionName) {
            this.collectionName = collectionName;
        }

        public String getCollectionName() {
            return collectionName;
        }
    }

    private TdeUtils() {
    }

    public static <T> List<List<T>> splitInHalf(final List<T> list) {
        int midIndex = ((list.size() / 2) - (((list.size() % 2) > 0) ? 0 : 1));

        List<T> a = new ArrayList<>();
        List<T> b = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i <= midIndex) {
                a.add(list.get(i));
            } else {
                b.add(list.get(i));
            }
        }

        return List.of(a, b);
    }

    public static Long extractId(final String line) {
        return Long.parseLong(line.split(",")[0]);
    }

    public static void iterateOverFile(RandomAccessFile file, Consumer<String> consumer) throws IOException {
        iterateOverFile(file, (line, lineSize) -> consumer.accept(line));
    }

    public static void iterateOverFile(RandomAccessFile file, CheckedFunction<String> consumer) throws IOException {
        try {
            String line;
            do {
                long initialFp = file.getFilePointer();
                line = file.readLine();
                if (line != null) consumer.accept(line, file.getFilePointer() - initialFp);
            } while (line != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface CheckedFunction<T> {
        void accept(T t, long value) throws Exception;
    }

    public enum MATCH_RESULT {
        WHITE('w', "White"), BLACK('b', "Black"), DRAW('d', "Draw"), RESIGN('r', "Resign"), UNKNOWN('u', "Unknown");
        private final Character code;
        private final String result;

        MATCH_RESULT(Character c, String result) {
            this.code = c;
            this.result = result;
        }

        public Character getCode() {
            return code;
        }

        public String getResult() {
            return result;
        }


        public static MATCH_RESULT getResult(String s) {
            return getResult(s.charAt(0));
        }

        public static MATCH_RESULT getResult(Character c) {
            return Arrays.stream(MATCH_RESULT.values()).filter(matchResult -> matchResult.code.equals(c)).findFirst().orElse(MATCH_RESULT.UNKNOWN);
        }
    }

    public static String calculateEstimatedTimeStr(long start, long end) {
        return String.format("Econtrado em %s segundos", BigDecimal.valueOf(end - start).divide(BigDecimal.valueOf(1000), MathContext.DECIMAL128));
    }
}
