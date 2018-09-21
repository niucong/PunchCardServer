package com.niucong.yunshitu.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.facesdk.FaceSDKNative;
import com.niucong.yunshitu.util.OSUtils;
import com.sh1r0.caffe_android_lib.CaffeMobile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cn.yunshitu.facesdk.face.Classifier;
import cn.yunshitu.facesdk.face.FaceDetector;
import cn.yunshitu.facesdk.face.Featurer;
import cn.yunshitu.facesdk.face.Tracker;
import cn.yunshitu.facesdk.face.impl.CaffeClassifier;
import cn.yunshitu.facesdk.face.impl.CaffeFeaturer;
import cn.yunshitu.facesdk.face.impl.KCFTracker;
import cn.yunshitu.facesdk.face.impl.MTCNNFaceDet;

//import com.tzutalin.dlib.FaceDet;

/**
 * Created by yunshitu on 17-12-21.
 */

public class ModelHelper {
    public static FaceDetector getLocalDlibFaceDetector(Context context, String type, String model) {
        //return getDlibFaceDetector(getModelFile(context, type + "_" + model, DefaultConfig.DLIB_MODEL_FILE));
        throw new UnsupportedOperationException("No Dlib lib");
    }

    /**
     * 通过本地assets获取MTCNN检测器
     * @param context
     * @param type
     * @param model
     * @param minFaceSize   最小人脸尺寸
     * @param threadsNum    线程数
     * @return
     */
    public static FaceDetector getLocalMTCNNFaceDetector(Context context, String type, String model,
                                                         int minFaceSize, int threadsNum) {
        return getMTCNNFaceDetector(getModelFiles(context, type + "_" + model), minFaceSize, threadsNum);
    }

    public static Tracker getLocalDlibTracker() {
        //return new DlibTracker();
        throw new UnsupportedOperationException("No Dlib lib");
    }

    /**
     * 通过本地assets获取Caffe分类器
     * @param context
     * @param type
     * @param model
     * @param numThreads    线程数
     * @param scale
     * @param meanValues
     * @return
     */
    public static Classifier getLocalCaffeClassifier(
            Context context, String type, String model, int numThreads, float scale, float[] meanValues) {
        return getCaffeClassifier(
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_PROTO_FILE),
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_MODEL_FILE),
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_MEAN_FILE),
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_LABEL_FILE),
                numThreads,
                scale,
                meanValues
        );
    }

    public static Featurer getLocalCaffeFeaturer(
            Context context, String type, String model, int numThreads, float scale, float[] meanValues) {
        return getCaffeFeaturer(
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_PROTO_FILE),
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_MODEL_FILE),
                getModelFile(context, type + "_" + model, DefaultConfig.CAFFE_MEAN_FILE),
                numThreads,
                scale,
                meanValues
        );
    }

    public static FaceDetector getDlibFaceDetector(File dlibFile) {
        //return new DlibFaceDet(new FaceDet(dlibFile.getAbsolutePath()));
        throw new UnsupportedOperationException("No Dlib lib");
    }

    /**
     *
     * @param mtcnnFile 检测的最小人脸设置 (480P miniFaceSize=80; 720P\1080 miniFaceSize=80-160)
     */
    public static FaceDetector getMTCNNFaceDetector(File mtcnnFile, int minFaceSize, int threadsNum) {
        FaceSDKNative faceSDKNative = new FaceSDKNative();
        boolean test = faceSDKNative.FaceDetectionModelInit(mtcnnFile.getAbsolutePath(),DefaultConfig.INIT_ENGING_KEY);
        Log.d("ModelHelper","test");
        faceSDKNative.SetMinFaceSize(minFaceSize);
        faceSDKNative.SetThreadsNumber(threadsNum);
        return new MTCNNFaceDet(faceSDKNative);
    }

    /**
     * 获取KCF实例
     * @param hog           HOG
     * @param fixedWindow   固定窗口
     * @param multiScale    多尺度
     * @param lab           LAB颜色空间
     * @return
     */
    public static Tracker getKCFTracker(boolean hog, boolean fixedWindow, boolean multiScale, boolean lab) {
        return new KCFTracker(hog, fixedWindow, multiScale, lab);
    }

    /**
     * 通过文件载入Caffe分类器实例
     * @param protoFile
     * @param modelFile
     * @param meanFile
     * @param labelFile
     * @param numThreads
     * @param scale
     * @param meanValues
     * @return
     */
    public static Classifier getCaffeClassifier(
            File protoFile, File modelFile, File meanFile,
            File labelFile, int numThreads, float scale, float[] meanValues) {

        Map.Entry<List<String>, List<List<Map.Entry<String, String>>>> labels;
        try {
            labels = loadLabel(new FileInputStream(labelFile));
        } catch (FileNotFoundException ignored) {
            List<String> labelNames = new ArrayList<>();
            List<List<Map.Entry<String, String>>> l = new ArrayList<>();
            labels = new AbstractMap.SimpleEntry<>(labelNames, l);
        }

        return new CaffeClassifier(
                loadCaffeMobile(protoFile, modelFile, meanFile, numThreads, scale, meanValues), labels.getKey(), labels.getValue());
    }

    /*public static Classifier getCaffeClassifier(
            File protoFile, File modelFile, File meanFile,
            Map.Entry<List<String>, List<List<Map.Entry<String, String>>>> labels, int numThreads, float scale, float[] meanValues) {
        CaffeMobile caffeMobile = new CaffeMobile();
        caffeMobile.setNumThreads(numThreads);
        caffeMobile.loadModel(
                protoFile.getAbsolutePath(),
                modelFile.getAbsolutePath());
        if (meanFile != null && meanFile.length() > 0) {
            caffeMobile.setMean(meanFile.getAbsolutePath());
        } else {
            caffeMobile.setMean(meanValues);
        }

        // 必须在载入后调用
        caffeMobile.setScale(scale);

        return new CaffeClassifier(caffeMobile, labels.getKey(), labels.getValue());
    }*/

    /**
     * 通过文件载入Caffe特征提取器实例
     * @param protoFile
     * @param modelFile
     * @param meanFile
     * @param numThreads
     * @param scale
     * @param meanValues
     * @return
     */
    public static Featurer getCaffeFeaturer(
            File protoFile, File modelFile, File meanFile,
            int numThreads, float scale, float[] meanValues) {

        return new CaffeFeaturer(loadCaffeMobile(protoFile, modelFile, meanFile, numThreads, scale, meanValues));
    }

    /**
     * 加载实际的Caffe对象
     */
    private static CaffeMobile loadCaffeMobile(File protoFile, File modelFile, File meanFile,
                                               int numThreads, float scale, float[] meanValues) {
        CaffeMobile caffeMobile = new CaffeMobile();
        caffeMobile.setNumThreads(numThreads);
        caffeMobile.loadModel(
                protoFile.getAbsolutePath(),
                modelFile.getAbsolutePath());
        if (meanFile != null && meanFile.length() > 0) {
            caffeMobile.setMean(meanFile.getAbsolutePath());
        } else {
            caffeMobile.setMean(meanValues);
        }

        // 必须在载入后调用
        caffeMobile.setScale(scale);
        return caffeMobile;
    }

    private static final int BUFFER_SIZE = 4096;

    private static File getModelFile(Context context, String fileDir, String modelFile) {
        File file = new File(OSUtils.getTempDir(context, DefaultConfig.TEMP_DIR), fileDir + modelFile);
        file.delete();
        AssetManager assets = context.getAssets();
        try (InputStream inputStream = new BufferedInputStream(assets.open(fileDir + File.separator + modelFile));
             OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int cnt;
            while ((cnt = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, cnt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private static File getModelFiles(Context context, String fileDir) {
        File file = new File(OSUtils.getTempDir(context, DefaultConfig.TEMP_DIR), fileDir);
        OSUtils.removeFiles(file);
        AssetManager assets = context.getAssets();
        copyAsserts(assets, fileDir, file);

        return file;
    }

    /**
     * 递归拷贝assets目录，以list(src)的长度来判断是否为文件，所以空目录产生的结果未知
     * mv src dst
     * @param asset
     * @param src   源
     * @param dst   目标
     */
    private static void copyAsserts(AssetManager asset, String src, File dst) {
        try {
            String[] ls = asset.list(src);
            if (ls.length > 0) {
                if (dst.isFile()) {
                    return;
                }
                dst.mkdirs();
                for (String file : ls) {
                    copyAsserts(asset, src + File.separator + file, new File(dst, file));
                }
            } else {
                try (InputStream in = asset.open(src); OutputStream out = new FileOutputStream(dst)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int cnt;
                    while ((cnt = in.read(buffer)) > 0) {
                        out.write(buffer, 0, cnt);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map.Entry<List<String>, List<List<Map.Entry<String, String>>>> loadLabel(InputStream in) {
        List<String> labelNames = new ArrayList<>();
        List<List<Map.Entry<String, String>>> labels = new ArrayList<>();

        try (Scanner sc = new Scanner(in)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] sp = line.split(";");
                try {
                    if (sp.length == 3) {
                        int num = Integer.valueOf(sp[0]);

                        labelNames.add(sp[2]);
                        List<Map.Entry<String, String>> label = new ArrayList<>();

                        while (--num >= 0 && sc.hasNextLine()) {
                            line = sc.nextLine();
                            sp = line.split(";");
                            if (sp.length == 2) {
                                label.add(new AbstractMap.SimpleEntry<>(sp[0], sp[1]));
                            }
                        }
                        labels.add(label);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(labelNames, labels);
    }

    /*private static Map.Entry<List<String>, List<List<Map.Entry<String, String>>>> loadLabel(Context context, String fileDir, String labelFile) {

        AssetManager am = context.getResources().getAssets();
        try (InputStream is = am.open(fileDir + "/" + labelFile)) {
            return loadLabel(is);
        } catch (IOException ignored) {
        }
        // 未将泛型定义成 (? extends)
        List<String> labelNames = new ArrayList<>();
        List<List<Map.Entry<String, String>>> labels = new ArrayList<>();
        return new AbstractMap.SimpleEntry<>(labelNames, labels);
    }*/
}
