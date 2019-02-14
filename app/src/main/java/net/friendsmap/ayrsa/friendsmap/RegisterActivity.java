package net.friendsmap.ayrsa.friendsmap;

import android.content.Intent;
import android.content.SharedPreferences;
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

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.ClientDataNonGeneric;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Models.Token;
import net.friendsmap.ayrsa.friendsmap.Models.User;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

public class RegisterActivity extends AppCompatActivity {
    EditText MobileTxt, PasswordTxt, RePasswordTxt, UserNameTxt;
    Button RegisterBtn;
    ConstraintLayout _layout;
    LinearLayout _formHolder;
    TextView IHaveMeTxt;

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

        String mobileNumber = getIntent().getStringExtra("MobileNumber");
        MobileTxt.setText(mobileNumber);
        UserNameTxt.requestFocus();
        RegisterBtn.setOnClickListener(v -> {
            User user = new User();
            user.setMobile(mobileNumber);
            user.setPassword(PasswordTxt.getText().toString());
            user.setUsername(UserNameTxt.getText().toString());
            RegisterActivity context = RegisterActivity.this;
            AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
            GeneralUtils.showLoading(loadingIndicatorView);

            NetworkManager.builder()
                    .setUrl(FriendsMap.BaseUrl + "/api/user/AddUser")
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

        });
    }
}

