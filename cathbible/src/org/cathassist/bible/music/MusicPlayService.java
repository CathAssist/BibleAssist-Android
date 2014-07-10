package org.cathassist.bible.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import org.cathassist.bible.App;
import org.cathassist.bible.MainActivity;
import org.cathassist.bible.R;
import org.cathassist.bible.lib.Func;
import org.cathassist.bible.lib.Para;
import org.cathassist.bible.lib.VerseInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MusicPlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    public static final int MODE_SINGLE = 0;
    public static final int MODE_SINGLE_LOOP = 1;
    public static final int MODE_ORDER = 2;

    public static final int CMD_PREV = 0;
    public static final int CMD_PLAY = 1;
    public static final int CMD_NEXT = 2;

    private List<OnCompletionListener> mOnCompletionListener = new ArrayList<OnCompletionListener>();
    private List<OnPlayListener> mOnPlayListener = new ArrayList<OnPlayListener>();
    private List<OnPlayChangedListener> mOnPlayChangedListener = new ArrayList<OnPlayChangedListener>();
    private MediaPlayer mPlayer;
    private String mLast = "";
    private int mPlayMode;
    private Handler mPlayHandler;
    private Runnable mPlayRunnable;
    private boolean mCallPlay = false;
    private Notification mNotification;
    private String mName = "圣经";
    private Remote mRemote;

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mPlayer != null) {
            stop();
            mPlayer.release();
            mPlayer = null;
        }
        stopForeground(true);
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        mPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);

        mPlayHandler = new Handler();
        mPlayRunnable = new Runnable() {
            @Override
            public void run() {
                if (mPlayer.isPlaying()) {
                    for (OnPlayListener listener : mOnPlayListener) {
                        if (listener != null) {
                            listener.onPlay(getProgress(), getDuration());
                        }
                    }
                }
                mPlayHandler.postDelayed(this, 1000);
            }
        };

        TelephonyManager teleManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        teleManager.listen(new CyclePhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        setNotification();
        NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.cancel(this.getClass().getName().hashCode());
        refreshNotification();
    }

    private void refreshNotification() {
        startForeground(this.getClass().getName().hashCode(), mNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mPlayer == null) {
            stopSelf();
        } else {
            try {
                int cmd = intent.getIntExtra("CMD", -1);
                switch (cmd) {
                    case CMD_PREV:
                        Func.ChangeChapter(false);
                        break;
                    case CMD_PLAY:
                        break;
                    case CMD_NEXT:
                        Func.ChangeChapter(true);
                        break;
                }
                if (cmd != -1) {
                    final File file = Func.getFilePath(Para.mp3Ver, Func.getFileName(Para.currentBook, Para.currentChapter));
                    if (file.exists()) {
                        play(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                    } else {
                        reset();
                        playNet(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                        Func.downChapter(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setNotification() {
        Intent prevIntent = new Intent();
        prevIntent.setClass(this, MusicPlayService.class);
        prevIntent.putExtra("CMD", CMD_PREV);

        Intent playIntent = new Intent();
        playIntent.setClass(this, MusicPlayService.class);
        playIntent.putExtra("CMD", CMD_PLAY);

        Intent nextIntent = new Intent();
        nextIntent.setClass(this, MusicPlayService.class);
        nextIntent.putExtra("CMD", CMD_NEXT);

        Intent appIntent = new Intent();
        appIntent.setClass(this, MainActivity.class);

        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.HONEYCOMB) {
            mRemote = new Remote(this.getPackageName(), R.layout.mp3_notification);
            mRemote.setOnClickPendingIntent(R.id.noti_prev, PendingIntent
                    .getService(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            mRemote.setOnClickPendingIntent(R.id.noti_play, PendingIntent
                    .getService(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            mRemote.setOnClickPendingIntent(R.id.noti_next, PendingIntent
                    .getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            mRemote.setOnClickPendingIntent(R.id.root, PendingIntent
                    .getActivity(this, 3, appIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            mNotification = new Notification();
            mNotification.icon = R.drawable.ic_launcher;
            mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
            mNotification.contentView = mRemote;
        } else {
            mRemote = new Remote(this.getPackageName(), R.layout.mp3_notification_old);

            mNotification = new Notification();
            mNotification.icon = R.drawable.ic_launcher;
            mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
            mNotification.contentView = mRemote;
            mNotification.contentIntent = PendingIntent.getActivity(this, 4, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            stop();
            mPlayer.release();
            mPlayer = null;
        }
        stopForeground(true);
        super.onDestroy();
    }

    public void playNet(int type, int book, int chapter) {
        mName = VerseInfo.CHN_NAME[book] + "第" + chapter + "章";
        String file = Func.getUrlPath(type, Func.getUrlName(book, chapter));
        if (mLast.equals(type + "/" + book + "/" + chapter) && getProgress() < getDuration()) {            //同一首
            if (mPlayer.isPlaying()) {           //在播放
                pause();
            } else {
                start();
            }
        } else {                            //不同首
            reset();
            if (App.get().isWifi() || Para.allow_gprs) {
                mRemote.setTextViewText(R.id.noti_name, "正在加载中");
                refreshNotification();
                try {
                    mPlayer.setDataSource(file);
                    mPlayer.prepareAsync();
                    mLast = type + "/" + book + "/" + chapter;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void play(int type, int book, int chapter) {
        mName = VerseInfo.CHN_NAME[book] + "第" + chapter + "章";
        String file = Func.getFilePath(type, Func.getFileName(book, chapter)).getAbsolutePath();
        if (mLast.equals(type + "/" + book + "/" + chapter) && getProgress() < getDuration()) {            //同一首
            if (mPlayer.isPlaying()) {           //在播放
                pause();
            } else {
                start();
            }
        } else {                            //不同首
            reset();
            try {
                mPlayer.setDataSource(file);
                mPlayer.prepareAsync();
                mLast = type + "/" + book + "/" + chapter;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        reset();
    }

    private void start() {
        mPlayer.start();
        mPlayHandler.postDelayed(mPlayRunnable, 1000);
        mRemote.setTextViewText(R.id.noti_name, mName);
        mRemote.setImageViewResource(R.id.noti_play, R.drawable.icon_pause);
        refreshNotification();
        for (OnPlayChangedListener listener : mOnPlayChangedListener) {
            if (listener != null) {
                listener.onPlayChanged(true);
            }
        }
    }

    private void reset() {
        mPlayer.reset();
        mLast = "";
        mPlayHandler.removeCallbacks(mPlayRunnable);
        mRemote.setTextViewText(R.id.noti_name, mName);
        mRemote.setImageViewResource(R.id.noti_play, R.drawable.icon_play);
        refreshNotification();
        for (OnPlayChangedListener listener : mOnPlayChangedListener) {
            if (listener != null) {
                listener.onPlayChanged(false);
            }
        }
    }

    private void pause() {
        mPlayer.pause();
        mPlayHandler.removeCallbacks(mPlayRunnable);
        mRemote.setTextViewText(R.id.noti_name, mName);
        mRemote.setImageViewResource(R.id.noti_play, R.drawable.icon_play);
        refreshNotification();
        for (OnPlayChangedListener listener : mOnPlayChangedListener) {
            if (listener != null) {
                listener.onPlayChanged(false);
            }
        }
    }

    public int getProgress() {
        return mPlayer.getCurrentPosition();
    }

    public void setProgress(int progress) {
        if (mPlayer.isPlaying()) {
            mPlayer.seekTo(progress);
        }
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public void setPlayMode(int mode) {
        mPlayMode = mode;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayMode == MODE_SINGLE) {
            reset();
        } else if (mPlayMode == MODE_SINGLE_LOOP) {
            play(Para.mp3Ver, Para.currentBook, Para.currentChapter);
        } else if (mPlayMode == MODE_ORDER) {
            Func.ChangeChapter(true);
            final File file = Func.getFilePath(Para.mp3Ver, Func.getFileName(Para.currentBook, Para.currentChapter));
            if (file.exists()) {
                play(Para.mp3Ver, Para.currentBook, Para.currentChapter);
            } else {
                playNet(Para.mp3Ver, Para.currentBook, Para.currentChapter);
                Func.downChapter(Para.mp3Ver, Para.currentBook, Para.currentChapter);
            }
        }
        for (OnCompletionListener listener : mOnCompletionListener) {
            if (listener != null) {
                listener.onCompletion();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener.add(listener);
    }

    public void setOnPlayListener(OnPlayListener listener) {
        mOnPlayListener.add(listener);
    }

    public void setOnPlayChangedListener(OnPlayChangedListener listener) {
        mOnPlayChangedListener.add(listener);
    }

    public interface OnCompletionListener {
        void onCompletion();
    }

    public interface OnPlayListener {
        void onPlay(int progress, int duration);
    }

    public interface OnPlayChangedListener {
        void onPlayChanged(boolean isPlay);
    }

    private class Remote extends RemoteViews {

        public Remote(String packageName, int layoutId) {
            super(packageName, layoutId);
        }
    }

    public class MusicPlayBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
        }
    }

    private final class CyclePhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (mPlayer != null) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        mCallPlay = mPlayer.isPlaying();
                        if (mCallPlay) {
                            pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mCallPlay) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    start();
                                    mCallPlay = false;
                                }
                            }, 1000);
                        }
                        break;
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
}
