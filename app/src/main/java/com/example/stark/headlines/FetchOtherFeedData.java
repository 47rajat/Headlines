package com.example.stark.headlines;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
 * Created by stark on 10/10/16.
 */

public class FetchOtherFeedData extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = FetchOtherFeedData.class.getSimpleName();
    private final Context mContext;
    private final String mSource;
    private final String mCategory;

    public FetchOtherFeedData(Context context, String source, String category){
        mContext = context;
        mSource = source;
        mCategory = category;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        HttpURLConnection urlConnection = null;
        BufferedReader newsFeedReader = null;

        String headlinesJsonString = null;

        try{
            //Construct URL for articles query
            final String HEADLINES_BASE_URL = mContext.getString(R.string.headlines_base_url);
            final String HEADLINES_SOURCE_PARAM = mContext.getString(R.string.headlines_source_param);
            final String HEADLINE_SORT_PARAM = mContext.getString(R.string.headlines_sort_by_param);
            final String API_KEY_PARAM = mContext.getString(R.string.headlines_api_param);

            Uri builtUri = Uri.parse(HEADLINES_BASE_URL).buildUpon()
                    .appendQueryParameter(HEADLINES_SOURCE_PARAM, mSource)
                    .appendQueryParameter(HEADLINE_SORT_PARAM, mCategory)
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
                return null;
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
                return null;
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

        return null;
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

            //Remove existing data
            mContext.getContentResolver().delete(DataContract.OtherArticleEntry.CONTENT_URI, null, null);

            //Add to database

            if(contentValuesVector.size() > 0) {
                ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(contentValuesArray);

                mContext.getContentResolver().bulkInsert(DataContract.OtherArticleEntry.CONTENT_URI, contentValuesArray);
            }


        } catch (JSONException e){
            e.printStackTrace();
        }

    }



}
