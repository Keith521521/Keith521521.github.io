package service;

import data.Data;

import java.io.*;
import java.net.Socket;

//请求处理线程类
public class RequestExecute extends Thread{
    //将Socket定义为成员变量，利用构造方法初始化
    private Socket socket;
    public RequestExecute(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        //从Socket中取出输入流，然后从输入流中取出数据
        InputStream in = null;//将字节输入流转换为缓冲字符输入流
        InputStreamReader reader = null;//转换流
        BufferedReader bufferedReader = null;//字符缓冲流
        //声明输出流 输出流是指向客户端的
        OutputStream out = null;
        PrintWriter pw = null;
        try{
            //从Socket中获取字节输出流
            out = socket.getOutputStream();
            //将字节输出流包装成字符输出流
            pw = new PrintWriter(out);
            //先判断服务器是否处于暂停状态
            if(Data.isPush) {
                pw.println("HTTP/1.1 200 OK");//输出响应行
                pw.println("Content-Type:text/html;charset=utf-8");
                //输出空行
                pw.println();//表示响应头结束，开始响应内容
                pw.write("<h2>欢迎访问Server1.0</h2>");
                pw.write("<h2>服务器目前处于暂停状态，请您稍后再试！</h2>");
                pw.flush();
                System.out.println("服务器目前处于暂停状态");
                return;
            }
            //从Socket中获取字节输入流
            in = socket.getInputStream();
            //转换和包装
            reader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(reader);
            //循环的从字符流中获取字符
            String line = null;
            int lineNum = 1;
            //存储请求路径
            String reqPath = "";
            String host = "";
            while ((line = bufferedReader.readLine())!=null) {
                System.out.println(line);
                //解析请求行
                if(lineNum==1) {//第一行  GET /xxx/xx.HTML HTTP1.1
                    //使用空格分割字符串
                    String [] arr = line.split(" ");
                    if(arr!=null || arr.length>2) {
                        reqPath = arr[1];//请求路径
                    }else {
                        throw new RuntimeException("请求行解析失败"+line);
                    }
                }else {
                    //解析其他行，取出Host的内容
                    String [] arr = line.split(":");
                    if(arr!=null || arr.length==2) {
                        //取出host
                        if(arr[0].equals("Host"))
                            host = arr[1];
                    }
                }
                lineNum++;
                if(line.equals(""))//读取到空行就结束，因为Http请求是长连接，无法读取到文件的末尾
                    break;
            }

            //输出请求信息
            if(!reqPath.equals("")) {
                System.out.println("处理请求：http://" + host + reqPath);
                //根据请求响应客户端 "/“直接响应一个欢迎页面 /…….html 就输出对应的文件内容
                if(reqPath.equals("/")){//没有资源的名称
                    pw.println("HTTP/1.1 200 OK");//输出响应行
                    pw.println("Content-Type:text/html;charset=utf-8");
                    //输出空行
                    pw.println();//表示响应头结束，开始响应内容
                    pw.write("<body bodystyle=\"text-align:center\"\n" +
                            "      background=\"img_1.jpg\"\n" +
                            "      style=\"background-repeat: no-repeat;\n" +
                            "   background-size: 100%100%;\n" +
                            "   background-attachment: fixed;\">");
                    pw.write("<br/>");
                    pw.write("<br/>");
                    pw.write("<br/>");
                    pw.write("<h2 align=\"CENTER\" style=\"font-size: 100px\"> 欢迎访问服务器</h2>");
                    pw.write("</body>");
                    pw.flush();
                    System.out.println("响应欢迎页面！");
                }else {
                    //查找对应的资源   /.html/.jpg/.png/.json……
                    //取出后缀
                    String ext = reqPath.substring(reqPath.lastIndexOf(".")+1);
                    reqPath = reqPath.substring(1);//去除前面的“/”；


                    //判断是在根目录下还是在其他的子目录下
                    if(reqPath.contains("/")) {//子目录
                        //判断文件是否存在
                        File file = new File(Data.resourcePath+reqPath);
                        if(file.exists()&&file.isFile()) {
                            //将文件内容响应到客户端
                            response200(out,file.getAbsolutePath(),ext);
                        }else {
                            //响应404页面
                            response404(out);
                        }
                    }else {//根目录
                        //判断这个资源是否存在
                        //获取根目录下的所有的文件的名称
                        File root = new File(Data.resourcePath);
                        if(root.isDirectory()) {
                            File [] list = root.listFiles();
                            boolean isExist = false;//标记访问的资源是否存在
                            for(File file : list){
                                if(file.isFile()&&file.getName().equals(reqPath)) {
                                    //文件存在
                                    isExist = true;
                                    break;
                                }
                            }
                            if(isExist){//文件存在
                                response200(out,Data.resourcePath+reqPath,ext);
                            }else {//文件不存在
                                response404(out);
                            }
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

    //将指定的文件输出到输出流中
    private void response200(OutputStream out,String filePath,String ext) {
        PrintWriter pw = null;
        //准备输入流读取磁盘上的文件
        InputStream in = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            if(ext.equals("jpg")||ext.equals("png")||ext.equals("gif")) {
                out.write("HTTP/1.1 200 OK\r\n".getBytes());//输出响应行
                if(ext.equals("jpg"))
                    out.write("Content-Type:image/jpg\r\n".getBytes());
                else if(ext.equals("png"))
                    out.write("Content-Type:image/png\r\n".getBytes());
                else if(ext.equals("gif"))
                    out.write("Content-Type:image/gif\r\n".getBytes());
                //输出一个空行
                out.write("\r\n".getBytes());//输出空行，表示响应头结束
                System.out.println("响应欢迎页面！");
                //利用自己而输入流读取文件内容，并且输出到输入流中
                //创建输入流
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
                pw.println();//输出空行，表示响应头结束
                System.out.println("响应欢迎页面！");
                //初始化输入流
                in = new FileInputStream(filePath);
                reader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(reader);
                //写出数据
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
    private void response404(OutputStream out) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(out);
            pw.println("HTTP/1.1 404");//输出响应行
            pw.println("Content-Type:text/html;charset=utf-8");
            //输出空行
            pw.println();//表示响应头结束，开始响应内容
            pw.write("<body bodystyle=\"text-align:center\"\n" +
                    "      background=\"img_1.jpg\"\n" +
                    "      style=\"background-repeat: no-repeat;\n" +
                    "\tbackground-size: 100%100%;\n" +
                    "\tbackground-attachment: fixed;\">");
            pw.write("<h1 align=\"CENTER\"  style=\"font-size: 100px\">404<h1>");
            pw.write("<h2 align=\"CENTER\" style=\"font-size: 100px\">客户端所请求的URL在服务端不存在</h2>");
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
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


//200(OK):找到了该资源，并且一切正常
//304(NOT MODIFIED)该资源在上次请求之后没有任何修改。这通常用于浏览器的缓存机制。
//401(UNAUTHORIZED)客户端无权访问该资源。这通常会使得浏览器要求用户输入用户名和密码，以登录到服务器。
//403(FORBIDDEN)客户端未能获得授权。这通常是在401之后输入了不正确的用户名或密码。
//404(NOT FOUND)在指定的位置不存在所申请的资源



