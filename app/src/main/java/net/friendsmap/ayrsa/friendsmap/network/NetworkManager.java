package net.friendsmap.ayrsa.friendsmap.network;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.friendsmap.ayrsa.friendsmap.FriendsMap;
import net.friendsmap.ayrsa.friendsmap.Models.Token;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager<T> {


    private static NetworkManager builder;
    private String mUrl;
    private String mTag;
    private Object mObject;
    private Map<String, String> parameterMap;

    private NetworkManager() {
        parameterMap = new HashMap<>();
    }

    public static NetworkManager builder() {
        if (builder == null) {
            builder = new NetworkManager();
        }

        return builder;
    }

    public NetworkManager setUrl(String url) {
        mUrl = url;
        return this;
    }

    public NetworkManager setTag(String tag) {
        mTag = tag;
        return this;
    }

    public NetworkManager addApplicationJsonBody(Object object) {
        mObject = object;
        return this;
    }

    public NetworkManager addPathParameter(String key, String value) {
        parameterMap.put(key, value);
        return this;
    }

    public NetworkManager addBodyParameter(String key, String value) {
        parameterMap.put(key, value);
        return this;
    }

    public void post(final TypeToken type, final INetwork iNetwork) {
        if (!GeneralUtils.TokenIsValid(FriendsMap.getContext())) {
            TokenManager.builder().setCallBack(new TokenManager.TokenManagerCallBack() {
                @Override
                public void onTickToken(long millisUntilFinished) {

                }

                @Override
                public void onFinishToken() {

                }

                @Override
                public void onCreateToken(String token) {
                    post(type, iNetwork);
                    return;
                }

                @Override
                public void onErrorToken(String error) {
                    ANError anError = new ANError();
                    anError.setErrorBody(error);
                    iNetwork.onError(anError);
                    return;
                }
            }).createToken();
            return;
        }

        AndroidNetworking.post(mUrl)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Authorization", TokenManager.builder().getToken())
                .addApplicationJsonBody(mObject)
                .setTag(mTag)
                .build()
                .getAsParsed(type, new ParsedRequestListener() {
                    @Override
                    public void onResponse(Object response) {
                        try {
                            iNetwork.onResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (anError.getErrorCode() == 401) {
                            Token token = new Token();
                            token.expires_in = 0;
                            GeneralUtils.writeToken(token, 0, FriendsMap.getContext());
                            post(type, iNetwork);
                            return;
                        }
                        iNetwork.onError(anError);
                    }
                });
    }

    public void get(final TypeToken type, final INetwork iNetwork) {
        if (!GeneralUtils.TokenIsValid(FriendsMap.getContext())) {
            TokenManager.builder().setCallBack(new TokenManager.TokenManagerCallBack() {
                @Override
                public void onTickToken(long millisUntilFinished) {

                }

                @Override
                public void onFinishToken() {

                }

                @Override
                public void onCreateToken(String token) {
                    get(type, iNetwork);
                    return;
                }

                @Override
                public void onErrorToken(String error) {
                    ANError anError = new ANError();
                    anError.setErrorBody(error);
                    iNetwork.onError(anError);
                    return;
                }
            }).createToken();
            return;
        }

        AndroidNetworking.get(mUrl)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Authorization", TokenManager.builder().getToken())
                .setTag(mTag).addPathParameter(parameterMap).build()
                .getAsParsed(type, new ParsedRequestListener() {
                    @Override
                    public void onResponse(Object response) {
                        try {
                            iNetwork.onResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (anError.getErrorCode() == 401) {
                            Token token = new Token();
                            token.expires_in = 0;
                            GeneralUtils.writeToken(token, 0, FriendsMap.getContext());
                            get(type, iNetwork);
                            return;
                        }
                        iNetwork.onError(anError);
                    }
                });


    }


    public void upload(final File file, final INetwork iNetwork) {
        if (!GeneralUtils.TokenIsValid(FriendsMap.getContext())) {
            TokenManager.builder().setCallBack(new TokenManager.TokenManagerCallBack() {
                @Override
                public void onTickToken(long millisUntilFinished) {

                }

                @Override
                public void onFinishToken() {

                }

                @Override
                public void onCreateToken(String token) {
                    upload(file, iNetwork);
                    return;
                }

                @Override
                public void onErrorToken(String error) {
                    ANError anError = new ANError();
                    anError.setErrorBody(error);
                    iNetwork.onError(anError);
                    return;
                }
            }).createToken();
            return;
        }

        AndroidNetworking.upload(mUrl)
                .addMultipartFile("image", file)
                .setTag("uploadTest")
                .addHeaders("Authorization", TokenManager.builder().getToken())
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener((bytesUploaded, totalBytes) -> {
                    // do anything with progress
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            iNetwork.onResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if (error.getErrorCode() == 401) {
                            Token token = new Token();
                            token.expires_in = 0;
                            GeneralUtils.writeToken(token, 0, FriendsMap.getContext());
                            upload(file, iNetwork);
                            return;
                        }
                        iNetwork.onError(error);
                    }
                });

    }

    public void cancel(String tag) {
        AndroidNetworking.cancel(tag);
    }
}
