/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.calldemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.rongcloud.calldemo.model.HomeListItemModel;
import cn.rongcloud.calldemo.view.adapter.HomeListAdapter;
import cn.rongcloud.callkit.activity.CallKitActivity;
import cn.rongcloud.demo.calllib.activity.CalllibActivity;
import io.rong.imlib.RongIMClient;

import static cn.rongcloud.calldemo.DemoApplication.APP_KEY;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getName();

    private static final String KEY_USER_ID = "USER_ID";
    private String mUserId = "";
    private TextView mUserIdTextView = null;
    private RecyclerView mRvList;
    private HomeListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        Intent intent = getIntent();
//        mUserId = intent.getStringExtra(KEY_USER_ID);
//        mUserIdTextView = findViewById(R.id.tv_userid);
//        mUserIdTextView.setText(mUserIdTextView.getText() + mUserId);

        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_menu_item) {
            // 响应 "注销" 点击事件
            RongIMClient.getInstance().logout();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mRvList = findViewById(R.id.rv_list);
        mRvList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mAdapter = new HomeListAdapter(this);
        mRvList.setAdapter(mAdapter);
    }

    private void initData() {
        List<HomeListItemModel> list = Arrays.asList(
                new HomeListItemModel(0,
                        "CallLib",
                        "不带UI的视频通话",
                        R.drawable.ic_call,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CalllibActivity.start(HomeActivity.this);
                            }
                        }),
                new HomeListItemModel(1,
                        "CallKit",
                        "带UI的视频通话",
                        R.drawable.ic_call,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CallKitActivity.start(HomeActivity.this, APP_KEY);
                            }
                        })
        );
        Collections.sort(list);
        mAdapter.refreshData(list);
    }

}