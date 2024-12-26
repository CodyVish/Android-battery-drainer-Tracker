package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.Intent;

import com.lohjason.genericbatterydrainer.services.VibrationService;
import com.lohjason.genericbatterydrainer.utils.Logg;

/**
 * VibrationManager
 * Created by user on 30/05/24.
 */
public class VibrationManager {

    private static final String LOG_TAG = "+_VibMgr";
    private static VibrationManager instance;
    private Intent vibrationServiceIntent;

    private VibrationManager() {
    }

    public static VibrationManager getInstance() {
        if (instance == null) {
            instance = new VibrationManager();
        }
        return instance;
    }

    public void startVibrationService(Application application){
        if(vibrationServiceIntent == null){
            vibrationServiceIntent = new Intent(application, VibrationService.class);
            application.startService(vibrationServiceIntent);
            Logg.d(LOG_TAG, "Vibration Service Started");
        }
    }

    public void stopVibrationService(Application application){
        if(vibrationServiceIntent != null){
            application.stopService(vibrationServiceIntent);
            vibrationServiceIntent = null;
            Logg.d(LOG_TAG, "Vibration Service Stopped");
        }
    }
}
