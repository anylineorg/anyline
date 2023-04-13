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

	private Point point						; // 坐标点[lng,lat][经度,纬度]
	private Point center 					; // 最小级别行政区中心坐标点[lng,lat][经度,纬度]
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
	private boolean direct = false			; // 是否直辖市
	private boolean correct = false			; //

	public Coordinate(String location){
		if(BasicUtil.isNotEmpty(location)){
			String[] tmps = location.split(",");
			if(tmps.length > 1){
				point = new Point(BasicUtil.parseDouble(tmps[0],null), BasicUtil.parseDouble(tmps[1],null));
			}
		}
	}
	public Coordinate(){
	}
	public Coordinate(TYPE type, String lng, String lat){
		this.type = type;
		point = new Point(BasicUtil.parseDouble(lng, null),BasicUtil.parseDouble(lat, null));
	}
	public Coordinate(TYPE type, Double lng, Double lat){
		this.type = type;
		point = new Point(lng, lat);
	}
	public Coordinate(String lng, String lat){
		point = new Point(BasicUtil.parseDouble(lng,null),BasicUtil.parseDouble(lat,null));
	}
	public Coordinate(Double lng, Double lat){
		point = new Point(lng,lat);
	}
	public Coordinate convert(TYPE type){
		Double[] loc = GISUtil.convert(this.type, point.getX(), point.getY(), type);
		point = new Point(loc[0], loc[1]);
		this.setType(type);
		return this;
	}
	public boolean isEmpty(){ 
		if(null == point || null == point.getX() || null == point.getY()){
			return true; 
		} 
		return false; 
	}
	public void setLocation(String location){
		if(BasicUtil.isNotEmpty(location)){
			String[] tmps = location.split(",");
			if(tmps.length > 1){
				point = new Point(BasicUtil.parseDouble(tmps[0],null),BasicUtil.parseDouble(tmps[1],null));
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
		return this.point.getX();
	}

	public void setLng(Double lng) {
		if(null == point){
			point = new Point();
		}
		this.point.setX(lng);
	}
	public void setLng(String lng) {
		setLng(BasicUtil.parseDouble(lng, null));
	}

	public Double getLat() {
		return this.point.getY();
	}

	public void setLat(Double lat) {
		if(null == point){
			point = new Point();
		}
		point.setY(lat);
	}
	public void setLat(String lat) {
		setLat(BasicUtil.parseDouble(lat, null));
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
		return "["+point.getX()+","+point.getY()+"]";
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
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

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
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

	public Coordinate correct(){
		if(correct){
			return this;
		}
		String code = BasicUtil.evl(getVillageCode(), getTownCode(), getCountyCode(), getCityCode(), getProvinceCode());
		if(null == code){
			return this;
		}
		if(code.startsWith("11")
				||code.startsWith("12")
				||code.startsWith("31")
				||code.startsWith("50")
		){
			cityCode = countyCode;
			cityName = countyName;
			countyCode = townCode;
			countyName = townName;
			townCode = villageCode;
			townName = villageName;
			correct = true;
		}
		return this;
	}
}
