package com.example.photofind.views.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.photofind.R;
import com.example.photofind.viewmodels.CreateCheckpointViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CreateCheckpointActivity extends AppCompatActivity {

    private final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1;

    private String currentPhotoPath;
    private Uri pictureUri;
    private LatLng latLng;

    private Button btnSetCoordinates;
    private Button btnAddPicture;
    private Button btnSelectImage;
    private Button btnSave;
    private Button btnBack;
    private EditText edtCoordinates;
    private EditText edtTitle;
    private ImageView picture;

    private CreateCheckpointViewModel model;
    private MaterialAlertDialogBuilder dialogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_checkpoint);

        btnSetCoordinates = findViewById(R.id.btnSetCoordinates);
        btnAddPicture = findViewById(R.id.btnAddPicture);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        edtCoordinates = findViewById(R.id.edtCoordinates);
        edtTitle = findViewById(R.id.edtCheckpointTitle);
        picture = findViewById(R.id.imgPicture);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        model = new ViewModelProvider(this).get(CreateCheckpointViewModel.class);

        btnSetCoordinates.setOnClickListener(view -> {
            String[] coordinates = edtCoordinates.getText().toString().trim().split(",");
            try {
                double lat = Double.parseDouble(coordinates[0].trim());
                double lng = Double.parseDouble(coordinates[1].trim());

                model.setManualLatLng(new LatLng(lat, lng));
            } catch (Exception ex) {}
        });

        btnAddPicture.setOnClickListener(view -> takePicture());

        btnSelectImage.setOnClickListener(view -> requestExternalStoragePermission());

        btnSave.setOnClickListener(view -> saveCheckpoint());

        btnBack.setOnClickListener(view -> goBack());

        model.getLatLng().observe(this, newLatLng -> {
            edtCoordinates.setText(String.format("%.5f", newLatLng.latitude) + ", " + String.format("%.5f", newLatLng.longitude));
        });
    }

    // Saves checkpoint and returns to previous activity
    public void saveCheckpoint() {
        latLng = model.getLatLng().getValue();
        String checkpointTitle = edtTitle.getText().toString();

        if (validateLocation(latLng) && validateTitle(checkpointTitle) && validateImage(pictureUri)) {
            Intent data = new Intent();
            data.putExtra("imageUri", pictureUri);
            data.putExtra("latLng", latLng);
            data.putExtra("title", edtTitle.getText().toString());
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    public void displayErrorMessage(String errorMessage) {
        dialogError = new MaterialAlertDialogBuilder(this);
        dialogError
                .setTitle(errorMessage)
                .setPositiveButton("Ok", null)
                .show();
    }

    public Boolean validateLocation(LatLng latLng) {
        if (latLng == null) {
            displayErrorMessage("Add a location");
            return false;
        } else {
            return true;
        }
    }

    public Boolean validateTitle(String checkpointTitle) {
        Integer length = checkpointTitle.length();
        if (length > 30) {
            displayErrorMessage("Title can't exceed 30 characters");
            return false;
        } else {
            return true;
        }
    }

    public Boolean validateImage(Uri pictureUri) {
        if (pictureUri == null) {
            displayErrorMessage("Add an image");
            return false;
        } else {
            return true;
        }
    }

    public void goBack() {
        finish();
    }

    // Launcher for camera
    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                // On return from camera activity
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        File file = new File(currentPhotoPath);
                        // Compress the image
                        File compressFile = compressImage(file);
                        Uri imageUri = Uri.fromFile(compressFile);

                        if (imageUri != null) {
                            pictureUri = imageUri;
                            Glide.with(CreateCheckpointActivity.this).load(imageUri).into(picture);
                        }
                    }
                }
            });

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {}

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(intent);
        }
    }

    // Launcher for image picker
    ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                // On return from image picker activity
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // Get actual file path from "content://" format
                        Cursor cursor = getContentResolver().query(data.getData(), new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                        cursor.moveToFirst();
                        String filePath = cursor.getString(0);
                        cursor.close();

                        File file = new File(filePath);

                        // Save the image in a new file
                        File imageFile = null;
                        try {
                            imageFile = createImageFile();

                            InputStream inputStream = new FileInputStream(file);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();

                            FileOutputStream outputStream = new FileOutputStream(imageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.close();
                        } catch (IOException ex) {}
                        
                        Uri imageUri = null;
                        if (imageFile != null) {
                            // Compress the image
                            File compressFile = compressImage(imageFile);
                            imageUri = Uri.fromFile(compressFile);
                        }

                        if (imageUri != null) {
                            pictureUri = imageUri;
                            Glide.with(CreateCheckpointActivity.this).load(imageUri).into(picture);
                        }
                    }
                }
            }
    );

    public void pickImage() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        albumIntent.setType("image/*");

        pickImageLauncher.launch(albumIntent);
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public File compressImage(File file){
        try {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 4;

            FileInputStream inputStream = new FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // Override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_EXTERNAL_STORAGE_PERMISSION)
    public void requestExternalStoragePermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this, permissions)) {
            // Permission granted
            pickImage();
        } else {
            // Permission not granted, request
            EasyPermissions.requestPermissions(this, "Allow the app to access your photos", REQUEST_EXTERNAL_STORAGE_PERMISSION, permissions);
        }
    }
}