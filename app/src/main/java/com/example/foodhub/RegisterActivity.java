package com.example.foodhub;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameEditText, emailEditText, passwordEditText, passwordConfirmEditText;
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordConfirmEditText);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void register(View view){
        Intent intent = new Intent(this, LoginActivity.class);

        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();


        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            // Show error message
            Toast.makeText(this, "Kérlek tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            // Show error message
            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Sikeres Regisztráció!", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthWeakPasswordException) {
                                String errorMessage = "Gyenge jelszó! A jelszó legyen legalább 6 karakter hosszú.";
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e(LOG_TAG, "Sikertelen regisztráció: \n" + errorMessage, exception);
                            }
                            else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                String errorMessage = "Helytelen email formátum.";
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e(LOG_TAG, "Sikertelen regisztráció: \n" + errorMessage, exception);
                            }
                            else if (exception instanceof FirebaseAuthUserCollisionException) {
                                String errorMessage = "Ezzel az emaillel már regisztráltak.";
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e(LOG_TAG, "Sikertelen regisztráció: \n" + errorMessage, exception);
                            }
                            else {
                                Log.e(LOG_TAG, "Sikertelen regisztráció!", exception);
                                Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }
    public void cancel(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


}