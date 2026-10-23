package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Shows each recipe with its status message underneath (if it has one). */
public class RecipeStatusAdapter extends RecyclerView.Adapter<RecipeStatusAdapter.VH> {

    public interface Listener {
        void onClick(Recipe r);
    }

    private final List<RecipeStatus> items;
    private final Listener listener;

    public RecipeStatusAdapter(List<RecipeStatus> items, Listener listener) {
        this.items = items;
        this.listener = listener;
            super(v);
            name = v.findViewById(R.id.tvStatusName);
            message = v.findViewById(R.id.tvStatusMessage);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_status, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RecipeStatus s = items.get(position);
        h.name.setText(s.recipe.name);
        if (s.message == null) {
            h.message.setVisibility(View.GONE); // 2+ problems: show nothing
        } else {
            h.message.setVisibility(View.VISIBLE);
            h.message.setText(s.message);
            h.message.setTextColor(s.canMake ? Color.parseColor("#2E7D32") : Color.parseColor("#E65100"));
        }
        h.itemView.setOnClickListener(v -> listener.onClick(s.recipe));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}