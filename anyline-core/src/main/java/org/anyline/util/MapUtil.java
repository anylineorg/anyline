package org.anyline.util;

import java.math.BigDecimal;

import org.anyline.entity.MapLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapUtil {
	private static final Logger log = LoggerFactory.getLogger(MapUtil.class);
	private static double EARTH_RADIUS = 6378.137;

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	/**
	 * 通过经纬度获取距离(单位：米)
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double distance(double lon1, double lat1, double lon2, double lat2) {
		try{
			double radLat1 = rad(lat1);
			double radLat2 = rad(lat2);
			double a = radLat1 - radLat2;
			double b = rad(lon1) - rad(lon2);
			double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
					+ Math.cos(radLat1) * Math.cos(radLat2)
					* Math.pow(Math.sin(b / 2), 2)));
			s = s * EARTH_RADIUS;
			s = Math.round(s * 10000d) / 10000d;
			s = s * 1000;
//			if(ConfigTable.isDebug()){
//				log.warn("\n\t[距离计算][LON1:"+lon1+"][LAT1:"+lat1+"][LON2:"+lon2+"][LAT2:"+lat2+"][DISTANCE:"+s+"]");
//			}
			BigDecimal decimal = new BigDecimal(s);  
			s = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	public static String distanceFormat(double lon1, double lat1, double lon2, double lat2) {
		double distance = distance(lon1, lat1, lon2, lat2);
		return distanceFormat(distance);
	}
	public static String distanceFormatCn(double lon1, double lat1, double lon2, double lat2) {
		double distance = distance(lon1, lat1, lon2, lat2);
		return distanceFormatCn(distance);
	}
	
	public static double distance(String lon1, String lat1, String lon2, String lat2) {
		double distance = -1;
		try{
			distance = distance(
					BasicUtil.parseDouble(lon1, -1.0),
					BasicUtil.parseDouble(lat1, -1.0),
					BasicUtil.parseDouble(lon2, -1.0),
					BasicUtil.parseDouble(lat2, -1.0)
					);
		}catch(Exception e){
			e.printStackTrace();
		}
		return distance;
	}
	public static String distanceFormat(String lon1, String lat1, String lon2, String lat2) {
		double distance = distance(lon1, lat1, lon2, lat2);
		return distanceFormat(distance);
	}
	public static String distanceFormatCn(String lon1, String lat1, String lon2, String lat2) {
		double distance = distance(lon1, lat1, lon2, lat2);
		return distanceFormatCn(distance);
	}

	
	public static double distance(MapLocation loc1, MapLocation loc2) {
		double distance = -1;
		try{
			distance = distance(
					BasicUtil.parseDouble(loc1.getLon(), -1.0),
					BasicUtil.parseDouble(loc1.getLat(), -1.0),
					BasicUtil.parseDouble(loc2.getLon(), -1.0),
					BasicUtil.parseDouble(loc2.getLat(), -1.0)
					);
		}catch(Exception e){
			e.printStackTrace();
		}
		return distance;
	}
	public static String distanceFormat(MapLocation loc1, MapLocation loc2) {
		double distance = distance(loc1.getLon(), loc1.getLat(), loc2.getLon(), loc2.getLat());
		return distanceFormat(distance);
	}
	public static String distanceFormatCn(MapLocation loc1, MapLocation loc2) {
		double distance = distance(loc1.getLon(), loc1.getLat(), loc2.getLon(), loc2.getLat());
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
	 * @param gps
	 * @return
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
}
