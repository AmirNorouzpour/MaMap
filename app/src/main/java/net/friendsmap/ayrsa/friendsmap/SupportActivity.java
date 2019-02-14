package net.friendsmap.ayrsa.friendsmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.isapanah.awesomespinner.AwesomeSpinner;
import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Models.ClientDataNonGeneric;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Models.SupportRequest;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;


public class SupportActivity extends AppCompatActivity {

    AVLoadingIndicatorView avLoadingIndicatorView;
    Button btn;
    int type = 0;
    EditText EmailTxt, MsgTxt;
    AwesomeSpinner mySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Toolbar mToolbar = findViewById(R.id.m_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mySpinner = findViewById(R.id.ReqType);
        EmailTxt = findViewById(R.id.EmailTxt);
        MsgTxt = findViewById(R.id.Body);

        if (FriendsMap.User != null)
            EmailTxt.setText(FriendsMap.User.getEmail());

        List<String> data = new ArrayList<>();
        data.add("انتقادات و پیشنهادات");
        data.add("گزارش خطا و مشکل");
        data.add("سوال و درخواست راهنمایی");
        data.add("درخواست همکاری");

        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(SupportActivity.this, R.layout.ax_spinner_item, data);
        mySpinner.setAdapter(categoriesAdapter);
        avLoadingIndicatorView = findViewById(R.id.avi);
        mySpinner.setOnSpinnerItemClickListener((position, itemAtPosition) -> {
            type = position + 1;
        });
        btn = findViewById(R.id.SaveCodeBtn);
        btn.setOnClickListener(v -> SendRequest());
    }

    private void SendRequest() {

        if (type < 1 || type > 4) {
            GeneralUtils.showToast("لطفا نوع درخواست را انتخاب نمائید", Toast.LENGTH_SHORT, OutType.Error);
            mySpinner.requestFocus();
            return;
        }
        if (EmailTxt.getText().toString() == null || EmailTxt.getText().toString().length() == 0) {
            GeneralUtils.showToast("لطفا ایمیل خود را وارد نمایید", Toast.LENGTH_SHORT, OutType.Error);
            EmailTxt.requestFocus();
            return;
        }

        if (MsgTxt.getText().toString() == null || MsgTxt.getText().toString().length() == 0) {
            GeneralUtils.showToast("لطفا پیام خود را وارد نمایید", Toast.LENGTH_SHORT, OutType.Error);
            MsgTxt.requestFocus();
            return;
        }

        SupportRequest request = new SupportRequest();
        request.setIp(GeneralUtils.GetIPAddress(true));
        request.setAppPlatform(3);
        request.setDevice(GeneralUtils.GetDeviceInfo());
        request.setAppVersion(GeneralUtils.GetVersionName());
        request.setRequestType(type);
        request.setUserId(FriendsMap.User.getUserId());
        request.setEmail(EmailTxt.getText().toString());
        request.setMessage(MsgTxt.getText().toString());


        GeneralUtils.showLoading(avLoadingIndicatorView);
        btn.setVisibility(View.GONE);

        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/General/SupportRequest")
                .addApplicationJsonBody(request)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {

                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        GeneralUtils.hideLoading(avLoadingIndicatorView);
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        btn.setVisibility(View.VISIBLE);
                        if (data.getOutType() == OutType.Success) {
                            finish();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.hideLoading(avLoadingIndicatorView);
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        btn.setVisibility(View.VISIBLE);
                    }
                });
    }
}
