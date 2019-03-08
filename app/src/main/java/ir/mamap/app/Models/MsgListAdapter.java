package ir.mamap.app.Models;

import android.content.Context;
import android.graphics.Typeface;
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

        Typeface baseFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/iran_san.ttf");
        titleTxt.setTypeface(baseFont);
        dateTxt.setTypeface(baseFont);
        textViewBody.setTypeface(baseFont);
        btnOk.setTypeface(baseFont);
        btnCancel.setTypeface(baseFont);
        view.setTag(currentItem);
        titleTxt.setText(currentItem.getTitle());
        textViewBody.setText(currentItem.getBody());

        Date mDate = GeneralUtils.StringToDate(currentItem.getInsertDateTime(), "yyyy-MM-dd'T'HH:mm:ss");
        Date currentTime = Calendar.getInstance().getTime();
        view.setTag(currentItem);
        String date = GeneralUtils.ComparativeDate(currentTime, mDate);
        dateTxt.setText(date);

        if (currentItem.getExpireDateTime() != null) {
            Date eDate = GeneralUtils.StringToDate(currentItem.getExpireDateTime(), "yyyy-MM-dd'T'HH:mm:ss");
            if (eDate.before(currentTime) || currentItem.getHideButtons()) {
                HideButtons();
            }
        }

        if (currentItem.getTag() == 0) {
            HideButtons();
        }

        btnOk.setOnClickListener(v -> {
            sendRes(currentItem.getId(), currentItem.getTag(), 1);
            HideButtons();

        });
        btnCancel.setOnClickListener(v -> {
            sendRes(currentItem.getId(), currentItem.getTag(), 0);
            HideButtons();
        });

        if (currentItem.isSeen())
            textViewMsgNew.setVisibility(View.GONE);
        return view;

    }

    private void HideButtons() {
        btnOk.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
    }

    private void sendRes(int msgId, int mapId, int result) {
        FriendResponse response = new FriendResponse();
        response.setAccept(result);
        response.setMsgId(msgId);
        response.setMapId(mapId);
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
