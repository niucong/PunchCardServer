package com.niucong.punchcardserver;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.niucong.punchcardserver.databinding.ActivityMainBinding;
import com.niucong.punchcardserver.service.ServerManager;

import java.util.LinkedList;
import java.util.List;

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
//                    BlueToothUtils utils = new BlueToothUtils();
//                    utils.setContext(MainActivity.this);
//                    utils.getAc().run();

//                    new ServerListener().start();

                    break;
                case R.id.main_member:
                    startActivity(new Intent(MainActivity.this, MemberListActivity.class));
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
        }
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
