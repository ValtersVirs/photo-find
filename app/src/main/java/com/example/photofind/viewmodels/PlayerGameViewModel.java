package com.example.photofind.viewmodels;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Player;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlayerGameViewModel extends ViewModel {
    final private DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");
    final private DatabaseReference databaseRefPlayers = FirebaseDatabase.getInstance().getReference("players");
    final private DatabaseReference databaseRefCheckpoints = FirebaseDatabase.getInstance().getReference("checkpoints");
    final private StorageReference storageRefCheckpoints = FirebaseStorage.getInstance().getReference("checkpoints");

    private MutableLiveData<ArrayList<Checkpoint>> playerCheckpointList;
    private ArrayList<Checkpoint> newPlayerCheckpointList;
    private MutableLiveData<Checkpoint> updatedCheckpoint;
    private Long childrenNr;

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

        databaseRefPlayers.child(playerId + "/checkpoints").addChildEventListener(new ChildEventListener() {
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
        databaseRefCheckpoints.child(checkpointId).addValueEventListener(new ValueEventListener() {
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

    public void addCheckpoint(Uri image, LatLng latLng, String playerId, String title) {
        String checkpointId = databaseRefCheckpoints.push().getKey();

        Date date = new Date();
        Checkpoint checkpoint = new Checkpoint(checkpointId, "", title, latLng.latitude, latLng.longitude, date.getTime());

        storageRefCheckpoints.child(checkpointId + ".jpg").putFile(image).addOnSuccessListener(taskSnapshot -> {
            storageRefCheckpoints.child(checkpointId + ".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                Map<String, Object> update = new HashMap<>();
                update.put("/imagePath", uri.toString());
                databaseRefCheckpoints.child(checkpointId).updateChildren(update);
            });
        });

        uploadCheckpoint(checkpoint, playerId);
    }

    public void uploadCheckpoint(Checkpoint checkpoint, String playerId) {
        String checkpointId = checkpoint.getId();
        databaseRefCheckpoints.child(checkpointId).setValue(checkpoint).addOnSuccessListener(o -> {
            databaseRefPlayers.child(playerId + "/checkpoints/" + checkpointId).setValue(true);
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
        databaseRefGames.child(this.gameId + "/checkpoints").addListenerForSingleValueEvent(new ValueEventListener() {
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
        databaseRefCheckpoints.child(checkpointId).addListenerForSingleValueEvent(new ValueEventListener() {
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

            databaseRefGames.child(gameId + "/ended").addValueEventListener(new ValueEventListener() {
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
        databaseRefPlayers.child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot playerSnapshot) {
                databaseRefGames.child(gameId + "/players/" + playerId).removeValue();
                for (DataSnapshot checkpointSnapshot : playerSnapshot.child("checkpoints").getChildren()) {
                    databaseRefCheckpoints.child(checkpointSnapshot.getKey()).removeValue();
                    storageRefCheckpoints.child(checkpointSnapshot.getKey() + ".jpg").delete();
                }
                databaseRefPlayers.child(playerId).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
