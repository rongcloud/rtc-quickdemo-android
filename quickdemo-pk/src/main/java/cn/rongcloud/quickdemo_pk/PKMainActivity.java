package cn.rongcloud.quickdemo_pk;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.common.tools.DialogUtils;
import cn.rongcloud.common.tools.Utils;
import cn.rongcloud.common.view.BaseActivity;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCMixConfig;
import cn.rongcloud.rtc.api.RCRTCOtherRoom;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCOtherRoomEventsListener;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PKMainActivity extends BaseActivity {

    TextView tv_uid, tv_roomId;
    private RCRTCRoom mainRoom;
    private RCRTCOtherRoom mOtherRoom;
    // 邀请View
    private View dialogView = null;
    private Dialog dialog;
    private LayoutInflater inflater;
    private EditText editText_roomid,editText_userId;
    private AppCompatCheckBox btnMuteMic,menu_close;
    //
    private FrameLayout frameyout_local,frameyout_remote;
    private Button btn_pk,btn_login1,btn_login2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_k_main);
        tv_uid=findViewById(R.id.tv_uid);
        tv_roomId =findViewById(R.id.tv_rid);
        frameyout_local=findViewById(R.id.frameyout_local);
        frameyout_remote=findViewById(R.id.frameyout_remote);
        menu_close=findViewById(R.id.menu_close);
        btnMuteMic = (AppCompatCheckBox) findViewById(R.id.menu_mute_mic);
        menu_close.setOnClickListener(clickListener);
        btnMuteMic.setOnClickListener(clickListener);
        btn_pk=findViewById(R.id.btn_pk);
        btn_login1=findViewById(R.id.btn_login1);
        btn_login2=findViewById(R.id.btn_login2);
    }

    public void click(View view){
        switch (view.getId()){
            case R.id.btn_pk:
                String text=btn_pk.getText().toString();
                if(TextUtils.equals(text,"PK")){
                    showPKDialog();
                }else if(TextUtils.equals(text,"结束PK")){
                    endPK(mOtherRoom.getRoomId());
                }
                break;
            case R.id.iv_back:
                leaveMainRoom();
                break;
            default:
                break;
        }
    }

    /**
     * 离开主房间
     */
    private void leaveMainRoom(){
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        });
    }

    /**
     * 结束连麦
     */
    private void endPK(String roomid){
        setBtnText("PK");
        if(mOtherRoom!=null){
            /**
             * 离开副房间
             *
             * @param roomId 需要离开的其他房间号码
             * @param callBack 离开房间回调
             * @param notifyFinished 离开副房间时是否结束连麦。如果为true，本端将会退出该副房间。连麦中的其他用户将收到回调通知 {@link cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener#onFinishOtherRoom(String, String)}
             * @group 房间管理
             */
            RCRTCEngine.getInstance().leaveOtherRoom(roomid, true, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (frameyout_remote!=null) {
                                frameyout_remote.removeAllViews();
                            }
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (frameyout_remote!=null) {
                                frameyout_remote.removeAllViews();
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 应答连麦邀请
     * @param inviterRoomId
     * @param inviterUserId
     * @param agree 是否同意跨房间连麦
     */
    private void answer(String inviterRoomId, String inviterUserId ,boolean agree) {
        if (checkEnv()) {
            /**
             * 响应跨房间连麦请求
             *
             * @param inviterRoomId 邀请者所在房间 id，
             * @param inviterUserId 邀请者用户 id
             * @param agree 被邀请者是否同意连麦邀请
             * @param inviteeAutoMix 被邀请者MCU服务器是否支持自动合流，默认为false。
             * <P>
             * 1：inviteeAutoMix 为true时：
             *
             * 1.1：如果邀请方在发送连麦请求之前发布了资源，当被邀请方加入邀请者房间成功后，服务器会把邀请方流资源合并到被邀请方视图（默认仅悬浮布局合流）上。
             *
             * 1.2：如果邀请方在发送连麦请求之前没有发布资源，将会在邀请方发布资源成功后，服务器才会把邀请方的资源合并到被邀请方视图（默认仅悬浮布局合流）上。
             *
             * 2: 无论为true或false，双方都可以使用{@link RCRTCLiveInfo#setMixConfig(RCRTCMixConfig, IRCRTCResultCallback)} 方法主动设置合流布局。一旦主动设置过合流布局，后续音视频直播过程中设置的自动合流参数将失效。
             *
             * <P/>
             * @param extra 扩展字段，默认为空
             * @param callback 发送回调
             * @group 房间管理
             */
            mainRoom.getLocalUser().responseJoinOtherRoom(inviterRoomId, inviterUserId, agree, true, "", new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            joinOtherRoom(inviterRoomId);
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
                    showToast("应答失败："+rtcErrorCode.getReason());
                }
            });
        }
    }

    /**
     * 加入其他房间
     * @param roomId
     */
    private void joinOtherRoom(String roomId) {
        /**
         * 加入副房间
         *
         * 前提必须已经 通过 {@link RCRTCEngine#joinRoom(String, RCRTCRoomType, IRCRTCResultDataCallback)} 或 {@link RCRTCEngine#joinRoom(String, IRCRTCResultDataCallback)} 加入了主房间
         *
         * @param roomId 房间 ID ，长度 64 个字符，可包含：`A-Z`、`a-z`、`0-9`、`+`、`=`、`-`、`_`
         * @param callBack 加入房间回调
         * @group 房间管理
         */
        RCRTCEngine.getInstance().joinOtherRoom(roomId, new IRCRTCResultDataCallback<RCRTCOtherRoom>() {
            @Override
            public void onSuccess(RCRTCOtherRoom rcrtcOtherRoom) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBtnText("结束PK");
                        mOtherRoom=rcrtcOtherRoom;
                        mOtherRoom.registerOtherRoomEventsListener(otherRoomEventsListener);
                        List<RCRTCInputStream> inputStreamList=new ArrayList<>();
                        //遍历远端用户列表
                        for (int i = 0; i < rcrtcOtherRoom.getRemoteUsers().size(); i++) {
                            //遍历远端用户发布的资源列表
                            for (RCRTCInputStream stream : rcrtcOtherRoom.getRemoteUsers().get(i).getStreams()) {
                                if(stream.getMediaType()== RCRTCMediaType.VIDEO){
                                    //如果远端用户发布的是视频流，创建显示视图RCRTCVideoView，并添加到布局中显示
                                    RCRTCVideoView remoteView = new RCRTCVideoView(PKMainActivity.this);
                                    ((RCRTCVideoInputStream)stream).setVideoView(remoteView);
                                    //todo 本demo只演示添加1个远端用户的视图
                                    frameyout_remote.removeAllViews();
                                    frameyout_remote.addView(remoteView);
                                }
                                //如果要订阅所有远端用户的流。保存所有流信息，方便后面统一订阅
                                inputStreamList.add(stream);
                            }
                        }
                        //开始订阅资源
                        mainRoom.getLocalUser().subscribeStreams(inputStreamList, new IRCRTCResultCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("订阅资源成功");
                            }

                            @Override
                            public void onFailed(RTCErrorCode rtcErrorCode) {
                                showToast("订阅资源失败： "+rtcErrorCode.getReason());
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("加入其他房间失败 ："+rtcErrorCode.getReason());
            }
        });
    }

    /**
     * 订阅远端资源并展示
     * @param streams
     */
    private void subscribeStream(List<RCRTCInputStream> streams) {
        for (RCRTCInputStream stream : streams) {
            if(stream.getMediaType()== RCRTCMediaType.VIDEO){
                RCRTCVideoView remoteView = new RCRTCVideoView(PKMainActivity.this);
                ((RCRTCVideoInputStream)stream).setVideoView(remoteView);
                frameyout_remote.removeAllViews();
                frameyout_remote.addView(remoteView);
            }
        }
        mainRoom.getLocalUser().subscribeStreams(streams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                showToast("订阅资源成功");
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("订阅资源失败： "+rtcErrorCode.getReason());
            }
        });
    }

    /**
     * 发起跨房间邀请
     */
    private void invite(String inviteeRoomId,String inviteeUserId) {
        if (checkEnv()) {
            String extra="";
            /**
             * 向指定用户发送跨房间连麦请求
             *
             * @param inviteeRoomId 被邀请者所在房间 id
             * @param inviteeUserId 被邀请用户 id
             * @param inviterAutoMix 邀请者MCU服务器是否支持自动合流，默认为false。
             * <P>
             * 1: inviterAutoMix为true时：
             *
             * 1.1：如果被邀请方在加入邀请方房间之前发布了资源，当被邀请方加入邀请者房间成功后，服务器会把被邀请方流资源合并到邀请方视图 ·（默认仅悬浮布局合流）上。
             *
             * 1.2：如果被邀请方在加入邀请方房间之前没有发布过资源，将会在被邀请方发布资源成功后，服务器会把被邀请方流资源合并到邀请方视图（默认仅悬浮布局合流）上。
             *
             * 2:无论为true或false，双方都可以使用{@link RCRTCLiveInfo#setMixConfig(RCRTCMixConfig, IRCRTCResultCallback)} 方法主动设置合流布局。一旦主动设置过合流布局，后续音视频直播过程中设置的自动合流参数将失效。
             *
             * <P/>
             * @param extra 扩展字段，默认为空
             * @param callback 发送回调
             */
            mainRoom.getLocalUser().requestJoinOtherRoom(inviteeRoomId, inviteeUserId, true, extra, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    showToast("邀请 "+inviteeUserId+" 发送成功");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    });
                    //todo 安卓端状态码说明文档：https://docs.rongcloud.cn/v4/views/rtc/call/code/android.html
                    showToast("邀请 "+inviteeUserId+" 发送失败 ："+rtcErrorCode.getReason());
                }
            });
        }
    }

    private void connectIM(String token) {
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            joinRTCRoom();
            return;
        }
        /**
         * 连接融云服务器，在整个应用程序全局，只需要调用一次，需在 {@link #init(Context, String)} 之后调用。
         * </p>
         * 如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
         * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。
         *
         * @param token    从服务端获取的用户身份令牌（Token）
         * @param callback 连接回调
         */
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {

            @Override
            public void onSuccess(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_uid.setText("UserId : "+s);
                        joinRTCRoom();
                    }
                });
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                showToast("IM 连接失败 ："+errorCode);
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus databaseOpenStatus) {

            }
        });
    }

    private void joinRTCRoom() {
        RCRTCConfig config = RCRTCConfig.Builder.create()
            //是否硬解码
            .enableHardwareDecoder(true)
            //是否硬编码
            .enableHardwareEncoder(true)
            .build();
        RCRTCEngine.getInstance().init(getApplicationContext(), config);

        RCRTCVideoStreamConfig videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create()
            //设置分辨率
            .setVideoResolution(RCRTCVideoResolution.RESOLUTION_480_640)
            //设置帧率
            .setVideoFps(RCRTCVideoFps.Fps_15)
            //设置最小码率，480P下推荐200
            .setMinRate(200)
            //设置最大码率，480P下推荐900
            .setMaxRate(900)
            .build();
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder);
        RCRTCEngine.getInstance().getDefaultVideoStream().enableTinyStream(false);

        Random random = new Random();
        String randomNum_roomid = String.valueOf(random.nextInt(9000) + 1000);
        /**
         * 加入指定类型房间
         *
         * @group 房间管理
         * @param roomId    房间 ID ，长度 64 个字符，可包含：`A-Z`、`a-z`、`0-9`、`+`、`=`、`-`、`_`
         * @param roomType  房间类型，用于区分普通房间还是直播房间
         * @param callBack  加入房间回调
         */
        RCRTCEngine.getInstance().joinRoom(randomNum_roomid, RCRTCRoomType.LIVE_AUDIO_VIDEO, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom room) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainRoom = room;
                        //注册主房间事件监听
                        mainRoom.registerRoomListener(roomEventsListener);

                        // 创建本地视频显示视图
                        RCRTCVideoView rongRTCVideoView = new RCRTCVideoView(getApplicationContext());
                        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(rongRTCVideoView);
                        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
                        frameyout_local.removeAllViews();
                        frameyout_local.addView(rongRTCVideoView);

                        tv_roomId.setText("房间号 : "+mainRoom.getRoomId());

                        //加入房间成功后可以通过 RCRTCLocalUser 对象发布本地默认音视频流，包括：麦克风采集的音频和摄像头采集的视频。
                        mainRoom.getLocalUser().publishDefaultLiveStreams(new IRCRTCResultDataCallback<RCRTCLiveInfo>() {
                            @Override
                            public void onSuccess(RCRTCLiveInfo rcrtcLiveInfo) {
                                showToast("发布资源成功");
                            }

                            @Override
                            public void onFailed(RTCErrorCode rtcErrorCode) {
                                showToast("发布资源失败");
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                //todo 安卓端状态码说明文档：https://docs.rongcloud.cn/v4/views/rtc/call/code/android.html
                showToast("加入房间失败 ： "+rtcErrorCode.getValue());
            }
        });
    }

    /**
     * 主房间事件监听
     * 详细说明请参考文档：https://www.rongcloud.cn/docs/api/android/rtclib_v4/cn/rongcloud/rtc/api/callback/IRCRTCRoomEventsListener.html
     */
    private IRCRTCRoomEventsListener roomEventsListener=new IRCRTCRoomEventsListener() {
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {

        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {

        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {

        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {

        }

        @Override
        public void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser) {

        }

        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {

        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {

        }

        @Override
        public void onLeaveRoom(int i) {

        }

        /**
         * 收到邀请者的跨房间连麦通知
         *
         * @param inviterRoomId 邀请者房间 Id
         * @param inviterUserId 邀请者用户 Id
         * @param extra 扩展字段，默认为空
         */
        @Override
        public void onRequestJoinOtherRoom(String inviterRoomId, String inviterUserId, String extra) {
            super.onRequestJoinOtherRoom(inviterRoomId, inviterUserId, extra);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(inviterUserId).append(" 邀请你进行视频PK，是否接受？");
                    DialogUtils.showDialog(PKMainActivity.this, stringBuffer.toString(), "接受", "拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            answer(inviterRoomId,inviterUserId,true);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            answer(inviterRoomId,inviterUserId,false);
                        }
                    });
                }
            });
        }

        /**
         * 收到邀请者的取消跨房间连麦通知
         *
         * @param inviterRoomId 邀请者房间 Id
         * @param inviterUserId 邀请者用户 Id
         * @param extra 扩展字段，默认为空
         */
        @Override
        public void onCancelRequestOtherRoom(String inviterRoomId, String inviterUserId, String extra) {
            super.onCancelRequestOtherRoom(inviterRoomId, inviterUserId, extra);
        }

        /**
         * 收到被邀请者的跨房间连麦响应
         * <p>
         * 1.如果被邀请者同意请求，邀请者房间和被邀请者房间中所有人会收到通知。
         *
         * 2.如果被邀请者拒绝请求，仅邀请者收到通知。
         * <p/>
         *
         * @param inviterRoomId 邀请者房间 Id
         * @param inviterUserId 邀请者用户 Id
         * @param inviteeRoomId 被邀请者房间 Id
         * @param inviteeUserId 被邀请者用户 Id
         * @param agree 被邀请者是否同意连麦请求
         * @param extra 扩展字段，默认为空
         */
        @Override
        public void onResponseJoinOtherRoom(String inviterRoomId, String inviterUserId, String inviteeRoomId, String inviteeUserId, boolean agree, String extra) {
            super.onResponseJoinOtherRoom(inviterRoomId, inviterUserId, inviteeRoomId, inviteeUserId, agree, extra);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(inviteeUserId+(agree?" 同意":" 拒绝")+"了你的邀请！");
                    if (agree){
                        joinOtherRoom(inviteeRoomId);
                    }
                }
            });
        }

        @Override
        public void onKickedByServer() {
            super.onKickedByServer();
        }

        /**
         * 收到结束跨房间连麦的通知，需要在此处调用 {@link RCRTCEngine#leaveOtherRoom(String, boolean, IRCRTCResultCallback)} 决定是否与对端结束连麦(停止合流)
         *
         * @param roomId 结束连麦的房间 Id
         * @param userId 发起结束连麦的用户 id
         */
        @Override
        public void onFinishOtherRoom(String roomId, String userId) {
            super.onFinishOtherRoom(roomId, userId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("已经结束PK");
                    setBtnText("PK");
                    if (frameyout_remote!=null) {
                        frameyout_remote.removeAllViews();
                    }
                    endPK(roomId);
                }
            });
        }

        /**
         * 加入的副房间，连接断开。
         */
        @Override
        public void onOtherRoomConnectionError(String otherRoomId) {
            super.onOtherRoomConnectionError(otherRoomId);
        }
    };

    /**
     * 副房间事件监听
     */
    private IRCRTCOtherRoomEventsListener otherRoomEventsListener=new IRCRTCOtherRoomEventsListener() {

        /**
         * 其他房间内用户发布资源
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         * @param list 发布的资源
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    subscribeStream(list);
                }
            });
        }

        /**
         * 其他房间用户发布的音频资源静音或者取消静音
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         * @param rcrtcInputStream 音频流
         * @param mute true表示静音，false表示取消静音
         */
        @Override
        public void onRemoteUserMuteAudio(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean mute) {

        }

        /**
         * 远端用户打开或关闭发布的视频流。 例如用户开启或者关闭摄像头
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         * @param rcrtcInputStream 视频流
         * @param mute true表示关闭，false表示打开
         */
        @Override
        public void onRemoteUserMuteVideo(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean mute) {

        }

        /**
         * 房间内用户取消发布资源
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         * @param list 远端用户取消发布的资源
         */
        @Override
        public void onRemoteUserUnpublishResource(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {

        }

        /**
         * 用户加入房间
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserJoined(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser) {

        }

        /**
         * 用户离开房间
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserLeft(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("用户 "+rcrtcRemoteUser.getUserId() +"退出");
                    setBtnText("PK");
                    if (frameyout_remote!=null) {
                        frameyout_remote.removeAllViews();
                    }
                }
            });
        }

        /**
         * 用户离线
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserOffline(RCRTCOtherRoom rcrtcOtherRoom, RCRTCRemoteUser rcrtcRemoteUser) {

        }

        /**
         * 自己退出其他房间。 例如断网退出等
         *
         * @param rcrtcOtherRoom 加入的其他房间对象
         * @param reasonCode 状态码
         */
        @Override
        public void onLeaveRoom(RCRTCOtherRoom rcrtcOtherRoom, int reasonCode) {

        }
    };

    private void showToast(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PKMainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 显示邀请跨房间PK弹窗
     */
    private void showPKDialog(){
        inflater = LayoutInflater.from(PKMainActivity.this);
        dialogView = inflater.inflate(R.layout.layout_pk, null);
        RelativeLayout relativeLayout=dialogView.findViewById(R.id.rel_data);
        editText_roomid=relativeLayout.findViewById(R.id.edit_roomid);
        editText_userId=relativeLayout.findViewById(R.id.edit_userId);
        Button btn_invite=relativeLayout.findViewById(R.id.btn_invite);
        Button btn_cancel=relativeLayout.findViewById(R.id.btn_cancel);
        btn_invite.setOnClickListener(clickListener);
        btn_cancel.setOnClickListener(clickListener);

        dialog = new Dialog(PKMainActivity.this, R.style.loadingdata_dialog);
        dialog.setCancelable(false);
        dialog.setContentView(relativeLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.setContentView(relativeLayout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        if (!PKMainActivity.this.isFinishing()){
            dialog.show();
        }
    }

    private OnClickListener clickListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_invite:
                    String inviteeRoomId=editText_roomid.getText().toString();
                    String inviteeUserId=editText_userId.getText().toString();
                    if(!TextUtils.isEmpty(inviteeRoomId) && !TextUtils.isEmpty(inviteeUserId)){
                        invite(inviteeRoomId,inviteeUserId);
                    }else{
                        showToast("填写数据不能为空!");
                    }
                    break;
                case R.id.btn_cancel:
                    dismiss();
                    break;
                case R.id.menu_mute_mic:
                    CheckBox checkBox = (CheckBox) v;
                    RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(checkBox.isChecked());
                    break;
                case R.id.menu_close:
                    CheckBox checkBox_camera =(CheckBox) v;;
                    if(checkBox_camera.isChecked()){
                        RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                    }else {
                        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainRoom!=null) {
            mainRoom.unregisterRoomListener();
            RCRTCEngine.getInstance().unInit();
        }
        if(mOtherRoom!=null){
            mOtherRoom.unregisterOtherRoomEventsListener();
        }
        if(frameyout_remote!=null){
            frameyout_remote.removeAllViews();
            frameyout_remote=null;
        } if (frameyout_local!=null) {
            frameyout_local.removeAllViews();
            frameyout_local=null;
        }
        if (dialog!=null && dialog.isShowing()) {
            dialog.dismiss();
            dialog=null;
        }
    }

    private boolean checkEnv(){
        return (mainRoom!=null && mainRoom.getLocalUser()!=null);
    }
    private void dismiss(){
        if (dialog!=null && !PKMainActivity.this.isFinishing() && dialog.isShowing()) {
            dialog.dismiss();
            dialog=null;
        }
    }
    private void setBtnText(String str){
        btn_pk.setText(str);
    }
    public  void loginClick(View view){
        switch (view.getId()){
            case R.id.btn_login1:
                connectIM(Utils.USER_1);
                btn_login2.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_login2:
                connectIM(Utils.USER_2);
                btn_login1.setVisibility(View.INVISIBLE);
                break;
        }
    }
}