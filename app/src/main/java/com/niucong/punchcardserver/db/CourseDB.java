package com.niucong.punchcardserver.db;

import org.litepal.crud.DataSupport;

/**
 * 课程表
 */
public class CourseDB extends DataSupport {

    private long id;// 唯一主键

    private int memberId;//  人员Id
    private String memberName;// 人员名称
    private String courseName;// 课程名称

    private String startYear;// 学年开始
    private String endYear;// 学年结束
    private String sessionName;// 第一学期、寒假、第二学期、第三学期、暑假
    private int start;// 开始周
    private int end;// 结束周
    private int[] weekDays;// 周几
    private String[] sectionName;// 节次名称

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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(int[] weekDays) {
        this.weekDays = weekDays;
    }

    public String[] getSectionName() {
        return sectionName;
    }

    public void setSectionName(String[] sectionName) {
        this.sectionName = sectionName;
    }
}
