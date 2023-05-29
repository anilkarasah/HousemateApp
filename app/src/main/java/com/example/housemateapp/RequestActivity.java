package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.entities.MatchingRequest;
import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.AuthUtils;
import com.example.housemateapp.utilities.CameraUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class RequestActivity extends AppCompatActivity {
    ImageView image_menuButton;

    ImageView image_profilePicture;
    TextView text_fullName;
    TextView text_department;
    TextView text_grade;
    TextView text_rangeInKilometers;
    TextView text_willStayForDays;
    TextView text_statusType;

    Button button_acceptMatchRequest;
    Button button_whatsapp;
    Button button_message;
    Button button_email;

    String targetRequestId;

    private MatchingRequest matchingRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        AuthUtils.redirectToLoginIfNotAuthenticated(this, mAuth);

        Bundle bundle = getIntent().getExtras();
        targetRequestId = bundle.getString(MatchingRequest.ID);
        Log.i("RequestActivity", "onCreate: " + targetRequestId);

        image_menuButton = findViewById(R.id.imageMenuButton);
        image_menuButton.setVisibility(View.VISIBLE);
        image_menuButton.setOnClickListener(view -> {
            Intent menuIntent = new Intent(this, MenuActivity.class);
            startActivity(menuIntent);
        });

        image_profilePicture = findViewById(R.id.imageRequestProfilePicture);
        text_fullName = findViewById(R.id.textRequestFullName);
        text_department = findViewById(R.id.textRequestDepartment);
        text_grade = findViewById(R.id.textRequestGrade);
        text_rangeInKilometers = findViewById(R.id.textRequestRangeInKilometers);
        text_willStayForDays = findViewById(R.id.textRequestWillStayForDays);
        text_statusType = findViewById(R.id.textRequestStatusType);
        button_acceptMatchRequest = findViewById(R.id.buttonRequestAccept);
        button_whatsapp = findViewById(R.id.buttonRequestWhatsapp);
        button_message = findViewById(R.id.buttonRequestMessage);
        button_email = findViewById(R.id.buttonRequestEmail);

        db.collection(MatchingRequest.COLLECTION_NAME)
            .document(targetRequestId)
            .get()
            .addOnFailureListener(e -> Log.e("RequestActivity", "MatchingRequestId: " + targetRequestId, e))
            .addOnSuccessListener(matchingRequestDocumentSnapshot -> {
                matchingRequest = MatchingRequest.parseDocumentSnapshot(matchingRequestDocumentSnapshot, mAuth.getCurrentUser().getUid());

                String userId = matchingRequest.fromCurrentUser ?
                    matchingRequest.toUid : matchingRequest.fromUid;
                db.collection(User.COLLECTION_NAME)
                    .document(userId)
                    .get()
                    .addOnFailureListener(e -> Log.e("RequestActivity", "UserId: " + userId, e))
                    .addOnSuccessListener(fromUserDocumentSnapshot -> {
                        User user = User.parseDocumentSnapshot(fromUserDocumentSnapshot);

                        text_fullName.setText(user.fullName);
                        text_department.setText(user.department);

                        text_statusType.setText(user.statusType);

                        Resources resources = getResources();

                        String gradeString = resources.getString(R.string.display_grade, user.grade);
                        text_grade.setText(gradeString);

                        String rangeInKilometersString = resources.getString(R.string.display_range_in_kilometers, user.rangeInKilometers);
                        text_rangeInKilometers.setText(rangeInKilometersString);

                        String willStayForDaysString = resources.getString(R.string.display_will_stay_for_days, user.willStayForDays);
                        text_willStayForDays.setText(willStayForDaysString);
                    });
                
                storage.getReference()
                    .child(CameraUtils.getStorageChild(userId))
                    .getBytes(CameraUtils.TWO_MEGABYTES)
                    .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = CameraUtils.getBitmap(bytes);
                        image_profilePicture.setImageBitmap(bitmap);
                    });
            });
    }
}