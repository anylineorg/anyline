package org.anyline.amap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpClientUtil;
import org.anyline.util.MD5Util;
import org.anyline.util.NumberUtil;
import org.apache.log4j.Logger;
/**
 * 高德云图
 * @author Administrator
 *
 */
public class AmapUtil {
	private static Logger log = Logger.getLogger(AmapUtil.class);

	/**
	 * 删除标注
	 * @param ids
	 * @return
	 */
	public static int delete(String ... ids){
		if(null == ids){
			return 0;
		}
		List<String> list = new ArrayList<String>();
		for(String id:ids){
			list.add(id);
		}
		return delete(list);
	}
	public static int delete(List<String> ids){
		int size = 0;
		if(null == ids || ids.size() ==0){
			return size;
		}
		String param = "";
		int length = ids.size();
		for(int i=0; i<length; i++){
			if(i==0){
				param += ids.get(i);
			}else{
				param += "," + ids.get(i);
			}
		}
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("ids", param);
		params.put("sig", sign(params));
		String url = "http://yuntuapi.amap.com/datamanage/data/delete";
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		if(ConfigTable.isDebug()){
			log.warn("[删除标注][param:"+BasicUtil.joinBySort(params)+"]");
		}
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("status")){
				String status = json.getString("status");
				if("1".equals(status)){
					size = json.getInt("success");
					log.warn("[删除标注完成][success:"+size+"][fail:"+json.getInt("fail")+"]");
				}else{
					log.warn("[更新标注失败][info:"+json.getString("info")+"]");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			size = 0;
		}
		return size;
	}
	/**
	 * 更新地图
	 * 需要清楚的数据设置成null
	 * 不需要更新的设置成""
	 * @param id
	 * @param name
	 * @param lon
	 * @param lat
	 * @param address
	 * @return
	 */
	public static boolean update(String id, String name, int loctype, String lon, String lat, String address, Map<String,String> extras){
		String url = "http://yuntuapi.amap.com/datamanage/data/update";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("loctype", loctype+"");
		Map<String,String> data = extras;
		if(null == data){
			data = new HashMap<String,String>();
		}
		data.put("_id", id);
		data.put("_name", name);
		if(BasicUtil.isNotEmpty(lon) && BasicUtil.isNotEmpty(lat)){
			data.put("_location", lon+","+lat);
		}
		if(BasicUtil.isNotEmpty(address)){
			data.put("_address", address);
		}
		params.put("data", JSONObject.fromObject(data).toString());
		params.put("sig", sign(params));
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		if(ConfigTable.isDebug()){
			log.warn("[更新标注][param:"+BasicUtil.joinBySort(params)+"]");
		}
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("status")){
				String status = json.getString("status");
				if("1".equals(status)){
					log.warn("[更新标注完成][id:"+id+"][name:"+name+"]");
				}else{
					log.warn("[更新标注失败][name:"+name+"][info:"+json.getString("info")+"]");
					return false;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean update(String id, String name, String lon, String lat, String address, Map<String,String> extras){
		return update(id, name, 1, lon, lat, address, extras);
	}
	public static boolean update(String id, String name, String lon, String lat, Map<String,String> extras){
		return update(id, name, 1, lon, lat, null, extras);
	}
	public static boolean update(String id, String name, int loctype, String lon, String lat, String address){
		return update(id, name, loctype, lon, lat, address, null);
	}
	public static boolean update(String id, String name, String lon, String lat, String address){
		return update(id, name, lon, lat, address, null);
	}
	public static boolean update(String id, String name, String lon, String lat){
		return update(id, name, lon, lat, null, null);
	}
	public static boolean update(String id, String name, String address){
		return update(id, name, null, null, address);
	}
	public static boolean update(String id, String name){
		return update(id, name, null);
	}
	/**
	 * 添加记录
	 * @param name
	 * @param loctype 1:经纬度 2:地址
	 * @param lon
	 * @param lat
	 * @param address
	 */
	public static String create(String name, int loctype, String lon, String lat, String address, Map<String, String> extras){
		String url = "http://yuntuapi.amap.com/datamanage/data/create";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("loctype", loctype+"");
		Map<String,String> data = extras;
		if(null == data){
			data = new HashMap<String,String>();
		}
		data.put("_name", name);
		if(BasicUtil.isNotEmpty(lon) && BasicUtil.isNotEmpty(lat)){
			data.put("_location", lon+","+lat);
		}
		if(BasicUtil.isNotEmpty(address)){
			data.put("_address", address);
		}
		params.put("data", JSONObject.fromObject(data).toString());
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		String id = null;
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("status")){
				String status = json.getString("status");
				if("1".equals(status) && json.has("_id")){
					id = json.getString("_id");
					log.warn("[添加标注完成][id:"+id+"][name:"+name+"]");
				}else{
					log.warn("[添加标注失败][name:"+name+"][info:"+json.getString("info")+"]");
					log.warn("[param:"+BasicUtil.joinBySort(params)+"]");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return id;
	}
	public static String create(String name, String lon, String lat, String address, Map<String,String> extras){
		return create(name, 1, lon, lat, address, extras);
	}
	public static String create(String name, String lon, String lat, Map<String,String> extras){
		return create(name, 1, lon, lat, null, extras);
	}
	public static String create(String name, int loctype, String lon, String lat, String address){
		return create(name, loctype, lon, lat, address, null);
	}
	public static String create(String name, String lon, String lat, String address){
		return create(name, lon, lat, address, null);
	}
	public static String create(String name, String lon, String lat){
		return create(name, lon, lat, null, null);
	}
	public static String create(String name, String address){
		return create(name, null, null, address);
	}
	
	/**
	 * 创建新地图
	 * @param name
	 * @return
	 */
	public static String createMap(String name){
		String tableId = null;
		String url = "http://yuntuapi.amap.com/datamanage/table/create";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("name", name);
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		JSONObject json = JSONObject.fromObject(txt);
		if(json.has("tableid")){
			tableId = json.getString("tableid");
			log.warn("[创建地图完成][tableid:"+tableId+"]");
		}else{
			log.warn("[创建地图失败][info:"+txt+"][param:"+BasicUtil.joinBySort(params)+"]");
		}
		return tableId;
	}
	/**
	 * 本地检索 检索指定云图tableid里，对应城市（全国/省/市/区县）范围的POI信息
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t1
	 * @param keywords
	 * @param city
	 * @param filter
	 * @param sortrule
	 * @param limit
	 * @param page
	 */
	public static DataSet local(String keywords, String city, String filter, String sortrule, int limit, int page){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datasearch/local";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("keywords", keywords);
		if(BasicUtil.isEmpty(city)){
			city = "全国";
		}
		params.put("city", city);
		params.put("filter", filter);
		params.put("sortrule", sortrule);
		limit = NumberUtil.getMin(limit, 100);
		params.put("limit", limit+"");
		page = NumberUtil.getMax(page, 1);
		params.put("page", page+"");
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				set = new DataSet();
				log.warn("[本地搜索失败][info:"+json.getString("info")+"]");
				log.warn("[本地搜索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[本地搜索失败][info:"+e.getMessage()+"]");
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}
	/**
	 * 周边搜索 在指定tableid的数据表内，搜索指定中心点和半径范围内，符合筛选条件的位置数据
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t2
	 * @param center
	 * @param radius
	 * @param keywords
	 * @param city
	 * @param filter
	 * @param sortrule
	 * @param limit
	 * @param page
	 * @return
	 */
	public static DataSet around(String center, String radius, String keywords, String city, String filter, String sortrule, int limit, int page){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datasearch/around";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("center", center);
		params.put("radius", radius);
		params.put("keywords", keywords);
		if(BasicUtil.isEmpty(city)){
			city = "全国";
		}
		params.put("city", city);
		params.put("filter", filter);
		params.put("sortrule", sortrule);
		limit = NumberUtil.getMin(limit, 100);
		params.put("limit", limit+"");
		page = NumberUtil.getMax(page, 1);
		params.put("page", page+"");
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				log.warn("[周边搜索失败][info:"+json.getString("info")+"]");
				log.warn("[周边搜索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set = new DataSet();
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[周边搜索失败][params:"+e.getMessage()+"]");
			e.printStackTrace();
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}

	/**
	 * 按条件检索数据（可遍历整表数据） 根据筛选条件检索指定tableid数据表中的数据
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t5
	 * @param keywords
	 * @param city
	 * @param filter
	 * @param sortrule
	 * @param limit
	 * @param page
	 */
	public static DataSet list(String filter, String sortrule, int limit, int page){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datamanage/data/list";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("filter", filter);
		params.put("sortrule", sortrule);
		limit = NumberUtil.getMin(limit, 100);
		params.put("limit", limit+"");
		page = NumberUtil.getMax(page, 1);
		params.put("page", page+"");
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				set = new DataSet();
				log.warn("[条件搜索失败][info:"+json.getString("info")+"]");
				log.warn("[条件搜索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[条件搜索失败][info:"+e.getMessage()+"]");
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}
	/**
	 * ID检索 在指定tableid的数据表内，查询对应数据id的数据详情
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t4
	 * API:在指定tableid的数据表内，查询对应数据id的数据详情
	 * @param id
	 * @return
	 */
	public DataRow info(String id){
		DataRow row = null;
		String url = "http://yuntuapi.amap.com/datasearch/id";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("_id", id);
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				DataSet set = DataSet.parseJson(json.getJSONArray("datas"));
				if(set.size() > 0){
					row = set.getRow(0);
				}
			}else{
				log.warn("[周边搜索失败][info:"+json.getString("info")+"]");
				log.warn("[周边搜索失败][params:"+BasicUtil.joinBySort(params)+"]");
			}
		}catch(Exception e){
			log.warn("[周边搜索失败][params:"+e.getMessage()+"]");
			e.printStackTrace();
		}
		return row;
	}
	/**
	 * 省数据分布检索 检索指定云图tableid里，全表数据或按照一定查询或筛选过滤而返回的数据中，含有数据的省名称（中文名称）和对应POI个数（count）的信息列表，按照count从高到低的排序展现
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t6
	 * @param keywords 关键字 必须
	 * @param country ""或null时 默认:中国
	 * @praam filter 条件
	 * @return
	 */
	public static DataSet statByProvince(String keywords, String country, String filter){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datasearch/statistics/province";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("filter", filter);
		params.put("keywords", keywords);
		country = BasicUtil.evl(country, "中国")+"";
		params.put("country", country);
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				set = new DataSet();
				log.warn("[数据分布检索失败][info:"+json.getString("info")+"]");
				log.warn("[数据分布检索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[数据分布检索失败][info:"+e.getMessage()+"]");
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}

	/**
	 * 市数据分布检索 检索指定云图tableid里，全表数据或按照一定查询或筛选过滤而返回的数据中，含有数据的市名称（中文名称）和对应POI个数（count）的信息列表，按照count从高到低的排序展现
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t6
	 * @param keywords 关键字 必须
	 * @param province ""或null时 默认:全国
	 * @praam filter 条件
	 * @return
	 */
	public static DataSet statByCity(String keywords, String province, String filter){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datasearch/statistics/city";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("filter", filter);
		params.put("keywords", keywords);
		province = BasicUtil.evl(province, "全国")+"";
		params.put("country", province);
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				set = new DataSet();
				log.warn("[数据分布检索失败][info:"+json.getString("info")+"]");
				log.warn("[数据分布检索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[数据分布检索失败][info:"+e.getMessage()+"]");
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}

	/**
	 * 区数据分布检索 检索指定云图tableid里，在指定的省，市下面全表数据或按照一定查询或筛选过滤而返回的数据中，所有区县名称（中文名称）和对应POI个数（count）的信息列表，按照count从高到低的排序展现
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t6
	 * @param keywords 关键字 必须
	 * @param province 
	 * @param city 
	 * @praam filter 条件
	 * @return
	 */
	public static DataSet statByDistrict(String keywords, String province, String city, String filter){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datasearch/statistics/province";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("tableid", AmapConfig.TABLE_ID);
		params.put("filter", filter);
		params.put("keywords", keywords);
		params.put("province", province);
		params.put("city", city);
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				set = new DataSet();
				log.warn("[数据分布检索失败][info:"+json.getString("info")+"]");
				log.warn("[数据分布检索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[数据分布检索失败][info:"+e.getMessage()+"]");
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}
	/**
	 * 检索1个中心点，周边一定公里范围内（直线距离或者导航距离最大10公里），一定时间范围内（最大24小时）上传过用户位置信息的用户，返回用户标识，经纬度，距离中心点距离。
	 * @param center
	 * @param radius
	 * @param searchtype
	 * @param limit
	 * @param timerange
	 * @return
	 */
	public DataSet nearby(String center, String radius, int limit, int timerange ){
		DataSet set = null;
		String url = "http://yuntuapi.amap.com/datasearch/statistics/province";
		Map<String,String> params = new HashMap<String,String>();
		params.put("key", AmapConfig.KEY);
		params.put("center", center);
		params.put("radius", radius);
		params.put("searchtype", "0");
		params.put("limit", NumberUtil.getMin(limit, 100)+"");
		params.put("timerange", BasicUtil.evl(timerange,"1800")+"");
		String sign = sign(params);
		params.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", params).getText();
		try{
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("datas")){
				set = DataSet.parseJson(json.getJSONArray("datas"));
			}else{
				set = new DataSet();
				log.warn("[附近检索失败][info:"+json.getString("info")+"]");
				log.warn("[附近检索失败][params:"+BasicUtil.joinBySort(params)+"]");
				set.setException(new Exception(json.getString("info")));
			}
		}catch(Exception e){
			log.warn("[附近检索失败][info:"+e.getMessage()+"]");
			set = new DataSet();
			set.setException(e);
		}
		return set;
	}
//	public static void main(String[] args) {
//		ConfigTable.put("AMAP_CONFIG_FILE", "D:\\develop\\git\\anyline\\anyline_amap\\config\\anyline-amap.xml");
//		create(BasicUtil.getRandomCnString(3), BasicUtil.getRandomNumber(100, 120)+".00","35.00",BasicUtil.getRandomCnString(15));
//		create(BasicUtil.getRandomCnString(3), "110.00",BasicUtil.getRandomNumber(25, 40)+".00",BasicUtil.getRandomCnString(15));
//		//update("9","test","120.00","35.00","山东青岛");
//		//delete("18");
//		//createMap("aa");
//		DataSet set = local("地","","","",100, 1);
//		System.out.println(set.toJSON());
//	}
	/**
	 * 签名
	 * @param params
	 * @return
	 */
	public static String sign(Map<String,String> params){
		String sign = "";
		sign = BasicUtil.joinBySort(params) + AmapConfig.PRIVATE_KEY;
		sign = MD5Util.sign(sign,"UTF-8");
		return sign;
	}
}