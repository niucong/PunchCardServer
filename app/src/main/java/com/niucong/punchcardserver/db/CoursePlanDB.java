package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 课程计划
 */
public class CoursePlanDB extends DataSupport {

    private int id;// 唯一主键
    private int serverId;// 服务端生成的Id
    private String name;// 计划名称
    private int ownerId;// 计划归属者Id
    private int createrId;// 计划创建者Id
    private long createTime;// 创建时间
    private long startTime;// 计划开始时间
    private long endTime;// 计划结束时间
    private long lastEditTime;// 最后一次编辑时间
    private long lastPushTime;// 最后一次推送时间
    private int number;// 推送次数
    private boolean isSync;// 是否同步到手机

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getCreaterId() {
        return createrId;
    }

    public void setCreaterId(int createrId) {
        this.createrId = createrId;
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

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
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
}
