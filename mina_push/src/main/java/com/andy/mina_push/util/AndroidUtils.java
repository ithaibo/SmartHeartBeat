package com.andy.mina_push.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Android相关的工具类.
 * 
 * @author john
 * 
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
public final class AndroidUtils
{

    /**
     * 公式的系数，像素= dpi/160*dp.
     */
    private static final int DP_RATIO = 160;


    /**
     * 获取当前app包信息对象.
     * 
     * @param context
     * @return
     * @throws NameNotFoundException
     */
    private static PackageInfo getCurrentAppPackageInfo(Context context)
    {
        try
        {
            PackageManager manager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            return info;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取手机的imei.
     * 
     * @param context
     * @return
     */
    public static String getDeviceId(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }

    /**
     * 获取手机显示数据.
     * 
     * @param activity
     *            活动对象
     * @return 手机手机显示数据
     */
    public static DisplayMetrics getDisplayMetrics(Activity activity)
    {
        if (activity != null)
        {
            DisplayMetrics metric = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
            return metric;
        }
        else
        {
            throw new RuntimeException("Activity must not be null.");
        }
    }

    /**
     * 获取安装app的意图.
     * 
     * @param apkFile
     * @return
     */
    public static Intent getInstallIntent(File apkFile)
    {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(apkFile), type);
        return intent;
    }

    /**
     * 从TextView获取整型值，如果获取的数据不能转换成预期的数据，则反回默认值.
     *
     * @param textView
     *            要取值的那个TextView组件
     * @param defaultValue
     *            默认值
     * @return 返回整型值
     */
    public static Integer getIntFromTextView(TextView textView, Integer defaultValue)
    {
        try
        {
            String trim = textView.getText().toString().trim();
            return Integer.valueOf(trim);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    /**
     * 根据uri获取图片的真实路径.
     *
     * @param activity
     *            活动对象
     * @param uri
     *            uri
     * @return 图片的真实路径
     */
    public static String getPath(Activity activity, Uri uri)
    {

        String[] proj =
        { MediaColumns.DATA };
        // 好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
        // 按我个人理解 这个是获得用户选择的图片的索引值
        int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        // 将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        // 最后根据索引值获取图片路径
        String path = cursor.getString(columnIndex);
        return path;
    }

    /**
     * 获取dp对应的像素.
     *
     * @param activity
     *            活动对象
     * @param dp
     *            dp值
     * @return 像素值
     */
    public static int getPixelByDp(Activity activity, int dp)
    {
        if (activity != null)
        {
            DisplayMetrics metric = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
            double dpi = metric.densityDpi;
            return (int) (dpi / DP_RATIO * dp);
        }
        else
        {
            throw new RuntimeException("Activity must not be null.");
        }
    }

    /**
     * 获取平台号.
     *
     * @return
     */
    public static String getPlatformNum()
    {
        return "Android " + getVersionRelease();
    }

    /**
     * 获取sd卡路径.
     *
     * @return sd card路径
     */
    public static File getSdCardPathFile()
    {
        if (isExternalStorageWritable())
        {
            return Environment.getExternalStorageDirectory();
        }
        else
        {
            throw new RuntimeException("SD卡不可用");
        }
    }

    /**
     * 获取系统版本.2.2对应是8.
     *
     * @return
     */
    public static int getSystemVersion()
    {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取view中的tag对象，并转换成指定的Java对象类型.
     *
     * @param view
     * @param clazz
     * @return
     */
    public static <T> T getTag(View view, Class<T> clazz)
    {
        Object tag = view.getTag();
        return (T) tag;
    }

    /**
     * 获取系统版本号.
     *
     * @return
     */
    public static String getVersionRelease()
    {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 安装apk文件.
     *
     * @param context
     * @param apkFile
     */
    public static void installApk(Context context, File apkFile)
    {
        Intent intent = new Intent();
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(apkFile), type);
        context.startActivity(intent);
    }

    /**
     * 外部存储是否可读，true代表可读，false代表不可读.
     *
     * @return
     */
    public static boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            return true;
        }
        return false;
    }

    /**
     * 外部存储是否可写(也可读)，true代表可写，false代表不可写.
     *
     * @return
     */
    public static boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }

    // 判断意图是否有效的
    public static boolean isIntentAvailable(Context context, String action)
    {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 判断当前网络是否处于2G/3G情况下.
     *
     * @param context
     *            上下文
     * @return 布尔值，true代表可用，false代表不可用
     */
    public static boolean isMobileNetworkValid(Context context)
    {
        return isPhoneNetworkValid(context, ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * 判断网络是否可用.
     *
     * @param context
     *            上下文
     * @return true代表网络可用，false代表网络不可用.
     */
    public static boolean isNetworkValid(Context context)
    {
        boolean result = false;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null)
        {
            result = false;
        }
        else
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info == null)
            {
                result = false;
            }
            else
            {
                if (info.isAvailable())
                {
                    result = true;
                }
            }
        }
        return result;
    }

    public static boolean isValidLocationNetwork(Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return providerEnabled;
    }

    public static boolean isValidLocation(Context context)
    {
        if (isValidLocationNetwork(context) || isValidLocationGps(context))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean isValidLocationGps(Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return providerEnabled;
    }

    public static boolean isPhoneNetworkValid(Context context, int type)
    {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(type).getState();
        if (mobile == State.CONNECTED || mobile == State.CONNECTING)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 用来判断服务是否后台运行.
     *
     * @param mContext
     *            上下文
     * @param className
     *            判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className)
    {
        ActivityManager myManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(className)) {
                return true;
            }
        }
        return false;
    }



    /**
     * 判断任务是否空闲.
     *
     * @param asyncTask
     *            异步任务
     * @return true代表是空闲的，false代表不是空闲的.
     */
    public static boolean isTaskIdle(AsyncTask asyncTask)
    {
        if (asyncTask == null || asyncTask.getStatus() == AsyncTask.Status.FINISHED)
        {
            return true;
        }
        return false;
    }

    /**
     * 判断当前网络是否处于wifi情况下.
     *
     * @param context
     *            上下文
     * @return 布尔值，true代表可用，false代表不可用
     */
    public static boolean isWifiNetworkValid(Context context)
    {
        return isPhoneNetworkValid(context, ConnectivityManager.TYPE_WIFI);
    }

    /**
     * 判断wifi网络是否可用.
     *
     * @param context
     *            上下文
     * @return true代表可用，false相反
     */
    public static boolean isWifiValid(Context context)
    {
        boolean result = false;
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = 0;
        if (wifiInfo != null)
        {
            ipAddress = wifiInfo.getIpAddress();
        }
        if (mWifiManager.isWifiEnabled() && ipAddress != 0)
        {
            result = true;
        }
        return result;
    }


    /**
     * 显示（时间是短暂的）土司消息
     *
     * @param context
     *            上下文
     * @param msg
     *            消息内容
     */
    public static void showToastMsg(Context context, String msg)
    {
        if (context != null && msg != null)
        {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }


}
