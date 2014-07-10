package org.cathassist.bible.lib;

import com.umeng.common.net.s;

public class VerseInfo {
    public static final String[] CHN_NAME =
            {"", "创世纪", "出谷纪", "肋未纪", "户藉纪", "申命纪", "若苏厄书", "民长纪", "卢德传", "撒慕尔纪上",
                    "撒慕尔纪下", "列王纪上", "列王纪下", "编年纪上", "编年纪下", "厄斯德拉上", "厄斯德拉下", "多俾亚传", "友弟德传", "艾斯德尔传",
                    "玛加伯上", "玛加伯下", "约伯传", "圣咏集", "箴言", "训道篇", "雅歌", "智慧篇", "德训篇", "依撒意亚",
                    "耶肋米亚", "耶肋米亚哀歌", "巴路克", "厄则克尔", "达尼尔", "欧色亚", "岳厄尔", "亚毛斯", "亚北底亚", "约纳",
                    "米该亚", "纳鸿", "哈巴谷", "索福尼亚", "哈盖", "匝加利亚", "玛拉基亚", "玛窦福音", "玛尔谷福音", "路加福音",
                    "若望福音", "宗徒大事录", "罗马书", "格林多前书", "格林多后书", "迦拉达书", "厄弗所书", "斐理伯书", "哥罗森书", "得撒洛尼前书",
                    "得撒洛尼后书", "弟茂德前书", "弟茂德后书", "弟铎书", "费肋孟书", "希伯莱书", "雅各伯书", "伯多禄前书", "伯多禄后书", "若望一书",
                    "若望二书", "若望三书", "犹达书", "若望默示录"};
    public static final String[] CHN_ABBR =
            {"", "创", "出", "肋", "户", "申", "苏", "民", "卢", "撒上",
                    "撒下", "列上", "列下", "编上", "编下", "厄上", "厄下", "多", "友", "艾",
                    "加上", "加下", "约", "咏", "箴", "训", "歌", "智", "德", "依",
                    "耶", "哀", "巴", "则", "达", "欧", "岳", "亚", "北", "纳",
                    "米", "鸿", "哈", "索", "盖", "匝", "拉", "玛", "谷", "路",
                    "若", "宗", "罗", "格前", "格后", "迦", "弗", "斐", "哥", "得前",
                    "得后", "弟前", "弟后", "铎", "费", "希", "雅", "伯前", "伯后", "若一",
                    "若二", "若三", "犹", "默"
            };
    public static final String[] ENG_NAME =
            {"", "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel",
                    "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Tobit", "Judith", "Esther",
                    "1 Maccabees", "2 Maccabees", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Songs", "Wisdom", "Ecclesiasticus", "Isaiah",
                    "Jeremiah", "Lamentations", "Baruch", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah",
                    "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi", "Matthew", "Mark", "Luck",
                    "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
                    "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter", "1 John",
                    "2 John", "3 John", "Jude", "Revelation"};
    public static final int[] CHAPTER_COUNT =
            {0, 50, 40, 27, 36, 34, 24, 21, 4, 31,
                    24, 22, 25, 29, 36, 10, 13, 14, 16, 10,
                    16, 15, 42, 150, 31, 12, 8, 19, 51, 66,
                    52, 5, 6, 48, 14, 14, 4, 9, 1, 4,
                    7, 3, 3, 3, 2, 14, 3, 28, 16, 24,
                    21, 28, 16, 16, 13, 6, 6, 4, 4, 5,
                    3, 6, 4, 3, 1, 13, 5, 5, 3, 5,
                    1, 1, 1, 22};

    public static final String[] BOOK_SCOPE = {"全书（创-默）","旧约（创-拉）","新约（玛-默）","梅瑟五书（创-申）","旧约史书（苏-加下）","智慧书（约-德）","大先知书（依-达）","小先知书（欧-拉）","四福音（玛-若）","教会历史（宗）","保禄书信（罗-费）","公函（希-犹）","若望默示录（默）"};

    public static final int WHOLE_BOOK = 0;
    public static final int OLD_TESTAMENT = 1;
    public static final int NEW_TESTAMENT = 2;
    public static final int PENTATEUCH = 3;
    public static final int HISTORY = 4;
    public static final int WISDOM_AND_POETRY = 5;
    public static final int MAJOR_PROPHETS = 6;
    public static final int MINOR_PROPHETS = 7;
    public static final int GOSPELS = 8;
    public static final int ACTS_OF_APOSTLES = 9;
    public static final int PAULINE_EPISTLES = 10;
    public static final int GENERAL_EPISTLES = 11;
    public static final int APOCALYPTIC = 12;

    public static int getBookType(int book) {
        int type = 0;
        if(book >= 1 && book <= 5) {
            type = PENTATEUCH;
        } else if(book >= 6 && book <= 21) {
            type = HISTORY;
        } else if(book >= 22 && book <= 28) {
            type = WISDOM_AND_POETRY;
        } else if(book >= 29 && book <= 34) {
            type = MAJOR_PROPHETS;
        } else if(book >= 35 && book <= 46) {
            type = MINOR_PROPHETS;
        } else if(book >= 47 && book <= 50) {
            type = GOSPELS;
        } else if(book >= 51 && book <= 51) {
            type = ACTS_OF_APOSTLES;
        } else if(book >= 52 && book <= 64) {
            type = PAULINE_EPISTLES;
        } else if(book >= 65 && book <= 72) {
            type = GENERAL_EPISTLES;
        } else if(book >= 73 && book <= 73) {
            type = APOCALYPTIC;
        }
        return type;
    }

    public static String getSearchScope(int scope) {
        String scopeString = "";
        switch (scope) {
            case OLD_TESTAMENT:
                scopeString = " AND book >= 1 AND book <= 46 ";
                break;
            case NEW_TESTAMENT:
                scopeString = " AND book >= 40 AND book <= 73 ";
                break;
            case PENTATEUCH:
                scopeString = " AND book >= 1 AND book <= 5 ";
                break;
            case HISTORY:
                scopeString = " AND book >= 6 AND book <= 21 ";
                break;
            case WISDOM_AND_POETRY:
                scopeString = " AND book >= 22 AND book <= 28 ";
                break;
            case MAJOR_PROPHETS:
                scopeString = " AND book >= 29 AND book <= 34 ";
                break;
            case MINOR_PROPHETS:
                scopeString = " AND book >= 35 AND book <= 46 ";
                break;
            case GOSPELS:
                scopeString = " AND book >= 47 AND book <= 50 ";
                break;
            case ACTS_OF_APOSTLES:
                scopeString = " AND book = 51 ";
                break;
            case PAULINE_EPISTLES:
                scopeString = " AND book >= 52 AND book <= 64 ";
                break;
            case GENERAL_EPISTLES:
                scopeString = " AND book >= 65 AND book <= 72 ";
                break;
            case APOCALYPTIC:
                scopeString = " AND book = 73 ";
                break;
            case WHOLE_BOOK:
                scopeString = " AND book >= 1 AND book <= 73 ";
            default:
                break;
        }
        return scopeString;
    }
}
