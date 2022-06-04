package com.example.photofind.viewmodels;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Game;
import com.example.photofind.models.GameOptions;
import com.example.photofind.models.TempCheckpoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateGameViewModel extends ViewModel {
    private final DatabaseReference databaseRefGames = FirebaseDatabase.getInstance().getReference("games");
    private final DatabaseReference databaseRefCheckpoints = FirebaseDatabase.getInstance().getReference("checkpoints");
    private final StorageReference storageRefCheckpoints = FirebaseStorage.getInstance().getReference("checkpoints");
    private final String SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private MutableLiveData<String> gameId;
    private String tempGameId;
    private String gameCode;
    private String gameName;
    private ArrayList<TempCheckpoint> tempCheckpoints;
    private GameOptions gameOptions;
    private Integer checkpointCount;
    private Integer checkpointsCompleted;
    private Integer imagesCompleted;

    public LiveData<String> getGameId() {
        if (gameId == null) {
            gameId = new MutableLiveData<>();
        }
        return gameId;
    }

    public void createGame(String gameName, ArrayList<TempCheckpoint> tempCheckpoints, GameOptions gameOptions) {
        if (gameCode == null) {
            this.gameName = gameName;
            this.tempCheckpoints = tempCheckpoints;
            this.gameOptions = gameOptions;

            generateGameCode();
        } else {
            tempGameId = databaseRefGames.push().getKey();

            Game newGame = new Game(tempGameId, gameName, gameCode, false, false, gameOptions);

            databaseRefGames.child(tempGameId).setValue(newGame).addOnSuccessListener(result -> {
                checkpointCount = tempCheckpoints.size();
                checkpointsCompleted = 0;
                imagesCompleted = 0;
                createCheckpoints(tempCheckpoints);
            });
        }
    }

    public void generateGameCode() {
        String tempCode = getRandomString(6);

        databaseRefGames.orderByChild("code").equalTo(tempCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    gameCode = tempCode;
                    createGame(gameName, tempCheckpoints, gameOptions);
                } else {
                    generateGameCode();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public String getRandomString(Integer size) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }
        return builder.toString();
    }

    public void createCheckpoints(ArrayList<TempCheckpoint> tempCheckpoints) {
        for (TempCheckpoint tempCheckpoint : tempCheckpoints) {
            Uri imageUri = tempCheckpoint.getImage();
            LatLng latLng = tempCheckpoint.getLatLng();
            String title = tempCheckpoint.getTitle();

            String checkpointId = databaseRefCheckpoints.push().getKey();
            Date date = new Date();
            Checkpoint checkpoint = new Checkpoint(checkpointId, "", title, latLng.latitude, latLng.longitude, date.getTime());

            uploadCheckpoint(checkpoint);
            uploadImage(checkpointId, imageUri);
        }
    }

    public void uploadImage(String checkpointId, Uri imageUri) {
        storageRefCheckpoints.child(checkpointId + ".jpg").putFile(imageUri).addOnCompleteListener(imageUpload -> {
            if (imageUpload.isSuccessful()) {
                storageRefCheckpoints.child(checkpointId + ".jpg").getDownloadUrl().addOnCompleteListener(imageUrl -> {
                    if (imageUrl.isSuccessful()) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("/imagePath", imageUrl.getResult().toString());
                        databaseRefCheckpoints.child(checkpointId).updateChildren(update).addOnCompleteListener(o -> {
                            imagesCompleted++;
                            gameUploadCompleted();
                        });
                    } else {
                        imagesCompleted++;
                    }
                });
            } else {
                imagesCompleted++;
            }
        });
    }

    public void uploadCheckpoint(Checkpoint checkpoint) {
        String checkpointId = checkpoint.getId();

        databaseRefCheckpoints.child(checkpointId).setValue(checkpoint).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                databaseRefGames.child(tempGameId + "/checkpoints/" + checkpointId).setValue(true).addOnCompleteListener(o -> {
                    checkpointsCompleted++;
                    gameUploadCompleted();
                });
            } else {
                checkpointsCompleted++;
            }
        });
    }

    public void gameUploadCompleted() {
        if ((checkpointCount == checkpointsCompleted) && (checkpointCount == imagesCompleted)) {
            gameId.setValue(tempGameId);
        }
    }
}
