package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.utilities.CameraUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.example.housemateapp.entities.User;

import java.util.Map;

public class MainPageActivity extends AppCompatActivity {
    TextView text_uid;
    TextView text_fullName;
    TextView text_emailAddress;
    TextView text_department;
    ImageView image_profilePicture;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        text_uid = findViewById(R.id.textUid);
        text_fullName = findViewById(R.id.textFullName);
        text_emailAddress = findViewById(R.id.textEmailAddress);
        text_department = findViewById(R.id.textDepartment);
        image_profilePicture = findViewById(R.id.imageMainAvatar);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        assert firebaseUser != null;
        String uid = firebaseUser.getUid();
        String fullName = firebaseUser.getDisplayName();
        String emailAddress = firebaseUser.getEmail();

        text_uid.setText(uid);
        text_fullName.setText(fullName);
        text_emailAddress.setText(emailAddress);

        db.collection(User.COLLECTION_NAME)
            .document(uid)
            .get()
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userMap = documentSnapshot.getData();

                assert userMap != null;
                String department = userMap.get(User.DEPARTMENT).toString();

                text_department.setText(department);
            });

        storage.getReference()
            .child("profiles/" + uid + ".jpg")
            .getBytes(CameraUtils.TWO_MEGABYTES)
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(bytes -> {
                Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                image_profilePicture.setImageBitmap(avatar);
            });
    }
}