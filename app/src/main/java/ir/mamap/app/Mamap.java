package ir.mamap.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ir.map.sdk_map.Mapir;
import ir.oxima.dialogbuilder.DialogBuilderConfig;
import ir.tapsell.sdk.Tapsell;
import okhttp3.OkHttpClient;


public class Mamap extends MultiDexApplication {

    private static Context sContext;
    public static String BaseUrl = "https://mamap.ir";
    //  public static String BaseUrl = "http://192.168.1.102:8080";

    public static ir.mamap.app.Models.User User = null;

    public static Context getContext() {
        return sContext;
    }

    public static LanguageType getLanguageType() {
        return LanguageType.forValue("en");
    }

    public void onCreate() {
        // Setup handler for uncaught exceptions.
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

        Tapsell.initialize(this, "jhabhdfjlqrsktdqqltjdaqaigjgilddeghipnotjgimoglerpbttqjeadfmhgialdcftt");
        Mapir.getInstance(this, "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImU5MmI2Y2U3YjY1NTBjZjA4ZDc4NjA5ZDUwMjIwMmQ5NTFlYTU1YjBjYTI2M2JmYzNlODcyNjc2YTU5OGU5OTQ3OWM1NzFmNWRkZTNmOTgwIn0.eyJhdWQiOiJteWF3ZXNvbWVhcHAiLCJqdGkiOiJlOTJiNmNlN2I2NTUwY2YwOGQ3ODYwOWQ1MDIyMDJkOTUxZWE1NWIwY2EyNjNiZmMzZTg3MjY3NmE1OThlOTk0NzljNTcxZjVkZGUzZjk4MCIsImlhdCI6MTUzMjk1NDQ1NiwibmJmIjoxNTMyOTU0NDU2LCJleHAiOjE1MzI5NTgwNTYsInN1YiI6IiIsInNjb3BlcyI6WyJiYXNpYyIsImVtYWlsIl19.wbWGkA7aQpYsWN8gOMMKyTH2dcst5qVVymDc0-rI6nxwswp3QNa2fMpkdING897HH4PDlqLjWaLzWsuxk5Z1pGjuRmaCns9fmvuM7-x2ZGcj3067EHuHyDrtUVEvPXkPgFvCzNiPNoVGTJ7o14JV44Ag02-lCANjsy0AT4RK4z2afaQvykCE7FIbNdp9VUcfuEBSvT_D9x26aYKkAmPSmJrxDtX5MeAcnGDYpD8f2tHz2oaxbo8wi97wvTn-KVR-gmzzPXeiR_e5HwDfPeXZFY4pjB_KMwRhLr0L8Z05CMfgsNkwUeKTCBwvUMM6r9pXDhf_scLFQM9N7FgZeTW_PQ");
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);
//        AndroidNetworking.initialize(getApplicationContext(), getUnsafeOkHttpClient());
        AndroidNetworking.setUserAgent("Mozilla/5.0(X11; Ubuntu; Linux x86_64;rv:53.0)Gecko/20100101 Firefox/53.0");
        AndroidNetworking.setParserFactory(new GsonParserFactory(gson));


        sContext = getApplicationContext();
        super.onCreate();


        DialogBuilderConfig.builder()
                .setActionFontPath("fonts/iran_san.ttf")
                .setTitleFontPath("fonts/iran_san.ttf")
                .setMessageFontPath("fonts/iran_san.ttf");

        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> handleUncaughtException(thread, e));
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        //e.printStackTrace(); // not all Android versions will print the stack trace automatically

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string

        //postToServer(sStackTrace);

        Intent intent = new Intent(getContext(), ExceptionActivity.class);
        intent.putExtra("StackTrace", sStackTrace);
        startActivity(intent);

        System.exit(1); // kill off the crashed app
    }


    private String extractLogToFile() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = Environment.getExternalStorageDirectory() + "/" + "MyApp/";
        String fullName = path;

        // Extract to file.
        File file = new File(fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
            writer.write("Device: " + model + "\n");
            writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        } catch (IOException e) {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        return fullName;
    }

    private OkHttpClient getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient = okHttpClient.newBuilder()
                    .sslSocketFactory(sslSocketFactory)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES)
                    .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}







