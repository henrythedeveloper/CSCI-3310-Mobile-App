package com.example.csci_3310_nav;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DirectionsAdapter extends RecyclerView.Adapter<DirectionsAdapter.ViewHolder> {

    private List<DirectionsFragment.NavStep> steps;

    public DirectionsAdapter(List<DirectionsFragment.NavStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_direction_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DirectionsFragment.NavStep step = steps.get(position);
        holder.instruction.setText(step.instruction);
        holder.distance.setText(step.distanceText);

        // Simple logic to rotate arrow based on text
        String text = step.instruction.toLowerCase();
        if (text.contains("left")) {
            holder.icon.setRotation(-90);
        } else if (text.contains("right")) {
            holder.icon.setRotation(90);
        } else if (text.contains("u-turn")) {
            holder.icon.setRotation(180);
        } else {
            holder.icon.setRotation(0); // Straight
        }
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView instruction, distance;
        ImageView icon;

        public ViewHolder(View view) {
            super(view);
            instruction = view.findViewById(R.id.step_instruction);
            distance = view.findViewById(R.id.step_distance);
            icon = view.findViewById(R.id.step_icon);
        }
    }
}