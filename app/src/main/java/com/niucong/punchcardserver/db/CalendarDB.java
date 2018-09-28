package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * 校历表
 */
public class CalendarDB extends DataSupport {

    private int id;// 唯一主键

    private Date day;// 某一天
    private int weekNumber;// 第几周
    private String sessionName;// 第一学期、寒假、第二学期、第三学期、暑假
    private String startYear;// 学年开始
    private String endYear;// 学年结束

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getStartYear() {
        return startYear;
    }

    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public void setEndYear(String endYear) {
        this.endYear = endYear;
    }
}
