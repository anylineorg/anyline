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

package org.anyline.metadata;

import org.anyline.entity.Order;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Index<M extends Index> extends TableAffiliation<M> implements Serializable {
    protected String keyword = "INDEX"           ;
    protected String type         ; //属性类型,如 btree
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
    protected boolean primary     ; // 是否是主键
    protected boolean cluster     ; // 是否聚簇索引
    protected boolean fulltext    ;
    protected boolean spatial     ;
    protected boolean approximate ; // 索引允许结果反映近似值
    protected LinkedHashMap<String, Integer> positions = new LinkedHashMap<>();
    protected LinkedHashMap<String, Order.TYPE> orders = new LinkedHashMap<>();

    protected boolean unique;
    public Index() {}
    public Index(String name) {
        setName(name);
    }
    public Index(Table table, String name, boolean unique) {
        setTable(table);
        setName(name);
        setUnique(unique);
    }
    public Index(Table table, String name) {
        setTable(table);
        setName(name);
    }
    public M drop() {
        this.action = ACTION.DDL.INDEX_DROP;
        return super.drop();
    }
    public boolean isCluster() {
        if(getmap && null != update) {
            return update.cluster;
        }
        return cluster;
    }

    public LinkedHashMap<String, Integer> getPositions() {
        return positions;
    }

    public M setPositions(LinkedHashMap<String, Integer> positions) {
        this.positions = positions;
        return (M)this;
    }

    public M setPosition(String column, Integer position) {
        this.positions.put(column.toUpperCase(), position);
        return (M)this;
    }
    public M setPosition(Column column, Integer position) {
        this.positions.put(column.getName().toUpperCase(), position);
        return (M)this;
    }
    public Integer getPosition(String column) {
        return positions.get(column.toUpperCase());
    }

    public M setOrders(LinkedHashMap<String, Order.TYPE> orders) {
        this.orders = orders;
        return (M)this;
    }

    public M setOrder(String column, Order.TYPE order) {
        this.orders.put(column.toUpperCase(), order);
        return (M)this;
    }
    public M setOrder(Column column, Order.TYPE order) {
        this.orders.put(column.getName().toUpperCase(), order);
        return (M)this;
    }
    public Order.TYPE getOrder(String column) {
        return orders.get(column.toUpperCase());
    }
    public M addColumn(Column column) {
        if(null == columns) {
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);
        return (M)this;
    }
    public M addColumn(String column) {
        return addColumn(new Column(column));
    }

    public M addColumn(String column, String order) {
        return addColumn(column, order, 0);
    }
    public M addColumn(String column, String order, int position) {
        positions.put(column.toUpperCase(), position);
        Order.TYPE type = Order.TYPE.ASC;
        if(null != order && order.toUpperCase().contains("DESC")) {
            type = Order.TYPE.DESC;
        }
        setOrder(column, type);
        return addColumn(new Column(column));
    }
    public M addColumn(String column, int position) {
        positions.put(column.toUpperCase(), position);
        return addColumn(new Column(column));
    }

    public String createName() {
        if(null == name) {
            name = "index_";
            if(null != columns) {
                name += BeanUtil.concat(columns.keySet(), "_");
            }
        }
        return name;
    }

    public Column getColumn(String name) {
        if(getmap && null != update) {
            return update.getColumn(name);
        }
        if(null != columns && null != name) {
            return columns.get(name.toUpperCase());
        }
        return null;
    }

    public boolean isApproximate() {
        return approximate;
    }

    public void setApproximate(boolean approximate) {
        this.approximate = approximate;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public M setCluster(boolean cluster) {
        if(setmap && null != update) {
            update.setCluster(cluster);
            return (M)this;
        }
        this.cluster = cluster;
        return (M)this;
    }

    public boolean isFulltext() {
        if(getmap && null != update) {
            return update.fulltext;
        }
        return fulltext;
    }

    public M setFulltext(boolean fulltext) {
        if(setmap && null != update) {
            update.setFulltext(fulltext);
            return (M)this;
        }
        this.fulltext = fulltext;
        return (M)this;
    }

    public boolean isSpatial() {
        if(getmap && null != update) {
            return update.spatial;
        }
        return spatial;
    }

    public M setSpatial(boolean spatial) {
        if(setmap && null != update) {
            update.setSpatial(spatial);
            return (M)this;
        }
        this.spatial = spatial;
        return (M)this;
    }

    public boolean isPrimary() {
        if(getmap && null != update) {
            return update.primary;
        }
        return primary;
    }

    public M setPrimary(boolean primary) {
        if(setmap && null != update) {
            update.setPrimary(primary);
            return (M)this;
        }
        this.primary = primary;
        if(primary) {
            setCluster(true);
            setUnique(true);
        }
        return (M)this;
    }
    public String getKeyword() {
        return this.keyword;
    }
    public boolean equals(Index index) {
        if(null == index) {
            return false;
        }
        String this_define = BeanUtil.concat(getColumns().values(), "name",",", false, true) + ":" + action;
        String index_define = BeanUtil.concat(index.getColumns().values(),"name",",", false, true) + ":" + index.action;
        return this_define.equalsIgnoreCase(index_define);
    }

/* ********************************* field refer ********************************** */
    public static final String FIELD_KEYWORD                       = "KEYWORD";
    public static final String FIELD_TYPE                          = "TYPE";
    public static final String FIELD_COLUMN                        = "COLUMN";
    public static final String FIELD_PRIMARY                       = "PRIMARY";
    public static final String FIELD_PRIMARY_CHECK                 = "PRIMARY_CHECK";
    public static final String FIELD_PRIMARY_CHECK_VALUE           = "PRIMARY_CHECK_VALUE";
    public static final String FIELD_CLUSTER                       = "CLUSTER";
    public static final String FIELD_CLUSTER_CHECK                 = "CLUSTER_CHECK";
    public static final String FIELD_CLUSTER_CHECK_VALUE           = "CLUSTER_CHECK_VALUE";
    public static final String FIELD_FULLTEXT                      = "FULLTEXT";
    public static final String FIELD_FULLTEXT_CHECK                = "FULLTEXT_CHECK";
    public static final String FIELD_FULLTEXT_CHECK_VALUE          = "FULLTEXT_CHECK_VALUE";
    public static final String FIELD_SPATIAL                       = "SPATIAL";
    public static final String FIELD_SPATIAL_CHECK                 = "SPATIAL_CHECK";
    public static final String FIELD_SPATIAL_CHECK_VALUE           = "SPATIAL_CHECK_VALUE";
    public static final String FIELD_APPROXIMATE                   = "APPROXIMATE";
    public static final String FIELD_APPROXIMATE_CHECK             = "APPROXIMATE_CHECK";
    public static final String FIELD_APPROXIMATE_CHECK_VALUE       = "APPROXIMATE_CHECK_VALUE";
    public static final String FIELD_POSITION                      = "POSITION";
    public static final String FIELD_ORDER                         = "ORDER";
    public static final String FIELD_UNIQUE                        = "UNIQUE";
    public static final String FIELD_UNIQUE_CHECK                  = "UNIQUE_CHECK";
    public static final String FIELD_UNIQUE_CHECK_VALUE            = "UNIQUE_CHECK_VALUE";
}