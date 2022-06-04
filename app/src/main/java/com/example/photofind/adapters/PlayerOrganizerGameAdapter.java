package com.example.photofind.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofind.R;
import com.example.photofind.models.Player;
import com.example.photofind.views.fragments.OrganizerPlayerMapFragment;

import java.util.ArrayList;

public class PlayerOrganizerGameAdapter extends RecyclerView.Adapter<PlayerOrganizerGameAdapter.ViewHolder> {
    ArrayList<Player> playerList;
    FragmentManager manager;

    public PlayerOrganizerGameAdapter(ArrayList<Player> playerList, FragmentManager manager) {
        this.playerList = playerList;
        this.manager = manager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_organizer_game_player_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.player = player;
        holder.manager = this.manager;
        holder.playerName.setText(player.getName());
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Player player;
        TextView playerName;
        Button btnViewCheckpoints;
        FragmentManager manager;
        FragmentTransaction transaction;
        OrganizerPlayerMapFragment fragment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.txtPlayerName);

            btnViewCheckpoints = itemView.findViewById(R.id.btnViewCheckpoints);

            btnViewCheckpoints.setOnClickListener(v -> {
                fragment = new OrganizerPlayerMapFragment();
                Bundle arguments = new Bundle();
                arguments.putString("playerId", player.getId());
                fragment.setArguments(arguments);

                transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentContainer, fragment, null);
                transaction.commit();
            });
        }
    }
}
