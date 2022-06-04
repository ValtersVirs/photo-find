package com.example.photofind.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class CreateCheckpointViewModel extends ViewModel {
    private MutableLiveData<LatLng> latLng;
    private MutableLiveData<LatLng> manualLatLng;

    public LiveData<LatLng> getLatLng() {
        if (latLng == null) {
            latLng = new MutableLiveData<>();
        }
        return latLng;
    }

    public void setLatLng(LatLng newLatLng) {
        if (latLng == null) {
            latLng = new MutableLiveData<>(newLatLng);
        } else {
            latLng.setValue(newLatLng);
        }
    }

    public LiveData<LatLng> getManualLatLng() {
        if (manualLatLng == null) {
            manualLatLng = new MutableLiveData<>();
        }
        return manualLatLng;
    }

    public void setManualLatLng(LatLng newLatLng) {
        if (latLng == null || manualLatLng == null) {
            latLng = new MutableLiveData<>(newLatLng);
            manualLatLng = new MutableLiveData<>(newLatLng);
        } else {
            latLng.setValue(newLatLng);
            manualLatLng.setValue(newLatLng);
        }
    }
}
