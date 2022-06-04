package com.example.photofind.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photofind.R;
import com.example.photofind.adapters.PlayerOrganizerNewestAdapter;
import com.example.photofind.models.PlayerCheckpoint;
import com.example.photofind.viewmodels.OrganizerNewestViewModel;

import java.util.ArrayList;
import java.util.Collections;

public class OrganizerNewestListFragment extends Fragment {

    private ArrayList<PlayerCheckpoint> checkpointList;
    private String gameId;

    private RecyclerView rvCheckpointList;

    private PlayerOrganizerNewestAdapter checkpointAdapter;
    private SharedPreferences sharedPref;
    private OrganizerNewestViewModel model;
    private FragmentManager manager;

    public OrganizerNewestListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = requireActivity().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        gameId = sharedPref.getString("gameId", "");

        model = new ViewModelProvider(requireActivity()).get(OrganizerNewestViewModel.class);

        checkpointList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_newest_list, container, false);

        manager = getParentFragmentManager();
        checkpointAdapter = new PlayerOrganizerNewestAdapter(checkpointList, manager);

        rvCheckpointList = view.findViewById(R.id.rvNewestList);
        rvCheckpointList.setHasFixedSize(true);
        rvCheckpointList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rvCheckpointList.setAdapter(checkpointAdapter);

        model.getCheckpoints(gameId).observe(getViewLifecycleOwner(), newCheckpoints -> {
            checkpointList.clear();
            checkpointList.addAll(newCheckpoints);
            Collections.sort(checkpointList, (o1, o2) -> o2.getCheckpoint().getUploadedAt().compareTo(o1.getCheckpoint().getUploadedAt()));
            checkpointAdapter.notifyDataSetChanged();
        });
        return view;
    }
}