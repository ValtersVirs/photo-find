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

import com.example.photofind.R;
import com.example.photofind.adapters.PlayerOrganizerAdapter;
import com.example.photofind.models.Player;
import com.example.photofind.viewmodels.OrganizerLobbyViewModel;

import java.util.ArrayList;

public class OrganizerLobbyActivity extends AppCompatActivity {

    private String gameId;
    private ArrayList<Player> playerList;

    private TextView txtGameName;
    private TextView txtGameCode;
    private Button btnStartGame;
    private RecyclerView rvPlayerList;
    private RelativeLayout rlContent;
    private ProgressBar progressBarGame;

    private SharedPreferences sharedPref;
    private PlayerOrganizerAdapter playerAdapter;
    private OrganizerLobbyViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_lobby);

        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        model = new ViewModelProvider(this).get(OrganizerLobbyViewModel.class);

        rlContent = findViewById(R.id.rlContent);
        progressBarGame = findViewById(R.id.pbGame);
        rlContent.setVisibility(View.GONE);

        btnStartGame = findViewById(R.id.btnStartGame);

        gameId = sharedPref.getString("gameId", "");
        txtGameName = findViewById(R.id.txtGameName);
        txtGameCode = findViewById(R.id.txtGameCode);

        rvPlayerList = findViewById(R.id.rvOrgPlayerList);
        rvPlayerList.setHasFixedSize(true);
        rvPlayerList.setLayoutManager(new LinearLayoutManager(this));

        playerList = new ArrayList<>();
        playerAdapter = new PlayerOrganizerAdapter(playerList, gameId);
        rvPlayerList.setAdapter(playerAdapter);

        btnStartGame.setOnClickListener(view -> {
            model.startGame(gameId);
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

        model.getIsStarted(gameId).observe(this, isStarted -> {
            if (isStarted) {
                startOrganizerGameActivity();
            }
        });
    }

    public void startOrganizerGameActivity() {
        Intent intent = new Intent(this, OrganizerGameActivity.class);
        startActivity(intent);
    }
}