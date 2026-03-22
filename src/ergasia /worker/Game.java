package worker;

import java.io.Serializable;
import common.GameInfo;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameName;
    private String providerName;
    private int stars;
    private int noOfVotes;
    private String gameLogo;
    private double minBet;
    private double maxBet;
    private String riskLevel;
    private String hashKey;
    private boolean active;

    public Game(String gameName, String providerName, int stars, int noOfVotes,
                String gameLogo, double minBet, double maxBet,
                String riskLevel, String hashKey) {
        this.gameName = gameName;
        this.providerName = providerName;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.gameLogo = gameLogo;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.riskLevel = riskLevel;
        this.hashKey = hashKey;
        this.active = true;
    }

    public Game(GameInfo info) {
        this.gameName = info.getGameName();
        this.providerName = info.getProviderName();
        this.stars = info.getStars();
        this.noOfVotes = info.getNoOfVotes();
        this.gameLogo = info.getGameLogo();
        this.minBet = info.getMinBet();
        this.maxBet = info.getMaxBet();
        this.riskLevel = info.getRiskLevel();
        this.hashKey = info.getHashKey();
        this.active = true;
    }

    public String getGameName() { return gameName; }
    public String getProviderName() { return providerName; }
    public int getStars() { return stars; }
    public int getNoOfVotes() { return noOfVotes; }
    public String getGameLogo() { return gameLogo; }
    public double getMinBet() { return minBet; }
    public double getMaxBet() { return maxBet; }
    public String getRiskLevel() { return riskLevel; }
    public String getHashKey() { return hashKey; }
    public boolean isActive() { return active; }

    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setActive(boolean active) { this.active = active; }
}