package com.android.touristguide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DisabledConstrainLayout extends ConstraintLayout {
    private boolean disabled;
    public DisabledConstrainLayout(@NonNull Context context) {
        super(context);
        disabled = true;
    }

    public DisabledConstrainLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        disabled = true;
    }

    public DisabledConstrainLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        disabled = true;

    }

    public DisabledConstrainLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        disabled = true;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
