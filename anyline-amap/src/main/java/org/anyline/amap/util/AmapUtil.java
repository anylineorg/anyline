package org.anyline.amap.util; 
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.MapLocation;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.impl.PageNaviImpl;
import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.util.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** 
 * 高德云图 
 * @author zh 
 * 
 */ 
public class AmapUtil { 
	private static final Logger log = LoggerFactory.getLogger(AmapUtil.class); 
 
	private String key = AmapConfig.KEY; 
	private String privateKey = AmapConfig.PRIVATE_KEY; 
	private String table = AmapConfig.TABLE_ID; 
	private static Map<String,AmapUtil> pool = new Hashtable<String,AmapUtil>(); 
 
	static{ 
		AmapUtil def = new AmapUtil(); 
		pool.put(def.table, def); 
	} 
	public static AmapUtil getInstance(String key, String privateKey, String table){ 
		AmapUtil util = new AmapUtil(); 
		util.key =  key; 
		util.privateKey = privateKey; 
		util.table = table; 
		return util; 
	} 
	public static AmapUtil getInstance(){ 
		return getInstance(AmapConfig.TABLE_ID); 
	} 
	public static AmapUtil getInstance(String table){ 
		AmapUtil util = pool.get(table); 
		if(null ==util){ 
			util = new AmapUtil(); 
			util.table = table; 
			pool.put(table, util); 
		} 
		return util; 
	} 
	public static AmapUtil defaultInstance(){ 
		return pool.get(AmapConfig.TABLE_ID); 
	} 
	 
 
	/** 
	 * 添加记录 
	 * @param name  name
	 * @param loctype 1:经纬度 2:地址 
	 * @param lon  lon
	 * @param lat  lat
	 * @param address  address
	 * @param extras  extras
	 * @return return
	 */ 
	public String create(String name, int loctype, String lon, String lat, String address, Map<String, Object> extras){ 
		String url = "http://yuntuapi.amap.com/datamanage/data/create"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("loctype", loctype+""); 
		Map<String,Object> data = new HashMap<String, Object>(); 
		if(null != extras){ 
			Iterator<String> keys = extras.keySet().iterator(); 
			while(keys.hasNext()){ 
				String key = keys.next(); 
				Object value = extras.get(key); 
				if(BasicUtil.isNotEmpty(value)){ 
					data.put(key, value); 
				} 
			} 
		} 
		data.put("_name", name); 
		if(BasicUtil.isNotEmpty(lon) && BasicUtil.isNotEmpty(lat)){ 
			data.put("_location", lon+","+lat); 
		} 
		if(BasicUtil.isNotEmpty(address)){ 
			data.put("_address", address); 
		} 
		params.put("data", BeanUtil.map2json(data)); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		String id = null; 
		try{ 
			DataRow row = DataRow.parseJson(txt);
			if(row.containsKey("status")){ 
				String status = row.getString("status"); 
				if("1".equals(status) && row.containsKey("_id")){ 
					id = row.getString("_id"); 
					log.warn("[添加标注完成][id:{}][name:{}]",id,name); 
				}else{ 
					log.warn("[添加标注失败][name:{}][info:{}]", name, row.getString("info")); 
					log.warn("[param:{}]",HttpUtil.param(params)); 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
		return id; 
	} 
	public String create(String name, String lon, String lat, String address, Map<String,Object> extras){ 
		return create(name, 1, lon, lat, address, extras); 
	} 
	public String create(String name, String lon, String lat, Map<String,Object> extras){ 
		return create(name, 1, lon, lat, null, extras); 
	} 
	public String create(String name, int loctype, String lon, String lat, String address){ 
		return create(name, loctype, lon, lat, address, null); 
	} 
	public String create(String name, String lon, String lat, String address){ 
		return create(name, lon, lat, address, null); 
	} 
	public String create(String name, String lon, String lat){ 
		return create(name, lon, lat, null, null); 
	} 
	public String create(String name, String address){ 
		return create(name, null, null, address); 
	} 
	 
	 
	/** 
	 * 删除标注 
	 * @param ids  ids
	 * @return return
	 */ 
	public int delete(String ... ids){ 
		if(null == ids){ 
			return 0; 
		} 
		List<String> list = new ArrayList<String>(); 
		for(String id:ids){ 
			list.add(id); 
		} 
		return delete(list); 
	} 
	public int delete(List<String> ids){ 
		int cnt = 0; 
		if(null == ids || ids.size() ==0){ 
			return cnt; 
		} 
		String param = ""; 
		int size = ids.size(); 
		//一次删除最多50条 大于50打后拆分数据 
		if(size > 50){ 
			int navi = (size-1)/50 + 1; 
			for(int i=0; i<navi; i++){			 
				int fr = i*50; 
				int to = i*50 + 49; 
				if(to > size-1){ 
					to = size - 1; 
				} 
				List<String> clds = ids.subList(fr, to); 
				cnt += delete(clds); 
			} 
			return cnt; 
		} 
		 
		for(int i=0; i<size; i++){ 
			if(i==0){ 
				param += ids.get(i); 
			}else{ 
				param += "," + ids.get(i); 
			} 
		} 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("ids", param); 
		params.put("sig", sign(params)); 
		String url = "http://yuntuapi.amap.com/datamanage/data/delete"; 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[删除标注][param:{}]",HttpUtil.param(params)); 
		} 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("status")){ 
				String status = json.getString("status"); 
				if("1".equals(status)){ 
					cnt = json.getInt("success"); 
					log.warn("[删除标注完成][success:{}][fail:{}]", cnt,json.getInt("fail")); 
				}else{ 
					log.warn("[删除标注失败][info:{}]",json.getString("info")); 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
			cnt = -1; 
		} 
		return cnt; 
	} 
	/** 
	 * 更新地图 
	 * @param id  id
	 * @param name  name
	 * @param loctype  loctype
	 * @param lon  lon
	 * @param lat  lat
	 * @param address  address
	 * @param extras  extras
	 * @return return 0:更新失败,没有对应的id  1:更新完成  -1:异常 
	 */ 
	public int update(String id, String name, int loctype, String lon, String lat, String address, Map<String,Object> extras){ 
		int cnt = 0; 
		String url = "http://yuntuapi.amap.com/datamanage/data/update"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("loctype", loctype+""); 
 
		Map<String,Object> data = new HashMap<String, Object>(); 
		if(null != extras){ 
			Iterator<String> keys = extras.keySet().iterator(); 
			while(keys.hasNext()){ 
				String key = keys.next(); 
				Object value = extras.get(key); 
				data.put(key, value); 
			} 
		} 
		data.put("_id", id); 
		data.put("_name", name); 
		if(BasicUtil.isNotEmpty(lon) && BasicUtil.isNotEmpty(lat)){ 
			data.put("_location", lon+","+lat); 
		} 
		if(BasicUtil.isNotEmpty(address)){ 
			data.put("_address", address); 
		} 
		params.put("data", BeanUtil.map2json(data)); 
		params.put("sig", sign(params)); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[更新标注][param:{}]",HttpUtil.param(params)); 
		} 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("status")){ 
				String status = json.getString("status"); 
				if("1".equals(status)){ 
					cnt = 1; 
					log.warn("[更新标注完成][id:{}][name:{}]",id,name); 
				}else{ 
					log.warn("[更新标注失败][name:{}][info:{}]",name,json.getString("info")); 
					cnt = 0; 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
			cnt = -1; 
		} 
		return cnt; 
	} 
	public int update(String id, String name, String lon, String lat, String address, Map<String,Object> extras){ 
		return update(id, name, 1, lon, lat, address, extras); 
	} 
	public int update(String id, String name, String lon, String lat, Map<String,Object> extras){ 
		return update(id, name, 1, lon, lat, null, extras); 
	} 
	public int update(String id, String name, int loctype, String lon, String lat, String address){ 
		return update(id, name, loctype, lon, lat, address, null); 
	} 
	public int update(String id, String name, String lon, String lat, String address){ 
		return update(id, name, lon, lat, address, null); 
	} 
	public int update(String id, String name, String lon, String lat){ 
		return update(id, name, lon, lat, null, null); 
	} 
	public int update(String id, String name, String address){ 
		return update(id, name, null, null, address); 
	} 
	public int update(String id, String name){ 
		return update(id, name, null); 
	} 
	 
	/** 
	 * 创建新地图 
	 * @param name  name
	 * @return return
	 */ 
	public String createTable(String name){ 
		String tableId = null; 
		String url = "http://yuntuapi.amap.com/datamanage/table/create"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("name", name); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		DataRow json = DataRow.parseJson(txt); 
		if(json.containsKey("tableid")){ 
			tableId = json.getString("tableid"); 
			log.warn("[创建地图完成][tableid:{}]",tableId); 
		}else{ 
			log.warn("[创建地图失败][info:{}][param:{}]",txt,HttpUtil.param(params)); 
		} 
		return tableId; 
	} 
	/** 
	 * 本地检索 检索指定云图tableid里，对应城市（全国/省/市/区县）范围的POI信息 
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t1 
	 * @param keywords  keywords
	 * @param city  city
	 * @param filter  filter
	 * @param sortrule  sortrule
	 * @param limit  limit
	 * @param page  page
	 * @return return
	 */ 
	public DataSet local(String keywords, String city, String filter, String sortrule, int limit, int page){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datasearch/local"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("keywords", keywords); 
		if(BasicUtil.isEmpty(city)){ 
			city = "全国"; 
		} 
		params.put("city", city); 
		params.put("filter", filter); 
		params.put("sortrule", sortrule); 
		limit = NumberUtil.min(limit, 100); 
		params.put("limit", limit+""); 
		page = NumberUtil.max(page, 1); 
		params.put("page", page+""); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		PageNavi navi = new PageNaviImpl(); 
		navi.setCurPage(page); 
		navi.setPageRows(limit); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("count")){ 
				navi.setTotalRow(json.getInt("count")); 
			} 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
			}else{ 
				set = new DataSet(); 
				log.warn("[本地搜索失败][info:{}]",json.getString("info")); 
				log.warn("[本地搜索失败][params:{}]",HttpUtil.param(params)); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[本地搜索失败][info:{}]",e.getMessage()); 
			set = new DataSet(); 
			set.setException(e); 
		} 
		set.setNavi(navi); 
		log.warn("[本地搜索][size:{}]",navi.getTotalRow()); 
		return set; 
	} 
	/** 
	 * 周边搜索 在指定tableid的数据表内，搜索指定中心点和半径范围内，符合筛选条件的位置数据 
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t2 
	 * @param center  center
	 * @param radius 查询半径 
	 * @param keywords 关键词 
	 * @param filters 过滤条件 
	 * @param sortrule 排序 
	 * @param limit 每页多少条 
	 * @param page 第几页 
	 * @return return
	 */ 
	public DataSet around(String center, int radius, String keywords, Map<String,String> filters, String sortrule, int limit, int page){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datasearch/around"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("center", center); 
		params.put("radius", radius+""); 
		if(BasicUtil.isNotEmpty(keywords)){ 
			params.put("keywords", keywords); 
		} 
		//过滤条件 
		if(null != filters && !filters.isEmpty()){ 
			String filter = ""; 
			Iterator<String> keys = filters.keySet().iterator(); 
			while(keys.hasNext()){ 
				String key = keys.next(); 
				String value = filters.get(key); 
				if(BasicUtil.isEmpty(value)){ 
					continue; 
				} 
				if("".equals(filter)){ 
					filter = key + ":" + value;  
				}else{ 
					filter = filter + "+" + key + ":" + value; 
				} 
			} 
			if(!"".equals(filter)){ 
				params.put("filter", filter); 
			} 
		} 
		if(BasicUtil.isNotEmpty(sortrule)){ 
			params.put("sortrule", sortrule); 
		} 
		limit = NumberUtil.min(limit, 100); 
		params.put("limit", limit+""); 
		page = NumberUtil.max(page, 1); 
		params.put("page", page+""); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		PageNavi navi = new PageNaviImpl(); 
		navi.setCurPage(page); 
		navi.setPageRows(limit); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("count")){ 
				navi.setTotalRow(json.getInt("count")); 
			} 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
			}else{ 
				log.warn("[周边搜索失败][info:{}]",json.getString("info")); 
				log.warn("[周边搜索失败][params:{}]",HttpUtil.param(params)); 
				set = new DataSet(); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[周边搜索失败][error:{}]",e.getMessage()); 
			e.printStackTrace(); 
			set = new DataSet(); 
			set.setException(e); 
		} 
		set.setNavi(navi); 
		log.warn("[周边搜索][size:{}]",navi.getTotalRow()); 
		return set; 
	} 
 
	public DataSet around(String center, int radius, Map<String,String> filters, String sortrule, int limit, int page){ 
		return around(center, radius, null, filters, sortrule, limit, page); 
	} 
	public DataSet around(String center, int radius, Map<String,String> filters, int limit, int page){ 
		return around(center, radius, null, filters, null, limit, page); 
	} 
	public DataSet around(String center, int radius, Map<String,String> filters, int limit){ 
		return around(center, radius, null, filters, null, limit, 1); 
	} 
	public DataSet around(String center, int radius, String keywords, String sortrule, int limit, int page){ 
		Map<String,String> filter = new HashMap<String,String>(); 
		return around(center, radius, keywords, filter, sortrule, limit, page); 
	} 
	 
	public DataSet around(String center, int radius, String keywords, int limit, int page){ 
		return around(center, radius, keywords, "", limit, page); 
	} 
	public DataSet around(String center, int radius, int limit, int page){ 
		return around(center, radius, "", limit, page); 
	} 
	public DataSet around(String center, int radius, int limit){ 
		return around(center, radius, "", limit, 1); 
	} 
	public DataSet around(String center, int radius){ 
		return around(center, radius, "", 100, 1); 
	} 
	public DataSet around(String center){ 
		return around(center, ConfigTable.getInt("AMAP_MAX_RADIUS")); 
	} 
	/** 
	 * 按条件检索数据（可遍历整表数据） 根据筛选条件检索指定tableid数据表中的数据 
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t5
	 * AmapUtil.getInstance(TABLE_TENANT).list("tenant_id:1","shop_id:1", 10, 1);  
	 * @param filter 查询条件 
	 * filter=key1:value1+key2:[value2,value3]   
	 * filter=type:酒店+star:[3,5]  等同于SQL语句的： WHERE type = "酒店" AND star BETWEEN 3 AND 5  
	 * @param sortrule 排序条件  
	 * 支持按用户自选的字段（仅支持数值类型字段）升降序排序。1：升序，0：降序  
	 * 若不填升降序，默认按升序排列。 示例：按年龄age字段升序排序 sortrule = age:1 
	 * @param limit 每页最大记录数为100 
	 * @param page 当前页数 &gt;=1 
	 * @return return
	 */ 
	public DataSet list(String filter, String sortrule, int limit, int page){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datamanage/data/list"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("filter", filter); 
		if(BasicUtil.isNotEmpty(sortrule)){ 
			params.put("sortrule", sortrule); 
		} 
		limit = NumberUtil.min(limit, 100); 
		params.put("limit", limit+""); 
		page = NumberUtil.max(page, 1); 
		params.put("page", page+""); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		PageNavi navi = new PageNaviImpl(); 
		navi.setCurPage(page); 
		navi.setPageRows(limit); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("count")){ 
				navi.setTotalRow(json.getInt("count")); 
			} 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
				if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
					log.warn("[条件搜索][结果数量:{}]",set.size());	 
				} 
			}else{ 
				set = new DataSet(); 
				log.warn("[条件搜索失败][info:{}]",json.getString("info")); 
				log.warn("[条件搜索失败][params:{}]",HttpUtil.param(params)); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[条件搜索失败][error:{}]",e.getMessage()); 
			set = new DataSet(); 
			set.setException(e); 
		} 
		set.setNavi(navi); 
		log.warn("[条件搜索][size:{}]",navi.getTotalRow()); 
		return set; 
	} 
	/** 
	 * ID检索 在指定tableid的数据表内，查询对应数据id的数据详情 
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t4 
	 * API:在指定tableid的数据表内，查询对应数据id的数据详情 
	 * @param id  id
	 * @return return
	 */ 
	public DataRow info(String id){ 
		DataRow row = null; 
		String url = "http://yuntuapi.amap.com/datasearch/id"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("_id", id); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("datas")){ 
				DataSet set = json.getSet("datas"); 
				if(set.size() > 0){ 
					row = set.getRow(0); 
				} 
			}else{ 
				log.warn("[周边搜索失败][info:{}]",json.getString("info")); 
				log.warn("[周边搜索失败][params:{}]",HttpUtil.param(params)); 
			} 
		}catch(Exception e){ 
			log.warn("[周边搜索失败][error:{}]",e.getMessage()); 
			e.printStackTrace(); 
		} 
		return row; 
	} 
	/** 
	 * 省数据分布检索 检索指定云图tableid里，全表数据或按照一定查询或筛选过滤而返回的数据中，含有数据的省名称（中文名称）和对应POI个数（count）的信息列表，按照count从高到低的排序展现 
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t6 
	 * @param keywords 关键字 必须 
	 * @param country ""或null时 默认:中国 
	 * @param filter 条件 
	 * @return return
	 */ 
	public DataSet statByProvince(String keywords, String country, String filter){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datasearch/statistics/province"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("filter", filter); 
		params.put("keywords", keywords); 
		country = BasicUtil.evl(country, "中国")+""; 
		params.put("country", country); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
			}else{ 
				set = new DataSet(); 
				log.warn("[数据分布检索失败][info:{}]",json.getString("info")); 
				log.warn("[数据分布检索失败][params:{}]",HttpUtil.param(params)); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[数据分布检索失败][error:{}]",e.getMessage()); 
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
	 * @param filter 条件 
	 * @return return
	 */ 
	public DataSet statByCity(String keywords, String province, String filter){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datasearch/statistics/city"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("filter", filter); 
		params.put("keywords", keywords); 
		province = BasicUtil.evl(province, "全国")+""; 
		params.put("country", province); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
			}else{ 
				set = new DataSet(); 
				log.warn("[数据分布检索失败][info:{}]",json.getString("info")); 
				log.warn("[数据分布检索失败][params:{}]",HttpUtil.param(params)); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[数据分布检索失败][error:{}]",e.getMessage()); 
			set = new DataSet(); 
			set.setException(e); 
		} 
		return set; 
	} 
 
	/** 
	 * 区数据分布检索 检索指定云图tableid里，在指定的省，市下面全表数据或按照一定查询或筛选过滤而返回的数据中，所有区县名称（中文名称）和对应POI个数（count）的信息列表，按照count从高到低的排序展现 
	 * API:http://lbs.amap.com/yuntu/reference/cloudsearch/#t6 
	 * @param keywords 关键字 必须 
	 * @param province   province
	 * @param city   city
	 * @param filter 条件 
	 * @return return
	 */ 
	public DataSet statByDistrict(String keywords, String province, String city, String filter){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datasearch/statistics/province"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("tableid", this.table); 
		params.put("filter", filter); 
		params.put("keywords", keywords); 
		params.put("province", province); 
		params.put("city", city); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
			}else{ 
				set = new DataSet(); 
				log.warn("[数据分布检索失败][info:{}]",json.getString("info")); 
				log.warn("[数据分布检索失败][params:{}]",HttpUtil.param(params)); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[数据分布检索失败][error:{}]",e.getMessage()); 
			set = new DataSet(); 
			set.setException(e); 
		} 
		return set; 
	} 
	/** 
	 * 检索1个中心点，周边一定公里范围内（直线距离或者导航距离最大10公里），一定时间范围内（最大24小时）上传过用户位置信息的用户，返回用户标识，经纬度，距离中心点距离。 
	 * @param center  center
	 * @param radius  radius
	 * @param limit  limit
	 * @param timerange  timerange
	 * @return return
	 */ 
	public DataSet nearby(String center, String radius, int limit, int timerange ){ 
		DataSet set = null; 
		String url = "http://yuntuapi.amap.com/datasearch/statistics/province"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("center", center); 
		params.put("radius", radius); 
		params.put("searchtype", "0"); 
		params.put("limit", NumberUtil.min(limit, 100)+""); 
		params.put("timerange", BasicUtil.evl(timerange,"1800")+""); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.post(url, "UTF-8", params).getText(); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			if(json.containsKey("datas")){ 
				set = json.getSet("datas"); 
			}else{ 
				set = new DataSet(); 
				log.warn("[附近检索失败][info:}{}]",json.getString("info")); 
				log.warn("[附近检索失败][params:{}]",HttpUtil.param(params)); 
				set.setException(new Exception(json.getString("info"))); 
			} 
		}catch(Exception e){ 
			log.warn("[附近检索失败][error:{}]",e.getMessage()); 
			set = new DataSet(); 
			set.setException(e); 
		} 
		return set; 
	} 
	/** 
	 * 按坐标查地址 
	 * @param location  location
	 * @return return
	 */ 
	public DataRow regeo(String location){ 
		DataRow row = null; 
		String url = "http://restapi.amap.com/v3/geocode/regeo"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("location", location); 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.get(url, "UTF-8", params).getText(); 
		try{ 
			row = DataRow.parseJson(txt); 
			if(null != row){ 
				row = row.getRow("regeocode"); 
				if(null != row){ 
					DataRow addressComponent = row.getRow("addressComponent"); 
					if(null != addressComponent){ 
						addressComponent.put("address", row.getString("formatted_address")); 
						row = addressComponent; 
					}else{ 
						row.put("address", row.getString("formatted_address")); 
					} 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
		return row; 
	} 
	/** 
	 * 根据地址查坐标 
	 * @param address  address
	 * @param city  city
	 * @return return
	 */ 
	public MapLocation geo(String address, String city){ 
		MapLocation location = null; 
		String url = "http://restapi.amap.com/v3/geocode/geo"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("address", address); 
		if(BasicUtil.isNotEmpty(city)){ 
			params.put("city", city); 
		} 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.get(url, "UTF-8", params).getText(); 
		try{ 
			DataRow json = DataRow.parseJson(txt); 
			DataSet set = null; 
			if(json.containsKey("geocodes")){ 
				set = json.getSet("geocodes"); 
				if(set.size()>0){ 
					DataRow row = set.getRow(0); 
					location = new MapLocation(row.getString("LOCATION")); 
					location.setCode(row.getString("ADCODE")); 
					location.setProvinceCode(BasicUtil.cut(row.getString("ADCODE"),0,4)); 
					location.setProvinceNm(row.getString("PROVINCE")); 
					location.setCityCode(row.getString("CITYCODE")); 
					location.setCityNm(row.getString("CITY")); 
					location.setCountyCode(row.getString("ADCODE")); 
					location.setCountyNm(row.getString("DISTRICT")); 
					location.setStreet(row.getString("STREET")); 
					location.setAddress(row.getString("FORMATTED_ADDRESS")); 
					location.setLevel(row.getString("LEVEL")); 
				} 
			}else{ 
				log.warn("[坐标查询失败][info:{}][params:{}]",json.getString("info"),HttpUtil.param(params)); 
			} 
		}catch(Exception e){ 
			log.warn("[坐标查询失败][error:{}]",e.getMessage()); 
		} 
		return location; 
	} 
	public MapLocation geo(String address){ 
		return geo(address, null); 
		 
	} 
	/** 
	 * 驾车路线规划 
	 * http://lbs.amap.com/api/webservice/guide/api/direction#driving			
	 * @param origin		出发地  origin		出发地
	 * @param destination	目的地  destination	目的地
	 * @param points		途经地 最多支持16个 坐标点之间用";"分隔 
	 * @param strategy		选路策略  0，不考虑当时路况，返回耗时最短的路线，但是此路线不一定距离最短 
	 *							  1，不走收费路段，且耗时最少的路线 
	 *							  2，不考虑路况，仅走距离最短的路线，但是可能存在穿越小路/小区的情况 			   
	 * @return return
	 */ 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataRow directionDrive(String origin, String destination, String points, int strategy){ 
		DataRow row = null; 
		String url = "http://restapi.amap.com/v3/direction/driving"; 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("key", this.key); 
		params.put("origin", origin); 
		params.put("destination", destination); 
		params.put("strategy", strategy+""); 
		if(BasicUtil.isNotEmpty(points)){ 
			params.put("points", points); 
		} 
		String sign = sign(params); 
		params.put("sig", sign); 
		String txt = HttpUtil.get(url, "UTF-8", params).getText(); 
		try{ 
			row = DataRow.parseJson(txt); 
			DataRow route = row.getRow("route"); 
			if(null != route){ 
				List paths = route.getList("PATHS"); 
				if(paths.size()>0){ 
					DataRow path = (DataRow)paths.get(0); 
					row = path; 
					List<DataRow> steps = (List<DataRow>)path.getList("steps"); 
					List<String> polylines = new ArrayList<String>(); 
					for(DataRow step:steps){ 
						String polyline = step.getString("polyline"); 
						String[] tmps = polyline.split(";"); 
						for(String tmp:tmps){ 
							polylines.add(tmp); 
						} 
					} 
					row.put("polylines", polylines); 
				} 
			} 
		}catch(Exception e){ 
			log.warn("[线路规划失败][error:{}]",e.getMessage()); 
		} 
		return row; 
	} 
	public DataRow directionDrive(String origin, String destination){ 
		return directionDrive(origin, destination, null, 0); 
	} 
	public DataRow directionDrive(String origin, String destination, String points){ 
		return directionDrive(origin, destination, points, 0); 
	} 
	/** 
	 * 签名 
	 * @param params  params
	 * @return return
	 */ 
	public String sign(Map<String,Object> params){ 
		String sign = ""; 
		sign = HttpUtil.param(params) + this.privateKey; 
		sign = MD5Util.sign(sign,"UTF-8"); 
		return sign; 
	} 
}
