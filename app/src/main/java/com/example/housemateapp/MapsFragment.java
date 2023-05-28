package com.example.housemateapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.housemateapp.entities.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private Location currentLocation;
    private GoogleMap googleMap;

    private Activity activity;
    private ArrayList<User> users;

    private Intent locationServiceIntent;

    private LocationService locationService;

    private Marker userMarker;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    String uid = null;

    public MapsFragment() {}

    public MapsFragment(Activity activity, ArrayList<User> users) {
        this.activity = activity;
        this.users = users;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        locationServiceIntent = new Intent(activity, LocationService.class);
        activity.startService(locationServiceIntent);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
            .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        activity.bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        activity.unbindService(serviceConnection);

        if (uid == null) return;

        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put(User.LATITUDE, currentLocation.getLatitude());
        locationMap.put(User.LONGITUDE, currentLocation.getLongitude());

        db.collection(User.COLLECTION_NAME)
            .document(uid)
            .update(locationMap)
            .addOnFailureListener(e -> Log.e("MapsFragment", "Error while updating location data", e))
            .addOnSuccessListener(unused -> Log.i("MapsFragment", String.format(Locale.GERMAN,
                "Successfully updated. Lat: %f, Lon: %f",
                currentLocation.getLatitude(), currentLocation.getLongitude())));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        activity.stopService(locationServiceIntent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        updateUserMarkers();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;

        updateUserMarkers();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
            locationService = binder.getService();

            locationService.onLocationChangeListener = location -> {
                currentLocation = location;

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                userMarker.setPosition(latLng);
            };

            Location location = locationService.getCurrentLocation();
            if (location != null) currentLocation = location;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };

    private void updateUserMarkers() {
        if (uid == null) {
            Log.i("MapsFragment", "updateUserMarkers: uid is null");
            return;
        }

        googleMap.clear();

        for (User user : users) {
            LatLng latLng = new LatLng(user.latitude, user.longitude);

            MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(user.fullName);

            if (uid.equals(user.uid)) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                userMarker = googleMap.addMarker(markerOptions);
            } else {
                googleMap.addMarker(markerOptions);
            }
        }
    }
}