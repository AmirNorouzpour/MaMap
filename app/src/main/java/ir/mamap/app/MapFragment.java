package ir.mamap.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.location.LocationEngineListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.FriendMap;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;
import ir.map.sdk_map.annotations.Marker;
import ir.map.sdk_map.annotations.MarkerOptions;
import ir.map.sdk_map.camera.CameraUpdateFactory;
import ir.map.sdk_map.geometry.LatLng;
import ir.map.sdk_map.location.LocationComponent;
import ir.map.sdk_map.maps.MapirMap;
import ir.map.sdk_map.maps.SupportMapFragment;
import ir.oxima.dialogbuilder.DialogBuilder;


public class MapFragment extends Fragment implements LocationEngineListener {

    public static final int FOCUSED_ZOOM_LEVEL = 15;
    private FriendMap _friendMap = null;
    LinearLayout UpdateLocationLayout;
    TextView StatusTxt;
    private MapirMap mapirMap;
    private Marker friendMarker;

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
        UpdateLocationLayout = view.findViewById(R.id.UpdateLocation);
        LinearLayout UpdateLocationParent = view.findViewById(R.id.UpdateLocationParent);
        MenuActivity menuActivity = (MenuActivity) getActivity();
        Typeface baseFont = Typeface.createFromAsset(menuActivity.getAssets(), "fonts/iran_san.ttf");
        StatusTxt.setTypeface(baseFont);
        UpdateLocationLayout.setOnClickListener(v -> {
            StatusTxt.setText("در حال پیدا کردن موقعیت جدید");
//                        final Handler handler = new Handler();
//                        handler.postDelayed(() -> {
//                            SetText(0, true);
//                        }, 15000);
            if (_friendMap != null)
                GetUserLocation(_friendMap.getUserId(), true, true);
            else UpdateLocationParent.setVisibility(View.GONE);
        });
        return view;
    }

    private Location _location;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FloatingActionButton fab = view.findViewById(R.id.FindMe);
        fab.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        fab.setOnClickListener(v -> {
            if (_location != null)
                mapirMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_location.getLatitude(), _location.getLongitude()), FOCUSED_ZOOM_LEVEL));
        });
        UpdateFriendLocation(_friendMap, true);
    }

    boolean isFirstTime = true;

    private void UpdateFriendLocation(FriendMap friendMap, boolean isInit) {
        if (friendMarker != null)
            friendMarker.remove();
        MenuActivity menuActivity = (MenuActivity) getActivity();

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.myMapView);
        fragment.getMapAsync(mapirMap -> {

                    if (mapirMap != null) {
                        //region get permissions
                        if (ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(menuActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        if (ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(menuActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        //endregion get permissions

                        if (ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(menuActivity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            LocationComponent component = mapirMap.getLocationComponent();
                            component.activateLocationComponent(menuActivity);
                            component.setLocationComponentEnabled(true);
                            if (component.getLocationEngine() != null) {
                                component.getLocationEngine().addLocationEngineListener(this);
                            }
                            mapirMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(new LatLng(component.getLastKnownLocation().getLatitude(),
                                            component.getLastKnownLocation().getLongitude()), FOCUSED_ZOOM_LEVEL));
                        }

                    }

                    if (friendMap != null) {
                        if (friendMap.isNotAvailable()) {
                            GeneralUtils.showToast("کاربر مورد نظر از دسترس خارج شده است", Toast.LENGTH_LONG, OutType.Warning);
                            return;
                        }
                        LatLng userLoc = new LatLng(friendMap.getLatitude(), friendMap.getLongitude());
                        friendMarker = mapirMap.addMarker(new MarkerOptions().position(userLoc).title(friendMap.getNickName()).snippet(friendMap.getNickName()));


                        mapirMap.setOnMarkerClickListener(v -> showUser(v));
                        mapirMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, FOCUSED_ZOOM_LEVEL));
                        if (isInit) {
                            StatusTxt.setText("در حال پیدا کردن موقعیت جدید");
//                       final Handler handler = new Handler();
//                        handler.postDelayed(() -> {
//                            SetText(0, true);
//                        }, 15000);
                            GetUserLocation(friendMap.getUserId(), true, false);
                        }

                    }
                });


    }


    private boolean showUser(Marker marker) {

        UserView myView = new UserView(getContext(), _friendMap);
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
        dialogBuilder.setMessage(_friendMap.getNickName());

//        dialogBuilder.setMessage(_friendMap.getNickName() + " با سرعت " + speed + " KM/H " + date + " در مختصات مشخص شده بوده است");

        dialogBuilder.show();
        //GeneralUtils.showToast(marker.getPosition().getLatitude() + " " + marker.getPosition().getLongitude(), Toast.LENGTH_LONG, OutType.Success);
        return true;
    }

    private void GetUserLocation(int userId, boolean withTracking, boolean setTimer) {
        String gd = GenerateData();
        String data = null;
        try {
            data = URLEncoder.encode(gd, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/Tracking/GetUserMapData")
                .addQueryParameter("friendId", String.valueOf(userId))
                .addQueryParameter("withTracking", Boolean.toString(withTracking))
                .addQueryParameter("data", data)
                .addQueryParameter("sT", Boolean.toString(setTimer))
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
                            GetUserExpLong();
                            UpdateFriendLocation(friendMap, false);
                        }
                    }


                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }

                }, getActivity());
    }

    private void GetUserExpLong() {
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/GetUserExp")
                .get(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {
                    @Override
                    public void onResponse(ClientDataNonGeneric response) {
                        if (response.getOutType() == OutType.Success && response.getTag() != null) {
                            String expStrEnc = (String) response.getTag();
                            double exp = 0;
                            try {
                                exp = Double.parseDouble(CryptoHelper.decrypt(expStrEnc));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            SetText((int) exp, true);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }

                }, getActivity());
    }

    CountDownTimer timer;

    private void SetText(int exp, boolean startTimer) {
        if (exp > 0) {
//        StatusTxt.setTextColor(Color.parseColor("#1769aa"));
            if (startTimer) {
                cancelTimer();
                timer = new CountDownTimer(exp, 1000) {

                    public void onTick(long millisUntilFinished) {
                        if (millisUntilFinished / 1000 > 60) {
                            int min = (int) millisUntilFinished / 60000;
                            int sec = (int) millisUntilFinished % 60000;
                            String secStr = sec > 9001 ? sec / 1000 + "" : "0" + sec / 1000;
                            StatusTxt.setText("بروزرسانی : " + min + ":" + secStr);
                        } else
                            StatusTxt.setText("بروزرسانی : " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        StatusTxt.setText("بروزرسانی");
                    }

                }.start();
            }
        } else StatusTxt.setText("بروزرسانی");

    }

    void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }

    private String GenerateData() {
        GPSTracker gps = new GPSTracker(Mamap.getContext());
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
        GetUserLocation(Integer.parseInt(target), false, false);
    }


    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (isFirstTime && _friendMap == null) {
                isFirstTime = false;
                mapirMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), FOCUSED_ZOOM_LEVEL));
            }
            _location = location;
        }

    }
}
