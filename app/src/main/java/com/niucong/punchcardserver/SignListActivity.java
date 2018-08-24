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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.niucong.punchcardserver.adapter.SignAdapter;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.SignDB;
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
                startDate = null;
                endDate = null;
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
                            "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(SignDB.class);
                } else {
                    allSize = DataSupport.count(SignDB.class);
                }
            }
            if (startTime != 0 && endTime != 0) {
                list.addAll(DataSupport.order("id desc").where("(startTime <= ? and endTime >= ?) " +
                                "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?)",
                        "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(SignDB.class));
            } else {
                list.addAll(DataSupport.order("id desc").offset(offset).limit(pageSize).find(SignDB.class));
            }
        } else {
            if (offset == 0) {
                list.clear();
                if (startTime != 0 && endTime != 0) {
                    allSize = DataSupport.where("name = ? and ((startTime <= ? and endTime >= ?) "
                                    + "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime,
                            "" + startTime, "" + endTime).count(MemberDB.class);
                } else {
                    allSize = DataSupport.where("name = ?", searchKey).count(MemberDB.class);
                }
            }
            if (startTime != 0 && endTime != 0) {
                list.addAll(DataSupport.order("id desc").where("name = ? and ((startTime <= ? and endTime >= ?) "
                                + "or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(SignDB.class));
            } else {
                list.addAll(DataSupport.order("id desc").where("name = ?", searchKey).offset(offset)
                        .limit(pageSize).find(SignDB.class));
            }
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
        startDate = null;
        endDate = null;
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
                showSubmitDia();
                break;
            case R.id.action_export:
                SimpleDateFormat YMDHM = new SimpleDateFormat("yyyyMMddHHmm");
                final String path = FileUtils.getSdcardDir() + "/考勤表_" + YMDHM.format(new Date()) + ".xls";
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
        String[] summary = {"姓名", "到达时间", "离开时间"};
        List<List<String>> summaryData = new ArrayList<List<String>>();
        List<SignDB> dbs = DataSupport.findAll(SignDB.class);
        SimpleDateFormat YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SignDB db : dbs) {
            List<String> rowData = new ArrayList<String>();
            rowData.add(db.getName());
            rowData.add(YMDHMS.format(new Date(db.getStartTime())));
            if (db.getEndTime() > 0) {
                rowData.add(YMDHMS.format(new Date(db.getEndTime())));
            } else {
                rowData.add("无");
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

        final SimpleDateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final Calendar c = Calendar.getInstance();
        try {
            startDate = ymdhm.parse(ymdhm.format(new Date()));// 当日00：00：00
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
                    startDate = ymdhm.parse(ds.getDate());
//                    if (ymd.format(new Date()).equals(de.getDate())) {// 结束日期是今天
//                        endDate = new Date();// 当前时间
//                    } else {
//                        endDate = new Date(ymd.parse(de.getDate()).getTime() + 1000 * 60 * 60 * 24 - 1);// 当日23：59：59
//                    }
                    endDate = ymdhm.parse(de.getDate());
                    if (endDate.before(startDate)) {
                        App.showToast("开始日期不能大于结束日期");
                    } else {
                        queryMembers();
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
