package com.lohjason.genericbatterydrainer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.lohjason.genericbatterydrainer.utils.Logg;

/**
 * VibrationService
 * Created by user on 30/05/24.
 */
public class VibrationService extends Service {

    private static final String LOG_TAG = "+_VibSvc";
    private Vibrator vibrator;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
               /*
                // Define a pattern for vibration (e.g., vibrate for 500ms, pause for 1000ms)
                long[] timings = {20, 20, 20, 20, 20};
                int[] amplitudes = {128, 255, 128, 255, 128};

                // Repeat the pattern from the beginning (0) or -1 for not repeating
                effect = VibrationEffect.createWaveform(timings, amplitudes, 1);
                vibrator.vibrate(effect);
                */
                 //rigourous pattern
            long[] timings = {50, 50, 100, 50, 50, 100, 50, 50};
            int[] amplitudes = {64, 128, 255, 128, 64, 255, 128, 64};
            effect = VibrationEffect.createWaveform(timings, amplitudes, 0);
            vibrator.vibrate(effect);

            }
            else{
                long[] timings = {50, 50, 100, 50, 50, 100, 50, 50};
                vibrator.vibrate(timings, -1);
            }






            Logg.d(LOG_TAG, "Vibration Started");
        } else {
            Logg.d(LOG_TAG, "Vibrator not available");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
            Logg.d(LOG_TAG, "Vibration Stopped");
        }
    }
}
