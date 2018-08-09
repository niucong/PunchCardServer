package com.niucong.punchcardserver.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class WifiConnect {

    private Socket socket;
    private BufferedWriter bWriter;//输出流，发送、写入信息
    private BufferedReader bReader;//输入流，接受、读取信息

    public void connect(final String ipAddress) {
        new AsyncTask<Void, String, Void>() {

            //在主线程中执行，不能用来初始化socket
            protected void onPreExecute() {
            }

            protected Void doInBackground(Void[] params) {
                try {
                    socket = new Socket(ipAddress, 1989);
                    bWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    publishProgress("connect success");
                } catch (IOException e) {
                    Log.e("connect error", "error");
                }

                String s;
                //readLine()是一个阻塞函数，当没有数据读取时，就一直会阻塞在那，而不是返回null
                //接受信息
                try {
                    while ((s = bReader.readLine()) != null) {
                        publishProgress(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onProgressUpdate(String[] values) {
                if (values[0].equalsIgnoreCase("connect success")) {
//                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    Log.d("", "连接成功");
                } else {
//                    textView.append("别人说" + values[0] + "\n");
                    Log.d("", "别人说" + values[0] + "\n");
                }
            }
        }.execute();
    }

    //发送信息
    public void send(String message) {
        try {
            bWriter.write(message + "\n");
            bWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
