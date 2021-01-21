package cn.rongcloud.quickdemo_live;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.rongcloud.common.tools.Utils;
import cn.rongcloud.common.view.Base;
import cn.rongcloud.common.tools.ChatRoomKit;
import cn.rongcloud.quickdemo_live.tools.Constants;
import cn.rongcloud.quickdemo_live.tools.RoleType;
import cn.rongcloud.quickdemo_live.tools.VideoViewManager;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCMixConfig;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.callback.RCRTCLiveCallback;
import cn.rongcloud.rtc.api.stream.RCRTCAudioInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCAVStreamType;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RCRTCSubscribeState;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.center.stream.RCVideoInputStreamImpl;
import io.rong.imlib.IRongCallback.ISendMessageCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OnReceiveMessageWrapperListener;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Base {

    private static final String TAG = "LiveMainActivity";
    private RCRTCRoom mRTCRoom;
    private Button btn_a,btn_b,btn_c,btn_d,btn_joinRoom,btn_subscribeLiveStream, btn_viewerJoin,btn_camera,btn_mic,btn_layout,btn_sendMessage;
    private TextView tv_title,tv_liveUrl;
    private LinearLayout linear_host;
    private VideoViewManager mVideoViewManager;
    private RoleType mRoleType = RoleType.UNKNOWN;
    private RCRTCLiveInfo mLiveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //TODO RCRTCConfig.Builder 请参考API文档：https://www.rongcloud.cn/docs/api/android/rtclib_v4/cn/rongcloud/rtc/api/RCRTCConfig.Builder.html
        RCRTCConfig config = cn.rongcloud.rtc.api.RCRTCConfig.Builder.create()
            .enableHardwareDecoder(true)    //是否硬解码
            .enableHardwareEncoder(true)    //是否硬编码
            .build();
        RCRTCEngine.getInstance().init(getApplicationContext(), config);
    }

    private void initView() {
        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("直播 Demo");
        btn_a = findViewById(R.id.btn_login_a);
        btn_b = findViewById(R.id.btn_login_b);
        btn_c = findViewById(R.id.btn_login_c);
        btn_d = findViewById(R.id.btn_login_d);
        btn_joinRoom = findViewById(R.id.btn_joinRoom);
        linear_host = findViewById(R.id.linear_videoView_manager);
        mVideoViewManager = new VideoViewManager(MainActivity.this.getApplicationContext());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linear_host.addView(mVideoViewManager,layoutParams2);
        tv_liveUrl = findViewById(R.id.tv_liveUrl);
        btn_subscribeLiveStream = findViewById(R.id.btn_subscribeLiveStream);
        btn_viewerJoin = findViewById(R.id.btn_viewerJoin);
        btn_camera = findViewById(R.id.btn_camera);
        btn_mic = findViewById(R.id.btn_mic);
        btn_layout = findViewById(R.id.btn_layout);
        btn_sendMessage = findViewById(R.id.btn_sendMessage);
        setButtonClickable(null,new Button[]{btn_joinRoom,btn_subscribeLiveStream, btn_viewerJoin,btn_camera,btn_mic,btn_layout,btn_sendMessage});
    }

    public void onClick(View view){
        String str = ((Button)view).getText().toString();;
        switch (view.getId()){
            case R.id.btn_login_a:
                if(TextUtils.equals(str,Constants.STR_USER_A)){
                    connectIM(Utils.USER_TOKEN_1,btn_a,new Button[]{btn_b,btn_c,btn_d});
                }else if(TextUtils.equals(str, Constants.LOGOUT)){
                    logout();
                    btn_a.setText(Constants.STR_USER_A);
                }
                break;
            case R.id.btn_login_b:
                if(TextUtils.equals(str,Constants.STR_USER_B)){
                    connectIM(Utils.USER_TOKEN_2,btn_b,new Button[]{btn_a,btn_c,btn_d});
                }else if(TextUtils.equals(str, Constants.LOGOUT)){
                    logout();
                    btn_b.setText(Constants.STR_USER_B);
                }
                break;
            case R.id.btn_login_c:
                if(TextUtils.equals(str,Constants.STR_USER_C)){
                    connectIM(Utils.USER_TOKEN_3,btn_c,new Button[]{btn_a,btn_b,btn_d});
                }else if(TextUtils.equals(str, Constants.LOGOUT)){
                    logout();
                    btn_c.setText(Constants.STR_USER_C);
                }
                break;
            case R.id.btn_login_d:
                if(TextUtils.equals(str,Constants.STR_USER_D)){
                    connectIM(Utils.USER_TOKEN_4,btn_d,new Button[]{btn_a,btn_b,btn_c});
                }else if(TextUtils.equals(str, Constants.LOGOUT)){
                    logout();
                    btn_d.setText(Constants.STR_USER_D);
                }
                break;
            case R.id.btn_joinRoom://开始直播
                String text = btn_joinRoom.getText().toString();
                tv_title.setText(Constants.HOST_PAGE_TITLE);
                if(TextUtils.equals(text,Constants.JOIN)){
                    joinRoom();
                }else{
                    leaveRoom();
                }
                break;
            case R.id.btn_layout://合流布局设置
                setMixLayout(str);
                break;
            case R.id.btn_subscribeLiveStream://观看直播
                tv_title.setText(Constants.VIEWER_PAGE_TITLE);
                String liveUrl = tv_liveUrl.getText().toString();
                if(TextUtils.isEmpty(liveUrl)){
                    showToast("观看的直播地址为空，请先通过主播获取直播地址！");
                    return;
                }
                if(TextUtils.equals(str,Constants.SUB)){
                    subscribeLiveStream(liveUrl);
                }else if(TextUtils.equals(str,Constants.UN_SUB)){
                    unsubscribeLiveStream(liveUrl);
                }
                break;
            case R.id.btn_sendMessage:
                sendLiveUrlMessage();
                break;
            case R.id.btn_camera:
                if(TextUtils.equals(str,Constants.CAMERA_STATUS_CLOSE)){
                    RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                    ((Button)view).setText(Constants.CAMERA_STATUS_OPEN);
                }else{
                    RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
                    ((Button)view).setText(Constants.CAMERA_STATUS_CLOSE);
                }
                break;
            case R.id.btn_mic:
                if(TextUtils.equals(str,Constants.MIC_STATUS_CLOSE)){
                    RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(true);
                    ((Button)view).setText(Constants.MIC_STATUS_OPEN);
                }else{
                    RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(false);
                    ((Button)view).setText(Constants.MIC_STATUS_CLOSE);
                }
                break;
            case R.id.btn_viewerJoin:
                if(TextUtils.equals(str,Constants.VIEWER_JOIN)){
                    viewerJoin();//观众上麦
                }else{
                    ((Button)view).setText(Constants.VIEWER_JOIN);
                    leaveRoom();//观众下麦
                }
                break;
            default:
                break;
        }
    }

    /**
     * TODO 主播合流布局设置，请参考开发者文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/anchorManage/layout/android.html#layout-1
     */
    private void setMixLayout(String str){
        RCRTCMixConfig config = null;
        String text=btn_layout.getText().toString();
        switch (str){
            case Constants.ADAPTIVE://自适应布局
                List<RCRTCStream> streams = new ArrayList<>();
                streams.add(RCRTCEngine.getInstance().getDefaultVideoStream());
                text = Constants.CUSTOM;
                if(mRTCRoom != null){
                    for (RCRTCRemoteUser remoteUser : mRTCRoom.getRemoteUsers()) {
                        for (RCRTCInputStream stream : remoteUser.getStreams()) {
                            if(stream.getMediaType() ==RCRTCMediaType.VIDEO && ((RCVideoInputStreamImpl)stream).getSubscribeState()== RCRTCSubscribeState.SUBSCRIBED){
                                streams.add(stream);
                            }
                        }
                    }
                }
                Log.d("RTCMixLayout","create_Custom_MixConfig");
                showToast("正在切换为自定义布局!");
                config = RTCMixLayout.getInstance().create_Custom_MixConfig(streams);
                break;
            case Constants.CUSTOM://自定义布局
                text= Constants.SUSPENSION;
                showToast("正在切换为悬浮布局！");
                config = RTCMixLayout.getInstance().create_Suspension_MixConfig(RCRTCEngine.getInstance().getDefaultVideoStream()); //切换为悬浮布局
                Log.d("RTCMixLayout","create_Suspension_MixConfig");
                break;
            case Constants.SUSPENSION://悬浮布局
                config = RTCMixLayout.getInstance().create_Adaptive_MixConfig();  //切换为自适应布局
                text=Constants.ADAPTIVE;
                showToast("正在切换为自适应布局!");
                Log.d("RTCMixLayout","create_Adaptive_MixConfig");
                break;
        }
        if(mLiveInfo == null){
            showToast("未开始直播，布局设置失败！");
            return;
        }
        String finalText = text;
        mLiveInfo.setMixConfig(config, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                btn_layout.setText(finalText);
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("合流布局设置失败 ："+ rtcErrorCode.getValue());
            }
        });
    }

    /**
     * todo 主播加入房间 或 观众上麦
     * 请参考开发者文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/joinManage/join/android.html#audienceJoin
     */
    private void joinRoom(){
        showLoading();
        RCRTCVideoStreamConfig videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create()
            .setVideoResolution(Constants.resolution)    //设置分辨率
            .setVideoFps(Constants.fps)  //设置帧率
            .setMinRate(Constants.mixRate)    //设置最小码率，480P下推荐200
            .setMaxRate(Constants.maxRate)     //设置最大码率，480P下推荐900
            .build();
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder);
        // 创建本地视频显示视图
        RCRTCVideoView rongRTCVideoView = new RCRTCVideoView(getApplicationContext());
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(rongRTCVideoView);
        mVideoViewManager.addRTCVideoView(RongIMClient.getInstance().getCurrentUserId(), rongRTCVideoView);
        //开启摄像头采集视频数据
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        //根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
        RCRTCRoomType rtcRoomType = RCRTCRoomType.LIVE_AUDIO_VIDEO;
        //mRoomId：最大长度 64 个字符，可包含：`A-Z`、`a-z`、`0-9`、`+`、`=`、`-`、`_`
        RCRTCEngine.getInstance().joinRoom(Constants.ROOM_ID, rtcRoomType, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom rcrtcRoom) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                        showToast("加入RTC房间成功");
                        setButtonClickable(new Button[]{btn_camera,btn_mic,btn_layout},new Button[]{btn_subscribeLiveStream});
                        if(mRoleType == RoleType.UNKNOWN){//第一次加入房间，身份更新至ANCHOR
                            btn_joinRoom.setText(Constants.LEAVE);
                            mRoleType = RoleType.ANCHOR;
                        }else if(mRoleType == RoleType.VIEWER){//观众上麦逻辑
                            btn_viewerJoin.setText(Constants.VIEWER_LEAVE);
                        }
                        mRTCRoom = rcrtcRoom;
                        mRTCRoom.registerRoomListener(roomEventsListener);//注册房间事件回调
                        //加入房间成功后，发布默认音视频流。该方法实现请查看开发者文档 ： https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/quick/anchor/android.html#publish
                        publishDefaultLiveStreams();
                        //加入房间成功后，如果房间中已存在用户且发布了音、视频流，就订阅远端用户发布的音视频流。该方法实现请查看开发者文档 ： https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/quick/anchor/android.html#Subscribe
                        for (RCRTCRemoteUser remoteUser : rcrtcRoom.getRemoteUsers()) {
                            subscribeAVStream(remoteUser.getUserId(),remoteUser.getStreams());
                        }
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                showToast("加入房间失败 ： " + errorCode.getReason());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVideoViewManager.release();
                        RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                        closeLoading();
                    }
                });
            }
        });
    }

    /**
     * 离开音视频房间
     * SDK 内部会自动取消发布本端资源和取消订阅远端用户资源，必须在成功或失败回调完成之后再开始新的音视频通话逻辑。
     */
    private void leaveRoom(){
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("结束直播失败 : "+rtcErrorCode.getReason());
            }
        });
        postUIThread(new Runnable() {
            @Override
            public void run() {
                if(mRoleType == RoleType.VIEWER){//观众下麦逻辑
                    mRoleType = RoleType.VIEWER;
                    setButtonClickable(new Button[]{btn_subscribeLiveStream},new Button[]{btn_camera,btn_mic,btn_layout,btn_sendMessage});
                    btn_camera.setText(Constants.CAMERA_STATUS_CLOSE);
                    btn_mic.setText(Constants.MIC_STATUS_CLOSE);
                    btn_layout.setText(Constants.ADAPTIVE);
                }else{
                    mRoleType = RoleType.UNKNOWN;
                }
                btn_joinRoom.setText(Constants.JOIN);
                if (mVideoViewManager != null) {
                    mVideoViewManager.release();
                }
            }
        });
    }

    /**
     * 主播订阅资源
     */
    private void subscribeAVStream(String userId , List<RCRTCInputStream> inputStreamList) {
        List<String> list=new ArrayList<>();
        for (RCRTCRemoteUser user : mRTCRoom.getRemoteUsers()) {
            for (RCRTCInputStream stream : user.getStreams()) {
                if(stream.getMediaType() ==RCRTCMediaType.VIDEO && ((RCVideoInputStreamImpl)stream).getSubscribeState()== RCRTCSubscribeState.SUBSCRIBED){
                    list.add(user.getUserId());
                }
            }
        }
        if(list.size() >= 3){
            showToast("本Demo仅展示包括本主播在内的其他3名主播！");
            return;
        }
        RCRTCVideoView remoteView = null;
        for (RCRTCInputStream inputStream : inputStreamList) {
            if(inputStream.getMediaType() == RCRTCMediaType.VIDEO){
                //如果远端用户发布的是视频流，创建显示视图RCRTCVideoView，并添加到布局中显示
                remoteView = new RCRTCVideoView(MainActivity.this.getApplicationContext());
                ((RCRTCVideoInputStream)inputStream).setVideoView(remoteView);
            }
        }
        RCRTCVideoView finalRemoteView = remoteView;
        mRTCRoom.getLocalUser().subscribeStreams(inputStreamList, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("订阅成功");
                        if(mVideoViewManager!=null){
                            mVideoViewManager.addRTCVideoView(userId, finalRemoteView);
                        }
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("订阅失败 ："+rtcErrorCode.getValue());
            }
        });
    }

    private void publishDefaultLiveStreams() {
        if(mRTCRoom == null && mRTCRoom.getLocalUser()!=null){
            showToast("请先加入房间！");
            return;
        }
        // localUser 对象由加入房间成功回调中RCRTCRoom对象获取，获取方式：RCRTCRoom.getLocalUser();
        mRTCRoom.getLocalUser().publishDefaultLiveStreams(new IRCRTCResultDataCallback<RCRTCLiveInfo>() {
            @Override
            public void onSuccess(RCRTCLiveInfo liveInfo) {
                showToast("发布资源成功");
                mLiveInfo = liveInfo;
                //TODO 上传liveUrl到客户自己的APP server，提供给观众端用来订阅
                String liveUrl = mLiveInfo.getLiveUrl();
                setLiveUrl(liveUrl);
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        setButtonClickable(new Button[]{btn_sendMessage},null);
                    }
                });
            }

            @Override
            public void onFailed(final RTCErrorCode errorCode) {
                showToast("发布资源失败 ：" + errorCode.getReason());
                setLiveUrl("");
            }
        });
    }

    //todo 观众观看直播，请参考开发者文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/audienceManage/android.html
    private void subscribeLiveStream(String liveUrl){
        showLoading();
        /**
         * 仅直播模式下观众可用。 作为观众，直接观看主播的直播，无需加入房间，通过传入主播的 url 进行订阅。
         *
         * @param liveUrl   直播URL
         * @param avStreamType  直播类型
         * @param callBack  加入直播房间结果回调
         */
        RCRTCEngine.getInstance().subscribeLiveStream(liveUrl, RCRTCAVStreamType.AUDIO_VIDEO, new RCRTCLiveCallback() {
            @Override
            public void onSuccess() {
                //订阅成功，后续会根据订阅的类型，触发 onAudioStreamReceived 和 onVideoStreamReceived方法
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                        mRoleType = RoleType.VIEWER;
                        setButtonClickable(new Button[]{btn_viewerJoin},new Button[]{btn_joinRoom,btn_camera,btn_mic,btn_layout, btn_sendMessage});
                    }
                });
            }

            @Override
            public void onVideoStreamReceived(final RCRTCVideoInputStream stream) {
                //收到视频流。操作UI需要转到 UI 线程
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_subscribeLiveStream.setText(Constants.UN_SUB);
                        //创建 RCRTCVideoView
                        RCRTCVideoView videoView = new RCRTCVideoView(getApplicationContext());
                        //给input stream设置用于显示视频的视图
                        stream.setVideoView(videoView);
                        if (mVideoViewManager != null) {
                            mVideoViewManager.addRTCVideoView(((RCVideoInputStreamImpl)stream).getUserId(),videoView);
                        }
                    }
                });
            }

            @Override
            public void onAudioStreamReceived(RCRTCAudioInputStream stream) {
                //收到音频流
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                showToast("观看直播失败："+errorCode.getReason());
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                    }
                });
            }
        });
    }

    //todo 取消观看直播：请参考开发者文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/audienceManage/android.html#unsubscribe
    private void unsubscribeLiveStream(String liveUrl){
        showLoading();
        RCRTCEngine.getInstance().unsubscribeLiveStream(liveUrl, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                showToast("取消观看直播成功");
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                showToast("取消观看直播失败 ： "+errorCode);
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                    }
                });
            }
        });
        btn_subscribeLiveStream.setText(Constants.SUB);
        mVideoViewManager.release();
    }

    private void connectIM(String token,Button btn,Button buttonArray[]) {
        showLoading();
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
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置IM消息监听，具体说明请参考开发者文档：https://docs.rongcloud.cn/v4/views/im/noui/guide/chatroom/setting/listener/android.html#messagelistener
                        ChatRoomKit.setReceiveMessageListener(new OnReceiveMessageWrapperListener() {
                            @Override
                            public boolean onReceived(Message message, int i, boolean b, boolean b1) {
                                MessageContent msgContent = message.getContent();
                                if(msgContent instanceof TextMessage)
                                    setLiveUrl(((TextMessage) msgContent).getContent());
                                return false;
                            }
                        });
                        ChatRoomKit.joinChatRoom(Constants.ROOM_ID, -1, new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("加入聊天室成功");
                            }

                            @Override
                            public void onError(ErrorCode errorCode) {
                                showToast("加入聊天室失败 ："+errorCode.getValue());
                            }
                        });
                        closeLoading();
                        btn.setText(Constants.LOGOUT);
                        showToast("用户 "+s +"登录成功!");
                        setButtonClickable(new Button[]{btn_joinRoom,btn_subscribeLiveStream},buttonArray);
                    }
                });
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                showToast("IM 连接失败 ："+errorCode);
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoading();
                    }
                });
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus databaseOpenStatus) {
            }
        });
    }

    //RTC房间事件监听，详细请查看开发者文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/quick/anchor/android.html#EventsListener
    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    subscribeAVStream(rcrtcRemoteUser.getUserId(),list);
                }
            });
        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean mute) {
            showToast("远端用户 "+rcrtcRemoteUser.getUserId() + (mute ?"关闭麦克风":"打开麦克风"));
        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean mute ) {
            showToast("远端用户 "+rcrtcRemoteUser.getUserId() + (mute ?"打开摄像头":"关闭摄像头"));
        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {

        }

        @Override
        public void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser) {
            showToast("用户 "+rcrtcRemoteUser.getUserId()+" 加入房间！");
        }

        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            showToast("用户 "+rcrtcRemoteUser.getUserId() +" 退出房间！");
            removeRTCVideoView(rcrtcRemoteUser.getUserId());
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {
            showToast("用户 "+rcrtcRemoteUser.getUserId() +" 离线！");
            removeRTCVideoView(rcrtcRemoteUser.getUserId());
        }

        @Override
        public void onLeaveRoom(int i) {

        }
    };

    private void setLiveUrl(String liveUrl){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                tv_liveUrl.setText(liveUrl);
            }
        });
    }

    /**
     * im 登出
     */
    private void logout(){
        showLoading();
        ChatRoomKit.quitChatRoom(Constants.ROOM_ID,null);
        setButtonClickable(new Button[]{btn_a,btn_b,btn_c,btn_d},new Button[]{btn_joinRoom,btn_subscribeLiveStream, btn_viewerJoin,btn_camera,btn_mic,btn_layout,btn_sendMessage});
        ChatRoomKit.setReceiveMessageListener(null);
        //
        if(mRoleType == RoleType.ANCHOR){
            leaveRoom();
        }else if(mRoleType == RoleType.VIEWER){
            unsubscribeLiveStream(tv_liveUrl.getText().toString());
        }
        //断开与融云服务器的连接，但仍然接收远程推送。<strong>注意：</strong>因为 SDK 在前后台切换或者网络出现异常都会自动重连，保证连接可靠性。所以除非您的 App 逻辑需要登出，否则一般不需要调用此方法进行手动断开。<br>
        RongIMClient.getInstance().disconnect();
        //断开与融云服务器的连接，并且不再接收远程推送消息。若想断开连接后仍然接受远程推送消息，可以调用 {@link #disconnect()}
        RongIMClient.getInstance().logout();
        resetView();
        setLiveUrl("");
        mLiveInfo = null;
        mRoleType = RoleType.UNKNOWN;
        closeLoading();
    }

    //观众上麦方法
    private void viewerJoin() {
        String liveUrl = tv_liveUrl.getText().toString();
        if(mVideoViewManager!=null){
            mVideoViewManager.clear();
        }
        if(!TextUtils.isEmpty(liveUrl)){
            btn_subscribeLiveStream.setText(Constants.SUB);
            RCRTCEngine.getInstance().unsubscribeLiveStream(liveUrl, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            joinRoom();
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            joinRoom();
                        }
                    });
                }
            });
        }else{
            joinRoom();
        }
    }

    private void sendLiveUrlMessage() {
        String liveUrl;
        liveUrl = tv_liveUrl.getText().toString();
        if(TextUtils.isEmpty(liveUrl)){
            showToast("观看的直播地址为空，请先通过主播获取直播地址！");
            return;
        }
        showLoading();
        TextMessage messageContent = TextMessage.obtain(liveUrl);
        ChatRoomKit.sendMessage(Constants.ROOM_ID, messageContent, new ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {
            }

            @Override
            public void onSuccess(Message message) {
                showToast("消息发送成功");
                closeLoading();
            }

            @Override
            public void onError(Message message, ErrorCode errorCode) {
                showToast("消息发送失败 ： "+errorCode.getValue());
                closeLoading();
            }
        });
    }

    private void removeRTCVideoView(String userId){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                if (mVideoViewManager!=null) {
                    mVideoViewManager.removeRTCVideoView(userId);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRTCRoom != null){
            mRTCRoom.unregisterRoomListener();
        }
        //TODO 取消初始化，释放资源。如果需要释放RTC lib SDK,主播请在leaveRoom成功/失败之后调用，观众在取消观看(unsubscribeLiveStream)成功/失败之后调用;
        RCRTCEngine.getInstance().unInit();
        if(mVideoViewManager!=null){
            mVideoViewManager.release();
        }
    }

    private void resetView(){
        btn_subscribeLiveStream.setText(Constants.SUB);
        btn_viewerJoin.setText(Constants.VIEWER_JOIN);
        btn_camera.setText(Constants.CAMERA_STATUS_CLOSE);
        btn_mic.setText(Constants.MIC_STATUS_CLOSE);
        btn_layout.setText(Constants.ADAPTIVE);
        tv_title.setText("直播 Demo");
    }
}