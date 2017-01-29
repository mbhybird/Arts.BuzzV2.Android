package com.buzz.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.buzz.activity.MyApplication;

/**
 * Created by NickChung on 10/8/16.
 */
public class PhoneBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = PhoneBroadcastReceiver.class.getSimpleName();
    private static String mIncomingNumber = null;
    MyApplication myApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        myApp = (MyApplication)context.getApplicationContext();
        // 如果是拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i(TAG, "call OUT:" + phoneNumber);
            myApp.stopSound();
            myApp.phoneCallState = true;

        } else {
            // 如果是来电
            TelephonyManager tManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            switch (tManager.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:
                    mIncomingNumber = intent.getStringExtra("incoming_number");
                    Log.i(TAG, "RINGING :" + mIncomingNumber);
                    myApp.stopSound();
                    myApp.phoneCallState = true;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(TAG, "incoming ACCEPT :" + mIncomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(TAG, "incoming IDLE");
                    myApp.phoneCallState = false;
                    break;
            }
        }
    }
}