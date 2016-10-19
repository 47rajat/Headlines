package com.rajat.stark.headlines.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by stark on 7/10/16.
 */

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "headlines.db";

    public DbHelper(Context context) {super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_NEWSFEED_TABLE = "CREATE TABLE " + DataContract.NewsFeedEntry.TABLE_NAME + " ("+
                DataContract.NewsFeedEntry._ID + " INTEGER PRIMARY KEY, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_ID + " TEXT UNIQUE NOT NULL, "+
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_NAME + " TEXT NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_DESC + " TEXT NOT NULL, "+
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_HOMEPAGE + " TEXT NOT NULL, "+
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_CATEGORY + " TEXT NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_COUNTRY + " TEXT NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LOGO + " TEXT NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LOGO_LARGE + " TEXT NOT NULL, "+
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_TOP + " INTEGER NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_LATEST + " INTEGER NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_POPULAR + " INTEGER NOT NULL, " +
                DataContract.NewsFeedEntry.COLUMN_NEWSFEED_IS_FAVOURITE + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_HEADLINES_TABLE = "CREATE TABLE " + DataContract.NewsFeedHeadlinesEntry.TABLE_NAME + " ("+
                DataContract.NewsFeedHeadlinesEntry._ID + " INTEGER PRIMARY KEY, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_SOURCE + " TEXT NOT NULL, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_AUTHOR + " TEXT NOT NULL, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_DESC + " TEXT NOT NULL, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_ARTICLE + " TEXT NOT NULL, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_URL + " TEXT UNIQUE NOT NULL, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_IMAGE + " TEXT NOT NULL, " +
                DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_PUBLISH_TIME + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_OTHER_ARTICLES_TABLE = "CREATE TABLE " + DataContract.OtherArticleEntry.TABLE_NAME + " ("+
                DataContract.OtherArticleEntry._ID + " INTEGER PRIMARY KEY, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_SOURCE + " TEXT NOT NULL, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_AUTHOR + " TEXT NOT NULL, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_DESC + " TEXT NOT NULL, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_ARTICLE + " TEXT NOT NULL, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_URL + " TEXT UNIQUE NOT NULL, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_IMAGE + " TEXT NOT NULL, " +
                DataContract.OtherArticleEntry.COLUMN_HEADLINE_PUBLISH_TIME + " TEXT NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_NEWSFEED_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_HEADLINES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_OTHER_ARTICLES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.NewsFeedEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.NewsFeedHeadlinesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.OtherArticleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
