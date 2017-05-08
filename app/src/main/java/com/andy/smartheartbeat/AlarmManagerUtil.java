package com.andy.smartheartbeat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Andy on 2017/5/5.
 */

public class AlarmManagerUtil {

    public static void addAlarmTask(Context context, Class<?> cls, String action) {
        if (context == null){
            return;
        }
        if (cls == null) {
            return;
        }
        if (TextUtils.isEmpty(action)) {
            return;
        }

        if (isTaskExistedInAmarmManager(context, action)){
            //alarm existed
            Log.i(AlarmManagerUtil.class.getSimpleName(),
                    "an alarm with action, " + action + " is existed.");
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerTime = SystemClock.elapsedRealtime();
//        long interval = 7 * 24 * 60 * 60 *1000;
        long interval = 3 * 60 *1000;

        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerTime, interval, pendingIntent);
    }

    public static boolean isTaskExistedInAmarmManager(Context context, String action) {
        boolean existed =  null!= PendingIntent.getService(context, 0,
                new Intent(action),
                PendingIntent.FLAG_NO_CREATE);

        return existed;
    }
}
