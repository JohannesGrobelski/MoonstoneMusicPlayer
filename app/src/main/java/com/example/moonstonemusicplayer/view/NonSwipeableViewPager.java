package com.example.moonstonemusicplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class NonSwipeableViewPager extends ViewPager {

    private boolean swipeEnabled;

    public NonSwipeableViewPager(Context context) {
        super(context);
        init();
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        swipeEnabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipeEnabled && super.onInterceptTouchEvent(event);
    }

    public void setSwipeEnabled(boolean enabled) {
        swipeEnabled = enabled;
    }
}
