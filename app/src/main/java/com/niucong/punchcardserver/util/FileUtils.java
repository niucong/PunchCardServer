package com.niucong.punchcardserver.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileUtils {
    private final static String TAG = "FileUtils";

    /**
     * 判断是否有该文件
     *
     * @param path
     * @throws IOException
     */
    public static boolean getFilePath(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 获取sd卡的路径
     *
     * @return
     */
    public static String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    /**
     * 查询实际路径
     *
     * @param context
     * @param contentUri
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] proj = {Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 生成xlsx格式的表格，生成xls格式的表格只需把HSSF替换成XSSF
     *
     * @param workbook
     * @param sheetNum
     * @param sheetTitle
     * @param headers
     * @param result
     * @throws Exception
     */
    public static void exportExcel(HSSFWorkbook workbook, int sheetNum, String sheetTitle,
                                   String[] headers, List<List<String>> result) throws Exception {
// 第一步，创建一个webbook，对应一个Excel以xsl为扩展名文件
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(sheetNum, sheetTitle);
//设置列宽度大小
//        sheet.setDefaultColumnWidth((short) 20);
//第二步， 生成表格第一行的样式和字体
//        HSSFCellStyle style = workbook.createCellStyle();
// 设置这些样式
//        style.setFillForegroundColor(XSSFColor.PALE_BLUE.index);
//        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
//        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
//        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
//        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
//        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
//        style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
// 生成一个字体
//        HSSFFont font = workbook.createFont();
//        font.setColor(XSSFColor.BLACK.index);
//设置字体所在的行高度
//        font.setFontHeightInPoints((short) 20);
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
// 把字体应用到当前的样式
//        style.setFont(font);
// 指定当单元格内容显示不下时自动换行
//        style.setWrapText(true);
// 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell((short) i);
//            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text.toString());
        }
// 第三步：遍历集合数据，产生数据行，开始插入数据
        if (result != null) {
            int index = 1;
            for (List<String> m : result) {
                row = sheet.createRow(index);
                int cellIndex = 0;
                for (String str : m) {
                    HSSFCell cell = row.createCell((int) cellIndex);
                    cell.setCellValue(str.toString());
                    cellIndex++;
                }
                index++;
            }
        }
    }

//    public void export2007(HttpServletResponse response, List<List<Object>> list, String filename, String[] title) {
//        String[] header = title;
//
//        XSSFWorkbook wb = new XSSFWorkbook();
//        XSSFSheet sheet = wb.createSheet(filename);
//        XSSFRow row = sheet.createRow((int) 0);
//        XSSFCellStyle style = wb.createCellStyle();
//
//        XSSFFont font = wb.createFont();
//        font.setFontHeightInPoints((short) 11);
//        font.setFontName("宋体");
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//
//        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
//        style.setFont(font);
//
//        XSSFCell cell = null;
//        for (int i = 0; i < header.length; i++) {
//            cell = row.createCell((short) i);
//            cell.setCellValue(header[i]);
//            cell.setCellStyle(style);
//        }
//
//        if (list == null) {
//            return;
//        }
//
//        for (int i = 0; i < list.size(); i++) {
//            row = sheet.createRow((int) i + 1);
//
//            List<Object> clist = list.get(i);
//            for (int n = 0; n < clist.size(); n++) {
//                Object value = clist.get(n);
//                if (value instanceof Date) {
//                    row.createCell((short) n).setCellValue(fmt.format(value));
//                } else {
//                    row.createCell((short) n).setCellValue(clist.get(n).toString());
//                }
//            }
//        }
//
//        try {
//            response.setContentType("application/force-download");
//            response.setHeader("Content-Disposition", "attachment;filename=\"" + java.net.URLEncoder.encode(filename, "UTF-8") + ".xlsx" + "\" ");
//            wb.write(response.getOutputStream());
//            response.getOutputStream().close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
