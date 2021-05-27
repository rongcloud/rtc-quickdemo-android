/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.screenshare.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import cn.rongcloud.rtc.api.callback.IRCRTCVideoSource;
import cn.rongcloud.rtc.api.stream.RCRTCSurfaceTextureHelper;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenShareUtil implements RCRTCSurfaceTextureHelper.Sink {
    private MediaProjection mMediaProjection;
    private static final int VIRTUAL_DISPLAY_DPI = 400;
    private static final int DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
    private IRCRTCVideoSource.IRCVideoConsumer videoConsumer;
    private VirtualDisplay mVirtualDisplay;
    private RCRTCVideoOutputStream videoOutputStream;

    public void init(Context context, RCRTCVideoOutputStream outputStream, Intent mediaProjectionData, int width, int height) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.mMediaProjection = mediaProjectionManager
                .getMediaProjection(Activity.RESULT_OK, mediaProjectionData);
        this.videoOutputStream = outputStream;
        outputStream.setSource(new IRCRTCVideoSource() {
            @Override
            public void onInit(IRCVideoConsumer ircVideoConsumer) {
                videoConsumer = ircVideoConsumer;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void onDispose() {
                videoConsumer = null;
            }
        });


        mVirtualDisplay = createVirtualDisplay(width, height);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay(int width, int height) {
        this.videoOutputStream.getTextureHelper().setTextureSize(width, height);
        VirtualDisplay virtualDisplay = this.mMediaProjection.createVirtualDisplay(
                "RongRTC_ScreenCapture",
                width,
                height,
                VIRTUAL_DISPLAY_DPI,
                DISPLAY_FLAGS,
                new Surface(this.videoOutputStream.getTextureHelper().getSurfaceTexture()),
                null,
                this.videoOutputStream.getTextureHelper().getHandler());
        this.videoOutputStream.getTextureHelper().startListening(this);
        return virtualDisplay;
    }

    public void release() {
        this.videoOutputStream.getTextureHelper().stopListening();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }

    }

    @Override
    public void onTexture(final int textureWidth, final int textureHeight, int oexTextureId, float[] transformMatrix, int rotation, long timestampNs) {
        if (videoConsumer != null) {
            videoConsumer.writeTexture(textureWidth, textureHeight, oexTextureId, transformMatrix, rotation, timestampNs);
        }
    }
}