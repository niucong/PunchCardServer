package com.niucong.punchcardserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.MemberDB;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MemberListActivity extends AppCompatActivity {

    private List<MemberDB> list = new ArrayList<>();

    private int allSize;
    private int offset = 0;
    private int pageSize = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }

    private void queryMembers(){
        if (offset == 0) {
            list.clear();
            allSize = DataSupport.count(MemberDB.class);
        }
        list = DataSupport.offset(offset).limit(pageSize).find(MemberDB.class);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_add:
                startActivityForResult(new Intent(MemberListActivity.this, MemberActivity.class), 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            if (requestCode == 0) {
                App.showToast("保存成功");
            }
        }
    }
}
