package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.utilities.AuthUtils;
import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    TextView text_profilePage;
    TextView text_mainPage;
    TextView text_mapPage;
    TextView text_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        AuthUtils.redirectToLoginIfNotAuthenticated(this, FirebaseAuth.getInstance());

        text_profilePage = findViewById(R.id.textMenuProfile);
        text_mainPage = findViewById(R.id.textMenuMainPage);
        text_mapPage = findViewById(R.id.textMenuMap);
        text_logout = findViewById(R.id.textMenuLogout);

        text_profilePage.setOnClickListener(view -> {
            Intent profilePageIntent = new Intent(this, ProfileActivity.class);
            startActivity(profilePageIntent);
        });

        text_mainPage.setOnClickListener(view -> {
            Intent mainPageIntent = new Intent(this, MainPageActivity.class);
            startActivity(mainPageIntent);
        });

        text_mapPage.setOnClickListener(view -> {
            Intent mapIntent = new Intent(this, MapsActivity.class);
            startActivity(mapIntent);
        });

        text_logout.setOnClickListener(view -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();

            Toast.makeText(this, R.string.message_signed_out, Toast.LENGTH_SHORT).show();

            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        });
    }
}