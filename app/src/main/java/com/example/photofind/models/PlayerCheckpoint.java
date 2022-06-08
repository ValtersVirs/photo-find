package com.example.photofind.models;

public class PlayerCheckpoint {
    Player player;
    Checkpoint checkpoint;

    public PlayerCheckpoint(Player player, Checkpoint checkpoint) {
        this.player = player;
        this.checkpoint = checkpoint;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
    }
}
