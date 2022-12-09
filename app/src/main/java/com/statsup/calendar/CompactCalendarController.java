package com.statsup.calendar;

import static java.util.Calendar.MONDAY;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.statsup.Activity;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CompactCalendarController {

    private static final int DAYS_IN_WEEK = 7;
    private int paddingWidth = 40;
    private int paddingHeight = 40;
    private int widthPerDay;
    private int heightPerDay;
    private int width;
    private int height;
    private int paddingRight;
    private int paddingLeft;
    private float bigCircleIndicatorRadius;
    private final Date currentDate = new Date();
    private List<Activity> activities;
    private final Calendar calendarWithFirstDayOfMonth;
    private final Calendar eventsCalendar;
    private final PointF accumulatedScrollOffset = new PointF();
    private final int color;
    private final Paint dayPaint;
    private final Rect textSizeRect;
    private String[] dayColumnNames;

    CompactCalendarController(int color, Rect textSizeRect, Locale locale, TimeZone timeZone) {
        this.color = color;
        this.textSizeRect = textSizeRect;
        calendarWithFirstDayOfMonth = Calendar.getInstance(timeZone, locale);
        eventsCalendar = Calendar.getInstance(timeZone, locale);
        eventsCalendar.setMinimalDaysInFirstWeek(1);
        calendarWithFirstDayOfMonth.setMinimalDaysInFirstWeek(1);

        setFirstDayOfWeek(locale);

        dayPaint = new Paint();
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setStyle(Paint.Style.STROKE);
        dayPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTypeface(Typeface.SANS_SERIF);
        int textSize = 30;
        dayPaint.setTextSize(textSize);
        dayPaint.getTextBounds("31", 0, "31".length(), textSizeRect);

        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate);
    }

    private void setCalenderToFirstDayOfMonth(Calendar calendarWithFirstDayOfMonth, Date currentDate) {
        setMonthOffset(calendarWithFirstDayOfMonth, currentDate);
        calendarWithFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
    }

    private void setMonthOffset(Calendar calendarWithFirstDayOfMonth, Date currentDate) {
        calendarWithFirstDayOfMonth.setTime(currentDate);
        calendarWithFirstDayOfMonth.add(Calendar.MONTH, 0);
        calendarWithFirstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        calendarWithFirstDayOfMonth.set(Calendar.MINUTE, 0);
        calendarWithFirstDayOfMonth.set(Calendar.SECOND, 0);
        calendarWithFirstDayOfMonth.set(Calendar.MILLISECOND, 0);
    }

    private void setFirstDayOfWeek(Locale locale){
        setUseWeekDayAbbreviation(locale);
        eventsCalendar.setFirstDayOfWeek(MONDAY);
        calendarWithFirstDayOfMonth.setFirstDayOfWeek(MONDAY);
    }

    private void setUseWeekDayAbbreviation(Locale locale) {
        dayColumnNames = getWeekdayNames(locale);
    }

    private String[] getWeekdayNames(Locale locale){
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        String[] dayNames = dateFormatSymbols.getShortWeekdays();
        String[] weekDayNames = new String[7];
        String[] weekDaysFromSunday = {dayNames[1], dayNames[2], dayNames[3], dayNames[4], dayNames[5], dayNames[6], dayNames[7]};
        for (int currentDay = MONDAY - 1, i = 0; i <= 6; i++, currentDay++) {
            currentDay = currentDay >= 7 ? 0 : currentDay;
            weekDayNames[i] = weekDaysFromSunday[currentDay];
        }

        for (int i = 0; i < weekDayNames.length; i++) {
            weekDayNames[i] = weekDayNames[i].substring(0, 1);
        }

        return weekDayNames;
    }

    void onMeasure(int width, int height, int paddingRight, int paddingLeft) {
        widthPerDay = (width) / DAYS_IN_WEEK;
        heightPerDay = height / 7;
        this.width = width;
        this.height = height;
        this.paddingRight = paddingRight;
        this.paddingLeft = paddingLeft;
        bigCircleIndicatorRadius = getInterpolatedBigCircleIndicator();
    }

    private float getInterpolatedBigCircleIndicator() {
        float x0 = textSizeRect.height();
        float x =  ((float) heightPerDay + textSizeRect.height()) / 2f;
        double y1 = 0.5 * Math.sqrt(((float) heightPerDay * (float) heightPerDay) + ((float) heightPerDay * (float) heightPerDay));
        double y0 = 0.5 * Math.sqrt((x0 * x0) + (x0 * x0));

        return (float) (y0 + ((y1 - y0) * ((x - x0) / ((float) heightPerDay - x0))));
    }

    void onDraw(Canvas canvas) {
        paddingWidth = widthPerDay / 2;
        paddingHeight = heightPerDay / 2;
        drawCalenderBackground(canvas);
        drawCalender(canvas);
    }

    void addEvents(List<Activity> events) {
        activities = events;
    }

    private void drawCalender(Canvas canvas) {
        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate);
        drawMonth(canvas, calendarWithFirstDayOfMonth);
    }

    private void drawCalenderBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);
    }

    void drawEvents(Canvas canvas) {
        for (int i = 0; i < activities.size(); i++) {
            Activity events = activities.get(i);
            long timeMillis = events.getDateInMillis();
            eventsCalendar.setTimeInMillis(timeMillis);

            int dayOfWeek = getDayOfWeek(eventsCalendar);

            int weekNumberForMonth = eventsCalendar.get(Calendar.WEEK_OF_MONTH);
            float xPosition = widthPerDay * dayOfWeek + paddingWidth + paddingLeft + accumulatedScrollOffset.x - paddingRight;
            float yPosition = weekNumberForMonth * heightPerDay + paddingHeight;

            drawEventIndicatorCircle(canvas, xPosition, yPosition);
        }
    }

    int getDayOfWeek(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - MONDAY;
        dayOfWeek = dayOfWeek < 0 ? 7 + dayOfWeek: dayOfWeek;
        return dayOfWeek;
    }

    void drawMonth(Canvas canvas, Calendar monthToDrawCalender) {
        drawEvents(canvas);

        int firstDayOfMonth = getDayOfWeek(monthToDrawCalender);

        for (int dayColumn = 0, colDirection = 0, dayRow = 0; dayColumn <= 6; dayRow++) {
            if (dayRow == 7) {
                colDirection++;
                dayRow = 0;
                dayColumn++;
            }
            if (dayColumn == dayColumnNames.length) {
                break;
            }
            float xPosition = widthPerDay * dayColumn + paddingWidth + paddingLeft + accumulatedScrollOffset.x + 0 - paddingRight;
            float yPosition = dayRow * heightPerDay + paddingHeight;
            dayPaint.setColor(Color.BLACK);
            if (dayRow == 0) {
                dayPaint.setTypeface(Typeface.DEFAULT_BOLD);
                dayPaint.setStyle(Paint.Style.FILL);
                canvas.drawText(dayColumnNames[colDirection], xPosition, paddingHeight, dayPaint);
                dayPaint.setTypeface(Typeface.DEFAULT);
            } else {
                int day = ((dayRow - 1) * 7 + colDirection + 1) - firstDayOfMonth;
                int maximumMonthDay = monthToDrawCalender.getActualMaximum(Calendar.DAY_OF_MONTH);
                if(day > 0 && day <= maximumMonthDay) {
                    dayPaint.setStyle(Paint.Style.FILL);
                    canvas.drawText(String.valueOf(day), xPosition, yPosition, dayPaint);
                }
            }
        }
    }

    private void drawEventIndicatorCircle(Canvas canvas, float x, float y) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        int textHeight = textSizeRect.height() * 3;
        canvas.drawCircle(x, y - (textHeight / 6f), bigCircleIndicatorRadius, paint);
    }
}
