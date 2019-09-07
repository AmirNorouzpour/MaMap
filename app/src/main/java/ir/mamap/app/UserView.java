package ir.mamap.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ir.mamap.app.Models.FriendMap;

import static android.content.Context.MODE_PRIVATE;

public class UserView extends LinearLayout {

    @StyleableRes
    int index0 = 0;
    @StyleableRes
    int index1 = 1;
    @StyleableRes
    int index2 = 2;

    TextView dateText;
    TextView speedText;

    TextView directionText;
    TextView geoDataText;
    TextView labelText;

    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _context = context;
        init(context, attrs);
    }

    private Context _context;
    private FriendMap _friendMap;

    public UserView(Context context, FriendMap friendMap) {
        super(context);
        _context = context;
        _friendMap = friendMap;
        inflate(context, R.layout.user_find_view, this);
        initComponents();
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.user_find_view, this);
        int[] sets = {R.attr.artistText, R.attr.trackText};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);
        CharSequence artist = typedArray.getText(index0);
        CharSequence track = typedArray.getText(index1);
        typedArray.recycle();
        initComponents();
    }

    private void initComponents() {
        dateText = findViewById(R.id.date_Text);
        speedText = findViewById(R.id.speed_Text);
        directionText = findViewById(R.id.direction_Text);
        geoDataText = findViewById(R.id.geoData_Text);
        labelText = findViewById(R.id.locationLabel_Text);

        TextView datelabel = findViewById(R.id.date_Label);
        TextView speedLabel = findViewById(R.id.speed_Label);
        TextView directionLabel = findViewById(R.id.direction_Label);
        TextView geoDataLabel = findViewById(R.id.geoData_Label);
        TextView labelLabel = findViewById(R.id.location_Label);
        ImageView qIcon = findViewById(R.id.iconQ);
        ImageView navigationIcon = findViewById(R.id.navigation);
        datelabel.setText("زمان");
        speedLabel.setText("سرعت");
        directionLabel.setText("جهت حرکت");
        geoDataLabel.setText("موقعیت جغرافیایی");
        labelLabel.setText("مکان");
        LinearLayout root = findViewById(R.id.root);
//        ToolTip.Builder builder = new ToolTip.Builder(Mamap.getContext(), labelLabel, root, "شما می توانید برای مکان های روی نقشه نام انتخاب کنید", ToolTip.GRAVITY_CENTER);
//        ToolTipsManager mToolTipsManager = new ToolTipsManager();
//        labelLabel.setOnClickListener(v -> mToolTipsManager.show(builder.build()));
//        qIcon.setOnClickListener(v -> mToolTipsManager.show(builder.build()));
        navigationIcon.setOnClickListener(v -> OpenInMap());
    }

    private void OpenInMap() {
        SharedPreferences sharedPreferences = Mamap.getContext().getSharedPreferences("UserLoc", MODE_PRIVATE);
        if (sharedPreferences.getString("UserLocLat", "") != "") {
            String lat = sharedPreferences.getString("UserLocLat", "");
            String lon = sharedPreferences.getString("UserLocLon", "");
            Uri uri = Uri.parse("geo:" + lat + "," + lon + "?q=" + Uri.encode(_friendMap.getLatitude() + "," + _friendMap.getLongitude() + "(" + _friendMap.getNickName() + ")"));
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            _context.startActivity(intent);
        }
    }


    public void setDateText(CharSequence dateText) {
        this.dateText.setText(dateText);
    }

    public void setSpeedText(CharSequence speedText) {
        this.speedText.setText(speedText);
    }

    public void setDirectionText(CharSequence directionText) {
        this.directionText.setText(directionText);
    }

    public void setGeoDataText(CharSequence geoDataText) {
        this.geoDataText.setText(geoDataText);
    }

    public void setLabelText(CharSequence labelText) {
        this.labelText.setText(labelText);
    }

}