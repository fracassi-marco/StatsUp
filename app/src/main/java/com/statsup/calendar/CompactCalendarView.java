package com.statsup.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.statsup.Activity;
import com.statsup.R;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CompactCalendarView extends View {

    private final CompactCalendarController compactCalendarController;

    public CompactCalendarView(Context context) {
        this(context, null);
    }

    public CompactCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompactCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        compactCalendarController = new CompactCalendarController(context.getColor(R.color.colorPrimary), new Rect(), Locale.getDefault(), TimeZone.getDefault());
    }

    public void addEvents(List<Activity> events){
        compactCalendarController.addEvents(events);
        invalidate();
    }

    @Override
    protected void onMeasure(int parentWidth, int parentHeight) {
        super.onMeasure(parentWidth, parentHeight);
        int width = MeasureSpec.getSize(parentWidth);
        int height = MeasureSpec.getSize(parentHeight);
        if(width > 0 && height > 0) {
            compactCalendarController.onMeasure(width, height, getPaddingRight(), getPaddingLeft());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        compactCalendarController.onDraw(canvas);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

}
