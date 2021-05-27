/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.meeting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MeetingPrepareActivity extends AppCompatActivity {

    private EditText mEditRoomId;

    public static void start(Context context) {
        Intent intent = new Intent(context, MeetingPrepareActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        mEditRoomId = findViewById(R.id.et_room_id);
    }

    public void onJoinMeetingButtonClicked(View view) {
        final String roomId = mEditRoomId.getText().toString().trim();
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "请输入房间 ID", Toast.LENGTH_SHORT).show();
            return;
        }
        final CharSequence[] items = {"普通会议", "加密会议"};
        new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MeetingActivity.start(MeetingPrepareActivity.this, roomId, "加密会议".equals(items[which].toString()));
            }
        }).show();
    }
}
