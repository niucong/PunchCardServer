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
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.SignDB;
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
 * 签到列表
 */
public class SignListHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("SignListHandler", "params=" + params.toString());
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
                Log.d("SignListHandler", jsonObject.toJSONString());
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
                Log.d("SignListHandler", jsonObject.toJSONString());
                return;
            }
        }
        String searchKey = "";
        if (params.containsKey("searchKey")) {
            searchKey = URLDecoder.decode(params.get("searchKey"), "utf-8");
        }
        MemberDB memberDB = DataSupport.find(MemberDB.class, Integer.valueOf(userId));
        int type = memberDB.getType();
        Log.d("SignListHandler", "userId=" + userId + ",type=" + type + ",searchKey=" + searchKey);
        if (type == 1) {
            if (TextUtils.isEmpty(searchKey)) {
                if (offset == 0) {
                    jsonObject.put("allSize", DataSupport.count(SignDB.class));
                }
                listToArray(response, jsonObject, DataSupport.order("id desc").offset(offset)
                        .limit(pageSize).find(SignDB.class));
            } else {
                if (offset == 0) {
                    jsonObject.put("allSize", DataSupport.where("name = ?", searchKey)
                            .count(SignDB.class));
                }
                listToArray(response, jsonObject, DataSupport.order("id desc")
                        .where("name = ?", searchKey).offset(offset).limit(pageSize).find(SignDB.class));
            }
        } else if (type == 2) {
            if (TextUtils.isEmpty(searchKey)) {
                if (offset == 0) {
                    jsonObject.put("allSize", DataSupport.where("memberId = ? or superId = ?", userId, userId)
                            .count(SignDB.class));
                }
                listToArray(response, jsonObject, DataSupport.order("id desc")
                        .where("memberId = ? or superId = ?", userId, userId).offset(offset).limit(pageSize).find(SignDB.class));
            } else {
                if (offset == 0) {
                    jsonObject.put("allSize", DataSupport.where("memberId = ? or superId = ? and name = ?", userId, userId, searchKey)
                            .count(SignDB.class));
                }
                listToArray(response, jsonObject, DataSupport.order("id desc")
                        .where("memberId = ? or superId = ? and name = ?", userId, userId, searchKey)
                        .offset(offset).limit(pageSize).find(SignDB.class));
            }
        } else {
            if (offset == 0) {
                jsonObject.put("allSize", DataSupport.where("memberId = ?", userId).count(SignDB.class));
            }
            listToArray(response, jsonObject, DataSupport.order("id desc").where("memberId = ?", userId)
                    .offset(offset).limit(pageSize).find(SignDB.class));
        }
    }

    private void listToArray(HttpResponse response, JSONObject jsonObject, List<SignDB> list) {
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
        jsonObject.put("code", 1);
        jsonObject.put("msg", "请求成功");
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("SignListHandler", jsonObject.toJSONString());
    }
}
