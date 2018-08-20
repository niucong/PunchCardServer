package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 打卡记录
 */
public class SignRecordDB extends DataSupport {

    private long id;// 唯一主键
    private int memberId;// 实验室人员Id
    private String number;// 工号或者学号（工号4位、学号7位）
    private String name;// 实验室人员名称
    private int type;// 身份类型：1主任、2老师、3学生
    private long startTime;
    private long endTime;

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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
