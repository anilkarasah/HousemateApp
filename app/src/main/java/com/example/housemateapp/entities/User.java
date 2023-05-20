package com.example.housemateapp.entities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.housemateapp.LoginActivity;
import com.example.housemateapp.utilities.ValidationException;
import com.google.firebase.auth.FirebaseAuth;

public class User {
    public static final String COLLECTION_NAME = "users";
    public static final String UID = "uid";
    public static final String FULL_NAME = "fullName";
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String DEPARTMENT = "department";
    public static final String GRADE = "grade";
    public static final String RANGE_IN_KILOMETERS = "rangeInKilometers";
    public static final String WILL_STAY_FOR_DAYS = "willStayForDays";
    public static final String STATUS_TYPE = "statusType";

    @Nullable
    public String uid;
    public String fullName;
    public String emailAddress;
    public String phoneNumber;
    public String department;
    public int grade;
    public double rangeInKilometers;
    public int willStayForDays;
    public String statusType;

    @Nullable
    public Bitmap profilePicture;

    public User(String fullName, String emailAddress, String phoneNumber, String department, int grade, double rangeInKilometers, int willStayForDays, String statusType) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.grade = grade;
        this.rangeInKilometers = rangeInKilometers;
        this.willStayForDays = willStayForDays;

        this.statusType = TextUtils.isEmpty(statusType) ? "Aramıyor" : statusType;
    }

    public static void validateEmail(String emailAddress) throws ValidationException {
        // email address cannot be null or empty
        if (TextUtils.isEmpty(emailAddress)) {
            throw new ValidationException("Email adresi boş olamaz.");
        }

        // email address must consist only 2 parts
        String[] domains = emailAddress.split("@");

        if (domains.length != 2 || !domains[1].equalsIgnoreCase("std.yildiz.edu.tr")) {
            throw new ValidationException("Email adresi \"example@std.yildiz.edu.tr\" formatında olmalıdır.");
        }
    }

    public static void assertAuthentication(AppCompatActivity activity) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(activity, "Lütfen tekrar giriş yapınız.", Toast.LENGTH_SHORT).show();

            Intent loginIntent = new Intent(activity, LoginActivity.class);
            activity.startActivity(loginIntent);
        }
    }
}
