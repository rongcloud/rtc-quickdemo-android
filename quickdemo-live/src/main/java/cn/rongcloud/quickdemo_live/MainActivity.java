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
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.RCRTCRoomConfig.Builder;
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
import cn.rongcloud.rtc.base.RCRTCLiveRole;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RCRTCSubscribeState;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.center.stream.RCVideoInputStreamImpl;
import cn.rongcloud.rtc.utils.RCConsts;
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
    private Button btn_a,btn_b,btn_c,btn_d,btn_joinRoom,btn_subscribeLiveStream, btn_viewerJoin,btn_camera,btn_mic,btn_layout;
    private TextView tv_title;
    private LinearLayout linear_host;
    private VideoViewManager mVideoViewManager;
    private RoleType mRoleType = RoleType.UNKNOWN;
    private RCRTCLiveInfo mLiveInfo;
    private boolean executing = false;  //是否执行任务中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initRTCSDK();
    }

    private void initRTCSDK() {
        //TODO RCRTCConfig.Builder 请参考API文档：https://www.rongcloud.cn/docs/api/android/rtclib_v4/cn/rongcloud/rtc/api/RCRTCConfig.Builder.html
        RCRTCConfig config = RCRTCConfig.Builder.create()
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
        btn_subscribeLiveStream = findViewById(R.id.btn_subscribeLiveStream);
        btn_viewerJoin = findViewById(R.id.btn_viewerJoin);
        btn_camera = findViewById(R.id.btn_camera);
        btn_mic = findViewById(R.id.btn_mic);
        btn_layout = findViewById(R.id.btn_layout);
        setButtonClickable(new Button[]{btn_joinRoom,btn_subscribeLiveStream, btn_viewerJoin,btn_camera,btn_mic,btn_layout},false);
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
                if (executing) {
                    showToast("您点击的太快了，请稍后再试");
                    return;
                }
                executing = true;
                mRoleType = RoleType.ANCHOR;
                String text = btn_joinRoom.getText().toString();
                tv_title.setText(Constants.HOST_PAGE_TITLE);
                if(TextUtils.equals(text,Constants.JOIN)){
                    joinRoom();
                }else{
                    leaveRoom(false);
                    setButtonClickable(new Button[]{btn_subscribeLiveStream,btn_joinRoom},true);
                }
                break;
            case R.id.btn_subscribeLiveStream://观众观看直播
                if (executing) {
                    showToast("您点击的太快了，请稍后再试");
                    return;
                }
                executing = true;
                mRoleType = RoleType.VIEWER;
                tv_title.setText(Constants.VIEWER_PAGE_TITLE);
                if(TextUtils.equals(str,Constants.SUB)){
                    joinRoom();
                }else if(TextUtils.equals(str,Constants.UN_SUB)){
                    leaveRoom(false);
                    setButtonClickable(new Button[]{btn_viewerJoin},false);
                }
                break;
            case R.id.btn_viewerJoin://观众上麦
                if (executing) {
                    showToast("您点击的太快了，请稍后再试");
                    return;
                }
                executing = true;
                if(TextUtils.equals(str,Constants.VIEWER_JOIN)){
                    mRoleType = RoleType.ANCHOR;
                    if (mRTCRoom != null)
                        leaveRoom(true);
                    else
                        joinRoom(true);
                }else{
                    ((Button)view).setText(Constants.VIEWER_JOIN);
                    mRoleType = RoleType.UNKNOWN;
                    leaveRoom(false);
                    setButtonClickable(new Button[]{btn_subscribeLiveStream,btn_joinRoom},true);
                    setButtonClickable(new Button[]{btn_viewerJoin},false);
                }
                break;
            case R.id.btn_layout://合流布局设置
                setMixLayout(str);
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
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (btn_layout != null)
                            btn_layout.setText(finalText);
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                showToast("合流布局设置失败 ："+ rtcErrorCode.getValue());
            }
        });
    }

    /**
     * 加入房间
     * 默认不是切换角色
     */
    private void joinRoom(){
        joinRoom(false);
    }

    /**
     * todo 主播加入房间 或 观众上麦
     * 请参考开发者文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/joinManage/join/android.html#audienceJoin
     * @param switchRole 是否切换角色操作
     */
    private void joinRoom(boolean switchRole){
        showLoading();
        //由于观众是不能发布资源的，所以不需要设置音视频配置的，只有主播才需要配置
        if (mRoleType == RoleType.ANCHOR) {
            RCRTCVideoStreamConfig videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create().setVideoResolution(Constants.resolution)    //设置分辨率
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
        }

        //设置房间配置：包括房间类型以及角色信息
        RCRTCRoomConfig roomConfig = Builder.create()
            //RTCLib 房间角色分为主播(RCRTCRole.ANCHOR) 和 观众两种角色(RCRTCRole.AUDIENCE)
            .setLiveRole(mRoleType == RoleType.VIEWER ? RCRTCLiveRole.AUDIENCE : RCRTCLiveRole.BROADCASTER)
            //根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
            .setRoomType(RCRTCRoomType.LIVE_AUDIO_VIDEO)
            .build();

        //mRoomId：最大长度 64 个字符，可包含：`A-Z`、`a-z`、`0-9`、`+`、`=`、`-`、`_`
        RCRTCEngine.getInstance().joinRoom(Constants.ROOM_ID, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom rcrtcRoom) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        executing = false;
                        closeLoading();
                        showToast("加入房间成功");
                        mRTCRoom = rcrtcRoom;
                        mRTCRoom.registerRoomListener(roomEventsListener);//注册房间事件回调
                        if (mRoleType == RoleType.ANCHOR)
                            onAnchorJoinSuccess(switchRole);
                        else
                            onAudienceJoinSuccess();


                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                showToast("加入房间失败 ： " + errorCode.getReason());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        executing = false;
                        mVideoViewManager.release();
                        RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                        closeLoading();
                    }
                });
            }
        });
    }

    /**
     * 观众加房间成功
     */
    private void onAudienceJoinSuccess() {
        //1. 订阅直播合流
        List<RCRTCInputStream> liveStreams = mRTCRoom.getLiveStreams();
        //这个需注意: 如果主播还未进RTC房间或进房间后未发布任何资源，则 getLiveStreams() 会返回null，
        // 如果房间内主播再观众加入之后发布了资源，可以通过注册房间回调事件通过 IRCRTCRoomEventsListener#onLiveStreamsPublish 回调方法来监听
        if (liveStreams != null && !liveStreams.isEmpty()){
            //由于直播流是所有主播合并后的流所有不能归属任何一个主播下，所以UserI可以根据实际情况自己定义
            //暂用 roomId 来表示
            subscribeAVStream(RCConsts.ROOMID,liveStreams);
        }

        //2. 除了可以订阅直播合流，还可以单独订阅某个主播的音视频流
        // 这里为了便于演示，默认订阅了所有主播的音视频流
        for (RCRTCRemoteUser remoteUser : mRTCRoom.getRemoteUsers()) {
            subscribeAVStream(remoteUser.getUserId(),remoteUser.getStreams());
        }
        //3. update ui
        setButtonClickable(new Button[]{btn_joinRoom,btn_camera,btn_mic,btn_layout},false);
        setButtonClickable(new Button[]{btn_viewerJoin},true);
        btn_subscribeLiveStream.setText(Constants.UN_SUB);
    }

    /**
     * 主播加入房间成功
     * @param switchRole 是否切换角色操作
     */
    private void onAnchorJoinSuccess(boolean switchRole) {
        setButtonClickable(new Button[]{btn_subscribeLiveStream},false);
        setButtonClickable(new Button[]{btn_camera,btn_mic,btn_layout},true);

        if(switchRole){//观众上麦逻辑
            btn_viewerJoin.setText(Constants.VIEWER_LEAVE);
        }else {
            btn_joinRoom.setText(Constants.LEAVE);
        }
        //加入房间成功后，发布默认音视频流。该方法实现请查看开发者文档 ： https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/quick/anchor/android.html#publish
        publishDefaultLiveStreams();
        //加入房间成功后，如果房间中已存在用户且发布了音、视频流，就订阅远端用户发布的音视频流。该方法实现请查看开发者文档 ： https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/quick/anchor/android.html#Subscribe
        for (RCRTCRemoteUser remoteUser : mRTCRoom.getRemoteUsers()) {
            subscribeAVStream(remoteUser.getUserId(),remoteUser.getStreams());
        }
    }

    /**
     * 离开音视频房间
     * SDK 内部会自动取消发布本端资源和取消订阅远端用户资源，必须在成功或失败回调完成之后再开始新的音视频通话逻辑。
     * @param switchRole 是否切换角色操作，true: 切换角色重新调用jonroom方法
     */
    private void leaveRoom(boolean switchRole){
        resetView();
        setButtonClickable(new Button[]{btn_camera,btn_mic,btn_layout},false);
        mVideoViewManager.release();
        mLiveInfo = null;
        mRTCRoom = null;
        if (!switchRole){
            btn_joinRoom.setText(Constants.JOIN);
        }
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果是切换角色，则需要再次重新加房间
                        if (switchRole){
                            joinRoom(true);
                        }else {
                            executing = false;
                        }
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                if (switchRole){
                    showToast("上麦失败: "+rtcErrorCode.getReason());
                }else {
                    showToast("结束直播失败 : " + rtcErrorCode.getReason());
                }
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        executing = false;
                    }
                });
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
            }

            @Override
            public void onFailed(final RTCErrorCode errorCode) {
                showToast("发布资源失败 ：" + errorCode.getReason());
            }
        });
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
                        initRTCSDK();
                        closeLoading();
                        btn.setText(Constants.LOGOUT);
                        showToast("用户 "+s +"登录成功!");
                        setButtonClickable(buttonArray,false);
                        setButtonClickable(new Button[]{btn_joinRoom,btn_subscribeLiveStream},true);
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

        @Override
        public void onPublishLiveStreams(final List<RCRTCInputStream> streams) {
            Log.e(TAG,"onPublishLiveStreams");
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    //由于直播流是所有主播合并后的流所有不能归属任何一个主播下，所以UserI可以根据实际情况自己定义
                    //暂用 roomId 来表示
                    subscribeAVStream(RCConsts.ROOMID,streams);
                }
            });
        }

        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> list) {

        }
    };

    /**
     * im 登出
     */
    private void logout(){
        showLoading();
        ChatRoomKit.quitChatRoom(Constants.ROOM_ID,null);
        setButtonClickable(new Button[]{btn_a,btn_b,btn_c,btn_d},true);
        setButtonClickable(new Button[]{btn_joinRoom,btn_subscribeLiveStream, btn_viewerJoin,btn_camera,btn_mic,btn_layout},false);
        ChatRoomKit.setReceiveMessageListener(null);
        leaveRoom(false);
        //断开与融云服务器的连接，但仍然接收远程推送。<strong>注意：</strong>因为 SDK 在前后台切换或者网络出现异常都会自动重连，保证连接可靠性。所以除非您的 App 逻辑需要登出，否则一般不需要调用此方法进行手动断开。<br>
        RongIMClient.getInstance().disconnect();
        //断开与融云服务器的连接，并且不再接收远程推送消息。若想断开连接后仍然接受远程推送消息，可以调用 {@link #disconnect()}
        RongIMClient.getInstance().logout();
        resetView();
        mLiveInfo = null;
        mRoleType = RoleType.UNKNOWN;
        closeLoading();
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