package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** SQLite database: pantry CRUD, seeded recipes, and the strict-matching logic. */
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context c) {
        super(c, "pantry.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, qty REAL, unit TEXT, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, steps TEXT)");
        db.execSQL("CREATE TABLE recipe_ing (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER, name TEXT, qty REAL, unit TEXT)");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS pantry");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS recipe_ing");
        onCreate(db);
    }

    // ---------- Pantry CRUD ----------
    public void addItem(Item i) {
        getWritableDatabase().insert("pantry", null, values(i));
    }

    public void updateItem(Item i) {
        getWritableDatabase().update("pantry", values(i), "id=?", new String[]{String.valueOf(i.id)});
    }

    public void deleteItem(long id) {
        getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    public Item getItem(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id,name,qty,unit,expiry FROM pantry WHERE id=?", new String[]{String.valueOf(id)});
        Item item = null;
        if (c.moveToFirst()) item = new Item(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), c.getString(4));
        c.close();
        return item;
    }

    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT id,name,qty,unit,expiry FROM pantry ORDER BY name", null);
        while (c.moveToNext())
            list.add(new Item(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), c.getString(4)));
        c.close();
        return list;
    }

    private ContentValues values(Item i) {
        ContentValues v = new ContentValues();
        v.put("name", i.name);
        v.put("qty", i.qty);
        v.put("unit", i.unit);
        v.put("expiry", i.expiry);
        return v;
    }

    // ---------- Recipes ----------
    public Recipe getRecipe(long id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id,name,steps FROM recipes WHERE id=?", new String[]{String.valueOf(id)});
        Recipe r = null;
        if (c.moveToFirst()) r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
        c.close();
        return r;
    }

    public List<String> getRecipeIngredients(long recipeId) {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT name,qty,unit FROM recipe_ing WHERE recipe_id=?", new String[]{String.valueOf(recipeId)});
        while (c.moveToNext()) {
            double q = c.getDouble(1);
            String qt = (q == Math.floor(q)) ? String.valueOf((long) q) : String.valueOf(q);
            list.add("- " + qt + " " + c.getString(2) + " " + c.getString(0));
        }
        c.close();
        return list;
    }

    /** Makes names comparable: lower-case, trimmed, simple singular form ("tomatoes" -> "tomato"). */
    public static String norm(String s) {
        s = s.trim().toLowerCase(Locale.ROOT);
        if (s.endsWith("ies") && s.length() > 4) return s.substring(0, s.length() - 3) + "y";
        if (s.endsWith("oes")) return s.substring(0, s.length() - 2);
        if (s.endsWith("s") && !s.endsWith("ss")) return s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * STRICT MATCHING: a recipe is returned only if EVERY ingredient is in the pantry
     * in at least the required quantity (same base unit). One missing = excluded.
     */
    public List<Recipe> getSuggestedRecipes() {
        Map<String, Double> have = new HashMap<>();
        for (Item i : getAllItems()) {
            String key = norm(i.name) + "|" + i.unit;
            have.put(key, have.getOrDefault(key, 0.0) + i.qty); // add up duplicate entries
        }
        List<Recipe> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor r = db.rawQuery("SELECT id,name,steps FROM recipes ORDER BY name", null);
        while (r.moveToNext()) {
            boolean canMake = true;
            Cursor c = db.rawQuery("SELECT name,qty,unit FROM recipe_ing WHERE recipe_id=?", new String[]{String.valueOf(r.getLong(0))});
            while (c.moveToNext()) {
                Double got = have.get(norm(c.getString(0)) + "|" + c.getString(2));
                if (got == null || got < c.getDouble(1)) canMake = false;
            }
            c.close();
            if (canMake) result.add(new Recipe(r.getLong(0), r.getString(1), r.getString(2)));
        }
        r.close();
        return result;
    }

    /**
     * Returns EVERY recipe with a status. A message is only added when
     * exactly ONE ingredient is missing or too low.
     */
    public List<RecipeStatus> getRecipeStatuses() {
        // add up what the pantry holds: "tomato|pcs" -> 3.0
        Map<String, Double> have = new HashMap<>();
        for (Item i : getAllItems()) {
            String key = norm(i.name) + "|" + i.unit;
            have.put(key, have.getOrDefault(key, 0.0) + i.qty);
        }

        List<RecipeStatus> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor r = db.rawQuery("SELECT id,name,steps FROM recipes", null);
        while (r.moveToNext()) {
            int problems = 0;
            String problemText = "";
            Cursor c = db.rawQuery("SELECT name,qty,unit FROM recipe_ing WHERE recipe_id=?",
                    new String[]{String.valueOf(r.getLong(0))});
            while (c.moveToNext()) {
                double need = c.getDouble(1);
                Double got = have.get(norm(c.getString(0)) + "|" + c.getString(2));
                double haveQty = (got == null) ? 0 : got;
                if (haveQty < need) { // missing completely, or not enough
                    problems++;
                    problemText = RecipeStatus.shortageMessage(c.getString(0), need, haveQty, c.getString(2));
                }
            }
            c.close();

            Recipe recipe = new Recipe(r.getLong(0), r.getString(1), r.getString(2));
            String message = null; // 2 or more problems: no message on purpose
            if (problems == 0) message = "You have everything you need!";
            else if (problems == 1) message = problemText;
            result.add(new RecipeStatus(recipe, problems == 0, problems, message));
        }
        r.close();
        RecipeStatus.sortByReadiness(result);
        return result;
    }

    // ---------- Seed data (runs once, on first launch) ----------
    private void seed(SQLiteDatabase db) {
        add(db, "Scrambled Eggs", "1. Beat the eggs with salt.\n2. Melt butter in a pan.\n3. Stir eggs gently until set.", "egg:2:pcs", "butter:10:g", "salt:2:g");
        add(db, "Cheese Omelette", "1. Beat eggs with salt.\n2. Pour into a hot pan.\n3. Add cheese, fold and serve.", "egg:3:pcs", "cheese:30:g", "salt:2:g");
        add(db, "Tomato Pasta", "1. Boil the pasta.\n2. Fry onion and garlic, add chopped tomatoes.\n3. Simmer, mix with pasta.", "pasta:200:g", "tomato:3:pcs", "onion:1:pcs", "garlic:2:pcs");
        add(db, "Egg Fried Rice", "1. Fry onion in oil.\n2. Add cooked rice.\n3. Push aside, scramble eggs, mix together.", "rice:200:g", "egg:2:pcs", "onion:1:pcs", "oil:15:ml");
        add(db, "Cheese Toastie", "1. Butter the bread.\n2. Add cheese between slices.\n3. Toast in a pan until golden.", "bread:2:pcs", "cheese:40:g", "butter:10:g");
        add(db, "Pancakes", "1. Whisk flour, milk, egg and sugar.\n2. Fry ladles of batter until golden on both sides.", "flour:150:g", "milk:250:ml", "egg:1:pcs", "sugar:20:g");
        add(db, "Mashed Potatoes", "1. Boil peeled potatoes.\n2. Drain and mash with butter, milk and salt.", "potato:4:pcs", "butter:20:g", "milk:50:ml", "salt:2:g");
        add(db, "Garlic Bread", "1. Mix butter with crushed garlic.\n2. Spread on bread.\n3. Bake until crisp.", "bread:4:pcs", "butter:30:g", "garlic:2:pcs");
        add(db, "Tuna Salad", "1. Drain the tuna.\n2. Chop tomato and onion.\n3. Mix with oil.", "tuna:1:pcs", "onion:1:pcs", "tomato:2:pcs", "oil:10:ml");
        add(db, "Vegetable Soup", "1. Chop vegetables.\n2. Simmer in water with salt for 25 minutes.", "carrot:2:pcs", "potato:2:pcs", "onion:1:pcs", "salt:3:g");
        add(db, "Banana Smoothie", "1. Peel bananas.\n2. Blend with milk and sugar.", "banana:2:pcs", "milk:300:ml", "sugar:10:g");
        add(db, "French Toast", "1. Whisk egg, milk and sugar.\n2. Dip bread.\n3. Fry until golden.", "bread:4:pcs", "egg:2:pcs", "milk:100:ml", "sugar:10:g");
        add(db, "Baked Potatoes", "1. Rub potatoes with oil and salt.\n2. Bake at 200C for 50 minutes.", "potato:4:pcs", "oil:15:ml", "salt:3:g");
        add(db, "Rice and Beans", "1. Fry onion and garlic.\n2. Add beans and cooked rice.\n3. Heat through.", "rice:200:g", "bean:1:pcs", "onion:1:pcs", "garlic:1:pcs");
        add(db, "Cheesy Pasta", "1. Boil pasta.\n2. Melt butter, add milk and cheese.\n3. Stir in pasta.", "pasta:200:g", "cheese:80:g", "milk:100:ml", "butter:15:g");
        add(db, "Tomato Soup", "1. Fry onion and garlic in butter.\n2. Add tomatoes, simmer 20 minutes.\n3. Blend.", "tomato:6:pcs", "onion:1:pcs", "garlic:2:pcs", "butter:15:g");
    }

    /** ingredients are written as "name:quantity:unit" */
    private void add(SQLiteDatabase db, String name, String steps, String... ings) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("steps", steps);
        long rid = db.insert("recipes", null, v);
        for (String s : ings) {
            String[] p = s.split(":");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", rid);
            iv.put("name", p[0]);
            iv.put("qty", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ing", null, iv);
        }
    }
}
