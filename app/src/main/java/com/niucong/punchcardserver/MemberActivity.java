package com.niucong.punchcardserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.databinding.ActivityMemberBinding;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.yunshitu.AddUserActivity;
import com.niucong.yunshitu.face.FaceReg;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;

import cn.yunshitu.facesdk.face.Person;

public class MemberActivity extends AppCompatActivity {

    ActivityMemberBinding binding;
    private MemberDB db;
    private boolean isEdit;

    private List<MemberDB> dbs;
    private MemberDB selectDB;

    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_member);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        isOwner = getIntent().getBooleanExtra("Owner", false);

        if (isOwner) {
            binding.llMemberType.setVisibility(View.GONE);
            db = DataSupport.where("type = ? and isDelete = ?", "1", "0").findFirst(MemberDB.class);
            if (db != null) {
                isEdit = true;
            } else {
                binding.memberOwnerTip.setVisibility(View.VISIBLE);
            }
        } else {
            if (DataSupport.where("type = ? and isDelete = ?", "1", "0").count(MemberDB.class) == 0) {
                isOwner = true;
                binding.llMemberType.setVisibility(View.GONE);
                binding.memberOwnerTip.setVisibility(View.VISIBLE);
            } else {
                binding.memberOwnerTip.setVisibility(View.GONE);
                binding.memberType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.member_teacher:
                                binding.memberSpinner.setVisibility(View.GONE);
                                binding.memberOwner.setVisibility(View.GONE);
                                binding.memberNumberTip.setText("工号：");
                                break;
                            case R.id.member_student:
                                if (dbs == null) {
                                    dbs = DataSupport.where("type = ? and isDelete = ?", "2", "0").find(MemberDB.class);
                                }
                                int size = dbs.size();
                                if (size == 0) {
                                    binding.memberTeacher.setChecked(true);
                                    App.showToast("请先添加老师账号");
                                } else {
                                    binding.memberSpinner.setVisibility(View.VISIBLE);
                                    binding.memberOwner.setVisibility(View.VISIBLE);
                                    binding.memberNumberTip.setText("学号：");

                                    String[] strs = new String[size + 1];
                                    strs[0] = "请选择";
                                    for (int i = 0; i < size; i++) {
                                        strs[i + 1] = dbs.get(i).getName();
                                    }
                                    setSpinner(binding.memberSpinner, strs, 0);
                                }
                                break;
                        }
                    }
                });
                db = getIntent().getParcelableExtra("MemberDB");
                isEdit = getIntent().getBooleanExtra("isEdit", false);
            }
        }

        if (db == null) {
            actionBar.setTitle("新增人员");
        } else {
            if (db.getType() == 1) {
                isOwner = true;
            }
            binding.memberNumber.setEnabled(false);
            if (isEdit) {
                actionBar.setTitle("人员编辑");
            } else {
                actionBar.setTitle("人员详情");
                binding.memberButton.setVisibility(View.GONE);
            }
            setData();
        }

        binding.memberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMember();
            }
        });

        binding.memberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.memberPhone.getText().toString();
                if (TextUtils.isEmpty(phone.trim()) || phone.length() < 11 || !phone.startsWith("1")) {
                    App.showToast("手机号码错误");
                    return;
                } else if (db == null && DataSupport.where("phone = ?", phone).count(MemberDB.class) > 0) {
                    App.showToast("手机号码已被使用");
                    return;
                }
                if (hasHeader) {
                    new AlertDialog.Builder(MemberActivity.this)
                            .setTitle("确定要删除头像吗?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    hasHeader = false;
                                    FaceReg.INSTANCE.del_face(phone);
                                    binding.memberIcon.setImageResource(R.mipmap.ic_launcher);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                } else {
                    startActivityForResult(new Intent(MemberActivity.this, AddUserActivity.class).putExtra("phone", phone), 0);
                }
            }
        });
        List<Person> person_list = FaceReg.INSTANCE.get_all_person(0, 10000);
        for (Person person : person_list) {
            if (person.getName().equals(binding.memberPhone.getText().toString())) {
                hasHeader = true;
                Glide.with(this).load(Uri.fromFile(new File(person.getPortrait()))).into(binding.memberIcon);
            }
        }
    }

    private boolean hasHeader;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            List<Person> person_list = FaceReg.INSTANCE.get_all_person(0, 10000);
            for (Person person : person_list) {
                if (person.getName().equals(binding.memberPhone.getText().toString())) {
                    Glide.with(this).load(Uri.fromFile(new File(person.getPortrait()))).into(binding.memberIcon);
                }
            }
        }
    }

    /**
     * 显示人员信息
     */
    private void setData() {
        if (isOwner || db.getType() == 1) {
            binding.llMemberType.setVisibility(View.GONE);
        } else {
            binding.memberStudent.setChecked(db.getType() == 3);
            if (db.getType() == 3) {
                binding.memberSpinner.setVisibility(View.VISIBLE);
                binding.memberOwner.setVisibility(View.VISIBLE);
                binding.memberNumberTip.setText("学号：");

                dbs = DataSupport.where("type = ? and isDelete = ?", "2", "0").find(MemberDB.class);
                int size = dbs.size();
                String[] strs = new String[size + 1];
                strs[0] = "请选择老师";
                int select = 0;
                for (int i = 0; i < size; i++) {
                    strs[i + 1] = dbs.get(i).getName();
                    if (db.getSuperId() == dbs.get(i).getId()) {
                        select = i + 1;
                    }
                }
                setSpinner(binding.memberSpinner, strs, 0);
                binding.memberSpinner.setSelection(select);
            }
        }
        binding.memberName.setText(db.getName());
        binding.memberNumber.setText(db.getNumber());
        binding.memberPhone.setText(db.getPhone());
        binding.memberPassword.setText(db.getPassword());
        binding.memberStatus.setChecked(db.getIsDelete() == 0);

        if (!isEdit) {
            binding.memberName.setEnabled(false);
            binding.memberPhone.setEnabled(false);
            binding.memberPassword.setEnabled(false);
            binding.memberStatus.setEnabled(false);
            binding.memberSpinner.setEnabled(false);
            binding.memberTeacher.setEnabled(false);
            binding.memberStudent.setEnabled(false);
        }
    }

    /**
     * 保存人员信息
     */
    private void saveMember() {
        int type = binding.memberStudent.isChecked() ? 3 : 2;
        if (isOwner) {
            type = 1;
        }
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
            if (type == 1 || type == 2) {
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
        } else if (db == null && DataSupport.where("phone = ?", phone).count(MemberDB.class) > 0) {
            App.showToast("手机号码已被使用");
            return;
        }
        String password = binding.memberPassword.getText().toString();
        if (TextUtils.isEmpty(password.trim())) {
            App.showToast("密码不能为空");
            return;
        }

        if (!isEdit) {
            db = new MemberDB();
        }
        db.setType(type);
        if (type == 3) {
            db.setSuperId(selectDB.getId());
        } else if (type == 2) {
            db.setSuperId(DataSupport.where("type = ? and isDelete = ?", "1", "0").findFirst(MemberDB.class).getId());
        }
        db.setName(name);
        db.setNumber(number);
        db.setPhone(phone);
        db.setPassword(password);
        db.setIsDelete(binding.memberStatus.isChecked() ? 0 : 1);
        db.setLastEditTime(System.currentTimeMillis());
        if (isEdit) {
            db.update(db.getId());
            setResult(RESULT_OK, getIntent().putExtra("MemberDB", db));
        } else {
            db.save();
            setResult(RESULT_OK);
        }

        Log.d("MemberActivity", "SuperId=" + db.getSuperId());
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
