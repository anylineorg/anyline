/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.client.map;

import org.anyline.entity.Coordinate;
import org.anyline.entity.SRS;
import org.anyline.util.BasicUtil;
import org.anyline.util.LogUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

public abstract class AbstractMapClient implements MapClient{
    private static final Log log = LogProxy.get(AbstractMapClient.class);

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
    public Coordinate geo(String address, String city) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getName() + ")未实现 Coordinate geo(String address, String city)", 37));
        }
        return null;
    }

    @Override
    public Coordinate geo(String address) {
        return geo(address, null);
    }

    /**
     * 逆地址解析
     * @param lng 经度
     * @param lat 纬度
     * @return Coordinate
     */
    @Override
    public Coordinate regeo(SRS srs, Double lng, Double lat) {
        Coordinate coordinate = new Coordinate(srs, lng, lat);
        return regeo(coordinate);
    }

    @Override
    public Coordinate regeo(SRS srs, String lng, String lat)  {
        return regeo(srs, BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }

    @Override
    public Coordinate regeo(SRS srs, String point)  {
        String[] points = point.split(",");
        return regeo(srs, BasicUtil.parseDouble(points[0], null), BasicUtil.parseDouble(points[1], null));
    }

    @Override
    public Coordinate regeo(String point)  {
        return regeo(SRS.GCJ02LL, point);
    }
    public Coordinate regeo(String lng, String lat) {
        return regeo(BasicUtil.parseDouble(lng, null), BasicUtil.parseDouble(lat, null));
    }

    @Override
    public Coordinate regeo(Double lng, Double lat) {
        return regeo(SRS.GCJ02LL, lng, lat);
    }

    @Override
    public Coordinate regeo(SRS srs, String[] point) {
        return regeo(srs, point[0], point[1]);
    }

    @Override
    public Coordinate regeo(String[] point) {
        return regeo(point[0], point[1]);
    }

    @Override
    public Coordinate regeo(SRS srs, Double[] point) {
        return regeo(srs, point[0], point[1]);
    }

    @Override
    public Coordinate regeo(Double[] point) {
        return regeo(point[0], point[1]);
    }

}
