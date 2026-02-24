/*
 * Copyright 2006-2025 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.entity;
 
import org.anyline.entity.geometry.Point;
import org.anyline.util.BasicUtil;
import org.anyline.util.GISUtil;

import java.util.List;

public class Coordinate {
	private String id						; // id
	private String title					; // 标题(POI标题)
	private Point point						; // 坐标点[lng, lat][经度, 纬度]
	private Point center 					; // 最小级别行政区中心坐标点[lng, lat][经度, 纬度]
	private SRS srs							; // 坐标系
	private String poiCategoryCode			;
	private String poiCategoryName			;
	private String provinceCode				; // 省编号
	private String provinceName				; // 省中文名
	private String cityCode					; // 市编号
	private String cityName					; // 市中文我
	private String countyCode				; // 区(县)编号
	private String countyName				; // 区(县)中文名
	private String townCode					; // 街道(乡镇)编号
	private String townName					; // 街道(乡镇)中文名
	private String villageCode				; // 社区(村)编号
	private String villageName				; // 社区(村)中文名
	private String street					; // 道路
	private String streetNumber				; // 门牌, 号
	private List<Double[]> border			; // 最小级别行政区边界点
	private String code						; // 当前地区最小级别行政区编号
	private int level						; // 级别(国家:0, 省:1)
	private String address					; // 详细地址
	private boolean success = true			; // 执行结果
	private String message = null			; // 执行结果说明
	private int reliability					; // 可信度参考：值范围 1 <低可信> - 10 <高可信>
	private int accuracy					; // 解析精度级别
	private boolean direct = false			; // 是否直辖市
	private boolean correct = false			; //
	private DataRow metadata				;

	public Coordinate(String location) {
		if(BasicUtil.isNotEmpty(location)) {
			String[] tmps = location.split(",");
			if(tmps.length > 1) {
				point = new Point(BasicUtil.parseDouble(tmps[0], null), BasicUtil.parseDouble(tmps[1], null));
			}
		}
	}
	public Coordinate() {
	}
	public Coordinate(SRS srs, String lng, String lat) {
		this.srs = srs;
		point = new Point(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
	}
	public Coordinate(SRS srs, Double lng, Double lat) {
		this.srs = srs;
		point = new Point(lng, lat);
	}
	public Coordinate(String lng, String lat) {
		point = new Point(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
	}
	public Coordinate(Double lng, Double lat) {
		point = new Point(lng, lat);
	}
	public Coordinate convert(SRS srs) {
		Double[] loc = GISUtil.convert(this.srs, point.x(), point.y(), srs);
		point = new Point(loc[0], loc[1]);
		this.setSrs(srs);
		return this;
	}
	public boolean isEmpty() {
		if(null == point || null == point.x() || null == point.y()) {
			return true; 
		} 
		return false; 
	}
	public void setLocation(String location) {
		if(BasicUtil.isNotEmpty(location)) {
			String[] tmps = location.split(",");
			if(tmps.length > 1) {
				point = new Point(BasicUtil.parseDouble(tmps[0], null), BasicUtil.parseDouble(tmps[1], null));
			}
		}
	}

	public DataRow getMetadata() {
		return metadata;
	}

	public void setMetadata(DataRow metadata) {
		this.metadata = metadata;
	}
	public Object getMetadata(String key) {
		if(null != metadata) {
			return metadata.get(key);
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPoiCategoryCode() {
		return poiCategoryCode;
	}

	public void setPoiCategoryCode(String poiCategoryCode) {
		this.poiCategoryCode = poiCategoryCode;
	}

	public String getPoiCategoryName() {
		return poiCategoryName;
	}

	public void setPoiCategoryName(String poiCategoryName) {
		this.poiCategoryName = poiCategoryName;
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
		return this.point.x();
	}

	public void setLng(Double lng) {
		if(null == point) {
			point = new Point();
		}
		this.point.x(lng);
	}
	public void setLng(String lng) {
		setLng(BasicUtil.parseDouble(lng, null));
	}

	public Double getLat() {
		return this.point.y();
	}

	public void setLat(Double lat) {
		if(null == point) {
			point = new Point();
		}
		point.y(lat);
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
	public String toString() {
		return "["+point.x()+","+point.y()+"]";
	}

	public SRS getSrs() {
		return srs;
	}

	public void setSrs(SRS srs) {
		this.srs = srs;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Coordinate correct() {
		if(correct) {
			return this;
		}
		String code = BasicUtil.evl(getVillageCode(), getTownCode(), getCountyCode(), getCityCode(), getProvinceCode());
		if(null == code) {
			return this;
		}
		if(code.startsWith("11")
				||code.startsWith("12")
				||code.startsWith("31")
				||code.startsWith("50")
		) {
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
