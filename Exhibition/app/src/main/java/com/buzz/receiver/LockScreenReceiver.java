package com.buzz.receiver;

/**
 * Lock Screen Receiver
 * Created by NickChung on 8/8/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.buzz.activity.MyApplication;
import com.buzz.utils.GlobalConst;

public class LockScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyApplication myApp=(MyApplication)context.getApplicationContext();
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (myApp.access) {
                Intent lockIntent = new Intent(GlobalConst.LOCK_SCREEN_ACTION);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                context.startActivity(lockIntent);
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // do other things if you need
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // do other things if you need
        }
    }
}
