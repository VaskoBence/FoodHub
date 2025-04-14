package com.example.foodhub;

public class Recipe {
    private String title;
    private int imageResId;
    private String ingredients;
    private String description;

    // constructor, getters
    public Recipe(String title, int imageResId, String ingredients, String description) {
        this.title = title;
        this.imageResId = imageResId;
        this.ingredients = ingredients;
        this.description = description;
    }

    public String getTitle() { return title; }
    public int getImageResId() { return imageResId; }
    public String getIngredients() { return ingredients; }
    public String getDescription() { return description; }
}
