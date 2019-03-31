package ir.mamap.app.Models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;

import ir.mamap.app.Mamap;
import ir.mamap.app.OnEnterActivity;
import ir.mamap.app.R;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;
import ir.oxima.dialogbuilder.DialogBuilder;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MenuAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<UserDataMenu> mItem;

    public MenuAdapter(Context mContext, ArrayList<UserDataMenu> mItem) {
        this.mContext = mContext;
        this.mItem = mItem;
    }

    @Override
    public int getCount() {
        return mItem.size();
    }

    @Override
    public Object getItem(int i) {
        return mItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_menu, viewGroup, false);
        }
        Object s = getItem(i);
        UserDataMenu currentItem = (UserDataMenu) s;

        TextView txtItemTitle = view.findViewById(R.id.txt_title);
        TextView txtItemValue = view.findViewById(R.id.txt_value);
        ImageView validationImg = view.findViewById(R.id.img_validation);

        if (!currentItem.getValid() && currentItem.getDataType() == 3 && currentItem.getValue() != null) {
            validationImg.setVisibility(View.VISIBLE);
            validationImg.setOnClickListener(v -> openValidationDialog(currentItem));
        }

        Typeface baseFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/iran_san.ttf");
        txtItemValue.setTypeface(baseFont);
        txtItemTitle.setTypeface(baseFont);
        view.setTag(currentItem);

        txtItemTitle.setText(currentItem.getTitle());
        txtItemValue.setText(currentItem.getValue());
        return view;

    }

    private void openValidationDialog(UserDataMenu data) {
        if (data.getDataType() == 3 && !data.getValid()) {

            DialogBuilder dialogBuilder = new DialogBuilder(mContext).asAlertDialog(false);
            dialogBuilder.setMessage("آیا می خواهید ایمیل خود را اعتبارسنجی کنید؟");
            dialogBuilder.setTitle("سوال");
            dialogBuilder.setPositiveButton("بله", dialog -> {
                dialog.dismiss();
                sendEmailRequest();
            });
            dialogBuilder.setNegativeButton("خیر", dialog -> dialog.dismiss());
            dialogBuilder.show();

        }
    }

    private void sendEmailRequest() {
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/VerificationCode/VerificationEmail")
                .get(new TypeToken<ClientDataNonGeneric>() {
                }, new INetwork<ClientDataNonGeneric>() {
                    @Override
                    public void onResponse(ClientDataNonGeneric response) {
                        GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }
                });

    }
}
