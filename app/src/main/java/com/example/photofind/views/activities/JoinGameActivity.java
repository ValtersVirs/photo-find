package com.example.photofind.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.photofind.R;
import com.example.photofind.viewmodels.JoinGameViewModel;
import com.example.photofind.views.activities.PlayerLobbyActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;

public class JoinGameActivity extends AppCompatActivity {

    private String playerName;
    private String gameCode;

    private Button btnJoinGame;
    private EditText edtGameCode;
    private EditText edtPlayerName;

    private SharedPreferences sharedPref;
    private JoinGameViewModel model;
    private MaterialAlertDialogBuilder dialogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        btnJoinGame = findViewById(R.id.btnJoinGame);
        edtGameCode = findViewById(R.id.edtGameCode);
        edtPlayerName = findViewById(R.id.edtPlayerName);

        model = new ViewModelProvider(this).get(JoinGameViewModel.class);

        // Button click to join game
        btnJoinGame.setOnClickListener(view -> {
            gameCode = edtGameCode.getText().toString().trim();
            playerName = edtPlayerName.getText().toString().trim();
            if (validateName(playerName) && validateCode(gameCode)) {
                model.checkGameCode(playerName, gameCode.toUpperCase());
            }
        });

        // Listen for when player is added to game and start view PlayerLobbyActivity
        model.getPlayerValues().observe(this, result -> {
            switch (result.get("status")) {
                case "join_lobby":
                    joinGameLobby(result.get("gameId"), result.get("playerId"));
                    break;
                case "join_game":
                    joinGameStarted(result.get("gameId"), result.get("playerId"));
                    break;
                case "game_ended":
                    joinError("Game has ended");
                    break;
                case "game_started":
                    joinError("Game has already started");
                    break;
                default:
                    joinError("Game not found");
            }
        });
    }

    public void joinGameLobby(String gameId, String playerId) {
        sharedPref = getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("gameId", gameId);
        editor.putString("playerId", playerId);
        editor.commit();

        startPlayerLobbyActivity();
    }

    public void joinGameStarted(String gameId, String playerId) {
        sharedPref = getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("gameId", gameId);
        editor.putString("playerId", playerId);
        editor.commit();

        startPlayerGameActivity();
    }

    public void joinError(String errorMessage) {
        dialogError = new MaterialAlertDialogBuilder(this);
        dialogError
                .setTitle(errorMessage)
                .setPositiveButton("Ok", null)
                .show();
    }

    public Boolean validateName(String playerName) {
        Integer length = playerName.length();
        if (length == 0) {
            dialogError = new MaterialAlertDialogBuilder(this);
            dialogError
                    .setTitle("Enter your name")
                    .setPositiveButton("ok", null)
                    .show();
            return false;
        } else if (length < 3 || length > 30) {
            dialogError = new MaterialAlertDialogBuilder(this);
            dialogError
                    .setTitle("Name must be between 3 and 30 characters")
                    .setPositiveButton("ok", null)
                    .show();
            return false;
        } else {
            return true;
        }
    }

    public Boolean validateCode(String gameCode) {
        Integer length = gameCode.length();
        if (length == 0) {
            dialogError = new MaterialAlertDialogBuilder(this);
            dialogError
                    .setTitle("Enter Game Code")
                    .setPositiveButton("ok", null)
                    .show();
            return false;
        } else {
            return true;
        }
    }

    public void startPlayerLobbyActivity() {
        Intent intent = new Intent(this, PlayerLobbyActivity.class);
        startActivity(intent);
    }

    public void startPlayerGameActivity() {
        Intent intent = new Intent(this, PlayerGameActivity.class);
        startActivity(intent);
    }
}