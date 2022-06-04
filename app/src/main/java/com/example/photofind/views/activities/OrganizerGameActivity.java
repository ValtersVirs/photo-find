package com.example.photofind.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.photofind.R;
import com.example.photofind.viewmodels.OrganizerGameViewModel;
import com.example.photofind.views.fragments.OrganizerMapFragment;
import com.example.photofind.views.fragments.OrganizerNewestListFragment;
import com.example.photofind.views.fragments.OrganizerPlayerListFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

public class OrganizerGameActivity extends AppCompatActivity {

    private String gameId;

    private TextView txtGameCode;
    private Button btnEndGame;
    private TabLayout tabLayout;

    private OrganizerGameViewModel model;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private SharedPreferences sharedPref;
    private MaterialAlertDialogBuilder dialogConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_game);

        model = new ViewModelProvider(this).get(OrganizerGameViewModel.class);

        sharedPref = getApplicationContext().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        gameId = sharedPref.getString("gameId", "");

        txtGameCode = findViewById(R.id.txtGameCode);
        btnEndGame = findViewById(R.id.btnEndGame);

        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        transaction.add(R.id.fragmentContainer, OrganizerMapFragment.class, null);
        transaction.commit();

        tabLayout = findViewById(R.id.tlOrganizerTabs);

        model.getGame(gameId).observe(this, game -> {
            if (game != null) {
                txtGameCode.setText(getResources().getString(R.string.code, game.getCode()));
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragmentContainer, OrganizerMapFragment.class, null);
                        transaction.commit();
                        txtGameCode.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        txtGameCode.setVisibility(View.GONE);
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragmentContainer, OrganizerPlayerListFragment.class, null);
                        transaction.commit();
                        break;
                    case 2:
                        txtGameCode.setVisibility(View.GONE);
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragmentContainer, OrganizerNewestListFragment.class, null);
                        transaction.commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        txtGameCode.setVisibility(View.GONE);
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragmentContainer, OrganizerPlayerListFragment.class, null);
                        transaction.commit();
                        break;
                    case 2:
                        txtGameCode.setVisibility(View.GONE);
                        transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragmentContainer, OrganizerNewestListFragment.class, null);
                        transaction.commit();
                        break;
                }
            }
        });

        btnEndGame.setOnClickListener(v -> confirmEndGame());

        model.getGameEnded(gameId).observe(this, ended -> {
            if (ended) {
                startGameEndActivity();
            }
        });
    }

    public void confirmEndGame() {
        dialogConfirm = new MaterialAlertDialogBuilder(this);
        dialogConfirm
                .setTitle("End game?")
                .setMessage("Are you sure you want to end the game?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("End game", ((dialogInterface, i) -> endGame()))
                .show();
    }

    public void endGame() {
        model.endGame(gameId);
    }

    public void startGameEndActivity() {
        Intent intent = new Intent(this, GameEndActivity.class);
        startActivity(intent);
    }
}