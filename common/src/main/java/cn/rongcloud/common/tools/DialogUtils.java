package cn.rongcloud.common.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;


/**
 * Created by wangw on 2019-08-23.
 */
public class DialogUtils {

    public static AlertDialog showDialog(Context context, String content){
        return showDialog(context, content, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }, null,null);
    }

    public static AlertDialog showDialog(Context context, String content, String positiveBtn, DialogInterface.OnClickListener positiveListener){
        return showDialog(context,content,positiveBtn,positiveListener,null,null);
    }


    public static AlertDialog showDialog(Context context, String content, final DialogInterface.OnClickListener positiveListener, final DialogInterface.OnClickListener negativeListener){
        return showDialog(context,content,"确定",positiveListener,"取消",negativeListener);
    }

    public static AlertDialog showDialog(Context context, String content, String positiveBtn,String negativeBtn,final DialogInterface.OnClickListener positiveListener, final DialogInterface.OnClickListener negativeListener){
        return showDialog(context,content,positiveBtn,positiveListener,negativeBtn,negativeListener);
    }

    public static AlertDialog showDialog(Context context, String content, String positiveBtn, final DialogInterface.OnClickListener positiveListener, final String negativeBtn, final DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder = builder.setMessage(content);
        builder.setCancelable(false);
        if (!TextUtils.isEmpty(positiveBtn)) {
            builder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (positiveListener != null){
                        positiveListener.onClick(dialog,which);
                    }else {
                        dialog.dismiss();
                    }
                }
            });
        }
        if (!TextUtils.isEmpty(negativeBtn)) {
            builder.setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (negativeBtn != null){
                        negativeListener.onClick(dialog,which);
                    }else {
                        dialog.dismiss();
                    }
                }
            });
        }
        return builder.show();
    }
}
