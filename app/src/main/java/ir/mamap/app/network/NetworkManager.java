package ir.mamap.app.network;


import android.content.Context;
import android.support.annotation.NonNull;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.reflect.TypeToken;

import ir.mamap.app.Mamap;
import ir.mamap.app.Models.Token;
import ir.mamap.app.Utils.GeneralUtils;
import ir.oxima.dialogbuilder.DialogBuilder;

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
    private Map<String, String> parameterQueryMap;
    private boolean _value = false;

    private NetworkManager() {
        parameterMap = new HashMap<>();
        parameterQueryMap = new HashMap<>();

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

    public NetworkManager addQueryParameter(String key, String value) {
        parameterQueryMap.put(key, value);
        return this;
    }

    public NetworkManager addBodyParameter(String key, String value) {
        parameterMap.put(key, value);
        return this;
    }

    public NetworkManager isQueryVersion(boolean value) {
        _value = value;
        return this;
    }


    public void post(final TypeToken type, final INetwork iNetwork, Context context) {
        if (!GeneralUtils.TokenIsValid(Mamap.getContext())) {
            TokenManager.builder().setCallBack(new TokenManager.TokenManagerCallBack() {
                @Override
                public void onTickToken(long millisUntilFinished) {

                }

                @Override
                public void onFinishToken() {

                }

                @Override
                public void onCreateToken(String token) {
                    post(type, iNetwork, context);
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
                            GeneralUtils.writeToken(token, -1, Mamap.getContext());
//                            post(type, iNetwork, context);
                            return;
                        }
                        iNetwork.onError(anError);
                        ShowErrorDialog(type, iNetwork, context, ReqType.Post);
                    }
                });
    }

    public void get(final TypeToken type, final INetwork iNetwork, Context context) {
        if (!GeneralUtils.TokenIsValid(Mamap.getContext())) {
            TokenManager.builder().setCallBack(new TokenManager.TokenManagerCallBack() {
                @Override
                public void onTickToken(long millisUntilFinished) {

                }

                @Override
                public void onFinishToken() {

                }

                @Override
                public void onCreateToken(String token) {
                    get(type, iNetwork, context);
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

        ANRequest request = AndroidNetworking.get(mUrl)
                .setPriority(Priority.MEDIUM).addPathParameter(parameterMap).addQueryParameter(parameterQueryMap)
                .addHeaders("Authorization", TokenManager.builder().getToken())
                .setTag(mTag).build();


        request.getAsParsed(type, new ParsedRequestListener() {
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
                    GeneralUtils.writeToken(token, -1, Mamap.getContext());
//                    get(type, iNetwork, context);

                    return;
                }
                ShowErrorDialog(type, iNetwork, context, ReqType.Get);
                iNetwork.onError(anError);
            }
        });


    }

    enum ReqType {
        Post,
        Get
    }

    private void ShowErrorDialog(final TypeToken type, INetwork iNetwork, Context context, ReqType reqType) {
        DialogBuilder dialogBuilder = getDialogBuilder(context);
        dialogBuilder.setPositiveButton("تلاش مجدد", dialog -> {
            try {
                dialogBuilder.dismiss();
                switch (reqType) {
                    case Post:
                        post(type, iNetwork, context);
                        break;
                    case Get:
                        get(type, iNetwork, context);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        dialogBuilder.show();
    }

    private void ShowErrorDialog(final File file, INetwork iNetwork, Context context) {
        DialogBuilder dialogBuilder = getDialogBuilder(context);
        dialogBuilder.setPositiveButton("تلاش مجدد", dialog -> {
            try {
                upload(file, iNetwork, context);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        dialogBuilder.show();
    }

    @NonNull
    private DialogBuilder getDialogBuilder(Context context) {
        DialogBuilder dialogBuilder = new DialogBuilder(context).asAlertDialog(false);
        dialogBuilder.setMessage("متاسفانه امکان برقراری ارتباط وجود ندارد، ارتباط دستگاه خود با اینترنت را بررسی نمائید");
        dialogBuilder.setTitle("خطا");
        return dialogBuilder;
    }


    public void upload(final File file, final INetwork iNetwork, Context context) {
        if (!GeneralUtils.TokenIsValid(Mamap.getContext())) {
            TokenManager.builder().setCallBack(new TokenManager.TokenManagerCallBack() {
                @Override
                public void onTickToken(long millisUntilFinished) {

                }

                @Override
                public void onFinishToken() {

                }

                @Override
                public void onCreateToken(String token) {
                    upload(file, iNetwork, context);
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
                            GeneralUtils.writeToken(token, -1, Mamap.getContext());
//                            upload(file, iNetwork, context);
                            return;
                        }
                        ShowErrorDialog(file, iNetwork, context);
                        iNetwork.onError(error);
                    }
                });

    }

    public void cancel(String tag) {
        AndroidNetworking.cancel(tag);
    }
}
