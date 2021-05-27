/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.calldemo.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.calldemo.R;
import cn.rongcloud.calldemo.model.HomeListItemModel;

public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.HomeItemHolder> {

    private List<HomeListItemModel> data = new ArrayList<>();
    private Context mContext;

    public HomeListAdapter(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public HomeItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new HomeItemHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_home_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeItemHolder homeItemHolder, int position) {
        HomeListItemModel model = data.get(position);
        homeItemHolder.ivIcon.setImageResource(model.getIcon());
        homeItemHolder.tvTitle.setText(model.getTitle());
        homeItemHolder.tvDescription.setText(model.getDescription());
        homeItemHolder.itemView.setOnClickListener(model.getOnClickListener());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void refreshData(List<HomeListItemModel> listItemModels) {
        data.clear();
        data.addAll(listItemModels);
        notifyDataSetChanged();
    }

    public static class HomeItemHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivIcon;
        AppCompatTextView tvTitle;
        AppCompatTextView tvDescription;

        public HomeItemHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}
