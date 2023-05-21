package com.example.housemateapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.CameraUtils;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ImageView image_menuButton;

    // image section
    ImageView image_profilePicture;
    Button button_takePicture;
    Button button_uploadPicture;
    Button button_updatePicture;

    // information section
    EditText text_fullName;
    EditText text_phoneNumber;
    EditText text_department;
    EditText text_grade;
    EditText text_rangeInKilometers;
    EditText text_willStayForDays;
    private int selectedDegreeIndex = 0;
    Spinner spinner_statusType;
    Button button_updateInformation;

    // email address section
    EditText text_emailAddress;
    Button button_updateEmailAddress;

    // password section
    EditText text_currentPassword;
    EditText text_newPassword;
    Button button_updatePassword;

    // log out section
    Button button_logout;

    // Firebase utilities
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        User.assertAuthentication(this);
        firebaseUser = mAuth.getCurrentUser();

        image_menuButton = findViewById(R.id.imageMenuButton);
        image_profilePicture = findViewById(R.id.imageProfilePicture);
        button_takePicture = findViewById(R.id.buttonProfileTakePicture);
        button_uploadPicture = findViewById(R.id.buttonProfileUploadPicture);
        button_updatePicture = findViewById(R.id.buttonUpdatePicture);
        text_fullName = findViewById(R.id.textProfileFullName);
        text_phoneNumber = findViewById(R.id.textProfilePhoneNumber);
        text_department = findViewById(R.id.textProfileDepartment);
        text_grade = findViewById(R.id.textProfileGrade);
        text_rangeInKilometers = findViewById(R.id.textProfileRangeInKilometers);
        text_willStayForDays = findViewById(R.id.textProfileWillStayForDays);
        spinner_statusType = findViewById(R.id.spinnerStatusType);
        button_updateInformation = findViewById(R.id.buttonUpdateProfile);
        text_emailAddress = findViewById(R.id.textProfileEmailAddress);
        button_updateEmailAddress = findViewById(R.id.buttonUpdateEmailAddress);
        text_currentPassword = findViewById(R.id.textProfileCurrentPassword);
        text_newPassword = findViewById(R.id.textProfileNewPassword);
        button_updatePassword = findViewById(R.id.buttonUpdatePassword);
        button_logout = findViewById(R.id.buttonLogout);

        image_menuButton.setVisibility(View.VISIBLE);
        image_menuButton.setOnClickListener(view -> {
            Intent menuIntent = new Intent(this, MenuActivity.class);
            startActivity(menuIntent);
        });

        // LOG OUT SECTION
        button_logout.setOnClickListener(view -> {
            disableButton(button_logout);

            mAuth.signOut();

            Toast.makeText(this, R.string.message_signed_out, Toast.LENGTH_SHORT).show();

            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        });

        // POPULATE USER DATA
        db.collection(User.COLLECTION_NAME)
            .document(firebaseUser.getUid())
            .get()
            .addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
            })
            .addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> userMap = documentSnapshot.getData();

                assert userMap != null;
                text_emailAddress.setText(userMap.get(User.EMAIL_ADDRESS).toString());
                text_fullName.setText(userMap.get(User.FULL_NAME).toString());
                text_phoneNumber.setText(userMap.get(User.PHONE_NUMBER).toString());
                text_department.setText(userMap.get(User.DEPARTMENT).toString());
                text_grade.setText(userMap.get(User.GRADE).toString());

                Object rangeInKilometers = userMap.get(User.RANGE_IN_KILOMETERS);
                if (rangeInKilometers != null && !TextUtils.isEmpty(rangeInKilometers.toString())) {
                    text_rangeInKilometers.setText(rangeInKilometers.toString());
                }

                Object willStayForDays = userMap.get(User.WILL_STAY_FOR_DAYS);
                if (willStayForDays != null && !TextUtils.isEmpty(willStayForDays.toString())) {
                    text_willStayForDays.setText(willStayForDays.toString());
                }

                Object statusType = userMap.get(User.STATUS_TYPE);
                if (statusType != null) {
                    int i = 0;
                    String[] statusTypes = getResources().getStringArray(R.array.status_types);
                    while (i < statusTypes.length && !statusType.toString().equalsIgnoreCase(statusTypes[i]))
                        i++;

                    if (i < statusTypes.length) {
                        spinner_statusType.setSelection(i);
                    } else {
                        spinner_statusType.setSelection(0);
                    }
                }
            });

        storage.getReference()
            .child(CameraUtils.getStorageChild(firebaseUser.getUid()))
            .getBytes(CameraUtils.TWO_MEGABYTES)
            .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
            .addOnSuccessListener(bytes -> {
                Bitmap profilePictureBitmap = CameraUtils.getBitmap(bytes);
                image_profilePicture.setImageBitmap(profilePictureBitmap);
            });

        // SET SPINNER VALUES FOR STATUS TYPE
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
            .createFromResource(this, R.array.status_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_statusType.setAdapter(adapter);
        spinner_statusType.setOnItemSelectedListener(this);

        // PROFILE PICTURE SECTION
        ActivityResultLauncher<Intent> takePictureActivityResultLauncher = CameraUtils
            .configureTakePictureLauncher(this, croppedBitmap -> image_profilePicture.setImageBitmap(croppedBitmap));

        button_takePicture.setOnClickListener(view -> {
            CameraUtils.askCameraPermissions(this);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (getPackageManager().resolveActivity(cameraIntent, 0) != null) {
                takePictureActivityResultLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, R.string.message_no_app_supporting, Toast.LENGTH_SHORT).show();
            }
        });

        ActivityResultLauncher<Intent> uploadPictureActivityResultLauncher = CameraUtils
            .configureUploadPictureLauncher(this, croppedBitmap -> image_profilePicture.setImageBitmap(croppedBitmap));

        button_uploadPicture.setOnClickListener(view -> {
            CameraUtils.askGalleryPermissions(this);

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            if (getPackageManager().resolveActivity(intent, 0) != null) {
                uploadPictureActivityResultLauncher.launch(intent);
            } else {
                Toast.makeText(this, R.string.message_no_app_supporting, Toast.LENGTH_SHORT).show();
            }
        });

        button_updatePicture.setOnClickListener(view -> {
            disableButton(button_updatePicture);

            byte[] pictureBytes = CameraUtils.getBitmapData(image_profilePicture);

            storage.getReference()
                .child(CameraUtils.getStorageChild(firebaseUser.getUid()))
                .putBytes(pictureBytes)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(this, R.string.message_profile_picture_updated, Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> enableButton(button_updatePicture));
        });

        // PROFILE INFORMATION SECTION
        button_updateInformation.setOnClickListener(view -> {
            disableButton(button_updateInformation);

            String uid = firebaseUser.getUid();

            String[] statusTypes = getResources().getStringArray(R.array.status_types);
            String selectedStatusType = statusTypes[selectedDegreeIndex];

            Map<String, Object> userMap = new HashMap<>();

            String fullName = text_fullName.getText().toString().trim();

            userMap.put(User.FULL_NAME, fullName);
            userMap.put(User.PHONE_NUMBER, text_phoneNumber.getText().toString().trim());
            userMap.put(User.DEPARTMENT, text_department.getText().toString().trim());
            userMap.put(User.GRADE, Integer.parseInt(text_grade.getText().toString().trim()));
            userMap.put(User.RANGE_IN_KILOMETERS, Double.parseDouble(text_rangeInKilometers.getText().toString().trim()));
            userMap.put(User.WILL_STAY_FOR_DAYS, Integer.parseInt(text_willStayForDays.getText().toString().trim()));
            userMap.put(User.STATUS_TYPE, selectedStatusType);

            db.collection(User.COLLECTION_NAME)
                .document(uid)
                .update(userMap)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.message_profile_information_updated, Toast.LENGTH_SHORT).show();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();

                    firebaseUser.updateProfile(profileChangeRequest);
                })
                .addOnCompleteListener(task -> enableButton(button_updateInformation));
        });

        // EMAIL ADDRESS SECTION
        button_updateEmailAddress.setOnClickListener(view -> {
            disableButton(button_updateEmailAddress);

            String newEmailAddress = text_emailAddress.getText().toString().trim();

            firebaseUser.verifyBeforeUpdateEmail(newEmailAddress)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> db.collection(User.COLLECTION_NAME)
                    .document(firebaseUser.getUid())
                    .update(User.EMAIL_ADDRESS, newEmailAddress)
                    .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(unused1 -> {
                        Toast.makeText(this, R.string.message_email_address_updated, Toast.LENGTH_SHORT).show();
                        Intent loginIntent = new Intent(this, LoginActivity.class);
                        startActivity(loginIntent);
                    }));
        });

        button_updatePassword.setOnClickListener(view -> {
            if (firebaseUser == null) {
                Toast.makeText(this, R.string.message_login_again, Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }

            disableButton(button_updatePassword);

            String emailAddress = firebaseUser.getEmail();
            String currentPassword = text_currentPassword.getText().toString();

            assert emailAddress != null;
            AuthCredential credential = EmailAuthProvider.getCredential(emailAddress, currentPassword);

            firebaseUser.reauthenticate(credential)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> {
                    String newPassword = text_newPassword.getText().toString();
                    firebaseUser.updatePassword(newPassword)
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                        .addOnSuccessListener(unused1 -> {
                            Toast.makeText(this, R.string.message_password_updated, Toast.LENGTH_SHORT).show();
                            Intent loginIntent = new Intent(this, LoginActivity.class);
                            startActivity(loginIntent);
                        });
                });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CameraUtils.CAMERA_PERM_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.message_permission_camera_required, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CameraUtils.GET_FROM_GALLERY) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.message_permission_gallery_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedDegreeIndex = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        selectedDegreeIndex = 0;
    }

    private void disableButton(Button button) {
        Drawable inactiveButtonDrawable = AppCompatResources.getDrawable(this, R.drawable.custom_button_inactive);
        button.setBackground(inactiveButtonDrawable);
        button.setEnabled(false);
    }

    private void enableButton(Button button) {
        Drawable activeButtonDrawable = AppCompatResources.getDrawable(this, R.drawable.custom_button);
        button.setBackground(activeButtonDrawable);
        button.setEnabled(true);
    }
}