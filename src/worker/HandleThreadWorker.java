package worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Request;
import common.RequestType;
import common.Response;
import common.GameInfo;

public class HandleThreadWorker extends Thread {

    private Socket client;
    private WorkerStorage storage;

    public HandleThreadWorker(Socket client, WorkerStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());

            Request request = (Request) in.readObject();
            Response response = handleRequest(request);

            out.writeObject(response);
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (client != null && !client.isClosed()) client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response handleRequest(Request request) {
        if (request == null) {
            return new Response(false, "Request is null");
        }

        RequestType type = request.getType();
        GameInfo gameInfo = request.getGameInfo();

        if (type == null) {
            return new Response(false, "Request type is null");
        }

        switch (type) {
            case ADD_GAME:
                return handleAddGame(gameInfo);

            case REMOVE_GAME:
                return handleRemoveGame(gameInfo);

            case UPDATE_RISK:
                return handleUpdateRisk(gameInfo);
            case SEARCH_GAMES:
                return handleSearchGames();
            case PLAY_GAME:
                return handlePlayGame(gameInfo);

            default:
                return new Response(false, "Unsupported request type: " + type);
        }
    }

    private Response handleAddGame(GameInfo gameInfo) {
    if (gameInfo == null) {
        return new Response(false, "GameInfo is null");
    }

    Game game = new Game(gameInfo);
    String result = storage.addGame(game);

    if (result.equals("Game added successfully")) {
        return new Response(true, result, gameInfo);
    }

    return new Response(false, result);
}
    private Response handleRemoveGame(GameInfo gameInfo) {
        if (gameInfo == null) {
            return new Response(false, "GameInfo is null");
        }

        String gameName = gameInfo.getGameName();
        String result = storage.removeGame(gameName);

        if (result.equals("Game removed successfully (set inactive)")) {
            return new Response(true, result);
        }

        return new Response(false, result);
    }

    private Response handleUpdateRisk(GameInfo gameInfo) {
        if (gameInfo == null) {
            return new Response(false, "GameInfo is null");
        }

        String gameName = gameInfo.getGameName();
        String newRisk = gameInfo.getRiskLevel();

        String result = storage.updateRisk(gameName, newRisk);

        if (result.equals("Risk updated successfully")) {
            return new Response(true, result);
        }

        return new Response(false, result);
    }
    private Response handleSearchGames() {
    return new Response(true, "Search results", storage.getActiveGames());
}

private Response handlePlayGame(GameInfo gameInfo) {
    if (gameInfo == null) {
        return new Response(false, "GameInfo is null");
    }

    String gameName = gameInfo.getGameName();
    double betAmount = gameInfo.getBetAmount();

    if (gameName == null || gameName.trim().isEmpty()) {
        return new Response(false, "Game name is empty");
    }

    Game game = storage.getGame(gameName);

    if (game == null) {
        return new Response(false, "Game not found");
    }

    if (!game.isActive()) {
        return new Response(false, "Game is inactive");
    }

    if (betAmount < game.getMinBet() || betAmount > game.getMaxBet()) {
        return new Response(false, "Bet amount is outside allowed range");
    }

    double payout = calculatePayout(game, betAmount);

    synchronized (game) {
        game.addToTotalBetAmount(betAmount);
        game.addToTotalPayoutAmount(payout);
    }

    String message = "Play successful. Bet: " + betAmount + ", Payout: " + payout;
    return new Response(true, message);
}
private double calculatePayout(Game game, double betAmount) {
    double[] multipliers;

    if (game.getRiskLevel().equalsIgnoreCase("low")) {
        multipliers = new double[]{0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
    } else if (game.getRiskLevel().equalsIgnoreCase("medium")) {
        multipliers = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5};
    } else {
        multipliers = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 6.5};
    }

    int randomNumber = (int) (Math.random() * 1000);

    if (randomNumber % 100 == 0) {
        return betAmount * game.getJackpot();
    }

    int index = randomNumber % 10;
    return betAmount * multipliers[index];
}
}