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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.niucong.punchcardserver.adapter.PlanAdapter;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.PlanDB;
import com.niucong.punchcardserver.util.FileUtils;
import com.niucong.selectdatetime.view.NiftyDialogBuilder;
import com.niucong.selectdatetime.view.wheel.DateSelectView;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.litepal.crud.DataSupport;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlanListActivity extends AppCompatActivity implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.plan_search)
    EditText planSearch;
    @BindView(R.id.plan_rv)
    RecyclerView planRv;
    @BindView(R.id.plan_srl)
    SwipeRefreshLayout planSrl;

    private PlanAdapter adapter;
    private List<PlanDB> list = new ArrayList<>();

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
                startDate = null;
                endDate = null;
                queryPlans();
            }
        });

        setAdapter();
        queryPlans();
    }

    private void setAdapter() {
        planSrl.setOnRefreshListener(this);
        planSrl.setColorSchemeColors(Color.rgb(47, 223, 189));
        adapter = new PlanAdapter(this, R.layout.item_plan, list);
        adapter.setOnLoadMoreListener(this, planRv);
        planRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        planRv.setAdapter(adapter);
    }

    private void queryPlans() {

        long startTime = 0;
        long endTime = 0;
        if (startDate != null && endDate != null) {
            startTime = startDate.getTime();
            endTime = endDate.getTime() + 24 * 60 * 60 * 1000;
        }

        if (TextUtils.isEmpty(searchKey)) {
            if (offset == 0) {
                list.clear();
                if (startTime != 0 && endTime != 0) {
                    allSize = DataSupport.where("(startTime <= ? and endTime >= ?) " +
                                    "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?)",
                            "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(PlanDB.class);
                } else {
                    allSize = DataSupport.count(PlanDB.class);
                }
            }
            if (startTime != 0 && endTime != 0) {
                list.addAll(DataSupport.order("id desc").where("(startTime <= ? and endTime >= ?) " +
                                "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?)",
                        "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(PlanDB.class));
            } else {
                list.addAll(DataSupport.order("id desc").offset(offset).limit(pageSize).find(PlanDB.class));
            }
        } else {
            if (offset == 0) {
                list.clear();
                if (startTime != 0 && endTime != 0) {
                    allSize = DataSupport.where("name like ? and ((startTime <= ? and endTime >= ?) "
                                    + "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            "%" + searchKey + "%", "" + startTime, "" + startTime, "" + endTime, "" + endTime,
                            "" + startTime, "" + endTime).count(PlanDB.class);
                } else {
                    allSize = DataSupport.where("name like ?", "%" + searchKey + "%").count(PlanDB.class);
                }
            }
            if (startTime != 0 && endTime != 0) {
                list.addAll(DataSupport.order("id desc").where("name like ? and ((startTime <= ? and endTime >= ?) "
                                + "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        "%" + searchKey + "%", "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(PlanDB.class));
            } else {
                list.addAll(DataSupport.order("id desc").where("name like ?", "%" + searchKey + "%").offset(offset)
                        .limit(pageSize).find(PlanDB.class));
            }
        }
        Log.d("PlanListActivity", "queryPlans " + list.size() + "/" + allSize);
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
        startDate = null;
        endDate = null;
        queryPlans();
    }

    @Override
    public void onLoadMoreRequested() {
        planSrl.setEnabled(false);
        offset = list.size();
        queryPlans();
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
                showSubmitDia();
                break;
            case R.id.action_export:
                SimpleDateFormat YMDHM = new SimpleDateFormat("yyyyMMddHHmm");
                final String path = FileUtils.getSdcardDir() + "/计划表_" + YMDHM.format(new Date()) + ".xls";
//                showProgressDialog("正在导出人员……");
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            saveExcel(path);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    dismissProgressDialog("已导出到" + path);
                                    App.showToast("已导出到" + path);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    dismissProgressDialog("导出失败");
                                    App.showToast("导出失败");
                                }
                            });
                        }
                        super.run();
                    }
                }.start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 导出人员表
     */
    private void saveExcel(String path) throws Exception {
//1、输出的文件地址及名称
        OutputStream out = new FileOutputStream(path);
//2、sheet表中的标题行内容，需要输入excel的汇总数据
        String[] summary = {"名称", "创建者", "关联人员", "开始时间", "结束时间", "状态"};
        List<List<String>> summaryData = new ArrayList<List<String>>();
        List<PlanDB> dbs = DataSupport.findAll(PlanDB.class);
        SimpleDateFormat YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (PlanDB db : dbs) {
            List<String> rowData = new ArrayList<String>();
            rowData.add(db.getName());
            rowData.add(db.getCreatorName());
            String names = "";
            try {
                List<MemberDB> members = JSON.parseArray(db.getMembers(), MemberDB.class);
                for (MemberDB member : members) {
                    names += "，" + member.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            rowData.add(names);
            rowData.add(YMDHMS.format(new Date(db.getStartTime())));
            rowData.add(YMDHMS.format(new Date(db.getEndTime())));

            if (db.getForceFinish() == 0) {
                if (db.getStartTime() > System.currentTimeMillis()) {
                    rowData.add("未开始");
                } else if (db.getEndTime() > System.currentTimeMillis()) {
                    rowData.add("进行中");
                } else {
                    rowData.add("已结束");
                }
            } else if (db.getForceFinish() == 1) {
                rowData.add("已取消");
            } else {
                rowData.add("已终止");
            }

            summaryData.add(rowData);
        }
//3、生成格式是xlsx可存储103万行数据，如果是xls则只能存不到6万行数据
        HSSFWorkbook workbook = new HSSFWorkbook();
//第一个表格内容
        FileUtils.exportExcel(workbook, 0, "考勤表", summary, summaryData);
//第二个表格内容
//            exportExcel(workbook, 1, "部分流水数据", water, waterData);
//将所有的数据一起写入，然后再关闭输入流。
        workbook.write(out);
        out.close();
    }

    private Date startDate, endDate;

    /**
     * 选择日期对话框
     */
    private void showSubmitDia() {
        final NiftyDialogBuilder submitDia = NiftyDialogBuilder.getInstance(this);
        View selectDateView = LayoutInflater.from(this).inflate(R.layout.dialog_select_date, null);
        final DateSelectView ds = (DateSelectView) selectDateView.findViewById(R.id.date_start);
        final DateSelectView de = (DateSelectView) selectDateView.findViewById(R.id.date_end);

        final SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar c = Calendar.getInstance();
        try {
            startDate = YMD.parse(YMD.format(new Date()));// 当日00：00：00
        } catch (ParseException e) {
            e.printStackTrace();
        }
        endDate = new Date();

        submitDia.withTitle("选择查询日期");
        submitDia.withButton1Text("取消", 0).setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDia.dismiss();
            }
        });
        submitDia.withButton2Text("确定", 0).setButton2Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startDate = YMD.parse(ds.getDate());
//                    if (ymd.format(new Date()).equals(de.getDate())) {// 结束日期是今天
//                        endDate = new Date();// 当前时间
//                    } else {
//                        endDate = new Date(ymd.parse(de.getDate()).getTime() + 1000 * 60 * 60 * 24 - 1);// 当日23：59：59
//                    }
                    endDate = YMD.parse(de.getDate());
                    if (endDate.before(startDate)) {
                        App.showToast("开始日期不能大于结束日期");
                    } else {
                        queryPlans();
                        submitDia.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        submitDia.setCustomView(selectDateView, this);// "请选择查询日期"
        submitDia.withMessage(null).withDuration(400);
        submitDia.isCancelable(false);
        submitDia.show();
    }

}
