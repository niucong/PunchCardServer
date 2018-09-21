package com.niucong.punchcardserver.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


/**
 * @ClassName:ToolUtils
 * @Description:工具类
 */
public class ToolUtils {

    /**
     * Activity 6.0运行权限设置
     *
     * @param context
     * @param activity
     * @param permission  权限  Manifest.permission.XXX
     * @param requestCode 请求代码匹配结果
     */
    public static boolean setPermission(Context context, Activity activity, String permission,
                                        int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return true;
            }
        }
        return false;
    }

    /**
     * Activity 6.0运行权限设置
     *
     * @param context
     * @param activity
     * @param  permissions 权限  Manifest.permission.
     * @param type
     */
    public static boolean setPermission(Context context, Activity activity, String[] permissions,
                                        int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(context, permissions[0]) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, permissions , type);
                return true;
            }
        }
        return false;
    }
}
