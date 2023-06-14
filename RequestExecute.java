package service;

import data.Data;
import java.io.*;
import java.net.Socket;

/**
 * 请求处理线程类
 *
 * @author 张明超
 *
 */
public class RequestExecute extends Thread{
    /**
     * 套接字
     * 将Socket定义为成员变量，利用构造方法初始化
     */
    private Socket socket;

    /**
     * 请求执行
     *
     * @param socket 套接字
     */
    public RequestExecute(Socket socket) {
        this.socket = socket;
    }

    /**
     * 运行
     */
    public void run() {
        /**
         * 从Socket中取出输入流，然后从输入流中取出数据
         */

        /**
         * 字节输入流in
         */
        InputStream in = null;
        /**
         *字符输入流reader
         */
        InputStreamReader reader = null;
        /**
         * 缓冲字符输入流bufferedReader
         */
        BufferedReader bufferedReader = null;
        /**
         * 字节输出流out
         */
        OutputStream out = null;
        /**
         * 字符输出流pw
         */
        PrintWriter pw = null;

        try{
            /**
             * 将字节输出流包装成字符输出流
             */
            out = socket.getOutputStream();
            pw = new PrintWriter(out);
            /**
             * 先判断服务器是否处于暂停状态
             */
            if(Data.isPush) {
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Type:text/html;charset=utf-8");
                pw.println();
                pw.write("<body bgcolor=\"#F0F8FF\">");
                pw.write("<h2 align=\"CENTER\" style=\"font-size: 50px \">抱歉!</h2>");
                pw.write("<h3 align=\"CENTER\" style=\"font-size: 50px \">服务器目前处于暂停状态，请您稍后再试！</h3>");
                pw.write("</body>");
                pw.flush();
                System.out.println("服务器目前处于暂停状态");
                return;
            }
            /**
             * 从Socket中获取字节输入流
             * 把字节输入流包装成字符输入流
             */
            in = socket.getInputStream();
            reader = new InputStreamReader(in);
            /**
             * 把字符输入流转换成缓冲字符输入流
             */
            bufferedReader = new BufferedReader(reader);
            String line = null;
            int lineNum = 1;

            String reqPath = "";
            String host = "";
            while ((line = bufferedReader.readLine())!=null) {
                System.out.println(line);

                if(lineNum==1) {
                    /**
                     * 第一行  GET /xxx/xx.HTML HTTP1.1
                     * 用空格分割字符串
                     */
                    String [] arr = line.split(" ");
                    if(arr!=null || arr.length>2) {
                        reqPath = arr[1];
                    }else {
                        throw new RuntimeException("请求行解析失败"+line);
                    }
                }else {
                    /**
                     * 解析其他行，取出Host的内容
                     */
                    String [] arr = line.split(":");
                    if(arr!=null || arr.length==2) {

                        if(arr[0].equals("Host"))
                            host = arr[1];
                    }
                }
                lineNum++;
                /**
                 * 读取到空行就结束，因为Http请求是长连接，无法读取到文件的末尾
                 */
                if(line.equals(""))
                    break;
            }

            /**
             * 输出请求信息
             */
            if(!reqPath.equals("")) {
                System.out.println("处理请求：http://" + host + reqPath);
                /**
                 * 没有资源名称
                 */
                if(reqPath.equals("/")){
                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Content-Type:text/html;charset=utf-8");

                    pw.println();
                    pw.write("<body bodystyle=\"text-align:center\"\n" +
                            "background=\"zhuomian.png\"\n" +
                            "style=\"background-repeat: no-repeat;\n" +
                            "background-size: 100%100%;\n" +
                            "background-attachment: fixed;\">");
                    pw.write("<br/>");
                    pw.write("<br/>");
                    pw.write("<br/>");
                    pw.write("<h2 align=\"CENTER\" style=\"font-size: 120px \"> HELLO!</h2>");
                    pw.write("<h3 align=\"CENTER\" style=\"font-size: 120px \"> 欢迎访问!</h3>");
                    pw.write("</body>");
                    pw.flush();
                    System.out.println("响应欢迎页面！");
                }else {
                    /**
                     * 查找对应的资源   /.html/.jpg/.png/.json……
                     */
                    String ext = reqPath.substring(reqPath.lastIndexOf(".")+1);
                    reqPath = reqPath.substring(1);
                    /**
                     * 在子目录下
                     */
                    if(reqPath.contains("/")) {
                        File file = new File(Data.resourcePath+reqPath);
                        if(file.exists()&&file.isFile()) {
                            response200(out,file.getAbsolutePath(),ext);
                        }else {
                            response404(out);
                        }
                    }
                    /**
                     * 在根目录下
                     */
                    else {
                        File root = new File(Data.resourcePath);
                        if(root.isDirectory()) {
                            File [] list = root.listFiles();
                            boolean isExist = false;
                            for(File file : list){
                                if(file.isFile()&&file.getName().equals(reqPath)) {
                                    isExist = true;
                                    break;
                                }
                            }
                            if(isExist){
                                response200(out,Data.resourcePath+reqPath,ext);
                            }else {
                                response404(out);
                            }
                        }else {
                            throw new RuntimeException("静态资源目录不存在："+Data.resourcePath);
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            try{
                if(in!=null) {
                    in.close();
                }
                if(reader!=null) {
                    reader.close();
                }
                if(bufferedReader!=null) {
                    bufferedReader.close();
                }
                if(pw!=null) {
                    pw.close();
                }
                if(out!=null) {
                    out.close();
                }
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * response200
     *
     * @param out      输出流
     * @param filePath 文件路径
     * @param ext      文件
     */
    private void response200(OutputStream out,String filePath,String ext) {
        PrintWriter pw = null;
        InputStream in = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            if(ext.equals("jpg")||ext.equals("png")||ext.equals("gif")) {
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                if(ext.equals("jpg"))
                    out.write("Content-Type:image/jpg\r\n".getBytes());
                else if(ext.equals("png"))
                    out.write("Content-Type:image/png\r\n".getBytes());
                else if(ext.equals("gif"))
                    out.write("Content-Type:image/gif\r\n".getBytes());
                out.write("\r\n".getBytes());
                System.out.println("响应欢迎页面！");

                in = new FileInputStream(filePath);
                int len = -1;
                byte [] buff = new byte[1024];
                while((len = in.read(buff))!=-1) {
                    out.write(buff,0,len);
                    out.flush();
                }
            }else if(ext.equals("html")||ext.equals("js")||ext.equals("css")||ext.equals("json")){
                pw = new PrintWriter(out);
                pw.println("HTTP/1.1 200 OK");
                if(ext.equals("html"))
                    pw.println("Content-Type:text/html;charset=utf-8");
                else if(ext.equals("js"))
                    pw.println("Content-Type:application/x-javascript");
                else if(ext.equals("css"))
                    pw.println("Content-Type:text/css");
                else if(ext.equals("json"))
                    pw.println("Content-Type:application/json;charset=utf-8");
                pw.println();
                System.out.println("响应欢迎页面！");

                in = new FileInputStream(filePath);
                reader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(reader);
                String line = null;
                while ((line = bufferedReader.readLine())!=null) {
                    pw.println(line);
                    pw.flush();
                }
            }else {
                response404(out);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(pw!=null) {
                    pw.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * response404
     *
     * @param out 输出流
     */
    private void response404(OutputStream out) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(out);
            pw.println("HTTP/1.1 404");
            pw.println("Content-Type:text/html;charset=utf-8");
            //输出空行
            pw.println();
            pw.write("<body bodystyle=\"text-align:center\"\n" +
                    "background=\"zhuomian.png\"\n" +
                    "style=\"background-repeat: no-repeat;\n" +
                    "\tbackground-size: 100%100%;\n" +
                    "\tbackground-attachment: fixed;\">");
            pw.write("<h1 align=\"CENTER\"  style=\"font-size: 100px\">404<h1>");
            pw.write("<h2 align=\"CENTER\" style=\"font-size: 80px\">客户端所请求的URL在服务端不存在</h2>");
            pw.write("</body>");
            pw.flush();
            System.out.println("响应欢迎页面！");

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(pw!=null) {
                    pw.close();
                }
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}