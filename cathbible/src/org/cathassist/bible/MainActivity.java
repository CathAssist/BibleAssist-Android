package org.cathassist.bible;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import org.cathassist.bible.lib.Fragments;
import org.cathassist.bible.lib.Func;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.music.MusicPlayService;
import org.cathassist.bible.provider.DownloadManager;
import org.cathassist.bible.provider.downloads.DownloadService;

public class MainActivity extends SlidingFragmentActivity implements ServiceConnection {
    private long mExitTime = 0;
    private ActionBar mActionBar;
    private MusicPlayService mMusicPlayService;
    private DownloadManager mDownloadManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Func.InitCommonPara();
        Func.InitOncePara();
        Func.LoadTheme();
        setTheme(Para.THEME);
        Func.InitTheme();
        loadLast();
        Fragments.initFragments();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_content);

        if (Para.auto_update) {
            UmengUpdateAgent.setUpdateOnlyWifi(!Para.allow_gprs);
            UmengUpdateAgent.update(this);
        }

        FeedbackAgent agent = new FeedbackAgent(this);
        agent.sync();

        Intent intent = getIntent();
        SherlockFragment fragment;
        if ("BIBLE_READ_FRAGMENT".equals(intent.getAction())) {
            fragment = Fragments.bibleReadFragment;
        } else {
            fragment = Fragments.homeFragment;
        }

        mActionBar = getSupportActionBar();
        setBehindContentView(R.layout.nav);
        FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();
        fragTrans.replace(R.id.nav_frame, new NavFragment());
        fragTrans.replace(R.id.content_frame, fragment);
        fragTrans.commit();

        initSlidingMenu();

        bindMusicPlayService();
        mDownloadManager = new DownloadManager(getContentResolver(), getPackageName());
        startDownloadService();
        Func.setDownloadManager(mDownloadManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (Para.always_bright) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences settings = getSharedPreferences(Para.STORE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("currentBook", Para.currentBook);
        editor.putInt("currentChapter", Para.currentChapter);
        editor.putInt("currentSection", Para.currentSection);
        editor.commit();

        if (mMusicPlayService != null) {
            unbindMusicPlayService();
        }
        Intent intent = new Intent();
        intent.setClass(this, DownloadService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initSlidingMenu() {
        SlidingMenu slidingMenu = getSlidingMenu();
        //设置是左滑还是右滑，还是左右都可以滑
        slidingMenu.setMode(SlidingMenu.LEFT);
        //设置阴影宽度
        slidingMenu.setShadowWidth(20);//getWindowManager().getDefaultDisplay().getWidth() / 40);        
        //设置左菜单阴影图片
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        //设置右菜单阴影图片
        //slidingMenu.setSecondaryShadowDrawable(R.drawable.right_shadow);
        //设置菜单占屏幕的比例
        slidingMenu.setBehindOffset(getWindowManager().getDefaultDisplay().getWidth() / 4);
        //设置滑动时菜单的是否淡入淡出
        slidingMenu.setFadeEnabled(false);
        //设置淡入淡出的比例
        //slidingMenu.setFadeDegree(0.6f);
        //设置滑动时拖拽效果
        slidingMenu.setBehindScrollScale(0.0f);
        //设置要使菜单滑动，触碰屏幕的范围
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.invalidate();

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    private void startDownloadService() {
        Intent intent = new Intent();
        intent.setClass(this, DownloadService.class);
        startService(intent);
    }

    private void loadLast() {
        Para.lastBook = Para.currentBook;
        Para.lastChapter = Para.currentChapter;
        Para.lastSection = Para.currentSection;
    }

    public MusicPlayService getMusicPlayService() {
        return mMusicPlayService;
    }

    private void bindMusicPlayService() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MusicPlayService.class);
        bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    private void unbindMusicPlayService() {
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mMusicPlayService = ((MusicPlayService.MusicPlayBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
