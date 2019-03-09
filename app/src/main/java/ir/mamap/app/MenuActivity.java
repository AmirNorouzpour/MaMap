package ir.mamap.app;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.CustomTypefaceSpan;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import java.net.URLEncoder;
import java.util.List;

import ir.oxima.dialogbuilder.DialogBuilder;

public class MenuActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public BottomNavigationView bottomNav;
    public MapFragment CurrentFragment = null;
    private GoogleApiClient mGoogleApiClient;
    AVLoadingIndicatorView loadingIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        int iii = 1 / 0;
        bottomNav = findViewById(R.id.bottom_nav);
        Typeface baseFont = Typeface.createFromAsset(MenuActivity.this.getAssets(), "fonts/iran_san.ttf");
        CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan("", baseFont);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            MenuItem menuItem = bottomNav.getMenu().getItem(i);
            SpannableStringBuilder spannableTitle = new SpannableStringBuilder(menuItem.getTitle());
            spannableTitle.setSpan(typefaceSpan, 0, spannableTitle.length(), 0);
            menuItem.setTitle(spannableTitle);
        }
        bottomNav.setSelectedItemId(R.id.item_1);
        loadingIndicatorView = findViewById(R.id.avi);
        bottomNav.setOnNavigationItemSelectedListener(this);
        CheckPermissions();
        LocationService mSensorService = new LocationService(Mamap.getContext());
        Intent mServiceIntent = new Intent(Mamap.getContext(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }
        GeneralUtils.startPowerSaverIntent(MenuActivity.this);
    }

    public void GetUserAccount() {
        GeneralUtils.showLoading(loadingIndicatorView);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/GetUserAccount")
                .get(new TypeToken<ClientData<User>>() {
                }, new INetwork<ClientData<User>>() {
                    @Override
                    public void onResponse(ClientData<User> response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                            Mamap.User = response.getEntity();
                            loadFragment(new FriendsFragment());
                            // startService(new Intent(MenuActivity.this, LocationService.class));

                        } else {
                            GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }
                });
    }

    private void ShowLocationDialog() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(MenuActivity.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(locationSettingsResult -> {

            final Status status = locationSettingsResult.getStatus();
            final LocationSettingsStates LS_state = locationSettingsResult.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    GoNextActivity();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                    if (ActivityCompat.checkSelfPermission(MenuActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    if (ActivityCompat.checkSelfPermission(MenuActivity.this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);//endregion get permissions

                    if (ActivityCompat.checkSelfPermission(MenuActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MenuActivity.this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MenuActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            exit();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        GeneralUtils.showToast("برای خروج از برنامه دوباره برگشت را فشار دهید", Toast.LENGTH_SHORT, OutType.Warning);

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }


    private void CheckPermissions() {
        Dexter.withActivity(MenuActivity.this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted()) {
                    ShowLocationRequestDialog();
                } else {

                    LocationManager lm = (LocationManager) MenuActivity.this.getSystemService(Context.LOCATION_SERVICE);
                    boolean gps_enabled = false;

                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ex) {
                    }

                    if (!gps_enabled) {


                        DialogBuilder dialog = new DialogBuilder(MenuActivity.this).asBottomSheetDialog(false);
                        dialog.setMessage("امکان دریافت موقعیت مکانی وجود ندارد، لطفا از روشن بودن موقعیت یاب اطمینان حاصل نمائید.");
                        dialog.setTitle("موقعیت یاب");
                        dialog.setPositiveButton("باشه", d -> {
                            d.dismiss();
                            ShowLocationDialog();
                        });
                        dialog.show();
                    } else
                        GoNextActivity();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    private void GoNextActivity() {
        GetUserAccount();
    }

    private void ShowLocationRequestDialog() {
        DialogBuilder dialogBuilder = new DialogBuilder(MenuActivity.this).asBottomSheetDialog(false);
        dialogBuilder.setMessage("برای کارکرد صحیح برنامه نیاز است برنامه دسترسی به موقعیت مکانی شما داشته باشد");
        dialogBuilder.setTitle("عدم دسترسی");
        dialogBuilder.setPositiveButton("اعمال", dialog -> {
            dialog.dismiss();
            CheckPermissions();
        });

        dialogBuilder.setNegativeButton("خروج", dialog -> {
            dialog.dismiss();
            exit();
        });

        dialogBuilder.show();
    }

    private void exit() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected static final int REQUEST_CHECK_SETTINGS = 1000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        GoNextActivity();
                        break;
                    case Activity.RESULT_CANCELED:
                        GeneralUtils.showToast("موقعیت یاب روشن نشد", Toast.LENGTH_SHORT, OutType.Error);
                        GoNextActivity();
                        break;
                    default:
                        break;
                }
                break;
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    //register your activity onResume()
    @Override
    public void onResume() {
        super.onResume();
        this.registerReceiver(mMessageReceiver, new IntentFilter("MenuActivity"));
    }

    //Must unregister onPause()
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mMessageReceiver);
    }


    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            String tUserId = intent.getStringExtra("tag");
            if (message.equals("1")) {

            } else if (message.equals("7")) {
                CurrentFragment.ShowMsg(tUserId);
            }
        }
    };

    public boolean
    loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() != bottomNav.getSelectedItemId())
            switch (menuItem.getItemId()) {
                case R.id.item_1:
                    return loadFragment(new FriendsFragment());
                case R.id.item_2: {
                    MapFragment mapFragment = new MapFragment();
                    CurrentFragment = mapFragment;
                    return loadFragment(mapFragment);
                }
                case R.id.item_3:
                    return loadFragment(new SettingsFragment());
                case R.id.item_4:
                    return loadFragment(new ProfileFragment());
            }
        return false;
    }

    public void GetUserPicture(ImageView imgAvatar) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .circleCropTransform()
                .error(R.drawable.ic_user);
        int userId = GeneralUtils.ReadUserId(MenuActivity.this);
        SharedPreferences editor = MenuActivity.this.getSharedPreferences("token", MODE_PRIVATE);
        String fileName = editor.getString("PFileName", "");
        String user = "";
        try {
            user = CryptoHelper.encrypt(userId + "");
            user = URLEncoder.encode(user, "utf-8");
            fileName = URLEncoder.encode(fileName, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }


        String userImgUrl = Mamap.BaseUrl + "/api/user/GetProfilePicture?userId=" + user + "&FileName=" + fileName;
        Glide.with(this).load(userImgUrl).apply(options).into(imgAvatar);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
