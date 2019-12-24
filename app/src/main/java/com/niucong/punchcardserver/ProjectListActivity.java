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
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.niucong.punchcardserver.adapter.ProjectAdapter;
import com.niucong.punchcardserver.db.ProjectDB;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProjectListActivity extends AppCompatActivity implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.plan_search)
    EditText planSearch;
    @BindView(R.id.plan_rv)
    RecyclerView planRv;
    @BindView(R.id.plan_srl)
    SwipeRefreshLayout planSrl;

    private ProjectAdapter adapter;
    private List<ProjectDB> list = new ArrayList<>();

    private int allSize;
    private int offset = 0;
    private int pageSize = 10;
    String searchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        planSearch.setHint("项目名称");
        planSearch.addTextChangedListener(new TextWatcher() {
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
                queryProjects();
            }
        });

        setAdapter();
        queryProjects();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void setAdapter() {
        planSrl.setOnRefreshListener(this);
        planSrl.setColorSchemeColors(Color.rgb(47, 223, 189));
        adapter = new ProjectAdapter(this, R.layout.item_project, list);
        adapter.setOnLoadMoreListener(this, planRv);
        planRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        planRv.setAdapter(adapter);
    }

    private void queryProjects() {

        if (TextUtils.isEmpty(searchKey)) {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.count(ProjectDB.class);
            }
            list.addAll(DataSupport.order("id desc").offset(offset).limit(pageSize).find(ProjectDB.class));
        } else {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.where("name like ?", "%" + searchKey + "%").count(ProjectDB.class);
            }
            list.addAll(DataSupport.order("id desc").where("name like ?", "%" + searchKey + "%").offset(offset)
                    .limit(pageSize).find(ProjectDB.class));
        }
//        setAdapter();
        adapter.notifyDataSetChanged();
        if (allSize == list.size()) {
            adapter.loadMoreEnd();
        } else {
            adapter.loadMoreComplete();
        }
        //取消下拉刷新动画
        planSrl.setRefreshing(false);
        //禁止下拉刷新
        planSrl.setEnabled(true);
    }

    @Override
    public void onRefresh() {
        adapter.setEnableLoadMore(false);
        offset = 0;
        queryProjects();
    }

    @Override
    public void onLoadMoreRequested() {
        planSrl.setEnabled(false);
        offset = list.size();
        queryProjects();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                onRefresh();
            }
        }
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
