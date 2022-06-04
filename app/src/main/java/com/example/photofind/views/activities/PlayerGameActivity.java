package com.example.photofind.views.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.photofind.R;
import com.example.photofind.viewmodels.PlayerGameViewModel;
import com.example.photofind.views.fragments.PlayerCheckpointFragment;
import com.example.photofind.views.fragments.PlayerMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PlayerGameActivity extends AppCompatActivity {

    private String playerId;
    private String gameId;

    private  Button btnAddCheckpoint;
    private  Button btnViewCheckpoints;
    private   Button btnLeave;
    private   Button btnBack;

    private    PlayerGameViewModel model;
    private    SharedPreferences sharedPref;
    private    FragmentManager manager;
    private    FragmentTransaction transaction;
    private    MaterialAlertDialogBuilder dialogConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_game);

        model = new ViewModelProvider(this).get(PlayerGameViewModel.class);
        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        playerId = sharedPref.getString("playerId", "");
        gameId = sharedPref.getString("gameId", "");

        btnAddCheckpoint = findViewById(R.id.btnAddCheckpoint);
        btnViewCheckpoints = findViewById(R.id.btnViewCheckpoints);
        btnBack = findViewById(R.id.btnBack);
        btnLeave = findViewById(R.id.btnLeave);

        btnAddCheckpoint.setOnClickListener(view -> createCheckpoint());
        btnViewCheckpoints.setOnClickListener(view -> viewCheckpoints());
        btnBack.setOnClickListener(view -> viewMap());
        btnLeave.setOnClickListener(view -> confirmLeaveGame());

        model.getGameEnded(gameId).observe(this, ended -> {
            if (ended) {
                startGameEndActivity();
            }
        });
    }

    ActivityResultLauncher<Intent> getCheckpoint = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri imageUri = result.getData().getParcelableExtra("imageUri");
                        LatLng latLng = result.getData().getParcelableExtra("latLng");
                        String title = result.getData().getStringExtra("title");
                        model.addCheckpoint(imageUri, latLng, playerId, title);
                    }
                }
            });

    public void createCheckpoint() {
        Intent intent = new Intent(this, CreateCheckpointActivity.class);
        getCheckpoint.launch(intent);
    }

    public void viewCheckpoints() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, PlayerCheckpointFragment.class, null);
        transaction.commit();

        btnViewCheckpoints.setVisibility(View.GONE);
        btnAddCheckpoint.setVisibility(View.GONE);
        btnLeave.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
    }

    public void viewMap() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, PlayerMapFragment.class, null);
        transaction.commit();

        btnViewCheckpoints.setVisibility(View.VISIBLE);
        btnAddCheckpoint.setVisibility(View.VISIBLE);
        btnLeave.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.GONE);
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

    public void startGameEndActivity() {
        Intent intent = new Intent(this, GameEndActivity.class);
        startActivity(intent);
    }

    public void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}