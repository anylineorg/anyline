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
 *
 *
 */
package org.anyline.data.listener;

import org.anyline.data.param.ConfigStore;
import org.anyline.entity.data.Procedure;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;

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
    public boolean beforeBuildQuery(RunPrepare prepare, ConfigStore configs, String ... conditions);


    /**
     * 统计总记录数之前调用
     * 
     * @param run sql
     */
    public void beforeTotal(Run run);
    /**
     * 统计总记录数之后调用
     * 
     * @param run sql
     * @param total total
     * @param millis 耗时(毫秒)
     */
    public void afterTotal(Run run, int total, long millis);
    /**
     * 查询之前调用<br/>
     * 不满足查询条件的不会走到这一步(如必须参数未提供)
     * 只有确定执行查询时才会到这一步，到了这一步已经不能修改查询条件<br/>
     * 要修改查询条件可以在afterCreateQuery实现
     * @param run sql
     * @param total 上一步合计的总行数
     */
    public void beforeQuery(Run run, int total);
    /**
     * 查询之后调用(调用service.map或service.maps)
     * 
     * @param run sql
     * @param maps 查询结果
     * @param millis 耗时(毫秒)
     */
    public void afterQuery(Run run, List<?>  maps, long millis);
    public void afterQuery(Run run, EntitySet<?> maps, long millis);
    /**
     * 查询之后调用(调用service.query或service.querys)
     * 
     * @param run sql
     * @param set 查询结果
     * @param millis 耗时(毫秒)
     */
    public void afterQuery(Run run, DataSet set, long millis);
    /**
     * count之前调用
     * 
     * @param run sql
     */
    public void beforeCount(Run run);
    /**
     * count之后调用
     * 
     * @param run sql
     * @param count count
     * @param millis 耗时(毫秒)
     */
    public void afterCount(Run run, int count, long millis);

    /**
     * 判断是否存在之前调用
     * 
     * @param run sql
     */
    public void beforeExists(Run run);
    /**
     * 判断是否存在之后调用
     * 
     * @param run sql
     * @param exists 是否存在
     * @param millis 耗时(毫秒)
     */
    public void afterExists(Run run, boolean exists, long millis);


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
    public boolean beforeBuildUpdate(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns);


    /**
     * 更新之前调用
     * 
     * @param run run
     * @param dest 需要更新的表
     * @param obj 更新内容
     * @param columns 需要更新的列
     * @return 是否执行  如果返回false 将不执行更新
     */
    public boolean beforeUpdate(Run run, String dest, Object obj, List<String> columns);
    /**
     * 更新之前调用
     * 
     * @param run run
     * @param count 影响行数
     * @param dest 需要更新的表
     * @param obj 更新内容
     * @param columns 需要更新的列
     * @param millis 耗时(毫秒)
     */
    public void afterUpdate(Run run, int count, String dest, Object obj, List<String> columns, long millis);


    /**
     * 创建插入相关的SQL之前调用<br/>
     * 要修改插入内容可以在这一步实现,注意不是在beforeInsert
     * @param dest 表
     * @param obj 实体
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
     * @return 如果返回false 则中断执行
     */
    public boolean beforeBuildInsert(String dest, Object obj, boolean checkPrimary, List<String> columns);
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
    public boolean beforeInsert(Run run, String dest, Object obj, boolean checkPrimary, List<String> columns);

    /**
     * 插入之后调用
     * 
     * @param run sql
     * @param count 影响行数
     * @param dest 需要插入的表
     * @param obj 接入内容
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要插入的列
     * @param millis 耗时(毫秒)
     */
    public void afterInsert(Run run, int count, String dest, Object obj, boolean checkPrimary, List<String> columns, long millis);

    /**
     * 执行SQL之前调用
     * 
     * @param run sql
     * @return 是否执行 如果返回false装不执行sql
     */
    public boolean beforeExecute(Run run);

    /**
     * 执行SQL之后调用
     * 
     * @param run sql
     * @param count 影响行数
     * @param millis 耗时(毫秒)
     */
    public void afterExecute(Run run, int count, long millis);

    /**
     * 执行存储过程之前调用
     * 
     * @param procedure 存储过程
     * @return 是否执行 如果返回false装不执行存储过程
     */
    public boolean beforeExecute(Procedure procedure);

    /**
     * 执行存储过程之后调用
     * 
     * @param procedure 存储过程
     * @param result 执行是否成功 如果需要返回值需要从procedure中获取
     * @param millis 耗时(毫秒)
     */
    public void afterExecute(Procedure procedure, boolean result, long millis);

    /**
     * 查询存过程之前调用
     * 
     * @param procedure 存储过程
     */
    public void beforeQuery(Procedure procedure);

    /**
     * 查询存储过程之后调用
     * 
     * @param procedure 存储过程
     * @param set 返回结果集
     * @param millis 耗时(毫秒)
     */
    public void afterQuery(Procedure procedure, DataSet set, long millis);

    /**
     * 创建删除SQL前调用(根据Entity/DataRow),修改删除条件可以在这一步实现<br/>
     * 注意不是beforeDelete<br/>
     * 注意beforeBuildDelete有两个函数需要实现
     * service.delete(DataRow/Entity);
     * @param dest 表
     * @param obj entity或DataRow
     * @param columns 删除条件的我
     * @return 如果返回false 则中断执行
     */
    public boolean beforeBuildDelete(String dest, Object obj, String ... columns);
    /**
     * 创建删除SQL前调用(根据条件),修改删除条件可以在这一步实现<br/>
     * 注意不是beforeDelete<br/>
     * 注意beforeBuildDelete有两个函数需要实现
     * service.delete("CRM_USER", "ID", "1", "2", "3");
     * @param table 表
     * @param key key
     * @param values values
     * @return 如果返回false 则中断执行
     */
    public boolean beforeBuildDelete(String table, String key, Object values);
    /**
     * 执行删除前调用
     * 
     * @param run sql
     * @return 是否执行 如果返回false装不执行删除
     */
    public boolean beforeDelete(Run run);

    /**
     * 执行删除后调用
     * 
     * @param run sql
     * @param count 影响行数
     * @param millis 耗时(毫秒)
     */
    public void afterDelete(Run run, int count, long millis);

    public void slow(String action, Run run, String sql, List inputs, List outputs, long millis);
}
