package org.anyline.util;

import org.anyline.entity.Coordinate;

public interface MapClient {

    /**
     * 通过IP地址获取其当前所在地理位置
     * @param ip ip
     * @return 坐标
     */
    public Coordinate ip(String ip);

    /**
     * 根据地址解析 坐标
     * @param address 地址 用原文签名 用url encode后提交
     * @return Coordinate
     */
    public Coordinate geo(String address);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param coordinate 坐标
     * @return Coordinate
     */
    public Coordinate regeo(Coordinate coordinate);

    public Coordinate regeo(double lng, double lat);
    public Coordinate regeo(String[] point);
    public Coordinate regeo(double[] point);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param lng 经度
     * @param lat 纬度
     * @return Coordinate
     */
    public Coordinate regeo(Coordinate.TYPE type, Double lng, Double lat);
    public Coordinate regeo(Coordinate.TYPE type, String lng, String lat);
    public Coordinate regeo(String lng, String lat);

}
