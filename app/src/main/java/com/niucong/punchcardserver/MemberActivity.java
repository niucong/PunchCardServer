package com.niucong.punchcardserver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.databinding.ActivityMemberBinding;
import com.niucong.punchcardserver.db.MemberDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MemberActivity extends AppCompatActivity {

    ActivityMemberBinding binding;
    private MemberDB db;
    private boolean isEdit;

    private List<MemberDB> dbs;
    private MemberDB selectDB;

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

        binding.memberType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.member_teacher:
                        binding.memberSpinner.setVisibility(View.GONE);
                        binding.memberNumberTip.setText("工号：");
                        break;
                    case R.id.member_student:
                        if (dbs == null) {
                            dbs = DataSupport.where("type = ?", "2").find(MemberDB.class);
                        }
                        int size = dbs.size();
                        if (size == 0) {
                            binding.memberTeacher.setChecked(true);
                            App.showToast("请先添加老师账号");
                        } else {
                            binding.memberSpinner.setVisibility(View.VISIBLE);
                            binding.memberNumberTip.setText("学号：");

                            String[] strs = new String[size + 1];
                            strs[0] = "请选择老师";
                            for (int i = 0; i < size; i++) {
                                strs[i + 1] = dbs.get(i).getName();
                            }
                            setSpinner(binding.memberSpinner, strs, 0);
                        }
                        break;
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

        if ("3".equals(db.getType())) {
            binding.memberSpinner.setVisibility(View.VISIBLE);
            binding.memberNumberTip.setText("学号：");

            dbs = DataSupport.where("type = ?", "2").find(MemberDB.class);
            int size = dbs.size();
            String[] strs = new String[size + 1];
            strs[0] = "请选择老师";
            int select = 0;
            for (int i = 0; i < size; i++) {
                strs[i + 1] = dbs.get(i).getName();
                if (db.getMemberId() == dbs.get(i).getMemberId()) {
                    select = i + 1;
                }
            }
            setSpinner(binding.memberSpinner, strs, 0);
            binding.memberSpinner.setSelection(select);
        }

        if (!isEdit) {
            binding.memberTeacher.setEnabled(false);
            binding.memberStudent.setEnabled(false);
            binding.memberName.setEnabled(false);
            binding.memberPhone.setEnabled(false);
            binding.memberPassword.setEnabled(false);
            binding.memberStatus.setEnabled(false);
            binding.memberSpinner.setEnabled(false);
        }
    }

    /**
     * 保存人员信息
     */
    private void saveMember() {
        int type = binding.memberStudent.isChecked() ? 3 : 2;
        if (type == 3 && selectDB == null) {
            App.showToast("请先选择老师");
            return;
        }
        String name = binding.memberName.getText().toString();
        if (TextUtils.isEmpty(name.trim())) {
            App.showToast("名字不能为空");
            return;
        }
        String number = binding.memberNumber.getText().toString();
        if (TextUtils.isEmpty(number.trim())) {
            if (type == 2) {
                App.showToast("工号不能为空");
            } else {
                App.showToast("学号不能为空");
            }
            return;
        }
        String phone = binding.memberPhone.getText().toString();
        if (TextUtils.isEmpty(phone.trim()) || phone.length() < 11 || !phone.startsWith("1")) {
            App.showToast("手机号码错误");
            return;
        }
        String password = binding.memberPassword.getText().toString();
        if (TextUtils.isEmpty(password.trim())) {
            App.showToast("密码不能为空");
            return;
        }
        boolean isDelete = !binding.memberStatus.isChecked();

        if (db == null) {
            db = new MemberDB();
        }
        db.setType(type);
        if (type == 3) {
            db.setMemberId(selectDB.getId());
        }
        db.setName(name);
        db.setNumber(number);
        db.setPhone(phone);
        db.setPassword(password);
        db.setMAC("");
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

    /**
     * @param sp
     * @param strs
     * @param type 0：单位、1：分类
     */
    private void setSpinner(Spinner sp, String[] strs, int type) {
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strs);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        sp.setAdapter(adapter);
        //添加事件Spinner事件监听
        sp.setOnItemSelectedListener(new SpinnerSelectedListener(type));
        //设置默认值
        sp.setVisibility(View.VISIBLE);
    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        int type;

        public SpinnerSelectedListener(int type) {
            this.type = type;
        }

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            if (type == 0) {
                if (arg2 == 0) {
                    selectDB = null;
                } else {
                    selectDB = dbs.get(arg2 - 1);
                }
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

}
