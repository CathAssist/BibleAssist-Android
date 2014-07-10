package org.cathassist.bible.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.R;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.provider.downloads.ui.DownloadList;

public class Mp3ManageActivity extends SherlockActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView mListView;
    private Mp3ManageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp3_manage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("圣经音频管理");

        mListView = (ListView) findViewById(R.id.list);

        mAdapter = new Mp3ManageAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        Toast.makeText(this, "长按选择默认音频版本", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getSupportMenuInflater().inflate(R.menu.mp3_manage_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.manage:
                Intent intent = new Intent();
                intent.setClass(this, DownloadList.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, Mp3ManageDetailActivity.class);
        intent.putExtra("name", Para.BIBLE_MP3_VERSION[position]);
        intent.putExtra("type", position);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Para.mp3Ver = position;
        SharedPreferences settings = getSharedPreferences(Para.STORE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("mp3Ver", Para.mp3Ver);
        editor.commit();
        mAdapter.notifyDataSetChanged();
        return true;
    }

    public static class Mp3ManageAdapter extends BaseAdapter {

        private Context mContext;

        public Mp3ManageAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public int getCount() {
            return Para.BIBLE_MP3_VERSION.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null || convertView.getTag() == null) {
                vh = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.mp3_manage_item, null);
                vh.text = (CheckedTextView) convertView.findViewById(R.id.text);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.text.setText(Para.BIBLE_MP3_VERSION[position]);
            if (Para.mp3Ver == position) {
                vh.text.setChecked(true);
            } else {
                vh.text.setChecked(false);
            }

            return convertView;
        }

        private class ViewHolder {
            CheckedTextView text;
        }
    }
}
