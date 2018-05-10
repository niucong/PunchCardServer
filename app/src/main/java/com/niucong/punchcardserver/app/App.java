package com.niucong.punchcardserver.app;

import android.app.Application;
import android.widget.Toast;

import org.litepal.LitePal;

public class App extends Application {

    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        LitePal.initialize(this);
    }

    public static void showToast(String text) {
        Toast.makeText(app, text, Toast.LENGTH_LONG).show();
    }
}
