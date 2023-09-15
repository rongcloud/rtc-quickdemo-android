/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.HomeItemHolder> {

    public static class HomeItemHolder extends RecyclerView.ViewHolder {
        AppCompatImageView ivIcon;
        AppCompatImageView ivExIcon;
        AppCompatTextView tvTitle;
        AppCompatTextView tvDescription;

        public HomeItemHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            ivExIcon = itemView.findViewById(R.id.iv_ex_icon);
            tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
    }

    public static class HomeListItemModel {
        public int index;
        public String title;
        public String desc;
        @DrawableRes
        public int icon;
        @DrawableRes
        public int exIcon;
        public View.OnClickListener clickListener;

        public HomeListItemModel(int index, String title, String description, @DrawableRes int icon,@DrawableRes int exIcon, View.OnClickListener onClickListener) {
            this.index = index;
            this.title = title;
            desc = description;
            this.icon = icon;
            this.exIcon = exIcon;
            this.clickListener = onClickListener;
        }

        public HomeListItemModel(int index, String title, String description, @DrawableRes int icon, View.OnClickListener onClickListener) {
            this.index = index;
            this.title = title;
            desc = description;
            this.icon = icon;
            clickListener = onClickListener;
        }
    }

    private final List<HomeListItemModel> modelList;

    public HomeListAdapter(List<HomeListItemModel> modelList) {
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public HomeItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeItemHolder homeItemHolder, int position) {
        HomeListItemModel model = modelList.get(position);
        homeItemHolder.ivIcon.setImageResource(model.icon);
        homeItemHolder.tvTitle.setText(model.title);
        homeItemHolder.tvDescription.setText(model.desc);
        homeItemHolder.itemView.setOnClickListener(model.clickListener);
        if (model.exIcon != 0) {
            homeItemHolder.ivExIcon.setVisibility(View.VISIBLE);
            homeItemHolder.ivExIcon.setImageResource(model.exIcon);
        }else {
            homeItemHolder.ivExIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
