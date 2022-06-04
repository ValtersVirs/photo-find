package com.example.photofind.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photofind.R;
import com.example.photofind.adapters.PlayerCheckpointAdapter;
import com.example.photofind.adapters.PlayerOrganizerGameAdapter;
import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.PlayerCheckpoint;
import com.example.photofind.viewmodels.PlayerGameViewModel;

import java.util.ArrayList;

public class PlayerCheckpointFragment extends Fragment {

    String gameId;
    ArrayList<Checkpoint> checkpointList;

    RecyclerView rvCheckpointList;

    PlayerCheckpointAdapter checkpointAdapter;
    PlayerGameViewModel model;
    SharedPreferences sharedPref;

    public PlayerCheckpointFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = requireActivity().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        gameId = sharedPref.getString("gameId", "");

        model = new ViewModelProvider(requireActivity()).get(PlayerGameViewModel.class);

        checkpointList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_checkpoint, container, false);

        checkpointAdapter = new PlayerCheckpointAdapter(checkpointList);

        rvCheckpointList = view.findViewById(R.id.rvCheckpointList);
        rvCheckpointList.setHasFixedSize(true);
        rvCheckpointList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rvCheckpointList.setAdapter(checkpointAdapter);

        model.getGameCheckpoints(gameId).observe(getViewLifecycleOwner(), gameCheckpoints -> {
            checkpointList.clear();
            checkpointList.addAll(gameCheckpoints);
            checkpointAdapter.notifyDataSetChanged();
        });

        return view;
    }
}