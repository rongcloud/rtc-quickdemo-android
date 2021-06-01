/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.live.presenter;

import android.content.Context;

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
import cn.rongcloud.rtc.base.RCRTCLiveRole;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;

/**
 * 观众端对 SDK 的调用
 */
public class LiveAudiencePresenter extends LiveBasePresenter {

    LiveCallback mLiveCallback = null;
    private RCRTCRoom mRtcRoom = null;
    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {

        /**
         * 房间内用户发布资源
         *
         * @param rcrtcRemoteUser 远端用户
         * @param list    发布的资源
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {
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
            try {
                getView().onPublishLiveStreams(list);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
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

    public LiveAudiencePresenter(Context context) {
        super(context);
    }

    protected LiveCallback getView() {
        if (mLiveCallback == null) {
            throw new IllegalStateException("view is not attached");
        } else {
            return mLiveCallback;
        }
    }

    public void attachView(LiveCallback callback) {
        mLiveCallback = callback;
    }

    public void detachView() {
        mLiveCallback = null;
    }

    public void subscribeAVStream() {
        if (mRtcRoom == null || mRtcRoom.getRemoteUsers() == null) {
            return;
        }
        final List<RCRTCInputStream> inputStreams = mRtcRoom.getLiveStreams();

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

    public void publishDefaultAVStream() {
    }

    public void config(Context context) {
        RCRTCConfig.Builder configBuilder = RCRTCConfig.Builder.create();
        // 是否硬解码
        configBuilder.enableHardwareDecoder(true);
        // 是否硬编码
        configBuilder.enableHardwareEncoder(true);

        RCRTCEngine.getInstance().unInit();
        RCRTCEngine.getInstance().init(context, configBuilder.build());
    }

    public void joinRoom(String roomId) {
        RCRTCRoomConfig roomConfig = RCRTCRoomConfig.Builder.create()
                // 根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
                .setRoomType(RCRTCRoomType.LIVE_AUDIO_VIDEO)
                .setLiveRole(RCRTCLiveRole.AUDIENCE)
                .build();
        RCRTCEngine.getInstance().joinRoom(roomId, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                LiveAudiencePresenter.this.mRtcRoom = rcrtcRoom;
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
        if (null != mRtcRoom)
            mRtcRoom.unregisterRoomListener();
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
     * 封装对 UI 层的回调，IRCRTCRoomEventsListener 提供了的更多的回调能力，根据业务需求添加监听
     */
    public interface LiveCallback {
        void onJoinRoomSuccess(RCRTCRoom rcrtcRoom);

        void onJoinRoomFailed(RTCErrorCode rtcErrorCode);

        void onSubscribeSuccess(List<RCRTCInputStream> inputStreamList);

        void onSubscribeFailed();

        void onPublishLiveStreams(List<RCRTCInputStream> list);

        void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list);

        void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser);

        void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser);
    }
}
