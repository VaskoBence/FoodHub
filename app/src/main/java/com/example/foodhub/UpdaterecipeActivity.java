package com.example.foodhub;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodhub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class UpdaterecipeActivity extends AppCompatActivity {

    private EditText titleEditText, ingredientsEditText, descriptionEditText;
    private ImageView recipeImageView;
    private Button saveButton, selectImageButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Uri imageUri;
    private String recipeId;
    private String imageBase64 = "";

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updaterecipe);
        EdgeToEdge.enable(this);

        titleEditText = findViewById(R.id.recipeTitleEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        recipeImageView = findViewById(R.id.recipeImageView);
        saveButton = findViewById(R.id.saveRecipeButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        // Change the text of the save button
        saveButton.setText("Recept frissítése");

        // Get recipeId from intent
        recipeId = getIntent().getStringExtra("recipeId");

        // Load existing recipe data
        loadRecipeData(recipeId);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecipe();
            }
        });
    }

    private void loadRecipeData(String recipeId) {
        DocumentReference docRef = db.collection("recipes").document(recipeId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Recipe recipe = documentSnapshot.toObject(Recipe.class);
                if (recipe != null) {
                    titleEditText.setText(recipe.getTitle());
                    ingredientsEditText.setText(recipe.getIngredients());
                    descriptionEditText.setText(recipe.getDescription());

                    imageBase64 = recipe.getRecipeImage();
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        recipeImageView.setImageBitmap(decodedByte);
                    }
                }
            } else {
                Toast.makeText(this, "Nem sikerült betölteni a receptet!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Nem sikerült betölteni a receptet!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                recipeImageView.setImageBitmap(bitmap);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateRecipe() {
        String title = titleEditText.getText().toString().trim();
        String ingredients = ingredientsEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty() || ingredients.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Kérlek tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DocumentReference recipeRef = db.collection("recipes").document(recipeId);
            recipeRef.update(
                    "title", title,
                    "ingredients", ingredients,
                    "description", description,
                    "recipeImage", imageBase64,
                    "createdAt", new Date() // Update the createdAt date
            ).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Recept sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Recept frissítési hiba!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}