package com.example.stark.headlines.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by stark on 7/10/16.
 */

public class DataProvider extends ContentProvider {
    public static final String LOG_TAG = DataProvider.class.getSimpleName();
    private DbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int NEWSFEED = 100;
    static final int NEWSFEED_WITH_ID = 101;

    static final int HEADLINES = 200;
    static final int HEADLINES_WITH_ID = 201;
    static final int HEADLINES_WITH_LIMIT = 202;

    static final int OTHERS = 400;
    static final int OTHERS_WITH_ID = 401;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_NEWSFEEDS, NEWSFEED);
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_NEWSFEEDS + "/" + "*", NEWSFEED_WITH_ID);

        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_HEADLINES, HEADLINES);
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_HEADLINES + "/" + "*", HEADLINES_WITH_ID);
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_HEADLINES + "/" + "*" + "/" + "#", HEADLINES_WITH_LIMIT);

        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_OTHER_ARTICLES, OTHERS);
        uriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_OTHER_ARTICLES + "/" + "*", OTHERS_WITH_ID);

        return uriMatcher;
    }

    private static final SQLiteQueryBuilder sNewsFeedQueryBuilder;
    private static final SQLiteQueryBuilder sHeadlinesQueryBuilder;
    private static final SQLiteQueryBuilder sOthersQueryBuilder;

    static {
        sNewsFeedQueryBuilder = new SQLiteQueryBuilder();
        sNewsFeedQueryBuilder.setTables(DataContract.NewsFeedEntry.TABLE_NAME);

        sHeadlinesQueryBuilder = new SQLiteQueryBuilder();
        sHeadlinesQueryBuilder.setTables(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME);

        sOthersQueryBuilder = new SQLiteQueryBuilder();
        sOthersQueryBuilder.setTables(DataContract.OtherArticleEntry.TABLE_NAME);

    }

    private static final String sNewsFeedSelectionById = DataContract.NewsFeedEntry.TABLE_NAME +
            "." + DataContract.NewsFeedEntry.COLUMN_NEWSFEED_ID + " = ? ";

    private static final String sHeadlinesSelectionById = DataContract.NewsFeedHeadlinesEntry.TABLE_NAME +
            "." + DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_IMAGE + " = ? ";

    private static final String sOthersSelectionById = DataContract.OtherArticleEntry.TABLE_NAME +
            "." + DataContract.OtherArticleEntry.COLUMN_HEADLINE_IMAGE + " = ? ";


    private Cursor getNewsFeedById(Uri uri, String[] projection, String sortOrder){
        String id = DataContract.NewsFeedEntry.getIdFromUri(uri);

        return sNewsFeedQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sNewsFeedSelectionById,
                new String[] {id},
                null,
                null,
                sortOrder);
    }

    private Cursor getHeadlinesById(Uri uri, String[] projection, String sortOrder) {
        String url = DataContract.NewsFeedHeadlinesEntry.getUrlFromHeadlinesUri(uri);

        return sHeadlinesQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sHeadlinesSelectionById,
                new String[] {url},
                null,
                null,
                sortOrder);
    }

    private Cursor getOthersById(Uri uri, String[] projection, String sortOrder) {
        String url = DataContract.OtherArticleEntry.getUrlFromFavouriteUri(uri);

        return sOthersQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                sOthersSelectionById,
                new String[] {url},
                null,
                null,
                sortOrder);
    }



    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)){
            case NEWSFEED:
            {
                cursor = mDbHelper.getReadableDatabase().query(DataContract.NewsFeedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case NEWSFEED_WITH_ID:
            {
                cursor = getNewsFeedById(uri, projection, sortOrder);
                break;
            }
            case HEADLINES:
            {
                cursor = mDbHelper.getReadableDatabase().query(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case HEADLINES_WITH_ID:
            {
                cursor = getHeadlinesById(uri, projection, sortOrder);
                break;
            }

            case HEADLINES_WITH_LIMIT:
            {

                String limit = DataContract.NewsFeedHeadlinesEntry.getLimitFromHeadlinesUri(uri);
                cursor = mDbHelper.getReadableDatabase().query(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        limit);
                break;
            }

            case OTHERS:
            {
                cursor = mDbHelper.getReadableDatabase().query(DataContract.OtherArticleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            }
            case OTHERS_WITH_ID:
            {
                cursor = getOthersById(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case NEWSFEED:
                return DataContract.NewsFeedEntry.CONTENT_TYPE;
            case NEWSFEED_WITH_ID:
                return DataContract.NewsFeedEntry.CONTENT_ITEM_TYPE;
            case HEADLINES:
                return DataContract.NewsFeedHeadlinesEntry.CONTENT_TYPE;
            case HEADLINES_WITH_ID:
                return DataContract.NewsFeedHeadlinesEntry.CONTENT_ITEM_TYPE;
            case HEADLINES_WITH_LIMIT:
                return DataContract.NewsFeedHeadlinesEntry.CONTENT_TYPE;
            case OTHERS:
                return DataContract.OtherArticleEntry.CONTENT_TYPE;
            case OTHERS_WITH_ID:
                return DataContract.OtherArticleEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match  = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case NEWSFEED:
            {
                long _id = database.insert(DataContract.NewsFeedEntry.TABLE_NAME,null,contentValues);
                if(_id > 0){
                    returnUri = DataContract.NewsFeedEntry.buildNewFeedUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case HEADLINES:
            {
                long _id = database.insert(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME, null, contentValues);
                if(_id > 0){
                    returnUri = DataContract.NewsFeedHeadlinesEntry.buildNewsFeedHeadlinesUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case OTHERS:
            {
                long _id = database.insert(DataContract.OtherArticleEntry.TABLE_NAME, null, contentValues);
                if(_id > 0){
                    returnUri = DataContract.OtherArticleEntry.buildFavouritesUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        final SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        if(s == null) s = "1";

        switch (match){
            case NEWSFEED:
            {
                rowsDeleted = database.delete(DataContract.NewsFeedEntry.TABLE_NAME, s, strings);
                break;
            }
            case NEWSFEED_WITH_ID:
            {
                rowsDeleted = database.delete(DataContract.NewsFeedEntry.TABLE_NAME, s, strings);
                break;
            }
            case HEADLINES:
            {
                rowsDeleted = database.delete(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME, s, strings);
                break;
            }
            case HEADLINES_WITH_ID:
            {
                rowsDeleted = database.delete(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME, s, strings);
                break;
            }
            case OTHERS:
            {
                rowsDeleted = database.delete(DataContract.OtherArticleEntry.TABLE_NAME, s, strings);
                break;
            }
            case OTHERS_WITH_ID:
            {
                rowsDeleted = database.delete(DataContract.OtherArticleEntry.TABLE_NAME, s, strings);
                break;
            }
            default:
                throw  new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        switch (match){
            case NEWSFEED:
            {
                database.beginTransaction();
                int returnCount = 0;
                try{
                    for(ContentValues contentValues: values){
                        long _id = database.insert(DataContract.NewsFeedEntry.TABLE_NAME, null, contentValues);

                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            }
            case HEADLINES:
            {
                database.beginTransaction();
                int returnCount = 0;

                try{
                    for(ContentValues contentValues: values){
                        long _id = database.insert(DataContract.NewsFeedHeadlinesEntry.TABLE_NAME, null, contentValues);

                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;
            }
            case OTHERS:
            {
                database.beginTransaction();
                int returnCount = 0;

                try{
                    for(ContentValues contentValues: values){
                        long _id = database.insert(DataContract.OtherArticleEntry.TABLE_NAME, null, contentValues);

                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri,null);
                return returnCount;

            }
            default:
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int rowsUpdated;

        switch (match){
            case NEWSFEED:
            {
                rowsUpdated = database.update(DataContract.NewsFeedEntry.TABLE_NAME, contentValues, s, strings);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
