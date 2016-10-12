package com.example.stark.headlines.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.stark.headlines.BuildConfig;
import com.example.stark.headlines.R;
import com.example.stark.headlines.data.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by stark on 8/10/16.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED = "com.example.headlines.ACTION_DATA_UPDATED";

    //Interval at which to sync with the newsapi in seconds
    public static final int SYNC_INTERVAL = 60*60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/2;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {


        Context context = getContext();

        Cursor cursor = context.getContentResolver().query(DataContract.NewsFeedEntry.CONTENT_URI,
                new String[] {DataContract.NewsFeedEntry.COLUMN_NEWSFEED_ID},
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_IS_FAVOURITE + " = ?",
                new String[] { "1"},
                null);

        if(cursor == null || cursor.getCount() == 0){
            getNewsFeedsFromUrl(context);
        } else {
        }

        if(cursor != null){
            cursor.moveToFirst();
            context.getContentResolver().delete(DataContract.NewsFeedHeadlinesEntry.CONTENT_URI, null, null);
            for(int i = 0; i < cursor.getCount(); i++){
                getHeadLinesFromUrl(context, cursor.getString(0));
                cursor.moveToNext();
            }
            cursor.close();
        } else {
        }





    }

    private void getHeadLinesFromUrl(Context context, String source) {
        //Need to be declared outside the try/catch block
        //so that they can be closed in the final block

        HttpURLConnection urlConnection = null;
        BufferedReader newsFeedReader = null;

        String headlinesJsonString = null;

        try{
            //Construct URL for articles query
            final String HEADLINES_BASE_URL = context.getString(R.string.headlines_base_url);
            final String HEADLINES_SOURCE_PARAM = context.getString(R.string.headlines_source_param);
            final String API_KEY_PARAM = context.getString(R.string.headlines_api_param);

            Uri builtUri = Uri.parse(HEADLINES_BASE_URL).buildUpon()
                    .appendQueryParameter(HEADLINES_SOURCE_PARAM, source)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.NEWSAPI_API)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to newsapi, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            newsFeedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = newsFeedReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            headlinesJsonString = buffer.toString();
            getHeadLinesFromJson(headlinesJsonString);


        } catch (IOException e){

        } catch (JSONException e) {

            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (newsFeedReader != null) {
                try {
                    newsFeedReader.close();
                } catch (final IOException e) {
                }
            }
        }

    }



    private void getNewsFeedsFromUrl(Context context) {
        //Need to be declared outside the try/catch block
        //so that they can be closed in the final block

        HttpURLConnection urlConnection = null;
        BufferedReader newsFeedReader = null;

        String newsFeedJsonString = null;

        try {
            //Construct URL for articles query
            final String HEADLINES_BASE_URL = context.getString(R.string.newsfeed_base_url);

            Uri newsFeedUri = Uri.parse(HEADLINES_BASE_URL);

            URL url = new URL(newsFeedUri.toString());

            // Create the request to newsapi, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            newsFeedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = newsFeedReader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            newsFeedJsonString = buffer.toString();
            getNewsFeedsFromJson(newsFeedJsonString);
        } catch (IOException e) {
            // If the code didn't successfully get the news data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (newsFeedReader != null) {
                try {
                    newsFeedReader.close();
                } catch (final IOException e) {
                }
            }
        }


    }

    private void getHeadLinesFromJson(String headlinesJsonString) throws JSONException {

        final String API_STATUS = "status";
        final String API_ARTICLES = "articles";
        final String API_SOURCE = "source";

        final String API_AUTHOR = "author";
        final String API_DESC = "description";
        final String API_TITLE = "title";
        final String API_URL = "url";
        final String API_IMAGE = "urlToImage";
        final String API_PUBLISH_TIME = "publishedAt";

        try {
            JSONObject headLinesJson = new JSONObject(headlinesJsonString);

            JSONArray headLinesArray = headLinesJson.getJSONArray(API_ARTICLES);

            //values to be collected
            String source = headLinesJson.getString(API_SOURCE);

            Vector<ContentValues> contentValuesVector = new Vector<>(headLinesArray.length());

            for(int i = 0; i < headLinesArray.length(); i++){

                //Other values to be collected
                String author;
                String description;
                String title;
                String url;
                String imageURL;
                String publishTime;

                JSONObject singleArticle = headLinesArray.getJSONObject(i);

                author = singleArticle.getString(API_AUTHOR);
                description = singleArticle.getString(API_DESC);
                title = singleArticle.getString(API_TITLE);
                url = singleArticle.getString(API_URL);
                imageURL = singleArticle.getString(API_IMAGE);
                publishTime = singleArticle.getString(API_PUBLISH_TIME);

                ContentValues contentValues = new ContentValues();
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_SOURCE, source);
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_AUTHOR, author);
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_DESC, description);
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_ARTICLE, title);
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_URL, url);
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_IMAGE, imageURL);
                contentValues.put(DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_PUBLISH_TIME, publishTime);

                contentValuesVector.add(contentValues);
            }

            int inserted = 0;

            //Add to database

            if(contentValuesVector.size() > 0) {
                ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);

                getContext().getContentResolver().bulkInsert(DataContract.NewsFeedHeadlinesEntry.CONTENT_URI, contentValuesArray);

                updateWidgets();
            }


        } catch (JSONException e){
            e.printStackTrace();
        }

    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private void getNewsFeedsFromJson(String newsFeedJsonString) throws JSONException {

            final String API_STATUS = "status";
            final String API_SOURCES = "sources";

            final String API_ID = "id";
            final String API_NAME = "name";
            final String API_DESC = "description";
            final String API_URL = "url";
            final String API_CATEGORY = "category";
            final String API_COUNTRY = "country";

            final String API_LOGO = "urlsToLogos";
            final String API_LOGO_MEDIUM = "medium";
            final String API_LOGO_LARGE = "large";

            final String API_SORT_CATEGORY = "sortBysAvailable";

            try{
                JSONObject newsFeedJson = new JSONObject(newsFeedJsonString);
                Context context = getContext();

                JSONArray newsFeedArray =newsFeedJson.getJSONArray(API_SOURCES);

                Vector<ContentValues> contentValuesVector = new Vector<>(newsFeedArray.length());

                for(int i = 0; i < newsFeedArray.length(); i++){

                    //These are the values that will be collected;
                    String id;
                    String name;
                    String description;
                    String url;
                    String category;
                    String country;
                    String logo;
                    String logoLarge;
                    int top = 0;
                    int latest = 0;
                    int popular = 0;
                    int favourite = 0;

                    JSONObject singleFeed = newsFeedArray.getJSONObject(i);

                    id = singleFeed.getString(API_ID);
                    name = singleFeed.getString(API_NAME);
                    description = singleFeed.getString(API_DESC);
                    url = singleFeed.getString(API_URL);
                    category = singleFeed.getString(API_CATEGORY);
                    country = singleFeed.getString(API_COUNTRY);
                    logo = singleFeed.getJSONObject(API_LOGO).getString(API_LOGO_MEDIUM);
                    logoLarge = singleFeed.getJSONObject(API_LOGO).getString(API_LOGO_LARGE);
                    JSONArray sortavail = singleFeed.getJSONArray(API_SORT_CATEGORY);

                    for(int j = 0; j < sortavail.length(); j++){
                        switch (sortavail.getString(j)){
                            case "top":
                                top = 1;
                                break;
                            case "popular":
                                popular = 1;
                                break;
                            case "latest":
                                latest = 1;
                                break;
                            default:
                                throw new UnsupportedOperationException("Sortavailable type " + sortavail.getString(j));
                        }
                    }

                    favourite = isDefaultFavourite(id);

                    ContentValues contentValues = new ContentValues();

                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_ID, id);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_NAME, name);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_DESC, description);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_HOMEPAGE, url);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY, category);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_COUNTRY, country);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LOGO, logo);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LOGO_LARGE, logoLarge);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_TOP, top);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LATEST, latest);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_POPULAR, popular);
                    contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_IS_FAVOURITE, favourite);

                    contentValuesVector.add(contentValues);
                }

                int inserted = 0;

                //Add to database

                if(contentValuesVector.size() > 0) {
                    ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                    contentValuesVector.toArray(contentValuesArray);

                    getContext().getContentResolver().bulkInsert(DataContract.NewsFeedEntry.CONTENT_URI, contentValuesArray);
                }


            } catch (JSONException e){
                e.printStackTrace();
            }
    }

    private int isDefaultFavourite(String id) {
        String[] defaultFavouritesId = new String[] {"bbc-news", "business-insider", "espn", "recode", "bloomberg"};

        for(String defaultId: defaultFavouritesId){
            if(id.equals(defaultId)){
                return 1;
            }
        }
        return 0;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


}
