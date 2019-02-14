package net.friendsmap.ayrsa.friendsmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.reflect.TypeToken;
import com.muddzdev.styleabletoast.StyleableToast;
import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Models.User;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

public class OnEnterActivity extends AppCompatActivity {
    Button _sendCodeBtn;
    EditText _mobileNumber;
    ConstraintLayout _layout;
    TextView _oTPTxt, _ruleTxt, _helpTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_enter);
        _oTPTxt = findViewById(R.id.OTPTxt);
        _ruleTxt = findViewById(R.id.ruleTxt);
        _helpTxt = findViewById(R.id.helpTxt);
        _ruleTxt.setTextSize(13);
        _oTPTxt.setTextSize(22);
        _helpTxt.setTextSize(16);
        _sendCodeBtn = findViewById(R.id.SendCodeBtn);
        _mobileNumber = findViewById(R.id.MobileNumber);
        _layout = findViewById(R.id.backgroundLayout);

        Typeface baseFont = Typeface.createFromAsset(OnEnterActivity.this.getAssets(), "fonts/iran_san.ttf");
        _oTPTxt.setTypeface(baseFont);
        _mobileNumber.setTypeface(baseFont);
        _ruleTxt.setTypeface(baseFont);
        _sendCodeBtn.setTypeface(baseFont);
        _helpTxt.setTypeface(baseFont);
        String mobileFromIntent = getIntent().getStringExtra("Mobile");
        _mobileNumber.setText(mobileFromIntent);

        _sendCodeBtn.setOnClickListener(view -> {
            String mobile = _mobileNumber.getText().toString();
            //String st = GeneralTools.readFromFile(BeforeLoginActivity.this);
            if (mobileFromIntent == null) {
                if (mobile.trim().length() > 0) {
                    if (mobile.trim().length() != 11 || !mobile.trim().startsWith("09")) {
                        StyleableToast.makeText(OnEnterActivity.this, "شماره موبایل وارد شده معتبر نیست", Toast.LENGTH_LONG, R.style.warning).show();
                        return;
                    }
                    _sendCodeBtn.setVisibility(View.GONE);
                    SendCode(mobile, false);

                } else {
                    StyleableToast.makeText(FriendsMap.getContext(), "لطفا شماره موبایل خود را وارد کنید", Toast.LENGTH_LONG, R.style.warning).show();
                }
            } else {
                SendCode(mobile, true);
            }

        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        LinearLayout _formHolder = findViewById(R.id.formHolder);
        GeneralUtils.setMargins(_formHolder, 0, (int) (height * 0.4), 0, 0);

        BackView backView = new BackView();
        _layout.setBackground(backView);

    }

    public void SendCode(String mobileNumber, boolean needVerification) {
        OnEnterActivity context = OnEnterActivity.this;

        AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
        GeneralUtils.showLoading(loadingIndicatorView);

        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/VerificationCode/RequestVerification/{Mobile}/{NeedVerification}")
                .addPathParameter("Mobile", mobileNumber)
                .addPathParameter("NeedVerification", Boolean.toString(needVerification))
                .get(new TypeToken<ClientData<User>>() {
                }, new INetwork<ClientData<User>>() {
                    @Override
                    public void onResponse(ClientData<User> response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                        if (response.getEntity() != null) {
                            Intent intent = new Intent(context, LoginActivity.class);
                            intent.putExtra("MobileNumber", mobileNumber);
                            User user = response.getEntity();
                            intent.putExtra("User", user);
                            startActivity(intent);
                        } else {
                            GeneralUtils.hideLoading(loadingIndicatorView);
                            Intent intent = new Intent(context, VerificationActivity.class);
                            intent.putExtra("MobileNumber", mobileNumber);
                            intent.putExtra("NeedVerification", response.getEntityId());
                            startActivity(intent);
                        }
                        _sendCodeBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        _sendCodeBtn.setVisibility(View.VISIBLE);
                    }
                });
    }

}