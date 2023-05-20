package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {
    TextView text_profilePage;
    TextView text_mainPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        text_profilePage = findViewById(R.id.textMenuProfile);
        text_mainPage = findViewById(R.id.textMenuMainPage);

        text_profilePage.setOnClickListener(view -> {
            Intent profilePageIntent = new Intent(this, ProfileActivity.class);
            startActivity(profilePageIntent);
        });

        text_mainPage.setOnClickListener(view -> {
            Intent mainPageIntent = new Intent(this, MainPageActivity.class);
            startActivity(mainPageIntent);
        });
    }
}