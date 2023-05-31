package org.anyline.baidu.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.geometry.Coordinate;
import org.anyline.exception.AnylineException;
import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.client.map.AbstractMapClient;
import org.anyline.client.map.MapClient;
import org.anyline.util.BeanUtil;
import org.anyline.util.encrypt.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
public class BaiduMapClient extends AbstractMapClient implements MapClient {
    private static Logger log = LoggerFactory.getLogger(BaiduMapClient.class);
    private static final String HOST = "https://api.map.baidu.com";

    public BaiduMapConfig config = null;
    private static Hashtable<String, BaiduMapClient> instances = new Hashtable<>();

    static {
        Hashtable<String, AnylineConfig> configs = BaiduMapConfig.getInstances();
        for(String key:configs.keySet()){
            instances.put(key, getInstance(key));
        }
    }
    public static Hashtable<String, BaiduMapClient> getInstances(){
        return instances;
    }

    public BaiduMapConfig getConfig(){
        return config;
    }
    public static BaiduMapClient getInstance() {
        return getInstance("default");
    }

    public static BaiduMapClient getInstance(String key) {
        if (BasicUtil.isEmpty(key)) {
            key = "default";
        }
        BaiduMapClient client = instances.get(key);
        if (null == client) {
            BaiduMapConfig config = BaiduMapConfig.getInstance(key);
            if(null != config) {
                client = new BaiduMapClient();
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
        return null;
    }

    @Override
    public Coordinate geo(String address, String city) {
        String api = "/geocoding/v3/";

        Coordinate coordinate = new Coordinate();
        coordinate.setAddress(address);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("address", address);
        params.put("output", "json");
        DataRow row = api(api, params);
        if(null != row){
            DataRow location = row.getRow("result","location");
            if(null != location) {
                coordinate.setLng(location.getString("lng"));
                coordinate.setLat(location.getString("lat"));
                coordinate.setSuccess(true);
            }
        }
        if(null != coordinate) {
            coordinate.correct();
        }
        return coordinate;
    }
    @Override
    public Coordinate regeo(Coordinate coordinate) {
        String api = "/reverse_geocoding/v3/";

        Coordinate.TYPE _type = coordinate.getType();
        Double _lng = coordinate.getLng();
        Double _lat = coordinate.getLat();
        coordinate.convert(Coordinate.TYPE.BD09LL);
        coordinate.setSuccess(false);

        // 换回原坐标系
        coordinate.setLng(_lng);
        coordinate.setLat(_lat);
        coordinate.setType(_type);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("location",coordinate.getLat()+","+coordinate.getLng());
        params.put("extensions_town","true");
        params.put("output","json");

        DataRow row = api(api, params);
        if(null != row){
            coordinate.setAddress(row.getString("formatted_address"));
            DataRow adr = row.getRow("result","addressComponent");
            if(null != adr) {
                String adcode = adr.getString("adcode");
                String provinceCode = adcode.substring(0,2);
                String cityCode = adcode.substring(0,4);
                coordinate.setProvinceCode(provinceCode);
                coordinate.setProvinceName(adr.getString("province"));
                coordinate.setCityCode(cityCode);
                coordinate.setCityName(adr.getString("city"));
                coordinate.setCountyName(adr.getString("district"));
                coordinate.setCountyCode(adr.getString("adcode"));
                coordinate.setTownCode(adr.getString("town_code"));
                coordinate.setTownName(adr.getString("town"));

                String street = adr.getString("street");
                coordinate.setStreet(street);
                String number = adr.getString("street_number");
                if(null != number && null != street){
                    number = number.replace(street,"");
                }
                coordinate.setStreet(street);
                coordinate.setStreetNumber(number);
                coordinate.setSuccess(true);
            }
        }
        if(null != coordinate) {
            coordinate.correct();
        }
        return coordinate;
    }


    private DataRow api(String api, Map<String,Object> params){
        DataRow row = null;
        sign(api, params);
        HttpResponse response = HttpUtil.get(HOST + api,"UTF-8", params);
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
                            api(api, params);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new AnylineException(status, row.getString("message"));
                    }
                }
            }
        }
        return row;
    }
    public void sign(String api, Map<String,Object> params){
        params.put("ak", config.AK);
        try {
            for (String key : params.keySet()) {
                String value = (String)params.get(key);
                value = URLEncoder.encode(value, "UTF-8");
                params.put(key, value);
            }
            String str = api + "?" + BeanUtil.map2string(params) + config.SK;
            str = URLEncoder.encode(str, "UTF-8");
            params.put("sn", MD5Util.crypto(str));
        }catch (Exception e){
            e.printStackTrace();
        }

    }



}
