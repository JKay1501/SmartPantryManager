package com.example.smartpantry;

public class RecipeStatus {
    public Recipe recipe;
    public boolean canMake;
    public int problemCount;
    public String message;

    public RecipeStatus(Recipe recipe, boolean canMake, int problemCount, String message) {
        this.recipe = recipe;
        this.canMake = canMake;
        this.problemCount = problemCount;
        this.message = message;
    }

        public static String formatQty(double q) {
            q = Math.round(q * 100) / 100.0;
            return (q == Math.floor(q)) ? String.valueOf((long) q) : String.valueOf(q);
        }

        public static String shortMessage(String name, double need, double have, String unit) {
            if (have <= 0) {
                return "Missing: " + formatQty(need) + " " + unit + " " + name;
            }
            return "Short of: " + formatQty(need - have) + " " + unit + " " + name
                    + " (you have " + formatQty(have) + ", need " + formatQty(need) + ")";
        }


    
}
