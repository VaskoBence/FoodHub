package com.example.foodhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String LOG_TAG = RecipeAdapter.class.getName();

    private boolean showDeleteButton;
    private List<QueryDocumentSnapshot> documents;

    public RecipeAdapter(Context context, List<Recipe> recipeList, List<QueryDocumentSnapshot> documents, boolean showDeleteButton) {
        this.context = context;
        this.recipeList = recipeList;
        this.documents = documents;
        this.showDeleteButton = showDeleteButton;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }



    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        QueryDocumentSnapshot document = documents.get(position); // Get the document at the current position
        holder.title.setText(recipe.getTitle());

        // Decode Base64 string to Bitmap
        String imageBase64 = recipe.getRecipeImage();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.image.setImageBitmap(decodedByte);
        } else {
            // If image is null or empty, set a default image
            holder.image.setImageResource(R.drawable.ic_launcher_background); // Replace with your default image
        }

        // Felhasználónév lekérése a Firestore-ból
        String userId = recipe.getUserId();
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                if (username != null && !username.isEmpty()) {
                    holder.recipeUserName.setText("Készítette: " + username);
                } else {
                    holder.recipeUserName.setText("Készítette: Ismeretlen felhasználó");
                }
            } else {
                holder.recipeUserName.setText("Készítette: Ismeretlen felhasználó");
            }
        }).addOnFailureListener(e -> {
            Log.e(LOG_TAG, "Nem sikerült lekérni a felhasználónevet: " + e.getMessage());
            holder.recipeUserName.setText("Készítette: Ismeretlen felhasználó");
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipeId", document.getId()); // Add the document ID here
            context.startActivity(intent);
        });
        // Törlés gomb megjelenítése/elrejtése
        if (showDeleteButton) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Törlés gomb eseménykezelője
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Recept törlése")
                    .setMessage("Biztosan törölni szeretnéd ezt a receptet?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Törlés a Firestore-ból
                        db.collection("recipes").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(LOG_TAG, "DocumentSnapshot successfully deleted!");
                                    // Törlés a listából és frissítés
                                    recipeList.remove(position);
                                    documents.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, recipeList.size());
                                    Toast.makeText(context, "Recept sikeresen törölve!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(LOG_TAG, "Error deleting document", e);
                                    Toast.makeText(context, "Hiba a recept törlése közben!", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView recipeUserName;
        ImageButton deleteButton;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recipeImage);
            title = itemView.findViewById(R.id.recipeTitle);
            recipeUserName = itemView.findViewById(R.id.recipeUserName); // And this line
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}