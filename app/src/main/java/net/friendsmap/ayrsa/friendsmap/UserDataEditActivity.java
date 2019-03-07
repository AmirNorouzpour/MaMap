package net.friendsmap.ayrsa.friendsmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Models.UserDataMenu;
import net.friendsmap.ayrsa.friendsmap.Models.ClientDataNonGeneric;
import net.friendsmap.ayrsa.friendsmap.Models.DeviceInformation;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

public class UserDataEditActivity extends AppCompatActivity {


    TextView textViewParameter;
    UserDataMenu data;
    Button SaveBtn;
    AVLoadingIndicatorView loadingIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_edit);

        Toolbar mToolbar = findViewById(R.id.m_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        textViewParameter = findViewById(R.id.ParamTxt);
        SaveBtn = findViewById(R.id.SaveBtn);

        Typeface baseFont = Typeface.createFromAsset(UserDataEditActivity.this.getAssets(), "fonts/iran_san.ttf");
        textViewParameter.setTypeface(baseFont);
        mToolbar.setNavigationOnClickListener(v -> finish());
        SaveBtn.setTypeface(baseFont);

        DefaultBackgeroundDrawable defaultBackgeroundDrawable = new DefaultBackgeroundDrawable();
        defaultBackgeroundDrawable.setCircleColor(Color.parseColor("#12252525"));
        defaultBackgeroundDrawable.setBackgroundColor(Color.parseColor("#fafafa"));
        ConstraintLayout layoutLaunch = findViewById(R.id.layout);
        layoutLaunch.setBackground(defaultBackgeroundDrawable);

        data = (UserDataMenu) getIntent().getSerializableExtra("Data");
        if (data != null) {
            textViewParameter.setText(data.getValue());
            textViewParameter.setHint(data.getTitle());
            textViewParameter.setEnabled(data.getEditable());
        }

        loadingIndicatorView = findViewById(R.id.avi);
        SaveBtn.setOnClickListener(v ->
        {
            SaveBtn.setVisibility(View.GONE);
            GeneralUtils.showLoading(loadingIndicatorView);
            SendToServer();
        });

    }

    private void SendToServer() {

        UserDataMenu userDataMenu = new UserDataMenu();
        String value = textViewParameter.getText().toString();
        if (value == null || value.length() == 0) {
            String title = data.getTitle();
            GeneralUtils.showToast("لظفا مقدار " + title + " را وراد کنید", Toast.LENGTH_SHORT, OutType.Error);
            SaveBtn.setVisibility(View.VISIBLE);
            return;
        }
        userDataMenu.setValue(value);
        userDataMenu.setDataType(data.getDataType());
        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/user/UpdateUserAccount")
                .addApplicationJsonBody(userDataMenu)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {

                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        if (data.getMsg() != null)
                            GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        SaveBtn.setVisibility(View.VISIBLE);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (data.getOutType() == OutType.Success)
                        {
                            finish();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }
                });

    }
}
