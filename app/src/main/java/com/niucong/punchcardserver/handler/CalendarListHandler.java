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
import com.niucong.punchcardserver.db.CalendarDB;
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
 * 查看校历
 */
public class CalendarListHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("CalendarListHandler", "params=" + params.toString());
        JSONObject jsonObject = new JSONObject();
        listToArray(response, jsonObject, DataSupport.findAll(CalendarDB.class));
    }

    private void listToArray(HttpResponse response, JSONObject jsonObject, List<CalendarDB> list) {
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
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("SignListHandler", jsonObject.toJSONString());
    }
}
