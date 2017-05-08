package com.andy.mina_push.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Andy on 2017/5/8.
 */

public class DateUtils {
    /**
     * 获取东八区当前时间，将MINUTE、HOUR_OF_DAY、HOUR_OF_DAY和MILLISECOND设置为0.
     * @return
     */
    public static long getTimeMilisM0H0S0MM0() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }


}
