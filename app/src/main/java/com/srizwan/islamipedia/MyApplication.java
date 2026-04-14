package com.srizwan.islamipedia;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Uncaught Exception Handler সেট করুন
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // সমস্যার ডাটা Intent এর মাধ্যমে পাঠান
                Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                intent.putExtra("error_message", throwable.getMessage());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // অ্যাপ বন্ধ করুন
                System.exit(1);
            }
        });
    }
}

