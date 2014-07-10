package org.cathassist.bible.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.R;
import org.cathassist.bible.lib.Func;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.VerseInfo;
import org.cathassist.bible.provider.DownloadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Mp3ManageDetailActivity extends SherlockActivity {
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                mMusicAdapter.notifyDataSetChanged();
            }
        }
    };
    private String mName;
    private int mType;
    private ExpandableListView mExpandableListView;
    private MusicManagementAdapter mMusicAdapter;
    private List<String> mGroup = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp3_manage_detail);

        mName = getIntent().getStringExtra("name");
        mType = getIntent().getIntExtra("type", 0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mName);

        mExpandableListView = (ExpandableListView) findViewById(R.id.exp_list);
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final int book = groupPosition + 1;
                final int chapter = childPosition + 1;
                final File file = Func.getFilePath(mType, Func.getFileName(book, chapter));
                if (file.exists()) {
                    new AlertDialog.Builder(Mp3ManageDetailActivity.this).setTitle("是否重新下载此音频？")
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    file.delete();
                                    Func.downChapter(mType, book, chapter);
                                }
                            }).setNegativeButton(R.string.cancel, null).show();
                } else {
                    Func.downChapter(mType, book, chapter);
                }
                mMusicAdapter.notifyDataSetChanged();
                return false;
            }
        });

        mExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int packedPositionType = ExpandableListView.getPackedPositionType(id);
                switch (packedPositionType) {
                    case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
                        int bookChild = ExpandableListView.getPackedPositionGroup(id) + 1;
                        int chapterChild = ExpandableListView.getPackedPositionChild(id) + 1;
                        final File file = Func.getFilePath(mType, Func.getFileName(bookChild, chapterChild));
                        if (file.exists()) {
                            new AlertDialog.Builder(Mp3ManageDetailActivity.this).setTitle("确定删除此音频文件？")
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            file.delete();
                                            Toast.makeText(Mp3ManageDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                            mMusicAdapter.notifyDataSetChanged();
                                        }
                                    }).setNegativeButton(R.string.cancel, null).show();
                        }
                        mMusicAdapter.notifyDataSetChanged();
                        return true;
                    case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
                        final int book = ExpandableListView.getPackedPositionGroup(id) + 1;
                        new AlertDialog.Builder(Mp3ManageDetailActivity.this).setTitle("确定下载整卷" + VerseInfo.CHN_NAME[book] + "？")
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Func.downBook(mType, book);
                                    }
                                }).setNegativeButton(R.string.cancel, null).show();
                        mMusicAdapter.notifyDataSetChanged();
                        return true;
                    default:
                        break;
                }
                mMusicAdapter.notifyDataSetChanged();
                return false;
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        this.registerReceiver(receiver, filter);

        loadData();
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

    private void loadData() {
        mGroup.clear();
        for (int i = 1; i <= 73; i++) {
            mGroup.add(VerseInfo.CHN_NAME[i]);
        }
        mMusicAdapter = new MusicManagementAdapter(this);
        mExpandableListView.setAdapter(mMusicAdapter);
        Toast.makeText(this, "长按书卷名,下载该卷全部音频", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            try {
                this.unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {

            }
        }
        super.onDestroy();

    }

    class MusicManagementAdapter extends BaseExpandableListAdapter {
        Activity activity;

        public MusicManagementAdapter(Activity a) {
            activity = a;
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return "第" + (childPosition + 1) + "章";
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return VerseInfo.CHAPTER_COUNT[groupPosition + 1];
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.mp3_manage_detail_child, null);
                vh = new ViewHolder();
                vh.title = (TextView) convertView.findViewById(R.id.child_name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            File file = Func.getFilePath(mType, Func.getFileName(groupPosition + 1, childPosition + 1));
            String state = file.exists() ? " (已下载)" : "";

            String title = getGroup(groupPosition) + getChild(groupPosition, childPosition) + state;
            SpannableString spannable = new SpannableString(title);
            spannable.setSpan(new ForegroundColorSpan(file.exists() ? Color.BLUE : Color.BLACK), title.length() - state.length(), title.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            vh.title.setText(spannable);
            vh.title.setTag(groupPosition);

            return convertView;
        }

        @Override
        public String getGroup(int groupPosition) {
            return mGroup.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mGroup.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.mp3_manage_detail_group, null);
                vh = new ViewHolder();
                vh.title = (TextView) convertView.findViewById(R.id.name);
                vh.icon = (ImageView) convertView.findViewById(R.id.icon);
                vh.indicator = (ImageView) convertView.findViewById(R.id.indicator);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            if (isExpanded) {
                vh.indicator.setBackgroundResource(R.drawable.arrow_sans_down);
            } else {
                vh.indicator.setBackgroundResource(R.drawable.arrow_sans_right);
            }
            vh.icon.setBackgroundResource(R.drawable.double_circle);
            vh.title.setText(mGroup.get(groupPosition));

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        class ViewHolder {
            public TextView title;
            public ImageView icon;
            public ImageView indicator;
        }
    }
}
