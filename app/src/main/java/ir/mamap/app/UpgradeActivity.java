package ir.mamap.app;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import info.hoang8f.android.segmented.SegmentedGroup;

public class UpgradeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    SegmentedGroup segmented2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserConfig.getInstance().init(this, Mamap.getLanguageType());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        segmented2 = (SegmentedGroup) findViewById(R.id.segmented);
        segmented2.setTintColor(getResources().getColor(R.color.colorAccent));
        segmented2.setOnCheckedChangeListener(this);
        addButton(segmented2,"تست پویا",R.id.button_r_1);
        addButton(segmented2,"تست 2",R.id.button_r_2);
        addButton(segmented2,"تست 3",R.id.button_r_3);
//        SegmentedGroup segmented3 = (SegmentedGroup) findViewById(R.id.segmented3);
//        segmented3.setTintColor(Color.parseColor("#FFD0FF3C"), Color.parseColor("#FF7B07B2"));
//
//        SegmentedGroup segmented4 = (SegmentedGroup) findViewById(R.id.segmented4);
//        segmented4.setTintColor(getResources().getColor(R.color.radio_button_selected_color));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.button_r_1:
                Toast.makeText(UpgradeActivity.this, "1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_2:
                Toast.makeText(UpgradeActivity.this, "2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_3:
                Toast.makeText(UpgradeActivity.this, "3", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_4:
                Toast.makeText(UpgradeActivity.this, "4", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_5:
                Toast.makeText(UpgradeActivity.this, "5", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_6:
                Toast.makeText(UpgradeActivity.this, "6", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_7:
                Toast.makeText(UpgradeActivity.this, "7", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_8:
                Toast.makeText(UpgradeActivity.this, "8", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_r_9:
                Toast.makeText(UpgradeActivity.this, "9", Toast.LENGTH_SHORT).show();
                break;
            default:
                // Nothing to do
        }
    }

    private void addButton(SegmentedGroup group, String text, int id) {
        RadioButton radioButton = (RadioButton) UpgradeActivity.this.getLayoutInflater().inflate(R.layout.radio_button_item, null);
        radioButton.setText(text);
        radioButton.setId(id);//R.id.button_r_1
        group.addView(radioButton);
        group.updateBackground();
    }
}
