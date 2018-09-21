package com.niucong.yunshitu.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.niucong.yunshitu.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by 刘文杰 on 2018/4/17.
 * 用于获取人脸GridView的适配工作
 */

public class FaceviewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{
    private Activity parentActivity;
    private List<Mat> mMatlist;

    // 采集ViewHolder，用于资源释放，by黄师恩
    private Collection<ViewHolder> mViewHolders = new ArrayList<>();


    public FaceviewAdapter(Activity parentActivity, List<Mat> Fimglist)
    {
        this.mMatlist = Fimglist;
        this.parentActivity = parentActivity;
    }

    @Override
    public int getCount() {
        return mMatlist.size();
    }

    @Override
    public Mat getItem(int position) {
        return this.mMatlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    //todo 对传递过来的内容进行判断，若遇到了imglist为空的情况需要进行判断
    // 2018-5-15: 添加ViewHolder，by黄师恩
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(parentActivity, R.layout.view_img, null);
            holder = new ViewHolder();
            holder.headview = convertView.findViewById(R.id.addhead);
            holder.deleteView = convertView.findViewById(R.id.delhead);
            convertView.setTag(holder);
            mViewHolders.add(holder);
        }

        // todo 此处可用AsyncTask优化，by黄师恩
        Mat mat = getItem(position);
        if (mat != null && mat.width() > 0 && mat.height() > 0) {
            if (holder.bitmap == null || holder.bitmap.getWidth() != mat.width() || holder.bitmap.getHeight() != mat.height()) {
                if (holder.bitmap != null) {
                    holder.bitmap.recycle();
                }
                holder.bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            }
            Utils.matToBitmap(mat, holder.bitmap);
            holder.headview.setImageBitmap(holder.bitmap);
        }
        //holder.headview.setImageBitmap(BitmapFactory.decodeFile((String) this.getItem(position)));


        return convertView;
    }

    // ViewHolder，用于减少Inflate造成的开销，by黄师恩
    private class ViewHolder {
        Bitmap bitmap;
        ImageView headview;
        ImageView deleteView;
    }

    // 回收所有Bitmap，by黄师恩
    public void recycleAll() {
        for (ViewHolder holder : mViewHolders) {
            if (holder.bitmap != null && !holder.bitmap.isRecycled()) {
                holder.bitmap.recycle();
            }
        }
        for (Mat mat : mMatlist) {
            if (mat != null) {
                mat.release();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}
