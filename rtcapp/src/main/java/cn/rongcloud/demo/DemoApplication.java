/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo;

import android.app.Application;
import android.widget.Toast;

import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.InitOption;

public class DemoApplication extends Application {

    /**
     * TODO: 请替换成您自己申请的 AppKey
     */
    public static final String APP_KEY = "";

    /**
     * TODO: 请替换成您自己 AppKey 对应的 Secret
     * 这里仅用于模拟从 App Server 获取 UserID 对应的 Token, 开发者在上线应用时客户端代码不要存储该 Secret，
     * 否则有被用户反编译获取的风险，拥有 Secret 可以向融云 Server 请求高级权限操作，对应用安全造成恶劣影响。
     */
    public static final String APP_SECRET = "";

    @Override
    public void onCreate() {
        super.onCreate();
        // 关键步骤 1：初始化 SDK，在整个应用程序全局只需要调用一次, 建议在 Application 继承类中调用。
        try {
            InitOption.Builder builder = new InitOption.Builder();
            RongCoreClient.init(this, APP_KEY, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("Can't init")) {
                /**
                 *  您需要去融云开发者后台申请App Key 并将代码中 APP_KEY，APP_SECRET 替换成您申请的
                 *
                 *  官网地址：https://developer.rongcloud.cn/app/appService/zCgO-wgi6w6d1dh2Ng7qcQ#?_sasdk=fMTkwODI4
                 */
                Toast.makeText(this, "您需要先去融云开发者后台申请App Key", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
