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
import com.niucong.punchcardserver.db.MemberDB;
import com.niucong.punchcardserver.db.SignDB;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 签到接口
 */
public class SignHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
//        Map<String, String> params = HttpRequestParser.parseParams(request);

        Log.d("SignHandler", "开始签到");

        String userId = "";
        for (Header header : request.getAllHeaders()) {
            if ("userId".equals(header.getName())) {
                userId = header.getValue();
                break;
            }
        }
        Log.d("SignHandler", "userId=" + userId);

        JSONObject jsonObject = new JSONObject();
        try {
            SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
            long time = YMD.parse(YMD.format(new Date())).getTime();
            SignDB signDB = DataSupport.where("memberId = ? and startTime > ? and startTime < ?",
                    userId, time + "", time + 24 * 60 * 60 * 1000 + "").findFirst(SignDB.class);
            Log.d("SignHandler", "startTime=" + YMD.format(new Date()));
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
            Log.d("SignHandler", "SuperId=" + signDB.getSuperId());

            jsonObject.put("startTime", signDB.getStartTime());
            jsonObject.put("endTime", signDB.getEndTime());
            jsonObject.put("code", 1);
            jsonObject.put("msg", "签到成功");
        } catch (Exception e) {
            response.setStatusCode(400);
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求参数错误");
        }
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
        Log.d("SignHandler", jsonObject.toJSONString());
    }
}
