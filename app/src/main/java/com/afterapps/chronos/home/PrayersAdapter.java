package com.afterapps.chronos.home;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.Utilities;
import com.afterapps.chronos.beans.Prayer;
import com.mikepenz.iconics.IconicsDrawable;

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

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_DAY_SUB_HEADER = 1;

    private final Context mContext;
    private final List<Prayer> mPrayerList;
    private final boolean mArabic;
    private final SimpleDateFormat timeFormat;

    PrayersAdapter(Context context, List<Prayer> prayerList, boolean arabic) {
        mContext = context;
        mPrayerList = prayerList;
        mArabic = arabic;
        timeFormat = new SimpleDateFormat("hh:mm a", new Locale(mArabic ? "ar" : "en"));
    }

    @Override
    public PrayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(viewType == VIEW_TYPE_DAY_SUB_HEADER ?
                        R.layout.item_prayer_sub_header : R.layout.item_prayer, parent, false);
        return new PrayerViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 || isFirstInDay(mPrayerList.get(position)) ?
                VIEW_TYPE_DAY_SUB_HEADER : VIEW_TYPE_DEFAULT;
    }

    private boolean isFirstInDay(Prayer prayer) {
        return prayer.getWhichPrayer().equals("fajr");
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
        @Nullable
        @BindView(R.id.item_prayer_day_text_view)
        TextView mItemPrayerDayTextView;

        PrayerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressWarnings({"unchecked"})
        void setPrayer(Prayer prayer) {
            final HashMap<String, String[]> prayerNames = Constants.PRAYER_NAMES;
            final String prayerTitle = prayerNames.get(prayer.getWhichPrayer())[mArabic ? 2 : 0];
            final String prayerSubtitle = prayerNames.get(prayer.getWhichPrayer())[mArabic ? 3 : 1];
            final String daySubHeaderString = getDayString(prayer);
            final IconicsDrawable prayerIcon = Utilities.getPrayerIcon(prayer, mContext);
            final int textColor = getTextColor();
            final int textStyle = getTextStyle();
            mItemPrayerTitleTextView.setText(prayerTitle);
            mItemPrayerSubtitleTextView.setText(prayerSubtitle);
            mItemPrayerTimingTextView.setText(timeFormat.format(prayer.getTimestamp()));
            mItemPrayerLogoImageView.setImageDrawable(prayerIcon);
            mItemPrayerTitleTextView.setTypeface(null, textStyle);
            mItemPrayerSubtitleTextView.setTypeface(null, textStyle);
            mItemPrayerTimingTextView.setTypeface(null, textStyle);
            mItemPrayerTitleTextView.setTextColor(textColor);
            mItemPrayerSubtitleTextView.setTextColor(textColor);
            mItemPrayerTimingTextView.setTextColor(textColor);
            if (mItemPrayerDayTextView != null) {
                mItemPrayerDayTextView.setText(daySubHeaderString);
            }
        }

        private String getDayString(Prayer prayer) {
            return mContext.getString(DateUtils.isToday(prayer.getTimestamp()) ?
                    R.string.sub_header_today : R.string.sub_header_tomorrow);
        }

        private int getTextColor() {
            return ContextCompat.getColor(mContext,
                    getAdapterPosition() == 0 ? R.color.colorAccent : R.color.colorTextPrimary);
        }

        private int getTextStyle() {
            return getAdapterPosition() == 0 ?
                    Typeface.BOLD : Typeface.NORMAL;
        }
    }
}
