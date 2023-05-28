package com.example.housemateapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.housemateapp.utilities.ArrayListUtils;
import com.example.housemateapp.utilities.AuthUtils;
import com.example.housemateapp.utilities.CameraUtils;
import com.example.housemateapp.utilities.UserAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import com.example.housemateapp.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainPageActivity extends AppCompatActivity {
    ImageView image_menuButton;
    LinearLayout layout_filterSettings;
    ViewPager2 viewPager;
    TabLayout tabLayout;

    double filterSelectedRange = 0f;
    int filterSelectedDays = 0;
    String filterSelectedStatusType = "";
    String filterSortBy = "";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public final ArrayList<User> users = new ArrayList<>();

    private UsersListFragment usersListFragment;
    private MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.prompt_list_view);
                    break;
                case 1:
                    tab.setText(R.string.prompt_map_view);
                    break;
            }
        }).attach();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        AuthUtils.redirectToLoginIfNotAuthenticated(this, mAuth);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        assert firebaseUser != null;

        if (!firebaseUser.isEmailVerified()) {
            Toast.makeText(this, R.string.message_verify_email_address, Toast.LENGTH_SHORT).show();
            firebaseUser.sendEmailVerification();
        }

        usersListFragment = new UsersListFragment(users);
        mapsFragment = new MapsFragment();

        Intent locationServiceIntent = new Intent(this, LocationService.class);
        startService(locationServiceIntent);

        layout_filterSettings = findViewById(R.id.layoutFilterSettings);

        ActivityResultLauncher<Intent> getFilterSettingsActivityResultLauncher = this.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                Intent data = result.getData();
                filterSelectedRange = data.getDoubleExtra(User.RANGE_IN_KILOMETERS, 0f);
                filterSelectedDays = data.getIntExtra(User.WILL_STAY_FOR_DAYS, 0);
                filterSelectedStatusType = data.getStringExtra(User.STATUS_TYPE);
                filterSortBy = data.getStringExtra("sortType");

                ArrayList<User> filteredUsers = ArrayListUtils.filterUsers(users, filterSelectedRange, filterSelectedDays, filterSelectedStatusType);
                ArrayListUtils.sortUsersBy(filteredUsers, filterSortBy);

                usersListFragment.notifyUserAdapter(filteredUsers);
            });

        layout_filterSettings.setOnClickListener(view -> {
            Intent filterIntent = new Intent(MainPageActivity.this, UserFilterActivity.class);
            filterIntent.putExtra(User.RANGE_IN_KILOMETERS, filterSelectedRange);
            filterIntent.putExtra(User.WILL_STAY_FOR_DAYS, filterSelectedDays);
            filterIntent.putExtra(User.STATUS_TYPE, filterSelectedStatusType);
            filterIntent.putExtra("sortType", filterSortBy);

            if (getPackageManager().resolveActivity(filterIntent, 0) != null) {
                getFilterSettingsActivityResultLauncher.launch(filterIntent);
            } else {
                Toast.makeText(this, R.string.message_filter_not_active, Toast.LENGTH_SHORT).show();
            }
        });

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {

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
                    users.add(user);
                    usersListFragment.notifyUserAdapter(users.indexOf(user));

                    storage.getReference()
                        .child(CameraUtils.getStorageChild(uid))
                        .getBytes(CameraUtils.TWO_MEGABYTES)
                        .addOnFailureListener(e -> Log.i("MainPageActivity/Storage", "Error retrieving profile picture of user with ID: " + uid))
                        .addOnSuccessListener(bytes -> {
                            user.profilePicture = CameraUtils.getBitmap(bytes);
                            usersListFragment.notifyUserAdapter(users.indexOf(user));
                        });
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            filterSelectedRange = data.getDoubleExtra(User.RANGE_IN_KILOMETERS, 0f);
            filterSelectedDays = data.getIntExtra(User.WILL_STAY_FOR_DAYS, 0);
            filterSelectedStatusType = data.getStringExtra(User.STATUS_TYPE);
            filterSortBy = data.getStringExtra("sortType");

//            updateRecyclerView();
        }
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {
        public SectionsPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return usersListFragment;
                case 1:
                    return mapsFragment;
            }

            return new Fragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}