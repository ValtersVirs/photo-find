package com.example.photofind.models;

import java.util.HashMap;

public class Player {
    private final Database database = new Database();

    String id;
    String name;
    HashMap<String, Boolean> checkpoints;

    public Player() {
    }

    public Player(String name, String id) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, Boolean> getCheckpoints() {
        return checkpoints;
    }

    public void removeFromGame(String gameId) {
        database.getGames().child(gameId + "/players/" + this.getId()).removeValue();
        database.getPlayers().child(this.getId()).removeValue();
    }
}
