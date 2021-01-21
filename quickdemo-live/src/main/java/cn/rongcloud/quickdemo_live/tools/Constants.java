package cn.rongcloud.quickdemo_live.tools;

import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;

public class Constants {

    //直播中使用的分辨率 帧率 码率
    public static RCRTCVideoResolution resolution = RCRTCVideoResolution.RESOLUTION_480_640;
    public static RCRTCVideoFps fps =RCRTCVideoFps.Fps_15;
    public static int mixRate = 200,maxRate= 900;
    //加入的RTC房间、聊天室号码
    public static final String ROOM_ID = "111222333";


    public static final String LOGOUT = "退出";
    public static final String STR_USER_A = "用户A";
    public static final String STR_USER_B = "用户B";
    public static final String STR_USER_C = "用户C";
    public static final String STR_USER_D = "用户D";
    public static final String JOIN = "开始直播";
    public static final String LEAVE = "结束直播";
    public static final String SUB = "观看直播";
    public static final String UN_SUB = "取消观看";

    public static final String CAMERA_STATUS_CLOSE = "关闭摄像头";
    public static final String CAMERA_STATUS_OPEN = "打开摄像头";
    public static final String MIC_STATUS_CLOSE = "关闭麦克风";
    public static final String MIC_STATUS_OPEN = "打开麦克风";

    public static final String HOST_PAGE_TITLE = "直播 Demo - 主播展示";
    public static final String VIEWER_PAGE_TITLE = "直播 Demo - 观众展示";
    public static final String VIEWER_JOIN ="上麦";
    public static final String VIEWER_LEAVE ="下麦";

    public static final String SUSPENSION = "悬浮布局";//默认
    public static final String ADAPTIVE = "自适应布局";
    public static final String CUSTOM = "自定义布局";
}
