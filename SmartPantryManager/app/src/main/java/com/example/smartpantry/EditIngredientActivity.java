package com.example.smartpantry;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Add (Create) or Edit (Update) a pantry item, with input validation. */
public class EditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs"};
    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private DbHelper db;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = new DbHelper(this);
        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        editId = getIntent().getLongExtra("id", -1); // data passed by the Intent
        if (editId != -1) {
            setTitle("Edit Ingredient");
            Item item = db.getItem(editId);
            if (item != null) {
                etName.setText(item.name);
                etQty.setText(item.qtyText());
                etExpiry.setText(item.expiry);
                for (int i = 0; i < UNITS.length; i++) if (UNITS[i].equals(item.unit)) spUnit.setSelection(i);
            }
        } else {
            setTitle("Add Ingredient");
        }
        Button save = findViewById(R.id.btnSave);
        save.setOnClickListener(v -> save());
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        // ---- validation ----
        if (name.isEmpty()) { etName.setError("Name is required"); return; }
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etQty.setError("Enter a valid number"); return;
        }
        if (qty <= 0) { etQty.setError("Quantity must be more than 0"); return; }
        if (!expiry.isEmpty() && !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etExpiry.setError("Use format yyyy-MM-dd"); return;
        }

        // ---- convert kg -> g and l -> ml so matching is consistent ----
        String unit = UNITS[spUnit.getSelectedItemPosition()];
        if (unit.equals("kg")) { qty *= 1000; unit = "g"; }
        if (unit.equals("l")) { qty *= 1000; unit = "ml"; }

        Item item = new Item(editId, name, qty, unit, expiry);
        if (editId == -1) db.addItem(item); else db.updateItem(item);
        Toast.makeText(this, "Saved Ingredient", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
