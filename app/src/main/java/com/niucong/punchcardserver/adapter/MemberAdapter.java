package com.niucong.punchcardserver.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.niucong.punchcardserver.MemberActivity;
import com.niucong.punchcardserver.R;
import com.niucong.punchcardserver.db.MemberDB;

import java.util.List;

public class MemberAdapter extends BaseQuickAdapter<MemberDB, BaseViewHolder> {

    private Context context;

    /**
     * @param context
     * @param layoutResId
     * @param dbs
     */
    public MemberAdapter(Context context, int layoutResId, List<MemberDB> dbs) {
        super(layoutResId, dbs);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final MemberDB db) {
        final int position = helper.getLayoutPosition();
        helper.setText(R.id.item_member_num, (position + 1) + "");
        helper.setText(R.id.item_member_name, db.getName());
        helper.setText(R.id.item_member_type, db.getType() == 2 ? "老师" : "学生");
        helper.setText(R.id.item_member_number, db.getNumber());
        helper.setText(R.id.item_member_phone, db.getPhone());
        helper.setText(R.id.item_member_password, db.getPassword());
        helper.setText(R.id.item_member_mac, db.getMAC());
        helper.setText(R.id.item_member_status, 0 == db.getIsDelete() ? "正常" : "停用");

        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MemberActivity.class)
                        .putExtra("MemberDB", db));
            }
        });

        helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((Activity) context).startActivityForResult(new Intent(context, MemberActivity.class)
                        .putExtra("MemberDB", db).putExtra("position", position)
                        .putExtra("isEdit", true), 1);
                return false;
            }
        });
    }
}
