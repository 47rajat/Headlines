package com.example.stark.headlines.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.stark.headlines.R;
import com.example.stark.headlines.data.DataContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by stark on 10/10/16.
 */

public class HeadlineWidgetRemoteService extends RemoteViewsService {
    public static final String LOG_TAG = HeadlineWidgetRemoteService.class.getSimpleName();
    public static final int ARTICLE_LIMIT = 15;

    public static final String[] HEADLINES_COLUMNS = {
            DataContract.NewsFeedHeadlinesEntry._ID,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_SOURCE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_ARTICLE,
            DataContract.NewsFeedHeadlinesEntry.COLUMN_HEADLINE_IMAGE
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_SOURCE = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_IMAGE = 3;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null){
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(DataContract.NewsFeedHeadlinesEntry.buildHeadlinesWithLimit(ARTICLE_LIMIT),
                        HEADLINES_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if(data != null){
                    data.close();
                    data = null;
                }

            }

            @Override
            public int getCount() {
                return  data == null? 0: data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if(i == AdapterView.INVALID_POSITION
                        || data == null || !data.moveToPosition(i)){
                    return  null;
                }
                final RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.widget_detail_list_item);

                remoteViews.setTextViewText(R.id.article_title, data.getString(COLUMN_TITLE));

                String url = null;

                if(data.getString(COLUMN_SOURCE).equals(getString(R.string.time_source_name))){
                    url = getString(R.string.time_logo_url);
                } else {
                    url = data.getString(COLUMN_IMAGE);
                }

                try {
                    Bitmap b = Picasso.with(HeadlineWidgetRemoteService.this)
                            .load(url)
                            .get();
                    remoteViews.setImageViewBitmap(R.id.widget_image, b);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final Intent fillInIntent = new Intent();
                Uri uri = DataContract.NewsFeedHeadlinesEntry.buildHeadlinesWithUrl(data.getString(COLUMN_IMAGE));
                fillInIntent.putExtra(Intent.EXTRA_TEXT, uri);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                if(data.moveToPosition(i))
                    return data.getLong(COLUMN_ID);
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
