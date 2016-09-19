package gavin.lovemusic.localmusic.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import gavin.lovemusic.constant.R;

/**
 * Created by Gavin Li on 2016/1/20.
 *
 * MainActivity的ListView适配器
 * 重写了BaseAdapter的getView方法，并对其做了优化
 */
public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<HashMap<String, String>> mItems;

    public ListViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public ListViewAdapter(List<HashMap<String, String>> items, Context context) {
        this(context);
        mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.song_list_view, null);

            viewHolder = new ViewHolder();
            viewHolder.itemId = (TextView)convertView.findViewById(R.id.itemId);
            viewHolder.musicName = (TextView)convertView.findViewById(R.id.musicName);
            viewHolder.musicInfo = (TextView)convertView.findViewById(R.id.musicInfo);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.itemId.setText(mItems.get(position).get("itemId"));
        viewHolder.musicName.setText(mItems.get(position).get("musicName"));
        viewHolder.musicInfo.setText(mItems.get(position).get("musicInfo"));
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    static class ViewHolder{
        public TextView itemId;
        public TextView musicName;
        public TextView musicInfo;
    }
}