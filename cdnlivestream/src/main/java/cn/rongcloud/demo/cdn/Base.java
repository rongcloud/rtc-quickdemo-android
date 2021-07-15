package cn.rongcloud.demo.cdn;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import cn.rongcloud.demo.common.UiUtils;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.utils.UUID22;

public class Base extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initRTC(this);
    }

    public void showToast(final String msg){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Base.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isFinish(){
        return isFinishing() || isDestroyed();
    }

    protected void postUIThread(final Runnable run) {
        if (isFinish())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinish())
                    return;
                run.run();
            }
        });
    }

    public void showLoading(){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                if (isFinish()){
                    return;
                }
                UiUtils.showWaitingDialog(Base.this);
            }
        });
    }

    public void closeLoading(){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                if (isFinish())
                    return;
                UiUtils.hideWaitingDialog();
            }
        });
    }

    /**
     * 初始化及设置
     */
    public void initRTC(Context context) {
        // 加入 rtc 房间前初始化 RTC SDK
        RCRTCConfig.Builder configBuilder = RCRTCConfig.Builder.create();
        // 是否硬解码
        configBuilder.enableHardwareDecoder(true);
        // 是否硬编码
        configBuilder.enableHardwareEncoder(true);
        RCRTCEngine.getInstance().unInit();
        RCRTCEngine.getInstance().init(context, configBuilder.build());

        // 主播加入房间前设置发布资源的分辨率、帧率等信息。更多设置相关信息请参考开发者文档：
        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        // 设置分辨率
        videoConfigBuilder.setVideoResolution(RCRTCVideoResolution.RESOLUTION_1080_1920);
        // 设置帧率
        videoConfigBuilder.setVideoFps(RCRTCVideoFps.Fps_30);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder.build());
        // 听筒播放，为避免噪音可在开发时设置为 false
        RCRTCEngine.getInstance().enableSpeaker(true);
    }
}
