package com.example.foodhub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView profileIcon, logoutButton;
    FloatingActionButton addRecipeButton;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private List<Recipe> originalRecipeList; // Eredeti lista megőrzéséhez
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String LOG_TAG = MainActivity.class.getName();
    EditText searchEditText;
    private Animation pulseAnimation;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;


    private List<QueryDocumentSnapshot> documents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Értesítési engedély kérése, ha szükséges
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }

        profileIcon = findViewById(R.id.profileIcon);
        logoutButton = findViewById(R.id.logoutButton);
        addRecipeButton = findViewById(R.id.addRecipeButton);
        searchEditText = findViewById(R.id.searchEditText);


        // Ablak insets beállítása
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        originalRecipeList = new ArrayList<>(); // Inicializáljuk az eredeti listát
        adapter = new RecipeAdapter(this, recipeList, documents, false);
        recyclerView.setAdapter(adapter);
        loadRecipesFromFirestore();

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
        // Load the pulse animation
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);

        // Start the pulse animation
        addRecipeButton.startAnimation(pulseAnimation);

        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddrecipeActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadRecipesFromFirestore() {
        db.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Order by createdAt
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recipeList.clear();
                        originalRecipeList.clear();
                        List<QueryDocumentSnapshot> documents = new ArrayList<>(); // Create a list to hold the documents
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipeList.add(recipe);
                            originalRecipeList.add(recipe);
                            documents.add(document); // Add the document to the list
                            Log.d(LOG_TAG, "Recept betöltve: " + recipe.getTitle());
                        }
                        adapter = new RecipeAdapter(MainActivity.this, recipeList, documents, false);                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(LOG_TAG, "Hiba a receptek betöltésekor.", task.getException());
                        Toast.makeText(this, "Hiba a receptek betöltésekor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        addRecipeButton.clearAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Frissítjük a receptlistát, amikor visszatérünk az aktivitásba
        loadRecipesFromFirestore();
        addRecipeButton.startAnimation(pulseAnimation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Engedély megadva
                Toast.makeText(this, "Értesítési engedély megadva!", Toast.LENGTH_SHORT).show();
            } else {
                // Engedély megtagadva
                Toast.makeText(this, "Értesítési engedély megtagadva!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // MainActivity.java
    private void filterRecipes(String text) {
        recipeList.clear();
        if (text.isEmpty()) {
            recipeList.addAll(originalRecipeList);
        } else {
            text = text.toLowerCase(Locale.getDefault());
            for (Recipe recipe : originalRecipeList) {
                if (recipe.getTitle().toLowerCase(Locale.getDefault()).contains(text)) {
                    recipeList.add(recipe);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void toProfile(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void toAddRecipe(View view) {
        Intent intent = new Intent(MainActivity.this, AddrecipeActivity.class);
        startActivity(intent);
    }
}