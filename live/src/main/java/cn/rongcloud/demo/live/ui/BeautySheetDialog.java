package cn.rongcloud.demo.live.ui;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.beauty.RCRTCBeautyEngine;
import cn.rongcloud.beauty.RCRTCBeautyFilter;
import cn.rongcloud.beauty.RCRTCBeautyOption;
import cn.rongcloud.demo.live.R;

import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static android.widget.LinearLayout.HORIZONTAL;

public class BeautySheetDialog extends BottomSheetDialog implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "BeautySheetDialog";
    private RadioGroup rgBeauty;
    private RadioButton ruddyTab;
    private RadioButton smoothTab;
    private RadioButton brightTab;
    private RadioButton whitenessTab;
    private RadioButton filterTab;
    private FrameLayout contentView;
    private int filterIconWidth;
    private int filterIconHeight;
    private List<FilterItemBean> itemBeans = new ArrayList<>();


    public BeautySheetDialog(@NonNull Context context) {
        super(context, R.style.style_beauty_sheet_dialog);
        initView(context);
    }

    public BeautySheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        initView(context);
    }

    protected BeautySheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    private void initView(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.beauty_sheet_layout, null);
        setContentView(view);
        rgBeauty = view.findViewById(R.id.rg_beauty);

        float size =  context.getResources().getDimension(R.dimen.beauty_drawable_size2);
        smoothTab = view.findViewById(R.id.beauty_smooth);
        Drawable smoothDrawable = context.getDrawable(R.drawable.beauty_sheet_smooth_bg);
        smoothDrawable.setBounds(0,0, (int)size, (int)size);
        smoothTab.setCompoundDrawables(null, smoothDrawable, null, null);
        whitenessTab = view.findViewById(R.id.beauty_whiteness);
        Drawable whitenessDrawable = context.getDrawable(R.drawable.beauty_sheet_whiteness_bg);
        whitenessDrawable.setBounds(0,0, (int)size, (int)size);
        whitenessTab.setCompoundDrawables(null, whitenessDrawable, null, null);
        ruddyTab = view.findViewById(R.id.beauty_ruddy);
        Drawable ruddyDrawable = context.getDrawable(R.drawable.beauty_sheet_ruddy_bg);
        ruddyDrawable.setBounds(0,0, (int)size, (int)size);
        ruddyTab.setCompoundDrawables(null, ruddyDrawable, null, null);
        brightTab = view.findViewById(R.id.beauty_bright);
        Drawable brightDrawable = context.getDrawable(R.drawable.beauty_sheet_bright_bg);
        brightDrawable.setBounds(0,0, (int)size, (int)size);
        brightTab.setCompoundDrawables(null, brightDrawable, null, null);
        filterTab = view.findViewById(R.id.beauty_filter);
        float size1 =  context.getResources().getDimension(R.dimen.beauty_drawable_size1);
        Drawable filterDrawable = context.getDrawable(R.drawable.beauty_sheet_filter_bg);
        filterDrawable.setBounds(0,0, (int)size1, (int)size1);
        filterTab.setCompoundDrawables(null, filterDrawable, null, null);
        contentView = view.findViewById(R.id.fl_beauty_content);

        filterIconWidth = (int)context.getResources().getDimension(R.dimen.beauty_filter_ic_width);
        filterIconHeight =
                (int)context.getResources().getDimension(R.dimen.beauty_filter_ic_height);

        rgBeauty.setOnCheckedChangeListener(this);
        setCanceledOnTouchOutside(true);
        hideStatusBar();
        initFilterContent();
        rgBeauty.check(R.id.beauty_filter);
    }

    private void hideStatusBar(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        getWindow().getDecorView().setSystemUiVisibility(flag |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.d(TAG, "onCheckedChanged: [group, checkedId]" + checkedId);
        int beautyTypeValue = 0;
        if (checkedId == R.id.beauty_filter) {
            updateBeautyFilterContent();
        } else if (checkedId == R.id.beauty_whiteness) {
            beautyTypeValue =
                    RCRTCBeautyEngine.getInstance().getCurrentBeautyOption().getWhitenessLevel();
            updateBeautyParamContent(checkedId, beautyTypeValue);
        } else if (checkedId == R.id.beauty_smooth) {
            beautyTypeValue =
                    RCRTCBeautyEngine.getInstance().getCurrentBeautyOption().getSmoothLevel();
            updateBeautyParamContent(checkedId, beautyTypeValue);
        } else if (checkedId == R.id.beauty_bright) {
            beautyTypeValue =
                    RCRTCBeautyEngine.getInstance().getCurrentBeautyOption().getBrightLevel();
            updateBeautyParamContent(checkedId, beautyTypeValue);
            beautyTypeValue =
                    RCRTCBeautyEngine.getInstance().getCurrentBeautyOption().getRuddyLevel();
            updateBeautyParamContent(checkedId, beautyTypeValue);
        } else if (checkedId == R.id.beauty_ruddy) {
            beautyTypeValue =
                    RCRTCBeautyEngine.getInstance().getCurrentBeautyOption().getRuddyLevel();
            updateBeautyParamContent(checkedId, beautyTypeValue);
        }
    }

    /**
     * 美颜滤镜
     */
    private void updateBeautyFilterContent(){
        contentView.removeAllViews();
        int padWidth = (int) getContext().getResources().getDimension(R.dimen.beauty_sheet_content_pad_width);
        int padHeight =
                (int)getContext().getResources().getDimension(R.dimen.beauty_sheet_content_pad_height);
        contentView.setPadding(padWidth,padHeight, padWidth, padHeight);
        RadioGroup filterRadios = new RadioGroup(getContext());
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
        filterRadios.setOrientation(HORIZONTAL);
        filterRadios.setWeightSum(itemBeans.size()-1);
        contentView.addView(filterRadios, params);
        for (int i = 0; i < itemBeans.size(); i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(i);       // index 就是id, 后面的监听事件中通过这个区别
            radioButton.setButtonDrawable(null);
            Drawable drawable = getContext().getDrawable(itemBeans.get(i).getResId());
            drawable.setBounds(0, 0, filterIconWidth, filterIconHeight);
            radioButton.setCompoundDrawables(null,drawable
                   , null, null);
            radioButton.setText(itemBeans.get(i).getDes());
            RadioGroup.LayoutParams rbParams =
                    new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT);
            radioButton.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            filterRadios.addView(radioButton,rbParams);
            if (i != itemBeans.size()-1) {
                View padView = new View(getContext());
                RadioGroup.LayoutParams padParams =
                        new RadioGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                                3);
                padParams.weight = 1;
                filterRadios.addView(padView, padParams);
            }
        }
        filterRadios.setOnCheckedChangeListener(new FilterCheckedListener());
        filterRadios.check(filterType2Index(RCRTCBeautyEngine.getInstance().getCurrentFilter()));   //初始值
    }

    /**
     * 更新基础美颜参数设置UI
     *
     * @param paramsTypeId radioButton 选择的id
     * @param value
     */
    private void updateBeautyParamContent(int paramsTypeId, int value){
        contentView.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(HORIZONTAL);
        FrameLayout.LayoutParams llParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        llParams.gravity = Gravity.CENTER_VERTICAL;
        linearLayout.setWeightSum(1);
        contentView.addView(linearLayout);
        // seekBar
        SeekBar seekBar = new SeekBar(getContext());
        seekBar.setMax(9);
        seekBar.setProgress(value);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        (int)getContext().getResources().getDimension(R.dimen.beauty_sheet_seek_height));
        params.weight = 1;
        linearLayout.addView(seekBar, params);
        int padWidth = (int) getContext().getResources().getDimension(R.dimen.beauty_sheet_content_pad_width);
        int padHeight =
                (int)getContext().getResources().getDimension(R.dimen.beauty_sheet_content_pad_height);
        contentView.setPadding(padWidth,padHeight, padWidth, padHeight);
        // textView
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams tvLayoutParams =
                new LinearLayout.LayoutParams(
                        (int)getContext().getResources().getDimension(R.dimen.beauty_sheet_progress_size),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLayoutParams.gravity = Gravity.CENTER;
        linearLayout.addView(textView,1, tvLayoutParams);

        int beautyTypeValue = getBeautyValue4SeekerType(paramsTypeId);      // 更新ui值
        seekBar.setProgress(beautyTypeValue);
        textView.setText(String.valueOf(beautyTypeValue));
        seekBar.setOnSeekBarChangeListener(new BeautyProgressListener(paramsTypeId, textView));
    }

    private int filterType2Index(RCRTCBeautyFilter filter){
        switch (filter){
            case NONE:{
                return 0;
            }

            case ESTHETIC:{

                return 1;
            }
            case FRESH:{
                return 2;
            }

            case ROMANTIC:{
                return 3;
            }
            default:{
                Log.e(TAG, "filterType2Index: [filter]" + filter.name());
                return 0;
            }
        }
    }

    private class FilterCheckedListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case 0:{
                    RCRTCBeautyEngine.getInstance().setBeautyFilter(RCRTCBeautyFilter.NONE);
                    break;
                }
                case 1:{
                    RCRTCBeautyEngine.getInstance().setBeautyFilter(RCRTCBeautyFilter.ESTHETIC);
                    break;
                }
                case 2:{
                    RCRTCBeautyEngine.getInstance().setBeautyFilter(RCRTCBeautyFilter.FRESH);
                    break;
                }
                case 3:{
                    RCRTCBeautyEngine.getInstance().setBeautyFilter(RCRTCBeautyFilter.ROMANTIC);
                    break;
                }
                default:{
                    Log.e(TAG, "onCheckedChanged: [group, checkedId]" + checkedId);
                    break;
                }

            }
        }
    }

    private int getBeautyValue4SeekerType(int seekTypId){
        RCRTCBeautyOption beautyOption =
                RCRTCBeautyEngine.getInstance().getCurrentBeautyOption();
        if (seekTypId == R.id.beauty_whiteness) {
            return beautyOption.getWhitenessLevel();
        } else if (seekTypId == R.id.beauty_smooth) {
            return beautyOption.getSmoothLevel();
        } else if (seekTypId == R.id.beauty_bright) {
            return beautyOption.getBrightLevel();
        } else if (seekTypId == R.id.beauty_ruddy) {
            return beautyOption.getRuddyLevel();
        }else {
            Log.w(TAG, "getBeautyValue4SeekerType: error type");
            return 0;
        }
    }

    /**
     * 初始话美颜滤镜数据
     */
    private void initFilterContent(){
        itemBeans.add(new FilterItemBean(R.drawable.beauty_sheet_origin_1,  "原图"));
        itemBeans.add(new FilterItemBean(R.drawable.beauty_sheet_esthetic,  "唯美"));
        itemBeans.add(new FilterItemBean(R.drawable.beauty_sheet_pure,  "清新"));
        itemBeans.add(new FilterItemBean(R.drawable.beauty_sheet_romantic,  "浪漫"));
    }

    private static class BeautyProgressListener implements SeekBar.OnSeekBarChangeListener {

        private int seekTypId;
        private TextView textView;

        public BeautyProgressListener(int seekTypId, TextView textView) {
            this.seekTypId = seekTypId;
            this.textView = textView;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged: [seekTypId,"+seekTypId+" progress, fromUser]" + progress);

            RCRTCBeautyOption beautyOption =
                    RCRTCBeautyEngine.getInstance().getCurrentBeautyOption();
            if (seekTypId == R.id.beauty_whiteness) {
                beautyOption.setWhitenessLevel(progress);
            } else if (seekTypId == R.id.beauty_smooth) {
                beautyOption.setSmoothLevel(progress);
            } else if (seekTypId == R.id.beauty_bright) {
                beautyOption.setBrightLevel(progress);
            } else if (seekTypId == R.id.beauty_ruddy) {
                beautyOption.setRuddyLevel(progress);
            }
            textView.setText(String.valueOf(progress));
            RCRTCBeautyEngine.getInstance().setBeautyOption(true, beautyOption);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    private static class FilterItemBean{
        private final int resId;
        private final String des;

        public FilterItemBean(int resId, String des) {
            this.resId = resId;
            this.des = des;
        }

        public int getResId() {
            return resId;
        }

        public String getDes() {
            return des;
        }
    }
}
