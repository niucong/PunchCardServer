package com.niucong.yunshitu.util;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于保存人脸采集产生的结果，目前先采用此方法
 */
public class FaceCollectData {
    private static List<Mat> data = new ArrayList<>();
    private FaceCollectData() { }
    public static List<Mat> getData() {
        return data;
    }
}
