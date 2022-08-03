package org.anyline.baidu.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.MapPoint;
import org.anyline.net.HttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;
public class BaiduMapUtil {
    private static Logger log = LoggerFactory.getLogger(BaiduMapUtil.class);
    public BaiduMapConfig config = null;
    private static Hashtable<String, BaiduMapUtil> instances = new Hashtable<>();

    static {
        Hashtable<String, AnylineConfig> configs = BaiduMapConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }
    public static Hashtable<String, BaiduMapUtil> getInstances(){
        return instances;
    }

    public BaiduMapConfig getConfig(){
        return config;
    }
    public static BaiduMapUtil getInstance() {
        return getInstance("default");
    }

    public static BaiduMapUtil getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        BaiduMapUtil util = instances.get(key);
        if (null == util) {
            BaiduMapConfig config = BaiduMapConfig.getInstance(key);
            if(null != config) {
                util = new BaiduMapUtil();
                util.config = config;
                instances.put(key, util);
            }
        }
        return util;
    }

    public MapPoint regeo(double lng, double lat){
        return regeo(lng+"", lat+"");
    }
    public MapPoint regeo(String lng, String lat){
        MapPoint point = null;
        String url = "https://api.map.baidu.com/reverse_geocoding/v3/?ak="+config.AK+"&location="+lat+","+lng+"&extensions_town=true&output=json";
        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                log.warn("[逆地理编码][执行失败][code:{}][info:{}]", status, row.getString("message"));
                return null;
            }else{
                point = new MapPoint(lng, lat);
                point.setAddress(row.getString("formatted_address"));
                DataRow adr = row.getRow("result","addressComponent");
                if(null != adr) {
                    String adcode = adr.getString("adcode");
                    String provinceCode = adcode.substring(0,2);
                    String cityCode = adcode.substring(0,4);
                    point.setProvinceCode(provinceCode);
                    point.setProvinceName(adr.getString("province"));
                    point.setCityCode(cityCode);
                    point.setCityName(adr.getString("city"));
                    point.setDistrictName(adr.getString("district"));
                    point.setDistrictCode(adr.getString("adcode"));
                    point.setTownCode(adr.getString("town_code"));
                    point.setTownName(adr.getString("town"));
                }

            }
        }
        return point;
    }
    public String sign(String api, Map<?,?> params){
        return null;
    }



}
