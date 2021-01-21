package cn.rongcloud.common.view;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import cn.rongcloud.common.R;
import cn.rongcloud.common.tools.LoadDialog;
import java.util.ArrayList;
import java.util.List;

public class Base extends Activity {

    List<String> unGrantedPermissions;
    public final String[] MANDATORY_PERMISSIONS = {
        "android.permission.MODIFY_AUDIO_SETTINGS",
        "android.permission.RECORD_AUDIO",
        "android.permission.INTERNET",
        "android.permission.CAMERA",
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkPermissions();
    }

    /**
     * 申请权限
     */
    private void checkPermissions() {
        unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) {
            //已经获得了所有权限
        } else { // 部分权限未获得，重新请求获取权限
            String[] array = new String[unGrantedPermissions.size()];
            ActivityCompat.requestPermissions(this, unGrantedPermissions.toArray(array), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        unGrantedPermissions.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                unGrantedPermissions.add(permissions[i]);
        }
        for (String permission : unGrantedPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "权限不足："+permission, Toast.LENGTH_SHORT).show();
            } else ActivityCompat.requestPermissions(this, new String[] {permission}, 0);
        }
        if (unGrantedPermissions.size() == 0) {
            //已经获得了所有权限
        }
    }

    public void checkPermission(){
        for (String permission : unGrantedPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "权限不足："+permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showToast(String msg){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Base.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isFinish(){
        return isFinishing() || isDestroyed();
    }

    protected void postUIThread(final Runnable run) {
        if (isFinish())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinish())
                    return;
                run.run();
            }
        });
    }

    public void showLoading(){
        if (isFinish()){
            return;
        }
        LoadDialog.show(this);
    }

    public void showLoading(String msg){
        if (isFinish())
            return;
        LoadDialog.show(this,msg);
    }

    public void closeLoading(){
        if (isFinish())
            return;
        LoadDialog.dismiss(this);
    }

    protected void setButtonClickable(Button[] clickableArray,Button[] notClickableArray) {
        if(clickableArray != null){
            for (int i = 0; i < clickableArray.length; i++) {
                clickableArray[i].setClickable(true);
                clickableArray[i].setBackgroundResource(R.drawable.shape_corner_button_blue);
            }
        }
        if(notClickableArray != null){
            for (int i = 0; i < notClickableArray.length; i++) {
                notClickableArray[i].setClickable(true);
                notClickableArray[i].setBackgroundResource(R.drawable.shape_corner_button_blue_invalid);
            }
        }
    }
}
