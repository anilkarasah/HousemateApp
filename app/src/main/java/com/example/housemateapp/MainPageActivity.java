package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.utilities.CameraUtils;
import com.example.housemateapp.utilities.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import com.example.housemateapp.entities.User;

import java.util.ArrayList;
import java.util.Map;

public class MainPageActivity extends AppCompatActivity {
    ImageView image_menuButton;

    RecyclerView view_users;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, R.string.message_login_again, Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            return;
        }

        if (!firebaseUser.isEmailVerified()) {
            Toast.makeText(this, R.string.message_verify_email_address, Toast.LENGTH_SHORT).show();
            firebaseUser.sendEmailVerification();
        }

        view_users = findViewById(R.id.mainPageUsersView);
        UserAdapter userAdapter = new UserAdapter(users, this, user -> Toast.makeText(this, user.fullName, Toast.LENGTH_SHORT).show());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        view_users.setAdapter(userAdapter);
        view_users.setItemAnimator(new DefaultItemAnimator());
        view_users.setLayoutManager(linearLayoutManager);

        image_menuButton = findViewById(R.id.imageMenuButton);
        image_menuButton.setVisibility(View.VISIBLE);
        image_menuButton.setOnClickListener(view -> {
            Intent menuIntent = new Intent(this, MenuActivity.class);
            startActivity(menuIntent);
        });

        db.collection(User.COLLECTION_NAME)
            .get()
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Map<String, Object> userMap = documentSnapshot.getData();

                    String uid = userMap.get(User.UID).toString();
                    String fullName = userMap.get(User.FULL_NAME).toString();
                    String emailAddress = userMap.get(User.EMAIL_ADDRESS).toString();
                    String phoneNumber = userMap.get(User.PHONE_NUMBER).toString();
                    String department = userMap.get(User.DEPARTMENT).toString();
                    int grade = Integer.parseInt(userMap.get(User.GRADE).toString());

                    User user = new User(fullName, emailAddress, phoneNumber, department, grade, 0, 0, null);
                    user.uid = uid;

                    storage.getReference()
                        .child("profiles/" + uid + ".jpg")
                        .getBytes(CameraUtils.TWO_MEGABYTES)
                        .addOnFailureListener(e -> Log.i("MainPageActivity/Storage", "Error retrieving profile picture of user with ID: " + uid))
                        .addOnSuccessListener(bytes -> {
                            user.profilePicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            userAdapter.notifyItemChanged(users.indexOf(user));
                        });

                    users.add(user);
                    userAdapter.notifyItemChanged(users.indexOf(user));
                }
            });
    }
}