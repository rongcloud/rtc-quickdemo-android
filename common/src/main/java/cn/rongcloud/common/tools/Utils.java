package cn.rongcloud.common.tools;

import android.content.Context;

public class Utils {
    private static Context mContext = null;

    //TODO 请替换成您自己申请的AppKey
    public static String appKey=;

    //TODO 请填写您生成的一个token
    public static final String USER_1 = ;

    //TODO 请填写您生成的不同于 token1 的一个token;
    public static final String USER_2 = ;

    public static void init(Context context) {
        Utils.mContext = context.getApplicationContext();
    }

    public static Context getContext() {
        if (null != mContext) {
            return mContext;
        }
        throw new NullPointerException("u should context init first");
    }
}
