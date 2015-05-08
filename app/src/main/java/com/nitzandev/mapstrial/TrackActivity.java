package com.nitzandev.mapstrial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Locale;


public class TrackActivity extends Activity implements View.OnClickListener {

    private static final int ERR_CODE = -1;
    private long mStartTime = 0;
    private float mDistanceTraveled;
    MainActivity.MEASURE_STATE mCurrState = MainActivity.MEASURE_STATE.MEASURE_NONE;
    private TextView mDistanceView;
    private TextView mRecoinsView;
    private TextView mSpeedView;
    private MeasureType mData;
    private float mRecoinsEarned;
    private Spinner mSpinner;

    LocationDataReceiver dataReceiver = new LocationDataReceiver() {

        @Override
        public void updateView(float speed) {
            mDistanceTraveled = getSharedPreferences(MainActivity.MAPS_TRIAL_PREFS_FILE, MODE_PRIVATE).getFloat(TrackService.DISTANCE, 0);;
            getDataFromPrefs();
            mDistanceView.setText(getString(R.string.distance, String.valueOf(mDistanceTraveled)));
            mSpeedView.setText(getString(R.string.speed, String.valueOf(speed)));
            Log.i("LocationDataReceiver", "dis: " + String.valueOf(mDistanceTraveled) + " mData.getCcx() " + mData.getCcx());
            mRecoinsEarned = Float.valueOf(mData.getCcx()) * mDistanceTraveled;
            mRecoinsView.setText(getString(R.string.recoins, String.valueOf(mRecoinsEarned)));


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            mCurrState.setValue(action);
            mStartTime = intent.getLongExtra(MainActivity.START_TIME, System.currentTimeMillis());
            Log.i(TrackActivity.class.getName(), "The time we started tracking is: " + Utils.milliToString(mStartTime));
            mData = new MeasureType();
            getDataFromPrefs();
            setContentView(R.layout.activity_track);
            initActionBar();
            initViews();
            IntentFilter filter = new IntentFilter();
            filter.addAction(TrackService.DATA_UPDATED);
            registerReceiver(dataReceiver, filter);

        } else {
            Log.e(TrackActivity.class.getName(), "intent is null");
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dataReceiver);
    }

    private void initViews() {
        mDistanceView = (TextView) findViewById(R.id.distance);
        mDistanceView.setText(getString(R.string.distance, 0.0));
        mSpeedView = (TextView) findViewById(R.id.speed);
        mSpeedView.setText(getString(R.string.speed, 0.0));
        mRecoinsView = (TextView) findViewById(R.id.recoins);
        mRecoinsView.setText(getString(R.string.recoins, 0.0));
        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);
        Button alive = (Button) findViewById(R.id.alive);
        alive.setOnClickListener(this);

    }

    private void initActionBar() {
        getActionBar().setTitle(getString(R.string.tracking).toUpperCase(Locale.getDefault()));
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = new Intent();
        switch (id) {
            case R.id.stop:
                intent.setAction(MainActivity.MEASURE_STATE.MEASURE_STOP.getValue());
                checkConditions();
                break;
            case R.id.alive:
                intent.setAction(MainActivity.MEASURE_STATE.MEASURE_ALIVE.getValue());
                break;
        }
        sendBroadcast(intent);
    }

    private void checkConditions() {
        int resultCode = 0;
        long stopTime = System.currentTimeMillis();
        float averageSpeed = getAverageSpeed(stopTime);
        Log.i(TrackActivity.class.getName(), "the average speed is: " + averageSpeed);
        ArrayList<Integer> errorMsgs = new ArrayList<>();
        //first condition - distance should be higher the minimum distance
        if (mDistanceTraveled < mData.getMinimumDistance()) {
            resultCode = ERR_CODE;
            errorMsgs.add(R.string.err_min_distance);
        }
        //second condition - average speed should be between threshholds
        if (mData.getLower_threshold() > averageSpeed) {
            resultCode = ERR_CODE;
            errorMsgs.add(R.string.err_average_lower);
        }
        if (mData.getUpper_threshold() < averageSpeed) {
            resultCode = ERR_CODE;
            errorMsgs.add(R.string.err_average_upper);
        }
        if (resultCode == ERR_CODE) {
            // one of the conditions was not kept
            Utils.showErrorDialog(this, errorMsgs);
        } else {
            showFinishDialog(averageSpeed);
        }
    }

    private void showFinishDialog(float averageSpeed) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle(R.string.done_tracking);
        alertDialogBuilder.setView(getFinishView(averageSpeed));
        alertDialogBuilder.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                TrackActivity.this.finish();
            }
        });

        AlertDialog resultDialog = alertDialogBuilder.create();
        resultDialog.setCanceledOnTouchOutside(true);
        resultDialog.show();
    }

    private View getFinishView(float averageSpeed) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View resultView = layoutInflater.inflate(R.layout.win_alert, null, false);
        TextView distance = (TextView) resultView.findViewById(R.id.distance);
        distance.setText(getString(R.string.finish_distance, mDistanceTraveled));
        TextView speed = (TextView) resultView.findViewById(R.id.speed);
        speed.setText(getString(R.string.finish_average_speed, averageSpeed));
        TextView recoins = (TextView) resultView.findViewById(R.id.recoins);
        recoins.setText(getString(R.string.finish_recoins, mRecoinsEarned));
        return resultView;
    }


    private float getAverageSpeed(long stopTime) {
        long timeDelta = Math.abs(stopTime - mStartTime);
        Log.i(TrackActivity.class.getName(), "the time passed is: " + Utils.milliToString(timeDelta));
        float timeInHours = timeDelta / (float)(1000 * 60 * 60);
        Log.i(TrackActivity.class.getName(), "the time passed in hour is: " + timeInHours);

        return mDistanceTraveled / timeInHours;
    }


    public void getDataFromPrefs() {

        String key = mCurrState.getValue();
        Log.i(TrackActivity.class.getName(), "the data in the prefs KEY is: " + key);

        String serverData = getSharedPreferences(MainActivity.MAPS_TRIAL_PREFS_FILE, MODE_PRIVATE).getString(key, "");
        Log.i(TrackActivity.class.getName(), "the data in the prefs is: " + serverData);
        if (!TextUtils.isEmpty(serverData)) {
            mData = new Gson().fromJson(serverData, MeasureType.class);
        }
    }
}
