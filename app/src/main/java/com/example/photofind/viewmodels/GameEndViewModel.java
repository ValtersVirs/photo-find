package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Game;
import com.example.photofind.models.PlayerRanking;
import com.example.photofind.models.Database;
import com.example.photofind.models.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class GameEndViewModel extends ViewModel {
    private final Database database = new Database();

    private String gameId;
    private int playerCount;

    private ArrayList<PlayerRanking> tempPlayers;
    private MutableLiveData<ArrayList<PlayerRanking>> playerRanking;
    private MutableLiveData<Game> game;

    public LiveData<ArrayList<PlayerRanking>> getPlayerRanking(String gameId) {
        if (playerRanking == null) {
            playerRanking = new MutableLiveData<>();
            this.gameId = gameId;
            loadGame();
            loadGamePlayerIds();
        }
        return playerRanking;
    }

    public LiveData<Game> getGameName() {
        if (game == null) {
            game = new MutableLiveData<>();
        }
        return game;
    }

    public void loadGame() {
        database.getGames().child(gameId + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void loadGamePlayerIds() {
        database.getGames().child(gameId + "/players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> playerIds = new ArrayList<>();
                if (snapshot.hasChildren()) {
                    for (DataSnapshot playerId : snapshot.getChildren()) {
                        playerIds.add(playerId.getKey());
                    }
                }
                playerCount = playerIds.size();
                tempPlayers = new ArrayList<>();
                for (String playerId : playerIds) {
                    loadPlayer(playerId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadPlayer(String playerId) {
        database.getPlayers().child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer count;

                Player player = snapshot.getValue(Player.class);
                count  = player.getCheckpoints().size();

                PlayerRanking playerCheckpoints = new PlayerRanking(player, count);
                tempPlayers.add(playerCheckpoints);

                checkAllPlayersLoaded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkAllPlayersLoaded() {
        if (tempPlayers.size() == playerCount) {
            // Sort by points descending
            Collections.sort(tempPlayers, (o1, o2) -> o2.getPoints().compareTo(o1.getPoints()));
            playerRanking.setValue(tempPlayers);
        }
    }
}
