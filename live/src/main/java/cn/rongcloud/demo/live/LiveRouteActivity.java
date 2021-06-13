/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.live;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.rongcloud.rtc.base.RCRTCLiveRole;

public class LiveRouteActivity extends AppCompatActivity {
    private EditText mEditRoomId;

    public static void start(Context context) {
        Intent intent = new Intent(context, LiveRouteActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_room);
        mEditRoomId = findViewById(R.id.et_room_id);
    }

    public void click(View view) {
        if (view.getId() == R.id.btn_start_live) {
            String roomId = mEditRoomId.getText().toString().trim();
            if (TextUtils.isEmpty(roomId)) {
                Toast.makeText(this, "请输入直播房间 ID", Toast.LENGTH_SHORT).show();
                return;
            }
            LiveActivity.start(this, roomId, RCRTCLiveRole.BROADCASTER.getType());
        } else if (view.getId() == R.id.btn_join_live) {
            String roomId = mEditRoomId.getText().toString().trim();
            if (TextUtils.isEmpty(roomId)) {
                Toast.makeText(this, "请输入直播房间 ID", Toast.LENGTH_SHORT).show();
                return;
            }
            LiveActivity.start(this, roomId, RCRTCLiveRole.AUDIENCE.getType());
        }
    }
}
