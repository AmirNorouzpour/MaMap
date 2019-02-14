package net.friendsmap.ayrsa.friendsmap;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MyLocationListener implements LocationListener {

    public MyLocationListener() {

    }

    @Override
    public void onLocationChanged(Location loc) {

        loc.getLatitude();
        loc.getLongitude();
        int speed = (int) ((loc.getSpeed() * 3600) / 1000);
        String Text = "My current location is: " +
                "Latitud = " + loc.getLatitude() +
                "Longitud = " + loc.getLongitude() + " Speed = " + speed + " km/h";
//        if (loc.getSpeed() > 0 ) {
//            Toast.makeText(FriendsMap.getContext(), Text, Toast.LENGTH_SHORT).show();
//
//        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(FriendsMap.getContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(FriendsMap.getContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}