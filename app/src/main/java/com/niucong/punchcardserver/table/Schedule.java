package com.niucong.punchcardserver.table;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name = "作息表")
public class Schedule {

    @SmartColumn(id = 1, name = "时段", autoMerge = true)
    private String timeRank;
    @SmartColumn(id = 2,name ="节次")
    private String sectionName;
    @SmartColumn(id = 3,name ="上课时间")
    private String time;

    public Schedule(String timeRank, String sectionName, String time) {
        this.timeRank = timeRank;
        this.sectionName = sectionName;
        this.time = time;
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
