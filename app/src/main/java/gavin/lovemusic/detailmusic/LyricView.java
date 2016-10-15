package gavin.lovemusic.detailmusic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import gavin.lovemusic.entity.LyricRow;

/**
 * Created by Gavin on 2015/8/24.
 * 歌词View
 */
public class LyricView extends View {
    private Paint paintHL = new Paint();
    private Paint paint = new Paint();

    private int width;
    private int height;

    private int index;

    private ArrayList<LyricRow> lyricList;

    private static final int TEXT_SIZE = 25;   //歌词文字大小值
    private static final int INTERVAL = 40;    //歌词每行的间隔

    private OnLyricViewSeekListener lyricViewSeekListener;

    public LyricView(Context context) {
        super(context);
        init();
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化各项数值
     */
    private void init() {
        paintHL.setColor(Color.WHITE);
        paintHL.setTextSize(25);
        paintHL.setAntiAlias(true);      //消除锯齿
        paintHL.setTextAlign(Paint.Align.CENTER);   //设置文本对齐方式

        paint.setColor(Color.GRAY);
        paint.setTextSize(25);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }
    /**
     * 初始化歌词列表
     */
    public void setLyricList(ArrayList<LyricRow> lyricList) {
        this.lyricList = lyricList;
    }

    public void setTime(long time) {
        if(!seekLyric) {
            this.index = getLyricIndex(lyricList, time);
        }
    }

    public void setOnLyricViewSeekListener(OnLyricViewSeekListener listener) {
        this.lyricViewSeekListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }
        if (lyricList == null) {
            canvas.drawText("没有歌词赶快下载吧...", width / 2, height / 2, paintHL);
            return;
        }

        float tempY = height / 2;
        String lyric = lyricList.get(index).getLyricStr();
        canvas.drawText(lyric, width / 2, tempY, paintHL);

        for (int i = index - 1; i >= 0; i--) {
            tempY = tempY - TEXT_SIZE - INTERVAL;
            if (tempY < 0) {
                break;
            }
            String temp = lyricList.get(i).getLyricStr();
            canvas.drawText(temp, width / 2, tempY, paint);
        }
        tempY = height / 2;

        for (int i = index + 1; i < lyricList.size(); i++) {
            tempY = tempY + TEXT_SIZE + INTERVAL;
            if (tempY > height) {
                break;
            }
            String temp = lyricList.get(i).getLyricStr();
            canvas.drawText(temp, width / 2, tempY, paint);
        }
    }

    private static final int MIN_SEEK_FIRED_OFFSET = TEXT_SIZE + INTERVAL;
    private float mLastTouchPostion;
    private boolean seekLyric = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(lyricList == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchPostion = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float offset = event.getY() - mLastTouchPostion;
                if(Math.abs(offset) < MIN_SEEK_FIRED_OFFSET) break;
                int rowOffset = (int) offset / (TEXT_SIZE + INTERVAL);
                index -= rowOffset;
                //防止index越界
                index = Math.max(0, index);
                index = Math.min(index, lyricList.size() - 1);
                mLastTouchPostion = event.getY();
                seekLyric = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(seekLyric) {
                    lyricViewSeekListener.lyricViewSeek(lyricList.get(index));
                    seekLyric = false;
                }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    //根据当前播放时间获取歌词索引
    private int getLyricIndex(ArrayList<LyricRow> lyricList, long currentTime) {
        int lyricIndex = 0;
        if (lyricList != null) {
            int index = 0;
            for (int i = 0; i < lyricList.size(); i++) {
                if (lyricList.get(i).getLyricTime() < currentTime) {
                    index++;
                }
                lyricIndex = index - 1;
                if (lyricIndex < 0) {
                    lyricIndex = 0;
                }
            }
        }
        return lyricIndex;
    }
}
