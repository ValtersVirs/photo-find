package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Player;
import com.example.photofind.models.PlayerCheckpoint;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrganizerPlayerViewModel extends ViewModel {
    final private DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");
    final private DatabaseReference databaseRefPlayers = FirebaseDatabase.getInstance().getReference("players");
    final private DatabaseReference databaseRefCheckpoints = FirebaseDatabase.getInstance().getReference("checkpoints");

    private MutableLiveData<ArrayList<Player>> playerList;
    private ArrayList<Player> newPlayerList;
    private String gameId;
    private Long totalPlayers;
    private Long playerCount;

    private MutableLiveData<ArrayList<Checkpoint>> playerCheckpoints;
    private ArrayList<Checkpoint> newCheckpointList;
    private MutableLiveData<Checkpoint> updatedCheckpoint;
    private String playerId;

    private ChildEventListener playerCheckpointListener;
    private HashMap<String, ValueEventListener> checkpointListeners;

    public LiveData<ArrayList<Player>> getPlayers(String gameId) {
        this.gameId = gameId;
        if (playerList == null) {
            playerList = new MutableLiveData<>();
            loadPlayers();
        }
        return playerList;
    }

    public void loadPlayers() {
        databaseRefGames.child(gameId + "/players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                newPlayerList = new ArrayList<>();
                totalPlayers = 0L;
                playerCount = 0L;
                if (snapshots.hasChildren()) {
                    totalPlayers = snapshots.getChildrenCount();
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

    public void loadPlayer(String playerId) {
        databaseRefPlayers.child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newPlayerList.add(snapshot.getValue(Player.class));
                playerCount++;
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
        if (this.playerId != null && !playerId.equals(this.playerId)) {
            databaseRefPlayers.child(this.playerId + "/checkpoints").removeEventListener(playerCheckpointListener);
            for (Map.Entry<String, ValueEventListener> checkpointListener : checkpointListeners.entrySet()) {
                databaseRefCheckpoints.child(checkpointListener.getKey()).removeEventListener(checkpointListener.getValue());
            }
            this.playerId = playerId;
            playerCheckpoints = new MutableLiveData<>();
            loadCheckpoints();
        } else if (playerCheckpoints == null) {
            this.playerId = playerId;
            checkpointListeners = new HashMap<>();
            playerCheckpoints = new MutableLiveData<>();
            loadCheckpoints();
        }
        return playerCheckpoints;
    }

    public void loadCheckpoints() {
        newCheckpointList = new ArrayList<>();
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

        databaseRefPlayers.child(playerId + "/checkpoints").addChildEventListener(playerCheckpointListener);
    }

    public void addCheckpointListener(String checkpointId) {
        ValueEventListener tempCheckpointListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Checkpoint checkpoint = snapshot.getValue(Checkpoint.class);
                    String newId = checkpoint.getId();
                    String oldId;
                    for (int i = 0; i < newCheckpointList.size(); i++) {
                        oldId = newCheckpointList.get(i).getId();
                        if (newId.equals(oldId)) {
                            newCheckpointList.set(i, checkpoint);
                            playerCheckpoints.setValue(newCheckpointList);
                            return;
                        }
                    }
                    newCheckpointList.add(checkpoint);
                    playerCheckpoints.setValue(newCheckpointList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        checkpointListeners.put(checkpointId, tempCheckpointListener);

        databaseRefCheckpoints.child(checkpointId).addValueEventListener(tempCheckpointListener);
    }

    public LiveData<Checkpoint> getUpdatedCheckpoint(String playerId) {
        if (updatedCheckpoint == null) {
            updatedCheckpoint = new MutableLiveData<>();
        }
        return updatedCheckpoint;
    }

    public void setUpdatedCheckpoint(String checkpointId) {
        databaseRefCheckpoints.child(checkpointId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    updatedCheckpoint.setValue(snapshot.getValue(Checkpoint.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
