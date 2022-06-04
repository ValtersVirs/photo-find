package com.example.photofind.adapters;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.photofind.models.TempCheckpoint;

import java.util.ArrayList;

public class OrganizerImageAdapter extends RecyclerView.Adapter<OrganizerImageAdapter.ViewHolder> {
    private static ClickListener clickListener;

    ArrayList<TempCheckpoint> checkpointList;

    public OrganizerImageAdapter(ArrayList<TempCheckpoint> checkpointList) {
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
        TempCheckpoint checkpoint = checkpointList.get(position);
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
        TempCheckpoint checkpoint;
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
        OrganizerImageAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemLongClick(int position, View v);
    }
}
