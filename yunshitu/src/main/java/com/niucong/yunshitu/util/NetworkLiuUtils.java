package com.niucong.yunshitu.util;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by 刘文杰 on 2018/4/27.
 * 单独的识别接口
 */

public class NetworkLiuUtils {
    private static final String TAG = NetworkLiuUtils.class.getSimpleName();

    private  static final String UPNAMEURL = "http://imgserver.yunshitu.cn/v1/dispatcher";
    private  static final String DELNAMEURL = "http://imgserver.yunshitu.cn/v1/dispatcher ";
    public   static final String TTS_TEMPLATE_URL = "http://tts.baidu.com/text2audio?lan=zh&ie=UTF-8&spd=5&text=%s";

    private static final String SERVER_8888 = "http://101.96.129.162:8888";
    private static final String FACETRACK_URL    = SERVER_8888 + "/manage/api/create_facetrack";
    private static final String UPNAME_TRACK_URL = SERVER_8888 + "/manage/api/create_person_from_facetrack";
    private static final String DETECT_FACETRACK_URL =SERVER_8888 + "/manage/api/match_facetrack_to_person";


    private static boolean getUpNameresult6009(String result)
    {
        if(result!=null)
        {
            try {
                Log.d(TAG, "getUpNameresult: "+result);
                JSONObject jsonObject = new JSONObject(result);
                int flag = (int)jsonObject.get("errno");
                if (flag==0)
                    return true;
                else
                    return false;
            }catch (JSONException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        else
            return false;
    }

    public static boolean getUpNameresult8888(String okmsg)
    {
        if(okmsg.equals("SUCC"))
            return true;
        else return false;
    }


    public static String addService6009(Bitmap bitmap, String name) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bitmap.recycle();
        byte[] imgData = bos.toByteArray();
        String imgBase64 = Base64.encodeToString(imgData, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        String responseStr = null;
        try {
            jsonObject.put("combiner_id", "6009");
            jsonObject.put("image_base64", imgBase64);

            JSONObject extrajosn = new JSONObject();
            extrajosn.put("match_op","add");
            extrajosn.put("name",name);
            jsonObject.put("extra",extrajosn);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url(UPNAMEURL)
                    .post(requestBody)
                    .build();

            Log.d(TAG,"json len: " + String.valueOf(jsonObject.put("image_base64",null).toString()));
            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            responseStr = responseBody != null ? responseBody.string() : null;
        } catch (JSONException |IOException e) {
            e.printStackTrace();
        }
        return responseStr;
    }


    public static String addService8888(List<Mat> matList, String name)
    {
        JSONArray imgArray =  new JSONArray();
        for( Mat mat :matList)
        {
            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bitmap.recycle();
            byte[] imgData = bos.toByteArray();
            String imgBase64 = Base64.encodeToString(imgData, Base64.DEFAULT);
            imgArray.put(imgBase64);
        }
        JSONObject jsonObject = new JSONObject();
        String okmsg =null;
        try {
            String uuid = UUID.randomUUID().toString();
            jsonObject.put("uuid", uuid);
            jsonObject.put("imgs", imgArray);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url(FACETRACK_URL)
                    .post(requestBody)
                    .build();

            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            String responseStr = responseBody != null ? responseBody.string() : null;
            Log.d(TAG, "addService8888 responseStr: "+responseStr);
            String factrackid = gettrackid(responseStr);
            Log.d(TAG, "addService8888 factrackid: "+factrackid);
            okmsg = adduser8888(factrackid,name);
        } catch (JSONException |IOException e) {
            e.printStackTrace();
        }
        return okmsg;
    }

    private static String adduser8888(String facetrackid,String name)
    {
        String oksmg ="";
        JSONObject jsonObject = new JSONObject();
        JSONArray grouparray = new JSONArray();
        try {
            jsonObject.put("name", name);
            jsonObject.put("nick_name", name);
            jsonObject.put("sex", 0);//0女1男
            jsonObject.put("remark", "");
            jsonObject.put("birthday", "1900-01-01");
            jsonObject.put("card_id", "");
            jsonObject.put("job_id", "");
            jsonObject.put("group_id", -1);
            jsonObject.put("facetrack_id", facetrackid);
            jsonObject.put("match_threshold", 50);
            jsonObject.put("welcome_content", name+"你好");


            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Log.d(TAG, "adduser8888: requestbody:"+jsonObject.toString());
            Request request = new Request.Builder()
                    .url(UPNAME_TRACK_URL)
                    .post(requestBody)
                    .build();

            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            String responseStr = responseBody != null ? responseBody.string() : null;
            Log.d(TAG, "adduser8888 responseStr: "+responseStr);
            JSONObject responseJson = new JSONObject(responseStr);
            oksmg= responseJson.getString("msg");

        } catch (JSONException |IOException e) {
            e.printStackTrace();
        }
        return oksmg;
    }




    private static OkHttpClient mOkHttpClient;
    private static final Object mLock = new byte[0];
    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            synchronized (mLock) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = new OkHttpClient.Builder()
                            .build();
                }
            }
        }
        return mOkHttpClient;
    }

    public static String detectService8888(Mat mat){
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bitmap.recycle();
        byte[] imgData = bos.toByteArray();
        String imgBase64 = Base64.encodeToString(imgData, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        JSONArray imgs  = new JSONArray();
        imgs .put(imgBase64);
        String responseStr = null;
        String personname=null;
        try {
            String uuid = UUID.randomUUID().toString();
            jsonObject.put("uuid", uuid);
            jsonObject.put("imgs", imgs);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url(FACETRACK_URL)
                    .post(requestBody)
                    .build();
            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            responseStr = responseBody != null ? responseBody.string() : null;
            Log.d("liu", "detectService8888: "+responseStr);
            String trackid = gettrackid(responseStr);

            personname = getdetect8888(trackid,1);
        } catch (JSONException|IOException e) {
            e.printStackTrace();
        }
        return personname;
    }

    private static String gettrackid(String str)
    {
        String facetrackid=null;
        try {
            JSONObject json = new JSONObject(str);
            facetrackid = json.getJSONObject("data").
                    getJSONObject("facetrack_info").
                    getJSONObject("facetrack_info").
                    getString("facetrack_id");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return facetrackid;
    }

    private static String getdetect8888(String trackid,int length)
    {
        JSONObject jsonObject = new JSONObject();
        String personname=null;
        double score = 1.0;
        try {
            String uuid = UUID.randomUUID().toString();
            jsonObject.put("facetrack_id", trackid);
            jsonObject.put("match_result_cnt", length);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url(DETECT_FACETRACK_URL)
                    .post(requestBody)
                    .build();
            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            String responseStr = responseBody != null ? responseBody.string() : null;
            Log.d(TAG, "getdetect8888 responseStr:"+responseStr);
            JSONArray userarry = new JSONObject(responseStr).getJSONObject("data").getJSONArray("match_result");
            personname = userarry.getJSONObject(0).getJSONObject("person_info").getString("name");
            score = userarry.getJSONObject(0).getDouble("score");
        } catch (JSONException|IOException e) {
            e.printStackTrace();
        }

        if(score<0.5)
            return null;
        else
        return personname;
    }

}
