package ir.mamap.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

public class RulesActivity extends AppCompatActivity {
    AVLoadingIndicatorView loadingIndicatorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        loadingIndicatorView = findViewById(R.id.avi);
        TextView toolbar_title = findViewById(R.id.toolbar_title);

        Toolbar mToolbar = findViewById(R.id.m_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());

        int key = getIntent().getIntExtra("Key", -1);
        if (key == 1) {
            toolbar_title.setText("قوانین و شرایط استفاده");
        } else if (key == 2) {
            toolbar_title.setText("درباره ما");
        }
        GeneralUtils.showLoading(loadingIndicatorView);
        GetData(key);
    }

    private void GetData(int key) {
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/General/GetData/{Key}")
                .addPathParameter("Key", key + "")
                .get(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {
                    @Override
                    public void onResponse(ClientDataNonGeneric response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (response.getOutType() == OutType.Success) {
                            String data = null;
                            try {
                                data = CryptoHelper.decrypt(response.getTag() + "");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            WebView webView = findViewById(R.id.webView);
                            webView.clearCache(true);
                            webView.getSettings();
                            webView.setBackgroundColor(Color.TRANSPARENT);
                            webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
                        }else
                            GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }
                });
    }
}
