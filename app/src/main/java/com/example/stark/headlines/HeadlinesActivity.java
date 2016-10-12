package com.example.stark.headlines;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;

import com.example.stark.headlines.data.DataContract;
import com.example.stark.headlines.sync.SyncAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HeadlinesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = HeadlinesActivity.class.getSimpleName();

    public static final String[] HEADLINES_COLUMNS = {
            DataContract.NewsFeedHeadlinesEntry._ID,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_SOURCE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_ARTICLE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_IMAGE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_PUBLISH_TIME,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_URL
    };


    public static final int COLUMN_ID = 0;
    public static final int COLUMN_SOURCE = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_IMAGE = 3;
    public static final int COLUMN_TIME = 4;
    public static final int COLUMN_URL = 5;

    private static final int HEADLINES_ADAPTER_ID = 0;

    private HeadlinesAdapter mHeadlinesAdapter;
    private Uri mUri;

    private CollapsingToolbarLayout mCollapsingToolbar;
    private Toolbar mToolbar;

    private RecyclerView mHeadlineView;
    private AdView mAdView;


    @Override
    protected void onResume() {
        super.onResume();
        if(!Utility.isOnline(this)){
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.headline_activity_view), R.string.not_online_message, Snackbar.LENGTH_INDEFINITE);
            mySnackbar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_headlines_view);
        makeAd();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.explode);

            getWindow().setEnterTransition(transition);

        }


        if(!Utility.isOnline(this)){
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.headline_activity_view), R.string.not_online_message, Snackbar.LENGTH_INDEFINITE);
            mySnackbar.show();
        }

        if(getIntent().hasExtra(Intent.EXTRA_TEXT)){
            mUri = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
        }
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setTitle(getString(R.string.app_name));
        mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        mCollapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);

        getLoaderManager().initLoader(HEADLINES_ADAPTER_ID, null,this);
        mHeadlinesAdapter = new HeadlinesAdapter(this);

        mHeadlineView = (RecyclerView) findViewById(R.id.headlines_list);
        mHeadlineView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mHeadlineView.setAdapter(mHeadlinesAdapter);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                mUri,
                HEADLINES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor == null || cursor.getCount() == 0){
            SyncAdapter.syncImmediately(this);
        } else {
            mHeadlinesAdapter.swapCursor(cursor);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void makeAd(){
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(android_id)
                .build();
        mAdView.loadAd(adRequest);
    }
}
