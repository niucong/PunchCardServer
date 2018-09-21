package com.niucong.yunshitu.config;

import java.util.Map;

import cn.yunshitu.facesdk.face.Classifier;
import cn.yunshitu.facesdk.face.FaceDetector;
import cn.yunshitu.facesdk.face.Featurer;
import cn.yunshitu.facesdk.face.Tracker;

/**
 * Created by yunshitu on 17-12-21.
 */

public interface Configuration {
    /**
     * 初始化
     */
    void init();

    /**
     * 异步初始化
     */
    void initAsync();

    /**
     * 是否载入完毕
     */
    boolean isInit();

    /**
     * 等待载入完毕
     */
    void waitForInit();

    /**
     * 获得人脸检测模块
     */
    FaceDetector getFaceDetector();

    /**
     * 获得人脸检测短边宽度
     */
    int getFaceDetectorWidth();

    /**
     *  获得追踪器
     */
    Tracker getTracker();

    /**
     * 获得追踪器阈值
     */
    double getTrackerThreshold();

    /**
     * 获得分类器
     */
    Classifier getClassifier();

    /**
     * 获得特征提取器
     */
    Featurer getFeaturer();

    /**
     * 获得缓冲区大小
     * @return 缓冲区大小
     */
    int getBufferedSize();

    /**
     * 获得人脸框缩放宽度
     */
    int getResizeWidth();

    /**
     * 获得人脸框缩放高度
     */
    int getResizeHeight();

    /**
     * 获得Crop宽度
     */
    int getCropWidth();

    /**
     * 获得Crop高度
     */
    int getCropHeight();

    /**
     * 获得最大人脸数
     */
    int getMaxNumOfFaces();

    /**
     * 获得摄像头采集速度
     * @return 每多少毫秒采集一帧
     */
    long getMillisecondPerFrame();

    /**
     * 获得人脸检测周期
     * @return 每多少次有效帧进行一次人脸检测
     */
    int getDetectPeriod();

    /**
     * 获得等待页面等待时长
     * @return 毫秒
     */
    long getStartSleepTime();

    /**
     * 是否显示关键点
     * @return 是否显示关键点
     */
    boolean isShowLandmarks();

    /**
     * 是否显示人脸
     * @return 是否显示人脸
     */
    boolean isShowSmallFace();

    /**
     * 是否自动缩放
     * @return
     */
    boolean isAutoMagnify();

    /**
     * 是否Upload
     * @return bool
     */
    boolean isUpload();

    /**
     * 是否使用在线识别
     * @return bool
     */
    boolean isUseOnlineRec();

    /**
     * 获得Upload间隔
     * @return int
     */
    int getUploadInterval();

    /**
     * Upload宽度
     * @return int
     */
    int getUploadWidth();

    /**
     * Upload高度
     * @return int
     */
    int getUploadHeight();

    /**
     * Upload格式
     * @return string
     */
    String getUploadFormat();

    /**
     * Upload质量
     * @return int
     */
    int getUploadQuality();

    /**
     * 是否需要更新
     * @return 是否需要更新
     */
    boolean isNeedUpdate();

    /**
     * 更新页面URL
     * @return URL
     */
    String getUpdateURL();

    /**
     * 几何活体检测阈值
     * @return 阈值,double
     */
    double getGeometryLivingError();

    /**
     * 几何平面活体检测通过累计数量
     * @return int
     */
    int getGeometryLivingDetectCount();

    /**
     * 几何活体检测两帧间隔
     * @return 时间
     */
    long getGeometryLivingDetectGap();

    /**
     * 红外活体检测阈值
     * @return 阈值,double
     */
    double getInfraredLivingError();

    /**
     * 获得输入设备个数
     * @return 个数
     */
    int getNumOfInputSources();

    /**
     * 获得输入设备类型
     * @param index 下标
     * @return      类型
     */
    String getTypeOfInputSource(int index);

    /**
     * 获得输入设备参数
     * @param index 下标
     * @return      参数
     */
    Map<String, Object> getParamsOfInputSource(int index);
}
