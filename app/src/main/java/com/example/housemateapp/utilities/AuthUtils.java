package com.example.housemateapp.utilities;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.housemateapp.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthUtils {
    public static boolean redirectToLoginIfNotAuthenticated(AppCompatActivity activity, FirebaseAuth auth) {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Intent loginIntent = new Intent(activity, LoginActivity.class);
            activity.startActivity(loginIntent);
            return false;
        }

        return true;
    }
}
