package ir.mamap.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.codesgood.views.JustifiedTextView;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.SearchResultAdapter;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import java.util.ArrayList;

public class AddUserActivity extends AppCompatActivity {

    Button searchBtn;
    ListView mList;
    TextView notFound, paramTxt;
    JustifiedTextView NoticeTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        searchBtn = findViewById(R.id.SearchBtn);
        mList = findViewById(R.id.list_view);
        paramTxt = findViewById(R.id.ParamTxt);
        notFound = findViewById(R.id.NotFoundImg);
        NoticeTxt = findViewById(R.id.NoticeTxt);
        Typeface baseFont = Typeface.createFromAsset(AddUserActivity.this.getAssets(), "fonts/iran_san.ttf");
        NoticeTxt.setTypeface(baseFont);
        paramTxt.setTypeface(baseFont);
        searchBtn.setTypeface(baseFont);
        notFound.setTypeface(baseFont);

        Toolbar mToolbar = findViewById(R.id.m_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DefaultBackgeroundDrawable defaultBackgeroundDrawable = new DefaultBackgeroundDrawable();
        defaultBackgeroundDrawable.setCircleColor(Color.parseColor("#12252525"));
        defaultBackgeroundDrawable.setBackgroundColor(Color.parseColor("#fafafa"));
        ConstraintLayout layoutLaunch = findViewById(R.id.layout);
        layoutLaunch.setBackground(defaultBackgeroundDrawable);

        mToolbar.setNavigationOnClickListener(v -> {
            //startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            finish();
        });
//        AddUserActivity.this.getActionBar().setDisplayHomeAsUpEnabled(true);
        searchBtn.setOnClickListener(v -> {
            ArrayList arrayItem = new ArrayList<>();

            SearchResultAdapter mAdapter = new SearchResultAdapter(AddUserActivity.this, arrayItem);
            String param = paramTxt.getText().toString();
            if (param == null || param.length() < 10) {
                GeneralUtils.showToast("شماره کاربر معتبر نیست", Toast.LENGTH_SHORT, OutType.Warning);
                return;
            }
            AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
            GeneralUtils.showLoading(loadingIndicatorView);
            NetworkManager.builder()
                    .setUrl(Mamap.BaseUrl + "/api/user/UserSearch/{param}")
                    .addPathParameter("param", param)
                    .get(new TypeToken<ClientData<ArrayList<User>>>() {
                    }, new INetwork<ClientData<ArrayList<User>>>() {
                        @Override
                        public void onResponse(ClientData<ArrayList<User>> response) {
                            GeneralUtils.hideLoading(loadingIndicatorView);
                            if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                                ArrayList<User> lst = response.getEntity();
                                //List<FriendMap> lst2 = GeneralUtils.StringToArray(response.getTag().toString(), FriendMap[].class);
                                if (lst.size() == 0)
                                    notFound.setVisibility(View.VISIBLE);
                                for (int i = 0; i < lst.size(); i++) {
                                    arrayItem.add(lst.get(i));
                                    notFound.setVisibility(View.GONE);
                                }
                                mList.setVisibility(View.VISIBLE);
                                mList.setAdapter(mAdapter);
                            } else if (response.getOutType() == OutType.Warning) {
                                notFound.setVisibility(View.VISIBLE);
                                mList.setVisibility(View.GONE);
                            } else {
                                GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            GeneralUtils.hideLoading(loadingIndicatorView);
                            GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        }

                    });

        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
