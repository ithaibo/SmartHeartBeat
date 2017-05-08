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
    public static long getTimeMilisGMT8() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return calendar.getTimeInMillis();
    }


}
