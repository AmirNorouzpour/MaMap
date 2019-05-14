package ir.mamap.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import info.hoang8f.android.segmented.SegmentedGroup;
import ir.mamap.app.Utils.Utils;

public class UpgradeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    SegmentedGroup segmented2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserConfig.getInstance().init(this, Mamap.getLanguageType());
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();

        setContentView(R.layout.activity_upgrade);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {

        }

        segmented2 = (SegmentedGroup) findViewById(R.id.segmented);
        segmented2.setTintColor(getResources().getColor(R.color.colorAccent));
        segmented2.setOnCheckedChangeListener(this);
        addButton(segmented2,"تست پویا",R.id.button_r_1);
        addButton(segmented2,"تست 2",R.id.button_r_2);
        addButton(segmented2,"تست 3",R.id.button_r_3);
//        SegmentedGroup segmented3 = (SegmentedGroup) findViewById(R.id.segmented3);
//        segmented3.setTintColor(Color.parseColor("#FFD0FF3C"), Color.parseColor("#FF7B07B2"));
//
//        SegmentedGroup segmented4 = (SegmentedGroup) findViewById(R.id.segmented4);
//        segmented4.setTintColor(getResources().getColor(R.color.radio_button_selected_color));




    }

    private static final String TAG = UpgradeActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(UpgradeActivity.this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {

                } else {
                    mService.requestLocationUpdates();
                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(UpgradeActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }





    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.button_r_1:
                Toast.makeText(UpgradeActivity.this, "1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_2:
                Toast.makeText(UpgradeActivity.this, "2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_3:
                Toast.makeText(UpgradeActivity.this, "3", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_4:
                Toast.makeText(UpgradeActivity.this, "4", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_5:
                Toast.makeText(UpgradeActivity.this, "5", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_6:
                Toast.makeText(UpgradeActivity.this, "6", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_7:
                Toast.makeText(UpgradeActivity.this, "7", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_8:
                Toast.makeText(UpgradeActivity.this, "8", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_9:
                Toast.makeText(UpgradeActivity.this, "9", Toast.LENGTH_SHORT).show();
                break;
            default:
                // Nothing to do
        }
    }

    private void addButton(SegmentedGroup group, String text, int id) {
        RadioButton radioButton = (RadioButton) UpgradeActivity.this.getLayoutInflater().inflate(R.layout.radio_button_item, null);
        radioButton.setText(text);
        radioButton.setId(id);//R.id.button_r_1
        group.addView(radioButton);
        group.updateBackground();
    }
}
