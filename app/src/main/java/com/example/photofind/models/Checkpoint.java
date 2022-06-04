package com.example.photofind.models;

public class Checkpoint {
    String id;
    String imagePath;
    String title;
    Double latitude;
    Double longitude;
    Long uploadedAt;

    public Checkpoint() {
    }

    public Checkpoint(String id, Double latitude, Double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Checkpoint(String id, String imagePath, String title, Double latitude, Double longitude) {
        this.id = id;
        this.imagePath = imagePath;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Checkpoint(String id, String imagePath, String title, Double latitude, Double longitude, Long uploadedAt) {
        this.id = id;
        this.imagePath = imagePath;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Long uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
