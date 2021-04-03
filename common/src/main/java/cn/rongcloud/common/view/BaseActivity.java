package cn.rongcloud.common.view;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.Nullable;
import cn.rongcloud.common.R;
import cn.rongcloud.common.tools.Utils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectionErrorCode;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;

/**
 * 共用同一个IM登录布局使用该类
 */
public abstract class BaseActivity extends Base {

    private LinearLayout linearLayout;
    private Button btn_login1,btn_login2;
    private static final String TAG = BaseActivity.class.getName();
    private String localUserId="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_baseactivity);
        linearLayout=findViewById(R.id.line1);
        btn_login1=findViewById(R.id.btn_login1);
        btn_login2=findViewById(R.id.btn_login2);
    }

    private void connectIM(String token) {
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == ConnectionStatus.CONNECTED) {
            showToast("IM已经连接成功，无需再次连接");
            return;
        }
        localUserId="";
        /**
         * 连接融云服务器，在整个应用程序全局，只需要调用一次，需在 {@link #init(Context, String)} 之后调用。
         * </p>
         * 如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
         * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。
         *
         * @param token    从服务端获取的用户身份令牌（Token）
         * @param callback 连接回调
         */
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {

            @Override
            public void onSuccess(String s) {
                Log.d(TAG,"RongIMClient.connect.onSuccess :"+s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("用户 "+s +"登录成功!");
                        localUserId=s;
                        IMConnectSuccess(s);
                    }
                });
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                if(ConnectionErrorCode.RC_CONNECTION_EXIST == errorCode){
                    //TODO IM连接已经存在，无需重复连接。可以直接加入RTC房间
                    return;
                }
                showToast("IM 连接失败 ："+errorCode);
                localUserId="";
                Log.d(TAG,"RongIMClient.connect.onError :"+errorCode.getValue());
                IMConnectError();
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus databaseOpenStatus) {

            }
        });
    }

    /**
     * 子Activity通过该方法添加视图
     * @param resId
     */
    public void setLayout(int resId) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(resId, null);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        contentView.setLayoutParams(layoutParams);
        linearLayout.addView(contentView);
        btn_login1=findViewById(R.id.btn_login1);
        btn_login2=findViewById(R.id.btn_login2);
    }

    public  void loginClick(View view){
        int id = view.getId();
        if (id == R.id.btn_login1) {
            connectIM(Utils.USER_TOKEN_1);
            btn_login2.setVisibility(View.INVISIBLE);
        } else if (id == R.id.btn_login2) {
            connectIM(Utils.USER_TOKEN_2);
            btn_login1.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 融云IM连接成功
     * @param userId
     */
    public abstract void IMConnectSuccess(String userId);

    /**
     * 融云IM连接失败
     */
    public abstract void IMConnectError();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 断开与融云服务器的连接，但仍然接收远程推送。
         * <p>若想断开连接后不接受远程推送消息，可以调用{@link #logout()}。<br>
         * <strong>注意：</strong>因为 SDK 在前后台切换或者网络出现异常都会自动重连，保证连接可靠性。
         * 所以除非您的 App 逻辑需要登出，否则一般不需要调用此方法进行手动断开。<br>
         * </p>
         */
//        RongIMClient.getInstance().disconnect();
        /**
         * 断开与融云服务器的连接，并且不再接收远程推送消息。
         * <p>
         * 若想断开连接后仍然接受远程推送消息，可以调用 {@link #disconnect()}
         * </p>
         */
        RongIMClient.getInstance().logout();
    }

    /**
     * IM登录后，获取当前登录的用户ID
     * @return
     */
    public String getLocalUserId() {
        return localUserId;
    }

    public String getTargetId(){
        if(TextUtils.isEmpty(getLocalUserId())){
            showToast("请先登录");
            return "";
        }
        if(TextUtils.equals(getLocalUserId(),Utils.USER_ID_1)){
            return Utils.USER_ID_2;
        }else if(TextUtils.equals(getLocalUserId(),Utils.USER_ID_2)){
            return Utils.USER_ID_1;
        }
        return "";
    }
}
