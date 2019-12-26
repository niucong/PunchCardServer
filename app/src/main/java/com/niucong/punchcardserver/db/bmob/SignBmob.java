package com.niucong.punchcardserver.db.bmob;

import android.text.TextUtils;
import android.util.Log;

import com.niucong.punchcardserver.db.SignDB;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class SignBmob extends BmobObject {

    private long id;// 唯一主键
    private int memberId;// 实验室人员Id
    private String name;// 实验室人员名称
    private int superId;// 上级id
    private long startTime;// 每天第一次打卡时间
    private long endTime;// 每天最后一次打卡时间

    public void saveOrUpdate(SignDB db) {
        this.id = db.getId();
        this.memberId = db.getMemberId();
        this.name = db.getName();
        this.superId = db.getSuperId();
        this.startTime = db.getStartTime();
        this.endTime = db.getEndTime();
        if (TextUtils.isEmpty(db.getObjectId())) {
            save(new SaveListener<String>() {
                @Override
                public void done(String objectId, BmobException e) {
                    if (e == null) {
                        Log.d("MemberBmob", "添加数据成功，返回objectId为：" + objectId);
                        db.setObjectId(objectId);
                        db.update(db.getId());
                    } else {
                        Log.d("MemberBmob", "创建数据失败：" + e.getMessage());
                    }
                }
            });
        } else {
            update("6b6c11c537", new UpdateListener() {

                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        Log.d("MemberBmob", "更新成功:" + getUpdatedAt());
                    } else {
                        Log.d("MemberBmob", "更新失败：" + e.getMessage());
                    }
                }

            });
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSuperId() {
        return superId;
    }

    public void setSuperId(int superId) {
        this.superId = superId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
