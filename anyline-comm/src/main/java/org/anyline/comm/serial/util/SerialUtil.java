package org.anyline.comm.serial.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SerialUtil {
    private static Logger log = LoggerFactory.getLogger(SerialUtil.class);

    /**
     * 获得系统可用的端口名称列表
     * @return 可用端口名称列表
     */
    @SuppressWarnings("unchecked")
    public static List<String> ports(){
        List<String> ports = new ArrayList<>();
        //获得系统可用的端口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        while(portList.hasMoreElements()) {
            String name = portList.nextElement().getName();//获得端口的名字
            ports.add(name);
        }
        return ports;
    }

    /**
     * 开启串口
     * @param name 串口名称
     * @param rate 波特率
     * @return 串口对象
     */
    public static SerialPort open(String name,int rate, int timeout) {
        try {
            //通过端口名称得到端口
            CommPortIdentifier identifier = CommPortIdentifier.getPortIdentifier(name);
            //打开端口，（自定义名字，打开超时时间）
            CommPort port = identifier.open(name, timeout);
            //判断是不是串口
            if (port instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) port;
                //设置串口参数（波特率，数据位8，停止位1，校验位无）
                serialPort.setSerialPortParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                log.warn("[开启串口][名称:{}][波特率:{}]", name, rate);
                return serialPort;
            }
            else {
                //是其他类型的端口
                throw new NoSuchPortException();
            }
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭串口
     * @param port 要关闭的串口对象
     */
    public static void close(SerialPort port) {
        if(port != null) {
            port.close();
            port = null;
        }
    }

    /**
     * 向串口发送数据
     * @param port 串口对象
     * @param data 发送的数据
     */
    public static void send(SerialPort port, byte[] data) {
        OutputStream os = null;
        try {
            os = port.getOutputStream();//获得串口的输出流
            os.write(data);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                    os = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 向串口发送数据
     * @param port 串口对象
     * @param data 发送的数据
     */
    public static void send(SerialPort port, String data) {
        send(port, data.getBytes());
    }
    /**
     * 从串口读取数据
     * @param port 要读取的串口
     * @return 读取的数据
     */
    public static byte[] read(SerialPort port) {
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = port.getInputStream();//获得串口的输入流
            int len = is.available();//获得数据长度
            while (len != 0) {
                bytes = new byte[len];//初始化byte数组
                is.read(bytes);
                len = is.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
    public static String read(SerialPort port, String encode) {
        byte[] bytes = read(port);
        String str = null;
        try {
            str = new String(bytes, encode);
        }catch (Exception e){
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 给串口设置监听
     * @param port port
     * @param listener listener
     */
    public static void setListener(SerialPort port, SerialPortEventListener listener) {
        try {
            //给串口添加事件监听
            port.addEventListener(listener);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        port.notifyOnDataAvailable(true);//串口有数据监听
        port.notifyOnBreakInterrupt(true);//中断事件监听
    }

}