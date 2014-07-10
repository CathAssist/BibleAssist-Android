package org.cathassist.bible.mark;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.R;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.Database;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MarkContentActivity extends SherlockActivity {
    EditText mTitle;
    EditText mContent;
    int mBook;
    int mChapter;
    int mSection;
    boolean mIsExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_content);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("书签内容");

        Intent intent = getIntent();
        mBook = intent.getIntExtra("book", 1);
        mChapter = intent.getIntExtra("chapter", 1);
        mSection = intent.getIntExtra("section", 1);

        mTitle = (EditText) findViewById(R.id.text_title);
        mContent = (EditText) findViewById(R.id.text_content);

        GetContent();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getSupportMenuInflater().inflate(R.menu.mark_content_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            case R.id.delete:
                DeleteBookmark();
                break;
            case R.id.confirm:
                SaveBookmark();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SaveBookmark() {
        if ((mTitle.getText() != null && TextUtils.isEmpty(mTitle.getText().toString().trim()))) {
            mTitle.setText("无标题");
        }

        SQLiteDatabase db = null;
        try {
            db = new Database(this).DbConnection(Para.DB_DATA_PATH + Para.DB_DATA_NAME);
            String sql;

            String updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            if (mIsExist) {
                sql = "update bookmark set updateTime = '" + updateTime + "', title = '" + mTitle.getText() + "', content = '" + mContent.getText() + "'"
                        + " where book = " + mBook + " and chapter = " + mChapter + " and section = " + mSection;
                db.execSQL(sql);
            } else {
                sql = "insert into bookmark (updateTime,book,chapter,section,title,content) values ("
                        + "'" + updateTime + "'," + mBook + "," + mChapter + "," + mSection + ","
                        + "'" + mTitle.getText() + "'," + "'" + mContent.getText() + "')";
                db.execSQL(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        this.finish();
    }

    private void DeleteBookmark() {
        if (mIsExist) {
            SQLiteDatabase db = null;
            try {
                db = new Database(this).DbConnection(Para.DB_DATA_PATH + Para.DB_DATA_NAME);
                String sql = "delete from bookmark where book = " + mBook + " and chapter = " + mChapter + " and section = " + mSection;
                db.execSQL(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }
        this.finish();
    }

    private void GetContent() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = new Database(this).DbConnection(Para.DB_DATA_PATH + Para.DB_DATA_NAME);

            String sql = "select * from bookmark where book = " + mBook + " and chapter = " + mChapter + " and section = " + mSection;
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                mIsExist = true;
                mTitle.setText(cursor.getString(cursor.getColumnIndex("title")));
                mContent.setText(cursor.getString(cursor.getColumnIndex("content")));
            } else {
                mIsExist = false;
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
        }

    }

}
