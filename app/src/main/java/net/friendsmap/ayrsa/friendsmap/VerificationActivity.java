package net.friendsmap.ayrsa.friendsmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.ClientDataNonGeneric;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Models.Token;
import net.friendsmap.ayrsa.friendsmap.Models.User;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

public class VerificationActivity extends AppCompatActivity {

    EditText editText1, editText2, editText3, editText4;
    Button sendCodeAgainBtn, okBtn;
    TextView sendCodeText, OTPTxt, remainTimeText;
    ConstraintLayout _layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        editText1 = findViewById(R.id.EditText1);
        editText2 = findViewById(R.id.EditText2);
        editText3 = findViewById(R.id.EditText3);
        editText4 = findViewById(R.id.EditText4);
        editText4 = findViewById(R.id.EditText4);
        sendCodeText = findViewById(R.id.SendCodeText);
        sendCodeAgainBtn = findViewById(R.id.SendCodeAgainBtn);
        _layout = findViewById(R.id.backgroundLayout);
        remainTimeText = findViewById(R.id.RemianTimeTxt);
        okBtn = findViewById(R.id.OkBtn);
        OTPTxt = findViewById(R.id.OTPTxt);

        Typeface baseFont = Typeface.createFromAsset(VerificationActivity.this.getAssets(), "fonts/iran_san.ttf");
        editText1.setTypeface(baseFont);
        editText2.setTypeface(baseFont);
        editText3.setTypeface(baseFont);
        editText4.setTypeface(baseFont);
        sendCodeText.setTypeface(baseFont);
        remainTimeText.setTypeface(baseFont);
        sendCodeAgainBtn.setTypeface(baseFont);
        okBtn.setTypeface(baseFont);
        OTPTxt.setTypeface(baseFont);
        OTPTxt.setTextSize(22);
        okBtn.setTextSize(14);

        String mobileNumber = getIntent().getStringExtra("MobileNumber");
        sendCodeText.setText("کد 4 رقمی ارسال شده به " + mobileNumber + " را وارد کنید");


        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText1.getText().length() > 0) {
                    editText2.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText2, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText2.getText().length() > 0) {
                    editText3.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText3, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText3.getText().length() > 0) {
                    editText4.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText4, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        editText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText1.getText().length() > 0 && editText2.getText().length() > 0 && editText3.getText().length() > 0 && editText4.getText().length() > 0) {
                    AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
                    GeneralUtils.showLoading(loadingIndicatorView);
                    String code = getCode();

                    NetworkManager.builder()
                            .setUrl(FriendsMap.BaseUrl + "/api/VerificationCode/CheckVerificationCode/{Mobile}/{Code}")
                            .addPathParameter("Mobile", mobileNumber)
                            .addPathParameter("Code", code)
                            .get(new TypeToken<ClientDataNonGeneric>() {
                            }, new INetwork<ClientDataNonGeneric>() {

                                @Override
                                public void onResponse(ClientDataNonGeneric data) {
                                    GeneralUtils.hideLoading(loadingIndicatorView);
                                    GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                                    if (data.getOutType() == OutType.Success) {
                                        Intent intent = new Intent(VerificationActivity.this, RegisterActivity.class);
                                        intent.putExtra("MobileNumber", mobileNumber);
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
        });


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        LinearLayout _formHolder = findViewById(R.id.formHolder);
        GeneralUtils.setMargins(_formHolder, 0, (int) (height * 0.4), 0, 0);

        BackView defaultBackgeroundDrawable = new BackView();
        _layout.setBackground(defaultBackgeroundDrawable);


        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                remainTimeText.setText("زمان باقی مانده " + millisUntilFinished / 1000 + " ثانیه ");
                sendCodeAgainBtn.setVisibility(View.GONE);
            }

            public void onFinish() {
                remainTimeText.setText("زمان به پایان رسید!");
                /*sendCodeBtn.setVisibility(View.VISIBLE);
                sendCodeBtn.setText("ارسال مجدد کد");*/
                sendCodeAgainBtn.setVisibility(View.VISIBLE);
                okBtn.setVisibility(View.GONE);
            }
        }.start();

        editText1.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText1, InputMethodManager.SHOW_IMPLICIT);
    }

    @NonNull
    private String getCode() {
        return editText1.getText().toString() + editText2.getText().toString() + editText3.getText().toString() + editText4.getText().toString();
    }
}
