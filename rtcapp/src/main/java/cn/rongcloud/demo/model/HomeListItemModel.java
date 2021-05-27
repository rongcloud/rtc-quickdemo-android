/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.model;

import android.view.View;

import androidx.annotation.DrawableRes;

public class HomeListItemModel implements Comparable<HomeListItemModel> {

    private String mTitle;
    private String mDescription;
    @DrawableRes
    private int mIcon;
    private View.OnClickListener mOnClickListener;
    private int mIndex;

    public HomeListItemModel(int index, String title, String description, @DrawableRes int icon, View.OnClickListener onClickListener) {
        mIndex = index;
        mTitle = title;
        mDescription = description;
        mIcon = icon;
        mOnClickListener = onClickListener;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public void setIcon(@DrawableRes int icon) {
        mIcon = icon;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public int compareTo(HomeListItemModel o) {
        return this.mIndex - o.getIndex();
    }
}
