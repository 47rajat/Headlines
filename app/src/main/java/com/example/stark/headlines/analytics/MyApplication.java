package com.example.stark.headlines.analytics;

import android.app.Application;
import android.util.Log;

import com.example.stark.headlines.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by stark on 11/10/16.
 */

public class MyApplication extends Application {

    Tracker mTracker;

    //Get the tracker associated with the app
    public void startTracking(){

        if (mTracker == null) {
            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);

            // Get the config data for the tracker
            mTracker = ga.newTracker(R.xml.track_app);

            // Enable tracking of activities
            ga.enableAutoActivityReports(this);
        }
    }

    public Tracker getTracker() {
        // Make sure the tracker exists
        startTracking();

        // Then return the tracker
        return mTracker;
    }
}
