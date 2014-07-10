package org.cathassist.bible.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import org.cathassist.bible.App;
import org.cathassist.bible.R;
import org.cathassist.bible.provider.DownloadManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Func {
    private static DownloadManager mDownloadManager;

    public static void setDownloadManager(DownloadManager manager) {
        mDownloadManager = manager;
    }

    public static void InitCommonPara() {
        SharedPreferences settings = App.get().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Para.font_size = settings.getFloat("font_size", 18);
        editor.putFloat("font_size", Para.font_size);
        Para.always_bright = settings.getBoolean("always_bright", true);
        editor.putBoolean("always_bright", Para.always_bright);
        Para.theme_black = settings.getBoolean("theme_black", false);
        editor.putBoolean("theme_black", Para.theme_black);
        Para.auto_update = settings.getBoolean("auto_update", true);
        editor.putBoolean("auto_update", Para.auto_update);
        Para.show_color = settings.getBoolean("show_color", true);
        editor.putBoolean("show_color", Para.show_color);
        Para.allow_gprs = settings.getBoolean("allow_gprs", false);
        editor.putBoolean("allow_gprs", Para.allow_gprs);
        editor.commit();
    }

    public static void LoadTheme() {
        Para.THEME = Para.theme_black ? R.style.DarkTheme : R.style.LightTheme;
        Para.BACKGROUND = Para.theme_black ? R.drawable.dark_bg : R.drawable.default_bg;
    }

    public static void InitTheme() {
        Para.DEFAULT_TEXT_COLOR
                = App.get().getTheme().obtainStyledAttributes(Para.THEME, new int[]{android.R.attr.textColorSecondary})
                .getColor(0, Para.theme_black ? Color.WHITE : Color.BLACK);
        Para.HIGHLIGHT_TEXT_COLOR = Para.theme_black ? Color.YELLOW : Color.BLUE;
    }

    public static void InitOncePara() {
        Para.DB_CONTENT_PATH = App.get().getDatabasePath(Para.DB_CONTENT_NAME).getParent() + "/";
        Para.DB_DATA_PATH = App.get().getDatabasePath(Para.DB_DATA_NAME).getParent() + "/";

        SharedPreferences settings = App.get().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        Para.currentBook = settings.getInt("currentBook", 1);
        Para.currentChapter = settings.getInt("currentChapter", 1);
        Para.currentSection = settings.getInt("currentSection", 0);
        Para.mp3Mode = settings.getInt("mp3Mode", 0);
        Para.mp3Ver = settings.getInt("mp3Ver", 0);
        Para.downPath = settings.getString("downPath", Environment.getExternalStorageDirectory().getAbsolutePath());

        if (Para.currentBook < 1 || Para.currentBook > 73) {
            Para.currentBook = 1;
        }
        if (Para.currentChapter < 1 || Para.currentChapter > VerseInfo.CHAPTER_COUNT[Para.currentBook]) {
            Para.currentChapter = 1;
        }
    }

    public static String getdownPath() {
        File file = new File(Para.downPath);
        if (file.exists()) {
            return Para.downPath;
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

    public static File getFilePath(int type, String name) {
        File file;
        file = new File(getdownPath() + Para.BIBLE_MP3_PATH[type] + name);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    public static String getUrlPath(int type, String name) {
        String url = Para.BIBLE_MP3_URL[type] + name;

        return url;
    }

    public static String getFileName(int book, int chapter) {
        String name = String.format("%03d", book) + "_"
                + String.format("%03d", chapter) + ".mp3";
        return name;
    }

    public static String getUrlName(int book, int chapter) {
        String name = String.format("%03d", book) + "/"
                + String.format("%03d", chapter) + ".mp3";
        return name;
    }

    public static void ChangeChapter(boolean isNext) {
        if (!isNext) {
            if (Para.currentChapter > 1) {
                Para.currentChapter--;
            } else {
                if (Para.currentBook > 1) {
                    Para.currentBook--;
                    Para.currentChapter = VerseInfo.CHAPTER_COUNT[Para.currentBook];
                }
            }
        } else {
            if (Para.currentChapter < VerseInfo.CHAPTER_COUNT[Para.currentBook]) {
                Para.currentChapter++;
            } else {
                if (Para.currentBook < 73) {
                    Para.currentBook++;
                    Para.currentChapter = 1;
                }
            }
        }
        Para.currentSection = 0;
    }

    public static void downChapter(int type, int book, int chapter) {
        final File file = Func.getFilePath(type, Func.getFileName(book, chapter));
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
            if (App.get().isWifi() || Para.allow_gprs) {
                download(type, book, chapter);
                Toast.makeText(App.get(), "下载已添加", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(App.get(), "下载失败\n请在WIFI环境下再下载\n或在设置中打开使用数据流量选项", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void downBook(int type, int book) {
        if (App.get().isWifi() || Para.allow_gprs) {
            for (int i = 1; i <= VerseInfo.CHAPTER_COUNT[book]; i++) {
                download(type, book, i);
            }
            Toast.makeText(App.get(), "下载已添加", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(App.get(), "下载失败\n请在WIFI环境下再下载\n或在设置中打开使用数据流量选项", Toast.LENGTH_LONG).show();
        }
    }

    public static long download(int type, int book, int chapter) {
        long reference = -1;
        final File file = Func.getFilePath(type, Func.getFileName(book, chapter));
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
            String url = Func.getUrlPath(type, Func.getUrlName(book, chapter));
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDestinationUri(Uri.fromFile(file));
            request.setTitle(VerseInfo.CHN_NAME[book] + "第" + chapter + "章");
            request.setDescription("下载中");
            if (App.get().isWifi() || Para.allow_gprs) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            try {
                reference = mDownloadManager.enqueue(request);
            } catch (Exception e) {
                Toast.makeText(App.get(), "无法下载，请稍候重试（可能是同时进行的下载任务太多了）", Toast.LENGTH_SHORT).show();
            }
        }
        return reference;
    }

    /*
    public static ArrayList<String> getExternalSdCards(){
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            ArrayList<String> list = new ArrayList<String>();
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure")) continue;
                if (line.contains("asec")) continue;

                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if(columns[1].toLowerCase().contains("sdcard")){
                            list.add(columns[1]);
                        }
                    }
                } else if (line.contains("fuse")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if(columns[1].toLowerCase().contains("sdcard")){
                            list.add(columns[1]);
                        }
                    }
                }
            }
            return list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /*

    public class CopyFolderTask extends AsyncTask<String, Void, Boolean> {
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
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if(copyFolder(params[0],params[1])) {
                deleteFolder(params[0]);
                return true;
            } else {
                return false;
            }
        }
    }

    public static class CopyFilesTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(App.get());
            dialog.setMessage("正在迁移数据，请不要关闭软件");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean isSuccess = true;
            for(int i=1;i<params.length;i++) {
                isSuccess &= copyFile(params[i], params[0]);
            }
            if(isSuccess) {
                return true;
            } else {
                return false;
            }
        }
    }*/

    public static boolean copyFile(String oldFile, String newPath) {
        boolean isOk = true;
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File temp = new File(oldFile);
            if (temp.isFile()) {
                FileInputStream input = new FileInputStream(temp);
                FileOutputStream output = new FileOutputStream(newPath + "/" + temp.getName());
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    output.write(b, 0, len);
                }
                output.flush();
                output.close();
                input.close();
                temp.delete();
            }
        } catch (Exception e) {
            isOk = false;
        }
        return isOk;
    }

    public static void deleteFolder(String oldPath) {
        File file = new File(oldPath);
        try {
            if (file.exists()) {
                if (file.listFiles().length > 0) {
                    for (File f : file.listFiles()) {
                        if (f.isDirectory()) {
                            deleteFolder(f.getAbsolutePath());
                        } else {
                            f.delete();
                        }
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean copyFolder(String oldPath, String newPath) {
        boolean isOk = true;
        try {
            (new File(oldPath)).mkdirs();
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            String[] files = new File(oldPath).list();
            File temp;
            for (String name : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + name);
                } else {
                    temp = new File(oldPath + File.separator + name);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    temp.delete();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    if (!copyFolder(oldPath + "/" + name, newPath + "/" + name)) {
                        isOk = false;
                        return isOk;
                    } else {
                        temp.delete();
                    }
                }
            }
        } catch (Exception e) {
            isOk = false;
        }
        return isOk;
    }
}
