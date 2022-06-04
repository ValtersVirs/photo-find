package com.example.photofind.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Database {
    final private DatabaseReference DATABASE_REF = FirebaseDatabase.getInstance().getReference();
    final private StorageReference STORAGE_REF = FirebaseStorage.getInstance().getReference();

    public DatabaseReference getPlayers() {
        return DATABASE_REF.child("players");
    }

    public DatabaseReference getGames() {
        return DATABASE_REF.child("games");
    }

    public DatabaseReference getCheckpoints() {
        return DATABASE_REF.child("checkpoints");
    }

    public StorageReference getCheckpointsStorage() {
        return STORAGE_REF.child("checkpoints");
    }
}
