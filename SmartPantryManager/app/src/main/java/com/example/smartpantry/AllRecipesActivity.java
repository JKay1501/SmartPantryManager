package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Shows ALL preloaded recipes, even when the pantry is empty. */
public class AllRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);
        setTitle("All Recipes");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // every recipe, each checked against the current pantry
        List<RecipeStatus> list = new DbHelper(this).getRecipeStatuses();

        RecyclerView rv = findViewById(R.id.rvAll);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RecipeStatusAdapter(list, r -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("id", r.id); // send the recipe id to the detail screen
            startActivity(i);
        }));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
