package com.zin.sideslip;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.zin.toolutils.log.LogcatUtil;

import java.util.ArrayList;
import java.util.List;

import static com.zin.sideslip.SideslipLayout.ActionEvent.ACTION_CANCEL;
import static com.zin.sideslip.SideslipLayout.ActionEvent.ACTION_LEFT;
import static com.zin.sideslip.SideslipLayout.ActionEvent.ACTION_RIGHT;

/**
 * Created by zhujinming on 2017/6/19.
 */
public class SideslipLayout extends ViewGroup {

    private float fraction = 0.5f;
    private int scaledTouchSlop;

    private boolean isCanLeftSwipe;
    private boolean isCanRightSwipe;

    private final List<View> mMatchParentChildren = new ArrayList<>(1);
    private static SideslipLayout mViewCache;
    private static ActionEvent mActionEvent;

    private View mLeftView;
    private View mRightView;
    private View mContentView;
    private Scroller mScroller;

    private MarginLayoutParams mContentViewLp;

    private PointF mLastP;
    private PointF mFirstP;

    public SideslipLayout(Context context) {
        super(context);
    }

    public SideslipLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SideslipLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取childView的个数

        int count = getChildCount();
        //参考frameLayout测量代码
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        //遍历childViews
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }
        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                            lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                            lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childCount = getChildCount();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        for (int i = 0; i < childCount; i++) {

            View childView = getChildAt(i);
            if (mLeftView == null && isCanLeftSwipe()) {

                mLeftView = childView;
                mLeftView.setClickable(true);
            } else if (mContentView == null) {

                mContentView = childView;
                mContentView.setClickable(true);
            } else if (mRightView == null && isCanRightSwipe()) {

                mRightView = childView;
                mRightView.setClickable(true);
            }
        }

        if (mContentView != null) {

            mContentViewLp = (MarginLayoutParams) mContentView.getLayoutParams();

            int top = paddingTop + mContentViewLp.topMargin;
            int left = paddingLeft + mContentViewLp.leftMargin;
            int right = paddingLeft + mContentViewLp.leftMargin + mContentView.getMeasuredWidth();
            int bottom = top + mContentView.getMeasuredHeight();

            mContentView.layout(left, top, right, bottom);
        }

        if (mLeftView != null) {
            MarginLayoutParams leftViewLp = (MarginLayoutParams) mLeftView.getLayoutParams();
            int top = paddingTop + leftViewLp.topMargin;
            int left = 0 - mLeftView.getMeasuredWidth() + leftViewLp.leftMargin + leftViewLp.rightMargin;
            int right = 0 - leftViewLp.rightMargin;
            int bottom = top + mLeftView.getMeasuredHeight();
            mLeftView.layout(left, top, right, bottom);
        }

        if (mRightView != null) {
            MarginLayoutParams rightViewLp = (MarginLayoutParams) mRightView.getLayoutParams();
            int top = paddingTop + rightViewLp.topMargin;
            int left = mContentView.getRight() + mContentViewLp.rightMargin + rightViewLp.leftMargin;
            int right = left + mRightView.getMeasuredWidth();
            int bottom = top + mRightView.getMeasuredHeight();
            mRightView.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.finalSwipe(ACTION_CANCEL);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this == mViewCache) {
            mViewCache.finalSwipe(mActionEvent);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    private void init(Context context) {

        mScroller = new Scroller(context);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context.getApplicationContext());
        scaledTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:

                LogcatUtil.getInstance().info("ACTION_DOWN", this);

                if (mLastP == null) {
                    mLastP = new PointF();
                }
                mLastP.set(ev.getRawX(), ev.getRawY());

                if (mFirstP == null) {
                    mFirstP = new PointF();
                }
                mFirstP.set(ev.getRawX(), ev.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:

                LogcatUtil.getInstance().info("ACTION_MOVE", this);

                float distanceX = mLastP.x - ev.getRawX();
                float distanceY = mLastP.y - ev.getRawY();
                if (Math.abs(distanceY) > scaledTouchSlop * 2) {
                    break;
                }
                //当处于水平滑动时，禁止父类拦截
                if (Math.abs(distanceX) > scaledTouchSlop * 2 || Math.abs(getScrollX()) > scaledTouchSlop * 2) {
                    requestDisallowInterceptTouchEvent(true);
                }
                //滑动使用scrollBy
                scrollBy((int) (distanceX), 0);

                swipe();

                mLastP.set(ev.getRawX(), ev.getRawY());

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                LogcatUtil.getInstance().info("ACTION_UP or ACTION_CANCEL", this);

                ActionEvent actionEvent = getActionEvent();
                finalSwipe(actionEvent);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Log.d(TAG, "dispatchTouchEvent() called with: " + "ev = [" + event + "]");

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //对左边界进行处理
                float distance = mLastP.x - event.getRawX();
                if (Math.abs(distance) > scaledTouchSlop) {
                    // 当手指拖动值大于mScaledTouchSlop值时，认为应该进行滚动，拦截子控件的事件
                    return true;
                }
                break;

            }

        }
        return super.onInterceptTouchEvent(event);
    }

    public ActionEvent getActionEvent() {

        int scrollX = getScrollX();

        if (scrollX < 0 && mLeftView != null) { // right move

            if (Math.abs(mLeftView.getWidth() * fraction) < Math.abs(scrollX)) {
                LogcatUtil.getInstance().info("right move", this);
                return ACTION_LEFT;
            }
        } else if (scrollX > 0 && mRightView != null) { // left move

            if (Math.abs(mRightView.getWidth() * fraction) < Math.abs(scrollX)) {
                LogcatUtil.getInstance().info("left move", this);
                return ACTION_RIGHT;
            }
        }

        return ACTION_CANCEL;
    }

    private void swipe() {

        //越界修正
        if (getScrollX() < 0) {
            if (!isCanRightSwipe || mLeftView == null) {
                scrollTo(0, 0);
            } else {
                //左滑
                if (getScrollX() < mLeftView.getLeft()) {
                    scrollTo(mLeftView.getLeft(), 0);
                }
            }
        } else if (getScrollX() > 0) {
            if (!isCanLeftSwipe || mRightView == null) {
                scrollTo(0, 0);
            } else {
                if (getScrollX() > mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin) {
                    scrollTo(mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin, 0);
                }
            }
        }
    }

    public void finalSwipe(ActionEvent actionEvent) {

        int scrollX = getScrollX();

        switch (actionEvent) {

            case ACTION_LEFT:
                mScroller.startScroll(scrollX, 0, mLeftView.getLeft() - scrollX, 0);
                break;

            case ACTION_RIGHT:
                mScroller.startScroll(scrollX, 0, mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin - scrollX, 0);
                break;

            case ACTION_CANCEL:
                mScroller.startScroll(scrollX, 0, -scrollX, 0);
                break;

            default:
                LogcatUtil.getInstance().error("Note that other event!!");
                break;
        }

        mViewCache = this;
        mActionEvent = actionEvent;

        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        //判断Scroller是否执行完毕：
        if (mScroller != null && mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //通知View重绘-invalidate()->onDraw()->computeScroll()
            invalidate();
        }
    }

    public enum ActionEvent {
        ACTION_LEFT,
        ACTION_RIGHT,
        ACTION_CANCEL
    }

    /**
     * Left slip state
     *
     * @return
     */
    public boolean isCanLeftSwipe() {
        return isCanLeftSwipe;
    }

    /**
     * Left slide switch
     *
     * @param isCanLeftSwipe
     */
    public void setCanLeftSwipe(boolean isCanLeftSwipe) {
        this.isCanLeftSwipe = isCanLeftSwipe;
    }

    /**
     * Right slip state
     *
     * @return
     */
    public boolean isCanRightSwipe() {
        return isCanRightSwipe;
    }

    /**
     * Right slide switch
     *
     * @param isCanRightSwipe
     */
    public void setCanRightSwipe(boolean isCanRightSwipe) {
        this.isCanRightSwipe = isCanRightSwipe;
    }

    /**
     * Left or Right switch
     *
     * @param isCanLeftRightSwipe
     */
    public void setCanLeftRightSwipe(boolean isCanLeftRightSwipe) {

        setCanRightSwipe(isCanLeftRightSwipe);
        setCanLeftSwipe(isCanLeftRightSwipe);
    }
}
