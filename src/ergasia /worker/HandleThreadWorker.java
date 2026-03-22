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
}