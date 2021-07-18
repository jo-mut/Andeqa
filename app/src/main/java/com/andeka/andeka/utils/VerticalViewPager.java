package com.andeka.andeka.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VerticalViewPager extends ViewPager {
    public VerticalViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setPageTransformer(true, new VerticalPageTransformer());
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private static final class VerticalPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(@NonNull View page, float position) {
            if (position < -1){
                //page always off screen to the left
                page.setAlpha(0);
            }else if (position <= 1){
                page.setAlpha(1);
                //counteract the default slide behavior
                page.setTranslationX(page.getWidth() * -position);

                // set y position to swipe in from top
                float yPosition = position * page.getHeight();
                page.setTranslationY(yPosition);
            }else {
                page.setAlpha(0);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final boolean toHandle = super.onTouchEvent(swapXY(ev));
        swapXY(ev);
        return toHandle;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = super.onInterceptTouchEvent(swapXY(ev));
        swapXY(ev);
        return intercepted;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollHorizontally(direction);
    }

    private MotionEvent swapXY(MotionEvent event){
        float width = getWidth();
        float height = getHeight();

        float newX = event.getY() / height * width;
        float newY = event.getX() / width * height;

        event.setLocation(newX, newY);

        return event;
    }

}
