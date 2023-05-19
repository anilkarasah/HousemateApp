package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.entities.User;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText text_emailAddress;
    EditText text_password;
    Button button_login;
    Button button_signup;
    TextView text_forgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        text_emailAddress = findViewById(R.id.textLoginEmailAddress);
        text_password = findViewById(R.id.textLoginPassword);
        button_login = findViewById(R.id.buttonLogin);
        button_signup = findViewById(R.id.buttonRedirectSignup);
        text_forgotPassword = findViewById(R.id.textRedirectForgotPassword);

        button_signup.setOnClickListener(view -> {
            String emailAddress = text_emailAddress.getText().toString();

            Intent signupIntent = new Intent(this, SignupActivity.class);
            signupIntent.putExtra(User.EMAIL_ADDRESS, emailAddress);
            startActivity(signupIntent);
        });

        text_forgotPassword.setOnClickListener(view -> {
            String emailAddress = text_emailAddress.getText().toString();

            Intent forgotPasswordIntent = new Intent(this, ForgotPasswordActivity.class);
            forgotPasswordIntent.putExtra(User.EMAIL_ADDRESS, emailAddress);
            startActivity(forgotPasswordIntent);
        });

        button_login.setOnClickListener(view -> {
            String emailAddress = text_emailAddress.getText().toString();
            String password = text_password.getText().toString();

            mAuth.signInWithEmailAndPassword(emailAddress, password)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(authResult -> {
                    Intent mainIntent = new Intent(this, MainPageActivity.class);
                    startActivity(mainIntent);
                });
        });
    }
}