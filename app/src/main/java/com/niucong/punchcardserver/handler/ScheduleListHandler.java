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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.niucong.punchcardserver.db.ScheduleDB;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 查看作息表
 */
public class ScheduleListHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("ScheduleListHandler", "params=" + params.toString());
        JSONObject jsonObject = new JSONObject();
        listToArray(response, jsonObject, DataSupport.findAll(ScheduleDB.class));
    }

    private void listToArray(HttpResponse response, JSONObject jsonObject, List<ScheduleDB> list) {
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
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("SignListHandler", jsonObject.toJSONString());
    }
}
