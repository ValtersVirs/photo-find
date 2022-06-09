package com.example.photofind.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.photofind.R;
import com.example.photofind.models.Checkpoint;

import java.util.ArrayList;

public class PlayerCheckpointAdapter extends RecyclerView.Adapter<PlayerCheckpointAdapter.ViewHolder> {
    ArrayList<Checkpoint> checkpointList;

    public PlayerCheckpointAdapter(ArrayList<Checkpoint> checkpointList) {
        this.checkpointList = checkpointList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_player_checkpoint_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerCheckpointAdapter.ViewHolder holder, int position) {
        Checkpoint checkpoint = checkpointList.get(position);
        holder.checkpoint = checkpoint;
        holder.checkpointName.setText(checkpoint.getTitle());

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
        Checkpoint checkpoint;
        TextView checkpointName;
        ImageView checkpointImage;
        ProgressBar pbImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkpointName = itemView.findViewById(R.id.txtCheckpointName);
            checkpointImage = itemView.findViewById(R.id.imgCheckpointImage);
            pbImage = itemView.findViewById(R.id.pbImage);

            itemView.setOnClickListener(v -> {

            });
        }
    }
}
