package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Database;
import com.example.photofind.models.Game;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrganizerGameViewModel extends ViewModel {
    private final Database database = new Database();
    
    private MutableLiveData<ArrayList<Checkpoint>> checkpointList;
    private MutableLiveData<Game> game;
    private MutableLiveData<Boolean> ended;

    private Long checkpointCount;
    private Long checkpointNr;

    public LiveData<ArrayList<Checkpoint>> getCheckpoints(String gameId) {
        if (checkpointList == null) {
            checkpointList = new MutableLiveData<>();
            loadCheckpoints(gameId);
        }
        return checkpointList;
    }

    public void loadCheckpoints(String gameId) {
        database.getGames().child(gameId + "/checkpoints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot result) {
                ArrayList<Checkpoint> tempCheckpointList = new ArrayList<>();
                if (result.hasChildren()) {
                    checkpointCount = result.getChildrenCount();
                    checkpointNr = 0L;
                    for (DataSnapshot dataSnapshot: result.getChildren()) {
                        String checkpointId = dataSnapshot.getKey();

                        database.getCheckpoints().child(checkpointId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    Checkpoint tempCheckpoint = snapshot.getValue(Checkpoint.class);
                                    tempCheckpointList.add(tempCheckpoint);
                                }
                                checkpointNr++;
                                if (checkpointCount == checkpointNr) {
                                    checkpointList.setValue(tempCheckpointList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    checkpointList.setValue(tempCheckpointList);
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
            database.getGames().child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void endGame(String gameId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/ended", true);
        database.getGames().child(gameId).updateChildren(update);
    }

}
