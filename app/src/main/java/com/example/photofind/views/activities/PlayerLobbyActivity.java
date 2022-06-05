package com.example.photofind.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.photofind.models.Player;
import com.example.photofind.adapters.PlayerAdapter;
import com.example.photofind.R;
import com.example.photofind.viewmodels.PlayerLobbyViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class PlayerLobbyActivity extends AppCompatActivity {

    private String gameId;
    private String playerId;
    private Button btnLeave;
    private ArrayList<Player> playerList;

    private TextView txtGameName;
    private TextView txtGameCode;
    private RelativeLayout rlContent;
    private ProgressBar progressBarGame;

    private SharedPreferences sharedPref;
    private RecyclerView rvPlayerList;
    private PlayerAdapter playerAdapter;
    private PlayerLobbyViewModel model;
    private MaterialAlertDialogBuilder dialogConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_lobby);

        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        model = new ViewModelProvider(this).get(PlayerLobbyViewModel.class);

        gameId = sharedPref.getString("gameId", "");
        playerId = sharedPref.getString("playerId", "");

        txtGameName = findViewById(R.id.txtGameName);
        txtGameCode = findViewById(R.id.txtGameCode);
        btnLeave = findViewById(R.id.btnLeave);

        progressBarGame = findViewById(R.id.pbGame);
        rlContent = findViewById(R.id.rlContent);
        rlContent.setVisibility(View.GONE);

        rvPlayerList = findViewById(R.id.rvPlayerList);
        rvPlayerList.setHasFixedSize(true);
        rvPlayerList.setLayoutManager(new LinearLayoutManager(this));

        playerList = new ArrayList<>();
        playerAdapter = new PlayerAdapter(playerList);
        rvPlayerList.setAdapter(playerAdapter);

        btnLeave.setOnClickListener(v -> confirmLeaveGame());

        model.getIsStarted(gameId).observe(this, isStarted -> {
            if (isStarted) {
                startPlayerGameActivity();
            }
        });

        model.checkRemoved(gameId, playerId).observe(this, removedFromGame -> {
            if (removedFromGame) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.commit();
                gameId = "";
                playerId = "";

                returnToMainMenu();
            }
        });

        model.getGame(gameId).observe(this, game -> {
            if (game != null) {
                txtGameName.setText(game.getName());
                txtGameCode.setText(game.getCode());

                progressBarGame.setVisibility(View.GONE);
                rlContent.setVisibility(View.VISIBLE);
            }
        });

        model.getGamePlayers(gameId).observe(this, newPlayers -> {
            playerList.clear();
            playerList.addAll(newPlayers);
            playerAdapter.notifyDataSetChanged();
        });
    }

    public void confirmLeaveGame() {
        dialogConfirm = new MaterialAlertDialogBuilder(this);
        dialogConfirm
                .setTitle("Leave game?")
                .setMessage("Are you sure you want to leave the game? All progress will be lost")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Leave", ((dialogInterface, i) -> leaveGame()))
                .show();
    }

    public void leaveGame() {
        model.leaveGame(gameId, playerId);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        gameId = "";
        playerId = "";

        returnToMainMenu();
    }

    public void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startPlayerGameActivity() {
        Intent intent = new Intent(this, PlayerGameActivity.class);
        startActivity(intent);
    }
}