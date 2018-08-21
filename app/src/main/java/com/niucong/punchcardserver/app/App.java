package com.niucong.punchcardserver.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import org.litepal.LitePal;

public class App extends MultiDexApplication {

    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        LitePal.initialize(this);
        Stetho.initializeWithDefaults(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    public static void showToast(String text) {
        Toast.makeText(app, text, Toast.LENGTH_LONG).show();
    }
}
