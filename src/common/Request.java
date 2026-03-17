package common;

import java.io.Serializable;

public class Request implements Serializable {
    private RequestType type;
    private GameInfo gameInfo;

    public Request() {}

    public Request(RequestType type, GameInfo gameInfo) {
        this.type = type;
        this.gameInfo = gameInfo;
    }

    public RequestType getType() {
        return type;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }
}