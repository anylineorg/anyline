package org.anyline.entity; 
 
import org.anyline.util.BasicUtil;
import org.anyline.util.GISUtil;

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

	private Double[] point = new Double[2]	; //坐标点[lng,lat][经度,纬度]
	private TYPE type						; //坐标系
	private String provinceCode				; //省
	private String provinceName				;
	private String cityCode					; //市
	private String cityName					;
	private String districtCode				; //区
	private String districtName				;
	private String townCode					; //街道
	private String townName					;
	private String code						;
	private int level						;
	private String address					;


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

	public String getDistrictName() {
		return this.districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
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

	public double getLng() {
		return this.point[0];
	}

	public void setLng(double lng) {
		this.point[0] = lng;
	}
	public void setLng(String lng) {
		this.point[0] = BasicUtil.parseDouble(lng, null);
	}

	public double getLat() {
		return this.point[1];
	}

	public void setLat(double lat) {
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


	public String getDistrictCode() {
		return this.districtCode;
	}

	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
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
}
