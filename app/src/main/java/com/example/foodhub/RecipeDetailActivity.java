package com.example.foodhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecipeDetailActivity extends AppCompatActivity {

    TextView titleTextView, ingredientsTextView, descriptionTextView;
    ImageView recipeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        titleTextView = findViewById(R.id.detailTitle);
        ingredientsTextView = findViewById(R.id.ingredientsText);
        descriptionTextView = findViewById(R.id.descriptionText);
        recipeImageView = findViewById(R.id.detailImage);

        // Intentből adatok lekérése
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        int imageResId = intent.getIntExtra("imageResId", R.drawable.ic_launcher_foreground); // default
        String ingredients = intent.getStringExtra("ingredients");
        String description = intent.getStringExtra("description");

        // UI beállítása
        titleTextView.setText(title);
        recipeImageView.setImageResource(imageResId);
        ingredientsTextView.setText(ingredients);
        descriptionTextView.setText(description);
    }
}
