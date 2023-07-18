/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.screenshare.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import cn.rongcloud.rtc.api.stream.RCRTCScreenShareAudioConfig;
import cn.rongcloud.rtc.api.stream.RCRTCScreenShareAudioConfig.Builder;
import cn.rongcloud.rtc.api.stream.RCRTCScreenShareOutputStream;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCScreenShareAudioUsage;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.demo.common.UiUtils;
import cn.rongcloud.demo.screenshare.R;
import cn.rongcloud.demo.screenshare.ui.model.VideoViewWrapper;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.core.RendererCommon;
import io.rong.imlib.RongIMClient;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MeetingActivity extends AppCompatActivity {

    public static final String INTERFACE_NAME_JOIN_ROOM = "INTERFACE_NAME_JOIN_ROOM";
    public static final String INTERFACE_NAME_LEAVE_ROOM = "INTERFACE_NAME_LEAVE_ROOM";
    public static final String INTERFACE_NAME_PUSH_STREAM = "INTERFACE_NAME_PUSH_STREAM";
    public static final String INTERFACE_NAME_SUBSCRIBE_STREAM = "INTERFACE_NAME_SUBSCRIBE_STREAM";
    public static final String INTERFACE_NAME_START_DESKTOP_STREAM =
            "INTERFACE_NAME_START_DESKTOP_STREAM";
    public static final String INTERFACE_NAME_STOP_DESKTOP_STREAM =
            "INTERFACE_NAME_STOP_DESKTOP_STREAM";
    private static final String TAG = "VideoMeetingActivity";
    private static final int REQUEST_CODE = 10000;


    private static final String KEY_MEETING_NUMBER = "KEY_MEETING_NUMBER";
    /**
     * 普通模式
     */
    private static final int MODEL_NORMAL = 0;
    /**
     * 全屏模式
     */
    private static final int MODEL_FULL_SCREEN = 1;
    private final List<VideoViewWrapper> mVideoViewList = new ArrayList<>();
    private final Map<String, VideoViewWrapper> mVideoViewWrapperMap = new HashMap<>();
    //当前会议的会议号码
    private String mMeetingNumber = null;
    // 视频容器数组
    private ViewGroup[] videoContainerArray = null;
    // 全部约束关系数组
    private ConstraintSet[] mConstraintSets = null;
    // 记录全屏状态时，全屏的 video
    private VideoViewWrapper currentFullVideoWrapper = null;
    // 当前的显示模式
    private int currentModel = MODEL_NORMAL;
    private ConstraintLayout mVideosView;
    private MenuItem mDesktopSharingItem;
    private ServiceConnection serviceConnection;
    // 记录是否已经绑定了Service
    private volatile boolean isBindDesktopShareService = false;
    private RCRTCRoom mRoom;
    private volatile boolean isDesktopSharing = false;
    private View mIvHangupButton;
    private View mIvMuteButton;
    private View mIvEarMonitoring;

    public static void startActivity(@NonNull Context context, @NonNull String meetingNumber) {
        Intent intent = new Intent(context, MeetingActivity.class);
        intent.putExtra(KEY_MEETING_NUMBER, meetingNumber);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_meeting);
        initValue();
        initActionBar();
        initMediaConfig();
        initView();
        initListener();
        initRoom();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        unbindDesktopShare();
        leaveRoom();
        super.onDestroy();
    }


    private void initRoom() {
        joinRoom(mMeetingNumber);
    }

    private void initListener() {
        for (ViewGroup videoContainer : videoContainerArray) {
            videoContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchScreenModel(v);
                }
            });
        }
        mIvHangupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mIvMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = v.isSelected();
                v.setSelected(!isSelected);
                RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(v.isSelected());
            }
        });

        mIvEarMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = v.isSelected();
                v.setSelected(!isSelected);
                RCRTCEngine.getInstance().getDefaultAudioStream().enableEarMonitoring(v.isSelected());
            }
        });
    }

    private void initView() {
        initVideosView();
        initConstraintSet();
        mVideosView = findViewById(R.id.cl_videos);
        initFunctionButton();
        initLocalVideoView();
    }

    private void initFunctionButton() {
        mIvHangupButton = findViewById(R.id.iv_hangup);
        mIvMuteButton = findViewById(R.id.iv_mute);
        mIvEarMonitoring = findViewById(R.id.iv_ear_monitoring);
    }

    private void initMediaConfig() {
        String manufacturer = Build.MANUFACTURER.trim();
        int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        //华为和vivo使用mic采集声音
        if (manufacturer.contains("HUAWEI") || manufacturer.contains("vivo")) {
            audioSource = MediaRecorder.AudioSource.MIC;
        }
        // 使用硬编硬解
        RCRTCConfig config = RCRTCConfig.Builder.create()
                .enableHardwareDecoder(true)
                .enableHardwareEncoder(true)
                .setAudioSource(audioSource)
                .build();

        RCRTCEngine.getInstance().init(getApplicationContext(), config);
        // 初始化本地视频流配置
        RCRTCVideoStreamConfig videoStreamConfig = RCRTCVideoStreamConfig.Builder.create()
                .setVideoResolution(RCRTCParamsType.RCRTCVideoResolution.RESOLUTION_720_1280)
                .setVideoFps(RCRTCParamsType.RCRTCVideoFps.Fps_30)
                .setMinRate(250)
                .setMaxRate(2200)
                .build();
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoStreamConfig);
        // 听筒播放
        RCRTCEngine.getInstance().enableSpeaker(false);

    }

    private void initVideosView() {
        FrameLayout flVideo0 = findViewById(R.id.fl_video0);
        FrameLayout flVideo1 = findViewById(R.id.fl_video1);
        FrameLayout flVideo2 = findViewById(R.id.fl_video2);
        FrameLayout flVideo3 = findViewById(R.id.fl_video3);

        videoContainerArray = new FrameLayout[]{flVideo0, flVideo1, flVideo2, flVideo3};
    }

    /**
     * 初始化约束关系
     */
    private void initConstraintSet() {
        // 初始化各种视频数量所用的约束
        ConstraintSet video1 = new ConstraintSet();
        ConstraintSet video2 = new ConstraintSet();
        ConstraintSet video3 = new ConstraintSet();
        ConstraintSet video4 = new ConstraintSet();

        // 初始化各个视频数量时的布局约束关系
        video1.clone(this, R.layout.layout_meeting_video1);
        video2.clone(this, R.layout.layout_meeting_video2);
        video3.clone(this, R.layout.layout_meeting_video3);
        video4.clone(this, R.layout.layout_meeting_video4);

        mConstraintSets = new ConstraintSet[]{video1, video2, video3, video4};
    }

    /**
     * 初始化数据
     */
    private void initValue() {
        mMeetingNumber = getIntent().getStringExtra(KEY_MEETING_NUMBER);
    }

    /**
     * 初始化 ActionBar
     */
    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("会议:" + mMeetingNumber);
        }
    }

    /**
     * VideoList 发生变化
     *
     * @param videoViewList 视频视图列表
     */
    private void onShowVideoViewChange(@NonNull final List<VideoViewWrapper> videoViewList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentModel == MODEL_NORMAL) {
                    resetLayoutByVideos(videoViewList);
                }
            }
        });
    }
    /**
     * 根据 Video 数量重新布局
     *
     * @param videoViewList 视频视图列表
     */
    private void resetLayoutByVideos(List<VideoViewWrapper> videoViewList) {
        for (int i = 0; i < videoContainerArray.length; i++) {
            ViewGroup container = videoContainerArray[i];
            if (i < videoViewList.size()) {
                VideoViewWrapper videoViewWrapper = videoViewList.get(i);
                if (container.getTag() != videoViewWrapper) {
                    ViewParent parent = videoViewWrapper.getRCRTCVideoView().getParent();
                    if (parent != null) {
                        ((ViewGroup) parent).removeView(videoViewWrapper.getRCRTCVideoView());
                    }
                    container.removeAllViews();
                    container.setTag(null);
                    container.addView(videoViewWrapper.getRCRTCVideoView(),
                        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    container.setTag(videoViewWrapper);
                }
            } else {
                container.removeAllViews();
                container.setTag(null);
            }
        }
        resetVideosLayout(videoViewList.size());
    }

    /**
     * 移除VideoView
     *
     * @param videoViewWrapper 视频视图包装对象
     */
    private void removeVideoView(@NonNull VideoViewWrapper videoViewWrapper) {
        if (currentModel == MODEL_FULL_SCREEN) {
            //全屏模式下判断移除的视频是否为当前全屏的视频
            if (currentFullVideoWrapper != null
                    && videoViewWrapper.getRCRTCVideoView() == currentFullVideoWrapper.getRCRTCVideoView()) {
                mVideosView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetLayoutByVideos(mVideoViewList);
                    }
                }, 500);

            }
        }
    }

    /**
     * 根据当前video数量重新设置约束关系
     *
     * @param videoCount 视频数量
     */
    private void resetVideosLayout(int videoCount) {
        if (videoCount > 0) {
            TransitionManager.beginDelayedTransition(mVideosView);
            ConstraintSet constraintSet;
            if (videoCount <= mConstraintSets.length) {
                constraintSet = mConstraintSets[videoCount - 1];
            } else {
                constraintSet = mConstraintSets[mConstraintSets.length - 1];
            }
            constraintSet.applyTo(mVideosView);
        }
    }

    /**
     * 切换屏幕模式
     *
     * @param view 被点击的 View
     */
    private void switchScreenModel(View view) {
        //没有或只有一个视频时不允许切换
        if (mVideoViewList.size() <= 1) {
            return;
        }
        if (currentModel == MODEL_NORMAL) {
            switchFullScreenModel(view);
        } else {
            switchNormal();
        }
    }

    /**
     * 切换到全屏模式
     *
     * @param view 被点击的 View
     */
    private void switchFullScreenModel(View view) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.layout_meeting_full_screen);
        for (ViewGroup viewGroup : videoContainerArray) {
            if (viewGroup == view) {
                //设置点击的视频为全屏
                constraintSet.connect(view.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID,
                        ConstraintSet.START);
                constraintSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP);
                constraintSet.connect(view.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID,
                        ConstraintSet.END);
                constraintSet.connect(view.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID
                        , ConstraintSet.BOTTOM);
                constraintSet.constrainPercentHeight(view.getId(), 1);
                constraintSet.constrainDefaultWidth(view.getId(), 1);
                currentFullVideoWrapper = (VideoViewWrapper) viewGroup.getTag();
            }
        }
        TransitionManager.beginDelayedTransition(mVideosView);
        constraintSet.applyTo(mVideosView);
        currentModel = MODEL_FULL_SCREEN;
    }

    /**
     * 切换到普通模式
     */
    private void switchNormal() {
        resetLayoutByVideos(mVideoViewList);
        currentFullVideoWrapper = null;
        currentModel = MODEL_NORMAL;
    }

    /**
     * 处理桌面共享事件
     */
    private void handlerDesktopSharing() {
        if (isDesktopSharing) {
            UiUtils.showWaitingDialog(this);
            stopDesktopSharing();
        } else {
            requestScreenCapture();
        }
    }

    /**
     * 请求桌面录制权限
     * todo 开发者文档地址为：https://doc.rongcloud.cn/meeting/Android/5.X/advance/screen-share#captureScreen
     */
    private void requestScreenCapture() {
        RCRTCVideoStreamConfig.Builder builder = RCRTCVideoStreamConfig.Builder.create();
        builder.setVideoFps(RCRTCVideoFps.Fps_15);
        builder.setVideoResolution(RCRTCVideoResolution.RESOLUTION_720_1280);
        builder.setMaxRate(2500);
        RCRTCEngine.getInstance().getScreenShareVideoStream().setVideoConfig(builder.build());
        RCRTCEngine.getInstance().getScreenShareVideoStream().startCaptureScreen(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "capture screen onSuccess: []");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                            RCRTCScreenShareAudioConfig build = Builder.create()
                                .addMatchingUsage(RCRTCScreenShareAudioUsage.MEDIA)
                                .addMatchingUsage(RCRTCScreenShareAudioUsage.GAME)
                                .addMatchingUsage(RCRTCScreenShareAudioUsage.UNKNOWN)
                                .build();
                            RCRTCEngine.getInstance().getScreenShareVideoStream().startCaptureAudio(build, new IRCRTCResultCallback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onFailed(RTCErrorCode errorCode) {
                                }
                            });
                        }
                    }
                });
                mRoom.getLocalUser().publishStream(RCRTCEngine.getInstance().getScreenShareVideoStream(), new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        onDesktopShareSuccess();
                        initScreenShareVideoView();
                        isDesktopSharing = true;
                        Log.d(TAG, "publish screen onSuccess: ");
                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        Log.d(TAG, "publish onFailed: ");
                        RCRTCEngine.getInstance().getScreenShareVideoStream().stopCaptureScreen();
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                Log.d(TAG, "onFailed: ");
            }
        });
    }


    private void onDesktopShareSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDesktopSharingItem.setTitle("结束共享");
                showToast("屏幕共享成功");
            }
        });
    }

    /**
     * 统一的错误回调
     *
     * @param interfaceName 发生错误的接口回调名
     * @param rtcErrorCode  错误信息
     */
    private void onFiledCallBack(@NonNull String interfaceName,
                                 @NonNull RTCErrorCode rtcErrorCode) {
        switch (interfaceName) {
            case INTERFACE_NAME_JOIN_ROOM:
                Log.d(TAG, "加入房间失败 : " + rtcErrorCode.getReason());
                finish();
                break;
            case INTERFACE_NAME_LEAVE_ROOM:
                Log.d(TAG, "离开房间失败 : " + rtcErrorCode.getReason());
                break;
            case INTERFACE_NAME_START_DESKTOP_STREAM:
                Log.d(TAG, "屏幕共享失败 : " + rtcErrorCode.getReason());
                unbindDesktopShare();
                break;
            case INTERFACE_NAME_STOP_DESKTOP_STREAM:
                Log.d(TAG, "屏幕共享结束失败 : " + rtcErrorCode.getReason());
                //强制设置为可以重新发起共享
                onDesktopShareStopSuccess();
                break;
            default:
                throw new IllegalArgumentException("Unknown parameter exception");
        }
    }

    private void unbindDesktopShare() {
        if (serviceConnection != null && isBindDesktopShareService) {
            unbindService(serviceConnection);
            isBindDesktopShareService = false;
        }
    }

    private void onDesktopShareStopSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unbindDesktopShare();
                mDesktopSharingItem.setTitle("屏幕共享");
                showToast("屏幕共享结束");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //添加桌面共享按钮
            getMenuInflater().inflate(R.menu.desktop_sharing_menu, menu);
            mDesktopSharingItem = menu.findItem(R.id.desktop_share);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 返回按钮响应事件
            finish();
            return true;
        } else if (item.getItemId() == R.id.desktop_share) {
            // 响应"桌面共享"点击事件
            handlerDesktopSharing();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 离开房间
     */
    private void leaveRoom() {
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                RCRTCEngine.getInstance().unInit();
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                Log.e(TAG, "leaveRoom:onFailed: " + rtcErrorCode.getReason());
                onFiledCallBack(INTERFACE_NAME_LEAVE_ROOM, rtcErrorCode);
            }
        });
    }


    /**
     * 加入会议
     *
     * @param roomId 房间 Id
     */
    private void joinRoom(@NonNull String roomId) {
        mMeetingNumber = roomId;
        UiUtils.showWaitingDialog(this);
        RCRTCEngine.getInstance().joinRoom(roomId, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom rcrtcRoom) {
                UiUtils.hideWaitingDialog();
                mRoom = rcrtcRoom;
                afterJoinRoomSuccess(rcrtcRoom);
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                Log.e(TAG, "joinRoom:onFailed: " + rtcErrorCode.getReason());
                UiUtils.hideWaitingDialog();
                onFiledCallBack(INTERFACE_NAME_JOIN_ROOM, rtcErrorCode);
            }
        });
    }

    /**
     * 处理入会成功后流程
     */
    private void afterJoinRoomSuccess(RCRTCRoom rcrtcRoom) {
        // 注册回调
        rcrtcRoom.registerRoomListener(new MyIRCRTCRoomEventsListener());
        // 开始推流
        publishDefaultAVStream(rcrtcRoom);
        // 订阅用户资源
        subscribeAVStream(rcrtcRoom);
    }

    /**
     * 发布默认视频流
     */
    private void publishDefaultAVStream(RCRTCRoom room) {
        room.getLocalUser().publishDefaultStreams(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                Log.e(TAG,
                        "publishDefaultAVStream:publishDefaultStreams:onFailed: " + errorCode.getReason());
                onFiledCallBack(INTERFACE_NAME_PUSH_STREAM, errorCode);
            }
        });
    }

    /**
     * 订阅已经在房间内的用户发布的资源
     */
    private void subscribeAVStream(RCRTCRoom rtcRoom) {
        if (rtcRoom == null || rtcRoom.getRemoteUsers() == null) {
            return;
        }
        List<RCRTCInputStream> inputStreams = new ArrayList<>();
        for (final RCRTCRemoteUser remoteUser : rtcRoom.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            List<RCRTCInputStream> userStreams = remoteUser.getStreams();
            for (RCRTCInputStream inputStream : userStreams) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    //选择订阅大流或是小流。默认小流
                    ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                    //创建VideoView并设置到stream
                    VideoViewWrapper videoViewWrapper =
                            createVideoViewByStreamId(inputStream.getStreamId(),
                                    remoteUser.getUserId());
                    ((RCRTCVideoInputStream) inputStream).setVideoView(videoViewWrapper.getRCRTCVideoView());
                    //将远端视图添加至布局
                    addVideoViewToList(videoViewWrapper);
                    onShowVideoViewChange(mVideoViewList);
                }
            }
            inputStreams.addAll(remoteUser.getStreams());
        }
        if (inputStreams.size() > 0) {
            rtcRoom.getLocalUser().subscribeStreams(inputStreams, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "subscribeStreams:onSuccess: ");
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    Log.d(TAG, "subscribeStreams:onFailed: " + errorCode.getReason());
                    onFiledCallBack(INTERFACE_NAME_SUBSCRIBE_STREAM, errorCode);
                }
            });
        }
    }

    private synchronized void addVideoViewToList(VideoViewWrapper videoViewWrapper) {
        mVideoViewList.add(videoViewWrapper);
        mVideoViewWrapperMap.put(videoViewWrapper.getStreamId(), videoViewWrapper);
    }

    private synchronized void removeVideoViewByStreamId(String streamId) {
        if (!TextUtils.isEmpty(streamId)) {
            VideoViewWrapper videoViewWrapper = mVideoViewWrapperMap.get(streamId);
            mVideoViewWrapperMap.remove(streamId);
            mVideoViewList.remove(videoViewWrapper);
        }
    }

    private void removeVideoByRemoteUser(RCRTCRemoteUser user) {
        if (user != null) {
            removeVideoVideoByUserId(user.getUserId());
            onShowVideoViewChange(mVideoViewList);
        }
    }

    private synchronized void removeVideoVideoByUserId(String userId) {
        if (!TextUtils.isEmpty(userId)) {
            List<VideoViewWrapper> list = new ArrayList<>();
            for (VideoViewWrapper videoViewWrapper : mVideoViewList) {
                if (userId.equals(videoViewWrapper.getUserId())) {
                    list.add(videoViewWrapper);
                }
            }
            for (VideoViewWrapper videoViewWrapper : list) {
                removeVideoView(videoViewWrapper);
                removeVideoByStreamId(videoViewWrapper.getStreamId());
            }
        }
    }

    private void removeVideoByStreamId(String streamId) {
        VideoViewWrapper videoViewWrapper = mVideoViewWrapperMap.get(streamId);
        if (videoViewWrapper != null) {
            removeVideoView(videoViewWrapper);
        }
        removeVideoViewByStreamId(streamId);

    }

    private VideoViewWrapper createVideoViewByStreamId(String streamId, String userId) {
        final RCRTCVideoView videoView = new RCRTCVideoView(getApplicationContext());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            }
        });

        return new VideoViewWrapper(userId, streamId, videoView);
    }

    /**
     * 初始化本地视频
     */
    private void initLocalVideoView() {
        RCRTCVideoView localVideoView = new RCRTCVideoView(getApplicationContext());
        VideoViewWrapper videoViewWrapper =
                new VideoViewWrapper(RongIMClient.getInstance().getCurrentUserId(), localVideoView);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(localVideoView);
        //确保本地视频始终处于第一个
        synchronized (this) {
            mVideoViewList.add(0, videoViewWrapper);
            mVideoViewWrapperMap.put(videoViewWrapper.getStreamId(), videoViewWrapper);
        }
        onShowVideoViewChange(mVideoViewList);
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
    }

    private void initScreenShareVideoView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView localVideoView = new RCRTCVideoView(getApplicationContext());
                localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                VideoViewWrapper videoViewWrapper =
                        new VideoViewWrapper(RCRTCEngine.getInstance().getScreenShareVideoStream().getStreamId(), localVideoView);
                //确保本地视频始终处于第一个
                synchronized (this) {
                    mVideoViewList.add(videoViewWrapper);
                    mVideoViewWrapperMap.put(videoViewWrapper.getStreamId(), videoViewWrapper);
                }
                onShowVideoViewChange(mVideoViewList);
                RCRTCEngine.getInstance().getScreenShareVideoStream().setVideoView(localVideoView);
            }
        });
    }

    private void unInitScreenShareVideoView(){

        VideoViewWrapper viewWrapper =
                mVideoViewWrapperMap.remove(RCRTCEngine.getInstance().getScreenShareVideoStream().getStreamId());
        Log.d(TAG, "unInitScreenShareVideoView: viewList: " + mVideoViewList.size());
        if (viewWrapper != null){
            mVideoViewList.remove(viewWrapper);
            Log.d(TAG, "unInitScreenShareVideoView: after size: " + mVideoViewList.size());
            onShowVideoViewChange(mVideoViewList);
        }
    }

    /**
     * 结束桌面共享
     */
    private void stopDesktopSharing() {
        mRoom.getLocalUser().unpublishStream(RCRTCEngine.getInstance().getScreenShareVideoStream(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                UiUtils.hideWaitingDialog();
                isDesktopSharing = false;
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                UiUtils.hideWaitingDialog();
                isDesktopSharing = false;
            }
        });
        RCRTCEngine.getInstance().getScreenShareVideoStream().stopCaptureScreen();
        unInitScreenShareVideoView();
        onDesktopShareStopSuccess();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 房间事件回调
     */
    private class MyIRCRTCRoomEventsListener extends IRCRTCRoomEventsListener {

        /**
         * 远端用户发布资源通知
         *
         * @param remoteUser 远端用户
         * @param streams    发布的资源
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser remoteUser,
                                                List<RCRTCInputStream> streams) {
            for (RCRTCInputStream inputStream : streams) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    // 选择订阅大流或是小流。默认小流
                    ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                    // 创建VideoView并设置到stream
                    VideoViewWrapper videoViewWrapper =
                            createVideoViewByStreamId(inputStream.getStreamId(),
                                    remoteUser.getUserId());
                    ((RCRTCVideoInputStream) inputStream).setVideoView(videoViewWrapper.getRCRTCVideoView());
                    // 将远端视图添加至布局
                    addVideoViewToList(videoViewWrapper);
                    onShowVideoViewChange(mVideoViewList);
                }
            }
            mRoom.getLocalUser().subscribeStreams(streams
                    , new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "subscribeStreams:onSuccess: ");
                        }

                        @Override
                        public void onFailed(RTCErrorCode rtcErrorCode) {
                            Log.d(TAG, "subscribeStreams:onFailed: " + rtcErrorCode.getReason());
                        }
                    });
        }


        /**
         * 远端用户音频静默改变通知
         *
         * @param remoteUser 远端用户
         * @param stream     音频流
         * @param mute       true表示静音，false表示取消静音
         */
        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser remoteUser, RCRTCInputStream stream,
                                          boolean mute) {
        }


        /**
         * 远端用户视频静默改变通知
         *
         * @param remoteUser 远端用户
         * @param stream     视频流
         * @param mute       true表示关闭，false表示打开
         */
        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser remoteUser, RCRTCInputStream stream,
                                          boolean mute) {

        }


        /**
         * 远端用户取消发布资源通知
         *
         * @param remoteUser 远端用户
         */
        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser remoteUser,
                                                  List<RCRTCInputStream> streams) {
            Log.d(TAG, "onRemoteUserUnpublishResource: ");
            for (RCRTCInputStream stream : streams) {
                removeVideoByStreamId(stream.getStreamId());
            }
            onShowVideoViewChange(mVideoViewList);
        }


        /**
         * 远端用户加入通知
         *
         * @param remoteUser 远端用户
         */
        @Override
        public void onUserJoined(RCRTCRemoteUser remoteUser) {

        }


        /**
         * 远端用户离开通知
         *
         * @param remoteUser 远端用户
         */
        @Override
        public void onUserLeft(RCRTCRemoteUser remoteUser) {
            Log.d(TAG, "onUserLeft: ");
            removeVideoByRemoteUser(remoteUser);
        }


        /**
         * 远端用户掉线通知
         *
         * @param remoteUser 远端用户
         */
        @Override
        public void onUserOffline(RCRTCRemoteUser remoteUser) {
            removeVideoByRemoteUser(remoteUser);
        }


        /**
         * 直播混合流资源发布
         * Add from 5.0.0
         *
         * @param streams 直播混合流资源列表
         */
        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> streams) {

        }


        /**
         * 直播混合流资源取消发布
         * Add from 5.0.0
         *
         * @param streams 直播混合流资源列表
         */
        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> streams) {

        }

        @Override
        public void onLeaveRoom(int i) {

        }
    }

}