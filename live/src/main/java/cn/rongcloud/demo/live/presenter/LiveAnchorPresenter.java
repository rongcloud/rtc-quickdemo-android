/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.live.presenter;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Handler;

import com.serenegiant.usb.USBMonitor;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.demo.live.AnchorConfig;
import cn.rongcloud.demo.live.helper.UsbCameraHelper;
import cn.rongcloud.demo.live.ui.RTCMixLayout;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCMixConfig;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoSource;
import cn.rongcloud.rtc.api.stream.RCRTCFileVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RCRTCSubscribeState;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.center.stream.RCVideoInputStreamImpl;

import static cn.rongcloud.demo.live.AnchorConfig.enableSpeaker;
import static cn.rongcloud.rtc.base.RCRTCLiveRole.BROADCASTER;

/**
 * 主播端对sdk的调用
 */
public class LiveAnchorPresenter extends LiveBasePresenter {

    LiveCallback mLiveCallback = null;
    UsbCameraHelper usbCameraHelper;
    private RCRTCRoom mRtcRoom = null;
    private RCRTCLiveInfo mLiveInfo;
    private RCRTCFileVideoOutputStream fileVideoOutputStream;
    private RCRTCVideoOutputStream mOutputStream;
    private volatile IRCRTCVideoSource.IRCVideoConsumer videoConsumer;
    private volatile boolean observerEnabled = false;
    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {

        /**
         * 房间内用户发布资源,直播模式下仅主播身份会执行该回调
         *
         * @param rcrtcRemoteUser 远端用户
         * @param list    发布的资源
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {

            try {
                subscribeAVStream();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {

        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
            try {
                getView().onRemoteUserUnpublishResource(rcrtcRemoteUser, list);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
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

    public LiveAnchorPresenter(Context context) {
        super(context);
    }

    protected LiveCallback getView() {
        if (mLiveCallback == null) {
            throw new IllegalStateException("view is not attached");
        } else {
            return mLiveCallback;
        }
    }

    /**
     * 取消发送自定义文件流
     */
    public void unpublishCustomStream() {

        mRtcRoom.getLocalUser().unpublishStream(fileVideoOutputStream, new IRCRTCResultCallback() {
            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                getView().onUnpublishCustomStreamFailed(rtcErrorCode);
            }

            @Override
            public void onSuccess() {
                getView().onUnpublishCustomStreamSuccess();
            }
        });

    }

    /**
     * 发送自定义文件流
     */
    public void publishCustomStream(String filePath) {
        fileVideoOutputStream = RCRTCEngine.getInstance().createFileVideoOutputStream(filePath, false,
                true,
                "RongRTCFileVideo", RCRTCVideoStreamConfig.Builder.create()
                        .setVideoResolution(RCRTCParamsType.RCRTCVideoResolution.RESOLUTION_360_640)
                        .setVideoFps(RCRTCParamsType.RCRTCVideoFps.Fps_24).build());

        mRtcRoom.getLocalUser().publishStream(fileVideoOutputStream, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                try {
                    getView().onPublishCustomStreamSuccess(fileVideoOutputStream);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                try {
                    getView().onPublishCustomStreamFailed(errorCode);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void unpublishUsbCameraStream() {
        mRtcRoom.getLocalUser().unpublishStream(mOutputStream, new IRCRTCResultCallback() {
            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                getView().onUnpublishUsbStreamFailed(rtcErrorCode);
            }

            @Override
            public void onSuccess() {
                usbCameraHelper.release();
                getView().onUnpublishUsbStreamSuccess();
            }
        });
    }

    public void publishUsbCameraStream() {
        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        videoConfigBuilder.setVideoResolution(RCRTCParamsType.RCRTCVideoResolution.RESOLUTION_480_640);
        mOutputStream = RCRTCEngine.getInstance().createVideoStream("USB", videoConfigBuilder.build());
        mOutputStream.setSource(new IRCRTCVideoSource() {
            @Override
            public void onInit(IRCVideoConsumer observer) {
                videoConsumer = observer;
            }

            @Override
            public void onStart() {
                observerEnabled = true;
            }

            @Override
            public void onStop() {
                videoConsumer = null;
            }

            @Override
            public void onDispose() {
                observerEnabled = false;
            }
        });

        usbCameraHelper = new UsbCameraHelper(mContext, mOutputStream.getTextureHelper(), new UsbCameraHelper.CameraHelperCallBack() {

            @Override
            public void onAttach(UsbDevice device) {
            }

            @Override
            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                mRtcRoom.getLocalUser().publishStream(mOutputStream, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        try {
                            getView().onPublishUsbStreamSuccess(mOutputStream);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        try {
                            getView().onPublishUsbStreamFailed(errorCode);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {

            }

            @Override
            public void onDettach(UsbDevice device) {
            }

            @Override
            public void onTexture(int textureWidth, int textureHeight, int oexTextureId, float[] transformMatrix, int rotation, long timestampNs, Handler handler) {
                if (null != videoConsumer)
                    videoConsumer.writeTexture(textureWidth, textureHeight, oexTextureId, transformMatrix, rotation, timestampNs);
            }
        });
    }

    /**
     * 设置合流布局
     */
    public void setMixLayout(String str) {
        RCRTCMixConfig config = null;
        String text = "";

        switch (str) {
            case AnchorConfig.ADAPTIVE://自适应布局
                List<RCRTCStream> streams = new ArrayList<>();
                streams.add(RCRTCEngine.getInstance().getDefaultVideoStream());
                text = AnchorConfig.CUSTOM;
                if (mRtcRoom != null) {
                    for (RCRTCRemoteUser remoteUser : mRtcRoom.getRemoteUsers()) {
                        for (RCRTCInputStream stream : remoteUser.getStreams()) {
                            if (stream.getMediaType() == RCRTCMediaType.VIDEO
                                    && ((RCVideoInputStreamImpl) stream)
                                    .getSubscribeState() == RCRTCSubscribeState.SUBSCRIBED) {
                                streams.add(stream);
                            }
                        }
                    }
                }
                config = RTCMixLayout.getInstance().create_Custom_MixConfig(streams);
                break;
            case AnchorConfig.CUSTOM: // 自定义布局
                text = AnchorConfig.SUSPENSION;
                config = RTCMixLayout.getInstance().create_Suspension_MixConfig(RCRTCEngine.getInstance()
                        .getDefaultVideoStream()); // 切换为悬浮布局
                break;
            case AnchorConfig.SUSPENSION: // 悬浮布局
                config = RTCMixLayout.getInstance().create_Adaptive_MixConfig();  // 切换为自适应布局
                text = AnchorConfig.ADAPTIVE;
                break;
        }
        if (mLiveInfo == null) {
            return;
        }
        final String finalText = text;
        mLiveInfo.setMixConfig(config, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                try {
                    getView().onSetMixLayoutSuccess(finalText);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                try {
                    getView().onSetMixLayoutFailed(rtcErrorCode);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void attachView(LiveCallback callback) {
        mLiveCallback = callback;
    }

    public void detachView() {
        mLiveCallback = null;
        if (null != usbCameraHelper) {
            usbCameraHelper.release();
        }
    }

    /**
     * 获得当前视频流
     */
    public void getVideoStream(List<RCRTCVideoOutputStream> outputStreams, List<RCRTCVideoInputStream> inputStreams) {
        for (final RCRTCRemoteUser remoteUser : mRtcRoom.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            List<RCRTCInputStream> userStreams = remoteUser.getStreams();
            for (RCRTCInputStream i : userStreams) {
                if (i.getMediaType() == RCRTCMediaType.VIDEO) {
                    inputStreams.add((RCRTCVideoInputStream) i);
                }
            }
        }

        for (RCRTCOutputStream o : mRtcRoom.getLocalUser().getStreams()) {
            if (o.getMediaType() == RCRTCMediaType.VIDEO) {
                outputStreams.add((RCRTCVideoOutputStream) o);
            }
        }
    }

    /**
     * 订阅远端用户资源
     */
    public void subscribeAVStream() {
        if (mRtcRoom == null || mRtcRoom.getRemoteUsers() == null) {
            return;
        }
        List<RCRTCInputStream> subscribeInputStreams = new ArrayList<>();
        for (final RCRTCRemoteUser remoteUser : mRtcRoom.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            List<RCRTCInputStream> userStreams = remoteUser.getStreams();
            for (RCRTCInputStream inputStream : userStreams) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    //选择订阅大流或是小流。默认小流
                    ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                }
            }
            subscribeInputStreams.addAll(userStreams);
        }

        if (subscribeInputStreams.size() == 0) {
            return;
        }
        mRtcRoom.getLocalUser().subscribeStreams(subscribeInputStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                try {
                    getView().onSubscribeSuccess();
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

    public void unPublishDefaultAVStream() {
        if (mRtcRoom == null) {
            return;
        }
        mRtcRoom.getLocalUser().unpublishDefaultStreams(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
            }
        });
    }

    /**
     * 推送用户资源
     */
    public void publishDefaultAVStream() {
        if (mRtcRoom == null) {
            return;
        }
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        mRtcRoom.getLocalUser().publishDefaultLiveStreams(new IRCRTCResultDataCallback<RCRTCLiveInfo>() {
            @Override
            public void onSuccess(RCRTCLiveInfo liveInfo) {
                try {
                    mLiveInfo = liveInfo;
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
     * 初始化及设置
     */
    public void config(Context context) {
        RCRTCConfig.Builder configBuilder = RCRTCConfig.Builder.create();
        // 是否硬解码
        configBuilder.enableHardwareDecoder(true);
        // 是否硬编码
        configBuilder.enableHardwareEncoder(true);
        RCRTCEngine.getInstance().unInit();
        RCRTCEngine.getInstance().init(context, configBuilder.build());

        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        // 设置分辨率
        videoConfigBuilder.setVideoResolution(AnchorConfig.resolution);
        // 设置帧率
        videoConfigBuilder.setVideoFps(AnchorConfig.fps);
        /**
         * 设置最小码率，可根据分辨率RCRTCVideoResolution设置
         * {@link RCRTCParamsType.RCRTCVideoResolution)}
         */
        videoConfigBuilder.setMinRate(AnchorConfig.mixRate);
        /**
         * 设置最大码率，可根据分辨率RCRTCVideoResolution设置
         * {@link RCRTCParamsType.RCRTCVideoResolution)}
         */
        videoConfigBuilder.setMaxRate(AnchorConfig.maxRate);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder.build());
        // 听筒播放，为避免噪音可在开发时设置为 false
        RCRTCEngine.getInstance().enableSpeaker(enableSpeaker);
    }

    /**
     * 加入房间
     */
    public void joinRoom(String roomId) {
        RCRTCRoomConfig roomConfig = RCRTCRoomConfig.Builder.create()
                // 根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
                .setRoomType(RCRTCRoomType.LIVE_AUDIO_VIDEO)
                .setLiveRole(BROADCASTER)
                .build();
        RCRTCEngine.getInstance().joinRoom(roomId, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                LiveAnchorPresenter.this.mRtcRoom = rcrtcRoom;
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
     * 封装对ui层的回调，IRCRTCRoomEventsListener 提供了的更多的回调能力，根据业务需求添加监听
     */
    public interface LiveCallback {

        void onJoinRoomSuccess(RCRTCRoom rcrtcRoom);

        void onJoinRoomFailed(RTCErrorCode rtcErrorCode);

        void onPublishSuccess();

        void onPublishFailed();

        void onSubscribeSuccess();

        void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list);

        void onSubscribeFailed();

        void onSetMixLayoutSuccess(String s);

        void onSetMixLayoutFailed(RTCErrorCode code);

        void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser);

        void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser);

        void onPublishCustomStreamSuccess(RCRTCVideoOutputStream stream);

        void onPublishCustomStreamFailed(RTCErrorCode code);

        void onUnpublishCustomStreamSuccess();

        void onUnpublishCustomStreamFailed(RTCErrorCode code);

        void onPublishUsbStreamSuccess(RCRTCVideoOutputStream stream);

        void onPublishUsbStreamFailed(RTCErrorCode code);

        void onUnpublishUsbStreamSuccess();

        void onUnpublishUsbStreamFailed(RTCErrorCode code);
    }
}
