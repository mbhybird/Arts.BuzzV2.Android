package com.buzz.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buzz.fonts.FuturaTextView;
import com.buzz.layout.BidirSlidingLayout;
import com.buzz.layout.CustomDialog;
import com.buzz.layout.CustomProgressDialog;
import com.buzz.models.MyCircle;
import com.buzz.models.MyRect;
import com.buzz.models.beacon;
import com.buzz.models.content;
import com.buzz.models.download;
import com.buzz.models.extag;
import com.buzz.models.favorite;
import com.buzz.receiver.HomeWatcherReceiver;
import com.buzz.service.BeaconReader;
import com.buzz.service.MessageService;
import com.buzz.service.CoreService;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.GlobalConst.AppRunningState;

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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class MainActivity extends Activity {

    final String TAG = this.getClass().getSimpleName();
    MyApplication myApp;
    IntentFilter intentFilter;
    RelativeLayout rootLayout;
    RelativeLayout menuLayout;
    RelativeLayout ballLayout;
    String bid;
    long firstTime = 0;
    List<MyCircle> myCircleList;
    Map<String,MyRect> myRectMap;
    List<MyRect> myRectList;
    int screenWidth;
    int screenHeight;
    DisplayMetrics dm;
    Dialog paintDlg;
    String lastShowTag;
    Handler dlgHandler;
    Runnable dlgRunnable;
    String imageDesc = "";
    String title = "";
    String artist = "";
    String year = "";
    String mExtag = "";
    TextView tvBoard;
    Message ballCountMsg;
    Handler ballCountHandler;
    Handler returnToExHandler;
    Runnable returnToExRunnable;
    boolean returnToExFlag;
    BidirSlidingLayout bidirSldingLayout;
    boolean canReceiveSignal = true;
    favorite f;
    favorite delFav;
    download d;
    download delDown;
    Button btnFavor;
    Button btnDownload;
    String lvMode = "f";
    ListView listView;
    ListView beaconListView;
    Dialog favDlg;
    CustomProgressDialog mProgressDialog;
    CustomDialog customDialog;
    Map<String, MyTask> taskList;
    int fileCounter = 0;
    List<extag> extagList;
    StopMediaReceiver stopMediaReceiver;
    List<DlgEvent> showDlgTargetList;
    Handler readerHandler;
    Runnable readerRunnable;
    FuturaTextView tvHeaderTitle;
    RelativeLayout boardLayout;
    Button btnSignalBack;
    List<Map<String, String>> beaconList;
    Map<String, String> beaconMap;
    BeaconListAdapter mBeaconListAdapter;
    TextView tvExTitle;
    int stayInterval = 0;
    String curPlayingId = "";
    HomeWatcherReceiver mHomeKeyReceiver;
    Button btnProfile;
    Handler wxHandler;
    Runnable wxRunnable;
    boolean isProfileActive = false;
    String shareExTitle;
    String shareLocale;
    String shareImagePath;
    String shareImageServerPath;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myApp = (MyApplication) getApplication();

        readerRunnable = new Runnable() {
            @Override
            public void run() {
                if (myApp.access && !LockScreenActivity.isLocked) {
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

        //初始化对话框列表
        showDlgTargetList = new ArrayList<DlgEvent>();

        //初始化下载任务列表
        taskList = new HashMap<String, MyTask>();

        //初始化展览列表
        extagList = new ArrayList<extag>();
        extag[] sortedList = myApp.extagList.values().toArray(new extag[]{});
        Arrays.sort(sortedList);
        for (extag et : sortedList) {
            extagList.add(et);
        }

        //下载进度对话框
        mProgressDialog = new CustomProgressDialog(this, R.style.CustomDialog);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getString(R.string.msg_file_downloading));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);

        //面版计数器
        tvBoard = (TextView) findViewById(R.id.main_activity_txt_ball_count);
        ballCountHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        tvBoard.setText(msg.obj.toString());
                        break;
                }
            }
        };

        //画作详细窗口
        paintDlg = new Dialog(MainActivity.this, R.style.PopupDialog);
        paintDlg.setCancelable(false);

        /*
        //监听窗口状态，如果超出响应范围则关闭
        dlgRunnable = new Runnable() {
            @Override
            public void run() {
                //取显示队列的头部无素
                if (showDlgTargetList.size() > 0) {
                    DlgEvent dlgEvent = showDlgTargetList.get(0);
                    //如果是自动操作
                    if (dlgEvent.mode == 1) {
                        if (myApp.beaconStateList.containsKey(lastShowTag)) {
                            if (paintDlg.isShowing()) {
                                if (myApp.beaconStateList.get(lastShowTag) == GlobalConst.TRIGGER_TYPE_OUT) {
                                    myApp.stopSound();
                                    try {
                                        paintDlg.dismiss();
                                        //清除列表
                                        showDlgTargetList.clear();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                showPaintDialog(dlgEvent);
                            }
                        } else {
                            showPaintDialog(dlgEvent);
                        }
                    } else {
                        //如果是手动操作
                        showPaintDialog(dlgEvent);
                    }
                }
                dlgHandler.postDelayed(this, 100);
            }
        };

        dlgHandler = new Handler();
        dlgHandler.post(dlgRunnable);*/

        myCircleList = new ArrayList<MyCircle>();
        myRectMap = new HashMap<String, MyRect>();
        myRectList = new ArrayList<MyRect>();
        beaconList = new ArrayList<Map<String, String>>();
        tvExTitle = (TextView) findViewById(R.id.main_activity_txt_ex_title);
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

        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        myApp.scaleVector = dm.densityDpi / 240f;
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        rootLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        menuLayout = (RelativeLayout) findViewById(R.id.main_activity_menu_layout);
        ballLayout = (RelativeLayout) findViewById(R.id.main_activity_ball_layout);

        registerStopMediaReceiver();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("clear")) {
                myApp.lastPlayingId = "";
            }
        }

        //退回展览界面
        returnToExFlag = false;
        returnToExHandler = new Handler();
        returnToExRunnable = new Runnable() {
            @Override
            public void run() {
                if (returnToExFlag) {

                } else {
                    returnToExHandler.postDelayed(this, 100);
                }
            }
        };

        returnToExHandler.post(returnToExRunnable);

        btnSignalBack = (Button) findViewById(R.id.main_activity_btn_signal_back);
        btnSignalBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myApp.stopCoreService();
                myApp.access = false;

                myApp.writeLog(myApp.lastPlayingId, GlobalConst.TRIGGER_TYPE_OUT);

                myApp.stopSound();
                myApp.lastPlayingId = "";
                readerHandler.removeCallbacks(readerRunnable);

                Intent it = new Intent();
                it.setClass(MainActivity.this, ExhibitionActivity.class);
                it.putExtra("index", myApp.currentExTagIndex);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
                MainActivity.this.finish();
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
        tvHeaderTitle = (FuturaTextView) findViewById(R.id.right_menu_header_title);

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

        //右侧菜单
        bidirSldingLayout = (BidirSlidingLayout) findViewById(R.id.mainSlidingLayout);
        bidirSldingLayout.setScrollEvent(findViewById(R.id.right_menu));
        bidirSldingLayout.setInterface(new BidirSlidingLayout.ISlidingCallBack() {
            @Override
            public void eventOnScrollToContentFromLeftMenu() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        canReceiveSignal = true;
                        beaconList.clear();
                        myApp.lastPlayingId = "";
                    }
                }, 1000);
            }
        });
        Button btnMenu = (Button) findViewById(R.id.main_activity_btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bidirSldingLayout.isLeftLayoutVisible()) {
                    bidirSldingLayout.scrollToContentFromLeftMenu();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canReceiveSignal = true;
                            beaconList.clear();
                            myApp.lastPlayingId = "";
                        }
                    }, 1000);
                } else {
                    bidirSldingLayout.initShowLeftState();
                    bidirSldingLayout.scrollToLeftMenu();
                    canReceiveSignal = false;
                    btnFavor.callOnClick();
                    myApp.stopSound();
                    myApp.writeLog(myApp.lastPlayingId, GlobalConst.TRIGGER_TYPE_OUT);
                    beaconList.clear();
                    myApp.lastPlayingId = "";
                }
            }
        });

        boardLayout = (RelativeLayout) findViewById(R.id.main_activity_board_layout);
        boardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canReceiveSignal) {
                    btnMenu.callOnClick();
                }
            }
        });
        menuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canReceiveSignal) {
                    btnMenu.callOnClick();
                }
            }
        });
        ballLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!canReceiveSignal) {
                    btnMenu.callOnClick();
                }
            }
        });

        //收藏对话框
        favDlg = new Dialog(MainActivity.this, R.style.PopupDialog);

        //右侧菜单列表
        listView = (ListView) findViewById(R.id.right_menu_lv);

        //Beacon列表
        beaconListView = (ListView) findViewById(R.id.main_activity_beacon_list_lv);
        beaconListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BeaconListAdapter myBeaconAdapter = (BeaconListAdapter) parent.getAdapter();
                Map<String, Object> map = (Map<String, Object>) myBeaconAdapter.getItem(position);
                showPaintDialog(new DlgEvent(map.get("beaconId").toString(), 0));
            }
        });

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
                        CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
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
                    CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
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
                        Toast.makeText(MainActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
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

                                Dialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.ShareTheme)
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
                            favDlg.dismiss();
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
                                Toast.makeText(MainActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(MainActivity.this, getString(R.string.msg_add_to_favorite_success), Toast.LENGTH_LONG).show();
                                    btnFavorDlg.setBackground(getResources().getDrawable(R.drawable.liked));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            btnFavor.callOnClick();
                        }
                    });

                    favDlg.setContentView(dlgView);
                    favDlg.show();
                }
                //下载模式
                else if (lvMode.equals("d")) {
                    d = (download) map.get("download");
                    if (!d.isFinished()) {
                        CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
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
                        Intent previewIntent = new Intent(MainActivity.this, PreviewActivity.class);
                        previewIntent.putExtra("exTag", d.getDextag().getExtag());
                        previewIntent.putExtra("root", "main");
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
                                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile"));
                            }
                        }
                    }

                }
            }
        });

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

                //更新收藏列表
                List<favorite> favoriteList = myApp.getFavoriteList();
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                String itemTitle = "";
                if (myApp.currentExTagIndex > -1) {
                    //extag targetExtag = extagList.get(myApp.currentExTagIndex);
                    for (favorite f : favoriteList) {
                        //if (f.getExtag().equals(targetExtag.getExtag())) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            content imgContent = myApp.contentList.get(f.getRefImageId());
                            if (imgContent == null) {
                                //itemTitle = getString(R.string.msg_file_not_exists);
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
                readerHandler.removeCallbacks(readerRunnable);
                Intent it = new Intent(MainActivity.this, SettingActivity.class);
                it.putExtra("root", "main");
                startActivity(it);
            }
        });

        btnFavor.callOnClick();
    }

    private void downloadExFiles(download down, String dlgTitle) {
        taskList.clear();
        fileCounter = 0;

        //保存配置文件并重新初始化
        down.saveToConfigThenReInit();
        String extraImages = down.getDextag().getWebsite();

        String[] suffix = new String[]{"_cc.mp3", "_en.mp3", "_sc.mp3", "_pt.mp3"};
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
        }

        if (taskList.size() > 0) {
            mProgressDialog.setTitle(dlgTitle);
            mProgressDialog.setProgress(0);
            mProgressDialog.incrementProgressBy(1);
            mProgressDialog.setMax(taskList.size());
            mProgressDialog.show();
        }
        //只需下载配置
        else {
            //更新下载列表
            myApp.updateDownloadList();
            //刷新下载列表
            btnDownload.callOnClick();
        }

        for (Map.Entry<String, MyTask> entry : taskList.entrySet()) {
            entry.getValue().execute(entry.getKey());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > GlobalConst.SYSTEM_EXIT_INTERVAL) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_quit_system), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterStopMediaReceiver();
        profileTracker.stopTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeKeyReceiver(this);
        Intent it = new Intent(GlobalConst.ACTION_APP_RUNNING_MONITOR);
        it.putExtra("BOF", AppRunningState.FRONT.ordinal());
        sendBroadcast(it);

        if(paintDlg.isShowing()){
            paintDlg.dismiss();
        }

        if (myApp.mediaPlayer.isPlaying()) {
            curPlayingId = myApp.lastPlayingId;
            showPaintDialog(new DlgEvent(myApp.lastPlayingId, 1));
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        unregisterHomeKeyReceiver(this);
        Intent it = new Intent(GlobalConst.ACTION_APP_RUNNING_MONITOR);
        it.putExtra("BOF", AppRunningState.BACK.ordinal());
        sendBroadcast(it);
        super.onPause();
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

    protected void registerHomeKeyReceiver(Context context) {
        //Log.i(TAG, "registerHomeKeyReceiver");
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    protected void unregisterHomeKeyReceiver(Context context) {
        //Log.i(TAG, "unregisterHomeKeyReceiver");
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void showPaintDialog(DlgEvent dlgEvent) {
        String bid = dlgEvent.beaconid;
        int mode =  dlgEvent.mode;

        if(mode==0) {
            myApp.stopSound();
        }

        //如果列表存在
        if (myApp.actionList.containsKey(bid)) {
            //如果已经在显示中
            if (paintDlg.isShowing()) {
                //如果显示的ID不一样则关闭当前窗口
                if (!myApp.lastPlayingId.equals(bid)) {
                    try {
                        paintDlg.dismiss();
                        //停止播放语音
                        myApp.stopSound();
                        //重新打开窗口
                        showPaintDialog(dlgEvent);
                    }catch (Exception e){
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
                        year = String.valueOf(c.getYear());
                        shareImagePath = c.getClientpath().concat(c.getFilename());
                        shareImageServerPath = c.getServerpath();
                        switch (myApp.logonUser.defaultLang) {
                            case GlobalConst.DEFAULT_LANG_CN:
                                title = c.getTitle_cn();
                                artist = c.getArtist_cn();
                                imageDesc = c.getDescription_cn();
                                shareLocale = "cn";
                                shareExTitle = exTag.getTitle_cn();
                                break;

                            case GlobalConst.DEFAULT_LANG_EN:
                                title = c.getTitle_en();
                                artist = c.getArtist_en();
                                imageDesc = c.getDescription_en();
                                shareLocale = "en";
                                shareExTitle = exTag.getTitle_en();
                                break;

                            case GlobalConst.DEFAULT_LANG_TW:
                                title = c.getTitle_tw();
                                artist = c.getArtist_tw();
                                imageDesc = c.getDescription_tw();
                                shareLocale = "tw";
                                shareExTitle = exTag.getTitle_tw();
                                break;

                            case GlobalConst.DEFAULT_LANG_PT:
                                title = c.getTitle_pt();
                                artist = c.getArtist_pt();
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
                wv.setWebViewClient(new WebViewClient(){
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
                        paintDlg.dismiss();
                        //停止播放语音
                        myApp.stopSound();
                        //清除列表
                        showDlgTargetList.clear();
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
                            Toast.makeText(MainActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_LONG).show();
                            return;
                        }
                        playAudio(audContentPath);
                    }
                });

                final Button btnFavorite = (Button) dlgView.findViewById(R.id.dlg_favorite_btn);
                Button btnMenu = (Button) dlgView.findViewById(R.id.dlg_btn_menu);
                btnMenu.setVisibility(View.GONE);

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

                            Dialog alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.ShareTheme)
                                    .setTitle(getString(R.string.lbl_share))
                                    .setIcon(R.drawable.share)
                                    .setItems(sharePlatform, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            if (which == 0) {
                                                if(wxReady) {
                                                    myApp.wxShared(
                                                            String.format(myApp.sharePageUrl, fav.getRefImageId(), shareLocale)
                                                            , shareExTitle, title, shareImagePath
                                                    );
                                                }
                                                else{
                                                    Profile profile = Profile.getCurrentProfile();
                                                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                            .setContentTitle(String.format("%s@%s", title, shareExTitle))
                                                            .setContentDescription("")
                                                            .setContentUrl(Uri.parse(String.format(myApp.sharePageUrl, fav.getRefImageId(), shareLocale)))
                                                            .setImageUrl(Uri.parse(shareImageServerPath))
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
                                                        .setContentTitle(String.format("%s@%s", title, shareExTitle))
                                                        .setContentDescription("")
                                                        .setContentUrl(Uri.parse(String.format(myApp.sharePageUrl, fav.getRefImageId(), shareLocale)))
                                                        .setImageUrl(Uri.parse(shareImageServerPath))
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
                                Toast.makeText(MainActivity.this, getString(R.string.msg_add_to_favorite_success), Toast.LENGTH_LONG).show();
                                btnFavorite.setBackground(getResources().getDrawable(R.drawable.liked));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                //显示自定义样式窗口dialog.xml + <style name="PopupDialog">
                paintDlg.setContentView(dlgView);
                try {
                    paintDlg.show();
                    if(mode==1) {
                        if(!myApp.lastPlayingId.equals(bid)) {
                            btnAudio.callOnClick();
                        }
                    }
                }
                catch (Exception e) {
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
                    Toast.makeText(MainActivity.this, getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
                }
            } else {
                myApp.playSound(realPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            fileCounter++;
            mProgressDialog.setProgress(fileCounter);

            if (fileCounter == taskList.size()) {
                //关闭下载对话框
                mProgressDialog.setProgress(0);
                mProgressDialog.dismiss();
                //更新下载列表
                myApp.updateDownloadList();
                //刷新下载列表
                btnDownload.callOnClick();
                myApp.startCoreService();
            }
        }
    }

    private void updateBallListView() {
        //切换右边菜单则停止检测信号
        if (!canReceiveSignal) return;

        //如果不满足返回条件则刷新小球视图
        if (!returnToExFlag) {
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

                ballCountMsg = new Message();
                ballCountMsg.what = 1;
                ballCountMsg.obj = beaconList.size();
                ballCountHandler.sendMessage(ballCountMsg);

                mBeaconListAdapter = new BeaconListAdapter(this, beaconList);
                beaconListView.setAdapter(mBeaconListAdapter);

                if (myApp.logonUser.earphonePlay.equals(GlobalConst.EARPHONE_PLAY_ON) && !myApp.headSetConnected) {
                    myApp.stopSound();
                    //Toast.makeText(MainActivity.this, getString(R.string.msg_conn_headset), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class StopMediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            myApp.stopSound();
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
}
