package com.example.foodhub;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;



public class RecipeDetailActivity extends AppCompatActivity {

    TextView titleTextView, ingredientsTextView, descriptionTextView, userNameTextView;
    ImageView recipeImageView;
    Button addToFavoritesButton, modifyButton; // Added modifyButton
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecipeAdapter recipeAdapter;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        EdgeToEdge.enable(this);


        titleTextView = findViewById(R.id.detailTitle);
        ingredientsTextView = findViewById(R.id.ingredientsText);
        descriptionTextView = findViewById(R.id.descriptionText);
        recipeImageView = findViewById(R.id.detailImage);
        userNameTextView = findViewById(R.id.detailUserName);
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        modifyButton = findViewById(R.id.modifyButton); // Initialize modifyButton
        modifyButton.setVisibility(View.GONE); // Initially hide the modify button

        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId");

        if (recipeId != null && !recipeId.isEmpty()) {
            loadRecipeDetails(recipeId);
        } else {
            Log.e("RecipeDetailActivity", "Nincs recept ID átadva!");
            Toast.makeText(this, "Hiba: Nincs recept ID!", Toast.LENGTH_SHORT).show();
        }

        addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorites();
            }
        });

        // Set onClickListener for the modify button
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the activity to modify the recipe
                Intent modifyIntent = new Intent(RecipeDetailActivity.this, UpdaterecipeActivity.class);
                modifyIntent.putExtra("recipeId", recipeId);
                startActivity(modifyIntent);
            }
        });
    }

    private void loadRecipeDetails(String recipeId) {
        DocumentReference docRef = db.collection("recipes").document(recipeId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Recipe recipe = documentSnapshot.toObject(Recipe.class);
                if (recipe != null) {
                    titleTextView.setText(recipe.getTitle());
                    ingredientsTextView.setText(recipe.getIngredients());
                    descriptionTextView.setText(recipe.getDescription());

                    String imageBase64 = recipe.getRecipeImage();
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        recipeImageView.setImageBitmap(decodedByte);
                    } else {
                        recipeImageView.setImageResource(R.drawable.ic_launcher_background);
                    }

                    // Felhasználónév lekérése a 'users' gyűjteményből
                    String userId = recipe.getUserId();
                    DocumentReference userRef = db.collection("users").document(userId);
                    userRef.get().addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            String username = userSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                userNameTextView.setText("Készítette: " + username);
                            } else {
                                userNameTextView.setText("Készítette: Ismeretlen felhasználó");
                            }
                        } else {
                            userNameTextView.setText("Készítette: Ismeretlen felhasználó");
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("RecipeDetailActivity", "Nem sikerült lekérni a felhasználónevet: " + e.getMessage());
                        userNameTextView.setText("Készítette: Ismeretlen felhasználó");
                    });

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String currentUserId = user.getUid();
                        DocumentReference currentUserRef = db.collection("users").document(currentUserId);

                        currentUserRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    List<String> favorites = (List<String>) document.get("favorites");
                                    if (favorites == null) {
                                        favorites = new ArrayList<>();
                                    }

                                    if (favorites.contains(recipeId)) {
                                        addToFavoritesButton.setText("Eltávolítás a kedvencekből");
                                        addToFavoritesButton.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.white));
                                    } else {
                                        addToFavoritesButton.setText("Kedvencekhez ad");
                                        addToFavoritesButton.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.black));
                                    }
                                }
                            }
                        });

                        // Ellenőrizzük, hogy a jelenlegi felhasználó a recept tulajdonosa-e
                        if (currentUserId.equals(recipe.getUserId())) {
                            modifyButton.setVisibility(View.VISIBLE); // Módosítás gomb megjelenítése
                        } else {
                            modifyButton.setVisibility(View.GONE); // Módosítás gomb elrejtése
                        }
                    }

                } else {
                    Log.e("RecipeDetailActivity", "Recept betöltési hiba");
                    Toast.makeText(this, "Hiba: Recept betöltési hiba!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("RecipeDetailActivity", "Nincs ilyen recept: " + recipeId);
                Toast.makeText(this, "Hiba: Nincs ilyen recept!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("RecipeDetailActivity", "Recept lekérdezési hiba: " + e.getMessage());
            Toast.makeText(this, "Hiba: Recept lekérdezési hiba!", Toast.LENGTH_SHORT).show();
        });
    }

    private void addToFavorites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> favorites = (List<String>) document.get("favorites");
                        if (favorites == null) {
                            favorites = new ArrayList<>();
                        }

                        boolean isFavorite = favorites.contains(recipeId);

                        if (isFavorite) {
                            favorites.remove(recipeId);
                            userRef.update("favorites", favorites)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Eltávolítva a kedvencekből!", Toast.LENGTH_SHORT).show();
                                        addToFavoritesButton.setText("Kedvencekhez ad");
                                        addToFavoritesButton.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.black));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("RecipeDetailActivity", "Hiba a kedvencekből eltávolításkor: " + e.getMessage());
                                        Toast.makeText(this, "Hiba a kedvencekből eltávolításkor!", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            userRef.update("favorites", FieldValue.arrayUnion(recipeId))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Hozzáadva a kedvencekhez!", Toast.LENGTH_SHORT).show();
                                        addToFavoritesButton.setText("Eltávolítás a kedvencekből");
                                        addToFavoritesButton.setCompoundDrawableTintList(ContextCompat.getColorStateList(this, R.color.white));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("RecipeDetailActivity", "Hiba a kedvencekhez adáskor: " + e.getMessage());
                                        Toast.makeText(this, "Hiba a kedvencekhez adáskor!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e("RecipeDetailActivity", "Nincs ilyen felhasználó!");
                        Toast.makeText(this, "Nincs ilyen felhasználó!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("RecipeDetailActivity", "Felhasználó lekérdezési hiba: " + task.getException());
                    Toast.makeText(this, "Felhasználó lekérdezési hiba!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}