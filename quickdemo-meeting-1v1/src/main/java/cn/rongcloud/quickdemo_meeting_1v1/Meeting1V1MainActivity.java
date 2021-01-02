package cn.rongcloud.quickdemo_meeting_1v1;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.common.tools.Utils;
import cn.rongcloud.common.view.BaseActivity;
import cn.rongcloud.rtc.api.RCRTCConfig.Builder;
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
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConnectionErrorCode;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;
import java.util.ArrayList;
import java.util.List;

public class Meeting1V1MainActivity extends BaseActivity {

    private static final String ROOM_ID = "112233";
    private RCRTCRoom rcrtcRoom = null;
    private TextView tv_textView;
    private FrameLayout localUser, remoteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting1_v1_main);
        tv_textView = (TextView) findViewById(R.id.tv_textView);
        localUser = (FrameLayout) findViewById(R.id.local_user);
        remoteUser = (FrameLayout) findViewById(R.id.remote_user);
    }

    private void connect(String token) {
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            joinRoom();
            return;
        }
        RongIMClient.connect(token, new ConnectCallback() {
            @Override
            public void onSuccess(String userid) {
                setText("登录成功");
                joinRoom();
            }

            @Override
            public void onError(ConnectionErrorCode connectionErrorCode) {
                setText("登录 IM 失败 ：" + connectionErrorCode.name());
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus databaseOpenStatus) {

            }
        });
    }


    private void joinRoom() {

        Builder configBuilder = Builder.create();
        //是否硬解码
        configBuilder.enableHardwareDecoder(true);
        //是否硬编码
        configBuilder.enableHardwareEncoder(true);
        RCRTCEngine.getInstance().init(getApplicationContext(), configBuilder.build());

        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        //设置分辨率
        videoConfigBuilder.setVideoResolution(RCRTCVideoResolution.RESOLUTION_720_1280);
        //设置帧率
        videoConfigBuilder.setVideoFps(RCRTCVideoFps.Fps_30);
        //设置最小码率，480P下推荐200
        videoConfigBuilder.setMinRate(200);
        //设置最大码率，480P下推荐900
        videoConfigBuilder.setMaxRate(900);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder.build());

        // 创建本地视频显示视图
        RCRTCVideoView rongRTCVideoView = new RCRTCVideoView(getApplicationContext());
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(rongRTCVideoView);

        //TODO 将本地视图添加至FrameLayout布局，需要开发者自行创建布局
        localUser.addView(rongRTCVideoView);
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        //mRoomId,长度 64 个字符，可包含：`A-Z`、`a-z`、`0-9`、`+`、`=`、`-`、`_`
        RCRTCEngine.getInstance().joinRoom(ROOM_ID, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setText("加入房间成功");
                        Meeting1V1MainActivity.this.rcrtcRoom = rcrtcRoom;
                        rcrtcRoom.registerRoomListener(roomEventsListener);
                        //加入房间成功后，开启摄像头采集视频数据
//        RongRTCCapture.getInstance().startCameraCapture();
                        //加入房间成功后，发布默认音视频流
                        publishDefaultAVStream(rcrtcRoom);
                        //加入房间成功后，如果房间中已存在用户且发布了音、视频流，就订阅远端用户发布的音视频流.
                        subscribeAVStream(rcrtcRoom);
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                setText("加入房间失败：" + rtcErrorCode.getReason());
            }
        });
    }

    private void setText(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(str)) {
                    tv_textView.setText("");
                    return;
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(tv_textView.getText());
                stringBuffer.append("->");
                stringBuffer.append(str);
                tv_textView.setText(stringBuffer.toString());
            }
        });
    }

    private void leaveRoom() {
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localUser.removeAllViews();
                        remoteUser.removeAllViews();
                        Toast.makeText(Meeting1V1MainActivity.this, "退出成功!", Toast.LENGTH_SHORT).show();
                        setText(null);
                        rcrtcRoom = null;
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                setText("退出房间失败：" + rtcErrorCode.getReason());
            }
        });
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_user1:
                setText("当前用户1");
                connect(Utils.USER_1);
                break;
            case R.id.btn_user2:
                setText("当前用户2");
                connect(Utils.USER_2);
                break;
            case R.id.btn_leave:
                leaveRoom();
                break;
            default:
                break;
        }
    }

    private void publishDefaultAVStream(RCRTCRoom rcrtcRoom) {
        rcrtcRoom.getLocalUser().publishDefaultStreams(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                setText("发布资源成功");
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                setText("发布失败：" + rtcErrorCode.getReason());
            }
        });
    }

    private void subscribeStreams(RCRTCRoom rcrtcRoom) {
        RCRTCRemoteUser remoteUser = rcrtcRoom.getRemoteUser("003");
        rcrtcRoom.getLocalUser().subscribeStreams(remoteUser.getStreams(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {

            }
        });
    }

    /**
     * 房间事件监听文档：https://www.rongcloud.cn/docs/api/android/rtclib_v4/cn/rongcloud/rtc/api/callback/IRCRTCRoomEventsListener.html
     */
    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {

        /**
         * 房间内用户发布资源
         *
         * @param rcrtcRemoteUser 远端用户
         * @param list    发布的资源
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (RCRTCInputStream inputStream : list) {
                        if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                            RCRTCVideoView remoteVideoView = new RCRTCVideoView(getApplicationContext());
                            remoteUser.removeAllViews();
                            //将远端视图添加至布局
                            remoteUser.addView(remoteVideoView);
                            ((RCRTCVideoInputStream) inputStream).setVideoView(remoteVideoView);
                            //选择订阅大流或是小流。默认小流
                            ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                        }
                    }
                    //TODO 按需在此订阅远端用户发布的资源
                    rcrtcRoom.getLocalUser().subscribeStreams(list, new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            setText("订阅成功");
                        }

                        @Override
                        public void onFailed(RTCErrorCode rtcErrorCode) {
                            setText("订阅失败：" + rtcErrorCode);
                        }
                    });
                }
            });
        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {

        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
        }


        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
            remoteUser.removeAllViews();
        }

        /**
         * 用户加入房间
         *
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserJoined(final RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Meeting1V1MainActivity.this, "用户：" + rcrtcRemoteUser.getUserId() + " 加入房间", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * 用户离开房间
         *
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remoteUser.removeAllViews();
                }
            });
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {

        }

        /**
         * 自己退出房间。 例如断网退出等
         * @param i 状态码
         */
        @Override
        public void onLeaveRoom(int i) {

        }
    };

    private void subscribeAVStream(RCRTCRoom rtcRoom) {
        if (rtcRoom == null || rtcRoom.getRemoteUsers() == null) {
            return;
        }
        List<RCRTCInputStream> inputStreams = new ArrayList<>();
        for (final RCRTCRemoteUser remoteUser : rcrtcRoom.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            List<RCRTCInputStream> userStreams = remoteUser.getStreams();
            for (RCRTCInputStream inputStream : userStreams) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    //选择订阅大流或是小流。默认小流
                    ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                    //创建VideoView并设置到stream
                    RCRTCVideoView videoView = new RCRTCVideoView(getApplicationContext());
                    ((RCRTCVideoInputStream) inputStream).setVideoView(videoView);
                    //将远端视图添加至布局
                    this.remoteUser.addView(videoView);
                }
            }
            inputStreams.addAll(remoteUser.getStreams());
        }

        if (inputStreams.size() == 0) {
            return;
        }
        rcrtcRoom.getLocalUser().subscribeStreams(inputStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                setText("订阅成功");
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                setText("订阅失败：" + errorCode.getReason());
            }
        });
    }
}