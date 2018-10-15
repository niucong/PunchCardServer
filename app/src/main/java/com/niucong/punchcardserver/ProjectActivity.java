package com.niucong.punchcardserver;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.niucong.punchcardserver.databinding.ActivityProjectBinding;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.ProjectDB;
import com.niucong.punchcardserver.util.ConstantUtil;

import java.util.Date;
import java.util.List;

public class ProjectActivity extends AppCompatActivity {

    ActivityProjectBinding binding;

    private ProjectDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_project);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = getIntent().getParcelableExtra("ProjectDB");
        if (db == null) {
            finish();
        } else {

            binding.projectName.setText(db.getName());
            binding.projectCreator.setVisibility(View.VISIBLE);
            binding.projectCreator.setText("创建者：" + db.getCreatorName());
            binding.projectCreate.setVisibility(View.VISIBLE);
            binding.projectCreate.setText("创建时间：" + ConstantUtil.YMDHM.format(new Date(db.getCreateTime())));
            if (!TextUtils.isEmpty(db.getRemark())) {
                binding.projectRemarkTv.setText("备注：" + db.getRemark());
            }

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
                binding.projectMembers.setText(names);
                binding.projectMembersLl.setVisibility(View.VISIBLE);
            } else {
                binding.projectMembersLl.setVisibility(View.GONE);
            }

            binding.projectStart.setText(ConstantUtil.YMDHM.format(new Date(db.getStartTime())));
            binding.projectEnd.setText(ConstantUtil.YMDHM.format(new Date(db.getEndTime())));

            if (db.getForceFinish() == 1) {
                binding.projectFinish.setVisibility(View.VISIBLE);
                binding.projectFinish.setText("取消时间：" + ConstantUtil.YMDHM.format(new Date(db.getCloseTime())));
            } else if (db.getForceFinish() == 2) {
                binding.projectFinish.setVisibility(View.VISIBLE);
                binding.projectFinish.setText("终止时间：" + ConstantUtil.YMDHM.format(new Date(db.getCloseTime())));
            }

            binding.projectStatusLl.setVisibility(View.VISIBLE);
            if (db.getApproveResult() == 0) {
                if (db.getForceFinish() == 1) {
                    setTextStutas("已取消", Color.argb(168, 255, 0, 0));
                } else {
                    setTextStutas("待审批", Color.argb(168, 0, 0, 255));
                }
            } else if (db.getApproveResult() == 2) {
                setTextStutas("被拒绝", Color.argb(168, 255, 0, 255));
            } else if (db.getForceFinish() == 0) {
                binding.projectApproveTime.setVisibility(View.VISIBLE);
                binding.projectApproveTime.setText("审批时间：" + ConstantUtil.YMDHM.format(new Date(db.getApproveTime())));
                if (db.getStartTimeReal() > 0) {
                    binding.projectDesignTime.setVisibility(View.VISIBLE);
                    binding.projectDesignTime.setText("实际开始时间：" + ConstantUtil.YMDHM.format(new Date(db.getStartTimeReal())));
                }
                if (db.getStartTimeDevelop() > 0) {
                    binding.projectDevelopTime.setVisibility(View.VISIBLE);
                    binding.projectDevelopTime.setText("开始研发时间：" + ConstantUtil.YMDHM.format(new Date(db.getStartTimeDevelop())));
                }
                if (db.getStartTimeTest() > 0) {
                    binding.projectTestTime.setVisibility(View.VISIBLE);
                    binding.projectTestTime.setText("开始测试时间：" + ConstantUtil.YMDHM.format(new Date(db.getStartTimeTest())));
                }
                if (db.getEndTimeReal() > 0) {
                    binding.projectEndReal.setVisibility(View.VISIBLE);
                    binding.projectEndReal.setText("实际完成时间：" + ConstantUtil.YMDHM.format(new Date(db.getEndTimeReal())));
                }
                int status = db.getStatus();
                if (status == 0) {
                    setTextStutas("未开始", Color.argb(168, 0, 0, 255));
                } else if (status == 1) {
                    setTextStutas("设计中", Color.argb(168, 0, 255, 0));
                } else if (status == 2) {
                    setTextStutas("开发中", Color.argb(168, 0, 255, 0));
                } else if (status == 3) {
                    setTextStutas("测试中", Color.argb(168, 0, 255, 0));
                } else {
                    setTextStutas("已完成", Color.argb(168, 0, 0, 0));
                    return;
                }
            } else if (db.getForceFinish() == 1) {
                setTextStutas("已取消", Color.argb(168, 255, 0, 0));
            } else {
                setTextStutas("已终止", Color.argb(168, 255, 0, 0));
            }
        }
    }

    private void setTextStutas(String msg, int color) {
        binding.projectStatus.setText(msg);
        binding.projectStatus.setTextColor(color);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
