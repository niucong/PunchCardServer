package com.niucong.yunshitu.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.yunshitu.facesdk.face.FaceDetectProcessor;
import cn.yunshitu.facesdk.face.FaceDetector;

/**
 * Created by yunshitu on 2017/8/29.
 */

public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();

    private static final int MIN_WIDTH = 170;
    private static final int MIN_HEIGHT = 170;

    private static Bitmap mCanvasBitmap;

    public static void drawResult(
            @NonNull Bitmap bitmap,
            FaceDetectProcessor.DetectResult result,
            boolean showLandmarks,
            Paint[] outStrokePaints,
            Paint[] inStrokePaints,
            Paint[] fillPaints,
            TextPaint[] textPaints) {
        if (result == null) {
            return;
        }
        List<FaceDetector.FaceRet> retResult = result.getDetectResult();
        List<List<Map.Entry<String, List<Map.Entry<String, Double>>>>> clsResult = result.getClassifyResult();
        List<Point3> headResult = result.getHeadPoseResult();
        List<Boolean> livingResult = result.getGeometryLivingResult();
        if (result.getDetectResult() == null || clsResult == null || headResult == null || livingResult == null) {
            return;
        }

        if (outStrokePaints == null || outStrokePaints.length == 0) {
            outStrokePaints = new Paint[] {
                    new Paint()
            };
            outStrokePaints[0].setColor(Color.RED);
            outStrokePaints[0].setStyle(Paint.Style.STROKE);
            outStrokePaints[0].setStrokeWidth(2);
        }
        if (fillPaints == null || fillPaints.length == 0) {
            fillPaints[0].setColor(Color.RED);
            fillPaints = new Paint[] {
                    new Paint()
            };
            fillPaints[0].setStyle(Paint.Style.FILL);
        }
        if (textPaints == null || textPaints.length == 0) {
            textPaints = new TextPaint[] {
                    new TextPaint()
            };
            textPaints[0].setColor(Color.RED);
            textPaints[0].setAntiAlias(true);
            textPaints[0].setTextSize(26);
            textPaints[0].setShadowLayer(10f, 10f, 10f, Color.BLACK);
        }

        int outStrokeIdx = 0;
        int inStrokeIdx = 0;
        int fillIdx = 0;
        int textIdx = 0;

        Iterator<FaceDetector.FaceRet> retIt = retResult.iterator();
        Iterator<List<Map.Entry<String, List<Map.Entry<String, Double>>>>> clsIt = clsResult.iterator();
        Iterator<Boolean> livingIt = livingResult.iterator();
        Iterator<Point3> headIt = headResult.iterator();

        if (mCanvasBitmap == null || mCanvasBitmap.getWidth() != bitmap.getWidth() || mCanvasBitmap.getHeight() != bitmap.getHeight()) {
            if (mCanvasBitmap != null) {
                // 回收旧的Bitmap
                mCanvasBitmap.recycle();
            }
            // 新建Bitmap
            mCanvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        }

        // 透明图层画布
        Canvas canvas = new Canvas(mCanvasBitmap);
        // 清空图层
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Xfermode clearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        while (retIt.hasNext() && clsIt.hasNext() && livingIt.hasNext()) {
            FaceDetector.FaceRet rect = retIt.next();
            List<Map.Entry<String, List<Map.Entry<String, Double>>>> cls = clsIt.next();
            boolean isLiving = livingIt.next();
            Point3 head = null;
            if (headIt.hasNext()) {
                head = headIt.next();
            }

            Point tl = new Point(rect.getLeft() * bitmap.getWidth(), rect.getTop() * bitmap.getHeight());
            Point br = new Point(rect.getRight() * bitmap.getWidth(), rect.getBottom() * bitmap.getHeight());

            int fillPaintIdx = isLiving ? fillIdx : fillPaints.length - 1;
            int inPaintIdx = isLiving ? inStrokeIdx : inStrokePaints.length - 1;
            int outPaintIdx = isLiving ? outStrokeIdx : outStrokePaints.length - 1;
            int textPaintIdx = isLiving ? textIdx : textPaints.length - 1;

            // 画特征点
            if (showLandmarks && rect.getFaceLandmarks() != null) {
                for (Point p : rect.getFaceLandmarks()) {
                    final float radius = 3;
                    Point cp = new Point(p.x * bitmap.getWidth(), p.y * bitmap.getHeight());
                    canvas.drawCircle((float) cp.x - radius / 2, (float) cp.y - radius / 2, radius, outStrokePaints[outStrokeIdx]);
                }
            }

            float left = (float) tl.x;
            float top = (float) tl.y;
            float right = (float) br.x;
            float bottom = (float) br.y;

            // 画外框
            Paint strokePaint = new Paint(outStrokePaints[outPaintIdx]);
            // 清空着色器，便于画阴影
            strokePaint.setShader(null);
            canvas.drawRect(left, top, right, bottom, strokePaint);
            // 还原着色器
            strokePaint.setShader(outStrokePaints[outPaintIdx].getShader());
            // 删除阴影
            strokePaint.clearShadowLayer();
            // 将除阴影的部分擦除，目的是为了不影响后面的透明度效果
            strokePaint.setXfermode(clearXfermode);
            canvas.drawRect(left, top, right, bottom, strokePaint);
            // 画除阴影剩下的部分
            strokePaint.setXfermode(null);
            canvas.drawRect(left, top, right, bottom, strokePaint);


            float cornerWidth = outStrokePaints[outPaintIdx].getStrokeWidth() * 4f;
            float cornerLength = Math.min(right - left, bottom - top) * 0.15f;

            // 画内框
            strokePaint = new Paint(inStrokePaints[inPaintIdx]);

            strokePaint.setShader(null);
            canvas.drawRect(left + cornerWidth, top + cornerWidth, right - cornerWidth, bottom - cornerWidth, strokePaint);

            strokePaint.setShader(inStrokePaints[inPaintIdx].getShader());

            strokePaint.clearShadowLayer();

            strokePaint.setXfermode(clearXfermode);
            canvas.drawRect(left + cornerWidth, top + cornerWidth, right - cornerWidth, bottom - cornerWidth, strokePaint);

            strokePaint.setXfermode(null);
            canvas.drawRect(left + cornerWidth, top + cornerWidth, right - cornerWidth, bottom - cornerWidth, strokePaint);

            // 左上，右上，左下，右下
            float[] xs = new float[] {left, right, left, right};
            float[] ys = new float[] {top, top, bottom, bottom};

            Paint fillPaint = new Paint();

            for (int i = 0; i < 4; ++i) {
                // 多边形
                Path path = new Path();
                float startX, startY;
                startX = (i & 1) == 0 ? xs[i] - cornerWidth / 2 : xs[i] + cornerWidth / 2;
                startY = i < 2 ? ys[i] - cornerWidth / 2 : ys[i] + cornerWidth / 2;
                path.moveTo(startX, startY);
                path.rLineTo((i & 1) == 0 ? cornerLength : -cornerLength , 0);
                path.rLineTo(0, i < 2 ? cornerWidth : -cornerWidth);
                path.rLineTo((i & 1) == 0 ? -(cornerLength - cornerWidth) : cornerLength - cornerWidth, 0);
                path.rLineTo(0, i < 2 ? cornerLength - cornerWidth : -(cornerLength - cornerWidth));
                path.rLineTo((i & 1) == 0 ? -cornerWidth : cornerWidth, 0);
                path.close();

                // 同上的方法绘制
                fillPaint.set(fillPaints[fillPaintIdx]);
                fillPaint.setShader(null);
                canvas.drawPath(path, fillPaint);
                fillPaint.setShader(fillPaints[fillPaintIdx].getShader());
                fillPaint.clearShadowLayer();

                fillPaint.setXfermode(clearXfermode);
                canvas.drawPath(path, fillPaint);
                fillPaint.setXfermode(null);
                canvas.drawPath(path, fillPaint);
            }

            if (br.x - tl.x >= MIN_WIDTH && br.y - tl.y >= MIN_HEIGHT) {
                String clsStr = classifyToString(cls);
                if (clsStr != null) {
                    TextPaint textPaint = new TextPaint(textPaints[textPaintIdx]);
                    String[] lines = clsStr.split("\n");
                    Paint.FontMetrics fm = textPaint.getFontMetrics();
                    float height = fm.bottom - fm.top;
                    Rect textRect = new Rect();

                    float startX = left + cornerWidth;
                    float startY = bottom - cornerWidth - lines.length * 1.2f * height - fm.top;
                    for (String line : lines) {
                        textPaint.set(textPaints[textPaintIdx]);
                        textPaint.getTextBounds(line, 0, line.length(), textRect);
                        float width = (float) textRect.width();

                        // 文字背景
                        fillPaint.set(fillPaints[fillPaintIdx]);
                        Path path = new Path();
                        path.moveTo(startX, startY - height + fm.bottom);
                        path.rLineTo(width, 0);
                        path.rLineTo(height, height / 2);
                        path.rLineTo(-height, height / 2);
                        path.rLineTo(-width, 0);
                        path.close();

                        fillPaint.set(fillPaints[fillPaintIdx]);
                        fillPaint.setShader(null);
                        canvas.drawPath(path, fillPaint);
                        fillPaint.setShader(fillPaints[fillPaintIdx].getShader());
                        fillPaint.clearShadowLayer();

                        fillPaint.setXfermode(clearXfermode);
                        canvas.drawPath(path, fillPaint);
                        fillPaint.setXfermode(null);
                        canvas.drawPath(path, fillPaint);

                        // 文字
                        textPaint.setShader(null);
                        canvas.drawText(line, startX, startY, textPaint);

                        textPaint.setShader(textPaints[textPaintIdx].getShader());
                        textPaint.clearShadowLayer();

                        textPaint.setXfermode(clearXfermode);
                        canvas.drawText(line, startX, startY, textPaint);
                        textPaint.setXfermode(null);
                        canvas.drawText(line, startX, startY, textPaint);

                        startY += 1.2f * height;
                    }
                }
            }

            if (head != null) {
                float length = (right - left) * 0.2f;
                drawHeadPose(canvas, head.x, head.y, head.z, right - length, top + length, length * 0.9f - cornerWidth);
            }

            if (++outStrokeIdx >= outStrokePaints.length - 1) { outStrokeIdx = 0; }
            if (++inStrokeIdx >= inStrokePaints.length - 1) { inStrokeIdx = 0; }
            if (++fillIdx >= fillPaints.length - 1) { fillIdx = 0; }
            if (++textIdx >= textPaints.length - 1) { textIdx = 0; }
        }
        // 将图层画到图像上
        new Canvas(bitmap).drawBitmap(mCanvasBitmap, 0, 0, null);
    }

    public static void drawSmallFace(Bitmap bitmap, Collection<Mat> mats, int sp) {
        Bitmap tempBitmap = null;
        float width = Math.min(bitmap.getWidth(), bitmap.getHeight()) * 1.0f / sp;
        Canvas canvas = new Canvas(bitmap);
        int index = 0;
        for (Mat mat : mats) {
            if (mat == null || mat.width() == 0 || mat.height() == 0) continue;
            if (tempBitmap == null || tempBitmap.getWidth() != mat.width() || tempBitmap.getHeight() != mat.height()) {
                if (tempBitmap != null) {
                    tempBitmap.recycle();
                }
                tempBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            }
            Utils.matToBitmap(mat, tempBitmap);

            float scale = width / tempBitmap.getWidth();
            canvas.drawBitmap(tempBitmap, null, new RectF(
                    index * width,
                    bitmap.getHeight() - tempBitmap.getHeight() * scale,
                    index * width + width,
                    bitmap.getHeight()), null);
            ++index;
        }
    }

    private static String classifyToString(List<Map.Entry<String, List<Map.Entry<String, Double>>>> result) {
        if (result == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        boolean line = false;
        for (Map.Entry<String, List<Map.Entry<String, Double>>> features : result) {
            if (features.getKey().equals("quality")) continue;          // 怱略图像质量
            if (line) { builder.append("\n"); } else { line = true; }   // 最后一行不换行
            //builder.append(features.getKey()).append(": ");
            boolean dot = false;
            for (Map.Entry<String, Double> feature : features.getValue()) {
                if (dot) { builder.append("; "); } else { dot = true; }
                builder.append(feature.getKey());
                        //.append(" ")
                        //.append(String.format(Locale.CHINA, "%.2f%%", feature.getValue() * 100.0));
            }
        }
        return builder.toString();
    }

    private static void drawHeadPose(Canvas canvas, double pitch_, double yaw_, double roll_, float x, float y, float length) {
        double pitch = Math.toRadians(pitch_);
        double yaw = Math.toRadians(yaw_);
        double roll = Math.toRadians(roll_);

        Mat alpha = new MatOfDouble(Math.cos(roll), -Math.sin(roll), 0,
                                    Math.sin(roll), Math.cos(roll), 0,
                                    0, 0, 1).reshape(0, 3);
        Mat beta = new MatOfDouble(Math.cos(yaw), 0, Math.sin(yaw),
                                    0, 1, 0,
                                    -Math.sin(yaw), 0, Math.cos(yaw)).reshape(0, 3);
        Mat gamma = new MatOfDouble(1, 0, 0,
                                    0, Math.cos(pitch), -Math.sin(pitch),
                                    0, Math.sin(pitch), Math.cos(pitch)).reshape(0, 3);

        Mat r = new Mat();

        Core.gemm(alpha, beta, 1.0, Mat.zeros(alpha.size(), alpha.type()), 0.0, r);
        Core.gemm(r, gamma, 1.0, Mat.zeros(r.size(), r.type()), 0.0, r);

        List<MatOfDouble> mats = new ArrayList<>();
        mats.add(new MatOfDouble(length, 0, 0));
        mats.add(new MatOfDouble(0, -length, 0));
        mats.add(new MatOfDouble(0, 0, -length));

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        double[] data = new double[1];

        for (int i = 0; i < mats.size(); ++i) {
            Mat m = new Mat();
            Core.gemm(r, mats.get(i), 1.0, Mat.zeros(alpha.size(), alpha.type()), 0.0, m);
            paint.setColor(Color.argb(0xFF, i == 0 ? 0xFF : 0, i == 1 ? 0xFF : 0, i == 2 ? 0xFF : 0));
            m.get(0, 0, data);
            float ex = (float) data[0];
            m.get(1, 0, data);
            float ey = (float) data[0];
            canvas.drawLine(x, y, x + ex, y + ey, paint);
            m.release();
        }

        alpha.release();
        beta.release();
        gamma.release();
        r.release();
        for (Mat m : mats) {
            m.release();
        }
    }
}
