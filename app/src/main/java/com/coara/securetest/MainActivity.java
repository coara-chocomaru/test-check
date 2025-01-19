package com.coara.securetest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends Activity {

    private TextView textView;
    private boolean isColorChanged = false;
    private SecurityMonitor securityMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

 
        if (isRooted() || isUsbDebuggingEnabled() || isDeveloperOptionsEnabled()) {
            showSecurityWarningAndExit();
            return;
        }

        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.helloText);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        textView.startAnimation(fadeIn);

        // テキストの色変更
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isColorChanged) {
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                } else {
                    textView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }
                isColorChanged = !isColorChanged;
            }
        });

        generateDummyFiles();

        securityMonitor = new SecurityMonitor(this);
        securityMonitor.startMonitoring();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (securityMonitor != null) {
            securityMonitor.stopMonitoring();
        }
    }

    private boolean isRooted() {
        String[] paths = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/system_ext/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsbDebuggingEnabled() {
        try {
            return Settings.Global.getInt(
                    getContentResolver(),
                    Settings.Global.ADB_ENABLED, 0) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDeveloperOptionsEnabled() {
        try {
            return Settings.Global.getInt(
                    getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private void showSecurityWarningAndExit() {
        Toast.makeText(this, "セキュリティ違反: アプリを終了します", Toast.LENGTH_SHORT).show();
        Log.e("Security", "アプリがセキュリティ要件を満たしていません。終了します。");
        finish();
    }

    private void generateDummyFiles() {
        File dummyDir = new File(getFilesDir(), "dummy");
        if (!dummyDir.exists()) {
            dummyDir.mkdir();
        }

        for (int i = 0; i < 200; i++) {
            File dummyFile = new File(dummyDir, generateRandomFileName());
            try {
                FileWriter writer = new FileWriter(dummyFile);
                writer.write("This is a dummy file.");
                writer.close();
            } catch (IOException e) {
                Log.e("DummyFile", "Failed to create dummy file: " + e.getMessage());
            }
        }
    }

    private String generateRandomFileName() {
        String[] words = {"Secure", "Android", "File", "Data", "Dummy", "Protect", "Code", "Example"};
        return words[(int) (Math.random() * words.length)] + "_" + System.nanoTime() + ".txt";
    }
}
