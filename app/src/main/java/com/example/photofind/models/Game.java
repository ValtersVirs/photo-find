package com.example.photofind.models;

import java.util.HashMap;

public class Game {
    String id;
    String name;
    String code;
    Boolean started;
    Boolean ended;
    Boolean joinAfterStart;
    HashMap<String, Boolean> players;
    HashMap<String, Boolean> checkpoints;

    public Game() {}

    public Game(String id, String name, String code, Boolean started, Boolean ended, Boolean joinAfterStart) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.started = started;
        this.ended = ended;
        this.joinAfterStart = joinAfterStart;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public Boolean getJoinAfterStart() {
        return joinAfterStart;
    }

    public void setJoinAfterStart(Boolean joinAfterStart) {
        this.joinAfterStart = joinAfterStart;
    }

    public HashMap<String, Boolean> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String, Boolean> players) {
        this.players = players;
    }

    public HashMap<String, Boolean> getCheckpoints() { return checkpoints; }

    public void setCheckpoints(HashMap<String, Boolean> checkpoints) { this.checkpoints = checkpoints; }
}
