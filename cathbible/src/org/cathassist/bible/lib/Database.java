package org.cathassist.bible.lib;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import org.cathassist.bible.lib.ProgressShow.ProgressCallBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Database {
    private Context mContext;

    public Database(Context context) {
        mContext = context;
    }

    public SQLiteDatabase DbConnection(String file) {
        SQLiteDatabase db = null;

        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                db = SQLiteDatabase.openOrCreateDatabase(file, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return db;
    }

    public boolean OpenDbCheck() {
        final boolean contentCheck = CheckDatabase(Para.DB_CONTENT_PATH, Para.DB_CONTENT_NAME, Para.DB_CONTENT_VER);
        final int dataVersion = CheckData(Para.DB_DATA_PATH, Para.DB_DATA_NAME);

        if (!contentCheck || (dataVersion != Para.DB_DATA_VER)) {
            if (!contentCheck) {
                CopyBigDatabase(Para.DB_CONTENT_ASSET, Para.DB_CONTENT_NAME,
                        Para.DB_CONTENT_PATH + Para.DB_CONTENT_NAME,
                        Para.DB_CONTENT_COUNT);
            }

            if (dataVersion != Para.DB_DATA_VER) {
                UpdateData(Para.DB_DATA_PATH, Para.DB_DATA_NAME, dataVersion);
            }
            return false;
        }

        return true;
    }

    public void CopyDatabase(final String source, final String dest) {
        final ProgressShow dialog = new ProgressShow(
                mContext, "请稍候", "数据库复制中", ProgressShow.DIALOG_TYPE_SPINNER, ProgressShow.DIALOG_DEFAULT_MAX);
        dialog.ShowDialog(new ProgressCallBack() {
            public void action() {
                try {
                    InputStream is = new FileInputStream(source);
                    OutputStream os = new FileOutputStream(dest);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }

                    os.flush();
                    os.close();
                    is.close();
                } catch (Exception e) {

                } finally {
                    dialog.CloseDialog();
                }
            }
        });
    }

    public void CopyBigDatabase(final String path, final String name, final String dest, final int count) {
        final ProgressShow dialog = new ProgressShow(
                mContext, "请稍候", "数据库复制中", ProgressShow.DIALOG_TYPE_BAR, ProgressShow.DIALOG_DEFAULT_MAX);
        dialog.ShowDialog(new ProgressCallBack() {
            public void action() {
                try {
                    InputStream is;
                    OutputStream os = new FileOutputStream(dest);

                    for (int i = 1; i <= count; i++) {
                        is = mContext.getAssets().open(path + "/" + name + "." + String.format("%03d", i));
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }

                        os.flush();
                        is.close();

                        if (dialog.GetProgress() < ProgressShow.DIALOG_DEFAULT_MAX - 1) {
                            dialog.AddProgress(ProgressShow.DIALOG_DEFAULT_INCREASE * ProgressShow.DIALOG_DEFAULT_MAX / count);
                        }
                    }
                    os.close();
                } catch (Exception e) {

                } finally {
                    dialog.CloseDialog();
                }
            }
        });
    }

    public int CheckData(String dict, String name) {
        File f;
        int version = 0;

        if (!new File(dict + name).exists()) {
            f = new File(dict);

            if (!f.exists()) {
                f.mkdirs();
            }
            version = 0;
        } else {
            SQLiteDatabase db = null;
            Cursor cursor = null;

            try {
                db = DbConnection(dict + name);

                if (db != null) {
                    String sql = "select * from version";
                    cursor = db.rawQuery(sql, null);
                    cursor.moveToFirst();

                    version = cursor.getInt(cursor.getColumnIndex("ver"));
                }
            } catch (Exception e) {
                version = 0;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
        }

        return version;
    }

    public void UpdateData(final String dict, final String name, final int ver) {
        final ProgressShow dialog = new ProgressShow(
                mContext, "请稍候", "数据库初始化中", ProgressShow.DIALOG_TYPE_SPINNER, ProgressShow.DIALOG_DEFAULT_MAX);
        dialog.ShowDialog(new ProgressCallBack() {
            public void action() {
                switch (ver) {
                    case 0:
                        SQLiteDatabase db = null;

                        try {
                            db = DbConnection(dict + name);

                            if (db != null) {
                                db.beginTransaction();

                                String sql = "CREATE TABLE bookmark (_id integer NOT NULL PRIMARY KEY AUTOINCREMENT," +
                                        "updateTime datetime, book integer, chapter integer, section integer," +
                                        " title text, content text)";
                                db.execSQL(sql);

                                sql = "CREATE TABLE version (ver integer)";
                                db.execSQL(sql);
                                sql = "insert into version (ver) values (" + Para.DB_DATA_VER + ")";
                                db.execSQL(sql);

                                sql = "CREATE TABLE verse (_id integer NOT NULL PRIMARY KEY AUTOINCREMENT, progress integer)";
                                db.execSQL(sql);
                                for (int i = 0; i < Para.VERSE_NUMBER; i++) {
                                    sql = "insert into verse (progress) values (0)";
                                    db.execSQL(sql);
                                }

                                db.setTransactionSuccessful();
                            }

                        } catch (Exception e) {

                        } finally {
                            db.endTransaction();

                            if (db != null) {
                                db.close();
                            }

                            dialog.CloseDialog();
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    public boolean CheckDatabase(String dict, String name, int ver) {
        boolean isUpdated = false;
        File f;

        if (!new File(dict + name).exists()) {
            f = new File(dict);

            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            SQLiteDatabase db = null;
            Cursor cursor = null;

            try {
                db = DbConnection(dict + name);

                if (db != null) {
                    String sql = "select * from version";
                    cursor = db.rawQuery(sql, null);
                    cursor.moveToNext();

                    if (cursor.getInt(cursor.getColumnIndex("ver")) == ver) {
                        isUpdated = true;
                    }
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

        if (isUpdated) {
            return true;
        } else {
            f = new File(dict + name);
            f.delete();
            return false;
        }

    }
}
