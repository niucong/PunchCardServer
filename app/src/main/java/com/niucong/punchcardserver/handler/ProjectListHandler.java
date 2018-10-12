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
import java.util.List;
import java.util.Map;

/**
 * 请假批假接口
 */
public class ProjectListHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("ProjectListHandler", "params=" + params.toString());
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
                Log.d("ProjectListHandler", jsonObject.toJSONString());
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
                Log.d("ProjectListHandler", jsonObject.toJSONString());
                return;
            }
        }
        String searchKey = "";
        if (params.containsKey("searchKey")) {
            searchKey = URLDecoder.decode(params.get("searchKey"), "utf-8");
        }

        Log.d("ProjectListHandler", "searchKey=" + searchKey);
        if (TextUtils.isEmpty(searchKey)) {
            listToArray(response, jsonObject, DataSupport.order("id desc")
                            .where("creatorId = ? or superId = ? or members like ?", userId, userId, "%:" + userId + ",%")
                            .offset(offset).limit(pageSize).find(ProjectDB.class),
                    DataSupport.where("creatorId = ? or superId = ? or members like ?",
                            userId, userId, "%:" + userId + ",%").count(ProjectDB.class));
        } else {
            listToArray(response, jsonObject, DataSupport.order("id desc")
                            .where("(creatorId = ? or superId = ? or members like ?) and (name like ?)",
                                    userId, userId, "%:" + userId + ",%", "%" + searchKey + "%")
                            .offset(offset).limit(pageSize).find(ProjectDB.class),
                    DataSupport.where("(creatorId = ? or superId = ? or members like ?) and (name like ?)",
                            userId, userId, "%:" + userId + ",%", "%" + searchKey + "%").count(ProjectDB.class));
        }
    }

    private void listToArray(HttpResponse response, JSONObject jsonObject, List<ProjectDB> list, int allSize) {
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
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("ProjectListHandler", jsonObject.toJSONString());
    }
}
