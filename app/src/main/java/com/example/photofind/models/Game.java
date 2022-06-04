package com.example.photofind.models;

import java.util.HashMap;

public class Game {
    String id;
    String name;
    String code;
    Boolean started;
    Boolean ended;
    HashMap<String, Boolean> players;
    HashMap<String, Boolean> checkpoints;
    GameOptions options;

    public Game() {}

    public Game(String name, Boolean started, GameOptions options) {
        this.name = name;
        this.started = started;
        this.options = options;
    }

    public Game(String id, String name, String code, Boolean started, Boolean ended, GameOptions options) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.started = started;
        this.ended = ended;
        this.options = options;
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

    public HashMap<String, Boolean> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String, Boolean> players) {
        this.players = players;
    }

    public HashMap<String, Boolean> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(HashMap<String, Boolean> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public GameOptions getOptions() {
        return options;
    }

    public void setOptions(GameOptions options) {
        this.options = options;
    }
}
