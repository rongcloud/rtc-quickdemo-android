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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import cn.rongcloud.demo.live.LiveRouteActivity;
import cn.rongcloud.demo.meeting.MeetingPrepareActivity;
import cn.rongcloud.demo.screenshare.ui.activity.CreateMeetingActivity;
import io.rong.imlib.RongIMClient;

/**
 * 功能选择页
 */
public class HomeActivity extends AppCompatActivity {

    private final List<HomeListAdapter.HomeListItemModel> modelList = Arrays.asList(
            new HomeListAdapter.HomeListItemModel(0,
                    "会议 1v1",
                    "支持一对一、多对多音视频会议",
                    R.drawable.ic_meeting,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MeetingPrepareActivity.start(HomeActivity.this);
                        }
                    }),
            new HomeListAdapter.HomeListItemModel(1,
                    "直播",
                    "支持多人连麦直播，观众上麦，主播下麦",
                    R.drawable.ic_live,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LiveRouteActivity.start(HomeActivity.this);
                        }
                    }),
            new HomeListAdapter.HomeListItemModel(2,
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
    private final HomeListAdapter adapter = new HomeListAdapter(modelList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ((RecyclerView) findViewById(R.id.rv_list)).setAdapter(adapter);
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
}