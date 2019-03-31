package ir.mamap.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.muddzdev.styleabletoast.StyleableToast;
import com.wang.avi.AVLoadingIndicatorView;

import java.net.URLEncoder;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

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
                    try {
                        SendCode(mobile, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    StyleableToast.makeText(Mamap.getContext(), "لطفا شماره موبایل خود را وارد کنید", Toast.LENGTH_LONG, R.style.warning).show();
                }
            } else {
                try {
                    SendCode(mobile, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public void SendCode(String mobileNumber, boolean needVerification) throws Exception {
        OnEnterActivity context = OnEnterActivity.this;

        AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
        GeneralUtils.showLoading(loadingIndicatorView);

        String mobileEncoded = CryptoHelper.encrypt(mobileNumber);
        mobileEncoded = URLEncoder.encode(mobileEncoded, "utf-8");

        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/VerificationCode/RequestVerification")
                .addQueryParameter("Mobile", mobileEncoded)
                .addQueryParameter("NeedVerification", Boolean.toString(needVerification))
                .get(new TypeToken<ClientData<User>>() {
                }, new INetwork<ClientData<User>>() {
                    @Override
                    public void onResponse(ClientData<User> response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());

                        if (response.getOutType() == OutType.Error) {
                            return;
                        }

                        if (response.getEntity() != null) {
                            Intent intent = new Intent(context, LoginActivity.class);
                            intent.putExtra("MobileNumber", mobileNumber);
                            User user = response.getEntity();
                            intent.putExtra("User", user);
                            startActivity(intent);
                        } else if (response.getEntityId() > 0) {
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
