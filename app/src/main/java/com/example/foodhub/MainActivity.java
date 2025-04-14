package com.example.foodhub;

import com.example.foodhub.Recipe;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    ImageView profileIcon, logoutButton;
    FloatingActionButton addRecipeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        profileIcon = findViewById(R.id.profileIcon);
        logoutButton = findViewById(R.id.logoutButton);
        addRecipeButton = findViewById(R.id.addRecipeButton);



        // Ablak insets beállítása
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Recipe> dummyRecipes = new ArrayList<>();
        dummyRecipes.add(new Recipe(
                "Lasagne",
                R.drawable.ic_lasagne,
                "- Tészta\n- Darálthús\n- Paradicsomszósz\n- Besamel\n- Sajt",
                "1. A húst pirítsd meg, majd add hozzá a paradicsomszószt.\n2. Rétegezd a tésztával és a besamellel.\n3. Süsd 180°C-on 40 percig."
        ));
        dummyRecipes.add(new Recipe(
                "Palacsinta",
                R.drawable.ic_pancake,
                "- Liszt\n- Tojás\n- Tej\n- Cukor\n- Olaj",
                "1. Keverd ki a tésztát.\n2. Süss kis adagokat forró serpenyőben.\n3. Töltsd meg lekvárral vagy Nutellával."
        ));
        dummyRecipes.add(new Recipe(
                "Tiramisu",
                R.drawable.ic_tiramisu,
                "- Mascarpone\n- Kávé\n- Tojás\n- Cukor\n- Babapiskóta",
                "1. A tojásokat és cukrot keverd habosra.\n2. Add hozzá a mascarponét.\n3. Rétegezd piskótával és kávéval.\n4. Hűtsd pár órát."
        ));
        dummyRecipes.add(new Recipe(
                "Gulyásleves",
                R.drawable.ic_gulyas,
                "- Marhahús\n- Hagyma\n- Paprika\n- Burgonya\n- Kömény",
                "1. A húst pirítsd meg hagymával.\n2. Add hozzá a paprikát és fűszereket.\n3. Öntsd fel vízzel, majd főzd puhára."
        ));


        RecipeAdapter adapter = new RecipeAdapter(this, dummyRecipes);
        recyclerView.setAdapter(adapter);

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
