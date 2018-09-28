package com.niucong.punchcardserver.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.baidu.tts.client.SpeechSynthesizer;
import com.facebook.stetho.Stetho;
import com.umeng.analytics.MobclickAgent;

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

public class App extends Application {

    public static App app;

    public String appId = "11785389";
    public String appKey = "tajQ8tfjOzMFAurBlhQBLaKk";
    public String secretKey = "CVbYIhkRZ66qjeRPi7cZfa9sFGhpv6Bi";

    public SpeechSynthesizer mSpeechSynthesizer;

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

        MobclickAgent.setScenarioType(this, null);
//        MobclickAgent. startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this,"",""));
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
//        query.addWhereEqualTo("deviceType", "android");
//        query.addWhereEqualTo("installationId", ids.get(0));
        query.addWhereContainedIn("installationId", ids);
        bmobPushManager.setQuery(query);
        bmobPushManager.pushMessage(json, new PushListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Log.e("App", "推送成功！");
                } else {
                    Log.e("App", "异常：" + e.getMessage());
                }
            }
        });
    }

}
