/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import cn.rongcloud.demo.common.MockAppServer;
import cn.rongcloud.demo.common.UiUtils;
import io.rong.imlib.RongIMClient;

/**
 * 登录页
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private EditText mEditUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEditUserId = findViewById(R.id.et_userId);
    }

    public void onLoginButtonClicked(View view) {
        if (checkPermission()) {
            getTokenFromAppServer();
        }
    }

    // 音视频功能所需权限检测
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissionList = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            };
            ArrayList<String> ungrantedPermissions = new ArrayList<>();
            for (String permission : permissionList) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ungrantedPermissions.add(permission);
                }
            }
            if (!ungrantedPermissions.isEmpty()) {
                String[] array = new String[ungrantedPermissions.size()];
                ActivityCompat.requestPermissions(this, ungrantedPermissions.toArray(array), 0);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int grantedCount = 0;
        for (int ret : grantResults) {
            if (PackageManager.PERMISSION_GRANTED == ret) {
                grantedCount++;
            }
        }
        if (grantedCount == permissions.length) {
            getTokenFromAppServer();
        } else {
            Toast.makeText(this, "应用所需权限不足！", Toast.LENGTH_SHORT).show();
        }
    }

    // 根据用户所填 UserID，模拟从开发者 App Server 获取 Token。
    private void getTokenFromAppServer() {
        String userId = mEditUserId.getText().toString().trim();
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "UserID 不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        UiUtils.showWaitingDialog(this);
        MockAppServer.getToken(DemoApplication.APP_KEY, DemoApplication.APP_SECRET, userId, new MockAppServer.GetTokenCallback() {

            @Override
            public void onGetTokenSuccess(String token) {
                Log.d(TAG, "onGetTokenSuccess() token = " + token);
                connectIMServer(token);
            }

            @Override
            public void onGetTokenFailed(String code) {
                UiUtils.hideWaitingDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "获取 Token 失败，code = " + code, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void connectIMServer(String token) {
        // 关键步骤 2：使用从 App Server 获取的代表 UserID 身份的 Token 字符串，连接融云 IM 服务。
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                UiUtils.hideWaitingDialog();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode code) {
                UiUtils.hideWaitingDialog();
                Toast.makeText(LoginActivity.this, "连接融云 IM 服务失败，code = " + code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                Log.d(TAG, "onDatabaseOpened");
            }
        });
    }
}