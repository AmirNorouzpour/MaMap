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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.Token;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

public class RegisterActivity extends AppCompatActivity {
    EditText MobileTxt, PasswordTxt, RePasswordTxt, UserNameTxt;
    Button RegisterBtn;
    LinearLayout _formHolder, _layout;
    TextView IHaveMeTxt;
    String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserConfig.getInstance().init(this, Mamap.getLanguageType());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MobileTxt = findViewById(R.id.MobileNumberTxt);
        UserNameTxt = findViewById(R.id.UserNameTxt);
        PasswordTxt = findViewById(R.id.PasswordTxt);
        RePasswordTxt = findViewById(R.id.RePasswordTxt);
        _layout = findViewById(R.id.backgroundLayout);
        _formHolder = findViewById(R.id.formHolder);
        IHaveMeTxt = findViewById(R.id.IHaveMeTxt);
        RegisterBtn = findViewById(R.id.RegisterBtn);

        Typeface baseFont = Typeface.createFromAsset(RegisterActivity.this.getAssets(), "fonts/iran_san.ttf");
        MobileTxt.setTypeface(baseFont);
        UserNameTxt.setTypeface(baseFont);
        RePasswordTxt.setTypeface(baseFont);
        PasswordTxt.setTypeface(baseFont);
        RegisterBtn.setTypeface(baseFont);
        IHaveMeTxt.setTypeface(baseFont);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        GeneralUtils.setMargins(_formHolder, 0, (int) (height * 0.35), 0, 0);

        BackView defaultBackgeroundDrawable = new BackView();
        _layout.setBackground(defaultBackgeroundDrawable);

        mobileNumber = getIntent().getStringExtra("MobileNumber");

        MobileTxt.setText(mobileNumber);
        UserNameTxt.requestFocus();
        RegisterBtn.setOnClickListener(v -> {

            //todo : check it
//            if (PasswordTxt.getText().toString() != RePasswordTxt.getText().toString() || PasswordTxt.getText() == null || PasswordTxt.getText().toString().length() == 0) {
//                GeneralUtils.showToast("رمز عبور وارد شده با تکرارش برابر نیست", Toast.LENGTH_SHORT, OutType.Error);
//                return;
//            }

            if (UserNameTxt.getText() == null) {
                GeneralUtils.showToast("نام کاریری خود را انتخاب کنید", Toast.LENGTH_SHORT, OutType.Error);
                return;
            }
            RegisterBtn.setVisibility(View.INVISIBLE);
            User user = new User();
            user.setMobile(mobileNumber);
            user.setPassword(PasswordTxt.getText().toString());
            user.setUsername(UserNameTxt.getText().toString());
            RegisterActivity context = RegisterActivity.this;
            AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
            GeneralUtils.showLoading(loadingIndicatorView);

            AddUser(user, context, loadingIndicatorView);
        });
    }


    private void AddUser(User user, RegisterActivity context, AVLoadingIndicatorView loadingIndicatorView) {
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/AddUser")
                .addApplicationJsonBody(user)
                .post(new TypeToken<ClientData<Token>>() {
                }, new INetwork<ClientData<Token>>() {

                    @Override
                    public void onResponse(ClientData<Token> data) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        RegisterBtn.setVisibility(View.VISIBLE);
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        if (data.getOutType() == OutType.Success) {
                            Token token = data.getEntity();
                            Intent intent = new Intent(context, MenuActivity.class);
                            intent.putExtra("userId", token.userId);
                            GeneralUtils.writeToken(token, token.userId, RegisterActivity.this);
                            String FToken;
                            FToken = FirebaseInstanceId.getInstance().getToken();
                            LoginActivity.sendRegistrationToServer(FToken, RegisterActivity.this);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        RegisterBtn.setVisibility(View.VISIBLE);
                    }
                }, RegisterActivity.this);
    }


}

