package service;

import data.Data;
import ui.MainFrame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    //监听的端口
    private  int port;
    private MainFrame frame;
    public Server(int port,MainFrame frame) {

        this.port = port;
        this.frame = frame;
    }

    @Override
    public void run() {
        //声明ServerSocket对象
        ServerSocket serverSocket = null;
        try{
            //创建ServerSocket对象，监听本机的8088端口
            serverSocket = new ServerSocket(port);
            //开始监听
            System.out.println("开始监听……");
            //输出日志
            frame.printLog("服务开始监听……");
            frame.printLog("监听端口："+port);
            frame.printLog("静态资源的路径："+Data.resourcePath);
            while (Data.isRun){//在允许服务器运行的情况下循环监听端口
                Socket socket = serverSocket.accept(); //等待来自客户端的请求获取和客户端对应的Socket对象
                System.out.println("接收到请求……");
                //将Socket交给RequestExecute处理
                RequestExecute re = new RequestExecute(socket);
                re.start();
            }
            //运行到这里说明程序要停止
            frame.printLog("服务监听停止！！");
        }catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("端口"+port+"监听失败"+e.getMessage());
        }finally {//关闭ServerSocket
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


