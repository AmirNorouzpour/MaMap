package net.friendsmap.ayrsa.friendsmap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import net.friendsmap.ayrsa.friendsmap.Utils.GeneralUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        DefaultBackgeroundDrawable defaultBackgeroundDrawable = new DefaultBackgeroundDrawable();
        defaultBackgeroundDrawable.setCircleColor(Color.parseColor("#12252525"));
        defaultBackgeroundDrawable.setBackgroundColor(Color.parseColor("#fafafa"));
        ConstraintLayout layoutLaunch = findViewById(R.id.layoutLaunch);
        layoutLaunch.setBackground(defaultBackgeroundDrawable);
        TextView versionTxt = findViewById(R.id.versionTxt);
        AVLoadingIndicatorView loadingIndicatorView = findViewById(R.id.avi);
        GeneralUtils.showLoading(loadingIndicatorView);
        versionTxt.setText(" نسخه " + GeneralUtils.GetVersionName());
        GoNextActivity();
    }


    private void GoNextActivity() {

        final Handler handler = new Handler();
        if (GeneralUtils.isSignedIn()) {
            handler.postDelayed(() -> {
                Intent intent = new Intent(getBaseContext(), MenuActivity.class);
                intent.putExtra("Type", "Normal");
                startActivity(intent);
            }, 2000);
        } else {

            handler.postDelayed(() -> {
                Intent intent = new Intent(getBaseContext(), OnEnterActivity.class);
                intent.putExtra("Type", "Normal");
                startActivity(intent);
            }, 2000);
        }
    }
}
