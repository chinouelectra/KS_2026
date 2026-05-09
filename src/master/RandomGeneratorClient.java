package master;

import common.GameInfo;
import common.Response;
import rng.RandomRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RandomGeneratorClient {
    private final String host;
    private final int port;

    public RandomGeneratorClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Response registerGame(GameInfo gameInfo) {
        if (gameInfo == null) {
            return new Response(false, "GameInfo is null");
        }

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(RandomRequest.registerGame(gameInfo.getGameName(), gameInfo.getHashKey()));
            out.flush();

            return (Response) in.readObject();
        } catch (Exception e) {
            return new Response(false, "Failed to communicate with random generator: " + e.getMessage());
        }
    }
}
