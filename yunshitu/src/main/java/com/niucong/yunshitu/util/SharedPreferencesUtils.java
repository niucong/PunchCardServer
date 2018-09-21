package com.niucong.yunshitu.util;

import android.content.Context;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.niucong.yunshitu.R;

/**
 * Created by yunshitu on 17-12-26.
 */

public class SharedPreferencesUtils {
    public static void putJSON(Context context, String json) {
        json = Base64.encodeToString(json.getBytes(), Base64.DEFAULT);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
                context.getString(R.string.settings_config_json_key), json).apply();
    }

    public static String getJSON(Context context) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.settings_config_json_key), null);
        return json != null ? new String(Base64.decode(json, Base64.DEFAULT)) : null;
    }

    public static void putCameraFacing(Context context, int facing) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(
                context.getString(R.string.settings_camera_face_key), facing).apply();
    }

    public static int getCameraFacing(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.settings_camera_face_key), Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public static void setUseOnlineConfig(Context context, boolean use) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                context.getString(R.string.settings_use_online_conf_key), use).apply();
    }

    public static boolean isUseOnlineConfig(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.settings_use_online_conf_key), true);
    }
}
