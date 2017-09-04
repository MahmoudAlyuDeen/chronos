package com.afterapps.chronos.home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afterapps.chronos.R;

/*
 * Created by mahmoudalyudeen on 5/9/17.
 */

class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder> {

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_DAY_SUB_HEADER = 1;


    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_DAY_SUB_HEADER : VIEW_TYPE_DEFAULT;
    }

    @Override
    public ProgressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(viewType == VIEW_TYPE_DAY_SUB_HEADER ?
                        R.layout.item_prayer_progress_sub_header : R.layout.item_prayer_progress, parent, false);
        return new ProgressViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProgressViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View itemView) {
            super(itemView);
        }
    }
}
