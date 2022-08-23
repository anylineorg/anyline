package org.ayline.map;

import org.anyline.amap.util.AmapUtil;
import org.anyline.baidu.map.util.BaiduMapUtil;
import org.anyline.entity.MapPoint;
import org.anyline.exception.AnylineException;
import org.anyline.qq.map.util.QQMapUtil;
import org.anyline.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MapProxy {
    private static AmapUtil amap;
    private static QQMapUtil qmap;
    private static BaiduMapUtil bmap;
    public static Map<String,String> over_limits = new HashMap<>();
    public static MapPoint regeo(double lng, double lat){
        return regeo(lng+"", lat+"");
    }
    private static boolean enable(String type, String platform){
        String ymd = over_limits.get(type+"_"+platform);
        if(null == ymd){
            return true;
        }
        if(DateUtil.format("yyyy-MM-dd").equals(ymd)){
            return false;
        }
        over_limits.remove(type+"_"+platform);
        return true;
    }
    public static MapPoint regeo(String lng, String lat){
        MapPoint point = null;
        String type = "regeo";
        if(null != amap && enable(type, "amap")){
            try{
                point = amap.regeo(lng, lat);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put(type+"_amap", DateUtil.format("yyyy-MM-dd"));
                }
            }
        }
        if(null == point && null != bmap && enable(type,"bmap")){
            try{
                point = bmap.regeo(lng, lat);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put(type+"_bmap", DateUtil.format("yyyy-MM-dd"));
                }
            }
        }
        if(null == point && null != qmap && enable(type,"qmap")){
            try{
                point = qmap.regeo(lng, lat);
            }catch (AnylineException e){
                if("API_OVER_LIMIT".equals(e.getCode())){
                    over_limits.put(type+"_qmap", DateUtil.format("yyyy-MM-dd"));
                }
            }
        }
        return point;
    }

    public static AmapUtil getAmap() {
        return MapProxy.amap;
    }

    public static void setAmap(AmapUtil amap) {
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
    public void init(AmapUtil amap){
        MapProxy.amap = amap;
    }
    @Autowired(required = false)
    public void init(QQMapUtil qmap){
        MapProxy.qmap = qmap;
    }
    @Autowired(required = false)
    public void init(BaiduMapUtil bmap){
        MapProxy.bmap = bmap;
    }
}
