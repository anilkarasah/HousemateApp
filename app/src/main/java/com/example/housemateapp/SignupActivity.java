package com.example.housemateapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.CameraUtils;
import com.example.housemateapp.utilities.ValidationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;

public class SignupActivity extends AppCompatActivity {
    EditText text_fullName;
    EditText text_emailAddress;
    EditText text_password;
    EditText text_phoneNumber;
    EditText text_department;
    EditText text_grade;
    ImageView image_profilePicture;
    Button button_takePicture;
    Button button_uploadPicture;
    Button button_signup;
    TextView text_login;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        text_fullName = findViewById(R.id.textSignupFullName);
        text_emailAddress = findViewById(R.id.textSignupEmailAddress);
        text_password = findViewById(R.id.textSignupPassword);
        text_phoneNumber = findViewById(R.id.textSignupPhoneNumber);
        text_department = findViewById(R.id.textSignupDepartment);
        text_grade = findViewById(R.id.textSignupGrade);
        image_profilePicture = findViewById(R.id.imageSignupAvatar);
        button_takePicture = findViewById(R.id.buttonSignupTakePicture);
        button_uploadPicture = findViewById(R.id.buttonSignupUploadPicture);
        button_signup = findViewById(R.id.buttonSignup);
        text_login = findViewById(R.id.textViewLoginButton);

        text_login.setOnClickListener(view -> {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        });

        ActivityResultLauncher<Intent> takePictureActivityResultLauncher = CameraUtils
            .configureTakePictureLauncher(this, croppedBitmap -> image_profilePicture.setImageBitmap(croppedBitmap));

        button_takePicture.setOnClickListener(view -> {
            CameraUtils.askCameraPermissions(this);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (getPackageManager().resolveActivity(cameraIntent, 0) != null) {
                takePictureActivityResultLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, R.string.no_app_supporting, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, R.string.no_app_supporting, Toast.LENGTH_SHORT).show();
            }
        });

        button_signup.setOnClickListener(view -> {
            String fullName = text_fullName.getText().toString();
            String emailAddress = text_emailAddress.getText().toString();
            String password = text_password.getText().toString();
            String phoneNumber = text_phoneNumber.getText().toString();
            String department = text_department.getText().toString();
            int grade = Integer.parseInt(text_grade.getText().toString());

            User user = new User(
                fullName, emailAddress, phoneNumber, department, grade, 0, 0);

            if (!validateUserData(user)) {
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();

                    assert firebaseUser != null;
                    firebaseUser.updateProfile(profileChangeRequest);

                    String uid = firebaseUser.getUid();

                    db.collection(User.COLLECTION_NAME)
                        .document(uid)
                        .set(user)
                        .addOnFailureListener(e -> Log.i("SignupActivity/Firestore", e.getMessage()));

                    byte[] data = getBitmapData(image_profilePicture);
                    storage.getReference()
                        .child("profiles/" + uid + ".jpg")
                        .putBytes(data)
                        .addOnFailureListener(e -> Log.i("SignupActivity/Storage", e.getMessage()));

                    firebaseUser.sendEmailVerification()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Doğrulama maili gönderildi!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, R.string.permission_camera, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CameraUtils.GET_FROM_GALLERY) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_gallery, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] getBitmapData(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private boolean validateUserData(User user) {
        try {
            User.validateEmail(user.emailAddress);

            return true;
        } catch (ValidationException validationException) {
            Toast.makeText(this, validationException.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}