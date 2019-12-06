package org.anyline.entity; 
 
import org.anyline.util.BasicUtil; 
 
public class MapLocation { 
	 
	private String lon; 
	private String lat; 
	private String provinceCode; 
	private String provinceNm; 
	private String cityCode; 
	private String cityNm; 
	private String countyCode; 
	private String countyNm; 
	private String street; 
	private String code; 
	private String level; 
	private String address; 
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
	public boolean isNotEmpty(){ 
		return !isEmpty(); 
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
	public String getProvinceCode() { 
		return provinceCode; 
	} 
	public void setProvinceCode(String provinceCode) { 
		this.provinceCode = provinceCode; 
	} 
	public String getProvinceNm() { 
		return provinceNm; 
	} 
	public void setProvinceNm(String provinceNm) { 
		this.provinceNm = provinceNm; 
	} 
	public String getCityCode() { 
		return cityCode; 
	} 
	public void setCityCode(String cityCode) { 
		this.cityCode = cityCode; 
	} 
	public String getCityNm() { 
		return cityNm; 
	} 
	public void setCityNm(String cityNm) { 
		this.cityNm = cityNm; 
	} 
	public String getCountyCode() { 
		return countyCode; 
	} 
	public void setCountyCode(String countyCode) { 
		this.countyCode = countyCode; 
	} 
	public String getCountyNm() { 
		return countyNm; 
	} 
	public void setCountyNm(String countyNm) { 
		this.countyNm = countyNm; 
	} 
	public String getStreet() { 
		return street; 
	} 
	public void setStreet(String street) { 
		this.street = street; 
	} 
	public String getCode() { 
		return code; 
	} 
	public void setCode(String code) { 
		this.code = code; 
	} 
	public String getLevel() { 
		return level; 
	} 
	public void setLevel(String level) { 
		this.level = level; 
	} 
	public String getAddress() { 
		return address; 
	} 
	public void setAddress(String address) { 
		this.address = address; 
	} 
	 
} 
