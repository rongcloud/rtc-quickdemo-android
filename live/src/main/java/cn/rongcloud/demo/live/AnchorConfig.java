package cn.rongcloud.demo.live;

import cn.rongcloud.rtc.base.RCRTCParamsType;

public class AnchorConfig {
    public static final String CAMERA_STATUS_CLOSE = "关闭摄像头";
    public static final String CAMERA_STATUS_OPEN = "打开摄像头";
    public static final String MIC_STATUS_CLOSE = "关闭麦克风";
    public static final String MIC_STATUS_OPEN = "打开麦克风";
    public static final String LIVE_STATUS_START = "开始直播";
    public static final String LIVE_STATUS_END = "结束直播";
    public static final String SUSPENSION = "悬浮布局";//默认
    public static final String ADAPTIVE = "自适应布局";
    public static final String CUSTOM = "自定义布局";
    // 直播中使用的分辨率 帧率 码率
    public static RCRTCParamsType.RCRTCVideoResolution resolution = RCRTCParamsType.RCRTCVideoResolution.RESOLUTION_720_1280;
    public static RCRTCParamsType.RCRTCVideoFps fps = RCRTCParamsType.RCRTCVideoFps.Fps_24;
    public static int mixRate = 250, maxRate = 2200;
    public static boolean enableSpeaker = false;

}
