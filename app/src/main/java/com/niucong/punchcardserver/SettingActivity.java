package com.niucong.punchcardserver;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;

    private String welTip, signTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        binding.setHandlers(new SettingClickHandlers());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        welTip = App.sp.getString("welTip", "欢迎参观！");
//        signTip = App.sp.getString("signTip","打卡成功，您已成功打卡");

        binding.settingWel.setText(welTip);
        binding.settingWel.setSelection(welTip.length());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public class SettingClickHandlers {
        public void onClickName(View v) {
            switch (v.getId()) {
                case R.id.setting_save:
                    String newTip = binding.settingWel.getText().toString();
                    if (TextUtils.isEmpty(newTip)) {
                        App.showToast("提示语不能为空");
                    } else {
                        App.sp.putString("welTip", newTip);
                        App.showToast("保存成功");
                        finish();
                    }
                    break;
            }
        }
    }

}
