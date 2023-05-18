package com.example.housemateapp.entities;

import android.graphics.Bitmap;

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

    public User(String uid, String fullName, String emailAddress, String phoneNumber, String department, int grade, int rangeInKilometers, int willStayForDays) {
        this.uid = uid;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.grade = grade;
        this.rangeInKilometers = rangeInKilometers;
        this.willStayForDays = willStayForDays;
    }
}
