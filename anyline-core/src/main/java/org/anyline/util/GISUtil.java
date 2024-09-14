/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.util;

import org.anyline.entity.Coordinate;
import org.anyline.entity.SRS;
import org.anyline.entity.geometry.Point;
import org.anyline.entity.geometry.Ring;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GISUtil {

    private static final Log log = LogProxy.get(GISUtil.class);
    private static Double EARTH_RADIUS = 6378.137;

    /*
     * WGS-84 GPS坐标（谷歌地图国外）
     * GCJ-02 国测局坐标（谷歌地图国内, 高德地图, 腾讯地图）
     * BD-09 百度坐标（百度地图）
     */
    private static Double rad(Double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 通过经纬度获取距离(单位:米)
     *
     * @param lat1  lat1
     * @param lng1  lng1
     * @param lat2  lat2
     * @param lng2  lng2
     * @return distance
     */
    public static Double distance(Double lng1, Double lat1, Double lng2, Double lat2) {
        try{
            Double radLat1 = rad(lat1);
            Double radLat2 = rad(lat2);
            Double a = radLat1 - radLat2;
            Double b = rad(lng1) - rad(lng2);
            Double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            s = Math.round(s * 10000d) / 10000d;
            s = s * 1000;
            BigDecimal decimal = new BigDecimal(s);
            s = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return s;
        }catch(Exception e) {
            log.error("distance exception:", e);
            return null;
        }
    }
    public static String distanceFormat(Double lng1, Double lat1, Double lng2, Double lat2) {
        Double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormat(distance);
    }
    public static String distanceFormatCn(Double lng1, Double lat1, Double lng2, Double lat2) {
        Double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormatCn(distance);
    }

    public static Double distance(String lng1, String lat1, String lng2, String lat2) {
        Double distance = null;
        try{
            distance = distance(
                    BasicUtil.parseDouble(lng1, null),
                    BasicUtil.parseDouble(lat1, null),
                    BasicUtil.parseDouble(lng2, null),
                    BasicUtil.parseDouble(lat2, null)
            );
        }catch(Exception e) {
            log.error("distance exception:", e);
        }
        return distance;
    }
    public static String distanceFormat(String lng1, String lat1, String lng2, String lat2) {
        Double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormat(distance);
    }
    public static String distanceFormatCn(String lng1, String lat1, String lng2, String lat2) {
        Double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormatCn(distance);
    }

    public static Double distance(Coordinate loc1, Coordinate loc2) {
        Double distance = null;
        try{
            distance = distance(
                    BasicUtil.parseDouble(loc1.getLng(), null),
                    BasicUtil.parseDouble(loc1.getLat(), null),
                    BasicUtil.parseDouble(loc2.getLng(), null),
                    BasicUtil.parseDouble(loc2.getLat(), null)
            );
        }catch(Exception e) {
            log.error("distance exception:", e);
        }
        return distance;
    }
    public static String distanceFormat(Coordinate loc1, Coordinate loc2) {
        Double distance = distance(loc1.getLng(), loc1.getLat(), loc2.getLng(), loc2.getLat());
        return distanceFormat(distance);
    }
    public static String distanceFormatCn(Coordinate loc1, Coordinate loc2) {
        Double distance = distance(loc1.getLng(), loc1.getLat(), loc2.getLng(), loc2.getLat());
        return distanceFormatCn(distance);
    }

    public static String distanceFormat(Double distance) {
        String result = distance+"m";
        if(distance > 1000) {
            result = NumberUtil.format(distance/1000, "0.00") +"km";
        }
        return result;
    }
    public static String distanceFormatCn(Double distance) {
        String result = distance+"米";
        if(distance > 1000) {
            result = NumberUtil.format(distance/1000, "0.00") +"千米";
        }
        return result;
    }

    /**
     * gps转经纬度
     * @param gps  gps
     * @return String
     */
    public static String parseGPS(String gps) {
        String result = null;
        if(null == gps) {
            return null;
        }
        gps = gps.replaceAll("[^0-9.]","");
        String d = gps.substring(0, gps.indexOf("."));
        String m = "";
        int idx = d.length() - 2;
        d = gps.substring(0, idx);
        m = gps.substring(idx);
        BigDecimal dd = BasicUtil.parseDecimal(d, 0d);
        BigDecimal dm = BasicUtil.parseDecimal(m, 0d).divide(new BigDecimal(60), 7, BigDecimal.ROUND_UP);
        result = dd.add(dm).toString();
        return result;
    }

     /*
     * WGS-84 GPS坐标（谷歌地图国外）
     * GCJ-02 国测局坐标（谷歌地图国内, 高德地图, 腾讯地图）
     * BD-09 百度坐标（百度地图）
      */
    /* 地球半径, 单位米（北京54 长半轴） */
    private static final Double RADIUS = 6378245D;

    /* 扁率 */
    private static final Double EE = 0.00669342162296594323;

    private static final Double PI = Math.PI;

    private static final Double X_PI = Math.PI * 3000.0 / 180.0;

    /**
     * src坐标系转成tar坐标系
     * @param src src
     * @param lng lng
     * @param lat lat
     * @param tar tar
     * @return Double
     */
    public static Double[] convert(SRS src, Double lng, Double lat, SRS tar) {
        Double[] location = new Double[2];
        if(src == tar) {
            location[0] = lng;
            location[1] = lat;
            return location;
        }
        if(tar == SRS.GCJ02LL) {
            if(src == SRS.WGS84LL) {
                location = wgs2gcj(lng, lat);
            }else if(src == SRS.BD09LL) {
                location = bd2gcj(lng, lat);
            }
        }else if(tar == SRS.WGS84LL) {
            if(src == SRS.GCJ02LL) {
                location = gcj2wgs(lng, lat);
            }else if(src == SRS.BD09LL) {
                location = bd2wgs(lng, lat);
            }
        }else if(tar == SRS.BD09LL) {
            if(src == SRS.GCJ02LL) {
                location = gcj2bd(lng, lat);
            }else if(src == SRS.WGS84LL) {
                location = wgs2bd(lng, lat);
            }
        }
        return location;
    }

    public static Double[] convert(SRS src, String lng, String lat, SRS tar) {
        return convert(src, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null), tar );
    }

    public static Double[] convert(SRS src, String[] location, SRS tar) {
        return convert(src, location[0], location[1], tar );
    }
    public static Double[] convert(SRS src, Double[] location, SRS tar) {
        return convert(src, location[0], location[1], tar );
    }

    public static Double[] bd2gcj(Double[] location) {
        return bd2gcj(location[0], location[1]);
    }
    public static Double[] bd2gcj(String[] location) {
        return bd2gcj(location[0], location[1]);
    }

    public static Double[] bd2gcj(String lng, String lat) {
        return bd2gcj(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    public static Double[] bd2gcj(Double lng, Double lat) {
        Double x = lng - 0.0065;
        Double y = lat - 0.006;
        Double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        Double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        Double gg_lng = z * Math.cos(theta);
        Double gg_lat = z * Math.sin(theta);
        return new Double[]{gg_lng, gg_lat};
    }

    public static Double[] bd2wgs(Double[] location) {
        return bd2wgs(location[0], location[1]);
    }
    public static Double[] bd2wgs(String[] location) {
        return bd2wgs(location[0], location[1]);
    }

    public static Double[] bd2wgs(String lng, String lat) {
        return bd2wgs(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    public static Double[] bd2wgs(Double lng, Double lat) {
        return gcj2wgs(bd2gcj(lng, lat));
    }

    public static Double[] gcj2bd(Double[] location) {
        return gcj2bd(location[0], location[1]);
    }

    public static Double[] gcj2bd(String[] location) {
        return gcj2bd(location[0], location[1]);
    }
    public static Double[] gcj2bd(String lng, String lat) {
        return gcj2bd(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    public static Double[] gcj2bd(Double lng, Double lat) {
        Double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        Double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        Double bd_lng = z * Math.cos(theta) + 0.0065;
        Double bd_lat = z * Math.sin(theta) + 0.006;
        return new Double[]{bd_lng, bd_lat};
    }

    public static Double[] wgs2bd(Double[] location) {
        return wgs2bd(location[0], location[1]);
    }

    public static Double[] wgs2bd(String[] location) {
        return wgs2bd(location[0], location[1]);
    }

    public static Double[] wgs2bd(String lng, String lat) {
        return gcj2bd(wgs2gcj(lng, lat));
    }
    public static Double[] wgs2bd(Double lng, Double lat) {
        return gcj2bd(wgs2gcj(lng, lat));
    }

    public static Double[] wgs2gcj(Double[] location) {
        return wgs2gcj(location[0], location[1]);
    }

    public static Double[] wgs2gcj(String[] location) {
        return wgs2gcj(location[0], location[1]);
    }

    public static Double[] wgs2gcj(String lng, String lat) {
        return wgs2gcj(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    public static Double[] wgs2gcj(Double lng, Double lat) {
        if (inChina(lng, lat)) {
            Double dlat = lat(lng - 105.0, lat - 35.0);
            Double dlng = lng(lng - 105.0, lat - 35.0);
            Double radlat = lat / 180.0 * PI;
            Double magic = Math.sin(radlat);
            magic = 1 - EE * magic * magic;
            Double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((RADIUS * (1 - EE)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (RADIUS / sqrtmagic * Math.cos(radlat) * PI);
            Double mglat = lat + dlat;
            Double mglng = lng + dlng;
            return new Double[]{mglng, mglat};
        } else {
            return new Double[]{lng, lat};
        }
    }

    public static Double[] gcj2wgs(Double[] location) {
        return gcj2wgs(location[0], location[1]);
    }

    public static Double[] gcj2wgs(String[] location) {
        return gcj2wgs(location[0], location[1]);
    }

    public static Double[] gcj2wgs(String lng, String lat) {
        return gcj2wgs(BasicUtil.parseDouble(lng, 0d), BasicUtil.parseDouble(lat, 0d));
    }
    public static Double[] gcj2wgs(Double lng, Double lat) {
        if (inChina(lng, lat)) {
            Double dlat = lat(lng - 105.0, lat - 35.0);
            Double dlng = lng(lng - 105.0, lat - 35.0);
            Double radlat = lat / 180.0 * PI;
            Double magic = Math.sin(radlat);
            magic = 1 - EE * magic * magic;
            Double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((RADIUS * (1 - EE)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (RADIUS / sqrtmagic * Math.cos(radlat) * PI);
            Double mglat = lat + dlat;
            Double mglng = lng + dlng;
            return new Double[]{lng * 2 - mglng, lat * 2 - mglat};
        } else {
            return new Double[]{lng, lat};
        }
    }

    private static Double lat(Double lng, Double lat) {
        Double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static Double lng(Double lng, Double lat) {
        Double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    public static boolean inChina(Double lng, Double lat) {
        // 纬度3.86~53.55, 经度73.66~135.05
        return (lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55);
    }

    /**
     * 坐标点是否在多边形内
     * @param point 检测点
     * @param ring 多边形边
     * @return boolean
     */
    public static boolean pnpoly(Point point, Ring ring) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(Point p:ring.getPoints()) {
            lngs.add(p.x());
            lats.add(p.y());
        }
        return pnpoly(point.x(), point.y(), lngs, lats);
    }
    public static boolean pnpoly(Point point, Point... points) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(Point p:points) {
            lngs.add(p.x());
            lats.add(p.y());
        }
        return pnpoly(point.x(), point.y(), lngs, lats);
    }
    public static boolean pnpoly(Point point, List<Point> points) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(Point p:points) {
            lngs.add(p.x());
            lats.add(p.y());
        }
        return pnpoly(point.x(), point.y(), lngs, lats);
    }

    /**
     * 坐标点是否在多边形内
     * @param lng lng
     * @param lat lat
     * @param points 边界点
     * @return boolean
     */
    public static boolean pnpoly(Double lng, Double lat, List<Double[]> points) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(Double[] point:points) {
            lngs.add(point[0]);
            lats.add(point[1]);
        }
        return pnpoly(lng, lat, lngs, lats);
    }
    public static boolean pnpoly(Double x, Double y, List<Double> xs, List<Double> ys) {
        if (null == xs || xs.isEmpty() || null == ys || ys.isEmpty()) {
            return false;
        }
        Double maxX = xs.stream().max(Comparator.comparingDouble(Double::doubleValue)).get();
        Double maxY = ys.stream().max(Comparator.comparingDouble(Double::doubleValue)).get();
        Double minX = xs.stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        Double minY = ys.stream().min(Comparator.comparingDouble(Double::doubleValue)).get();

        if (x < minX || x > maxX || y < minY || y > maxY) {
            return false;
        }
        int i, j;
        boolean result = false;
        int n = xs.size();
        Double[] vertx = xs.toArray(new Double[0]);
        Double[] verty = ys.toArray(new Double[0]);
        for (i = 0, j = n - 1; i < n; j = i++) {
            if ((verty[i] > y) != (verty[j] > y) &&
                    (x < (vertx[j] - vertx[i]) * (y - verty[i]) / (verty[j] - verty[i]) + vertx[i])) {
                result = !result;
            }
        }
        return result;
    }

}
