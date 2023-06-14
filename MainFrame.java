package ui;
import data.Data;
import service.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * GUI界面
 * @author 赵紫萱
 * @version 1.0 2023/06/13
 */
public class MainFrame extends JFrame {

    /**
     * 监听端口标签
     */
    private JLabel labPort;
    /**
     * 资源位置标签
     */
    private JLabel labPath;
    /**
     * 端口号文本区
     */
    private JTextField textPort;
    /**
     * 路径文本区
     */
    private JTextField textPath;
    /**
     * btn启动服务器
     */
    private JButton btnStartServer;
    /**
     * 暂停服务按钮
     */
    private JButton btnPushServer;
    /**
     * 停止服务按钮
     */
    private JButton btnStopServer;
    /**
     * btn设置路径
     */
    private JButton btnSetPath;

    /**
     * 控制台滚动窗格
     */
    private JScrollPane scrollPane;
    /**
     * 文本区域
     */
    private JTextArea textArea;

    /**
     * 主要方法
     *
     * @param args arg 参数
     */
    public static void main(String[] args) {
        /**
         创建一个窗口对象
         */
        MainFrame mf = new MainFrame();
    }

    /**
     * 主框架
     * 构造方法
     */
    public MainFrame() {
        /**
         * 调用init方法
         */
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        /**
         * 关闭窗口的同时退出程序
         */
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setBounds(350,150,800,500);
        this.setTitle("Http服务器");
        /**
         * 设置窗口背景颜色，调色板调色
         */
        this.setBackground(new Color(251, 241, 241, 255));
        this.setResizable(false);

        /**
         * 设置主面板,面板可以看成一个空间，但是不能单独存在
         */
        JPanel contentPanel = (JPanel) this.getContentPane();
        /**
         * 设置内容面板未透明  true代表透明， 透明之后的gui界面是看不到背景图像的
         * 设置布局，null代表可以自己设置布局，避免是用默认的流水布局
         */
        contentPanel.setOpaque(false);
        contentPanel.setLayout(null);
        this.setContentPane(contentPanel);

        /**
         * 端口设置
         */
        labPort = new JLabel("监听端口");
        labPort.setForeground(Color.BLACK);
        labPort.setFont(new Font("楷体",Font.BOLD,17));
        labPort.setBounds(22,10,90,25);
        contentPanel.add(labPort);

        textPort = new JTextField("8088");
        textPort.setBounds(130,10,150,25);
        contentPanel.add(textPort);

        /**
         * 启动服务
         */
        btnStartServer = new JButton("启动服务");
        btnStartServer.setForeground(Color.BLACK);
        btnStartServer.setFont(new Font("楷体",Font.BOLD,17));
        btnStartServer.setBounds(300,10,120,25);
        contentPanel.add(btnStartServer);

        /**
         * 暂停服务
         */
        btnPushServer = new JButton("暂停服务");
        btnPushServer.setForeground(Color.BLACK);
        btnPushServer.setFont(new Font("楷体",Font.BOLD,17));
        btnPushServer.setBounds(440,10,120,25);
        btnPushServer.setEnabled(false);
        contentPanel.add(btnPushServer);

        /**
         * 停止服务
         */
        btnStopServer = new JButton("停止服务");
        btnStopServer.setForeground(Color.BLACK);
        btnStopServer.setFont(new Font("楷体",Font.BOLD,17));
        btnStopServer.setBounds(580,10,120,25);
        btnStopServer.setEnabled(false);
        contentPanel.add(btnStopServer);

        /**
         * 资源路径设置
         */
        labPath = new JLabel("资源位置");
        labPath.setForeground(Color.BLACK);
        labPath.setFont(new Font("楷体",Font.BOLD,17));
        labPath.setBounds(22,45,90,25);
        contentPanel.add(labPath);

        textPath = new JTextField("");
        /**
         * 设置默认的路径
         */
        textPath.setText(Data.resourcePath);
        textPath.setBounds(130,45,430,25);
        contentPanel.add(textPath);

        btnSetPath = new JButton("设置资源位置");
        btnSetPath.setForeground(Color.BLACK);
        btnSetPath.setFont(new Font("楷体",Font.BOLD,17));
        btnSetPath.setBounds(580,45,180,25);
        contentPanel.add(btnSetPath);

        /**
         * 控制台
         * 文本域，允许输入多行，创建一个包含指定文本的文本域
         */
        textArea = new JTextArea("                               --控制台--\r\n");
        textArea.setLineWrap(true);
        textArea.setForeground(Color.BLACK);
        textArea.setFont(new Font("楷体",Font.BOLD,18));
        textArea.setBackground(new Color(238, 246, 251, 255));
        scrollPane = new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        /**
         * 将文本域放到滚动窗口中
         */
        scrollPane.setBounds(20,80,750,350);
        contentPanel.add(scrollPane);

        /**
         * 设置资源文件夹按钮事件,设置一个监听器
         */
        btnSetPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int op = jfc.showDialog(MainFrame.this,"请选择静态资源文件夹");
                if(op==JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();

                    String filePath = file.getAbsolutePath();
                    textPath.setText(filePath);

                    Data.resourcePath = filePath;
                }
            }
        });

        /**
         * 启动按钮事件
         */
        btnStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.isRun = true;
                Data.isPush = false;

                int port = 8088;
                try{
                    port = Integer.parseInt(textPort.getText().trim());
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
                /**
                 * 创建Server并且启动
                 */
                Server server = new Server(port,MainFrame.this);
                new Thread(server).start();
                /**
                 * 修改按钮的状态
                 */
                btnStartServer.setEnabled(false);
                btnStopServer.setEnabled(true);
                btnPushServer.setEnabled(true);
            }
        });

        /**
         * 暂停按钮事件
         */
        btnPushServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Data.isPush){
                    Data.isPush = false;
                    btnPushServer.setText("暂停服务");
                    printLog("服务器继续运行！");
                }else {
                    Data.isPush = true;
                    btnPushServer.setText("继续运行");
                    printLog("服务器暂停运行！");
                }
            }
        });

        /**
         * 停止按钮事件
         */
        btnStopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.isRun = false;
                Data.isPush = false;

                int port = 8088;
                try{
                    port = Integer.parseInt(textPort.getText().trim());
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                /**
                 * 修改之后，服务还会监听一次。这里主动给监听服务发送一次请求
                 */
                try {
                    Socket socket = new Socket("127.0.0.1",port);
                }catch (Exception e1){
                    e1.printStackTrace();
                }
                btnStartServer.setEnabled(true);
                btnPushServer.setEnabled(false);
                btnStopServer.setEnabled(false);
            }
        });
        this.setVisible(true);
    }


    /**
     * 输出日志到控制台
     *
     * @param msg 信息
     */
    public void printLog(final String msg) {
        new Thread() {
            public void run(){
                String date = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date());
                String info = textArea.getText()+date + " " + msg + "\r\n";
                textArea.setText(info);
            }
        }.start();
    }
}