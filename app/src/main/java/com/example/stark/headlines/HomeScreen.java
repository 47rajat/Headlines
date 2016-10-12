package com.example.stark.headlines;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Button;

import com.example.stark.headlines.analytics.MyApplication;
import com.example.stark.headlines.data.DataContract;
import com.example.stark.headlines.sync.SyncAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HomeScreen extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = HomeScreen.class.getSimpleName();

    private static final String[] NEWSFEED_COLUMNS = {
            DataContract.NewsFeedEntry.TABLE_NAME + "." + DataContract.NewsFeedEntry._ID,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_NAME,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LOGO_LARGE,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_IS_FAVOURITE,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_ID,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_TOP,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LATEST,
            DataContract.NewsFeedEntry.COLUMN_NEWSFEED_POPULAR
    };

    private static final String[] HEADLINES_COLUMNS = {
            DataContract.NewsFeedHeadlinesEntry._ID,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_SOURCE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_ARTICLE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_IMAGE,
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_LOGO = 2;
    public static final int COLUMN_CATEGORY = 3;
    public static final int COLUMN_IS_FAVOURITE = 4;
    public static final int COLUMN_NEWSFEED_ID = 5;
    public static final int COLUMN_HAS_TOP = 6;
    public static final int COLUMN_HAS_LATEST = 7;
    public static final int COLUMN_HAS_POPULAR = 8;

    private NewsFeedAdapter mAllFeedAdapter;
    private NewsFeedAdapter mFavouriteAdapter;
    private NewsFeedAdapter mGeneralFeedAdapter;
    private NewsFeedAdapter mBusinessFeedAdapter;
    private NewsFeedAdapter mEntertainmentFeedAdapter;
    private NewsFeedAdapter mSportsFeedAdapter;
    private NewsFeedAdapter mTechnologyFeedAdapter;
    private NewsFeedAdapter mScienceAndTechnologyFeedAdapter;
    private NewsFeedAdapter mMusicFeedAdapter;
    private NewsFeedAdapter mGamingFeedAdapter;
    private HeadlineSliderAdapter mHeadlineSliderAdapter;

    private static final int FAVOURITE_NEWSFEED_ADAPTER_ID = 0;
    private static final int ALL_NEWSFEED_ADAPTER_ID = 1;
    private static final int GENERAL_NEWSFEED_ADAPTER_ID = 2;
    private static final int BUSINESS_NEWSFEED_ADAPTER_ID = 3;
    private static final int ENTERTAINMENT_NEWSFEED_ADAPTER_ID = 4;
    private static final int SPORTS_NEWSFEED_ADAPTER_ID = 5;
    private static final int TECHNOLOGY_NEWSFEED_ADAPTER_ID = 6;
    private static final int SCINECE_AND_NATURE_NEWSFEED_ADAPTER_ID = 7;
    private static final int MUSIC_NEWSFEED_ADAPTER_ID = 8;
    private static final int GAMING_NEWSFEED_ADAPTER_ID = 9;
    private static final int HEADLINES_SLIDER_ADAPTER_ID = 20;



    private RecyclerView mAllFeedsView;
    private RecyclerView mGeneralFeedsView;
    private RecyclerView mBusinessFeedsView;
    private RecyclerView mEntertainmentFeedsView;
    private RecyclerView mSportsFeedsView;
    private RecyclerView mTechnologyFeedsView;
    private RecyclerView mScienceAndNatureFeedsView;
    private RecyclerView mMusicFeedsView;
    private RecyclerView mGamingFeedsView;
    private RecyclerView mFavouriteFeedsView;
    private RecyclerView mHeadlinesSlider;


    private static final int mMaxSliderAtricleLimit = 10;

    private int mPositionAllFeeds;
    private final String LAST_POSITION = "Position";


    public static final String FEED_CATEGORY_ALL = "all";
    public static final String FEED_CATEGORY_GENERAL = "general";
    public static final String FEED_CATEGORY_BUSINESS = "business";
    public static final String FEED_CATEGORY_ENTERTAINMENT = "entertainment";
    public static final String FEED_CATEGORY_SPORTS = "sport";
    public static final String FEED_CATEGORY_TECHNOLOGY = "technology";
    public static final String FEED_CATEGORY_SCIENCE_AND_NATURE = "science-and-nature";
    public static final String FEED_CATEGORY_MUSIC = "music";
    public static final String FEED_CATEGORY_GAMING = "gaming";
    public static final String FEED_CATEGORY_FAVOURITES = "favourites";


    private Button mYourHeadlinesButton;
    private Intent mHeadlinesActivityIntent;

    private AdView mAdView;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        if(mPositionAllFeeds != RecyclerView.NO_POSITION){
//            outState.putInt(LAST_POSITION, mPositionAllFeeds);
//        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Utility.isOnline(this)){
            Snackbar.make(findViewById(R.id.home_sceen_activity), R.string.not_online_message, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SyncAdapter.initializeSyncAdapter(this);
        setContentView(R.layout.activity_home_screen);
        ((MyApplication)getApplication()).startTracking();
        makeAd();


        mHeadlinesActivityIntent = new Intent(this, HeadlinesActivity.class);
        mHeadlinesActivityIntent.putExtra(Intent.EXTRA_TEXT, DataContract.NewsFeedHeadlinesEntry.CONTENT_URI);

        mYourHeadlinesButton = (Button) findViewById(R.id.headlines_button);
        mYourHeadlinesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    Transition transition = TransitionInflater.from(HomeScreen.this).inflateTransition(R.transition.explode);
                    getWindow().setExitTransition(transition);
                    Bundle bundle = ActivityOptions
                            .makeSceneTransitionAnimation(HomeScreen.this)
                            .toBundle();


                    startActivity(mHeadlinesActivityIntent, bundle);
                } else {
                    startActivity(mHeadlinesActivityIntent);
                }


            }
        });


        getLoaderManager().initLoader(FAVOURITE_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(ALL_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(GENERAL_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(BUSINESS_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(ENTERTAINMENT_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(SPORTS_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(TECHNOLOGY_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(SCINECE_AND_NATURE_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(MUSIC_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(GAMING_NEWSFEED_ADAPTER_ID, null, this);
        getLoaderManager().initLoader(HEADLINES_SLIDER_ADAPTER_ID, null, this);



        mFavouriteAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_FAVOURITES);
        mAllFeedAdapter = new NewsFeedAdapter(this,FEED_CATEGORY_ALL);
        mGeneralFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_GENERAL);
        mBusinessFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_BUSINESS);
        mEntertainmentFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_ENTERTAINMENT);
        mSportsFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_SPORTS);
        mTechnologyFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_TECHNOLOGY);
        mScienceAndTechnologyFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_SCIENCE_AND_NATURE);
        mMusicFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_MUSIC);
        mGamingFeedAdapter = new NewsFeedAdapter(this, FEED_CATEGORY_GAMING);
        mHeadlineSliderAdapter = new HeadlineSliderAdapter(this);


        mAllFeedsView = (RecyclerView) findViewById(R.id.pager_all_feeds);
        mAllFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mAllFeedsView.setNestedScrollingEnabled(false);
        mAllFeedsView.setAdapter(mAllFeedAdapter);


        mFavouriteFeedsView = (RecyclerView) findViewById(R.id.pager_fav_feeds);
        mFavouriteFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mFavouriteFeedsView.setNestedScrollingEnabled(false);
        mFavouriteFeedsView.setAdapter(mFavouriteAdapter);

        mGeneralFeedsView = (RecyclerView) findViewById(R.id.pager_general_feeds);
        mGeneralFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mGeneralFeedsView.setNestedScrollingEnabled(false);
        mGeneralFeedsView.setAdapter(mGeneralFeedAdapter);

        mBusinessFeedsView = (RecyclerView) findViewById(R.id.pager_business_feeds);
        mBusinessFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mBusinessFeedsView.setNestedScrollingEnabled(false);
        mBusinessFeedsView.setAdapter(mBusinessFeedAdapter);

        mEntertainmentFeedsView = (RecyclerView) findViewById(R.id.pager_entertainment_feeds);
        mEntertainmentFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mEntertainmentFeedsView.setNestedScrollingEnabled(false);
        mEntertainmentFeedsView.setAdapter(mEntertainmentFeedAdapter);

        mSportsFeedsView = (RecyclerView) findViewById(R.id.pager_sport_feeds);
        mSportsFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mSportsFeedsView.setNestedScrollingEnabled(false);
        mSportsFeedsView.setAdapter(mSportsFeedAdapter);

        mTechnologyFeedsView = (RecyclerView) findViewById(R.id.pager_technology_feeds);
        mTechnologyFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mTechnologyFeedsView.setNestedScrollingEnabled(false);
        mTechnologyFeedsView.setAdapter(mTechnologyFeedAdapter);

        mScienceAndNatureFeedsView = (RecyclerView) findViewById(R.id.pager_science_and_nature_feeds);
        mScienceAndNatureFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mScienceAndNatureFeedsView.setNestedScrollingEnabled(false);
        mScienceAndNatureFeedsView.setAdapter(mScienceAndTechnologyFeedAdapter);

        mMusicFeedsView = (RecyclerView) findViewById(R.id.pager_music_feeds);
        mMusicFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mMusicFeedsView.setNestedScrollingEnabled(false);
        mMusicFeedsView.setAdapter(mMusicFeedAdapter);

        mGamingFeedsView = (RecyclerView) findViewById(R.id.pager_gaming_feeds);
        mGamingFeedsView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mGamingFeedsView.setNestedScrollingEnabled(false);
        mGamingFeedsView.setAdapter(mGeneralFeedAdapter);

        mHeadlinesSlider = (RecyclerView) findViewById(R.id.slider_headlines);
        mHeadlinesSlider.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mHeadlinesSlider.setAdapter(mHeadlineSliderAdapter);




    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case FAVOURITE_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_IS_FAVOURITE + " = ?",
                        new String[] {"1"},
                        null);

            case ALL_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        null,
                        null,
                        null);

            case GENERAL_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_GENERAL},
                        null);

            case BUSINESS_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                    DataContract.NewsFeedEntry.CONTENT_URI,
                    NEWSFEED_COLUMNS,
                    DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                    new String[]{FEED_CATEGORY_BUSINESS},
                    null);

            case ENTERTAINMENT_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_ENTERTAINMENT},
                        null);

            case SPORTS_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_SPORTS},
                        null);

            case TECHNOLOGY_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_TECHNOLOGY},
                        null);

            case SCINECE_AND_NATURE_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_SCIENCE_AND_NATURE},
                        null);

            case MUSIC_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_MUSIC},
                        null);

            case GAMING_NEWSFEED_ADAPTER_ID:
                return new CursorLoader(this,
                        DataContract.NewsFeedEntry.CONTENT_URI,
                        NEWSFEED_COLUMNS,
                        DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " = ?",
                        new String[]{FEED_CATEGORY_GAMING},
                        null);

            case HEADLINES_SLIDER_ADAPTER_ID:
                return  new CursorLoader(this,
                        DataContract.NewsFeedHeadlinesEntry.buildHeadlinesWithLimit(mMaxSliderAtricleLimit),
                        HEADLINES_COLUMNS,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Unkown adapter initialization requested, request code: " + i);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()){
            case FAVOURITE_NEWSFEED_ADAPTER_ID:
                mFavouriteAdapter.swapCursor(cursor);
                break;

            case ALL_NEWSFEED_ADAPTER_ID:
                mAllFeedAdapter.swapCursor(cursor);
                break;

            case GENERAL_NEWSFEED_ADAPTER_ID:
                mGeneralFeedAdapter.swapCursor(cursor);
                break;

            case BUSINESS_NEWSFEED_ADAPTER_ID:
                mBusinessFeedAdapter.swapCursor(cursor);
                break;

            case ENTERTAINMENT_NEWSFEED_ADAPTER_ID:
                mEntertainmentFeedAdapter.swapCursor(cursor);
                break;

            case SPORTS_NEWSFEED_ADAPTER_ID:
                mSportsFeedAdapter.swapCursor(cursor);
                break;

            case TECHNOLOGY_NEWSFEED_ADAPTER_ID:
                mTechnologyFeedAdapter.swapCursor(cursor);
                break;

            case SCINECE_AND_NATURE_NEWSFEED_ADAPTER_ID:
                mScienceAndTechnologyFeedAdapter.swapCursor(cursor);
                break;

            case MUSIC_NEWSFEED_ADAPTER_ID:
                mMusicFeedAdapter.swapCursor(cursor);
                break;

            case GAMING_NEWSFEED_ADAPTER_ID:
                mGamingFeedAdapter.swapCursor(cursor);
                break;
            case HEADLINES_SLIDER_ADAPTER_ID:
                mHeadlineSliderAdapter.swapCursor(cursor);
                break;
            default:
                throw new RuntimeException("Unknown adapter updated, request code: " + loader.getId());
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
