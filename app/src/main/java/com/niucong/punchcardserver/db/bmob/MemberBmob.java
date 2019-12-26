package com.niucong.punchcardserver.db.bmob;

import android.text.TextUtils;
import android.util.Log;

import com.niucong.punchcardserver.db.MemberDB;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MemberBmob extends BmobObject {

    private int id;// 唯一主键
    private String number;// 工号或者学号（工号4位、学号7位）
    private String password;// 账号密码
    private String name;// 实验室人员名称
    private String phone;// 手机号
    private int type;// 身份类型：1主任、2老师、3学生
    private int superId;// 上级id
    private String bmobID;// 推送设备标识
    private String faceId;// 人脸标识
    private long lastEditTime;// 最后一次编辑时间
    private int isDelete;// 是否删除：0正常、1停用

    public void saveOrUpdate(MemberDB db) {
        this.id = db.getId();
        this.number = db.getNumber();
        this.password = db.getPassword();
        this.name = db.getName();
        this.phone = db.getPhone();
        this.type = db.getType();
        this.superId = db.getSuperId();
        this.bmobID = db.getBmobID();
        this.faceId = db.getFaceId();
        this.lastEditTime = db.getLastEditTime();
        this.isDelete = db.getIsDelete();
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

//    BmobQuery<MemberBmob> bmobQuery = new BmobQuery<MemberBmob>();
//        bmobQuery.addWhereEqualTo("number",number);
//        bmobQuery.findObjects(new FindListener<MemberBmob>()
//
//    {
//        @Override
//        public void done (List < MemberBmob > list, BmobException e){
//    }
//    });

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSuperId() {
        return superId;
    }

    public void setSuperId(int superId) {
        this.superId = superId;
    }

    public String getBmobID() {
        return bmobID;
    }

    public void setBmobID(String bmobID) {
        this.bmobID = bmobID;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }
}
