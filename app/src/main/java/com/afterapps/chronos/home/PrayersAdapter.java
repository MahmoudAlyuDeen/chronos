package com.afterapps.chronos.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Prayer;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by mahmoud on 8/27/17.
 */

class PrayersAdapter extends RecyclerView.Adapter<PrayersAdapter.PrayerViewHolder> {

    private final Context mContext;
    private final List<Prayer> mPrayerList;
    private final boolean mArabic;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

    PrayersAdapter(Context context, List<Prayer> prayerList, boolean arabic) {
        mContext = context;
        mPrayerList = prayerList;
        mArabic = arabic;
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
        @BindView(R.id.item_prayer_subtitle_text_view)
        TextView mItemPrayerSubtitleTextView;
        @BindView(R.id.item_prayer_timing_text_view)
        TextView mItemPrayerTimingTextView;

        PrayerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressWarnings({"unchecked"})
        void setPrayer(Prayer prayer) {
            HashMap<String, String[]> prayerNames = Constants.PRAYER_NAMES;
            String prayerTitle = prayerNames.get(prayer.getWhichPrayer())[mArabic ? 2 : 0];
            String prayerSubtitle = prayerNames.get(prayer.getWhichPrayer())[mArabic ? 3 : 1];
            mItemPrayerTitleTextView.setText(prayerTitle);
            mItemPrayerSubtitleTextView.setText(prayerSubtitle);
            mItemPrayerTimingTextView.setText(timeFormat.format(prayer.getTimestamp()));
        }
    }
}
