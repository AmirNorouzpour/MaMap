package ir.mamap.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.CustomAdapter;
import ir.mamap.app.Models.FriendMap;
import ir.mamap.app.Models.FriendRequestStatus;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.User;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {
    ListView mList;
    ArrayList<FriendMap> arrayItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, null);

        FloatingActionButton addFriend = view.findViewById(R.id.addFriendBtn);
        addFriend.setOnClickListener(v -> showAddUser());
        addFriend.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        MenuActivity menuActivity = (MenuActivity) getActivity();

        mList = view.findViewById(R.id.list_view);
        TextView nickName = view.findViewById(R.id.txt_nickName);
        TextView MobileTxt = view.findViewById(R.id.txt_Mobile);
        Typeface baseFont = Typeface.createFromAsset(menuActivity.getAssets(), "fonts/iran_san.ttf");
        ImageView img = view.findViewById(R.id.img_avatar);
        img.setOnClickListener(v -> {
            menuActivity.loadFragment(new ProfileFragment());
            menuActivity.bottomNav.getMenu().getItem(0).setChecked(true);
        });
        AVLoadingIndicatorView loadingIndicatorView = view.findViewById(R.id.avi);
        SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            GetUserFriends(menuActivity, loadingIndicatorView);
            pullToRefresh.setRefreshing(false);
        });

        nickName.setTypeface(baseFont);
        MobileTxt.setTypeface(baseFont);

        mList.setOnItemLongClickListener((arg0, v, pos, id) -> {
            View msgView = v.findViewById(R.id.txt_message);
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(menuActivity, msgView);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.popup_menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                Toast.makeText(
                        menuActivity,
                        "You Clicked : " + item.getTitle(),
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            });

            popup.show(); //showing popup menu

            return true;
        });
        TextView msgCountTxt = view.findViewById(R.id.msgCountTxt);
        ImageButton checkMsg = view.findViewById(R.id.checkMsg);
        msgCountTxt.setTypeface(baseFont);
        msgCountTxt.setVisibility(View.INVISIBLE);
        mList.setOnItemClickListener((adapterView, view1, i, l) -> {
            if (view1.getTag() != null) {

                FriendMap item = (FriendMap) view1.getTag();
                if (item.getStatus() == FriendRequestStatus.Accepted) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("FriendMap", item);
                    MapFragment mapFragment = new MapFragment();
                    mapFragment.setArguments(bundle);
                    menuActivity.loadFragment(mapFragment);
                    menuActivity.CurrentFragment = mapFragment;
                    menuActivity.bottomNav.getMenu().getItem(2).setChecked(true);
                } else {
                    GeneralUtils.showToast("تا قبل از پذیرش دوستتان شما نمی توانید موقعیت مکانی آنها را مشاهده کنید", Toast.LENGTH_SHORT, OutType.Warning);
                }
            }
        });


        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/GetUserMessagesCount")
                .get(new TypeToken<ClientData<User>>() {
                }, new INetwork<ClientData<User>>() {
                    @Override
                    public void onResponse(ClientData<User> response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (response.getOutType() == OutType.Success) {
                            int count = response.getEntityId();
                            if (count > 0) {
                                msgCountTxt.setText(String.valueOf(count));
                                msgCountTxt.setVisibility(View.VISIBLE);
                            } else msgCountTxt.setVisibility(View.INVISIBLE);
                        } else {
                            GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                        }


                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }

                });

        checkMsg.setOnClickListener(v -> showMsgList());
        User user = Mamap.User;

        if (user != null) {
            nickName.setText(user.getNickName());
            MobileTxt.setText(user.getMobile());
        }

        GetUserFriends(menuActivity, loadingIndicatorView);

        menuActivity.GetUserPicture(img);

        return view;
    }

    private void GetUserFriends(MenuActivity menuActivity, AVLoadingIndicatorView loadingIndicatorView) {
        arrayItem = new ArrayList<>();

        CustomAdapter mAdapter = new CustomAdapter(menuActivity, arrayItem);
        GeneralUtils.showLoading(loadingIndicatorView);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/GetUserFriends")
                .get(new TypeToken<ClientData<ArrayList<FriendMap>>>() {
                }, new INetwork<ClientData<ArrayList<FriendMap>>>() {
                    @Override
                    public void onResponse(ClientData<ArrayList<FriendMap>> response) {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                            ArrayList<FriendMap> lst = response.getEntity();
                            //List<FriendMap> lst2 = GeneralUtils.StringToArray(response.getTag().toString(), FriendMap[].class);
                            for (int i = 0; i < lst.size(); i++) {
                                arrayItem.add(lst.get(i));
                            }
                            mList.setAdapter(mAdapter);
                        } else {
                            GeneralUtils.hideLoading(loadingIndicatorView);
                            GeneralUtils.showToast(response.getMsg(), Toast.LENGTH_LONG, response.getOutType());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                    }

                });
    }

    private void showAddUser() {
        MenuActivity menuActivity = (MenuActivity) getActivity();
        Intent intent = new Intent(menuActivity, AddUserActivity.class);
//        intent.putExtra("Type", "Normal");
        startActivity(intent);

    }

    private void showMsgList() {
        MenuActivity menuActivity = (MenuActivity) getActivity();
        Intent intent = new Intent(menuActivity, MessageListActivity.class);
//        intent.putExtra("Type", "Normal");
        startActivity(intent);

    }
}
