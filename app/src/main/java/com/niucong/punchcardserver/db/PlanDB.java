package com.niucong.punchcardserver.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * 课程计划
 */
public class PlanDB extends DataSupport implements Parcelable {

    private int id;// 唯一主键
    private String name;// 计划名称
    private String members;// 计划关联者id、name
    private int creatorId;// 计划创建者Id
    private String creatorName;// 计划创建者name
    private long createTime;// 创建时间
    private long startTime;// 计划开始时间
    private long endTime;// 计划结束时间
    private int forceFinish;// 0正常、1被取消（开始之前可以取消）、2被终止（开始以后只能终止）
    private String cause;// 取消或终止原因
    private long editTime;// 编辑时间

    private long lastPushTime;// 最后一次推送时间
    private int number;// 推送次数
    private boolean isSync;// 是否同步到手机

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    public int getForceFinish() {
        return forceFinish;
    }

    public void setForceFinish(int forceFinish) {
        this.forceFinish = forceFinish;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public long getLastPushTime() {
        return lastPushTime;
    }

    public void setLastPushTime(long lastPushTime) {
        this.lastPushTime = lastPushTime;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.members);
        dest.writeInt(this.creatorId);
        dest.writeString(this.creatorName);
        dest.writeLong(this.createTime);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeInt(this.forceFinish);
        dest.writeString(this.cause);
        dest.writeLong(this.editTime);
        dest.writeLong(this.lastPushTime);
        dest.writeInt(this.number);
        dest.writeByte(this.isSync ? (byte) 1 : (byte) 0);
    }

    public PlanDB() {
    }

    protected PlanDB(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.members = in.readString();
        this.creatorId = in.readInt();
        this.creatorName = in.readString();
        this.createTime = in.readLong();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.forceFinish = in.readInt();
        this.cause = in.readString();
        this.editTime = in.readLong();
        this.lastPushTime = in.readLong();
        this.number = in.readInt();
        this.isSync = in.readByte() != 0;
    }

    public static final Parcelable.Creator<PlanDB> CREATOR = new Creator<PlanDB>() {
        @Override
        public PlanDB createFromParcel(Parcel source) {
            return new PlanDB(source);
        }

        @Override
        public PlanDB[] newArray(int size) {
            return new PlanDB[size];
        }
    };
}
