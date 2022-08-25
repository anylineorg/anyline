package org.anyline.baidu.map.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
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

    public Coordinate regeo(Coordinate coordinate) {
        Coordinate.TYPE _type = coordinate.getType();
        Double _lng = coordinate.getLng();
        Double _lat = coordinate.getLat();
        coordinate.convert(Coordinate.TYPE.BD09LL);

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
                    coordinate.setDistrictName(adr.getString("district"));
                    coordinate.setDistrictCode(adr.getString("adcode"));
                    coordinate.setTownCode(adr.getString("town_code"));
                    coordinate.setTownName(adr.getString("town"));
                }

            }
        }
        //换回原坐标系
        coordinate.setLng(_lng);
        coordinate.setLat(_lat);
        coordinate.setType(_type);
        return coordinate;
    }

    public Coordinate regeo(double lng, double lat) {
        return regeo(Coordinate.TYPE.BD09LL,lng, lat);
    }
    public Coordinate regeo(double[] point) {
        return regeo(point[0], point[1]);
    }
    public Coordinate regeo(String[] point) {
        return regeo(point[0], point[1]);
    }
    public Coordinate regeo(Coordinate.TYPE type, Double lng, Double lat) {
        Coordinate coordinate = new Coordinate(type, lng, lat);
        return regeo(coordinate);
    }
    public Coordinate regeo(String lng, String lat) {
        return regeo(Coordinate.TYPE.BD09LL, lng, lat);
    }
    public Coordinate regeo(Coordinate.TYPE type, String lng, String lat) {
        return regeo(type, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    public String sign(String api, Map<?,?> params){
        return null;
    }



}
