/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.rongcloud.demo.live.LiveRouteActivity;
import cn.rongcloud.demo.meeting.MeetingPrepareActivity;
import cn.rongcloud.demo.model.HomeListItemModel;
import cn.rongcloud.demo.screenshare.ui.activity.CreateMeetingActivity;
import cn.rongcloud.demo.view.adapter.HomeListAdapter;
import io.rong.imlib.RongIMClient;

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
                        "会议 1v1",
                        "支持一对一、多对多音视频会议",
                        R.drawable.ic_meeting,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MeetingPrepareActivity.start(HomeActivity.this);
                            }
                        }),
                new HomeListItemModel(1,
                        "直播",
                        "支持多人连麦直播，观众上麦，主播下麦",
                        R.drawable.ic_live,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LiveRouteActivity.start(HomeActivity.this);
                            }
                        }),
                new HomeListItemModel(4,
                        "桌面共享",
                        "支持桌面共享功能",
                        R.drawable.ic_meeting,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    CreateMeetingActivity.startActivity(HomeActivity.this);
                                } else {
                                    Toast.makeText(HomeActivity.this, "该功能仅支持 Android 5.0 及以上版本", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
        );
        Collections.sort(list);
        mAdapter.refreshData(list);
    }

}