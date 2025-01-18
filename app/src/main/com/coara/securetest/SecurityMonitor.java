package com.coara.securetest;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;

public class SecurityMonitor {
    private final Context context;
    private final Handler handler = new Handler();
    private final int CHECK_INTERVAL = 2000;

    public SecurityMonitor(Context context) {
        this.context = context;
    }

    public void startMonitoring() {
        handler.post(checkSecuritySettings);
    }

    private final Runnable checkSecuritySettings = new Runnable() {
        @Override
        public void run() {
            try {

                boolean isDevModeEnabled = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;


                boolean isUsbDebuggingEnabled = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.ADB_ENABLED, 0) == 1;

                if (isDevModeEnabled || isUsbDebuggingEnabled) {
                    stopApp("セキュリティ監視によりアプリを停止します。");
                } else {
                    handler.postDelayed(this, CHECK_INTERVAL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void stopApp(String reason) {
        System.out.println(reason);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void stopMonitoring() {
        handler.removeCallbacks(checkSecuritySettings);
    }
}