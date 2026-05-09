package rng;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;

public class RandomQueueRegistry {
    private static final int PREFILL_SIZE = 20;

    private final Map<String, GameRandomQueue> queues = new HashMap<>();
    private final Random random = new Random();

    public synchronized String registerGame(String gameName, String secret) {
        if (gameName == null || gameName.trim().isEmpty()) {
            return "Game name is empty";
        }
        if (secret == null || secret.trim().isEmpty()) {
            return "Secret is empty";
        }

        GameRandomQueue gameQueue = new GameRandomQueue(secret);
        fillQueue(gameQueue);
        queues.put(gameName, gameQueue);
        return "Game queue registered";
    }

    public synchronized RandomResult nextRandom(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new IllegalArgumentException("Game name is empty");
        }

        GameRandomQueue gameQueue = queues.get(gameName);
        if (gameQueue == null) {
            throw new IllegalArgumentException("Game queue not found for " + gameName);
        }

        if (gameQueue.numbers.isEmpty()) {
            fillQueue(gameQueue);
        }

        int number = gameQueue.numbers.remove();
        if (gameQueue.numbers.size() < 5) {
            fillQueue(gameQueue);
        }

        return new RandomResult(number, gameQueue.secret);
    }

    private void fillQueue(GameRandomQueue gameQueue) {
        while (gameQueue.numbers.size() < PREFILL_SIZE) {
            gameQueue.numbers.add(random.nextInt(1000));
        }
    }

    private static class GameRandomQueue {
        private final String secret;
        private final Queue<Integer> numbers = new LinkedList<>();

        private GameRandomQueue(String secret) {
            this.secret = secret;
        }
    }
}
