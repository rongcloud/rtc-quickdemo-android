package cn.rongcloud.quickdemo_callkit;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import io.rong.callkit.RongCallKit;
import io.rong.callkit.RongCallKit.CallMediaType;

/**
 * 音视频通话代码流程：
 * 1. 初始化IM，本 quickdemo-callkit/src/main/java/cn/rongcloud/quickdemo_callkit/CallKitApp.java 类中
 * 2. 登录IM，{@link CallkitBaseActivity#connectIM(String) }
 * 3. 登录成功后，调用Callkit中方法发起单/多人音视频通话；{@link MainActivity#callClick(View) }
 * 4. 接听通话UI及其逻辑在callkit模块中，开发者不需要关心
 */
public class MainActivity extends CallkitBaseActivity{

    private RadioButton rb_videoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout(R.layout.activity_main);

        rb_videoCall=findViewById(R.id.rb_videoCall);
        rb_videoCall.setChecked(true);
    }

    //TODO 本demo仅展示使用calkit模块发起音视频通话，不使用callkit模块请查看开发者文档：https://docs.rongcloud.cn/v4/views/rtc/call/noui/quick/android.html
    public void callClick(View view){
        String targetId=getTargetId();
        if(TextUtils.isEmpty(targetId)){
            showToast("目标会话 id获取错误，可能没有登录！");
            return;
        }
        CallMediaType mediaType = rb_videoCall.isChecked()?CallMediaType.CALL_MEDIA_TYPE_VIDEO:CallMediaType.CALL_MEDIA_TYPE_AUDIO;
        /**
         * 发起单人通话。
         *
         * @param context 上下文
         * @param targetId 目标会话 id ，单人通话为对方 UserId ,群组通话为 GroupId ，如果实现的是不基于群组的通话，那此参数无意义，传 null 即可
         * @param mediaType 会话媒体类型
         */
        RongCallKit.startSingleCall(MainActivity.this.getApplicationContext(),targetId,mediaType);
    }

    @Override
    public void IMConnectSuccess(String userId) {
    }

    @Override
    public void IMConnectError() {
    }
}