package service;

import data.Data;
import ui.MainFrame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器
 * @author 张明超
 * @version 1.0 2023/06/13
 */


public class Server extends Thread{
    /**
     * 监听的端口
     */
    private  int port;
    private MainFrame frame;
    public Server(int port,MainFrame frame) {

        this.port = port;
        this.frame = frame;
    }

    @Override
    public void run() {
        /**
         * 线程的主程序
         */
        ServerSocket serverSocket = null;
        try{
            //创建ServerSocket对象，监听本机的8088端口
            serverSocket = new ServerSocket(port);
            System.out.println("开始监听……");
            frame.printLog("服务开始监听……");
            frame.printLog("监听端口："+port);
            frame.printLog("静态资源的路径："+ Data.resourcePath);
            while (Data.isRun){
                /**
                 * 允许服务器运行的情况下循环监听端口
                 */
                Socket socket = serverSocket.accept();
                System.out.println("接收到请求……");
                RequestExecute re = new RequestExecute(socket);
                re.start();
            }
            /**
             * 运行到这里说明程序要停止
             */
            frame.printLog("服务监听停止！！");
        }catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("端口"+port+"监听失败"+e.getMessage());
        }finally {
            /**
             * 关闭ServerSocket
             */
            if(serverSocket!=null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
