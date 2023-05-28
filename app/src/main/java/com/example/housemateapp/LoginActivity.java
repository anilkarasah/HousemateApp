package com.example.housemateapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

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

        if (mAuth.getCurrentUser() != null) {
            Intent mainPageIntent = new Intent(this, MainPageActivity.class);
            startActivity(mainPageIntent);
        }

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
            Drawable inactiveButton = AppCompatResources.getDrawable(this, R.drawable.custom_button_inactive);
            button_login.setBackground(inactiveButton);
            button_login.setEnabled(false);

            String emailAddress = text_emailAddress.getText().toString();
            String password = text_password.getText().toString();

            mAuth.signInWithEmailAndPassword(emailAddress, password)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Drawable activeButton = AppCompatResources.getDrawable(this, R.drawable.custom_button);
                    button_login.setBackground(activeButton);
                    button_login.setEnabled(true);
                })
                .addOnSuccessListener(authResult -> {
                    Intent mainIntent = new Intent(this, MainPageActivity.class);
                    startActivity(mainIntent);
                });
        });
    }
}