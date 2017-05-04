package com.andy.mina_push.util;

import android.content.Context;
import android.net.ConnectivityManager;


/**
 * Created by neal on 2014/12/2.
 */
public class NetworkUtil {
    public static boolean isNetworkConnect(Context c){
        if(c.getApplicationContext()==null){
            return false;
        }
        ConnectivityManager connectivityManager= (ConnectivityManager)c.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null) {
            if(connectivityManager.getActiveNetworkInfo()!=null && connectivityManager.getActiveNetworkInfo().isConnected()){
                return true;
            }
        }
        return false;
    }
}
