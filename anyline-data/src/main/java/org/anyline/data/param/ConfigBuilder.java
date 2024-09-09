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

package org.anyline.data.param;

import org.anyline.adapter.KeyAdapter;
import org.anyline.data.param.init.DefaultConfig;
import org.anyline.data.param.init.DefaultConfigChain;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.entity.*;

import java.util.List;

public class ConfigBuilder {
    public static ConfigStore build(String json) {
        DataRow row = DataRow.parseJson(KeyAdapter.KEY_CASE.UPPER, json);
        ConfigStore configs = parse(row);
        return configs;
    }
    public static ConfigStore parse(DataRow row) {
        DefaultConfigStore configs = new DefaultConfigStore();
        DataRow conditions = row.getRow("conditions");
        if(null != conditions) {
            ConfigChain chain = parseConfigChain(conditions);
            configs.setChain(chain);
        }
        DataRow columns = row.getRow("columns");
        if(null != columns) {
            List<String> query = (List<String>)columns.getList("query");
            configs.columns(query);
            List<String> excludes = (List<String>)columns.getList("exclude");
            if(null == excludes){
                excludes = (List<String>)columns.getList("excludes");
            }
            configs.excludes(excludes);
        }
        DataRow navi = row.getRow("navi");
        if(null != navi) {
            configs.setPageNavi(parseNavi(navi));
        }
        return configs;
    }
    public static PageNavi parseNavi(DataRow row) {
        PageNavi navi = new DefaultPageNavi();
        navi.setCurPage(row.getInt("page", 1));
        navi.setPageRows(row.getInt("vol", PageNaviConfig.DEFAULT_VAR_PAGE_DEFAULT_VOL));
        navi.setTotalRow(row.getInt("total", 0));
        Boolean auto = row.getBoolean("auto_count", PageNaviConfig.IS_AUTO_COUNT);
        if(null != auto) {
            navi.autoCount(auto);
        }
        return navi;
    }
    public static Config parseConfig(DataRow row) {
        Config config = null;
        DataSet items = row.getSet("items");
        if(null != items && !items.isEmpty()) {
            config = parseConfigChain(row);
        }else {
            ParseResult parser = ParseResult.build(row);
            config = new DefaultConfig(parser);
            String join = row.getString("join");
            if(null != join){
                config.setJoin(Condition.JOIN.valueOf(join.trim().toUpperCase()));
            }
            config.setText(row.getString("text"));
            config.setKey(row.getString("key"));
            config.setValue(row.get("values"));
            config.datatype(row.getString("datatype"));
            config.setOverCondition(row.getBoolean("over_condition", false));
            config.setOverValue(row.getBoolean("over_value", true));
            config.setCompare(compare(row.getInt("compare", Compare.EQUAL.getCode())));
        }
        return config;
    }
    public static ConfigChain parseConfigChain(DataRow row) {
        ConfigChain chain = null;
        chain = new DefaultConfigChain();
        String join = row.getString("join");
        if(null != join){
            chain.setJoin(Condition.JOIN.valueOf(join.trim().toUpperCase()));
        }
        chain.setText(row.getString("text"));
        DataSet items = row.getSet("items");
        if(null != items) {
            for(DataRow item:items) {
                Config config = parseConfig(item);
                if(null != config) {
                    chain.addConfig(config);
                }
            }
        }
        return chain;
    }
    public static Compare compare(int code) {
        for(Compare compare:Compare.values()) {
            if(compare.getCode() == code) {
                return compare;
            }
        }
        return Compare.EQUAL;
    }
}
/*
{
    "columns": {
        "query": [],
        "exclude": []
    },
    "conditions": {
        "join": "AND",
        "items": [
            {
                "join": "AND",
                "items": [
                    {
                        "join": "AND",
                        "text": null,
                        "key": null,
                        "var": "ID",
                        "compare": 10,
                        "values": [1],
                        datatype: "int", //数据类型 以StandardTypeMetadata枚举为准不区分大小写
                        "over_condition": false,
                        "over_value": true,
                        "parser": {
                            "prefix": null,
                            "var": "ID",
                            "class": null,
                            "method": null,
                            "key": null,
                            "default": [],
                            "compare": 10,
                            "join": "AND"
                        }
                    },
                    {
                        "join": "AND",
                        "text": null,
                        "key": null,
                        "var": "NAME",
                        "compare": 50,
                        "values": [
                            "ZH"
                        ],
                        datatype: "varchar",
                        "over_condition": false,
                        "over_value": true,
                        "parser": {
                            "prefix": null,
                            "var": "NAME",
                            "class": null,
                            "method": null,
                            "key": null,
                            "default": [

                            ],
                            "compare": 50,
                            "join": "AND"
                        }
                    },
                    {
                        "join": "AND",
                        "text": null,
                        "key": null,
                        "var": "TYPE_CODE",
                        "compare": 40,
                        "values": [
                            "1",
                            "2",
                            "3"
                        ],
                        "over_condition": false,
                        "over_value": true,
                        "parser": {
                            "prefix": null,
                            "var": "TYPE_CODE",
                            "class": null,
                            "method": null,
                            "key": null,
                            "default": [

                            ],
                            "compare": 40,
                            "join": "AND"
                        }
                    }
                ]
            },
            {
                "join": "OR",
                "items": [
                    {
                        "join": "AND",
                        "items": [
                            {
                                "join": "AND",
                                "text": null,
                                "key": null,
                                "var": "A",
                                "compare": 10,
                                "values": [
                                    1
                                ],
                                "over_condition": false,
                                "over_value": true,
                                "parser": {
                                    "prefix": null,
                                    "var": "A",
                                    "class": null,
                                    "method": null,
                                    "key": null,
                                    "default": [

                                    ],
                                    "compare": 10,
                                    "join": "AND"
                                }
                            },
                            {
                                "join": "OR",
                                "text": null,
                                "key": null,
                                "var": "B",
                                "compare": 10,
                                "values": null,
                                "over_condition": false,
                                "over_value": true,
                                "parser": {
                                    "prefix": null,
                                    "var": "B",
                                    "class": null,
                                    "method": null,
                                    "key": null,
                                    "default": [

                                    ],
                                    "compare": 10,
                                    "join": "OR"
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    }
}
{
        tables:[{
            table:'hr_department'    //数据源-关联表名
            join:'left'              //连接方式(inner：内连接, left：左连接 ,right：右连接)
            relations:[{             //关联条件
                join:'and'                         //关联方式(可选and,or,ors)
                column:'id'                        //列名
                compare:10                         //比较运算符
                value:null                         //常量值或变量(二选一)
                relattion_table:'hr_employee'      //比较表名(二选一)
                relattion_column:'department_id'   //比较列名
            }] //end-relattions
        }] //end-tables

        ,conditions:[{                             //过滤条件
            join:'and'
            table:'hr_employee'
            column:'type_id'
            compare:10
            value:100                               //常量值或变量
            items:[{
                与上级一致
            }]
        }]//end-conditions
        ,havings:[{                                 //分组过滤条件
            join:'and'
            method:'sum'
            table:'hr_employee'
            column:'type_id'
            compare:10
            value:100                               //常量值或变量
        }]//end-havings
        ,orders:[{                                  //排序
            table:'hr_employee'
            column:'type_id'
            type:'ASC'
        }]
        ,groups:[{                                  //分组
            table:'hr_employee'
            column:'type_id'
        }]
}
* */