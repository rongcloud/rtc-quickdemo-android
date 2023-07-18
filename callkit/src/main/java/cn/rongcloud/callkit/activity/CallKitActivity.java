/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.callkit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import cn.rongcloud.callkit.R;
import cn.rongcloud.demo.common.UiUtils;
import io.rong.callkit.RongCallKit;
import io.rong.callkit.RongCallModule;
import io.rong.imkit.picture.tools.ToastUtils;


public class CallKitActivity extends AppCompatActivity {

    private static final String TAG = CallKitActivity.class.getName();

    private static String APP_KEY = null;

    private View mBtnAudioCall;
    private View mBtnVideoCall;
    private AppCompatEditText mEtCallNumber;

    public static void start(@NonNull Context context, @NonNull String appKey) {
        APP_KEY = appKey;
        Intent intent = new Intent(context, CallKitActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_kit);
        initClient();
        initView();
        initListener();
    }

    private void initClient() {
        RongCallModule rongCallModule = new RongCallModule();
        rongCallModule.onInit(getApplicationContext(), APP_KEY);
        //正常情况下应采取以下方式初始化
        //RongIM.init(getApplication(), APP_KEY);
    }

    private void initView() {
        mBtnAudioCall = findViewById(R.id.btn_audio_call);
        mBtnVideoCall = findViewById(R.id.btn_video_call);
        mEtCallNumber = findViewById(R.id.et_call_number);
        //显示返回按钮
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initListener() {
        mBtnAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UiUtils.isFastClick()) {
                    makeCallByType(RongCallKit.CallMediaType.CALL_MEDIA_TYPE_AUDIO);
                }
            }
        });
        mBtnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UiUtils.isFastClick()) {
                    makeCallByType(RongCallKit.CallMediaType.CALL_MEDIA_TYPE_VIDEO);
                }
            }
        });
    }

    private void makeCallByType(RongCallKit.CallMediaType callType) {
        Editable text = mEtCallNumber.getText();
        if (text != null && !TextUtils.isEmpty(text.toString().trim())) {
            //发起呼叫
            RongCallKit.startSingleCall(CallKitActivity.this, text.toString(), callType);
        } else {
            ToastUtils.s(CallKitActivity.this, "对方号码不能为空");
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