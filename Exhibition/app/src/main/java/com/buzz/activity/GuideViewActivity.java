package com.buzz.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.view.View;
import android.widget.LinearLayout;

import com.buzz.fonts.FuturaButton;
import com.buzz.utils.ConfigHelper;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.ImageHelper;
import com.buzz.models.favorite;

import java.util.ArrayList;
import java.util.List;


public class GuideViewActivity extends Activity implements OnClickListener, OnPageChangeListener {
    final static String TAG = GuideViewActivity.class.getSimpleName();
    //引导图片资源
    private final static int[] pics = new int[6];
    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    //底部小店图片
    private ImageView[] dots;

    //记录当前选中位置
    private int currentIndex;

    FuturaButton btnStartToExp;
    ImageView btnClose;
    ConfigHelper configHelper;
    int mCount = 0;
    Thread thread;
    MyApplication myApp;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_view);

        myApp = (MyApplication) getApplication();
        configHelper = ConfigHelper.getInstance(this);

        switch (myApp.logonUser.defaultLang) {
            case GlobalConst.DEFAULT_LANG_CN:
                pics[0] = R.drawable.h1_cn;
                pics[1] = R.drawable.h2_cn;
                pics[2] = R.drawable.h3_cn;
                pics[3] = R.drawable.h4_cn;
                pics[4] = R.drawable.h5_cn;
                pics[5] = R.drawable.h6_cn;
                break;

            case GlobalConst.DEFAULT_LANG_TW:
                pics[0] = R.drawable.h1_tw;
                pics[1] = R.drawable.h2_tw;
                pics[2] = R.drawable.h3_tw;
                pics[3] = R.drawable.h4_tw;
                pics[4] = R.drawable.h5_tw;
                pics[5] = R.drawable.h6_tw;
                break;

            case GlobalConst.DEFAULT_LANG_EN:
            case GlobalConst.DEFAULT_LANG_PT:
                pics[0] = R.drawable.h1_en;
                pics[1] = R.drawable.h2_en;
                pics[2] = R.drawable.h3_en;
                pics[3] = R.drawable.h4_en;
                pics[4] = R.drawable.h5_en;
                pics[5] = R.drawable.h6_en;
                break;
        }

        views = new ArrayList<View>();

        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        //初始化引导图片列表
        for (int i = 0; i < pics.length; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(mParams);
            iv.setImageBitmap(ImageHelper.readBitMap(getApplicationContext(), pics[i]));
            if (i == 0) {
                iv.setPadding(40, 0, 40, 100);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            views.add(iv);
        }
        vp = (ViewPager) findViewById(R.id.viewpager);
        //初始化Adapter
        vpAdapter = new ViewPagerAdapter(views);
        vp.setAdapter(vpAdapter);
        //绑定回调
        vp.setOnPageChangeListener(this);

        //初始化底部小点
        initDots();

        btnClose = (ImageView) findViewById(R.id.guide_activity_btnClose);
        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartToExp.callOnClick();
            }
        });

        btnStartToExp = (FuturaButton) findViewById(R.id.btn_startToExp);
        btnStartToExp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //for test
                /*
                List<favorite> favList = new ArrayList<favorite>();
                List<favorite> delList = new ArrayList<favorite>();
                favorite f;
                for (Integer i = 77; i < 95; i += 2) {
                    if (i != 85) {
                        f = new favorite();
                        f.setExtag("mmm");
                        f.setRefImageId("c" + i.toString());
                        f.setRefAudioId("c" + String.valueOf(i + 1));

                        favList.add(f);
                    }
                }
                myApp.updateFavoriteList(favList, delList);*/
                thread.start();
            }
        });

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                dataInitThenRedirect();
            }
        });
    }

    private void dataInitThenRedirect(){
        //生成目录文件
        configHelper.updateCatalog();
        //初始化数据版本
        configHelper.updateDataVersion();
        //初始化展览列表
        myApp.initExtagList();
        //初始化beacon列表和action列表
        myApp.initSysParams();

        startActivity(new Intent(GuideViewActivity.this, ExhibitionActivity.class));
        GuideViewActivity.this.finish();
    }

    private void initDots() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.guide_activity_linearLayout);

        dots = new ImageView[pics.length];

        //循环取得小点图片
        for (int i = 0; i < pics.length; i++) {
            dots[i] = (ImageView) layout.getChildAt(i);
            dots[i].setEnabled(true);//都设为灰色
            dots[i].setOnClickListener(this);
            dots[i].setTag(i);//设置位置tag，方便取出与当前位置对应
        }

        currentIndex = 0;
        dots[currentIndex].setEnabled(false);//设置为白色，即选中状态
    }

    /**
     * 设置当前的引导页
     */
    private void setCurView(int position) {
        if (position < 0 || position >= pics.length) {
            return;
        }

        vp.setCurrentItem(position);
    }

    /**
     * 这只当前引导小点的选中
     */
    private void setCurDot(int positon) {
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }

        dots[positon].setEnabled(false);
        dots[currentIndex].setEnabled(true);

        currentIndex = positon;
    }

    //当滑动状态改变时调用
    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
    }

    //当当前页面被滑动时调用
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        if (arg0 == pics.length - 1) {
            mCount++;
        } else {
            mCount = 0;
        }

        if (mCount > 1) {
            dataInitThenRedirect();
            mCount = 0;
        }
    }

    //当新的页面被选中时调用
    @Override
    public void onPageSelected(int arg0) {
        //设置底部小点选中状态
        setCurDot(arg0);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        setCurView(position);
        setCurDot(position);
    }

    private class ViewPagerAdapter extends PagerAdapter {
        //界面列表
        private List<View> views;

        public ViewPagerAdapter(List<View> views) {
            this.views = views;
        }

        //销毁arg1位置的界面
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub
        }

        //获得当前界面数
        @Override
        public int getCount() {
            if (views != null) {
                return views.size();
            }
            return 0;
        }

        //初始化arg1位置的界面
        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1), 0);
            return views.get(arg1);
        }

        //判断是否由对象生成界面
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (arg0 == arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub
        }

    }
}