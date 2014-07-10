package org.cathassist.bible.lib;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class ProgressShow {
    public static final int DIALOG_TYPE_SPINNER = 0;
    public static final int DIALOG_TYPE_BAR = 1;
    public static final int DIALOG_DEFAULT_MAX = 100;
    public static final int DIALOG_DEFAULT_INCREASE = 1;
    public static final int DIALOG_SHOW = 0;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DIALOG_SHOW) {
                dialog.show();
            } else {
                dialog.incrementProgressBy(msg.what);
                if (GetProgress() >= GetMax() || msg.what >= GetMax()) {
                    dialog.cancel();
                }
            }

            super.handleMessage(msg);
        }
    };
    private ProgressDialog dialog = null;

    public ProgressShow(Context context, String title, String message, int type, int max) {
        dialog = new ProgressDialog(context);

        switch (type) {
            case DIALOG_TYPE_SPINNER:
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                break;

            case DIALOG_TYPE_BAR:
            default:
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                break;
        }

        dialog.setMax(max);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(false);
        //dialog.setCancelable(false);
        //dialog.setCanceledOnTouchOutside(false);
        dialog.setProgress(-dialog.getProgress());
    }

    public void ShowDialog(final ProgressCallBack callBack) {
        handler.sendEmptyMessage(DIALOG_SHOW);

        new Thread() {
            public void run() {
                callBack.action();
            }
        }.start();
    }

    public void AddProgress(int increase) {
        handler.sendEmptyMessage(increase);
    }

    public int GetProgress() {
        return dialog.getProgress();
    }

    public void CloseDialog() {
        handler.sendEmptyMessage(GetMax());
    }

    public int GetMax() {
        return dialog.getMax();
    }

    public interface ProgressCallBack {
        public void action();
    }

}
