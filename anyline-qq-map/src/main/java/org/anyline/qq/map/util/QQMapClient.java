package org.anyline.qq.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
import org.anyline.net.HttpUtil;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class QQMapClient implements MapClient {
    private static Logger log = LoggerFactory.getLogger(QQMapClient.class);
    public QQMapConfig config = null;
    private static Hashtable<String, QQMapClient> instances = new Hashtable<>();

    static {
        Hashtable<String, AnylineConfig> configs = QQMapConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }
    public static Hashtable<String, QQMapClient> getInstances(){
        return instances;
    }
    public QQMapConfig getConfig(){
        return config;
    }
    public static QQMapClient getInstance() {
        return getInstance("default");
    }

    public static QQMapClient getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        QQMapClient client = instances.get(key);
        if (null == client) {
            QQMapConfig config = QQMapConfig.getInstance(key);
            if(null != config) {
                client = new QQMapClient();
                client.config = config;
                instances.put(key, client);
            }
        }
        return client;
    }


    /**
     * 参数签名
     * @param api 接口
     * @param params 参数
     * @return String
     */
    private String sign(String api, Map<String, Object> params){
        String sign = null;
        String src = api + "?" + BeanUtil.map2string(params, true, true)+config.SECRET;
        sign = MD5Util.crypto(src);
        return sign;
    }

    /**
     * 通过IP地址获取其当前所在地理位置
     * @param ip ip
     * @return 坐标
     */
    @Override
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
                // [code:121][info:此key每日调用量已达到上限]
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
                        coordinate.setLng(point.getDouble("lng", -1.0));
                        coordinate.setLat(point.getDouble("lat", -1.d));
                    }
                    DataRow ad = result.getRow("ad_info");
                    if(null != ad){
                        coordinate.setProvinceName(ad.getString("province"));
                        coordinate.setCityName(ad.getString("city"));
                        coordinate.setCountyName(ad.getString("district"));
                        coordinate.setCountyCode(ad.getString("adcode"));
                    }
                }
            }
        }
        if(null != coordinate) {
            coordinate.setType(Coordinate.TYPE.GCJ02LL);
        }
        return coordinate;
    }

    /**
     * 根据地址解析 坐标
     * https://lbs.qq.com/service/webService/webServiceGuide/webServiceGeocoder
     * @param address 地址 用原文签名 用url encode后提交
     * @return Coordinate
     */
    @Override
    public Coordinate geo(String address){
        Coordinate coordinate = new Coordinate();
        coordinate.setAddress(address);
        if(null != address){
            address = address.replace(" ","");
        }
        String api = "/ws/geocoder/v1";
        Map<String,Object> params = new HashMap<>();
        params.put("key", config.KEY);
        params.put("address", address);
        String sign = sign(api, params);
        try {
            params.put("address", URLEncoder.encode(address, "UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        String url = QQMapConfig.HOST + api + "?" + BeanUtil.map2string(params, false,true) + "&sig=" + sign;

        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                log.warn("[地址解析][执行失败][instance:{}][code:{}][info:{}]", config.INSTANCE_KEY, status, row.getString("message"));
                log.warn("[地址解析][response:{}]",txt);
                if("121".equals(status)) {
                    throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                }else if("120".equals(status)){
                    log.warn("并发量已达到上限,sleep 50 ...");
                    try {
                        Thread.sleep(50);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return geo(address);
                }else{
                    throw new AnylineException(status, row.getString("message"));
                }
            }else{
                DataRow result = row.getRow("result");
                DataRow location = row.getRow("result","location");
                if(null != location){
                    coordinate.setLat(location.getString("lat"));
                    coordinate.setLng(location.getString("lng"));
                }

                DataRow adr = row.getRow("result","address_components");
                if(null != adr) {
                    coordinate.setProvinceName(adr.getString("province"));
                    coordinate.setCityName(adr.getString("city"));
                    coordinate.setCountyName(adr.getString("district"));
                    String street = adr.getString("street");
                    coordinate.setStreet(street);
                    String number = adr.getString("street_number");
                    if(null != number && null != street){
                        number = number.replace(street,"");
                    }
                    coordinate.setStreetNumber(number);
                }
                adr = row.getRow("result","ad_info");
                if(null != adr) {
                    String adcode = adr.getString("adcode");
                    String provinceCode = adcode.substring(0,2);
                    String cityCode = adcode.substring(0,4);
                    coordinate.setProvinceCode(provinceCode);
                    coordinate.setCityCode(cityCode);
                    coordinate.setCountyCode(adcode);
                }
                coordinate.setReliability(result.getInt("reliability",0));
                coordinate.setAccuracy(result.getInt("level",0));
                coordinate.setSuccess(true);
            }
        }
        return coordinate;
    }
    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * https://lbs.qq.com/service/webService/webServiceGuide/webServiceGcoder
     * @param coordinate 坐标
     * @return Coordinate
     */
    @Override
    public Coordinate regeo(Coordinate coordinate){
        Coordinate.TYPE _type = coordinate.getType();
        Double _lng = coordinate.getLng();
        Double _lat = coordinate.getLat();

        coordinate.convert(Coordinate.TYPE.GCJ02LL);
        coordinate.setSuccess(false);

        String api = "/ws/geocoder/v1";
        Map<String, Object> params = new HashMap<>();
        params.put("location", coordinate.getLat()+","+coordinate.getLng());        // 这里是纬度在前
        params.put("key", config.KEY);
        String sign = sign(api, params);
        String url = QQMapConfig.HOST + api + "?" + BeanUtil.map2string(params, false,true) + "&sig=" + sign;

        // 换回原坐标系
        coordinate.setLng(_lng);
        coordinate.setLat(_lat);
        coordinate.setType(_type);

        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                log.warn("[逆地理编码][执行失败][instance:{}][code:{}][info:{}]", config.INSTANCE_KEY, status, row.getString("message"));
                log.warn("[逆地理编码][response:{}]",txt);
                if("121".equals(status)) {
                    throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                }else if("120".equals(status)){
                    log.warn("并发量已达到上限,sleep 50 ...");
                    try {
                        Thread.sleep(50);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return regeo(coordinate);
                }else{
                    throw new AnylineException(status, row.getString("message"));
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
                    coordinate.setCountyName(adr.getString("district"));

                    String street = adr.getString("street");
                    coordinate.setStreet(street);
                    String number = adr.getString("street_number");
                    if(null != number && null != street){
                        number = number.replace(street,"");
                    }
                }
                adr = row.getRow("result","ad_info");
                if(null != adr) {
                    String adcode = adr.getString("adcode");
                    String provinceCode = adcode.substring(0,2);
                    String cityCode = adcode.substring(0,4);
                    coordinate.setProvinceCode(provinceCode);
                    coordinate.setCityCode(cityCode);
                    coordinate.setCountyCode(adcode);
                }
                adr = row.getRow("result","address_reference","town");
                if(null != adr){
                    coordinate.setTownCode(adr.getString("id"));
                    coordinate.setTownName(adr.getString("title"));
                }
                coordinate.setSuccess(true);
            }
        }
        return coordinate;
    }
    @Override
    public Coordinate regeo(double lng, double lat){
        return regeo(Coordinate.TYPE.GCJ02LL, lng, lat);
    }
    @Override
    public Coordinate regeo(String[] point){
        return regeo(point[0], point[1]);
    }
    @Override
    public Coordinate regeo(double[] point){
        return regeo(point[0], point[1]);
    }
    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param lng 经度
     * @param lat 纬度
     * @return Coordinate
     */
    @Override
    public Coordinate regeo(Coordinate.TYPE type, Double lng, Double lat){
        Coordinate coordinate = new Coordinate(type, lng, lat);
        return regeo(coordinate);
    }

    @Override
    public Coordinate regeo(Coordinate.TYPE type, String lng, String lat){
        return regeo(type, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    @Override
    public Coordinate regeo(String lng, String lat){
        return regeo(Coordinate.TYPE.GCJ02LL, lng, lat);
    }

}
