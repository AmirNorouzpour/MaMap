package ir.mamap.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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
import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.Token;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

public class RegisterActivity extends AppCompatActivity {
    EditText MobileTxt, PasswordTxt, RePasswordTxt, UserNameTxt;
    Button RegisterBtn;
    ConstraintLayout _layout;
    LinearLayout _formHolder;
    TextView IHaveMeTxt;
    String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        UserNameTxt.requestFocus();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        GeneralUtils.setMargins(_formHolder, 0, (int) (height * 0.35), 0, 0);

        BackView defaultBackgeroundDrawable = new BackView();
        _layout.setBackground(defaultBackgeroundDrawable);

        mobileNumber = getIntent().getStringExtra("MobileNumber");
        String userName = getIntent().getStringExtra("UserName");
        int needVerification = getIntent().getIntExtra("NeedVerification", 0);

        MobileTxt.setText(mobileNumber);
        UserNameTxt.setText(userName);
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
            User user = new User();
            user.setMobile(mobileNumber);
            user.setPassword(PasswordTxt.getText().toString());
            user.setUsername(UserNameTxt.getText().toString());
            RegisterActivity context = RegisterActivity.this;
            AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
            GeneralUtils.showLoading(loadingIndicatorView);

            if (needVerification == 1) {
                UpdatePassword(loadingIndicatorView);
            } else {
                AddUser(user, context, loadingIndicatorView);
            }
        });
    }

    private void UpdatePassword(AVLoadingIndicatorView loadingIndicatorView) {
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
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
                        GeneralUtils.showToast("امکان ارتباط وجود ندارد", Toast.LENGTH_LONG, OutType.Error);
                    }
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
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        if (data.getOutType() == OutType.Success) {
                            Token token = data.getEntity();
                            Intent intent = new Intent(context, MenuActivity.class);
                            intent.putExtra("userId", token.userId);
                            GeneralUtils.writeToken(token, token.userId, RegisterActivity.this);
                            String FToken = null;
                            FToken = FirebaseInstanceId.getInstance().getToken();
                            LoginActivity.sendRegistrationToServer(FToken);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        GeneralUtils.showToast("امکان ارتباط وجود ندارد", Toast.LENGTH_LONG, OutType.Error);
                    }
                });
    }
}

