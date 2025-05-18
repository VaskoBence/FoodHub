package com.example.foodhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodhub.AddrecipeActivity;
import com.example.foodhub.R;
import com.example.foodhub.Recipe;
import com.example.foodhub.RecipeAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileTitle;
    private RecyclerView myRecipesRecyclerView;
    private RecyclerView savedRecipesRecyclerView;
    private RecipeAdapter myRecipeAdapter;
    private RecipeAdapter savedRecipeAdapter;
    private List<Recipe> myRecipeList = new ArrayList<>();
    private List<Recipe> savedRecipeList = new ArrayList<>();
    private List<Recipe> originalMyRecipeList = new ArrayList<>();
    private List<Recipe> originalSavedRecipeList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String LOG_TAG = ProfileActivity.class.getName();
    private EditText searchEditText;

    private ImageView profileIcon;
    private Animation pulseAnimation;
    FloatingActionButton addRecipeButton;

    private List<QueryDocumentSnapshot> documents = new ArrayList<>();

    private List<QueryDocumentSnapshot> documents2 = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileIcon = findViewById(R.id.logoImageView);
        profileTitle = findViewById(R.id.profileTitle);
        myRecipesRecyclerView = findViewById(R.id.myRecipesRecyclerView);
        savedRecipesRecyclerView = findViewById(R.id.savedRecipesRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        addRecipeButton = findViewById(R.id.addRecipeButton);

        // Load the pulse animation
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);

        // Start the pulse animation
        addRecipeButton.startAnimation(pulseAnimation);

        // Saját receptek RecyclerView beállítása
        myRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecipeAdapter = new RecipeAdapter(this, myRecipeList,documents,  true);
        myRecipesRecyclerView.setAdapter(myRecipeAdapter);

        // Kedvenc receptek RecyclerView beállítása
        savedRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedRecipeAdapter = new RecipeAdapter(this, savedRecipeList,documents2, false);        savedRecipesRecyclerView.setAdapter(savedRecipeAdapter);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserProfile(user);
            loadMyRecipes(user.getUid());
            loadFavoriteRecipes(user.getUid());
        } else {
            profileTitle.setText("Felhasználó nincs bejelentkezve");
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserProfile(user);
            loadMyRecipes(user.getUid());
            loadFavoriteRecipes(user.getUid());
            addRecipeButton.startAnimation(pulseAnimation);
        } else {
            profileTitle.setText("Felhasználó nincs bejelentkezve");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        addRecipeButton.clearAnimation();
    }

    private void loadUserProfile(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    if (username != null && !username.isEmpty()) {
                        profileTitle.setText("Üdv, " + username + "!");
                        profileTitle.invalidate();
                        profileTitle.requestLayout();
                    } else {
                        Log.d(LOG_TAG, "A felhasználónév üres vagy null.");
                        profileTitle.setText("Üdv, Felhasználó!");
                        profileTitle.invalidate();
                        profileTitle.requestLayout();
                    }
                } else {
                    Log.d(LOG_TAG, "Nincs ilyen dokumentum.");
                    profileTitle.setText("Üdv, Felhasználó!");
                    profileTitle.invalidate();
                    profileTitle.requestLayout();
                }
            } else {
                Log.d(LOG_TAG, "Sikertelen lekérés: ", task.getException());
                profileTitle.setText("Üdv, Felhasználó!");
                profileTitle.invalidate();
                profileTitle.requestLayout();
            }
        });
    }

    private void loadMyRecipes(String userId) {
        db.collection("recipes")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        myRecipeList.clear();
                        originalMyRecipeList.clear();
                        documents.clear(); // Clear the documents list here
                        int recipeCount = 0; // Counter for loaded recipes
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);

                            if (recipe.getCreatedAt() != null) {
                                myRecipeList.add(recipe);
                                originalMyRecipeList.add(recipe);
                                documents.add(document); // Add the document to the list
                                Log.d(LOG_TAG, "Saját recept betöltve: " + recipe.getTitle());
                                recipeCount++; // Increment the counter
                            } else {
                                Log.w(LOG_TAG, "Skipping recipe due to null createdAt: " + document.getId());
                            }

                        }
                        myRecipeAdapter.notifyDataSetChanged();
                        Log.d(LOG_TAG, "Number of recipes loaded: " + recipeCount); // Log the number of loaded recipes
                        if (recipeCount == 0) {
                            Toast.makeText(this, "Nincsenek saját receptjeid!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(LOG_TAG, "Hiba a saját receptek betöltésekor.", task.getException());
                        Toast.makeText(this, "Hiba a saját receptek betöltésekor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void loadFavoriteRecipes(String userId) {
        Log.d(LOG_TAG, "loadFavoriteRecipes called for userId: " + userId);
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> favorites = (List<String>) document.get("favorites");
                    Log.d(LOG_TAG, "Favorites list: " + favorites);

                    if (favorites != null && !favorites.isEmpty()) {
                        loadRecipesFromFavorites(userId, favorites);
                    } else {
                        Log.d(LOG_TAG, "No favorites found for user.");
                        clearFavoriteRecipes();
                        Toast.makeText(this, "Nincsenek kedvenc receptjeid!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(LOG_TAG, "User document not found.");
                    clearFavoriteRecipes();
                    Toast.makeText(this, "Nincs ilyen felhasználó!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(LOG_TAG, "Failed to get user document: ", task.getException());
                clearFavoriteRecipes();
                Toast.makeText(this, "Sikertelen lekérés!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipesFromFavorites(String userId, List<String> favorites) {
        Log.d(LOG_TAG, "Loading recipes from favorites: " + favorites);
        db.collection("recipes")
                .whereIn("__name__", favorites)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> recipesToRemove = new ArrayList<>();
                        savedRecipeList.clear();
                        originalSavedRecipeList.clear();
                        documents2.clear(); // Clear the documents2 list here

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            savedRecipeList.add(recipe);
                            originalSavedRecipeList.add(recipe);
                            documents2.add(document); // Add the document to the list
                            Log.d(LOG_TAG, "Kedvenc recept betöltve: " + recipe.getTitle() + ", ID: " + document.getId());
                        }

                        // Check if all favorites exist, if not, add them to the removal list
                        for (String favoriteId : favorites) {
                            boolean found = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().equals(favoriteId)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                recipesToRemove.add(favoriteId);
                                Log.d(LOG_TAG, "Recipe ID " + favoriteId + " not found, adding to removal list.");
                            }
                        }

                        // Remove non-existing recipes from favorites
                        if (!recipesToRemove.isEmpty()) {
                            removeNonExistingRecipes(userId, recipesToRemove);
                        } else {
                            savedRecipeAdapter.notifyDataSetChanged();
                            Log.d(LOG_TAG, "Favorite recipes loaded and adapter updated. Count: " + savedRecipeList.size());
                        }
                    } else {
                        Log.e(LOG_TAG, "Failed to get favorite recipes: ", task.getException());
                        Toast.makeText(this, "Hiba a kedvenc receptek betöltésekor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeNonExistingRecipes(String userId, List<String> recipesToRemove) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> favorites = (List<String>) document.get("favorites");
                    if (favorites != null) {
                        favorites.removeAll(recipesToRemove);
                        userRef.update("favorites", favorites)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(LOG_TAG, "Successfully removed non-existing recipes from favorites.");
                                    savedRecipeAdapter.notifyDataSetChanged();
                                    // Reload favorites to reflect changes
                                    loadRecipesFromFavorites(userId, favorites);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(LOG_TAG, "Failed to remove non-existing recipes from favorites: ", e);
                                    Toast.makeText(this, "Hiba a kedvenc receptek frissítésekor!", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            } else {
                Log.e(LOG_TAG, "Failed to get user document: ", task.getException());
                Toast.makeText(this, "Sikertelen lekérés!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFavoriteRecipes() {
        savedRecipeList.clear();
        originalSavedRecipeList.clear();
        savedRecipeAdapter.notifyDataSetChanged();
        Log.d(LOG_TAG, "Favorite recipes cleared.");
    }

    // ProfileActivity.java
    private void filterRecipes(String text) {
        myRecipeList.clear();
        savedRecipeList.clear();

        if (text.isEmpty()) {
            myRecipeList.addAll(originalMyRecipeList);
            savedRecipeList.addAll(originalSavedRecipeList);
        } else {
            text = text.toLowerCase(Locale.getDefault());

            // Szűrés a saját receptek között
            for (Recipe recipe : originalMyRecipeList) {
                if (recipe.getTitle().toLowerCase(Locale.getDefault()).contains(text)) {
                    myRecipeList.add(recipe);
                }
            }

            // Szűrés a kedvenc receptek között
            for (Recipe recipe : originalSavedRecipeList) {
                if (recipe.getTitle().toLowerCase(Locale.getDefault()).contains(text)) {
                    savedRecipeList.add(recipe);
                }
            }
        }
        myRecipeAdapter.notifyDataSetChanged();
        savedRecipeAdapter.notifyDataSetChanged();
    }

    public void toMain(View view){
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void toAddRecipe(View view) {
        Intent intent = new Intent(ProfileActivity.this, AddrecipeActivity.class);
        startActivity(intent);
    }
}