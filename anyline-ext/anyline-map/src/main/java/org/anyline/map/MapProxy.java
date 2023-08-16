/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.map;

import org.anyline.amap.util.AmapClient;
import org.anyline.baidu.map.util.BaiduMapClient;
import org.anyline.entity.Coordinate;
import org.anyline.exception.AnylineException;
import org.anyline.qq.map.util.QQMapClient;
import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("anyline.map.proxy")
public class MapProxy {
    private static AmapClient amap;
    private static QQMapClient qmap;
    private static BaiduMapClient bmap;
    public static Map<String, String> over_limits = new HashMap<>();
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

    public static AmapClient getAmap() {
        return MapProxy.amap;
    }
    public  static void setAmap(AmapClient amap) {
        MapProxy.amap = amap;
    }

    public static QQMapClient getQmap() {
        return MapProxy.qmap;
    }

    public static void setQmap(QQMapClient qmap) {
        MapProxy.qmap = qmap;
    }

    public static BaiduMapClient getBmap() {
        return MapProxy.bmap;
    }

    public static void setBmap(BaiduMapClient bmap) {
        MapProxy.bmap = bmap;
    }


    @Autowired(required = false)
    @Qualifier("anyline.amap.init.client")
    public void init(AmapClient amap){
        MapProxy.amap = amap;
    }
    @Autowired(required = false)
    @Qualifier("anyline.qq.map.init.client")
    public void init(QQMapClient qmap){
        MapProxy.qmap = qmap;
    }
    @Autowired(required = false)
    @Qualifier("anyline.baidu.map.init.client")
    public void init(BaiduMapClient bmap){
        MapProxy.bmap = bmap;
    }

}
