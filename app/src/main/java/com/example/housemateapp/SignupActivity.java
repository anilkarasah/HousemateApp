package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {
    EditText text_fullName;
    EditText text_emailAddress;
    EditText text_password;
    EditText text_phoneNumber;
    EditText text_department;
    EditText text_grade;
    Button button_takePicture;
    Button button_uploadPicture;
    Button button_signup;
    TextView text_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        text_fullName = findViewById(R.id.textSignupFullName);
        text_emailAddress = findViewById(R.id.textSignupEmailAddress);
        text_password = findViewById(R.id.textSignupPassword);
        text_phoneNumber = findViewById(R.id.textSignupPhoneNumber);
        text_department = findViewById(R.id.textSignupDepartment);
        text_grade = findViewById(R.id.textSignupGrade);
        button_takePicture = findViewById(R.id.buttonSignupTakePicture);
        button_uploadPicture = findViewById(R.id.buttonSignupUploadPicture);
        button_signup = findViewById(R.id.buttonSignup);
        text_login = findViewById(R.id.textViewLoginButton);

        text_login.setOnClickListener(view -> {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        });

        button_takePicture.setOnClickListener(view -> Toast.makeText(this, "Fotoğraf çek", Toast.LENGTH_SHORT).show());

        button_uploadPicture.setOnClickListener(view -> Toast.makeText(this, "Fotoğraf yükle", Toast.LENGTH_SHORT).show());

        button_signup.setOnClickListener(view -> Toast.makeText(this, "Kayıt ol", Toast.LENGTH_SHORT).show());
    }
}