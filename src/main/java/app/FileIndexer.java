package app;

import app.exceptions.TDEException;
import app.exceptions.TDERuntimeException;
import app.models.ChessMatch;
import app.models.PkIndex;
import app.utils.TdeUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Stream;

public class FileIndexer {
    private final String dataFileName;
    private static final String PRIMARY_KEY_INDEX_FILE_NAME = System.getProperty("user.dir") + "\\src\\main\\java\\app\\data\\pkIndex.csv";
    private static final String PLAYER_INDEX_FILE_NAME = System.getProperty("user.dir") + "\\src\\main\\java\\app\\data\\playerIndex.csv";

    public FileIndexer(String fileName) {
        this.dataFileName = fileName;
    }

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public List<PkIndex> loadPrimaryKeyIndex() {
        return this.loadPrimaryKeyIndex(false);
    }

    public List<PkIndex> loadPrimaryKeyIndex(boolean forceReexec) {
        try {
            File indexFile = new File(PRIMARY_KEY_INDEX_FILE_NAME);

            if (forceReexec || !indexFile.exists()) {
                if (!forceReexec && !indexFile.createNewFile()) {
                    throw new TDEException("Não foi possível criar o arquivo de índice de chave primária");
                }

                return this.populatePrimaryIndexFile();
            }

            System.out.println("Lendo arquivo de índice primário");
            try (RandomAccessFile indexFileReader = new RandomAccessFile(indexFile, "r")) {
                List<PkIndex> pkIndex = new ArrayList<>();
                TdeUtils.iterateOverFile(indexFileReader, line -> pkIndex.add(new PkIndex(line)));
                return pkIndex;
            }
        } catch (IOException | TDEException e) {
            System.err.println("Não foi possível abrir o arquivo de índice primário");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<PkIndex> populatePrimaryIndexFile() throws IOException {
        System.out.println("Criando arquivo de índice primário");

        try (RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "r"); RandomAccessFile indexFile = new RandomAccessFile(PRIMARY_KEY_INDEX_FILE_NAME, "rw")) {
            List<String> index = new ArrayList<>();
            TdeUtils.iterateOverFile(dataFile, (line, lineSize) -> {
                try {
                    index.add(TdeUtils.extractId(line) + "," + padLeftZeros(String.valueOf(dataFile.getFilePointer() - lineSize), 10));
                } catch (IOException e) {
                    throw new TDERuntimeException(e);
                }
            });

            return index.stream()
                .sorted(Comparator.comparing(TdeUtils::extractId))
                .map(line -> {
                    try {
                        indexFile.write((line + "\n").getBytes());
                        return line;
                    } catch (IOException e) {
                        throw new TDERuntimeException(e);
                    }
                })
                .map(PkIndex::new)
                .toList();
        }
    }

    public Optional<ChessMatch> findByPk(long code, List<PkIndex> indexList) {
        try (RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "r"); RandomAccessFile indexFile = new RandomAccessFile(PRIMARY_KEY_INDEX_FILE_NAME, "r")) {
            int indexFileLineSize = 19;
            int lineQtd = (int) (indexFile.length() / indexFileLineSize);

            PkIndex index = this.binarySearch(indexList, 0, lineQtd, code);

            if (index == null) {
                return Optional.empty();
            }

            dataFile.seek(index.getAddress());
            String dataLine = dataFile.readLine();
            if (dataLine == null) {
                throw new TDEException(String.format("Não foi possível encontrar dado original no arquivo de dados (%s)", index));
            }

            return Optional.of(new ChessMatch(dataLine));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private PkIndex binarySearch(List<PkIndex> indexList, int start, int end, long idToSearch) {
        if (end >= start) {
            int mid = start + (end - start) / 2;
            System.out.printf("Pesquisa binária de %d a %d (mid: %d)%n", start, end, mid);

            PkIndex data = indexList.get(mid);

            if (data.getId() == idToSearch) {
                return data;
            } else if (data.getId() > idToSearch) {
                return binarySearch(indexList, start, mid - 1, idToSearch);
            } else {
                return binarySearch(indexList, mid + 1, end, idToSearch);
            }
        }

        return null;
    }

    public Map<String, List<String>> loadPlayerMatchesIndex() {
        return this.loadPlayerMatchesIndex(false);
    }

    public Map<String, List<String>> loadPlayerMatchesIndex(boolean force) {
        File playerIndex = new File(PLAYER_INDEX_FILE_NAME);

        try {
            if (force || !playerIndex.exists()) {
                if (!force && !playerIndex.createNewFile()) {
                    throw new TDEException("Não foi possível criar o arquivo de índice de partidas do jogador");
                }

                return this.populatePlayerMatchesIndexFile();
            }

            System.out.println("Lendo index de partidas do jogador");
            try (RandomAccessFile indexFile = new RandomAccessFile(playerIndex, "rw")) {
                HashMap<String, List<String>> indexMap = new HashMap<>();
                TdeUtils.iterateOverFile(indexFile, line -> {
                    List<String> split = new ArrayList<>(List.of(line.split(",")));
                    String name = split.remove(0);
                    indexMap.put(name, split);
                });
                return indexMap;
            }
        } catch (TDEException | IOException e) {
            System.err.println("Não foi possível abrir o arquivo de índice de partidas do jogador");
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    private Map<String, List<String>> populatePlayerMatchesIndexFile() throws IOException {
        System.out.println("Criando índice de partidas do jogador");

        HashMap<String, List<String>> playerAddressesMap = new HashMap<>();
        try (RandomAccessFile playerIndex = new RandomAccessFile(PLAYER_INDEX_FILE_NAME, "rw"); RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "r")) {
            TdeUtils.iterateOverFile(dataFile, (line, lineSize) -> {
                String[] split = line.split(",");
                String name = split[split.length - 1].trim();
                List<String> playerAddresses = playerAddressesMap.getOrDefault(name, Collections.emptyList());
                playerAddressesMap.put(
                    name,
                    Stream.concat(
                        playerAddresses.stream(),
                        Stream.of(
                            padLeftZeros(
                                String.valueOf(dataFile.getFilePointer() - lineSize), 10)
                        )
                    ).toList()
                );
            });

            playerAddressesMap.forEach((key, value) -> {
                try {
                    playerIndex.write((key + "," + String.join(",", value) + "\n").getBytes());
                } catch (IOException e) {
                    throw new TDERuntimeException(e);
                }
            });

            return playerAddressesMap;
        }
    }

    public List<ChessMatch> findByAdresses(List<String> addresses) {
        try (RandomAccessFile f = new RandomAccessFile(dataFileName, "r")) {
            return addresses.stream().map(address -> {
                try {
                    f.seek(Long.parseLong(address));
                    String line = f.readLine();
                    if (line == null) {
                        throw new TDERuntimeException("Linha não existe");
                    }
                    return new ChessMatch(line);
                } catch (IOException | TDEException e) {
                    throw new TDERuntimeException(e);
                }
            }).toList();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
