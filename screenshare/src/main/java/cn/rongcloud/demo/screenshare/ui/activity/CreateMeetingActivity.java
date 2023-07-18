/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.screenshare.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import cn.rongcloud.demo.common.UiUtils;
import cn.rongcloud.demo.screenshare.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CreateMeetingActivity extends AppCompatActivity {

    private AppCompatEditText etRoomNumber;
    private static final String TAG = "CreateMeetingActivity";

    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, CreateMeetingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_desktop_sharing);
        initActionBar();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private void initView() {
        etRoomNumber = findViewById(R.id.et_room_number);
        findViewById(R.id.btn_join_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etRoomNumber.getText())) {
                    if (UiUtils.isFastClick()) {
                        MeetingActivity.startActivity(CreateMeetingActivity.this, etRoomNumber.getText().toString().trim());
                    }
                } else {
                    Toast.makeText(CreateMeetingActivity.this, "会议号码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("会议桌面共享");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //返回按钮响应事件
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}