package gavin.lovemusic.localmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import gavin.lovemusic.constant.R;
import gavin.lovemusic.entity.Music;

/**
 * Created by GavinLi
 * on 16-9-23.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Music> mMusicList;
    private OnItemClickListener mOnItemClickListener;
    private Context context;

    public RecyclerViewAdapter(List<Music> musicList) {
        this.mMusicList = musicList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.musicName.setText(mMusicList.get(position).getTitle());
        String musicInfo = mMusicList.get(position).getArtist() + " - " + mMusicList.get(position).getAlbum();
        holder.musicInfo.setText(musicInfo);
        Glide.with(context)
                .load(mMusicList.get(position).getAlbumPath())
                .into(holder.album);
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView musicName;
        public TextView musicInfo;
        public ImageView album;

        public ViewHolder(View itemView) {
            super(itemView);
            musicName = (TextView) itemView.findViewById(R.id.musicName);
            musicInfo = (TextView) itemView.findViewById(R.id.musicInfo);
            album = (ImageView) itemView.findViewById(R.id.musicAlbum);
            itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(getAdapterPosition()));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
