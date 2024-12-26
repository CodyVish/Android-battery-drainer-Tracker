package com.lohjason.genericbatterydrainer.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.view.Surface;
import android.widget.Toast;

import com.lohjason.genericbatterydrainer.utils.Logg;

import java.io.File;
import java.io.IOException;

public class VideoRecordingService extends Service {

    private MediaRecorder mediaRecorder;
    private File outputFile;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaRecorder = new MediaRecorder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logg.d("VideoRecordingService", flags + " " + startId);
        if (intent != null && "START".equals(intent.getAction())) {
            startRecording();
        } else if (intent != null && "STOP".equals(intent.getAction())) {
            stopRecording();
        }
        //return START_NOT_STICKY;
        return START_REDELIVER_INTENT;
    }

    private void startRecording() {
        outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "video.mp4");

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(new Surface(new SurfaceTexture(10)));

        try {
            mediaRecorder.prepare();
            Thread.sleep(1000);
            mediaRecorder.start();
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

