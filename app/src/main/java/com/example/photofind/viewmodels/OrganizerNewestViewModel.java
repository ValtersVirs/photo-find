package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Database;
import com.example.photofind.models.Player;
import com.example.photofind.models.PlayerCheckpoint;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrganizerNewestViewModel extends ViewModel {
    private final Database database = new Database();

    private MutableLiveData<ArrayList<PlayerCheckpoint>> checkpointList;
    private MutableLiveData<Checkpoint> checkpoint;
    private ArrayList<PlayerCheckpoint> newCheckpointList;
    private String gameId;
    private String checkpointId;

    public LiveData<ArrayList<PlayerCheckpoint>> getCheckpoints(String gameId) {
        this.gameId = gameId;
        if (checkpointList == null) {
            checkpointList = new MutableLiveData<>();
            loadGamePlayers();
        }
        return checkpointList;
    }

    public void loadGamePlayers() {
        newCheckpointList = new ArrayList<>();
        database.getGames().child(gameId + "/players").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                addPlayerListener(snapshot.getKey());
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
        });
    }

    public void addPlayerListener(String playerId) {
        database.getPlayers().child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot playerSnapshot) {
                database.getPlayers().child(playerId + "/checkpoints").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        addCheckpointListener(snapshot.getKey(), playerSnapshot.getValue(Player.class));
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        for (int i = 0; i < newCheckpointList.size(); i++) {
                            if (newCheckpointList.get(i).getCheckpoint().getId().equals(snapshot.getKey())) {
                                newCheckpointList.remove(i);
                                checkpointList.setValue(newCheckpointList);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addCheckpointListener(String checkpointId, Player player) {
        database.getCheckpoints().child(checkpointId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Checkpoint tempCheckpoint = snapshot.getValue(Checkpoint.class);
                    String newId = tempCheckpoint.getId();
                    String oldId;
                    for (int i = 0; i < newCheckpointList.size(); i++) {
                        oldId = newCheckpointList.get(i).getCheckpoint().getId();
                        if (newId.equals(oldId)) {
                            newCheckpointList.set(i, new PlayerCheckpoint(player, tempCheckpoint));
                            checkpointList.setValue(newCheckpointList);
                            return;
                        }
                    }
                    newCheckpointList.add(new PlayerCheckpoint(player, tempCheckpoint));
                    checkpointList.setValue(newCheckpointList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<Checkpoint> getCheckpoint(String checkpointId) {
        if (checkpoint == null || !checkpointId.equals(this.checkpointId)) {
            this.checkpointId = checkpointId;
            checkpoint = new MutableLiveData<>();
            loadCheckpoint();
        }

        return checkpoint;
    }

    public void loadCheckpoint() {
        database.getCheckpoints().child(this.checkpointId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkpoint.setValue(snapshot.getValue(Checkpoint.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
