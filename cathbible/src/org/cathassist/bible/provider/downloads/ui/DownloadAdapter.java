/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cathassist.bible.provider.downloads.ui;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.cathassist.bible.R;
import org.cathassist.bible.provider.DownloadManager;
import org.cathassist.bible.provider.downloads.ui.DownloadItem.DownloadSelectListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * List adapter for Cursors returned by {@link DownloadManager}.
 */
public class DownloadAdapter extends CursorAdapter {
    final private int mTitleColumnId;
    final private int mStatusColumnId;
    final private int mReasonColumnId;
    final private int mTotalBytesColumnId;
    final private int mCurrentBytesColumnId;
    final private int mMediaTypeColumnId;
    final private int mDateColumnId;
    final private int mIdColumnId;
    private Context mContext;
    private Cursor mCursor;
    private DownloadSelectListener mDownloadSelectionListener;
    private Resources mResources;
    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;

    public DownloadAdapter(Context context, Cursor cursor,
                           DownloadSelectListener selectionListener) {
        super(context, cursor);
        mContext = context;
        mCursor = cursor;
        mResources = mContext.getResources();
        mDownloadSelectionListener = selectionListener;
        mDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        mTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        mIdColumnId = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
        mTitleColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
        mStatusColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
        mReasonColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
        mTotalBytesColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
        mCurrentBytesColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
        mMediaTypeColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
        mDateColumnId = cursor
                .getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
    }

    public View newView() {
        ViewHolder vh = new ViewHolder();
        DownloadItem view = (DownloadItem) LayoutInflater.from(mContext)
                .inflate(R.layout.download_list_item, null);
        vh.title = (TextView) view.findViewById(R.id.download_title);
        vh.down = (TextView) view.findViewById(R.id.down_text);
        vh.size = (TextView) view.findViewById(R.id.size_text);
        vh.status = (TextView) view.findViewById(R.id.status_text);
        vh.date = (TextView) view.findViewById(R.id.last_modified_date);
        vh.checkBox = (CheckBox) view.findViewById(R.id.download_checkbox);
        view.setTag(vh);
        view.setSelectListener(mDownloadSelectionListener);
        return view;
    }

    public void bindView(View convertView) {
        if (!(convertView instanceof DownloadItem)) {
            return;
        }
        ViewHolder vh = (ViewHolder) convertView.getTag();
        long downloadId = mCursor.getLong(mIdColumnId);
        ((DownloadItem) convertView).setDownloadId(downloadId);

        // Retrieve the icon for this download

        String title = mCursor.getString(mTitleColumnId);
        long totalBytes = mCursor.getLong(mTotalBytesColumnId);
        long currentBytes = mCursor.getLong(mCurrentBytesColumnId);
        int status = mCursor.getInt(mStatusColumnId);

        if (title.length() == 0) {
            title = mResources.getString(R.string.missing_title);
        }

        //int progress = getProgressValue(totalBytes, currentBytes);

        vh.title.setText(title);
        vh.down.setText(getSizeText(currentBytes));
        vh.size.setText(getSizeText(totalBytes));
        vh.status.setText(mResources.getString(getStatusStringId(status)));
        vh.date.setText(getDateString());
        vh.checkBox.setChecked(mDownloadSelectionListener
                .isDownloadSelected(downloadId));

        if (status == DownloadManager.STATUS_FAILED) {
            vh.status.setTextColor(mResources.getColor(R.color.red));
        } else if (status == DownloadManager.STATUS_PAUSED) {
            vh.status.setTextColor(mResources.getColor(R.color.green));
        } else {
            vh.status.setTextColor(vh.title.getCurrentTextColor());
        }
    }

    private String getDateString() {
        Date date = new Date(mCursor.getLong(mDateColumnId));
        if (date.before(getStartOfToday())) {
            return mDateFormat.format(date);
        } else {
            return mTimeFormat.format(date);
        }
    }

    private Date getStartOfToday() {
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }

    public int getProgressValue(long totalBytes, long currentBytes) {
        if (totalBytes == -1) {
            return 0;
        }
        return (int) (currentBytes * 100 / totalBytes);
    }

    private String getSizeText(long totalBytes) {
        String sizeText = "未知";
        if (totalBytes >= 0) {
            sizeText = Formatter.formatFileSize(mContext, totalBytes);
        }
        return sizeText;
    }

    private int getStatusStringId(int status) {
        switch (status) {
            case DownloadManager.STATUS_FAILED:
                return R.string.download_error;

            case DownloadManager.STATUS_SUCCESSFUL:
                return R.string.download_success;

            case DownloadManager.STATUS_PENDING:
            case DownloadManager.STATUS_RUNNING:
                return R.string.download_running;

            case DownloadManager.STATUS_PAUSED:
                if (mCursor.getInt(mReasonColumnId) == DownloadManager.PAUSED_QUEUED_FOR_WIFI) {
                    return R.string.download_queued;
                } else {
                    return R.string.download_paused;
                }
        }
        throw new IllegalStateException("Unknown status: "
                + mCursor.getInt(mStatusColumnId));
    }

    private void setTextForView(View parent, int textViewId, String text) {
        TextView view = (TextView) parent.findViewById(textViewId);
        view.setText(text);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return newView();
    }

    // CursorAdapter overrides

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bindView(view);
    }

    private static class ViewHolder {
        TextView title;
        TextView down;
        TextView size;
        TextView status;
        TextView date;
        CheckBox checkBox;
    }
}
