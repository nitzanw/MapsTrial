package com.nitzandev.mapstrial;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String START_TIME = "com.nitzandev.mapstrial.data.START";
    public static final String MAPS_TRIAL_PREFS_FILE = "com.nitzandev.mapstrial.action.SHARED_PREFS";
    static private String api = "https://development.changers.com/services/api/rest/json/?api_key=440485f417a91afb943178bf507e870a880d10fd&method='changersBank.exchanges.list'";
    private static final String CURR_MEASURE_STATE = "CURR_MEASURE_STATE";
    public MEASURE_STATE mCurrMeasure = MEASURE_STATE.MEASURE_NONE;
    private long mStartTime;

    enum MEASURE_STATE {
        MEASURE_ALIVE("com.nitzandev.mapstrial.action.ALIVE"),
        MEASURE_NONE("com.nitzandev.mapstrial.action.NONE"),
        MEASURE_WALKING("com.nitzandev.mapstrial.action.WALKING"),
        MEASURE_TRANSPORT("com.nitzandev.mapstrial.action.TRANSPORT"),
        MEASURE_STOP("com.nitzandev.mapstrial.action.STOP");
        private String value;

        MEASURE_STATE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private final BroadcastReceiver receiver = new StopReceiver() {
        @Override
        protected void stopTracking(Intent intent) {
            Log.i(MainActivity.class.getName(), "stop the measure");
            String action = intent.getAction();
            if (action.equals(MainActivity.MEASURE_STATE.MEASURE_STOP.getValue())) {
                mCurrMeasure = MainActivity.MEASURE_STATE.MEASURE_STOP;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(CURR_MEASURE_STATE)) {
            mCurrMeasure.setValue(savedInstanceState.getString(CURR_MEASURE_STATE));
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.MEASURE_STATE.MEASURE_STOP.getValue());
        registerReceiver(receiver, filter);

        setContentView(R.layout.activity_main);
        getActionBar().setTitle(getString(R.string.home).toUpperCase(Locale.getDefault()));
        initViews();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save the current measure state
        outState.putString(CURR_MEASURE_STATE, mCurrMeasure.getValue());
    }

    private void initViews() {
        ImageButton walkingButton = (ImageButton) findViewById(R.id.walking_button);
        walkingButton.setOnClickListener(this);
        ImageButton publicTButton = (ImageButton) findViewById(R.id.public_transport_button);
        publicTButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MainActivity.class.getName(), "the current state is: " + mCurrMeasure.getValue());
        Long ts = getPreferences(MODE_PRIVATE).getLong("ts", System.currentTimeMillis());
        // if the data from the server is stale - download new data
//        if (Math.abs(ts - System.currentTimeMillis()) > 1000 * 60 * 60 * 24 * 7) {
            Log.i(MainActivity.class.getName(),"ts is old - get new data from server");
            new Communicator().execute();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        MEASURE_STATE prevState = mCurrMeasure;

        if (view.getId() == R.id.walking_button) {
            //if we are already measuring - don't allow to measure again
            if (mCurrMeasure == MEASURE_STATE.MEASURE_TRANSPORT) {
                Utils.showErrorDialog(this, R.string.already_tracking);
                return;
            }
            mCurrMeasure = MEASURE_STATE.MEASURE_WALKING;

        } else if (view.getId() == R.id.public_transport_button) {
            //if we are already measuring - don't allow to measure again
            if (mCurrMeasure == MEASURE_STATE.MEASURE_WALKING) {
                Utils.showErrorDialog(this, R.string.already_tracking);
                return;
            }

            mCurrMeasure = MEASURE_STATE.MEASURE_TRANSPORT;
        }
        // only spawn the service if the former state was different from the current new state -
        // i.e None or Stop
        Log.i(MainActivity.class.getName(), "the curr state is: " + mCurrMeasure + " the prevState is: " + prevState);
        if (mCurrMeasure != prevState) {
            //this hppenes just once for each new measurement!
            Intent trackServiceIntent = new Intent(this, TrackService.class);
            mStartTime = System.currentTimeMillis();
            trackServiceIntent.setAction(mCurrMeasure.getValue());

            startService(trackServiceIntent);
        }

        // create the activity with the current measure state
        Intent trackActivityIntent = new Intent(this, TrackActivity.class);
        trackActivityIntent.setAction(mCurrMeasure.getValue());
        trackActivityIntent.putExtra(START_TIME, mStartTime);
        startActivity(trackActivityIntent);
    }


    private class Communicator extends AsyncTask<Void, Void, ArrayList<MeasureType>> {

        @Override
        protected void onPostExecute(ArrayList<MeasureType> list) {
            if (list != null) {
                //keep the data obtained from the server in shared pref
                SharedPreferences prefs= getSharedPreferences(MAPS_TRIAL_PREFS_FILE,MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                for (MeasureType m : list) {
                    String data = new Gson().toJson(m);
                    if (TextUtils.equals(m.getType(), "Walking")) {
                        editor.putString(MEASURE_STATE.MEASURE_WALKING.getValue(), data).commit();
                    }
                    if (TextUtils.equals(m.getType(), "Local")) {
                        editor.putString(MEASURE_STATE.MEASURE_TRANSPORT.getValue(), data).commit();


                    }
                }
                editor.putLong("ts", System.currentTimeMillis()).commit();
            }
        }

        @Override
        protected ArrayList<MeasureType> doInBackground(Void... voids) {
            Log.i("Communicator ", "doInBackground");
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(api);

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("method", "changersBank.exchanges.list"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String ret = EntityUtils.toString(response.getEntity());
                Log.i("Util response", ret);

                JSONObject jObj = new JSONObject(ret);
                JSONArray jArray = jObj.getJSONArray("result");
                MeasureType[] receivedMeasures = new Gson().fromJson(jArray.toString(), MeasureType[].class);
                ArrayList<MeasureType> typeList = new ArrayList<>();
                for (MeasureType m : receivedMeasures) {
                    if (TextUtils.equals(m.getType(), "Walking") || TextUtils.equals(m.getType(), "Local")) {
                        typeList.add(m);
                    }
                }


                return typeList;

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
