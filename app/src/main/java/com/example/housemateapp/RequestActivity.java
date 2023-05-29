package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Button button_denyMatchRequest;
    Button button_whatsapp;
    Button button_message;
    Button button_email;

    String targetRequestId, emailAddress, phoneNumber;

    private MatchingRequest matchingRequest;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        AuthUtils.redirectToLoginIfNotAuthenticated(this, mAuth);

        Bundle bundle = getIntent().getExtras();
        targetRequestId = bundle.getString(MatchingRequest.ID);
        emailAddress = bundle.getString(User.EMAIL_ADDRESS);
        phoneNumber = bundle.getString(User.PHONE_NUMBER);

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
        button_denyMatchRequest = findViewById(R.id.buttonRequestDeny);

        button_acceptMatchRequest.setOnClickListener(view -> {
            Drawable inactiveButton = AppCompatResources.getDrawable(this, R.drawable.custom_button_inactive);
            button_acceptMatchRequest.setBackground(inactiveButton);
            button_acceptMatchRequest.setEnabled(false);
            button_denyMatchRequest.setBackground(inactiveButton);
            button_denyMatchRequest.setEnabled(false);

            acceptRequest();
        });

        button_denyMatchRequest.setOnClickListener(view -> {
            Drawable inactiveButton = AppCompatResources.getDrawable(this, R.drawable.custom_button_inactive);
            button_acceptMatchRequest.setBackground(inactiveButton);
            button_acceptMatchRequest.setEnabled(false);
            button_denyMatchRequest.setBackground(inactiveButton);
            button_denyMatchRequest.setEnabled(false);

            denyRequest();
        });

        button_whatsapp = findViewById(R.id.buttonContactWhatsapp);
        button_message = findViewById(R.id.buttonContactMessage);
        button_email = findViewById(R.id.buttonContactEmail);

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

                        if (matchingRequest.isAccepted != 0) {
                            button_acceptMatchRequest.setVisibility(View.GONE);
                            button_denyMatchRequest.setVisibility(View.GONE);
                            activateContactButtons();
                        }

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

                        button_acceptMatchRequest.setEnabled(true);
                        button_denyMatchRequest.setEnabled(true);
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

    private void acceptRequest() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(MatchingRequest.IS_ACCEPTED, 1);

        db.collection(MatchingRequest.COLLECTION_NAME)
            .document(targetRequestId)
            .update(requestMap)
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(unused -> {
                button_acceptMatchRequest.setVisibility(View.GONE);
                button_denyMatchRequest.setVisibility(View.GONE);
                activateContactButtons();
            });

        Map<String, Object> userMap = new HashMap<>();
        userMap.put(User.STATUS_TYPE, "Aramıyor");

        db.collection(User.COLLECTION_NAME)
            .document(matchingRequest.fromUid)
            .update(userMap);

        db.collection(User.COLLECTION_NAME)
            .document(matchingRequest.toUid)
            .update(userMap);
    }

    private void denyRequest() {
        db.collection(MatchingRequest.COLLECTION_NAME)
            .document(targetRequestId)
            .delete()
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(unused -> {
                Intent mainPageIntent = new Intent(this, MainPageActivity.class);
                startActivity(mainPageIntent);
            });
    }

    private void activateContactButtons() {
        button_whatsapp.setVisibility(View.VISIBLE);
        button_message.setVisibility(View.VISIBLE);
        button_email.setVisibility(View.VISIBLE);

        button_whatsapp.setOnClickListener(view -> {
            Uri uri = Uri.parse("smsto:" + phoneNumber);
            Intent whatsappIntent = new Intent(Intent.ACTION_SENDTO, uri);
            whatsappIntent.setPackage("com.whatsapp");

            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(whatsappIntent, 0);

            if (resolveInfos.size() > 0) {
                startActivity(whatsappIntent);
            } else {
                Toast.makeText(this, R.string.message_whatsapp_not_installed, Toast.LENGTH_SHORT).show();
            }

            startActivity(whatsappIntent);
        });

        button_message.setOnClickListener(view -> {
            Intent messageIntent = new Intent(Intent.ACTION_SENDTO);
            messageIntent.setData(Uri.parse(String.format("sms:%s", phoneNumber)));
            messageIntent.putExtra("sms_body", "Merhaba! Anlaşma sağlayabilmek adına en kısa zamanda dönüş yapabilirseniz sevinirim. Saygılarımla!");
            startActivity(Intent.createChooser(messageIntent, "Mesajlaşma uygulamasını seçiniz"));
        });

        button_email.setOnClickListener(view -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sizinle Aynı Evi Paylaşmak İstiyorum!");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Merhaba! Anlaşma sağlayabilmek adına en kısa zamanda dönüş yapabilirseniz sevinirim. Saygılarımla!");
            startActivity(Intent.createChooser(emailIntent, "E-posta uygulamasını seçiniz"));
        });
    }
}