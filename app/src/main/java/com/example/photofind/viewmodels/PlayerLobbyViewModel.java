package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Database;
import com.example.photofind.models.Game;
import com.example.photofind.models.Player;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlayerLobbyViewModel extends ViewModel {
    private final Database database = new Database();

    private MutableLiveData<ArrayList<Player>> playerList;
    private MutableLiveData<Boolean> isStarted;
    private MutableLiveData<Boolean> removedFromGame;
    private MutableLiveData<Game> game;

    public LiveData<ArrayList<Player>> getGamePlayers(String gameId) {
        if (playerList == null) {
            playerList = new MutableLiveData<>();
            loadGamePlayers(gameId);
        }
        return playerList;
    }

    public void loadGamePlayers(String gameId) {
        database.getGames().child(gameId + "/players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot result) {
                ArrayList<Player> newPlayerList = new ArrayList<>();
                if (result.hasChildren()) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        Boolean isActive = (Boolean) dataSnapshot.getValue();
                        if (isActive) {
                            String playerId = dataSnapshot.getKey();

                            database.getPlayers().child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        Player player = snapshot.getValue(Player.class);
                                        newPlayerList.add(player);
                                        playerList.setValue(newPlayerList);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
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

    public LiveData<Boolean> getIsStarted(String gameId) {
        if (isStarted == null) {
            isStarted = new MutableLiveData<>();
            isStarted.setValue(false);
            database.getGames().child(gameId + "/started").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isGameStarted = (Boolean) snapshot.getValue();
                    if (isGameStarted) {
                        isStarted.setValue(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return isStarted;
    }

    public LiveData<Boolean> checkRemoved(String gameId, String playerId) {
        if (removedFromGame == null) {
            removedFromGame = new MutableLiveData<>();

            database.getGames().child(gameId + "/players").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getKey().equals(playerId)) {
                        removedFromGame.setValue(true);
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
        return removedFromGame;
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

    public void leaveGame(String gameId, String playerId) {
        database.getGames().child(gameId + "/players/" + playerId).removeValue();
        database.getPlayers().child(playerId).removeValue();
    }
}
