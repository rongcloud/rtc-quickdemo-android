package cn.rongcloud.demo.live.ui;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCMixConfig;
import cn.rongcloud.rtc.api.RCRTCMixConfig.CustomLayoutList.CustomLayout;
import cn.rongcloud.rtc.api.RCRTCMixConfig.MixLayoutMode;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCStream;

/**
 * 主播合理布局设置类
 * TODO 参考主播合流布局设置文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/anchorManage/layout/android.html#layout
 */
public class RTCMixLayout {
    private static final String TAG = "RTCMixLayout";
    private static final int MARGIN = 10;
    // 是否裁减画布
    private static boolean isCrop = true;

    private RTCMixLayout() {
    }

    public static RTCMixLayout getInstance() {
        return holder.layout;
    }

    /**
     * 创建自定义合流布局配置，TODO 请参考开发文档：https://docs.rongcloud.cn/v4/views/rtc/livevideo/guide/anchorManage/layout/android.html#custom
     *
     * @param rtcStreams 本主播和已经订阅成功的其他主播的【视频流】集合
     * @return
     */
    public RCRTCMixConfig create_Custom_MixConfig(List<RCRTCStream> rtcStreams) {
        RCRTCMixConfig config = new RCRTCMixConfig();
        // 1. 设置自定义合流布局模式
        config.setLayoutMode(MixLayoutMode.CUSTOM);

        // 2. 合流画布设置
        canvasConfiguration(config);

        // 3. 设置每个视频流小窗口的坐标及宽高
        ArrayList<CustomLayout> list = new ArrayList<>();
        CustomLayout videoLayout = null;

        RCRTCVideoStreamConfig defaultVideoConfig = RCRTCEngine.getInstance().getDefaultVideoStream().getVideoConfig();
        int width = defaultVideoConfig.getVideoResolution().getWidth();//默认 480
        int height = defaultVideoConfig.getVideoResolution().getHeight();//默认 640
        int size = rtcStreams.size();
        for (int i = 0; i < size; i++) {
            videoLayout = new CustomLayout();
            videoLayout.setVideoStream(rtcStreams.get(i));
            if (size == 1) {
                Log.d("RTCMixLayout", "  ---single");
                single(videoLayout, i, width, height);
            } else if (size == 2) {
                Log.d("RTCMixLayout", "  ---twoPeople");
                twoPeople(videoLayout, i, width, height);
            } else if (size == 3) {
                Log.d("RTCMixLayout", "  ---threePeople");
                threePeople(videoLayout, i, width, height);
            } else if (size == 4) {
                Log.d("RTCMixLayout", "  ---fourPeople");
                fourPeople(videoLayout, i, width, height);
            } else {
                //
            }
            list.add(videoLayout);
        }
        config.setCustomLayouts(list);
        return config;
    }

    /**
     * 创建悬浮布局合流配置
     *
     * @param stream 当做背景的视频流
     * @return
     */
    public RCRTCMixConfig create_Suspension_MixConfig(RCRTCStream stream) {
        RCRTCMixConfig config = new RCRTCMixConfig();
        if (stream == null) {
            Log.e(TAG, "create_Suspension_MixConfig . stream == null");
            return config;
        }
        // 1. 设置悬浮合流布局模式
        config.setLayoutMode(MixLayoutMode.SUSPENSION);

        // 2. 合流画布设置
        canvasConfiguration(config);

        // 3. 设置当做背景的视频流
        config.setHostVideoStream(stream);
        return config;
    }

    /**
     * 创建自适应布局合流配置
     *
     * @return
     */
    public RCRTCMixConfig create_Adaptive_MixConfig() {
        RCRTCMixConfig config = new RCRTCMixConfig();
        // 1. 设置自适应合流布局模式
        config.setLayoutMode(MixLayoutMode.ADAPTIVE);
        // 2. 合流画布设置
        canvasConfiguration(config);
        return config;
    }

    /**
     * 画布设置
     */
    private RCRTCMixConfig canvasConfiguration(RCRTCMixConfig config) {
        // TODO RCRTCMixConfig API文档：https://www.rongcloud.cn/docs/api/android/rtclib_v4/cn/rongcloud/rtc/api/RCRTCMixConfig.html
        RCRTCMixConfig.MediaConfig mediaConfig = new RCRTCMixConfig.MediaConfig();
        config.setMediaConfig(mediaConfig);
        // 视频输出配置
        RCRTCMixConfig.MediaConfig.VideoConfig videoConfig = new RCRTCMixConfig.MediaConfig.VideoConfig();
        mediaConfig.setVideoConfig(videoConfig);
        // 大流视频的输出参数
        RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout normal = new RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout();
        videoConfig.setVideoLayout(normal);
//        videoConfig.setBackgroundColor(255,0,0);
        // 推荐宽、高、帧率参数值可以通过默认视频流的配置获取，也可以根据实际需求来自定义设置
        // 如不设置宽高值则服务端将使用默认宽高 360 * 640
        // 例:发布的视频分辨率为720 * 1280，如果不设置则观众端看到的视频分辨率为 360 * 640,
        // 所以如果想让观众端看到的视频分辨率和发布视频分辨率一致，则应从发布的视频流中获取分辨率配置并设置到 mediaConfig 中
        // TODO RCRTCVideoStreamConfig API文档：https://www.rongcloud.cn/docs/api/android/rtclib_v4/cn/rongcloud/rtc/api/stream/RCRTCVideoStreamConfig.html
        RCRTCVideoStreamConfig defaultVideoConfig = RCRTCEngine.getInstance().getDefaultVideoStream().getVideoConfig();
        int fps = defaultVideoConfig.getVideoFps().getFps();
        int width = defaultVideoConfig.getVideoResolution().getWidth();
        int height = defaultVideoConfig.getVideoResolution().getHeight();
        normal.setWidth(width);   //视频宽
        normal.setHeight(height); //视频高
        normal.setFps(fps); //视频帧率
        // 小流视频的输出参数和标准视频流设置方法是一样的，不同的只是输出参数
        // RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout tinyVideo = new RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout();
        // videoConfig.setTinyVideoLayout(tinyVideo);

        // 设置渲染裁剪模式（可选）
        // 裁剪模式共分为两种： VideoRenderMode.CROP/VideoRenderMode.WHOLE(默认)
        videoConfig.setExtend(new RCRTCMixConfig.MediaConfig.VideoConfig.VideoExtend(isCrop ? RCRTCMixConfig.VideoRenderMode.CROP : RCRTCMixConfig.VideoRenderMode.WHOLE));
        return config;
    }

    private CustomLayout single(CustomLayout videoLayout, int i, int width, int height) {
        videoLayout.setX(0);
        videoLayout.setY(0);
        videoLayout.setWidth(width);
        videoLayout.setHeight(height);
        return videoLayout;
    }

    /**
     * 2个主播合流布局
     *
     * @param videoLayout
     * @param i           流下标
     * @param width       本地主播发布视频流的宽
     * @param height      本地主播发布的视频流高
     * @return
     */
    private CustomLayout twoPeople(CustomLayout videoLayout, int i, int width, int height) {
        int streamWidth = width / 2 - (MARGIN * 2);
        if (i == 0) {
            videoLayout.setX(MARGIN);
            videoLayout.setY((height - streamWidth) / 2);
            videoLayout.setWidth(streamWidth);
            videoLayout.setHeight(streamWidth);
        } else if (i == 1) {
            videoLayout.setX(streamWidth + (MARGIN * 3));
            videoLayout.setY((height - streamWidth) / 2);
            videoLayout.setWidth(streamWidth);
            videoLayout.setHeight(streamWidth);
        }
        return videoLayout;
    }

    private CustomLayout threePeople(CustomLayout videoLayout, int i, int videoWidth, int videoHeight) {
        int width = 220, height = 220;
        int y2 = (videoHeight - 240 - height - MARGIN) / 2;//85
        if (i == 0) {
            videoLayout.setX(videoWidth / 4);
            videoLayout.setY(y2);
            videoLayout.setWidth(240);
            videoLayout.setHeight(240);
        } else if (i == 1) {
            videoLayout.setX(MARGIN);
            videoLayout.setY(y2 + MARGIN + 240);//335
            videoLayout.setWidth(width);
            videoLayout.setHeight(height);
        } else if (i == 2) {
            videoLayout.setX(245);
            videoLayout.setY(y2 + MARGIN + 240);
            videoLayout.setWidth(width);
            videoLayout.setHeight(height);
        }
        return videoLayout;
    }

    private CustomLayout fourPeople(CustomLayout videoLayout, int i, int videoWidth, int videoHeight) {
        int width = 230, height = 310;
        switch (i) {
            case 0:
                videoLayout.setX(0);
                videoLayout.setY(0);
                videoLayout.setWidth(width);
                videoLayout.setHeight(height);
                break;
            case 1:
                videoLayout.setX(videoWidth / 2 + MARGIN);
                videoLayout.setY(0);
                videoLayout.setWidth(width);
                videoLayout.setHeight(height);
                break;
            case 2:
                videoLayout.setX(0);
                videoLayout.setY(videoHeight / 2 + MARGIN);
                videoLayout.setWidth(width);
                videoLayout.setHeight(height);
                break;
            case 3:
                videoLayout.setX(videoWidth / 2 + MARGIN);
                videoLayout.setY(videoHeight / 2 + MARGIN);
                videoLayout.setWidth(width);
                videoLayout.setHeight(height);
                break;
            default:
                break;
        }
        return videoLayout;
    }

    private static class holder {
        static RTCMixLayout layout = new RTCMixLayout();
    }
}


