package com.buzz.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by buzz on 2015/5/5.
 */
public class SignActivity extends Activity {

    MyApplication myApp;
    SignUpReceiver signUpReceiver;
    FindUserReceiver findUserReceiver;
    IntentFilter intentFilter;
    final String TAG = this.getClass().getSimpleName();
    String email = "";
    String nickname = "";
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign);

        myApp = (MyApplication) getApplication();

        signUpReceiver = new SignUpReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConst.ACTION_USER_SIGN_UP);
        registerReceiver(signUpReceiver, intentFilter);

        findUserReceiver = new FindUserReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConst.ACTION_USER_FIND);
        registerReceiver(findUserReceiver, intentFilter);

        final Button btn_sign = (Button) findViewById(R.id.sign_activity_btn_sign);
        final Button btn_login = (Button) findViewById(R.id.sign_activity_btn_login);
        final EditText et_nickname = (EditText) findViewById(R.id.sign_activity_et_nickname);
        final EditText et_email = (EditText) findViewById(R.id.sign_activity_et_email);
        final EditText et_password = (EditText) findViewById(R.id.sign_activity_et_password);

        //for testing
        //et_email.setText("abc@itz.com");
        //et_nickname.setText("abc");
        //et_password.setText("123456");


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果已经注册则将邮箱地址和密码绑定并返回登录界面
                Intent it = new Intent(SignActivity.this, LoginActivity.class);
                if (!email.equals("")) {
                    it.putExtra("email", email);
                }
                if (!password.equals("")) {
                    it.putExtra("pwd", password);
                }
                startActivity(it);
                SignActivity.this.finish();
            }
        });

        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nickname = et_nickname.getText().toString();
                email = et_email.getText().toString();
                password = et_password.getText().toString();

                if (nickname.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_check_nickname), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_check_email), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!myApp.isEmail(email)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_email_not_valid), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_check_pwd), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (myApp.serverReady) {
                    //查找用户
                    //myApp.socketHelper.Send(String.format(GlobalConst.CMD_FIND_USER, email));
                    String userId = email.replace(".", "$");
                    if (ConfigHelper.getInstance(getApplicationContext()).findAppUser(userId)) {
                        Toast.makeText(SignActivity.this, getString(R.string.msg_email_exists), Toast.LENGTH_LONG).show();
                    } else {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject();
                            jsonObject.put("userid", userId);
                            jsonObject.put("email", email);
                            jsonObject.put("nickname", nickname);
                            jsonObject.put("password", password);
                            jsonObject.put("defaultlang", myApp.logonUser.defaultLang);
                            jsonObject.put("voicelang", myApp.logonUser.voiceLang);
                        } catch (JSONException e) {

                        }
                        if (ConfigHelper.getInstance(getApplicationContext()).addAppUser(jsonObject)) {
                            Toast.makeText(SignActivity.this, getString(R.string.msg_sign_up_success), Toast.LENGTH_LONG).show();
                            btn_login.callOnClick();
                        }
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
        unregisterReceiver(signUpReceiver);
        unregisterReceiver(findUserReceiver);
    }

    public class SignUpReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getStringExtra("result").equals("1")) {
                    Toast.makeText(SignActivity.this, getString(R.string.msg_sign_up_success), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class FindUserReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getStringExtra("result").equals("0")) {
                    //没果没有则新增
                    myApp.socketHelper.Send(String.format(
                            GlobalConst.CMD_ADD_USER, email, password, nickname, myApp.logonUser.voiceLang, myApp.logonUser.defaultLang));
                } else {
                    Toast.makeText(SignActivity.this, getString(R.string.msg_email_exists), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
