package com.buzz.utils;

import android.os.Environment;

/**
 * Created by NickChung on 2/14/15.
 */
public class GlobalConst {

    public static final String CMD_GET_BEACON_INFO_LIST = "00";
    public static final String CMD_GET_BEACON_INFO = "01 %s,%s";//{major,minor}
    public static final String CMD_GET_SYS_PARAMS = "02";
    public static final String CMD_GET_USER_INFO = "03 %s,%s";//{userid,password}
    public static final String CMD_ADD_SYS_LOG = "04 %s,%s,%s,%s";//{userid,beaconid,logtime,triggertype}
    public static final String CMD_ADD_USER = "05 %s,%s,%s,%s,%s";//{userid(email),password,nickname,voicelang,defaultlang}
    public static final String CMD_FIND_USER = "06 %s";//{userid}
    public static final String CMD_GET_EXTAG_LIST = "07";
    public static final String CMD_OPEN = "90";
    public static final String CMD_CLOSE = "91";

    public static final int FOREGROUND_SCAN_PERIOD = 500;
    public static final int BACKGROUND_SCAN_PERIOD = 500;
    public static final int RSSI_SMOOTH_STEP = 6;
    public static final String SOCKET_HOST = "things.buzz";
    public static final int SOCKET_PORT = 2397;
    public static final int SYSTEM_EXIT_INTERVAL = 3000;
    public static final String PATH_SDCARD = Environment.getExternalStorageDirectory() + "/";
    public static final String PATH_SYSTEM = "com.buzz.exhibition/";
    public static final String PATH_SAVE = "com.buzz.exhibition/config/";
    public static final String PATH_APK = "com.buzz.exhibition/apk/";
    public static final String FILENAME_BEACON_INFO_LIST = "beaconinfolist.txt";
    public static final String FILENAME_USER_INFO = "userinfo.txt";
    public static final String FILENAME_EXTAG_LIST = "extaglist.txt";
    public static final String FILENAME_FAVORITE_LIST = "favorite.txt";

    public static final String VOICE_LANG_CC = "1";
    public static final String VOICE_LANG_SC = "2";
    public static final String VOICE_LANG_EN = "3";
    public static final String VOICE_LANG_PT = "4";
    public static final String VOICE_LANG_ARTIST = "0";

    public static final String DEFAULT_LANG_TW = "0";
    public static final String DEFAULT_LANG_CN = "1";
    public static final String DEFAULT_LANG_EN = "2";
    public static final String DEFAULT_LANG_PT = "3";

    public static final String TRIGGER_TYPE_IN = "0";
    public static final String TRIGGER_TYPE_OUT = "1";
    public static final String TRIGGER_TYPE_STAY = "2";
    public static final String TRIGGER_TYPE_THROUGH = "3";

    public static final String AUTO_PLAY_ON = "1";
    public static final String AUTO_PLAY_OFF = "0";

    public static final String EARPHONE_PLAY_ON = "1";
    public static final String EARPHONE_PLAY_OFF = "0";

    /*
    public static enum VoiceLang {
        CC, SC, EN, PT, NONE
    }

    public static enum DisplayLang {
        TW, CN, EN, PT, NONE
    }

    public static enum TriggerType {
        IN, OUT, STAY, THROUGH, NONE
    }*/

    public static enum AppRunningState {
        FRONT, BACK, NONE
    }

    public static final String ACTION_APP_RUNNING_MONITOR = "android.intent.action.MY_APP_MONITOR";
    public static final String ACTION_MY_APP_SERVICE = "android.intent.action.MY_APP_SERVICE";
    public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
    public static final String ACTION_USER_LOGIN = "android.intent.action.MY_APP_LOGIN";
    public static final String ACTION_USER_SIGN_UP = "android.intent.action.MY_APP_SIGN_UP";
    public static final String ACTION_USER_FIND = "android.intent.action.MY_APP_FIND_USER";
    public static final String ACTION_STOP_MEDIA = "android.intent.action.MY_APP_STOP_MEDIA";
    public static final String LOCK_SCREEN_ACTION = "android.intent.My_LOCK_SCREEN";

    public static final int ANIMATION_DURATION = 300;
    public static final int ANIMATION_INTERVAL = 5;
    public static final int ANIMATION_ELEMENT_LIMITED = 5;

    public static final int HEX_COLOR_LEVEL_01 = 0xffFE9375;
    public static final int HEX_COLOR_LEVEL_02 = 0xff1DD5C0;
    public static final int HEX_COLOR_LEVEL_03 = 0xff00A8A7;
    public static final int HEX_COLOR_LEVEL_04 = 0xff698686;

    //从sysconfig步长算出
    public static final int RSSI_COLOR_LEVEL_01 = -10;
    public static final int RSSI_COLOR_LEVEL_02 = -12;
    public static final int RSSI_COLOR_LEVEL_03 = -15;
    public static final int RSSI_COLOR_LEVEL_04 = -18;

    public static final String RANGE_DIRECTION_FRONT = "0";
    public static final String RANGE_DIRECTION_BACK = "1";
    public static final String RANGE_DIRECTION_BOTH = "2";

    public static final String BEACON_USAGE_INTRO = "0";
    public static final String BEACON_USAGE_DETAIL = "1";

    //从sysconfig读取
    public static final int RANGE_ACCESS_BALL_VIEW = -90;
    public static final int RANGE_QUIT_BALL_VIEW = -90;

    public static final int CONTENT_TYPE_IMAGE = 2;
    public static final int CONTENT_TYPE_AUDIO = 1;

    public static final String CONTENT_USAGE_CATALOG = "0";
    public static final String CONTENT_USAGE_PAINT = "1";

    public static final int SPECIAL_VERSION = 105;
}
