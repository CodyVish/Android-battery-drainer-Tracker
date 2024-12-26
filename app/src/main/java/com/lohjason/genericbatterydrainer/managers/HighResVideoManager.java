package com.lohjason.genericbatterydrainer.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class HighResVideoManager {

    private static HighResVideoManager instance;

    private HighResVideoManager() {
    }

    public static HighResVideoManager getInstance() {
        if (instance == null) {
            instance = new HighResVideoManager();
        }
        return instance;
    }

    public void playHighResVideo(Context context, String videoUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        //intent.setDataAndType(Uri.parse(videoUrl), "video/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("force_fullscreen", true);
        context.startActivity(intent);
    }
}
