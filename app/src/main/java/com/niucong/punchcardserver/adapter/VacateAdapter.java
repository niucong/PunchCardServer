package com.niucong.punchcardserver.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.niucong.punchcardserver.R;
import com.niucong.punchcardserver.db.VacateDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VacateAdapter extends BaseQuickAdapter<VacateDB, BaseViewHolder> {

    SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    Context context;
    List<VacateDB> dbs;

    /**
     * @param layoutResId
     * @param dbs
     */
    public VacateAdapter(Context context, int layoutResId, List<VacateDB> dbs) {
        super(layoutResId, dbs);
        this.context = context;
        this.dbs = dbs;
    }

    @Override
    protected void convert(BaseViewHolder helper, final VacateDB db) {
        final int position = helper.getLayoutPosition();
        helper.setText(R.id.item_vacate_num, (position + 1) + "");
        helper.setText(R.id.item_vacate_name, db.getName());
        helper.setText(R.id.item_vacate_type, db.getType() == 1 ? "事假" : db.getType() == 2 ? "病假" :
                db.getType() == 3 ? "年假" : db.getType() == 4 ? "调休" : "其它");
        helper.setText(R.id.item_vacate_starttime, "开始时间：" + YMDHM.format(new Date(db.getStartTime())));
        helper.setText(R.id.item_vacate_endtime, "结束时间：" + YMDHM.format(new Date(db.getEndTime())));
        helper.setText(R.id.item_vacate_creattime, "申请时间：" + YMDHM.format(new Date(db.getCreateTime())));
        helper.setText(R.id.item_vacate_edittime, "批复时间：" + ((db.getEditTime() > 0 && db.getEditTime() != db.getCreateTime()) ?
                YMDHM.format(new Date(db.getEditTime())) : ""));

        if (db.getApproveResult() == 0) {
            setTextStutas(helper, "待批复", Color.argb(128, 0, 0, 255));
        } else if (db.getApproveResult() == 1) {
            helper.setText(R.id.item_vacate_status, "同意");
            setTextStutas(helper, "同意", Color.argb(128, 0, 255, 0));
        } else {
            setTextStutas(helper, "不同意", Color.argb(128, 255, 0, 0));
        }

        helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context).setTitle("提示")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setMessage("删除此假条？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbs.remove(position);
                                db.delete();
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton("取消", null).show();

                return false;
            }
        });
    }

    private void setTextStutas(BaseViewHolder helper, String status, int Color) {
        helper.setText(R.id.item_vacate_status, status);
        helper.setTextColor(R.id.item_vacate_status, Color);
    }
}
