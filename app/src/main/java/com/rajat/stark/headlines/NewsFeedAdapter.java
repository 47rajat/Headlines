package com.rajat.stark.headlines;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rajat.stark.headlines.data.DataContract;
import com.rajat.stark.headlines.sync.SyncAdapter;
import com.squareup.picasso.Picasso;

/**
 * Created by stark on 8/10/16.
 */

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.NewsFeedAdapterViewHolder> {

    private static final String LOG_TAG = NewsFeedAdapter.class.getSimpleName();

    private Cursor mCursor;
    private String mCategory;
    private final Context mContext;
//    final private View mEmptyView;

    public NewsFeedAdapter(Context context, String category)  {
        mContext = context;
        mCategory = category;
    }

    @Override
    public NewsFeedAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_card_view, parent, false);
            view.setFocusable(true);
            return new NewsFeedAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(NewsFeedAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        if(mCursor != null){
            holder.mNewsFeedName.setText(mCursor.getString(HomeScreen.COLUMN_NAME));
        }

        if(mCursor.getString(HomeScreen.COLUMN_LOGO) != null){
            Picasso.with(mContext)
                    .load(mCursor.getString(HomeScreen.COLUMN_LOGO))
                    .into(holder.mNewsFeedThumbnail);
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                holder.mNewsFeedThumbnail.setTransitionName(mCursor.getString(HomeScreen.COLUMN_LOGO));
//            }
        }

        if(mCursor.getInt(HomeScreen.COLUMN_IS_FAVOURITE) == 1){

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.mFavouriteStatus.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.ic_star_black_24dp, mContext.getTheme()));
            } else {

                holder.mFavouriteStatus.setImageDrawable(mContext.getResources().getDrawable
                        (R.drawable.ic_star_black_24dp));
            }
        } else {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.mFavouriteStatus.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.ic_star_border_black_24dp, mContext.getTheme()));
            } else {

                holder.mFavouriteStatus.setImageDrawable(mContext.getResources().getDrawable
                        (R.drawable.ic_star_border_black_24dp));
            }
        }


    }

    @Override
    public int getItemCount() {
        if(null == mCursor) return  0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();

    }

    public void setCategory(String category){
        mCategory = category;
    }

    public class NewsFeedAdapterViewHolder extends RecyclerView.ViewHolder {
        private ImageView mNewsFeedThumbnail;
        private TextView mNewsFeedName;
        private ImageView mFavouriteStatus;

        public NewsFeedAdapterViewHolder(View itemView) {
            super(itemView);

            mFavouriteStatus = (ImageView) itemView.findViewById(R.id.newsfeed_status);
            mNewsFeedName = (TextView) itemView.findViewById(R.id.newsfeed_name);
            mNewsFeedThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);

            mNewsFeedThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNewsFeedDetailScreen(mNewsFeedThumbnail);
                }
            });

            mNewsFeedName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNewsFeedDetailScreen(mNewsFeedThumbnail);
                }
            });

            mFavouriteStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateFavouriteStatus();
                }
            });

        }


        private void openNewsFeedDetailScreen(ImageView imageView){
            int pos = getAdapterPosition();
            mCursor.moveToPosition(pos);

            String source = mCursor.getString(HomeScreen.COLUMN_NEWSFEED_ID);
            int top = mCursor.getInt(HomeScreen.COLUMN_HAS_TOP);
            int latest = mCursor.getInt(HomeScreen.COLUMN_HAS_LATEST);
            int popular = mCursor.getInt(HomeScreen.COLUMN_HAS_POPULAR);
            String feedId = mCursor.getString(HomeScreen.COLUMN_NEWSFEED_ID);

            Intent intent = new Intent(mContext, OtherHeadlinesActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, mCursor.getString(HomeScreen.COLUMN_NEWSFEED_ID));
            intent.putExtra(OtherHeadlinesActivity.CATEGORY_TOP, top);
            intent.putExtra(OtherHeadlinesActivity.CATEGORY_LATEST, latest);
            intent.putExtra(OtherHeadlinesActivity.CATEGORY_POPULAR, popular);
            intent.putExtra(OtherHeadlinesActivity.SOURCE_NEWS_FEED, DataContract.NewsFeedEntry.buildNewsFeedWithId(feedId));

            mContext.getContentResolver().delete(DataContract.OtherArticleEntry.CONTENT_URI,null,null);
            if(Utility.isOnline(mContext)) {
                new FetchOtherFeedData(mContext, source, OtherHeadlinesActivity.CATEGORY_TOP).execute();
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Bundle bundle = ActivityOptions
                        .makeSceneTransitionAnimation(
                                (Activity)mContext,
                                imageView,
                                imageView.getTransitionName()
                        ).toBundle();
                mContext.startActivity(intent, bundle);
            }else {
                mContext.startActivity(intent);
            }
        }

        private void updateFavouriteStatus(){
            int pos = getAdapterPosition();
            mCursor.moveToPosition(pos);

            int favStatus = mCursor.getInt(HomeScreen.COLUMN_IS_FAVOURITE);
            String feedId = mCursor.getString(HomeScreen.COLUMN_NEWSFEED_ID);

            ContentValues contentValues  = new ContentValues();
            contentValues.put(DataContract.NewsFeedEntry.COLUMN_NEWSFEED_IS_FAVOURITE, 1 - favStatus);
            mContext.getContentResolver().update(DataContract.NewsFeedEntry.CONTENT_URI,
                    contentValues,
                    DataContract.NewsFeedEntry.COLUMN_NEWSFEED_ID + " = ?",
                    new String[] {feedId});

            SyncAdapter.syncImmediately(mContext);
        }

    }
}
