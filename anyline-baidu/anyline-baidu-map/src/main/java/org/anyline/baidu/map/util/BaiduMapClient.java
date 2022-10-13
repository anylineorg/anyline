package org.anyline.baidu.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
import org.anyline.net.HttpUtil;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.client.map.AbstractMapClient;
import org.anyline.client.map.MapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;
public class BaiduMapClient extends AbstractMapClient implements MapClient {
    private static Logger log = LoggerFactory.getLogger(BaiduMapClient.class);
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
        Coordinate coordinate = new Coordinate();
        coordinate.setAddress(address);
        if(null != address){
            address = address.replace(" ","");
        }
        String url = "https://api.map.baidu.com/geocoding/v3/?ak="+config.AK+"&address="+address+"&output=json";
        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                // [code:302][info:天配额超限，限制访问]
                log.warn("[地理编码][执行失败][code:{}][info:{}]", status, row.getString("message"));
                log.warn("[地理编码][response:{}]",txt);
                if("302".equals(status)) {
                    throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                }else if("401".equals(status) || "402".equals(status)){
                    try{
                        log.warn("并发量已达到上限,sleep 50 ...");
                        Thread.sleep(50);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    throw new AnylineException(status, row.getString("message"));
                }
            }else{
                DataRow location = row.getRow("result","location");
                if(null != location) {
                    coordinate.setLng(location.getString("lng"));
                    coordinate.setLat(location.getString("lat"));
                    coordinate.setSuccess(true);
                }

            }
        }
        return coordinate;
    }
    @Override
    public Coordinate regeo(Coordinate coordinate) {
        Coordinate.TYPE _type = coordinate.getType();
        Double _lng = coordinate.getLng();
        Double _lat = coordinate.getLat();
        coordinate.convert(Coordinate.TYPE.BD09LL);
        coordinate.setSuccess(false);

        // 换回原坐标系
        coordinate.setLng(_lng);
        coordinate.setLat(_lat);
        coordinate.setType(_type);

        String url = "https://api.map.baidu.com/reverse_geocoding/v3/?ak="+config.AK+"&location="+coordinate.getLat()+","+coordinate.getLng()+"&extensions_town=true&output=json";
        String txt = HttpUtil.get(url).getText();
        DataRow row = DataRow.parseJson(txt);
        if(null != row){
            int status = row.getInt("status",-1);
            if(status != 0){
                // [code:302][info:天配额超限，限制访问]
                log.warn("[逆地理编码][执行失败][code:{}][info:{}]", status, row.getString("message"));
                log.warn("[逆地理编码][response:{}]",txt);
                if("302".equals(status)) {
                    throw new AnylineException("API_OVER_LIMIT", "访问已超出日访问量");
                }else if("401".equals(status) || "402".equals(status)){
                    try{
                        log.warn("并发量已达到上限,sleep 50 ...");
                        Thread.sleep(50);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    throw new AnylineException(status, row.getString("message"));
                }
            }else{
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
        }
        return coordinate;
    }


    public String sign(String api, Map<?,?> params){
        return null;
    }



}
