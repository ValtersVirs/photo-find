package com.example.photofind.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.photofind.R;
import com.example.photofind.viewmodels.MainViewModel;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_LOCATION_PERMISSION = 1;

    String gameId;
    String playerId;

    Button btnCreateGame;
    Button btnJoinGame;
    RelativeLayout rlButtons;
    ProgressBar pbLoadGame;

    SharedPreferences sharedPref;
    MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestLocationPermission();

        model = new ViewModelProvider(this).get(MainViewModel.class);
        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);

        gameId = sharedPref.getString("gameId", "");
        playerId = sharedPref.getString("playerId", "");

        btnCreateGame = findViewById(R.id.btnCreateGame);
        btnJoinGame = findViewById(R.id.btnJoinGame);
        rlButtons = findViewById(R.id.rlButtons);
        pbLoadGame = findViewById(R.id.pbLoadGame);

        if (!gameId.isEmpty()) {
            rlButtons.setVisibility(View.GONE);
            pbLoadGame.setVisibility(View.VISIBLE);
            model.isInGame(gameId, playerId).observe(this, gameState -> {
                if (gameState != null) {
                    switch (gameState) {
                        case "organizer_lobby":
                            startOrganizerLobbyActivity();
                            break;
                        case "organizer_game":
                            startOrganizerGameActivity();
                            break;
                        case "player_lobby":
                            startPlayerLobbyActivity();
                            break;
                        case "player_game":
                            startPlayerGameActivity();
                            break;
                        default:
                            rlButtons.setVisibility(View.VISIBLE);
                            pbLoadGame.setVisibility(View.GONE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.clear();
                            editor.commit();
                            gameId = "";
                            playerId = "";
                    }
                }
            });
        }

        btnCreateGame.setOnClickListener(v -> startCreateGameActivity());

        btnJoinGame.setOnClickListener(v -> startJoinGameActivity());
    }

    public void startCreateGameActivity() {
        Intent intent = new Intent(this, CreateGameActivity.class);
        startActivity(intent);
    }

    public void startJoinGameActivity() {
        Intent intent =  new Intent(this, JoinGameActivity.class);
        startActivity(intent);
    }

    public void startOrganizerLobbyActivity() {
        Intent intent = new Intent(this, OrganizerLobbyActivity.class);
        startActivity(intent);
    }

    public void startOrganizerGameActivity() {
        Intent intent = new Intent(this, OrganizerGameActivity.class);
        startActivity(intent);
    }

    public void startPlayerLobbyActivity() {
        Intent intent = new Intent(this, PlayerLobbyActivity.class);
        startActivity(intent);
    }

    public void startPlayerGameActivity() {
        Intent intent = new Intent(this, PlayerGameActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, permissions)) {
            // Permission granted
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, permissions);
        }
    }
}