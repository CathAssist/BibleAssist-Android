package org.cathassist.bible;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

import org.cathassist.bible.lib.Database;
import org.cathassist.bible.lib.Fragments;
import org.cathassist.bible.lib.Func;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.Share;
import org.cathassist.bible.lib.VerseInfo;

import java.io.File;
import java.util.Random;

public class HomeFragment extends SherlockFragment implements OnClickListener {
    private MainActivity mActivity;
    private ActionBar mActionBar;
    private FragmentManager mManager;
    private TextView mVerse;
    private TextView mMark;
    private TextView mLast;
    private Button mShare;
    private LinearLayout mStatus, mSlide;
    private int mVerseBook = 0;
    private int mVerseChapter = 0;
    private int mVerseSection = 0;
    private int mMarkBook = 0;
    private int mMarkChapter = 0;
    private int mMarkSection = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mActivity = (MainActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();
        mManager = mActivity.getSupportFragmentManager();
        mActivity.getSupportActionBar().setTitle(R.string.app_name);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, null);
        mVerse = (TextView) view.findViewById(R.id.text_verse);
        mMark = (TextView) view.findViewById(R.id.text_mark);
        mLast = (TextView) view.findViewById(R.id.text_last);
        mShare = (Button) view.findViewById(R.id.button_share);
        mStatus = (LinearLayout) view.findViewById(R.id.layout_status);
        mSlide = (LinearLayout) view.findViewById(R.id.layout_slide);

        mVerse.setOnClickListener(this);
        mMark.setOnClickListener(this);
        mLast.setOnClickListener(this);
        mShare.setOnClickListener(this);

        if (new Database(mActivity).OpenDbCheck()) {
            mStatus.setVisibility(View.VISIBLE);
            mShare.setVisibility(View.VISIBLE);
            mSlide.setVisibility(View.GONE);
            ShowVerse();
            ShowMark();
            ShowLast();
        } else {
            mVerse.setText("天主竟这样爱了世界，甚至赐下了自己的独生子，使凡信他的人不至丧亡，反而获得永生。(若望福音 3:16)");
            mStatus.setVisibility(View.GONE);
            mShare.setVisibility(View.GONE);
            mSlide.setVisibility(View.VISIBLE);
        }
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cathbible/bible/mp3/chn");
        if (file.exists()) {
            File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cathbible/bible/mp3/chn_female");
            file.renameTo(newFile);
        }
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Para.ROOT_PATH);
        if(file.exists()) {
            String[] params = new String[]{Environment.getExternalStorageDirectory().getAbsolutePath() + Para.ROOT_PATH,
                    Environment.getExternalStorageDirectory().getAbsolutePath() + Para.NEW_ROOT_PATH};
            new CopyFolderTask(mActivity).execute(params);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.home_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                if (new Database(mActivity).OpenDbCheck()) {
                    mStatus.setVisibility(View.VISIBLE);
                    mShare.setVisibility(View.VISIBLE);
                    ShowVerse();
                    ShowMark();
                    ShowLast();
                } else {
                    mVerse.setText("天主竟这样爱了世界，甚至赐下了自己的独生子，使凡信他的人不至丧亡，反而获得永生。(若望福音 3:16)");
                    mStatus.setVisibility(View.GONE);
                    mShare.setVisibility(View.GONE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragTrans;

        switch (v.getId()) {
            case R.id.text_verse:
                if (mVerseBook * mVerseChapter * mVerseSection != 0) {
                    Para.currentBook = mVerseBook;
                    Para.currentChapter = mVerseChapter;
                    Para.currentSection = mVerseSection;

                    Para.menuIndex = Para.MENU_BIBLE;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    mActivity.getMusicPlayService().stop();
                    fragTrans = mManager.beginTransaction();
                    fragTrans.replace(R.id.content_frame, Fragments.bibleReadFragment);
                    fragTrans.commit();
                }
                break;
            case R.id.text_mark:
                if (mMarkBook * mMarkChapter * mMarkSection != 0) {
                    Para.currentBook = mMarkBook;
                    Para.currentChapter = mMarkChapter;
                    Para.currentSection = mMarkSection;

                    Para.menuIndex = Para.MENU_BIBLE;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    mActivity.getMusicPlayService().stop();
                    fragTrans = mManager.beginTransaction();
                    fragTrans.replace(R.id.content_frame, Fragments.bibleReadFragment);
                    fragTrans.commit();
                }
                break;
            case R.id.text_last:
                if (Para.lastBook * Para.lastChapter * Para.lastSection != 0) {
                    Para.currentBook = Para.lastBook;
                    Para.currentChapter = Para.lastChapter;
                    Para.currentSection = Para.lastSection;

                    Para.menuIndex = Para.MENU_BIBLE;
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                    mActivity.getMusicPlayService().stop();
                    fragTrans = mManager.beginTransaction();
                    fragTrans.replace(R.id.content_frame, Fragments.bibleReadFragment);
                    fragTrans.commit();
                }
                break;
            case R.id.button_share:
                if (mVerseBook * mVerseChapter * mVerseSection != 0) {
                    String content = mVerse.getText().toString().trim() + "\n" + "来自圣经小助手";
                    Share.shareByAndroid(getActivity(), content);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void ShowVerse() {
        Random random = new Random(System.currentTimeMillis());
        int id = random.nextInt(Para.VERSE_NUMBER) % (Para.VERSE_NUMBER - 1 + 1) + 1;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = new Database(mActivity).DbConnection(Para.DB_CONTENT_PATH + Para.DB_CONTENT_NAME);

            if (db != null) {
                String sql = "select * from verse where _id = " + id;
                cursor = db.rawQuery(sql, null);
                cursor.moveToFirst();
                int book = cursor.getInt(cursor.getColumnIndex("book"));
                int chapter = cursor.getInt(cursor.getColumnIndex("chapter"));
                int bSection = cursor.getInt(cursor.getColumnIndex("bSection"));
                int eSection = cursor.getInt(cursor.getColumnIndex("eSection"));

                if (cursor != null) {
                    cursor.close();
                }
                sql = "select chn from cathbible where book = " + book + " and chapter = " + chapter
                        + " and section >= " + bSection + " and section <= " + eSection;
                cursor = db.rawQuery(sql, null);

                String content = "";
                while (cursor.moveToNext()) {
                    String tempContent = cursor.getString(cursor.getColumnIndex("chn"));
                    if (!TextUtils.isEmpty(tempContent) && !"NULL".equals(tempContent)) {
                        content += tempContent;
                    }
                }

                String title = "(" + VerseInfo.CHN_NAME[book] + " " + chapter + ":" + bSection;
                if (bSection == eSection) {
                    title += ")";
                } else {
                    title += "-" + eSection + ")";
                }

                mVerseBook = book;
                mVerseChapter = chapter;
                mVerseSection = bSection;

                mVerse.setText(content + title);
            }
        } catch (Exception e) {
            mVerseBook = 50;
            mVerseChapter = 3;
            mVerseSection = 16;

            mVerse.setText("天主竟这样爱了世界，甚至赐下了自己的独生子，使凡信他的人不至丧亡，反而获得永生。(若望福音 3:16)");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private void ShowMark() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = new Database(getSherlockActivity()).DbConnection(Para.DB_DATA_PATH + Para.DB_DATA_NAME);

            if (db != null) {
                String sql = "select * from bookmark order by updatetime DESC limit 1";
                cursor = db.rawQuery(sql, null);
                cursor.moveToFirst();
                int book = cursor.getInt(cursor.getColumnIndex("book"));
                int chapter = cursor.getInt(cursor.getColumnIndex("chapter"));
                int section = cursor.getInt(cursor.getColumnIndex("section"));
                String title = cursor.getString(cursor.getColumnIndex("title"));

                String bookName = "(" + VerseInfo.CHN_NAME[book]
                        + " " + chapter;
                if (section > 1000) {
                    bookName += ")";
                } else {
                    bookName += ":" + section + ")";
                }

                mMarkBook = book;
                mMarkChapter = chapter;
                mMarkSection = section;

                mMark.setText(title + bookName);
            }
        } catch (Exception e) {
            mMarkBook = 0;
            mMarkChapter = 0;
            mMarkSection = 0;

            mMark.setText("无最新书签");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private void ShowLast() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = new Database(mActivity).DbConnection(Para.DB_CONTENT_PATH + Para.DB_CONTENT_NAME);

            if (db != null) {
                String sql = "select chn from cathbible where book = " + Para.lastBook
                        + " and chapter = " + Para.lastChapter
                        + " and section = " + Para.lastSection;
                cursor = db.rawQuery(sql, null);

                cursor.moveToFirst();
                String content = cursor.getString(cursor.getColumnIndex("chn"));

                String title = "(" + VerseInfo.CHN_NAME[Para.lastBook] + " " + Para.lastChapter;
                if (Para.lastSection > 1000) {
                    title += ")";
                } else {
                    title += ":" + Para.lastSection + ")";
                }

                mLast.setText(content + title);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Para.lastBook = 0;
            Para.lastChapter = 0;
            Para.lastSection = 0;

            mLast.setText("无上次阅读经文");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    class CopyFolderTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog;
        Context mContext;

        public CopyFolderTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage("正在迁移数据，请不要关闭软件");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result) {
                Toast.makeText(mActivity, "数据迁移完成", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, "数据迁移失败，请尝试手动迁移", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (Func.copyFolder(params[0], params[1])) {
                Func.deleteFolder(params[0]);
                return true;
            } else {
                return false;
            }
        }
    }
}
