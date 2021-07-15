package cn.rongcloud.demo.cdn;

import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;

public interface IVideoConfigListener {

    void onCheckedChanged(RCRTCVideoResolution resolution);

}
