package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 打卡记录
 */
public class SignRecordDB extends DataSupport {

    private int id;// 唯一主键
    private int serverId;// 服务端生成的Id
    private int memberId;// 实验室人员Id
    private long time;// 打卡时间

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

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
