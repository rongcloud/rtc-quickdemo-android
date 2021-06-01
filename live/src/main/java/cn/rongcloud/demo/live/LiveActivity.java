/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.live;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.demo.live.gpuimage.GPUImageBeautyFilter;
import cn.rongcloud.demo.live.gpuimage.GPUImageFilter;
import cn.rongcloud.demo.live.presenter.LiveAnchorPresenter;
import cn.rongcloud.demo.live.presenter.LiveAudiencePresenter;
import cn.rongcloud.demo.live.status.IStatus;
import cn.rongcloud.demo.live.ui.VideoViewManager;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoOutputFrameListener;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCLiveRole;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCVideoFrame;
import cn.rongcloud.rtc.base.RTCErrorCode;

/**
 * RTC QuickDemo 直播功能activity
 */
public class LiveActivity extends AppCompatActivity {

    public static final String KEY_ROOM_NUMBER = "room_number";
    public static final String KEY_ROLE = "role_type";
    private static final String TAG = "LiveActivity";
    private static final String DEFAULT_FILE_STREAM_NAME = "file:///android_asset/video_1.mp4";
    /**
     * 创建 BeautyFilter 事件
     */
    private static final int EVENT_MESSAGE_CREATE_BEAUTY_FILTER = 0;
    /**
     * 销毁 Filter 事件
     */
    private static final int EVENT_MESSAGE_DESTROY_FILTER = 1;
    private static String mRoomId = "";
    // 主播sdk调用
    LiveAnchorPresenter mAnchorPresenter;
    // 观众sdk调用
    LiveAudiencePresenter mAudiencePresenter;
    /**
     * 当前用户的状态，可以设置为主播和观众
     */
    IStatus curStatus;
    /**
     * 用于切换状态失败时恢复状态的备份变量
     */
    IStatus BakStatus;
    FrameLayout flSurfaceContainer;
    VideoViewManager videoViewManager;
    Button mMixlayout;
    IStatus mIdleStatus = new IdleStatus();
    IStatus mAnchorStatus = new AnchorStatus();
    IStatus mAudienceStatus = new AudienceStatus();
    private Button mRequestLive;
    private Button mCamera;
    private Button mMic;
    private Button mSwitch;
    private Button mEndLive;
    private MenuItem mBeautyMenuItem;
    // 美颜开关状态
    private volatile boolean mBeautyStatus = false;
    private VideoFilterHandler mVideoFilterHandler = null;
    private MenuItem mMenuFileStreamItem;
    private MenuItem mMenuUsbStreamItem;
    private PushFileStreamStatus mMenuFileStreamStatus = PushFileStreamStatus.Push;
    private PushFileStreamStatus mMenuUsbStreamStatus = PushFileStreamStatus.Push;

    public static void start(Context context, String roomId, int roleType) {
        Intent intent = new Intent(context, LiveActivity.class);
        intent.putExtra(KEY_ROOM_NUMBER, roomId);
        intent.putExtra(KEY_ROLE, roleType);
        context.startActivity(intent);
    }

    IStatus getCurStatus() {
        return curStatus;
    }

    /**
     * 设置为空闲状态，在加入房间成功后设置为对应的主播或观众的状态
     */
    void setIdleStatus() {
        mRequestLive.setVisibility(View.INVISIBLE);
        mCamera.setVisibility(View.INVISIBLE);
        mMic.setVisibility(View.INVISIBLE);
        mSwitch.setVisibility(View.INVISIBLE);
        mEndLive.setVisibility(View.INVISIBLE);
        mMixlayout.setVisibility(View.INVISIBLE);

        curStatus = mIdleStatus;
    }

    /**
     * 当远端或本端视频流发生变化时全量更新ui
     */
    void updateVideoView(List<RCRTCVideoOutputStream> outputStreams, List<RCRTCVideoInputStream> inputStreams) {
        ArrayList<RCRTCVideoView> list = new ArrayList<>();
        if (null != outputStreams) {
            for (RCRTCVideoOutputStream o : outputStreams) {
                RCRTCVideoView rongRTCVideoView = new RCRTCVideoView(LiveActivity.this);
                o.setVideoView(rongRTCVideoView);
                list.add(rongRTCVideoView);
            }
        }

        if (null != inputStreams) {
            for (RCRTCVideoInputStream i : inputStreams) {
                RCRTCVideoView rongRTCVideoView = new RCRTCVideoView(LiveActivity.this);
                i.setVideoView(rongRTCVideoView);
                list.add(rongRTCVideoView);
            }
        }
        videoViewManager.update(list);
    }

    /**
     * 主播状态准备
     */
    void setAnchorStatus() {
        if (null != curStatus) {
            curStatus.detach();
            BakStatus = curStatus;
            // 恢复到 idle 状态
            setIdleStatus();
        }
        mAnchorStatus.attach();
    }

    /**
     * 观众状态准备
     */
    void setAudienceStatus() {
        if (null != curStatus) {
            curStatus.detach();
            BakStatus = curStatus;
            setIdleStatus();
        }
        mAudienceStatus.attach();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_main);
        mRequestLive = findViewById(R.id.bu_requestlive);
        mCamera = findViewById(R.id.bu_camera);
        mMic = findViewById(R.id.bu_mic);
        mSwitch = findViewById(R.id.bu_switchcrame);
        mEndLive = findViewById(R.id.bu_endlive);
        mMixlayout = findViewById(R.id.bu_layout);

        flSurfaceContainer = findViewById(R.id.surfcecontainer);
        flSurfaceContainer.post(new Runnable() {
            @Override
            public void run() {
                videoViewManager = new VideoViewManager(flSurfaceContainer, flSurfaceContainer.getWidth(),
                        flSurfaceContainer.getHeight());
            }
        });

        setIdleStatus();

        Intent intent = getIntent();
        mRoomId = intent.getStringExtra(KEY_ROOM_NUMBER);
        int type = intent.getIntExtra(KEY_ROLE, RCRTCLiveRole.BROADCASTER.getType());

        mAnchorPresenter = new LiveAnchorPresenter(this);
        mAudiencePresenter = new LiveAudiencePresenter(this);
        if (type == RCRTCLiveRole.BROADCASTER.getType()) {
            setAnchorStatus();
        } else {
            setAudienceStatus();
        }
        initTitle(type);
    }

    private void initTitle(int type) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle((type == RCRTCLiveRole.BROADCASTER.getType() ? "主播端" : "观众端") + ":" + mRoomId);
        }
        showBackButton();
    }

    private void showBackButton() {
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
        getCurStatus().detach();
        if (mVideoFilterHandler != null) {
            mVideoFilterHandler.sendEmptyMessage(EVENT_MESSAGE_DESTROY_FILTER);
        }
    }

    public void click(View view) {
        if (view.getId() == R.id.bu_requestlive) {
            setAnchorStatus();

        } else if (view.getId() == R.id.bu_endlive) {
            setAudienceStatus();

        } else if (view.getId() == R.id.bu_camera) {
            String str = ((Button) view).getText().toString();
            if (TextUtils.equals(str, AnchorConfig.CAMERA_STATUS_CLOSE)) {
                RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                ((Button) view).setText(AnchorConfig.CAMERA_STATUS_OPEN);
            } else {
                RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);

                ((Button) view).setText(AnchorConfig.CAMERA_STATUS_CLOSE);
            }
        } else if (view.getId() == R.id.bu_mic) {
            String str = ((Button) view).getText().toString();
            if (TextUtils.equals(str, AnchorConfig.MIC_STATUS_CLOSE)) {
                RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(true);
                ((Button) view).setText(AnchorConfig.MIC_STATUS_OPEN);
            } else {
                RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(false);
                ((Button) view).setText(AnchorConfig.MIC_STATUS_CLOSE);
            }
        } else if (view.getId() == R.id.bu_switchcrame) {
            RCRTCEngine.getInstance().getDefaultVideoStream().switchCamera(null);
        } else if (view.getId() == R.id.bu_layout) {
            String str = ((Button) view).getText().toString();
            setMixLayout(str);
        }
    }

    private void setMixLayout(String str) {
        curStatus.mixLayout(str);
    }

    /**
     * 标题栏事件响应
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 返回按钮响应事件
            finish();
            return true;
        } else if (item.getItemId() == R.id.beauty_action_btn) {
            // 响应美颜按钮点击事件
            handleBeautyStatusChange(!mBeautyStatus);
        } else if (item.getItemId() == R.id.filestream_action_btn) {
            if (mMenuFileStreamStatus == PushFileStreamStatus.BackDoing) {

            } else if (mMenuFileStreamStatus == PushFileStreamStatus.Push) {
                handleMenuFileStreamStatus(PushFileStreamStatus.BackDoing);
            } else if (mMenuFileStreamStatus == PushFileStreamStatus.Unpush) {
                handleMenuFileStreamStatus(PushFileStreamStatus.BackDoing);
            }
        } else if (item.getItemId() == R.id.usbstream_action_btn) {
            if (mMenuUsbStreamStatus == PushFileStreamStatus.BackDoing) {

            } else if (mMenuUsbStreamStatus == PushFileStreamStatus.Push) {
                handleMenuUsbStreamStatus(PushFileStreamStatus.BackDoing);
            } else if (mMenuUsbStreamStatus == PushFileStreamStatus.Unpush) {
                handleMenuUsbStreamStatus(PushFileStreamStatus.BackDoing);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理美颜功能按钮状态改变
     *
     * @param beautyStatus
     */
    private void handleBeautyStatusChange(boolean beautyStatus) {
        if (mBeautyMenuItem != null) {
            mBeautyStatus = beautyStatus;
            mBeautyMenuItem.setTitle(mBeautyStatus ? "关闭美颜" : "打开美颜");
        }
    }

    private void handleMenuUsbStreamStatus(PushFileStreamStatus status) {
        if (mMenuUsbStreamItem != null) {
            if (mMenuUsbStreamStatus == PushFileStreamStatus.BackDoing) {
                if (status == PushFileStreamStatus.BackDoing) {
                    return;
                } else if (status == PushFileStreamStatus.Push) {
                    mMenuUsbStreamStatus = PushFileStreamStatus.Push;
                    mMenuUsbStreamItem.setTitle("打开usb摄像头");
                } else if (status == PushFileStreamStatus.Unpush) {
                    mMenuUsbStreamStatus = PushFileStreamStatus.Unpush;
                    mMenuUsbStreamItem.setTitle("关闭usb摄像头");
                }
            } else if (mMenuUsbStreamStatus == PushFileStreamStatus.Push) {
                if (status == PushFileStreamStatus.BackDoing) {
                    curStatus.publishUsbCameraStream();
                    mMenuUsbStreamStatus = PushFileStreamStatus.BackDoing;
                    mMenuUsbStreamItem.setTitle("进行中");
                } else if (status == PushFileStreamStatus.Push) {
                    return;
                } else if (status == PushFileStreamStatus.Unpush) {
                    return;
                }
            } else if (mMenuUsbStreamStatus == PushFileStreamStatus.Unpush) {
                if (status == PushFileStreamStatus.BackDoing) {
                    curStatus.unpublishUsbCameraStream();
                    mMenuUsbStreamStatus = PushFileStreamStatus.BackDoing;
                    mMenuUsbStreamItem.setTitle("进行中");
                } else if (status == PushFileStreamStatus.Push) {
                    return;
                } else if (status == PushFileStreamStatus.Unpush) {
                    return;
                }
            }
        }
    }

    private void handleMenuFileStreamStatus(PushFileStreamStatus status) {
        if (mMenuFileStreamItem != null) {
            if (mMenuFileStreamStatus == PushFileStreamStatus.BackDoing) {
                if (status == PushFileStreamStatus.BackDoing) {
                    return;
                } else if (status == PushFileStreamStatus.Push) {
                    mMenuFileStreamStatus = PushFileStreamStatus.Push;
                    mMenuFileStreamItem.setTitle("发送视频流");
                } else if (status == PushFileStreamStatus.Unpush) {
                    mMenuFileStreamStatus = PushFileStreamStatus.Unpush;
                    mMenuFileStreamItem.setTitle("取消发送视频流");
                }
            } else if (mMenuFileStreamStatus == PushFileStreamStatus.Push) {
                if (status == PushFileStreamStatus.BackDoing) {
                    curStatus.publishCustomStream(DEFAULT_FILE_STREAM_NAME);
                    mMenuFileStreamStatus = PushFileStreamStatus.BackDoing;
                    mMenuFileStreamItem.setTitle("进行中");
                } else if (status == PushFileStreamStatus.Push) {
                    return;
                } else if (status == PushFileStreamStatus.Unpush) {
                    return;
                }
            } else if (mMenuFileStreamStatus == PushFileStreamStatus.Unpush) {
                if (status == PushFileStreamStatus.BackDoing) {
                    curStatus.unpublishCustomStream();
                    mMenuFileStreamStatus = PushFileStreamStatus.BackDoing;
                    mMenuFileStreamItem.setTitle("进行中");
                } else if (status == PushFileStreamStatus.Push) {
                    return;
                } else if (status == PushFileStreamStatus.Unpush) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 添加 ActionBar 美颜按钮
        getMenuInflater().inflate(R.menu.beauty_menu, menu);
        mBeautyMenuItem = menu.findItem(R.id.beauty_action_btn);
        mMenuFileStreamItem = menu.findItem(R.id.filestream_action_btn);
        mMenuUsbStreamItem = menu.findItem(R.id.usbstream_action_btn);
        handleBeautyStatusChange(false);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 推送自定义视频流的状态枚举
     */
    enum PushFileStreamStatus {
        Push,
        BackDoing,
        Unpush
    }

    /**
     * 通过该 Handler 确保 Filter 的创建销毁和使用都在同一个线程中，
     * 避免错误的线程销毁导致 OpenGL 内存泄漏
     */
    public static class VideoFilterHandler extends Handler {
        // 当前的 Filter
        private GPUImageFilter currentImageFilter = null;

        public VideoFilterHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EVENT_MESSAGE_CREATE_BEAUTY_FILTER:
                    destroyCurrentFilter();
                    VideoFilterCreateModel model = (VideoFilterCreateModel) msg.obj;
                    GPUImageFilter gpuImageFilter = new GPUImageBeautyFilter();
                    gpuImageFilter.draw(model.width, model.height, model.textureId);
                    currentImageFilter = gpuImageFilter;
                    break;
                case EVENT_MESSAGE_DESTROY_FILTER:
                    destroyCurrentFilter();
                    removeCallbacksAndMessages(null);
                    break;
            }
        }

        private void destroyCurrentFilter() {
            if (currentImageFilter != null) {
                GPUImageFilter imageFilter = this.currentImageFilter;
                // 立刻置空，防止其他线程获取到一个正在销毁的 Filter
                currentImageFilter = null;
                imageFilter.destroy();
            }
        }

        @Nullable
        public GPUImageFilter getCurrentImageFilter() {
            return currentImageFilter;
        }
    }

    private static class VideoFilterCreateModel {
        int width;
        int height;
        int textureId;

        public VideoFilterCreateModel(int width, int height, int textureId) {
            this.width = width;
            this.height = height;
            this.textureId = textureId;
        }
    }

    class IdleStatus implements IStatus {

        @Override
        public void attach() {

        }

        @Override
        public void detach() {

        }

        @Override
        public void config() {

        }

        @Override
        public void joinRoom(String roomId) {

        }

        @Override
        public void leaveRoom() {

        }

        @Override
        public void publishDefaultAVStream() {

        }

        @Override
        public void subscribeAVStream() {

        }

        @Override
        public void requestSpeak() {

        }

        @Override
        public void downSpeak() {

        }

        @Override
        public void changeUi() {

        }

        @Override
        public void mixLayout(String str) {

        }

        @Override
        public void publishCustomStream(String filePath) {

        }

        @Override
        public void unpublishCustomStream() {

        }

        @Override
        public void publishUsbCameraStream() {

        }

        @Override
        public void unpublishUsbCameraStream() {

        }
    }

    class AnchorStatus implements IStatus, LiveAnchorPresenter.LiveCallback {

        @Override
        public void attach() {
            mAnchorPresenter.attachView(this);
            mAnchorPresenter.config(LiveActivity.this);
            mAnchorPresenter.joinRoom(mRoomId);
        }

        @Override
        public void detach() {
            mAnchorPresenter.leaveRoom();
            mAnchorPresenter.detachView();
        }

        @Override
        public void config() {
            mAnchorPresenter.config(LiveActivity.this);
        }

        @Override
        public void joinRoom(String roomId) {
            mAnchorPresenter.joinRoom(roomId);
        }

        @Override
        public void leaveRoom() {
            mAnchorPresenter.leaveRoom();
        }

        @Override
        public void publishDefaultAVStream() {
            mAnchorPresenter.publishDefaultAVStream();
        }

        @Override
        public void subscribeAVStream() {
            mAnchorPresenter.subscribeAVStream();
        }

        @Override
        public void requestSpeak() {
        }

        @Override
        public void downSpeak() {
            mAnchorPresenter.unPublishDefaultAVStream();
        }

        @Override
        public void changeUi() {
            mRequestLive.setVisibility(View.INVISIBLE);
            mCamera.setVisibility(View.VISIBLE);
            mMic.setVisibility(View.VISIBLE);
            mSwitch.setVisibility(View.VISIBLE);
            mEndLive.setVisibility(View.VISIBLE);
            mMixlayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void mixLayout(String str) {
            mAnchorPresenter.setMixLayout(str);
        }

        @Override
        public void publishCustomStream(String filePath) {
            mAnchorPresenter.publishCustomStream(filePath);
        }

        @Override
        public void unpublishCustomStream() {
            mAnchorPresenter.unpublishCustomStream();
        }

        @Override
        public void publishUsbCameraStream() {
            mAnchorPresenter.publishUsbCameraStream();
        }

        @Override
        public void unpublishUsbCameraStream() {
            mAnchorPresenter.unpublishUsbCameraStream();
        }

        @Override
        public void onJoinRoomSuccess(RCRTCRoom rcrtcRoom) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    curStatus = mAnchorStatus;
                    curStatus.changeUi();
                    // 本地用户发布
                    AnchorStatus.this.publishDefaultAVStream();
                    AnchorStatus.this.subscribeAVStream();
                }
            });
        }

        @Override
        public void onJoinRoomFailed(RTCErrorCode rtcErrorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    curStatus = BakStatus;
                    curStatus.changeUi();
                }
            });

        }

        @Override
        public void onPublishSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    RCRTCEngine.getInstance().getDefaultVideoStream().setVideoFrameListener(new IRCRTCVideoOutputFrameListener() {
                        @Override
                        public RCRTCVideoFrame processVideoFrame(RCRTCVideoFrame rcrtcVideoFrame) {
                            if (mBeautyStatus) {
                                try {
                                    if (mVideoFilterHandler == null) {
                                        //通过当前线程的Looper创建Handler，确保OpenGL的资源创建销毁都在当前线程
                                        mVideoFilterHandler = new VideoFilterHandler(Looper.myLooper());
                                    }
                                    GPUImageFilter imageFilter = mVideoFilterHandler.getCurrentImageFilter();
                                    if (imageFilter == null) {
                                        //创建要使用的Filter
                                        Message message = mVideoFilterHandler.obtainMessage();
                                        VideoFilterCreateModel model = new VideoFilterCreateModel(rcrtcVideoFrame.getWidth(), rcrtcVideoFrame.getHeight(), rcrtcVideoFrame.getTextureId());
                                        message.what = EVENT_MESSAGE_CREATE_BEAUTY_FILTER;
                                        message.obj = model;
                                        mVideoFilterHandler.sendMessage(message);
                                    } else {
                                        //调用Filter对视频进行美颜处理
                                        rcrtcVideoFrame.setTextureId(imageFilter.draw(rcrtcVideoFrame.getWidth(), rcrtcVideoFrame.getHeight(), rcrtcVideoFrame.getTextureId()));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "processVideoFrame: " + e.getMessage());
                                }
                            }
                            return rcrtcVideoFrame;
                        }
                    });
                }
            });
        }

        @Override
        public void onPublishFailed() {

        }

        @Override
        public void onSubscribeSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                }
            });
        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);

                }
            });
        }

        @Override
        public void onSubscribeFailed() {

        }

        @Override
        public void onSetMixLayoutSuccess(final String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMixlayout.setText(s);
                }
            });
        }

        @Override
        public void onSetMixLayoutFailed(RTCErrorCode code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onUserJoined(final RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveActivity.this, ("用户:" + rcrtcRemoteUser.getUserId() + "加入会议"),
                            Toast.LENGTH_LONG).show();
                }
            });

        }

        @Override
        public void onUserLeft(final RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveActivity.this, ("用户:" + rcrtcRemoteUser.getUserId() + "退出会议"),
                            Toast.LENGTH_LONG).show();
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                }
            });
        }

        @Override
        public void onPublishCustomStreamSuccess(RCRTCVideoOutputStream stream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    handleMenuFileStreamStatus(PushFileStreamStatus.Unpush);
                }
            });
        }

        @Override
        public void onPublishCustomStreamFailed(RTCErrorCode code) {
            List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
            List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
            mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
            updateVideoView(outputStreams, inputStreams);
            handleMenuFileStreamStatus(PushFileStreamStatus.Push);
        }

        @Override
        public void onUnpublishCustomStreamSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    handleMenuFileStreamStatus(PushFileStreamStatus.Push);
                }
            });

        }

        @Override
        public void onUnpublishCustomStreamFailed(RTCErrorCode code) {
            List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
            List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
            mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
            updateVideoView(outputStreams, inputStreams);
            handleMenuFileStreamStatus(PushFileStreamStatus.Unpush);
        }

        @Override
        public void onPublishUsbStreamSuccess(RCRTCVideoOutputStream stream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    handleMenuUsbStreamStatus(PushFileStreamStatus.Unpush);

                }
            });
        }

        @Override
        public void onPublishUsbStreamFailed(RTCErrorCode code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    handleMenuUsbStreamStatus(PushFileStreamStatus.Push);
                }
            });
        }

        @Override
        public void onUnpublishUsbStreamSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    handleMenuUsbStreamStatus(PushFileStreamStatus.Push);
                }
            });
        }

        @Override
        public void onUnpublishUsbStreamFailed(RTCErrorCode code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    mAnchorPresenter.getVideoStream(outputStreams, inputStreams);
                    updateVideoView(outputStreams, inputStreams);
                    handleMenuUsbStreamStatus(PushFileStreamStatus.Unpush);
                }
            });
        }

    }

    class AudienceStatus implements IStatus, LiveAudiencePresenter.LiveCallback {

        @Override
        public void attach() {
            mAudiencePresenter.attachView(this);
            mAudiencePresenter.config(LiveActivity.this);
            mAudiencePresenter.joinRoom(mRoomId);
        }

        @Override
        public void detach() {
            mAudiencePresenter.leaveRoom();
            mAudiencePresenter.detachView();
        }

        @Override
        public void config() {
            mAudiencePresenter.config(LiveActivity.this);
        }

        @Override
        public void joinRoom(String roomId) {
            mAudiencePresenter.joinRoom(roomId);
        }

        @Override
        public void leaveRoom() {
            mAudiencePresenter.leaveRoom();
        }

        @Override
        public void publishDefaultAVStream() {
        }

        @Override
        public void subscribeAVStream() {
            mAudiencePresenter.subscribeAVStream();
        }

        @Override
        public void requestSpeak() {
        }

        @Override
        public void downSpeak() {
        }

        @Override
        public void changeUi() {
            mRequestLive.setVisibility(View.VISIBLE);
            mCamera.setVisibility(View.INVISIBLE);
            mMic.setVisibility(View.INVISIBLE);
            mSwitch.setVisibility(View.INVISIBLE);
            mEndLive.setVisibility(View.INVISIBLE);
            mMixlayout.setVisibility(View.INVISIBLE);
        }

        @Override
        public void mixLayout(String str) {
        }

        @Override
        public void publishCustomStream(String filePath) {
        }

        @Override
        public void unpublishCustomStream() {
        }

        @Override
        public void publishUsbCameraStream() {
        }

        @Override
        public void unpublishUsbCameraStream() {
        }

        @Override
        public void onJoinRoomSuccess(RCRTCRoom rcrtcRoom) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    curStatus = mAudienceStatus;
                    curStatus.changeUi();
                    // 主动订阅
                    AudienceStatus.this.subscribeAVStream();
                }
            });
        }

        @Override
        public void onJoinRoomFailed(RTCErrorCode rtcErrorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            curStatus = BakStatus;
                            curStatus.changeUi();
                        }
                    });
                }
            });
        }

        @Override
        public void onSubscribeSuccess(final List<RCRTCInputStream> inputStreamList) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<RCRTCVideoOutputStream> outputStreams = new ArrayList<>();
                    List<RCRTCVideoInputStream> inputStreams = new ArrayList<>();
                    for (RCRTCInputStream inputStream : inputStreamList) {
                        if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                            inputStreams.add((RCRTCVideoInputStream) inputStream);
                            break;
                        }
                    }
                    updateVideoView(null, inputStreams);
                }
            });
        }

        @Override
        public void onSubscribeFailed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveActivity.this, ("onSubscribeFailed"), Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> list) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AudienceStatus.this.subscribeAVStream();
                }
            });
        }

        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
        }

        @Override
        public void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveActivity.this, ("onUserJoined"), Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveActivity.this, ("onUserLeft"), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}