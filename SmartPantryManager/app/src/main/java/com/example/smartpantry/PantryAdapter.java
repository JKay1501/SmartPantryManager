package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Custom adapter: binds pantry items from the database to the RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(Item item);
        void onDelete(Item item);
    }

    private final List<Item> items;
    private final boolean showAlerts;
    private final Listener listener;

    public PantryAdapter(List<Item> items, boolean showAlerts, Listener listener) {
        this.items = items; this.showAlerts = showAlerts; this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, detail;
        Button delete;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            detail = v.findViewById(R.id.tvDetail);
            delete = v.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item item = items.get(position);
        h.name.setText(item.name);
        String text = item.qtyText() + " " + item.unit;
        boolean soon = showAlerts && expiresSoon(item.expiry);
        if (item.expiry != null && !item.expiry.isEmpty()) text += "  |  Expires: " + item.expiry;
        if (soon) text += "  (EXPIRING SOON)";
        h.detail.setText(text);
        h.detail.setTextColor(soon ? Color.RED : Color.DKGRAY);
        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private boolean expiresSoon(String e) {
        if (e == null || e.isEmpty()) return false;
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(e);
            return d.getTime() - System.currentTimeMillis() < 3L * 24 * 3600 * 1000;
        } catch (Exception ex) {
            return false;
        }
    }
}
