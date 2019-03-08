package ir.mamap.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ExceptionActivity extends AppCompatActivity {
    TextView stactTraceTextView;
    Button detialBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_exception);
        stactTraceTextView = findViewById(R.id.StactTraceText);
        String data = getIntent().getStringExtra("StackTrace");
        stactTraceTextView.setText(data);
        detialBtn = findViewById(R.id.button3);
        detialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stactTraceTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}
