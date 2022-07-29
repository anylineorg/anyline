package org.anyline.util;

import org.anyline.entity.MapPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GISUtil {

    private static final Logger log = LoggerFactory.getLogger(GISUtil.class);
    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
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
    public static double distance(double lng1, double lat1, double lng2, double lat2) {
        try{
            double radLat1 = rad(lat1);
            double radLat2 = rad(lat2);
            double a = radLat1 - radLat2;
            double b = rad(lng1) - rad(lng2);
            double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            s = Math.round(s * 10000d) / 10000d;
            s = s * 1000;
            BigDecimal decimal = new BigDecimal(s);
            s = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return s;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }
    public static String distanceFormat(double lng1, double lat1, double lng2, double lat2) {
        double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormat(distance);
    }
    public static String distanceFormatCn(double lng1, double lat1, double lng2, double lat2) {
        double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormatCn(distance);
    }

    public static double distance(String lng1, String lat1, String lng2, String lat2) {
        double distance = -1;
        try{
            distance = distance(
                    BasicUtil.parseDouble(lng1, -1.0),
                    BasicUtil.parseDouble(lat1, -1.0),
                    BasicUtil.parseDouble(lng2, -1.0),
                    BasicUtil.parseDouble(lat2, -1.0)
            );
        }catch(Exception e){
            e.printStackTrace();
        }
        return distance;
    }
    public static String distanceFormat(String lng1, String lat1, String lng2, String lat2) {
        double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormat(distance);
    }
    public static String distanceFormatCn(String lng1, String lat1, String lng2, String lat2) {
        double distance = distance(lng1, lat1, lng2, lat2);
        return distanceFormatCn(distance);
    }


    public static double distance(MapPoint loc1, MapPoint loc2) {
        double distance = -1;
        try{
            distance = distance(
                    BasicUtil.parseDouble(loc1.getLng(), -1.0),
                    BasicUtil.parseDouble(loc1.getLat(), -1.0),
                    BasicUtil.parseDouble(loc2.getLng(), -1.0),
                    BasicUtil.parseDouble(loc2.getLat(), -1.0)
            );
        }catch(Exception e){
            e.printStackTrace();
        }
        return distance;
    }
    public static String distanceFormat(MapPoint loc1, MapPoint loc2) {
        double distance = distance(loc1.getLng(), loc1.getLat(), loc2.getLng(), loc2.getLat());
        return distanceFormat(distance);
    }
    public static String distanceFormatCn(MapPoint loc1, MapPoint loc2) {
        double distance = distance(loc1.getLng(), loc1.getLat(), loc2.getLng(), loc2.getLat());
        return distanceFormatCn(distance);
    }

    public static String distanceFormat(double distance){
        String result = distance+"m";
        if(distance > 1000){
            result = NumberUtil.format(distance/1000,"0.00") +"km";
        }
        return result;
    }
    public static String distanceFormatCn(double distance){
        String result = distance+"米";
        if(distance > 1000){
            result = NumberUtil.format(distance/1000,"0.00") +"千米";
        }
        return result;
    }
    /**
     * gps转经纬度
     * @param gps  gps
     * @return String
     */
    public static String parseGPS(String gps){
        String result = null;
        if(null == gps){
            return null;
        }
        gps = gps.replaceAll("[^0-9.]", "");
        String d = gps.substring(0, gps.indexOf("."));
        String m = "";
        int idx = d.length() - 2;
        d = gps.substring(0,idx);
        m = gps.substring(idx);
        BigDecimal dd = BasicUtil.parseDecimal(d, 0d);
        BigDecimal dm = BasicUtil.parseDecimal(m, 0d).divide(new BigDecimal(60), 7, BigDecimal.ROUND_UP);
        result = dd.add(dm).toString();
        return result;
    }

     /*
     * WGS-84 GPS坐标（谷歌地图国外）
     * GCJ-02 国测局坐标（谷歌地图国内，高德地图）
     * BD-09 百度坐标（百度地图）
      */
    /* 地球半径,单位米（北京54 长半轴） */
    private static final double RADIUS = 6378245;

    /* 扁率 */
    private static final double EE = 0.00669342162296594323;

    private static final double PI = Math.PI;

    private static final double X_PI = Math.PI * 3000.0 / 180.0;

    public static double[] bd2gcj(double[] bd_lngLat) {
        return bd2gcj(bd_lngLat[0], bd_lngLat[1]);
    }

    public static double[] bd2gcj(double bd_lng, double bd_lat) {
        double x = bd_lng - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[]{gg_lng, gg_lat};
    }

    public static double[] gcj2bd(double[] lngLat) {
        return gcj2bd(lngLat[0], lngLat[1]);
    }

    public static double[] gcj2bd(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[]{bd_lng, bd_lat};
    }
    public static double[] wgs2bd(double[] lngLat){
        return wgs2bd(lngLat[0], lngLat[1]);
    }
    public static double[] wgs2bd(double lng, double lat){
        return gcj2bd(wgs2gcj(lng, lat));
    }

    public static double[] wgs2gcj(double[] lngLat) {
        return wgs2gcj(lngLat[0], lngLat[1]);
    }

    public static double[] wgs2gcj(double lng, double lat) {
        if (inChina(lng, lat)) {
            double dlat = lat(lng - 105.0, lat - 35.0);
            double dlng = lng(lng - 105.0, lat - 35.0);
            double radlat = lat / 180.0 * PI;
            double magic = Math.sin(radlat);
            magic = 1 - EE * magic * magic;
            double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((RADIUS * (1 - EE)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (RADIUS / sqrtmagic * Math.cos(radlat) * PI);
            double mglat = lat + dlat;
            double mglng = lng + dlng;
            return new double[]{mglng, mglat};
        } else {
            return new double[]{lng, lat};
        }
    }

    public static double[] gcj2wgs(double[] lngLat) {
        return gcj2wgs(lngLat[0], lngLat[1]);
    }

    public static double[] gcj2wgs(double lng, double lat) {
        if (inChina(lng, lat)) {
            double dlat = lat(lng - 105.0, lat - 35.0);
            double dlng = lng(lng - 105.0, lat - 35.0);
            double radlat = lat / 180.0 * PI;
            double magic = Math.sin(radlat);
            magic = 1 - EE * magic * magic;
            double sqrtmagic = Math.sqrt(magic);
            dlat = (dlat * 180.0) / ((RADIUS * (1 - EE)) / (magic * sqrtmagic) * PI);
            dlng = (dlng * 180.0) / (RADIUS / sqrtmagic * Math.cos(radlat) * PI);
            double mglat = lat + dlat;
            double mglng = lng + dlng;
            return new double[]{lng * 2 - mglng, lat * 2 - mglat};
        } else {
            return new double[]{lng, lat};
        }
    }

    private static double lat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double lng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    public static boolean inChina(double lng, double lat) {
        // 纬度3.86~53.55,经度73.66~135.05
        return (lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55);
    }


    /**
     * 坐标点是否在多边形内
     * @param point 检测点
     * @param points 多边形边界点
     * @return boolean
     */
    public static boolean pnpoly(MapPoint point, List<MapPoint> points) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(MapPoint p:points){
            lngs.add(p.getLng());
            lats.add(p.getLat());
        }
        return pnpoly(point.getLng(), point.getLat(), lngs, lats);
    }
    public static boolean pnpoly(MapPoint point, MapPoint ... points) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(MapPoint p:points){
            lngs.add(p.getLng());
            lats.add(p.getLat());
        }
        return pnpoly(point.getLng(), point.getLat(), lngs, lats);
    }
    /**
     * 坐标点是否在多边形内
     * @param lng lng
     * @param lat lat
     * @param points 边界点
     * @return boolean
     */
    public static boolean pnpoly(double lng, double lat, List<Double[]> points) {
        List<Double> lngs = new ArrayList<>();
        List<Double> lats = new ArrayList<>();
        for(Double[] point:points){
            lngs.add(point[0]);
            lats.add(point[1]);
        }
        return pnpoly(lng, lat, lngs, lats);
    }
    public static boolean pnpoly(double x, double y, List<Double> xs, List<Double> ys) {
        if (CollectionUtils.isEmpty(xs) || CollectionUtils.isEmpty(ys)) {
            return false;
        }
        double maxX = xs.stream().max(Comparator.comparingDouble(Double::doubleValue)).get();
        double maxY = ys.stream().max(Comparator.comparingDouble(Double::doubleValue)).get();
        double minX = xs.stream().min(Comparator.comparingDouble(Double::doubleValue)).get();
        double minY = ys.stream().min(Comparator.comparingDouble(Double::doubleValue)).get();

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
