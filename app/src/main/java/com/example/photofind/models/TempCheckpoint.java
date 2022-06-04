package com.example.photofind.models;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class TempCheckpoint {
    Uri image;
    LatLng latLng;
    String title;

    public TempCheckpoint(Uri image, LatLng latLng, String title) {
        this.image = image;
        this.latLng = latLng;
        this.title = title;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
