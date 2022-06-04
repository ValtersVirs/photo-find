package com.example.photofind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofind.R;
import com.example.photofind.models.Player;

import java.util.ArrayList;

public class PlayerOrganizerAdapter extends RecyclerView.Adapter<PlayerOrganizerAdapter.ViewHolder> {
    ArrayList<Player> playerList;
    String gameId;

    public PlayerOrganizerAdapter(ArrayList<Player> playerList, String gameId) {
        this.playerList = playerList;
        this.gameId = gameId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_organizer_player_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = playerList.get(position);
        holder.player = player;
        holder.gameId = this.gameId;
        holder.txtPlayerName.setText(player.getName());
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Player player;
        String gameId;
        TextView txtPlayerName;
        Button btnRemovePlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlayerName = itemView.findViewById(R.id.txtPlayerName);
            btnRemovePlayer = itemView.findViewById(R.id.btnRemovePlayer);

            btnRemovePlayer.setOnClickListener(v -> player.removeFromGame(gameId));
        }
    }
}