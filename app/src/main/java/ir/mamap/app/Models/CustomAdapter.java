package ir.mamap.app.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import ir.mamap.app.Mamap;
import ir.mamap.app.R;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class CustomAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<FriendMap> mItem;

    public CustomAdapter(Context mContext, ArrayList<FriendMap> mItem) {
        this.mContext = mContext;
        this.mItem = mItem;
    }

    @Override
    public int getCount() {
        return mItem.size();
    }

    @Override
    public Object getItem(int i) {
        return mItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);
        }
        Object s = getItem(i);
        FriendMap currentItem = (FriendMap) s;

        ImageView imgItemAvatar = view.findViewById(R.id.user_image);
        TextView txtItemName = view.findViewById(R.id.txt_name);
        TextView txtItemLoc = view.findViewById(R.id.txt_Location);
        Typeface baseFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/iran_san.ttf");
        txtItemLoc.setTypeface(baseFont);
        txtItemName.setTypeface(baseFont);
        TextView txtItemMessage = view.findViewById(R.id.txt_message);
        String distanceText;
        if (currentItem.getStatus() == FriendRequestStatus.Accepted) {
            if (!currentItem.isNotAvailable())
                distanceText = GetDistanceOfTwoLocations(currentItem);
            else
                distanceText = "خارج از دسترس";
            txtItemLoc.setText(distanceText);
        } else {
            txtItemLoc.setText("منتظر پذیرش");
            txtItemLoc.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
        }
        txtItemName.setText(currentItem.getNickName());
        if (!currentItem.isNotAvailable()) {
            Date mDate = GeneralUtils.StringToDate(currentItem.getSeen(), "yyyy-MM-dd'T'HH:mm:ss");
            //String date = ShamsiDateUtil.getShmasiString(mDate);
            Date currentTime = Calendar.getInstance().getTime();
            view.setTag(currentItem);
            String date = GeneralUtils.ComparativeDate(currentTime, mDate);
            txtItemMessage.setText(date);
            txtItemMessage.setTypeface(baseFont);
        }


        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .circleCropTransform()
                .error(R.drawable.ic_user);
        String user = "", fileName = "";
        try {
            user = CryptoHelper.encrypt(currentItem.getUserId() + "");
            user = URLEncoder.encode(user, "utf-8");
            fileName = URLEncoder.encode(currentItem.getFileName(), "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

        String userImgUrl = Mamap.BaseUrl + "/api/user/GetProfilePicture?userId=" + user + "&Filename=" + fileName;
        Glide.with(mContext).load(userImgUrl).apply(options).into(imgItemAvatar);

        return view;

    }

    private String GetDistanceOfTwoLocations(FriendMap currentItem) {
        Location location = new Location("User");
        SharedPreferences sharedPreferencesUser = Mamap.getContext().getSharedPreferences("UserLoc", MODE_PRIVATE);
        String userLat = sharedPreferencesUser.getString("UserLocLat", null);
        String userLon = sharedPreferencesUser.getString("UserLocLon", null);
        if (userLat != null && userLon != null) {
            location.setLatitude(Double.parseDouble(userLat));
            location.setLatitude(Double.parseDouble(userLon));
        } else
            return "";

        float[] result = new float[1];
        Location.distanceBetween(Double.parseDouble(userLat), Double.parseDouble(userLon), currentItem.getLatitude(), currentItem.getLongitude(), result);
        float distance = result[0];
        if (distance > 1000) {
            float d = (distance / 1000);
            return String.format("%.1f", d) + " کیلومتر";
        }
        return (int) distance + " متر";

    }

}