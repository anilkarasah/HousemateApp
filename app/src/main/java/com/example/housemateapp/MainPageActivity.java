package com.example.housemateapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.ArrayListUtils;
import com.example.housemateapp.utilities.AuthUtils;
import com.example.housemateapp.utilities.CameraUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
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

        viewPager = findViewById(R.id.usersListViewPager);
        tabLayout = findViewById(R.id.usersListTabLayout);

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
            showEmailVerificationMessage(firebaseUser);
        }

        usersListFragment = new UsersListFragment(users);
        mapsFragment = new MapsFragment(this, users);

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
                mapsFragment.setUsers(filteredUsers);
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
                    User user = User.parseDocumentSnapshot(documentSnapshot);

                    users.add(user);
                    usersListFragment.notifyUserAdapter(users.indexOf(user));

                    storage.getReference()
                        .child(CameraUtils.getStorageChild(user.uid))
                        .getBytes(CameraUtils.TWO_MEGABYTES)
                        .addOnFailureListener(e -> Log.i("MainPageActivity/Storage", "Error retrieving profile picture of user with ID: " + user.uid))
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
        }
    }

    private void showEmailVerificationMessage(FirebaseUser firebaseUser) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.message_verify_email_address, Snackbar.LENGTH_LONG);
        snackbar.setAction("Doğrula", view -> firebaseUser.sendEmailVerification());
        snackbar.setDuration(10 * 1000); // 10 seconds
        snackbar.show();
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