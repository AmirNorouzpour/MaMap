package ir.mamap.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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

import ir.oxima.dialogbuilder.DialogBuilder;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    AVLoadingIndicatorView loadingIndicatorView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, null);

        MenuActivity menuActivity = (MenuActivity) getActivity();
        LinearLayout ExitFromAccount = view.findViewById(R.id.ExitFromAccount);
        Switch switchNotAvailable = view.findViewById(R.id.switchNotAvailable);
        Switch searchTypeMethod = view.findViewById(R.id.SearchTypeMethod);
        ImageView helpImg = view.findViewById(R.id.img_help);
        helpImg.setOnClickListener(v -> OpenSreachTypeHelp());
        loadingIndicatorView = view.findViewById(R.id.avi);
        SharedPreferences userData = menuActivity.getSharedPreferences("UserData", MODE_PRIVATE);

        int searchTypeMethodValue = 1;
        if (Mamap.User != null)
            searchTypeMethodValue = Mamap.User.getSearchMethod();

        searchTypeMethod.setChecked(searchTypeMethodValue == 1);
        searchTypeMethod.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                SetOnServer(!isChecked, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        switchNotAvailable.setChecked(userData.getBoolean("NotAvailable", true));
        switchNotAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                SetOnServer(!isChecked, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ExitFromAccount.setOnClickListener(v -> {

            DialogBuilder dialogBuilder = new DialogBuilder(menuActivity).asAlertDialog(false);
            dialogBuilder.setMessage("آیا از حساب کاربری خود خارج می شوید؟");
            dialogBuilder.setTitle("سوال");
            dialogBuilder.setPositiveButton("بله", dialog -> {
                dialog.dismiss();
                SharedPreferences settings = menuActivity.getSharedPreferences("token", MODE_PRIVATE);
                settings.edit().clear().commit();
                Intent intent = new Intent(menuActivity, OnEnterActivity.class);
                intent.putExtra("Type", "Normal");
                startActivity(intent);
            });

            dialogBuilder.setNegativeButton("خیر", dialog -> dialog.dismiss());

            dialogBuilder.show();

        });

        LinearLayout RulesLayout = view.findViewById(R.id.Rules);
        LinearLayout AboutUsLayout = view.findViewById(R.id.AboutUsLayout);
        LinearLayout SupportLayout = view.findViewById(R.id.SupportLayout);
        LinearLayout UpgradeAccountLayout = view.findViewById(R.id.UpgradeAccount);
        RulesLayout.setOnClickListener(v -> OpenActivity(1, RulesActivity.class));
        AboutUsLayout.setOnClickListener(v -> OpenActivity(2, RulesActivity.class));
        SupportLayout.setOnClickListener(v -> OpenActivity(0, SupportActivity.class));
        UpgradeAccountLayout.setOnClickListener(v -> GeneralUtils.showToast("نسخه آزمایشی محدودیتی ندارد", Toast.LENGTH_SHORT, OutType.Success));

        return view;
    }

    private void OpenSreachTypeHelp() {
        DialogBuilder dialogBuilder = new DialogBuilder(getActivity()).asAlertDialog(true);
        dialogBuilder.setMessage("برای جستجو دو روش وجود دارد در روش همراه با اعلان یک نوتیفیکشن در دستگاه کاربر مقصد نمایان می شود و این روش روش دقیق  تری برای جستجو است، اما اگر با این روش موفق به جستجو نشدید از روش بدون اعلان استفاده کنید.");
        dialogBuilder.setTitle("نوع جستجو");
        dialogBuilder.setPositiveButton("باشه", dialog -> dialog.dismiss());
        dialogBuilder.show();
    }

    private void OpenActivity(int key, Class<?> activity) {
        Intent intent = new Intent(getActivity(), activity);
        intent.putExtra("Key", key);
        startActivity(intent);
    }

    public void SetOnServer(boolean value, int type) throws Exception {
        GeneralUtils.showLoading(loadingIndicatorView);
        ClientDataNonGeneric data = new ClientDataNonGeneric();
        String valueToEncrypt = !value ? "1" : "0";
        valueToEncrypt += ",," + type;
        String dataEnc = CryptoHelper.encrypt(valueToEncrypt);
        data.setTag(dataEnc);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/SetUserData")
                .addApplicationJsonBody(data)
                .post(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {
                    @Override
                    public void onResponse(ClientDataNonGeneric data) {
                        GeneralUtils.showToast(data.getMsg(), Toast.LENGTH_LONG, data.getOutType());
                        if (data.getOutType() == OutType.Success) {
                            SharedPreferences userData = getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = userData.edit();
                            if (type == 2) {
                                editor.putBoolean("NotAvailable", !value);
                                editor.apply();
                            }
                            if (type == 1) {
                                int value1 = !value ? 1 : 0;
                                SetInUser(value1);
                            }
                        }
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                    }
                }, getActivity());

    }

    private void SetInUser(int value1) {
        if (Mamap.User != null)
            Mamap.User.setSearchMethod(value1);
    }

}
