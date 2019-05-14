package ir.mamap.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import ir.mamap.app.Models.BaseResponse;
import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.DeviceInformation;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class FirebaseService extends FirebaseMessagingService {


    private static final String TAG = "FirebaseService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> params = remoteMessage.getData();
            JSONObject object = new JSONObject(params);
            if (object.has("title")) {
                try {
                    sendNotification(object.getString("title"), object.getString("body"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
            try {
                //String title = object.getString("title");
                // sendNotification(title);
                int catId = object.getInt("CatId");
                int trId = 0;
                int tag = 0;
                if (object.has("Tag"))
                    tag = object.getInt("Tag");
                if (object.has("TRId"))
                    trId = object.getInt("TRId");
                if (catId == 1) {
                    SharedPreferences sharedPreferences = Mamap.getContext().getSharedPreferences("UserLoc", MODE_PRIVATE);
                    String lat = sharedPreferences.getString("UserLocLat", null);
                    String lon = sharedPreferences.getString("UserLocLon", null);
                    String speed = sharedPreferences.getString("UserSpeed", null);

                    GPSTracker gps = new GPSTracker(Mamap.getContext());
                    if ((!gps.isGPSEnabled && gps.isNetworkEnabled) || gps.getLatitude() == 0)
                        SystemClock.sleep(5000);
                    if (gps.getLatitude() != 0) {
                        lat = String.valueOf(gps.getLatitude());
                        lon = String.valueOf(gps.getLongitude());
                        speed = String.valueOf(gps.getSpeed());
                    }
                    gps.stopUsingGPS();
                    try {
                        int batLevel = 0;
                        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                        }
//                        Intent notificationIntent = new Intent(Mamap.getContext(), LocationService.class);
//
//                        PendingIntent pendingIntent = PendingIntent.getActivity(Mamap.getContext(), 0,
//                                notificationIntent, 0);
//
//                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
//                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
//                        Notification notification = notificationBuilder.setOngoing(true)
//                                .setSmallIcon(R.mipmap.ic_launcher)
//                                .setPriority(PRIORITY_MAX)
//                                .setContentText("saLAM")
//                                .setCategory(NotificationCompat.CATEGORY_SERVICE)
//                                .setContentIntent(pendingIntent)
//                                .build();
//
//                        startForeground(1337, notification);

                        String data = tag + ",,," + lat + ",,," + lon + ",,," + speed + ",,," + trId;
                        String dataEnc = CryptoHelper.encrypt(data);
                        SendUserLocation(dataEnc.replace("\n", ""));

//                        Intent intent = new Intent(Mamap.getContext(), MyForeGroundService.class);
//                        intent.setAction(MyForeGroundService.ACTION_START_FOREGROUND_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            startMyOwnForeground();
                        else
                            startForeground(1, new Notification());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (catId == 7)
                    updateMyActivity(Mamap.getContext(), String.valueOf(catId), String.valueOf(tag));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(null, remoteMessage.getNotification().getBody());
            // updateMyActivity(Mamap.getContext(), "test ast");
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // This function will create an intent. This intent must take as parameter the "unique_name" that you registered your activity with
    static void updateMyActivity(Context context, String message, String tag) {

        Intent intent = new Intent("MenuActivity");

        //put whatever data you want to send, if any
        intent.putExtra("message", message);
        intent.putExtra("tag", tag);

        //send broadcast
        context.sendBroadcast(intent);
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
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
    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
      /*  // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]*/
    }

    public void appendLog(String text) {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");

    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        if (GeneralUtils.isSignedIn()) {

            DeviceInformation deviceInformation = new DeviceInformation();
            deviceInformation.setToken(token);
            deviceInformation.setDeviceName(Build.MODEL);
            deviceInformation.setDeviceId(Build.MODEL);
            String ip = GeneralUtils.GetIPAddress(true);
            deviceInformation.setIp(ip);
            deviceInformation.setOs(GeneralUtils.GetDeviceInfo());
            deviceInformation.setInstalledAppVersion(GeneralUtils.GetVersionName());

            NetworkManager.builder()
                    .setUrl(Mamap.BaseUrl + "/api/user/SetDeviceInformation")
                    .addApplicationJsonBody(deviceInformation)
                    .post(new TypeToken<ClientDataNonGeneric>() {
                    }, new INetwork<ClientDataNonGeneric>() {

                        @Override
                        public void onResponse(ClientDataNonGeneric data) {
                            if (data.getOutType() != OutType.Success)
                                GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, OutType.Error);
                        }

                        @Override
                        public void onError(ANError anError) {
                            GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        }
                    }, Mamap.getContext());
        }
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    public void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MessageListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "100";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
