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
import com.example.photofind.adapters.PlayerRankingAdapter;
import com.example.photofind.models.PlayerRanking;
import com.example.photofind.viewmodels.GameEndViewModel;

import java.util.ArrayList;

public class GameEndActivity extends AppCompatActivity {

    private String gameId;
    private ArrayList<PlayerRanking> playerRanking;

    private TextView txtGameName;
    private Button btnReturn;
    private RecyclerView rvPlayerRanking;
    private RelativeLayout rlContent;
    private ProgressBar pbLoading;

    private GameEndViewModel model;
    private SharedPreferences sharedPref;
    private PlayerRankingAdapter rankingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);

        txtGameName = findViewById(R.id.txtGameName);
        pbLoading = findViewById(R.id.pbGame);
        btnReturn = findViewById(R.id.btnReturn);
        rlContent = findViewById(R.id.rlContent);
        rlContent.setVisibility(View.GONE);

        model = new ViewModelProvider(this).get(GameEndViewModel.class);

        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        gameId = sharedPref.getString("gameId", "");

        rvPlayerRanking = findViewById(R.id.rvPlayerRanking);
        rvPlayerRanking.setHasFixedSize(true);
        rvPlayerRanking.setLayoutManager(new LinearLayoutManager(this));

        playerRanking = new ArrayList<>();
        rankingAdapter = new PlayerRankingAdapter(playerRanking);
        rvPlayerRanking.setAdapter(rankingAdapter);

        btnReturn.setOnClickListener(v -> returnToMainMenu());

        model.getPlayerRanking(gameId).observe(this, playerRanking -> {
            this.playerRanking.clear();
            this.playerRanking.addAll(playerRanking);
            rankingAdapter.notifyDataSetChanged();

            pbLoading.setVisibility(View.GONE);
            rlContent.setVisibility(View.VISIBLE);
        });

        model.getGameName().observe(this, game -> {
            if (game != null) {
                txtGameName.setText(game.getName());
            }
        });
    }

    public void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}