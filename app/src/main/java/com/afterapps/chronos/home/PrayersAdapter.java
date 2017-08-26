package com.afterapps.chronos.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Prayer;

import java.util.List;

import butterknife.BindView;

/*
 * Created by mahmoud on 8/27/17.
 */

class PrayersAdapter extends RecyclerView.Adapter<PrayersAdapter.PrayerViewHolder> {

    private final Context mContext;
    private final List<Prayer> mPrayerList;

    PrayersAdapter(Context context, List<Prayer> prayerList) {
        mContext = context;
        mPrayerList = prayerList;
    }

    @Override
    public PrayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prayer, parent, false);
        return new PrayerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PrayerViewHolder holder, int position) {
        Prayer prayer = mPrayerList.get(position);
        if (prayer != null) {
            holder.setPrayer(prayer);
        }
    }

    @Override
    public int getItemCount() {
        return mPrayerList.size();
    }

    class PrayerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_prayer_logo_image_view)
        ImageView mItemPrayerLogoImageView;
        @BindView(R.id.item_prayer_title_text_view)
        TextView mItemPrayerTitleTextView;
        @BindView(R.id.item_prayer_timing_text_view)
        TextView mItemPrayerTimingTextView;

        PrayerViewHolder(View itemView) {
            super(itemView);
        }

        void setPrayer(Prayer prayer) {

        }
    }
}
