package app;

import app.exceptions.TDEException;
import app.helpers.WinnersDTO;
import app.models.PkIndex;
import app.models.TrieNode;
import app.utils.TdeUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String FILE_NAME = System.getProperty("user.dir") + "\\src\\main\\java\\app\\data\\chessStats.csv";
    private static final FileIndexer fileIndexers = new FileIndexer(FILE_NAME);
    private static final MemoryIndexer secondaryIndexer = new MemoryIndexer(FILE_NAME);

    public static void main(String[] args) throws TDEException {
        int action;

        MongoService mongoService = new MongoService();

        // Em arquivo
        Map<String, List<String>> playerMatchesIndex = fileIndexers.loadPlayerMatchesIndex();
        List<PkIndex> pkIndex = fileIndexers.loadPrimaryKeyIndex();
        // Em memória
        TrieNode winnerNamesTrie = secondaryIndexer.loadUsernameIndex()
            .orElseThrow(() -> new TDEException("Não foi possível gerar o índice de nomes com árvore Trie"));
        Map<TdeUtils.MATCH_RESULT, BigDecimal> matchResultIndex = secondaryIndexer.loadMatchResultIndex();

        do {
            System.out.println("Qual ação você deseja executar?");
            System.out.println("0 - Sair");
            System.out.println("1 - Procurar por código");
            System.out.println("2 - Ver maiores ganhadores");
            System.out.println("3 - Procurar por nome");
            System.out.println("4 - Procurar partidas de um jogador");
            System.out.println("5 - Checar quantidade de resultados");
            System.out.println("------------------------");
            System.out.println("Operações com MongoDb:");
            System.out.println("6 - Buscar por código");
            System.out.println("7 - Ver maiores ganhadores");
            System.out.println("8 - Procurar por nome");
            System.out.println("9 - Checar quantidade de resultados");
            System.out.println("------------------------");
            System.out.println("10 - Recalcular arquivo de índice primário");
            System.out.println("11 - Recalcular arquivo de índice de partidas de jogadores");
            System.out.println("12 - Recalcular árvore Trie de nomes de jogadores");
            System.out.println("13 - Recalcular índice de resultados");
            System.out.println("14 - Repopular MongoDB");
            action = scanner.nextInt();

            if (action == 1) {
                searchByPk(pkIndex);
            } else if (action == 2) {
                showTopWinners(winnerNamesTrie);
            } else if (action == 3) {
                findName(winnerNamesTrie);
            } else if (action == 4) {
                findPlayerMatches(playerMatchesIndex);
            } else if (action == 5) {
                findTotalMatchResults(matchResultIndex);
            } else if (action == 6) {
                mongoService.searchByPk();
            } else if (action == 7) {
                mongoService.showTopWinners();
            } else if (action == 8) {
                mongoService.searchByName();
            } else if (action == 9) {
                mongoService.findTotalMatchResults();
            }else if (action == 10) {
                pkIndex = fileIndexers.loadPrimaryKeyIndex(true);
            }  else if (action == 11) {
                playerMatchesIndex = fileIndexers.loadPlayerMatchesIndex(true);
            }  else if (action == 12) {
                winnerNamesTrie = secondaryIndexer.loadUsernameIndex()
                    .orElseThrow(() -> new TDEException("Não foi possível gerar o índice de nomes com árvore Trie"));
            }  else if (action == 13) {
                matchResultIndex = secondaryIndexer.loadMatchResultIndex();
            }  else if (action == 14) {
                mongoService.populateMongo();
            } else if (action != 0) {
                System.err.println("Ação não reconhecida");
            }
        } while (action != 0);
    }

    private static void findTotalMatchResults(Map<TdeUtils.MATCH_RESULT, BigDecimal> matchResultIndex) {
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
        System.out.println("Total de resultados: " + matchResultIndex.getOrDefault(result, BigDecimal.ZERO));
    }

    private static void findPlayerMatches(Map<String, List<String>> playerMatchesIndex) {
        System.out.println("Digite o nome do jogador:");
        String name = scanner.next();
        if (!playerMatchesIndex.containsKey(name)) {
            System.out.println("Jogador não encontrado!");
            return;
        }
        List<String> addresses = playerMatchesIndex.get(name);
        fileIndexers.findByAdresses(addresses).forEach(System.out::println);
    }

    private static void findName(TrieNode winnerNamesTrie) {
        System.out.println("Digite o nome a ser pesquisado:");
        String name = scanner.next();
        System.out.println(
            winnerNamesTrie.find(name)
                .map(result -> String.format("Nome: %s%n Vitórias: %d", result.getName(), result.getWins()))
                .orElse("Não encontrado")
        );
    }

    private static void showTopWinners(TrieNode winnerNamesTrie) {
        System.out.println("Top de maiores ganhadores da plataforma:");
        long start = System.currentTimeMillis();
        List<WinnersDTO> topWinners = winnerNamesTrie.getTopWinners();
        long end = System.currentTimeMillis();
        topWinners.subList(0, 9)
            .forEach(winner -> System.out.printf("%s - %d%n", winner.getName(), winner.getWins()));
        System.out.println("Mostrar todos? (s/n)");
        String action = scanner.next();
        if (action.equalsIgnoreCase("s")) {
            topWinners.forEach(winner -> System.out.printf("%s - %d%n", winner.getName(), winner.getWins()));
        }
        System.out.println(TdeUtils.calculateEstimatedTimeStr(start, end));
    }

    private static void searchByPk(List<PkIndex> pkIndex) {
        System.out.println("Digite o código do registro a ser procurado");
        long code = scanner.nextLong();
        long start = System.currentTimeMillis();
        System.out.println(
            fileIndexers.findByPk(code, pkIndex)
                .map(chessMatchData -> "Dado encontrado:\n" + chessMatchData)
                .orElse("Dado não encontrado")
        );
        long end = System.currentTimeMillis();
        System.out.println(TdeUtils.calculateEstimatedTimeStr(start, end));
    }
}