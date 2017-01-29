package com.buzz.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.VersionUpdateHelper;

import java.util.Locale;

/**
 * Created by NickChung on 6/8/15.
 */
public class NavActivity extends Activity {
    MyApplication myApp;
    ConfigHelper configHelper;
    VersionUpdateHelper versionUpdateHelper;
    DownloadCompleteReceiver completeReceiver;
    long firstTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        completeReceiver = new DownloadCompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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

        myApp = (MyApplication) getApplication();
        configHelper = ConfigHelper.getInstance(this);

        versionUpdateHelper = new VersionUpdateHelper(this);
        //显示版本
        TextView txtVersion = (TextView) findViewById(R.id.view);
        String currentVersion = versionUpdateHelper.getVersionName();
        txtVersion.setText("version:" + currentVersion);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //检查WIFI和蓝牙
        myApp.checkWifi();
        myApp.checkBluetooth();

        //如果已经登入过则记下登入状态
        if (configHelper.getAppUser() != "") {
            //if (myApp.fileHelper.isFileExist(GlobalConst.PATH_SAVE + GlobalConst.FILENAME_USER_INFO)) {
            myApp.logonUser.isLogon = true;
            //获取显示语言及播放语言
            //初始化用户数据
            myApp.initUserInfo();
            myApp.setDisplayLang(myApp.logonUser.defaultLang, false);
            myApp.setVoiceLang(myApp.logonUser.voiceLang);
        } else {
            //设置默认显示语言
            //如果地区是HK则手动转换成hk,地区是TW则手动转换成tw
            if (Locale.getDefault().getCountry().toLowerCase().equals("hk")) {
                myApp.setDisplayLang("hk", true);
            } else if (Locale.getDefault().getCountry().toLowerCase().equals("tw")) {
                myApp.setDisplayLang("tw", true);
            } else {
                myApp.setDisplayLang(Locale.getDefault().getLanguage(), true);
            }
        }

        //如果有WIFI直接连接服务器
        if (myApp.wifiReady) {
            startActivity(new Intent(NavActivity.this, InitialActivity.class));
            NavActivity.this.finish();
        } else {
            //如果没有WIFI则打开WIFI
            myApp.openWifi();
            startActivity(new Intent(NavActivity.this, InitialActivity.class));
            NavActivity.this.finish();
        }
    }

    private void showVersionUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NavActivity.this);
        builder.setIcon(R.drawable.logo);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.msg_found_new_app_version));
        builder.setPositiveButton(getString(R.string.msg_dlg_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (myApp.fileHelper.isFileExist(GlobalConst.PATH_APK.concat(versionUpdateHelper.getFileName()))) {
                    versionUpdateHelper.install();
                } else {
                    downloadNewPackage();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.msg_dlg_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private void downloadNewPackage() {
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //创建下载请求
        DownloadManager.Request down = new DownloadManager.Request(Uri.parse(versionUpdateHelper.getmPackageURL()));
        //设置允许使用的网络类型，这里是移动网络和wifi都可以
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //后台下载
        down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //显示下载界面
        down.setVisibleInDownloadsUi(true);
        //设置下载后文件存放的位置
        down.setDestinationInExternalPublicDir(GlobalConst.PATH_APK, versionUpdateHelper.getFileName());
        //将下载请求放入队列
        manager.enqueue(down);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (completeReceiver != null) unregisterReceiver(completeReceiver);
    }

    @Override
    protected void onResume() {
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        super.onResume();
    }

    //接受下载完成后的intent
    private class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                versionUpdateHelper.install();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > GlobalConst.SYSTEM_EXIT_INTERVAL) {
                Toast.makeText(NavActivity.this, getString(R.string.msg_quit_system), Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            } else {
                this.finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
