package org.anyline.entity; 
 
import org.anyline.util.BasicUtil; 
 
public class MapPoint {
	 
	private double lng;
	private double lat;
	private String provinceCode; 
	private String provinceNm; 
	private String cityCode; 
	private String cityNm; 
	private String countyCode; 
	private String countyNm; 
	private String street; 
	private String code; 
	private int level;
	private String address;


	public MapPoint(String location){
		if(BasicUtil.isNotEmpty(location)){ 
			String[] tmps = location.split(","); 
			if(tmps.length > 1){ 
				lng = BasicUtil.parseDouble(tmps[0],-1d);
				lat = BasicUtil.parseDouble(tmps[1],-1d);
			} 
		} 
	}
	public MapPoint(String lng, String lat){
		this.lng = BasicUtil.parseDouble(lng,0d);
		this.lat = BasicUtil.parseDouble(lat,0d);
	}
	public MapPoint(double lng, double lat){
		this.lng = lng;
		this.lat = lat;
	}
	public boolean isEmpty(){ 
		if(BasicUtil.isEmpty(lng) || BasicUtil.isEmpty(lat) || "-1".equals(lng) || "-1".equals(lat)){
			return true; 
		} 
		return false; 
	}

	public double getLng() {
		return this.lng;
	}

	public void setLng(final double lng) {
		this.lng = lng;
	}

	public double getLat() {
		return this.lat;
	}

	public void setLat(final double lat) {
		this.lat = lat;
	}

	public String getProvinceCode() {
		return this.provinceCode;
	}

	public void setProvinceCode(final String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getProvinceNm() {
		return this.provinceNm;
	}

	public void setProvinceNm(final String provinceNm) {
		this.provinceNm = provinceNm;
	}

	public String getCityCode() {
		return this.cityCode;
	}

	public void setCityCode(final String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCityNm() {
		return this.cityNm;
	}

	public void setCityNm(final String cityNm) {
		this.cityNm = cityNm;
	}

	public String getCountyCode() {
		return this.countyCode;
	}

	public void setCountyCode(final String countyCode) {
		this.countyCode = countyCode;
	}

	public String getCountyNm() {
		return this.countyNm;
	}

	public void setCountyNm(final String countyNm) {
		this.countyNm = countyNm;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(final String street) {
		this.street = street;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public int getLevel() {
		return this.level;
	}

	public void setLevel(final int level) {
		this.level = level;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}
	public String toString(){
		return "["+lng+","+lat+"]";
	}
}
