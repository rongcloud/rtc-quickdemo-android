package cn.rongcloud.demo.meeting.presenter;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCJoinType;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RTCErrorCode;

/**
 * 对 RTCSDK 会议功能相关 SDK 调用的封装类
 */
public class MeetingPresenter {

    MeetingCallback mMeetingCallback = null;
    private RCRTCRoom mRtcRoom = null;
    private final IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {
        /**
         * 房间内用户发布资源
         *
         * @param rcrtcRemoteUser 远端用户
         * @param list    发布的资源
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {
            subscribeAVStream();
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

        /**
         * 用户加入房间
         *
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserJoined(final RCRTCRemoteUser rcrtcRemoteUser) {
            try {
                getView().onUserJoined(rcrtcRemoteUser);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        /**
         * 用户离开房间
         *
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            try {
                getView().onUserLeft(rcrtcRemoteUser);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {
        }

        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> list) {
        }

        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> list) {
        }

        /**
         * 自己退出房间。 例如断网退出等
         * @param i 状态码
         */
        @Override
        public void onLeaveRoom(int i) {
        }
    };

    protected MeetingCallback getView() {
        if (mMeetingCallback == null) {
            throw new IllegalStateException("view is not attached");
        } else {
            return mMeetingCallback;
        }
    }

    public void attachView(MeetingCallback callback) {
        mMeetingCallback = callback;
    }

    public void detachView() {
        mMeetingCallback = null;
    }

    /**
     * 主动订阅远端用户发布的流
     * 视频流需要用户设置用于显示载体的videoview
     */
    public void subscribeAVStream() {
        if (mRtcRoom == null || mRtcRoom.getRemoteUsers() == null) {
            return;
        }
        final List<RCRTCInputStream> inputStreams = new ArrayList<>();
        for (final RCRTCRemoteUser remoteUser : mRtcRoom.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            List<RCRTCInputStream> userStreams = remoteUser.getStreams();
            for (RCRTCInputStream inputStream : userStreams) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    // 选择订阅大流或是小流。默认小流
                    ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                }
            }
            inputStreams.addAll(remoteUser.getStreams());
        }

        if (inputStreams.size() == 0) {
            return;
        }
        mRtcRoom.getLocalUser().subscribeStreams(inputStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {


                try {
                    getView().onSubscribeSuccess(inputStreams);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                try {
                    getView().onSubscribeFailed();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发布默认流
     */
    public void publishDefaultAVStream() {
        if (mRtcRoom == null) {
            return;
        }
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        mRtcRoom.getLocalUser().publishDefaultStreams(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                try {
                    getView().onPublishSuccess();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                try {
                    getView().onPublishFailed();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 配置rtc sdk
     */
    public void config(Context context) {

        RCRTCConfig.Builder configBuilder = RCRTCConfig.Builder.create();
        // 是否硬解码
        configBuilder.enableHardwareDecoder(true);
        // 是否硬编码
        configBuilder.enableHardwareEncoder(true);

        // init 需结合 uninit 使用，否则有些配置无法重新初始化
        RCRTCEngine.getInstance().unInit();
        RCRTCEngine.getInstance().init(context, configBuilder.build());

        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        // 设置分辨率
        videoConfigBuilder.setVideoResolution(RCRTCParamsType.RCRTCVideoResolution.RESOLUTION_720_1280);
        // 设置帧率
        videoConfigBuilder.setVideoFps(RCRTCParamsType.RCRTCVideoFps.Fps_30);
        /**
         * 设置最小码率，可根据分辨率RCRTCVideoResolution设置
         * {@link RCRTCParamsType.RCRTCVideoResolution)}
         */
        videoConfigBuilder.setMinRate(250);
        /**
         * 设置最大码率，可根据分辨率RCRTCVideoResolution设置
         * {@link RCRTCParamsType.RCRTCVideoResolution)}
         */
        videoConfigBuilder.setMaxRate(2200);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder.build());

        // 听筒播放
        RCRTCEngine.getInstance().enableSpeaker(false);
    }

    public void joinRoom(String roomId, boolean isEncryption) {
        RCRTCRoomConfig roomConfig = RCRTCRoomConfig.Builder.create()
                // 根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
                .setRoomType(RCRTCRoomType.MEETING)
                .enableAudioEncryption(isEncryption)
                .enableVideoEncryption(isEncryption)
                .build();
        RCRTCEngine.getInstance().joinRoom(roomId, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                // 远端用户，本地用户相关资源的获取都依赖RtcRoom
                MeetingPresenter.this.mRtcRoom = rcrtcRoom;
                // 注册房间回调
                rcrtcRoom.registerRoomListener(roomEventsListener);
                try {
                    getView().onJoinRoomSuccess(rcrtcRoom);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                try {
                    getView().onJoinRoomFailed(rtcErrorCode);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void leaveRoom() {
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
            }

            @Override
            public void onSuccess() {
            }
        });
    }

    /**
     * activity相关回调
     */
    public interface MeetingCallback {
        void onJoinRoomSuccess(RCRTCRoom rcrtcRoom);

        void onJoinRoomFailed(RTCErrorCode rtcErrorCode);

        void onPublishSuccess();

        void onPublishFailed();

        void onSubscribeSuccess(List<RCRTCInputStream> inputStreamList);

        void onSubscribeFailed();

        void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser);

        void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser);
    }
}
