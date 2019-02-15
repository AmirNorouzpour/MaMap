package net.friendsmap.ayrsa.friendsmap;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Utils.CryptoHelper;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public int counter = 0;
    Context context;

    public LocationService(Context applicationContext) {
        super();
        context = applicationContext;
    }

    public LocationService() {
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }


        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);
            Log.d(TAG, "onLocationChanged()");
            float distance = 51;

            SharedPreferences sharedPreferences = FriendsMap.getContext().getSharedPreferences("UserLocOld", MODE_PRIVATE);
            if (sharedPreferences.getString("UserLocLat", "") != "") {
                float lat = Float.parseFloat(sharedPreferences.getString("UserLocLat", ""));
                float lon = Float.parseFloat(sharedPreferences.getString("UserLocLon", ""));
                float speed = Float.parseFloat(sharedPreferences.getString("UserSpeed", ""));
                Location preLocation = new Location("Pre");
                preLocation.setLatitude(lat);
                preLocation.setLongitude(lon);
                preLocation.setSpeed(speed);
                distance = preLocation.distanceTo(location);
            }

            String data = "0,,," + location.getLatitude() + ",,," + location.getLongitude() + ",,," + location.getSpeed();
            //  GeneralUtils.showToast("distance : " + distance, Toast.LENGTH_LONG, OutType.Error);
            if (distance > 100) {


                String dataEnc = null;
                try {
                    dataEnc = CryptoHelper.encrypt(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FirebaseService.SendUserLocation(dataEnc.replace("\n", ""));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UserLocLat", String.valueOf(location.getLatitude()));
                editor.putString("UserLocLon", String.valueOf(location.getLongitude()));
                editor.putString("UserSpeed", String.valueOf(location.getSpeed()));
                editor.apply();
            }

            SharedPreferences.Editor sharedPreferencesUser = FriendsMap.getContext().getSharedPreferences("UserLoc", MODE_PRIVATE).edit();
            sharedPreferencesUser.putString("UserLocLat", String.valueOf(location.getLatitude()));
            sharedPreferencesUser.putString("UserLocLon", String.valueOf(location.getLongitude()));
            sharedPreferencesUser.putString("UserSpeed", String.valueOf(location.getSpeed()));
            sharedPreferencesUser.apply();

        }


        @Override
        public void onProviderDisabled(String provider) {
            //  GeneralUtils.showToast("onProviderDisabled: " + provider, Toast.LENGTH_SHORT, OutType.Warning);
        }

        @Override
        public void onProviderEnabled(String provider) {
            //   GeneralUtils.showToast("onProviderEnabled: " + provider, Toast.LENGTH_SHORT, OutType.Warning);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //  GeneralUtils.showToast("onStatusChanged: " + provider, Toast.LENGTH_SHORT, OutType.Warning);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, " In recieve Custome Broadcast receiver");
        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }

        Log.i("EXIT", "ondestroy!");

        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}