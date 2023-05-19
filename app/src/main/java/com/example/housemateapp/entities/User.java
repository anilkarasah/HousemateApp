package com.example.housemateapp.entities;

import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.Nullable;

public class User {
    public String uid;
    public String fullName;
    public String emailAddress;
    public String phoneNumber;
    public String department;
    public int grade;
    public int rangeInKilometers;
    public int willStayForDays;

    @Nullable
    public Bitmap profilePicture;

    private User(String uid, String fullName, String emailAddress, String phoneNumber, String department, int grade, int rangeInKilometers, int willStayForDays) {
        this.uid = uid;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.grade = grade;
        this.rangeInKilometers = rangeInKilometers;
        this.willStayForDays = willStayForDays;
    }

    public static User Create(String uid, String fullName, String emailAddress, String phoneNumber, String department, int grade, int rangeInKilometers, int willStayForDays) {
        if (!validateEmail(emailAddress)) return null;

        return new User(uid, fullName, emailAddress, phoneNumber, department, grade, rangeInKilometers, willStayForDays);
    }

    public static boolean validateEmail(String emailAddress) {
        // email address cannot be null or empty
        if (TextUtils.isEmpty(emailAddress)) {
            return false;
        }

        // email address must consist only 2 parts
        String[] domains = emailAddress.split("@");

        if (domains.length != 2) {
            return false;
        }

        // email address' host must be equal to:
        return domains[1].equalsIgnoreCase("std.yildiz.edu.tr");
    }
}
