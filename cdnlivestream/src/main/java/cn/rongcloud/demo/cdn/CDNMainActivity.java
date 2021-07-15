package cn.rongcloud.demo.cdn;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 * CDN直播拉流主页
 */
public class CDNMainActivity extends AppCompatActivity {
    private EditText mEditRoomId;

    public static void start(Context context) {
        Intent intent = new Intent(context, CDNMainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cdnmain);
        mEditRoomId = findViewById(R.id.et_room_id);
    }

    public void click(View view) {
        if (view.getId() == R.id.btn_start_live) {
            String roomId = mEditRoomId.getText().toString().trim();
            if (TextUtils.isEmpty(roomId)) {
                Toast.makeText(this, "请输入直播房间 ID", Toast.LENGTH_SHORT).show();
                return;
            }
            AnchorActivity.start(this, roomId);
        } else if (view.getId() == R.id.btn_join_live) {
            String roomId = mEditRoomId.getText().toString().trim();
            if (TextUtils.isEmpty(roomId)) {
                Toast.makeText(this, "请输入直播房间 ID", Toast.LENGTH_SHORT).show();
                return;
            }
            AudienceActivity.start(this, roomId);
        }
    }
}