package com.example.photofind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofind.R;
import com.example.photofind.models.PlayerRanking;

import java.util.ArrayList;

public class PlayerRankingAdapter extends RecyclerView.Adapter<PlayerRankingAdapter.ViewHolder> {
    ArrayList<PlayerRanking> playerRanking;

    public PlayerRankingAdapter(ArrayList<PlayerRanking> playerRanking) {
        this.playerRanking = playerRanking;
    }

    @NonNull
    @Override
    public PlayerRankingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_player_ranking, parent, false);
        return new PlayerRankingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerRankingAdapter.ViewHolder holder, int position) {
        PlayerRanking player = playerRanking.get(position);
        holder.player = player;
        holder.txtPlayerPlace.setText(((Integer) (position + 1)).toString() + ".");
        holder.txtPlayerName.setText(player.getPlayer().getName());
        holder.txtPlayerPoints.setText(player.getPoints().toString());

        int textSize = 0;

        switch (position + 1) {
            case 1:
                textSize = 22;
                break;
            case 2:
                textSize = 20;
                break;
            case 3:
                textSize = 18;
                break;
        }

        if (textSize != 0) {
            holder.txtPlayerPlace.setTextSize(textSize);
            holder.txtPlayerName.setTextSize(textSize);
            holder.txtPlayerPoints.setTextSize(textSize);
        }
    }

    @Override
    public int getItemCount() {
        return playerRanking.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PlayerRanking player;
        TextView txtPlayerPlace;
        TextView txtPlayerName;
        TextView txtPlayerPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlayerPlace = itemView.findViewById(R.id.txtPlayerPlace);
            txtPlayerName = itemView.findViewById(R.id.txtPlayerName);
            txtPlayerPoints = itemView.findViewById(R.id.txtPlayerPoints);
        }
    }
}
