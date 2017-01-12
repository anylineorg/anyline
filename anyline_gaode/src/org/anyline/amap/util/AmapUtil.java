package org.anyline.amap.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.util.HttpClientUtil;
import org.anyline.util.MD5Util;
import org.apache.log4j.Logger;
/**
 * 高德云图
 * @author Administrator
 *
 */
public class AmapUtil {
	private static Logger log = Logger.getLogger(AmapUtil.class);
	
	/**
	 * 创建新地图
	 * @param name
	 * @return
	 */
	public static String craeteMap(String name){
		String tableId = null;
		String url = "http://yuntuapi.amap.com/datamanage/table/create";
		String param = "key=" + AmapConfig.KEY+"&name=" + name + AmapConfig.PRIVATE_KEY;
		String sign = MD5Util.sign(param,"UTF-8");
		Map<String,String> map = new HashMap<String,String>();
		map.put("key", AmapConfig.KEY);
		map.put("name", name);
		map.put("sig", sign);
		String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), url, "UTF-8", map).getText();
		JSONObject json = JSONObject.fromObject(txt);
		if(json.has("tableid")){
			tableId = json.getString("tableid");
		}else{
			log.warn("[创建地图失败][info:"+txt+"]");
		}
		return tableId;
	}
}