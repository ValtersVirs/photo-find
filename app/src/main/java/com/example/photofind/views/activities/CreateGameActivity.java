package com.example.photofind.views.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.photofind.R;
import com.example.photofind.adapters.OrganizerImageAdapter;
import com.example.photofind.models.TempCheckpoint;
import com.example.photofind.viewmodels.CreateGameViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class CreateGameActivity extends AppCompatActivity {

    private Button btnCreateNewGame;
    private Button btnAddCheckpoint;
    private TextView txtCheckpointText;
    private EditText edtTxtInput;
    private RelativeLayout rlContent;
    private RelativeLayout rlCreatingGame;
    private RecyclerView rvImages;

    private ArrayList<TempCheckpoint> checkpointList;
    private SwitchMaterial optionStarted;

    private SharedPreferences sharedPref;
    private CreateGameViewModel model;
    private OrganizerImageAdapter imageAdapter;
    private MaterialAlertDialogBuilder dialogError;
    private MaterialAlertDialogBuilder dialogConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        model = new ViewModelProvider(this).get(CreateGameViewModel.class);

        btnCreateNewGame = findViewById(R.id.btnCreateNewGame);
        btnAddCheckpoint = findViewById(R.id.btnAddCheckpoint);
        edtTxtInput = findViewById(R.id.edtTxtInput);
        rlContent = findViewById(R.id.rlContent);
        rlCreatingGame = findViewById(R.id.rlCreatingGame);
        txtCheckpointText = findViewById(R.id.txtCheckpointText);

        checkpointList = new ArrayList<>();

        imageAdapter = new OrganizerImageAdapter(checkpointList);

        rvImages = findViewById(R.id.rvImages);
        rvImages.setHasFixedSize(true);
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        optionStarted = findViewById(R.id.swOptionStarted);

        btnCreateNewGame.setOnClickListener(view -> createGame());

        btnAddCheckpoint.setOnClickListener(view -> createCheckpoint());

        // Delete checkpoint by holing on it
        imageAdapter.setOnItemClickListener((position, v) -> {
            dialogConfirm = new MaterialAlertDialogBuilder(v.getContext());
            dialogConfirm
                    .setTitle("Delete checkpoint?")
                    .setMessage("Are you sure you want to delete the checkpoint?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", ((dialogInterface, i) -> removeCheckpoint(position)))
                    .show();

            if (checkpointList.size() == 0) {
                txtCheckpointText.setText(getResources().getString(R.string.add_checkpoints));
            }
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
    
    public void createGame() {
        String gameName = edtTxtInput.getText().toString().trim();
        Boolean joinAfterStart = optionStarted.isChecked();

        if (validateName(gameName) && validateCheckpoints()) {
            rlContent.setVisibility(View.GONE);
            rlCreatingGame.setVisibility(View.VISIBLE);
            model.createGame(gameName, checkpointList, joinAfterStart);
        }
    }

    public void displayErrorMessage(String errorMessage) {
        dialogError = new MaterialAlertDialogBuilder(this);
        dialogError
                .setTitle(errorMessage)
                .setPositiveButton("Ok", null)
                .show();
    }

    public Boolean validateName(String gameName) {
        Integer length = gameName.length();
        if (length == 0) {
            displayErrorMessage("Enter a name for game");
            return false;
        } else if (length < 3 || length > 30) {
            displayErrorMessage("Name must be between 3 and 30 characters");
            return false;
        } else {
            return true;
        }
    }

    public Boolean validateCheckpoints() {
        if (checkpointList.isEmpty()) {
            displayErrorMessage("Add at least 1 checkpoint");
            return false;
        } else {
            return true;
        }
    }

    public void removeCheckpoint(int position) {
        imageAdapter.removeItem(position);

        if (checkpointList.size() == 0) {
            txtCheckpointText.setText(getResources().getString(R.string.add_checkpoints));
        } else {
            txtCheckpointText.setText("Checkpoints added: " + checkpointList.size());
        }
    }

    // Launcher for checkpoint creation activity
    ActivityResultLauncher<Intent> addCheckpointLauncher = registerForActivityResult(
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
                        txtCheckpointText.setText("Checkpoints added: " + checkpointList.size());
                        imageAdapter.notifyDataSetChanged();
                    }
                }
            });

    public void createCheckpoint() {
        Intent intent = new Intent(this, CreateCheckpointActivity.class);
        addCheckpointLauncher.launch(intent);
    }

    public void startOrganizerLobbyActivity() {
        Intent intent = new Intent(this, OrganizerLobbyActivity.class);
        startActivity(intent);
    }
}