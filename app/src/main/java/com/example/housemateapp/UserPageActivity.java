package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class UserPageActivity extends AppCompatActivity {
    ImageView image_menuButton;

    ImageView image_profilePicture;
    TextView text_fullName;
    TextView text_department;
    TextView text_grade;
    TextView text_rangeInKilometers;
    TextView text_willStayForDays;
    TextView text_statusType;

    TextView text_sendRequestPrompt;

    Button button_sendMatchRequest;

    private String fromUid;
    private String toUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        AuthUtils.redirectToLoginIfNotAuthenticated(this, mAuth);

        Bundle bundle = getIntent().getExtras();
        toUid = bundle.getString(User.UID);

        image_menuButton = findViewById(R.id.imageMenuButton);
        image_menuButton.setVisibility(View.VISIBLE);
        image_menuButton.setOnClickListener(view -> {
            Intent menuIntent = new Intent(this, MenuActivity.class);
            startActivity(menuIntent);
        });

        image_profilePicture = findViewById(R.id.imageUserPageProfilePicture);
        text_fullName = findViewById(R.id.textUserPageFullName);
        text_department = findViewById(R.id.textUserPageDepartment);
        text_grade = findViewById(R.id.textUserPageGrade);
        text_rangeInKilometers = findViewById(R.id.textUserPageRangeInKilometers);
        text_willStayForDays = findViewById(R.id.textUserPageWillStayForDays);
        text_statusType = findViewById(R.id.textUserPageStatusType);
        button_sendMatchRequest = findViewById(R.id.buttonUserPageSendMatchRequest);
        text_sendRequestPrompt = findViewById(R.id.textViewSendRequestPrompt);

        storage.getReference()
            .child(CameraUtils.getStorageChild(toUid))
            .getBytes(CameraUtils.TWO_MEGABYTES)
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(bytes -> {
                Bitmap bitmap = CameraUtils.getBitmap(bytes);
                image_profilePicture.setImageBitmap(bitmap);
            });

        db.collection(User.COLLECTION_NAME)
            .document(toUid)
            .get()
            .addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Intent mainPageIntent = new Intent(this, MainPageActivity.class);
                startActivity(mainPageIntent);
            })
            .addOnSuccessListener(documentSnapshot -> {
                User user = User.parseDocumentSnapshot(documentSnapshot);

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

                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                assert firebaseUser != null;
                fromUid = firebaseUser.getUid();
                db.collection(User.COLLECTION_NAME)
                    .document(fromUid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String statusType = snapshot.get(User.STATUS_TYPE).toString();

                        if (isEligibleForMatchingRequest(statusType, user.statusType) || isEligibleForMatchingRequest(user.statusType, statusType)) {
                            Drawable button = AppCompatResources.getDrawable(this, R.drawable.custom_button_send_email);
                            button_sendMatchRequest.setBackground(button);
                            button_sendMatchRequest.setEnabled(true);
                            text_sendRequestPrompt.setVisibility(View.INVISIBLE);
                        }
                    });
            });

        button_sendMatchRequest.setOnClickListener(view -> {
            MatchingRequest matchingRequest = new MatchingRequest(fromUid, toUid, false, mAuth.getCurrentUser().getUid());

            db.collection(MatchingRequest.COLLECTION_NAME)
                .add(matchingRequest)
                .addOnFailureListener(e -> Log.e("UserPageActivity", "onCreate: Could not create Matching Request object", e))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "İstek gönderildi.", Toast.LENGTH_SHORT).show();
                    Intent mainPageIntent = new Intent(this, MainPageActivity.class);
                    startActivity(mainPageIntent);
                });
        });
    }

    private boolean isEligibleForMatchingRequest(String firstUserStatusType, String secondUserStatusType) {
        return firstUserStatusType.equalsIgnoreCase("Kalacak Ev/Oda Arıyor") && secondUserStatusType.equalsIgnoreCase("Ev/Oda Arkadaşı Arıyor");
    }
}