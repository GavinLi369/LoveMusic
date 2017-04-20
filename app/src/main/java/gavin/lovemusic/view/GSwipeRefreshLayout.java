package gavin.lovemusic.view;

import android.content.Context;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by GavinLi
 * on 4/19/17.
 *
 * 优化SwipeRefreshLayout下拉刷新
 */

public class GSwipeRefreshLayout extends SwipeRefreshLayout {
    private boolean mHasScrollingChild = false;
    private ScrollingView mScrollingChild = null;

    public GSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public GSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //此时SwipeRefreshLayout会有一个CircleImageView的子View
        if(getChildCount() > 1 && getChildAt(1) instanceof ScrollingView) {
            mHasScrollingChild = true;
            mScrollingChild = (ScrollingView) getChildAt(1);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mHasScrollingChild) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setEnabled(true);
                    if(mScrollingChild.computeVerticalScrollOffset() != 0) {
                        setEnabled(false);
                    }
                default:
                    return super.dispatchTouchEvent(ev);
            }
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }
}
