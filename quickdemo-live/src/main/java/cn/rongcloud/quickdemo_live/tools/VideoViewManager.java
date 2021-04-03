package cn.rongcloud.quickdemo_live.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import cn.rongcloud.quickdemo_live.R;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import java.util.LinkedHashMap;
import java.util.Map;

public class VideoViewManager extends LinearLayout {

    private static final String TAG = VideoViewManager.class.getName();
    private FrameLayout frameLayout_surfaceView;
    private Map<String,RCRTCVideoView> linkedHashMap = new LinkedHashMap<>();
    private LinearLayout mLinear_host;
    private Context mContext;
    int mScreenWidth =0;
    private static final int MARGIN =10;

    public VideoViewManager(Context context) {
        super(context);
        mContext=context;
        WindowManager wm = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        init(screenWidth);
    }

    public VideoViewManager(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(int screenWidth) {
        LayoutInflater.from(mContext).inflate(R.layout.layout_host, this, true);
        frameLayout_surfaceView = findViewById(R.id.frameLayout_surfaceView);
        mLinear_host = findViewById(R.id.linear_host);
        mScreenWidth = screenWidth -20;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenWidth);
        layoutParams.setMargins(10,0,10,5);
        mLinear_host.setLayoutParams(layoutParams);
    }

    public void addRTCVideoView(String userId, RCRTCVideoView videoView){
        Log.e(TAG,"addRTCVideoView.userId : "+userId);
        linkedHashMap.put(userId,videoView);
        addVideoView();
    }

    public void removeRTCVideoView(String userId){
        linkedHashMap.remove(userId);
        Log.e(TAG,"removeRTCVideoView.userId : "+userId);
        addVideoView();
    }

    private void addVideoView() {
        LinearLayout.LayoutParams layoutParams_1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mScreenWidth);
        layoutParams_1.setMargins(10,0,10,5);
        mLinear_host.setLayoutParams(layoutParams_1);

        int size = linkedHashMap.size();
        Log.e(TAG,"addRTCVideoView.Size : "+size);
        LayoutParams layoutParams = null;
        int width = mScreenWidth /2-20;
        int index =0;
        if(frameLayout_surfaceView.getChildCount()>0){
            frameLayout_surfaceView.removeAllViews();
        }
        for(Map.Entry<String, RCRTCVideoView> entry : linkedHashMap.entrySet()){
            layoutParams = new LayoutParams(width, width);
            if(size==3||size==4){
                if(index==0){
                    layoutParams.setMargins(MARGIN,MARGIN,MARGIN,MARGIN);
                }else if(index==1){
                    layoutParams.setMargins(width+(MARGIN*3),MARGIN,MARGIN,MARGIN);
                }else if(index==2){
                    layoutParams.setMargins(MARGIN,width+(MARGIN*3),MARGIN,MARGIN);
                }else if(index==3){
                    layoutParams.setMargins(width+(MARGIN*3),width+(MARGIN*3),MARGIN,MARGIN);
                }
            }else if(size==1){
                layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }else if(size==2){
                if(index==0){
                    layoutParams.setMargins(MARGIN,mScreenWidth/4,MARGIN,0);
                }else if(index==1){
                    layoutParams.setMargins(width+(MARGIN*3),mScreenWidth/4,MARGIN,0);
                }
            }
            frameLayout_surfaceView.addView(entry.getValue(),layoutParams);
            index++;
        }
    }

    public void release(){
        clear();
        mContext = null;
    }

    public void clear(){
        frameLayout_surfaceView.removeAllViews();
        linkedHashMap.clear();
    }
}
