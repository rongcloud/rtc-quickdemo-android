package cn.rongcloud.common;

import android.support.multidex.MultiDexApplication;
import cn.rongcloud.common.tools.Utils;
import io.rong.imlib.RongIMClient;

public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this.getApplicationContext());
        RongIMClient.init(this, Utils.appKey, false);
    }
}
