package com.niucong.punchcardserver.yunshitu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.niucong.yunshitu.util.FaceCollectData;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import cn.yunshitu.facesdk.face.FaceDetectProcessor;

public class FaceCollectActivity extends FaceDetectActivity {
    private int mCutFlag = 0;
    private ArrayList<Long> mFaceMatResult = new ArrayList<>();
    //public static final String RESULT_KEY = "faces";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FaceCollectData.getData().clear();
    }

    @Override
    protected void onDetectResultCallback(FaceDetectProcessor.DetectResult result, boolean skip) {
        if (result == null) {
            return;
        }
        super.drawResultOnTexture(result, skip);

        Iterator<Mat> it1 = result.getAlignResult().iterator();
        Iterator<UUID> it2 = result.getUUID().iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Mat mat = it1.next();
            UUID uuid = it2.next();

            if(mat != null) {
                if(mat.width() > 0 && mat.height() > 0)
                {
                    Bitmap flagmap = Bitmap.createBitmap(
                            mat.width(),
                            mat.height(),
                            Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat,flagmap);
                    if(mCutFlag < 30)
                    {
                        if(mCutFlag % 5 == 0) {
                            // todo 若能使Mat序列化，则可以直接传递，目前采有全局变量的方法传递数据
                            FaceCollectData.getData().add(mat.clone());
                        }
                        mCutFlag++;
                    }
                    else
                    {
                        // 开启添加头像的返回
                        Intent i = getIntent();
                        //i.putExtra(RESULT_KEY, mFaceMatResult);
                        setResult(0, i);
                        finish();
                    }
                }
            }
        }
    }
}
