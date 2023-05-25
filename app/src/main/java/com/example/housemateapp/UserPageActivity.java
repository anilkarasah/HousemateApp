package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.AuthUtils;
import com.example.housemateapp.utilities.CameraUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

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

    Button button_sendMatchRequest;

    private String emailAddress;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        AuthUtils.redirectToLoginIfNotAuthenticated(this, mAuth);

        Bundle bundle = getIntent().getExtras();
        String uid = bundle.getString(User.UID);

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

        storage.getReference()
            .child(CameraUtils.getStorageChild(uid))
            .getBytes(CameraUtils.TWO_MEGABYTES)
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(bytes -> {
                Bitmap bitmap = CameraUtils.getBitmap(bytes);
                image_profilePicture.setImageBitmap(bitmap);
            });

        db.collection(User.COLLECTION_NAME)
            .document(uid)
            .get()
            .addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Intent mainPageIntent = new Intent(this, MainPageActivity.class);
                startActivity(mainPageIntent);
            })
            .addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userMap = documentSnapshot.getData();

                assert userMap != null;
                text_fullName.setText(userMap.get(User.FULL_NAME).toString());
                text_department.setText(userMap.get(User.DEPARTMENT).toString());
                text_statusType.setText(userMap.get(User.STATUS_TYPE).toString());

                Resources resources = getResources();

                int grade = Integer.parseInt(userMap.get(User.GRADE).toString());
                String gradeString = resources.getString(R.string.display_grade, grade);
                text_grade.setText(gradeString);

                double rangeInKilometers = Double.parseDouble(userMap.get(User.RANGE_IN_KILOMETERS).toString());
                String rangeInKilometersString = resources.getString(R.string.display_range_in_kilometers, rangeInKilometers);
                text_rangeInKilometers.setText(rangeInKilometersString);

                int willStayForDays = Integer.parseInt(userMap.get(User.WILL_STAY_FOR_DAYS).toString());
                String willStayForDaysString = resources.getString(R.string.display_will_stay_for_days, willStayForDays);
                text_willStayForDays.setText(willStayForDaysString);

                emailAddress = userMap.get(User.EMAIL_ADDRESS).toString();
                phoneNumber = userMap.get(User.PHONE_NUMBER).toString();
            });

        // TODO ilk iş bunu yapmalıyım
        button_sendMatchRequest.setOnClickListener(view -> Toast.makeText(this, "BURADAN EŞLEŞME İSTEĞİ GÖNDERİLECEK!", Toast.LENGTH_SHORT).show());
    }
}