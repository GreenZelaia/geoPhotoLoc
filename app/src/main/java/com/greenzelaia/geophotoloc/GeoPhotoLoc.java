package com.greenzelaia.geophotoloc;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;


/**
 * Created by aki on 24/2/15.
 */
public class GeoPhotoLoc extends Application {

    Tracker mTracker;

    public GeoPhotoLoc() {
        super();
    }

    public synchronized Tracker getTracker() {
        if (mTracker == null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            mTracker = analytics.newTracker(R.xml.app_tracker);
        }
        return mTracker;
    }
}
