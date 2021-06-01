/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.calldemo;

import android.app.Application;

import io.rong.imlib.RongIMClient;

public class DemoApplication extends Application {

    /**
     * TODO: 请替换成您自己申请的 AppKey
     */
    public static final String APP_KEY = "n19jmcy59ocx9";

    /**
     * TODO: 请替换成您自己 AppKey 对应的 Secret
     * 这里仅用于模拟从 App Server 获取 UserID 对应的 Token, 开发者在上线应用时客户端代码不要存储该 Secret，
     * 否则有被用户反编译获取的风险，拥有 Secret 可以向融云 Server 请求高级权限操作，对应用安全造成恶劣影响。
     */
    public static final String APP_SECRET = "hccSLZPPmu";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 SDK，在整个应用程序全局只需要调用一次, 建议在 Application 继承类中调用。
        RongIMClient.init(this, APP_KEY, false);
    }
}
