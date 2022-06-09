package com.example.photofind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofind.R;
import com.example.photofind.models.Checkpoint;

import java.util.ArrayList;

public class CreateGameImageAdapter extends RecyclerView.Adapter<CreateGameImageAdapter.ViewHolder> {
    private static ClickListener clickListener;

    ArrayList<Checkpoint> checkpointList;

    public CreateGameImageAdapter(ArrayList<Checkpoint> checkpointList) {
        this.checkpointList = checkpointList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_checkpoint_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Checkpoint checkpoint = checkpointList.get(position);
        holder.checkpoint = checkpoint;

        Glide.with(holder.itemView)
                .load(holder.checkpoint.getImage())
                .into(holder.checkpointImage);
    }

    @Override
    public int getItemCount() {
        return checkpointList.size();
    }

    public void removeItem(int position) {
        checkpointList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        Checkpoint checkpoint;
        ImageView checkpointImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkpointImage = itemView.findViewById(R.id.imgCheckpoint);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        CreateGameImageAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemLongClick(int position, View v);
    }
}
