package com.niucong.punchcardserver.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.niucong.punchcardserver.PlanActivity;
import com.niucong.punchcardserver.R;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.PlanDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PlanAdapter extends BaseQuickAdapter<PlanDB, BaseViewHolder> {

    private Context context;
    SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * @param layoutResId
     * @param dbs
     */
    public PlanAdapter(Context context, int layoutResId, List<PlanDB> dbs) {
        super(layoutResId, dbs);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final PlanDB db) {
        final int position = helper.getLayoutPosition();
        helper.setText(R.id.item_plan_num, (position + 1) + "");
        helper.setText(R.id.item_plan_name, db.getName());
        helper.setText(R.id.item_plan_creator, "创建者：" + db.getCreatorName());
        helper.setText(R.id.item_plan_creattime, "创建时间：" + YMDHM.format(new Date(db.getCreateTime())));
        helper.setText(R.id.item_plan_starttime, "开始时间：" + YMDHM.format(new Date(db.getStartTime())));
        helper.setText(R.id.item_plan_endtime, "结束时间：" + YMDHM.format(new Date(db.getEndTime())));

        String names = "";
        Log.d("PlanAdapter", "members=" + db.getMembers());
        try {
            List<MemberDB> members = JSON.parseArray(db.getMembers(), MemberDB.class);
            for (MemberDB member : members) {
                names += "，" + member.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("PlanAdapter", "names=" + names);
        if (names.length() > 0) {
            names = names.substring(1);
        }
        helper.setText(R.id.item_plan_owners, "关联人员：" + names);

        if (db.getForceFinish() == 0) {
            if (db.getStartTime() > System.currentTimeMillis()) {
                setTextStutas(helper, "未开始", Color.argb(168, 0, 0, 255));
            } else if (db.getEndTime() > System.currentTimeMillis()) {
                setTextStutas(helper, "进行中", Color.argb(168, 0, 255, 0));
            } else {
                setTextStutas(helper, "已结束", Color.argb(100, 0, 0, 0));
            }
        } else if (db.getForceFinish() == 1) {
            setTextStutas(helper, "已取消", Color.argb(168, 255, 0, 0));
        } else {
            setTextStutas(helper, "已终止", Color.argb(168, 255, 0, 0));
        }

        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).startActivity(new Intent(context, PlanActivity.class)
                        .putExtra("PlanDB", db));
            }
        });

    }

    private void setTextStutas(BaseViewHolder helper, String status, int Color) {
        helper.setText(R.id.item_plan_status, status);
        helper.setTextColor(R.id.item_plan_status, Color);
    }
}
