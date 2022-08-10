package org.anyline.qq.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.MapPoint;
import org.anyline.net.HttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class QQMapUtil {
    private static Logger log = LoggerFactory.getLogger(QQMapUtil.class);
    public QQMapConfig config = null;
    private static Hashtable<String, QQMapUtil> instances = new Hashtable<>();

    static {
        Hashtable<String, AnylineConfig> configs = QQMapConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }
    public static Hashtable<String, QQMapUtil> getInstances(){
        return instances;
    }
    public QQMapConfig getConfig(){
        return config;
    }
    public static QQMapUtil getInstance() {
        return getInstance("default");
    }

    public static QQMapUtil getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        QQMapUtil util = instances.get(key);
        if (null == util) {
            QQMapConfig config = QQMapConfig.getInstance(key);
            if(null != config) {
                util = new QQMapUtil();
                util.config = config;
                instances.put(key, util);
            }
        }
        return util;
    }


    public MapPoint regeo(double lng, double lat){
        return regeo(lng+"", lat+"");
    }
    private String sign(String api, Map<String, Object> params){
        String sign = null;
        String src = api + "?" + BeanUtil.map2string(params, true, true)+config.SECRET;
        sign = MD5Util.crypto(src);
        return sign;
    }

    /**
     * 通过IP地址获取其当前所在地理位置
     * @param ip
     * @return
     */
    public MapPoint ip(String ip){
        MapPoint point = null;
        String api = "/ws/location/v1/ip";
        Map<String, Object> params = new HashMap<>();
        params.put("ip", ip);
        params.put("key", config.KEY);
        String sign = sign(api, params);
        String url = QQMapConfig.HOST + api+"?"+BeanUtil.map2string(params, false,true)+"&sig="+sign;
        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                log.warn("[IP地址解析][执行失败][instance:{}][code:{}][info:{}]", config.INSTANCE_KEY, status, row.getString("message"));
                return null;
            }else{
                point = new MapPoint();
                DataRow result = row.getRow("result");
                if(null != result) {
                    DataRow location = result.getRow("location");
                    if(null != location){
                        point.setLng(location.getDouble("lng", -1));
                        point.setLat(location.getDouble("lat", -1));
                    }
                    DataRow ad = result.getRow("ad_info");
                    if(null != ad){
                        point.setProvinceName(ad.getString("province"));
                        point.setCityName(ad.getString("city"));
                        point.setDistrictName(ad.getString("district"));
                        point.setDistrictCode(ad.getString("adcode"));
                    }
                }
            }
        }
        return point;
    }

    /**
     * 逆地址解析
     * @param lng 经度
     * @param lat 纬度
     * @return MapPoint
     */
    public MapPoint regeo(String lng, String lat){
        MapPoint point = null;
        String api = "/ws/geocoder/v1";
        Map<String, Object> params = new HashMap<>();
        params.put("location", lat+","+lng);
        params.put("key", config.KEY);
        String sign = sign(api, params);
        String url = QQMapConfig.HOST + api+"?"+BeanUtil.map2string(params, false,true)+"&sig="+sign;

        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                log.warn("[逆地理编码][执行失败][instance:{}][code:{}][info:{}]", config.INSTANCE_KEY, status, row.getString("message"));
                return null;
            }else{
                point = new MapPoint(lng, lat);
                DataRow result = row.getRow("result");
                if(null != result) {
                    point.setAddress(result.getString("address"));
                }
                DataRow adr = row.getRow("result","address_component");
                if(null != adr) {
                    point.setProvinceName(adr.getString("province"));
                    point.setCityName(adr.getString("city"));
                    point.setDistrictName(adr.getString("district"));
                }
                adr = row.getRow("result","ad_info");
                if(null != adr) {
                    String adcode = adr.getString("adcode");
                    String provinceCode = adcode.substring(0,2);
                    String cityCode = adcode.substring(0,4);
                    point.setProvinceCode(provinceCode);
                    point.setCityCode(cityCode);
                    point.setDistrictCode(adcode);
                }
                adr = row.getRow("result","address_reference","town");
                if(null != adr){
                    point.setTownCode(adr.getString("id"));
                    point.setTownName(adr.getString("title"));
                }
            }
        }
        return point;
    }

}
