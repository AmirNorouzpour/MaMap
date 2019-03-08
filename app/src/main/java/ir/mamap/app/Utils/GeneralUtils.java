package ir.mamap.app.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.muddzdev.styleabletoast.StyleableToast;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.BuildConfig;
import ir.mamap.app.Models.Constants;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.SimpleNumber;
import ir.mamap.app.Models.Token;
import ir.mamap.app.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ir.oxima.dialogbuilder.DialogBuilder;

import static android.content.Context.MODE_PRIVATE;
import static ir.mamap.app.Mamap.getContext;

public class GeneralUtils {

    public static SimpleNumber getSimpleNumbers() {
        int n1 = getRandomBetweenRange(1, 100);
        int n2 = getRandomBetweenRange(1, 100);
        int sub = Math.abs(n1 - n2);
        int sum = n1 + n2;
        int subG = Math.abs(sub - sum);
        int n3 = sub * sum * subG;
        SimpleNumber simpleNumber = new SimpleNumber();
        try {
            simpleNumber.n1 = CryptoHelper.encrypt(Integer.toString(n1));
            simpleNumber.n2 = CryptoHelper.encrypt(Integer.toString(n2));
            simpleNumber.n3 = CryptoHelper.encrypt(Integer.toString(n3));
        } catch (Exception ex) {
            //log error
        }
        return simpleNumber;
    }

    public static int getRandomBetweenRange(int min, int max) {
        int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    public static <T> List<T> StringToArray(String s, Class<T[]> clazz) {
        T[] arr = new Gson().fromJson(s, clazz);
        return Arrays.asList(arr); //or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }


    public static void showLoading(AVLoadingIndicatorView avLoadingIndicatorView) {
        if (avLoadingIndicatorView != null) {
            avLoadingIndicatorView.setIndicator("BallPulseIndicator");
            avLoadingIndicatorView.show();
        }
    }


    public static void hideLoading(AVLoadingIndicatorView avLoadingIndicatorView) {
        if (avLoadingIndicatorView != null)
            avLoadingIndicatorView.hide();
    }

    public static void showToast(String text, int length, OutType type) {
        int style = R.style.warning;
        if (type == OutType.Error)
            style = R.style.error;
        if (type == OutType.Success)
            style = R.style.success;

        StyleableToast.makeText(getContext(), text, length, style).show();

    }

    public static String GetIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

    public static String GetVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static int GetVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String GetDeviceInfo() {
        return "MANUFACTURER:" + Build.MANUFACTURER + ";Model:" + Build.MODEL + ";VersionRelease:" + Build.VERSION.RELEASE + ";VersionCode:" + Build.VERSION.SDK_INT;
    }

    public static Date StringToDate(String dateString, String format) {
        if (dateString == null)
            return null;
        if (format == null)
            format = "MM/dd/yyyy HH:mm:ss";
        SimpleDateFormat SDFormat = new SimpleDateFormat(format);
        try {
            Date date = SDFormat.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeToken(Token token, int userId, Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        SharedPreferences.Editor editor = context.getSharedPreferences("token", MODE_PRIVATE).edit();
        editor.putString("accesToken", "bearer " + token.access_token);
        editor.putInt("userId", userId).apply();
        Date expires_in = Calendar.getInstance().getTime();//now
        int numberOfseconds = token.expires_in;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expires_in);
        calendar.add(Calendar.SECOND, numberOfseconds);
        String date = dateFormat.format(calendar.getTime());
        editor.putString("expDate", date);
        editor.apply();
    }

    public static String ReadToken(Context context) {
        SharedPreferences editor = context.getSharedPreferences("token", MODE_PRIVATE);
        String token = editor.getString("accesToken", null);
        return token;
    }

    public static int ReadUserId(Context context) {
        SharedPreferences editor = context.getSharedPreferences("token", MODE_PRIVATE);
        int userId = editor.getInt("userId", 0);
        return userId;
    }

    public static Boolean TokenIsValid(Context context) {
        SharedPreferences editor = context.getSharedPreferences("token", MODE_PRIVATE);
        String expDateString = editor.getString("expDate", null);
        if (expDateString == null)
            return false;
        Date expDate = GeneralUtils.StringToDate(expDateString, null);
        Boolean result = new Date().before(expDate);
        return result;
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public static String ComparativeDate(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return "";
        long diff = date1.getTime() - date2.getTime();

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (minutes < 1 && hours < 1 && days < 1)
            return "چند لحظه پیش";


        if (minutes > 1 && hours < 1 && days < 1)
            return minutes + " دقیقه پیش";


        if (hours > 0 && days < 1)
            return hours + " ساعت " + (minutes - 60 * hours) + " دقیقه پیش";

        if (days >= 1)
            return days + " روز پیش";

        return "چند لحظه پیش";
    }

    public static boolean isSignedIn() {
        Context context = getContext();
        SharedPreferences editor = context.getSharedPreferences("token", MODE_PRIVATE);
        String expDateString = editor.getString("expDate", null);
        Date expDate = new Date();
        if (expDateString != null)
            expDate = GeneralUtils.StringToDate(expDateString, null);
        int userId = editor.getInt("userId", 0);
        if (userId > 0 && new Date().before(expDate)) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(128,
                128, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = 64;
            opt.outHeight = 64;
            return BitmapFactory.decodeResource(context.getResources(), drawableId, opt);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof VectorDrawable) {
                return getBitmap((VectorDrawable) drawable);
            } else {
                throw new IllegalArgumentException("unsupported drawable type");
            }
        }
        return null;
    }

    public static void startPowerSaverIntent(Context context) {
        SharedPreferences settings = context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
        if (!skipMessage) {
            SharedPreferences.Editor editor = settings.edit();
            boolean foundCorrectIntent = false;
            for (Intent intent : Constants.POWERMANAGER_INTENTS) {
                if (isCallable(context, intent)) {
                    foundCorrectIntent = true;

                    DialogBuilder dialogBuilder = new DialogBuilder(context).asAlertDialog(false);
                    dialogBuilder.setMessage(" مامپ جهت ارائه عملکرد صحیح بر روی دستگاه شما نیازمند دسترسی Auto Start می باشد لطفا در تنظیمات گوشی خود این دسترسی را برای مامپ فراهم کنید.");
                    dialogBuilder.setTitle(Build.MANUFACTURER.toUpperCase() + " Auto Start");
                    dialogBuilder.setPositiveButton("اعمال دسترسی", dialog -> {
                        context.startActivity(intent);
                        dialog.dismiss();
                        editor.putBoolean("skipProtectedAppCheck", true);
                        editor.apply();
                    });
                    dialogBuilder.setNegativeButton("بعدا", dialog -> dialog.dismiss());
                    dialogBuilder.show();
                    break;
                }
            }
            if (!foundCorrectIntent) {
                editor.putBoolean("skipProtectedAppCheck", true);
                editor.apply();
            }
        }
    }

    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

}


