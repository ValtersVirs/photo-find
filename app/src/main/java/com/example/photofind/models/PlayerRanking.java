package com.example.photofind.models;

public class PlayerRanking {
    private Player player;
    private Integer points;

    public PlayerRanking(Player player, Integer points) {
        this.player = player;
        this.points = points;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
