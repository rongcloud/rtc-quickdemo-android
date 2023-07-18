/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.meeting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import cn.rongcloud.demo.meeting.presenter.MeetingPresenter;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RTCErrorCode;

/**
 * 1v1 视频会议演示
 */
public class MeetingActivity extends AppCompatActivity implements MeetingPresenter.MeetingCallback {

    public static final String KEY_ROOM_NUMBER = "room_number";
    public static final String KEY_IS_ENCRYPTION = "KEY_IS_ENCRYPTION";
    private static String mRoomId = "";

    static {
        // 自定义加密功能所需 so 库
        System.loadLibrary("custom_frame_crypto");
    }

    MeetingPresenter mMeetingPresenter;
    private boolean isEncryption = false;
    // 本地预览远端用户，全屏显示 VideoView
    private FrameLayout mFlLocalUser, mFlRemoteUser, mFlFull;

    public static void start(Context context, String roomId, boolean isEncryption) {
        Intent intent = new Intent(context, MeetingActivity.class);
        intent.putExtra(KEY_ROOM_NUMBER, roomId);
        intent.putExtra(KEY_IS_ENCRYPTION, isEncryption);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting1_v1_main);
        mFlLocalUser = findViewById(R.id.local_user);
        mFlRemoteUser = findViewById(R.id.remote_user);
        mFlFull = findViewById(R.id.fl_fullscreen);

        Intent intent = getIntent();
        mRoomId = intent.getStringExtra(KEY_ROOM_NUMBER);
        isEncryption = intent.getBooleanExtra(KEY_IS_ENCRYPTION, false);
        mMeetingPresenter = new MeetingPresenter();
        mMeetingPresenter.attachView(this);
        mMeetingPresenter.config(this);
        mMeetingPresenter.joinRoom(mRoomId,isEncryption);

        initBackButton();
        initTitle();
    }

    private void initTitle() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle((isEncryption ? "加密会议" : "普通会议") + ":" + mRoomId);
        }
    }

    private void initBackButton() {
        // 显示返回按钮
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMeetingPresenter.leaveRoom();
        mMeetingPresenter.detachView();
        mFlFull.removeAllViews();
        mFlLocalUser.removeAllViews();
        mFlRemoteUser.removeAllViews();
        RCRTCEngine.getInstance().unInit();
    }

    public void click(View view) {
        if (view.getId() == R.id.btn_leave) {
            finish();
        }
    }

    @Override
    public void onJoinRoomSuccess(RCRTCRoom rcrtcRoom) {
        // 加入房间成功，在 UI 线程设置本地用户显示的 View
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView rongRTCVideoView = new MeetVideoView(getApplicationContext()) {
                    public boolean onTouchEvent(MotionEvent event) {
                        super.onTouchEvent(event);
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // 本低预览视频全屏切换
                                if (this.getParent() == mFlLocalUser) {
                                    mFlLocalUser.removeView(this);
                                    mFlFull.addView(this);
                                } else {
                                    mFlFull.removeView(this);
                                    mFlLocalUser.addView(this);
                                }
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                };
                RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(rongRTCVideoView);
                mFlLocalUser.addView(rongRTCVideoView);
                // 本地用户发布
                mMeetingPresenter.publishDefaultAVStream();
                // 主动订阅远端用户发布的资源
                mMeetingPresenter.subscribeAVStream();
            }
        });
    }

    @Override
    public void onJoinRoomFailed(RTCErrorCode rtcErrorCode) {
    }

    @Override
    public void onPublishSuccess() {
    }

    @Override
    public void onPublishFailed() {
    }

    @Override
    public void onSubscribeSuccess(final List<RCRTCInputStream> inputStreamList) {
        // 订阅远端用户发布资源成功，设置显示的 view，在 UI 线程中执行
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView videoView = new MeetVideoView(getApplicationContext()) {
                    public boolean onTouchEvent(MotionEvent event) {
                        super.onTouchEvent(event);
                        // 远端用户视频全屏切换
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (this.getParent() == mFlRemoteUser) {
                                mFlRemoteUser.removeView(this);
                                mFlFull.addView(this);
                            } else {
                                mFlFull.removeView(this);
                                mFlRemoteUser.addView(this);
                            }
                        }
                        return true;
                    }
                };
                for (RCRTCInputStream inputStream : inputStreamList) {
                    if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                        ((RCRTCVideoInputStream) inputStream).setVideoView(videoView);
                        // 将远端视图添加至布局
                        MeetingActivity.this.mFlRemoteUser.addView(videoView);
                        break;
                    }
                }
            }
        });

    }

    @Override
    public void onSubscribeFailed() {
    }

    @Override
    public void onUserJoined(final RCRTCRemoteUser rcrtcRemoteUser) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MeetingActivity.this, ("用户:" + rcrtcRemoteUser.getUserId() + "加入会议"), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView rongRTCVideoView = mFlRemoteUser.findViewWithTag(MeetVideoView.class.getName());
                // 远端用户离开时, videoview 在 mFlRemoteUser上，删除挂载在 mFlRemoteUser 上的 videoview
                if (null != rongRTCVideoView) {
                    mFlRemoteUser.removeAllViews();
                    rongRTCVideoView = mFlFull.findViewWithTag(MeetVideoView.class.getName());
                    // 远端用户离开时，如果本地预览正处于全屏状态自动退出全屏
                    if (rongRTCVideoView != null) {
                        mFlFull.removeAllViews();
                        mFlLocalUser.addView(rongRTCVideoView);
                    }
                } else {
                    // 远端用户离开时 , videoview 在 mFlFull 上，删除挂载在 mFlFull 上的 videoview
                    mFlFull.removeAllViews();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 返回按钮响应事件
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 继承RCRTCVideoView,可以重写RCRTCVideoView方法定制特殊需求，
     * 例如本例中重写onTouchEvent实现点击全屏
     */
    class MeetVideoView extends RCRTCVideoView {
        public MeetVideoView(Context context) {
            super(context);
            this.setTag(MeetVideoView.class.getName());
        }
    }
}