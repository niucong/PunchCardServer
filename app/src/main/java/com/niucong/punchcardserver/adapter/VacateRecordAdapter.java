package com.niucong.punchcardserver.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.niucong.punchcardserver.R;
import com.niucong.punchcardserver.db.VacateRecordDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VacateRecordAdapter extends BaseQuickAdapter<VacateRecordDB, BaseViewHolder> {

    SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * @param layoutResId
     * @param dbs
     */
    public VacateRecordAdapter(int layoutResId, List<VacateRecordDB> dbs) {
        super(layoutResId, dbs);
    }

    @Override
    protected void convert(BaseViewHolder helper, final VacateRecordDB db) {
        final int position = helper.getLayoutPosition();
        helper.setText(R.id.item_vacate_num, (position + 1) + "");
        helper.setText(R.id.item_vacate_name, db.getName());
        helper.setText(R.id.item_vacate_type, db.getType() == 1 ? "事假" : db.getType() == 2 ? "病假" :
                db.getType() == 3 ? "年假" : db.getType() == 4 ? "调休" : "其它");
        helper.setText(R.id.item_vacate_starttime, YMDHM.format(new Date(db.getStartTime())));
        helper.setText(R.id.item_vacate_endtime, YMDHM.format(new Date(db.getEndTime())));
        helper.setText(R.id.item_vacate_creattime, YMDHM.format(new Date(db.getCreateTime())));
        helper.setText(R.id.item_vacate_edittime, db.getEditTime() > 0 ? YMDHM.format(new Date(db.getEditTime())) : "");

        if (db.getApproveResult() == 0) {
            setTextStutas(helper, "待批复", Color.argb(128, 0, 0, 255));
        } else if (db.getApproveResult() == 1) {
            helper.setText(R.id.item_vacate_status, "同意");
            setTextStutas(helper, "同意", Color.argb(128, 0, 255, 0));
        } else {
            setTextStutas(helper, "不同意", Color.argb(128, 255, 0, 0));
        }
    }

    private void setTextStutas(BaseViewHolder helper, String status, int Color) {
        helper.setText(R.id.item_vacate_status, status);
        helper.setTextColor(R.id.item_vacate_status, Color);
    }
}
