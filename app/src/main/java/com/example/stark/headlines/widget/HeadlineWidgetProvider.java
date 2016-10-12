package com.example.stark.headlines.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.example.stark.headlines.DetailActivity;
import com.example.stark.headlines.HomeScreen;
import com.example.stark.headlines.R;

/**
 * Created by stark on 10/10/16.
 */

public class HeadlineWidgetProvider extends AppWidgetProvider {

    public static final String LOG_TAG = HeadlineWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds){
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //Create Intent to launch HomeScreen
            Intent intent = new Intent(context, HomeScreen.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            setRemoteAdapter(context, remoteViews);

            Intent clickIntentTemplate = new Intent(context, DetailActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);

            //Perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(context.getString(R.string.ACTION_DATA_UPDATED).equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass())
            );
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        } else {
        }
    }

    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views){
        views.setRemoteAdapter(R.id.widget_list, new Intent(context, HeadlineWidgetRemoteService.class));
    }

}
