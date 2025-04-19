package com.haibin.calendarview;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Date;

/**
 * xml中预览使用的控件
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2021/10/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
public class PreviewMonthView extends DefaultMonthView {

    public PreviewMonthView(Context context) {
        super(context);
    }

    public PreviewMonthView(Context context, AttributeSet attrs) {
        super(context);

        if (isInEditMode()) {

            setup(new CalendarViewDelegate(context, attrs));

            Calendar mCurrentDate = new Calendar();
            Date d = new Date();
            mCurrentDate.setYear(CalendarUtil.getDate("yyyy", d));
            mCurrentDate.setMonth(CalendarUtil.getDate("MM", d));
            mCurrentDate.setDay(CalendarUtil.getDate("dd", d));
            mCurrentDate.setCurrentDay(true);
            LunarCalendar.setupLunarCalendar(mCurrentDate);

            initMonthWithDate(mCurrentDate.getYear(), mCurrentDate.getMonth());
        }
    }
}
