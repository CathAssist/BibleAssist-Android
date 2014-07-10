package org.cathassist.bible.lib;

import org.cathassist.bible.HomeFragment;
import org.cathassist.bible.SearchFragment;
import org.cathassist.bible.VerseFragment;
import org.cathassist.bible.mark.MarkFragment;
import org.cathassist.bible.read.BibleReadFragment;

public class FragmentManager {
    public static HomeFragment homeFragment;
    public static BibleReadFragment bibleReadFragment;
    public static MarkFragment markFragment;
    public static VerseFragment verseFragment;
    public static SearchFragment searchFragment;

    public static void initFragments() {
        homeFragment = new HomeFragment();
        bibleReadFragment = new BibleReadFragment();
        markFragment = new MarkFragment();
        verseFragment = new VerseFragment();
        searchFragment = new SearchFragment();
    }

}
