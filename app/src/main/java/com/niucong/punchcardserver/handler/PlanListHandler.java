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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.util.List;
import java.util.Map;

/**
 * 请假批假接口
 */
public class PlanListHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("PlanListHandler", "params=" + params.toString());
        JSONObject jsonObject = new JSONObject();

        String userId = "";
        for (Header header : request.getAllHeaders()) {
            if ("userId".equals(header.getName())) {
                userId = header.getValue();
                break;
            }
        }

        int offset = 0;
        int pageSize = 10;
        if (params.containsKey("offset")) {
            try {
                offset = Integer.valueOf(params.get("offset"));
            } catch (NumberFormatException e) {
                response.setStatusCode(400);
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起始参数错误");
                response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return;
            }
        }
        if (params.containsKey("pageSize")) {
            try {
                pageSize = Integer.valueOf(params.get("pageSize"));
            } catch (NumberFormatException e) {
                response.setStatusCode(400);
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求数量参数错误");
                response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return;
            }
        }
        String searchKey = "";
        if (params.containsKey("searchKey")) {
            searchKey = URLDecoder.decode(params.get("searchKey"), "utf-8");
        }

        long startTime = 0;
        long endTime = 0;
        if (params.containsKey("startTime") && params.containsKey("endTime")) {
            try {
                startTime = Long.valueOf(params.get("startTime"));
                endTime = Long.valueOf(params.get("endTime"));
            } catch (NumberFormatException e) {
                response.setStatusCode(400);
                jsonObject.put("code", 0);
                jsonObject.put("msg", "请求起止时间参数错误");
                response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
                Log.d("PlanListHandler", jsonObject.toJSONString());
                return;
            }
        }

        Log.d("PlanListHandler", "searchKey=" + searchKey);
        if (TextUtils.isEmpty(searchKey)) {
            if (startTime != 0 && endTime != 0) {
                listToArray(response, jsonObject, DataSupport.order("id desc").where("(creatorId = ? or members like ?) and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "%:" + userId + ",%", "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(PlanDB.class));
            } else {
                listToArray(response, jsonObject, DataSupport.order("id desc")
                        .where("creatorId = ? or members like ?", userId, "%:" + userId + ",%").offset(offset).limit(pageSize).find(PlanDB.class));
            }
        } else {
            if (startTime != 0 && endTime != 0) {
                listToArray(response, jsonObject, DataSupport.order("id desc").where("(creatorId = ? or members like ?) and (name like ?) and " +
                                "((startTime <= ? and endTime >= ?) or (startTime <= ? and endTime >= ?) or (startTime >= ? and endTime <= ?))",
                        userId, "%:" + userId + ",%", "%" + searchKey + "%", "" + startTime, "" + startTime, "" + endTime, "" + endTime, "" + startTime, "" + endTime)
                        .offset(offset).limit(pageSize).find(PlanDB.class));
            } else {
                listToArray(response, jsonObject, DataSupport.order("id desc")
                        .where("(creatorId = ? or members like ?) and (name like ?)",
                                userId, "%:" + userId + ",%", "%" + searchKey + "%")
                        .offset(offset).limit(pageSize).find(PlanDB.class));
            }
        }
    }

    private void listToArray(HttpResponse response, JSONObject jsonObject, List<PlanDB> list) {
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
        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("PlanListHandler", jsonObject.toJSONString());
    }
}
