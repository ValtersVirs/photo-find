package com.example.photofind.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Player {
    final private DatabaseReference databaseRefPlayers = FirebaseDatabase.getInstance().getReference("players");
    final private DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");

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
        databaseRefGames.child(gameId + "/players/" + this.getId()).removeValue();
        databaseRefPlayers.child(this.getId()).removeValue();
    }
}
