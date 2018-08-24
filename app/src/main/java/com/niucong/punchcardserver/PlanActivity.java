package com.niucong.punchcardserver;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.niucong.punchcardserver.databinding.ActivityPlanBinding;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.PlanDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PlanActivity extends AppCompatActivity {

    ActivityPlanBinding binding;

    SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private PlanDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_plan);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = getIntent().getParcelableExtra("PlanDB");
        binding.planName.setText(db.getName());
        binding.planCreator.setVisibility(View.VISIBLE);
        binding.planCreator.setText("创建者：" + db.getCreatorName());
        binding.planCreate.setVisibility(View.VISIBLE);
        binding.planCreate.setText("创建时间：" + YMDHM.format(new Date(db.getCreateTime())));

        String names = "";
        Log.d("PlanAdapter", "members=" + db.getMembers());
        try {
            List<MemberDB> members = JSON.parseArray(db.getMembers(), MemberDB.class);
            for (MemberDB member : members) {
                names += "，" + member.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("PlanAdapter", "names=" + names);
        if (names.length() > 0) {
            names = names.substring(1);
            binding.planMembers.setText("关联人员：" + names);
        } else {
            binding.planMembersLl.setVisibility(View.GONE);
        }

        binding.planStart.setText(YMDHM.format(new Date(db.getStartTime())));
        binding.planEnd.setText(YMDHM.format(new Date(db.getEndTime())));

        if (db.getEditTime() > 0) {
            binding.planEdit.setVisibility(View.VISIBLE);
            binding.planEdit.setText("修改时间：" + YMDHM.format(new Date(db.getEditTime())));
        }

        binding.planStatusLl.setVisibility(View.VISIBLE);
        if (db.getForceFinish() == 0) {
            if (db.getStartTime() > System.currentTimeMillis()) {
                setTextStutas("未开始", Color.argb(168, 0, 0, 255));
            } else if (db.getEndTime() > System.currentTimeMillis()) {
                setTextStutas("进行中", Color.argb(168, 0, 255, 0));
            } else {
                setTextStutas("已结束", Color.argb(168, 0, 0, 0));
            }
        } else if (db.getForceFinish() == 1) {
            setTextStutas("已取消", Color.argb(168, 255, 0, 0));
            binding.planIsfinishCause.setText("取消原因：" + db.getCause());
        } else {
            setTextStutas("已终止", Color.argb(168, 255, 0, 0));
            binding.planIsfinishCause.setText("终止原因：" + db.getCause());
        }

        binding.planName.setEnabled(false);
        binding.planMembers.setEnabled(false);
        binding.planStart.setEnabled(false);
        binding.planEnd.setEnabled(false);

        binding.planName.setBackgroundColor(Color.alpha(0));
        binding.planName.setTextColor(Color.GRAY);
        binding.planMembers.setTextColor(Color.GRAY);
        binding.planStart.setTextColor(Color.GRAY);
        binding.planEnd.setTextColor(Color.GRAY);
    }

    private void setTextStutas(String msg, int color) {
        binding.planStatus.setText(msg);
        binding.planStatus.setTextColor(color);
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

}
