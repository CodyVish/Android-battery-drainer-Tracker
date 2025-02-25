package com.lohjason.genericbatterydrainer.ui;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.managers.BluetoothScanManager;
import com.lohjason.genericbatterydrainer.managers.DrainManager;
import com.lohjason.genericbatterydrainer.managers.WifiScanManager;
import com.lohjason.genericbatterydrainer.models.BatteryInfo;
import com.lohjason.genericbatterydrainer.services.DrainForegroundService;
import com.lohjason.genericbatterydrainer.utils.DialogUtils;
import com.lohjason.genericbatterydrainer.utils.PermissionUtils;
import com.lohjason.genericbatterydrainer.utils.SharedPrefsUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private long startTimeMillis;
    private long endTimeMillis;
    private static final String LOG_TAG = "+_ManAtv";
    public static final String ACTION_START_VIDEO_RECORDING = "com.lohjason.genericbatterydrainer.START_VIDEO_RECORDING";
    private static final int REQUEST_PERMISSIONS = 105;
    private SwitchCompat      switchFlash;
    private SwitchCompat      switchCpu;
    private SwitchCompat      switchGpu;
    private SwitchCompat      switchScreen;
    private SwitchCompat      switchGps;
    private SwitchCompat      switchWifi;
    private SwitchCompat      switchBluetooth;

    private SwitchCompat switchPlayVideo;
    private SwitchCompat switchOpenApps;
    private SwitchCompat switchRecordVideo;
    private SwitchCompat switchVibration;
    private TextView          btnStart;
    private ImageView         ivAboutApp;
    private ImageView         ivSettings;
    private Disposable        isDrainingDisposable;
    private TextView          tvBattLevel;
    private TextView          tvVoltage;
    private TextView          tvBattTemp;
    private BroadcastReceiver batteryLevelReceiver;

    private AlertDialog aboutDialog;
    private AlertDialog openSettingsDialog;
    private AlertDialog permissionRationaleDialog;

    private Handler handler;
    private Runnable vibrationRunnable;

    MainViewModel mainViewModel;

    private final BroadcastReceiver videoRecordingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START_VIDEO_RECORDING.equals(intent.getAction())) {
                requestPermissionsIfNeeded();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(ACTION_START_VIDEO_RECORDING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(videoRecordingReceiver, filter);
        }
        mainViewModel = ViewModelProviders.of(MainActivity.this).get(MainViewModel.class);
        setupViews();
        setupSwitchStates();
        setupObservables();

        handler = new Handler();
    }

    private void setupViews() {
        switchFlash = findViewById(R.id.switch_flash);
        switchScreen = findViewById(R.id.switch_screen);
        switchCpu = findViewById(R.id.switch_cpu);
        switchGpu = findViewById(R.id.switch_gpu);
        switchGps = findViewById(R.id.switch_gps);
        switchWifi = findViewById(R.id.switch_wifi);
        switchBluetooth = findViewById(R.id.switch_bluetooth);

        switchPlayVideo = findViewById(R.id.switch_play_video);
        switchOpenApps = findViewById(R.id.switch_open_apps);
        switchRecordVideo = findViewById(R.id.switch_record_video);
        switchVibration = findViewById(R.id.switch_vibration);

        btnStart = findViewById(R.id.tv_start);
        ivAboutApp = findViewById(R.id.iv_about);
        ivSettings = findViewById(R.id.iv_settings);
        tvBattLevel = findViewById(R.id.tv_battery_percentage);
        tvBattTemp = findViewById(R.id.tv_battery_temp);
        tvVoltage = findViewById(R.id.tv_battery_voltage);


        switchFlash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionUtils.hasCameraPermission(MainActivity.this)) {
                    switchFlash.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_CAMERA);
                }
            }
        });

        switchScreen.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionUtils.hasWriteSettingsPermission(MainActivity.this)) {
                    switchScreen.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_WRITE_SETTINGS);
                }
            }
        }));

        switchGps.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!PermissionUtils.hasLocationPermission(MainActivity.this)) {
                    switchGps.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_FINE_LOCATION);
                }
            }
        }));

        switchWifi.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!WifiScanManager.getInstance().isWifiScanEnabled(getApplication())) {
                    switchWifi.setChecked(false);
                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_WIFI);
                }
            }
        }));

        switchBluetooth.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                if (!BluetoothScanManager.getInstance().isBluetoothEnabled()) {
                    switchBluetooth.setChecked(false);
//                    showPermissionRationaleDialog(PermissionUtils.REQUEST_CODE_BLUETOOTH);
                    BluetoothScanManager.getInstance().enableBluetooth(this);
                }
            }
        }));





        // Camera Recording and Flashlight

        switchRecordVideo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Code to start recording video
                // Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                // startActivity(intent);
                Log.d("VideoRecording", "button checked");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    switchRecordVideo.setChecked(false);
                    Log.d("VideoRecording", "req perm");
                    ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    }, 105);
            }
            }
        });


        btnStart.setOnClickListener(v -> {
            DrainManager drainManager = DrainManager.getInstance(getApplication());
            if (drainManager.isDraining()) {
                stopDraining();
            } else {
                startDraining();
            }

        });

        ivAboutApp.setOnClickListener(v -> showAboutAppDialog());

        ivSettings.setOnClickListener(v -> showSettingsDialogFragment());
    }

    private void setupSwitchStates() {
        boolean[] switchStates = SharedPrefsUtils.getSwitchStates(this);
        boolean   flashOn      = switchStates[0];
        boolean   screenOn     = switchStates[1];
        boolean   cpuOn        = switchStates[2];
        boolean   gpuOn        = switchStates[3];
        boolean   locationOn   = switchStates[4];
        boolean   wifiOn       = switchStates[5];
        boolean   bluetoothOn  = switchStates[6];
        boolean   playVideo    = switchStates[7];
        boolean   openApps     = switchStates[8];
        boolean   recordVideo  = switchStates[9];
        boolean   vibration    = switchStates[10];

        if (PermissionUtils.hasCameraPermission(this)) {
            switchFlash.setChecked(flashOn);
        }
        if (PermissionUtils.hasWriteSettingsPermission(this)) {
            switchScreen.setChecked(screenOn);
        }
        switchCpu.setChecked(cpuOn);
        switchGpu.setChecked(gpuOn);
        if (PermissionUtils.hasLocationPermission(this)) {
            switchGps.setChecked(locationOn);
        }
        if (WifiScanManager.getInstance().isWifiScanEnabled(getApplication())) {
            switchWifi.setChecked(wifiOn);
        }
        if (BluetoothScanManager.getInstance().isBluetoothEnabled()) {
            switchBluetooth.setChecked(bluetoothOn);
        }
    }

    private void setupObservables() {
        isDrainingDisposable = DrainManager.getInstance(getApplication())
                .getDrainBehaviorSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(b -> {
                    if (b == null) {
                        return;
                    }
                    setStartDrainingButtonState(b);
                });

        Observer<String[]> batteryInfoObserver = batteryInfo -> {
            if(batteryInfo != null && batteryInfo.length == 3){
                tvBattLevel.setText(batteryInfo[0]);
                tvBattTemp.setText(batteryInfo[1]);
                tvVoltage.setText(batteryInfo[2]);
            }
        };
        mainViewModel.getBatteryInfoLiveData().observe(MainActivity.this, batteryInfoObserver);
    }

    private void setupBatteryLevelReceiver() {
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level           = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale           = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int tempDeciCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                int milliVoltage    = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

                BatteryInfo batteryInfo = new BatteryInfo(level, milliVoltage, tempDeciCelsius, scale);
                mainViewModel.setBatteryInfo(batteryInfo, SharedPrefsUtils.getUsesFahrenheit(MainActivity.this));
            }
        };

        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

    private void setStartDrainingButtonState(boolean isNowDraining) {
        if (isNowDraining) {
            btnStart.setText(R.string.stop);
            btnStart.setBackgroundResource(R.drawable.rounded_shape_red);
            btnStart.setTextColor(ContextCompat.getColor(this, R.color.material_red_400));
        } else {
            btnStart.setText(R.string.start);
            btnStart.setBackgroundResource(R.drawable.rounded_shape_green);
            btnStart.setTextColor(ContextCompat.getColor(this, R.color.material_green_400));
        }
    }

    private void startDraining() {
        if (DrainManager.getInstance(getApplication()).isDraining()) {
            return;
        }
        Intent startIntent = new Intent(MainActivity.this, DrainForegroundService.class);
        startIntent.setAction(DrainForegroundService.ACTION_START);

        startIntent.putExtra(DrainForegroundService.KEY_FLASH, switchFlash.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_SCREEN, switchScreen.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_CPU, switchCpu.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_GPU, switchGpu.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_GPS, switchGps.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_WIFI, switchWifi.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_BLUETOOTH, switchBluetooth.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_VIDEO, switchPlayVideo.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_OPEN, switchOpenApps.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_RECORD, switchRecordVideo.isChecked());
        startIntent.putExtra(DrainForegroundService.KEY_VIBRATION, switchVibration.isChecked());

        Toast.makeText(this, "Drain Started!", Toast.LENGTH_SHORT).show();
        saveSwitchStates();
        startService(startIntent);
        startTimeMillis = System.currentTimeMillis();
    }

    private void stopDraining() {
        endTimeMillis = System.currentTimeMillis();

        // Calculate the elapsed time
        long elapsedTimeMillis = endTimeMillis - startTimeMillis;

        // Convert milliseconds to seconds or minutes for readability
        long elapsedTimeSeconds = elapsedTimeMillis / 1000;
        long elapsedTimeMinutes = elapsedTimeSeconds / 60;

        // Construct the message for the Toast
        String message = "Time taken: " + elapsedTimeSeconds + " seconds";

        // Display the elapsed time in a Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        releaserecorder();
        Intent stopIntent = new Intent(MainActivity.this, DrainForegroundService.class);
        stopIntent.setAction(DrainForegroundService.ACTION_STOP);
        startService(stopIntent);

    }

    private void showSettingsDialogFragment() {
        SettingsDialogFragment fragment = SettingsDialogFragment.getNewInstance();
        fragment.show(getSupportFragmentManager(), "SETTINGS_FRAGMENT");
    }

    private void showOpenSettingsDialog(int requestCode) {
        if (openSettingsDialog != null) {
            openSettingsDialog.dismiss();
        }
        openSettingsDialog = DialogUtils.getOpenSettingsDialog(this, requestCode);
    }

    private void showAboutAppDialog() {
        if (aboutDialog != null) {
            aboutDialog.dismiss();
        }
        aboutDialog = DialogUtils.getAboutAppDialog(this);
    }

    private void showPermissionRationaleDialog(int requestCode) {
        if (permissionRationaleDialog != null) {
            permissionRationaleDialog.dismiss();
        }
        permissionRationaleDialog = DialogUtils.showPermissionRationaleDialog(this, requestCode);
    }

    private void saveSwitchStates() {
        SharedPrefsUtils.setSwitchStates(this,
                                         switchFlash.isChecked(),
                                         switchScreen.isChecked(),
                                         switchCpu.isChecked(),
                                         switchGpu.isChecked(),
                                         switchGps.isChecked(),
                                         switchWifi.isChecked(),
                                         switchBluetooth.isChecked(),
                                         switchPlayVideo.isChecked(),
                                         switchOpenApps.isChecked(),
                                         switchRecordVideo.isChecked(),
                                         switchVibration.isChecked()
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveSwitchStates();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switchFlash.setChecked(true);
                    } else {
                        if (PermissionUtils.canRequestCameraPermission(MainActivity.this)) {
                            showPermissionRationaleDialog(requestCode);
                        } else {
                            showOpenSettingsDialog(requestCode);
                        }
                    }
                }
                break;
            }
            case PermissionUtils.REQUEST_CODE_FINE_LOCATION: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        switchGps.setChecked(true);
                    } else {
                        if (PermissionUtils.canRequestLocationPermission(MainActivity.this)) {
                            showPermissionRationaleDialog(requestCode);
                        } else {
                            showOpenSettingsDialog(requestCode);
                        }
                    }
                }
                break;
            }
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recordHighResVideo();
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_BLUETOOTH: {
                if (resultCode == Activity.RESULT_OK) {
                    switchBluetooth.setChecked(true);
                } else {
                    showPermissionRationaleDialog(requestCode);
                }
                break;
            }
            case PermissionUtils.REQUEST_CODE_WRITE_SETTINGS: {
                if (PermissionUtils.hasWriteSettingsPermission(this)) {
                    switchScreen.setChecked(true);
                }
                break;
            }
            default: {
                onActivityResult(requestCode, resultCode, data);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBatteryLevelReceiver();
    }

    @Override
    protected void onPause() {
        if (batteryLevelReceiver != null) {
            unregisterReceiver(batteryLevelReceiver);
        }
        if (openSettingsDialog != null && openSettingsDialog.isShowing()) {
            openSettingsDialog.dismiss();
        }
        if (aboutDialog != null && aboutDialog.isShowing()) {
            aboutDialog.dismiss();
        }
        if (permissionRationaleDialog != null && permissionRationaleDialog.isShowing()) {
            permissionRationaleDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (isDrainingDisposable != null) {
            isDrainingDisposable.dispose();
            isDrainingDisposable = null;
        }
        super.onDestroy();

        unregisterReceiver(videoRecordingReceiver);
    }

    private void requestPermissionsIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
        } else {
            recordHighResVideo();
        }
    }
    private void releaserecorder() {
        /*
        Intent intent = new Intent(this, VideoRecordingService.class);
        intent.setAction("STOP");
        startService(intent);

         */
    }

    private void recordHighResVideo() {
        /*
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // High quality
        startActivity(intent);
        */
        /*
        Intent intent = new Intent(this, VideoRecordingService.class);
        intent.setAction("START");
        startService(intent);

         */

    }




}
