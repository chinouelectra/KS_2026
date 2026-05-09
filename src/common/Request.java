package common;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Request implements Serializable {
    private final RequestType type;
    private final GameInfo gameInfo;
    private final String gameName;
    private final String providerName;
    private final String playerId;
    private final String jobId;
    private final String reducerHost;
    private final String riskLevel;
    private final String betCategory;
    private final Double minBet;
    private final Double maxBet;
    private final Double betAmount;
    private final Integer minStars;
    private final Integer reducerPort;
    private final Integer expectedResults;
    private final Map<String, Double> partialTotals;

    public Request(RequestType type, GameInfo gameInfo, String gameName, String providerName,
                   String playerId, String jobId, String reducerHost, String riskLevel, String betCategory,
                   Double minBet, Double maxBet, Double betAmount, Integer minStars,
                   Integer reducerPort, Integer expectedResults,
                   Map<String, Double> partialTotals) {
        this.type = type;
        this.gameInfo = gameInfo;
        this.gameName = gameName;
        this.providerName = providerName;
        this.playerId = playerId;
        this.jobId = jobId;
        this.reducerHost = reducerHost;
        this.riskLevel = riskLevel;
        this.betCategory = betCategory;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.betAmount = betAmount;
        this.minStars = minStars;
        this.reducerPort = reducerPort;
        this.expectedResults = expectedResults;
        this.partialTotals = partialTotals == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(partialTotals));
    }

    public static Request addGame(GameInfo gameInfo) {
        return new Request(
                RequestType.ADD_GAME,
                gameInfo,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request removeGame(String gameName) {
        return new Request(
                RequestType.REMOVE_GAME,
                null,
                gameName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request updateGameRisk(String gameName, String riskLevel) {
        return new Request(
                RequestType.UPDATE_GAME_RISK,
                null,
                gameName,
                null,
                null,
                null,
                null,
                riskLevel,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request updateGameBetLimits(String gameName, double minBet, double maxBet) {
        return new Request(
                RequestType.UPDATE_GAME_BET_LIMITS,
                null,
                gameName,
                null,
                null,
                null,
                null,
                null,
                null,
                minBet,
                maxBet,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request providerStats(String providerName) {
        return new Request(
                RequestType.GET_PROVIDER_STATS,
                null,
                null,
                providerName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request playerStats(String playerId) {
        return new Request(
                RequestType.GET_PLAYER_STATS,
                null,
                null,
                null,
                playerId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request getAllGames() {
        return new Request(
                RequestType.GET_ALL_GAMES,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Request searchGames(String playerId, String providerName, String riskLevel, String betCategory, Integer minStars) {
        return new Request(
                RequestType.SEARCH_GAMES,
                null,
                null,
                providerName,
                playerId,
                null,
                null,
                riskLevel,
                betCategory,
                null,
                null,
                null,
                minStars,
                null,
                null,
                null
        );
    }

    public static Request placeBet(String playerId, String gameName, double betAmount) {
        return new Request(
                RequestType.PLACE_BET,
                null,
                gameName,
                null,
                playerId,
                null,
                null,
                null,
                null,
                null,
                null,
                betAmount,
                null,
                null,
                null,
                null
        );
    }

    public static Request addBalance(String playerId, double amount) {
        return new Request(
                RequestType.ADD_BALANCE,
                null,
                null,
                null,
                playerId,
                null,
                null,
                null,
                null,
                null,
                null,
                amount,
                null,
                null,
                null,
                null
        );
    }

    public static Request initProviderReduceJob(String providerName, String jobId, int expectedResults) {
        return new Request(
                RequestType.INIT_PROVIDER_REDUCE_JOB,
                null,
                null,
                providerName,
                null,
                jobId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                expectedResults,
                null
        );
    }

    public static Request initPlayerReduceJob(String playerId, String jobId, int expectedResults) {
        return new Request(
                RequestType.INIT_PLAYER_REDUCE_JOB,
                null,
                null,
                null,
                playerId,
                jobId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                expectedResults,
                null
        );
    }

    public static Request providerMapTask(String providerName, String jobId, String reducerHost, int reducerPort) {
        return new Request(
                RequestType.MAP_PROVIDER_STATS,
                null,
                null,
                providerName,
                null,
                jobId,
                reducerHost,
                null,
                null,
                null,
                null,
                null,
                null,
                reducerPort,
                null,
                null
        );
    }

    public static Request playerMapTask(String playerId, String jobId, String reducerHost, int reducerPort) {
        return new Request(
                RequestType.MAP_PLAYER_STATS,
                null,
                null,
                null,
                playerId,
                jobId,
                reducerHost,
                null,
                null,
                null,
                null,
                null,
                null,
                reducerPort,
                null,
                null
        );
    }

    public static Request submitProviderMapResult(String providerName, String jobId, Map<String, Double> partialTotals) {
        return new Request(
                RequestType.SUBMIT_PROVIDER_MAP_RESULT,
                null,
                null,
                providerName,
                null,
                jobId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                partialTotals
        );
    }

    public static Request submitPlayerMapResult(String playerId, String jobId, Map<String, Double> partialTotals) {
        return new Request(
                RequestType.SUBMIT_PLAYER_MAP_RESULT,
                null,
                null,
                null,
                playerId,
                jobId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                partialTotals
        );
    }

    public RequestType getType() { return type; }
    public GameInfo getGameInfo() { return gameInfo; }
    public String getGameName() { return gameName; }
    public String getProviderName() { return providerName; }
    public String getPlayerId() { return playerId; }
    public String getJobId() { return jobId; }
    public String getReducerHost() { return reducerHost; }
    public String getRiskLevel() { return riskLevel; }
    public String getBetCategory() { return betCategory; }
    public Double getMinBet() { return minBet; }
    public Double getMaxBet() { return maxBet; }
    public Double getBetAmount() { return betAmount; }
    public Integer getMinStars() { return minStars; }
    public Integer getReducerPort() { return reducerPort; }
    public Integer getExpectedResults() { return expectedResults; }
    public Map<String, Double> getPartialTotals() { return partialTotals; }
}
