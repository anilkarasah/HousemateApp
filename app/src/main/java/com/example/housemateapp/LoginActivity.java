package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText text_emailAddress;
    EditText text_password;
    Button button_login;
    Button button_signup;
    TextView text_forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        text_emailAddress = findViewById(R.id.textLoginEmailAddress);
        text_password = findViewById(R.id.textLoginPassword);
        button_login = findViewById(R.id.buttonLogin);
        button_signup = findViewById(R.id.buttonRedirectSignup);
        text_forgotPassword = findViewById(R.id.textRedirectForgotPassword);

        button_signup.setOnClickListener(view -> {
            Intent signupIntent = new Intent(this, SignupActivity.class);
            startActivity(signupIntent);
        });

        text_forgotPassword.setOnClickListener(view -> Toast.makeText(this, "Şifremi unuttum", Toast.LENGTH_SHORT).show());

        button_login.setOnClickListener(view -> Toast.makeText(this, "Giriş yap", Toast.LENGTH_SHORT).show());
    }
}