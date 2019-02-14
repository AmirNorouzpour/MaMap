package net.friendsmap.ayrsa.friendsmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wang.avi.AVLoadingIndicatorView;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        AVLoadingIndicatorView avLoadingIndicatorView = findViewById(R.id.avi);
        avLoadingIndicatorView.show();
    }
}
