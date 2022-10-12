package org.anyline.entity; 
 
import org.anyline.util.BasicUtil;
import org.anyline.util.GISUtil;

import java.util.List;

public class Coordinate {


	public static enum TYPE {
		WGS84LL     {public String getCode(){return "WGS84LL";} public String getName(){return "大地坐标系";} 	public String getRemark(){return "GPS/国外谷歌";}},
		GCJ02LL		{public String getCode(){return "GCJ02LL";} public String getName(){return "火星坐标系";} 	public String getRemark(){return "国家测绘局制定(国内谷歌/高德/腾讯)";}},
		BD09LL		{public String getCode(){return "BD09LL";}  public String getName(){return "百度坐标系";} 	public String getRemark(){return "百度坐标系";}},
		BD09MC		{public String getCode(){return "BD09MC";}  public String getName(){return "百度米制坐标系";} 	public String getRemark(){return "百度米制坐标系";}};
		public abstract String getCode();
		public abstract String getName();
		public abstract String getRemark();
	}

	private Double[] point = new Double[2]	; // 坐标点[lng,lat][经度,纬度]
	private Double[] center= new Double[2]	; // 最小级别行政区中心坐标点[lng,lat][经度,纬度]
	private TYPE type						; // 坐标系
	private String provinceCode				; // 省编号
	private String provinceName				; // 省中文名
	private String cityCode					; // 市编号
	private String cityName					; // 市中文我
	private String countyCode				; // 区编号
	private String countyName				; // 区中文名
	private String townCode					; // 街道编号
	private String townName					; // 街道中文名
	private String villageCode				; // 社区(村)编号
	private String villageName				; // 社区(村)中文名
	private String street					; // 道路
	private String streetNumber				; // 门牌,号
	private List<Double[]> border			; // 最小级别行政区边界点
	private String code						; // 当前地区最小级别行政区编号
	private int level						; // 级别(国家:0,省:1)
	private String address					; // 详细地址
	private boolean success = true			; // 执行结果
	private String message = null			; // 执行结果说明
	private int reliability					; // 可信度参考：值范围 1 <低可信> - 10 <高可信>
	private int accuracy					; // 解析精度级别

	public Coordinate(String location){
		if(BasicUtil.isNotEmpty(location)){
			String[] tmps = location.split(",");
			if(tmps.length > 1){
				point[0] = BasicUtil.parseDouble(tmps[0],null);
				point[1] = BasicUtil.parseDouble(tmps[1],null);
			}
		}
	}
	public Coordinate(){
	}
	public Coordinate(TYPE type, String lng, String lat){
		this.type = type;
		point[0] = BasicUtil.parseDouble(lng, null);
		point[1] = BasicUtil.parseDouble(lat, null);
	}
	public Coordinate(TYPE type, double lng, double lat){
		this.type = type;
		point[0] = lng;
		point[1] = lat;
	}
	public Coordinate(String lng, String lat){
		point[0] = BasicUtil.parseDouble(lng,null);
		point[1] = BasicUtil.parseDouble(lat,null);
	}
	public Coordinate(double lng, double lat){
		point[0] = lng;
		point[1] = lat;
	}
	public Coordinate convert(TYPE type){
		this.point = GISUtil.convert(this.type, this.point[0], this.point[1], type);
		this.setType(type);
		return this;
	}
	public boolean isEmpty(){ 
		if(point.length != 2 || null == point[0] || null == point[1]){
			return true; 
		} 
		return false; 
	}
	public void setLocation(String location){
		if(BasicUtil.isNotEmpty(location)){
			String[] tmps = location.split(",");
			if(tmps.length > 1){
				point[0] = BasicUtil.parseDouble(tmps[0],null);
				point[1] = BasicUtil.parseDouble(tmps[1],null);
			}
		}
	}
	public String getProvinceName() {
		return this.provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getCityName() {
		return this.cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCountyName() {
		return this.countyName;
	}

	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}

	public String getTownCode() {
		return this.townCode;
	}

	public void setTownCode(String townCode) {
		this.townCode = townCode;
	}

	public String getTownName() {
		return this.townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}

	public Double getLng() {
		return this.point[0];
	}

	public void setLng(Double lng) {
		this.point[0] = lng;
	}
	public void setLng(String lng) {
		this.point[0] = BasicUtil.parseDouble(lng, null);
	}

	public Double getLat() {
		return this.point[1];
	}

	public void setLat(Double lat) {
		this.point[1] = lat;
	}
	public void setLat(String lat) {
		this.point[1] = BasicUtil.parseDouble(lat, null);
	}

	public String getProvinceCode() {
		return this.provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}


	public String getCityCode() {
		return this.cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}


	public String getCountyCode() {
		return this.countyCode;
	}

	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}


	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getLevel() {
		return this.level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	public String toString(){
		return "["+point[0]+","+point[1]+"]";
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public Double[] getPoint() {
		return point;
	}

	public void setPoint(Double[] point) {
		this.point = point;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Double[] getCenter() {
		return center;
	}

	public void setCenter(Double[] center) {
		this.center = center;
	}

	public String getVillageCode() {
		return villageCode;
	}

	public void setVillageCode(String villageCode) {
		this.villageCode = villageCode;
	}

	public String getVillageName() {
		return villageName;
	}

	public void setVillageName(String villageName) {
		this.villageName = villageName;
	}

	public List<Double[]> getBorder() {
		return border;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}

	public void setBorder(List<Double[]> border) {
		this.border = border;
	}

	public int getReliability() {
		return reliability;
	}

	public void setReliability(int reliability) {
		this.reliability = reliability;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
}
