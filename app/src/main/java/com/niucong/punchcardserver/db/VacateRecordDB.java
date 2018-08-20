package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 请假记录
 */
public class VacateRecordDB extends DataSupport {

    private int id;// 唯一主键
    private int ownerId;// 请假者Id
    private int type;// 请假类型：1事假、2病假、3年假、4调休、5其他（3、4、5只有老师有）
    private String cause;// 请假原因
    private long createTime;// 请假时间
    private long startTime;// 请假开始时间
    private long endTime;// 请假结束时间
    private boolean isUpPush;// 是否推送给上级
    private int approveResult;// 审批结果：1同意、2不同意
    private boolean isDownPush;// 是否推送反馈给下级
    private long lastPushTime;// 最后一次推送时间
    private int number;// 推送次数

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
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

    public boolean isUpPush() {
        return isUpPush;
    }

    public void setUpPush(boolean upPush) {
        isUpPush = upPush;
    }

    public int getApproveResult() {
        return approveResult;
    }

    public void setApproveResult(int approveResult) {
        this.approveResult = approveResult;
    }

    public boolean isDownPush() {
        return isDownPush;
    }

    public void setDownPush(boolean downPush) {
        isDownPush = downPush;
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
}
