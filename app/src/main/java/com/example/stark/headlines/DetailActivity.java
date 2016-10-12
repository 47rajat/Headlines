package com.example.stark.headlines;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_SOURCE = 1;
    public static final int COLUMN_AUTHOR = 2;
    public static final int COLUMN_DESC = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_URL = 5;
    public static final int COLUMN_IMAGE = 6;
    public static final int COLUMN_PUBLISH = 7;

    private Uri mContenUri;
    private Cursor mCursor;

    private TextView mArticleTitle;
    private TextView mArticleAuthor;
    private TextView mArticleDescription;
    private ImageView mArticleImage;
    private FloatingActionButton mArticleUrl;
    private ImageButton mArticleShare;

    private ImageButton mUpButton;
    private AdView mAdView;


    @Override
    protected void onResume() {
        super.onResume();
        if(!Utility.isOnline(this)){
            Snackbar.make(findViewById(R.id.detail_article_layout), R.string.not_online_message, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        makeAd();
        if(!Utility.isOnline(this)){
            Snackbar.make(findViewById(R.id.detail_article_layout), R.string.not_online_message, Snackbar.LENGTH_INDEFINITE).show();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.implode);
            getWindow().setExitTransition(transition);

        }
        mContenUri = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
        mArticleTitle = (TextView) findViewById(R.id.article_title);
        mArticleAuthor = (TextView) findViewById(R.id.article_publisher);
        mArticleDescription = (TextView) findViewById(R.id.article_desc);
        mArticleImage = (ImageView) findViewById(R.id.article_image);
        mUpButton = (ImageButton) findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mArticleUrl = (FloatingActionButton)findViewById(R.id.go_to_article);
        mArticleShare = (ImageButton) findViewById(R.id.article_share);


        getLoaderManager().initLoader(0,null,this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                mContenUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        if(mCursor != null && mCursor.getCount() > 0){
            mCursor.moveToFirst();
            mArticleTitle.setText(mCursor.getString(COLUMN_TITLE));

            String source = mCursor.getString(COLUMN_AUTHOR);
            String time = mCursor.getString(COLUMN_PUBLISH);
            time = "  " +  time.substring(0, time.indexOf('T'));
            mArticleAuthor.setText(getString(R.string.author,source,time));


            mArticleDescription.setText(mCursor.getString(COLUMN_DESC));
            Picasso.with(this)
                    .load(mCursor.getString(COLUMN_IMAGE))
                    .into(mArticleImage);

            mArticleUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(mCursor.getString(COLUMN_URL)));

                    if(intent.resolveActivity(getPackageManager()) != null){
                        startActivity(intent);
                    }
                }
            });

            mArticleShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    } else {
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    }
                    shareIntent.setType("text/plain");

                    shareIntent.putExtra(Intent.EXTRA_TEXT, mCursor.getString(COLUMN_URL));
                    startActivity(shareIntent);
                }
            });

        } else {
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
