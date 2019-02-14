package net.friendsmap.ayrsa.friendsmap;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Models.ClientData;
import net.friendsmap.ayrsa.friendsmap.Models.MsgListAdapter;
import net.friendsmap.ayrsa.friendsmap.Models.OutType;
import net.friendsmap.ayrsa.friendsmap.Models.UserMessage;
import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;
import net.friendsmap.ayrsa.friendsmap.network.INetwork;
import net.friendsmap.ayrsa.friendsmap.network.NetworkManager;

import java.util.ArrayList;

public class MessageListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Toolbar mToolbar = findViewById(R.id.m_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText("لیست پیام های شما");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DefaultBackgeroundDrawable defaultBackgeroundDrawable = new DefaultBackgeroundDrawable();
        defaultBackgeroundDrawable.setCircleColor(Color.parseColor("#12252525"));
        defaultBackgeroundDrawable.setBackgroundColor(Color.parseColor("#fafafa"));
        ConstraintLayout layoutLaunch = findViewById(R.id.layout);
        layoutLaunch.setBackground(defaultBackgeroundDrawable);

        mToolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            GetUserMessages();
            pullToRefresh.setRefreshing(false);
        });

        GetUserMessages();
    }

    private void GetUserMessages() {
        ArrayList arrayItem = new ArrayList<>();
        TextView notFound = findViewById(R.id.NotFoundImg);
        ListView mList = findViewById(R.id.list_view);
        MsgListAdapter mAdapter = new MsgListAdapter(MessageListActivity.this, arrayItem);
        AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
        GeneralUtils.showLoading(loadingIndicatorView);
        NetworkManager.builder()
                .setUrl(FriendsMap.BaseUrl + "/api/user/GetUserMessages")
                .get(new TypeToken<ClientData<ArrayList<UserMessage>>>() {
                }, new INetwork<ClientData<ArrayList<UserMessage>>>() {
                    @Override
                    public void onResponse(ClientData<ArrayList<UserMessage>> response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                            ArrayList<UserMessage> lst = response.getEntity();
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
    }
}
