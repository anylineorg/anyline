package org.ayline.map;

import org.anyline.entity.MapPoint;

public class MapAdapter {
    public static MapPoint regeo(double lng, double lat){
        return regeo(lng+"", lat+"");
    }
    public static MapPoint regeo(String lng, String lat){
        return null;
    }
}
