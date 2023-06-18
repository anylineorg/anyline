/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (JDBCRuntime runtime, the "License"){}
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
 *
 *
 */
package org.anyline.data.listener;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.data.Procedure;

import java.util.List;

public interface DMListener {

    /**
     * 创建查相关的SQL之前调用,包括slect exists count等<br/>
     * 要修改查询条件可以在这一步实现,注意不是在beforeQuery
     * @param prepare  prepare
     * @param configs 查询条件配置
     * @param conditions 查询条件
     * @return 如果返回false 则中断执行
     */
    default boolean prepareQuery(JDBCRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){return true;}


    /**
     * 统计总记录数之前调用
     * 
     * @param run sql
     */
    default void beforeTotal(JDBCRuntime runtime, Run run){}
    /**
     * 统计总记录数之后调用
     * 
     * @param run sql
     * @param total total
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterTotal(JDBCRuntime runtime, Run run, int total, long millis){}
    /**
     * 查询之前调用<br/>
     * 不满足查询条件的不会走到这一步(JDBCRuntime runtime, 如必须参数未提供)
     * 只有确定执行查询时才会到这一步，到了这一步已经不能修改查询条件<br/>
     * 要修改查询条件可以在prepareQuery实现
     * @param run sql
     * @param total 上一步合计的总行数
     */
    default void beforeQuery(JDBCRuntime runtime, Run run, int total){}
    /**
     * 查询之后调用(JDBCRuntime runtime, 调用service.map或service.maps)
     * 
     * @param run sql
     * @param maps 查询结果
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterQuery(JDBCRuntime runtime, Run run, List<?>  maps, long millis){}
    default void afterQuery(JDBCRuntime runtime, Run run, EntitySet<?> maps, long millis){}
    /**
     * 查询之后调用(JDBCRuntime runtime, 调用service.query或service.querys)
     * 
     * @param run sql
     * @param set 查询结果
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterQuery(JDBCRuntime runtime, Run run, DataSet set, long millis){}
    /**
     * count之前调用
     * 
     * @param run sql
     */
    default void beforeCount(JDBCRuntime runtime, Run run){}
    /**
     * count之后调用
     * 
     * @param run sql
     * @param count count
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterCount(JDBCRuntime runtime, Run run, int count, long millis){}

    /**
     * 判断是否存在之前调用
     * 
     * @param run sql
     */
    default void beforeExists(JDBCRuntime runtime, Run run){}
    /**
     * 判断是否存在之后调用
     * 
     * @param run sql
     * @param exists 是否存在
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterExists(JDBCRuntime runtime, Run run, boolean exists, long millis){}


    /**
     * 创建更新相关的SQL之前调用<br/>
     * 要修改更新内容或条件可以在这一步实现,注意不是在beforeUpdate
     * @param dest 表
     * @param obj Entity或DtaRow
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要更新的列
     * @param configs 更新条件
     * @return 如果返回false 则中断执行
     */
    default boolean prepareUpdate(JDBCRuntime runtime, String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){return true;}


    /**
     * 更新之前调用
     * 
     * @param run run
     * @param dest 需要更新的表
     * @param obj 更新内容
     * @param columns 需要更新的列
     * @return 是否执行  如果返回false 将不执行更新
     */
    default boolean beforeUpdate(JDBCRuntime runtime, Run run, String dest, Object obj, List<String> columns){return true;}
    /**
     * 更新之前调用
     * 
     * @param run run
     * @param count 影响行数
     * @param dest 需要更新的表
     * @param obj 更新内容
     * @param columns 需要更新的列
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterUpdate(JDBCRuntime runtime, Run run, int count, String dest, Object obj, List<String> columns, long millis){}


    /**
     * 创建插入相关的SQL之前调用<br/>
     * 要修改插入内容可以在这一步实现,注意不是在beforeInsert
     * @param dest 表
     * @param obj 实体
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
     * @return 如果返回false 则中断执行
     */
    default boolean prepareInsert(JDBCRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns){return true;}
    /**
     * 创建insert sql之前调用
     * 
     * @param run sql
     * @param dest 需要插入的表
     * @param obj 接入内容
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要插入的列
     * @return 是否执行  如果返回false 将不执行插入
     */
    default boolean beforeInsert(JDBCRuntime runtime, Run run, String dest, Object obj, boolean checkPrimary, List<String> columns){return true;}

    /**
     * 插入之后调用
     * 
     * @param run sql
     * @param count 影响行数
     * @param dest 需要插入的表
     * @param obj 接入内容
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要插入的列
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterInsert(JDBCRuntime runtime, Run run, int count, String dest, Object obj, boolean checkPrimary, List<String> columns, long millis){}

    /**
     * 执行SQL之前调用
     * 
     * @param run sql
     * @return 是否执行 如果返回false装不执行sql
     */
    default boolean beforeExecute(JDBCRuntime runtime, Run run){return true;}

    /**
     * 执行SQL之后调用
     * 
     * @param run sql
     * @param count 影响行数
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterExecute(JDBCRuntime runtime, Run run, int count, long millis){}

    /**
     * 执行存储过程之前调用
     * 
     * @param procedure 存储过程
     * @return 是否执行 如果返回false装不执行存储过程
     */
    default boolean prepareExecute(JDBCRuntime runtime, Procedure procedure){return true;}
    default boolean beforeExecute(JDBCRuntime runtime, Procedure procedure){return true;}

    /**
     * 执行存储过程之后调用
     * 
     * @param procedure 存储过程
     * @param result 执行是否成功 如果需要返回值需要从procedure中获取
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterExecute(JDBCRuntime runtime, Procedure procedure, boolean result, long millis){}

    /**
     * 查询存过程之前调用
     * 
     * @param procedure 存储过程
     */
    default void beforeQuery(JDBCRuntime runtime, Procedure procedure){}

    /**
     * 查询存储过程之后调用
     * 
     * @param procedure 存储过程
     * @param set 返回结果集
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterQuery(JDBCRuntime runtime, Procedure procedure, DataSet set, long millis){}

    /**
     * 创建删除SQL前调用(JDBCRuntime runtime, 根据Entity/DataRow),修改删除条件可以在这一步实现<br/>
     * 注意不是beforeDelete<br/>
     * 注意prepareDelete有两个函数需要实现
     * service.delete(JDBCRuntime runtime, DataRow/Entity){}
     * @param dest 表
     * @param obj entity或DataRow
     * @param columns 删除条件的我
     * @return 如果返回false 则中断执行
     */
    default boolean prepareDelete(JDBCRuntime runtime, String dest, Object obj, String ... columns){return true;}
    /**
     * 创建删除SQL前调用(JDBCRuntime runtime, 根据条件),修改删除条件可以在这一步实现<br/>
     * 注意不是beforeDelete<br/>
     * 注意prepareDelete有两个函数需要实现
     * service.delete(JDBCRuntime runtime, "CRM_USER", "ID", "1", "2", "3"){}
     * @param table 表
     * @param key key
     * @param values values
     * @return 如果返回false 则中断执行
     */
    default boolean prepareDelete(JDBCRuntime runtime, String table, String key, Object values){return true;}
    /**
     * 执行删除前调用
     * 
     * @param run sql
     * @return 是否执行 如果返回false装不执行删除
     */
    default boolean beforeDelete(JDBCRuntime runtime, Run run){return true;}

    /**
     * 执行删除后调用
     * 
     * @param run sql
     * @param count 影响行数
     * @param millis 耗时(JDBCRuntime runtime, 毫秒)
     */
    default void afterDelete(JDBCRuntime runtime, Run run, int count, long millis){}

    default void slow(JDBCRuntime runtime, String action, Run run, String sql, List inputs, List outputs, long millis){}
}
