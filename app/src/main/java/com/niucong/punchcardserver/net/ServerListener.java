package com.niucong.punchcardserver.net;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread {

    @Override
    public void run() {
        super.run();

        //端口范围:1-65535
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(1989);
            while (true) {
                Socket socket = serverSocket.accept();
                Log.d("ServerListener", "有客户端连接到了1989端口");
                ChatSocket cs = new ChatSocket(socket);
                cs.start();
                ChatManager.getChatManager().add(cs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
