package com.example.housemateapp.entities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.housemateapp.LoginActivity;
import com.example.housemateapp.R;
import com.example.housemateapp.utilities.ValidationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

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
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

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
    public double latitude;
    public double longitude;

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

    public User(String fullName, String emailAddress, String phoneNumber, String department, int grade, double rangeInKilometers, int willStayForDays, String statusType, double latitude, double longitude) {
        this(fullName, emailAddress, phoneNumber, department, grade, rangeInKilometers, willStayForDays, statusType);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static User parseDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Map<String, Object> userMap = documentSnapshot.getData();

        assert userMap != null;
        String uid = userMap.get(User.UID).toString();
        String fullName = userMap.get(User.FULL_NAME).toString();
        String emailAddress = userMap.get(User.EMAIL_ADDRESS).toString();
        String phoneNumber = userMap.get(User.PHONE_NUMBER).toString();
        String department = userMap.get(User.DEPARTMENT).toString();
        int grade = Integer.parseInt(userMap.get(User.GRADE).toString());

        double rangeInKilometers = 0f;
        Object rangeInKilometersObject = userMap.get(User.RANGE_IN_KILOMETERS);
        if (rangeInKilometersObject != null) {
            rangeInKilometers = Double.parseDouble(rangeInKilometersObject.toString());
        }

        int willStayForDays = 0;
        Object willStayForDaysObject = userMap.get(User.WILL_STAY_FOR_DAYS);
        if (willStayForDaysObject != null) {
            willStayForDays = Integer.parseInt(willStayForDaysObject.toString());
        }

        String statusType = "Hepsi";
        Object statusTypeObject = userMap.get(User.STATUS_TYPE);
        if (statusTypeObject != null) {
            statusType = statusTypeObject.toString();
        }

        double latitude = 0f;
        Object latitudeObject = userMap.get(User.LATITUDE);
        if (latitudeObject != null) {
            latitude = Double.parseDouble(latitudeObject.toString());
        }

        double longitude = 0f;
        Object longitudeObject = userMap.get(User.LONGITUDE);
        if (longitudeObject != null) {
            longitude = Double.parseDouble(longitudeObject.toString());
        }

        User user = new User(fullName, emailAddress, phoneNumber, department, grade, rangeInKilometers, willStayForDays, statusType, latitude, longitude);
        user.uid = uid;

        return user;
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

    @NonNull
    @Override
    public String toString() {
        return "User{" +
            "uid='" + uid + '\'' +
            ", fullName='" + fullName + '\'' +
            ", emailAddress='" + emailAddress + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", department='" + department + '\'' +
            ", grade=" + grade +
            ", rangeInKilometers=" + rangeInKilometers +
            ", willStayForDays=" + willStayForDays +
            ", statusType='" + statusType + '\'' +
            ", profilePicture=" + profilePicture +
            '}';
    }
}
