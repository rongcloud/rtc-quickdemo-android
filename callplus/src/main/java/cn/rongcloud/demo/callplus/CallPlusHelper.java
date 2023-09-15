package cn.rongcloud.demo.callplus;


import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.callplus.api.RCCallPlusClient;
import cn.rongcloud.callplus.api.RCCallPlusConfig;
import cn.rongcloud.callplus.api.RCCallPlusConfig.Builder;
import cn.rongcloud.callplus.api.RCCallPlusMediaType;
import cn.rongcloud.callplus.api.RCCallPlusType;
import cn.rongcloud.callplus.api.callback.IRCCallPlusEventListener;
import cn.rongcloud.callplus.api.callback.IRCCallPlusResultListener;

/**
 * Created by RongCloud on 2023/6/14.
 */
public enum CallPlusHelper {

    INSTANCE;

    /**
     * 初始化 CallPlusHelper
     */
    public synchronized void init(IRCCallPlusResultListener listener, IRCCallPlusEventListener eventListener) {
        //销毁之前的 RCCallPlusClient
        destroy();
        //构建RCCallPlusConfig对象
        RCCallPlusConfig config = Builder.create().build();
        //初始化RCCallPlusClient
        RCCallPlusClient.getInstance().init(config);
        RCCallPlusClient.getInstance().setCallPlusResultListener(listener);
        RCCallPlusClient.getInstance().setCallPlusEventListener(eventListener);
    }


    /**
     * 拨打音视频通话
     */
    public void startCall(String userId) {
        List<String> userDataList = new ArrayList<>();
        userDataList.add(userId);
        RCCallPlusType callPlusType = RCCallPlusType.PRIVATE;
        RCCallPlusMediaType mediaType = RCCallPlusMediaType.VIDEO;
        RCCallPlusClient.getInstance().startCall(userDataList, callPlusType, mediaType);
    }

    /**
     * 销毁 CallPlusHelper
     */
    private synchronized void destroy() {
        RCCallPlusClient.getInstance().setCallPlusResultListener(null);
        RCCallPlusClient.getInstance().setCallPlusEventListener(null);
    }


}
