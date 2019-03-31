package ir.mamap.app.Models;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;

import ir.mamap.app.Mamap;
import ir.mamap.app.R;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MsgListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<UserMessage> _data;

    public MsgListAdapter(Context mContext, ArrayList<UserMessage> data) {
        this.mContext = mContext;
        this._data = data;
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    Button btnOk, btnCancel;

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.msg_list_item, viewGroup, false);
        }
        Object obj = getItem(i);
        UserMessage currentItem = (UserMessage) obj;

        TextView titleTxt = view.findViewById(R.id.txt_title);
        btnOk = view.findViewById(R.id.btn_ok);
        btnCancel = view.findViewById(R.id.btn_cancel);
        TextView dateTxt = view.findViewById(R.id.txt_date);
        TextView textViewBody = view.findViewById(R.id.txt_body);
        TextView textViewMsgNew = view.findViewById(R.id.msgNew);

        view.setTag(currentItem);
        titleTxt.setText(currentItem.getTitle());
        textViewBody.setText(currentItem.getBody());
        textViewBody.setGravity(Gravity.TOP);
        Date mDate = GeneralUtils.StringToDate(currentItem.getInsertDateTime(), "yyyy-MM-dd'T'HH:mm:ss");
        Date currentTime = Calendar.getInstance().getTime();
        view.setTag(currentItem);
        String date = GeneralUtils.ComparativeDate(currentTime, mDate);
        dateTxt.setText(date);

        if (currentItem.getExpireDateTime() != null) {
            Date eDate = GeneralUtils.StringToDate(currentItem.getExpireDateTime(), "yyyy-MM-dd'T'HH:mm:ss");
            if (eDate.before(currentTime) || currentItem.getHideButtons() || currentItem.isSeen()) {
                HideButtons();
            }
        }

        if (currentItem.getMessageHistoryTypeEnum() == 5 && !currentItem.isSeen()) {
            btnOk.setVisibility(View.VISIBLE);
            btnOk.setText("خوانده شده");
            btnOk.setOnClickListener(v -> {
                sendRes(currentItem.getId(), currentItem.getTag(), 1, currentItem.getMessageHistoryTypeEnum());
                btnOk.setVisibility(View.INVISIBLE);
            });
        }

        if (currentItem.getMessageHistoryTypeEnum() == 1 && !currentItem.isSeen()) {
            btnOk.setVisibility(View.VISIBLE);
            btnOk.setText("پذیرش");
            btnOk.setOnClickListener(v -> {
                sendRes(currentItem.getId(), currentItem.getTag(), 1, currentItem.getMessageHistoryTypeEnum());
                HideButtons();

            });
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("عدم پذیرش");
            btnCancel.setOnClickListener(v -> {
                sendRes(currentItem.getId(), currentItem.getTag(), 0, currentItem.getMessageHistoryTypeEnum());
                HideButtons();
            });
        }


        if (currentItem.isSeen())
            textViewMsgNew.setVisibility(View.GONE);
        return view;

    }

    private void HideButtons() {
        btnOk.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
    }

    private void sendRes(int msgId, int mapId, int result, int type) {
        FriendResponse response = new FriendResponse();
        response.setAccept(result);
        response.setMsgId(msgId);
        response.setMapId(mapId);
        response.setMessageHistoryTypeEnum(type);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/AddFriendResponse")
                .addApplicationJsonBody(response)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {
                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }
                });
    }

}
