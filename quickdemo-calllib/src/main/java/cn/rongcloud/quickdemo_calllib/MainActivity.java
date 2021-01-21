package cn.rongcloud.quickdemo_calllib;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.common.view.BaseActivity;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.model.Conversation;

/**
 * 音视频通话代码流程：
 * 1. 初始化IM，本Demo在common/src/main/java/cn/rongcloud/common/MyApplication.java类中
 * 2. 登录IM，{@link BaseActivity#connectIM(String)}
 * 3. 注册监听器 {@link MainActivity#registerListener()}
 * 4. 发起通话 {@link MainActivity#call()}
 * 5. 接电话 {@link MainActivity#acceptCall()}
 * 6. 挂电话 {@link MainActivity#hangUpCall()}
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout(R.layout.activity_main);

        local = findViewById(R.id.local);
        remote = findViewById(R.id.remote);

        loading = findViewById(R.id.loading);
        callButton = findViewById(R.id.call);
        acceptButton = findViewById(R.id.accept);
        hangUpButton = findViewById(R.id.hang_up);

        callButton.setOnClickListener(onClickListener);
        acceptButton.setOnClickListener(onClickListener);
        hangUpButton.setOnClickListener(onClickListener);
    }

    @Override
    public void IMConnectSuccess(String userId) {
        Log.d(TAG, "IMConnectSuccess user = " + userId);
        registerListener();
        changeToNormalPanel();
    }

    private void changeToNormalPanel() {
        loading.setVisibility(View.GONE);
        callButton.setVisibility(View.VISIBLE);
        acceptButton.setVisibility(View.GONE);
        hangUpButton.setVisibility(View.GONE);
    }

    private void registerListener() {
        RongCallClient.setReceivedCallListener(receivedCallListener);
        RongCallClient.getInstance().setVoIPCallListener(callListener);
    }

    @Override
    public void IMConnectError() {
        Log.d(TAG, "IMConnectError");
    }

    private View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.call:
                call();
                changeToLoadingPanel();
                break;
            case R.id.accept:
                acceptCall();
                break;
            case R.id.hang_up:
                hangUpCall();
                break;
        }
    };

    private void call() {
        Conversation.ConversationType conversationType = Conversation.ConversationType.PRIVATE;
        String targetId = getTargetId();
        List<String> userIds = new ArrayList<>();
        userIds.add(targetId);
        RongCallCommon.CallMediaType mediaType = RongCallCommon.CallMediaType.VIDEO;
        String extra = "";
        RongCallClient.getInstance().startCall(conversationType, targetId, userIds, null, mediaType, extra);
    }

    private void acceptCall() {
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().acceptCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }

    private void hangUpCall() {
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().hangUpCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }

    private void changeToLoadingPanel() {
        loading.setVisibility(View.VISIBLE);
        callButton.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        hangUpButton.setVisibility(View.GONE);
    }

    private IRongReceivedCallListener receivedCallListener = new IRongReceivedCallListener() {

        @Override
        public void onCheckPermission(RongCallSession session) {

        }

        @Override
        public void onReceivedCall(RongCallSession session) {
            Log.d(TAG, "onReceivedCall");
            changeToReceivedCallPanel();
        }
    };

    private void changeToReceivedCallPanel() {
        loading.setVisibility(View.GONE);
        callButton.setVisibility(View.GONE);
        acceptButton.setVisibility(View.VISIBLE);
        hangUpButton.setVisibility(View.VISIBLE);
    }

    private IRongCallListener callListener = new IRongCallListener() {

        /**
         * 电话已拨出
         *
         * @param session 通话实体
         * @param local 本地 camera 信息
         */
        @Override
        public void onCallOutgoing(RongCallSession session, SurfaceView local) {
            Log.d(TAG, "onCallOutgoing");
            changeToHangUpPanel();
            addLocalView(local);
        }

        /**
         * 已建立通话
         *
         * @param session 通话实体
         * @param local 本地 camera 信息
         */
        @Override
        public void onCallConnected(RongCallSession session, SurfaceView local) {
            Log.d(TAG, "onCallConnected");
            changeToHangUpPanel();
            addLocalView(local);
        }

        /**
         * 通话结束
         *
         * @param session 通话实体
         * @param reason 通话中断原因
         */
        @Override
        public void onCallDisconnected(RongCallSession session, RongCallCommon.CallDisconnectedReason reason) {
            Log.d(TAG, "onCallDisconnected reason = " + reason);
            changeToNormalPanel();
            clearViews();
            showToast("通话结束");
        }

        /**
         * 被叫端正在振铃
         *
         * @param uid 振铃端用户id
         */
        @Override
        public void onRemoteUserRinging(String uid) {
            Log.d(TAG, "onRemoteUserRinging uid = " + uid);

        }

        /**
         * 被叫端加入通话
         *
         * @param uid 加入的用户id
         * @param type 加入用户的媒体类型
         * @param ut 加入用户的类型
         * @param view 加入用户者的 camera 信息
         */
        @Override
        public void onRemoteUserJoined(String uid, RongCallCommon.CallMediaType type, int ut, SurfaceView view) {
            Log.d(TAG, "onRemoteUserRinging uid = " + uid);
            addRemoteView(view);
        }

        /**
         * 被叫端离开通话
         *
         * @param uid 离开的用户id
         * @param reason 离开原因
         */
        @Override
        public void onRemoteUserLeft(String uid, RongCallCommon.CallDisconnectedReason reason) {
            Log.d(TAG, "onRemoteUserLeft uid = " + uid);
            changeToNormalPanel();
        }

        /**
         * 通话过程中发生异常
         *
         * @param code 异常原因
         */
        @Override
        public void onError(RongCallCommon.CallErrorCode code) {
            Log.e(TAG, "onError code = " + code);
            changeToNormalPanel();
            clearViews();
            showToast("通话异常");
        }

        @Override
        public void onRemoteUserInvited(String uid, RongCallCommon.CallMediaType type) {
            Log.d(TAG, "onRemoteUserInvited uid = " + uid);
        }

        @Override
        public void onMediaTypeChanged(String uid, RongCallCommon.CallMediaType type, SurfaceView video) {
            Log.d(TAG, "onMediaTypeChanged uid = " + uid + ", type = " + type);
        }

        @Override
        public void onRemoteCameraDisabled(String uid, boolean disabled) {
            Log.d(TAG, "onRemoteCameraDisabled uid = " + uid + ", disabled = " + disabled);
        }

        @Override
        public void onRemoteMicrophoneDisabled(String uid, boolean disabled) {
            Log.d(TAG, "onRemoteMicrophoneDisabled uid = " + uid + ", disabled = " + disabled);
        }

        @Override
        public void onRemoteUserPublishVideoStream(String uid, String sid, String tag, SurfaceView surfaceView) {
            Log.d(TAG, "onRemoteUserPublishVideoStream uid = " + uid + ", sid = " + sid + ", tag = " + tag);
        }

        @Override
        public void onRemoteUserUnpublishVideoStream(String uid, String sid, String tag) {
            Log.d(TAG, "onRemoteUserUnpublishVideoStream uid = " + uid + ", sid = " + sid + ", tag = " + tag);
        }

        @Override
        public void onFirstRemoteVideoFrame(String uid, int height, int width) {
            Log.d(TAG, "onFirstRemoteVideoFrame uid = " + uid + ", height = " + height + ", width = " + width);
        }

        @Override
        public void onNetworkSendLost(int lossRate, int delay) {
            Log.d(TAG, "onNetworkSendLost lossRate = " + lossRate + ", delay = " + delay);
        }

        @Override
        public void onNetworkReceiveLost(String uid, int lossRate) {
            Log.d(TAG, "onNetworkReceiveLost uid = " + uid + ", lossRate = " + lossRate);
        }

        @Override
        public void onAudioLevelSend(String level) {
            Log.d(TAG, "onAudioLevelSend level = " + level);
        }

        @Override
        public void onAudioLevelReceive(HashMap<String, String> levels) {
            Log.d(TAG, "onAudioLevelReceive levels = " + levels);
        }
    };

    private void changeToHangUpPanel() {
        loading.setVisibility(View.GONE);
        callButton.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        hangUpButton.setVisibility(View.VISIBLE);
    }

    private void addLocalView(SurfaceView view) {
        local.removeAllViews();
        local.addView(view);
    }

    private void addRemoteView(SurfaceView view) {
        remote.removeAllViews();
        remote.addView(view);
    }

    private void clearViews() {
        local.removeAllViews();
        remote.removeAllViews();
    }

    private FrameLayout local;

    private FrameLayout remote;

    private ProgressBar loading;

    private TextView callButton;

    private TextView acceptButton;

    private TextView hangUpButton;

    private static final String TAG = "CALL_LIB";

}