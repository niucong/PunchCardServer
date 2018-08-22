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
import com.niucong.punchcardserver.db.MemberDB;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;

import org.apache.httpcore.Header;
import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

/**
 * 成员列表
 */
public class MemberListHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        JSONObject jsonObject = new JSONObject();

        String userId = "";
        for (Header header : request.getAllHeaders()) {
            if ("userId".equals(header.getName())) {
                userId = header.getValue();
                break;
            }
        }
        Log.d("MemberListHandler", "userId=" + userId);
        listToArray(response, jsonObject, DataSupport.order("id desc").
                where("id != ?", userId).find(MemberDB.class));
    }

    private void listToArray(HttpResponse response, JSONObject jsonObject, List<MemberDB> list) {
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
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("MemberListHandler", jsonObject.toJSONString());
    }
}
