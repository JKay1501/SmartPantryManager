package com.example.smartpantry;

public class Recipe {
    public long id;
    public String name;
    public String steps;

    public Recipe(long id, String name, String steps) {
        this.id = id; this.name = name; this.steps = steps;
    }
}
