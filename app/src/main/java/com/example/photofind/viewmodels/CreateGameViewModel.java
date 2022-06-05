package com.example.photofind.viewmodels;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Database;
import com.example.photofind.models.Game;
import com.example.photofind.models.TempCheckpoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateGameViewModel extends ViewModel {
    private final String SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Database database = new Database();

    private MutableLiveData<String> gameId;
    private String tempGameId;
    private String gameCode;
    private String gameName;
    private ArrayList<TempCheckpoint> tempCheckpoints;
    private Boolean joinAfterStart;
    private Integer checkpointCount;
    private Integer checkpointsCompleted;
    private Integer imagesCompleted;

    public LiveData<String> getGameId() {
        if (gameId == null) {
            gameId = new MutableLiveData<>();
        }
        return gameId;
    }

    public void createGame(String gameName, ArrayList<TempCheckpoint> tempCheckpoints, Boolean joinAfterStart) {
        if (gameCode == null) {
            this.gameName = gameName;
            this.tempCheckpoints = tempCheckpoints;
            this.joinAfterStart = joinAfterStart;

            generateGameCode();
        } else {
            tempGameId = database.getGames().push().getKey();

            Game newGame = new Game(tempGameId, gameName, gameCode, false, false, joinAfterStart);

            database.getGames().child(tempGameId).setValue(newGame).addOnSuccessListener(result -> {
                checkpointCount = tempCheckpoints.size();
                checkpointsCompleted = 0;
                imagesCompleted = 0;
                createCheckpoints(tempCheckpoints);
            });
        }
    }

    public void generateGameCode() {
        String tempCode = getRandomString(6);

        database.getGames().orderByChild("code").equalTo(tempCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    gameCode = tempCode;
                    createGame(gameName, tempCheckpoints, joinAfterStart);
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

            String checkpointId = database.getCheckpoints().push().getKey();
            Date date = new Date();
            Checkpoint checkpoint = new Checkpoint(checkpointId, "", title, latLng.latitude, latLng.longitude, date.getTime());

            uploadCheckpoint(checkpoint);
            uploadImage(checkpointId, imageUri);
        }
    }

    public void uploadImage(String checkpointId, Uri imageUri) {
        database.getCheckpointsStorage().child(checkpointId + ".jpg").putFile(imageUri).addOnCompleteListener(imageUpload -> {
            if (imageUpload.isSuccessful()) {
                database.getCheckpointsStorage().child(checkpointId + ".jpg").getDownloadUrl().addOnCompleteListener(imageUrl -> {
                    if (imageUrl.isSuccessful()) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("/imagePath", imageUrl.getResult().toString());
                        database.getCheckpoints().child(checkpointId).updateChildren(update).addOnCompleteListener(o -> {
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

        database.getCheckpoints().child(checkpointId).setValue(checkpoint).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                database.getGames().child(tempGameId + "/checkpoints/" + checkpointId).setValue(true).addOnCompleteListener(o -> {
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
