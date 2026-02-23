/*
 * Copyright 2006-2025 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.client.map;

import org.anyline.entity.Coordinate;
import org.anyline.entity.SRS;
import org.anyline.entity.geometry.Point;

import java.util.List;

public interface MapClient {

    /**
     * 通过IP地址获取其当前所在地理位置
     * @param ip ip
     * @return 坐标
     */
    Coordinate ip(String ip);

    Coordinate geo(String address, String city);

    /**
     * 根据地址解析 坐标
     * @param address 地址 用原文签名 用url encode后提交
     * @return Coordinate
     */
    Coordinate geo(String address);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param coordinate 坐标
     * @return Coordinate
     */
    Coordinate regeo(Coordinate coordinate);

    Coordinate regeo(Double lng, Double lat);

    Coordinate regeo(SRS srs, String[] point);

    Coordinate regeo(String[] point);

    Coordinate regeo(SRS srs, Double[] point);

    Coordinate regeo(Double[] point);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param srs 坐标系
     * @param lng 经度
     * @param lat 纬度
     * @return Coordinate
     */
    Coordinate regeo(SRS srs, Double lng, Double lat);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param srs 坐标系
     * @param lng 经度
     * @param lat 纬度
     * @return Coordinate
     */
    Coordinate regeo(SRS srs, String lng, String lat);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param srs 坐标系
     * @param point lng, lat 经度, 纬度
     * @return Coordinate
     */
    Coordinate regeo(SRS srs, String point);

    Coordinate regeo(String point);

    Coordinate regeo(String lng, String lat);

    /**
     * 附近poi
     * @param lng 经度
     * @param lat 经度
     * @param radius 半径
     * @param category 类别
     * @param keyword 关键定
     * @return List
     */
    List<Coordinate> poi(Double lng, Double lat, int radius, String category, String keyword);

    /**
     * 附近poi
     * @param city 城市
     * @param category 类别
     * @param keyword 关键定
     * @return List
     */
    List<Coordinate> poi(String city, String category, String keyword);

    /**
     * 范围内 poi
     * @param points 多边形
     * @param category 类别
     * @param keyword 关键定
     * @return List
     */
    List<Coordinate> poi(List<Point> points, String category, String keyword);
}
