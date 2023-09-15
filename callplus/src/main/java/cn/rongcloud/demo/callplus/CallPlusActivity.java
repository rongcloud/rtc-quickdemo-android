/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.callplus;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.rongcloud.callplus.api.RCCallPlusClient;
import cn.rongcloud.callplus.api.RCCallPlusCode;
import cn.rongcloud.callplus.api.RCCallPlusLocalVideoView;
import cn.rongcloud.callplus.api.RCCallPlusReason;
import cn.rongcloud.callplus.api.RCCallPlusRemoteVideoView;
import cn.rongcloud.callplus.api.RCCallPlusSession;
import cn.rongcloud.callplus.api.RCCallPlusUser;
import cn.rongcloud.callplus.api.callback.IRCCallPlusEventListener;
import cn.rongcloud.callplus.api.callback.IRCCallPlusResultListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class CallPlusActivity extends AppCompatActivity implements View.OnClickListener {

    public static void start(Context context) {
        Intent starter = new Intent(context, CallPlusActivity.class);
        context.startActivity(starter);
    }


    enum CallStatus {
        UnInit,
        Idle,
        Outgoing,
        Incoming,
        Connected

    }

    private static final String TAG = "CallPlusActivity";
    private static CallStatus currentStatus = CallStatus.UnInit;
    private FrameLayout local;
    private FrameLayout remote;
    private Button acceptButton;
    private Button hangUpButton;
    private Button switchCamera;
    private Button stopCamera;
    private Button enableMicrophone;
    private Button enableSpeaker;
    private EditText idInputEditText;
    private TextView statusTextView;

    private TextView callTime;

    private LinearLayout deviceLinearlayout;

    private String acceptCallId; // 收到来电时的callId

    private boolean isStartCamera = true;
    private boolean isEnableSpeaker = true;
    private boolean isSwitchCamera = true;
    private boolean isMicrophoneEnabled = true;

    private RCCallPlusClient client;
    private IRCCallPlusResultListener resultListener = new CallPlusResultListenerImpl() {
        @Override
        public void onStartCall(RCCallPlusCode code, String callId, List<RCCallPlusUser> userList) {
            super.onStartCall(code, callId, userList);
            runOnUiThread(() -> {
                if (code == RCCallPlusCode.SUCCESS) {
                    Log.d(TAG, "拨打成功 callId：" + callId);
                    currentStatus = CallStatus.Outgoing;
                } else {
                    currentStatus = CallStatus.UnInit;
                    Log.d(TAG, "拨打失败 code：" + code.getReason());
                }
                updateUI();
            });
        }

        @Override
        public void onAccept(RCCallPlusCode code, String callId) {
            super.onAccept(code, callId);
            if (code == RCCallPlusCode.SUCCESS) {
                currentStatus = CallStatus.Incoming;
                updateUI();
                Log.d(TAG, "接听成功 callId：" + callId);
            } else {
                Log.d(TAG, "接听失败 code：" + code.getReason());
                callToast("接听失败，请重新尝试");
            }
        }
    };

    private IRCCallPlusEventListener eventListener = new CallPlusEventListenerImpl() {
        @Override
        public void onReceivedCall(RCCallPlusSession callSession) {
            Log.d(TAG, "有人邀请你callId：" + callSession.getCallId());
            currentStatus = CallStatus.Idle;
            acceptCallId = callSession.getCallId();
            callToast("有人呼叫你");
            updateUI();
        }

        @Override
        public void onCallEnded(RCCallPlusSession session, RCCallPlusReason reason) {
            super.onCallEnded(session, reason);
            currentStatus = CallStatus.UnInit;
            callToast("通话结束");
            updateUI();
            cancelStartTimeTask();
        }

        @Override
        public void onCallConnected(RCCallPlusSession callSession) {
            super.onCallConnected(callSession);
            currentStatus = CallStatus.Connected;
            Log.d(TAG, "通话建立 callId：" + callSession.getCallId());
            updateUI();
            remoteView();
        }

        @Override
        public void onCallStartTimeFromServer(long callStartTime) {
            super.onCallStartTimeFromServer(callStartTime);
            callTime(callStartTime);
        }
    };

    private void updateUI() {
        runOnUiThread(() -> {
            if (currentStatus == CallStatus.UnInit) {
                local.removeAllViews();
                remote.removeAllViews();
                statusTextView.setText("--");
                acceptButton.setVisibility(View.VISIBLE);
                acceptButton.setText(R.string.call);
                acceptButton.setBackground(getResources().getDrawable(R.drawable.shape_call_plus_invite));
                hangUpButton.setVisibility(View.GONE);
                callTime.setText(R.string.default_time);
                resetUIState();
                deviceLinearlayout.setVisibility(View.GONE);
            } else if (currentStatus == CallStatus.Incoming) {
                acceptButton.setVisibility(View.GONE);
                statusTextView.setText(R.string.accept_success);
            } else if (currentStatus == CallStatus.Connected) {  // 通话建立
                deviceLinearlayout.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.call_on);
            } else if (currentStatus == CallStatus.Idle) { // 收到呼叫
                acceptButton.setVisibility(View.VISIBLE);
                hangUpButton.setVisibility(View.VISIBLE);
                acceptButton.setBackground(getResources().getDrawable(R.drawable.shape_call_plus_accept));
                acceptButton.setText(R.string.accept);
                statusTextView.setText(R.string.call_invite);
                RCCallPlusLocalVideoView videoView =
                        new RCCallPlusLocalVideoView(getApplicationContext());
                client.setVideoView(videoView);
                local.addView(videoView);
                client.startCamera();
            } else if (currentStatus == CallStatus.Outgoing) {  // 拨打成功
                startCallSuccess();
                statusTextView.setText(R.string.call_outgoing);
                acceptButton.setVisibility(View.GONE);
                hangUpButton.setVisibility(View.VISIBLE);
            }
        });
    }


    private void resetUIState() {
        isStartCamera = true;
        isEnableSpeaker = true;
        isSwitchCamera = true;
        isMicrophoneEnabled = true;

        enableSpeaker.setText(R.string.enableSpeakerNo);
        enableSpeaker.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
        enableSpeaker.setTextColor(getResources().getColor(R.color.blue));

        enableMicrophone.setText(R.string.enableMicrophoneNo);
        enableMicrophone.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
        enableMicrophone.setTextColor(getResources().getColor(R.color.blue));

        switchCamera.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
        switchCamera.setTextColor(getResources().getColor(R.color.blue));

        stopCamera.setText(R.string.stop_camera);
        stopCamera.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
        stopCamera.setTextColor(getResources().getColor(R.color.blue));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_plus);
        findViews();
        CallPlusHelper.INSTANCE.init(resultListener, eventListener);
        client = RCCallPlusClient.getInstance();
    }

    private void findViews() {
        local = findViewById(R.id.local);
        remote = findViewById(R.id.remote);
        idInputEditText = findViewById(R.id.et_userId);
        statusTextView = findViewById(R.id.tv_status);
        acceptButton = findViewById(R.id.accept);
        hangUpButton = findViewById(R.id.hang_up);
        switchCamera = findViewById(R.id.switch_camera);
        stopCamera = findViewById(R.id.stop_camera);
        enableMicrophone = findViewById(R.id.enableMicrophone);
        enableSpeaker = findViewById(R.id.enableSpeaker);
        callTime = findViewById(R.id.call_time);
        deviceLinearlayout = findViewById(R.id.device_linearLayout);
        acceptButton.setOnClickListener(this);
        hangUpButton.setOnClickListener(this);
        switchCamera.setOnClickListener(this);
        stopCamera.setOnClickListener(this);
        enableMicrophone.setOnClickListener(this);
        enableSpeaker.setOnClickListener(this);
    }

    private TimerTask mStartTimeTask;
    private Timer mStartTimeTimer;

    /**
     * 通话时间
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void callTime(long  callStartTime) {
        cancelStartTimeTask();
        mStartTimeTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        long duration =
                                                (System.currentTimeMillis() - callStartTime) / 1000;
                                        if (duration >= 3600) {
                                            callTime.setText(
                                                    "通话中 "
                                                            + String.format(
                                                            "%d:%02d:%02d",
                                                            duration / 3600,
                                                            (duration % 3600) / 60,
                                                            (duration % 60)));
                                        } else {
                                            callTime.setText(
                                                    "通话中 "
                                                            + String.format(
                                                            "%02d:%02d",
                                                            (duration % 3600) / 60,
                                                            (duration % 60)));
                                        }
                                    }
                                });
                    }
                };
        mStartTimeTimer = new Timer();
        mStartTimeTimer.schedule(mStartTimeTask, 0, 1000);
    }

    void cancelStartTimeTask() {
        if (mStartTimeTask != null) {
            mStartTimeTask.cancel();
            mStartTimeTask = null;
        }
        if (mStartTimeTimer != null) {
            mStartTimeTimer.cancel();
            mStartTimeTimer = null;
        }
    }

    /**
     * 拨打成功
     */
    private void startCallSuccess() {
        RCCallPlusLocalVideoView videoView =
                new RCCallPlusLocalVideoView(getApplicationContext());
        client.setVideoView(videoView);
        local.removeAllViews();
        local.addView(videoView);
        client.startCamera();
    }

    /**
     * 接听音视频通话
     */
    private void acceptCall() {
        if (currentStatus != CallStatus.Idle && TextUtils.isEmpty(acceptCallId)) {
            Log.d(TAG, "接听失败：原因可能为：不在被呼叫状态或者callId为空");
            return;
        }
        client.accept(acceptCallId);
    }

    private void remoteView() {
        runOnUiThread(() -> {
            List<String> userIds = new ArrayList<>();
            List<RCCallPlusRemoteVideoView> rcCallPlusRemoteVideoViewList = new ArrayList<>();
            for (RCCallPlusUser rcCallPlusUser : client.getCurrentCallSession().getRemoteUserList()) {
                String remoteUserId = rcCallPlusUser.getUserId();
                userIds.add(remoteUserId);
                client.removeVideoView(userIds);
                RCCallPlusRemoteVideoView rcCallPlusVideoView = new
                        RCCallPlusRemoteVideoView(remoteUserId, getApplicationContext(), false);
                remote.removeAllViews();
                remote.addView(rcCallPlusVideoView);
                rcCallPlusRemoteVideoViewList.add(rcCallPlusVideoView);
            }
            client.setVideoView(rcCallPlusRemoteVideoViewList);
        });
    }

    /**
     * 拨打
     */
    private void startCall() {
        List<String> list = new ArrayList<>();
        String callUserId = idInputEditText.getText().toString();
        if (TextUtils.isEmpty(callUserId)) {
            callToast("请输入正确的userid");
            return;
        }
        list.add(callUserId);
        CallPlusHelper.INSTANCE.startCall(callUserId);
    }

    private void callToast(String s) {
        runOnUiThread(() -> Toast.makeText(CallPlusActivity.this, s, Toast.LENGTH_SHORT).show());
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.accept) {   // 拨打-接听
            if (acceptButton.getText().equals(getResources().getString(R.string.call))) {
                startCall();
            } else {
                acceptCall();
            }
        } else if (viewId == R.id.hang_up) {  // 挂断
            client.hangup();
        } else if (viewId == R.id.stop_camera) {  // 关闭摄像头
            if (isStartCamera) {
                client.stopCamera();
                stopCamera.setText(R.string.start_camera);
                stopCamera.setBackground(getResources().getDrawable(R.drawable.shape_call_plus_invite));
                stopCamera.setTextColor(getResources().getColor(R.color.white));
            } else {
                client.startCamera();
                stopCamera.setText(R.string.stop_camera);
                stopCamera.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
                stopCamera.setTextColor(getResources().getColor(R.color.blue));
            }
            isStartCamera = !isStartCamera;
        } else if (viewId == R.id.switch_camera) {  // 切换摄像头
            if (isSwitchCamera) {
                switchCamera.setBackground(getResources().getDrawable(R.drawable.shape_call_plus_invite));
                switchCamera.setTextColor(getResources().getColor(R.color.white));
            } else {
                switchCamera.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
                switchCamera.setTextColor(getResources().getColor(R.color.blue));
            }
            isSwitchCamera = !isSwitchCamera;
            client.switchCamera();
        } else if (viewId == R.id.enableMicrophone) {  // 是否关闭麦克风
            if (isMicrophoneEnabled) {
                client.enableMicrophone(true);
                enableMicrophone.setText(R.string.enableMicrophoneNo);
                enableMicrophone.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
                enableMicrophone.setTextColor(getResources().getColor(R.color.blue));
            } else {
                enableMicrophone.setText(R.string.enableMicrophoneYes);
                client.enableMicrophone(false);
                enableMicrophone.setBackground(getResources().getDrawable(R.drawable.shape_call_plus_invite));
                enableMicrophone.setTextColor(getResources().getColor(R.color.white));
            }
            isMicrophoneEnabled = !isMicrophoneEnabled;
        } else if (viewId == R.id.enableSpeaker) {   // 扬声器 -听筒切换
            if (isEnableSpeaker) {
                client.enableSpeaker(false);
                enableSpeaker.setText(R.string.enableSpeakerYes);
                enableSpeaker.setBackground(getResources().getDrawable(R.drawable.shape_call_plus_invite));
                enableSpeaker.setTextColor(getResources().getColor(R.color.white));
            } else {
                client.enableSpeaker(true);
                enableSpeaker.setText(R.string.enableSpeakerNo);
                enableSpeaker.setBackground(getResources().getDrawable(R.drawable.shape_device_btn));
                enableSpeaker.setTextColor(getResources().getColor(R.color.blue));
            }
            isEnableSpeaker = !isEnableSpeaker;
        }


    }


}