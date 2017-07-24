package com.afterapps.chronos.location;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Location;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

import static com.afterapps.chronos.Constants.STATIC_MAP_URL_FORMAT;

/*
 * Created by mahmoudalyudeen on 7/23/17.
 */

class LocationsAdapter extends RealmRecyclerViewAdapter<Location, LocationsAdapter.LocationViewHolder> {

    private final Context mContext;

    LocationsAdapter(@Nullable OrderedRealmCollection<Location> data, Context context) {
        super(data, true, true);
        mContext = context;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        Location location = getItem(position);
        if (location != null) {
            holder.displayItem(location);
        }
    }

    class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.item_location_image_view)
        ImageView mItemLocationImageView;
        @BindView(R.id.item_location_title_text_view)
        TextView mItemLocationTitleTextView;
        @BindView(R.id.item_location_card_view)
        CardView mItemLocationCardView;

        LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void displayItem(Location location) {
            Glide.with(mContext)
                    .load(getStaticMapUrl(location))
                    .centerCrop()
                    .dontAnimate()
                    .placeholder(R.drawable.placeholder_location)
                    .into(mItemLocationImageView);

            mItemLocationTitleTextView.setText(location.getDisplayName());

            mItemLocationTitleTextView.setAlpha(location.isSelected() ? 1f : 0.75f);
            mItemLocationCardView.setCardElevation(location.isSelected() ? dpToPx(8, mContext) : 0);
        }

        private String getStaticMapUrl(Location location) {
            return String.format(STATIC_MAP_URL_FORMAT, location.getLatitude(), location.getLongitude());
        }

        private int dpToPx(int dp, Context context) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }

        @Override
        public void onClick(View v) {

        }
    }
}
