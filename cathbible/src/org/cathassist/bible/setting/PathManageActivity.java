package org.cathassist.bible.setting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.R;
import org.cathassist.bible.lib.Func;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.PathScan;

import java.util.ArrayList;

public class PathManageActivity extends SherlockActivity implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private PathManageAdapter mAdapter;
    private ArrayList<String> mMount = new ArrayList<String>();
    private ArrayList<String> mLabel = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp3_manage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("下载路径管理");

        mListView = (ListView) findViewById(R.id.list);

        PathScan.determineStorageOptions();
        mMount = PathScan.mMounts;
        mLabel = PathScan.mLabels;

        /*
        ArrayList<String> sdCards = Func.getExternalSdCards();
        String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        boolean isExist = false;
        for(String sd:sdCards) {
            mMount.add(sd);
            if(sd.equals(defaultPath)) {
                isExist = true;
            }
        }
        if(!isExist) {
            mMount.add(defaultPath);
        }*/

        mAdapter = new PathManageAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = mMount.get(position);
        String oldPath = Para.downPath;

        if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(oldPath) && !path.equals(oldPath)) {
            String[] params = new String[]{oldPath + Para.ROOT_PATH, path + Para.ROOT_PATH};
            new CopyFolderTask(this).execute(params);

            Para.downPath = path;
            SharedPreferences settings = getSharedPreferences(Para.STORE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("downPath", Para.downPath);
            editor.commit();
            mAdapter.notifyDataSetChanged();
        }
    }

    public class PathManageAdapter extends BaseAdapter {
        private Context mContext;

        public PathManageAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public int getCount() {
            return mMount.size();
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.path_manage_item, null);
                vh.path = (CheckedTextView) convertView.findViewById(R.id.path);
                vh.label = (TextView) convertView.findViewById(R.id.label);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.path.setText(mMount.get(position));
            vh.path.setChecked(mMount.get(position).equals(Para.downPath));
            vh.label.setText("剩余空间：" + mLabel.get(position) + "MB");

            return convertView;
        }

        private class ViewHolder {
            CheckedTextView path;
            TextView label;
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
            mAdapter.notifyDataSetChanged();
            if (result) {
                Toast.makeText(PathManageActivity.this, "数据迁移完成", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PathManageActivity.this, "数据迁移失败，请尝试手动迁移", Toast.LENGTH_SHORT).show();
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
