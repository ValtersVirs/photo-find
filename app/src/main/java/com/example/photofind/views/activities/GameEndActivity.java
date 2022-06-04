package com.example.photofind.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.example.photofind.R;
import com.example.photofind.adapters.PlayerRankingAdapter;
import com.example.photofind.models.PlayerRanking;
import com.example.photofind.viewmodels.GameEndViewModel;

import java.util.ArrayList;

public class GameEndActivity extends AppCompatActivity {

    private String gameId;
    private ArrayList<PlayerRanking> playerRanking;

    private Button btnReturn;
    private RecyclerView rvPlayerRanking;

    private GameEndViewModel model;
    private SharedPreferences sharedPref;
    private PlayerRankingAdapter rankingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);

        model = new ViewModelProvider(this).get(GameEndViewModel.class);

        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        gameId = sharedPref.getString("gameId", "");

        btnReturn = findViewById(R.id.btnReturn);

        btnReturn.setOnClickListener(v -> returnToMainMenu());

        rvPlayerRanking = findViewById(R.id.rvPlayerRanking);
        rvPlayerRanking.setHasFixedSize(true);
        rvPlayerRanking.setLayoutManager(new LinearLayoutManager(this));

        playerRanking = new ArrayList<>();
        rankingAdapter = new PlayerRankingAdapter(playerRanking);
        rvPlayerRanking.setAdapter(rankingAdapter);

        model.getPlayerRanking(gameId).observe(this, playerRanking -> {
            this.playerRanking.clear();
            this.playerRanking.addAll(playerRanking);
            rankingAdapter.notifyDataSetChanged();
        });
    }

    public void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}