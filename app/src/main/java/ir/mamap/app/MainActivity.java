package ir.mamap.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.rubengees.introduction.IntroductionBuilder;
import com.rubengees.introduction.Slide;
import com.rubengees.introduction.interfaces.OnSlideListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.Version;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;
import ir.oxima.dialogbuilder.DialogBuilder;

import static ir.mamap.app.Mamap.getContext;

public class MainActivity extends AppCompatActivity {

    AVLoadingIndicatorView loadingIndicatorView;

    private static final OnSlideListener DEFAULT_ON_SLIDE_LISTENER = new OnSlideListener() {
        @Override
        public void onSlideInit(int position, @Nullable TextView title, @NonNull ImageView image,
                                @Nullable TextView description) {
//            if (position % 3 == 1) {
//                Glide.with(image.getContext())
//                        .load(R.drawable.image3)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .into(image);
//            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserConfig.getInstance().init(this, Mamap.getLanguageType());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ApplicationCrashHandler.installHandler();
        Typeface baseFont = Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/iran_san.ttf");
        DefaultBackgeroundDrawable defaultBackgeroundDrawable = new DefaultBackgeroundDrawable();
        defaultBackgeroundDrawable.setCircleColor(Color.parseColor("#12252525"));
        defaultBackgeroundDrawable.setBackgroundColor(Color.parseColor("#fafafa"));
        ConstraintLayout layoutLaunch = findViewById(R.id.layoutLaunch);
        layoutLaunch.setBackground(defaultBackgeroundDrawable);
        TextView versionTxt = findViewById(R.id.versionTxt);
        loadingIndicatorView = findViewById(R.id.avi);
        versionTxt.setText(" نسخه " + "آزمایشی");//GeneralUtils.GetVersionName()

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserGuide", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("UserIntroPassed", false)) {
            new IntroductionBuilder(MainActivity.this)
                    .withSlides(generateSlides())
                    .withTitleTypeface(baseFont)
                    .withOnSlideListener(DEFAULT_ON_SLIDE_LISTENER)
                    .withDescriptionTypeface(baseFont)
                    .introduceMyself();
        } else {
            CheckVersion();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserGuide", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("UserIntroPassed", true).apply();
        CheckVersion();
    }


    private List<Slide> generateSlides() {
        List<Slide> result = new ArrayList<>();

        result.add(new Slide()
                .withTitle("مامَپ")
                .withDescription("مامَپ سرویسی است برای به اشتراک گذاری موقعیت مکانی با عزیزانتان")
                .withColorResource(R.color.colorAccent)
                .withTitleSize(34f)
                .withImage(R.drawable.ic_my_location_white_24dp)
        );

        result.add(new Slide()
                .withTitle("رایگان")
                .withDescription("امکانات پایه مامَپ رایگان است و نیازی به پرداخت هزینه برای آن نیست")
                .withTitleSize(34f)
                .withColorResource(R.color.colorLightGreen)
                .withImage(R.drawable.ic_coins)
        );

        result.add(new Slide()
                .withTitle("بروزرسانی")
                .withDescription("امکانات مامَپ مرتبا بروزرسانی می شوند، منتظر امکانت جدید و جالب انگیر مامَپ باشید")
                .withTitleSize(34f)
                .withColorResource(R.color.colorPurple)
                .withImage(R.drawable.ic_growth)
        );

        result.add(new Slide()
                .withTitle("پشتیبانی")
                .withDescription("برای پاسخگویی به سوالات و یا انتقادات خود ما همیشه در بخش ارتباط با ما حاضر هستیم")
                .withTitleSize(34f)
                .withColorResource(R.color.colorGreenBlue)
                .withImage(R.drawable.ic_conversation)
        );


        return result;
    }


    private void CheckVersion() {
        GeneralUtils.showLoading(loadingIndicatorView);
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
                        // GeneralUtils.hideLoading(loadingIndicatorView);
                        if (anError.getErrorCode() == 0) {
                            Intent intent = new Intent(MainActivity.this, ExceptionActivity.class);
                            intent.putExtra("ANError", anError);
                            startActivity(intent);
                        }
                    }
                }, MainActivity.this);
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
