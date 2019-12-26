package com.niucong.punchcardserver.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * 实验室人员
 */
public class MemberDB extends DataSupport implements Parcelable {

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

    private String objectId;// 服务端唯一主键

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

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

    public MemberDB() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.number);
        dest.writeString(this.password);
        dest.writeString(this.name);
        dest.writeString(this.phone);
        dest.writeInt(this.type);
        dest.writeInt(this.superId);
        dest.writeString(this.bmobID);
        dest.writeString(this.faceId);
        dest.writeLong(this.lastEditTime);
        dest.writeInt(this.isDelete);
        dest.writeString(this.objectId);
    }

    protected MemberDB(Parcel in) {
        this.id = in.readInt();
        this.number = in.readString();
        this.password = in.readString();
        this.name = in.readString();
        this.phone = in.readString();
        this.type = in.readInt();
        this.superId = in.readInt();
        this.bmobID = in.readString();
        this.faceId = in.readString();
        this.lastEditTime = in.readLong();
        this.isDelete = in.readInt();
        this.objectId = in.readString();
    }

    public static final Creator<MemberDB> CREATOR = new Creator<MemberDB>() {
        @Override
        public MemberDB createFromParcel(Parcel source) {
            return new MemberDB(source);
        }

        @Override
        public MemberDB[] newArray(int size) {
            return new MemberDB[size];
        }
    };
}
