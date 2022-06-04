package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Game;
import com.example.photofind.models.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class JoinGameViewModel extends ViewModel {
    private final DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");
    private final DatabaseReference databaseRefPlayers = FirebaseDatabase.getInstance().getReference("players");

    private MutableLiveData<HashMap<String, String>> joinStatus;

    public void joinGame(String playerName, DataSnapshot game, String gameStatus)  {
        String gameId = (String) game.child("id").getValue();
        String playerId = databaseRefPlayers.push().getKey();
        Player player = new Player(playerName, playerId);

        OnSuccessListener playerAdded = o -> {
            HashMap<String, String> status = new HashMap<>();
            status.put("status", gameStatus);
            status.put("gameId", gameId);
            status.put("playerId", playerId);
            joinStatus.setValue(status);
        };

        databaseRefPlayers.child(playerId).setValue(player).addOnSuccessListener(playerAdded);
        databaseRefGames.child(gameId + "/players/" + playerId).setValue(true);
    }

    public void checkGameCode(String playerName, String code) {
        databaseRefGames.orderByChild("code").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot result) {
                if (result.getValue() != null && result.getChildrenCount() == 1) {
                    for (DataSnapshot snapshot : result.getChildren()) {
                        if (snapshot.hasChild("ended") && (Boolean) snapshot.child("ended").getValue()) {
                            HashMap<String, String> status = new HashMap<>();
                            status.put("status", "game_ended");
                            joinStatus.setValue(status);
                        } else if (snapshot.hasChild("started")
                                && snapshot.hasChild("options/joinAfterStart")
                                && (Boolean) snapshot.child("started").getValue()
                                && (Boolean) snapshot.child("options/joinAfterStart").getValue() == false) {
                            HashMap<String, String> status = new HashMap<>();
                            status.put("status", "game_started");
                            joinStatus.setValue(status);
                        } else if (snapshot.hasChild("started") && (Boolean) snapshot.child("started").getValue()) {
                            joinGame(playerName, snapshot, "join_game");
                        } else {
                            joinGame(playerName, snapshot, "join_lobby");
                        }
                    }
                } else {
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

    public LiveData<HashMap<String, String>> getPlayerValues() {
        if (joinStatus == null) {
            joinStatus = new MutableLiveData<>();
        }
        return joinStatus;
    }
}
