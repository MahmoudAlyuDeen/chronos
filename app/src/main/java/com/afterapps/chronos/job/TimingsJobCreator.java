package com.afterapps.chronos.job;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/*
 * Created by mahmoud on 9/6/17.
 */

public class TimingsJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case PrayersJob.TAG:
                return new PrayersJob();
            default:
                return null;
        }
    }
}
