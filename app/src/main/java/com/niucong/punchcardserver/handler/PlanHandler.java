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
import com.niucong.punchcardserver.db.PlanDB;
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
 * 计划接口
 */
public class PlanHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("PlanHandler", "params=" + params.toString());
        JSONObject jsonObject = new JSONObject();

        String userId = "";
        for (Header header : request.getAllHeaders()) {
            if ("userId".equals(header.getName()) || "userid".equals(header.getName())) {
                userId = header.getValue();
                break;
            }
        }

        long serverId = 0;
        if (params.containsKey("serverId")) {
            serverId = Long.valueOf(params.get("serverId"));
        }
        Log.d("PlanHandler", "userId=" + userId + ",serverId=" + serverId);
        try {
            List<String> ids = new ArrayList<>();
            org.json.JSONObject object = new org.json.JSONObject();
            PlanDB planDB;
            if (serverId == 0) {
                if (!params.containsKey("name") || !params.containsKey("start") || !params.containsKey("end")) {
                    response.setStatusCode(400);
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                    Log.d("PlanHandler", jsonObject.toJSONString());
                    return;
                }

                String name = URLDecoder.decode(params.get("name"), "utf-8");
                long start = Long.valueOf(params.get("start"));
                long end = Long.valueOf(params.get("end"));

                planDB = new PlanDB();
                planDB.setName(name);
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                planDB.setCreatorId(Integer.valueOf(userId));
                planDB.setCreatorName(memberDB.getName());
                if (params.containsKey("members")) {
                    planDB.setMembers(URLDecoder.decode(params.get("members"), "utf-8"));
                }
                if (params.containsKey("cause")) {
                    planDB.setCause(URLDecoder.decode(params.get("cause"), "utf-8"));
                }
                planDB.setStartTime(start);
                planDB.setEndTime(end);
                planDB.setCreateTime(System.currentTimeMillis());
//                planDB.setEditTime(planDB.getCreateTime());
                planDB.save();
            } else {
                if (!params.containsKey("forceFinish")) {
                    response.setStatusCode(400);
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                    Log.d("PlanHandler", jsonObject.toJSONString());
                    return;
                }

                planDB = DataSupport.find(PlanDB.class, serverId);
                planDB.setForceFinish(Integer.valueOf(params.get("forceFinish")));
                planDB.setCause(URLDecoder.decode(params.get("cause"), "utf-8"));
                planDB.setEditTime(System.currentTimeMillis());
                planDB.update(serverId);
            }

            jsonObject.put("code", 1);
            jsonObject.put("msg", "操作成功");

            List<MemberDB> members = JSON.parseArray(planDB.getMembers(), MemberDB.class);
            for (MemberDB member : members) {
                String bmobID = DataSupport.find(MemberDB.class, member.getId()).getBmobID();
                Log.d("PlanHandler", "bmobID=" + bmobID);
                if (!TextUtils.isEmpty(bmobID) && bmobID.contains("-")) {
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
            response.setStatusCode(400);
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求异常");
        }
        Log.d("PlanHandler", jsonObject.toJSONString());
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
    }
}
