package com.android.touristguide;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.jsibbold.zoomage.ZoomageView;

public class ZoomableImageView extends ZoomageView{
    private GestureDetector gestureDetector;
    private AppBarLayout appbar;

    public void setAppbar(AppBarLayout appbar) {
        this.appbar = appbar;
    }

    public ZoomableImageView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context,gestureListener);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context,gestureListener);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetector(context,gestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (appbar.getVisibility() == View.VISIBLE){
                appbar.animate().alpha(0.0f).setDuration(200).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        appbar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }else{
                appbar.animate().alpha(1.0f).setDuration(200).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        appbar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
            return false;
        }
    };
}
