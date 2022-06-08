package com.example.photofind.viewmodels;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Database;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlayerGameViewModel extends ViewModel {
    private final Database database = new Database();

    private MutableLiveData<ArrayList<Checkpoint>> playerCheckpointList;
    private ArrayList<Checkpoint> newPlayerCheckpointList;

    private MutableLiveData<ArrayList<Checkpoint>> gameCheckpointList;
    private ArrayList<Checkpoint> newGameCheckpointList;
    private String gameId;

    private MutableLiveData<Boolean> ended;

    public LiveData<ArrayList<Checkpoint>> getCheckpoints(String playerId) {
        if (playerCheckpointList == null) {
            playerCheckpointList = new MutableLiveData<>();
            loadCheckpoints(playerId);
        }
        return playerCheckpointList;
    }

    public void loadCheckpoints(String playerId) {
        newPlayerCheckpointList = new ArrayList<>();

        database.getPlayers().child(playerId + "/checkpoints").addChildEventListener(new ChildEventListener() {
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
        });
    }

    public void addCheckpointListener(String checkpointId) {
        database.getCheckpoints().child(checkpointId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Checkpoint checkpoint = snapshot.getValue(Checkpoint.class);
                    String newId = checkpoint.getId();
                    String oldId;
                    for (int i = 0; i < newPlayerCheckpointList.size(); i++) {
                        oldId = newPlayerCheckpointList.get(i).getId();
                        if (newId.equals(oldId)) {
                            newPlayerCheckpointList.set(i, checkpoint);
                            playerCheckpointList.setValue(newPlayerCheckpointList);
                            return;
                        }
                    }
                    newPlayerCheckpointList.add(checkpoint);
                    playerCheckpointList.setValue(newPlayerCheckpointList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addCheckpoint(Uri image, LatLng latLng, String playerId, String title) {
        String checkpointId = database.getCheckpoints().push().getKey();

        Date date = new Date();
        Checkpoint checkpoint = new Checkpoint(checkpointId, "", title, latLng.latitude, latLng.longitude, date.getTime());

        database.getCheckpointsStorage().child(checkpointId + ".jpg").putFile(image).addOnSuccessListener(taskSnapshot -> {
            database.getCheckpointsStorage().child(checkpointId + ".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                Map<String, Object> update = new HashMap<>();
                update.put("/imagePath", uri.toString());
                database.getCheckpoints().child(checkpointId).updateChildren(update);
            });
        });

        uploadCheckpoint(checkpoint, playerId);
    }

    public void uploadCheckpoint(Checkpoint checkpoint, String playerId) {
        String checkpointId = checkpoint.getId();
        database.getCheckpoints().child(checkpointId).setValue(checkpoint).addOnSuccessListener(o -> {
            database.getPlayers().child(playerId + "/checkpoints/" + checkpointId).setValue(true);
        });
    }

    public LiveData<ArrayList<Checkpoint>> getGameCheckpoints(String gameId) {
        if (gameCheckpointList == null) {
            this.gameId = gameId;
            gameCheckpointList = new MutableLiveData<>();
            loadGameCheckpoints();
        }
        return gameCheckpointList;
    }

    public void loadGameCheckpoints() {
        database.getGames().child(this.gameId + "/checkpoints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                newGameCheckpointList = new ArrayList<>();
                if (snapshots.hasChildren()) {
                    for (DataSnapshot snapshot : snapshots.getChildren()) {
                        addCheckpoint(snapshot.getKey());
                    }
                } else {
                    gameCheckpointList.setValue(newGameCheckpointList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addCheckpoint(String checkpointId) {
        database.getCheckpoints().child(checkpointId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newGameCheckpointList.add(snapshot.getValue(Checkpoint.class));
                gameCheckpointList.setValue(newGameCheckpointList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<Boolean> getGameEnded(String gameId) {
        if (ended == null) {
            ended = new MutableLiveData<>();

            database.getGames().child(gameId + "/ended").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if ((Boolean) snapshot.getValue()) {
                        ended.setValue(true);
                    } else {
                        ended.setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return ended;
    }

    public void leaveGame(String gameId, String playerId) {
        database.getPlayers().child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot playerSnapshot) {
                database.getGames().child(gameId + "/players/" + playerId).removeValue();
                for (DataSnapshot checkpointSnapshot : playerSnapshot.child("checkpoints").getChildren()) {
                    database.getCheckpoints().child(checkpointSnapshot.getKey()).removeValue();
                    database.getCheckpointsStorage().child(checkpointSnapshot.getKey() + ".jpg").delete();
                }
                database.getPlayers().child(playerId).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
