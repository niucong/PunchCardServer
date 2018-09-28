package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 作息表
 */
public class ScheduleDB extends DataSupport {

    private int id;// 唯一主键
    private int timeRank;// 时段：0上午、1下午、2晚上
    private String sectionName;// 节次名称：1-13、课间休息1-4
    private int type;// 节次类型：0上课时间、1课间休息
    private long startTime;// 开始时间
    private long endTime;// 结束时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimeRank() {
        return timeRank;
    }

    public void setTimeRank(int timeRank) {
        this.timeRank = timeRank;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
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
