package org.anyline.qq.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
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
    public Coordinate ip(String ip) {
        Coordinate coordinate = null;
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
                //[code:121][info:此key每日调用量已达到上限]
                log.warn("[IP地址解析][执行失败][instance:{}][code:{}][info:{}]", config.INSTANCE_KEY, status, row.getString("message"));
                log.warn("[IP地址解析][response:{}]",txt);
                if("121".equals(status)) {
                    throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                }
            }else{
                coordinate = new Coordinate();
                DataRow result = row.getRow("result");
                if(null != result) {
                    DataRow point = result.getRow("location");
                    if(null != point){
                        coordinate.setLng(point.getDouble("lng", -1));
                        coordinate.setLat(point.getDouble("lat", -1));
                    }
                    DataRow ad = result.getRow("ad_info");
                    if(null != ad){
                        coordinate.setProvinceName(ad.getString("province"));
                        coordinate.setCityName(ad.getString("city"));
                        coordinate.setDistrictName(ad.getString("district"));
                        coordinate.setDistrictCode(ad.getString("adcode"));
                    }
                }
            }
        }
        coordinate.setType(Coordinate.TYPE.GCJ02LL);
        return coordinate;
    }

    public Coordinate regeo(double lng, double lat){
        return regeo(lng+"", lat+"");
    }
    public Coordinate regeo(String[] point){
        return regeo(point[0], point[1]);
    }
    public Coordinate regeo(double[] point){
        return regeo(point[0], point[1]);
    }
    /**
     * 逆地址解析
     * @param lng 经度
     * @param lat 纬度
     * @return MapPoint
     */
    public Coordinate regeo(Coordinate.TYPE type, Double lng, Double lat){
        Coordinate coordinate = new Coordinate(type, lng, lat);
        coordinate.convert(Coordinate.TYPE.GCJ02LL);
        String api = "/ws/geocoder/v1";
        Map<String, Object> params = new HashMap<>();
        params.put("location", coordinate.getLat()+","+coordinate.getLng());        //这里是纬度在前
        params.put("key", config.KEY);
        String sign = sign(api, params);
        String url = QQMapConfig.HOST + api+"?"+BeanUtil.map2string(params, false,true)+"&sig="+sign;

        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                log.warn("[逆地理编码][执行失败][instance:{}][code:{}][info:{}]", config.INSTANCE_KEY, status, row.getString("message"));
                log.warn("[逆地理编码][response:{}]",txt);
                if("121".equals(status)) {
                    throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                }
            }else{
                DataRow result = row.getRow("result");
                if(null != result) {
                    coordinate.setAddress(result.getString("address"));
                }
                DataRow adr = row.getRow("result","address_component");
                if(null != adr) {
                    coordinate.setProvinceName(adr.getString("province"));
                    coordinate.setCityName(adr.getString("city"));
                    coordinate.setDistrictName(adr.getString("district"));
                }
                adr = row.getRow("result","ad_info");
                if(null != adr) {
                    String adcode = adr.getString("adcode");
                    String provinceCode = adcode.substring(0,2);
                    String cityCode = adcode.substring(0,4);
                    coordinate.setProvinceCode(provinceCode);
                    coordinate.setCityCode(cityCode);
                    coordinate.setDistrictCode(adcode);
                }
                adr = row.getRow("result","address_reference","town");
                if(null != adr){
                    coordinate.setTownCode(adr.getString("id"));
                    coordinate.setTownName(adr.getString("title"));
                }
            }
        }
        //换回原坐标系
        coordinate.setLng(lng);
        coordinate.setLat(lat);
        coordinate.setType(type);
        return coordinate;
    }
    public Coordinate regeo(Coordinate.TYPE type, String lng, String lat){
        return regeo(type, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    public Coordinate regeo(String lng, String lat){
        return regeo(Coordinate.TYPE.GCJ02LL, lng, lat);
    }

}
