package org.anyline.client.map;

import org.anyline.entity.Coordinate;
import org.anyline.util.BasicUtil;
import org.anyline.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMapClient implements MapClient{
    private static Logger log = LoggerFactory.getLogger(AbstractMapClient.class);

    /**
     * 逆地址解析 根据坐标返回详细地址及各级地区编号
     * @param coordinate 坐标
     * @return Coordinate
     */
    @Override
    public Coordinate regeo(Coordinate coordinate) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getName() + ")未实现 public Coordinate regeo(Coordinate coordinate)", 37));
        }
        return null;
    }
    /**
     * 根据地址查坐标(子类实现)
     * @param address  address
     * @param city  city
     * @return Coordinate
     */
    @Override
    public Coordinate geo(String address, String city){
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getName() + ")未实现 Coordinate geo(String address, String city)", 37));
        }
        return null;
    }

    @Override
    public Coordinate geo(String address){
        return geo(address, null);
    }
    /**
     * 逆地址解析
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
    public Coordinate regeo(Coordinate.TYPE type, String lng, String lat)  {
        return regeo(type, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    @Override
    public Coordinate regeo(Coordinate.TYPE type, String point)  {
        String[] points = point.split(",");
        return regeo(type, BasicUtil.parseDouble(points[0], null), BasicUtil.parseDouble(points[1], null));
    }
    @Override
    public Coordinate regeo(String point)  {
        return regeo(Coordinate.TYPE.GCJ02LL, point);
    }
    public Coordinate regeo(String lng, String lat){
        return regeo(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }
    @Override
    public Coordinate regeo(Double lng, Double lat){
        return regeo(Coordinate.TYPE.GCJ02LL,lng, lat);
    }
    @Override
    public Coordinate regeo(Coordinate.TYPE type, String[] point){
        return regeo(type, point[0],point[1]);
    }
    @Override
    public Coordinate regeo(String[] point){
        return regeo(point[0],point[1]);
    }
    @Override
    public Coordinate regeo(Coordinate.TYPE type, Double[] point){
        return regeo(type, point[0],point[1]);
    }
    @Override
    public Coordinate regeo(Double[] point){
        return regeo(point[0],point[1]);
    }

}
