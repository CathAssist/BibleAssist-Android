package org.cathassist.bible.read;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.R;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.VerseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChapterSelectActivity extends SherlockActivity implements OnItemClickListener {
    GridView mChapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter_select);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("选择章");

        mChapter = (GridView) findViewById(R.id.grid);

        SimpleAdapter adapter = new SimpleAdapter(this,
                GetData(),
                R.layout.chapter_select_item,
                new String[]{"chapter"},
                new int[]{R.id.text});
        mChapter.setAdapter(adapter);
        mChapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Para.currentChapter = position + 1;
        Para.currentSection = 0;

        view.setBackgroundColor(getResources().getColor(R.color.light_blue));
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Map<String, String>> GetData() {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        int max = VerseInfo.CHAPTER_COUNT[Para.currentBook];
        for (int i = 1; i <= max; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("chapter", String.valueOf(i));
            data.add(map);
        }
        return data;
    }
}

