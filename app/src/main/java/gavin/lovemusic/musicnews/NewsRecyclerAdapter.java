package gavin.lovemusic.musicnews;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import gavin.lovemusic.constant.R;

/**
 * Created by GavinLi
 * on 4/5/17.
 */

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder> {
    private List<NewsEntry> mNewsEntries = new ArrayList<>();
    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_news, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mNews.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, NewsActivity.class);
            intent.putExtra(NewsActivity.URL_KEY, mNewsEntries.get(position).getLinkUrl());
            mContext.startActivity(intent);
        });
        holder.mTitle.setText(mNewsEntries.get(position).getTitle());
        holder.mSubTitle.setText(mNewsEntries.get(position).getSubTitle());
        Glide.with(mContext)
                .load(mNewsEntries.get(position).getImageUrl())
                .into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return mNewsEntries == null ? 0 : mNewsEntries.size();
    }

    public void setNewsEntries(List<NewsEntry> newsEntries) {
        mNewsEntries.clear();
        mNewsEntries.addAll(newsEntries);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mNews;
        private ImageView mImage;
        private TextView mTitle;
        private TextView mSubTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            mNews = (LinearLayout) itemView.findViewById(R.id.layout_news);
            mImage = (ImageView) itemView.findViewById(R.id.img_image);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mSubTitle = (TextView) itemView.findViewById(R.id.tv_sub_title);
        }
    }
}
