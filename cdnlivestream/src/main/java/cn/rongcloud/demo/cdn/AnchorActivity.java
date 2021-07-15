package cn.rongcloud.demo.cdn;

import static cn.rongcloud.rtc.base.RCRTCLiveRole.BROADCASTER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCMixConfig;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;

/**
 * CDN直播拉流-主播页面
 */
public class AnchorActivity extends Base {

    public static final String KEY_ROOM_NUMBER = "room_number";
    private static final String TAG = "AnchorActivity";
    private String mRoomId;
    private RCRTCLiveInfo mLiveInfo = null;
    private FrameLayout mFrameLayout = null;
    private Button btn_enableInnerCDN;
    // 主播端 开关内置 CDN 功能， Demo 中 Button 显示字符
    private static final String ENABLE_INNER_CDN = "开始推流到CDN";
    private static final String DISABLE_INNER_CDN = "停止推流到CDN";

    public static void start(Context context, String roomId) {
        Intent intent = new Intent(context, AnchorActivity.class);
        intent.putExtra(KEY_ROOM_NUMBER, roomId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anchor);
        Intent intent = getIntent();
        TextView tv_room_id = findViewById(R.id.tv_room_id);
        mRoomId = intent.getStringExtra(KEY_ROOM_NUMBER);
        mFrameLayout = findViewById(R.id.frameLayout);
        btn_enableInnerCDN = findViewById(R.id.btn_enableInnerCDN);
        tv_room_id.setText(mRoomId);
        joinRTCRoomAndPublish();
    }

    /**
     * 主播加入房间并发布直播资源
     *
     * todo 本 demo 主播端仅展示本地视图，其他主播加入不订阅和渲染
     */
    private void joinRTCRoomAndPublish() {
        //开启摄像头
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        RCRTCVideoView videoView = new RCRTCVideoView(AnchorActivity.this);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(videoView);
        mFrameLayout.removeAllViews();
        // 设置本地渲染视图
        mFrameLayout.addView(videoView);

        RCRTCRoomConfig roomConfig = RCRTCRoomConfig.Builder.create()
            // 根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
            .setRoomType(RCRTCRoomType.LIVE_AUDIO_VIDEO)
            .setLiveRole(BROADCASTER)   // 以主播身份加入房间
            .build();
        showLoading();
        RCRTCEngine.getInstance().joinRoom(mRoomId, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //todo 主播发布直播资源,对应开发者文档：https://docs.rongcloud.cn/v4/5X/views/rtc/livevideo/android/userstream/broadcaster.html#%E5%8F%91%E5%B8%83%E8%B5%84%E6%BA%90
                        rcrtcRoom.getLocalUser().publishDefaultLiveStreams(new IRCRTCResultDataCallback<RCRTCLiveInfo>() {
                            @Override
                            public void onSuccess(final RCRTCLiveInfo rcrtcLiveInfo) {
                                postUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeLoading();
                                        AnchorActivity.this.mLiveInfo = rcrtcLiveInfo;
                                        setMixConfig();
                                    }
                                });
                            }

                            @Override
                            public void onFailed(RTCErrorCode rtcErrorCode) {
                                showToast("发布资源失败:"+rtcErrorCode.getValue());
                                postUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeLoading();
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("加入房间失败:"+rtcErrorCode.getValue());
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                    }
                });
            }
        });
    }

    /**
     * 页面中按钮点击事件
     * @param view
     */
    public void clickAnchor(View view) {
        if (view.getId() == R.id.btn_leaveRoom) {
            leaveRoom();
        } else if(view.getId() == R.id.btn_enableInnerCDN) {
            if (mLiveInfo == null) {
                showToast("主播未发布直播流或发布直播流失败");
                return;
            }
            showLoading();
            final boolean enable = setInnerCDN();
            mLiveInfo.enableInnerCDN(enable, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    showToast((enable?"开启":"关闭")+"内置CDN成功");
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_enableInnerCDN.setText(enable?DISABLE_INNER_CDN:ENABLE_INNER_CDN);
                            closeLoading();
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
                    switch (rtcErrorCode.getValue()) {
                        case 48006:
                        case 48007:
                            showToast("您已在后台配置了自动推CDN ，无需手动推CDN");
                            break;
                        default:
                            showToast((enable?"开启":"关闭")+"内置CDN失败 : "+rtcErrorCode.getValue());
                            break;
                    }
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            closeLoading();
                        }
                    });
                }
            });
        }
    }

    private boolean setInnerCDN() {
        String text = btn_enableInnerCDN.getText().toString().trim();
        boolean enable = false;
        if (text.equals(ENABLE_INNER_CDN)) {
            enable = true;
        } else if (text.equals(DISABLE_INNER_CDN)) {
            enable = false;
        }
        return enable;
    }

    private void leaveRoom() {
        showLoading();
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                        AnchorActivity.this.finish();
                    }
                });
            }

            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                        AnchorActivity.this.finish();
                    }
                });
            }
        });
    }

    /**
     * todo 设置合流分辨率，对应开发者文档为：https://docs.rongcloud.cn/v4/5X/views/rtc/livevideo/android/userstream/publishstreammanage.html
     * Demo 演示 主播发布直播流成功后，设置推送给CDN的分辨率、帧率为 1080P 30FPS
     */
    private void setMixConfig() {
        if (mLiveInfo == null) {
            showToast("没有发布流");
            return;
        }
        RCRTCMixConfig config = new RCRTCMixConfig();
        RCRTCMixConfig.MediaConfig mediaConfig = new RCRTCMixConfig.MediaConfig();
        config.setMediaConfig(mediaConfig);
        //视频输出配置
        RCRTCMixConfig.MediaConfig.VideoConfig videoConfig = new RCRTCMixConfig.MediaConfig.VideoConfig();
        mediaConfig.setVideoConfig(videoConfig);
        //大流视频的输出参数
        RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout normal = new RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout();
        RCRTCVideoResolution videoResolution = RCRTCVideoResolution.RESOLUTION_1080_1920;
        normal.setWidth(videoResolution.getWidth());
        normal.setHeight(videoResolution.getHeight());
        normal.setBitrate(4000);
        normal.setFps(30);
        videoConfig.setVideoLayout(normal);

        mLiveInfo.setMixConfig(config, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                showToast("设置Config失败 : "+errorCode.getValue());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }
}