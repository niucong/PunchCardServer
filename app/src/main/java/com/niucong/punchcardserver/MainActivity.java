package com.niucong.punchcardserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.databinding.ActivityMainBinding;
import com.niucong.punchcardserver.service.ServerManager;
import com.niucong.yunshitu.FaceDetectActivity;
import com.niucong.yunshitu.config.Configuration;
import com.niucong.yunshitu.config.GlobalConfiguration;
import com.niucong.yunshitu.config.JSONConfiguration;
import com.niucong.yunshitu.face.FaceReg;
import com.niucong.yunshitu.util.NetworkUtils;
import com.niucong.yunshitu.util.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.util.LinkedList;
import java.util.List;

import cn.yunshitu.facesdk.face.KnnFaceReg;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ServerManager mServerManager;
    private String mRootUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        setContentView(R.layout.activity_main);
//        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setHandlers(new MainClickHandlers());

        // AndServer run in the service.
        mServerManager = new ServerManager(this);
        mServerManager.register();

        initialTts();

        if (!setPermission(this, this, new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, 1)) {
        }

        // 开始后台载入工作
        new LoadTask().execute("1.8.0");
    }

    /**
     * Activity 6.0运行权限设置
     *
     * @param context
     * @param activity
     * @param permissions 权限  Manifest.permission.
     * @param type
     */
    public static boolean setPermission(Context context, Activity activity, String[] permissions,
                                        int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permissions[0]) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, permissions, type);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "权限被拒绝,无法使用此应用", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* 控件 */
//    private View mDetectBtn;
//    private TextView mProgressText;
//    private ProgressBar mProgressBar;
//    private View mAdduserBtn;

    @SuppressLint("StaticFieldLeak")
    // 后台初始化
    private class InitTask extends AsyncTask<Integer, String, Void> {
        private Class<?> mStartActivity;

        public InitTask(Class<?> startActivity) {
            mStartActivity = startActivity;
        }

        @Override
        protected void onPreExecute() {
//            // 启用进度条
//            mProgressBar.setVisibility(View.VISIBLE);
//            // 禁用按钮
//            mDetectBtn.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Integer... index) {
            publishProgress("正在载入");

            // 载入OpenCV
            OpenCVLoader.initDebug();

            // 获得单例配置
            if (GlobalConfiguration.getConfiguration() == null) {
                Configuration config = null;
                config = new JSONConfiguration(getApplicationContext(), "{}");
                GlobalConfiguration.setConfiguration(config);
            } else {
                GlobalConfiguration.getConfiguration().waitForInit();
            }
            if (FaceReg.INSTANCE == null) {
                FaceReg.INSTANCE = new KnnFaceReg(
                        getApplicationContext(),
                        GlobalConfiguration.getConfiguration().getFeaturer(),
                        GlobalConfiguration.getConfiguration().getResizeWidth(),
                        GlobalConfiguration.getConfiguration().getResizeHeight(),
                        GlobalConfiguration.getConfiguration().getCropWidth(),
                        GlobalConfiguration.getConfiguration().getCropHeight()
                );
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length > 0) {
                App.showToast(values[0]);
//                mProgressText.setText(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//            mProgressBar.setVisibility(View.INVISIBLE);
//            mProgressText.setText(null);
            startActivity(new Intent(MainActivity.this, mStartActivity));
//            mDetectBtn.setEnabled(true);

            /*MatOfPoint2f m1 = new MatOfPoint2f(
                    new Point(0.4527, 0.4749),
                    new Point(0.638, 0.4543),
                    new Point(0.6019, 0.5565),
                    new Point(0.5034, 0.6427),
                    new Point(0.6475, 0.6234)
            );
            MatOfPoint2f m2 = new MatOfPoint2f(
                    new Point(0.3893, 0.4764),
                    new Point(0.5877, 0.4706),
                    new Point(0.4945, 0.5736),
                    new Point(0.4219, 0.6477),
                    new Point(0.5699, 0.6455)
            );
            cn.yunshitu.facesdk.face.Helper.livingBody(m1, m2);*/

            /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abel_pacheco);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            Log.i(TAG, "channel: " + mat.channels());
            Imgproc.resize(mat, mat, new Size(142, 142));
            Mat cropMat = new Mat(mat,
                    new Rect(
                            Math.abs(mat.width() - 128) / 2,
                            Math.abs(mat.height() - 128) / 2,
                            128,
                            128));
            Imgproc.cvtColor(cropMat, cropMat, Imgproc.COLOR_RGBA2BGR, 3);
            Featurer featurer = ModelHelper.getLocalCaffeFeaturer(getApplicationContext(),
                    "caffe", "feature_shuffle_net", 1, 0.017f, new float[] {0});
            double[] features = featurer.extract(cropMat);
            mat.release();
            cropMat.release();

            StringBuilder sb = new StringBuilder();
            for (double val : features) {
                sb.append(val).append(",");
            }
            Log.i(TAG, "features: " + sb.toString());*/
        }
    }

    @SuppressLint("StaticFieldLeak")
    // 后台载入
    private class LoadTask extends AsyncTask<String, Void, Void> {
        private boolean mIsOnline;

        @Override
        protected void onPreExecute() {
            mIsOnline = SharedPreferencesUtils.isUseOnlineConfig(getApplicationContext());
        }

        @Override
        protected Void doInBackground(String... str) {
            long start = System.currentTimeMillis();
            Configuration configuration = GlobalConfiguration.getConfiguration();
            if (configuration == null) {
                String versionCode = str.length > 0 ? str[0] : "wtf";
                String json = mIsOnline ? NetworkUtils.getJSONConfig(versionCode) : null;

            /* ****************For test***************
            try (InputStream in = getResources().openRawResource(R.raw.config)) {
                json = NetworkUtils.getStringFromInputStream(
                        in
                        , "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "for-test: " + json);
             ****************For test****************/

                boolean success = false;
                if (json != null) {
                    try {
                        // 测试是否能被JSON解析
                        new JSONObject(json);
                        success = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "配置文件读取成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException ignored) {
                    }
                }

                if (success) {
                    // 保存通过解析的JSON
                    SharedPreferencesUtils.putJSON(getApplicationContext(), json);
                } else {
                    // 读取上一次保存的JSON
                    json = SharedPreferencesUtils.getJSON(getApplicationContext());
                    if (json == null) {
                        // 读取默认的JSON
                        json = "{}";
                    }
                }
                configuration = new JSONConfiguration(MainActivity.this.getApplicationContext(), json);
                GlobalConfiguration.setConfiguration(configuration);
                GlobalConfiguration.getConfiguration().initAsync();
            }
            long end = System.currentTimeMillis();
            long sleep = Math.max(0, configuration.getStartSleepTime() - (end - start));
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            App.showToast("人脸识别初始化完成");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServerManager.unRegister();
    }

    public class MainClickHandlers {
        public void onClickName(View v) {
            Log.d("MainActivity", "MainClickHandlers");
            switch (v.getId()) {
                case R.id.main_sign:
                    new InitTask(FaceDetectActivity.class).execute();
                    break;
                case R.id.main_member:
                case R.id.main_plan:
                case R.id.main_attendance:
                case R.id.main_vacate:
                case R.id.main_setting:
                case R.id.btn_start:
                case R.id.btn_stop:
                    alertEdit(v.getId());
                    break;
            }
        }
    }

    /**
     * 初始化引擎
     */
    private void initialTts() {
        App.app.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        App.app.mSpeechSynthesizer.setContext(this);
        App.app.mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {
                Log.d("MainActivity", "initialTts onSynthesizeStart s=" + s);
            }

            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
                Log.d("MainActivity", "initialTts onSynthesizeDataArrived s=" + s + "，bytes=" + bytes + ",i=" + i);
            }

            @Override
            public void onSynthesizeFinish(String s) {
                Log.d("MainActivity", "initialTts onSynthesizeFinish s=" + s);
            }

            @Override
            public void onSpeechStart(String s) {
                Log.d("MainActivity", "initialTts onSynthesizeFinish s=" + s);
            }

            @Override
            public void onSpeechProgressChanged(String s, int i) {
                Log.d("MainActivity", "initialTts onSynthesizeFinish s=" + s + ",i=" + i);
            }

            @Override
            public void onSpeechFinish(String s) {
                Log.d("MainActivity", "initialTts onSynthesizeFinish s=" + s);
            }

            @Override
            public void onError(String s, SpeechError speechError) {
                Log.d("MainActivity", "initialTts onError s=" + s + ",speechError=" + speechError.description);
            }
        });
        App.app.mSpeechSynthesizer.setAppId(App.app.appId/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
        App.app.mSpeechSynthesizer.setApiKey(App.app.appKey,
                App.app.secretKey/*这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey*/);
        App.app.mSpeechSynthesizer.auth(TtsMode.ONLINE); // 离在线混合
        App.app.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0"); // 设置发声的人声音，在线生效
        App.app.mSpeechSynthesizer.initTts(TtsMode.ONLINE); // 初始化离在线混合模式，如果只需要在线合成功能，使用 TtsMode.ONLINE
    }

    private void alertEdit(final int type) {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et.setText("admin");
        new AlertDialog.Builder(this).setTitle("请输入密码")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String psd = et.getText().toString();
                        if ("admin".equals(psd)) {
                            switch (type) {
                                case R.id.main_member:
                                    new InitTask(MemberListActivity.class).execute();
                                    break;
                                case R.id.main_plan:
                                    startActivity(new Intent(MainActivity.this, PlanListActivity.class));
                                    break;
                                case R.id.main_attendance:
                                    startActivity(new Intent(MainActivity.this, SignListActivity.class));
                                    break;
                                case R.id.main_vacate:
                                    startActivity(new Intent(MainActivity.this, VacateListActivity.class));
                                    break;
                                case R.id.main_setting:
                                    startActivity(new Intent(MainActivity.this, MemberActivity.class)
                                            .putExtra("Owner", true));
                                    break;
                                case R.id.btn_start:
                                    showDialog();
                                    mServerManager.startService();
                                    break;
                                case R.id.btn_stop:
                                    showDialog();
                                    mServerManager.stopService();
                                    break;
                            }
                        } else {
                            App.showToast("请输入正确密码");
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }

    /**
     * Start notify.
     */
    public void serverStart(String ip) {
        closeDialog();
        binding.btnStart.setVisibility(View.GONE);
        binding.btnStop.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(ip)) {
            List<String> addressList = new LinkedList<>();
            mRootUrl = "http://" + ip + ":8080/";
            addressList.add(mRootUrl);
            addressList.add("http://" + ip + ":8080/login.html");
//            addressList.add("http://" + ip + ":8080/image");
//            addressList.add("http://" + ip + ":8080/download");
//            addressList.add("http://" + ip + ":8080/upload");
            binding.tvMessage.setText(TextUtils.join("\n", addressList));
        } else {
            mRootUrl = null;
            binding.tvMessage.setText(R.string.server_ip_error);
        }
    }

    /**
     * Error notify.
     */
    public void serverError(String message) {
        closeDialog();
        mRootUrl = null;
        binding.btnStart.setVisibility(View.VISIBLE);
        binding.btnStop.setVisibility(View.GONE);
        binding.tvMessage.setText(message);
    }

    /**
     * Stop notify.
     */
    public void serverStop() {
        closeDialog();
        mRootUrl = null;
        binding.btnStart.setVisibility(View.VISIBLE);
        binding.btnStop.setVisibility(View.GONE);
        binding.tvMessage.setText(R.string.server_stop_succeed);
    }

    private void showDialog() {
//        if (mDialog == null)
//            mDialog = new LoadingDialog(this);
//        if (!mDialog.isShowing())
//            mDialog.show();
    }

    private void closeDialog() {
//        if (mDialog != null && mDialog.isShowing())
//            mDialog.dismiss();
    }
}
