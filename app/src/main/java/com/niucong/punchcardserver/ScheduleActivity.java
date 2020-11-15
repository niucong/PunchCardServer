package com.niucong.punchcardserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.bin.david.form.core.SmartTable;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.databinding.ActivityScheduleBinding;
import com.niucong.punchcardserver.db.ScheduleDB;
import com.niucong.punchcardserver.table.Schedule;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    ActivityScheduleBinding binding;

    private SmartTable<Schedule> table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_schedule);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        table = binding.scheduleTable;
        WindowManager wm = this.getWindowManager();
        int screenWith = wm.getDefaultDisplay().getWidth();
        table.getConfig().setMinTableWidth(screenWith); //设置最小宽度 屏幕宽度
        getScheduleList();
    }

    private void getScheduleList() {
        if (DataSupport.count(ScheduleDB.class) == 0) {
            if (!ToolUtils.setPermission(this, this, Manifest
                    .permission.READ_EXTERNAL_STORAGE, 1)) {
                selectFilePath();
            }
        }

        final List<Schedule> schedules = new ArrayList<>();
        for (ScheduleDB scheduleDB : DataSupport.findAll(ScheduleDB.class)) {
            schedules.add(new Schedule(scheduleDB.getTimeRank(), scheduleDB.getSectionName(), scheduleDB.getTime()));
        }

        table.setData(schedules);
        table.getConfig().setShowTableTitle(false);
        table.setZoom(true, 2, 0.2f);
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
            if (requestCode == 2) {
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
//                showProgressDialog("正在导入人员……");
                new Thread() {
                    @Override
                    public void run() {
                        List<ScheduleDB> list = readExcel(filePath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (list.size() > 0) {
                                    DataSupport.saveAll(list);

                                    getScheduleList();
                                } else {
                                    finish();
                                }
                            }
                        });
                        super.run();
                    }
                }.start();
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

    /**
     * 读取表格
     *
     * @param filePath
     */
    private List<ScheduleDB> readExcel(String filePath) {
//        App.showToast(filePath);
        List<ScheduleDB> list = new ArrayList<>();
        InputStream stream = null;
        try {
            /** We now need something to iterate through the cells. **/
            Iterator<Row> rowIter = null;
//            if (obj instanceof HSSFWorkbook) {
//            }
            Log.d("ScheduleActivity", "readExcel filePath=" + filePath);
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

                String timeRank = "";
                while (rowIter.hasNext()) {
//                    if (i > 2) {// 第一条数据是标题
                    Row myRow = rowIter.next();
//                    threadPogress("正在导入第" + (i - 1) + "件商品……");
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    ScheduleDB scheduleDB = new ScheduleDB();
                    int j = 0;
                    boolean isFailure = false;
                    while (cellIter.hasNext()) {
                        Cell myCell = (Cell) cellIter.next();
                        if (j < 4) {
                            int columnIndex = myCell.getColumnIndex();
                            if (columnIndex == 0) {
                                Log.d("ScheduleActivity", "readExcel TimeRank=" + myCell.toString());
                                if ("时段".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else if (TextUtils.isEmpty(myCell.toString())) {
                                    scheduleDB.setTimeRank(timeRank);
                                } else {
                                    timeRank = myCell.toString();
                                    scheduleDB.setTimeRank(myCell.toString());
                                }
                            } else if (columnIndex == 1) {
                                Log.d("ScheduleActivity", "readExcel SectionName=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "节次".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    scheduleDB.setSectionName(myCell.toString());
                                }
                            } else if (columnIndex == 2) {
                                Log.d("ScheduleActivity", "readExcel Time=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "上课时间".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    scheduleDB.setTime(myCell.toString());
                                }
                            } else {
                                break;
                            }
                        }
                        j += 1;
                    }
                    if (!isFailure) {
                        list.add(scheduleDB);
                    }
//                    }
                    i += 1;
                }
//                threadToast("共导入" + (i - 1) + "位实验室人员");
            }
            threadToast("" + list.size());
            return list;
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
        return list;
    }

    private void threadToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                App.showToast(msg);
            }
        });
    }

}
