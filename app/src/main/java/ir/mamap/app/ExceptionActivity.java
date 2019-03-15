package ir.mamap.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;

import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.SystemLog;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import static ir.mamap.app.Mamap.getContext;

public class ExceptionActivity extends AppCompatActivity {
    TextView stackTraceTextView;
    Button detialBtn, backBtn, sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_exception);
        stackTraceTextView = findViewById(R.id.StackTraceText);
        String data = getIntent().getStringExtra("StackTrace");
        stackTraceTextView.setText(data);

        detialBtn = findViewById(R.id.button3);
        backBtn = findViewById(R.id.button2);
        backBtn.setOnClickListener(v -> end());
        sendBtn = findViewById(R.id.button1);
        postToServer(data);
        sendBtn.setOnClickListener(v -> end());
        detialBtn.setOnClickListener(view -> {
            if (stackTraceTextView.getVisibility() == View.VISIBLE)
                stackTraceTextView.setVisibility(View.GONE);
            else
                stackTraceTextView.setVisibility(View.VISIBLE);
        });
    }

    private void end() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        finish();
        System.exit(1); // kill off the crashed app
    }


    public void postToServer(String data) {

        SystemLog systemLog = new SystemLog();
        systemLog.setException(data);
        systemLog.setAppVersion(GeneralUtils.GetVersionName());
        systemLog.setIp(GeneralUtils.GetIPAddress(true));
        systemLog.setLogger("Android");
        systemLog.setCallSite(GeneralUtils.GetDeviceInfo());

        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/Error/AddError")
                .addApplicationJsonBody(systemLog)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {

                    @Override
                    public void onResponse(ClientDataNonGeneric res) {
                        // finish();
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);

                    }
                });

    }


}
