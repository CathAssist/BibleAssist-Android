package org.cathassist.bible.read;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.R;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.VerseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookSelectActivity extends SherlockActivity implements OnItemClickListener {
    ListView mList;
    GridView mGrid;
    List<Map<String,String>> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_select);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("选择卷");

        mGrid = (GridView) findViewById(R.id.grid);

        mData = getData();
        SimpleAdapter adapter = new SimpleAdapter(this,
                mData,
                R.layout.book_select_item,
                new String[] {"abbr","name"},
                new int[] {R.id.abbr,R.id.name})
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = super.getView(position, convertView, parent);
                    holder = new ViewHolder();
                    holder.abbr = (TextView) convertView.findViewById(R.id.abbr);
                    holder.name = (TextView) convertView.findViewById(R.id.name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                int book = position + 1;
                int color;
                switch (VerseInfo.getBookType(book)) {
                    case VerseInfo.PENTATEUCH:
                        color = getResources().getColor(R.color.yellow);
                        break;
                    case VerseInfo.HISTORY:
                        color = getResources().getColor(R.color.cyan);
                        break;
                    case VerseInfo.WISDOM_AND_POETRY:
                        color = getResources().getColor(R.color.gray);
                        break;
                    case VerseInfo.MAJOR_PROPHETS:
                        color = getResources().getColor(R.color.light_yellow);
                        break;
                    case VerseInfo.MINOR_PROPHETS:
                        color = getResources().getColor(R.color.pink);
                        break;
                    case VerseInfo.GOSPELS:
                        color = getResources().getColor(R.color.yellow);
                        break;
                    case VerseInfo.ACTS_OF_APOSTLES:
                        color = getResources().getColor(R.color.cyan);
                        break;
                    case VerseInfo.PAULINE_EPISTLES:
                        color = getResources().getColor(R.color.gray);
                        break;
                    case VerseInfo.GENERAL_EPISTLES:
                        color = getResources().getColor(R.color.light_yellow);
                        break;
                    case VerseInfo.APOCALYPTIC:
                        color = getResources().getColor(R.color.pink);
                        break;
                    default:
                        color = getResources().getColor(R.color.white);
                        break;
                }
                convertView.setBackgroundColor(color);

                holder.abbr.setText(mData.get(position).get("abbr"));
                holder.name.setText(mData.get(position).get("name"));
                return convertView;
            }
        };

        mGrid.setAdapter(adapter);
        mGrid.setOnItemClickListener(this);
    }

    private static final class ViewHolder {
        public TextView abbr;
        public TextView name;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Para.currentBook = position + 1;
        Para.currentChapter = 1;
        Para.currentSection = 0;

        view.setBackgroundColor(getResources().getColor(R.color.light_blue));
        Intent intent = new Intent();
        intent.setClass(this, ChapterSelectActivity.class);
        startActivity(intent);
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

    private List<Map<String, String>> getData() {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i = 1; i <= 73; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", VerseInfo.CHN_NAME[i]);
            map.put("abbr",VerseInfo.CHN_ABBR[i]);
            data.add(map);
        }
        return data;
    }
}
