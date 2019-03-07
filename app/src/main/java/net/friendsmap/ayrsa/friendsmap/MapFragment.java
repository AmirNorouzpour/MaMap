package net.friendsmap.ayrsa.friendsmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.FriendMap;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Utils.CryptoHelper;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import ir.map.sdk_common.MaptexLatLng;
import ir.map.sdk_map.wrapper.MaptexBitmapDescriptorFactory;
import ir.map.sdk_map.wrapper.MaptexCameraUpdateFactory;
import ir.map.sdk_map.wrapper.MaptexMap;
import ir.map.sdk_map.wrapper.MaptexMarker;
import ir.map.sdk_map.wrapper.MaptexMarkerOptions;
import ir.map.sdk_map.wrapper.SupportMaptexFragment;
import ir.oxima.dialogbuilder.DialogBuilder;


public class MapFragment extends Fragment {

    public static final int FOCUSED_ZOOM_LEVEL = 15;
    private FriendMap _friendMap = null;
    MaptexMarker friendMarker;

    TextView StatusTxt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        if (getArguments() != null)
            _friendMap = (FriendMap) getArguments().getSerializable("FriendMap");
//        else {
//            GeneralUtils.showToast("برای مشاهده نقشه یکی از دوستان خود را انتخاب کنید", Toast.LENGTH_SHORT, OutType.Warning);
//            return null;
//        }
        StatusTxt = view.findViewById(R.id.StatusTxt);
        MenuActivity menuActivity = (MenuActivity) getActivity();
        Typeface baseFont = Typeface.createFromAsset(menuActivity.getAssets(), "fonts/iran_san.ttf");
        StatusTxt.setTypeface(baseFont);
        return view;
    }

    private Location _location;
    private MaptexMap maptexMap;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FloatingActionButton fab = view.findViewById(R.id.FindMe);
        fab.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        fab.setOnClickListener(v -> {
            if (_location != null)
                maptexMap.animateCamera(MaptexCameraUpdateFactory.newLatLngZoom(new MaptexLatLng(_location.getLatitude(), _location.getLongitude()), FOCUSED_ZOOM_LEVEL));
        });
        UpdateFriendLocation(_friendMap, true);

    }

    boolean isFirstTime = true;

    private void UpdateFriendLocation(FriendMap friendMap, boolean isInit) {
        if (friendMarker != null)
            friendMarker.remove();
        MenuActivity menuActivity = (MenuActivity) getActivity();
        SupportMaptexFragment fragment = (SupportMaptexFragment) getChildFragmentManager().findFragmentById(R.id.myMapView);
        fragment.getMaptexAsync(map -> {
            maptexMap = map;
            if (maptexMap != null) { // Checks if we were successful in obtaining the map
                //region get permissions
                //TODO : This block of code is necessary for android level 6.0 or high
                if (ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(menuActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if (ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(menuActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                //endregion get permissions

                if (ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    maptexMap.setMyLocationEnabled(true);
                maptexMap.setBuildingsEnabled(true);

                maptexMap.setOnMyLocationChangeListener(location -> {
                            if (isFirstTime && _friendMap == null) {
                                isFirstTime = false;
                                maptexMap.animateCamera(MaptexCameraUpdateFactory.newLatLngZoom(new MaptexLatLng(location.getLatitude(), location.getLongitude()), FOCUSED_ZOOM_LEVEL));
                            }
                            _location = location;
                        }
                );

                if (friendMap != null) {
                    if (friendMap.isNotAvailable()) {
                        GeneralUtils.showToast("کاربر مورد نظر از دسترس خارج شده است", Toast.LENGTH_LONG, OutType.Warning);
                        return;
                    }
                    MaptexLatLng userLoc = new MaptexLatLng(friendMap.getLatitude(), friendMap.getLongitude());
                    Bitmap bitmap = GeneralUtils.getBitmap(getContext(), R.drawable.ic_pin);
                    friendMarker = maptexMap.addMarker(new MaptexMarkerOptions()
                            .position(userLoc).title(friendMap.getNickName())
                            .icon(MaptexBitmapDescriptorFactory.fromBitmap(bitmap)));

                    maptexMap.setOnMarkerClickListener(v -> showUser(v));
                    // maptexMap.addPolyline((new MaptexPolylineOptions()).add(userLoc, new MaptexLatLng(userLoc.latitude + 10, userLoc.longitude + 10)));
                    //maptexMap.addCircle(new MaptexCircleOptions().center(userLoc).fillColor(Color.parseColor("#cce6ff")));
                    maptexMap.animateCamera(MaptexCameraUpdateFactory.newLatLngZoom(userLoc, FOCUSED_ZOOM_LEVEL));
                    if (isInit) {
                        StatusTxt.setText("در حال پیدا کردن موقعیت جدید");
                        final Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            SetText();
                        }, 15000);
                        GetUserLocation(friendMap.getUserId(), true);
                    }

                }
            }
        });
    }


    private boolean showUser(MaptexMarker marker) {

        UserView myView = new UserView(getContext(),_friendMap);
        int speed = (int) ((_friendMap.getSpeed() * 3600) / 1000);
        Date mDate = GeneralUtils.StringToDate(_friendMap.getSeen(), "yyyy-MM-dd'T'HH:mm:ss");
        //String date = ShamsiDateUtil.getShmasiString(mDate);
        Date currentTime = Calendar.getInstance().getTime();
        String date = GeneralUtils.ComparativeDate(currentTime, mDate);

        myView.setDateText(date);
        myView.setSpeedText(speed + " کیلومتر بر ساعت ");
        myView.setDirectionText("شمال");
        myView.setGeoDataText(_friendMap.getLatitude() + "  ,  " + _friendMap.getLongitude());

        AndroidNetworking.get("https://map.ir/reverse?lat=" + _friendMap.getLatitude() + "&lon=" + _friendMap.getLongitude())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String loc = response.getString("address_compact");
                            myView.setLabelText(loc);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {

                    }
                });


        DialogBuilder dialogBuilder = new DialogBuilder(getActivity()).asBottomSheetDialog(true);
        dialogBuilder.setCustomView(myView);
        dialogBuilder.setMessage("جزئیات موقعیت کاربر");
        dialogBuilder.setTitle(_friendMap.getNickName());

//        dialogBuilder.setMessage(_friendMap.getNickName() + " با سرعت " + speed + " KM/H " + date + " در مختصات مشخص شده بوده است");
        dialogBuilder.setPositiveButton("بروزرسانی", dialog -> {
        });
        dialogBuilder.setNegativeButton("بستن", dialog -> dialog.dismiss());

        dialogBuilder.show();
        GeneralUtils.showToast(marker.getPosition().latitude + " " + marker.getPosition().longitude, Toast.LENGTH_LONG, OutType.Success);
        return true;
    }

    private void GetUserLocation(int userId, boolean withTracking) {
        String gd = GenerateData();
        String data = null;
        try {
            data = URLEncoder.encode(gd, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/Tracking/GetUserMapData")
                .addQueryParameter("friendId", String.valueOf(userId))
                .addQueryParameter("withTracking", Boolean.toString(withTracking))
                .addQueryParameter("data", data)
                .get(new TypeToken<ClientData<FriendMap>>() {
                }, new INetwork<ClientData<FriendMap>>() {
                    @Override
                    public void onResponse(ClientData<FriendMap> response) {
                        // GeneralUtils.hideLoading(getView().findViewById(R.id.avi));
                        if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                            FriendMap friendMap = response.getEntity();
                            StatusTxt.setText("پیدا شد");
                            _friendMap = friendMap;
                            StatusTxt.setVisibility(View.VISIBLE);
                            UpdateFriendLocation(friendMap, false);
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> SetText(), 5000);

                        }
                    }


                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }

                });
    }

    private void SetText() {
        StatusTxt.setText("جزئیات بیشتر");
        StatusTxt.setTextColor(Color.parseColor("#1769aa"));
        StatusTxt.setOnClickListener(v -> GeneralUtils.showToast("جزئیات بیشتر :)", Toast.LENGTH_SHORT, OutType.Success));
    }

    private String GenerateData() {
        GPSTracker gps = new GPSTracker(FriendsMap.getContext());
//        if (!gps.isGPSEnabled && gps.isNetworkEnabled)
//            SystemClock.sleep(2000);
        double lat = 0, lon = 0, speed = 0;
        if (gps.getLatitude() != 0) {
            lat = gps.getLatitude();
            lon = gps.getLongitude();
            speed = gps.getSpeed();
        }
        gps.stopUsingGPS();
        try {
            String data = "0,,," + lat + ",,," + lon + ",,," + speed + ",,,0";
            String dataEnc = CryptoHelper.encrypt(data);
            return dataEnc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void ShowMsg(String target) {
        GetUserLocation(Integer.parseInt(target), false);
    }


}
