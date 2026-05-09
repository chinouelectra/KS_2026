package rng;

import common.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RandomRequestHandler extends Thread {

    private final Socket client;
    private final RandomQueueRegistry registry;

    public RandomRequestHandler(Socket client, RandomQueueRegistry registry) {
        this.client = client;
        this.registry = registry;
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(client.getOutputStream());
            out.flush();

            in = new ObjectInputStream(client.getInputStream());

            RandomRequest request = (RandomRequest) in.readObject();
            out.writeObject(handle(request));
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
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Object handle(RandomRequest request) {
        if (request == null || request.getType() == null) {
            return new Response(false, "Invalid RNG request");
        }

        return switch (request.getType()) {
            case REGISTER_GAME -> handleRegisterGame(request);
            case GET_GAME_RANDOM -> handleNextRandom(request);
        };
    }

    private Response handleRegisterGame(RandomRequest request) {
        String result = registry.registerGame(request.getGameName(), request.getSecret());
        if ("Game queue registered".equals(result)) {
            return new Response(true, result);
        }
        return new Response(false, result);
    }

    private RandomResult handleNextRandom(RandomRequest request) {
        RandomResult rawResult = registry.nextRandom(request.getGameName());
        String hash = sha256(rawResult.getRandomNumber() + rawResult.getHash());
        return new RandomResult(rawResult.getRandomNumber(), hash);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
