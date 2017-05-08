package com.andy.mina_push.util;


import com.andy.mina_push.BuildConfig;

/**
 * Created by Andy on 2017/5/4.
 */

public class Constants {
    public final static String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String PPLINK_ALARM_ON = APP_PACKAGE_NAME + ".ALARM_ON";
    public static final String PPLINK_ALARM_OFF = APP_PACKAGE_NAME + ".ALARM_OFF";
    public static final String PPLINK_HEART_BEAT_MODE = APP_PACKAGE_NAME + ".HEART_BEAT";

    //-----------NAT ABOUT----------------------------
    /**最近一次NAT试探时间*/
    public static final String KEY_SP_LAST_NAT_TIME = "LAST_NAT_TIME";
    /**默认值KEY_SP_LAST_NAT_TIME*/
    public static final long DEFAULT_NAT_TIME = 0;
    /**NAT数据保存的SharedPreference文件Name*/
    public static final String NAME_SP_NAT = "NAT_RECORD";
    /**相邻两次NAT试探周期   (默认为1周)*/
//    public static final long VALID_NAT_TRACE_PEROID = 24 * 60 * 60 * 1000L;
    public static final long VALID_NAT_TRACE_PEROID = 1 * 60 * 1000L;
    //-----------NAT ABOUT----------------------------
}
