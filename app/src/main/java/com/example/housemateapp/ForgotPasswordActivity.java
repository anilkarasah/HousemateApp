package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.housemateapp.entities.User;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText text_emailAddress;
    Button button_sendConfirmation;
    Button button_goBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        text_emailAddress = findViewById(R.id.textForgotPasswordEmailAddress);
        button_sendConfirmation = findViewById(R.id.buttonSendConfirmationEmail);
        button_goBack = findViewById(R.id.buttonGoBack);

        Bundle bundle = getIntent().getExtras();
        String emailAddressFromLoginIntent = bundle.getString(User.EMAIL_ADDRESS, "");
        text_emailAddress.setText(emailAddressFromLoginIntent);

        button_goBack.setOnClickListener(view -> {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        });

        button_sendConfirmation.setOnClickListener(view -> {
            Drawable inactiveButton = AppCompatResources.getDrawable(this, R.drawable.custom_button_inactive);
            button_sendConfirmation.setBackground(inactiveButton);
            button_sendConfirmation.setEnabled(false);

            String emailAddress = text_emailAddress.getText().toString();

            mAuth.sendPasswordResetEmail(emailAddress)
                .addOnFailureListener(e -> {
                    Drawable activeButton = AppCompatResources.getDrawable(this, R.drawable.custom_button);
                    button_sendConfirmation.setBackground(activeButton);
                    button_sendConfirmation.setEnabled(true);

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.prompt_send_confirmation_email, Toast.LENGTH_SHORT).show();

                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                });
        });
    }
}