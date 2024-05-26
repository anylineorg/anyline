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

import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.entity.DataRow;

public class ConfigBuilder {
    public static ConfigStore build(String json){
        ConfigStore configs = new DefaultConfigStore();
        DataRow row = DataRow.parseJson(json);
        return configs;
    }

    public static void main(String[] args) {
        DefaultConfigStore configs = new DefaultConfigStore();
        configs.and("ID", 100);
        configs.and("CODE", 200);

        System.out.println();
    }
}
/*
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