package org.anyline.map;

import org.anyline.amap.util.AmapUtil;
import org.anyline.baidu.map.util.BaiduMapUtil;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
import org.anyline.qq.map.util.QQMapUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.GISUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("anyline.map.proxy")
public class MapProxy {
    private static AmapUtil amap;
    private static QQMapUtil qmap;
    private static BaiduMapUtil bmap;
    public static Map<String,String> over_limits = new HashMap<>();
    public MapProxy(){}
    private static boolean enable(String api, String platform){
        String ymd = over_limits.get(api+"_"+platform);
        if(null == ymd){
            return true;
        }
        if(DateUtil.format("yyyy-MM-dd").equals(ymd)){
            return false;
        }
        over_limits.remove(api+"_"+platform);
        return true;
    }

    /**
     *
     * @param type 坐标系
     * @param lng 经度
     * @param lat 纬度
     * @return Coordinate
     */
    public static Coordinate regeo(Coordinate.TYPE type, double lng, double lat){
        Coordinate coordinate = new Coordinate(type, lng, lat);
        String api = "regeo";
        Double[] point = null;
        if(null != amap && enable(api, "amap")){
            try{
                amap.regeo(coordinate);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put(api+"_amap", DateUtil.format("yyyy-MM-dd"));
                }
            }catch (Exception e){
            }
        }
        if(!coordinate.isSuccess()  && null != bmap && enable(api,"bmap")){
            try{
                coordinate = bmap.regeo(coordinate);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put(api+"_bmap", DateUtil.format("yyyy-MM-dd"));
                }
            }catch (Exception e){
            }
        }
        if(!coordinate.isSuccess() && null != qmap && enable(api,"qmap")){
            try{
                coordinate = qmap.regeo(coordinate);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put(api+"_qmap", DateUtil.format("yyyy-MM-dd"));
                }
            }catch (Exception e){
            }
        }

        return coordinate;
    }

    public static Coordinate regeo(Coordinate.TYPE coord, String lng, String lat){
        return regeo(coord, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat,null));
    }
    public static Coordinate regeo(Coordinate.TYPE coord, String[] location){
        return regeo(coord, location[0], location[1]);
    }
    public static Coordinate regeo(Coordinate.TYPE coord, double[] location){
        return regeo(coord, location[0], location[1]);
    }

    public static Coordinate regeo(Coordinate coordinate){
        return regeo(coordinate.getType(), coordinate.getLng(), coordinate.getLat());
    }

    public static AmapUtil getAmap() {
        return MapProxy.amap;
    }
    public  static void setAmap(AmapUtil amap) {
        MapProxy.amap = amap;
    }

    public static QQMapUtil getQmap() {
        return MapProxy.qmap;
    }

    public static void setQmap(QQMapUtil qmap) {
        MapProxy.qmap = qmap;
    }

    public static BaiduMapUtil getBmap() {
        return MapProxy.bmap;
    }

    public static void setBmap(BaiduMapUtil bmap) {
        MapProxy.bmap = bmap;
    }


    @Autowired(required = false)
    @Qualifier("anyline.amap.init.util")
    public void init(AmapUtil amap){
        MapProxy.amap = amap;
    }
    @Autowired(required = false)
    @Qualifier("anyline.qq.map.init.util")
    public void init(QQMapUtil qmap){
        MapProxy.qmap = qmap;
    }
    @Autowired(required = false)
    @Qualifier("anyline.baidu.map.init.util")
    public void init(BaiduMapUtil bmap){
        MapProxy.bmap = bmap;
    }

}
