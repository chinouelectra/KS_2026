package rng;

import java.io.Serializable;

public class RandomRequest implements Serializable {
    private final RandomRequestType type;
    private final String gameName;
    private final String secret;

    public RandomRequest(RandomRequestType type, String gameName, String secret) {
        this.type = type;
        this.gameName = gameName;
        this.secret = secret;
    }

    public static RandomRequest registerGame(String gameName, String secret) {
        return new RandomRequest(RandomRequestType.REGISTER_GAME, gameName, secret);
    }

    public static RandomRequest nextRandom(String gameName) {
        return new RandomRequest(RandomRequestType.GET_GAME_RANDOM, gameName, null);
    }

    public RandomRequestType getType() {
        return type;
    }

    public String getGameName() {
        return gameName;
    }

    public String getSecret() {
        return secret;
    }
}
