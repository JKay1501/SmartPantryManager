package com.example.smartpantry;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DbHelper db = new DbHelper(this);
        long id = getIntent().getLongExtra("id", -1);
        Recipe r = db.getRecipe(id);
        if (r == null) { finish(); return; }

        setTitle(r.name);
        ((TextView) findViewById(R.id.tvTitle)).setText(r.name);
        StringBuilder sb = new StringBuilder();
        for (String line : db.getRecipeIngredients(id)) sb.append(line).append("\n");
        ((TextView) findViewById(R.id.tvIngredients)).setText(sb.toString());
        ((TextView) findViewById(R.id.tvSteps)).setText(r.steps);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
