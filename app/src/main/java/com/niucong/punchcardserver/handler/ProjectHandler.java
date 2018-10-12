/*
 * Copyright © 2016 Yan Zhenjie.
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
package com.niucong.punchcardserver.handler;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.niucong.punchcardserver.app.App;
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.ProjectDB;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.Header;
import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目接口
 */
public class ProjectHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("ProjectHandler", "params=" + params.toString());
        JSONObject jsonObject = new JSONObject();

        String userId = "";
        for (Header header : request.getAllHeaders()) {
            if ("userId".equals(header.getName())) {
                userId = header.getValue();
                break;
            }
        }

        long serverId = 0;
        if (params.containsKey("serverId")) {
            serverId = Long.valueOf(params.get("serverId"));
        }
        Log.d("ProjectHandler", "userId=" + userId + ",serverId=" + serverId);
        try {
            List<String> ids = new ArrayList<>();
            org.json.JSONObject object = new org.json.JSONObject();
            ProjectDB projectDB;
            if (serverId == 0) {// 创建
                if (!params.containsKey("name") || !params.containsKey("start") || !params.containsKey("end")) {
                    response.setStatusCode(400);
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                    Log.d("ProjectHandler", jsonObject.toJSONString());
                    return;
                }

                String name = URLDecoder.decode(params.get("name"), "utf-8");
                long start = Long.valueOf(params.get("start"));
                long end = Long.valueOf(params.get("end"));

                projectDB = new ProjectDB();
                projectDB.setName(name);
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                projectDB.setCreatorId(Integer.valueOf(userId));
                projectDB.setCreatorName(memberDB.getName());
                projectDB.setSuperId(memberDB.getSuperId());
                projectDB.setSuperName(DataSupport.find(MemberDB.class, memberDB.getSuperId()).getName());
                if (params.containsKey("members")) {
                    projectDB.setMembers(URLDecoder.decode(params.get("members"), "utf-8"));
                }
                if (params.containsKey("cause")) {
                    projectDB.setCause(URLDecoder.decode(params.get("cause"), "utf-8"));
                }
                projectDB.setStartTime(start);
                projectDB.setEndTime(end);
                projectDB.setCreateTime(System.currentTimeMillis());
                projectDB.save();

                object.put("msg", projectDB.getCreatorName() + " 创建了 " + projectDB.getName() + " 项目");
                String bmobID = DataSupport.find(MemberDB.class, memberDB.getSuperId()).getBmobID();
                ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
            } else {
                if (!params.containsKey("approveResult") && !params.containsKey("status")
                        && !params.containsKey("forceFinish")) {
                    response.setStatusCode(400);
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                    Log.d("ProjectHandler", jsonObject.toJSONString());
                    return;
                } else {
                    projectDB = DataSupport.find(ProjectDB.class, serverId);
                    if (params.containsKey("approveResult")) {// 审批：0待批复、1同意、2不同意
                        projectDB.setApproveResult(Integer.valueOf(params.get("approveResult")));
                        projectDB.setRefuseCause(URLDecoder.decode(params.get("refuseCause"), "utf-8"));
                        projectDB.setApproveTime(System.currentTimeMillis());

                        if (projectDB.getApproveResult() == 1) {
                            object.put("msg", projectDB.getSuperName() + " 同意了" + projectDB.getCreatorName() + " 创建的 " + projectDB.getName() + " 项目");
                        } else {
                            object.put("msg", projectDB.getSuperName() + " 拒绝了" + projectDB.getCreatorName() + "创建的 " + projectDB.getName() + " 项目");
                        }
                        String bmobID = DataSupport.find(MemberDB.class, projectDB.getSuperId()).getBmobID();
                        if (!TextUtils.isEmpty(bmobID)) {
                            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                        }
                    } else if (params.containsKey("status")) {// 状态变更
                        int status = Integer.valueOf(params.get("status"));
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
                        String bmobID = DataSupport.find(MemberDB.class, projectDB.getSuperId()).getBmobID();
                        if (!TextUtils.isEmpty(bmobID)) {
                            ids.add(bmobID.substring(bmobID.indexOf("-") + 1));
                        }
                    } else if (params.containsKey("forceFinish")) {// 关闭
                        projectDB.setForceFinish(Integer.valueOf(params.get("forceFinish")));
                        projectDB.setCause(URLDecoder.decode(params.get("cause"), "utf-8"));
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
            response.setStatusCode(400);
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求异常");
        }
        Log.d("ProjectHandler", jsonObject.toJSONString());
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
    }
}
