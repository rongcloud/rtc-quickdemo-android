package cn.rongcloud.demo.cdn;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Bundle;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.stream.RCRTCCDNInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCLiveRole;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import java.util.List;

/**
 * CDN直播拉流-观众页面
 */
public class AudienceActivity extends Base {

    public static final String KEY_ROOM_NUMBER = "room_number";
    private String mRoomId;
    private RCRTCRoom mRTCRoom = null;
    private RelativeLayout mRelativeLayout;
    private final String ACTION_SUBSCRIBE = "订阅";
    private final String ACTION_PAUSE = "暂停";
    private final String ACTION_RESUME = "恢复";
    private Button btn_action;

    public static void start(Context context, String roomId) {
        Intent intent = new Intent(context, AudienceActivity.class);
        intent.putExtra(KEY_ROOM_NUMBER, roomId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audience);
        mRelativeLayout  = findViewById(R.id.relativeLayout);
        TextView tv_room_id = findViewById(R.id.tv_room_id);
        btn_action= findViewById(R.id.btn_action);
        Intent intent = getIntent();
        mRoomId = intent.getStringExtra(KEY_ROOM_NUMBER);
        tv_room_id.setText(mRoomId);
        joinRTCRoom();
    }

    private void joinRTCRoom() {
        RCRTCRoomConfig roomConfig = RCRTCRoomConfig.Builder.create()
            // 根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
            .setRoomType(RCRTCRoomType.LIVE_AUDIO_VIDEO)
            .setLiveRole(RCRTCLiveRole.AUDIENCE)    //以观众身份加入直播
            .build();
        showLoading();
        RCRTCEngine.getInstance().joinRoom(mRoomId, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        AudienceActivity.this.mRTCRoom = rcrtcRoom;
                        RCRTCCDNInputStream inputStream = rcrtcRoom.getCDNStream();
                        rcrtcRoom.registerRoomListener(roomEventsListener);
                        // 观众加入房间成功后。判断房间中是否已经有主播发布资源。如果有 就去订阅该主播的资源
                        if (inputStream != null) {
                            subscribeStream(inputStream, new IRCRTCResultCallback() {
                                @Override
                                public void onSuccess() {
                                    btn_action.setText(ACTION_PAUSE);
                                }

                                @Override
                                public void onFailed(RTCErrorCode rtcErrorCode) {

                                }
                            });
                        } else {
                            closeLoading();
                        }
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("加入房间失败:"+rtcErrorCode.getValue());
                closeLoading();
            }
        });
    }

    /**
     * 房间事件监听，方法说明请参考Java Doc 文档：https://www.rongcloud.cn/docs/api/android/rtclib_v5/cn/rongcloud/rtc/api/callback/IRCRTCRoomEventsListener.html
     */
    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {

        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
            showToast("用户 "+rcrtcRemoteUser.getUserId()+" "+(b?"关闭":"打开")+"麦克风");
        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
            showToast("用户 "+rcrtcRemoteUser.getUserId()+" "+(b?"关闭":"打开")+"摄像头");
        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {

        }

        @Override
        public void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser) {
            showToast("用户 "+rcrtcRemoteUser.getUserId()+" 加入房间");
        }

        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            showToast("用户 "+rcrtcRemoteUser.getUserId()+" 离开房间");
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {
            showToast("用户 "+rcrtcRemoteUser.getUserId()+" 离线退出");
        }

        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> list) {

        }

        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> list) {

        }

        @Override
        public void onPublishCDNStream(final RCRTCCDNInputStream stream) {
            super.onPublishCDNStream(stream);
            showToast("房间中有CDN流发布");
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    subscribeStream(stream, new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            btn_action.setText(ACTION_PAUSE);
                        }

                        @Override
                        public void onFailed(RTCErrorCode rtcErrorCode) {
                        }
                    });
                }
            });
        }

        @Override
        public void onUnpublishCDNStream(RCRTCCDNInputStream stream) {
            super.onUnpublishCDNStream(stream);
            showToast("房间中有CDN流取消发布");
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    mRelativeLayout.removeAllViews();
                    btn_action.setText(ACTION_SUBSCRIBE);
                }
            });
        }

        @Override
        public void onLeaveRoom(int i) {

        }
    };

    /**
     * 订阅CDN资源
     * todo 本Demo 仅演示订阅 CDN 资源
     * 对应开发者文档：https://docs.rongcloud.cn/v4/5X/views/rtc/livevideo/android/userstream/audience.html#%E8%AE%A2%E9%98%85%E8%B5%84%E6%BA%90
     *
     * @param inputStream
     */
    private void subscribeStream(RCRTCCDNInputStream inputStream, final IRCRTCResultCallback callback) {
        if (mRTCRoom == null) {
            return;
        }
        RCRTCVideoView videoView = new RCRTCVideoView(AudienceActivity.this);
        inputStream.setVideoView(videoView);
        mRelativeLayout.removeAllViews();
        mRelativeLayout.addView(videoView);
        mRTCRoom.getLocalUser().subscribeStream(inputStream, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailed(final RTCErrorCode rtcErrorCode) {
                showToast("订阅 CDN 流失败："+rtcErrorCode.getValue());
                closeLoading();
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFailed(rtcErrorCode);
                        }
                    }
                });
            }
        });
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
                        AudienceActivity.this.finish();
                    }
                });
            }

            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                        AudienceActivity.this.finish();
                    }
                });
            }
        });
    }

    /**
     * 刷新已经订阅的 CDN 资源操作为：先取消订阅CDN流、再重新订阅
     */
    private void refresh() {
        if (mRTCRoom == null || mRTCRoom.getCDNStream() == null) {
            showToast("未加入房间或房间中没有CDN资源");
            return;
        }
        showLoading();
        mRTCRoom.getLocalUser().unsubscribeStream(mRTCRoom.getCDNStream(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        subscribeStream(mRTCRoom.getCDNStream(), new IRCRTCResultCallback() {
                            @Override
                            public void onSuccess() {
                                closeLoading();
                            }

                            @Override
                            public void onFailed(RTCErrorCode rtcErrorCode) {
                                showToast("刷新失败:"+rtcErrorCode.getValue());
                                closeLoading();
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("刷新失败:"+rtcErrorCode.getValue());
                closeLoading();
            }
        });
    }

    public void clickAudience(View view) {
        int id = view.getId();
        if (id == R.id.btn_leaveRoom) {
            leaveRoom();
        } else if (id == R.id.btn_refresh) {
            refresh();
        } else if (id == R.id.btn_action) {
            if (mRTCRoom == null || mRTCRoom.getCDNStream() == null) {
                showToast("未加入房间或房间中没有CDN资源");
                return;
            }
            String text = btn_action.getText().toString().trim();
            if (TextUtils.equals(ACTION_SUBSCRIBE, text)) {
                showLoading();
                subscribeStream(mRTCRoom.getCDNStream(), new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        btn_action.setText(ACTION_PAUSE);
                    }

                    @Override
                    public void onFailed(RTCErrorCode rtcErrorCode) {
                    }
                });
            } else if (TextUtils.equals(ACTION_PAUSE, text)) {
                mRTCRoom.getCDNStream().mute(true);
                btn_action.setText(ACTION_RESUME);
            } else if (TextUtils.equals(ACTION_RESUME, text)) {
                mRTCRoom.getCDNStream().mute(false);
                btn_action.setText(ACTION_PAUSE);
            }
        } else if (id == R.id.btn_changeVideoSize) {
            if (mRTCRoom == null || mRTCRoom.getCDNStream() == null) {
                showToast("未加入房间或房间中没有CDN资源");
                return;
            }
            VideoResolutionFragment.newInstance(mRTCRoom.getCDNStream().getVideoResolution() == null ? RCRTCVideoResolution.RESOLUTION_1080_1920:mRTCRoom.getCDNStream().getVideoResolution(), new IVideoConfigListener() {
                @Override
                public void onCheckedChanged(RCRTCVideoResolution resolution) {
                    showLoading();
                    /**
                     * Added from 5.1.5
                     *
                     * 该方法有两个功能如下：
                     * 1. 当没有订阅 RCRTCCDNInputStream 流时，订阅前指定的分辨率、帧率。
                     * 2. 已经订阅 RCRTCCDNInputStream 时调用，切换已经订阅 RCRTCCDNInputStream 的分辨率、帧率。
                     *
                     * @param videoResolution 默认为null。为null时默认订阅原始 CDN 流分辨率
                     * @param videoFps 默认为null。为null时默认订阅原始 CDN 流帧率
                     */
                    mRTCRoom.getCDNStream().setVideoConfig(resolution, RCRTCVideoFps.Fps_30, new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            closeLoading();
                        }

                        @Override
                        public void onFailed(RTCErrorCode rtcErrorCode) {
                            closeLoading();
                            showToast("切换分辨率失败:"+rtcErrorCode.getValue());
                        }
                    });
                }
            }).show(getFragmentManager(),"VideoResolutionFragment");
        }
    }
}