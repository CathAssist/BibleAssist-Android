package org.cathassist.bible;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.read.BibleReadFragment;
import org.cathassist.bible.setting.SettingActivity;

import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NavFragment extends SherlockFragment implements OnItemClickListener {
    private static final int INTENT_SET = 0;
    private MainActivity mActivity;
    private ActionBar mActionBar;
    private FragmentManager mManager;
    private Fragment mFragment;
    private ListView mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();
        mManager = mActivity.getSupportFragmentManager();
        Para.menuIndex = Para.MENU_HOME;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nav, null);
        mMenu = (ListView) view.findViewById(R.id.list);
        mMenu.setOnItemClickListener(this);
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < Para.MENU_NAME.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("img", Integer.toString(Para.MENU_IMAGE[i]));
            map.put("text", Para.MENU_NAME[i]);
            list.add(map);
        }

        String[] from = {"img", "text"};
        int[] to = {R.id.img, R.id.text};

        SimpleAdapter adapter = new SimpleAdapter(mActivity, list, R.layout.nav_item, from, to);
        mMenu.setAdapter(adapter);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case Para.MENU_HOME:
                if (Para.menuIndex != Para.MENU_HOME) {
                    Para.menuIndex = Para.MENU_HOME;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    FragmentTransaction fragTrans = mManager.beginTransaction();
                    mFragment = org.cathassist.bible.lib.FragmentManager.homeFragment;
                    fragTrans.replace(R.id.content_frame, mFragment);
                    fragTrans.commit();
                }
                delayToggle();
                break;

            case Para.MENU_BIBLE:
                if (Para.menuIndex != Para.MENU_BIBLE) {
                    Para.menuIndex = Para.MENU_BIBLE;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    FragmentTransaction fragTrans = mManager.beginTransaction();
                    mFragment = org.cathassist.bible.lib.FragmentManager.bibleReadFragment;
                    fragTrans.replace(R.id.content_frame, mFragment);
                    fragTrans.commit();
                }
                delayToggle();
                break;

            case Para.MENU_MARK:
                if (Para.menuIndex != Para.MENU_MARK) {
                    Para.menuIndex = Para.MENU_MARK;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    FragmentTransaction fragTrans = mManager.beginTransaction();
                    mFragment = org.cathassist.bible.lib.FragmentManager.markFragment;
                    fragTrans.replace(R.id.content_frame, mFragment);
                    fragTrans.commit();
                }
                delayToggle();
                break;

            case Para.MENU_VERSE:
                if (Para.menuIndex != Para.MENU_VERSE) {
                    Para.menuIndex = Para.MENU_VERSE;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    FragmentTransaction fragTrans = mManager.beginTransaction();
                    mFragment = org.cathassist.bible.lib.FragmentManager.verseFragment;
                    fragTrans.replace(R.id.content_frame, mFragment);
                    fragTrans.commit();
                }
                delayToggle();
                break;
            case Para.MENU_SEARCH:
                if (Para.menuIndex != Para.MENU_SEARCH) {
                    Para.menuIndex = Para.MENU_SEARCH;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    FragmentTransaction fragTrans = mManager.beginTransaction();
                    mFragment = org.cathassist.bible.lib.FragmentManager.searchFragment;
                    fragTrans.replace(R.id.content_frame, mFragment);
                    fragTrans.commit();
                }
                delayToggle();
                break;

            case Para.MENU_SET:
                Intent intent;
                intent = new Intent();
                intent.setClass(mActivity, SettingActivity.class);
                startActivityForResult(intent, INTENT_SET);
                break;
        }
    }

    private void delayToggle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.getSlidingMenu().toggle();
            }
        }, 200);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_SET:
                if (resultCode == Para.NEED_RESTART) {
                    Reload();
                }
                break;
        }
    }

    protected void Reload() {
        Intent intent = new Intent(mActivity, MainActivity.class);
        mActivity.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mActivity.finish();
        mActivity.overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
