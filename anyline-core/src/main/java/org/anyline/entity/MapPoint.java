package org.anyline.entity; 
 
import org.anyline.util.BasicUtil; 
 
public class MapPoint {


	public static enum COORD_TYPE{
		WGS84LL     			{public String getCode(){return "WGS84LL";} public String getName(){return "大地坐标系(GPS)";}},         //谷歌地图国外
		GCJ02LL			        {public String getCode(){return "GCJ02LL";} public String getName(){return "火星坐标系(国家测绘局制定)";}}, //谷歌地图国内,高德地图,腾讯地图
		BD09LL		            {public String getCode(){return "BD09LL";} public String getName(){return "百度坐标系";}},
		BD09MC		            {public String getCode(){return "BD09MC";} public String getName(){return "百度米制坐标系";}};
		public abstract String getCode();
		public abstract String getName();
	}
	
	private double lng;
	private double lat;
	private String provinceCode; 		//省
	private String provinceName;
	private String cityCode; 			//市
	private String cityName;
	private String districtCode;		//区
	private String districtName;
	private String townCode;			//街道
	private String townName;
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
	public MapPoint(){
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

	public String getProvinceName() {
		return this.provinceName;
	}

	public void setProvinceName(final String provinceName) {
		this.provinceName = provinceName;
	}

	public String getCityName() {
		return this.cityName;
	}

	public void setCityName(final String cityName) {
		this.cityName = cityName;
	}

	public String getDistrictName() {
		return this.districtName;
	}

	public void setDistrictName(final String districtName) {
		this.districtName = districtName;
	}

	public String getTownCode() {
		return this.townCode;
	}

	public void setTownCode(final String townCode) {
		this.townCode = townCode;
	}

	public String getTownName() {
		return this.townName;
	}

	public void setTownName(final String townName) {
		this.townName = townName;
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


	public String getCityCode() {
		return this.cityCode;
	}

	public void setCityCode(final String cityCode) {
		this.cityCode = cityCode;
	}


	public String getDistrictCode() {
		return this.districtCode;
	}

	public void setDistrictCode(final String districtCode) {
		this.districtCode = districtCode;
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
