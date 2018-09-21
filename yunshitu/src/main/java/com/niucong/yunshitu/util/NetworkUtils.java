package com.niucong.yunshitu.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by yunshitu on 17-12-22.
 */

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String CONFIG_URL = "http://imgserver.yunshitu.cn/app/sdk_face/conf_online/config-%s.txt";
    private static final String UPLOAD_URL = "http://imgserver.yunshitu.cn/upload";
    private static final String SERVICE_URL = "http://imgserver.yunshitu.cn/v1/dispatcher";

    public static final String MODELS_DIR = "models";

    public static String getStringFromInputStream(InputStream inputStream, String charsetName) throws IOException {
        byte[] byteArr;
        int available = inputStream.available();
        if (available > 0) {
            byteArr = new byte[available];
            inputStream.read(byteArr);
        } else {
            final int BUFFER_SIZE = 1024;
            int cnt;
            byte[] buffer = new byte[BUFFER_SIZE];
            List<Byte> totalByte = new ArrayList<>();
            while ((cnt = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < cnt; ++i) {
                    totalByte.add(buffer[i]);
                }
            }

            byteArr = new byte[totalByte.size()];
            for (int i = 0; i < totalByte.size(); ++i) {
                byteArr[i] = totalByte.get(i);
            }
        }
        return new String(byteArr, charsetName);
    }

    private static OkHttpClient mOkHttpClient;
    private static final Object mLock = new byte[0];

    /**
     * 获得单例OkHttpClient，以备需要保存Cookie
     * @return 单例OkHttpClient
     */
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

    public static String getJSONConfig(String version) {
        final String url = String.format(Locale.CHINA, CONFIG_URL, version);
        OkHttpClient okHttpClient = getOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        String result = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            result = body != null ? body.string() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String uploadImage(@NonNull Mat mat, @NonNull Size size, @NonNull String format, int quality,
                                     String fileKey, Map<String, String> params) {
        // 缩放并压缩图片
        Mat resizeMat = new Mat();
        try {
            Imgproc.resize(mat, resizeMat, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap bitmap = Bitmap.createBitmap(resizeMat.width(), resizeMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resizeMat, bitmap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        switch (format.toLowerCase()) {
            case "jpg": case "jpeg": default:
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                break;
            case "png":
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
                break;
        }
        bitmap.recycle();

        // 上传
        OkHttpClient okHttpClient = getOkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        builder.addFormDataPart(fileKey, "random", RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray()));
        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(builder.build())
                .build();
        String responseString = null;
        try {
            ResponseBody responseBody = okHttpClient.newCall(request).execute().body();
            if (responseBody != null) {
                responseString = responseBody.string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseString;
    }

    public static String detectService6009(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bitmap.recycle();
        byte[] imgData = bos.toByteArray();
        String imgBase64 = Base64.encodeToString(imgData, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        String responseStr = null;
        try {
            jsonObject.put("combiner_id", 6009);
            jsonObject.put("image_base64", imgBase64);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Request request = new Request.Builder()
                    .url(SERVICE_URL)
                    .post(requestBody)
                    .build();
            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            responseStr = responseBody != null ? responseBody.string() : null;
        } catch (JSONException|IOException e) {
            e.printStackTrace();
        }
        return responseStr;
    }

    public static File downloadMusic(Context context, String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        File file = null;
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                file = new File(OSUtils.getTempDir(context, "musicTemp"), "music.mp3");
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(responseBody.bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File downloadModel(Context context, String zipUrl, boolean forced) {
        String hash = null;
        try {
            hash = urlHash(zipUrl, "MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (hash == null) {
            return null;
        }
        File modelsDir = OSUtils.getExtDir(context, MODELS_DIR);
        File modelDir = new File(modelsDir, hash);
        File content = new File(modelDir, "content");
        if (!forced && checkModel(modelDir)) {
            return content;
        }
        OSUtils.removeFiles(modelDir);

        if (!content.mkdirs()) {
            return null;
        }
        File zip = downloadZip(modelDir, zipUrl);
        /*File zip = null;
        try {
            zip = downloadZipTest(modelDir, context.getAssets().open("caffe_shuffle_net.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        if (zip == null || !unzip(zip, content)) {
            return null;
        }
        zip.delete();

        try (OutputStream out = new FileOutputStream(new File(modelDir, "SUCCESS"))) { } catch (IOException ignored) { }
        return content;
    }

    private static String urlHash(String url, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest msgDigest = MessageDigest.getInstance(algorithm);
        msgDigest.update(url.getBytes());
        byte[] digest = msgDigest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            int v = ((int) b) & 0xFF;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    private static boolean checkModel(File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        for (File f : dir.listFiles()) {
            if (f.isFile() && f.getName().equals("SUCCESS")) {
                return true;
            }
        }
        return false;
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[2048];
        int cnt;
        while ((cnt = in.read(buffer)) > 0) {
            out.write(buffer, 0, cnt);
        }
    }

    private static File downloadZipTest(File dir, InputStream stream) {
        File file = null;
        try {
            try (InputStream in = new BufferedInputStream(stream);
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(file = new File(dir, "model.zip")))) {
                copyStream(in, out);
            }
        } catch (IOException ignored) {

        }
        return file;
    }

    private static File downloadZip(File dir, String zipUrl) {
        OkHttpClient client = getOkHttpClient();
        Request request = new Request.Builder()
                .url(zipUrl)
                .build();
        File file = null;
        try {
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            if (body != null) {
                try (InputStream in = new BufferedInputStream(body.byteStream());
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(file = new File(dir, "model.zip")))) {
                    copyStream(in, out);
                }
            }
        } catch (IOException ignored) {

        }
        return file;
    }

    private static boolean unzip(File src, File dst) {
        try {
            ZipFile zipFile = new ZipFile(src);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = e.nextElement();

                File file = new File(dst, zipEntry.getName());
                if (zipEntry.getName().endsWith("/")) {
                    file.mkdirs();
                } else {
                    try (InputStream in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                         OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        copyStream(in, out);
                    }
                }
            }
            zipFile.close();
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }
}
