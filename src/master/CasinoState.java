package master;

import common.GameInfo;
import common.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CasinoState {
    private final Map<String, GameInfo> gamesByName = new LinkedHashMap<>();
    private final Map<String, Double> playerBalances = new LinkedHashMap<>();
    private final Map<String, Double> playerProfitLoss = new LinkedHashMap<>();
    private final Map<String, Double> providerProfitLoss = new LinkedHashMap<>();

    public synchronized Response addGame(GameInfo gameInfo) {
        if (gameInfo == null || gameInfo.getGameName() == null || gameInfo.getGameName().isBlank()) {
            return new Response(false, "Missing game info");
        }
        gamesByName.put(gameInfo.getGameName(), gameInfo);
        return new Response(true, "Game added/updated: " + gameInfo.getGameName());
    }

    public synchronized Response removeGame(String gameName) {
        GameInfo removed = gamesByName.remove(gameName);
        return removed == null
                ? new Response(false, "Game not found: " + gameName)
                : new Response(true, "Removed game: " + gameName);
    }

    public synchronized Response updateRisk(String gameName, String riskLevel) {
        GameInfo gameInfo = gamesByName.get(gameName);
        if (gameInfo == null) return new Response(false, "Game not found: " + gameName);
        gamesByName.put(gameName, gameInfo.withRiskLevel(riskLevel));
        return new Response(true, "Risk updated for " + gameName);
    }

    public synchronized Response updateBetLimits(String gameName, double minBet, double maxBet) {
        GameInfo gameInfo = gamesByName.get(gameName);
        if (gameInfo == null) return new Response(false, "Game not found: " + gameName);

        GameInfo updated = new GameInfo(gameInfo.getGameName(), gameInfo.getProviderName(), gameInfo.getStars(),
                gameInfo.getNoOfVotes(), gameInfo.getGameLogo(), minBet, maxBet, gameInfo.getRiskLevel(), gameInfo.getHashKey());
        gamesByName.put(gameName, updated);
        return new Response(true, "Bet limits updated for " + gameName);
    }

    public synchronized Response search(String providerName, String riskLevel, String betCategory, Integer minStars) {
        List<GameInfo> filtered = new ArrayList<>();
        for (GameInfo g : gamesByName.values()) {
            if (providerName != null && !providerName.isBlank() && !providerName.equalsIgnoreCase(g.getProviderName())) continue;
            if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equalsIgnoreCase(g.getRiskLevel())) continue;
            if (betCategory != null && !betCategory.isBlank() && !betCategory.equals(g.getBetCategory())) continue;
            if (minStars != null && g.getStars() < minStars) continue;
            filtered.add(g);
        }
        return new Response(true, "Found " + filtered.size() + " games", filtered);
    }

    public synchronized Response addBalance(String playerId, double amount) {
        double newBal = playerBalances.getOrDefault(playerId, 0.0) + amount;
        playerBalances.put(playerId, newBal);
        return new Response(true, "Balance for " + playerId + " = " + String.format("%.2f", newBal));
    }

    public synchronized Response placeBet(String playerId, String gameName, double betAmount) {
        GameInfo game = gamesByName.get(gameName);
        if (game == null) return new Response(false, "Game not found: " + gameName);
        if (betAmount < game.getMinBet() || betAmount > game.getMaxBet()) {
            return new Response(false, "Bet out of range [" + game.getMinBet() + ", " + game.getMaxBet() + "]");
        }

        double balance = playerBalances.getOrDefault(playerId, 0.0);
        if (balance < betAmount) return new Response(false, "Not enough balance");

        double payout = simulatePayout(game, betAmount);
        double net = payout - betAmount;
        playerBalances.put(playerId, balance + net);
        playerProfitLoss.merge(playerId, net, Double::sum);
        providerProfitLoss.merge(game.getProviderName(), -net, Double::sum);

        return new Response(true, "Bet settled. net=" + String.format("%.2f", net) +
                " newBalance=" + String.format("%.2f", playerBalances.get(playerId)));
    }

    public synchronized Response playerStats(String playerId) {
        Map<String, Double> totals = new LinkedHashMap<>();
        totals.put("Total Profit/Loss", playerProfitLoss.getOrDefault(playerId, 0.0));
        totals.put("Balance", playerBalances.getOrDefault(playerId, 0.0));
        return new Response(true, "Player stats", totals);
    }

    public synchronized Response providerStats(String providerName) {
        Map<String, Double> totals = new LinkedHashMap<>();
        totals.put("Total", providerProfitLoss.getOrDefault(providerName, 0.0));
        return new Response(true, "Provider stats", totals);
    }

    private double simulatePayout(GameInfo game, double betAmount) {
        int mod = ThreadLocalRandom.current().nextInt(100);
        if (mod == 0) {
            return betAmount * game.getJackpotMultiplier();
        }
        double coefficient = switch (game.getRiskLevel().toLowerCase()) {
            case "high" -> new double[]{0, 0, 0, 0, 0, 0, 0, 1.0, 2.0, 6.5}[mod % 10];
            case "medium" -> new double[]{0, 0, 0, 0, 0, 0, 0.5, 1.0, 1.5, 2.5}[mod % 10];
            default -> new double[]{0, 0, 0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5}[mod % 10];
        };
        return betAmount * coefficient;
    }
}
