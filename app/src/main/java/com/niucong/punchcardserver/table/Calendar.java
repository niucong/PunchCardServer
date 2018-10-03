package com.niucong.punchcardserver.table;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name = "校历")
public class Calendar {

    @SmartColumn(id = 1, name = "学期", autoMerge = true, width = 30)
    private String session;
    @SmartColumn(id = 2, name = "周数", autoMerge = true)
    private String weekly;
    @SmartColumn(id = 3, name = "月份", autoMerge = true)
    private String month;
    @SmartColumn(id = 4, name = "一")
    private String monday;
    @SmartColumn(id = 5, name = "二")
    private String tuesday;
    @SmartColumn(id = 6, name = "三")
    private String wednesday;
    @SmartColumn(id = 7, name = "四")
    private String thursday;
    @SmartColumn(id = 8, name = "五")
    private String friday;
    @SmartColumn(id = 9, name = "六")
    private String saturday;
    @SmartColumn(id = 10, name = "日")
    private String sunday;

    public Calendar(String session, String weekly, String month, String monday, String tuesday,
                    String wednesday, String thursday, String friday, String saturday, String sunday) {

        this.session = session;
        this.weekly = weekly;
        this.month = month;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getWeekly() {
        return weekly;
    }

    public void setWeekly(String weekly) {
        this.weekly = weekly;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMonday() {
        return monday;
    }

    public void setMonday(String monday) {
        this.monday = monday;
    }

    public String getTuesday() {
        return tuesday;
    }

    public void setTuesday(String tuesday) {
        this.tuesday = tuesday;
    }

    public String getWednesday() {
        return wednesday;
    }

    public void setWednesday(String wednesday) {
        this.wednesday = wednesday;
    }

    public String getThursday() {
        return thursday;
    }

    public void setThursday(String thursday) {
        this.thursday = thursday;
    }

    public String getFriday() {
        return friday;
    }

    public void setFriday(String friday) {
        this.friday = friday;
    }

    public String getSaturday() {
        return saturday;
    }

    public void setSaturday(String saturday) {
        this.saturday = saturday;
    }

    public String getSunday() {
        return sunday;
    }

    public void setSunday(String sunday) {
        this.sunday = sunday;
    }
}
