package cn.rongcloud.demo.cdn;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import androidx.annotation.Nullable;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;

/**
 * 分辨率切换页面
 *
 * TODO 实例代码中仅演示  360x640、720x1280、1080x1920 三种分辨率，更多分辨率切换请查看文档 : https://www.rongcloud.cn/docs/api/android/rtclib_v5/cn/rongcloud/rtc/base/RCRTCParamsType.RCRTCVideoResolution.html
 */
public class VideoResolutionFragment extends DialogFragment {

    private IVideoConfigListener mListener;

    RCRTCVideoResolution mResolution = null;

    public static VideoResolutionFragment newInstance(RCRTCVideoResolution videoResolution, IVideoConfigListener listener){
        VideoResolutionFragment dialog = new VideoResolutionFragment();
        dialog.setListener(listener);
        Bundle args = new Bundle();
        args.putString("label", videoResolution.getLabel());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null)
            return;
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT,WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.layout_video_resolution, container, false);
        String label = getArguments().getString("label");
        RadioButton rb_360_640 = mRootView.findViewById(R.id.rb_360_640);
        RadioButton rb_720_1280 = mRootView.findViewById(R.id.rb_720_1280);
        RadioButton rb_1080_1920 = mRootView.findViewById(R.id.rb_1080_1920);
        if (label.equals(RCRTCVideoResolution.RESOLUTION_360_640.getLabel())) {
            rb_360_640.setChecked(true);
            mResolution = RCRTCVideoResolution.RESOLUTION_360_640;
        } else if (label.equals(RCRTCVideoResolution.RESOLUTION_720_1280.getLabel())) {
            rb_720_1280.setChecked(true);
            mResolution = RCRTCVideoResolution.RESOLUTION_720_1280;
        } else if (label.equals(RCRTCVideoResolution.RESOLUTION_1080_1920.getLabel())) {
            rb_1080_1920.setChecked(true);
            mResolution = RCRTCVideoResolution.RESOLUTION_1080_1920;
        }
        ImageView iv_close = mRootView.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RadioGroup radioGroup = mRootView.findViewById(R.id.rg_videoResolution);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_360_640) {
                    mResolution = RCRTCVideoResolution.RESOLUTION_360_640;
                } else if (checkedId == R.id.rb_720_1280) {
                    mResolution = RCRTCVideoResolution.RESOLUTION_720_1280;
                } else if (checkedId == R.id.rb_1080_1920) {
                    mResolution = RCRTCVideoResolution.RESOLUTION_1080_1920;
                }
            }
        });

        Button btn_ok = mRootView.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCheckedChanged(mResolution);
                }
                dismiss();
            }
        });
        return mRootView;
    }

    public void setListener(IVideoConfigListener mListener) {
        this.mListener = mListener;
    }
}
