package com.niucong.punchcardserver.yunshitu;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.niucong.yunshitu.adapter.FaceviewAdapter;
import com.niucong.yunshitu.config.Configuration;
import com.niucong.yunshitu.config.GlobalConfiguration;
import com.niucong.yunshitu.config.JSONConfiguration;
import com.niucong.yunshitu.dialog.SelectPicDialog;
import com.niucong.yunshitu.face.FaceReg;
import com.niucong.yunshitu.util.FaceCollectData;
import com.niucong.yunshitu.util.NetworkLiuUtils;
import com.niucong.yunshitu.util.NetworkUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.yunshitu.facesdk.face.FaceDetector;


/**
 * 用于添加用户
 * Created by liuwenjie on 2018/3/26.
 */

public class AddUserActivity extends AppCompatActivity {

    public Button uploadbutton;
    public Button addimgbutton;
    private GridView mGridView;
    private MediaPlayer mMediaPlayer;
    protected List<Mat> mMatList = new ArrayList<>();
    boolean mIsPlaying = false;
    private Context mContext = this;
    private FaceviewAdapter mGridviewAdapter = new FaceviewAdapter(this, mMatList);
    int upselect = 2;
    private static final int PHOTO_REQUEST_GALLERY = 3;

    private Configuration mConfiguration;
    private FaceDetector mDetector;

    private Size mResize;
    SelectPicDialog selectPicDialog;
    String pname = "";

    private void init() {
        setContentView(com.niucong.yunshitu.R.layout.activity_add_user);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            actionBar.setTitle("新增人脸");
        }
        pname = getIntent().getStringExtra("phone");

        mGridView = findViewById(com.niucong.yunshitu.R.id.imgeGridView);
        mGridView.setAdapter(mGridviewAdapter);
        uploadbutton = findViewById(com.niucong.yunshitu.R.id.uploadbutton);
        addimgbutton = findViewById(com.niucong.yunshitu.R.id.addimgbutton);
        mMediaPlayer = new MediaPlayer();
        mGridView.setOnItemClickListener(new Myitemlistener());
        mConfiguration = GlobalConfiguration.getConfiguration();

        if (mConfiguration == null) {
            // TODO: 应用崩溃重启时mConfiguration会为null
        }
        mDetector = mConfiguration.getFaceDetector();
        mResize = new Size(mConfiguration.getResizeWidth(), mConfiguration.getResizeHeight());

        if (!TextUtils.isEmpty(pname)) {
            try {
                File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/images/person/" + pname);
                if (folder.exists()&&folder.isDirectory()){
                    for (File file : folder.listFiles()) {
                        try {
                            Uri uri = Uri.fromFile(file);
                            ContentResolver cr = this.getContentResolver();
                            if (uri != null) {
                                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                                Mat face_mat = new Mat();
                                Utils.bitmapToMat(bitmap, face_mat);
                                List<Mat> crop_result_list = mDetector.crop_face(face_mat, mConfiguration.getFaceDetectorWidth(), mResize);
                                mMatList.addAll(crop_result_list);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mGridviewAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ProgressDialog pd;

    private void showDialog() {
        pd = new ProgressDialog(this);
//        pd.setTitle("HORIZONTAL PROGRESS DIAGLOG");
//        pd.setIcon(R.mipmap.ic_launcher);
        pd.setMessage("正在保存头像，请稍后...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(true);
        pd.setIndeterminate(true);
        pd.show();
    }

    private void dismissDialog() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    private void setonclicklistener() {
        uploadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                startupload();
                            }
                        }
                ).start();
            }
        });
        addimgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicDialog = new SelectPicDialog(AddUserActivity.this, itemsOnClick);
                selectPicDialog.showAtLocation(AddUserActivity.this.findViewById(com.niucong.yunshitu.R.id.adduser_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setonclicklistener();
        new InitTask().execute(0);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mIsPlaying = false;
            }
        });
    }

    private void startupload() {
        Log.d("AddUserActivity", "startupload pname=" + pname);
        if (pname.equals(""))
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    Toast.makeText(AddUserActivity.this, "没有输入用户名", Toast.LENGTH_SHORT).show();
                }
            });
        else {
            if (mMatList.size() > 0 && upselect == 1) {
                int upflag = 0;
                Iterator<Mat> it = mMatList.iterator();
                while (it.hasNext()) {
                    Mat mat = it.next();
                    Log.d("AddUserActivity", "up pic :" + upflag);
                    if (upflag >= 10)
                        break;
                    List<Mat> singleMatList = Collections.singletonList(mat);
                    if (NetworkLiuUtils.getUpNameresult8888(NetworkLiuUtils.addService8888(singleMatList, pname)))
                        upflag++;
                }

                if (mediaPlay(pname, upflag >= 1))
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddUserActivity.this, "播放", Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddUserActivity.this, "未播放", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else if (mMatList.size() > 0 && upselect == 2) {
                Log.d("AddUserActivity", "startupload size=" + mMatList.size());
                boolean succ = false;
                FaceReg.INSTANCE.del_face(pname);
                if (mConfiguration.isUseOnlineRec()) {
                    succ = NetworkLiuUtils.getUpNameresult8888(NetworkLiuUtils.addService8888(mMatList, pname));
                } else {
                    for (Mat m : mMatList) {
                        Log.d("AddUserActivity", "startupload FaceReg.INSTANCE=" + (FaceReg.INSTANCE == null));
                        succ |= FaceReg.INSTANCE.add_face(pname, m);
                    }
                }

                final boolean result = succ;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(AddUserActivity.this, result ? "添加成功" : "添加失败", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                });

//                if (mediaPlay(pname, succ))
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(AddUserActivity.this, "播放", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                else
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(AddUserActivity.this, "未播放", Toast.LENGTH_SHORT).show();
//                        }
//                    });
            }

        }
    }


    //动态添加图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AddUserActivity", "onActivityResult requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (data != null && requestCode == 1) {
            mMatList.addAll(FaceCollectData.getData());
//            imageLayout.removeAllViews();
            if (mMatList.size() > 0) {
                if (mMatList.size() > 12)
                    mMatList.subList(12, mMatList.size()).clear();
                mGridviewAdapter.notifyDataSetChanged();
            }
        }
        if (data != null && requestCode == PHOTO_REQUEST_GALLERY) {// 拍照
            Uri uri = data.getData();
            Log.d("AddUserActivity", "onActivityResult uri=" + uri);
            ContentResolver cr = this.getContentResolver();
            if (uri == null) return;
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                Mat face_mat = new Mat();
                Utils.bitmapToMat(bitmap, face_mat);
                // 从图片中检测出的的人脸区域图片
                // Mat var1, int var2, Size var3
                List<Mat> crop_result_list = mDetector.crop_face(face_mat, mConfiguration.getFaceDetectorWidth(), mResize);
                mMatList.addAll(crop_result_list);
                mGridviewAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean mediaPlay(String name, boolean sucflag) {

        String encode = "";
        try {
            if (sucflag)
                encode = URLEncoder.encode(name + "上传成功", "UTF-8");
            else
                encode = URLEncoder.encode(name + "上传失败", "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        File music = NetworkUtils.downloadMusic(getApplicationContext(),
                String.format(NetworkLiuUtils.TTS_TEMPLATE_URL, encode));

        if (music == null) {
            return false;
        }

        mMediaPlayer.reset();
        try (FileInputStream in = new FileInputStream(music)) {
            mMediaPlayer.setDataSource(in.getFD());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
        return true;
    }

    private class InitTask extends AsyncTask<Integer, String, Void> {
        @Override
        protected Void doInBackground(Integer... index) {
            // 载入OpenCV
            OpenCVLoader.initDebug();

            // 获得单例配置
            if (GlobalConfiguration.getConfiguration() == null) {
                Configuration config = null;
                config = new JSONConfiguration(getApplicationContext(), "{}");
                GlobalConfiguration.setConfiguration(config);
            } else {
                GlobalConfiguration.getConfiguration().waitForInit();
            }

            return null;
        }
    }

    private class Myitemlistener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mMatList.size() > 0) {
                if (position < mMatList.size()) {
                    mMatList.remove(position);
                    mGridviewAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            selectPicDialog.dismiss();
            if (v.getId() == com.niucong.yunshitu.R.id.btn_pick_photo) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
            } else if (v.getId() == com.niucong.yunshitu.R.id.btn_take_photo) {
                Intent it = new Intent(AddUserActivity.this, FaceCollectActivity.class);
                it.putExtra("mUploadFlag", 1);
                startActivityForResult(it, 1);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGridviewAdapter != null) {
            mGridviewAdapter.recycleAll();
        }
    }
}


