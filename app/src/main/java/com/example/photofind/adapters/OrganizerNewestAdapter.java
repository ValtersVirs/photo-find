package com.example.photofind.adapters;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.photofind.R;
import com.example.photofind.models.Checkpoint;
import com.example.photofind.models.Player;
import com.example.photofind.models.PlayerCheckpoint;
import com.example.photofind.views.fragments.OrganizerNewestMapFragment;

import java.util.ArrayList;
import java.util.Date;

public class OrganizerNewestAdapter extends RecyclerView.Adapter<OrganizerNewestAdapter.ViewHolder> {
    ArrayList<PlayerCheckpoint> checkpointList;
    FragmentManager manager;

    public OrganizerNewestAdapter(ArrayList<PlayerCheckpoint> checkpointList, FragmentManager manager) {
        this.checkpointList = checkpointList;
        this.manager = manager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_organizer_game_newest_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerNewestAdapter.ViewHolder holder, int position) {
        PlayerCheckpoint playerCheckpoint = checkpointList.get(position);
        holder.player = playerCheckpoint.getPlayer();
        holder.checkpoint = playerCheckpoint.getCheckpoint();
        holder.manager = this.manager;
        holder.playerName.setText(playerCheckpoint.getPlayer().getName());
        holder.checkpointName.setText(playerCheckpoint.getCheckpoint().getTitle());

        Long currentTime = new Date().getTime();
        Long timeDifference = currentTime - holder.checkpoint.getUploadedAt();

        int days = (int) (timeDifference / (1000 * 60 * 60 * 24));
        timeDifference = timeDifference % (1000 * 60 * 60 * 24);
        int hours = (int) (timeDifference / (1000 * 60 * 60));
        timeDifference = timeDifference % (1000 * 60 * 60);
        int minutes = (int) (timeDifference / (1000 * 60));
        timeDifference = timeDifference % (1000 * 60);
        int seconds = (int) (timeDifference / (1000));

        String result;
        if (days != 0) {
            result = days + "d";
        } else if (hours != 0) {
            result = hours + "h";
        } else if (minutes != 0) {
            result = minutes + "m";
        } else {
            result = seconds + "s";
        }

        holder.timeAgo.setText(result + " ago");


        Glide.with(holder.itemView)
                .load(holder.checkpoint.getImagePath())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.pbImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.checkpointImage);
    }

    @Override
    public int getItemCount() {
        return checkpointList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Player player;
        Checkpoint checkpoint;
        TextView playerName;
        TextView checkpointName;
        TextView timeAgo;
        ImageView checkpointImage;
        ProgressBar pbImage;
        FragmentManager manager;
        FragmentTransaction transaction;
        OrganizerNewestMapFragment fragment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.txtPlayerName);
            checkpointName = itemView.findViewById(R.id.txtCheckpointName);
            timeAgo = itemView.findViewById(R.id.txtTimeAgo);
            checkpointImage = itemView.findViewById(R.id.imgCheckpointImage);
            pbImage = itemView.findViewById(R.id.pbImage);

            itemView.setOnClickListener(v -> {
                fragment = new OrganizerNewestMapFragment();
                Bundle arguments = new Bundle();
                arguments.putString("checkpointId", checkpoint.getId());
                fragment.setArguments(arguments);

                transaction = manager.beginTransaction();
                transaction.replace(R.id.fragmentContainer, fragment, null);
                transaction.commit();
            });
        }
    }
}
