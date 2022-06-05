package com.example.photofind.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Database;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainViewModel extends ViewModel {
    private final Database database = new Database();
    
    private MutableLiveData<String> gameState;
    private String playerId;

    public MutableLiveData<String> isInGame(String gameId, String playerId) {
        if (gameState == null) {
            this.playerId = playerId;
            gameState = new MutableLiveData<>();
            database.getGames().child(gameId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        if (snapshot.hasChild("ended") && (Boolean) snapshot.child("ended").getValue()) {
                            gameState.setValue("not_in_game");
                        } else {
                            if (snapshot.hasChild("started") && (Boolean) snapshot.child("started").getValue()) {
                                if (playerId.equals("organizer")) {
                                    gameState.setValue("organizer_game");
                                } else if (snapshot.hasChild("players/" + playerId) && (Boolean) snapshot.child("players/" + playerId).getValue()) {
                                    gameState.setValue("player_game");
                                } else {
                                    gameState.setValue("not_in_game");
                                }
                            } else {
                                if (playerId.equals("organizer")) {
                                    gameState.setValue("organizer_lobby");
                                } else if (snapshot.hasChild("players/" + playerId) && (Boolean) snapshot.child("players/" + playerId).getValue()) {
                                    gameState.setValue("player_lobby");
                                } else {
                                    gameState.setValue("not_in_game");
                                }
                            }
                        }
                    } else {
                        gameState.setValue("not_in_game");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return gameState;
    }
}
