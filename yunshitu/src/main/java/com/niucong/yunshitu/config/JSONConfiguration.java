package com.niucong.yunshitu.config;

import android.content.Context;
import android.support.annotation.NonNull;

import com.niucong.yunshitu.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.yunshitu.facesdk.face.Classifier;
import cn.yunshitu.facesdk.face.FaceDetector;
import cn.yunshitu.facesdk.face.Featurer;
import cn.yunshitu.facesdk.face.Tracker;

/**
 * Created by yunshitu on 17-12-21.
 */

public class JSONConfiguration implements Configuration {
    private Context mContext;

    private FaceDetector mFaceDetector;
    private Classifier mClassifier;
    private Featurer mFeaturer;

    private String mDetectorType = DefaultConfig.DEFAULT_DETECTOR;
    private String mDetectorModel = DefaultConfig.DEFAULT_DLIB_MODEL;
    private int mMtcnnMinFaceSize = DefaultConfig.DEFAULT_MTCNN_MIN_FACE_SIZE;
    private int mMtcnnThreadsNum = DefaultConfig.DEFAULT_MTCNN_THREADS_NUM;
    private int mDetectorWidth = DefaultConfig.DEFAULT_DETECTOR_WIDTH;

    private String mTrackerType = DefaultConfig.DEFAULT_TRACKER;
    private double mTrackerThreshold = DefaultConfig.DEFAULT_TRACKER_THRESHOLD;

    private boolean mKCFHog = DefaultConfig.DEFAULT_KCF_HOG;
    private boolean mKCFFixedWindow = DefaultConfig.DEFAULT_KCF_FIXED_WINDOW;
    private boolean mKCFMultiScale = DefaultConfig.DEFAULT_KCF_MULTISCALE;
    private boolean mKCFLab = DefaultConfig.DEFAULT_KCF_LAB;

    private String mClassifierType = DefaultConfig.DEFAULT_CLASSIFIER_TYPE;
    private String mClassifierModel = DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_MODEL;
    private String mClassifierModelURL = null;
    private int mCaffeClassifierNumberThreads = DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_NUM_THREADS;
    private float mCaffeClassifierScale = DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_SCALE;
    private float[] mCaffeClassifierMeanValues = DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_MEAN_VALUE;

    private String mFeaturerType = DefaultConfig.DEFAULT_FEATURER_TYPE;
    private String mFeaturerModel = DefaultConfig.DEFAULT_CAFFE_FEATURER_MODEL;
    private String mFeaturerModelURL = null;
    private int mCaffeFeaturerNumberThreads = DefaultConfig.DEFAULT_CAFFE_FEATURER_NUM_THREADS;
    private float mCaffeFeaturerScale = DefaultConfig.DEFAULT_CAFFE_FEATURER_SCALE;
    private float[] mCaffeFeaturerMeanValues = DefaultConfig.DEFAULT_CAFFE_FEATURER_MEAN_VALUE;

    private int mBufferedSize = DefaultConfig.DEFAULT_BUFFERED_SIZE;

    private int mResizeWidth = DefaultConfig.DEFAULT_RESIZE_WIDTH;
    private int mResizeHeight = DefaultConfig.DEFAULT_RESIZE_HEIGHT;
    private int mCropWidth = DefaultConfig.DEFAULT_CROP_WIDTH;
    private int mCropHeight = DefaultConfig.DEFAULT_CROP_HEIGHT;

    private boolean mIsUseOnlineRec = DefaultConfig.DEFAULT_IS_USE_ONLINE_REC;

    private int mMaxNumOfFaces = DefaultConfig.DEFAULT_MAX_FACES;
    private long mMillisecondPerFrame = DefaultConfig.DEFAULT_MILLISECOND_FRAME;
    private int mDetectPeriod = DefaultConfig.DEFAULT_DETECT_PERIOD;
    private long mStartSleepTime = DefaultConfig.DEFAULT_START_SLEEP_TIME;
    private boolean mIsShowLandmarks = DefaultConfig.DEFAULT_SHOW_LANDMARKS;
    private boolean mIsShowSmallFace = DefaultConfig.DEFAULT_SHOW_SMALL_FACE;
    private boolean mIsAutoMagnify = DefaultConfig.DEFAULT_AUTO_MAGNIFY;

    private boolean mIsUpload = DefaultConfig.DEFAULT_IS_UPLOAD;
    private int mUploadInterval = DefaultConfig.DEFAULT_UPLOAD_INTERVAL;
    private int mUploadWidth = DefaultConfig.DEFAULT_UPLOAD_WIDTH;
    private int mUploadHeight = DefaultConfig.DEFAULT_UPLOAD_HEIGHT;
    private String mUploadFormat = DefaultConfig.DEFAULT_UPLOAD_FORMAT;
    private int mUploadQuality = DefaultConfig.DEFAULT_UPLOAD_QUALITY;

    private boolean mIsNeedUpdate = DefaultConfig.DEFAULT_NEED_UPDATE;
    private String mUpdateURL = DefaultConfig.DEFAULT_UPDATE_URL;

    private double mGeometryLivingError = DefaultConfig.DEFAULT_GEOMETRY_LIVING_ERROR;
    private long mGeometryLivingDetectGap = DefaultConfig.DEFAULT_GEOMETRY_LIVING_DETECT_GAP;
    private int mGeometryLivingDetectCount = DefaultConfig.DEFAULT_GEOMETRY_LIVING_DETECT_COUNT;

    private double mInfraredLivingError = DefaultConfig.DEFAULT_INFRARED_LIVING_ERROR;

    private List<String> mInputSourceTypes = new ArrayList<>();
    private List<Map<String, Object>> mInputSourceParams = new ArrayList<>();

    public JSONConfiguration(@NonNull Context context, String jsonStr) {
        mContext = context;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            loadInputSource(jsonObject.optJSONObject("input_src"));
            loadModel(jsonObject.optJSONObject("model"));
            loadOnlineRec(jsonObject.optJSONObject("online_rec"));
            loadDisplay(jsonObject.optJSONObject("display"));
            loadUpload(jsonObject.optJSONObject("upload"));
            loadUpdate(jsonObject.optJSONObject("update"));
            loadGeometryLivingDetect(jsonObject.optJSONObject("geometry_living_detect"));
            loadInfraredLivingDetect(jsonObject.optJSONObject("infrared_living_detect"));
            mBufferedSize = jsonObject.optInt("buffered_size", DefaultConfig.DEFAULT_BUFFERED_SIZE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        //mDetectorType = "mtcnn";
        //mMtcnnMinFaceSize = 50;
        //mMtcnnThreadsNum = 1;
        //mDetectorWidth = 240;
        switch (mDetectorType) {
            //case "dlib":
            //    mFaceDetector = ModelHelper.getLocalDlibFaceDetector(mContext, mDetectorType, mDetectorModel);
            //    break;
            case "mtcnn": default:
                mFaceDetector = ModelHelper.getLocalMTCNNFaceDetector(
                        mContext, mDetectorType, mDetectorModel, mMtcnnMinFaceSize, mMtcnnThreadsNum);
                break;
        }
        if (mFaceDetector != null) {
            mFaceDetector.load();
        }

        //mClassifier = ModelHelper.getLocalCaffeClassifier(mContext, mClassifierType, mClassifierModel,
        //        mCaffeClassifierNumberThreads, mCaffeClassifierScale, mCaffeClassifierMeanValues);
        //File f  = NetworkUtils.downloadModel(mContext, "http://imgserver.yunshitu.cn/app/sdk_face/conf_online/models/caffe_shuffle_net.zip", false);
        if (mClassifierModelURL != null) {
            // 下载模型
            File f  = NetworkUtils.downloadModel(mContext, mClassifierModelURL, false);
            if (f != null) {
                mClassifier = ModelHelper.getCaffeClassifier(
                        new File(f, DefaultConfig.CAFFE_PROTO_FILE),
                        new File(f, DefaultConfig.CAFFE_MODEL_FILE),
                        new File(f, DefaultConfig.CAFFE_MEAN_FILE),
                        new File(f, DefaultConfig.CAFFE_LABEL_FILE),
                        mCaffeClassifierNumberThreads,
                        mCaffeClassifierScale,
                        mCaffeClassifierMeanValues
                );
            } else {
                // 载入默认模型
                mClassifier = ModelHelper.getLocalCaffeClassifier(mContext,
                        DefaultConfig.DEFAULT_CLASSIFIER_TYPE,
                        DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_MODEL,
                        DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_NUM_THREADS,
                        DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_SCALE,
                        DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_MEAN_VALUE);
            }
        } else if (mClassifierType != null) {
            // 载入本地模型
            mClassifier = ModelHelper.getLocalCaffeClassifier(mContext, mClassifierType, mClassifierModel,
                    mCaffeClassifierNumberThreads, mCaffeClassifierScale, mCaffeClassifierMeanValues);
        }
        if (mClassifier != null) {
            mClassifier.load();
        }

        if (mFeaturerModelURL != null) {
            // 下载模型
            File f  = NetworkUtils.downloadModel(mContext, mFeaturerModelURL, false);
            if (f != null) {
                mFeaturer = ModelHelper.getCaffeFeaturer(
                        new File(f, DefaultConfig.CAFFE_PROTO_FILE),
                        new File(f, DefaultConfig.CAFFE_MODEL_FILE),
                        new File(f, DefaultConfig.CAFFE_MEAN_FILE),
                        mCaffeFeaturerNumberThreads,
                        mCaffeFeaturerScale,
                        mCaffeFeaturerMeanValues
                );
            } else {
                // 载入默认模型
                mFeaturer = ModelHelper.getLocalCaffeFeaturer(mContext,
                        DefaultConfig.DEFAULT_FEATURER_TYPE,
                        DefaultConfig.DEFAULT_CAFFE_FEATURER_MODEL,
                        DefaultConfig.DEFAULT_CAFFE_FEATURER_NUM_THREADS,
                        DefaultConfig.DEFAULT_CAFFE_FEATURER_SCALE,
                        DefaultConfig.DEFAULT_CAFFE_FEATURER_MEAN_VALUE);
            }
        } else if (mFeaturerType != null) {
            // 载入本地模型
            mFeaturer = ModelHelper.getLocalCaffeFeaturer(mContext, mFeaturerType, mFeaturerModel,
                    mCaffeFeaturerNumberThreads, mCaffeFeaturerScale, mCaffeFeaturerMeanValues);
        }
    }

    private boolean mIsInit;
    private final Object mLock = new byte[0];
    private Thread mInitThread;
    private boolean mIsDestroy = false;

    @Override
    public void initAsync() {
        synchronized (mLock) {
            if (!mIsDestroy) {
                (mInitThread = new Thread(() -> {
                    init();
                    synchronized (mLock) {
                        mIsInit = true;
                    }
                })).start();
            }
        }
    }

    @Override
    public boolean isInit() {
        synchronized (mLock) {
            return mIsInit;
        }
    }

    @Override
    public void waitForInit() {
        if (mInitThread != null) {
            try {
                mInitThread.join();
            } catch (InterruptedException ignored) { }
        }
    }

    @Override
    public FaceDetector getFaceDetector() {
        return mFaceDetector;
    }

    @Override
    public int getFaceDetectorWidth() {
        return mDetectorWidth;
    }

    @Override
    public Tracker getTracker() {
        Tracker tracker;
        switch (mTrackerType) {
            case "kcf": default:
                tracker = ModelHelper.getKCFTracker(mKCFHog, mKCFFixedWindow, mKCFMultiScale, mKCFLab);
                break;
            //case "dlib":
            //    tracker = ModelHelper.getLocalDlibTracker();
            //    break;
        }
        return tracker;
    }

    @Override
    public double getTrackerThreshold() {
        return mTrackerThreshold;
    }

    @Override
    public Classifier getClassifier() {
        return mClassifier;
    }

    @Override
    public Featurer getFeaturer() {
        return mFeaturer;
    }

    @Override
    public int getBufferedSize() {
        return mBufferedSize;
    }

    @Override
    public int getResizeWidth() {
        return mResizeWidth;
    }

    @Override
    public int getResizeHeight() {
        return mResizeHeight;
    }

    @Override
    public int getCropWidth() {
        return mCropWidth;
    }

    @Override
    public int getCropHeight() {
        return mCropHeight;
    }

    @Override
    public int getMaxNumOfFaces() {
        return mMaxNumOfFaces;
    }

    @Override
    public long getMillisecondPerFrame() {
        return mMillisecondPerFrame;
    }

    @Override
    public int getDetectPeriod() {
        return mDetectPeriod;
    }

    @Override
    public long getStartSleepTime() {
        return mStartSleepTime;
    }

    @Override
    public boolean isShowLandmarks() {
        return mIsShowLandmarks;
    }

    @Override
    public boolean isShowSmallFace() {
        return mIsShowSmallFace;
    }

    @Override
    public boolean isAutoMagnify() {
        return mIsAutoMagnify;
    }

    @Override
    public boolean isUpload() {
        return mIsUpload;
    }

    @Override
    public boolean isUseOnlineRec() { return mIsUseOnlineRec; }

    @Override
    public int getUploadInterval() {
        return mUploadInterval;
    }

    @Override
    public int getUploadWidth() {
        return mUploadWidth;
    }

    @Override
    public int getUploadHeight() {
        return mUploadHeight;
    }

    @Override
    public String getUploadFormat() {
        return mUploadFormat;
    }

    @Override
    public int getUploadQuality() {
        return mUploadQuality;
    }

    @Override
    public boolean isNeedUpdate() {
        return mIsNeedUpdate;
    }

    @Override
    public String getUpdateURL() {
        return mUpdateURL;
    }

    @Override
    public double getGeometryLivingError() {
        return mGeometryLivingError;
    }

    @Override
    public int getGeometryLivingDetectCount() {
        return mGeometryLivingDetectCount;
    }

    @Override
    public long getGeometryLivingDetectGap() {
        return mGeometryLivingDetectGap;
    }

    @Override
    public double getInfraredLivingError() {
        return mInfraredLivingError;
    }

    @Override
    public int getNumOfInputSources() {
        return mInputSourceTypes.size();
    }

    @Override
    public String getTypeOfInputSource(int index) {
        return index >= 0 && index < mInputSourceTypes.size() ? mInputSourceTypes.get(index) : null;
    }

    @Override
    public Map<String, Object> getParamsOfInputSource(int index) {
        return index >= 0 && index < mInputSourceParams.size() ? mInputSourceParams.get(index) : null;
    }

    private void loadModel(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONObject ref = json.optJSONObject("model-reference");
        loadWithRef(ref, json.optJSONObject("detector"), this::loadDetector);
        loadWithRef(ref, json.optJSONObject("tracker"), this::loadTracker);
        loadWithRef(ref, json.optJSONObject("classifier"), this::loadClassifier);
        loadWithRef(ref, json.optJSONObject("featurer"), this::loadFeaturer);
        loadAlign(json.optJSONObject("align"));
    }

    private interface LoadCallback {
        void load(JSONObject json);
    }

    private static void loadWithRef(JSONObject ref, JSONObject json, @NonNull LoadCallback callback) {
        if (json == null) {
            return;
        }
        String reference = json.optString("reference");
        if (reference != null && ref != null) {
            JSONObject r = ref.optJSONObject(reference);
            if (r != null) {
                callback.load(r);
            } else {
                callback.load(json);
            }
        } else {
            callback.load(json);
        }
    }

    private void loadInputSource(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray src = json.optJSONArray("src");
        JSONObject devices = json.optJSONObject("devices");
        if (devices != null) {
            for (int i = 0; i < src.length(); ++i) {
                String name = src.optString(i, null);
                if (name != null) {
                    JSONObject device = devices.optJSONObject(name);
                    if (device != null) {
                        String type = device.optString("type", "unknown");
                        mInputSourceTypes.add(type);
                        Map<String, Object> p = new HashMap<>();
                        JSONObject params = device.optJSONObject("params");
                        if (params != null) {
                            Iterator<String> it = params.keys();
                            while (it.hasNext()) {
                                String key = it.next();
                                p.put(key, params.opt(key));
                            }
                        }
                        mInputSourceParams.add(p);
                    }
                }
            }
        }
    }

    private void loadDetector(JSONObject json) {
        if (json == null) {
            return;
        }
        switch (json.optString("type", "")) {
            //case "dlib": default:
            //    loadDlibDetector(json);
            //    break;
            case "mtcnn": default:
                loadMtcnnDetector(json);
                break;
        }
        mDetectorWidth = json.optInt("width", DefaultConfig.DEFAULT_DETECTOR_WIDTH);
    }

    private void loadTracker(JSONObject json) {
        if (json == null) {
            return;
        }
        switch (json.optString("type", "")) {
            case "kcf": default:
                loadKCFTracker(json);
                break;
            //case "dlib":
            //    loadDLibTracker(json);
            //    break;
        }
    }

    private void loadClassifier(JSONObject json) {
        if (json == null) {
            return;
        }
        String type = json.optString("type", null);
        if (type == null) {
            mClassifierType = null;
            return;
        }
        switch (type) {
            case "caffe": default:
                loadCaffeClassifier(json);
                break;
        }
        mClassifierModelURL = json.optString("url", null);
    }

    private void loadFeaturer(JSONObject json) {
        if (json == null) {
            return;
        }
        String type = json.optString("type", null);
        if (type == null) {
            mFeaturerType = null;
            return;
        }
        switch (type) {
            case "caffe": default:
                loadCaffeFeaturer(json);
                break;
        }
        mFeaturerModelURL = json.optString("url", null);
    }

    private void loadAlign(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray resizeJSON = json.optJSONArray("resize");
        if (resizeJSON != null && resizeJSON.length() == 2) {
            mResizeWidth = resizeJSON.optInt(0, DefaultConfig.DEFAULT_RESIZE_WIDTH);
            mResizeHeight = resizeJSON.optInt(1, DefaultConfig.DEFAULT_RESIZE_HEIGHT);
        }

        JSONArray cropJSON = json.optJSONArray("crop");
        if (cropJSON != null && cropJSON.length() == 2) {
            mCropWidth = cropJSON.optInt(0, DefaultConfig.DEFAULT_CROP_WIDTH);
            mCropHeight = cropJSON.optInt(1, DefaultConfig.DEFAULT_CROP_HEIGHT);
        }
    }

    private void loadOnlineRec(JSONObject json){
        if (json == null) {
            return;
        }
        mIsUseOnlineRec = json.optBoolean("enable", DefaultConfig.DEFAULT_IS_USE_ONLINE_REC);
    }

    private void loadDisplay(JSONObject json) {
        if (json == null) {
            return;
        }
        mMaxNumOfFaces = json.optInt("max_faces", DefaultConfig.DEFAULT_MAX_FACES);
        mDetectPeriod = json.optInt("detect_period", DefaultConfig.DEFAULT_DETECT_PERIOD);
        mMillisecondPerFrame = json.optLong("millisecond_frame", DefaultConfig.DEFAULT_MILLISECOND_FRAME);
        mStartSleepTime = json.optLong("start_sleep_time", DefaultConfig.DEFAULT_START_SLEEP_TIME);
        mIsShowLandmarks = json.optBoolean("show_landmarks", DefaultConfig.DEFAULT_SHOW_LANDMARKS);
        mIsAutoMagnify = json.optBoolean("auto_magnify", DefaultConfig.DEFAULT_AUTO_MAGNIFY);
        mIsShowSmallFace = json.optBoolean("show_small_face", DefaultConfig.DEFAULT_SHOW_SMALL_FACE);
    }

    private void loadUpload(JSONObject json) {
        if (json == null) {
            return;
        }
        mIsUpload = json.optBoolean("enable", DefaultConfig.DEFAULT_IS_UPLOAD);
        mUploadFormat = json.optString("format", DefaultConfig.DEFAULT_UPLOAD_FORMAT);
        mUploadQuality = json.optInt("quality", DefaultConfig.DEFAULT_UPLOAD_QUALITY);
        mUploadInterval = json.optInt("interval", DefaultConfig.DEFAULT_UPLOAD_INTERVAL);
        JSONArray jsonArray = json.optJSONArray("size");
        if (jsonArray != null && jsonArray.length() >= 2) {
            mUploadWidth = jsonArray.optInt(0, DefaultConfig.DEFAULT_UPLOAD_WIDTH);
            mUploadHeight = jsonArray.optInt(1, DefaultConfig.DEFAULT_UPLOAD_HEIGHT);
        }
    }

    private void loadUpdate(JSONObject json) {
        if (json == null) {
            return;
        }
        mIsNeedUpdate = json.optBoolean("need", DefaultConfig.DEFAULT_NEED_UPDATE);
        mUpdateURL = json.optString("url", DefaultConfig.DEFAULT_UPDATE_URL);
    }

    private void loadGeometryLivingDetect(JSONObject json) {
        if (json == null) {
            return;
        }
        mGeometryLivingError = json.optDouble("living_error", DefaultConfig.DEFAULT_GEOMETRY_LIVING_ERROR);
        mGeometryLivingDetectGap = json.optLong("living_detect_gap", DefaultConfig.DEFAULT_GEOMETRY_LIVING_DETECT_GAP);
        mGeometryLivingDetectCount = json.optInt("living_detect_count", DefaultConfig.DEFAULT_GEOMETRY_LIVING_DETECT_COUNT);
    }

    private void loadInfraredLivingDetect(JSONObject json) {
        if (json == null) {
            return;
        }
        mInfraredLivingError = json.optDouble("living_error", DefaultConfig.DEFAULT_INFRARED_LIVING_ERROR);
    }

    private void loadDlibDetector(@NonNull JSONObject json) {
        mDetectorType = "dlib";
        mDetectorModel = json.optString("model", DefaultConfig.DEFAULT_DLIB_MODEL);
    }

    private void loadMtcnnDetector(@NonNull JSONObject json) {
        mDetectorType = "mtcnn";
        mDetectorModel = json.optString("model", DefaultConfig.DEFAULT_MTCNN_MODEL);
        JSONObject params = json.optJSONObject("params");
        if (params != null) {
            mMtcnnMinFaceSize = params.optInt("min_face_size", DefaultConfig.DEFAULT_MTCNN_MIN_FACE_SIZE);
            mMtcnnThreadsNum = params.optInt("threads_num", DefaultConfig.DEFAULT_MTCNN_THREADS_NUM);
        }
    }

    private void loadKCFTracker(@NonNull JSONObject json) {
        mTrackerType = "kcf";
        mTrackerThreshold = json.optDouble("threshold", DefaultConfig.DEFAULT_TRACKER_THRESHOLD);
        JSONObject params = json.optJSONObject("params");
        if (params != null) {
            mKCFHog = params.optBoolean("hog", DefaultConfig.DEFAULT_KCF_HOG);
            mKCFFixedWindow = params.optBoolean("fixed_window", DefaultConfig.DEFAULT_KCF_FIXED_WINDOW);
            mKCFMultiScale = params.optBoolean("multi_scale", DefaultConfig.DEFAULT_KCF_MULTISCALE);
            mKCFLab = params.optBoolean("lab", DefaultConfig.DEFAULT_KCF_LAB);
        }
    }

    private void loadDLibTracker(@NonNull JSONObject json) {
        mTrackerType = "dlib";
        mTrackerThreshold = json.optDouble("threshold", DefaultConfig.DEFAULT_TRACKER_THRESHOLD);
    }

    private void loadCaffeClassifier(@NonNull JSONObject json) {
        mClassifierType = "caffe";
        mClassifierModel = json.optString("model", DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_MODEL);
        JSONObject params = json.optJSONObject("params");
        if (params != null) {
            mCaffeClassifierNumberThreads = params.optInt("num_threads", DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_NUM_THREADS);
            mCaffeClassifierScale = (float) params.optDouble("scale", DefaultConfig.DEFAULT_CAFFE_CLASSIFIER_SCALE);

            JSONArray jsonArray = params.optJSONArray("mean_values");
            if (jsonArray != null) {
                mCaffeClassifierMeanValues = new float[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); ++i) {
                    mCaffeClassifierMeanValues[i] = (float) jsonArray.optDouble(i, 0.0);
                }
            }
        }
    }

    private void loadCaffeFeaturer(@NonNull JSONObject json) {
        mFeaturerType = "caffe";
        mFeaturerModel = json.optString("model", DefaultConfig.DEFAULT_CAFFE_FEATURER_MODEL);
        JSONObject params = json.optJSONObject("params");
        if (params != null) {
            mCaffeFeaturerNumberThreads = params.optInt("num_threads", DefaultConfig.DEFAULT_CAFFE_FEATURER_NUM_THREADS);
            mCaffeFeaturerScale = (float) params.optDouble("scale", DefaultConfig.DEFAULT_CAFFE_FEATURER_SCALE);

            JSONArray jsonArray = params.optJSONArray("mean_values");
            if (jsonArray != null) {
                mCaffeFeaturerMeanValues = new float[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); ++i) {
                    mCaffeFeaturerMeanValues[i] = (float) jsonArray.optDouble(i, 0.0);
                }
            }
        }
    }
}
