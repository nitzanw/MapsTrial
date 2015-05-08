package com.nitzandev.mapstrial;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class TrackService
        extends IntentService implements LocationProvider.LocationCallback {


    public static final String DATA_UPDATED = "com.nitzandev.mapstrial.action.UPDATE";
    public static final String DISTANCE = "com.nitzandev.mapstrial.data.DISTANCE";
    public static final String SPEED = "com.nitzandev.mapstrial.data.SPEED";
    private LocationProvider mLocationProvider;
    private boolean currentlyProcessingLocation = false;
    private Location mCurrLocation;
    private float mDistance = 0;

    public TrackService() {
        super("TrackService");
    }

    private final StopReceiver stopReceiver = new StopReceiver() {
        @Override
        protected void stopTracking(Intent intent) {
            Log.i(TrackService.class.getName(), "stop the service");
            String action = intent.getAction();
            Log.i(TrackService.class.getName(), action);
            if (TextUtils.equals(action, MainActivity.MEASURE_STATE.MEASURE_STOP.getValue())) {
                Log.i(TrackService.class.getName(), "stop the service");
                mLocationProvider.disconnect();
                stopSelf();
            } else if (TextUtils.equals(action, MainActivity.MEASURE_STATE.MEASURE_ALIVE.getValue())) {
                Log.i(TrackService.class.getName(), "service is alive!");
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d(TrackService.class.getName(), "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            mLocationProvider = new LocationProvider(this, this);

            if (!mLocationProvider.isConnected() || !mLocationProvider.isConnecting()) {
                mLocationProvider.connect();
            }
        } else {
            Log.e(TrackService.class.getName(), "unable to connect to google play services.");
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TrackService.class.getName(), "Track service onCreate was called");
        IntentFilter stopFilter = new IntentFilter();
        stopFilter.addAction(MainActivity.MEASURE_STATE.MEASURE_STOP.getValue());
        stopFilter.addAction(MainActivity.MEASURE_STATE.MEASURE_ALIVE.getValue()); //TODO remove this <<
        //register the receiver for the stop button
        registerReceiver(stopReceiver, stopFilter);
        Log.i(TrackService.class.getName(),"Track service onCreate was called at: " + Utils.milliToString(System.currentTimeMillis()));

    }

    @Override
    public void onDestroy() {
        if (stopReceiver != null) {
            unregisterReceiver(stopReceiver);
        }
        if (mLocationProvider != null) {
            mLocationProvider.disconnect();
        }

        Log.i(TrackService.class.getName(), "onDestroy called");

        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TrackService.class.getName(), "service was started with null!");
        }
    }


    @Override
    public void handleNewLocation(Location location) {
        Log.i(TrackService.class.getName(), "location: " + location.toString());
        float currSpeed = 0;
        if (location.hasSpeed()) {
            currSpeed = location.getSpeed();
        }
        if (mCurrLocation != null) {
            //save the distance in km
            mDistance += mCurrLocation.distanceTo(location) / (float)1000;
            SharedPreferences prefs= getSharedPreferences(MainActivity.MAPS_TRIAL_PREFS_FILE,MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(DISTANCE,mDistance).commit();
        }
        Intent intent = new Intent();
        intent.putExtra(SPEED, currSpeed);
        intent.setAction(DATA_UPDATED);
        sendBroadcast(intent);
        mCurrLocation = location;
    }
}
