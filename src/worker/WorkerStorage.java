package worker;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class WorkerStorage {

    private HashMap<String, Game> games;

    public WorkerStorage() {
        games = new HashMap<>();
    }

    // =========================
    // ADD GAME
    // =========================
    public synchronized String addGame(Game game) {
        if (game == null) {
            return "Game is null";
        }

        String gameName = game.getGameName();

        if (gameName == null || gameName.trim().isEmpty()) {
            return "Game name is empty";
        }

        if (games.containsKey(gameName)) {
            return "Game already exists";
        }

        games.put(gameName, game);
        return "Game added successfully";
    }

    // =========================
    // REMOVE GAME (set inactive)
    // =========================
    public synchronized String removeGame(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            return "Game name is empty";
        }

        Game game = games.get(gameName);

        if (game == null) {
            return "Game not found";
        }

        game.setActive(false);
        return "Game removed successfully (set inactive)";
    }

    // =========================
    // UPDATE RISK
    // =========================
    public synchronized String updateRisk(String gameName, String newRisk) {
        if (gameName == null || gameName.trim().isEmpty()) {
            return "Game name is empty";
        }

        Game game = games.get(gameName);

        if (game == null) {
            return "Game not found";
        }

        game.setRiskLevel(newRisk);
        return "Risk updated successfully";
    }

    // =========================
    // GET GAME
    // =========================
    public synchronized Game getGame(String gameName) {
        return games.get(gameName);
    }

    // =========================
    // GET ALL GAMES
    // =========================
    public synchronized HashMap<String, Game> getAllGames() {
        return games;
    }

    // =========================
    // SEARCH (πολύ σημαντικό)
    // =========================
    public synchronized List<Game> getActiveGames() {
        List<Game> result = new ArrayList<>();

        for (Game game : games.values()) {
            if (game.isActive()) {
                result.add(game);
            }
        }

        return result;
    }
}