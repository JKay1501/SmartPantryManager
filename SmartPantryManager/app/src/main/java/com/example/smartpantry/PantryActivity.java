package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Main screen: lists pantry items (Read), opens Add/Edit, deletes items. */
public class PantryActivity extends AppCompatActivity {

    private DbHelper db;
    private RecyclerView rv;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        setTitle("My Pantry");
        db = new DbHelper(this);
        rv = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, EditIngredientActivity.class)));
    }

    @Override
    protected void onResume() { // reload the list every time we come back to this screen
        super.onResume();
        loadList();
    }

    private void loadList() {
        List<Item> items = db.getAllItems();
        boolean alerts = getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("alerts", true);
        rv.setAdapter(new PantryAdapter(items, alerts, new PantryAdapter.Listener() {
            @Override
            public void onEdit(Item item) {
                Intent i = new Intent(PantryActivity.this, EditIngredientActivity.class);
                i.putExtra("id", item.id); // pass data to the next screen
                startActivity(i);
            }

            @Override
            public void onDelete(Item item) {
                new AlertDialog.Builder(PantryActivity.this)
                        .setTitle("Delete " + item.name + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteItem(item.id);
                            loadList();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_all) {
            startActivity(new Intent(this, AllRecipesActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_suggested) {
            startActivity(new Intent(this, SuggestedActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}