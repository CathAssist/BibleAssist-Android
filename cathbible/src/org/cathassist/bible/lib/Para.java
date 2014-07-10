package org.cathassist.bible.lib;

import android.graphics.Color;

import org.cathassist.bible.R;
import org.cathassist.bible.music.MusicPlayService;

public class Para {
    public static final String DB_CONTENT_ASSET = "cathbible";
    public static final String DB_CONTENT_NAME = "cathbible.db";
    public static final int DB_CONTENT_VER = 4;
    public static final String DB_DATA_NAME = "data.db";
    public static final int DB_DATA_VER = 1;
    public static final int VERSE_NUMBER = 473;
    public static final String STORE_NAME = "Settings";
    public static final int DB_CONTENT_COUNT = 10;
    public static final String[] MENU_NAME =
            {"主页", "圣经", "书签", "金句", "搜索", "设置"};
    public static final int[] MENU_IMAGE =
            {R.drawable.nav_home, R.drawable.nav_bible, R.drawable.nav_mark,
                    R.drawable.nav_verse, R.drawable.nav_search, R.drawable.nav_setting};
    public static final int MENU_HOME = 0;
    public static final int MENU_BIBLE = 1;
    public static final int MENU_MARK = 2;
    public static final int MENU_VERSE = 3;
    public static final int MENU_SEARCH = 4;
    public static final int MENU_SET = 5;
    public static final int NEED_RESTART = 1;
    public static final int NOT_NEED_RESTART = 0;
    public static final String[] VERSE_CHOISE = {"添加查看书签", "复制到剪贴板", "分享到网络"};
    public static final int VERSE_CHOISE_MARK = 0;
    public static final int VERSE_CHOISE_COPY = 1;
    public static final int VERSE_CHOISE_SHARE = 2;
    public static final String[] BIBLE_MP3_URL = {
            "http://media.cathassist.org/bible/mp3/cn/female/",
            "http://media.cathassist.org/bible/mp3/cn/male/"};
    public static final String ROOT_PATH = "/cathbible";
    public static final String[] BIBLE_MP3_PATH = {
            "/cathbible/bible/mp3/chn_female/",
            "/cathbible/bible/mp3/chn_male/"};
    public static final String[] BIBLE_MP3_VERSION = {
            "思高版女声",
            "思高版男声"};
    public static String DB_CONTENT_PATH = "";
    public static String DB_DATA_PATH = "";
    //public static String BIBLE_MP3_PATH = "";
    public static int DEFAULT_TEXT_COLOR = Color.BLACK;
    public static int HIGHLIGHT_TEXT_COLOR = Color.BLUE;
    public static int THEME = R.style.LightTheme;
    public static int BACKGROUND = R.drawable.default_bg;
    public static Float font_size;
    public static boolean always_bright;
    public static boolean theme_black;
    public static boolean auto_update;
    public static boolean show_color;
    public static boolean allow_gprs;
    public static int currentBook;
    public static int currentChapter;
    public static int currentSection;
    public static int currentCount;
    public static int lastBook;
    public static int lastChapter;
    public static int lastSection;
    public static int bookmarkPos = 0;
    public static int mp3Mode = MusicPlayService.MODE_SINGLE;
    public static int menuIndex = 0;
    public static int mp3Ver = 0;
    public static String downPath = "";
}
