package com.buzz.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.buzz.activity.*;
import com.buzz.models.*;
import com.buzz.receiver.HeadSetUtil;
import com.buzz.utils.*;
import com.buzz.receiver.AppMonitorReceiver;

import java.util.*;

/**
 * Created by NickChung on 21/01/2015.
 */
public class MessageService extends Service {

    private static final String TAG = MessageService.class.getSimpleName();

    private NotificationManager manager;
    private MyApplication myApp;
    private HeadsetPlugReceiver headsetPlugReceiver;
    private AppMonitorReceiver appMonitorReceiver;
    private BeaconReceiver myBeaconReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        registerHeadsetPlugReceiver();
        registerAppMonitorReceiver();
        registerBeaconReceiver();

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //注册耳机事件
        HeadSetUtil.getInstance().setOnHeadSetListener(new HeadSetUtil.OnHeadSetListener() {
            @Override
            public void onClick() {
                Intent it = new Intent(GlobalConst.ACTION_STOP_MEDIA);
                it.putExtra("action", "sc");
                sendBroadcast(it);
            }

            @Override
            public void onDoubleClick() {
                Intent it = new Intent(GlobalConst.ACTION_STOP_MEDIA);
                it.putExtra("action", "dc");
                sendBroadcast(it);
            }

            @Override
            public void onTripleClick() {
                Intent it = new Intent(GlobalConst.ACTION_STOP_MEDIA);
                it.putExtra("action", "tc");
                sendBroadcast(it);
            }
        });

        HeadSetUtil.getInstance().open(this);
    }

    private void showNotification(String beaconid) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("bid", beaconid);
        resultIntent.putExtras(bundle);
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Iterator<content> it = myApp.actionList.get(beaconid).iterator();

        extag exTag = (extag)myApp.extagList.values().toArray()[myApp.currentExTagIndex];

        String exTitle = "";
        String title = "";

        while (it.hasNext()) {
            content c = it.next();
            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                switch (myApp.logonUser.defaultLang) {
                    case GlobalConst.DEFAULT_LANG_EN:
                        title = c.getTitle_en();
                        exTitle = exTag.getTitle_en();
                        break;
                    case GlobalConst.DEFAULT_LANG_CN:
                        title = c.getTitle_cn();
                        exTitle = exTag.getTitle_cn();
                        break;
                    case GlobalConst.DEFAULT_LANG_TW:
                        title = c.getTitle_tw();
                        exTitle = exTag.getTitle_tw();
                        break;
                    case GlobalConst.DEFAULT_LANG_PT:
                        title = c.getTitle_pt();
                        exTitle = exTag.getTitle_pt();
                        break;
                }
            }
        }

        Notification notification = new Notification.Builder(getApplicationContext())
                .setLargeIcon(ImageHelper.readBitMap(getApplicationContext(), R.drawable.icon_logo))
                .setSmallIcon(R.drawable.icon_logo)
                .setTicker(getString(R.string.app_name))
                .setContentInfo("@")
                .setContentTitle(exTitle)
                .setContentText(title)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build();
        manager.notify(0, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myApp = (MyApplication) getApplication();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        unregisterReceiver(headsetPlugReceiver);
        unregisterReceiver(appMonitorReceiver);
        unregisterReceiver(myBeaconReceiver);

        HeadSetUtil.getInstance().close(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void registerAppMonitorReceiver() {
        appMonitorReceiver = new AppMonitorReceiver();
        IntentFilter filter = new IntentFilter(GlobalConst.ACTION_APP_RUNNING_MONITOR);
        registerReceiver(appMonitorReceiver, filter);
    }

    private void registerHeadsetPlugReceiver() {
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter filter = new IntentFilter(GlobalConst.ACTION_HEADSET_PLUG);
        registerReceiver(headsetPlugReceiver, filter);
    }

    public class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {
                    Log.i(TAG, "headset not connected");
                    myApp.headSetConnected = false;
                    myApp.stopSound();
                } else if (intent.getIntExtra("state", 0) == 1) {
                    Log.i(TAG, "headset connected");
                    myApp.headSetConnected = true;
                }
            }
        }
    }

    private void registerBeaconReceiver() {
        myBeaconReceiver = new MyBeaconReceiver();
        IntentFilter filter = new IntentFilter(CoreService.ACTION_BEACON_SEND);
        registerReceiver(myBeaconReceiver, filter);
    }

    private class MyBeaconReceiver extends BeaconReceiver {
        /*
        @Override
        public void Do(Map<String, BeaconReader> map) {
            if (myApp.stopServiceMedia) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlayEnd = true;
                    myApp.stopServiceMedia = false;
                }

                myApp.stopServiceMedia = false;
            }

            for (BeaconReader beaconReader : map.values()) {
                beacon b = myApp.beaconList.get(beaconReader.getBeaconId());

                int rangeIn = -1;
                if (b != null) {
                    //获取当前信号状态，前台或后台或所有
                    if (myApp.appRunningState.equals(GlobalConst.AppRunningState.FRONT)) {
                        //如果是前台读取，则取前台对应的Beacon响应值
                        if (b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_FRONT)
                                || b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BOTH)) {
                            rangeIn = b.getEffectiverangein();
                        }
                    } else if (myApp.appRunningState.equals(GlobalConst.AppRunningState.BACK)) {
                        //如果是后台读取，则取后台对应的Beacon响应值
                        if (b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BACK)
                                || b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BOTH)) {
                            rangeIn = b.getEffectiverangein();
                        }
                    }
                }

                if (beaconReader.getRssi() >= rangeIn) {
                    currentPlay = beaconReader.getBeaconId();
                    state = "In";

                    //写日志，一进一出，配对出现
                    if (!myApp.beaconStateList.containsKey(currentPlay)) {
                        if(!lastPlay.equals(currentPlay)) {
                            showNotification(currentPlay);
                        }
                        myApp.beaconStateList.put(currentPlay, GlobalConst.TRIGGER_TYPE_IN);
                        String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        //连接上服务器才写日志
                        if (myApp.serverReady) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("userid", myApp.logonUser.userId);
                                jsonObject.put("beaconid", currentPlay);
                                jsonObject.put("logtime", dt);
                                jsonObject.put("triggertype", GlobalConst.TRIGGER_TYPE_IN);
                                jsonObject.put("extag", myApp.beaconList.get(currentPlay).getExtag());
                                ConfigHelper.getInstance(getApplicationContext()).addSysLog(jsonObject);
                            } catch (JSONException e) {
                            }
                            //myApp.socketHelper.Send(String.format(GlobalConst.CMD_ADD_SYS_LOG, myApp.logonUser.userId, currentPlay, dt, GlobalConst.TRIGGER_TYPE_IN));
                        }
                    } else {
                        if (myApp.beaconStateList.get(currentPlay) != GlobalConst.TRIGGER_TYPE_IN) {
                            if(!lastPlay.equals(currentPlay)) {
                                showNotification(currentPlay);
                            }
                            myApp.beaconStateList.put(currentPlay, GlobalConst.TRIGGER_TYPE_IN);
                            String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            //连接上服务器才写日志
                            if (myApp.serverReady) {
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("userid", myApp.logonUser.userId);
                                    jsonObject.put("beaconid", currentPlay);
                                    jsonObject.put("logtime", dt);
                                    jsonObject.put("triggertype", GlobalConst.TRIGGER_TYPE_IN);
                                    jsonObject.put("extag", myApp.beaconList.get(currentPlay).getExtag());
                                    ConfigHelper.getInstance(getApplicationContext()).addSysLog(jsonObject);
                                } catch (JSONException e) {
                                }
                                //myApp.socketHelper.Send(String.format(GlobalConst.CMD_ADD_SYS_LOG, myApp.logonUser.userId, currentPlay, dt, GlobalConst.TRIGGER_TYPE_IN));
                            }
                        }
                    }

                }

                int rangeOut = -100;
                if (b != null) {
                    //获取当前信号状态，前台或后台或所有
                    if (myApp.appRunningState.equals(GlobalConst.AppRunningState.FRONT)) {
                        //如果是前台读取，则取前台对应的Beacon响应值
                        if (b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_FRONT)
                                || b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BOTH)) {
                            rangeOut = b.getEffectiverangeout();
                        }
                    } else if (myApp.appRunningState.equals(GlobalConst.AppRunningState.BACK)) {
                        //如果是后台读取，则取后台对应的Beacon响应值
                        if (b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BACK)
                                || b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BOTH)) {
                            rangeOut = b.getEffectiverangeout();
                        }
                    }
                }

                //写日志，一进一出，配对出现
                if (beaconReader.getRssi() < rangeOut) {
                    if (myApp.beaconStateList.containsKey(beaconReader.getBeaconId())) {
                        if (myApp.beaconStateList.get(beaconReader.getBeaconId()) != GlobalConst.TRIGGER_TYPE_OUT) {
                            myApp.beaconStateList.put(beaconReader.getBeaconId(), GlobalConst.TRIGGER_TYPE_OUT);
                            String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            //连接上服务器才写日志
                            if (myApp.serverReady) {
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("userid", myApp.logonUser.userId);
                                    jsonObject.put("beaconid", beaconReader.getBeaconId());
                                    jsonObject.put("logtime", dt);
                                    jsonObject.put("triggertype", GlobalConst.TRIGGER_TYPE_OUT);
                                    jsonObject.put("extag", myApp.beaconList.get(beaconReader.getBeaconId()).getExtag());
                                    ConfigHelper.getInstance(getApplicationContext()).addSysLog(jsonObject);
                                } catch (JSONException e) {
                                }
                                //myApp.socketHelper.Send(String.format(GlobalConst.CMD_ADD_SYS_LOG, myApp.logonUser.userId, beaconReader.getBeaconId(), dt, GlobalConst.TRIGGER_TYPE_OUT));
                            }
                        }
                    }
                }
            }

            //最后响应Beacon
            if (map.containsKey(lastPlay)) {
                int rangeOut = -100;
                beacon b = myApp.beaconList.get(lastPlay);
                if (b != null) {
                    if (myApp.appRunningState.equals(GlobalConst.AppRunningState.FRONT)) {
                        //如果是前台读取，则取前台对应的Beacon响应值
                        if (b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_FRONT)
                                || b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BOTH)) {
                            rangeOut = b.getEffectiverangeout();
                        }
                    } else if (myApp.appRunningState.equals(GlobalConst.AppRunningState.BACK)) {
                        //如果是后台读取，则取后台对应的Beacon响应值
                        if (b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BACK)
                                || b.getRangedirection().equals(GlobalConst.RANGE_DIRECTION_BOTH)) {
                            rangeOut = b.getEffectiverangeout();
                        }
                    }
                }

                //如果超出范围则停止播放音频
                if (map.get(lastPlay).getRssi() < rangeOut) {
                    state = "Out";
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        isPlayEnd = true;
                    }
                }
            }

            //设置广播值返回给前台Activity
            //Intent intent = new Intent(GlobalConst.ACTION_MY_APP_SERVICE);
            //intent.putExtra("map", (Serializable) seqMap);
            //intent.putExtra("bid", currentPlay);
            //intent.putExtra("state", state);

            if (currentPlay != "") {
                if (!myApp.playHistoryList.containsKey(currentPlay)) {
                    myApp.playHistoryList.put(currentPlay, new PlayHistory());
                    if (currentPlay != lastPlay) {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                            isPlayEnd = true;
                        }
                    }
                    if (isPlayEnd) {
                        //showNotification(currentPlay);
                        mediaPlayer = new MediaPlayer();
                        lastPlay = currentPlay;
                        currentPlay = "";
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                // TODO Auto-generated method stub
                                mediaPlayer.release();
                                mediaPlayer = null;
                                isPlayEnd = true;
                            }
                        });


                        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                // TODO Auto-generated method stub
                                mediaPlayer.release();
                                mediaPlayer = null;
                                isPlayEnd = true;
                                return true;
                            }
                        });

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                if (myApp.headSetConnected) {
                                    mediaPlayer.start();
                                    isPlayEnd = false;
                                }
                            }
                        });

                        try {

                            String audioPath = "";
                            String imagePath = "";
                            String imageDesc = "";

                            Iterator<content> it = myApp.actionList.get(lastPlay).iterator();

                            while (it.hasNext()) {
                                content c = it.next();
                                if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                                    imagePath = GlobalConst.PATH_SDCARD.concat(c.getClientpath()).concat(c.getFilename());
                                    imageDesc = c.getDescription_cn();
                                } else if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                    audioPath = GlobalConst.PATH_SDCARD.concat(c.getClientpath()).concat(c.getFilename());
                                }
                            }

                            myApp.playHistoryList.get(lastPlay).audioPath = audioPath;
                            myApp.playHistoryList.get(lastPlay).imagePath = imagePath;
                            myApp.playHistoryList.get(lastPlay).imageDesc = imageDesc;

                            String suffix = "";
                            switch (myApp.logonUser.voiceLang) {
                                case GlobalConst.VOICE_LANG_CC:
                                    suffix = "_cc.mp3";
                                    break;

                                case GlobalConst.VOICE_LANG_SC:
                                    suffix = "_sc.mp3";
                                    break;

                                case GlobalConst.VOICE_LANG_EN:
                                    suffix = "_en.mp3";
                                    break;

                                case GlobalConst.VOICE_LANG_PT:
                                    suffix = "_pt.mp3";
                                    break;
                            }
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(audioPath.replace(".mp3", suffix));
                            mediaPlayer.prepare();

                        } catch (Exception ex) {
                            Log.i(TAG, ex.toString());
                        }
                    }
                }
            }

            //intent.putExtra("playEnd", isPlayEnd);
            //sendBroadcast(intent);
        }*/
    }
}
