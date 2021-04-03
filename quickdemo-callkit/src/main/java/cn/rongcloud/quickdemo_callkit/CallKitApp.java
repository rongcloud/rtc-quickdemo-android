package cn.rongcloud.quickdemo_callkit;

import androidx.multidex.MultiDexApplication;
import cn.rongcloud.common.tools.Utils;
import io.rong.imkit.RongIM;

public class CallKitApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this.getApplicationContext());
        RongIM.init(this, Utils.appKey, false);
    }
}