package org.cathassist.bible;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class App extends Application implements Thread.UncaughtExceptionHandler {
    private static App thiz;

    public App() {
        thiz = this;
    }

    public static App get() {
        if (thiz == null) {
            throw new IllegalStateException("App can't be null after it is created");
        }
        return thiz;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thiz = this;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        MobclickAgent.reportError(App.get(), "Uncaught Error:" + getExceptionCause(ex));
        Toast.makeText(this, "很抱歉，圣经小助手出了点小问题:(", Toast.LENGTH_SHORT).show();
        /*
        SharedPreferences settings = getSharedPreferences(Para.STORE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Calendar nowTime = Calendar.getInstance();
        Calendar lastTime = Calendar.getInstance();
        lastTime.setTimeInMillis(settings.getLong("last_crash",0));
        editor.putLong("last_crash",nowTime.getTimeInMillis());
        editor.commit();
        */
        android.os.Process.killProcess(android.os.Process.myPid());

        /*
        if((nowTime.getTimeInMillis() - lastTime.getTimeInMillis()) > 15000) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent restartIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500,
                    restartIntent); // 500ms后重启应用
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }*/
    }

    private String getExceptionCause(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return "UnCaught Exception: " + writer.toString();
    }

    public String getDeviceId() {
        TelephonyManager mTeleManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mTeleManager != null) {
            return mTeleManager.getDeviceId();
        }
        return null;
    }

    public String getMacAddress() {
        WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                return info.getMacAddress();
            }
        }
        return null;
    }

    public boolean isWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public int getVersionCode() {
        int code;
        try {
            code = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            code = 0;
        }
        return code;
    }

    public String getVersionName() {
        String verName;
        try {
            verName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            verName = "";
        }
        return verName;
    }
}
