package ir.mamap.app.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.reflect.TypeToken;

import ir.mamap.app.Mamap;
import ir.mamap.app.R;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchResultAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<User> _user;
    Button btnSelect;

    public SearchResultAdapter(Context mContext, ArrayList<User> user) {
        this.mContext = mContext;
        this._user = user;
    }

    @Override
    public int getCount() {
        return _user.size();
    }

    @Override
    public Object getItem(int i) {
        return _user.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.search_result_item, viewGroup, false);
        }
        Object s = getItem(i);
        User currentItem = (User) s;

        ImageView imgItemAvatar = view.findViewById(R.id.img_avatar);
        TextView txtItemName = view.findViewById(R.id.txt_name);
        btnSelect = view.findViewById(R.id.btn_Select);
        TextView txtItemMobile = view.findViewById(R.id.txt_Mobile);
        view.setTag(currentItem);
        txtItemName.setText(currentItem.getNickName());
        txtItemMobile.setText(currentItem.getMobile());


        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .circleCropTransform()
                .error(R.drawable.ic_user);
        String user = "", fileName = "";
        try {
            user = CryptoHelper.encrypt(currentItem.getUserId() + "");
            user = URLEncoder.encode(user, "utf-8");
            fileName = URLEncoder.encode(currentItem.getUserId() + "", "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

        String userImgUrl = Mamap.BaseUrl + "/api/user/GetProfilePicture?userId=" + user + "&Filename=" + fileName;
        Glide.with(mContext).load(userImgUrl).apply(options).into(imgItemAvatar);

        if (currentItem.getFriend() != null && currentItem.getFriend().getStatus() == FriendRequestStatus.Pending) {
            btnSelect.setText("منتظر پذیرفتن");
            btnSelect.setEnabled(false);
        } else if (currentItem.getFriend() != null && currentItem.getFriend().getStatus() == FriendRequestStatus.Accepted) {
            btnSelect.setText("پذیرفته شده");
            btnSelect.setEnabled(false);
            btnSelect.setBackgroundColor(view.getResources().getColor(R.color.colorText2));
        } else {
            btnSelect.setOnClickListener(v -> {
                sendRequest(currentItem.getUserId());
            });
        }
        if (currentItem.getFriend() != null && currentItem.getFriend().getBlocked())
            btnSelect.setVisibility(View.INVISIBLE);

        return view;

    }

    private void sendRequest(int friendId) {

        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/AddFriendRequest/{friendUserId}")
                .addPathParameter("friendUserId", String.valueOf(friendId))
                .get(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {
                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        if (data.getOutType() == OutType.Success) {
                            btnSelect.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }
                });

    }

}