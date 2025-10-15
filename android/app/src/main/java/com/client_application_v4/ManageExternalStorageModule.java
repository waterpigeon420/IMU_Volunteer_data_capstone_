package com.client_application_v4;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

public class ManageExternalStorageModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public ManageExternalStorageModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "ManageExternalStorage";
    }

    @ReactMethod
    public void hasPermission(Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                boolean hasPermission = Environment.isExternalStorageManager();
                promise.resolve(hasPermission);
            } else {
                promise.resolve(true); // No special permission needed below Android 11
            }
        } catch (Exception e) {
            promise.reject("ERROR_HAS_PERMISSION", e);
        }
    }

    @ReactMethod
    public void requestPermission(Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    promise.reject("ERROR_ACTIVITY_NULL", "Activity is null. Cannot request permission.");
                    return;
                }

                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
                
                // ✅ Return a success message to JS
                promise.resolve("Permission request launched");
            } else {
                promise.resolve("No permission required for Android versions below 11");
            }
        } catch (Exception e) {
            promise.reject("ERROR_REQUEST_PERMISSION", e);
        }
    }
}
