package org.anyline.util;

import java.math.BigDecimal;

public class MapUtil {
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
			BigDecimal decimal = new BigDecimal(s);  
			s = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
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
}
