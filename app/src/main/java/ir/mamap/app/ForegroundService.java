package ir.mamap.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidnetworking.error.ANError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.reflect.TypeToken;

import java.util.concurrent.atomic.AtomicInteger;

import ir.mamap.app.Models.BaseResponse;
import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

public class ForegroundService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    private IBinder mIBinder;
    private LocationCallback locationCallback;
    private NotificationManager notificationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static ForegroundService instance = null;


    public static ForegroundService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        runInForeground("سرویس", "سرویس مامپ در حال اجرا ...", R.drawable.ic_sync_black_24dp);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    int speed = (int) ((locationResult.getLastLocation().getSpeed() * 3600) / 1000);

                    String data = tagId + ",,," + locationResult.getLastLocation().getLatitude() + ",,," + locationResult.getLastLocation().getLongitude() + ",,," + speed + ",,," + trId;
                    String dataEnc = null;
                    try {
                        dataEnc = CryptoHelper.encrypt(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SendUserLocation(dataEnc.replace("\n", ""));

                    System.out.println(String.format("%s %s", locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
                    // new Handler(Looper.getMainLooper()).post(() -> playSound());

                    Log.i("LOCATION_TAG", String.format("%s %s", locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));

                    stopService();
                }
            }
        };
        googleApiClient.connect();
    }

    public void stopService() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        googleApiClient.disconnect();
        stopForeground(true);
        stopSelf();
        instance = null;
    }

    private void playSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }

    @Override
    public void onDestroy() {
        instance = null;
    }

    private void SendUserLocation(String data) {
        ClientDataNonGeneric cdata = new ClientDataNonGeneric();
        cdata.setTag(data);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/Tracking/SetUserMapData")
                .addApplicationJsonBody(cdata)
                .post(new TypeToken<ClientData<BaseResponse>>() {
                }, new INetwork<ClientData<BaseResponse>>() {
                    @Override
                    public void onResponse(ClientData<BaseResponse> response) {
                        if (response.getOutType() == OutType.Success) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }

                }, Mamap.getContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mIBinder == null) {
            mIBinder = new Binder();
        }
        return mIBinder;
    }

    long tagId = 0;
    long trId = 0;
    private static final long UPDATE_INTERVAL = 1000, FASTEST_INTERVAL = 1000; // = 1 second

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tagId = (int) intent.getExtras().get("tag");
        trId = (int) intent.getExtras().get("trId");
        return START_STICKY;
    }

    protected LocationManager locationManager;

    @SuppressLint("MissingPermission")
    private void initLocationService() {
        final LocationRequest locationRequest = new LocationRequest();
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled)
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        else
            locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationService();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void runInForeground(String title, String subTitle, int icon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, "ID");
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setSmallIcon(icon)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(subTitle)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
        startForeground(new AtomicInteger(0).incrementAndGet(), notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("ID", "Tracker", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
    }
}
