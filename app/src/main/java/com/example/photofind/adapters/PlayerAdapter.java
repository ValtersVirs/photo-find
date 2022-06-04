package com.example.photofind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofind.R;
import com.example.photofind.models.Player;

import java.util.ArrayList;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {
    ArrayList<Player> playerList;

    public PlayerAdapter(ArrayList<Player> playerList) {
        this.playerList = playerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_player_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.player = player;
        holder.playerName.setText(player.getName());
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Player player;
        TextView playerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.txtPlayerName);
        }
    }
}
