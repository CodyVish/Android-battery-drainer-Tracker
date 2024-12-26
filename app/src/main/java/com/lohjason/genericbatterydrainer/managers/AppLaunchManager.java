package com.lohjason.genericbatterydrainer.managers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class AppLaunchManager {

    private static AppLaunchManager instance;

    private AppLaunchManager() {
    }

    public static AppLaunchManager getInstance() {
        if (instance == null) {
            instance = new AppLaunchManager();
        }
        return instance;
    }

    public void openMultipleApps(Context context, String[] packageNames) {
        PackageManager packageManager = context.getPackageManager();
        for (String packageName : packageNames) {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                context.startActivity(launchIntent);
            } else {
                Toast.makeText(context, "App not installed: " + packageName, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
