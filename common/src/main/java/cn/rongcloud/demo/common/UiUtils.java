/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.common;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;

public class UiUtils {

    /// 两次点击按钮之间的点击间隔不能少于 1000 毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;

    private static long lastClickTime;
    private static AlertDialog waitingDialog;

    public static boolean isFastClick() {
        return isFastClick(-1);
    }

    public static boolean isFastClick(long during) {
        boolean flag = false;
        long curClickTime = SystemClock.elapsedRealtime();
        long tempDuring = during < 0 ? MIN_CLICK_DELAY_TIME : during;
        if ((curClickTime - lastClickTime) >= tempDuring) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    public static void showWaitingDialog(Context context) {
        if (waitingDialog == null) {
            waitingDialog = new AlertDialog.Builder(context, R.style.TransparentDialog).create();
            waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
            waitingDialog.setCancelable(false);
            waitingDialog.setCanceledOnTouchOutside(false);
        }
        waitingDialog.show();
        waitingDialog.setContentView(R.layout.layout_loading);
    }

    public static void hideWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
    }
}
