package com.afterapps.chronos.location;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afterapps.chronos.R;
import com.afterapps.chronos.beans.Location;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

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

        @BindView(R.id.item_location_title_text_view)
        TextView mItemLocationTitleTextView;
        @BindView(R.id.item_location_active_radio_button)
        RadioButton mItemLocationActiveRadioButton;

        LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void displayItem(Location location) {
            mItemLocationTitleTextView.setText(location.getDisplayName());
            mItemLocationActiveRadioButton.setChecked(location.isSelected());
        }

        @Override
        public void onClick(View v) {
            Location location = getItem(getLayoutPosition());
            if (location != null && location.isValid()) {
                EventBus.getDefault().post(new LocationClickEvent(location.getTimezoneId()));
            }
        }
    }

    class LocationClickEvent {

        private final String timezoneId;

        String getTimezoneId() {
            return timezoneId;
        }

        LocationClickEvent(String timezoneId) {
            this.timezoneId = timezoneId;
        }
    }
}
