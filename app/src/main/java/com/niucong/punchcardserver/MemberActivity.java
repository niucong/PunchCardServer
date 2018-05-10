package com.niucong.punchcardserver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.niucong.punchcardserver.databinding.ActivityMemberBinding;
import com.niucong.punchcardserver.db.MemberDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MemberActivity extends AppCompatActivity {

    ActivityMemberBinding binding;
    private MemberDB db;
    private boolean isEdit;

    private List<MemberDB> dbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_member);
//        setContentView(R.layout.activity_member);
//        binding = ActivityMemberBinding.inflate(getLayoutInflater());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = getIntent().getParcelableExtra("MemberDB");
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (db == null) {
            actionBar.setTitle("新增人员");
        } else {
            binding.memberNumber.setEnabled(false);
            if (isEdit) {
                actionBar.setTitle("人员编辑");
            } else {
                actionBar.setTitle("人员详情");
                binding.memberButton.setVisibility(View.GONE);
            }
            setData();
        }

//        binding.
        binding.memberStudent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("MemberActivity", "isChecked=" + isChecked);
                if (isChecked) {
                    if (dbs == null) {
                        dbs = DataSupport.where("type = ?", "2").find(MemberDB.class);
                    }
                    if (dbs.size() == 0) {
//                        binding.memberStudent.setChecked(false);
                        binding.memberTeacher.setChecked(true);
//                        buttonView.setChecked(false);
                        Toast.makeText(MemberActivity.this, "请先添加老师账号", Toast.LENGTH_LONG).show();
                    } else {
                        binding.memberSpinner.setVisibility(View.VISIBLE);
                        binding.memberNumberTip.setText("学号：");
                    }
                } else {
                    binding.memberSpinner.setVisibility(View.GONE);
                    binding.memberNumberTip.setText("工号：");
                }
            }
        });

        binding.memberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MemberActivity", "保存");
                saveMember();
            }
        });
    }

    /**
     * 显示人员信息
     */
    private void setData() {
        binding.memberStudent.setChecked("3".equals(db.getType()));
        binding.memberName.setText(db.getName());
        binding.memberNumber.setText(db.getNumber());
        binding.memberPhone.setText(db.getPhone());
        binding.memberPassword.setText(db.getPassword());
        binding.memberStatus.setChecked(!db.isDelete());

        if (!isEdit) {
            binding.memberTeacher.setEnabled(false);
            binding.memberStudent.setEnabled(false);
            binding.memberName.setEnabled(false);
            binding.memberPhone.setEnabled(false);
            binding.memberPassword.setEnabled(false);
            binding.memberStatus.setEnabled(false);
        }
    }

    /**
     * 保存人员信息
     */
    private void saveMember() {
        int type = binding.memberStudent.isChecked() ? 3 : 2;
        String name = binding.memberName.getText().toString();
        if (TextUtils.isEmpty(name.trim())) {
            Toast.makeText(this, "名字不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        String number = binding.memberNumber.getText().toString();
        if (TextUtils.isEmpty(number.trim())) {
            if (type == 2) {
                Toast.makeText(this, "工号不能为空", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "学号不能为空", Toast.LENGTH_LONG).show();
            }
            return;
        }
        String phone = binding.memberPhone.getText().toString();
        if (TextUtils.isEmpty(phone.trim()) || phone.length() < 11 || !phone.startsWith("1")) {
            Toast.makeText(this, "手机号码错误", Toast.LENGTH_LONG).show();
            return;
        }
        String password = binding.memberPassword.getText().toString();
        if (TextUtils.isEmpty(password.trim())) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        boolean isDelete = !binding.memberStatus.isChecked();

        if (db == null) {
            db = new MemberDB();
        }
        db.setType(type);
        db.setName(name);
        db.setNumber(number);
        db.setPhone(phone);
        db.setPassword(password);
        db.setDelete(isDelete);
        if (isEdit) {
            db.update(db.getId());
        } else {
            db.save();
        }
        finish();
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
