package com.example.smartpantry;

public class RecipeStatus {
    public Recipe recipe;
    public boolean canMake;
    public int problemCount;
    public String message;

    public RecipeStatus(Recipe recipe, boolean canMake, int problemCount, String message){
        this.recipe = recipe;
        this.canMake = canMake;
        this.problemCount = problemCount;
        this.message = message;
    }
}
