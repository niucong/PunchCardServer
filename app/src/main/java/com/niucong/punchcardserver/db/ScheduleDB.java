package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 作息表
 */
public class ScheduleDB extends DataSupport {

    private int id;// 唯一主键

    private String timeRank;// 时段：上午、下午、晚上
    private String sectionName;// 节次
    private String time;// 上课时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimeRank() {
        return timeRank;
    }

    public void setTimeRank(String timeRank) {
        this.timeRank = timeRank;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
