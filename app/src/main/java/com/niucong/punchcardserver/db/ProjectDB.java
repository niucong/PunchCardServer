package com.niucong.punchcardserver.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
 * 项目
 */
public class ProjectDB extends DataSupport implements Parcelable {

    private int id;// 唯一主键
    private String name;// 项目名称
    private int creatorId;// 项目创建者Id
    private String creatorName;// 项目创建者name
    private int superId;// 上级id
    private String members;// 项目关联者id、name
    private long createTime;// 创建时间
    private long startTime;// 项目开始时间——设计开始阶段
    private long endTime;// 项目结束时间——结束阶段
    private long devTime;// 项目研发开始阶段
    private long tstTime;// 项目测试开始阶段
    private int status;// 状态：0待批复、1已批复、2被驳回、3被取消（审批之前可以取消）、4被终止（审批以后只能终止）
    private String cause;// 取消、驳回或终止原因
    private long editTime;// 编辑时间

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

    public int getSuperId() {
        return superId;
    }

    public void setSuperId(int superId) {
        this.superId = superId;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
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

    public long getDevTime() {
        return devTime;
    }

    public void setDevTime(long devTime) {
        this.devTime = devTime;
    }

    public long getTstTime() {
        return tstTime;
    }

    public void setTstTime(long tstTime) {
        this.tstTime = tstTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.creatorId);
        dest.writeString(this.creatorName);
        dest.writeInt(this.superId);
        dest.writeString(this.members);
        dest.writeLong(this.createTime);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeLong(this.devTime);
        dest.writeLong(this.tstTime);
        dest.writeInt(this.status);
        dest.writeString(this.cause);
        dest.writeLong(this.editTime);
    }

    public ProjectDB() {
    }

    protected ProjectDB(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.creatorId = in.readInt();
        this.creatorName = in.readString();
        this.superId = in.readInt();
        this.members = in.readString();
        this.createTime = in.readLong();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.devTime = in.readLong();
        this.tstTime = in.readLong();
        this.status = in.readInt();
        this.cause = in.readString();
        this.editTime = in.readLong();
    }

    public static final Creator<ProjectDB> CREATOR = new Creator<ProjectDB>() {
        @Override
        public ProjectDB createFromParcel(Parcel source) {
            return new ProjectDB(source);
        }

        @Override
        public ProjectDB[] newArray(int size) {
            return new ProjectDB[size];
        }
    };
}
