/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo;

import static cn.rongcloud.demo.DemoApplication.APP_KEY;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.State;
import cn.rongcloud.callkit.activity.CallKitActivity;
import cn.rongcloud.demo.HomeListAdapter.HomeListItemModel;
import cn.rongcloud.demo.calllib.activity.CalllibActivity;
import cn.rongcloud.demo.callplus.CallPlusActivity;
import cn.rongcloud.demo.cdn.CDNMainActivity;
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

    private final List<HomeListItemModel> modelList = Arrays.asList(
            new HomeListItemModel(0,
                    "会议 1v1",
                    "支持一对一、多对多音视频会议",
                    R.drawable.ic_meeting, v -> MeetingPrepareActivity.start(HomeActivity.this)),
            new HomeListItemModel(1,
                    "直播",
                    "支持多人连麦直播，观众上麦，主播下麦",
                    R.drawable.ic_live, v -> LiveRouteActivity.start(HomeActivity.this)),
        new HomeListItemModel(2,
            "CallPlus",
            "最新版音视频通话 SDK",
            R.drawable.ic_call_plus,
            R.drawable.ic_new_label, v -> CallPlusActivity.start(HomeActivity.this)),
        new HomeListItemModel(3,
            "CallLib",
            "旧版本音视频通话(不含 UI)",
            R.drawable.ic_call, v -> CalllibActivity.start(HomeActivity.this)),
        new HomeListItemModel(4,
            "CallKit",
            "音视频通话(含 UI, 基于 CallLib)",
            R.drawable.ic_call_kit, v -> CallKitActivity.start(HomeActivity.this, APP_KEY)),
            new HomeListItemModel(5,
                    "桌面共享",
                    "支持桌面共享功能",
                    R.drawable.ic_screen, v -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            CreateMeetingActivity.startActivity(HomeActivity.this);
                        } else {
                            Toast.makeText(HomeActivity.this, "该功能仅支持 Android 5.0 及以上版本", Toast.LENGTH_SHORT).show();
                        }
                    }),
            new HomeListItemModel(6,
                "CDN直播拉流",
                "支持主播发布CDN流、观众订阅CDN流",
                R.drawable.ic_live, v -> CDNMainActivity.start(HomeActivity.this))
    );
    private final HomeListAdapter adapter = new HomeListAdapter(modelList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        RecyclerView listview = findViewById(R.id.rv_list);
        listview.setAdapter(adapter);
        listview.addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = 40;
            }
        });
        ((TextView)findViewById(R.id.tv_userid)).setText("User ID: "+RongIMClient.getInstance().getCurrentUserId());
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