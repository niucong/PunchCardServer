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
import com.niucong.punchcardserver.adapter.VacateRecordAdapter;
import com.niucong.punchcardserver.db.VacateRecordDB;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VacateListActivity extends AppCompatActivity implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.vacate_search)
    EditText vacateSearch;
    @BindView(R.id.vacate_rv)
    RecyclerView vacateRv;
    @BindView(R.id.vacate_srl)
    SwipeRefreshLayout vacateSrl;

    private VacateRecordAdapter adapter;
    private List<VacateRecordDB> list = new ArrayList<>();

    private int allSize;
    private int offset = 0;
    private int pageSize = 10;
    String searchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacate_list);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        vacateSearch.addTextChangedListener(new TextWatcher() {
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
        vacateSrl.setOnRefreshListener(this);
        vacateSrl.setColorSchemeColors(Color.rgb(47, 223, 189));
        adapter = new VacateRecordAdapter(R.layout.item_vacate, list);
        adapter.setOnLoadMoreListener(this, vacateRv);
        vacateRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        vacateRv.setAdapter(adapter);
    }

    private void queryMembers() {
        if (TextUtils.isEmpty(searchKey)) {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.count(VacateRecordDB.class);
            }
            list.addAll(DataSupport.order("id desc").offset(offset).limit(pageSize).find(VacateRecordDB.class));
        } else {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.where("name = ?", searchKey).count(VacateRecordDB.class);
            }
            list.addAll(DataSupport.order("id desc").where("name = ?", searchKey).offset(offset).limit(pageSize).find(VacateRecordDB.class));
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
        vacateSrl.setRefreshing(false);
        //禁止下拉刷新
        vacateSrl.setEnabled(true);
    }

    @Override
    public void onRefresh() {
        adapter.setEnableLoadMore(false);
        offset = 0;
        queryMembers();
    }

    @Override
    public void onLoadMoreRequested() {
        vacateSrl.setEnabled(false);
        offset = list.size();
        queryMembers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_signrecord, menu);
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
