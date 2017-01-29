package com.buzz.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by buzz on 2015/5/5.
 */
public class SettingActivity extends Activity {
    List<SpinnerData> listDisplay;
    List<SpinnerData> listVoice;
    ArrayAdapter<SpinnerData> adpDisplay;
    ArrayAdapter<SpinnerData> adpVoice;
    MyApplication myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        myApp = (MyApplication) getApplication();

        listVoice = new ArrayList<SpinnerData>();
        listDisplay = new ArrayList<SpinnerData>();

        listDisplay.add(new SpinnerData(GlobalConst.DEFAULT_LANG_TW, getString(R.string.lang_tw)));
        listDisplay.add(new SpinnerData(GlobalConst.DEFAULT_LANG_CN, getString(R.string.lang_cn)));
        listDisplay.add(new SpinnerData(GlobalConst.DEFAULT_LANG_EN, getString(R.string.lang_en)));
        listDisplay.add(new SpinnerData(GlobalConst.DEFAULT_LANG_PT, getString(R.string.lang_pt)));

        listVoice.add(new SpinnerData(GlobalConst.VOICE_LANG_ARTIST, getString(R.string.langArtist)));
        listVoice.add(new SpinnerData(GlobalConst.VOICE_LANG_CC, getString(R.string.langCC)));
        listVoice.add(new SpinnerData(GlobalConst.VOICE_LANG_SC, getString(R.string.langSC)));
        listVoice.add(new SpinnerData(GlobalConst.VOICE_LANG_EN, getString(R.string.langEN)));
        listVoice.add(new SpinnerData(GlobalConst.VOICE_LANG_PT, getString(R.string.langPT)));

        final Spinner spDisplay = (Spinner) findViewById(R.id.setting_activity_spin_display_lang);
        adpDisplay = new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_item, listDisplay);
        adpDisplay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDisplay.setAdapter(adpDisplay);
        spDisplay.setSelection(Integer.parseInt(myApp.logonUser.defaultLang), true);

        final Spinner spVoice = (Spinner) findViewById(R.id.setting_activity_spin_voice_lang);
        adpVoice = new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_item, listVoice);
        adpVoice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVoice.setAdapter(adpVoice);
        spVoice.setSelection(Integer.parseInt(myApp.logonUser.voiceLang), true);

        final ImageButton btnBack = (ImageButton) findViewById(R.id.setting_activity_btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重新加载展览界面
                String root = getIntent().getStringExtra("root");
                if (root.equals("ex")) {
                    Intent it = new Intent();
                    it.setClass(SettingActivity.this, ExhibitionActivity.class);
                    it.putExtra("index", myApp.currentExTagIndex);
                    it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
                    SettingActivity.this.finish();
                } else if (root.equals("main")) {
                    Intent it = new Intent();
                    it.setClass(SettingActivity.this, MainActivity.class);
                    it.putExtra("clear", true);
                    it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
                    SettingActivity.this.finish();
                }
            }
        });

        spDisplay.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                myApp.setDisplayLang(adpDisplay.getItem(arg2).getValue(), true);
                spVoice.setSelection(Integer.parseInt(myApp.logonUser.voiceLang), true);
                arg0.setVisibility(View.VISIBLE);

                //Toast.makeText(SettingActivity.this, getString(R.string.msg_restart), Toast.LENGTH_LONG).show();
                updateUserConfig();

                Intent it = new Intent(SettingActivity.this, SettingActivity.class);
                it.putExtra("root", getIntent().getStringExtra("root"));
                startActivity(it);
                SettingActivity.this.finish();

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        spVoice.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                String value = adpVoice.getItem(arg2).getValue();
                myApp.setVoiceLang(value);
                arg0.setVisibility(View.VISIBLE);

                /*
                switch (value) {
                    case GlobalConst.VOICE_LANG_CC:
                        Toast.makeText(SettingActivity.this, getString(R.string.msg_voice_lang_cc), Toast.LENGTH_LONG).show();
                        break;

                    case GlobalConst.VOICE_LANG_SC:
                        Toast.makeText(SettingActivity.this, getString(R.string.msg_voice_lang_sc), Toast.LENGTH_LONG).show();
                        break;

                    case GlobalConst.VOICE_LANG_EN:
                        Toast.makeText(SettingActivity.this, getString(R.string.msg_voice_lang_en), Toast.LENGTH_LONG).show();
                        break;

                    case GlobalConst.VOICE_LANG_PT:
                        Toast.makeText(SettingActivity.this, getString(R.string.msg_voice_lang_pt), Toast.LENGTH_LONG).show();
                        break;
                }*/

                updateUserConfig();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        final RadioButton rbtn = (RadioButton) findViewById(R.id.setting_activity_rbtn_auto_play);
        if (myApp.logonUser.autoPlay.equals(GlobalConst.AUTO_PLAY_ON)) {
            rbtn.setBackground(getResources().getDrawable(R.drawable.button2));
        } else {
            rbtn.setBackground(getResources().getDrawable(R.drawable.button1));
        }

        rbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApp.logonUser.autoPlay.equals(GlobalConst.AUTO_PLAY_OFF)) {
                    rbtn.setBackground(getResources().getDrawable(R.drawable.button2));
                    myApp.logonUser.autoPlay = GlobalConst.AUTO_PLAY_ON;
                } else {
                    rbtn.setBackground(getResources().getDrawable(R.drawable.button1));
                    myApp.logonUser.autoPlay = GlobalConst.AUTO_PLAY_OFF;
                }

                updateUserConfig();
            }
        });

        final RadioButton rbtnEarphonePlay = (RadioButton) findViewById(R.id.setting_activity_rbtn_earphone_play);
        if (myApp.logonUser.earphonePlay.equals(GlobalConst.EARPHONE_PLAY_ON)) {
            rbtnEarphonePlay.setBackground(getResources().getDrawable(R.drawable.button2));
        } else {
            rbtnEarphonePlay.setBackground(getResources().getDrawable(R.drawable.button1));
        }

        rbtnEarphonePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApp.logonUser.earphonePlay.equals(GlobalConst.EARPHONE_PLAY_OFF)) {
                    rbtnEarphonePlay.setBackground(getResources().getDrawable(R.drawable.button2));
                    myApp.logonUser.earphonePlay = GlobalConst.EARPHONE_PLAY_ON;
                } else {
                    rbtnEarphonePlay.setBackground(getResources().getDrawable(R.drawable.button1));
                    myApp.logonUser.earphonePlay = GlobalConst.EARPHONE_PLAY_OFF;
                }

                updateUserConfig();
            }
        });
    }

    private void updateUserConfig() {
        myApp.connectServer();
        ConfigHelper.getInstance(this).updateAppUser(myApp.logonUser.userId, myApp.logonUser.defaultLang, myApp.logonUser.voiceLang, myApp.logonUser.autoPlay, myApp.logonUser.earphonePlay);


        //String userInfoJson = "";
        //UserInfo userInfo = null;

        /*
        if (myApp.fileHelper.isFileExist(GlobalConst.PATH_SAVE + GlobalConst.FILENAME_USER_INFO)) {
            userInfoJson = myApp.fileHelper.readFromSDFile(GlobalConst.PATH_SDCARD.concat(GlobalConst.PATH_SAVE).concat(GlobalConst.FILENAME_USER_INFO));
            if (userInfoJson != "") {
                try {
                    userInfoList = myApp.objectMapper.readValue(userInfoJson, UserInfo[].class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (userInfo != null) {
            userInfo.setDefaultlang(myApp.logonUser.defaultLang);
            userInfo.setVoicelang(myApp.logonUser.voiceLang);
            userInfo.setAutoplay(myApp.logonUser.autoPlay);
            try {
                if (myApp.fileHelper.deleteFile(GlobalConst.PATH_SAVE + GlobalConst.FILENAME_USER_INFO)) {
                    myApp.fileHelper.writeToSDFromJson(
                            GlobalConst.PATH_SAVE, GlobalConst.FILENAME_USER_INFO, myApp.objectMapper.writeValueAsString(userInfoList));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    public class SpinnerData {

        private String value = "";
        private String text = "";

        public SpinnerData() {
            value = "";
            text = "";
        }

        public SpinnerData(String _value, String _text) {
            value = _value;
            text = _text;
        }

        @Override
        public String toString() {
            return text;
        }

        public String getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }

}

