package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Database;
import com.example.photofind.models.Player;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrganizerPlayerViewModel extends ViewModel {
    private final Database database = new Database();

    private MutableLiveData<ArrayList<Player>> playerList;
    private ArrayList<Player> newPlayerList;
    private String gameId;
    private Long totalPlayers;
    private Long playerCount;

    private MutableLiveData<ArrayList<Checkpoint>> playerCheckpoints;
    private ArrayList<Checkpoint> tempCheckpointList;
    private String playerId;

    private ChildEventListener playerCheckpointListener;
    private HashMap<String, ValueEventListener> checkpointListeners;

    public LiveData<ArrayList<Player>> getPlayers(String gameId) {
        this.gameId = gameId;
        if (playerList == null) {
            playerList = new MutableLiveData<>();
            loadPlayerList();
        }
        return playerList;
    }
    
    // Loads current game's player list
    public void loadPlayerList() {
        database.getGames().child(gameId + "/players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                newPlayerList = new ArrayList<>();
                totalPlayers = 0L;
                playerCount = 0L;
                if (snapshots.hasChildren()) {
                    totalPlayers = snapshots.getChildrenCount();
                    for (DataSnapshot snapshot : snapshots.getChildren()) {
                        loadPlayer(snapshot.getKey());
                    }
                } else {
                    playerList.setValue(newPlayerList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // Loads a player
    public void loadPlayer(String playerId) {
        database.getPlayers().child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newPlayerList.add(snapshot.getValue(Player.class));
                playerCount++;
                // Update players only when all are loaded
                if (playerCount >= totalPlayers) {
                    playerList.setValue(newPlayerList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<ArrayList<Checkpoint>> getCheckpoints(String playerId) {
        if (this.playerId != null && !playerId.equals(this.playerId)) { // Different player
            // Remove previous player listener
            database.getPlayers().child(this.playerId + "/checkpoints").removeEventListener(playerCheckpointListener);
            // Remove previous player checkpoint listeners
            for (Map.Entry<String, ValueEventListener> checkpointListener : checkpointListeners.entrySet()) {
                database.getCheckpoints().child(checkpointListener.getKey()).removeEventListener(checkpointListener.getValue());
            }
            this.playerId = playerId;
            playerCheckpoints = new MutableLiveData<>();
            loadCheckpoints();
        } else if (playerCheckpoints == null) { // Player not set yet
            this.playerId = playerId;
            checkpointListeners = new HashMap<>();
            playerCheckpoints = new MutableLiveData<>();
            loadCheckpoints();
        }
        return playerCheckpoints;
    }

    // Loads checkpoints
    public void loadCheckpoints() {
        tempCheckpointList = new ArrayList<>();
        playerCheckpointListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                addCheckpointListener(snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        database.getPlayers().child(playerId + "/checkpoints").addChildEventListener(playerCheckpointListener);
    }

    // Adds a listener to a checkpoint that listens to data change
    public void addCheckpointListener(String checkpointId) {
        ValueEventListener tempCheckpointListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Checkpoint checkpoint = snapshot.getValue(Checkpoint.class);
                    String newId = checkpoint.getId();
                    String oldId;

                    // If a checkpoint is updated, find it and update the checkpoint list
                    for (int i = 0; i < tempCheckpointList.size(); i++) {
                        oldId = tempCheckpointList.get(i).getId();
                        if (newId.equals(oldId)) {
                            tempCheckpointList.set(i, checkpoint);
                            playerCheckpoints.setValue(tempCheckpointList);
                            return;
                        }
                    }

                    // If a checkpoint is added, add it to the checkpoint list
                    tempCheckpointList.add(checkpoint);
                    playerCheckpoints.setValue(tempCheckpointList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        // Add checkpoint listener to an array so it can be removed when changing observed player
        checkpointListeners.put(checkpointId, tempCheckpointListener);

        database.getCheckpoints().child(checkpointId).addValueEventListener(tempCheckpointListener);
    }
}
