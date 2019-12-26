/*
 * Copyright 2018 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.niucong.punchcardserver.andserver.controller;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.CalendarDB;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.PlanDB;
import com.niucong.punchcardserver.db.ProjectDB;
import com.niucong.punchcardserver.db.ScheduleDB;
import com.niucong.punchcardserver.db.SignDB;
import com.niucong.punchcardserver.db.VacateDB;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.util.MediaType;

import org.litepal.crud.DataSupport;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by YanZhenjie on 2018/6/9.
 */
@RestController
@RequestMapping(path = "/")
public class ApiController {

    /**
     * 登录接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = {"/login"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String login(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        List<String> names = request.getParameterNames();
        if (!names.contains("username") || !names.contains("password")) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求参数错误");
            return jsonObject.toString();
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "账号密码不能为空");
        } else {
            MemberDB memberDB = DataSupport.where("phone = ?", username).findFirst(MemberDB.class);
            if (memberDB == null) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "账号不存在");
            } else if (!password.equals(memberDB.getPassword())) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "密码错误");
            } else if (memberDB.getIsDelete() == 1) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "账号已失效");
            } else {
                jsonObject.put("code", 1);
                jsonObject.put("msg", "登录成功");
                jsonObject.put("memberId", memberDB.getId());
                jsonObject.put("type", memberDB.getType());

                if (names.contains("bmobID")) {
                    String bmobID = request.getParameter("bmobID");
                    if (!TextUtils.isEmpty(bmobID)) {
                        memberDB.setBmobID(bmobID);
                        memberDB.update(memberDB.getId());
                    }
                }
            }
        }

        return jsonObject.toString();
    }

    /**
     * 成员列表
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/memberList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String memberList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<MemberDB> list = DataSupport.order("id desc").
                where("id != ?", userId).find(MemberDB.class);
        JSONArray array = new JSONArray();
        for (MemberDB memberDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", memberDB.getId());
            json.put("name", memberDB.getName());
            json.put("type", memberDB.getType());
            json.put("superId", memberDB.getSuperId());
            json.put("number", memberDB.getNumber());
            json.put("phone", memberDB.getPhone());
            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");
        return jsonObject.toJSONString();
    }

    /**
     * 签到接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/sign", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String sign(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        try {
            SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
            long time = YMD.parse(YMD.format(new Date())).getTime();
            SignDB signDB = DataSupport.where("memberId = ? and startTime > ? and startTime < ?",
                    userId, time + "", time + 24 * 60 * 60 * 1000 + "").findFirst(SignDB.class);
            Log.d("ApiController", "sign startTime=" + YMD.format(new Date()));
            if (signDB == null) {
                signDB = new SignDB();
                signDB.setMemberId(Integer.valueOf(userId));
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                signDB.setName(memberDB.getName());
                signDB.setSuperId(memberDB.getSuperId());
                signDB.setStartTime(System.currentTimeMillis());
                signDB.save();
            } else {
                signDB.setEndTime(System.currentTimeMillis());
                signDB.update(signDB.getId());
            }
            Log.d("ApiController", "sign SuperId=" + signDB.getSuperId());

            jsonObject.put("startTime", signDB.getStartTime());
            jsonObject.put("endTime", signDB.getEndTime());
            jsonObject.put("code", 1);
            jsonObject.put("msg", "签到成功");
            App.app.mSpeechSynthesizer.speak(signDB.getName() + "打卡成功！");
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求参数错误");
        }
        return jsonObject.toJSONString();
    }

    /**
     * 签到列表
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/signList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String signList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        int offset = 0;
        int pageSize = 10;
        if (names.contains("offset")) {
            try {
                offset = Integer.valueOf(request.getParameter("offset"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起始参数错误");
                Log.d("ApiController", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        if (names.contains("pageSize")) {
            try {
                pageSize = Integer.valueOf(request.getParameter("pageSize"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求数量参数错误");
                Log.d("ApiController", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        String searchKey = "";
        if (names.contains("searchKey")) {
            try {
                searchKey = URLDecoder.decode(request.getParameter("searchKey"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("ApiController", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        long startTime = 0;
        long endTime = 0;
        if (names.contains("startTime") && names.contains("endTime")) {
            try {
                startTime = Long.valueOf(request.getParameter("startTime"));
                endTime = Long.valueOf(request.getParameter("endTime"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("ApiController", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
        int type = memberDB.getType();
        Log.d("ApiController", "userId=" + userId + ",type=" + type + ",searchKey=" + searchKey);
        List<SignDB> list;
        int allSize;
        if (type == 1) {
            if (TextUtils.isEmpty(searchKey)) {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where(
                            "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where(
                            "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(SignDB.class);
                } else {
                    list = DataSupport.order("id desc").offset(offset)
                            .limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.count(SignDB.class);
                }
            } else {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where("name = ? and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where("name = ? and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(SignDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .where("name = ?", searchKey).offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where("name = ?", searchKey).count(SignDB.class);
                }
            }
        } else if (type == 2) {
            if (TextUtils.isEmpty(searchKey)) {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where("(memberId = ? or superId = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where("(memberId = ? or superId = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(SignDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .where("memberId = ? or superId = ?", userId, userId).offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where("memberId = ? or superId = ?", userId, userId).count(SignDB.class);
                }
            } else {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where("(memberId = ? or superId = ? and name = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where("(memberId = ? or superId = ? and name = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime,
                            "" + endTime).count(SignDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .where("memberId = ? or superId = ? and name = ?", userId, userId, searchKey)
                            .offset(offset).limit(pageSize).find(SignDB.class);
                    allSize = DataSupport.where("memberId = ? or superId = ? and name = ?", userId, userId, searchKey).count(SignDB.class);
                }
            }
        } else {
            if (startTime != 0 && endTime != 0) {
                list = DataSupport.order("id desc").where("memberId = ? and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(SignDB.class);
                allSize = DataSupport.where("memberId = ? and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(SignDB.class);
            } else {
                list = DataSupport.order("id desc").where("memberId = ?", userId)
                        .offset(offset).limit(pageSize).find(SignDB.class);
                allSize = DataSupport.where("memberId = ?", userId).count(SignDB.class);
            }
        }
        JSONArray array = new JSONArray();
        for (SignDB signDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", signDB.getId());
            json.put("memberId", signDB.getMemberId());
            json.put("name", signDB.getName());
            json.put("superId", signDB.getSuperId());
            json.put("startTime", signDB.getStartTime());
            json.put("endTime", signDB.getEndTime());
            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("allSize", allSize);

        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");

        return jsonObject.toString();
    }

    /**
     * 请假批假接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/vacate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String vacate(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        long serverId = 0;
        if (names.contains("serverId")) {
            serverId = Long.valueOf(request.getParameter("serverId"));
        }
        Log.d("VacateHandler", "userId=" + userId + ",serverId=" + serverId);
        try {
            List<String> ids = new ArrayList<>();
            org.json.JSONObject object = new org.json.JSONObject();
            String bmobID;
            if (serverId == 0) {
                if (!names.contains("type") || !names.contains("start") || !names.contains("end")) {
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    Log.d("VacateHandler", jsonObject.toJSONString());
                    return jsonObject.toString();
                }
                int type = Integer.valueOf(request.getParameter("type"));
                long start = Long.valueOf(request.getParameter("start"));
                long end = Long.valueOf(request.getParameter("end"));
                VacateDB vacateDB = new VacateDB();
                vacateDB.setMemberId(Integer.valueOf(userId));
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                vacateDB.setName(memberDB.getName());
                vacateDB.setSuperId(memberDB.getSuperId());
                vacateDB.setType(type);
                vacateDB.setCause(URLDecoder.decode(request.getParameter("cause"), "utf-8"));
                vacateDB.setStartTime(start);
                vacateDB.setEndTime(end);
                vacateDB.setCreateTime(System.currentTimeMillis());
                vacateDB.setEditTime(vacateDB.getCreateTime());
                vacateDB.setApproveResult(0);
                vacateDB.save();

                jsonObject.put("code", 1);
                jsonObject.put("msg", "创建成功");

                bmobID = DataSupport.find(MemberDB.class, memberDB.getSuperId()).getBmobID();
                object.put("msg", memberDB.getName() + "请假了");
            } else {
                if (!names.contains("approveResult")) {
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    Log.d("VacateHandler", jsonObject.toJSONString());
                    return jsonObject.toString();
                }
                VacateDB vacateDB = DataSupport.find(VacateDB.class, serverId);
                vacateDB.setApproveResult(Integer.valueOf(request.getParameter("approveResult")));
                vacateDB.setRefuseCause(URLDecoder.decode(request.getParameter("refuseCause"), "utf-8"));
                vacateDB.setEditTime(System.currentTimeMillis());
                vacateDB.update(serverId);

                jsonObject.put("code", 1);
                jsonObject.put("msg", "批复成功");

                bmobID = DataSupport.find(MemberDB.class, vacateDB.getMemberId()).getBmobID();
                object.put("msg", "你有假条被处理了");
            }
            object.put("code", 0);
            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
            for (String id : ids) {
                Log.d("VacateHandler", "id=" + id);
            }
            App.addPush(ids, object);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求异常");
        }

        return jsonObject.toString();
    }

    /**
     * 请假批假列表接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/vacateList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String vacateList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        int offset = 0;
        int pageSize = 10;
        if (names.contains("offset")) {
            try {
                offset = Integer.valueOf(request.getParameter("offset"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起始参数错误");
                Log.d("SignListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        if (names.contains("pageSize")) {
            try {
                pageSize = Integer.valueOf(request.getParameter("pageSize"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求数量参数错误");
                Log.d("SignListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        String searchKey = "";
        if (names.contains("searchKey")) {
            try {
                searchKey = URLDecoder.decode(request.getParameter("searchKey"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        long startTime = 0;
        long endTime = 0;
        if (names.contains("startTime") && names.contains("endTime")) {
            try {
                startTime = Long.valueOf(request.getParameter("startTime"));
                endTime = Long.valueOf(request.getParameter("endTime"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
        int type = memberDB.getType();
        Log.d("SignListHandler", "userId=" + userId + ",type=" + type + ",searchKey=" + searchKey);
        List<VacateDB> list;
        int allSize;
        if (type == 1) {
            if (TextUtils.isEmpty(searchKey)) {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where(
                            "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where(
                            "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(VacateDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.count(VacateDB.class);
                }
            } else {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where("name = ? and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where("name = ? and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(VacateDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .where("name = ?", searchKey).offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where("name = ?", searchKey).count(VacateDB.class);
                }
            }
        } else if (type == 2) {
            if (TextUtils.isEmpty(searchKey)) {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where("(memberId = ? or superId = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where("(memberId = ? or superId = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(VacateDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .where("memberId = ? or superId = ?", userId, userId)
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where("memberId = ? or superId = ?", userId, userId).count(VacateDB.class);
                }
            } else {
                if (startTime != 0 && endTime != 0) {
                    list = DataSupport.order("id desc").where("(memberId = ? or superId = ? and name = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where("(memberId = ? or superId = ? and name = ?) and " +
                                    "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                            userId, userId, searchKey, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(VacateDB.class);
                } else {
                    list = DataSupport.order("id desc")
                            .where("memberId = ? or superId = ? and name = ?", userId, userId, searchKey)
                            .offset(offset).limit(pageSize).find(VacateDB.class);
                    allSize = DataSupport.where("memberId = ? or superId = ? and name = ?", userId, userId, searchKey).count(VacateDB.class);
                }
            }
        } else {
            if (startTime != 0 && endTime != 0) {
                list = DataSupport.order("id desc").where("memberId = ? and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(VacateDB.class);
                allSize = DataSupport.where("memberId = ? and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime).count(VacateDB.class);
            } else {
                list = DataSupport.order("id desc").where("memberId = ?", userId)
                        .offset(offset).limit(pageSize).find(VacateDB.class);
                allSize = DataSupport.where("memberId = ?", userId).count(VacateDB.class);
            }
        }
        JSONArray array = new JSONArray();
        for (VacateDB vacateDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", vacateDB.getId());
            json.put("memberId", vacateDB.getMemberId());
            json.put("name", vacateDB.getName());
            json.put("superId", vacateDB.getSuperId());
            json.put("type", vacateDB.getType());
            json.put("cause", vacateDB.getCause());
            json.put("createTime", vacateDB.getCreateTime());
            json.put("startTime", vacateDB.getStartTime());
            json.put("endTime", vacateDB.getEndTime());
            json.put("editTime", vacateDB.getEditTime());
            json.put("approveResult", vacateDB.getApproveResult());
            json.put("refuseCause", vacateDB.getRefuseCause());
            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("allSize", allSize);

        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");

        return jsonObject.toString();
    }

    /**
     * 计划接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/plan", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String plan(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        long serverId = 0;
        if (names.contains("serverId")) {
            serverId = Long.valueOf(request.getParameter("serverId"));
        }
        Log.d("VacateHandler", "userId=" + userId + ",serverId=" + serverId);
        try {
            List<String> ids = new ArrayList<>();
            org.json.JSONObject object = new org.json.JSONObject();
            PlanDB planDB;
            if (serverId == 0) {
                if (!names.contains("name") || !names.contains("start") || !names.contains("end")) {
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    Log.d("PlanHandler", jsonObject.toJSONString());
                    return jsonObject.toString();
                }

                String name = URLDecoder.decode(request.getParameter("name"), "utf-8");
                long start = Long.valueOf(request.getParameter("start"));
                long end = Long.valueOf(request.getParameter("end"));

                planDB = new PlanDB();
                planDB.setName(name);
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                planDB.setCreatorId(Integer.valueOf(userId));
                planDB.setCreatorName(memberDB.getName());
                if (names.contains("members")) {
                    planDB.setMembers(URLDecoder.decode(request.getParameter("members"), "utf-8"));
                }
                if (names.contains("cause")) {
                    planDB.setCause(URLDecoder.decode(request.getParameter("cause"), "utf-8"));
                }
                planDB.setStartTime(start);
                planDB.setEndTime(end);
                planDB.setCreateTime(System.currentTimeMillis());
//                planDB.setEditTime(planDB.getCreateTime());
                planDB.save();
            } else {
                if (!names.contains("forceFinish")) {
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    Log.d("PlanHandler", jsonObject.toJSONString());
                    return jsonObject.toString();
                }

                planDB = DataSupport.find(PlanDB.class, serverId);
                planDB.setForceFinish(Integer.valueOf(request.getParameter("forceFinish")));
                planDB.setCause(URLDecoder.decode(request.getParameter("cause"), "utf-8"));
                planDB.setEditTime(System.currentTimeMillis());
                planDB.update(serverId);
            }

            jsonObject.put("code", 1);
            jsonObject.put("msg", "操作成功");

            List<MemberDB> members = JSON.parseArray(planDB.getMembers(), MemberDB.class);
            for (MemberDB member : members) {
                String bmobID = DataSupport.find(MemberDB.class, member.getId()).getBmobID();
                if (!TextUtils.isEmpty(bmobID)) {
                    ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                }
            }
            if (ids.size() > 0) {
                for (String id : ids) {
                    Log.d("PlanHandler", "id=" + id);
                }
                if (planDB.getForceFinish() == 0) {
                    object.put("msg", planDB.getCreatorName() + "创建了 " + planDB.getName() + " 计划");
                } else if (planDB.getForceFinish() == 1) {
                    object.put("msg", planDB.getName() + " 计划被取消了");
                } else {
                    object.put("msg", planDB.getName() + " 计划被终止了");
                }
                object.put("code", 1);
                App.addPush(ids, object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求异常");
        }
        Log.d("PlanHandler", jsonObject.toJSONString());

        return jsonObject.toString();
    }

    /**
     * 计划列表接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/planList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String planList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        int offset = 0;
        int pageSize = 10;
        if (names.contains("offset")) {
            try {
                offset = Integer.valueOf(request.getParameter("offset"));
            } catch (NumberFormatException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起始参数错误");
                Log.d("SignListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        if (names.contains("pageSize")) {
            try {
                pageSize = Integer.valueOf(request.getParameter("pageSize"));
            } catch (NumberFormatException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求数量参数错误");
                Log.d("SignListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        String searchKey = "";
        if (names.contains("searchKey")) {
            try {
                searchKey = URLDecoder.decode(request.getParameter("searchKey"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        long startTime = 0;
        long endTime = 0;
        if (names.contains("startTime") && names.contains("endTime")) {
            try {
                startTime = Long.valueOf(request.getParameter("startTime"));
                endTime = Long.valueOf(request.getParameter("endTime"));
            } catch (NumberFormatException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        Log.d("SignListHandler", "userId=" + userId + ",searchKey=" + searchKey);
        List<PlanDB> list;
        int allSize;
        if (TextUtils.isEmpty(searchKey)) {
            if (startTime != 0 && endTime != 0) {
                list = DataSupport.order("id desc").where("(creatorId = ? or members like ?) and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "%:" + userId + ",%", "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(PlanDB.class);
                allSize = DataSupport.where("(creatorId = ? or members like ?) and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "%:" + userId + ",%", "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .count(PlanDB.class);
            } else {
                list = DataSupport.order("id desc")
                        .where("creatorId = ? or members like ?", userId, "%:" + userId + ",%")
                        .offset(offset).limit(pageSize).find(PlanDB.class);
                allSize = DataSupport.where("creatorId = ? or members like ?", userId, "%:" + userId + ",%").count(PlanDB.class);
            }
        } else {
            if (startTime != 0 && endTime != 0) {
                list = DataSupport.order("id desc").where("(creatorId = ? or members like ?) and (name like ?) and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "%:" + userId + ",%", "%" + searchKey + "%", "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(PlanDB.class);
                allSize = DataSupport.where("(creatorId = ? or members like ?) and (name like ?) and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "%:" + userId + ",%", "%" + searchKey + "%", "" + startTime, "" + startTime, "" + endTime, "" + endTime,
                        "" + startTime, "" + endTime).count(PlanDB.class);
            } else {
                list = DataSupport.order("id desc")
                        .where("(creatorId = ? or members like ?) and (name like ?)",
                                userId, "%:" + userId + ",%", "%" + searchKey + "%")
                        .offset(offset).limit(pageSize).find(PlanDB.class);
                allSize = DataSupport.where("(creatorId = ? or members like ?) and (name like ?)",
                        userId, "%:" + userId + ",%", "%" + searchKey + "%").count(PlanDB.class);
            }
        }
        JSONArray array = new JSONArray();
        for (PlanDB planDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", planDB.getId());
            json.put("name", planDB.getName());
            json.put("creatorId", planDB.getCreatorId());
            json.put("creatorName", planDB.getCreatorName());
            json.put("members", planDB.getMembers());
            json.put("cause", planDB.getCause());
            json.put("createTime", planDB.getCreateTime());
            json.put("startTime", planDB.getStartTime());
            json.put("endTime", planDB.getEndTime());
            json.put("editTime", planDB.getEditTime());
            json.put("forceFinish", planDB.getForceFinish());
            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("allSize", allSize);

        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");

        return jsonObject.toString();
    }

    /**
     * 项目接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/project", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String project(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        long serverId = 0;
        if (names.contains("serverId")) {
            serverId = Long.valueOf(request.getParameter("serverId"));
        }
        Log.d("VacateHandler", "userId=" + userId + ",serverId=" + serverId);
        try {
            List<String> ids = new ArrayList<>();
            org.json.JSONObject object = new org.json.JSONObject();
            ProjectDB projectDB;
            if (serverId == 0) {// 创建
                if (!names.contains("name") || !names.contains("start") || !names.contains("end")) {
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    Log.d("ProjectHandler", jsonObject.toJSONString());
                    return jsonObject.toString();
                }

                String name = URLDecoder.decode(request.getParameter("name"), "utf-8");
                long start = Long.valueOf(request.getParameter("start"));
                long end = Long.valueOf(request.getParameter("end"));

                projectDB = new ProjectDB();
                projectDB.setName(name);
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                projectDB.setCreatorId(Integer.valueOf(userId));
                projectDB.setCreatorName(memberDB.getName());
                projectDB.setSuperId(memberDB.getSuperId());
                projectDB.setSuperName(DataSupport.find(MemberDB.class, memberDB.getSuperId()).getName());
                if (names.contains("members")) {
                    projectDB.setMembers(URLDecoder.decode(request.getParameter("members"), "utf-8"));
                }
                if (names.contains("remark")) {
                    projectDB.setRemark(URLDecoder.decode(request.getParameter("remark"), "utf-8"));
                }
                projectDB.setStartTime(start);
                projectDB.setEndTime(end);
                projectDB.setCreateTime(System.currentTimeMillis());
                projectDB.save();

                object.put("msg", projectDB.getCreatorName() + " 创建了 " + projectDB.getName() + " 项目");
                String bmobID = DataSupport.find(MemberDB.class, memberDB.getSuperId()).getBmobID();
                ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
            } else {
                if (!names.contains("approveResult") && !names.contains("status")
                        && !names.contains("forceFinish")) {
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    Log.d("ProjectHandler", jsonObject.toJSONString());
                    return jsonObject.toString();
                } else {
                    projectDB = DataSupport.find(ProjectDB.class, serverId);
                    if (names.contains("approveResult")) {// 审批：0待批复、1同意、2不同意
                        projectDB.setApproveResult(Integer.valueOf(request.getParameter("approveResult")));
                        projectDB.setRefuseCause(URLDecoder.decode(request.getParameter("refuseCause"), "utf-8"));
                        projectDB.setApproveTime(System.currentTimeMillis());

                        if (projectDB.getApproveResult() == 1) {
                            object.put("msg", projectDB.getSuperName() + " 同意了" + projectDB.getCreatorName() + " 创建的 " + projectDB.getName() + " 项目");
                        } else {
                            if (names.contains("refuseCause")) {
                                projectDB.setRefuseCause(URLDecoder.decode(request.getParameter("refuseCause"), "utf-8"));
                            }
                            object.put("msg", projectDB.getSuperName() + " 拒绝了" + projectDB.getCreatorName() + "创建的 " + projectDB.getName() + " 项目");
                        }
                        String bmobID = DataSupport.find(MemberDB.class, projectDB.getSuperId()).getBmobID();
                        if (!TextUtils.isEmpty(bmobID)) {
                            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                        }
                    } else if (names.contains("status")) {// 状态变更
                        int status = Integer.valueOf(request.getParameter("status"));
                        projectDB.setStatus(status);
                        // 0未开始、1设计中、2研发中、3测试中、4已完成
                        if (status == 1) {
                            projectDB.setStartTimeReal(System.currentTimeMillis());
                            object.put("msg", projectDB.getName() + " 项目开始设计了");
                        } else if (status == 2) {
                            projectDB.setStartTimeDevelop(System.currentTimeMillis());
                            object.put("msg", projectDB.getName() + " 项目开始开发了");
                        } else if (status == 3) {
                            projectDB.setStartTimeTest(System.currentTimeMillis());
                            object.put("msg", projectDB.getName() + " 项目开始测试了");
                        } else if (status == 4) {
                            projectDB.setEndTimeReal(System.currentTimeMillis());
                            object.put("msg", projectDB.getName() + " 项目已经完成了");
                        }
                        if (names.contains("remark")) {
                            String remark = URLDecoder.decode(request.getParameter("remark"), "utf-8");
                            if (!TextUtils.isEmpty(remark)) {
                                if (!TextUtils.isEmpty(projectDB.getRemark())) {
                                    projectDB.setRemark(projectDB.getRemark() + "\n" + remark);
                                } else {
                                    projectDB.setRemark(remark);
                                }
                            }
                        }
                        String bmobID = DataSupport.find(MemberDB.class, projectDB.getSuperId()).getBmobID();
                        if (!TextUtils.isEmpty(bmobID)) {
                            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                        }
                    } else if (names.contains("forceFinish")) {// 关闭
                        projectDB.setForceFinish(Integer.valueOf(request.getParameter("forceFinish")));
                        if (names.contains("cause")) {
                            projectDB.setCause(URLDecoder.decode(request.getParameter("cause"), "utf-8"));
                        }
                        projectDB.setCloseTime(System.currentTimeMillis());
                        if (projectDB.getForceFinish() == 1) {
                            object.put("msg", projectDB.getName() + " 项目被取消了");
                        } else {
                            object.put("msg", projectDB.getName() + " 项目被终止了");
                        }
                        String bmobID = DataSupport.find(MemberDB.class, projectDB.getSuperId()).getBmobID();
                        if (!TextUtils.isEmpty(bmobID)) {
                            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                        }
                    }
                    projectDB.update(serverId);

                    // 项目被拒不用通知关联人
                    if (projectDB.getApproveResult() != 2) {
                        List<MemberDB> members = JSON.parseArray(projectDB.getMembers(), MemberDB.class);
                        for (MemberDB member : members) {
                            String bmobID = DataSupport.find(MemberDB.class, member.getId()).getBmobID();
                            if (!TextUtils.isEmpty(bmobID)) {
                                ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                            }
                        }
                    }
                }
            }
            object.put("code", 3);
            App.addPush(ids, object);

            jsonObject.put("code", 1);
            jsonObject.put("msg", "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求异常");
        }
        Log.d("PlanHandler", jsonObject.toJSONString());

        return jsonObject.toString();
    }

    /**
     * 计划列表接口
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/projectList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String projectList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        String userId = request.getHeader("userId");

        List<String> names = request.getParameterNames();
        int offset = 0;
        int pageSize = 10;
        if (names.contains("offset")) {
            try {
                offset = Integer.valueOf(request.getParameter("offset"));
            } catch (NumberFormatException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起始参数错误");
                Log.d("SignListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        if (names.contains("pageSize")) {
            try {
                pageSize = Integer.valueOf(request.getParameter("pageSize"));
            } catch (NumberFormatException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求数量参数错误");
                Log.d("SignListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }
        String searchKey = "";
        if (names.contains("searchKey")) {
            try {
                searchKey = URLDecoder.decode(request.getParameter("searchKey"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        long startTime = 0;
        long endTime = 0;
        if (names.contains("startTime") && names.contains("endTime")) {
            try {
                startTime = Long.valueOf(request.getParameter("startTime"));
                endTime = Long.valueOf(request.getParameter("endTime"));
            } catch (NumberFormatException e) {
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return jsonObject.toString();
            }
        }

        Log.d("SignListHandler", "userId=" + userId + ",searchKey=" + searchKey);
        List<ProjectDB> list;
        int allSize;
        if (TextUtils.isEmpty(searchKey)) {
            list = DataSupport.order("id desc")
                    .where("creatorId = ? or superId = ? or members like ?", userId, userId, "%:" + userId + ",%")
                    .offset(offset).limit(pageSize).find(ProjectDB.class);
            allSize = DataSupport.where("creatorId = ? or superId = ? or members like ?",
                    userId, userId, "%:" + userId + ",%").count(ProjectDB.class);
        } else {
            list = DataSupport.order("id desc")
                    .where("(creatorId = ? or superId = ? or members like ?) and (name like ?)",
                            userId, userId, "%:" + userId + ",%", "%" + searchKey + "%")
                    .offset(offset).limit(pageSize).find(ProjectDB.class);
            allSize = DataSupport.where("(creatorId = ? or superId = ? or members like ?) and (name like ?)",
                    userId, userId, "%:" + userId + ",%", "%" + searchKey + "%").count(ProjectDB.class);
        }
        JSONArray array = new JSONArray();
        for (ProjectDB projectDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", projectDB.getId());
            json.put("name", projectDB.getName());
            json.put("members", projectDB.getMembers());
            json.put("creatorId", projectDB.getCreatorId());
            json.put("creatorName", projectDB.getCreatorName());
            json.put("createTime", projectDB.getCreateTime());
            json.put("startTime", projectDB.getStartTime());
            json.put("endTime", projectDB.getEndTime());
            json.put("forceFinish", projectDB.getForceFinish());
            json.put("cause", projectDB.getCause());
            json.put("closeTime", projectDB.getCloseTime());

            json.put("superId", projectDB.getSuperId());
            json.put("superName", projectDB.getSuperName());
            json.put("approveResult", projectDB.getApproveResult());
            json.put("approveTime", projectDB.getApproveTime());
            json.put("refuseCause", projectDB.getRefuseCause());

            json.put("remark", projectDB.getRemark());
            json.put("status", projectDB.getStatus());
            json.put("approveResult", projectDB.getApproveResult());
            json.put("startTimeReal", projectDB.getStartTimeReal());
            json.put("startTimeDevelop", projectDB.getStartTimeDevelop());
            json.put("startTimeTest", projectDB.getStartTimeTest());
            json.put("endTimeReal", projectDB.getEndTimeReal());

            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("allSize", allSize);

        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");

        return jsonObject.toString();
    }

    /**
     * 查看作息表
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/scheduleList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String scheduleList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        List<ScheduleDB> list = DataSupport.findAll(ScheduleDB.class);
        JSONArray array = new JSONArray();
        for (ScheduleDB scheduleDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", scheduleDB.getId());
            json.put("timeRank", scheduleDB.getTimeRank());
            json.put("sectionName", scheduleDB.getSectionName());
            json.put("time", scheduleDB.getTime());
            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");
        Log.d("PlanHandler", jsonObject.toJSONString());

        return jsonObject.toString();
    }

    /**
     * 查看校历
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping(path = "/calendarList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String calendarList(HttpRequest request, HttpResponse response) {
        JSONObject jsonObject = new JSONObject();
        List<CalendarDB> list = DataSupport.findAll(CalendarDB.class);
        JSONArray array = new JSONArray();
        for (CalendarDB calendarDB : list) {
            JSONObject json = new JSONObject();
            json.put("id", calendarDB.getId());
            json.put("session", calendarDB.getSession());
            json.put("weekly", calendarDB.getWeekly());
            json.put("month", calendarDB.getMonth());
            json.put("monday", calendarDB.getMonday());
            json.put("tuesday", calendarDB.getTuesday());
            json.put("wednesday", calendarDB.getWednesday());
            json.put("thursday", calendarDB.getThursday());
            json.put("friday", calendarDB.getFriday());
            json.put("saturday", calendarDB.getSaturday());
            json.put("sunday", calendarDB.getSunday());
            array.add(json);
        }
        jsonObject.put("list", array);
        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");
        Log.d("PlanHandler", jsonObject.toJSONString());

        return jsonObject.toString();
    }
}