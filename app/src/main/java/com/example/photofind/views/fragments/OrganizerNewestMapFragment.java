package com.example.photofind.views.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.photofind.R;
import com.example.photofind.models.Checkpoint;
import com.example.photofind.viewmodels.OrganizerNewestViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

public class OrganizerNewestMapFragment extends Fragment {

    private String checkpointId;
    private Marker marker;

    private OrganizerNewestViewModel model;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setOnMarkerClickListener(marker -> {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setView(R.layout.card_view_checkpoint);

                AlertDialog alert = builder.create();
                alert.show();

                TextView txtCheckpointId = (TextView) alert.findViewById(R.id.txtCheckpointId);
                ImageView imgCheckpointImage = (ImageView) alert.findViewById(R.id.imgCheckpointImage);
                ProgressBar progressBar = (ProgressBar) alert.findViewById(R.id.pbImage);
                Checkpoint checkpoint = (Checkpoint) marker.getTag();
                if (checkpoint.getTitle() == null || checkpoint.getTitle().isEmpty()) {
                    txtCheckpointId.setVisibility(View.GONE);
                } else {
                    txtCheckpointId.setText(checkpoint.getTitle());
                    txtCheckpointId.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.VISIBLE);
                Glide.with(getView())
                        .load(checkpoint.getImagePath())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imgCheckpointImage);

                return true;
            });

            model.getCheckpoint(checkpointId).observe(getViewLifecycleOwner(), checkpoint -> {
                googleMap.clear();
                LatLng latLng = new LatLng(checkpoint.getLatitude(), checkpoint.getLongitude());
                marker = googleMap.addMarker(new MarkerOptions().position(latLng));
                marker.setTag(checkpoint);
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_newest_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        model = new ViewModelProvider(requireActivity()).get(OrganizerNewestViewModel.class);

        checkpointId = getArguments().getString("checkpointId");
    }
}