package com.niucong.yunshitu.config;

/**
 * 包含一些默认的配置信息
 */

public class DefaultConfig {

    static final String INIT_ENGING_KEY = "1466e439-6766-4ee9-9bce-84b18884f0fb";//初始化需要提供授权KEY,进行授权
    static final String CAFFE_MODEL_FILE = "train.caffemodel";
    static final String CAFFE_PROTO_FILE = "deploy.prototxt";
    static final String CAFFE_MEAN_FILE = "mean.binaryproto";
    static final String CAFFE_LABEL_FILE = "label.txt";

    static final String DLIB_MODEL_FILE = "shape_predictor_68_face_landmarks.dat";

    static final String TEMP_DIR = "modelTemp";

    static final String DEFAULT_DETECTOR = "mtcnn";
    static final String DEFAULT_DLIB_MODEL = "default";
    static final String DEFAULT_MTCNN_MODEL = "default";
    static final int DEFAULT_MTCNN_MIN_FACE_SIZE = 80;
    static final int DEFAULT_MTCNN_THREADS_NUM = 1;
    static final int DEFAULT_DETECTOR_WIDTH = 200;

    static final String DEFAULT_TRACKER = "kcf";
    static final double DEFAULT_TRACKER_THRESHOLD = 0.3;

    static final boolean DEFAULT_KCF_HOG = false;
    static final boolean DEFAULT_KCF_FIXED_WINDOW = true;
    static final boolean DEFAULT_KCF_MULTISCALE = true;
    static final boolean DEFAULT_KCF_LAB = false;

    static final String DEFAULT_CLASSIFIER_TYPE = null;
    static final String DEFAULT_CAFFE_CLASSIFIER_MODEL = "attr_shuffle_net";
    static final int DEFAULT_CAFFE_CLASSIFIER_NUM_THREADS = 1;
    static final float[] DEFAULT_CAFFE_CLASSIFIER_MEAN_VALUE = {0.0f};
    static final float DEFAULT_CAFFE_CLASSIFIER_SCALE = 0.017f;

    static final String DEFAULT_FEATURER_TYPE = "caffe";
    static final String DEFAULT_CAFFE_FEATURER_MODEL = "feature_resnet27_net";
    static final int DEFAULT_CAFFE_FEATURER_NUM_THREADS = 1;
    static final float[] DEFAULT_CAFFE_FEATURER_MEAN_VALUE = {0.0f};
    static final float DEFAULT_CAFFE_FEATURER_SCALE = 0.017f;

    static final int DEFAULT_BUFFERED_SIZE = 10;

    static final int DEFAULT_RESIZE_WIDTH = 144;
    static final int DEFAULT_RESIZE_HEIGHT = 144;
    static final int DEFAULT_CROP_WIDTH = 128;
    static final int DEFAULT_CROP_HEIGHT = 128;

    static final boolean DEFAULT_IS_USE_ONLINE_REC = false;

    static final int DEFAULT_MAX_FACES = 2;
    static final long DEFAULT_MILLISECOND_FRAME = 60;
    static final int DEFAULT_DETECT_PERIOD = 4;
    static final long DEFAULT_START_SLEEP_TIME = 2500;
    static final boolean DEFAULT_SHOW_LANDMARKS = true;
    static final int DEFAULT_PREVIEW_WIDTH = 1280;
    static final int DEFAULT_PREVIEW_HEIGHT = 720;
    static final boolean DEFAULT_AUTO_MAGNIFY = false;
    static final boolean DEFAULT_SHOW_SMALL_FACE = false;

    static final boolean DEFAULT_IS_UPLOAD = false;
    static final int DEFAULT_UPLOAD_INTERVAL = 1;
    static final int DEFAULT_UPLOAD_WIDTH = 144;
    static final int DEFAULT_UPLOAD_HEIGHT = 144;
    static final String DEFAULT_UPLOAD_FORMAT = "jpg";
    static final int DEFAULT_UPLOAD_QUALITY = 100;

    static final boolean DEFAULT_NEED_UPDATE = false;
    static final String DEFAULT_UPDATE_URL = "http://www.yunshitu.cn/";

    static final double DEFAULT_GEOMETRY_LIVING_ERROR = 0.025;
    static final long DEFAULT_GEOMETRY_LIVING_DETECT_GAP = 1000;
    static final int DEFAULT_GEOMETRY_LIVING_DETECT_COUNT = 2;

    static double DEFAULT_INFRARED_LIVING_ERROR = 0.5;
}
