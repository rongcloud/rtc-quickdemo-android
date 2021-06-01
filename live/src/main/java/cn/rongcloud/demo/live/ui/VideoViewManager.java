package cn.rongcloud.demo.live.ui;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.rongcloud.rtc.api.stream.RCRTCVideoView;

import static cn.rongcloud.rtc.core.RendererCommon.ScalingType.SCALE_ASPECT_FILL;

public class VideoViewManager {

    private static final String TAG = "VideoViewManager";
    ArrayList<RCRTCVideoView> arrayListVideoView;
    int mWidth, mHeight;
    private FrameLayout flSurfaceContainer;
    private Map<String, RCRTCVideoView> linkedHashMap = new LinkedHashMap<>();

    public VideoViewManager(FrameLayout surfaceContainer, int width, int height) {
        flSurfaceContainer = surfaceContainer;
        mWidth = width;
        mHeight = height;
    }

    /**
     * 2 列多行显示 videoview
     *
     * @param list
     */
    public void update(ArrayList<RCRTCVideoView> list) {
        arrayListVideoView = list;

        int row = 0;
        int column = 0;

        column = list.size() > 1 ? 2 : 1;
        row = (list.size() + 1) / 2;

        flSurfaceContainer.removeAllViews();

        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) flSurfaceContainer.getLayoutParams();

        int width = mWidth;
        int height = mHeight;
        int index = 0;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width / column, height / row);
                int marginLeft = j * (width / column);
                int marginRight = mWidth - (marginLeft + width / column);
                int marginTop = i * (height / row);
                int marginBottom = mHeight - (marginTop + height / row);
                layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
                if (index >= arrayListVideoView.size()) {
                    break;
                }
                arrayListVideoView.get(index).setScalingType(SCALE_ASPECT_FILL);
                flSurfaceContainer.addView(arrayListVideoView.get(index), layoutParams);
                index++;
            }
        }
    }

    public void release() {
        if (null != arrayListVideoView && 0 != arrayListVideoView.size()) {
            for (RCRTCVideoView v : arrayListVideoView) {
                v.release();

            }
        }
    }
}
