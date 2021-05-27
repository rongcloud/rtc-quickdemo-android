package cn.rongcloud.demo.live.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

class LiveBasePresenter {
    static protected Handler mMainHandler = new Handler(Looper.getMainLooper());
    protected Context mContext;

    public LiveBasePresenter(Context context) {
        mContext = context;
    }
}
