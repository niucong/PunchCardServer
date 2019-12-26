package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 打卡记录
 */
public class SignDB extends DataSupport {

    private long id;// 唯一主键
    private int memberId;// 实验室人员Id
    private String name;// 实验室人员名称
    private int superId;// 上级id
    private long startTime;// 每天第一次打卡时间
    private long endTime;// 每天最后一次打卡时间

    private String objectId;// 服务端唯一主键

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
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
