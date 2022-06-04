package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Game;
import com.example.photofind.models.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrganizerLobbyViewModel extends ViewModel {
    final private DatabaseReference databaseRefPlayers = FirebaseDatabase.getInstance().getReference("players");
    final private DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");

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
        databaseRefGames.child(gameId + "/players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot result) {
                ArrayList<Player> newPlayerList = new ArrayList<>();
                if (result.hasChildren()) {
                    for (DataSnapshot dataSnapshot : result.getChildren()) {
                        Boolean isActive = (Boolean) dataSnapshot.getValue();
                        if (isActive) {
                            String playerId = dataSnapshot.getKey();

                            databaseRefPlayers.child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        databaseRefGames.child(gameId + "/started").addValueEventListener(new ValueEventListener() {
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

    public void startGame(String gameId) {
        databaseRefGames.child(gameId + "/started").setValue(true);
    }
}
