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
import com.niucong.punchcardserver.ProjectActivity;
import com.niucong.punchcardserver.R;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.ProjectDB;
import com.niucong.punchcardserver.util.ConstantUtil;

import java.util.Date;
import java.util.List;

public class ProjectAdapter extends BaseQuickAdapter<ProjectDB, BaseViewHolder> {

    private Context context;

    /**
     * @param layoutResId
     * @param dbs
     */
    public ProjectAdapter(Context context, int layoutResId, List<ProjectDB> dbs) {
        super(layoutResId, dbs);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final ProjectDB db) {
        final int position = helper.getLayoutPosition();
        helper.setText(R.id.item_project_num, (position + 1) + "");
        helper.setText(R.id.item_project_name, db.getName());
        helper.setText(R.id.item_project_creator, "创建者：" + db.getCreatorName());
        helper.setText(R.id.item_project_creattime, ConstantUtil.YMDHM.format(new Date(db.getCreateTime())));

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
            helper.setText(R.id.item_project_owners, "关联人员：" + names);
            helper.setGone(R.id.item_project_owners, true);
        } else {
            helper.setGone(R.id.item_project_owners, false);
        }

        if (db.getApproveResult() == 0) {
            if (db.getForceFinish() == 1) {
                setTextStutas(helper, "已取消", Color.argb(168, 255, 0, 0));
            } else {
                setTextStutas(helper, "待批复", Color.argb(168, 0, 0, 255));
            }
        } else if (db.getApproveResult() == 1) {
            if (db.getForceFinish() == 0) {
                if (db.getStatus() == 0) {
                    setTextStutas(helper, "未开始", Color.argb(168, 0, 0, 255));
                } else if (db.getStatus() == 1) {
                    setTextStutas(helper, "设计中", Color.argb(168, 0, 255, 0));
                } else if (db.getStatus() == 2) {
                    setTextStutas(helper, "开发中", Color.argb(168, 0, 255, 0));
                } else if (db.getStatus() == 3) {
                    setTextStutas(helper, "测试中", Color.argb(168, 0, 255, 0));
                } else {
                    setTextStutas(helper, "已结束", Color.argb(100, 0, 0, 0));
                }
            } else if (db.getForceFinish() == 1) {
                setTextStutas(helper, "已取消", Color.argb(168, 255, 0, 0));
            } else {
                setTextStutas(helper, "已终止", Color.argb(168, 255, 0, 0));
            }
        } else {
            setTextStutas(helper, "不同意", Color.argb(168, 255, 0, 0));
        }

        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).startActivityForResult(new Intent(context, ProjectActivity.class)
                        .putExtra("ProjectDB", db), 1);
            }
        });

    }

    private void setTextStutas(BaseViewHolder helper, String status, int Color) {
        helper.setText(R.id.item_project_status, status);
        helper.setTextColor(R.id.item_project_status, Color);
    }
}
