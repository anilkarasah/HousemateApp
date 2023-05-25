package com.example.housemateapp.utilities;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.housemateapp.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthUtils {
    public static FirebaseUser getAuthenticatedUser(AppCompatActivity activity) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser != null) return firebaseUser;

        Intent loginIntent = new Intent(activity, LoginActivity.class);
        activity.startActivity(loginIntent);
        return null;
    }
}
