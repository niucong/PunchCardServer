package com.niucong.yunshitu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by yunshitu on 2017/9/13.
 */

public class AutoFixTextureView extends TextureView {
    private int mPreviewWidth;
    private int mPreviewHeight;

    public AutoFixTextureView(Context context) {
        this(context, null);
    }

    public AutoFixTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFixTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //public AutoFixTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        //super(context, attrs, defStyleAttr, defStyleRes);
    //}

    public void setAspectRatio(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
    }

    public void drawBitmap(Bitmap bitmap) {
        Canvas canvas = lockCanvas();
        if (canvas != null && bitmap != null) {
            canvas.drawBitmap(bitmap,
                    new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                    new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
        }
        unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mPreviewWidth == 0 || mPreviewHeight == 0) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mPreviewWidth / mPreviewHeight) {
                setMeasuredDimension(width, width * mPreviewHeight / mPreviewWidth);
            } else {
                setMeasuredDimension(height * mPreviewWidth / mPreviewHeight, height);
            }
        }
    }
}
