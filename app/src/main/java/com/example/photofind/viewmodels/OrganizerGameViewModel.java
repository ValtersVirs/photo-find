package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Game;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrganizerGameViewModel extends ViewModel {
    final private DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");
    final private DatabaseReference databaseRefPlayers = FirebaseDatabase.getInstance().getReference("players");
    final private DatabaseReference databaseRefCheckpoints = FirebaseDatabase.getInstance().getReference("checkpoints");
    final private StorageReference storageRefCheckpoints = FirebaseStorage.getInstance().getReference("checkpoints");

    private MutableLiveData<ArrayList<Checkpoint>> checkpointList;
    private MutableLiveData<Game> game;
    private MutableLiveData<Boolean> ended;

    private Long childrenNr;

    public LiveData<ArrayList<Checkpoint>> getCheckpoints(String gameId) {
        if (checkpointList == null) {
            checkpointList = new MutableLiveData<>();
            loadCheckpoints(gameId);
        }
        return checkpointList;
    }

    public void loadCheckpoints(String gameId) {
        databaseRefGames.child(gameId + "/checkpoints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot result) {
                ArrayList<Checkpoint> newCheckpointList = new ArrayList<>();
                if (result.hasChildren()) {
                    Long childrenCount = result.getChildrenCount();
                    childrenNr = 0L;
                    for (DataSnapshot dataSnapshot: result.getChildren()) {
                        String checkpointId = dataSnapshot.getKey();

                        databaseRefCheckpoints.child(checkpointId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Checkpoint tempCheckpoint = snapshot.getValue(Checkpoint.class);
                                    newCheckpointList.add(tempCheckpoint);
                                }
                                childrenNr++;
                                if (childrenCount == childrenNr) {
                                    checkpointList.setValue(newCheckpointList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    checkpointList.setValue(newCheckpointList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<Game> getGame(String gameId) {
        if (game == null) {
            game = new MutableLiveData<>();
            databaseRefGames.child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        game.setValue(snapshot.getValue(Game.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return game;
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

    public void endGame(String gameId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/ended", true);
        databaseRefGames.child(gameId).updateChildren(update);
    }

}
