package com.buzz.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.buzz.fonts.FuturaTextView;
import com.buzz.fragment.CatalogItemFragment;
import com.buzz.impl.VideoEnabledWebView;
import com.buzz.layout.BidirSlidingLayout;
import com.buzz.layout.CustomDialog;
import com.buzz.layout.CustomProgressDialog;
import com.buzz.models.content;
import com.buzz.models.extag;
import com.buzz.service.BeaconReader;
import com.buzz.service.MessageService;
import com.buzz.service.CoreService;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.models.favorite;
import com.buzz.models.download;
import com.buzz.models.beacon;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Created by buzz on 2015/5/5.
 */
public class ExhibitionActivity extends FragmentActivity implements CatalogItemFragment.OnEventCallBackListener {
    final static String TAG = ExhibitionActivity.class.getSimpleName();
    private static final int SWIPE_MIN_DISTANCE = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    MyApplication myApp;
    ProgressBar progressBar;
    ProgressBar progressBarInit;
    int fileCounter = 0;
    Map<String, MyTask> taskList;
    FuturaTextView title;
    FuturaTextView nav_left;
    FuturaTextView nav_right;
    WebView desc;
    RelativeLayout background;
    List<extag> extagList;
    Runnable runnable;
    Handler handler;
    long firstTime = 0;
    Dialog paintDlg;
    boolean canSwipe = false;
    boolean initDone = false;
    favorite f;
    favorite delFav;
    download d;
    download delDown;
    Button btnFavor;
    Button btnDownload;
    Button btnIconDownload;
    Button btnIconPreview;
    String lvMode = "f";
    CustomProgressDialog mProgressDialog;
    CustomDialog customDialog;
    BidirSlidingLayout bidirSldingLayout;
    ListView listView;
    GestureDetector detector;
    IntentFilter intentFilter;
    download subDownload;
    HeadsetPlugReceiver headsetPlugReceiver;
    StopMediaReceiver stopMediaReceiver;
    Handler readerHandler;
    Runnable readerRunnable;
    FuturaTextView tvHeaderTitle;
    Button btnMenu;
    RelativeLayout ex_context;
    ViewFlipper vf;
    Handler btCheckHandler;
    Runnable btCheckRunnable;
    Button btnSignalGo;
    Button btnProfile;
    Handler wxHandler;
    Runnable wxRunnable;
    boolean isProfileActive = false;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private boolean canPresentShareDialog;
    private ShareDialog shareDialog;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.i(TAG, "Share Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.i(TAG, String.format("Share Error: %s", error.toString()));
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.i(TAG, "Share Success!");
        }
    };

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<Fragment>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exhibition);

        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        myApp = (MyApplication) getApplication();
        myApp.currentExTagIndex = -1;
        extagList = new ArrayList<extag>();

        if (!myApp.btReady) {
            //打开蓝牙
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        }

        readerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!myApp.access) {
                    if (!myApp.downloading) {
                    }
                    readerHandler.postDelayed(this, 100);
                }
            }
        };
        readerHandler = new Handler();
        readerHandler.post(readerRunnable);

        myApp.startCoreService();
        btnSignalGo = (Button) findViewById(R.id.exhibition_btn_signal_go);
        btnSignalGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到小球页面
                Intent it = new Intent();
                it.setClass(ExhibitionActivity.this, MainActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);

                myApp.access = true;

                //停止播放语音
                myApp.stopSound();

                ExhibitionActivity.this.finish();
            }
        });
        btCheckHandler = new Handler();
        btCheckRunnable = new Runnable() {
            @Override
            public void run() {
                btnSignalGo.setVisibility(View.INVISIBLE);
                myApp.checkBluetooth();
                if (myApp.btReady) {
                    for (Map.Entry<String, BeaconReader> m : myApp.readerMap.entrySet()) {
                        beacon b = myApp.beaconList.get(m.getKey());
                        if (b != null) {
                            if (extagList.size() > 0) {
                                extag[] sortedList = extagList.toArray(new extag[]{});
                                Arrays.sort(sortedList);
                                if (myApp.currentExTagIndex != -1) {
                                    extag exTemp = sortedList[myApp.currentExTagIndex];
                                    if (exTemp != null) {
                                        if (b.getExtag().equals(exTemp.getExtag())
                                                && b.getBeaconid().equals(m.getValue().getBeaconId())) {
                                            btnSignalGo.setVisibility(View.VISIBLE);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                btCheckHandler.postDelayed(this, 1000);
            }
        };
        btCheckHandler.post(btCheckRunnable);

        //收藏对话框
        paintDlg = new Dialog(ExhibitionActivity.this, R.style.PopupDialog);
        paintDlg.setCancelable(false);

        //下载进度对话框
        //mProgressDialog = new ProgressDialog(this);
        mProgressDialog = new CustomProgressDialog(this, R.style.CustomDialog);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getString(R.string.msg_file_downloading));
        //mProgressDialog.setIcon(R.drawable.icon_download);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);

        detector = new GestureDetector(this, new MyGestureDetector());

        listView = (ListView) findViewById(R.id.right_menu_lv);

        //长按
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MyAdspter myAdspter = (MyAdspter) parent.getAdapter();
                Map<String, Object> map = (Map<String, Object>) myAdspter.getItem(position);
                //下载模式
                if (lvMode.equals("d")) {
                    delDown = (download) map.get("download");
                    //下载完成才能进行操作
                    if (delDown.isFinished()) {
                        CustomDialog.Builder builder = new CustomDialog.Builder(ExhibitionActivity.this);
                        builder.setTitle(getString(R.string.msg_dlg_title_tips));
                        builder.setMessage(getString(R.string.msg_delete_all_files));
                        builder.setConfirmButton(getString(R.string.msg_dlg_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //删除所有相关文件
                                String[] suffix = new String[]{"_cc.mp3", "_en.mp3", "_sc.mp3", "_pt.mp3"};
                                for (content c : delDown.getContentList()) {
                                    if (c.getClientpath() != null && c.getServerpath() != null) {
                                        //音频文件需转换为真实文件后删除
                                        if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                                            for (String s : suffix) {
                                                String realFileName = c.getFilename().replace(".mp3", s);
                                                myApp.fileHelper.deleteFile(c.getClientpath().concat(realFileName));
                                            }
                                        } else {
                                            //图片则直接删除
                                            myApp.fileHelper.deleteFile(c.getClientpath().concat(c.getFilename()));
                                        }
                                    }
                                }

                                //删除配置文件后重新初始化
                                delDown.deleteConfigThenReInit();

                                //更新下载列表
                                myApp.updateDownloadList();

                                //删除相关收藏
                                List<favorite> currentFavList = myApp.getFavoriteList();
                                List<favorite> deleteFavList = new ArrayList<favorite>();

                                for (favorite f : currentFavList) {
                                    if (f.getExtag().equals(delDown.getDextag().getExtag())) {
                                        deleteFavList.add(f);
                                    }
                                }

                                myApp.updateFavoriteList(currentFavList, deleteFavList);

                                //刷新下载列表
                                showNextOrPrevEx(2);
                                btnDownload.callOnClick();
                                customDialog.dismiss();
                            }
                        });

                        builder.setBackButton(getString(R.string.msg_dlg_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                customDialog.dismiss();
                            }
                        });

                        customDialog = builder.create();
                        customDialog.show();
                    }
                }
                //收藏模式
                else if (lvMode.equals("f")) {
                    delFav = (favorite) map.get("favorite");
                    CustomDialog.Builder builder = new CustomDialog.Builder(ExhibitionActivity.this);
                    builder.setTitle(getString(R.string.msg_dlg_title_tips));
                    builder.setMessage(getString(R.string.msg_delete_favorite));
                    builder.setConfirmButton(getString(R.string.msg_dlg_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除收藏
                            myApp.removeFavoriteFromList(delFav);
                            //刷新收藏列表
                            btnFavor.callOnClick();
                            customDialog.dismiss();
                        }
                    });

                    builder.setBackButton(getString(R.string.msg_dlg_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            customDialog.dismiss();
                        }
                    });

                    customDialog = builder.create();
                    customDialog.show();
                }
                return true;
            }
        });

        //单击
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyAdspter myAdspter = (MyAdspter) parent.getAdapter();
                Map<String, Object> map = (Map<String, Object>) myAdspter.getItem(position);

                //收藏模式
                if (lvMode.equals("f")) {
                    f = (favorite) map.get("favorite");
                    String imageDesc = "";
                    View dlgView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog, null);
                    content imgContent = myApp.contentList.get(f.getRefImageId());
                    if (imgContent == null) {
                        Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
                        return;
                    }
                    switch (myApp.logonUser.defaultLang) {
                        case GlobalConst.DEFAULT_LANG_CN:
                            imageDesc = imgContent.getDescription_cn();
                            break;

                        case GlobalConst.DEFAULT_LANG_EN:
                            imageDesc = imgContent.getDescription_en();
                            break;

                        case GlobalConst.DEFAULT_LANG_TW:
                            imageDesc = imgContent.getDescription_tw();
                            break;

                        case GlobalConst.DEFAULT_LANG_PT:
                            imageDesc = imgContent.getDescription_pt();
                            break;
                    }

                    LinearLayout linearLayout = (LinearLayout) dlgView.findViewById(R.id.dlg_imgView);
                    linearLayout.setBackground(Drawable.createFromPath(
                            GlobalConst.PATH_SDCARD.concat(imgContent.getClientpath()).concat(imgContent.getFilename())));

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
                    Button btnFavorDlg = (Button) dlgView.findViewById(R.id.dlg_favorite_btn);
                    Button btnMenu = (Button) dlgView.findViewById(R.id.dlg_btn_menu);
                    Button btnShare = (Button) dlgView.findViewById(R.id.dlg_share_btn);

                    if(ConfigHelper.getInstance(getApplicationContext()).getProfile().equals("")
                            && myApp.fbName.equals("")) {
                        btnShare.setVisibility(View.INVISIBLE);
                    }
                    else {
                        btnShare.setVisibility(View.VISIBLE);
                        btnShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> itemList = new ArrayList<String>();
                                boolean wxReady = !ConfigHelper.getInstance(getApplicationContext()).getProfile().equals("");
                                boolean fbReady = !myApp.fbName.equals("");
                                if(wxReady) {
                                    itemList.add(getString(R.string.lbl_share_to_wechat));
                                }
                                if(fbReady) {
                                    itemList.add(getString(R.string.lbl_share_to_facebook));
                                }
                                final int itemListSize = itemList.size();
                                final String[] sharePlatform = (String[])itemList.toArray(new String[itemListSize]);

                                Dialog alertDialog = new AlertDialog.Builder(ExhibitionActivity.this,R.style.ShareTheme)
                                        .setTitle(getString(R.string.lbl_share))
                                        .setIcon(R.drawable.share)
                                        .setItems(sharePlatform, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                String title = "", exTitle = "", locale = "";
                                                extag exTag = myApp.extagList.get(f.getExtag());
                                                switch (myApp.logonUser.defaultLang) {
                                                    case GlobalConst.DEFAULT_LANG_CN:
                                                        locale = "cn";
                                                        title = imgContent.getTitle_cn();
                                                        exTitle = exTag.getTitle_cn();
                                                        break;

                                                    case GlobalConst.DEFAULT_LANG_EN:
                                                        locale = "en";
                                                        title = imgContent.getTitle_en();
                                                        exTitle = exTag.getTitle_en();
                                                        break;

                                                    case GlobalConst.DEFAULT_LANG_TW:
                                                        locale = "tw";
                                                        title = imgContent.getTitle_tw();
                                                        exTitle = exTag.getTitle_tw();
                                                        break;

                                                    case GlobalConst.DEFAULT_LANG_PT:
                                                        locale = "pt";
                                                        title = imgContent.getTitle_pt();
                                                        exTitle = exTag.getTitle_pt();
                                                        break;
                                                }
                                                if (which == 0) {
                                                    if(wxReady) {
                                                        myApp.wxShared(
                                                                String.format(myApp.sharePageUrl, f.getRefImageId(), locale)
                                                                , exTitle, title, imgContent.getClientpath().concat(imgContent.getFilename())
                                                        );
                                                    }
                                                    else{
                                                        Profile profile = Profile.getCurrentProfile();
                                                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                                .setContentTitle(String.format("%s@%s", title, exTitle))
                                                                .setContentDescription("")
                                                                .setContentUrl(Uri.parse(String.format(myApp.sharePageUrl, f.getRefImageId(), locale)))
                                                                .setImageUrl(Uri.parse(imgContent.getServerpath()))
                                                                .build();
                                                        if (canPresentShareDialog) {
                                                            shareDialog.show(linkContent);
                                                        } else if (profile != null && hasPublishPermission()) {
                                                            ShareApi.share(linkContent, shareCallback);
                                                        }
                                                    }
                                                } else {
                                                    Profile profile = Profile.getCurrentProfile();
                                                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                            .setContentTitle(String.format("%s@%s", title, exTitle))
                                                            .setContentDescription("")
                                                            .setContentUrl(Uri.parse(String.format(myApp.sharePageUrl, f.getRefImageId(), locale)))
                                                            .setImageUrl(Uri.parse(imgContent.getServerpath()))
                                                            .build();
                                                    if (canPresentShareDialog) {
                                                        shareDialog.show(linkContent);
                                                    } else if (profile != null && hasPublishPermission()) {
                                                        ShareApi.share(linkContent, shareCallback);
                                                    }
                                                }

                                            }
                                        })
                                        .setNegativeButton(getString(R.string.msg_dlg_cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                                dialog.dismiss();
                                            }
                                        })
                                        .create();

                                alertDialog.getWindow().setGravity(Gravity.BOTTOM);
                                alertDialog.show();
                            }
                        });
                    }

                    btnMenu.setVisibility(View.GONE);

                    String html = imageDesc.replace("{appuser}", myApp.logonUser.userId);
                    html = html.replace("{locale}", myApp.getLocaleString(myApp.logonUser.defaultLang));

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
                                    .concat(imgContent.getClientpath())
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
                            paintDlg.dismiss();
                            //停止播放语音
                            myApp.stopSound();

                        }
                    });

                    //播放音频
                    content audContent = myApp.contentList.get(f.getRefAudioId());
                    String audContentPath = (audContent != null) ? audContent.getClientpath().concat(audContent.getFilename()) : "";

                    Button btnAudio = (Button) dlgView.findViewById(R.id.dlg_audio_btn);
                    btnAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (audContent == null) {
                                Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
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

                    //已收藏显示实心图标
                    if (findFavoriteInList(f)) {
                        btnFavorDlg.setBackground(getResources().getDrawable(R.drawable.liked));
                    } else {
                        //未收藏显示空心图标并添加收藏事件
                        btnFavorDlg.setBackground(getResources().getDrawable(R.drawable.favorite));
                    }

                    btnFavorDlg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (findFavoriteInList(f)) {
                                myApp.removeFavoriteFromList(f);
                                btnFavorDlg.setBackground(getResources().getDrawable(R.drawable.favorite));
                            } else {
                                try {
                                    myApp.addToFavorite(myApp.objectMapper.writeValueAsString(f) + ",");
                                    Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_add_to_favorite_success), Toast.LENGTH_LONG).show();
                                    btnFavorDlg.setBackground(getResources().getDrawable(R.drawable.liked));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            btnFavor.callOnClick();
                        }
                    });

                    paintDlg.setContentView(dlgView);
                    paintDlg.show();
                }
                //下载模式
                else if (lvMode.equals("d")) {
                    d = (download) map.get("download");
                    if (!d.isFinished()) {
                        CustomDialog.Builder builder = new CustomDialog.Builder(ExhibitionActivity.this);
                        builder.setTitle(getString(R.string.msg_dlg_title_tips));
                        builder.setMessage(String.format(getString(R.string.msg_dlg_download_confirm),map.get("title").toString()));
                        builder.setConfirmButton(getString(R.string.msg_dlg_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadExFiles(d, map.get("title").toString());
                                customDialog.dismiss();
                            }
                        });

                        builder.setBackButton(getString(R.string.msg_dlg_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                customDialog.dismiss();
                            }
                        });

                        customDialog = builder.create();
                        customDialog.show();
                    }
                    else {
                        //preview
                        Intent previewIntent = new Intent(ExhibitionActivity.this, PreviewActivity.class);
                        previewIntent.putExtra("exTag", d.getDextag().getExtag());
                        previewIntent.putExtra("root", "ex");
                        startActivity(previewIntent);
                    }
                }
                //个人资料模式
                else if (lvMode.equals("p")) {
                    String type = map.get("type").toString();
                    if(type.equals("wechat")) {
                        if (ConfigHelper.getInstance(getApplicationContext()).getProfile().equals("")) {
                            myApp.wxLogin();
                        }
                    }
                    else {
                        if (!Boolean.valueOf(map.get("link").toString())) {
                            if (!getAccessToken()) {
                                LoginManager.getInstance().logInWithReadPermissions(ExhibitionActivity.this, Arrays.asList("public_profile"));
                            }
                        }
                        else{
                            //for debug
                            //LoginManager.getInstance().logOut();
                        }
                    }
                }
            }
        });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
            }
        };
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.i(TAG, "Login Success!");
                        updateUI();
                    }

                    @Override
                    public void onCancel() {
                        Log.i(TAG, "Login Canceled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i(TAG, String.format("Login Error: %s", exception.getMessage()));
                    }

                });

        // Can we present the share dialog for regular links?
        canPresentShareDialog = ShareDialog.canShow(
                ShareLinkContent.class);

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);

        btnFavor = (Button) findViewById(R.id.right_menu_btn_favor);
        btnDownload = (Button) findViewById(R.id.right_menu_btn_download);
        btnIconDownload = (Button) findViewById(R.id.exhibition_btn_download);
        btnIconPreview = (Button) findViewById(R.id.exhibition_btn_preview);

        btnProfile = (Button) findViewById(R.id.right_menu_btn_profile);
        wxHandler = new Handler();
        wxRunnable = new Runnable() {
            @Override
            public void run() {
                if (myApp.wxLoginResultCode == 0 || !myApp.fbName.equals("")) {
                    if (isProfileActive) {
                        btnProfile.callOnClick();
                    }
                }
                wxHandler.postDelayed(this, 1000);
            }
        };
        wxHandler.post(wxRunnable);

        progressBar = (ProgressBar) findViewById(R.id.exhibition_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
        taskList = new HashMap<String, MyTask>();

        background = (RelativeLayout) findViewById(R.id.exhibition_background);
        title = (FuturaTextView) findViewById(R.id.exhibition_title);
        nav_left = (FuturaTextView) findViewById(R.id.exhibition_nav_left);
        nav_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextOrPrevEx(0);
            }
        });
        nav_right = (FuturaTextView) findViewById(R.id.exhibition_nav_right);
        nav_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextOrPrevEx(1);
            }
        });
        tvHeaderTitle = (FuturaTextView) findViewById(R.id.right_menu_header_title);
        desc = (WebView) findViewById(R.id.exhibition_desc);
        desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canSwipe) {
                    btnMenu.callOnClick();
                }
            }
        });
        ex_context = (RelativeLayout) findViewById(R.id.ex_context);
        ex_context.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canSwipe) {
                    btnMenu.callOnClick();
                }
            }
        });

        //右侧菜单
        bidirSldingLayout = (BidirSlidingLayout) findViewById(R.id.exhibition);
        bidirSldingLayout.setScrollEvent(findViewById(R.id.right_menu));
        vf = (ViewFlipper) findViewById(R.id.activity_exhibition_vf);
        btnMenu = (Button) findViewById(R.id.exhibition_btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (initDone) {
                        if (bidirSldingLayout.isLeftLayoutVisible()) {
                            bidirSldingLayout.scrollToContentFromLeftMenu();
                            canSwipe = true;
                        } else {
                            bidirSldingLayout.initShowLeftState();
                            bidirSldingLayout.scrollToLeftMenu();
                            canSwipe = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        progressBarInit = (ProgressBar) findViewById(R.id.exhibition_progress_bar_init);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                progressBarInit.setVisibility(View.INVISIBLE);
                if (myApp.extagList.size() > 0) {
                    initDone = true;
                    //如果没有展览数据则下载
                    taskList.clear();
                    for (extag et : myApp.extagList.values()) {
                        if (et.getContent() != null) {
                            if (!myApp.fileHelper.isFileExist(et.getContent().getClientpath().concat(et.getContent().getFilename()))) {
                                if (et.getContent().getServerpath() != null) {
                                    taskList.put(et.getContent().getServerpath()
                                            , new MyTask(et.getContent().getClientpath(), et.getContent().getFilename()));
                                }
                            }

                            //extra catalog images
                            if (et.getLocation() != null) {
                                if (et.getContent().getServerpath() != null) {
                                    String serverPath = et.getContent().getServerpath();
                                    if (et.getLocation().indexOf(",") >= 0) {
                                        String[] imgGroup = et.getLocation().split(",");
                                        for (String imgName : imgGroup) {
                                            if (!myApp.fileHelper.isFileExist(et.getContent().getClientpath().concat(imgName + ".png"))) {
                                                taskList.put(serverPath.substring(0, serverPath.lastIndexOf("/") + 1) + imgName + ".png"
                                                        , new MyTask(et.getContent().getClientpath(), imgName + ".png"));
                                            }
                                        }
                                    } else {
                                        if (!myApp.fileHelper.isFileExist(et.getContent().getClientpath().concat(et.getLocation() + ".png"))) {
                                            taskList.put(serverPath.substring(0, serverPath.lastIndexOf("/") + 1) + et.getLocation() + ".png"
                                                    , new MyTask(et.getContent().getClientpath(), et.getLocation() + ".png"));
                                        }
                                    }
                                }
                            }
                        }

                        extagList.add(et);
                    }

                    if (taskList.size() > 0) {
                        initDone = false;
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_catalog_loading), Toast.LENGTH_LONG).show();
                    }

                    for (Map.Entry<String, MyTask> entry : taskList.entrySet()) {
                        entry.getValue().execute(entry.getKey());
                    }
                }

                if (initDone) {
                    canSwipe = true;
                    //显示最近的展览
                    if (extagList.size() > 0) {
                        try {
                            extag[] sortedList = extagList.toArray(new extag[]{});
                            Arrays.sort(sortedList);
                            myApp.currentExTagIndex = extagList.indexOf(sortedList[0]);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        //showNextOrPrevEx(2);
                        initViewPager();
                    }
                }
            }
        };

        if (!getIntent().hasExtra("index")) {
            //第一次进入需初始化
            Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_app_initializing), Toast.LENGTH_LONG).show();
            handler.postDelayed(runnable, myApp.initTime * 1000);
        } else {
            //小球返回不需要重新初始化
            progressBarInit.setVisibility(View.INVISIBLE);
            for (extag et : myApp.extagList.values()) {
                extagList.add(et);
            }

            initDone = true;
            canSwipe = true;

            //显示原来展览目录
            if (extagList.size() > 0) {
                myApp.currentExTagIndex = getIntent().getIntExtra("index", 0);
                //showNextOrPrevEx(2);
                initViewPager();
            }
        }

        //收藏列表
        btnFavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //收藏模式
                lvMode = "f";
                btnFavor.setBackground(getResources().getDrawable(R.drawable.favorite_selected));
                btnDownload.setBackground(getResources().getDrawable(R.drawable.download));
                btnProfile.setBackground(getResources().getDrawable(R.drawable.profile));
                isProfileActive = false;
                tvHeaderTitle.setText(getString(R.string.lbl_my_collections));

                String sDesc = "";
                if (myApp.currentExTagIndex > -1) {
                    extag targetExtag = extagList.get(myApp.currentExTagIndex);
                    switch (myApp.logonUser.defaultLang) {
                        case GlobalConst.DEFAULT_LANG_EN:
                            sDesc = targetExtag.getDescription_en();
                            break;
                        case GlobalConst.DEFAULT_LANG_CN:
                            sDesc = targetExtag.getDescription_cn();
                            break;
                        case GlobalConst.DEFAULT_LANG_TW:
                            sDesc = targetExtag.getDescription_tw();
                            break;
                        case GlobalConst.DEFAULT_LANG_PT:
                            sDesc = targetExtag.getDescription_pt();
                            break;
                    }

                    try {
                        String finalHtml = sDesc.replace("{appuser}", myApp.logonUser.userId);
                        finalHtml = finalHtml.replace("{locale}", myApp.getLocaleString(myApp.logonUser.defaultLang));
                        desc.loadDataWithBaseURL("", finalHtml, "text/html", "utf-8", null);
                        desc.setBackgroundColor(getResources().getColor(R.color.bg_color));
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    /*
                    desc.setText(Html.fromHtml(finalHtml));
                    desc.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/minion_pro.otf"));
                    desc.setMovementMethod(LinkMovementMethod.getInstance());*/
                    background.setBackground(Drawable.createFromPath(GlobalConst.PATH_SDCARD
                            .concat(extagList.get(myApp.currentExTagIndex).getContent().getClientpath())
                            .concat(extagList.get(myApp.currentExTagIndex).getContent().getFilename())));

                    //更新收藏列表
                    List<favorite> favoriteList = myApp.getFavoriteList();
                    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    String itemTitle = "";
                    for (favorite f : favoriteList) {
                        //if (f.getExtag().equals(targetExtag.getExtag())) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            content imgContent = myApp.contentList.get(f.getRefImageId());
                            if (imgContent == null) {

                            } else {
                                switch (myApp.logonUser.defaultLang) {
                                    case GlobalConst.DEFAULT_LANG_EN:
                                        itemTitle = imgContent.getTitle_en();
                                        break;
                                    case GlobalConst.DEFAULT_LANG_CN:
                                        itemTitle = imgContent.getTitle_cn();
                                        break;
                                    case GlobalConst.DEFAULT_LANG_TW:
                                        itemTitle = imgContent.getTitle_tw();
                                        break;
                                    case GlobalConst.DEFAULT_LANG_PT:
                                        itemTitle = imgContent.getTitle_pt();
                                        break;
                                }

                                map.put("title", itemTitle);
                                map.put("favorite", f);
                                map.put("mode", "f");
                                list.add(map);
                            }
                        //}
                    }
                    listView.setAdapter(new MyAdspter(getApplicationContext(), list));
                } else {
                    listView.setAdapter(new MyAdspter(getApplicationContext(), new ArrayList<Map<String, Object>>()));
                }
            }
        });

        //下载列表
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下载模式
                lvMode = "d";
                //选中
                btnDownload.setBackground(getResources().getDrawable(R.drawable.download_selected));
                btnFavor.setBackground(getResources().getDrawable(R.drawable.favorite));
                btnProfile.setBackground(getResources().getDrawable(R.drawable.profile));
                isProfileActive = false;
                tvHeaderTitle.setText(getString(R.string.lbl_downloads));

                //更新下载列表
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                String itemTitle = "";
                List<favorite> currentFavList = myApp.getFavoriteList();
                List<String> contentIdList = new ArrayList<String>();
                List<favorite> deleteFavList = new ArrayList<favorite>();
                for (download d : myApp.downloadList) {
                    //如果已经完成下载，检查是否有文件删除及已经收藏
                    if (d.isFinished()) {
                        for (content c : d.getContentList()) {
                            contentIdList.add(c.getContentid());
                        }

                        for (favorite f : currentFavList) {
                            if (f.getExtag().equals(d.getDextag().getExtag())) {
                                if (!contentIdList.contains(f.getRefAudioId())
                                        || !contentIdList.contains(f.getRefImageId())) {
                                    deleteFavList.add(f);
                                }
                            }
                        }

                        myApp.updateFavoriteList(currentFavList, deleteFavList);
                    }

                    Map<String, Object> map = new HashMap<String, Object>();
                    switch (myApp.logonUser.defaultLang) {
                        case GlobalConst.DEFAULT_LANG_EN:
                            itemTitle = d.getDextag().getTitle_en();
                            break;
                        case GlobalConst.DEFAULT_LANG_CN:
                            itemTitle = d.getDextag().getTitle_cn();
                            break;
                        case GlobalConst.DEFAULT_LANG_TW:
                            itemTitle = d.getDextag().getTitle_tw();
                            break;
                        case GlobalConst.DEFAULT_LANG_PT:
                            itemTitle = d.getDextag().getTitle_pt();
                            break;
                    }
                    map.put("title", itemTitle);
                    map.put("download", d);
                    map.put("mode", "d");
                    list.add(map);
                }
                listView.setAdapter(new MyAdspter(getApplicationContext(), list));
            }
        });

        //个人资料
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //个人资料模式
                lvMode = "p";
                //选中
                btnProfile.setBackground(getResources().getDrawable(R.drawable.profile_selected));
                btnDownload.setBackground(getResources().getDrawable(R.drawable.download));
                btnFavor.setBackground(getResources().getDrawable(R.drawable.favorite));
                isProfileActive = true;
                tvHeaderTitle.setText(getString(R.string.lbl_profile));

                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Map<String, Object> map = new HashMap<String, Object>();

                String wxProfile = ConfigHelper.getInstance(getApplicationContext()).getProfile();
                JSONObject wxJsonObj = null;
                try {
                    if (!wxProfile.equals("")) {
                        wxJsonObj = new JSONObject(wxProfile);
                    }
                    map.put("title", wxJsonObj == null ? getString(R.string.lbl_link_to_wechat) : wxJsonObj.getString("nickname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("link", !wxProfile.equals(""));
                map.put("mode", "p");
                map.put("type", "wechat");
                list.add(map);

                Map<String, Object> fbMap = new HashMap<String, Object>();
                if (!myApp.fbName.equals("")) {
                    fbMap.put("title", myApp.fbName);
                    fbMap.put("link", true);
                } else {
                    fbMap.put("title", getString(R.string.lbl_link_to_facebook));
                    fbMap.put("link", false);
                }
                fbMap.put("mode", "p");
                fbMap.put("type", "facebook");
                list.add(fbMap);

                listView.setAdapter(new MyAdspter(getApplicationContext(), list));
            }
        });

        //设置则跳转设置界面
        final Button btnSetting = (Button) findViewById(R.id.right_menu_btn_setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfileActive = false;
                Intent it = new Intent(ExhibitionActivity.this, SettingActivity.class);
                it.putExtra("root", "ex");
                startActivity(it);
            }
        });

        btnFavor.callOnClick();

        registerHeadsetPlugReceiver();
        registerStopMediaReceiver();
    }

    private String convertToFinalHtml(String source) {
        try {
            String finalHtml = source.replace("{appuser}", myApp.logonUser.userId)
                    .replace("{locale}", myApp.getLocaleString(myApp.logonUser.defaultLang));
            return finalHtml;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initViewPager() {
        background.setVisibility(View.INVISIBLE);
        mFragments.clear();
        extag[] sortedList = myApp.extagList.values().toArray(new extag[]{});
        Arrays.sort(sortedList);
        String pager_item_desc = "";
        for (extag et : sortedList) {
            switch (myApp.logonUser.defaultLang) {
                case GlobalConst.DEFAULT_LANG_EN:
                    pager_item_desc = et.getDescription_en();
                    break;
                case GlobalConst.DEFAULT_LANG_CN:
                    pager_item_desc = et.getDescription_cn();
                    break;
                case GlobalConst.DEFAULT_LANG_TW:
                    pager_item_desc = et.getDescription_tw();
                    break;
                case GlobalConst.DEFAULT_LANG_PT:
                    pager_item_desc = et.getDescription_pt();
                    break;
            }
            mFragments.add(CatalogItemFragment.newInstance(
                    GlobalConst.PATH_SDCARD.concat(et.getContent().getClientpath()).concat(et.getContent().getFilename()),
                    convertToFinalHtml(pager_item_desc)));
        }

        int maxIndex = sortedList.length - 1;
        if (maxIndex > 1) {
            mFragments.add(CatalogItemFragment.newInstance(
                    GlobalConst.PATH_SDCARD
                            .concat(sortedList[0].getContent().getClientpath())
                            .concat(sortedList[0].getContent().getFilename()),
                    convertToFinalHtml(pager_item_desc)));
            mFragments.add(0, CatalogItemFragment.newInstance(
                    GlobalConst.PATH_SDCARD
                            .concat(sortedList[maxIndex].getContent().getClientpath())
                            .concat(sortedList[maxIndex].getContent().getFilename()),
                    convertToFinalHtml(pager_item_desc)));
        }

        /**
         * 初始化Adapter
         */
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mFragments.get(arg0);
            }
        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int pageIndex = position;

                if (position == 0) {
                    pageIndex = mFragments.size() - 2;
                } else if (position == mFragments.size() - 2 + 1) {
                    pageIndex = 1;
                }
                if (position != pageIndex) {
                    mViewPager.setCurrentItem(pageIndex, false);
                }

                myApp.currentExTagIndex = pageIndex - 1;
                showNextOrPrevEx(2);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                CatalogItemFragment curItem = (CatalogItemFragment) mFragments.get(mViewPager.getCurrentItem());
                if (curItem != null) {
                    VideoEnabledWebView webView = (VideoEnabledWebView) curItem.getView().findViewById(R.id.catalog_fragment_exhibition_desc);
                    if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                        webView.onPause();
                    } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                        webView.onResume();
                    }
                }
            }
        });
        mViewPager.setCurrentItem(1);
    }


    @Override
    public void OnEventCallBack(String param) {

    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private boolean getAccessToken() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    private void updateUI() {
        Profile profile = Profile.getCurrentProfile();
        if (getAccessToken() && profile != null) {
            myApp.fbName = profile.getName();
        } else {
            myApp.fbName = "";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            CatalogItemFragment curItem = (CatalogItemFragment) mFragments.get(mViewPager.getCurrentItem());
            if (curItem != null) {
                VideoEnabledWebView webView = (VideoEnabledWebView) curItem.getView().findViewById(R.id.catalog_fragment_exhibition_desc);
                String videoUrl = webView.getUrl().toLowerCase();
                if (videoUrl.indexOf(".mp4") >= 0) {
                    webView.loadUrl("about:blank");
                    webView.loadUrl(videoUrl);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(headsetPlugReceiver);
        unregisterStopMediaReceiver();
        profileTracker.stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void downloadExFiles(download down, String dlgTitle) {
        taskList.clear();
        fileCounter = 0;
        //String[] suffix = new String[]{"_cc.mp3", "_en.mp3", "_sc.mp3", "_pt.mp3"};

        //保存配置文件并重新初始化
        down.saveToConfigThenReInit();
        /*
        String extraImages = down.getDextag().getWebsite();

        for (content c : down.getContentList()) {
            if (c.getClientpath() != null && c.getServerpath() != null) {
                //音频文件需转换为真实路径和文件名
                if (c.getContenttype() == GlobalConst.CONTENT_TYPE_AUDIO) {
                    for (String s : suffix) {
                        String realFileName = c.getFilename().replace(".mp3", s);
                        String realServerPath = c.getServerpath().replace(".mp3", s);
                        if (!myApp.fileHelper.isFileExist(c.getClientpath().concat(realFileName))) {
                            taskList.put(realServerPath, new MyTask(c.getClientpath(), realFileName));
                        }
                    }
                } else {
                    //图片则直接下载
                    if (!myApp.fileHelper.isFileExist(c.getClientpath().concat(c.getFilename()))) {
                        taskList.put(c.getServerpath(), new MyTask(c.getClientpath(), c.getFilename()));
                    }

                    //extra content images
                    if (extraImages != null) {
                        if (extraImages.indexOf(",") >= 0) {
                            String[] imgGroup = extraImages.split(",");
                            for (String imgName : imgGroup) {
                                if (!myApp.fileHelper.isFileExist(c.getClientpath().concat(imgName + ".png"))) {
                                    taskList.put(
                                            c.getServerpath().substring(0, c.getServerpath().lastIndexOf("/") + 1) + imgName + ".png"
                                            , new MyTask(c.getClientpath(), imgName + ".png"));
                                }
                            }
                        } else {
                            if (!myApp.fileHelper.isFileExist(c.getClientpath().concat(extraImages + ".png"))) {
                                taskList.put(
                                        c.getServerpath().substring(0, c.getServerpath().lastIndexOf("/") + 1) + extraImages + ".png"
                                        , new MyTask(c.getClientpath(), extraImages + ".png"));
                            }
                        }
                    }
                }
            }
        }*/
        taskList.put("http://arts.things.buzz/download/package/" + down.getDextag().getExtag() + ".zip"
                , new MyTask("/com.buzz.exhibition/", down.getDextag().getExtag() + ".zip"));

        if (taskList.size() > 0) {
            mProgressDialog.setTitle(dlgTitle);
            mProgressDialog.setProgress(0);
            mProgressDialog.incrementProgressBy(1);
            mProgressDialog.setMax(100);
            mProgressDialog.show();
        }
        //只需下载配置
        else {
            //更新下载列表
            myApp.updateDownloadList();
            //刷新左视图
            showNextOrPrevEx(2);
            //刷新下载列表
            btnDownload.callOnClick();
        }

        for (Map.Entry<String, MyTask> entry : taskList.entrySet()) {
            entry.getValue().execute(entry.getKey());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //detector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > GlobalConst.SYSTEM_EXIT_INTERVAL) {
                Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_quit_system), Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            } else {
                try {
                    //先停止Beacon服务
                    Intent itCore = new Intent(this, CoreService.class);
                    stopService(itCore);

                    //再停止后台消息服务
                    Intent itBack = new Intent(this, MessageService.class);
                    stopService(itBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                this.finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showNextOrPrevEx(int flag) {
        if (extagList.size() > 0) {
            if (flag == 1) {
                //向右滑动
                myApp.currentExTagIndex++;
                if (myApp.currentExTagIndex >= extagList.size()) {
                    myApp.currentExTagIndex = 0;
                }
            } else if (flag == 0) {
                //向左滑动
                myApp.currentExTagIndex--;
                if (myApp.currentExTagIndex < 0) {
                    myApp.currentExTagIndex = extagList.size() - 1;
                }
            } else if (flag == 2) {
                //显示原来图片，不添加逻辑
            }

            if (myApp.currentExTagIndex > -1) {
                //显示或隐藏下载图标
                extag[] sortedList = myApp.extagList.values().toArray(new extag[]{});
                Arrays.sort(sortedList);
                for (download down : myApp.downloadList) {
                    if (down.getDextag().getExtag().equals(sortedList[myApp.currentExTagIndex].getExtag())) {
                        if (!down.isFinished()) {
                            subDownload = down;
                            btnIconPreview.setVisibility(View.INVISIBLE);
                            btnIconPreview.setOnClickListener(null);
                            btnIconDownload.setVisibility(View.VISIBLE);
                            btnIconDownload.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CustomDialog.Builder builder = new CustomDialog.Builder(ExhibitionActivity.this);
                                    builder.setTitle(getString(R.string.msg_dlg_title_tips));
                                    String dlgConfirmTitle = "";
                                    switch (myApp.logonUser.defaultLang) {
                                        case GlobalConst.DEFAULT_LANG_CN:
                                            dlgConfirmTitle = subDownload.getDextag().getTitle_cn();
                                            break;
                                        case GlobalConst.DEFAULT_LANG_TW:
                                            dlgConfirmTitle = subDownload.getDextag().getTitle_tw();
                                            break;
                                        case GlobalConst.DEFAULT_LANG_EN:
                                            dlgConfirmTitle = subDownload.getDextag().getTitle_en();
                                            break;
                                        case GlobalConst.DEFAULT_LANG_PT:
                                            dlgConfirmTitle = subDownload.getDextag().getTitle_pt();
                                            break;
                                    }
                                    builder.setMessage(String.format(getString(R.string.msg_dlg_download_confirm),dlgConfirmTitle));
                                    builder.setConfirmButton(getString(R.string.msg_dlg_ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            lvMode = "d";
                                            downloadExFiles(subDownload, title.getText().toString());
                                            customDialog.dismiss();
                                        }
                                    });

                                    builder.setBackButton(getString(R.string.msg_dlg_cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            customDialog.dismiss();
                                        }
                                    });

                                    customDialog = builder.create();
                                    customDialog.show();
                                }
                            });
                        } else {
                            btnIconDownload.setVisibility(View.INVISIBLE);
                            btnIconDownload.setOnClickListener(null);
                            btnIconPreview.setVisibility(View.VISIBLE);
                            btnIconPreview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent previewIntent = new Intent(ExhibitionActivity.this, PreviewActivity.class);
                                    previewIntent.putExtra("exTag", down.getDextag().getExtag());
                                    previewIntent.putExtra("root", "ex");
                                    startActivity(previewIntent);
                                }
                            });
                        }

                        break;
                    } else {
                        btnIconDownload.setVisibility(View.INVISIBLE);
                        btnIconDownload.setOnClickListener(null);
                        btnIconPreview.setVisibility(View.INVISIBLE);
                        btnIconPreview.setOnClickListener(null);
                    }
                }
            }

            //选中收藏按钮
            btnFavor.callOnClick();
        }
    }

    private void playAudio(String audioPath) {
        //停止后台音频
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
                    Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
                }
            } else {
                myApp.playSound(realPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerStopMediaReceiver() {
        stopMediaReceiver = new StopMediaReceiver();
        intentFilter = new IntentFilter(GlobalConst.ACTION_STOP_MEDIA);
        registerReceiver(stopMediaReceiver, intentFilter);
    }

    private void unregisterStopMediaReceiver() {
        if (stopMediaReceiver != null) {
            unregisterReceiver(stopMediaReceiver);
        }
    }

    private class MyTask extends AsyncTask<String, Integer, String> {
        private String clientPath;
        private String fileName;

        protected MyTask(String clientPath, String fileName) {
            this.clientPath = clientPath;
            this.fileName = fileName;
        }

        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            //Log.i(TAG, "onPreExecute() called");
            myApp.downloading = true;
            myApp.stopCoreService();
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            //Log.i(TAG, "doInBackground(Params... params) called");
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0]);
                HttpResponse response = client.execute(get);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    long total = entity.getContentLength();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024 * 10];
                    int count = 0;
                    int length = -1;
                    while ((length = is.read(buf)) != -1) {
                        baos.write(buf, 0, length);
                        count += length;
                        //调用publishProgress公布进度,最后onProgressUpdate方法将被执行
                        publishProgress((int) ((count / (float) total) * 100));
                        //为了演示进度,休眠500毫秒
                        //Thread.sleep(5);
                    }

                    //保存文件
                    String filePath = GlobalConst.PATH_SDCARD + this.clientPath;
                    String fileName = this.fileName;
                    String saveTo = filePath + fileName;
                    File file = new File(filePath);
                    file.mkdirs();
                    file = null;
                    file = new File(saveTo);
                    file.createNewFile();
                    OutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(baos.toByteArray());
                    baos.close();
                    baos.flush();
                    outputStream.close();
                    outputStream.flush();
                    client.getConnectionManager().shutdown();
                    file = null;

                    return this.fileName;
                }
            } catch (Exception e) {
                //Log.i(TAG, e.getMessage());
            }
            return null;
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Integer... progresses) {
            //Log.i(TAG, "onProgressUpdate(Progress... progresses) called");
            if (lvMode.equals("d")) {
                mProgressDialog.setProgress(progresses[0]);
            } else {
                progressBar.setProgress(progresses[0]);
            }

        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            fileCounter++;
            if (lvMode.equals("d")) {
                //mProgressDialog.setProgress(fileCounter);
            } else {
                progressBar.setSecondaryProgress((int) ((fileCounter * 1.0 / taskList.size()) * 100));
            }

            if (fileCounter == taskList.size()) {
                if (lvMode.equals("d")) {
                    try {
                        File sourceZipFile = new File(GlobalConst.PATH_SDCARD + this.clientPath + this.fileName);
                        if (sourceZipFile.exists()) {
                            ZipFile zipFile = new ZipFile(sourceZipFile);
                            String destPath = sourceZipFile.getPath().replace(".zip", "/");
                            zipFile.extractAll(destPath);
                            sourceZipFile.delete();
                        }
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }
                    //关闭下载对话框
                    mProgressDialog.setProgress(0);
                    mProgressDialog.dismiss();
                    //更新下载列表
                    myApp.updateDownloadList();
                    //刷新下载列表
                    btnDownload.callOnClick();
                    //隐藏下载图标
                    btnIconDownload.setVisibility(View.INVISIBLE);
                    //显示预览图标
                    btnIconPreview.setVisibility(View.VISIBLE);
                    //预览事件
                    btnIconPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent previewIntent = new Intent(ExhibitionActivity.this, PreviewActivity.class);
                            previewIntent.putExtra("exTag", subDownload.getDextag().getExtag());
                            previewIntent.putExtra("root", "ex");
                            startActivity(previewIntent);
                        }
                    });
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ExhibitionActivity.this, getString(R.string.msg_catalog_finished), Toast.LENGTH_LONG).show();
                    //显示最近的展览
                    if (extagList.size() > 0) {
                        try {
                            extag[] sortedList = extagList.toArray(new extag[]{});
                            Arrays.sort(sortedList);
                            myApp.currentExTagIndex = extagList.indexOf(sortedList[0]);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        //showNextOrPrevEx(2);
                        initViewPager();
                    }
                }

                initDone = true;
                canSwipe = !bidirSldingLayout.isLeftLayoutVisible();
                myApp.downloading = false;
                myApp.startCoreService();
            }
        }
    }

    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (canSwipe) {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //right
                    showNextOrPrevEx(1);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //left
                    showNextOrPrevEx(0);
                }
            }

            return true;
        }
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

    public class StopMediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            myApp.stopSound();
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
