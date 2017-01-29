package com.buzz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.buzz.activity.MyApplication;
import com.buzz.utils.GlobalConst.AppRunningState;

/**
 * Created by NickChung on 5/5/15.
 */
public class AppMonitorReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyApplication myApp = (MyApplication) context.getApplicationContext();
        myApp.appRunningState = (intent.getIntExtra("BOF", -1) == AppRunningState.FRONT.ordinal())
                ? AppRunningState.FRONT : AppRunningState.BACK;
    }
}