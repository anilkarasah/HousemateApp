package com.example.housemateapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    LinearLayout layout_filterSettings;
    FragmentContainerView fragment_filterSettings;

    RecyclerView view_users;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private final ArrayList<User> users = new ArrayList<>();

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

        layout_filterSettings = findViewById(R.id.layoutFilterSettings);
        fragment_filterSettings = findViewById(R.id.fragmentFilterSettings);
        UserFilterFragment filterFragment = new UserFilterFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .add(R.id.fragmentFilterSettings, filterFragment)
            .commit();

        fragment_filterSettings.setVisibility(View.INVISIBLE);

        layout_filterSettings.setOnClickListener(view -> {
            if (fragment_filterSettings.getVisibility() == View.VISIBLE) {
                fragment_filterSettings.setVisibility(View.INVISIBLE);
            } else {
                fragment_filterSettings.setVisibility(View.VISIBLE);
            }
        });

        view_users = findViewById(R.id.mainPageUsersView);
        UserAdapter userAdapter = new UserAdapter(users, this, user -> {
            Intent userPageIntent = new Intent(MainPageActivity.this, UserPageActivity.class);
            userPageIntent.putExtra(User.UID, user.uid);
            startActivity(userPageIntent);
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        view_users.setAdapter(userAdapter);
        view_users.setItemAnimator(new DefaultItemAnimator());
        view_users.setLayoutManager(linearLayoutManager);

        view_users.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());

                if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(childView);
                    User clickedUser = users.get(position);
                    Toast.makeText(MainPageActivity.this, "nabers", Toast.LENGTH_SHORT).show();

//                    Intent intent = new Intent(MainPageActivity.this, UserPageActivity.class);
//                    intent.putExtra(User.UID, clickedUser.uid);
//                    startActivity(intent);
                }
            }

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

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

                    double rangeInKilometers = 0;
                    Object rangeInKilometersObject = userMap.get(User.RANGE_IN_KILOMETERS);
                    if (rangeInKilometersObject != null) {
                        rangeInKilometers = Double.parseDouble(rangeInKilometersObject.toString());
                    }

                    int willStayForDays = 0;
                    Object willStayForDaysObject = userMap.get(User.WILL_STAY_FOR_DAYS);
                    if (willStayForDaysObject != null) {
                        willStayForDays = Integer.parseInt(willStayForDaysObject.toString());
                    }

                    String statusType = "Belirtilmemiş";
                    Object statusTypeObject = userMap.get(User.STATUS_TYPE);
                    if (statusTypeObject != null) {
                        statusType = statusTypeObject.toString();
                    }

                    User user = new User(fullName, emailAddress, phoneNumber, department, grade, rangeInKilometers, willStayForDays, statusType);
                    user.uid = uid;

                    storage.getReference()
                        .child(CameraUtils.getStorageChild(uid))
                        .getBytes(CameraUtils.TWO_MEGABYTES)
                        .addOnFailureListener(e -> Log.i("MainPageActivity/Storage", "Error retrieving profile picture of user with ID: " + uid))
                        .addOnSuccessListener(bytes -> {
                            user.profilePicture = CameraUtils.getBitmap(bytes);
                            users.add(user);
                            userAdapter.notifyItemChanged(users.indexOf(user));
                        });
                }
            });
    }
}