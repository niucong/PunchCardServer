package com.niucong.punchcardserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import com.niucong.punchcardserver.util.FileUtils;
import com.niucong.punchcardserver.util.ToolUtils;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.litepal.crud.DataSupport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
            list.addAll(DataSupport.order("id desc").offset(offset).limit(pageSize).find(MemberDB.class));
        } else {
            if (offset == 0) {
                list.clear();
                allSize = DataSupport.where("number = ? or name = ? or phone = ?", searchKey, searchKey, searchKey).count(MemberDB.class);
            }
            list.addAll(DataSupport.order("id desc").where("number = ? or name = ? or phone = ?", searchKey, searchKey, searchKey)
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
            case R.id.action_import:
                if (!ToolUtils.setPermission(this, this, Manifest
                        .permission.READ_EXTERNAL_STORAGE, 1)) {
                    selectFilePath();
                }
                break;
            case R.id.action_export:
                SimpleDateFormat YMDHM = new SimpleDateFormat("yyyyMMddHHmm");
                final String path = FileUtils.getSdcardDir() + "/人员表_" + YMDHM.format(new Date()) + ".xls";
                showProgressDialog("正在导出人员……");
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            saveExcel(path);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog("已导出到" + path);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog("导出失败");
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
        String[] summary = {"姓名", "身份类型", "工号/学号", "手机号", "密码", "是否删除"};
        List<List<String>> summaryData = new ArrayList<List<String>>();
        List<MemberDB> dbs = DataSupport.findAll(MemberDB.class);
        for (MemberDB db : dbs) {
            List<String> rowData = new ArrayList<String>();
            rowData.add(db.getName());
            rowData.add(db.getType() == 1 ? "主任" : db.getType() == 2 ? "老师" : "学生");
            rowData.add(db.getNumber());
            rowData.add(db.getPhone());
            rowData.add(db.getPassword());
            rowData.add(db.getIsDelete() == 0 ? "否" : "是");
            summaryData.add(rowData);
        }

//            String[] water = {"系统名称", "门店号", "门店名称", "小票号", "活动编号"
//                    , "活动名称", "发券数量", "商品条码", "商品名称", "购买数量"
//                    , "发券时间", "分类代码", "是否领赠", "数据是否为真"};
//            List<List<String>> waterData = new ArrayList<List<String>>();
//            List<GenerWater> _listWater = new ArrayList<GenerWater>();
//            for (GenerWater wat : _listWater) {
//                List<String> rowData = new ArrayList<String>();
//                rowData.add(wat.getXtmc());
//                rowData.add(wat.getMdh());
//                rowData.add(wat.getMdmc());
//                rowData.add(wat.getXph());
//                rowData.add(wat.getHdbh());
//                rowData.add(wat.getHdmc());
//                rowData.add(wat.getFqsl());
//                rowData.add(wat.getSptm());
//                rowData.add(wat.getSpmc());
//                rowData.add(wat.getSl());
//                rowData.add(wat.getFqsj());
//                rowData.add(wat.getFldm());
//                rowData.add(wat.getSflq());
//                rowData.add(wat.getReal());
//                waterData.add(rowData);
//            }
//3、生成格式是xlsx可存储103万行数据，如果是xls则只能存不到6万行数据
        HSSFWorkbook workbook = new HSSFWorkbook();
//第一个表格内容
        FileUtils.exportExcel(workbook, 0, "人员表", summary, summaryData);
//第二个表格内容
//            exportExcel(workbook, 1, "部分流水数据", water, waterData);
//将所有的数据一起写入，然后再关闭输入流。
        workbook.write(out);
        out.close();
    }

    private void selectFilePath() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
//        intent.setType("video/*;image/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFilePath();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            } else if (requestCode == 2) {
                Uri uri = data.getData();
                String path;
                if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                    path = uri.getPath();
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    path = FileUtils.getPath(this, uri);
                } else {//4.4以下下系统调用方法
                    path = FileUtils.getRealPathFromURI(this, uri);
                }

                final String filePath = path;
                showProgressDialog("正在导入人员……");
                new Thread() {
                    @Override
                    public void run() {
                        readExcel(filePath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog("");
                                onRefresh();
                            }
                        });
                        super.run();
                    }
                }.start();
            }
        }
    }

    /**
     * 读取表格
     *
     * @param filePath
     */
    private void readExcel(String filePath) {
//        App.showToast(filePath);
        InputStream stream = null;
        try {
            /** We now need something to iterate through the cells. **/
            Iterator<Row> rowIter = null;
//            if (obj instanceof HSSFWorkbook) {
//            }
//            LoggerUtil.d("filePath=" + filePath);
            if (filePath.endsWith(".xlsx")) {
                stream = new FileInputStream(filePath);
                XSSFWorkbook workbook = new XSSFWorkbook(stream);
                XSSFSheet mySheet = workbook.getSheetAt(0);
                rowIter = mySheet.rowIterator();
            } else {
                stream = new FileInputStream(filePath);
                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(stream);
                // Create a workbook using the File System
                HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
                // Get the first sheet from workbook
                HSSFSheet mySheet = myWorkBook.getSheetAt(0);
                rowIter = mySheet.rowIterator();
            }
            if (rowIter != null) {
                int i = 0;
                while (rowIter.hasNext()) {
//                    if (i > 2) {// 第一条数据是标题
                    Row myRow = rowIter.next();
                    threadPogress("正在导入第" + (i - 1) + "件商品……");
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    MemberDB memberDB = new MemberDB();
                    int j = 0;

                    boolean isFailure = false;
                    while (cellIter.hasNext()) {
                        Cell myCell = (Cell) cellIter.next();
                        if (j < 7) {
                            int columnIndex = myCell.getColumnIndex();
                            if (columnIndex == 0) {
                                if (TextUtils.isEmpty(myCell.toString()) || "姓名".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    memberDB.setName(myCell.toString());
                                }
                            } else if (columnIndex == 1) {
                                memberDB.setType("主任".equals(myCell.toString()) ? 1 : "老师".equals(myCell.toString()) ? 2 : 3);
                            } else if (columnIndex == 2) {
                                if (TextUtils.isEmpty(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    memberDB.setNumber(myCell.toString());
                                }
                            } else if (columnIndex == 3) {
                                if (TextUtils.isEmpty(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    memberDB.setPhone(myCell.toString());
                                }
                            } else if (columnIndex == 4) {
                                memberDB.setPassword(myCell.toString());
                            } else if (columnIndex == 5) {
                                memberDB.setIsDelete(0);
                            } else {
                                break;
                            }
                        }
                        j += 1;
                    }
                    if (!isFailure) {
                        MemberDB oldDb = DataSupport.where("phone = ?",
                                memberDB.getPhone()).findFirst(MemberDB.class);

                        if (oldDb == null) {
                            memberDB.setLastEditTime(System.currentTimeMillis());
                            memberDB.save();
                        } else {
                            oldDb.setName(memberDB.getName());
                            oldDb.setType(memberDB.getType());
                            oldDb.setNumber(memberDB.getNumber());
                            oldDb.setPhone(memberDB.getPhone());
                            oldDb.setPassword(memberDB.getPassword());
                            oldDb.setLastEditTime(System.currentTimeMillis());
                            oldDb.setIsDelete(memberDB.getIsDelete());
                            oldDb.update(oldDb.getId());
                        }
                    }
//                    }
                    i += 1;
                }
                threadToast("共导入" + (i - 1) + "位实验室人员");
            }
        } catch (Exception e) {
            e.printStackTrace();
            threadToast("获取导入的文件错误");
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void threadToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                App.showToast(msg);
            }
        });
    }

    private void threadPogress(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog(msg);
            }
        });
    }

    private void showProgressDialog(String msg) {

    }

    private void dismissProgressDialog(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            App.showToast(msg);
        }
    }

}
