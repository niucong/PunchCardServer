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
    private String members;// 项目关联者id、name
    private int creatorId;// 项目创建者Id
    private String creatorName;// 项目创建者name
    private long createTime;// 创建时间
    private long startTime;// 预计项目开始时间
    private long endTime;// 预计项目结束时间
    private int forceFinish;// 0正常、1被取消（开始之前可以取消）、2被终止（开始以后只能终止）
    private String cause;// 取消或终止原因
    private long closeTime;// 关闭时间

    private int superId;// 上级id
    private String superName;// 上级名称
    private int approveResult;// 审批结果：0待批复、1同意、2不同意
    private long approveTime;// 审批时间
    private String refuseCause;// 拒绝理由

    private String remark;// 备注-只能叠加不能修改
    private int status;// 0未开始、1设计中、2研发中、3测试中、4已完成
    private long startTimeReal;// 项目实际开始(设计)时间
    private long startTimeDevelop;// 项目研发开始时间
    private long startTimeTest;// 项目测试开始时间
    private long endTimeReal;// 项目实际开始时间

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

    public long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public int getSuperId() {
        return superId;
    }

    public void setSuperId(int superId) {
        this.superId = superId;
    }

    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public int getApproveResult() {
        return approveResult;
    }

    public void setApproveResult(int approveResult) {
        this.approveResult = approveResult;
    }

    public long getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(long approveTime) {
        this.approveTime = approveTime;
    }

    public String getRefuseCause() {
        return refuseCause;
    }

    public void setRefuseCause(String refuseCause) {
        this.refuseCause = refuseCause;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getStartTimeReal() {
        return startTimeReal;
    }

    public void setStartTimeReal(long startTimeReal) {
        this.startTimeReal = startTimeReal;
    }

    public long getStartTimeDevelop() {
        return startTimeDevelop;
    }

    public void setStartTimeDevelop(long startTimeDevelop) {
        this.startTimeDevelop = startTimeDevelop;
    }

    public long getStartTimeTest() {
        return startTimeTest;
    }

    public void setStartTimeTest(long startTimeTest) {
        this.startTimeTest = startTimeTest;
    }

    public long getEndTimeReal() {
        return endTimeReal;
    }

    public void setEndTimeReal(long endTimeReal) {
        this.endTimeReal = endTimeReal;
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
        dest.writeLong(this.closeTime);
        dest.writeInt(this.superId);
        dest.writeString(this.superName);
        dest.writeInt(this.approveResult);
        dest.writeLong(this.approveTime);
        dest.writeString(this.refuseCause);
        dest.writeString(this.remark);
        dest.writeInt(this.status);
        dest.writeLong(this.startTimeReal);
        dest.writeLong(this.startTimeDevelop);
        dest.writeLong(this.startTimeTest);
        dest.writeLong(this.endTimeReal);
    }

    public ProjectDB() {
    }

    protected ProjectDB(Parcel in) {
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
        this.closeTime = in.readLong();
        this.superId = in.readInt();
        this.superName = in.readString();
        this.approveResult = in.readInt();
        this.approveTime = in.readLong();
        this.refuseCause = in.readString();
        this.remark = in.readString();
        this.status = in.readInt();
        this.startTimeReal = in.readLong();
        this.startTimeDevelop = in.readLong();
        this.startTimeTest = in.readLong();
        this.endTimeReal = in.readLong();
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
