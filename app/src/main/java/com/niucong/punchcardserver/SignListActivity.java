package com.niucong.punchcardserver;

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
import com.niucong.punchcardserver.adapter.SignAdapter;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.SignDB;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignListActivity extends AppCompatActivity implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.sign_search)
    EditText signSearch;
    @BindView(R.id.sign_rv)
    RecyclerView signRv;
    @BindView(R.id.sign_srl)
    SwipeRefreshLayout signSrl;

    private SignAdapter adapter;
    private List<SignDB> list = new ArrayList<>();

    private int allSize;
    private int offset = 0;
    private int pageSize = 10;
    String searchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_list);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        signSearch.addTextChangedListener(new TextWatcher() {
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
        signSrl.setOnRefreshListener(this);
        signSrl.setColorSchemeColors(Color.rgb(47, 223, 189));
        adapter = new SignAdapter(R.layout.item_sign, list);
        adapter.setOnLoadMoreListener(this, signRv);
        signRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        signRv.setAdapter(adapter);
    }

    private void queryMembers() {
        if (TextUtils.isEmpty(searchKey)) {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.count(SignDB.class);
            }
            list.addAll(DataSupport.offset(offset).limit(pageSize).find(SignDB.class));
        } else {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.where("name = ?", searchKey).count(MemberDB.class);
            }
            list.addAll(DataSupport.where("name = ?", searchKey).offset(offset).limit(pageSize).find(SignDB.class));
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
        signSrl.setRefreshing(false);
        //禁止下拉刷新
        signSrl.setEnabled(true);
    }

    @Override
    public void onRefresh() {
        adapter.setEnableLoadMore(false);
        offset = 0;
        queryMembers();
    }

    @Override
    public void onLoadMoreRequested() {
        signSrl.setEnabled(false);
        offset = list.size();
        queryMembers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_sign, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_date:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
