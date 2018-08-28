package com.niucong.punchcardserver.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import org.litepal.LitePal;

import java.util.List;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.PushListener;

public class App extends MultiDexApplication {

    public static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        LitePal.initialize(this);
        Stetho.initializeWithDefaults(this);

        //TODO 集成：1.4、初始化数据服务SDK、初始化设备信息并启动推送服务
// 初始化BmobSDK
        Bmob.initialize(this, "d82f0138681a35ea0ab3b7194f0e5a22");
// 使用推送服务时的初始化操作
        BmobInstallationManager.getInstance().initialize(new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                if (e == null) {
                    Log.i("App", bmobInstallation.getObjectId() + "-" + bmobInstallation.getInstallationId());
                } else {
                    Log.e("App", e.getMessage());
                }
            }
        });
// 启动推送服务
        BmobPush.startWork(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    public static void showToast(String text) {
        Toast.makeText(app, text, Toast.LENGTH_LONG).show();
    }



    public static void addPush(List<String> ids, org.json.JSONObject json) {

        BmobPushManager bmobPushManager = new BmobPushManager();
        BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
        //TODO 属性值为android
//                    query.addWhereEqualTo("deviceType", "android");
        query.addWhereContainsAll("installationId", ids);
        bmobPushManager.pushMessage(json, new PushListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Log.e("MainActivity", "推送成功！");
                } else {
                    Log.e("MainActivity", "异常：" + e.getMessage());
                }
            }
        });
    }

}
