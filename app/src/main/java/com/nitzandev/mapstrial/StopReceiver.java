package com.nitzandev.mapstrial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by nitzanwerber on 5/6/15.
 */
public class StopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(context.getPackageName(), "onReceive");
        stopTracking(intent);
    }

    protected  void stopTracking(Intent intent) {
    }
}
