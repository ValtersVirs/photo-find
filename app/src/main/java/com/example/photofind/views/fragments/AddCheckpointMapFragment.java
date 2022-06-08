package com.example.photofind.views.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photofind.R;
import com.example.photofind.viewmodels.CreateCheckpointViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AddCheckpointMapFragment extends Fragment {

    private Marker marker;
    private LatLng defaultLatLng = new LatLng(56.8801729, 24.6057484);

    private CreateCheckpointViewModel model;
    private FusedLocationProviderClient location;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // Check location permissions and move map to a location
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                location.getLastLocation().addOnSuccessListener(curLocation -> {
                    if (curLocation != null) {
                        LatLng latLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
                        marker = googleMap.addMarker(new MarkerOptions().position(latLng));
                        model.setLatLng(latLng);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 6));
                    }
                });
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 6));
            }

            // Add marker on map click
            googleMap.setOnMapClickListener(latLng -> {
                if (marker != null) {
                    marker.remove();
                }
                marker = googleMap.addMarker(new MarkerOptions().position(latLng));
                model.setLatLng(latLng);
            });

            // Listen for manual coordinates entered in text field
            model.getManualLatLng().observe(requireActivity(), newLatLng -> {
                if (marker != null) {
                    marker.remove();
                }
                marker = googleMap.addMarker(new MarkerOptions().position(newLatLng));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 10));
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_checkpoint_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        model = new ViewModelProvider(requireActivity()).get(CreateCheckpointViewModel.class);
        location = LocationServices.getFusedLocationProviderClient(requireActivity());
    }
}