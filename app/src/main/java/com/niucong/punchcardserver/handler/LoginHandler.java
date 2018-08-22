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

import com.alibaba.fastjson.JSONObject;
import com.niucong.punchcardserver.db.MemberDB;
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
import java.net.URLDecoder;
import java.util.Map;

/**
 * 登录接口
 */
public class LoginHandler implements RequestHandler {

    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        Log.d("LoginHandler", "params=" + params.toString());
        JSONObject jsonObject = new JSONObject();
        if (!params.containsKey("username") || !params.containsKey("password")) {
            response.setStatusCode(400);
            jsonObject.put("code", 0);
            jsonObject.put("msg", "请求参数错误");
            response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
            return;
        }

        String userName = URLDecoder.decode(params.get("username"), "utf-8");
        String password = URLDecoder.decode(params.get("password"), "utf-8");
        response.setStatusCode(200);
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "账号密码不能为空");
            response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
            return;
        }

        MemberDB memberDB = DataSupport.where("phone = ?", userName).findFirst(MemberDB.class);
        if (memberDB == null) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "账号不存在");
        } else if (!password.equals(memberDB.getPassword())) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "密码错误");
        } else if (memberDB.getIsDelete() == 1) {
            jsonObject.put("code", 0);
            jsonObject.put("msg", "账号已失效");
        } else {
            jsonObject.put("code", 1);
            jsonObject.put("msg", "登录成功");
            jsonObject.put("memberId", memberDB.getId());
            jsonObject.put("type", memberDB.getType());
        }
        response.setEntity(new StringEntity(jsonObject.toString(), "utf-8"));
    }
}
