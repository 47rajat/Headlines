package com.rajat.stark.headlines.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by stark on 7/10/16.
 */

public class DataContract {

    //Content authority for the app
    public static final String CONTENT_AUTHORITY = "com.example.stark.headlines";

    //The base of all URI the app will use to contact the Content Provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Possible path appended to the base uri
    //Also the name for the SQL tables being used by the app
    public static final String PATH_NEWSFEEDS = "newsFeeds";
    public static final String PATH_HEADLINES = "headlinesFromFavouriteFeed";
    public static final String PATH_OTHER_ARTICLES = "otherArticles";


    //Inner class that defines the table content of newsFeed table
    public static final class NewsFeedEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon().appendPath(PATH_NEWSFEEDS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWSFEEDS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWSFEEDS;

        //Table name
        public static final String TABLE_NAME = "newsFeed";

        //The unique identifier for the news source.
        public static final String COLUMN_NEWSFEED_ID = "id";

        //The display-friendly name of the news source.
        public static final String COLUMN_NEWSFEED_NAME = "name";

        //A brief description of the news source and what area they specialize in.
        public static final String COLUMN_NEWSFEED_DESC = "description";

        //The base URL or homepage of the source.
        public static final String COLUMN_NEWSFEED_HOMEPAGE = "url";

        //The topic category that the source focuses on.
        public static final String COLUMN_NEWSFEED_CATEGORY = "category";

        //The 2-letter ISO 3166-1 code of the country that the source mainly focuses on.
        public static final String COLUMN_NEWSFEED_COUNTRY = "country";

        //An object containing URLs to the source's logo.
        public static final String COLUMN_NEWSFEED_LOGO = "medium";
        public static final String COLUMN_NEWSFEED_LOGO_LARGE = "large";

        //The available headline lists for the news source.
        public static final String COLUMN_NEWSFEED_TOP = "top";
        public static final String COLUMN_NEWSFEED_LATEST = "latest";
        public static final String COLUMN_NEWSFEED_POPULAR = "popular";

        public static final String COLUMN_NEWSFEED_IS_FAVOURITE = "isFavourite";


        public static Uri buildNewFeedUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildNewsFeedWithId(String id){
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }


    public static class NewsFeedHeadlinesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon().appendPath(PATH_HEADLINES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HEADLINES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HEADLINES;

        //Table Name
        public static final String TABLE_NAME = "newsHeadlines";

        //The identifier of the source requested.
        public static final String COLUMN_HEADLINE_SOURCE = "source";

        //The author of the article.
        public static final String COLUMN_HEADLINE_AUTHOR = "author";

        //A description or preface for the article.
        public static final String COLUMN_HEADLINE_DESC = "description";

        //The headline or title of the article.
        public static final String COLUMN_HEADLINE_ARTICLE = "title";

        //The direct URL to the content page of the article.
        public static final String COLUMN_HEADLINE_URL = "url";

        //The URL to a relevant image for the article.
        public static final String COLUMN_HEADLINE_IMAGE = "urlToImage";

        //The best attempt at finding a date for the article, in UTC (+0).
        public static final String COLUMN_HEADLINE_PUBLISH_TIME = "publishedAt";

        public static Uri buildNewsFeedHeadlinesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildHeadlinesWithUrl(String url){
            return CONTENT_URI.buildUpon().appendPath(url).build();
        }
        public static Uri buildHeadlinesWithLimit(int limit){
            return CONTENT_URI.buildUpon()
                    .appendPath("limit")
                    .appendPath(Integer.toString(limit)).build();
        }

        public static String getUrlFromHeadlinesUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getLimitFromHeadlinesUri(Uri uri){
            return uri.getPathSegments().get(2);
        }
    }

    public static class OtherArticleEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon().appendPath(PATH_OTHER_ARTICLES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OTHER_ARTICLES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OTHER_ARTICLES;

        //Table Name
        public static final String TABLE_NAME = "otherArticleHeadlines";

        //The identifier of the source requested.
        public static final String COLUMN_HEADLINE_SOURCE = "source";

        //The author of the article.
        public static final String COLUMN_HEADLINE_AUTHOR = "author";

        //A description or preface for the article.
        public static final String COLUMN_HEADLINE_DESC = "description";

        //The headline or title of the article.
        public static final String COLUMN_HEADLINE_ARTICLE = "title";

        //The direct URL to the content page of the article.
        public static final String COLUMN_HEADLINE_URL = "url";

        //The URL to a relevant image for the article.
        public static final String COLUMN_HEADLINE_IMAGE = "urlToImage";

        //The best attempt at finding a date for the article, in UTC (+0).
        public static final String COLUMN_HEADLINE_PUBLISH_TIME = "publishedAt";

        public static Uri buildFavouritesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildOtherHeadlineWithUrl(String url){
            return CONTENT_URI.buildUpon().appendPath(url).build();
        }

        public static String getUrlFromFavouriteUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }

}
