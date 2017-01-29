package com.buzz.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buzz.models.MyRect;
import com.buzz.models.beacon;
import com.buzz.models.content;
import com.buzz.models.extag;
import com.buzz.models.favorite;
import com.buzz.utils.GlobalConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NickChung on 12/20/16.
 */
public class PreviewActivity extends Activity {

    final String TAG = this.getClass().getSimpleName();
    Map<String, String> beaconMap;
    BeaconListAdapter mBeaconListAdapter;
    MyApplication myApp;
    Map<String, MyRect> myRectMap;
    List<MyRect> myRectList;
    List<Map<String, String>> beaconList;
    ListView beaconListView;
    TextView tvExTitle;
    String mExTag;
    Dialog paintDlg;
    Button iconBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        paintDlg = new Dialog(PreviewActivity.this, R.style.PopupDialog);
        paintDlg.setCancelable(false);

        mExTag = this.getIntent().getStringExtra("exTag");
        myApp = (MyApplication) getApplication();
        myRectMap = new HashMap<String, MyRect>();
        myRectList = new ArrayList<MyRect>();
        beaconList = new ArrayList<Map<String, String>>();

        tvExTitle = (TextView) findViewById(R.id.preview_activity_txt_ex_title);
        tvExTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/futura.ttf"));
        String sourceText, convertText;
        switch (myApp.logonUser.defaultLang) {
            case GlobalConst.DEFAULT_LANG_CN:
                sourceText = myApp.extagList.get(mExTag).getTitle_cn();
                if (sourceText.length() > 15) {
                    convertText = sourceText.substring(0, 15).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;

            case GlobalConst.DEFAULT_LANG_EN:
                sourceText = myApp.extagList.get(mExTag).getTitle_en();
                if (sourceText.length() > 25) {
                    convertText = sourceText.substring(0, 25).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;

            case GlobalConst.DEFAULT_LANG_TW:
                sourceText = myApp.extagList.get(mExTag).getTitle_tw();
                if (sourceText.length() > 15) {
                    convertText = sourceText.substring(0, 15).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;

            case GlobalConst.DEFAULT_LANG_PT:
                sourceText = myApp.extagList.get(mExTag).getTitle_pt();
                if (sourceText.length() > 25) {
                    convertText = sourceText.substring(0, 25).concat("...");
                } else {
                    convertText = sourceText;
                }
                tvExTitle.setText(convertText);
                break;
        }

        //Beacon列表
        beaconListView = (ListView) findViewById(R.id.preview_activity_beacon_list_lv);
        beaconListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BeaconListAdapter myBeaconAdapter = (BeaconListAdapter) parent.getAdapter();
                Map<String, Object> map = (Map<String, Object>) myBeaconAdapter.getItem(position);
                showPaintDialog(map.get("beaconId").toString(), 0);
            }
        });

        iconBack = (Button) findViewById(R.id.preview_activity_btn_back);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconBack.setEnabled(false);
                String root = getIntent().getStringExtra("root");
                if (root.equals("ex")) {
                    Intent it = new Intent();
                    it.setClass(PreviewActivity.this, ExhibitionActivity.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
                    PreviewActivity.this.finish();
                } else if (root.equals("main")) {
                    Intent it = new Intent();
                    it.setClass(PreviewActivity.this, MainActivity.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
                    PreviewActivity.this.finish();
                }
            }
        });

        updateBallListView(mExTag);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateBallListView(String exTag) {
        String title = "";
        String imagePath = "";
        String tag;
        String audioPath = "";

        beaconList.clear();

        for (beacon b : myApp.beaconList.values()) {
            if (b.getExtag().equals(exTag)) {
                String direction = b.getRangedirection();
                if ((direction.equals(GlobalConst.RANGE_DIRECTION_FRONT)
                        || direction.equals(GlobalConst.RANGE_DIRECTION_BOTH))
                        && b.getUsage().equals(GlobalConst.BEACON_USAGE_DETAIL)) {

                    tag = b.getDisplayname();
                    Iterator it = myApp.actionList.get(b.getBeaconid()).iterator();

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

                    String mKey = b.getBeaconid();
                    MyRect myRect = new MyRect(-100, tag, title, imagePath, audioPath, mKey, System.currentTimeMillis());
                    myRectMap.put(mKey, myRect);
                    myRectList.add(myRect);
                }
            }
        }

        Collections.sort(myRectList);

        for (MyRect mr : myRectList) {
            beaconMap = new HashMap<String, String>();
            beaconMap.put("tag", mr.Tag);
            beaconMap.put("title", mr.Title);
            beaconMap.put("imagePath", mr.ImagePath);
            beaconMap.put("beaconId", mr.BeaconId);
            beaconMap.put("audioPath", mr.AudioPath);
            beaconList.add(beaconMap);
        }

        mBeaconListAdapter = new BeaconListAdapter(this, beaconList);
        beaconListView.setAdapter(mBeaconListAdapter);
    }

    private void showPaintDialog(String bid, int mode) {
        if (mode == 0) {
            myApp.stopSound();
        }

        String imageDesc = "";
        String pImagePath = "";

        //如果列表存在
        if (myApp.actionList.containsKey(bid)) {
            //如果未显示则找出对应的图片、说明、音频并播放(播放前先停止后台播放音频及当前播放音频)
            View dlgView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog, null);

            Iterator<content> it = myApp.actionList.get(bid).iterator();
            final favorite fav = new favorite();
            fav.setExtag(mExTag);

            extag exTag = myApp.extagList.get(mExTag);

            //查找匹配项
            while (it.hasNext()) {
                content c = it.next();
                if (c.getContenttype() == GlobalConst.CONTENT_TYPE_IMAGE) {
                    fav.setRefImageId(c.getContentid());
                    pImagePath = c.getClientpath().concat(c.getFilename());
                    switch (myApp.logonUser.defaultLang) {
                        case GlobalConst.DEFAULT_LANG_CN:
                            imageDesc = c.getDescription_cn();
                            break;

                        case GlobalConst.DEFAULT_LANG_EN:
                            imageDesc = c.getDescription_en();
                            break;

                        case GlobalConst.DEFAULT_LANG_TW:
                            imageDesc = c.getDescription_tw();
                            break;

                        case GlobalConst.DEFAULT_LANG_PT:
                            imageDesc = c.getDescription_pt();
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
                            .concat(pImagePath.substring(0, pImagePath.lastIndexOf("/") + 1))
                            .concat(m1.group(1)));
                }
            }

            if (html.indexOf("flag='##'") >= 0) {
                wv.loadUrl(html.replace("<script>var flag='##';window.location.href='", "").replace("';</script>", ""));
            } else {
                wv.loadDataWithBaseURL("http://arts.things.buzz", html, "text/html", "utf-8", null);
            }

            ImageButton ibtn = (ImageButton) dlgView.findViewById(R.id.dlg_btnBack);
            ibtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paintDlg.dismiss();
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
                        Toast.makeText(PreviewActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
                        return;
                    }
                    playAudio(audContentPath);
                }
            });

            final Button btnFavorite = (Button) dlgView.findViewById(R.id.dlg_favorite_btn);
            Button btnMenu = (Button) dlgView.findViewById(R.id.dlg_btn_menu);
            btnMenu.setVisibility(View.GONE);

            Button btnShare = (Button) dlgView.findViewById(R.id.dlg_share_btn);

            btnShare.setVisibility(View.INVISIBLE);

            Button btnPlayOrPause = (Button) dlgView.findViewById(R.id.dlg_audio_play_pause);
            btnPlayOrPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myApp.playOrPauseSound(audContentPath);
                }
            });

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
                            Toast.makeText(PreviewActivity.this, getString(R.string.msg_add_to_favorite_success), Toast.LENGTH_LONG).show();
                            btnFavorite.setBackground(getResources().getDrawable(R.drawable.liked));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            paintDlg.setContentView(dlgView);
            try {
                paintDlg.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                    Toast.makeText(PreviewActivity.this, getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
                }
            } else {
                myApp.playSound(realPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

