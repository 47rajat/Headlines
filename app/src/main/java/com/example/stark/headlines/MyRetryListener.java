package com.example.stark.headlines;

import android.content.Context;
import android.view.View;

import com.example.stark.headlines.sync.SyncAdapter;

/**
 * Created by stark on 11/10/16.
 */

public class MyRetryListener implements View.OnClickListener {
    private static final String LOG_TAG = MyRetryListener.class.getSimpleName();

    private final Context mContex;
    private final String mSource;
    private final String mCategory;

    public MyRetryListener(Context context, String source, String category){
        mContex = context;
        mSource = source;
        mCategory = category;
    }
    @Override
    public void onClick(View view) {
        new FetchOtherFeedData(mContex ,mSource, mCategory).execute();
        SyncAdapter.syncImmediately(mContex);

    }
}
