package com.buzz.activity;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.buzz.models.LogonUser;
import com.buzz.models.PlayHistory;
import com.buzz.models.beacon;
import com.buzz.models.download;
import com.buzz.models.content;
import com.buzz.models.extag;
import com.buzz.models.favorite;
import com.buzz.models.excontent;
import com.buzz.models.UserInfo;
import com.buzz.models.triggercontent;
import com.buzz.service.BeaconReader;
import com.buzz.service.CoreService;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.FileHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.GlobalConst.AppRunningState;
import com.buzz.utils.SocketHelper;
import com.buzz.utils.VersionUpdateHelper;
import com.buzz.utils.WebAPIHelper;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by NickChung on 3/17/15.
 */
public class MyApplication extends Application {

    boolean doorOpen;
    boolean wifiReady;
    boolean btReady;
    public boolean serverReady;
    public boolean headSetConnected;
    public boolean stopServiceMedia;
    Socket socket;
    public SocketHelper socketHelper;
    public ObjectMapper objectMapper;
    public FileHelper fileHelper;
    public LogonUser logonUser;
    public Map<String, beacon> beaconList;
    public Map<String, List<content>> actionList;
    public Map<String, PlayHistory> playHistoryList;
    public Map<String, String> beaconStateList;
    public AppRunningState appRunningState;
    public float scaleVector;
    public Map<String, extag> extagList;
    public int currentExTagIndex;
    public int initTime;
    public List<download> downloadList;
    public Map<String, BeaconReader> readerMap;
    public boolean access;
    public boolean downloading;
    public Map<String, content> contentList;
    public Map<String, String> oldCatalogVersionList;
    public Map<String, String> newCatalogVersionList;
    VersionUpdateHelper versionUpdateHelper;
    public String lastPlayingId = "";
    MediaPlayer mediaPlayer;
    public static final String APP_ID = "wx7352b6799da2cf9e";
    public static final String SECURE = "d4624c36b6795d1d99dcf0547af5443d";
    public static final String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    public static final String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
    public static final String sharePageUrl = "http://arts.things.buzz/Share.html?refImageId=%s&locale=%s";
    public IWXAPI wxapi;
    public static Handler wxHandler;
    public int wxLoginResultCode = -100;
    public static final String TAG = "MyApplication";
    boolean isSoundPrepare = false;
    public String fbName = "";
    public boolean phoneCallState = false;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        versionUpdateHelper = new VersionUpdateHelper(getApplicationContext());

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                //mediaPlayer.stop();
                //mediaPlayer.reset();
            }
        });


        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                mediaPlayer.release();
                mediaPlayer = null;
                return true;
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                isSoundPrepare = true;
            }
        });

        regToWx(true);

        wxHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                wxLoginResultCode = msg.arg1;
            }
        };

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    public MyApplication() {

        StrictMode.setThreadPolicy(
                new StrictMode
                        .ThreadPolicy
                        .Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build());

        StrictMode.setVmPolicy(
                new StrictMode
                        .VmPolicy
                        .Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        //.penaltyDeath()
                        .build());

        fileHelper = new FileHelper();
        objectMapper = new ObjectMapper();
        doorOpen = false;
        wifiReady = false;
        btReady = false;
        serverReady = false;
        headSetConnected = false;
        stopServiceMedia = false;
        beaconList = new HashMap<String, beacon>();
        actionList = new HashMap<String, List<content>>();
        playHistoryList = new HashMap<String, PlayHistory>();
        beaconStateList = new HashMap<String, String>();
        extagList = new HashMap<String, extag>();
        appRunningState = AppRunningState.FRONT;
        scaleVector = 1.0f;
        initTime = 0;

        logonUser = new LogonUser();
        logonUser.isLogon = false;

        downloadList = new ArrayList<download>();
        readerMap = new HashMap<String, BeaconReader>();
        access = false;
        downloading = false;
        contentList = new HashMap<String, content>();
        oldCatalogVersionList = new HashMap<String, String>();
        newCatalogVersionList = new HashMap<String, String>();
    }

    public void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            isSoundPrepare = false;
        }
    }

    public void writeLog(String playingId, String triggerType) {
        extag[] sortedList = extagList.values().toArray(new extag[]{});
        Arrays.sort(sortedList);
        extag exTag = sortedList[currentExTagIndex];
        String dt;
        String tag = exTag != null ? exTag.getExtag() : "";
        if (this.serverReady) {
            try {

                JSONObject jsonObject = new JSONObject();
                dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                if (playingId != "") {
                    jsonObject.put("userid", this.logonUser.userId);
                    jsonObject.put("beaconid", playingId);
                    jsonObject.put("logtime", dt);
                    jsonObject.put("triggertype", triggerType);
                    jsonObject.put("extag", tag);
                    ConfigHelper.getInstance(getApplicationContext()).addSysLog(jsonObject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void playOrPauseSound(String audioPath) {
        if (mediaPlayer != null) {
            if(isSoundPrepare) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    if(!phoneCallState) {
                        mediaPlayer.start();
                    }
                }
            }
            else{
                //停止后台音频
                stopSound();

                String suffix = "";
                switch (logonUser.voiceLang) {
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

                    case GlobalConst.VOICE_LANG_ARTIST:
                        suffix = "_en.mp3";
                        break;
                }

                String realPath = GlobalConst.PATH_SDCARD.concat(audioPath.replace(".mp3", suffix));
                try {
                    //检查耳机并播放前台音频
                    if (logonUser.earphonePlay.equals(GlobalConst.EARPHONE_PLAY_ON)) {
                        if (headSetConnected) {
                            playSound(realPath);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        playSound(realPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void playSound(String audioPath) {
        try {
            if (mediaPlayer != null) {
                if(fileHelper.isFileExistIncludeSDPath(audioPath)) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(audioPath);
                    if(!phoneCallState) {
                        mediaPlayer.prepare();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wxLogin(){
        if (!wxapi.isWXAppInstalled()) {
            //wx is not installed
            Toast.makeText(getApplicationContext(), "wx is not installed", Toast.LENGTH_LONG).show();
            return;
        }

        // send oauth request
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state =  "arts.buzz.wx.login";
        wxapi.sendReq(req);
    }

    public void wxShared(String webPageUrl,String exTitle,String title,String imagePath) {
        if (!wxapi.isWXAppInstalled()) {
            //wx is not installed
            Toast.makeText(getApplicationContext(), "wx is not installed", Toast.LENGTH_LONG).show();
            return;
        }

        WXWebpageObject webPage = new WXWebpageObject();
        //"http://arts.things.buzz/Share.html?refImageId=c42&locale=cn";
        webPage.webpageUrl = webPageUrl;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = webPage;
        msg.title = String.format("%s@%s", title, exTitle);
        msg.description = "WebPage Description";
        //"/com.buzz.exhibition/mmm/images/yixiang.png";
        String path = Environment.getExternalStorageDirectory() + imagePath;
        Bitmap thumbTemp = BitmapFactory.decodeFile(path);
        double factor = bmpToByteArray(thumbTemp, true).length / (32 * 1024);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize =  (int) Math.ceil(Math.sqrt(factor));;
        Bitmap thumb = BitmapFactory.decodeFile(path, opts);
        msg.setThumbImage(thumb);
        msg.mediaTagName = "Arts.Buzz";

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("shareToTimeline");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        wxapi.sendReq(req);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void regToWx(boolean checkSignature) {
        if(wxapi == null) {
            wxapi = WXAPIFactory.createWXAPI(this, APP_ID, checkSignature);
            wxapi.registerApp(APP_ID);
        }
    }

    private String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void setVoiceLang(String lang) {
        this.logonUser.voiceLang = lang;
    }

    public void setDisplayLang(String lang, boolean cascadingVoice) {
        Configuration config = getResources().getConfiguration();
        Locale curLocale;

        switch (lang) {
            case "en":
            case GlobalConst.DEFAULT_LANG_EN:
                curLocale = Locale.ENGLISH;
                this.logonUser.defaultLang = GlobalConst.DEFAULT_LANG_EN;
                if (cascadingVoice) {
                    this.logonUser.voiceLang = GlobalConst.VOICE_LANG_EN;
                }
                break;

            case "cn":
            case "zh":
            case "zh-rCN":
            case GlobalConst.DEFAULT_LANG_CN:
                curLocale = Locale.SIMPLIFIED_CHINESE;
                this.logonUser.defaultLang = GlobalConst.DEFAULT_LANG_CN;
                if (cascadingVoice) {
                    this.logonUser.voiceLang = GlobalConst.VOICE_LANG_SC;
                }
                break;

            case "tw":
            case "hk":
            case "zh-rTW":
            case GlobalConst.DEFAULT_LANG_TW:
                curLocale = Locale.TAIWAN;
                this.logonUser.defaultLang = GlobalConst.DEFAULT_LANG_TW;
                if (cascadingVoice) {
                    this.logonUser.voiceLang = GlobalConst.VOICE_LANG_CC;
                }
                break;

            case "pt":
            case GlobalConst.DEFAULT_LANG_PT:
                curLocale = new Locale("pt");
                this.logonUser.defaultLang = GlobalConst.DEFAULT_LANG_PT;
                if (cascadingVoice) {
                    this.logonUser.voiceLang = GlobalConst.VOICE_LANG_PT;
                }
                break;

            //找不到对应的语言则以简体中文显示
            default:
                curLocale = Locale.SIMPLIFIED_CHINESE;
                this.logonUser.defaultLang = GlobalConst.DEFAULT_LANG_CN;
                if (cascadingVoice) {
                    this.logonUser.voiceLang = GlobalConst.VOICE_LANG_SC;
                }
                break;
        }

        config.locale = curLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public List<favorite> getFavoriteList() {
        List<favorite> favList = new ArrayList<favorite>();
        String favoriteJson = ConfigHelper.getInstance(this).getFavorite();//this.fileHelper.readFromSDFile(GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SAVE).concat(GlobalConst.FILENAME_FAVORITE_LIST));
        try {
            if (favoriteJson != "") {
                //去掉结尾之“,”号并加上[]
                favoriteJson = "[" + favoriteJson.substring(0, favoriteJson.length() - 1) + "]";
                favorite[] favorites = this.objectMapper.readValue(favoriteJson, favorite[].class);
                favList = Arrays.asList(favorites);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return favList;
    }

    public void removeFavoriteFromList(favorite fav) {
        List<favorite> favList = getFavoriteList();
        List<favorite> targetList = new ArrayList<favorite>();
        for (favorite f : favList) {
            if (f.equals(fav)) {
                continue;
            }
            targetList.add(f);
        }

        //fileHelper.deleteFile(GlobalConst.PATH_SAVE.concat(GlobalConst.FILENAME_FAVORITE_LIST));
        try {
            StringBuilder sb = new StringBuilder();
            for (favorite f : targetList) {
                //JSON逐个写入
                sb.append(objectMapper.writeValueAsString(f) + ",");
            }
            ConfigHelper.getInstance(this).updateFavorite(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateFavoriteList(List<favorite> favList, List<favorite> delFavList) {
        try {
            StringBuilder sb = new StringBuilder();
            for (favorite f : favList) {
                if (getAppVersionCode() != GlobalConst.SPECIAL_VERSION) {
                    if (!delFavList.contains(f)) {
                        //JSON逐个写入
                        sb.append(objectMapper.writeValueAsString(f) + ",");
                    }
                }
            }

            if (getAppVersionCode() != GlobalConst.SPECIAL_VERSION) {
                ConfigHelper.getInstance(this).updateFavorite(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDownloadList() {
        List<download> downloadList = new ArrayList<download>();
        String[] suffix = new String[]{"_cc.mp3", "_en.mp3", "_sc.mp3", "_pt.mp3"};
        extag[] sortedList = extagList.values().toArray(new extag[]{});
        Arrays.sort(sortedList);
        for (extag et : sortedList) {
            if (et.getFileCount() > 0) {
                download d = new download(et.getExtag(), getApplicationContext());
                d.setDextag(et);
                excontent exc = null;
                try {
                    if (d.isNeedVersionUpdate()) {
                        d.setFinished(false);
                        exc = this.objectMapper.readValue(d.getUpdateSchema(), excontent.class);
                    } else {
                        exc = this.objectMapper.readValue(d.getLocalSchema(), excontent.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (exc != null) {
                    for (content c : exc.getContents()) {
                        if (c.getUsage().equals(GlobalConst.CONTENT_USAGE_PAINT)) {
                            d.getContentList().add(c);
                        }
                    }

                    for (content c : d.getContentList()) {
                        for (String sf : suffix) {
                            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                if (!fileHelper.isFileExist(c.getClientpath().concat(c.getFilename().replace(".mp3", sf)))) {
                                    d.setFinished(false);
                                }
                            } else {
                                if (!fileHelper.isFileExist(c.getClientpath().concat(c.getFilename()))) {
                                    d.setFinished(false);
                                }
                            }
                        }
                    }
                }

                downloadList.add(d);
            }
        }

        this.downloadList = downloadList;
    }


    public void addToFavorite(String text) {
        //fileHelper.appendToSDFile(GlobalConst.PATH_SAVE, GlobalConst.FILENAME_FAVORITE_LIST, text);
        ConfigHelper cfh = ConfigHelper.getInstance(this);
        cfh.updateFavorite(cfh.getFavorite().concat(text));
    }

    public void stopCoreService(){
        //停止Beacon服务
        Intent itCore = new Intent(getApplicationContext(), CoreService.class);
        stopService(itCore);
    }

    public void startCoreService(){
        //启动Beacon服务
        Intent itCore = new Intent(getApplicationContext(), CoreService.class);
        startService(itCore);
    }


    public void connectServer() {
        this.serverReady = WebAPIHelper.Factory.getPingServer();
        /*
        try {
            socket = new Socket(GlobalConst.SOCKET_HOST, GlobalConst.SOCKET_PORT);
            if (socket.isConnected()) {
                this.serverReady = true;
                socketHelper = new SocketHelper(socket, this);
                //生成系统配置文件
                if (!fileHelper.isFileExist(GlobalConst.PATH_SAVE + GlobalConst.FILENAME_BEACON_INFO_LIST)) {
                    initTime += 5;
                    socketHelper.Send(GlobalConst.CMD_GET_BEACON_INFO_LIST);
                }
                if (!fileHelper.isFileExist(GlobalConst.PATH_SAVE + GlobalConst.FILENAME_EXTAG_LIST)) {
                    //生成展览目录文件
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initTime += 5;
                            socketHelper.Send(GlobalConst.CMD_GET_EXTAG_LIST);
                        }
                    }, 5 * 1000);

                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.print(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(e.toString());
        }*/
    }

    public void checkWifi() {
        ConnectivityManager con = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        this.wifiReady = wifi;
    }

    public void openWifi() {
        WifiManager wm = (WifiManager) getSystemService(Activity.WIFI_SERVICE);
        boolean isWifiEnabled = wm.isWifiEnabled();
        if (!isWifiEnabled) {
            wm.setWifiEnabled(true);
            this.wifiReady = true;
        }
    }

    public void checkBluetooth() {
        try {
            boolean bt = BluetoothAdapter.getDefaultAdapter().isEnabled();
            this.btReady = bt;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initUserInfo() {
        String userInfoJson = ConfigHelper.getInstance(this).getAppUser();//this.fileHelper.readFromSDFile(GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SAVE).concat(GlobalConst.FILENAME_USER_INFO));
        try {
            //初始化系统用户资料
            if (userInfoJson != "") {
                UserInfo userInfo = this.objectMapper.readValue(userInfoJson, UserInfo.class);
                if (userInfo != null) {
                    this.logonUser.userId = userInfo.getUserid();
                    //this.logonUser.userNameCN = userInfoList[0].getUsername_cn();
                    //this.logonUser.userNameEN = userInfoList[0].getUsername_en();
                    //this.logonUser.userNameTW = userInfoList[0].getUsername_tw();
                    //this.logonUser.userNamePT = userInfoList[0].getUsername_pt();
                    this.logonUser.defaultLang = userInfo.getDefaultlang();
                    this.logonUser.voiceLang = userInfo.getVoicelang();
                    this.logonUser.email = userInfo.getEmail();
                    this.logonUser.nickName = userInfo.getNickname();
                    this.logonUser.autoPlay = userInfo.getAutoplay();
                    this.logonUser.earphonePlay = userInfo.getEarphoneplay();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initSysParams() {/*
        String sysParamsJson = this.fileHelper.readFromSDFile(GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SAVE).concat(GlobalConst.FILENAME_BEACON_INFO_LIST));
        try {
            //初始化BEACON列表及对应行为列表
            if (sysParamsJson != "") {
                BeaconInfoList beaconInfoList = this.objectMapper.readValue(sysParamsJson, BeaconInfoList.class);
                for (com.buzz.models.beacon b : beaconInfoList.getBeacon()) {
                    this.beaconList.put(b.getBeaconid(), b);
                }
                for (action a : beaconInfoList.getAction()) {
                    if (!this.actionList.containsKey(a.getBeaconid())) {
                        this.actionList.put(a.getBeaconid(), new ArrayList<action>());
                        this.actionList.get(a.getBeaconid()).add(a);
                    } else {
                        this.actionList.get(a.getBeaconid()).add(a);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        ConfigHelper cfh = ConfigHelper.getInstance(this);
        //先清空再初始化
        this.beaconList.clear();
        this.actionList.clear();
        this.contentList.clear();
        for (extag et : this.extagList.values()) {
            if (cfh.isExTagExists(et.getExtag())) {
                String jsonExConetnt = cfh.getExContent(et.getExtag());
                if (jsonExConetnt != "") {
                    try {
                        excontent exc = this.objectMapper.readValue(jsonExConetnt, excontent.class);
                        for(content c:exc.getContents()) {
                            contentList.put(c.getContentid(), c);
                        }
                        for (beacon b : exc.getBeacons()) {
                            b.setExtag(et.getExtag());
                            this.beaconList.put(b.getBeaconid(), b);

                            for (triggercontent tc : b.getTriggercontent()) {
                                if (!this.actionList.containsKey(b.getBeaconid())) {
                                    this.actionList.put(b.getBeaconid(), new ArrayList<content>());
                                    this.actionList.get(b.getBeaconid()).add(tc.getContent());
                                } else {
                                    this.actionList.get(b.getBeaconid()).add(tc.getContent());
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void deleteUselessFiles() {
        List<favorite> inUseFavList = new ArrayList<favorite>();
        List<favorite> currentFavList = this.getFavoriteList();

        List<String> unUseFolder = new ArrayList<String>();
        List<String> currentUseFolder = this.listSystemFolder();

        //检查文件使用状态
        for (extag et : this.extagList.values()) {
            for (favorite f : currentFavList) {
                if (f.getExtag().equals(et.getExtag())) {
                    if (!inUseFavList.contains(f)) {
                        inUseFavList.add(f);
                    }
                }
            }

            if (currentUseFolder.contains(et.getExtag())) {
                currentUseFolder.remove(et.getExtag());
            }
        }

        //更新收藏列表，删除过期的收藏
        try {
            StringBuilder sb = new StringBuilder();
            for (favorite f : inUseFavList) {
                //JSON逐个写入
                sb.append(this.objectMapper.writeValueAsString(f) + ",");
            }
            ConfigHelper.getInstance(this).updateFavorite(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //删除过期的资源文件
        for (String tag : currentUseFolder) {
            this.deleteExFiles(tag);
        }
    }

    public void deleteExFiles(String extag) {
        String filePath = GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SYSTEM).concat(extag);
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    File[] subFiles = files[i].listFiles();
                    if (subFiles != null) {
                        for (int j = 0; j < subFiles.length; j++) {
                            subFiles[j].delete();
                        }
                    }
                }

                files[i].delete();
            }
        }
        file.delete();
    }

    public List<String> listSystemFolder() {
        List<String> folderList = new ArrayList<String>();
        String filePath = GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SYSTEM);
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getName().equals("apk")) {
                    folderList.add(files[i].getName());
                }
            }
        }

        return folderList;
    }

    public void initExtagList() {
        ConfigHelper cfh = ConfigHelper.getInstance(this);
        String extagListJson = cfh.getCatalog();//this.fileHelper.readFromSDFile(GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SAVE).concat(GlobalConst.FILENAME_EXTAG_LIST));
        try {
            //初始化展览目录列表
            if (extagListJson != "") {
                extag[] exTagList = this.objectMapper.readValue(extagListJson, extag[].class);
                Arrays.sort(exTagList);
                if (exTagList != null) {
                    this.extagList.clear();
                    newCatalogVersionList.clear();
                    for (extag et : exTagList) {
                        newCatalogVersionList.put(et.getExtag(),et.getPublish());
                        this.extagList.put(et.getExtag(), et);
                    }
                }

                //更新下载列表
                updateDownloadList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测邮箱地址是否合法
     *
     * @param email
     * @return true合法 false不合法
     */
    public boolean isEmail(String email) {
        if (null == email || "".equals(email)) return false;
        Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 检查beacon资源是否存在
     *
     * @param triggerContents
     * @return true/false
     */
    public boolean isBeaconResourceReady(List<triggercontent> triggerContents) {
        String[] suffix = new String[]{"_cc.mp3", "_en.mp3", "_sc.mp3", "_pt.mp3"};
        int resourceCount = 0;

        List<content> contentList = new ArrayList<content>();
        for (triggercontent tc : triggerContents) {
            contentList.add(tc.getContent());
        }

        for (content c : contentList) {
            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                for (String sf : suffix) {
                    if (fileHelper.isFileExist(c.getClientpath().concat(c.getFilename().replace(".mp3", sf)))) {
                        resourceCount++;//4-audio
                    }
                }
            } else {
                if (fileHelper.isFileExist(c.getClientpath().concat(c.getFilename()))) {
                    resourceCount++;//1-image
                }
            }
        }

        if (getAppVersionCode() != GlobalConst.SPECIAL_VERSION) {
            return (resourceCount == 5);
        } else {
            return (resourceCount >= 1);
        }

    }

    public int getAppVersionCode(){
       return versionUpdateHelper.getVersionCode();
    }

    //生成自动登入Email
    public String genEmail(){
        return String.valueOf(System.currentTimeMillis()).concat("@buzz.com");
    }

    public String getLocaleString(String localeCode) {
        String localeStr = "";
        switch (localeCode) {
            case GlobalConst.DEFAULT_LANG_CN:
                localeStr = "cn";
                break;

            case GlobalConst.DEFAULT_LANG_TW:
                localeStr = "tw";
                break;

            case GlobalConst.DEFAULT_LANG_EN:
                localeStr = "en";
                break;

            case GlobalConst.DEFAULT_LANG_PT:
                localeStr = "pt";
                break;
        }

        return localeStr;
    }

}
