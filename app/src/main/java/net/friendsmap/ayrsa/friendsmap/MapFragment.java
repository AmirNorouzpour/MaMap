package net.friendsmap.ayrsa.friendsmap;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
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

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.FriendMap;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Utils.CryptoHelper;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

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

import static android.content.Context.MODE_PRIVATE;


public class MapFragment extends Fragment {

    public static final int FOCUSED_ZOOM_LEVEL = 15;
    private FriendMap _friendMap = null;
    MaptexMarker friendMarker;

    TextView StatusTxt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        //like if the class is HomeFragment it should have R.layout.home_fragment
        View view = inflater.inflate(R.layout.fragment_map, null);
        //if it is DashboardFragment it should have R.layout.fragment_dashboard
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
    private View _view;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        _view = view;
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
                        GetUserLocation(friendMap.getUserId(), true);
                    }
                }
            }
        });
    }

    private boolean showUser(MaptexMarker marker) {

        UserView myView = new UserView(getContext());


        Date mDate = GeneralUtils.StringToDate(_friendMap.getSeen(), "yyyy-MM-dd'T'HH:mm:ss");
        //String date = ShamsiDateUtil.getShmasiString(mDate);
        Date currentTime = Calendar.getInstance().getTime();
        String date = GeneralUtils.ComparativeDate(currentTime, mDate);

        DialogBuilder dialogBuilder = new DialogBuilder(getActivity()).asBottomSheetDialog(true);
//        dialogBuilder.setTitle(_friendMap.getNickName());
        int speed = (int) ((_friendMap.getSpeed() * 3600) / 1000);
//        dialogBuilder.setMessage(_friendMap.getNickName() + " با سرعت " + speed + " KM/H " + date + " در مختصات مشخص شده بوده است");
        dialogBuilder.setPositiveButton("بروزرسانی", dialog -> {
        });
        dialogBuilder.setNegativeButton("بستن", dialog -> dialog.dismiss());
        dialogBuilder.setCustomView(myView);
        dialogBuilder.show();
        GeneralUtils.showToast(marker.getPosition().latitude + " " + marker.getPosition().longitude, Toast.LENGTH_LONG, OutType.Success);
        return true;
    }

    private void GetUserLocation(int userId, boolean withTracking) {
        SetMyCurrentLocationData();
        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/user/GetUserMapData/{friendId}/{withTracking}")
                .addPathParameter("friendId", String.valueOf(userId))
                .addPathParameter("withTracking", Boolean.toString(withTracking))
                .get(new TypeToken<ClientData<FriendMap>>() {
                }, new INetwork<ClientData<FriendMap>>() {
                    @Override
                    public void onResponse(ClientData<FriendMap> response) {
                        // GeneralUtils.hideLoading(getView().findViewById(R.id.avi));
                        if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                            FriendMap friendMap = response.getEntity();
                            StatusTxt.setText("پیدا شد");
                            UpdateFriendLocation(friendMap, false);
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                StatusTxt.setVisibility(View.INVISIBLE);
                            }, 5000);

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }

                });
    }

    private void SetMyCurrentLocationData() {
        SharedPreferences sharedPreferences = FriendsMap.getContext().getSharedPreferences("UserLoc", MODE_PRIVATE);
        String lat = sharedPreferences.getString("UserLocLat", null);
        String lon = sharedPreferences.getString("UserLocLon", null);
        String speed = sharedPreferences.getString("UserSpeed", null);


        GPSTracker gps = new GPSTracker(FriendsMap.getContext());
        if (gps.getLatitude() != 0) {
            lat = String.valueOf(gps.getLatitude());
            lon = String.valueOf(gps.getLongitude());
            speed = String.valueOf(gps.getSpeed());
        }
        try {
            String data = FriendsMap.User.getUserId() + ",,," + lat + ",,," + lon + ",,," + speed;
            String dataEnc = CryptoHelper.encrypt(data);
            FirebaseService.SendUserLocation(dataEnc.replace("\n", ""), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ShowMsg(String target) {
        StatusTxt.setText("در حال پیدا کردن موقعیت جدید");
        GetUserLocation(Integer.parseInt(target), false);
    }


}
