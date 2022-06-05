package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Database;
import com.example.photofind.models.Game;
import com.example.photofind.models.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrganizerLobbyViewModel extends ViewModel {
    private final Database database = new Database();

    private MutableLiveData<ArrayList<Player>> playerList;
    private MutableLiveData<Boolean> isStarted;
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
            loadGameStarted(gameId);
        }
        return isStarted;
    }

    public void loadGameStarted(String gameId) {
        database.getGames().child(gameId + "/started").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((Boolean) snapshot.getValue()) {
                    isStarted.setValue(true);
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

    public void startGame(String gameId) {
        database.getGames().child(gameId + "/started").setValue(true);
    }
}
