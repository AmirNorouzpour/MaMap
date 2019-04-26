package ir.mamap.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText MobileTxt, PasswordTxt, RePasswordTxt;
    Button RegisterBtn;
    LinearLayout _layout, _formHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        MobileTxt = findViewById(R.id.MobileNumberTxt);
        PasswordTxt = findViewById(R.id.PasswordTxt);
        RePasswordTxt = findViewById(R.id.RePasswordTxt);
        _layout = findViewById(R.id.backgroundLayout);
        _formHolder = findViewById(R.id.formHolder);
        RegisterBtn = findViewById(R.id.RegisterBtn);
        PasswordTxt.requestFocus();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        GeneralUtils.setMargins(_formHolder, 0, (int) (height * 0.35), 0, 0);

        _layout.setBackground(new BackView());

        String mobileNumber = getIntent().getStringExtra("MobileNumber");

        MobileTxt.setText(mobileNumber);

        RegisterBtn.setOnClickListener(v -> {

            RegisterBtn.setVisibility(View.INVISIBLE);

            AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
            GeneralUtils.showLoading(loadingIndicatorView);

            UpdatePassword(mobileNumber, loadingIndicatorView);
        });
    }

    private void UpdatePassword(String mobileNumber, AVLoadingIndicatorView loadingIndicatorView) {
        User user = new User();

        try {
            user.setMobile(CryptoHelper.encrypt(mobileNumber));
            user.setPassword(CryptoHelper.encrypt(PasswordTxt.getText().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/UpdatePassword")
                .addApplicationJsonBody(user)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {

                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        if (data.getOutType() == OutType.Success) {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            intent.putExtra("MobileNumber", mobileNumber);
                            User user = new User();
                            user.setUserId(data.getEntityId());
                            intent.putExtra("User", user);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }
                }, ForgotPasswordActivity.this);
    }
}
