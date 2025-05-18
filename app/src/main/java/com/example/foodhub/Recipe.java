// Recipe.java
package com.example.foodhub;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Recipe {

    private String userId;
    private String title;
    private String recipeImage; // Base64 encoded image
    private String ingredients;
    private String description;
    @ServerTimestamp
    private Date createdAt;

    public Recipe() {
        // Default constructor required for Firestore
    }

    public Recipe(String userId, String title, String recipeImage, String ingredients, String description) {
        this.userId = userId;
        this.title = title;
        this.recipeImage = recipeImage;
        this.ingredients = ingredients;
        this.description = description;
    }


    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}