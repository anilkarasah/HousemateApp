package com.example.housemateapp;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.housemateapp.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocationService extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser == null) {
                return;
            }

            String uid = firebaseUser.getUid();

            Map<String, Object> locationMap = new HashMap<>();
            locationMap.put(User.LATITUDE, location.getLatitude());
            locationMap.put(User.LONGITUDE, location.getLongitude());

            db.collection(User.COLLECTION_NAME)
                .document(uid)
                .update(locationMap)
                .addOnFailureListener(e -> Log.e("LocationService", "Error while updation location data", e))
                .addOnSuccessListener(unused -> {
                    Log.i("LocationService", String.format(Locale.GERMAN, "Successfully updated. Lat: %f, Lon: %f", latitude, longitude));
                });
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long minTime = 1 * 1000; // a minute
        float minDistance = 10;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}