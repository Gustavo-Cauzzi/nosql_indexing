package app;

import app.models.TrieNode;
import app.utils.TdeUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.*;

public class MemoryIndexer {
    private String dataFileName;

    public MemoryIndexer() {
    }

    public MemoryIndexer(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public Optional<TrieNode> loadUsernameIndex() {
        TrieNode trie = new TrieNode();
        System.out.println("Carregando árvore Trie de nomes");

        try (RandomAccessFile f = new RandomAccessFile(dataFileName, "r")) {
            TdeUtils.iterateOverFile(f, line -> {
                String[] split = line.split(",");
                trie.insert(split[split.length - 1].trim());
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(trie);
    }

    public Map<TdeUtils.MATCH_RESULT, BigDecimal> loadMatchResultIndex () {
        System.out.println("Carregando índice de resultados");
        try (RandomAccessFile f = new RandomAccessFile(dataFileName, "r")) {
            EnumMap<TdeUtils.MATCH_RESULT, BigDecimal> indexMap = new EnumMap<>(TdeUtils.MATCH_RESULT.class);
            TdeUtils.iterateOverFile(f, line -> {
                String[] split = line.split(",");
                TdeUtils.MATCH_RESULT result = TdeUtils.MATCH_RESULT.getResult(split[1]);
                indexMap.put(result, indexMap.getOrDefault(result, BigDecimal.ZERO).add(BigDecimal.ONE));
            });
            return indexMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }
}
