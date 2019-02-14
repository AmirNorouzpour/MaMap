package net.friendsmap.ayrsa.friendsmap.Models;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.friendsmap.ayrsa.friendsmap.R;

import java.util.ArrayList;

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
        Typeface baseFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/iran_san.ttf");
        txtItemValue.setTypeface(baseFont);
        txtItemTitle.setTypeface(baseFont);
        view.setTag(currentItem);

        txtItemTitle.setText(currentItem.getTitle());
        txtItemValue.setText(currentItem.getValue());
        return view;

    }
}
