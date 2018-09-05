package com.niucong.punchcardserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.niucong.punchcardserver.app.App;
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
                    App.showToast("此功能暂未开放");
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

    private void alertEdit(final int type) {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
                                    startActivity(new Intent(MainActivity.this, MemberListActivity.class));
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
