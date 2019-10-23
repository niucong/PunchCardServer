package com.niucong.punchcardserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bin.david.form.core.SmartTable;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.databinding.ActivityScheduleBinding;
import com.niucong.punchcardserver.db.CalendarDB;
import com.niucong.punchcardserver.table.Calendar;
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

public class CalendarActivity extends AppCompatActivity {

    ActivityScheduleBinding binding;

    private SmartTable<Calendar> table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_schedule);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

//        FontStyle.setDefaultTextSize(DensityUtils.sp2px(this,15)); //设置全局字体大小
        table = binding.scheduleTable;
        getCalendarList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_refresh:
                if (!ToolUtils.setPermission(this, this, Manifest
                        .permission.READ_EXTERNAL_STORAGE, 1)) {
                    selectFilePath();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCalendarList() {
        threadToast("" + DataSupport.count(CalendarDB.class));
        if (DataSupport.count(CalendarDB.class) == 0) {
            if (!ToolUtils.setPermission(this, this, Manifest
                    .permission.READ_EXTERNAL_STORAGE, 1)) {
                selectFilePath();
            }
        }

        final List<Calendar> calendars = new ArrayList<>();
        for (CalendarDB calendarDB : DataSupport.findAll(CalendarDB.class)) {
            calendars.add(new Calendar(calendarDB.getSession(), calendarDB.getWeekly(), calendarDB.getMonth(),
                    calendarDB.getMonday(), calendarDB.getTuesday(), calendarDB.getWednesday(), calendarDB.getThursday(),
                    calendarDB.getFriday(), calendarDB.getSaturday(), calendarDB.getSunday()));
        }

        table.setData(calendars);
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
                        List<CalendarDB> list = readExcel(filePath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (list.size() > 0) {
                                    DataSupport.deleteAll(CalendarDB.class);
                                    DataSupport.saveAll(list);

                                    getCalendarList();
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

    /**
     * 读取表格
     *
     * @param filePath
     */
    private List<CalendarDB> readExcel(String filePath) {
//        App.showToast(filePath);
        List<CalendarDB> list = new ArrayList<>();
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

                String session = "";// 学期
                String weekly = "";// 周数
                String month = "";// 月份
                while (rowIter.hasNext()) {
//                    if (i > 2) {// 第一条数据是标题
                    Row myRow = rowIter.next();
//                    threadPogress("正在导入第" + (i - 1) + "件商品……");
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    CalendarDB calendarDB = new CalendarDB();
                    int j = 0;
                    boolean isFailure = false;
                    while (cellIter.hasNext()) {
                        Cell myCell = (Cell) cellIter.next();
                        if (j < 11) {
                            int columnIndex = myCell.getColumnIndex();
                            if (columnIndex == 0) {
                                Log.d("ScheduleActivity", "readExcel session=" + myCell.toString());
                                if ("学期".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else if (TextUtils.isEmpty(myCell.toString())) {
                                    calendarDB.setSession(session);
                                } else {
                                    session = myCell.toString();
                                    calendarDB.setSession(session);
                                }
                            } else if (columnIndex == 1) {
                                Log.d("ScheduleActivity", "readExcel weekly=" + myCell.toString());
                                if ("周数".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else if (TextUtils.isEmpty(myCell.toString())) {
                                    calendarDB.setWeekly(weekly);
                                } else {
                                    weekly = myCell.toString();
                                    calendarDB.setWeekly(weekly);
                                }
                            } else if (columnIndex == 2) {
                                Log.d("ScheduleActivity", "readExcel month=" + myCell.toString());
                                if ("\t\t\t\t星期\n月份".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else if (TextUtils.isEmpty(myCell.toString())) {
                                    calendarDB.setMonth(month);
                                } else {
                                    month = myCell.toString();
                                    calendarDB.setMonth(month);
                                }
                            } else if (columnIndex == 3) {
                                Log.d("ScheduleActivity", "readExcel monday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "一".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setMonday(myCell.toString());
                                }
                            } else if (columnIndex == 4) {
                                Log.d("ScheduleActivity", "readExcel Tuesday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "二".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setTuesday(myCell.toString());
                                }
                            } else if (columnIndex == 5) {
                                Log.d("ScheduleActivity", "readExcel Wednesday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "三".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setWednesday(myCell.toString());
                                }
                            } else if (columnIndex == 6) {
                                Log.d("ScheduleActivity", "readExcel Thursday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "四".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setThursday(myCell.toString());
                                }
                            } else if (columnIndex == 7) {
                                Log.d("ScheduleActivity", "readExcel Friday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "五".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setFriday(myCell.toString());
                                }
                            } else if (columnIndex == 8) {
                                Log.d("ScheduleActivity", "readExcel Saturday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "六".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setSaturday(myCell.toString());
                                }
                            } else if (columnIndex == 9) {
                                Log.d("ScheduleActivity", "readExcel sunday=" + myCell.toString());
                                if (TextUtils.isEmpty(myCell.toString()) || "日".equals(myCell.toString())) {
                                    isFailure = true;
                                    break;
                                } else {
                                    calendarDB.setSunday(myCell.toString());
                                }
                            } else {
                                break;
                            }
                        }
                        j += 1;
                    }
                    if (!isFailure) {
                        list.add(calendarDB);
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
