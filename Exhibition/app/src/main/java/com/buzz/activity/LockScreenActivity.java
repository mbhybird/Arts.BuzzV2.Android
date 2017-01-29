package com.buzz.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buzz.models.MyRect;
import com.buzz.models.beacon;
import com.buzz.models.content;
import com.buzz.models.extag;
import com.buzz.models.favorite;
import com.buzz.service.BeaconReader;
import com.buzz.service.LockScreenService;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.TimeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * if the screen is locked, this Activity will show.
 *
 * @author NickChung
 */
public class LockScreenActivity extends Activity {
    /**
     * click this ImageView to unlock screen
     */
    private ImageView iv_key;

    /**
     * this TextView is used to show current time as an example, also you can show any thing on the {@link LockScreenActivity} you want to
     */
    private TextView tv_time;

    /**
     * check whether the screen is locked or not
     */
    public static boolean isLocked = false;

    //Handler myTimeHandler;
    //Runnable myTimeRunnable;

    final String TAG = this.getClass().getSimpleName();
    MyApplication myApp;
    Handler readerHandler;
    Runnable readerRunnable;
    List<Map<String, String>> beaconList;
    Map<String, String> beaconMap;
    BeaconListAdapter mBeaconListAdapter;
    int stayInterval = 0;
    String curPlayingId = "";
    Map<String,MyRect> myRectMap;
    List<MyRect> myRectList;
    ListView beaconListView;
    List<extag> extagList;
    TextView tvExTitle;
    String mExtag = "";
    String imageDesc = "";
    String title = "";
    String shareExTitle;
    String shareLocale;
    String shareImagePath;
    RelativeLayout paintDlg;
    RelativeLayout ballLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_lock_screen);

        myApp = (MyApplication) getApplication();

        readerRunnable = new Runnable() {
            @Override
            public void run() {
                if (myApp.access && LockScreenActivity.isLocked) {
                    updateBallListView();
                    if (beaconList.size() > 0) {
                        if (curPlayingId.equals(beaconList.get(0).get("beaconId"))) {
                            stayInterval++;
                            if (stayInterval >= 3) {
                                if (!myApp.lastPlayingId.equals(curPlayingId)) {
                                    myApp.writeLog(myApp.lastPlayingId, GlobalConst.TRIGGER_TYPE_OUT);
                                    myApp.writeLog(curPlayingId, GlobalConst.TRIGGER_TYPE_IN);
                                    showPaintDialog(new DlgEvent(curPlayingId, 1));
                                    myApp.lastPlayingId = curPlayingId;
                                }
                            }
                        } else {
                            curPlayingId = beaconList.get(0).get("beaconId");
                            stayInterval = 1;
                        }
                    }
                }
                readerHandler.postDelayed(this, 1000);
            }
        };
        readerHandler = new Handler();
        readerHandler.post(readerRunnable);

        myRectMap = new HashMap<String, MyRect>();
        myRectList = new ArrayList<MyRect>();
        beaconList = new ArrayList<Map<String, String>>();

        extagList = new ArrayList<extag>();
        extag[] sortedList = myApp.extagList.values().toArray(new extag[]{});
        Arrays.sort(sortedList);
        for (extag et : sortedList) {
            extagList.add(et);
        }

        beaconListView = (ListView) findViewById(R.id.lock_screen_activity_beacon_list_lv);
        beaconListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BeaconListAdapter myBeaconAdapter = (BeaconListAdapter) parent.getAdapter();
                Map<String, Object> map = (Map<String, Object>) myBeaconAdapter.getItem(position);
                showPaintDialog(new DlgEvent(map.get("beaconId").toString(), 0));
            }
        });
        tvExTitle = (TextView) findViewById(R.id.lock_screen_activity_txt_ex_title);
        tvExTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/futura.ttf"));
        String sourceText,convertText;
        switch (myApp.logonUser.defaultLang) {
            case GlobalConst.DEFAULT_LANG_CN:
                sourceText = extagList.get(myApp.currentExTagIndex).getTitle_cn();
                if (sourceText.length() > 15) {
                    convertText = sourceText.substring(0, 15).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;

            case GlobalConst.DEFAULT_LANG_EN:
                sourceText = extagList.get(myApp.currentExTagIndex).getTitle_en();
                if (sourceText.length() > 25) {
                    convertText = sourceText.substring(0, 25).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;

            case GlobalConst.DEFAULT_LANG_TW:
                sourceText = extagList.get(myApp.currentExTagIndex).getTitle_tw();
                if (sourceText.length() > 15) {
                    convertText = sourceText.substring(0, 15).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;

            case GlobalConst.DEFAULT_LANG_PT:
                sourceText = extagList.get(myApp.currentExTagIndex).getTitle_pt();
                if (sourceText.length() > 25) {
                    convertText = sourceText.substring(0, 25).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;
        }

        isLocked = true;

        iv_key = (ImageView) findViewById(R.id.iv_key);
        /*
        tv_time = (TextView) findViewById(R.id.tv_time);

        myTimeHandler = new Handler();
        myTimeRunnable = new Runnable() {
            @Override
            public void run() {
                tv_time.setText(TimeHelper.getTime());
                myTimeHandler.postDelayed(this, 100);
            }
        };
        myTimeHandler.post(myTimeRunnable);*/

        iv_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                virbate();
                isLocked = false;
                readerHandler.removeCallbacks(readerRunnable);
                Toast.makeText(LockScreenActivity.this, "Screen is unlocked", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        try {
            startService(new Intent(this, LockScreenService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        paintDlg = (RelativeLayout)findViewById(R.id.lock_screen_activity_paint_layout);
        ballLayout = (RelativeLayout)findViewById(R.id.lock_screen_activity_ball_layout);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            // Key code constant: Home key. This key is handled by the framework and is never delivered to applications.
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (paintDlg.getVisibility() == View.VISIBLE) {
            paintDlg.setVisibility(View.INVISIBLE);
            ballLayout.setVisibility(View.VISIBLE);
            iv_key.setVisibility(View.VISIBLE);
        }

        if (myApp.mediaPlayer.isPlaying()) {
            curPlayingId = myApp.lastPlayingId;
            showPaintDialog(new DlgEvent(myApp.lastPlayingId, 1));
        }
    }

    @Override
    public void onBackPressed() {
        //return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * virbate means that the screen is unlocked success
     */
    private void virbate() {
        Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void playAudio(String audioPath) {
        myApp.stopSound();

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

            case GlobalConst.VOICE_LANG_ARTIST:
                suffix = "_en.mp3";
                break;
        }

        String realPath = GlobalConst.PATH_SDCARD.concat(audioPath.replace(".mp3", suffix));
        try {
            //检查耳机并播放前台音频
            if (myApp.logonUser.earphonePlay.equals(GlobalConst.EARPHONE_PLAY_ON)) {
                if (myApp.headSetConnected) {
                    myApp.playSound(realPath);
                } else {
                    Toast.makeText(LockScreenActivity.this, getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
                }
            } else {
                myApp.playSound(realPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBallListView() {
        try {
            String title = "";
            String imagePath = "";
            String tag;
            String audioPath = "";

            beaconList.clear();

            for (Map.Entry<String, BeaconReader> m : myApp.readerMap.entrySet()) {
                beacon b = myApp.beaconList.get(m.getKey());
                if (b != null) {
                    if (b.getExtag() == extagList.get(myApp.currentExTagIndex).getExtag()) {
                        if (myApp.isBeaconResourceReady(b.getTriggercontent())) {
                            String direction = b.getRangedirection();
                            if ((direction.equals(GlobalConst.RANGE_DIRECTION_FRONT)
                                    || direction.equals(GlobalConst.RANGE_DIRECTION_BOTH))
                                    && b.getUsage().equals(GlobalConst.BEACON_USAGE_DETAIL)) {

                                tag = b.getDisplayname();
                                Iterator it = myApp.actionList.get(m.getKey()).iterator();

                                while (it.hasNext()) {
                                    content c = (content) it.next();
                                    switch (myApp.logonUser.defaultLang) {
                                        case GlobalConst.DEFAULT_LANG_CN:
                                            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                                                title = c.getTitle_cn();
                                                imagePath = GlobalConst.PATH_SDCARD.concat(c.getClientpath()).concat(c.getFilename());
                                            } else if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                                audioPath = c.getClientpath().concat(c.getFilename());
                                            }
                                            break;

                                        case GlobalConst.DEFAULT_LANG_EN:
                                            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                                                title = c.getTitle_en();
                                                imagePath = GlobalConst.PATH_SDCARD.concat(c.getClientpath()).concat(c.getFilename());
                                            } else if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                                audioPath = c.getClientpath().concat(c.getFilename());
                                            }
                                            break;

                                        case GlobalConst.DEFAULT_LANG_TW:
                                            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                                                title = c.getTitle_tw();
                                                imagePath = GlobalConst.PATH_SDCARD.concat(c.getClientpath()).concat(c.getFilename());
                                            } else if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                                audioPath = c.getClientpath().concat(c.getFilename());
                                            }
                                            break;

                                        case GlobalConst.DEFAULT_LANG_PT:
                                            if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                                                title = c.getTitle_pt();
                                                imagePath = GlobalConst.PATH_SDCARD.concat(c.getClientpath()).concat(c.getFilename());
                                            } else if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                                audioPath = c.getClientpath().concat(c.getFilename());
                                            }
                                            break;
                                    }
                                }

                                String mKey = m.getValue().getBeaconId();
                                if (!myRectMap.containsKey(mKey)) {
                                    MyRect myRect = new MyRect(m.getValue().getRssi(), tag, title, imagePath, audioPath, mKey, System.currentTimeMillis());
                                    myRectMap.put(mKey, myRect);
                                    myRectList.add(myRect);

                                } else {
                                    myRectMap.get(mKey).RSSI = m.getValue().getRssi();
                                    myRectMap.get(mKey).CreateTimeTicks = System.currentTimeMillis();
                                }
                            }
                        }
                    }
                }
            }

            Collections.sort(myRectList);

            for (MyRect mr : myRectList) {
                if (System.currentTimeMillis() - mr.CreateTimeTicks <= 5000) {
                    beaconMap = new HashMap<String, String>();
                    beaconMap.put("tag", mr.Tag);
                    beaconMap.put("title", mr.Title);
                    beaconMap.put("imagePath", mr.ImagePath);
                    beaconMap.put("beaconId", mr.BeaconId);
                    beaconMap.put("audioPath", mr.AudioPath);
                    beaconList.add(beaconMap);
                }
            }

            mBeaconListAdapter = new BeaconListAdapter(this, beaconList);
            beaconListView.setAdapter(mBeaconListAdapter);

            if (myApp.logonUser.earphonePlay.equals(GlobalConst.EARPHONE_PLAY_ON) && !myApp.headSetConnected) {
                myApp.stopSound();
                //Toast.makeText(LockScreenActivity.this, getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DlgEvent {
        public String beaconid;
        public int mode;//0->手动,1->自动

        public DlgEvent(String beaconid, int mode) {
            this.beaconid = beaconid;
            this.mode = mode;
        }
    }

    private void showPaintDialog(DlgEvent dlgEvent) {
        String bid = dlgEvent.beaconid;
        int mode = dlgEvent.mode;

        if (mode == 0) {
            myApp.stopSound();
        }

        //如果列表存在
        if (myApp.actionList.containsKey(bid)) {
            //如果已经在显示中
            if (paintDlg.getVisibility() == View.VISIBLE) {
                //如果显示的ID不一样则关闭当前窗口
                if (!myApp.lastPlayingId.equals(bid)) {
                    try {
                        paintDlg.setVisibility(View.INVISIBLE);
                        ballLayout.setVisibility(View.VISIBLE);
                        iv_key.setVisibility(View.VISIBLE);
                        //停止播放语音
                        myApp.stopSound();
                        //重新打开窗口
                        showPaintDialog(dlgEvent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //如果未显示则找出对应的图片、说明、音频并播放(播放前先停止后台播放音频及当前播放音频)
                View dlgView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog, null);

                mExtag = myApp.beaconList.get(bid).getExtag();
                Iterator<content> it = myApp.actionList.get(bid).iterator();
                final favorite fav = new favorite();
                fav.setExtag(mExtag);

                extag exTag = extagList.get(myApp.currentExTagIndex);

                //查找匹配项
                while (it.hasNext()) {
                    content c = it.next();
                    if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                        fav.setRefImageId(c.getContentid());
                        shareImagePath = c.getClientpath().concat(c.getFilename());
                        switch (myApp.logonUser.defaultLang) {
                            case GlobalConst.DEFAULT_LANG_CN:
                                title = c.getTitle_cn();
                                imageDesc = c.getDescription_cn();
                                shareLocale = "cn";
                                shareExTitle = exTag.getTitle_cn();
                                break;

                            case GlobalConst.DEFAULT_LANG_EN:
                                title = c.getTitle_en();
                                imageDesc = c.getDescription_en();
                                shareLocale = "en";
                                shareExTitle = exTag.getTitle_en();
                                break;

                            case GlobalConst.DEFAULT_LANG_TW:
                                title = c.getTitle_tw();
                                imageDesc = c.getDescription_tw();
                                shareLocale = "tw";
                                shareExTitle = exTag.getTitle_tw();
                                break;

                            case GlobalConst.DEFAULT_LANG_PT:
                                title = c.getTitle_pt();
                                imageDesc = c.getDescription_pt();
                                shareLocale = "pt";
                                shareExTitle = exTag.getTitle_pt();
                                break;
                        }
                    } else if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                        fav.setRefAudioId(c.getContentid());
                    }
                }

                WebView wv = (WebView) dlgView.findViewById(R.id.dialog_webview);
                wv.getSettings().setJavaScriptEnabled(true);
                wv.clearCache(true);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        //startActivity(intent);
                        return true;
                    }
                });

                String html = imageDesc.replace("{appuser}", myApp.logonUser.userId);

                Pattern p = Pattern.compile("\\{/.*\\}");
                Matcher m = p.matcher(html);
                while (m.find()) {
                    html = html.replace(m.group(0)
                            , "file://"
                            .concat(Environment.getExternalStorageDirectory().getPath())
                            .concat(m.group(0).replace("{", "").replace("}", "")));
                }

                Pattern p1 = Pattern.compile("url[(](.*)[)]");
                Matcher m1 = p1.matcher(html);
                while (m1.find()) {
                    if (m1.group(1).indexOf("file://") < 0) {
                        html = html.replace(m1.group(1)
                                , "file://"
                                .concat(Environment.getExternalStorageDirectory().getPath())
                                .concat(shareImagePath.substring(0, shareImagePath.lastIndexOf("/") + 1))
                                .concat(m1.group(1)));
                    }
                }

                if(html.indexOf("flag='##'")>=0) {
                    wv.loadUrl(html.replace("<script>var flag='##';window.location.href='", "").replace("';</script>", ""));
                }
                else {
                    wv.loadDataWithBaseURL("http://arts.things.buzz", html, "text/html", "utf-8", null);
                }

                ImageButton ibtn = (ImageButton) dlgView.findViewById(R.id.dlg_btnBack);
                ibtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paintDlg.setVisibility(View.INVISIBLE);
                        ballLayout.setVisibility(View.VISIBLE);
                        iv_key.setVisibility(View.VISIBLE);
                        //停止播放语音
                        myApp.stopSound();
                    }
                });

                //播放音频
                content audContent = myApp.contentList.get(fav.getRefAudioId());
                String audContentPath = (audContent != null) ? audContent.getClientpath().concat(audContent.getFilename()) : "";

                Button btnAudio = (Button) dlgView.findViewById(R.id.dlg_audio_btn);
                btnAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (audContent == null) {
                            Toast.makeText(LockScreenActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
                            return;
                        }
                        playAudio(audContentPath);
                    }
                });

                Button btnPlayOrPause = (Button) dlgView.findViewById(R.id.dlg_audio_play_pause);
                btnPlayOrPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myApp.playOrPauseSound(audContentPath);
                    }
                });

                final Button btnFavorite = (Button) dlgView.findViewById(R.id.dlg_favorite_btn);
                Button btnMenu = (Button) dlgView.findViewById(R.id.dlg_btn_menu);
                btnMenu.setVisibility(View.GONE);

                Button btnShare = (Button) dlgView.findViewById(R.id.dlg_share_btn);
                btnShare.setVisibility(View.INVISIBLE);
                /*
                if (ConfigHelper.getInstance(getApplicationContext()).getProfile().equals("")) {
                    btnShare.setVisibility(View.INVISIBLE);
                } else {
                    btnShare.setVisibility(View.VISIBLE);
                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myApp.wxShared(
                                    String.format(myApp.sharePageUrl, fav.getRefImageId(), shareLocale)
                                    , shareExTitle, title, shareImagePath
                            );
                        }
                    });
                }*/

                //已收藏显示实心图标
                if (findFavoriteInList(fav)) {
                    btnFavorite.setBackground(getResources().getDrawable(R.drawable.liked));
                } else {
                    //未收藏显示空心图标并添加收藏事件
                    btnFavorite.setBackground(getResources().getDrawable(R.drawable.favorite));
                }

                btnFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (findFavoriteInList(fav)) {
                            myApp.removeFavoriteFromList(fav);
                            btnFavorite.setBackground(getResources().getDrawable(R.drawable.favorite));
                        } else {
                            try {
                                myApp.addToFavorite(myApp.objectMapper.writeValueAsString(fav) + ",");
                                Toast.makeText(LockScreenActivity.this, getString(R.string.msg_add_to_favorite_success), Toast.LENGTH_LONG).show();
                                btnFavorite.setBackground(getResources().getDrawable(R.drawable.liked));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                //显示自定义样式窗口dialog.xml + <style name="PopupDialog">
                paintDlg.addView(dlgView);
                try {
                    paintDlg.setVisibility(View.VISIBLE);
                    ballLayout.setVisibility(View.INVISIBLE);
                    iv_key.setVisibility(View.INVISIBLE);
                    if (mode == 1) {
                        if (!myApp.lastPlayingId.equals(bid)) {
                            btnAudio.callOnClick();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean findFavoriteInList(favorite fav) {
        for (favorite f : myApp.getFavoriteList()) {
            if (f.equals(fav)) {
                return true;
            }
        }

        return false;
    }
}
