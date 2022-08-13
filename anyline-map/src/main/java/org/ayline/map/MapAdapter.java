package org.ayline.map;

import org.anyline.amap.util.AmapUtil;
import org.anyline.baidu.map.util.BaiduMapUtil;
import org.anyline.entity.MapPoint;
import org.anyline.exception.AnylineException;
import org.anyline.qq.map.util.QQMapUtil;
import org.anyline.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

public class MapAdapter {
    private static AmapUtil amap;
    private static QQMapUtil qmap;
    private static BaiduMapUtil bmap;
    public static Map<String,String> over_limits = new HashMap<>();
    public static MapPoint regeo(double lng, double lat){
        return regeo(lng+"", lat+"");
    }
    private static boolean enable(String type){
        String ymd = over_limits.get(type);
        if(null == ymd){
            return true;
        }
        if(DateUtil.format("yyyy-MM-dd").equals(ymd)){
            return false;
        }
        over_limits.remove(type);
        return true;
    }
    public static MapPoint regeo(String lng, String lat){
        MapPoint point = null;
        if(null != amap && enable("amap")){
            try{
                point = amap.regeo(lng, lat);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put("amap", DateUtil.format("yyyy-MM-dd"));
                }
            }
        }
        if(null == point && null != bmap && enable("bmap")){
            try{
                point = bmap.regeo(lng, lat);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put("bmap", DateUtil.format("yyyy-MM-dd"));
                }
            }
        }
        if(null == point && null != qmap && enable("qmap")){
            try{
                point = qmap.regeo(lng, lat);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put("bmap", DateUtil.format("yyyy-MM-dd"));
                }
            }
        }
        return point;
    }

    public static AmapUtil getAmap() {
        return MapAdapter.amap;
    }

    public static void setAmap(final AmapUtil amap) {
        MapAdapter.amap = amap;
    }

    public static QQMapUtil getQmap() {
        return MapAdapter.qmap;
    }

    public static void setQmap(final QQMapUtil qmap) {
        MapAdapter.qmap = qmap;
    }

    public static BaiduMapUtil getBmap() {
        return MapAdapter.bmap;
    }

    public static void setBmap(final BaiduMapUtil bmap) {
        MapAdapter.bmap = bmap;
    }
}
