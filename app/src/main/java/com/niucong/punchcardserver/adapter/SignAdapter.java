package com.niucong.punchcardserver.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.niucong.punchcardserver.R;
import com.niucong.punchcardserver.db.SignDB;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SignAdapter extends BaseQuickAdapter<SignDB, BaseViewHolder> {

    SimpleDateFormat YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @param layoutResId
     * @param dbs
     */
    public SignAdapter(int layoutResId, List<SignDB> dbs) {
        super(layoutResId, dbs);
    }

    @Override
    protected void convert(BaseViewHolder helper, final SignDB db) {
        final int position = helper.getLayoutPosition();
        helper.setText(R.id.item_sign_num, (position + 1) + "");
        helper.setText(R.id.item_sign_name, db.getName());
        helper.setText(R.id.item_sign_starttime, YMDHMS.format(new Date(db.getStartTime())));
        helper.setText(R.id.item_sign_endtime, db.getEndTime() > 0 ? YMDHMS.format(new Date(db.getEndTime())) : "-");
    }
}
