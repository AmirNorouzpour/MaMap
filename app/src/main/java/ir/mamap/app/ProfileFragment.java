package ir.mamap.app;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wang.avi.AVLoadingIndicatorView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.listener.OnCheckedListener;

import ir.mamap.app.Models.ClientData;
import ir.mamap.app.Models.ClientDataNonGeneric;
import ir.mamap.app.Models.MenuAdapter;
import ir.mamap.app.Models.OutType;
import ir.mamap.app.Models.User;
import ir.mamap.app.Models.UserDataMenu;
import ir.mamap.app.Utils.CryptoHelper;
import ir.mamap.app.Utils.GeneralUtils;
import ir.mamap.app.Utils.GifSizeFilter;
import ir.mamap.app.Utils.Glide4Engine;
import ir.mamap.app.network.INetwork;
import ir.mamap.app.network.NetworkManager;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    ListView mList;
    ArrayList<UserDataMenu> arrayItem;
    AVLoadingIndicatorView loadingIndicatorView;
    FloatingActionButton profilePictureBtn;
    TextView nickName, txtExpireDate, mobileTxt;
    ImageView imgAvatar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, null);

        MenuActivity menuActivity = (MenuActivity) getActivity();

        mList = view.findViewById(R.id.list_view_profileMenu);
        nickName = view.findViewById(R.id.txt_nickName);
        mobileTxt = view.findViewById(R.id.txt_Mobile);
        Typeface baseFont = Typeface.createFromAsset(menuActivity.getAssets(), "fonts/iran_san.ttf");
        txtExpireDate = view.findViewById(R.id.txtExpireDate);
        imgAvatar = view.findViewById(R.id.img_avatar);

        profilePictureBtn = view.findViewById(R.id.ProfilePictureBtn);
        TextView versionTxt = view.findViewById(R.id.VersionTxt);
        profilePictureBtn.setBackgroundColor(getResources().getColor(R.color.colorSilver));
        profilePictureBtn.setOnClickListener(v -> ChooseImage());

        versionTxt.setTypeface(baseFont);
        versionTxt.setText(" مامَپ نسخه " + GeneralUtils.GetVersionName());
        nickName.setTypeface(baseFont);
        mobileTxt.setTypeface(baseFont);
        txtExpireDate.setTypeface(baseFont);

        mList.setOnItemClickListener((adapterView, view1, i, l) -> {
            UserDataMenu tag = (UserDataMenu) view1.getTag();
            if (tag.getEditable())
                editData(tag);
        });

        loadingIndicatorView = view.findViewById(R.id.avi);

        GetUserMenus();

        User user = Mamap.User;
        if (user != null) {
            nickName.setText(user.getNickName());
            mobileTxt.setText(user.getMobile());
            txtExpireDate.setText(user.getExpDateText());
        }

        menuActivity.GetUserPicture(imgAvatar);
        return view;
    }


    public void pickImage() {
        MenuActivity menuActivity = (MenuActivity) getActivity();
        Matisse.from(menuActivity)
                .choose(MimeType.ofImage())
                .countable(false)
                .maxSelectable(1)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .theme(R.style.Matisse_Dracula)
                .setOnCheckedListener(new OnCheckedListener() {
                    @Override
                    public void onCheck(boolean isChecked) {
                        // DO SOMETHING IMMEDIATELY HERE
                        Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                    }
                })
                .forResult(REQUEST_CODE_CHOOSE);

    }


    private void ChooseImage() {

        MenuActivity menuActivity = (MenuActivity) getActivity();
        Dexter.withActivity(menuActivity)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted())
                    GeneralUtils.showToast("برای تغییر تصویر کاربری باید بتوانیم تصاویر را از دستگاه شما دریافت کنیم", Toast.LENGTH_SHORT, OutType.Error);
                else {
                    pickImage();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    public static final int REQUEST_CODE_CHOOSE = 1;

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        try {
//            if (resultCode == Activity.RESULT_OK) {
//                if (requestCode == PICK_IMAGE) {
//                    MenuActivity menuActivity = (MenuActivity) getActivity();
//
//                    Uri selectedImage = data.getData();
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                    Cursor cursor = menuActivity.getContentResolver().query(selectedImage,
//                            filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                    String picturePath = cursor.getString(columnIndex);
//                    cursor.close();
//
//                    if (picturePath != null) {
//                        File f = new File(picturePath);
//                        GeneralUtils.showLoading(loadingIndicatorView);
//                        SendImageToServer(f);
//                    }
//
////                    Uri selectedImageUri = data.getData();
////                    InputStream inputStream = menuActivity.getContentResolver().openInputStream(data.getData());
////                    // Get the path from the Uri
////                    final String path = getPathFromURI(selectedImageUri);
////                    if (path != null) {
////                        File f = new File(path);
////                        SendImageToServer(f);
////                        selectedImageUri = Uri.fromFile(f);
////                    }
//                    // Set the image in ImageView
//                    //   ImageView((ImageView) findViewById(R.id.imgView)).setImageURI(selectedImageUri);
//                }
//            }
//        } catch (Exception e) {
//            Log.e("FileSelectorActivity", "File select error", e);
//        }
//    }


//    @Override
//    public void onResume() {
//        GetUserMenus();
//        super.onResume();
//
//    }

    private void GetUserMenus() {
        arrayItem = new ArrayList<>();
        MenuActivity menuActivity = (MenuActivity) getActivity();
        MenuAdapter mAdapter = new MenuAdapter(menuActivity, arrayItem);
        GeneralUtils.showLoading(loadingIndicatorView);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/GetUserMenu")
                .get(new TypeToken<ClientData<String>>() {
                }, new INetwork<ClientData<String>>() {
                    @Override
                    public void onResponse(ClientData<String> response) throws Exception {
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (response.getOutType() == OutType.Success && response.getEntity() != null) {
                            String encData = response.getEntity();
                            String data = CryptoHelper.decrypt(encData);

                            Type listType = new TypeToken<List<UserDataMenu>>() {
                            }.getType();
                            List<UserDataMenu> lst = new Gson().fromJson(data, listType);

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

    private void editData(UserDataMenu data) {
        Intent intent = new Intent(getActivity(), UserDataEditActivity.class);
        intent.putExtra("Data", data);
        startActivityForResult(intent, 10);
        // getActivity().startActivityForResult(intent, 22);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 10) {
            if (resultCode == Activity.RESULT_OK) {
                GetUserMenus();
                MenuActivity menuActivity = (MenuActivity) getActivity();
                menuActivity.GetUserAccount();
                User user = Mamap.User;
                nickName.setText(user.getNickName());
                mobileTxt.setText(user.getMobile());
                txtExpireDate.setText(user.getExpDateText());
            }
        }

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> paths = Matisse.obtainPathResult(data);
                File f = new File(paths.get(0));

                try {
                    File compressedImageFile = new Compressor(getActivity()).compressToFile(f);
                    GeneralUtils.showLoading(loadingIndicatorView);
                    SendImageToServer(compressedImageFile);
                    Uri imageUri = Uri.fromFile(compressedImageFile);
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_user)
                            .circleCropTransform()
                            .error(R.drawable.ic_user);

                    Glide.with(this)
                            .load(imageUri).apply(options)
                            .into(imgAvatar);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }


    private void SendImageToServer(File file) {
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "در حال ارسال", true);
        NetworkManager.builder()
                .setUrl(Mamap.BaseUrl + "/api/user/UploadPicture")
                .upload(file, new INetwork<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        ClientDataNonGeneric res = gson.fromJson(response.toString(), ClientDataNonGeneric.class);
                        GeneralUtils.showToast(res.getMsg(), Toast.LENGTH_LONG, res.getOutType());
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        if (res.getOutType() == OutType.Success) {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences("token", MODE_PRIVATE).edit();
                            editor.putString("PFileName", res.getTag() + "");
                            editor.apply();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        GeneralUtils.showToast(anError.getErrorBody(), Toast.LENGTH_LONG, OutType.Error);
                        GeneralUtils.hideLoading(loadingIndicatorView);
                        dialog.dismiss();
                    }

                });
    }
}
