package com.nitzandev.mapstrial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by nitzanwerber on 5/7/15.
 */
public class LocationDataReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        float speed = intent.getFloatExtra(TrackService.SPEED, 0);
        updateView(speed);
    }

    public void updateView(float speed) {
    }
}
