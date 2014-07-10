package org.cathassist.bible;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

import org.cathassist.bible.lib.Database;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.VerseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends SherlockFragment implements OnClickListener, OnItemClickListener, AdapterView.OnItemSelectedListener {
    int mCount = 0;
    private Button button_search;
    private ListView list_search;
    private EditText text_search;
    private MainActivity mActivity = null;
    private ActionBar mActionBar = null;
    private FragmentManager mManager = null;
    private List<Map<String, String>> mData = new ArrayList<Map<String, String>>();
    private Spinner mSpinner;
    private ArrayAdapter<String> mScopeAdapter;
    private int mScope = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getSherlockActivity();
        mActionBar = mActivity.getSupportActionBar();
        mManager = mActivity.getSupportFragmentManager();
        mActivity.getSupportActionBar().setTitle("搜索");

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search, null);

        button_search = (Button) view.findViewById(R.id.button_search);
        list_search = (ListView) view.findViewById(R.id.list);
        text_search = (EditText) view.findViewById(R.id.text_word);

        button_search.setOnClickListener(this);
        list_search.setOnItemClickListener(this);

        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mScopeAdapter = new ArrayAdapter<String>(getSherlockActivity(),
                R.layout.sherlock_spinner_item,
                VerseInfo.BOOK_SCOPE);
        mScopeAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        mSpinner.setAdapter(mScopeAdapter);
        mSpinner.setOnItemSelectedListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = mActivity.getSharedPreferences(Para.STORE_NAME, Context.MODE_PRIVATE);
        text_search.setText(settings.getString("search_key", ""));
        text_search.setSelectAllOnFocus(true);

        //SearchVerse();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mScope = position;
        //SearchVerse();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search:
                SearchVerse();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem count = menu.findItem(R.id.search_count);
        count.setTitle(mCount == -1 ? "正在搜索" : "搜索到" + mCount + "条");
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Map<String, String> map = mData.get(arg2);

        FragmentTransaction fragTrans;
        Para.currentBook = Integer.parseInt(map.get("book"));
        Para.currentChapter = Integer.parseInt(map.get("chapter"));
        Para.currentSection = Integer.parseInt(map.get("section"));

        Para.menuIndex = Para.MENU_BIBLE;
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mActivity.getMusicPlayService().stop();
        fragTrans = mManager.beginTransaction();
        fragTrans.replace(R.id.content_frame, org.cathassist.bible.lib.FragmentManager.bibleReadFragment);
        fragTrans.commit();
    }

    private void SearchVerse() {
        list_search.setAdapter(null);
        mCount = -1;
        mActivity.supportInvalidateOptionsMenu();
        mData = GetData();

        try {
            SimpleAdapter adapter = new SimpleAdapter(mActivity,
                    mData,
                    R.layout.search_item,
                    new String[]{"title", "verse"},
                    new int[]{R.id.title, R.id.content});

            list_search.setAdapter(adapter);
            mActivity.supportInvalidateOptionsMenu();

            InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService
                    (Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, String>> GetData() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        try {
            db = new Database(mActivity)
                    .DbConnection(Para.DB_CONTENT_PATH
                            + Para.DB_CONTENT_NAME);

            String text = text_search.getText().toString().trim();
            if (!text.equals("")) {
                SharedPreferences settings = mActivity.getSharedPreferences(
                        Para.STORE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("search_key", text);
                editor.commit();

                String condition = "%"
                        + text_search.getText().toString().trim()
                        .replace(' ', '%') + "%";
                String sql = "select book,chapter,section,chn from cathbible where chn like '" + condition + "'" + VerseInfo.getSearchScope(mScope);

                cursor = db.rawQuery(sql, null);

                mCount = cursor.getCount();
                while (cursor.moveToNext()) {
                    int book = cursor.getInt(cursor.getColumnIndex("book"));

                    String bookName = VerseInfo.CHN_ABBR[book];
                    bookName += cursor.getInt(cursor.getColumnIndex("chapter"));
                    if (cursor.getInt(cursor.getColumnIndex("section")) <= 1000) {
                        bookName += ":\n" + cursor.getInt(cursor.getColumnIndex("section"));
                    }
                    String content = cursor.getString(cursor.getColumnIndex("chn")).trim();

                    if (!content.trim().equals("")) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("book", cursor.getString(cursor.getColumnIndex("book")));
                        map.put("chapter", cursor.getString(cursor.getColumnIndex("chapter")));
                        map.put("section", cursor.getString(cursor.getColumnIndex("section")));
                        map.put("title", bookName.trim());
                        map.put("verse", content.trim());
                        data.add(map);
                    }
                }
            } else {
                mCount = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCount = 0;
            data.clear();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return data;
    }
}
