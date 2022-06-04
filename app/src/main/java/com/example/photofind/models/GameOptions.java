package com.example.photofind.models;

public class GameOptions {
    Boolean joinAfterStart;
    String winnerBy;
    Integer timeLimit;

    public GameOptions() {}

    public GameOptions(Boolean joinAfterStart) {
        this.joinAfterStart = joinAfterStart;
    }

    public GameOptions(Boolean joinAfterStart, String winnerBy, Integer timeLimit) {
        this.joinAfterStart = joinAfterStart;
        this.winnerBy = winnerBy;
        this.timeLimit = timeLimit;
    }

    public Boolean getJoinAfterStart() {
        return joinAfterStart;
    }

    public void setJoinAfterStart(Boolean joinAfterStart) {
        this.joinAfterStart = joinAfterStart;
    }

    public String getWinnerBy() {
        return winnerBy;
    }

    public void setWinnerBy(String winnerBy) {
        this.winnerBy = winnerBy;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
}
