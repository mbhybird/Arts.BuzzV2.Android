package com.buzz.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.buzz.models.extag;
import com.buzz.service.LockScreenService;
import com.buzz.service.MessageService;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.VersionUpdateHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class InitialActivity extends ActionBarActivity {
    MyApplication myApp;
    ConfigHelper configHelper;
    VersionUpdateHelper versionUpdateHelper;
    Handler loginHandler;
    Message loginMessage;
    long firstTime = 0;
    Handler wifiHandler;
    Runnable wifiRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        ProgressDialog dlg = new ProgressDialog(this, R.style.LoadingDialog);
        dlg.setTitle("");
        dlg.setMessage("Loading, Please wait...");
        dlg.setIndeterminate(true);
        dlg.setCancelable(false);
        dlg.show();

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
        TextView txtVersion = (TextView) findViewById(R.id.activity_initial_tvVersion);
        String currentVersion = versionUpdateHelper.getVersionName();
        txtVersion.setText("version:" + currentVersion);

        loginHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                wifiHandler.removeCallbacks(wifiRunnable);
                switch (msg.what) {
                    case 1:
                        //启动后台消息服务
                        Intent itBack = new Intent(InitialActivity.this, MessageService.class);
                        startService(itBack);

                        //start lock screen service
                        Intent intent = new Intent(InitialActivity.this, LockScreenService.class);
                        startService(intent);

                        ConfigHelper cfh = ConfigHelper.getInstance(getApplicationContext());
                        //第一次登录
                        if (cfh.getFirstTimeIn()) {
                            cfh.updateFirstTimeIn();
                            //显示指引界面
                            startActivity(new Intent(InitialActivity.this, GuideViewActivity.class));
                            InitialActivity.this.finish();
                        }
                        //已经登录过
                        else {
                            //进入首页
                            startActivity(new Intent(InitialActivity.this, ExhibitionActivity.class));
                            InitialActivity.this.finish();
                        }
                        break;
                }
            }
        };

        wifiHandler = new Handler();
        wifiRunnable = new Runnable() {
            @Override
            public void run() {
                sysBootstrap();
            }
        };

        wifiHandler.post(wifiRunnable);
    }

    private void sysBootstrap(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dataVersionChecking();
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                signUpThenAccess();
            }
        }, 3000);
    }

    private void signUpThenAccess() {
        //如果有用户登录文件
        if (myApp.logonUser.isLogon) {
            wifiHandler.removeCallbacks(wifiRunnable);
            //启动后台消息服务
            Intent itBack = new Intent(InitialActivity.this, MessageService.class);
            startService(itBack);

            //start lock screen service
            Intent intent = new Intent(InitialActivity.this, LockScreenService.class);
            startService(intent);

            //直接进入首页
            startActivity(new Intent(InitialActivity.this, ExhibitionActivity.class));
            InitialActivity.this.finish();
        } else {
            //自动注册并登录
            JSONObject jsonObject = null;
            String email = myApp.genEmail();
            String userId = email.replace(".", "$");
            String password = "123456";
            String nickname = "nickname";
            try {
                jsonObject = new JSONObject();
                jsonObject.put("userid", userId);
                jsonObject.put("email", email);
                jsonObject.put("nickname", nickname);
                jsonObject.put("password", password);
                jsonObject.put("defaultlang", myApp.logonUser.defaultLang);
                jsonObject.put("voicelang", GlobalConst.VOICE_LANG_ARTIST);//myApp.logonUser.voiceLang;
            } catch (JSONException e) {

            }
            if (ConfigHelper.getInstance(getApplicationContext()).addAppUser(jsonObject)) {
                myApp.logonUser.isLogon = true;
                ConfigHelper.getInstance(getApplicationContext()).updateAppUser(userId);
                //初始化用户信息
                myApp.initUserInfo();
                myApp.setDisplayLang(myApp.logonUser.defaultLang, false);
                myApp.setVoiceLang(myApp.logonUser.voiceLang);
                loginMessage = new Message();
                loginMessage.what = 1;
                loginHandler.sendMessageDelayed(loginMessage, 1000);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.msg_check_wifi), Toast.LENGTH_LONG).show();
                wifiHandler.postDelayed(wifiRunnable, 1000);
            }
        }
    }

    private void dataVersionChecking() {
        boolean dataVersionUpdate = false;
        myApp.connectServer();
        if (myApp.serverReady) {
            //是否第一次更新数据
            if (configHelper.getCatalog() == "") {
                //生成目录文件
                configHelper.updateCatalog();
                //初始化数据版本
                configHelper.updateDataVersion();
            }
            //数据版本更新
            else if (configHelper.isDataVersionUpdate()) {
                dataVersionUpdate = true;
                //获取旧展览目录
                String extagListJson = configHelper.getCatalog();
                if (extagListJson != "") {
                    try {
                        extag[] exTagList = myApp.objectMapper.readValue(extagListJson, extag[].class);
                        if (exTagList != null) {
                            myApp.oldCatalogVersionList.clear();
                            for (extag et : exTagList) {
                                myApp.oldCatalogVersionList.put(et.getExtag(),et.getPublish());
                                //删除旧目录
                                configHelper.deleteExContentFromConfig(et.getExtag());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //更新目录文件
                configHelper.updateCatalog();
                //更新数据版本
                configHelper.updateDataVersion();
            }
        }
        //初始化展览列表
        myApp.initExtagList();

        if (dataVersionUpdate) {
            //如果展览不存在，删除无用的收藏文件和资源文件
            myApp.deleteUselessFiles();
        }

        //初始化beacon列表和action列表
        myApp.initSysParams();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > GlobalConst.SYSTEM_EXIT_INTERVAL) {
                Toast.makeText(InitialActivity.this, getString(R.string.msg_quit_system), Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            } else {
                wifiHandler.removeCallbacks(wifiRunnable);
                this.finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
