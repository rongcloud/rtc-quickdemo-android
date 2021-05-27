/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.calllib.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import cn.rongcloud.demo.calllib.R;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.model.Conversation;

public class CalllibActivity extends AppCompatActivity {


    enum CallStatus {
        Idle,
        Calling,
        BeCall,
        OnCall
    }

    private static CallStatus currentStatus = CallStatus.Idle;


    private FrameLayout local;
    private FrameLayout remote;

    private Button callButton;
    private Button acceptButton;
    private Button hangUpButton;
    private EditText idInputEditText;
    private TextView statusTextView;


    private static final String TAG = "CalllibActivity";

    public static void start(Context context) {
        Intent intent = new Intent(context, CalllibActivity.class);
        context.startActivity(intent);
    }

    private void changeUi() {
        if (CallStatus.Idle == currentStatus) {
            callButton.setVisibility(View.VISIBLE);
            statusTextView.setText("");
            acceptButton.setVisibility(View.INVISIBLE);
            hangUpButton.setVisibility(View.INVISIBLE);
        } else if (CallStatus.Calling == currentStatus) {
            callButton.setVisibility(View.INVISIBLE);
            statusTextView.setText("呼叫中");
            hangUpButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.INVISIBLE);
        } else if (CallStatus.BeCall == currentStatus) {
            callButton.setVisibility(View.INVISIBLE);
            statusTextView.setText("有人找你");
            hangUpButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.VISIBLE);
        } else if (CallStatus.OnCall == currentStatus) {
            callButton.setVisibility(View.INVISIBLE);
            statusTextView.setText("通话中");
            hangUpButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initUi() {
        local = findViewById(R.id.local);
        remote = findViewById(R.id.remote);

        idInputEditText = findViewById(R.id.et_userId);
        statusTextView = findViewById(R.id.tv_status);
        callButton = findViewById(R.id.call);
        acceptButton = findViewById(R.id.accept);
        hangUpButton = findViewById(R.id.hang_up);

        callButton.setOnClickListener(onClickListener);
        acceptButton.setOnClickListener(onClickListener);
        hangUpButton.setOnClickListener(onClickListener);
    }

    private void registerCallListener() {
        RongCallClient.setReceivedCallListener(receivedCallListener);
        RongCallClient.getInstance().setVoIPCallListener(callListener);
    }

    private void unRegisterCallListener() {
        RongCallClient.setReceivedCallListener(null);
        RongCallClient.getInstance().setVoIPCallListener(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_main);

        initUi();
        changeUi();
        registerCallListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterCallListener();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.call) {
                call();
            } else if (id == R.id.accept) {
                acceptCall();
            } else if (id == R.id.hang_up) {
                hangUpCall();
            }
        }
    };

    private void call() {
        Conversation.ConversationType conversationType = Conversation.ConversationType.PRIVATE;
        String targetId = idInputEditText.getText().toString().trim();
        if (TextUtils.isEmpty(targetId)) {
            Toast.makeText(this, "请输入userid", Toast.LENGTH_LONG).show();
            return;
        }

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

    private IRongReceivedCallListener receivedCallListener = new IRongReceivedCallListener() {

        @Override
        public void onCheckPermission(RongCallSession session) {

        }

        @Override
        public void onReceivedCall(RongCallSession session) {
            Log.d(TAG, "onReceivedCall");
            currentStatus = CallStatus.BeCall;
            changeUi();
        }
    };

    private IRongCallListener callListener = new IRongCallListener() {

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

        /**
         * 电话已拨出
         *
         * @param session 通话实体
         * @param local 本地 camera 信息
         */
        @Override
        public void onCallOutgoing(RongCallSession session, SurfaceView local) {
            Log.d(TAG, "onCallOutgoing");
            currentStatus = CallStatus.Calling;
            changeUi();
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
            currentStatus = CallStatus.OnCall;
            changeUi();
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
            currentStatus = CallStatus.Idle;
            changeUi();
            clearViews();
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
            currentStatus = CallStatus.Idle;
            changeUi();
            clearViews();
        }

        /**
         * 通话过程中发生异常
         *
         * @param code 异常原因
         */
        @Override
        public void onError(RongCallCommon.CallErrorCode code) {
            Log.e(TAG, "onError code = " + code);
            currentStatus = CallStatus.Idle;
            changeUi();
            clearViews();
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
}