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
import com.example.photofind.adapters.PlayerOrganizerGameAdapter;
import com.example.photofind.models.Player;
import com.example.photofind.viewmodels.OrganizerPlayerViewModel;

import java.util.ArrayList;

public class OrganizerPlayerListFragment extends Fragment {

    private ArrayList<Player> playerList;
    private String gameId;

    private RecyclerView rvPlayerList;

    private PlayerOrganizerGameAdapter playerAdapter;
    private SharedPreferences sharedPref;
    private OrganizerPlayerViewModel model;
    private FragmentManager manager;

    public OrganizerPlayerListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = requireActivity().getSharedPreferences("CurrentGame", Context.MODE_PRIVATE);
        gameId = sharedPref.getString("gameId", "");

        model = new ViewModelProvider(requireActivity()).get(OrganizerPlayerViewModel.class);

        playerList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_organizer_player_list, container, false);

        manager = getParentFragmentManager();
        playerAdapter = new PlayerOrganizerGameAdapter(playerList, manager);

        rvPlayerList = view.findViewById(R.id.rvPlayerList);
        rvPlayerList.setHasFixedSize(true);
        rvPlayerList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rvPlayerList.setAdapter(playerAdapter);

        model.getPlayers(gameId).observe(getViewLifecycleOwner(), newPlayers -> {
            playerList.clear();
            playerList.addAll(newPlayers);
            playerAdapter.notifyDataSetChanged();
        });

        return view;
    }
}