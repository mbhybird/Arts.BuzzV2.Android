package com.buzz.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.buzz.service.MessageService;
import com.buzz.service.CoreService;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;

public class LoginActivity extends ActionBarActivity {

    MyApplication myApp;
    LoginReceiver loginReceiver;
    IntentFilter intentFilter;
    long firstTime = 0;
    ProgressDialog progressDialog;
    Handler loginHandler;
    Message loginMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myApp = (MyApplication) getApplication();

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.msg_logging_in));
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        loginHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        progressDialog.show();
                        break;
                    case 1:
                        progressDialog.dismiss();
                        //启动Beacon服务
                        Intent itCore = new Intent(LoginActivity.this, CoreService.class);
                        startService(itCore);
                        //启动后台消息服务
                        Intent itBack = new Intent(LoginActivity.this, MessageService.class);
                        startService(itBack);

                        ConfigHelper cfh = ConfigHelper.getInstance(getApplicationContext());
                        //第一次登录
                        if (cfh.getFirstTimeIn()) {
                            cfh.updateFirstTimeIn();
                            //显示指引界面
                            startActivity(new Intent(LoginActivity.this, GuideViewActivity.class));
                            LoginActivity.this.finish();
                        }
                        //已经登录过
                        else {
                            //进入首页
                            startActivity(new Intent(LoginActivity.this, ExhibitionActivity.class));
                            LoginActivity.this.finish();
                        }
                        break;

                    case 2:
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_login_failed), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        loginReceiver = new LoginReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConst.ACTION_USER_LOGIN);
        registerReceiver(loginReceiver, intentFilter);

        final Button btn_sign = (Button) findViewById(R.id.login_activity_btn_sign);
        final EditText et_email = (EditText) findViewById(R.id.login_activity_et_email);
        final EditText et_password = (EditText) findViewById(R.id.login_activity_et_password);

        //for testing
        //et_email.setText("nick.chung");
        //et_password.setText("123456");

        Intent it = getIntent();
        if (it.hasExtra("email")) {
            et_email.setText(it.getStringExtra("email"));
        }

        if (it.hasExtra("pwd")) {
            et_password.setText(it.getStringExtra("pwd"));
        }

        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(LoginActivity.this, SignActivity.class);
                startActivity(it);
                LoginActivity.this.finish();
            }
        });


        Button btnLogin = (Button) findViewById(R.id.login_activity_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_check_email), Toast.LENGTH_LONG).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_check_pwd), Toast.LENGTH_LONG).show();
                    return;

                }
                if(!myApp.isEmail(email)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_email_not_valid), Toast.LENGTH_LONG).show();
                    return;
                }

                if (myApp.serverReady) {
                    //myApp.socketHelper.Send(String.format(GlobalConst.CMD_GET_USER_INFO, email, password));
                    String userId = email.replace(".", "$");
                    loginMessage = new Message();
                    loginMessage.what = 0;
                    loginHandler.sendMessage(loginMessage);

                    boolean checkAppUser = ConfigHelper.getInstance(getApplicationContext()).checkAppUser(userId, password);
                    if (checkAppUser) {
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
                        loginMessage = new Message();
                        loginMessage.what = 2;
                        loginHandler.sendMessageDelayed(loginMessage, 1000);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_check_server), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(loginReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > GlobalConst.SYSTEM_EXIT_INTERVAL) {
                Toast.makeText(LoginActivity.this, getString(R.string.msg_quit_system), Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            } else {

                //先停止Beacon服务
                Intent itCore = new Intent(this, CoreService.class);
                stopService(itCore);

                //再停止后台消息服务
                Intent itBack = new Intent(this, MessageService.class);
                stopService(itBack);

                this.finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public class LoginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (myApp.logonUser.isLogon) {
                    startActivity(new Intent(LoginActivity.this, ExhibitionActivity.class));
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_login_failed), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
