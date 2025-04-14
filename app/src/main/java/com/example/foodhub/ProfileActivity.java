package com.example.foodhub;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Ablak insets beállítása
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // A TextView referenciája
        profileTitle = findViewById(R.id.profileTitle);

        // Firebase bejelentkezett felhasználó lekérése
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Ha van bejelentkezett felhasználó, a profil címet email címre állítjuk
            String userEmail = user.getEmail();
            profileTitle.setText("Üdv, " + userEmail);  // Email cím kiírása
        } else {
            // Ha nincs bejelentkezve felhasználó, alapértelmezett üzenet
            profileTitle.setText("Felhasználó nincs bejelentkezve");
        }
    }
}
