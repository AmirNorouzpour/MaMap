package net.friendsmap.ayrsa.friendsmap;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.ClientDataNonGeneric;
import net.friendsmap.ayrsa.friendsmap.Models.DeviceInformation;
import net.friendsmap.ayrsa.friendsmap.Models.LoginViewModel;
import net.friendsmap.ayrsa.friendsmap.Models.LoginViewModelInnerData;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Models.Token;
import net.friendsmap.ayrsa.friendsmap.Models.User;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

public class LoginActivity extends AppCompatActivity {
    EditText MobileNumber, Password;
    Button LoginCodeBtn;
    TextView RememberMeTxt, RuleTxt, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        String mobileNumber = getIntent().getStringExtra("MobileNumber");
        MobileNumber = findViewById(R.id.MobileNumberTxt);
        Password = findViewById(R.id.PasswordTxt);
        MobileNumber.setText(mobileNumber);
        LoginCodeBtn = findViewById(R.id.LoginCodeBtn);
        Password.requestFocus();
        LoginCodeBtn.setOnClickListener(view -> {
            LoginCodeBtn.setVisibility(View.GONE);
            LoginRequest(mobileNumber, Password.getText().toString());

        });
        RememberMeTxt = findViewById(R.id.RememberMeTxt);
        RuleTxt = findViewById(R.id.ruleTxt);
        title = findViewById(R.id.titleTxt);

        Typeface baseFont = Typeface.createFromAsset(LoginActivity.this.getAssets(), "fonts/iran_san.ttf");
        MobileNumber.setTypeface(baseFont);
        LoginCodeBtn.setTypeface(baseFont);
        RuleTxt.setTypeface(baseFont);
        RememberMeTxt.setTypeface(baseFont);
        Password.setTypeface(baseFont);
        title.setTypeface(baseFont);
        title.setTextSize(22);
        RememberMeTxt.setOnClickListener(v ->
        {
            Intent intent = new Intent(LoginActivity.this, OnEnterActivity.class);
            intent.putExtra("Mobile", MobileNumber.getText().toString());
            startActivity(intent);
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        LinearLayout _formHolder = findViewById(R.id.formHolder);
        GeneralUtils.setMargins(_formHolder, 0, (int) (height * 0.4), 0, 0);

        ConstraintLayout _layout = findViewById(R.id.backgroundLayout);
        BackView backView = new BackView();
        _layout.setBackground(backView);
    }

    public void LoginRequest(String mobileNumber, String password) {
        LoginViewModel loginViewModel = new LoginViewModel();
        loginViewModel.Ip = GeneralUtils.GetIPAddress(true);
        loginViewModel.AppPlatform = 3;
        loginViewModel.Os = GeneralUtils.GetDeviceInfo();
        loginViewModel.AppVersion = GeneralUtils.GetVersionName();
        loginViewModel.LoginViewModel = new LoginViewModelInnerData();
        loginViewModel.LoginViewModel.Mobile = mobileNumber;
        loginViewModel.LoginViewModel.Password = password;

        AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
        GeneralUtils.showLoading(loadingIndicatorView);

        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/user/Login")
                .addApplicationJsonBody(loginViewModel)
                .post(new TypeToken<ClientData<Token>>() {
                }, new INetwork<ClientData<Token>>() {

                    @Override
                    public void onResponse(ClientData<Token> data) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (data.getOutType() == OutType.Success) {
                            if (data.getEntity() != null) {
                                Token token = data.getEntity();
                                User user = (User) getIntent().getSerializableExtra("User");
                                FriendsMap.User = user;
                                GeneralUtils.writeToken(token, user.getUserId(), LoginActivity.this);
                                String FToken = null;
                                FToken = FirebaseInstanceId.getInstance().getToken();
                                sendRegistrationToServer(FToken);
                                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                startActivity(intent);
                            } else {
                                GeneralUtils.showToast("مشکلی در پردازش اطلاعات وجود دارد", Toast.LENGTH_LONG, OutType.Error);
                            }
                        } else {
                            GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, OutType.Error);
                        }
                        LoginCodeBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        LoginCodeBtn.setVisibility(View.VISIBLE);
                    }
                });
    }

    public static void sendRegistrationToServer(String token) {

        DeviceInformation deviceInformation = new DeviceInformation();
        deviceInformation.setToken(token);
        deviceInformation.setDeviceName(Build.MODEL);
        deviceInformation.setDeviceId(Build.MODEL);
        String ip = GeneralUtils.GetIPAddress(true);
        deviceInformation.setIp(ip);
        deviceInformation.setOs(GeneralUtils.GetDeviceInfo());
        deviceInformation.setInstalledAppVersion(GeneralUtils.GetVersionName());

        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/user/SetDeviceInformation")
                .addApplicationJsonBody(deviceInformation)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {

                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        if (data.getOutType() == OutType.Error)
                            GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, OutType.Error);
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }
                });

    }
}
