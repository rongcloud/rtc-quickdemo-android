/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.screenshare.ui.model;

import androidx.annotation.NonNull;

import cn.rongcloud.rtc.api.stream.RCRTCVideoView;

public class VideoViewWrapper {
    private final RCRTCVideoView mRCRTCVideoView;
    private final String streamId;
    private final String userId;

    public VideoViewWrapper(String streamId, @NonNull RCRTCVideoView rcrtcVideoView) {
        this(null, streamId, rcrtcVideoView);
    }

    public VideoViewWrapper(String userId, String streamId, @NonNull RCRTCVideoView rcrtcVideoView) {
        mRCRTCVideoView = rcrtcVideoView;
        this.streamId = streamId;
        this.userId = userId;
    }

    public RCRTCVideoView getRCRTCVideoView() {
        return mRCRTCVideoView;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getUserId() {
        return userId;
    }
}
