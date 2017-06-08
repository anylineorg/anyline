package org.anyline.entity;

import org.anyline.util.BasicUtil;

public class MapLocation {
	private String lon;
	private String lat;
	public MapLocation(String location){
		if(BasicUtil.isNotEmpty(location)){
			String[] tmps = location.split(",");
			if(tmps.length > 1){
				lon = tmps[0];
				lat = tmps[1];
			}
		}
	}
	public MapLocation(String lon, String lat){
		this.lon = lon;
		this.lat = lat;
	}
	public boolean isEmpty(){
		if(BasicUtil.isEmpty(lon) || BasicUtil.isEmpty(lat) || "-1".equals(lon) || "-1".equals(lat)){
			return true;
		}
		return false;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLocation(){
		return lon + "," + lat;
	}
	public String getCenter(){
		return getLocation();
	}
	
}
