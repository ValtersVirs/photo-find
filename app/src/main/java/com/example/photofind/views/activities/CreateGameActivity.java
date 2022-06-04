package com.example.photofind.views.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.photofind.R;
import com.example.photofind.adapters.OrganizerImageAdapter;
import com.example.photofind.models.Game;
import com.example.photofind.models.GameOptions;
import com.example.photofind.models.TempCheckpoint;
import com.example.photofind.viewmodels.CreateGameViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CreateGameActivity extends AppCompatActivity {

    Button btnCreateNewGame;
    Button btnAddCheckpoint;
    EditText edtTxtInput;
    ImageView imgPicture;
    RelativeLayout rlContent;
    RelativeLayout rlCreatingGame;
    RecyclerView rvImages;

    ArrayList<TempCheckpoint> checkpointList;
    SwitchMaterial optionStarted;
    AutoCompleteTextView optionWinner;

    SharedPreferences sharedPref;
    CreateGameViewModel model;
    OrganizerImageAdapter imageAdapter;
    MaterialAlertDialogBuilder dialogError;
    MaterialAlertDialogBuilder dialogConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        model = new ViewModelProvider(this).get(CreateGameViewModel.class);

        btnCreateNewGame = findViewById(R.id.btnCreateNewGame);
        btnAddCheckpoint = findViewById(R.id.btnAddCheckpoint);
        edtTxtInput = findViewById(R.id.edtTxtInput);
        imgPicture = findViewById(R.id.imgPicture);
        rlContent = findViewById(R.id.rlContent);
        rlCreatingGame = findViewById(R.id.rlCreatingGame);

        checkpointList = new ArrayList<>();

        imageAdapter = new OrganizerImageAdapter(checkpointList);

        rvImages = findViewById(R.id.rvImages);
        rvImages.setHasFixedSize(true);
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        optionStarted = findViewById(R.id.swOptionStarted);

        String[] winnerOptions = {"none", "count", "time"};
        ArrayAdapter winnerAdapter = new ArrayAdapter(CreateGameActivity.this, R.layout.row_winner_options, winnerOptions);
        optionWinner = findViewById(R.id.acOptionsWinner);
        optionWinner.setAdapter(winnerAdapter);
        optionWinner.setText(winnerOptions[0], false);

        NumberPicker minutes = findViewById(R.id.npMinutes);
        minutes.setMinValue(0);
        minutes.setMaxValue(60);
        minutes.setFormatter(i -> String.format("%02d", i));

        NumberPicker hours = findViewById(R.id.npHours);
        hours.setMinValue(0);
        hours.setMaxValue(24);
        hours.setFormatter(i -> String.format("%02d", i));

        NumberPicker days = findViewById(R.id.npDays);
        days.setMinValue(0);
        days.setMaxValue(99);
        days.setFormatter(i -> String.format("%02d", i));

        btnCreateNewGame.setOnClickListener(view -> {
            String gameName = edtTxtInput.getText().toString().trim();

            Boolean joinAfterStart = optionStarted.isChecked();
            String winnerBy = optionWinner.getText().toString();
            int timeLimit = (minutes.getValue()) + (hours.getValue() * 60) + (days.getValue() * 1440);

            GameOptions gameOptions = new GameOptions(joinAfterStart, winnerBy, timeLimit);

            if (gameName.isEmpty()) {
                joinError("Enter a name for game");
            } else if (checkpointList.isEmpty()) {
                joinError("Add at least 1 checkpoint");
            } else {
                rlContent.setVisibility(View.GONE);
                rlCreatingGame.setVisibility(View.VISIBLE);
                model.createGame(gameName, checkpointList, gameOptions);
            }
        });


        btnAddCheckpoint.setOnClickListener(view -> createCheckpoint());

        // Delete checkpoint by holing on it
        imageAdapter.setOnItemClickListener((position, v) -> {
            dialogConfirm = new MaterialAlertDialogBuilder(v.getContext());
            dialogConfirm
                    .setTitle("Delete checkpoint?")
                    .setMessage("Are you sure you want to delete the checkpoint?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", ((dialogInterface, i) -> imageAdapter.removeItem(position)))
                    .show();
        });

        // Listen for when game is created and start new view OrganizerLobbyActivity
        model.getGameId().observe(this, gameId -> {
            sharedPref = getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("gameId", gameId);
            editor.putString("playerId", "organizer");
            editor.commit();

            startOrganizerLobbyActivity();
        });
    }

    public void joinError(String errorMessage) {
        dialogError = new MaterialAlertDialogBuilder(this);
        dialogError
                .setTitle(errorMessage)
                .setPositiveButton("Ok", null)
                .show();
    }

    public void startOrganizerLobbyActivity() {
        Intent intent = new Intent(this, OrganizerLobbyActivity.class);
        startActivity(intent);
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

                        TempCheckpoint checkpoint = new TempCheckpoint(imageUri, latLng, title);

                        checkpointList.add(checkpoint);
                        imageAdapter.notifyDataSetChanged();
                    }
                }
            });

    public void createCheckpoint() {
        Intent intent = new Intent(this, CreateCheckpointActivity.class);
        getCheckpoint.launch(intent);
    }
}