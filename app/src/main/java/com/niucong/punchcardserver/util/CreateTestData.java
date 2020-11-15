package com.niucong.punchcardserver.util;

import android.util.Log;

import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.SignDB;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateTestData {

    /**
     * 生成测试人员
     */
    public static void createMembers() {
        for (int i = 0; i < 20; i++) {
            String phone = RandomValue.getTel();
            MemberDB db = new MemberDB();
            db.setName(RandomValue.getChineseName());
            db.setNumber((2020100 + i) + "");
            db.setPhone(RandomValue.getTel());
            db.setPassword("123");
            db.setType(3);
            db.setSuperId(3);
            db.setIsDelete(0);
            db.setIsTest(1);
            db.setLastEditTime(System.currentTimeMillis());
            MemberDB oldDb = DataSupport.where("phone = ?", phone).findFirst(MemberDB.class);
            if (oldDb == null) {
                db.setLastEditTime(System.currentTimeMillis());
                db.save();
            } else {
                oldDb.setName(db.getName());
                oldDb.setType(db.getType());
                oldDb.setNumber(db.getNumber());
                oldDb.setPhone(db.getPhone());
                oldDb.setPassword(db.getPassword());
                oldDb.setType(db.getType());
                oldDb.setSuperId(db.getSuperId());
                oldDb.setLastEditTime(System.currentTimeMillis());
                oldDb.setIsDelete(db.getIsDelete());
                oldDb.setIsTest(db.getIsTest());
                oldDb.update(oldDb.getId());
            }
        }
    }

    private static String[] dates = {"2020-09-01","2020-09-02","2020-09-03","2020-09-04","2020-09-05","2020-09-06","2020-09-07","2020-09-08","2020-09-09","2020-09-10",
            "2020-09-11","2020-09-12","2020-09-13","2020-09-14","2020-09-15","2020-09-16","2020-09-17","2020-09-18","2020-09-19","2020-09-20",
            "2020-09-21","2020-09-22","2020-09-23","2020-09-24","2020-09-25","2020-09-26","2020-09-27","2020-09-28","2020-09-29","2020-09-30",
            "2020-10-01","2020-10-02","2020-10-03","2020-10-04","2020-10-05","2020-10-06","2020-10-07","2020-10-08","2020-10-09","2020-10-10",
            "2020-10-11","2020-10-12","2020-10-13","2020-10-14","2020-10-15","2020-10-16","2020-10-17","2020-10-18","2020-10-19","2020-10-20",
            "2020-10-21","2020-10-22","2020-10-23","2020-10-24","2020-10-25","2020-10-26","2020-10-27","2020-10-28","2020-10-29","2020-10-30"};

    /**
     * 生成签到数据
     */
    public static void createSignData() {
        int MIN = 7*60*60;
//        int MID = 12*60*60;
        int MAX = 23*60*60;
        Random random = new Random();
        try {
            List<MemberDB> list = DataSupport.where("istest = ?", "1").find(MemberDB.class);
            for (String date : dates) {
                for (MemberDB memberDB : list) {
                    if (random.nextInt(100)>1) {
                        SignDB signDB = new SignDB();
                        signDB.setMemberId(memberDB.getId());
                        signDB.setName(memberDB.getName());
                        signDB.setSuperId(memberDB.getSuperId());
                        SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
                        long time = YMD.parse(date).getTime();
                        long time1 = time + (MIN + random.nextInt(MAX-MIN))*1000;
                        long time2 = time + (MIN + random.nextInt(MAX-MIN))*1000;
                        signDB.setStartTime(Math.min(time1, time2));
                        if (random.nextInt(100)>2) {
                            signDB.setEndTime(Math.max(time1,time2));
                        }
//                        long time1 = time + (MIN + random.nextInt(MID-MIN))*1000;
//                        long time2 = time + (MID + random.nextInt(MAX-MID))*1000;
//                        signDB.setStartTime(time1);
//                        if (random.nextInt(100)>2) {
//                            signDB.setEndTime(time2);
//                        }
                        signDB.setIsTest(1);
                        signDB.save();
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印每个时间段的人数和各个时长人数
     */
    public static void statisticsSigns(){
        List<SignDB> list = DataSupport.findAll(SignDB.class);
        Map<String,Integer> mapTimeFrame = new HashMap<>();
        for (int i = 7; i < 23; i++) {
            mapTimeFrame.put(i+"",0);
        }
        Map<String,Integer> mapDuration = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            mapDuration.put(i+"",0);
        }
        for (SignDB signDB : list) {
            if (signDB.getEndTime()>signDB.getStartTime()) {
                Date date1 = new Date(signDB.getStartTime());
                String key1 = date1.getHours() + "";
                int value1 = mapTimeFrame.get(key1);
                mapTimeFrame.put(key1,++value1);

                Date date2 = new Date(signDB.getEndTime());
                String key2 = date2.getHours() - date1.getHours() + "";
                int value2 = mapDuration.get(key2);
                mapDuration.put(key2,++value2);
            }
        }
        for (Map.Entry<String, Integer> entry : mapTimeFrame.entrySet()) {
            Log.d("CreateTestData","" + entry.getKey()+"点总人数：" + entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : mapDuration.entrySet()) {
            Log.d("CreateTestData","每日时长" + entry.getKey()+"个小时总人数：" + entry.getValue());
        }
    }
}
