package com.example.housemateapp.utilities;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class CameraUtils {
    public static final int GET_FROM_GALLERY = 3;
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int TWO_MEGABYTES = 2 * 1024 * 1024;

    public static void askCameraPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
    }

    public static void askGalleryPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    public static String getStorageChild(String uid) {
        return String.format("profiles/%s.jpg", uid);
    }

    public static byte[] getBitmapData(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap cropBitmapToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width == height) return bitmap;

        int crop;
        Bitmap croppedBitmap;

        if (width > height) {
            crop = (width - height) / 2;
            croppedBitmap = Bitmap.createBitmap(bitmap, crop, 0, height, height);
        } else {
            crop = (height - width) / 2;
            croppedBitmap = Bitmap.createBitmap(bitmap, 0, crop, width, width);
        }

        return croppedBitmap;
    }

    private static ActivityResultLauncher<Intent> getActivityResultLauncher(
        AppCompatActivity activity, SetIntentCallback callback) {
        return activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                callback.act(result.getData());
            }
        );
    }

    public static ActivityResultLauncher<Intent> configureTakePictureLauncher(
        AppCompatActivity activity, SetBitmapCallback callback) {
        return getActivityResultLauncher(activity, data -> {
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            Bitmap croppedBitmap = CameraUtils.cropBitmapToSquare(bitmap);
            callback.act(croppedBitmap);
        });
    }

    public static ActivityResultLauncher<Intent> configureUploadPictureLauncher(
        AppCompatActivity activity, SetBitmapCallback callback) {
        return getActivityResultLauncher(activity, data -> {
            Uri imageUri = data.getData();
            ContentResolver contentResolver = activity.getContentResolver();
            InputStream inputStream;
            try {
                inputStream = contentResolver.openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap croppedBitmap = CameraUtils.cropBitmapToSquare(bitmap);
            callback.act(croppedBitmap);
        });
    }

    private interface SetIntentCallback {
        void act(Intent data);
    }

    public interface SetBitmapCallback {
        void act(Bitmap bitmap);
    }
}
