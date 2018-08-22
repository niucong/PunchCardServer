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

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.niucong.punchcardserver.db.CoursePlanDB;
import com.niucong.punchcardserver.db.MemberDB;
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
import java.util.Map;

/**
 * 课程计划接口
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
            if ("userId".equals(header.getName())) {
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
            jsonObject.put("code", 1);
            if (serverId == 0) {
                if (!params.containsKey("name") || !params.containsKey("start") || !params.containsKey("end")) {
                    response.setStatusCode(400);
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                    Log.d("PlanHandler", jsonObject.toJSONString());
                    return;
                }
                long start = Long.valueOf(params.get("start"));
                long end = Long.valueOf(params.get("end"));
                CoursePlanDB planDB = new CoursePlanDB();
                planDB.setName(URLDecoder.decode(params.get("name"), "utf-8"));
                MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
                planDB.setCreaterId(Integer.valueOf(userId));
                planDB.setCreaterName(memberDB.getName());
                if (params.containsKey("members")){
                    planDB.setMembers(URLDecoder.decode(params.get("members"), "utf-8"));
                }
                if (params.containsKey("cause")){
                    planDB.setCause(URLDecoder.decode(params.get("cause"), "utf-8"));
                }
                planDB.setStartTime(start);
                planDB.setEndTime(end);
                planDB.setCreateTime(System.currentTimeMillis());
                planDB.save();

                jsonObject.put("serverId", planDB.getId());
                jsonObject.put("createTime", planDB.getCreateTime());
                jsonObject.put("msg", "创建成功");
            } else {
                if (!params.containsKey("forceFinish")) {
                    response.setStatusCode(400);
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "请求参数错误");
                    response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                    Log.d("PlanHandler", jsonObject.toJSONString());
                    return;
                }
                CoursePlanDB planDB = DataSupport.find(CoursePlanDB.class, serverId);
                planDB.setEditTime(System.currentTimeMillis());
                planDB.setForceFinish(Integer.valueOf(params.get("forceFinish")));
                planDB.setCause(URLDecoder.decode(params.get("cause"), "utf-8"));
                planDB.update(serverId);
                jsonObject.put("msg", "操作成功");
            }
        } catch (Exception e) {
            response.setStatusCode(400);
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求异常");
        }
        Log.d("PlanHandler", jsonObject.toJSONString());
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
    }
}
