package com.niucong.punchcardserver.yunshitu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.SignDB;
import com.niucong.yunshitu.config.Configuration;
import com.niucong.yunshitu.config.GlobalConfiguration;
import com.niucong.yunshitu.face.FaceReg;
import com.niucong.yunshitu.util.ImageUtils;
import com.niucong.yunshitu.util.NetworkLiuUtils;
import com.niucong.yunshitu.util.NetworkUtils;
import com.niucong.yunshitu.util.OSUtils;
import com.niucong.yunshitu.util.SharedPreferencesUtils;
import com.niucong.yunshitu.view.AutoFixTextureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.yunshitu.facesdk.camera.GLCameraSource;
import cn.yunshitu.facesdk.face.Classifier;
import cn.yunshitu.facesdk.face.FaceDetectProcessor;
import cn.yunshitu.facesdk.face.FaceDetector;
import cn.yunshitu.facesdk.face.Tracker;

/**
 * 人脸检测Activity
 */
public class FaceDetectActivity extends AppCompatActivity {
    private static final String TAG = FaceDetectActivity.class.getSimpleName();

    // 用于非主线程连续绘制Bitmap
    private AutoFixTextureView mTextureView;
    private ImageButton mRotateButton;
    private TextView mTextView;

    private FaceDetectProcessor mProcessor;
    private GLCameraSource mCameraSrc;
    private int mCameraType;

    private Bitmap mBitmap;

    private Configuration mConfiguration;

    private final Object mDrawLock = new byte[0];
    private boolean mIsDrawing;

    private boolean mIsDebugUpdate = false;

    private final static int[] COLOR_LIST_1 = {
            // 绿色渐变
            Color.argb(0x7A, 0x00, 0xCC, 0xFF), Color.argb(0x7A, 0x7A, 0xFD, 0x96),
            // 橘色渐变
            Color.argb(0x7A, 0xF5, 0x9A, 0x56), Color.argb(0x7A, 0xFA, 0xFA, 0xA7),
            // 紫色渐变
            Color.argb(0x7A, 0x6F, 0x3E, 0xED), Color.argb(0x7A, 0xFA, 0x6F, 0xDE),
            // 红色渐变
            Color.argb(0x7A, 0xFF, 0x00, 0x00), Color.argb(0x7A, 0xFF, 0x00, 0x66),
    };

    private final static int[] COLOR_LIST_2 = {
            // 绿色渐变
            Color.argb(0x8F, 0x00, 0xCC, 0xFF), Color.argb(0x8F, 0x7A, 0xFD, 0x96),
            // 橘色渐变
            Color.argb(0x8F, 0xF5, 0x9A, 0x56), Color.argb(0x8F, 0xFA, 0xFA, 0xA7),
            // 紫色渐变
            Color.argb(0x8F, 0x6F, 0x3E, 0xED), Color.argb(0x8F, 0xFA, 0x6F, 0xDE),
            // 红色渐变
            Color.argb(0x8F, 0xFF, 0x00, 0x00), Color.argb(0x8F, 0xFF, 0x00, 0x66),
    };

    private final static int[] COLOR_LIST_3 = {
            // 绿色渐变
            Color.argb(0x9F, 0x00, 0xCC, 0xFF), Color.argb(0x9F, 0x7A, 0xFD, 0x96),
            // 橘色渐变
            Color.argb(0x9F, 0xF5, 0x9A, 0x56), Color.argb(0x9F, 0xFA, 0xFA, 0xA7),
            // 紫色渐变
            Color.argb(0x9F, 0x6F, 0x3E, 0xED), Color.argb(0x9F, 0xFA, 0x6F, 0xDE),
            // 红色渐变
            Color.argb(0x9F, 0xFF, 0x00, 0x00), Color.argb(0x9F, 0xFF, 0x00, 0x66),
    };


    // 边框画笔
    private Paint[] mOutStrokePaints;
    private Paint[] mInStrokePaints;
    // 填充框画笔
    private Paint[] mFillPaints;
    // 字体画笔
    private TextPaint[] mTextPaints;

    private ExecutorService mUploadExecutor;                 // 线程池
    private final Object mExecutorLock = new byte[0];       // 线程池锁
    private final static int EXECUTOR_CAPACITY = 4;

    private ExecutorService mServiceExecutor;

    private Set<UUID> mUUIDSet;

    private MediaPlayer mMediaPlayer;
    private AtomicBoolean mIsPlaying = new AtomicBoolean(false);

    private void initView() {
        mTextureView = findViewById(com.niucong.yunshitu.R.id.texture_view);
        mRotateButton = findViewById(com.niucong.yunshitu.R.id.rotate_btn);
        mTextView = findViewById(com.niucong.yunshitu.R.id.debug_info);
    }

    private void initPaint() {
        mOutStrokePaints = new Paint[COLOR_LIST_1.length / 2];
        mInStrokePaints = new Paint[COLOR_LIST_2.length / 2];
        mFillPaints = new Paint[COLOR_LIST_3.length / 2];
        mTextPaints = new TextPaint[1];

        mTextPaints[0] = new TextPaint();
        mTextPaints[0].setAntiAlias(true);
        mTextPaints[0].setTextSize(16);
        mTextPaints[0].setColor(Color.argb(0xEA, 0xFF, 0xFF, 0xFF));
        mTextPaints[0].setShadowLayer(2f, 0f, 2f, Color.rgb(0x02, 0x05, 0x46));
        mTextPaints[0].setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.NORMAL));

        for (int i = 0; i < mOutStrokePaints.length; ++i) {
            Shader shader = new LinearGradient(
                    0, 0, 100, 100, COLOR_LIST_1[2 * i], COLOR_LIST_1[2 * i + 1], Shader.TileMode.MIRROR
            );
            mOutStrokePaints[i] = new Paint();
            mOutStrokePaints[i].setStyle(Paint.Style.STROKE);
            mOutStrokePaints[i].setStrokeWidth(3);
            mOutStrokePaints[i].setShader(shader);
            mOutStrokePaints[i].setShadowLayer(2f, 0f, 2f, Color.rgb(0x02, 0x05, 0x46));

        }

        for (int i = 0; i < mInStrokePaints.length; ++i) {
            Shader shader = new LinearGradient(
                    0, 0, 100, 100, COLOR_LIST_2[2 * i], COLOR_LIST_2[2 * i + 1], Shader.TileMode.MIRROR
            );
            mInStrokePaints[i] = new Paint();
            mInStrokePaints[i].setStyle(Paint.Style.STROKE);
            mInStrokePaints[i].setStrokeWidth(2);
            mInStrokePaints[i].setShader(shader);
        }

        for (int i = 0; i < mFillPaints.length; ++i) {
            Shader shader = new LinearGradient(
                    0, 0, 100, 100, COLOR_LIST_3[2 * i], COLOR_LIST_3[2 * i + 1], Shader.TileMode.MIRROR
            );
            mFillPaints[i] = new Paint();
            mFillPaints[i].setStyle(Paint.Style.FILL);
            mFillPaints[i].setShader(shader);
            mFillPaints[i].setShadowLayer(5f, 0f, 3f, Color.rgb(0x02, 0x05, 0x46));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(com.niucong.yunshitu.R.layout.activity_face_detect);

        initView();

        initPaint();

        mCameraType = SharedPreferencesUtils.getCameraFacing(getApplicationContext());
        mCameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;

        mConfiguration = GlobalConfiguration.getConfiguration();

        if (mConfiguration == null) {
            // TODO: 应用崩溃重启时mConfiguration会为null
        }

        mUploadExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(EXECUTOR_CAPACITY), new ThreadPoolExecutor.AbortPolicy());

        mServiceExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.AbortPolicy());

        initProcessor();
        //mProcessor.setAutoFocus(true);
        //mProcessor.setCameraType(mCameraType);

        // 添加摄像头作为输入源
        mCameraSrc = new GLCameraSource(getApplicationContext())
                .setCameraType(mCameraType)
                .setPreviewSize(640, 480)
                .setXFlip(mCameraType == Camera.CameraInfo.CAMERA_FACING_FRONT);
        mProcessor.addInputSrc(mCameraSrc);

        // 本地文件测试
        /*File file = new File(OSUtils.getTempDir(getApplicationContext(), "video"), "test.mp4");
        try (InputStream in = getResources().openRawResource(R.raw.output);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int cnt;
            while ((cnt = in.read(buffer)) != -1) {
                out.write(buffer, 0, cnt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        VideoSource videoSrc = new VideoSource(file.getAbsolutePath(), 40);
        mProcessor.addInputSrc(videoSrc);*/

        mRotateButton.setVisibility(View.GONE);
        mRotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProcessor != null) {
                    mRotateButton.setEnabled(false);
                    disableDraw();
                    mCameraType = mCameraType == Camera.CameraInfo.CAMERA_FACING_BACK ?
                            Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
                    SharedPreferencesUtils.putCameraFacing(getApplicationContext(), mCameraType);
                    mCameraSrc.stop();
                    mProcessor.stop();

                    mCameraSrc.setCameraType(mCameraType);

                    mFaceMats.clear();

                    mCameraSrc.start();
                    mCameraSrc.setAutoFocus(true);
                    mProcessor.start();
                    // 延迟1s开启绘制，防止抖动
                    new Handler(getMainLooper()).postDelayed(() -> {
                        mRotateButton.setEnabled(true);
                        enableDraw();
                    }, 1000);
                }
            }
        });

        mTextureView.setOnClickListener(view -> mIsDebugUpdate = !mIsDebugUpdate);
        /*
        // 触屏对焦测试
        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float x = motionEvent.getX(), y = motionEvent.getY();
                        float length = Math.min(view.getWidth(), view.getHeight()) / 10.0f;
                        x -= length / 2;
                        y -= length / 2;
                        x = Math.max(Math.min(view.getWidth() - length, x), 0);
                        y = Math.max(Math.min(view.getHeight() - length, y), 0);
                        Rect2d rect = new Rect2d(x / view.getWidth(), y / view.getHeight(), length / view.getWidth(), length / view.getHeight());
                        if (mProcessor != null) {
                            mProcessor.setFocus(rect);
                        }
                        break;
                }
                return false;
            }
        });
        */

        mUUIDSet = new HashSet<>();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> mIsPlaying.set(false));
    }

    @Override
    protected void onDestroy() {
        synchronized (mExecutorLock) {
            if (mUploadExecutor != null) {
                mUploadExecutor.shutdown();
            }
            if (mServiceExecutor != null) {
                mServiceExecutor.shutdown();
            }
        }
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mProcessor != null) {
            mFaceMats.clear();
            if (mCameraSrc.start() && mProcessor.start()) {
                mCameraSrc.setAutoFocus(true);
                enableDraw();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(com.niucong.yunshitu.R.string.camera_open_failed)
                        .setCancelable(false)
                        .setNegativeButton(com.niucong.yunshitu.R.string.go_back, (dialogInterface, i) -> finish())
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        if (mProcessor != null) {
            disableDraw();
            mProcessor.stop();
        }
        if (mCameraSrc != null) {
            mCameraSrc.stop();
        }
        if (mUUIDSet != null) {
            mUUIDSet.clear();
        }
        super.onPause();
    }

    private Map<UUID, Mat> mFaceMats = new LinkedHashMap<>();

    /**
     * 每帧结果回调
     *
     * @param result 识别结果
     * @param skip   是否跳帧
     */
    protected void onDetectResultCallback(FaceDetectProcessor.DetectResult result, boolean skip) {
        // 检测结果
        if (result == null || result.getMats() == null) {
            return;
        }
//        Log.d(TAG,"onDetectResultCallback skip=" + skip);
        drawResultOnTexture(result, skip);
        if (mConfiguration.isAutoMagnify()) {
            //autoMagnify(result, skip);
        }

        Iterator<Mat> it1 = result.getAlignResult().iterator();
        Iterator<UUID> it2 = result.getUUID().iterator();

        Log.e(TAG, "onDetectResultCallback result=" + result.toString());
        while (it1.hasNext() && it2.hasNext()) {
            Mat mat = it1.next();
            UUID uuid = it2.next();
            if (mat != null && mat.width() > 0 && mat.height() > 0) {
                Mat oldMat = mFaceMats.get(uuid);
                if (oldMat != null) {
                    oldMat.release();
                }
                mFaceMats.put(uuid, mat.clone());
            }

            Log.e(TAG, "onDetectResultCallback uuid=" + uuid.toString());

            if (mConfiguration.isUpload() && uploadCnt++ % mConfiguration.getUploadInterval() == 0) {
                uploadFace(mat, uuid);
            }
            faceRecognize(mat, uuid);

            /*if(mat!=null) {
                if(mUploadFlag!=0&&mBitmap!=null&&mat.width()>0&&mat.height()>0)
                {
                    Bitmap flagmap = Bitmap.createBitmap(
                            mat.width(),
                            mat.height(),
                            Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat,flagmap);
                    if(cutflag<30)
                    {
                        if(cutflag%5==0)
                            savecapture(flagmap);
                        cutflag++;
                    }
                    else
                    {
                        // 开启添加头像的返回
                        Intent i = new Intent(FaceDetectActivity.this,AddUserActivity.class);
                        i.putStringArrayListExtra("mMatList", mMatList);
                        setResult(1,i);
                        finish();
                    }
                }
            }*/
        }
    }

    /**
     * 人脸序列终止回调
     *
     * @param faceUUID 序列UUID
     */
    protected void onFaceFinishedCallback(UUID faceUUID) {
        mUUIDSet.remove(faceUUID);
        mFaceMats.remove(faceUUID);
    }

    /**
     * 绘制结果至控件
     *
     * @param result 识别结果
     * @param skip   是否为跳帧
     */
    protected void drawResultOnTexture(FaceDetectProcessor.DetectResult result, boolean skip) {
        if (result == null || result.getMats() == null || result.getMats().size() == 0) {
            return;
        }
        Mat firstMat = result.getMats().get(0);
        if (mBitmap == null || mBitmap.getWidth() != firstMat.width() || mBitmap.getHeight() != firstMat.height()) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            mBitmap = Bitmap.createBitmap(
                    firstMat.width(),
                    firstMat.height(),
                    Bitmap.Config.ARGB_8888);
            runOnUiThread(() -> {
                mTextureView.setAspectRatio(mBitmap.getWidth(), mBitmap.getHeight());
                mTextureView.requestLayout();
            });
        }

        if (isDraw()) {
            Utils.matToBitmap(firstMat, mBitmap);
            ImageUtils.drawResult(
                    mBitmap, result, mConfiguration.isShowLandmarks() && !skip,
                    mOutStrokePaints, mInStrokePaints, mFillPaints, mTextPaints);
            // 底部人脸绘制
            if (mConfiguration.isShowSmallFace()) {
                ImageUtils.drawSmallFace(mBitmap, mFaceMats.values(), 4);
            }
            mTextureView.drawBitmap(mBitmap);
        }
        runOnUiThread(() -> mTextView.setText(mIsDebugUpdate ? result.getDebugInfo() : ""));
    }

    private int mMagnifyCount = 0;

    /**
     * 自动缩放
     * @param result 识别结果
     * @param skip   是否为跳帧
     */
    /*protected void autoMagnify(FaceDetectProcessor.DetectResult result, boolean skip) {
        if (result == null) {
            return;
        }
        if (result.getDetectResult().size() > 0) {
            FaceDetector.FaceRet faceRet = result.getDetectResult().get(0);
            Rect2d magnify = mProcessor.getMagnify();
            if (magnify == null) {
                magnify = new Rect2d(0, 0, 1, 1);
            }
            double areaRatio =
                    (faceRet.getRight() - faceRet.getLeft()) * (faceRet.getBottom() - faceRet.getTop());
            if (areaRatio < 1.0 / 5 || areaRatio > 1.0 / 3) {
                if (mMagnifyCount == 0) {
                    double x = magnify.x + magnify.width * faceRet.getLeft();
                    double y = magnify.y + magnify.height * faceRet.getTop();
                    double width = magnify.width * (faceRet.getRight() - faceRet.getLeft());
                    double height = magnify.height * (faceRet.getBottom() - faceRet.getTop());

                    double centerX = x + width / 2;
                    double centerY = y + height / 2;

                    double newWidth = 2 * Math.sqrt(width * height);
                    double newHeight = newWidth;

                    if (newWidth >= 1.0) {
                        mProcessor.setMagnify(null);
                    } else {
                        double newX = centerX - newWidth / 2;
                        double newY = centerY - newHeight / 2;
                        newX = Math.max(Math.min(newX, 1), 0);
                        newY = Math.max(Math.min(newY, 1), 0);
                        mProcessor.setMagnify(new Rect2d(newX, newY, newWidth, newHeight));
                    }
                    mMagnifyCount = 20;
                    //mProcessor.setFocus(new Rect2d(x, y, width, height));
                } else {
                    --mMagnifyCount;
                }
            }
        } else {
            if (mMagnifyCount == 0) {
                mProcessor.setMagnify(null);
            } else {
                --mMagnifyCount;
            }
        }
    }*/

    /**
     * 上传人脸
     *
     * @param faceMat  人脸图像
     * @param faceUUID 人脸UUID
     */
    protected void uploadFace(@Nullable Mat faceMat, @Nullable UUID faceUUID) {
        if (faceMat == null || faceUUID == null) {
            return;
        }
        final Map<String, String> params = new HashMap<>();
        params.put("trace_id", faceUUID.toString());
        params.put("device_id", OSUtils.getDeviceID(FaceDetectActivity.this));
        params.put("note", "");
        Log.e(TAG, "uploadFace params=" + params.toString());
        final Mat mat = faceMat.clone();

        synchronized (mExecutorLock) {
            if (!mUploadExecutor.isShutdown()) {
                try {
                    mUploadExecutor.execute(() -> {
                        // 上传
                        String response = NetworkUtils.uploadImage(
                                mat,
                                new Size(mConfiguration.getUploadWidth(), mConfiguration.getUploadHeight()),
                                mConfiguration.getUploadFormat(),
                                mConfiguration.getUploadQuality(),
                                "content",
                                params);
                        Log.i(TAG, "uploadFace response=" + response);
                    });
                } catch (RejectedExecutionException ignored) {
                    mat.release();
                }
            } else {
                mat.release();
            }
        }
    }

    /**
     * 人脸姓名识别流程,调用具体识别方法,语音欢迎并维护已识别人脸
     *
     * @param faceMat  人脸图像
     * @param faceUUID 人脸ID
     */
    protected void faceRecognize(@Nullable Mat faceMat, @Nullable UUID faceUUID) {
        if (faceMat == null || faceUUID == null) {
            return;
        }
        Log.e(TAG, "faceRecognize faceUUID=" + faceUUID.toString());
        if (!mUUIDSet.contains(faceUUID) && mIsPlaying.compareAndSet(false, true)) {
            final Mat mat = faceMat.clone();
            synchronized (mExecutorLock) {
                if (!mServiceExecutor.isShutdown()) {
                    try {
                        mServiceExecutor.execute(() -> {
                            String name = null;
                            if (mConfiguration.isUseOnlineRec()) {
                                Log.i(TAG, "faceRecognize use_online true");
                                name = faceIdentify(mat);
                            } else {
                                Log.i(TAG, "faceRecognize use_online false");
                                name = FaceReg.INSTANCE.search_face(mat);
                            }
                            Log.i(TAG, "faceRecognize name=" + name);
                            if (name != null) {
                                MemberDB db = DataSupport
                                        .where("phone = ? and isDelete = 0", name)
                                        .findFirst(MemberDB.class);
                                String tip = "";
                                if (db != null) {
                                    name = db.getName();
                                    if (!updateSign(db))
                                        return;
                                    int count = DataSupport.where("memberId = ?",
                                            "" + db.getId()).count(SignDB.class);
                                    tip = name + "打卡成功，您已成功打卡" + count + "天";
                                } else {
                                    tip = App.sp.getString("welTip", "欢迎参观！");
                                }

                                final String tipStr = tip;
                                this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(FaceDetectActivity.this, tipStr,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                App.app.mSpeechSynthesizer.speak(tipStr);
                                // 已识别
                                mUUIDSet.add(faceUUID);
                            } else {
                                mIsPlaying.set(false);
                                App.app.mSpeechSynthesizer.speak(App.sp.getString("welTip", "欢迎参观！"));
                            }
                            mat.release();

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);

                        });
                    } catch (RejectedExecutionException ignored) {
                        mat.release();
                        mIsPlaying.set(false);
                    }
                } else {
                    mat.release();
                }
            }
        }
    }

    /**
     * 打卡
     *
     * @param db
     */
    private boolean updateSign(MemberDB db) {
        try {
            String userId = db.getId() + "";
            SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
            long time = YMD.parse(YMD.format(new Date())).getTime();
            SignDB signDB = DataSupport.where("memberId = ? and startTime > ? and startTime < ?",
                    userId, time + "", time + 24 * 60 * 60 * 1000 + "").findFirst(SignDB.class);
            Log.i(TAG, "SignHandler startTime=" + YMD.format(new Date()));
            if (signDB == null) {
                signDB = new SignDB();
                signDB.setMemberId(Integer.valueOf(userId));
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                signDB.setName(memberDB.getName());
                signDB.setSuperId(memberDB.getSuperId());
                signDB.setStartTime(System.currentTimeMillis());
                signDB.save();
            } else {
//                long lastTime = Math.max(signDB.getStartTime(), signDB.getEndTime());
//                if (System.currentTimeMillis() - lastTime < 60 * 1000) {// 避免一分钟内连续打卡
//                    return false;
//                }
                signDB.setEndTime(System.currentTimeMillis());
                signDB.update(signDB.getId());
            }
            Log.i(TAG, "SignHandler SuperId=" + signDB.getSuperId());

            // TODO 推送给客户端
            SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            List<String> ids = new ArrayList<>();
            org.json.JSONObject object = new org.json.JSONObject();
            String bmobID = db.getBmobID();
            object.put("msg", YMDHM.format(new Date()) + "打卡成功");
            object.put("code", 2);
            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
            App.addPush(ids, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 在线人脸姓名识别具体方法与处理
     *
     * @param mat 人脸图像
     * @return 识别姓名, null表示识别失败
     */
    protected String faceIdentify(Mat mat) {
        if (mat == null || mat.width() == 0 || mat.height() == 0) {
            return null;
        }
        String result = NetworkLiuUtils.detectService8888(mat);
        Log.i(TAG, "identify: " + result);
        if (result == null) {
            return null;
        }
        return result;
    }

    private void initProcessor() {
        // 初始化 构造函数签名
        // 1 int bufferedSize	缓冲区大小（单位：帧）
        // 2 long millisecondPerFrame	每隔多少毫秒从源取一帧
        // 3 int maxFaces	同时维护的最大人脸序列
        // 4 int detectPeriod	调用检测的周期
        // 5 int detectorWidth	算法输入短边尺寸
        // 6 Size resize	人脸对对齐后的缩放尺寸
        // 7 Size cropSize	分类的居中尺寸
        // 8 boolean discard	图像输入队列满时是否丢弃最早的数据
        // int var1, long var2, int var4, int var5, int var6, Size var7, Size var8, boolean var9
        mProcessor = new FaceDetectProcessor(
                mConfiguration.getBufferedSize(),
                mConfiguration.getMillisecondPerFrame(), /*mConfiguration.getMaxNumOfFaces()*/
                50,
                mConfiguration.getDetectPeriod(),
                mConfiguration.getFaceDetectorWidth(),
                new Size(mConfiguration.getResizeWidth(), mConfiguration.getResizeHeight()),
                new Size(mConfiguration.getCropWidth(), mConfiguration.getCropHeight()),
                true) {
            @Nullable
            @Override
            protected Classifier getClassifier() {
                return mConfiguration.getClassifier();// 获得分类器
            }

            @Nullable
            @Override
            protected FaceDetector getFaceDetector() {
                return mConfiguration.getFaceDetector();// 获得人脸检测模块
            }

            @Nullable
            @Override
            protected Tracker getNewTracker() {
                return mConfiguration.getTracker();// 获得追踪器
            }

            @Override
            protected double getTrackerThreshold() {
                return mConfiguration.getTrackerThreshold();// 获得追踪器阈值
            }
        };
        mProcessor.setOnDetectCallback(this::onDetectResultCallback);

        mProcessor.setOnFaceFinishedCallback(this::onFaceFinishedCallback);

        mProcessor
                .setGeometryLivingError(mConfiguration.getGeometryLivingError())
                .setGeometryLivingDetectCount(mConfiguration.getGeometryLivingDetectCount())
                .setGeometryLivingDetectGap(mConfiguration.getGeometryLivingDetectGap());
    }

    private void enableDraw() {
        synchronized (mDrawLock) {
            mIsDrawing = true;
        }
    }

    private void disableDraw() {
        synchronized (mDrawLock) {
            mIsDrawing = false;
        }
    }

    private boolean isDraw() {
        synchronized (mDrawLock) {
            return mIsDrawing;
        }
    }

    private int uploadCnt = 0;

    private static final String TTS_TEMPLATE_URL = "http://tts.baidu.com/text2audio?lan=zh&ie=UTF-8&spd=5&text=%s";

    private static String getName(String result) {
        String name = null;

        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.optJSONObject("data");
            if (data != null) {
                JSONArray faceApp = data.optJSONArray("face_app");
                JSONObject face = faceApp.length() > 0 ? faceApp.optJSONObject(0) : null;
                if (face != null) {
                    name = face.getString("name");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return name;
    }

    private ArrayList<String> fPathlist = new ArrayList<>();
    private int cutflag = 0;

    //截图
    private int savecapture(Bitmap myfacebitmap) {
        String dir = getFilesDir().getAbsolutePath() + "/yunshitu/liu/";
        String filename = UUID.randomUUID().toString() + ",jpg";
        try {
            File mydir = new File(dir);
            if (!mydir.exists()) {
                mydir.mkdirs();
            }

            Log.d("liu", "savecapture: " + cutflag);
            File f = new File(dir + filename);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream o = new FileOutputStream(f);
            myfacebitmap.compress(Bitmap.CompressFormat.JPEG, 100, o);
            o.flush();
            o.close();
            cutflag++;
            fPathlist.add(f.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cutflag;
    }
}
