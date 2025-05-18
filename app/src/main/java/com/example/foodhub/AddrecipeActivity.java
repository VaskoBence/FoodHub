package com.example.foodhub;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

public class AddrecipeActivity extends AppCompatActivity {

    private static final String LOG_TAG = AddrecipeActivity.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private EditText recipeTitleEditText, ingredientsEditText, descriptionEditText;
    private ImageView recipeImageView;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addrecipe);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recipeTitleEditText = findViewById(R.id.recipeTitleEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        recipeImageView = findViewById(R.id.recipeImageView);

        // Itt állítjuk be az alapértelmezett képet
        recipeImageView.setImageResource(R.drawable.img_placeholder);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                Toast.makeText(this, "Engedélyek megadva!", Toast.LENGTH_SHORT).show();
                chooseImageDialog(); // Show image selection dialog after permissions are granted
            } else {
                // Permissions denied
                Toast.makeText(this, "Engedélyek megtagadva!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void chooseImage(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            chooseImageDialog(); // Show image selection dialog if permissions are already granted
        }
    }

    private void chooseImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Válassz egy képet");
        builder.setItems(new CharSequence[]{"Kamera", "Galéria", "Mégse"}, (dialog, which) -> {
            switch (which) {
                case 0: // Kamera
                    takePictureFromCamera();
                    break;
                case 1: // Galéria
                    pickImageFromGallery();
                    break;
                case 2: // Mégse
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void takePictureFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    recipeImageView.setImageBitmap(imageBitmap);
                    imageUri = getImageUri(getApplicationContext(), imageBitmap);
                    break;
                case REQUEST_IMAGE_GALLERY:
                    imageUri = data.getData();
                    recipeImageView.setImageURI(imageUri);
                    break;
            }
        } else {
            // Ha nincs kép kiválasztva, állítsd be az img_placeholder képet
            recipeImageView.setImageResource(R.drawable.img_placeholder);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void saveRecipe(View view) {
        String title = recipeTitleEditText.getText().toString();
        String ingredients = ingredientsEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        if (title.isEmpty() || ingredients.isEmpty() || description.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Kérlek tölts ki minden mezőt és válassz egy képet!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            uploadImageAndSaveRecipe(userId, title, ingredients, description);
        } else {
            Toast.makeText(this, "Nincs bejelentkezett felhasználó!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveRecipe(String userId, String title, String ingredients, String description) {
        String imageBase64 = imageToBase64(imageUri);
        if (imageBase64 != null) {
            saveRecipeToFirestore(userId, title, ingredients, description, imageBase64);
        } else {
            Toast.makeText(this, "Hiba a kép átalakításakor!", Toast.LENGTH_SHORT).show();
        }
    }

    private String imageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveRecipeToFirestore(String userId, String title, String ingredients, String description, String imageBase64) {
        Recipe recipe = new Recipe(userId, title, imageBase64, ingredients, description);
        recipe.setCreatedAt(new Date()); // Set the createdAt date

        db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    Log.d(LOG_TAG, "Recept sikeresen hozzáadva a Firestore-hoz: " + documentReference.getId());
                    Toast.makeText(this, "Recept sikeresen hozzáadva!", Toast.LENGTH_SHORT).show();

                    // Értesítés küldése a NotificationService segítségével
                    Intent intent = new Intent(AddrecipeActivity.this, NotificationService.class);
                    intent.putExtra("message", "Új recept feltöltve: " + title);
                    intent.putExtra("recipeId", documentReference.getId()); // Recept ID hozzáadása
                    startService(intent);

                    finish(); // Visszatérés az előző activity-re
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Hiba a recept hozzáadásakor a Firestore-hoz!", e);
                    Toast.makeText(this, "Hiba a recept hozzáadásakor!", Toast.LENGTH_SHORT).show();
                });
    }
}