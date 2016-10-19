package com.rajat.stark.headlines;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.ImageView;

import com.rajat.stark.headlines.data.DataContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

public class OtherHeadlinesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = OtherHeadlinesActivity.class.getSimpleName();

    public static final String[] HEADLINES_COLUMNS = {
            DataContract.OtherArticleEntry._ID,
            DataContract.OtherArticleEntry.COLUMN_HEADLINE_SOURCE,
            DataContract.OtherArticleEntry.COLUMN_HEADLINE_ARTICLE,
            DataContract.OtherArticleEntry.COLUMN_HEADLINE_IMAGE,
            DataContract.OtherArticleEntry.COLUMN_HEADLINE_PUBLISH_TIME
    };

    private static final String[] NEWSFEED_COLUMNS = {
            DataContract.NewsFeedEntry.TABLE_NAME + "." + DataContract.NewsFeedEntry._ID,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_NAME,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LOGO_LARGE,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_DESC,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_HOMEPAGE
    };

    private final int COLUMN_ID = 0;
    private final int COLUMN_NAME = 1;
    private final int COLUMN_LOGO = 2;
    private final int COLUMN_DESC = 3;
    private final int COLUMN_URL = 4;


    public static final String  SOURCE_NEWS_FEED = "newsFeed";
    public static final String CATEGORY_TOP = "top";
    public static final String CATEGORY_LATEST = "latest";
    public static final String CATEGORY_POPULAR = "popular";

    private RecyclerView mOtherFeedArticleView;


    private FloatingActionButton mHomePageButton;
    private ImageView mSourceFeedLogo;
    CollapsingToolbarLayout mSourceFeedTitle;


    private final int OTHER_FEED_ARTICLE_LOADER_ID = 0;
    private OtherHeadlinesAdapter mOtherHeadlineAdapter;

    private final int SOURCE_FEED_LOADER_ID = 1;
    private Uri mSourceFeedUri;

    private String mSource;
    private AdView mAdView;


    @Override
    protected void onResume() {
        super.onResume();
        if(!Utility.isOnline(this)){
            Snackbar.make(findViewById(R.id.activity_news_feed_detail_screen), R.string.not_online_message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_string, new MyRetryListener(this,mSource,CATEGORY_TOP))
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_headlines_view);
        makeAd();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHomePageButton = (FloatingActionButton) findViewById(R.id.go_to_newsfeed);
        mSourceFeedLogo = (ImageView) findViewById(R.id.newsfeed_logo);
        mSourceFeedTitle = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        if(getIntent().hasExtra(SOURCE_NEWS_FEED)){
            mSourceFeedUri = getIntent().getParcelableExtra(SOURCE_NEWS_FEED);
        }

        getLoaderManager().initLoader(SOURCE_FEED_LOADER_ID,null,this);

        getLoaderManager().initLoader(OTHER_FEED_ARTICLE_LOADER_ID, null, this);
        mOtherHeadlineAdapter = new OtherHeadlinesAdapter(this);

        mOtherFeedArticleView = (RecyclerView) findViewById(R.id.newsfeed_article_list);
        mOtherFeedArticleView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mOtherFeedArticleView.setAdapter(mOtherHeadlineAdapter);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.implode);
            getWindow().setExitTransition(transition);

        }

        if(getIntent().hasExtra(Intent.EXTRA_TEXT)){
            mSource = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        }



    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case OTHER_FEED_ARTICLE_LOADER_ID:
                return new CursorLoader(this,
                        DataContract.OtherArticleEntry.CONTENT_URI,
                        HEADLINES_COLUMNS,
                        null,
                        null,
                        null);

            case SOURCE_FEED_LOADER_ID:
                return new CursorLoader(this,
                        mSourceFeedUri,
                        NEWSFEED_COLUMNS,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("unknown loader requested, loader id " + i);

        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {

        switch (loader.getId()){
            case OTHER_FEED_ARTICLE_LOADER_ID:
                mOtherHeadlineAdapter.swapCursor(cursor);
                break;

            case SOURCE_FEED_LOADER_ID:
                if(cursor != null && cursor.getCount() > 0){
                    cursor.moveToFirst();
                    Picasso.with(this)
                            .load(cursor.getString(COLUMN_LOGO))
                            .into(mSourceFeedLogo);

//                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                        mSourceFeedLogo.setTransitionName(cursor.getString(COLUMN_LOGO));
//                    }

                    mSourceFeedTitle.setTitle(cursor.getString(COLUMN_NAME));

                    mHomePageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(cursor.getString(COLUMN_URL)));

                            if(intent.resolveActivity(getPackageManager()) != null){
                                startActivity(intent);
                            }
                        }
                    });

                }
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
