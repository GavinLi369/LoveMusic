package gavin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import gavin.model.LyricContent;

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

    private ArrayList<LyricContent> lyricList = null;

    private static final int TEXT_SIZE = 25;   //歌词文字大小值
    private static final int INTERVAL = 40;    //歌词每行的间隔

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
     * 设置索引值
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 初始化歌词列表
     */
    public void setLyricList(ArrayList<LyricContent> lyricList) {
        this.lyricList = lyricList;
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

}
