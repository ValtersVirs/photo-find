package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Database;
import com.example.photofind.models.Player;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class JoinGameViewModel extends ViewModel {
    private final Database database = new Database();

    private MutableLiveData<HashMap<String, String>> joinStatus;

    public void joinGame(String playerName, DataSnapshot game, String gameStatus)  {
        String gameId = (String) game.child("id").getValue();
        String playerId = database.getPlayers().push().getKey();
        Player player = new Player(playerName, playerId);

        OnSuccessListener playerAdded = o -> {
            HashMap<String, String> status = new HashMap<>();
            status.put("status", gameStatus);
            status.put("gameId", gameId);
            status.put("playerId", playerId);
            joinStatus.setValue(status);
        };

        database.getPlayers().child(playerId).setValue(player).addOnSuccessListener(playerAdded);
        database.getGames().child(gameId + "/players/" + playerId).setValue(true);
    }

    public void checkGameStatus(String playerName, String code) {
        database.getGames().orderByChild("code").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot result) {
                if (result.getValue() != null && result.getChildrenCount() == 1) {
                    for (DataSnapshot snapshot : result.getChildren()) {
                        if (snapshot.hasChild("ended") && (Boolean) snapshot.child("ended").getValue()) {
                            // Game has ended
                            HashMap<String, String> status = new HashMap<>();
                            status.put("status", "game_ended");
                            joinStatus.setValue(status);
                        } else if (snapshot.hasChild("started")
                                && snapshot.hasChild("joinAfterStart")
                                && (Boolean) snapshot.child("started").getValue()
                                && !((Boolean) snapshot.child("joinAfterStart").getValue())) {
                            // Game is already started and cannot be joined
                            HashMap<String, String> status = new HashMap<>();
                            status.put("status", "game_started");
                            joinStatus.setValue(status);
                        } else if (snapshot.hasChild("started") && (Boolean) snapshot.child("started").getValue()) {
                            // Game is already started but can be joined
                            joinGame(playerName, snapshot, "join_game");
                        } else {
                            // Game hasn't started yet
                            joinGame(playerName, snapshot, "join_lobby");
                        }
                    }
                } else {
                    // Game not found
                    HashMap<String, String> status = new HashMap<>();
                    status.put("status", "not_found");
                    joinStatus.setValue(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public LiveData<HashMap<String, String>> getJoinStatus() {
        if (joinStatus == null) {
            joinStatus = new MutableLiveData<>();
        }
        return joinStatus;
    }
}
