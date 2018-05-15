package com.niucong.punchcardserver;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.niucong.punchcardserver.adapter.MemberAdapter;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.MemberDB;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberListActivity extends AppCompatActivity implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.member_search)
    EditText memberSearch;
    @BindView(R.id.member_rv)
    RecyclerView memberRv;
    @BindView(R.id.member_srl)
    SwipeRefreshLayout memberSrl;

    private MemberAdapter adapter;
    private List<MemberDB> list = new ArrayList<>();

    private int allSize;
    private int offset = 0;
    private int pageSize = 10;
    String searchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        memberSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchKey = s.toString();
                offset = 0;
                queryMembers();
            }
        });

        setAdapter();
        queryMembers();
    }

    private void setAdapter() {
        memberSrl.setOnRefreshListener(this);
        memberSrl.setColorSchemeColors(Color.rgb(47, 223, 189));
        adapter = new MemberAdapter(this, R.layout.item_member, list);
        adapter.setOnLoadMoreListener(this, memberRv);
        memberRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        memberRv.setAdapter(adapter);
    }

    private void queryMembers() {
        if (TextUtils.isEmpty(searchKey)) {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.count(MemberDB.class);
            }
            list.addAll(DataSupport.offset(offset).limit(pageSize).find(MemberDB.class));
        } else {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.where("number = ? or name = ? or phone = ?", searchKey, searchKey, searchKey).count(MemberDB.class);
            }
            list.addAll(DataSupport.where("number = ? or name = ? or phone = ?", searchKey, searchKey, searchKey)
                    .offset(offset).limit(pageSize).find(MemberDB.class));
        }
        Log.d("MemberListActivity", "queryMembers " + list.size() + "/" + allSize);
//        setAdapter();
        adapter.notifyDataSetChanged();
        if (allSize == list.size()) {
            adapter.loadMoreEnd();
        } else {
            adapter.loadMoreComplete();
        }
        //取消下拉刷新动画
        memberSrl.setRefreshing(false);
        //禁止下拉刷新
        memberSrl.setEnabled(true);
    }

    @Override
    public void onRefresh() {
        adapter.setEnableLoadMore(false);
        offset = 0;
        queryMembers();
    }

    @Override
    public void onLoadMoreRequested() {
        memberSrl.setEnabled(false);
        offset = list.size();
        queryMembers();
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
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                App.showToast("保存成功");
                offset = 0;
                queryMembers();
            } else if (requestCode == 1) {
                App.showToast("修改成功");
                list.remove(data.getIntExtra("position", 0));
                list.add(data.getIntExtra("position", 0), (MemberDB) data.getParcelableExtra("MemberDB"));
                adapter.notifyDataSetChanged();
            }
        }
    }

}
