package ir.mamap.app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.Version;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import ir.oxima.dialogbuilder.DialogBuilder;

public class MainActivity extends AppCompatActivity {

    AVLoadingIndicatorView loadingIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//        ApplicationCrashHandler.installHandler();

        DefaultBackgeroundDrawable defaultBackgeroundDrawable = new DefaultBackgeroundDrawable();
        defaultBackgeroundDrawable.setCircleColor(Color.parseColor("#12252525"));
        defaultBackgeroundDrawable.setBackgroundColor(Color.parseColor("#fafafa"));
        ConstraintLayout layoutLaunch = findViewById(R.id.layoutLaunch);
        layoutLaunch.setBackground(defaultBackgeroundDrawable);
        TextView versionTxt = findViewById(R.id.versionTxt);
        loadingIndicatorView = findViewById(R.id.avi);
        GeneralUtils.showLoading(loadingIndicatorView);
        versionTxt.setText(" نسخه " + GeneralUtils.GetVersionName());
        CheckVersion();
    }

    private void CheckVersion() {

        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/Version/CheckVersion/{Platform}/{VersionCode}")
                .addPathParameter("Platform", "3")
                .addPathParameter("VersionCode", GeneralUtils.GetVersionCode() + "")
                .get(new TypeToken<ClientData<Version>>() {
                }, new INetwork<ClientData<Version>>() {
                    @Override
                    public void onResponse(ClientData<Version> response) {
                        if (response.getOutType() == OutType.Success) {
                            if (response.getEntity() != null) {
                                Version version = response.getEntity();
                                ShowVersionDialog(version);
                            } else
                                GoNextActivity();
                        } else
                            GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }
                });
    }


    private void ShowVersionDialog(Version version) {
        DialogBuilder dialogBuilder = new DialogBuilder(MainActivity.this).asAlertDialog(false);
        dialogBuilder.setMessage(version.getMssage());
        dialogBuilder.setTitle(" بروزرسانی " + version.getAppVersionName());
        dialogBuilder.setPositiveButton("بروزرسانی", dialog -> {
            dialog.dismiss();

         //   final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + version.getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(version.getLink()));
                startActivity(browserIntent);
            }
            finish();
        });
        if (!version.getIsNecessary())
            dialogBuilder.setNegativeButton("بعدا", dialog -> {
                dialog.dismiss();
                GoNextActivity();
            });
        dialogBuilder.show();
    }

    private void GoNextActivity() {

        final Handler handler = new Handler();
        if (GeneralUtils.isSignedIn()) {
            handler.postDelayed(() -> {
                Intent intent = new Intent(getBaseContext(), MenuActivity.class);
                intent.putExtra("Type", "Normal");
                startActivity(intent);
            }, 500);
        } else {

            handler.postDelayed(() -> {
                Intent intent = new Intent(getBaseContext(), OnEnterActivity.class);
                intent.putExtra("Type", "Normal");
                startActivity(intent);
            }, 500);
        }
    }
}
