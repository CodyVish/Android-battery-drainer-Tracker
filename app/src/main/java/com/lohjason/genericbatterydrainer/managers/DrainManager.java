package com.lohjason.genericbatterydrainer.managers;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;
import com.lohjason.genericbatterydrainer.ui.MainActivity;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.subjects.BehaviorSubject;

/**
 * DrainManager
 * Created by jason on 2/7/18.
 */
public class DrainManager {

    private static final String LOG_TAG = "+_DrnMgr";
    public static final String ACTION_START_VIDEO_RECORDING = "com.lohjason.genericbatterydrainer.START_VIDEO_RECORDING";

    private static DrainManager instance;
    private Application application;
    private FlashManager flashManager;
    private ScreenManager screenManager;
    private CpuManager cpuManager;
    private RenderscriptManager renderscriptManager;
    private WifiScanManager wifiScanManager;
    private FineLocationManager fineLocationManager;
    private BluetoothScanManager bluetoothScanManager;
    private HighResVideoManager HighResVideoManager;
    private AppLaunchManager AppLaunchManager;
    private HighResVideoRecordManager HighResVideoRecordManager;
    private VibrationManager vibrationManager;
    private AtomicBoolean isDraining;
    private BehaviorSubject<Boolean> drainBehaviorSubject;


    private DrainManager() {
    }

    public static DrainManager getInstance(Application application) {
        if (instance == null) {
            instance = new DrainManager();
            instance.flashManager = FlashManager.getInstance();
            instance.screenManager = ScreenManager.getInstance();
            instance.cpuManager = CpuManager.getInstance();
            instance.renderscriptManager = RenderscriptManager.getInstance();
            instance.fineLocationManager = FineLocationManager.getInstance();
            instance.wifiScanManager = WifiScanManager.getInstance();
            instance.bluetoothScanManager = BluetoothScanManager.getInstance();
            instance.HighResVideoManager= com.lohjason.genericbatterydrainer.managers.HighResVideoManager.getInstance();
            instance.AppLaunchManager = com.lohjason.genericbatterydrainer.managers.AppLaunchManager.getInstance();
            instance.HighResVideoRecordManager = com.lohjason.genericbatterydrainer.managers.HighResVideoRecordManager.getInstance();
            instance.vibrationManager = VibrationManager.getInstance();
            instance.isDraining = new AtomicBoolean(false);
            instance.drainBehaviorSubject = BehaviorSubject.create();
            instance.application = application;
        }
        return instance;
    }

    public BehaviorSubject<Boolean> getDrainBehaviorSubject() {
        return drainBehaviorSubject;
    }

    private void setIsDrainingOn(boolean setOn) {
        isDraining.set(setOn);
        drainBehaviorSubject.onNext(setOn);
    }

    public boolean isDraining() {
        return isDraining.get();
    }

    public void startDraining(boolean useFlash,
                              boolean useScreen,
                              boolean useCpu,
                              boolean useGpu,
                              boolean useLocation,
                              boolean useWifi,
                              boolean useBluetooth,
                              boolean videoOn,
                              boolean openOn,
                              boolean recordOn,
                              boolean vibrationOn) {
        if (isDraining.get()) {
            Toast.makeText(application, "Already Draining Battery", Toast.LENGTH_SHORT).show();
            return;
        }
        setIsDrainingOn(true);
        if (useFlash) {
            flashManager.startFlashService(application);
        }
        if (useScreen) {
            screenManager.setBrightnessToMax(application, true);
            screenManager.setScreenTimeoutToMax(application, true);
        }
        if (useCpu) {
            cpuManager.setComputationOn(true);
        }
        if (useGpu) {
//            renderscriptManager.setRenderingOn(application, true);
            renderscriptManager.setMatMulOn(application, true);
        }
        if (useCpu || useGpu) {
            cpuManager.setWakelockOn(application, true);
        }
        if (useLocation) {
            fineLocationManager.setRequestingGpsOn(application, true);
        }
        if (useWifi) {
            wifiScanManager.setWifiScanOn(application, true);
        }
        if (useBluetooth) {
            bluetoothScanManager.setBluetoothScan(application, true);
        }
        if (videoOn) {
            String videoUrl = "https://www.youtube.com/watch?v=zQd7THqbcoE"; // Example 4K video
            HighResVideoManager.playHighResVideo(application, videoUrl);
        }

        if (openOn) {
            String[] packageNames = {
                    "com.google.android.youtube", // YouTube
                    "com.google.android.gm", // Gmail
                    "com.android.chrome", // Chrome
                    "com.antutu.ABenchMark", //antutu
                    "com.primatelabs.geekbench5", //Geekbench
                    "com.futuremark.dmandroid.application" //3dMark
                    // Add more package names as needed
            };
            AppLaunchManager.getInstance().openMultipleApps(application, packageNames);
        }

        if (recordOn) {

            Intent recordIntent = new Intent(MainActivity.ACTION_START_VIDEO_RECORDING);
            application.sendBroadcast(recordIntent);
        }
        if (vibrationOn) {
            vibrationManager.startVibrationService(application);
        }
    }


    public void stopDraining(boolean useFlash,
                             boolean useScreen,
                             boolean useCpu,
                             boolean useGpu,
                             boolean useLocation,
                             boolean useWifi,
                             boolean useBluetooth, boolean videoOn, boolean openOn, boolean recordOn, boolean vibrationOn) {
        if (!isDraining.get()) {
            Toast.makeText(application, "Not Draining Battery", Toast.LENGTH_SHORT).show();
            return;
        }
        if (useFlash) {
            flashManager.stopFlashService(application);
        }
        if (useScreen) {
            screenManager.setBrightnessToMax(application, false);
            screenManager.setScreenTimeoutToMax(application, false);
        }
        if (useCpu) {
            cpuManager.setComputationOn(false);
        }
        if (useGpu) {
//            renderscriptManager.setRenderingOn(application, false);
            renderscriptManager.setMatMulOn(application, false);
        }
        if (useCpu || useGpu) {
            cpuManager.setWakelockOn(application, false);
        }
        if (useLocation) {
            fineLocationManager.setRequestingGpsOn(application, false);
        }
        if (useWifi) {
            wifiScanManager.setWifiScanOn(application, false);
        }
        if (useBluetooth) {
            bluetoothScanManager.setBluetoothScan(application, false);
        }
        if (vibrationOn){
            vibrationManager.stopVibrationService(application);
        }
        setIsDrainingOn(false);
        Runtime.getRuntime().gc();
    }
}

