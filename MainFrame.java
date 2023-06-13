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

public class MainFrame extends JFrame {

    //要使用的组件声明，可以作为公共的组件
    private JLabel labPort;//标签
    private JLabel labInfo;//标签
    private JLabel labPath;//标签
    private JTextField textPort;//文本框
    private JTextField textPath;//文本框
    private JButton btnStartServer;//按钮
    private JButton btnPushServer;//按钮
    private JButton btnStopServer;//按钮
    private JButton btnSetPath;//按钮
    private JPanel contentPanel;//面板

    private JScrollPane scrollPane;
    private JTextArea textArea;

    public static void main(String[] args) {
        //创建一个窗口对象
        MainFrame mf = new MainFrame();
    }
    //构造方法
    public MainFrame() {
        //调用init方法
        init();
    }

    private void init() {

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//关闭窗口的同时退出程序
        this.setBounds(350,150,800,500);//位于页面位置
        this.setTitle("Http服务器");//窗口题目
        /* this.setBackground(new Color(22, 40, 243));*/
        //设置窗口背景颜色，蓝色，可找调色板调色，创建的时候会默认添加一个rootpane（根窗口），面板和rootpane平级，rootpane把原来面板挡住，不能显示颜色
        this.setResizable(false);//窗口不能随意改变大小
        //窗口设置背景
        ImageIcon bg = new ImageIcon("D:\\java项目\\HttpServer\\src\\日落.jpg");
        JLabel label=new JLabel(bg);//新建一个标签存放背景
        label.setSize(bg.getIconWidth(), bg.getIconHeight());//设置背景与窗口一样大
        this.getLayeredPane().add(label, Integer.valueOf(Integer.MIN_VALUE));// 设置底层把图片放在窗口最下面的一层

        //设置主面板,面板可以看成一个空间，但是不能单独存在
        JPanel contentPanel = (JPanel) this.getContentPane();
        contentPanel.setOpaque(false); //设置内容面板未透明  true代表透明， 透明之后的gui界面是看不到背景图像的
        contentPanel.setLayout(null);//设置布局，null代表可以自己设置布局，避免是用默认的流水布局
        this.setContentPane(contentPanel);//将contentPanel设置为根容器，JFrame继承自Frame。JFrame不能通过add方法添加组件，因为其是一个框架而不是容器，添加组件只能用这种方法
        this.getContentPane().setBackground(Color.PINK);//把面板添加到窗口上，背景色就可以更改，并且两个不是平级

        //端口设置
        labPort = new JLabel("监听端口");
        labPort.setForeground(Color.BLACK);
        labPort.setFont(new Font("楷体",Font.BOLD,17));//字体加粗
        labPort.setBounds(22,10,90,25);
        contentPanel.add(labPort);

        textPort = new JTextField("8088");//文本框默认数据8088
        textPort.setBounds(130,10,150,25);
        contentPanel.add(textPort);

        //三个按钮
        btnStartServer = new JButton("启动服务");
        btnStartServer.setForeground(Color.BLACK);
        btnStartServer.setFont(new Font("楷体",Font.BOLD,17));//字体加粗
        btnStartServer.setBounds(300,10,120,25);
        contentPanel.add(btnStartServer);

        btnPushServer = new JButton("暂停服务");
        btnPushServer.setForeground(Color.BLACK);
        btnPushServer.setFont(new Font("楷体",Font.BOLD,17));//字体加粗
        btnPushServer.setBounds(440,10,120,25);
        btnPushServer.setEnabled(false);//设置该按钮一开始不可用
        contentPanel.add(btnPushServer);

        btnStopServer = new JButton("停止服务");
        btnStopServer.setForeground(Color.BLACK);
        btnStopServer.setFont(new Font("楷体",Font.BOLD,17));//字体加粗
        btnStopServer.setBounds(580,10,120,25);
        btnStopServer.setEnabled(false);//设置该按钮一开始不可用
        contentPanel.add(btnStopServer);

        //资源路径设置
        labPath = new JLabel("资源位置");
        labPath.setForeground(Color.BLACK);
        labPath.setFont(new Font("楷体",Font.BOLD,17));//字体加粗
        labPath.setBounds(22,45,90,25);
        contentPanel.add(labPath);

        textPath = new JTextField("");//文本框
        //设置默认的路径
        textPath.setText(Data.resourcePath);
        textPath.setBounds(130,45,430,25);
        contentPanel.add(textPath);

        btnSetPath = new JButton("设置资源位置");
        btnSetPath.setForeground(Color.BLACK);
        btnSetPath.setFont(new Font("楷体",Font.BOLD,17));//字体加粗
        btnSetPath.setBounds(580,45,180,25);
        contentPanel.add(btnSetPath);

        //控制台
        textArea = new JTextArea("                               --控制台--\r\n");//文本域，允许输入多行，创建一个包含指定文本的文本域
        textArea.setLineWrap(true);//允许自动换行
        textArea.setForeground(Color.BLACK);//设置文本域的字体颜色
        textArea.setFont(new Font("楷体",Font.BOLD,18));//字体加粗
        textArea.setBackground(Color.PINK);//设置背景颜色
        scrollPane = new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);//将文本域放到滚动窗口中
        scrollPane.setBounds(20,80,750,350);//文本域的位置
        contentPanel.add(scrollPane);

        //设置资源文件夹按钮事件,设置一个监听器
        btnSetPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();//文件选择
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int op = jfc.showDialog(MainFrame.this,"请选择静态资源文件夹");//得到选择的路径状态
                if(op==JFileChooser.APPROVE_OPTION) {//批准选择
                    File file = jfc.getSelectedFile();
                    //取出绝对路径
                    String filePath = file.getAbsolutePath();
                    textPath.setText(filePath);//将其写入文本框
                    //修改Data里面的静态变量
                    Data.resourcePath = filePath;
                }
            }
        });

        //启动按钮事件
        btnStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.isRun = true;
                Data.isPush = false;
                //获取端口
                int port = 8088;//默认端口号
                try{
                    port = new Integer(textPort.getText().trim());//端口号等于文本框输入的
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
                //创建Server并且启动
                Server server = new Server(port,MainFrame.this);
                new Thread(server).start();//新线程开始
                //修改按钮的状态
                btnStartServer.setEnabled(false);
                btnStopServer.setEnabled(true);
                btnPushServer.setEnabled(true);
            }
        });

        //暂停按钮事件
        btnPushServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Data.isPush){//暂停状态
                    Data.isPush = false;
                    btnPushServer.setText("暂停服务");
                    printLog("服务器继续运行！");
                }else {//非暂停状态
                    Data.isPush = true;
                    btnPushServer.setText("继续运行");
                    printLog("服务器暂停运行！");
                }
            }
        });

        //停止按钮事件
        btnStopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Data.isRun = false;
                Data.isPush = false;
                //获取端口
                int port = 8088;
                try{
                    port = new Integer(textPort.getText().trim());
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                //修改之后，服务还会监听一次。这里主动给监听服务发送一次请求
                try {
                    Socket socket = new Socket("127.0.0.1",port);//自动请求服务器
                }catch (Exception e1){
                    e1.printStackTrace();
                }
                //修改按钮的状态
                btnStartServer.setEnabled(true);
                btnPushServer.setEnabled(false);
                btnStopServer.setEnabled(false);
            }
        });
        this.setVisible(true);
    }


    //输出日志到控制台
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