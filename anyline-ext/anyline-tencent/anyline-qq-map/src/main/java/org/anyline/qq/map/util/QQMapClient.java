package org.anyline.qq.map.util;

import org.anyline.client.map.AbstractMapClient;
import org.anyline.entity.DataRow;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.*;
import org.anyline.client.map.MapClient;
import org.anyline.util.encrypt.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class QQMapClient extends AbstractMapClient implements MapClient {
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
        DataRow row = api(api, params);
        if(null != row){
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
        if(null != coordinate) {
            coordinate.setType(Coordinate.TYPE.GCJ02LL);
        }
        if(null != coordinate) {
            coordinate.correct();
        }
        return coordinate;
    }

    /**
     * 根据地址解析 坐标
     * https://lbs.qq.com/service/webService/webServiceGuide/webServiceGeocoder
     * @param address 地址 用原文签名 用url encode后提交
     * @param city 城市(没有用到可以不传)
     * @return Coordinate
     */
    @Override
    public Coordinate geo(String address, String city){
        Coordinate coordinate = new Coordinate();
        coordinate.setAddress(address);
        if(null != address){
            address = address.replace(" ","");
            if(null != city && !address.contains(city)){
                address = city + address;
            }
        }
        String api = "/ws/geocoder/v1";
        Map<String,Object> params = new HashMap<>();
        params.put("address", address);
        DataRow row = api(api, params);
        if(null != row){
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
        if(null != coordinate) {
            coordinate.correct();
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
        String api = "/ws/geocoder/v1";

        Coordinate.TYPE _type = coordinate.getType();
        Double _lng = coordinate.getLng();
        Double _lat = coordinate.getLat();

        coordinate.convert(Coordinate.TYPE.GCJ02LL);
        coordinate.setSuccess(false);

        Map<String, Object> params = new HashMap<>();
        params.put("location", coordinate.getLat()+","+coordinate.getLng());        // 这里是纬度在前

        // 换回原坐标系
        coordinate.setLng(_lng);
        coordinate.setLat(_lat);
        coordinate.setType(_type);

        DataRow row = api(api, params);
        if(null != row){
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
                if(BasicUtil.isNotEmpty(adcode)) {
                    String provinceCode = adcode.substring(0, 2);
                    String cityCode = adcode.substring(0, 4);
                    coordinate.setProvinceCode(provinceCode);
                    coordinate.setCityCode(cityCode);
                    coordinate.setCountyCode(adcode);
                }
            }
            adr = row.getRow("result","address_reference","town");
            if(null != adr){
                coordinate.setTownCode(adr.getString("id"));
                coordinate.setTownName(adr.getString("title"));
            }
            coordinate.setSuccess(true);

        }
        if(null != coordinate) {
            coordinate.correct();
        }
        return coordinate;
    }



    /**
     * 参数签名
     * @param api 接口
     * @param params 参数
     * @return String
     */
    private String sign(String api, Map<String, Object> params){
        params.put("key", config.KEY);
        String sign = null;
        String src = api + "?" + BeanUtil.map2string(params) + config.SECRET;
        sign = MD5Util.crypto(src);
        params.put("sig", sign);
        return sign;
    }
    private DataRow api(String api, Map<String,Object> params){
        DataRow row = null;
        sign(api, params);
        HttpResponse response = HttpUtil.get(QQMapConfig.HOST + api,"UTF-8", params);
        int status = response.getStatus();
        if(status == 200){
            String txt = response.getText();
            row = DataRow.parseJson(txt);
            if(null == row){
                status = row.getInt("status",-1);
                if(status != 0) {
                    log.warn("[{}][执行失败][status:{}][info:{}]", api , status, row.getString("message"));
                    log.warn("[{}][response:{}]", api, txt);
                    if ("302".equals(status)) {
                        throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                    } else if ("401".equals(status) || "402".equals(status)) {
                        try {
                            log.warn("并发量已达到上限,sleep 50 ...");
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        api(api, params);
                    } else {
                        throw new AnylineException(status, row.getString("message"));
                    }
                }
            }
        }
        return row;
    }

}
