package org.cathassist.bible.read;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.MainActivity;
import org.cathassist.bible.R;
import org.cathassist.bible.lib.Database;
import org.cathassist.bible.lib.Func;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.Share;
import org.cathassist.bible.lib.VerseInfo;
import org.cathassist.bible.mark.MarkContentActivity;
import org.cathassist.bible.music.MusicPlayService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleReadFragment extends SherlockFragment implements OnClickListener, OnItemClickListener,
        OnSeekBarChangeListener, MusicPlayService.OnPlayListener, MusicPlayService.OnPlayChangedListener,
        MusicPlayService.OnCompletionListener, View.OnTouchListener {
    List<Map<String, String>> mContent;
    private MainActivity mActivity = null;
    private ListView mList;
    private ImageView mLeft, mRight, mSound;
    private View mMusicPanel;
    private TextView mPlay, mPrev, mNext, mMode, mMusicTitle, mPast, mTotal;
    private Button mMusicCancel, mMusicDown, mMusicDownBook;
    private SeekBar mSeekBar;
    private List<Integer> mMarkedSections = new ArrayList<Integer>();
    private MusicPlayService mService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getSherlockActivity();
        setHasOptionsMenu(true);
        mActivity.getSupportActionBar().setTitle("圣经");
        mService = ((MainActivity) getSherlockActivity()).getMusicPlayService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bible_read, null);
        mList = (ListView) view.findViewById(R.id.list);
        mList.setOnItemClickListener(this);
        mList.setOnTouchListener(this);

        mLeft = (ImageView) view.findViewById(R.id.action_bar_left);
        mLeft.setOnClickListener(this);
        mRight = (ImageView) view.findViewById(R.id.action_bar_right);
        mRight.setOnClickListener(this);
        mSound = (ImageView) view.findViewById(R.id.action_bar_sound);
        mSound.setOnClickListener(this);

        mMusicPanel = view.findViewById(R.id.music_panel);
        mMusicPanel.setVisibility(View.GONE);
        mMusicCancel = (Button) view.findViewById(R.id.btn_music_cancel);
        mMusicCancel.setOnClickListener(this);
        mMusicDown = (Button) view.findViewById(R.id.btn_music_down);
        mMusicDown.setOnClickListener(this);
        mMusicDownBook = (Button) view.findViewById(R.id.btn_music_down_book);
        mMusicDownBook.setOnClickListener(this);
        mPrev = (TextView) view.findViewById(R.id.music_prev);
        mPrev.setOnClickListener(this);
        mPlay = (TextView) view.findViewById(R.id.music_play);
        mPlay.setOnClickListener(this);
        mNext = (TextView) view.findViewById(R.id.music_next);
        mNext.setOnClickListener(this);
        mMode = (TextView) view.findViewById(R.id.music_mode);
        mMode.setOnClickListener(this);
        mSeekBar = (SeekBar) view.findViewById(R.id.music_seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mMusicTitle = (TextView) view.findViewById(R.id.music_title);
        mPast = (TextView) view.findViewById(R.id.music_left_time_indicator);
        mTotal = (TextView) view.findViewById(R.id.music_right_time_indicator);

        mService.setPlayMode(Para.mp3Mode);
        mService.setOnPlayListener(this);
        mService.setOnPlayChangedListener(this);
        mService.setOnCompletionListener(this);

        switch (Para.mp3Mode) {
            case MusicPlayService.MODE_SINGLE:
            default:
                mMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_mode_single, 0, 0);
                mMode.setText("单章");
                break;
            case MusicPlayService.MODE_SINGLE_LOOP:
                mMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_mode_single_loop, 0, 0);
                mMode.setText("重复");
                break;
            case MusicPlayService.MODE_ORDER:
                mMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_mode_order, 0, 0);
                mMode.setText("顺序");
                break;
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadChapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Para.currentSection = Integer.valueOf(mContent.get(mList.getFirstVisiblePosition()).get("section"));
        } catch (Exception ex) {
            Para.currentSection = 0;
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.bible_read_menu, menu);
        MenuItem book = menu.findItem(R.id.book);
        MenuItem chapter = menu.findItem(R.id.chapter);
        book.setTitle(VerseInfo.CHN_NAME[Para.currentBook]);
        chapter.setTitle(String.valueOf(Para.currentChapter));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.book:
                mService.stop();
                intent.setClass(mActivity, BookSelectActivity.class);
                startActivity(intent);
                break;
            case R.id.chapter:
                mService.stop();
                intent.setClass(mActivity, ChapterSelectActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bar_left:
                mService.stop();
                ChangeVerse(false);
                break;
            case R.id.action_bar_right:
                mService.stop();
                ChangeVerse(true);
                break;
            case R.id.action_bar_sound:
                mMusicPanel.setVisibility(View.VISIBLE);
                break;
            case R.id.music_play:
                if (checkMp3()) {
                    mService.play(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                } else {
                    mService.playNet(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                    Func.downChapter(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                }
                break;
            case R.id.music_prev:
                mService.stop();
                ChangeVerse(false);
                if (checkMp3()) {
                    mService.play(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                } else {
                    mService.playNet(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                    Func.downChapter(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                }
                break;
            case R.id.music_next:
                mService.stop();
                ChangeVerse(true);
                if (checkMp3()) {
                    mService.play(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                } else {
                    mService.playNet(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                    Func.downChapter(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                }
                break;
            case R.id.music_mode:
                switch (Para.mp3Mode) {
                    case MusicPlayService.MODE_SINGLE:
                        Para.mp3Mode = MusicPlayService.MODE_SINGLE_LOOP;
                        mMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_mode_single_loop, 0, 0);
                        mMode.setText("重复");
                        break;
                    case MusicPlayService.MODE_SINGLE_LOOP:
                        Para.mp3Mode = MusicPlayService.MODE_ORDER;
                        mMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_mode_order, 0, 0);
                        mMode.setText("顺序");
                        break;
                    case MusicPlayService.MODE_ORDER:
                    default:
                        Para.mp3Mode = MusicPlayService.MODE_SINGLE;
                        mMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_mode_single, 0, 0);
                        mMode.setText("单章");
                        break;
                }
                mService.setPlayMode(Para.mp3Mode);
                SharedPreferences settings = mActivity.getSharedPreferences(Para.STORE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("mp3Mode", Para.mp3Mode);
                editor.commit();
                break;
            case R.id.btn_music_cancel:
                mMusicPanel.setVisibility(View.GONE);
                break;
            case R.id.btn_music_down:
                Func.downChapter(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                break;
            case R.id.btn_music_down_book:
                new AlertDialog.Builder(mActivity).setTitle("确定下载整卷" + VerseInfo.CHN_NAME[Para.currentBook] + "？")
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Func.downBook(Para.mp3Ver, Para.currentBook);
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                break;
            default:
                break;
        }
    }

    private boolean checkMp3() {
        final File file = Func.getFilePath(Para.mp3Ver, Func.getFileName(Para.currentBook, Para.currentChapter));
        return file.exists();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int pos = position;
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setItems(Para.VERSE_CHOISE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> map = mContent.get(pos);

                        String bookName = VerseInfo.CHN_NAME[Integer.parseInt(map.get("book"))];
                        String content = map.get("verse")
                                + "(" + bookName + " "
                                + map.get("chapter") + ":"
                                + map.get("section") + ")" + "\n"
                                + "来自圣经小助手";

                        switch (which) {
                            case Para.VERSE_CHOISE_MARK:
                                Intent intent = new Intent();
                                intent.putExtra("book", Integer.parseInt(map.get("book")));
                                intent.putExtra("chapter", Integer.parseInt(map.get("chapter")));
                                intent.putExtra("section", Integer.parseInt(map.get("section")));
                                intent.setClass(mActivity, MarkContentActivity.class);
                                startActivity(intent);
                                break;
                            case Para.VERSE_CHOISE_COPY:
                                ClipboardManager clip = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                                clip.setText(content); // 复制
                                Toast.makeText(mActivity, content + "    已复制到剪贴板", Toast.LENGTH_SHORT).show();
                                break;
                            case Para.VERSE_CHOISE_SHARE:
                                Share.shareByAndroid(getActivity(), content);
                                break;
                        }
                    }

                }).create();
        dialog.show();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (fromUser) {
            if (mService != null) {
                mService.setProgress(progress);
            }
        }
    }

    @Override
    public void onPlay(int progress, int duration) {
        if (isAdded()) {
            mSeekBar.setMax(duration);
            mSeekBar.setProgress(progress);
            mPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_pause, 0, 0);
            mPlay.setText("暂停");
            mPast.setText(String.format("%02d", progress / 1000 / 60) + ":" + String.format("%02d", progress / 1000 % 60));
            mTotal.setText(String.format("%02d", duration / 1000 / 60) + ":" + String.format("%02d", duration / 1000 % 60));
        }
    }

    @Override
    public void onPlayChanged(boolean isPlay) {
        if (isAdded()) {
            if (isPlay) {
                reloadChapter();
                mPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_pause, 0, 0);
                mPlay.setText("暂停");
            } else {
                mPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.music_play, 0, 0);
                mPlay.setText("播放");
            }
        }
    }

    @Override
    public void onCompletion() {
        if (isAdded()) {
            reloadChapter();
        }
    }

    private void reloadChapter() {
        if (isAdded()) {
            SetButtonName();
            GetVerse();
            ChangePosition();
        }
    }

    private void GetVerse() {
        mContent = GetData();
        BibleAdapter adapter = new BibleAdapter(getSherlockActivity());
        mList.setAdapter(adapter);

        SharedPreferences settings = mActivity.getSharedPreferences(Para.STORE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("currentBook", Para.currentBook);
        editor.putInt("currentChapter", Para.currentChapter);
        editor.putInt("currentSection", Para.currentSection);
        editor.commit();
    }

    public void SetButtonName() {
        if (Para.currentBook < 1 || Para.currentBook > 73) {
            Para.currentBook = 1;
        }
        if (Para.currentChapter < 1 || Para.currentChapter > VerseInfo.CHAPTER_COUNT[Para.currentBook]) {
            Para.currentChapter = 1;
        }
        mMusicTitle.setText(VerseInfo.CHN_NAME[Para.currentBook] + " 第" + Para.currentChapter + "章");
        mActivity.supportInvalidateOptionsMenu();
    }

    private void ChangePosition() {
        int section = Para.currentSection;
        int position = 0;
        for (int i = 0; i < mContent.size(); i++) {
            Map<String, String> map = mContent.get(i);
            if (Integer.valueOf(map.get("section")) == section) {
                position = i;
                break;
            }
        }
        mList.setSelection(position);
    }

    public void ChangeVerse(Boolean isNext) {
        Func.ChangeChapter(isNext);

        reloadChapter();
    }

    private List<Map<String, String>> GetData() {
        SQLiteDatabase db = null;
        SQLiteDatabase dbMark = null;
        Cursor cursor = null;
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        mMarkedSections.clear();

        try {
            db = new Database(mActivity).DbConnection(Para.DB_CONTENT_PATH + Para.DB_CONTENT_NAME);

            String sql = "select book,chapter,section,chn from cathbible where book = " + Para.currentBook
                    + " and chapter = " + Para.currentChapter;

            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String content = cursor.getString(cursor.getColumnIndex("chn"));

                if (!TextUtils.isEmpty(content)) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("book", String.valueOf(cursor.getInt(cursor.getColumnIndex("book"))));
                    map.put("chapter", String.valueOf(cursor.getInt(cursor.getColumnIndex("chapter"))));
                    map.put("section", String.valueOf(cursor.getInt(cursor.getColumnIndex("section"))));
                    map.put("verse", content);
                    data.add(map);
                }
            }
            if (cursor != null) {
                cursor.close();
            }

            dbMark = new Database(mActivity).DbConnection(Para.DB_DATA_PATH + Para.DB_DATA_NAME);
            sql = "select * from bookmark where book = " + Para.currentBook
                    + " and chapter = " + Para.currentChapter;
            cursor = dbMark.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                mMarkedSections.add(cursor.getInt(cursor.getColumnIndex("section")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
            if (dbMark != null) {
                dbMark.close();
            }
        }

        return data;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.list:
                mMusicPanel.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    class BibleAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public BibleAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = mInflater.inflate(R.layout.bible_read_item, null);

                vh.title = (TextView) convertView.findViewById(R.id.title);
                vh.content = (TextView) convertView.findViewById(R.id.content);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, Para.font_size);

            Map<String, String> map = mContent.get(position);
            //int book = Integer.valueOf(map.get("book"));
            //int chapter = Integer.valueOf(map.get("chapter"));
            int section = Integer.valueOf(map.get("section"));
            String verse = map.get("verse");
            TextPaint paint = vh.content.getPaint();
            if (section > 1000) {
                vh.title.setText("");
                paint.setFakeBoldText(true);
            } else {
                vh.title.setText(String.valueOf(section));
                paint.setFakeBoldText(false);
            }
            vh.title.setText(section > 1000 ? "" : String.valueOf(section));
            vh.content.setText(verse);

            if (Para.show_color) {
                if (mMarkedSections.contains(section)) {
                    vh.title.setTextColor(Para.HIGHLIGHT_TEXT_COLOR);
                    vh.content.setTextColor(Para.HIGHLIGHT_TEXT_COLOR);
                } else {
                    vh.title.setTextColor(Para.DEFAULT_TEXT_COLOR);
                    vh.content.setTextColor(Para.DEFAULT_TEXT_COLOR);
                }
            }

            if (position == getCount() - 1) {
                convertView.setPadding(5, 5, 5, 100);
            } else {
                convertView.setPadding(5, 5, 5, 5);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return mContent.size();
        }

        @Override
        public Object getItem(int position) {
            return mContent.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView title;
            TextView content;
        }
    }

}
