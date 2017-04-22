package gavin.lovemusic.view;

import android.content.Context;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by GavinLi
 * on 4/19/17.
 *
 * 优化SwipeRefreshLayout下拉刷新
 */

public class GSwipeRefreshLayout extends SwipeRefreshLayout {
    private boolean mHasScrollingChild = false;
    private ScrollingView mScrollingChild = null;
    private boolean mIsDragMode = false;

    public GSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public GSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 此时SwipeRefreshLayout会有一个CircleImageView的子View
        if(getChildCount() > 1 && getChildAt(1) instanceof ScrollingView) {
            mHasScrollingChild = true;
            mScrollingChild = (ScrollingView) getChildAt(1);
        }
    }

    private float mDownPostion;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mHasScrollingChild) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setEnabled(true);
                    mDownPostion = ev.getY();
                    if(mScrollingChild.computeVerticalScrollOffset() != 0) {
                        setEnabled(false);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isEnabled()) {
                        if (ev.getY() < mDownPostion) setEnabled(false);
                        else mIsDragMode = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mIsDragMode = false;
            }
            return super.dispatchTouchEvent(ev);
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    // 当CircleImageView向下拖动时，停止向子View分发单击事件(即使在当前点击事件中再次向上滑动)。
    // 由于停止点击事件的分发造成滑动的灵敏度降低(恢复正常，原来的灵敏度过高)，
    // 猜测可能是在原来的滑动过程中子View通过调用onNestedScroll()方法间接重复调用了
    // MoveSpanner()方法导致。
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsDragMode || super.onInterceptTouchEvent(ev);
    }

    // 控件disable时禁止调用moveSpinner()方法
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        if(isEnabled()) {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        } else {
            try {
                Field mParentOffsetInWindowField =
                        SwipeRefreshLayout.class.getDeclaredField("mParentOffsetInWindow");
                mParentOffsetInWindowField.setAccessible(true);
                int[] mParentOffsetInWindow = (int[]) mParentOffsetInWindowField.get(this);
                // Dispatch up to the nested parent first
                dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                        mParentOffsetInWindow);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
