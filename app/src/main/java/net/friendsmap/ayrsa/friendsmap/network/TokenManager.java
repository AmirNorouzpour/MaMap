package net.friendsmap.ayrsa.friendsmap.network;

import android.os.CountDownTimer;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import net.friendsmap.ayrsa.friendsmap.FriendsMap;
import net.friendsmap.ayrsa.friendsmap.Models.SimpleNumber;
import net.friendsmap.ayrsa.friendsmap.Models.Token;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;

public class TokenManager {

    private static TokenManager tokenManager;
    public TokenManagerCallBack callBack;

    public interface TokenManagerCallBack {
        void onTickToken(long millisUntilFinished);

        void onFinishToken();

        void onCreateToken(String token);

        void onErrorToken(String error);
    }

    private TokenManager() {
    }

    public TokenManager setCallBack(TokenManagerCallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public static TokenManager builder() {
        if (tokenManager == null) {
            tokenManager = new TokenManager();
        }
        return tokenManager;
    }

    public void createToken() {
        SimpleNumber simpleNumber = GeneralUtils.getSimpleNumbers();
        AndroidNetworking.post(FriendsMap.BaseUrl + "/api/General/key")
                .addApplicationJsonBody(simpleNumber)
                .setTag("")
                .setPriority(Priority.MEDIUM)
                .build()

                .getAsObject(SimpleNumber.class, new ParsedRequestListener<SimpleNumber>() {
                    @Override
                    public void onResponse(SimpleNumber response) {
                        if (response == null) {
                            if (callBack != null) {
                                callBack.onErrorToken("we have a error to create token!");
                                return;
                            }
                        }
                        SimpleNumber simpleNumberRes = response;
                        AndroidNetworking.post(FriendsMap.BaseUrl + "/GetAxfToken")
                                .addBodyParameter("grant_type", "password")
                                .addBodyParameter("username", simpleNumberRes.d)
                                .addBodyParameter("password", simpleNumberRes.h)
                                .setPriority(Priority.HIGH)
                                .build().getAsObject(Token.class, new ParsedRequestListener<Token>() {

                            @Override
                            public void onResponse(Token token) {
                                if (callBack != null) {
                                    callBack.onCreateToken(token.access_token);
                                }
                                GeneralUtils.writeToken(token, 0, FriendsMap.getContext());
                                return;
                            }

                            @Override
                            public void onError(ANError anError) {
                                callBack.onErrorToken("we have a error to create token!");
                            }
                        });
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (callBack != null) {
                            callBack.onErrorToken(anError.getErrorDetail());
                            return;
                        }
                    }
                });

    }

    public String getToken() {
        return GeneralUtils.ReadToken(FriendsMap.getContext());
    }

//    private void startTimer(int time) {
//        if (countDownTimer != null) {
//            countDownTimer.cancel();
//        }
//        countDownTimer = new CountDownTimer(time * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                if (callBack != null) {
//                    callBack.onTickToken(millisUntilFinished);
//                }
//            }
//
//
//            @Override
//            public void onFinish() {
//                _token = null;
//                if (callBack != null) {
//                    callBack.onFinishToken();
//                }
//            }
//        }.start();
//    }

}
