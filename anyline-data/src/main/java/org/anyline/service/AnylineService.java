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



package org.anyline.service;

import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.handler.DataHandler;
import org.anyline.data.handler.EntityHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionState;
import org.anyline.entity.*;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.MetadataDiffer;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.TransactionProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineService<E>{
	AnylineService setDao(AnylineDao dao);
	AnylineDao getDao();

	/**
	 * 相关数据源
	 * @return String
	 */
	String datasource();
	DriverAdapter adapter();
	DataRuntime runtime();

	/**
	 * 验证相关数据源是否正常,异常会被捕捉并返回false
	 * @return boolean
	 */
	default boolean validity() {
		return DataSourceHolder.validity(datasource());
	}
	/**
	 * 验证相关数据源是否正常
	 * 有可能会抛出异常
	 * @return boolean
	 * @throws Exception 如果连接异常会抛出
	 */
	default boolean hit() throws Exception {
		return DataSourceHolder.hit(datasource());
	}
	default List<String> copy() {
		return DataSourceHolder.copy(datasource());
	}

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * BATCH INSERT		: 批量插入
	 * UPDATE			: 更新
	 * SAVE				: 根据情况插入或更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 * CACHE			: 缓存
	 * METADATA			: 简单格式元数据, 只返回NAME
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													INSERT
	 ******************************************************************************************************************/
	/**
	 * 插入数据
	 * @param batch 批量执行每批最多数量
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data entity或list或DataRow或DataSet
	 * @param columns 需要插入哪些列
	 * @return 影响行数
	 */
	long insert(int batch, String dest, Object data, ConfigStore configs, List<String> columns);
	default long insert(int batch, String dest, Object data, List<String> columns) {
		return insert(batch, dest, data, null, columns);
	}
	default long insert(int batch, Object data, String ... columns) {
		return insert(batch, (String)null, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, ConfigStore configs, String ... columns) {
		return insert(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(int batch, String dest, Object data, String ... columns) {
		return insert(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(String dest, Object data, List<String> columns) {
		return insert(0, dest, data, columns);
	}
	default long insert(String dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(0, dest, data, configs, columns);
	}
	default long insert(String dest, Object data, String ... columns) {
		return insert(dest, data, BeanUtil.array2list(columns));
	}
	default long insert(String dest, Object data, ConfigStore configs, String ... columns) {
		return insert(dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(Object data, String ... columns) {
		return insert((String)null, data, BeanUtil.array2list(columns));
	}
	default long insert(Object data, ConfigStore configs, String ... columns) {
		return insert((String)null, data, configs, BeanUtil.array2list(columns));
	}

	long insert(int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long insert(int batch, Table dest, Object data, List<String> columns) {
		return insert(batch, dest, data, null, columns);
	}
	default long insert(int batch, Table dest, Object data, String ... columns) {
		return insert(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(Table dest, Object data, List<String> columns) {
		return insert(0, dest, data, columns);
	}
	default long insert(Table dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(0, dest, data, configs, columns);
	}
	default long insert(Table dest, Object data, String ... columns) {
		return insert(dest, data, BeanUtil.array2list(columns));
	}
	default long insert(Table dest, Object data, ConfigStore configs, String ... columns) {
		return insert(dest, data, configs, BeanUtil.array2list(columns));
	}


	/* *****************************************************************************************************************
	 * 													INSERT SELECT
	 ******************************************************************************************************************/
	/**
	 * insert into table select * from table
	 * 与query参数一致
	 * @param dest 插入表
	 * @param origin 查询表
	 * @param configs 查询条件及相关配置
	 * @param obj 查询条件
	 * @param conditions 查询条件
	 * @return 影响行数
	 */
	long insert(Table dest, Table origin, ConfigStore configs, Object obj, String ... conditions);
	default long insert(Table dest, Table origin, Object obj, String ... conditions) {
		return insert(dest, origin, (ConfigStore) null, obj, conditions);
	}
	default long insert(Table dest, Table origin, long first, long last, Object obj, String ... conditions) {
		return insert(dest, origin, new DefaultConfigStore(first, last), obj, conditions);
	}
	default long insert(Table dest, Table origin, ConfigStore configs, String ... conditions) {
		return insert(dest, origin, configs, null, conditions);
	}
	default long insert(Table dest, Table origin, String ... conditions) {
		return insert(dest, origin, (ConfigStore) null, null, conditions);
	}
	default long insert(Table dest, Table origin, PageNavi navi, String ... conditions) {
		return insert(dest, origin, new DefaultConfigStore().setPageNavi(navi), null, conditions);
	}
	default long insert(Table dest, Table origin, long first, long last, String ... conditions) {
		return insert(dest, origin, first, last, null, conditions);
	}

	default long insert(String dest, String origin, ConfigStore configs, Object obj, String ... conditions){
		return insert(new Table(dest), new Table(origin), configs, obj, conditions);
	}
	default long insert(String dest, String origin, Object obj, String ... conditions) {
		return insert(dest, origin, (ConfigStore) null, obj, conditions);
	}
	default long insert(String dest, String origin, long first, long last, Object obj, String ... conditions) {
		return insert(dest, origin, new DefaultConfigStore(first, last), obj, conditions);
	}
	default long insert(String dest, String origin, ConfigStore configs, String ... conditions) {
		return insert(dest, origin, configs, null, conditions);
	}
	default long insert(String dest, String origin, String ... conditions) {
		return insert(dest, origin, (ConfigStore) null, null, conditions);
	}
	default long insert(String dest, String origin, PageNavi navi, String ... conditions) {
		return insert(dest, origin, new DefaultConfigStore().setPageNavi(navi), null, conditions);
	}
	default long insert(String dest, String origin, long first, long last, String ... conditions) {
		return insert(dest, origin, first, last, null, conditions);
	}


	/**
	 * insert into table select * from table
	 * 复杂的查询先通过TableBuilder构造查询
	 * @param dest 表 table(c1,c2,c3)需要插入的列可以通过addColumn添加到dest中
	 * @param prepare 一般通过TableBuilder生成查询
	 * @return 影响行数
	 */
	long insert(Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default long insert(Table dest, RunPrepare prepare, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return insert(dest, prepare, configs, obj, conditions);
	}
	default long insert(Table dest, RunPrepare prepare, Object obj, String ... conditions) {
		return insert(dest, prepare, (ConfigStore) null, obj, conditions);
	}
	default long insert(Table dest, RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return insert(dest, prepare, configs, obj, conditions);
	}

	default long insert(Table dest, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return insert(dest, prepare, configs, null, conditions);
	}
	default long insert(Table dest, RunPrepare prepare, String ... conditions) {
		return insert(dest, prepare, (ConfigStore) null, null, conditions);
	}
	default long insert(Table dest, RunPrepare prepare, long first, long last, String ... conditions) {
		return insert(dest, prepare, first, last, null, conditions);
	}


	default long insert(String dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
		return insert(new Table<>(dest), prepare, configs, obj, conditions);
	}
	default long insert(String dest, RunPrepare prepare, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		return insert(new Table(dest), prepare, first, last, configs, obj, conditions);
	}
	default long insert(String dest, RunPrepare prepare, Object obj, String ... conditions) {
		return insert(dest, prepare, (ConfigStore) null, obj, conditions);
	}
	default long insert(String dest, RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return insert(dest, prepare, configs, obj, conditions);
	}

	default long insert(String dest, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return insert(dest, prepare, configs, null, conditions);
	}
	default long insert(String dest, RunPrepare prepare, String ... conditions) {
		return insert(dest, prepare, (ConfigStore) null, null, conditions);
	}
	default long insert(String dest, RunPrepare prepare, long first, long last, String ... conditions) {
		return insert(dest, prepare, first, last, null, conditions);
	}


	/* *****************************************************************************************************************
	 * 													UPDATE
	 ******************************************************************************************************************/
	/**
	 * 更新记录
	 * 默认情况下以主键为更新条件, 需在更新的数据保存在data中
	 * 如果提供了dest则更新dest表, 如果没有提供则根据data解析出表名
	 * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键, 则根据TYPE_CODE更新
	 * 可以提供了ConfigStore以实现更复杂的更新条件
	 * 需要更新的列通过 columns提供
	 * @param batch 批量执行每批最多数量
	 * @param columns	需要更新的列
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 		更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
	 * @param configs 	更新条件
	 * @return int 影响行数
	 */

	long update(int batch, String dest, Object data, ConfigStore configs, List<String>columns);
	default long update(int batch, String dest, Object data, String ... columns) {
		return update(batch, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return update(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Object data, String ... columns) {
		return update(batch, (String)null, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, Object data, ConfigStore configs, String ... columns) {
		return update(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}

	default long update(String dest, Object data, ConfigStore configs, List<String>columns) {
		return update(0, dest, data, configs, columns);
	}
	default long update(String dest, Object data, String ... columns) {
		return update(dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(String dest, Object data, ConfigStore configs, String ... columns) {
		return update(dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, String ... columns) {
		return update((String)null, data, null, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, String ... columns) {
		return update((String)null, data, configs, BeanUtil.array2list(columns));
	}

	long update(int batch, Table dest, Object data, ConfigStore configs, List<String>columns);
	default long update(int batch, Table dest, Object data, String ... columns) {
		return update(batch, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return update(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Table dest, Object data, ConfigStore configs, List<String>columns) {
		return update(0, dest, data, configs, columns);
	}
	default long update(Table dest, Object data, String ... columns) {
		return update(dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(Table dest, Object data, ConfigStore configs, String ... columns) {
		return update(dest, data, configs, BeanUtil.array2list(columns));
	}
	/* *****************************************************************************************************************
	 * 													SAVE
	 ******************************************************************************************************************/
	/**
	 * save insert区别
	 * 操作单个对象时没有区别
	 * 在操作集合时区别:
	 * save会循环操作数据库每次都会判断insert|update
	 * save 集合中的数据可以是不同的表不同的结构
	 * insert 集合中的数据必须保存到相同的表, 结构必须相同
	 * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
	 *
	 * 保存(insert|update)根据是否有主键值确定insert或update
	 * @param batch 批量执行每批最多数量
	 * @param data  数据
	 * @param columns 指定更新或保存的列
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return 影响行数
	 */
	long save(int batch, String dest, Object data, ConfigStore configs, List<String> columns);
	default long save(int batch, String dest, Object data, List<String> columns) {
		return save(batch, dest, data, null, columns);
	}
	default long save(int batch, String dest, Object data, String ... columns) {
		return save(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return save(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(int batch, Object data, String ... columns) {
		return save(batch, (String)null, data, columns);
	}
	default long save(int batch, Object data, ConfigStore configs, String ... columns) {
		return save(batch, (String)null, data, configs, columns);
	}
	default long save(String dest, Object data, List<String> columns) {
		return save(0, dest, data, columns);
	}
	default long save(String dest, Object data, ConfigStore configs, List<String> columns) {
		return save(0, dest, data, configs, columns);
	}
	default long save(String dest, Object data, String ... columns) {
		return save(dest, data, BeanUtil.array2list(columns));
	}
	default long save(String dest, Object data, ConfigStore configs, String ... columns) {
		return save(dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(Object data, String ... columns) {
		return save((String)null, data, columns);
	}
	default long save(Object data, ConfigStore configs, String ... columns) {
		return save((String)null, data, configs, columns);
	}
	default long save(Object data, List<String> columns) {
		return save((String)null, data, columns);
	}
	default long save(Object data, ConfigStore configs, List<String> columns) {
		return save((String)null, data, configs, columns);
	}

	long save(int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long save(int batch, Table dest, Object data, List<String> columns) {
		return save(batch, dest, data, null, columns);
	}

	default long save(int batch, Table dest, Object data, String ... columns) {
		return save(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return save(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(Table dest, Object data, List<String> columns) {
		return save(0, dest, data, columns);
	}
	default long save(Table dest, Object data, ConfigStore configs, List<String> columns) {
		return save(0, dest, data, configs, columns);
	}
	default long save(Table dest, Object data, String ... columns) {
		return save(dest, data, BeanUtil.array2list(columns));
	}
	default long save(Table dest, Object data, ConfigStore configs, String ... columns) {
		return save(dest, data, configs, BeanUtil.array2list(columns));
	}

	/* *****************************************************************************************************************
	 * 													QUERY
	 ******************************************************************************************************************/

	/**
	 * 按条件查询
	 * @param dest 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件
	 * @param conditions	固定查询条件 <br/>
	 * 			CD:1 生成SQL: CD = 1<br/>
	 * 			CD: 忽略<br/>
	 * 			CD:null 忽略<br/>
	 * 			CD:NULL 生成SQL:CD IS NULL<br/>
	 * 			原生SQL(包括GROUP、ORDER、HAVING等)如 ID > 1 AND ID < 10<br/>
	 * 			${原生SQL}:${}之内的SQL不全处理 如果原生SQL比较复杂(如出现小时格式)可能与以上几种格式混淆, 可以用${}表示不解析按原文执行<br/>
	 * 			
	 * @return DataSet
	 */
	DataSet querys(String dest, ConfigStore configs, Object obj, String ... conditions);
	default DataSet querys(DataSet set, ConfigStore configs) {
		if(null != configs) {
			ConfigChain chain = configs.getConfigChain();
			if(null != chain) {
				List<Config> list = chain.getConfigs();
				if(null != list) {
					for(Config config:list) {
						set = set.select.filter(config.getCompare(), config.getVariable(), config.getValues());
					}
				}
			}
			PageNavi navi = configs.getPageNavi();
			if(null != navi) {
				set = set.getRows(navi);
			}
		}
		return set;
	}
	default DataSet querys(String dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return querys(dest, configs, obj, conditions);
	}
	default DataSet querys(String dest, Object obj, String ... conditions) {
		return querys(dest, (ConfigStore) null, obj, conditions);
	}
	default void querys(String dest, DataHandler handler, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(dest, configs, obj, conditions);
	}
	default DataSet querys(String dest, PageNavi navi, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.setPageNavi(navi);
		return querys(dest, configs, obj, conditions);
	}

	/**
	 * 按条件查询
	 * @param dest 			数据源(表或自定义SQL或SELECT语句)
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param first 起 下标从0开始
	 * @param last 止
	 * @param conditions	固定查询条件
	 * @return DataSet
	 */
	default DataSet querys(String dest, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return querys(dest, configs, obj, conditions);
	}

	default DataSet querys(String dest, ConfigStore configs, String ... conditions) {
		return querys(dest, configs, null, conditions);
	}
	default DataSet querys(String dest, long first, long last, ConfigStore configs, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return querys(dest, configs, conditions);
	}
	default DataSet querys(String dest, String ... conditions) {
		return querys(dest, (Object) null, conditions);
	}
	default void querys(String dest, DataHandler handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(dest, configs, conditions);
	}
	default DataSet querys(String dest, PageNavi navi, String ... conditions) {
		return querys(dest, navi, null, conditions);
	}
	default DataSet querys(String dest, long first, long last, String ... conditions) {
		return querys(dest, first, last, null, conditions);
	}
	default DataSet querys(String dest, DataHandler handler, long first, long last, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		ConfigStore configs = new DefaultConfigStore();
		navi.scope(first, last);
		configs.setPageNavi(navi);
		configs.handler(handler);
		return querys(dest, first, last, configs, conditions);
	}

	DataSet querys(Table dest, ConfigStore configs, Object obj, String ... conditions);
	default DataSet querys(Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return querys(dest, configs, obj, conditions);
	}
	default DataSet querys(Table dest, Object obj, String ... conditions) {
		return querys(dest, (ConfigStore) null, obj, conditions);
	}
	default void querys(Table dest, DataHandler handler, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(dest, configs, obj, conditions);
	}
	default DataSet querys(Table dest, PageNavi navi, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.setPageNavi(navi);
		return querys(dest, configs, obj, conditions);
	}

	/**
	 * 按条件查询
	 * @param dest 			数据源(表或自定义SQL或SELECT语句)
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param first 起 下标从0开始
	 * @param last 止
	 * @param conditions	固定查询条件
	 * @return DataSet
	 */
	default DataSet querys(Table dest, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return querys(dest, configs, obj, conditions);
	}

	default DataSet querys(Table dest, ConfigStore configs, String ... conditions) {
		return querys(dest, configs, null, conditions);
	}
	default DataSet querys(Table dest, long first, long last, ConfigStore configs, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return querys(dest, configs, conditions);
	}
	default DataSet querys(Table dest, String ... conditions) {
		return querys(dest, (Object) null, conditions);
	}
	default void querys(Table dest, DataHandler handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(dest, configs, conditions);
	}
	default DataSet querys(Table dest, PageNavi navi, String ... conditions) {
		return querys(dest, navi, null, conditions);
	}
	default DataSet querys(Table dest, long first, long last, String ... conditions) {
		return querys(dest, first, last, null, conditions);
	}
	default DataSet querys(Table dest, DataHandler handler, long first, long last, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		ConfigStore configs = new DefaultConfigStore();
		navi.scope(first, last);
		configs.setPageNavi(navi);
		configs.handler(handler);
		return querys(dest, first, last, configs, conditions);
	}
	DataRow query(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	DataRow query(String dest, ConfigStore configs, Object obj, String ... conditions);
	default DataRow query(String dest, Object obj, String ... conditions) {
		return query(dest, null, obj, conditions);
	}
	default DataRow query(String dest, ConfigStore configs, String ... conditions) {
		return query(dest, configs, null, conditions);
	}
	default DataRow query(String dest, String ... conditions) {
		return query(dest, (ConfigStore) null, conditions);
	}
	default DataRow query(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return query(prepare, configs, null, conditions);
	}
	default DataRow query(RunPrepare prepare, Object obj, String ... conditions) {
		return query(prepare, null, obj, conditions);
	}
	default DataRow query(RunPrepare prepare, String ... conditions) {
		return query(prepare, null, null, conditions);
	}
	DataRow query(Table dest, ConfigStore configs, Object obj, String ... conditions);
	default DataRow query(Table dest, Object obj, String ... conditions) {
		return query(dest, null, obj, conditions);
	}
	default DataRow query(Table dest, ConfigStore configs, String ... conditions) {
		return query(dest, configs, null, conditions);
	}
	default DataRow query(Table dest, String ... conditions) {
		return query(dest, (ConfigStore) null, conditions);
	}

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param name 	序列名
	 * @return long 查询失败返回null
	 */
	BigDecimal sequence(boolean next, String name);
	/**
	 * 查询序列next value
	 * @param name 序列名
	 * @return long 查询失败返回null
	 */
	default BigDecimal sequence(String name) {
		return sequence(true, name);
	}

	/**
	 * 查询序列cur 或 next value
	 * @param names 序列名
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @return DataRow 查询结果按序列名保存到DataRow中，查询失败返回null
	 */
	DataRow sequences(boolean next, String ... names);
	default DataRow sequences(String ... names) {
		return sequences(true, names);
	}

	/**
	 * 根据SQL或自定义SQL返回实体
	 * @param dest SQL或自定义SQL
	 * @param clazz 返回类型
	 * @param configs 根据http等上下文构造查询条件
	 * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 固定查询条件
	 * @return EntitySet
	 * @param <T> T
	 */
	<T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, PageNavi navi, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.setPageNavi(navi);
		return selects(dest, clazz, configs, entity, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, T entity, String ... conditions) {
		return selects(dest, clazz, (ConfigStore) null, entity, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return selects(dest, clazz, configs, entity, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, long first, long last, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return selects(dest, clazz, configs, entity, conditions);
	}

	default <T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
		return selects(dest, clazz, configs, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, PageNavi navi, String ... conditions) {
		return selects(dest, clazz, navi, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, String ... conditions) {
		return selects(dest, clazz, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, EntityHandler<T> handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return selects(dest, clazz, configs, conditions);
	}
	default <T> EntitySet<T> selects(String dest, Class<T> clazz, long first, long last, String ... conditions) {
		return selects(dest, clazz, first, last, (T) null, conditions);
	}

	<T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, PageNavi navi, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.setPageNavi(navi);
		return selects(dest, clazz, configs, entity, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, T entity, String ... conditions) {
		return selects(dest, clazz, (ConfigStore) null, entity, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return selects(dest, clazz, configs, entity, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, long first, long last, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return selects(dest, clazz, configs, entity, conditions);
	}

	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
		return selects(dest, clazz, configs, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, PageNavi navi, String ... conditions) {
		return selects(dest, clazz, navi, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, String ... conditions) {
		return selects(dest, clazz, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, EntityHandler<T> handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return selects(dest, clazz, configs, conditions);
	}
	default <T> EntitySet<T> selects(Table dest, Class<T> clazz, long first, long last, String ... conditions) {
		return selects(dest, clazz, first, last, (T) null, conditions);
	}

	<T> T select(String dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	default <T> T select(String dest, Class<T> clazz, T entity, String ... conditions) {
		return select(dest, clazz, (ConfigStore) null, entity, conditions);
	}
	default <T> T select(String dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
		return select(dest, clazz, configs, (T) null, conditions);
	}
	default <T> T select(String dest, Class<T> clazz, String ... conditions) {
		return select(dest, clazz, (T) null, conditions);
	}
	<T> T select(Table dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	default <T> T select(Table dest, Class<T> clazz, T entity, String ... conditions) {
		return select(dest, clazz, (ConfigStore) null, entity, conditions);
	}
	default <T> T select(Table dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
		return select(dest, clazz, configs, (T) null, conditions);
	}
	default <T> T select(Table dest, Class<T> clazz, String ... conditions) {
		return select(dest, clazz, (T) null, conditions);
	}

	/**
	 *
	 * @param clazz 返回类型
	 * @param configs 根据http等上下文构造查询条件
	 * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 固定查询条件
	 * @return EntitySet
	 * @param <T> T
	 */
	<T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	default <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.setPageNavi(navi);
		return selects(clazz, configs, entity, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, T entity, String ... conditions) {
		return selects(clazz, (ConfigStore) null, entity, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return selects(clazz, configs, entity, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, long first, long last, T entity, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return selects(clazz, configs, entity, conditions);
	}
	<T> T select(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	default <T> T select(Class<T> clazz, T entity, String ... conditions) {
		return select(clazz, (ConfigStore) null, entity, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, String ... conditions) {
		return selects(clazz, configs, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, String ... conditions) {
		return selects(clazz, navi, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, String ... conditions) {
		return selects(clazz, (T) null, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, EntityHandler<T> handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return selects(clazz, configs, conditions);
	}
	default <T> EntitySet<T> selects(Class<T> clazz, long first, long last, String ... conditions) {
		return selects(clazz, first, last, (T) null, conditions);
	}

	default <T> T select(Class<T> clazz, ConfigStore configs, String ... conditions) {
		return select(clazz, configs, (T) null, conditions);
	}
	default <T> T select(Class<T> clazz, String ... conditions) {
		return select(clazz, (T) null, conditions);
	}
	/*根据service构造泛型查询*/
	EntitySet<E> gets(ConfigStore configs, String ... conditions);
	default EntitySet<E> gets(PageNavi navi, String ... conditions) {
		return gets(new DefaultConfigStore().setPageNavi(navi), conditions);
	}

	// 与DataSet querys(String dest, String ... conditions);  签名冲突
	default EntitySet<E> gets(String ... conditions) {
		return gets((ConfigStore) null, conditions);
	}
	default EntitySet<E> gets(EntityHandler<E> handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		return gets(configs, conditions);
	}
	default EntitySet<E> gets(long first, long last, String ... conditions) {
		return gets(new DefaultConfigStore(first, last), conditions);
	}
	E get(ConfigStore configs, String ... conditions);
	default E get(String ... conditions) {
		return get(null, conditions);
	}

	/**
	 * 直接返回Map集合不封装, 不分页
	 * @param dest			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions	固定查询条件
	 * @return List
	 */
	List<Map<String, Object>> maps(String dest, ConfigStore configs, Object obj, String ... conditions);
	default void maps(String dest, DataHandler handler, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		maps(dest, configs, obj, conditions);
	}
	default List<Map<String, Object>> maps(String dest, Object obj, String ... conditions) {
		return maps(dest, (ConfigStore) null, obj, conditions);
	}
	default List<Map<String, Object>> maps(String dest, long first, long last, Object obj, String ... conditions) {
		return maps(dest, new DefaultConfigStore(first, last), obj, conditions);
	}
	default List<Map<String, Object>> maps(String dest, ConfigStore configs, String ... conditions) {
		return maps(dest, configs, null, conditions);
	}
	default List<Map<String, Object>> maps(String dest, String ... conditions) {
		return maps(dest, (ConfigStore) null, null, conditions);
	}
	default void maps(String dest, DataHandler handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		maps(dest, configs, null, conditions);
	}
	default List<Map<String, Object>> maps(String dest, PageNavi navi, String ... conditions) {
		return maps(dest, new DefaultConfigStore().setPageNavi(navi), null, conditions);
	}
	default List<Map<String, Object>> maps(String dest, long first, long last, String ... conditions) {
		return maps(dest, first, last, null, conditions);
	}
	default List<Map<String, Object>> maps(String dest, DataHandler handler, long first, long last, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		configs.handler(handler);
		return maps(dest, first, last, conditions, conditions);
	}

	List<Map<String, Object>> maps(Table dest, ConfigStore configs, Object obj, String ... conditions);
	default void maps(Table dest, DataHandler handler, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		maps(dest, configs, obj, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, Object obj, String ... conditions) {
		return maps(dest, (ConfigStore) null, obj, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, long first, long last, Object obj, String ... conditions) {
		return maps(dest, new DefaultConfigStore(first, last), obj, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, ConfigStore configs, String ... conditions) {
		return maps(dest, configs, null, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, String ... conditions) {
		return maps(dest, (ConfigStore) null, null, conditions);
	}
	default void maps(Table dest, DataHandler handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		maps(dest, configs, null, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, PageNavi navi, String ... conditions) {
		return maps(dest, new DefaultConfigStore().setPageNavi(navi), null, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, long first, long last, String ... conditions) {
		return maps(dest, first, last, null, conditions);
	}
	default List<Map<String, Object>> maps(Table dest, DataHandler handler, long first, long last, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		configs.handler(handler);
		return maps(dest, first, last, configs, conditions);
	}

	/**
	 *
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param obj 根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	List<Map<String, Object>> maps(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default List<Map<String, Object>> maps(RunPrepare prepare, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return maps(prepare, configs, obj, conditions);
	}
	default List<Map<String, Object>> maps(RunPrepare prepare, Object obj, String ... conditions) {
		return maps(prepare, (ConfigStore) null, obj, conditions);
	}
	default void maps(RunPrepare prepare, StreamHandler handler, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(prepare, configs, obj, conditions);
	}
	default List<Map<String, Object>> maps(RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return maps(prepare, configs, obj, conditions);
	}

	default List<Map<String, Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return maps(prepare, configs, null, conditions);
	}
	default List<Map<String, Object>> maps(RunPrepare prepare, String ... conditions) {
		return maps(prepare, (ConfigStore) null, null, conditions);
	}
	default void maps(RunPrepare prepare, StreamHandler handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		maps(prepare, configs, null, conditions);
	}
	default List<Map<String, Object>> maps(RunPrepare prepare, long first, long last, String ... conditions) {
		return maps(prepare, first, last, null, conditions);
	}
	/**
	 * 列名转找成参数名 可以给condition()提供参数用来接收前端参数
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return List
	 */
	List<String> column2param(String table);
	/**
	 * 如果二级缓存开启 会从二级缓存中提取数据
	 * @param cache			对应ehcache缓存配置文件 中的cache.name
	 * @param dest 			查询或操作的目标(表、存储过程、SQL等)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 	固定查询条件
	 * @return DataSet
	 */
	DataSet caches(String cache, String dest, ConfigStore configs, Object obj, String ... conditions);
	default DataSet caches(String cache, String dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return caches(cache, dest, configs, obj, conditions);
	}
	default DataSet caches(String cache, String dest, Object obj, String ... conditions) {
		return caches(cache, dest, null, obj, conditions);
	}
	default DataSet caches(String cache, String dest, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return caches(cache, dest, configs, obj, conditions);
	}

	DataSet caches(String cache, Table dest, ConfigStore configs, Object obj, String ... conditions);
	default DataSet caches(String cache, Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return caches(cache, dest, configs, obj, conditions);
	}
	default DataSet caches(String cache, Table dest, Object obj, String ... conditions) {
		return caches(cache, dest, null, obj, conditions);
	}
	default DataSet caches(String cache, Table dest, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return caches(cache, dest, configs, obj, conditions);
	}

	/**
	 * @param cache			对应ehcache缓存配置文件 中的cache.name
	 * @param dest 			查询或操作的目标(表、存储过程、SQL等)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 	固定查询条件
	 * @return DataSet
	 */
	DataRow cache(String cache, String dest, ConfigStore configs, Object obj, String ... conditions);
	default DataRow cache(String cache, String dest, Object obj, String ... conditions) {
		return cache(cache, dest, null, obj, conditions);
	}
	default DataSet caches(String cache, String dest, ConfigStore configs, String ... conditions) {
		return caches(cache, dest, configs, (Object) null, conditions);
	}
	default DataSet caches(String cache, String dest, long first, long last, ConfigStore configs, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return caches(cache, dest, configs, conditions);
	}
	default DataSet caches(String cache, String dest, String ... conditions) {
		return caches(cache, dest, null, null, conditions);
	}
	default DataSet caches(String cache, String dest, long first, long last, String ... conditions) {
		return caches(cache, dest, first, last, null, conditions);
	}
	default DataRow cache(String cache, String dest, ConfigStore configs, String ... conditions) {
		return cache(cache, dest, configs, null, conditions);
	}
	default DataRow cache(String cache, String dest, String ... conditions) {
		return cache(cache, dest, null, null, conditions);
	}

	DataRow cache(String cache, Table dest, ConfigStore configs, Object obj, String ... conditions);
	default DataRow cache(String cache, Table dest, Object obj, String ... conditions) {
		return cache(cache, dest, null, obj, conditions);
	}
	default DataSet caches(String cache, Table dest, ConfigStore configs, String ... conditions) {
		return caches(cache, dest, configs, (Object) null, conditions);
	}
	default DataSet caches(String cache, Table dest, long first, long last, ConfigStore configs, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return caches(cache, dest, configs, conditions);
	}
	default DataSet caches(String cache, Table dest, String ... conditions) {
		return caches(cache, dest, null, null, conditions);
	}
	default DataSet caches(String cache, Table dest, long first, long last, String ... conditions) {
		return caches(cache, dest, first, last, null, conditions);
	}
	default DataRow cache(String cache, Table dest, ConfigStore configs, String ... conditions) {
		return cache(cache, dest, configs, null, conditions);
	}
	default DataRow cache(String cache, Table dest, String ... conditions) {
		return cache(cache, dest, null, null, conditions);
	}
	/*多表查询, 左右连接时使用*/

	/**
	 *
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param obj 根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default DataSet querys(RunPrepare prepare, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return querys(prepare, configs, obj, conditions);
	}
	default DataSet querys(RunPrepare prepare, Object obj, String ... conditions) {
		return querys(prepare, (ConfigStore) null, obj, conditions);
	}
	default void querys(RunPrepare prepare, StreamHandler handler, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(prepare, configs, obj, conditions);
	}
	default DataSet querys(RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return querys(prepare, configs, obj, conditions);
	}

	default DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return querys(prepare, configs, null, conditions);
	}
	default DataSet querys(RunPrepare prepare, String ... conditions) {
		return querys(prepare, (ConfigStore) null, null, conditions);
	}
	default void querys(RunPrepare prepare, StreamHandler handler, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore();
		configs.handler(handler);
		querys(prepare, configs, null, conditions);
	}
	default DataSet querys(RunPrepare prepare, long first, long last, String ... conditions) {
		return querys(prepare, first, last, null, conditions);
	}

	/**
	 *
	 * @param cache 缓存 channel
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param obj 根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default DataSet caches(String cache, RunPrepare prepare, Object obj, String ... conditions) {
		return caches(cache, prepare, null, obj, conditions);
	}
	default DataSet caches(String cache, RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
		ConfigStore configs = new DefaultConfigStore(first, last);
		return caches(cache, prepare, configs, obj, conditions);
	}

	/**
	 *
	 * @param cache 缓存 channel
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param obj 根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default DataRow cache(String cache, RunPrepare prepare, Object obj, String ... conditions) {
		return cache(cache, prepare, null, obj, conditions);
	}
	default DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return caches(cache, prepare, configs, null, conditions);
	}
	default DataSet caches(String cache, RunPrepare prepare, String ... conditions) {
		return caches(cache, prepare, null, null, conditions);
	}
	default DataSet caches(String cache, RunPrepare prepare, long first, long last, String ... conditions) {
		return caches(cache, prepare, first, last, null, conditions);
	}
	default DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return cache(cache, prepare, configs, null, conditions);
	}
	default DataRow cache(String cache, RunPrepare prepare, long first, long last, ConfigStore configs, String ... conditions) {
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		navi.scope(first, last);
		configs.setPageNavi(navi);
		return cache(cache, prepare, configs, conditions);
	}
	default DataRow cache(String cache, RunPrepare prepare, String ... conditions) {
		return cache(cache, prepare, null, null, conditions);
	}

	/**
	 * 删除缓存 参数保持与查询参数完全一致
	 * @param channel 		channel
	 * @param dest 			查询或操作的目标(表、存储过程、SQL等)
	 * @param configs  		根据http等上下文构造查询条件
	 * @param conditions 	固定查询条件
	 * @return boolean
	 */
	boolean removeCache(String channel, String dest, ConfigStore configs, String ... conditions);
	default boolean removeCache(String channel, String dest, String ... conditions) {
		return removeCache(channel, dest, null, conditions);
	}
	default boolean removeCache(String channel, String dest, long first, long last, String ... conditions) {
		return removeCache(channel, dest, new DefaultConfigStore(first, last), conditions);
	}

	boolean removeCache(String channel, Table dest, ConfigStore configs, String ... conditions);
	default boolean removeCache(String channel, Table dest, String ... conditions) {
		return removeCache(channel, dest, null, conditions);
	}
	default boolean removeCache(String channel, Table dest, long first, long last, String ... conditions) {
		return removeCache(channel, dest, new DefaultConfigStore(first, last), conditions);
	}

	/**
	 * 清空缓存
	 * @param channel channel
	 * @return boolean
	 */
	boolean clearCache(String channel);
	/**
	 * 清空全部缓存
	 * @return boolean
	 */
	boolean clearCaches();

	/* *****************************************************************************************************************
	 * 													EXISTS
	 ******************************************************************************************************************/

	/**
	 * 是否存在
	 *
	 * @param dest       查询或操作的目标(表、存储过程、SQL等)
	 * @param configs    根据http等上下文构造查询条件
	 * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 固定查询条件
	 * @return boolean
	 */
	boolean exists(String dest, ConfigStore configs, Object obj, String ... conditions);
	default boolean exists(String dest, Object obj, String ... conditions) {
		return exists(dest, null, obj, conditions);
	}
	default boolean exists(String dest, ConfigStore configs, String ... conditions) {
		return exists(dest, configs, null, conditions);
	}
	default boolean exists(String dest, String ... conditions) {
		return exists(dest, null, null, conditions);
	}
	boolean exists(String dest, DataRow row);
	default boolean exists(DataRow row) {
		return exists((String)null, row);
	}

	boolean exists(Table dest, ConfigStore configs, Object obj, String ... conditions);
	default boolean exists(Table dest, Object obj, String ... conditions) {
		return exists(dest, null, obj, conditions);
	}
	default boolean exists(Table dest, ConfigStore configs, String ... conditions) {
		return exists(dest, configs, null, conditions);
	}
	default boolean exists(Table dest, String ... conditions) {
		return exists(dest, null, null, conditions);
	}
	boolean exists(Table dest, DataRow row);

	/* *****************************************************************************************************************
	 * 													COUNT
	 ******************************************************************************************************************/

	/**
	 * count
	 * @param dest 查询或操作的目标(表、存储过程、SQL等)
	 * @param configs 过滤条件
	 * @param obj 根据obj生成的过滤条件
	 * @param conditions 简单过滤条件
	 * @return long
	 */
	long count(String dest, ConfigStore configs, Object obj, String ... conditions);
	default long count(String dest, Object obj, String ... conditions) {
		return count(dest, null, obj, conditions);
	}
	default long count(String dest, ConfigStore configs, String ... conditions) {
		return count(dest, configs, null, conditions);
	}
	default long count(String dest, String ... conditions) {
		return count(dest, null, null, conditions);
	}

	long count(Table dest, ConfigStore configs, Object obj, String ... conditions);
	default long count(Table dest, Object obj, String ... conditions) {
		return count(dest, null, obj, conditions);
	}
	default long count(Table dest, ConfigStore configs, String ... conditions) {
		return count(dest, configs, null, conditions);
	}
	default long count(Table dest, String ... conditions) {
		return count(dest, null, null, conditions);
	}

	long count(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default long count(RunPrepare prepare, Object obj, String ... conditions) {
		return count(prepare, null, obj, conditions);
	}
	default long count(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return count(prepare, configs, null, conditions);
	}
	default long count(RunPrepare prepare, String ... conditions) {
		return count(prepare, null, null, conditions);
	}

	/* *****************************************************************************************************************
	 * 													EXECUTE
	 ******************************************************************************************************************/

	/**
	 * 执行存储过程
	 * @param procedure 存储过程
	 * @param inputs 输入参数
	 * @return 执行是否成功
	 */
	boolean execute(Procedure procedure, String... inputs);
	/** 
	 * 执行 
	 * @param dest  查询或操作的目标(表、存储过程、SQL等)
	 * @param configs  configs
	 * @param conditions  conditions
	 * @return int
	 */ 
	long execute(String dest, ConfigStore configs, String ... conditions);
	default long execute(String dest, String ... conditions) {
		return execute(dest, null, conditions);
	}

	/**
	 * 执行SQL文件
	 * @param file 文件内容
	 * @param charset 文件编码
	 * @param brk 失败后是否中断
	 * @return 成功数量,失败数量
	 */
	default int[] execute(File file, Charset charset, boolean brk) {
		String txt = FileUtil.read(file).toString();
		return execute(txt, brk);
	}

	default int[] execute(File file, boolean brk) {
		return execute(file, StandardCharsets.UTF_8, brk);
	}

	default int[] execute(String txt, boolean brk) {
		int success = 0;
		int fail = 0;
		String sqls[] = txt.split(";");
		for(String sql:sqls) {
			if(BasicUtil.isNotEmpty(sql)) {
				try{
					execute(sql);
					success++;
				}catch (Exception e) {
					fail ++;
					e.printStackTrace();
					if(brk) {
						break;
					}
				}
			}
		}
		return new int[]{success, fail};
	}

	/**
	 * 执行命令<br/>
	 * execute(10, sql, List&lt;List&lt;Object&gt;&gt;);<br/>
	 * 批量执行要求SQL一样
	 * @param batch 大于1时表示批量执行
	 * @param sql sql
	 * @param values 如果是批量执行，需要提供一个二维数组
	 * @return 影响行数批量执行时不准确
	 */
	long execute(int batch, String sql, Collection<Object> values);
	/**
	 * 执行命令<br/>
	 * execute(10, sql, List&lt;List&lt;Object&gt;&gt;);<br/>
	 * 批量执行要求SQL一样
	 * @param batch 大于1时表示批量执行
	 * @param vol 每行多少个占位符
	 * @param sql sql
	 * @param values 如果是批量执行，需要提供一个二维数组
	 * @return 影响行数批量执行时不准确
	 */
	long execute(int batch, int vol, String sql, Collection<Object> values);

	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @param inputs  inputs
	 * @return boolean
	 */ 
	default boolean executeProcedure(String procedure, String... inputs) {
		Procedure proc = new Procedure();
		proc.setName(procedure);
		for (String input : inputs) {
			proc.addInput(input);
		}
		return execute(proc);
	}

	/**
	 * 查询存储过程
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @param inputs 输入参数
	 * @return DataSet
	 */
	DataSet querys(Procedure procedure, PageNavi navi, String ... inputs);
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @param first  first
	 * @param last  last
	 * @param inputs  inputs
	 * @return DataSet
	 */
	default DataSet querysProcedure(String procedure, long first, long last, String ... inputs) {
		PageNavi navi = new DefaultPageNavi();
		navi.scope(first, last);
		return querysProcedure(procedure, navi, inputs);
	}
	default DataSet querysProcedure(String procedure, PageNavi navi, String ... inputs) {
		Procedure proc = new Procedure();
		proc.setName(procedure);
		if (null != inputs) {
			for (String input : inputs) {
				proc.addInput(input);
			}
		}
		return querys(proc, navi);
	}
	default DataSet querysProcedure(String procedure, String ... inputs) {
		return querysProcedure(procedure, null, inputs);
	}
	default DataSet querys(Procedure procedure, String ... inputs) {
		return querys(procedure, null, inputs);
	}
	default DataSet querys(Procedure procedure, long first, long last, String ... inputs) {
		PageNavi navi = new DefaultPageNavi();
		navi.scope(first, last);
		return querys(procedure, navi, inputs);
	}

	default DataRow queryProcedure(String procedure, String ... inputs) {
		Procedure proc = new Procedure();
		proc.setName(procedure);
		return query(procedure, inputs);
	}
	DataRow query(Procedure procedure, String ... inputs);

	/* *****************************************************************************************************************
	 * 													DELETE
	 ******************************************************************************************************************/
	/**
	 * 根据ConfigStore中的条件+conditions条件删除
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param configs 匹配条件
	 * @param conditions  匹配条件
	 * @return 影响行数
	 */
	long delete(String dest, ConfigStore configs, String ... conditions);
	long delete(Table dest, ConfigStore configs, String ... conditions);
	/**
	 * 删除 根据columns列删除 可设置复合主键<br/>
	 * 注意:为了避免整表删除, columns必须提供否则会抛出异常 <br/>
	 * 如果要删除整表需要单独写原生的SQL调用execute(sql) <br/>
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 数据
	 * @param columns 生成删除条件的列, 如果不设置则根据主键删除
	 * @return 影响行数
	 */
	long delete(String dest, DataSet set, String ... columns);
	long delete(Table dest, DataSet set, String ... columns);
	default long delete(DataSet set, String ... columns) {
		return delete((Table)null, set, columns);
	}
	long delete(String dest, DataRow row, String ... columns);
	long delete(Table dest, DataRow row, String ... columns);

	/**
	 * 根据columns列删除 <br/>
	 * 注意:为了避免整表删除, columns必须提供否则会抛出异常 <br/>
	 * 如果要删除整表需要单独写原生的SQL调用execute(sql) <br/>
	 * delete(User/DataRow, "TYPE","AGE")<br/>
	 * DELETE FROM USER WHERE TYPE = ? AND AGE = ?
	 * @param obj 实体对象或DataRow/Dataset
	 * @param columns 生成删除条件的列, 如果不设置则根据主键删除
	 * @return 影响行数
	 */
	long delete(Object obj, String ... columns);

	/**
	 * 根据多列条件删除<br/>
	 * 注意:为了避免整表删除, values必须提供否则会抛出异常<br/>
	 * 整表删除请调用service.execute("DELETE FROM TAB");或service.truncate("TAB“)<br/>
	 * 以k, v, k, v或"k:v"形式提供参数<br/>
	 * delete("HR_EMPLOYEE","type","1","age:20");<br/>
	 * DELETE FROM HR_EMPLOYEE WHERE TYPE = 1 AND AGE = 20<br/>
	 *<br/>
	 * 注意以下两咱情况, 并不会忽略空值
	 *<br/>
	 * service.delete("HR_EMPLOYEE","ID","","CODE:20");<br/>
	 * DELETE FROM HR_EMPLOYEE WHERE ID = '' AND CODE = 20<br/>
	 *<br/>
	 * service.delete("HR_EMPLOYEE","ID","1","CODE:");<br/>
	 * DELETE FROM HR_EMPLOYEE WHERE ID = 1 AND CODE = ''<br/>
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param kvs key-value
	 * @return 影响行数
	 */
	long delete(String table, String ... kvs);
	long delete(Table table, String ... kvs);
	long delete(Table table, Collection values);
	long delete(String table, Collection values);
	/**
	 * 根据一列的多个值删除<br/>
	 * 注意:为了避免整表删除, values必须提供否则会抛出异常<br/>
	 * 整表删除请调用service.execute("DELETE FROM TAB");或service.truncate("TAB“)<br/>
	 * delete("USER","TYPE", [1, 2, 3])<br/>
	 * DELETE FROM USER WHERE TYPE IN(1, 2, 3)
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	<T> long deletes(int batch, String table, String key, Collection<T> values);
	<T> long deletes(int batch, Table table, String key, Collection<T> values);
	default <T> long deletes(String table, String key, Collection<T> values) {
		return deletes(0, table, key, values);
	}
	default <T> long deletes(Table table, String key, Collection<T> values) {
		return deletes(0, table, key, values);
	}

	/**
	 * 根据一列的多个值删除<br/>
	 * 注意:为了避免整表删除, values必须提供否则会抛出异常<br/>
	 * 整表删除请调用service.execute("DELETE FROM TAB");或service.truncate("TAB“)<br/>
	 * delete("USER","TYPE","1","2","3")<br/>
	 * DELETE FROM USER WHERE TYPE IN(1, 2, 3)
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 名
	 * @param values 值集合
	 * @return 影响行数
	 */
	<T> long deletes(int batch, String table, String key, T ... values);
	<T> long deletes(int batch, Table table, String key, T ... values);
	default <T> long deletes(String table, String key, T ... values) {
		return deletes(0, table, key, values);
	}
	default <T> long deletes(Table table, String key, T ... values) {
		return deletes(0, table, key, values);
	}
	long truncate(String table);
	long truncate(Table table);

	/* *****************************************************************************************************************
	 * 													METADATA
	 ******************************************************************************************************************/

	List<String> tables(Catalog catalog, Schema schema, String name, int types);
	default List<String> tables(String catalog, String schema, String name, int types) {
		Catalog _catalog = null;
		if(BasicUtil.isNotEmpty(catalog)) {
			_catalog = new Catalog(catalog);
		}
		return tables(_catalog, new Schema(schema), name, types);
	}
	default List<String> tables(Schema schema, String name, int types) {
		return tables(null, schema, name, types);
	}
	default List<String> tables(String schema, String name, int types) {
		return tables(null, new Schema(schema), name, types);
	}
	default List<String> tables(String name, int types) {
		return tables((Catalog) null, null, name, types);
	}
	default List<String> tables(int types) {
		return tables((Catalog) null, null, null, types);
	}
	default List<String> tables() {
		return tables(Table.TYPE.NORMAL.value);
	}

	List<String> views(boolean greedy, Catalog catalog, Schema schema, String name, int types);
	default List<String> views(boolean greedy, Schema schema, String name, int types) {
		return views(greedy, null, schema, name, types);
	}
	default List<String> views(boolean greedy, String name, int types) {
		return views(greedy, null, null, name, types);
	}
	default List<String> views(boolean greedy, int types) {
		return views(greedy, null, null, null, types);
	}
	default List<String> views(boolean greedy) {
		return views(greedy, View.TYPE.NORMAL.value);
	}
	default List<String> views(Catalog catalog, Schema schema, String name, int types) {
		return views(false, catalog, schema, name, types);
	}
	default List<String> views(Schema schema, String name, int types) {
		return views(false, null, schema, name, types);
	}
	default List<String> views(String name, int types) {
		return views(false, null, null, name, types);
	}
	default List<String> views(int types) {
		return views(false, null, null, null, types);
	}
	default List<String> views() {
		return views(false, View.TYPE.NORMAL.value);
	}

	List<String> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types);
	default List<String> masterTables(boolean greedy, Schema schema, String name, int types) {
		return masterTables(greedy, null, schema, name, types);
	}
	default List<String> masterTables(boolean greedy, String name, int types) {
		return masterTables(greedy, null, null, name, types);
	}
	default List<String> masterTables(boolean greedy, int types) {
		return masterTables(greedy, null, null, null, types);
	}
	default List<String> masterTables(boolean greedy) {
		return masterTables(greedy, MasterTable.TYPE.NORMAL.value);
	}
	default List<String> masterTables(Catalog catalog, Schema schema, String name, int types) {
		return masterTables(false, catalog, schema, name, types);
	}
	default List<String> masterTables(Schema schema, String name, int types) {
		return masterTables(false, null, schema, name, types);
	}
	default List<String> masterTables(String name, int types) {
		return masterTables(false, null, null, name, types);
	}
	default List<String> masterTables(int types) {
		return masterTables(false, null, null, null, types);
	}
	default List<String> masterTables() {
		return masterTables(false, MasterTable.TYPE.NORMAL.value);
	}

	List<String> columns(boolean greedy, Table table);
	default List<String> columns(boolean greedy, String table) {
		return columns(greedy, (Catalog) null, null, table);
	}
	default List<String> columns(boolean greedy, Catalog catalog, Schema schema, String table) {
		return columns(greedy, new Table(catalog, schema, table));
	}
	default List<String> columns(boolean greedy, String catalog, String schema, String table) {
		return columns(greedy, new Table(catalog, schema, table));
	}
	default List<String> columns(Table table) {
		return columns(false, table);
	}
	default List<String> columns(String table) {
		return columns(false, (Catalog) null, null, table);
	}
	default List<String> columns(Catalog catalog, Schema schema, String table) {
		return columns(false, catalog, schema, table);
	}

	List<String> tags(boolean greedy, Table table);
	default List<String> tags(boolean greedy, Catalog catalog, Schema schema, String table) {
		return tags(greedy, new Table(catalog, schema, table));
	}
	default List<String> tags(boolean greedy, String table) {
		return tags(greedy, null, null, table);
	}
	default List<String> tags(Table table) {
		return tags(false, table);
	}
	default List<String> tags(String table) {
		return tags(false, new Table(table));
	}
	default List<String> tags(Catalog catalog, Schema schema, String table) {
		return tags(false, new Table(catalog, schema, table));
	}

	public boolean save(Table table) throws Exception;
	public boolean save(Column column) throws Exception ;
	public boolean drop(Table table) throws Exception;
	public boolean drop(Column column) throws Exception;

	DDLService ddl();
	MetaDataService metadata();

	/**
	 * 根据结果集对象获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
	 * @param sql sql
	 * @param comment 是否需要列注释
	 * @param condition 是否需要拼接查询条件, 如果需要会拼接where 1=0 条件(默认不添加，通常情况下SQL自带查询条件，给参数赋值NULL达到相同的效果)
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, Column> metadata(String sql, boolean comment, boolean condition);
	default LinkedHashMap<String, Column> metadata(String sql) {
		return metadata(sql, false, false);
	}
	ConfigStore condition();

	/**
	 * 启动事务
	 * @param define 事务定义
	 * @return TransactionState 回滚或提交时用到
	 * @throws Exception Exception
	 */
	default TransactionState start(TransactionDefine define) throws Exception{
		return TransactionProxy.start(datasource(), define);
	}
	default TransactionState start() throws Exception{
		return TransactionProxy.start(datasource());
	}
	default TransactionState start(int behavior) throws Exception{
		return TransactionProxy.start(datasource(), behavior);
	}

	/**
	 * 提交事务
	 * @param state 启动事务时返回status
	 */
	default void commit(TransactionState state) throws Exception {
		TransactionProxy.commit(state);
	}
	/**
	 * 回滚事务
	 * @param state 启动事务时返回status
	 */
	default void rollback(TransactionState state) throws Exception {
		TransactionProxy.rollback(state);
	}
	/**
	 * 根据差异生成SQL
	 * @param differ differ
	 * @return sqls
	 */
	List<Run> ddl(MetadataDiffer differ);
	/**
	 * 根据差异生成SQL
	 * @param differs differs
	 * @return sqls
	 */
	List<Run> ddl(List<MetadataDiffer> differs);

	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/
	interface MetaDataService{
		/* *************************************************************************************************************
		 * 													database
		 **************************************************************************************************************/

		/**
		 * 当前数据源 数据库类型
		 * @return DatabaseType
		 */
		DatabaseType type();
		/**
		 * 当前数据源 数据库版本 版本号比较复杂 不是全数字
		 * @return String
		 */
		String version();
		/**
		 * 当前数据源 数据库描述(产品名称+版本号)
		 * @return String
		 */
		String product();
		Database database();
		/**
		 * 查询全部数据库
		 * @return databases
		 */
		LinkedHashMap<String, Database> databases(String name);
		default LinkedHashMap<String, Database> databases() {
			return databases(null);
		}
		List<Database> databases(boolean greedy, String name);
		default List<Database> databases(boolean greedy) {
			return databases(greedy, null);
		}
		Database database(String name);

		/* *************************************************************************************************************
		 * 													catalog
		 **************************************************************************************************************/
		Catalog catalog();
		LinkedHashMap<String, Catalog> catalogs(String name);
		default LinkedHashMap<String, Catalog> catalogs() {
			return catalogs(null);
		}
		List<Catalog> catalogs(boolean greedy, String name);
		default List<Catalog> catalogs(boolean greedy) {
			return catalogs(greedy, null);
		}

		/* *************************************************************************************************************
		 * 													schema
		 **************************************************************************************************************/
		Schema schema();
		LinkedHashMap<String, Schema> schemas(Catalog catalog, String name);
		default LinkedHashMap<String, Schema> schemas(Catalog catalog) {
			return schemas(catalog, null);
		}
		default LinkedHashMap<String, Schema> schemas() {
			return schemas(null, null);
		}
		default LinkedHashMap<String, Schema> schemas(String name) {
			return schemas(null, name);
		}
		List<Schema> schemas(boolean greedy, Catalog catalog, String name);
		default List<Schema> schemas(boolean greedy) {
			return schemas(greedy, null, null);
		}
		default List<Schema> schemas(boolean greedy, Catalog catalog) {
			return schemas(greedy, catalog, null);
		}
		default List<Schema> schemas(boolean greedy, String name) {
			return schemas(greedy, null, name);
		}
		/* *************************************************************************************************************
		 * 													table
		 **************************************************************************************************************/

		/**
		 * 表是否存在
		 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @return boolean
		 */
		boolean exists(boolean greedy, Table table);
		default boolean exists(Table table) {
			return exists(false, table);
		}

		/**
		 * tables
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param types Metadata.TYPE
		 * @return tables
		 */
		<T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
			return tables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return tables(greedy, catalog, schema, name, types, structs, configs);
		}
		default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return tables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, int struct) {
			return tables(greedy, null, schema, name, types, struct);
		}
		default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, boolean struct) {
			return tables(greedy, null, schema, name, types, struct);
		}
		default <T extends Table> List<T> tables(boolean greedy, String name, int types, boolean struct) {
			return tables(greedy, null, null, name, types, struct);
		}
		default <T extends Table> List<T> tables(boolean greedy, String name, int types, int struct) {
			return tables(greedy, null, null, name, types, struct);
		}
		default <T extends Table> List<T> tables(boolean greedy, int types, int struct) {
			return tables(greedy, null, types, struct);
		}
		default <T extends Table> List<T> tables(boolean greedy, int types, boolean struct) {
			return tables(greedy, null, types, struct);
		}
		default <T extends Table> List<T> tables(boolean greedy, boolean struct) {
			return tables(greedy, Table.TYPE.NORMAL.value, struct);
		}

		<T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct){
			return tables(catalog, schema, name, types, struct, null);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return tables(catalog, schema, name, types, structs, configs);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return tables(catalog, schema, name, types, struct, null);
		}

		default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, int struct) {
			return tables(null, schema, name, types, struct);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, boolean struct) {
			return tables(null, schema, name, types, struct);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, int struct) {
			return tables(null, null, name, types, struct);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, boolean struct) {
			return tables(null, null, name, types, struct);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(int types, int struct) {
			return tables(null, types, struct);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(int types, boolean struct) {
			return tables(null, types, struct);
		}
		default <T extends Table> LinkedHashMap<String, T> tables() {
			return tables( Table.TYPE.NORMAL.value, false);
		}

		default <T extends Table> LinkedHashMap<String, T> tables(int types, int struct, ConfigStore configs) {
			return tables(null, null, null, types, struct, configs);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(int types, boolean struct, ConfigStore configs) {
			return tables(null, null, null,  types, struct, configs);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(ConfigStore configs) {
			return tables(Table.TYPE.NORMAL.value, false, configs);
		}


		default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
			return tables(greedy, catalog, schema, name, types, false, configs);
		}
		default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
			return tables(greedy, catalog, schema, name, types, false);
		}
		default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types) {
			return tables(greedy, null, schema, name, types, false);
		}
		default <T extends Table> List<T> tables(boolean greedy, String name, int types) {
			return tables(greedy, null, null, name, types, false);
		}
		default <T extends Table> List<T> tables(boolean greedy, int types) {
			return tables(greedy, null, types, false);
		}
		default <T extends Table> List<T> tables(boolean greedy) {
			return tables(greedy, Table.TYPE.NORMAL.value, false);
		}
		default <T extends Table> List<T> tables(boolean greedy, int types, ConfigStore configs) {
			return tables(greedy, null, null, null, types, configs);
		}
		default <T extends Table> List<T> tables(boolean greedy, ConfigStore configs) {
			return tables(greedy, null, null, null, Table.TYPE.NORMAL.value, configs);
		}

		default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types) {
			return tables(catalog, schema, name, types, false);
		}

		default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types) {
			return tables(null, schema, name, types, false);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(String name, int types) {
			return tables(null, null, name, types, false);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(int types) {
			return tables(null, types, false);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(int types, ConfigStore configs) {
			return tables(null,null,null, types, false, configs);
		}

		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param struct 是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
		 * @return Table
		 */
		Table table(boolean greedy, Catalog catalog, Schema schema, String name, int struct);
		default Table table(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return table(greedy, catalog, schema, name, structs);
		}
		default Table table(boolean greedy, Schema schema, String name, int struct) {
			return table(greedy, null, schema, name, struct);
		}
		default Table table(boolean greedy, Schema schema, String name, boolean struct) {
			return table(greedy, null, schema, name, struct);
		}
		default Table table(boolean greedy, String name, int struct) {
			return table(greedy, null, null, name, struct);
		}
		default Table table(boolean greedy, String name, boolean struct) {
			return table(greedy, null, null, name, struct);
		}

		Table table(Catalog catalog, Schema schema, String name, int struct);
		default Table table(Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return table(catalog, schema, name, structs);
		}
		default Table table(Schema schema, String name, int struct) {
			return table(false, null, schema, name, struct);
		}
		default Table table(Schema schema, String name, boolean struct) {
			return table(false, null, schema, name, struct);
		}
		default Table table(String name, int struct) {
			return table(false, null, null, name, struct);
		}
		default Table table(String name, boolean struct) {
			return table(false, null, null, name, struct);
		}
		default Table table(boolean greedy, Catalog catalog, Schema schema, String name) {
			return table(greedy, catalog, schema, name, true);
		}
		default Table table(boolean greedy, Schema schema, String name) {
			return table(greedy, null, schema, name, true);
		}
		default Table table(boolean greedy, String name) {
			return table(greedy, null, null, name, true);
		}

		default Table table(Catalog catalog, Schema schema, String name) {
			return table( catalog, schema, name, true);
		}
		default Table table(Schema schema, String name) {
			return table(null, schema, name, true);
		}
		default Table table(String name) {
			return table(null, null, name, true);
		}

		/**
		 * 表的创建SQL
		 * @param table table
		 * @param init 是否还原初始状态 默认false
		 * @return ddl
		 */
		List<String> ddl(Table table, boolean init);
		default List<String> ddl(String table, boolean init) {
			return ddl(new Table(table), init);
		}
		default List<String> ddl(Table table) {
			return ddl(table, false);
		}
		default List<String> ddl(String table) {
			return ddl(new Table(table));
		}

		/* *****************************************************************************************************************
		 * 													view
		 ******************************************************************************************************************/

		/**
		 * 视图是否存在
		 * @param view 视图 如果不提供视图名则根据data解析, 视图名可以事实前缀&lt;数据源名&gt;视图示切换数据源
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @return boolean
		 */
		boolean exists(boolean greedy, View view);
		default boolean exists(View view) {
			return exists(false, view);
		}
		/**
		 * views
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的视图的话, 可以直接设置为null, 如果设置为特定的视图名称, 则返回该视图的具体信息。
		 * @param types Metadata.TYPE
		 * @return views
		 */
		<T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
			return views(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return views(greedy, catalog, schema, name, types, structs, configs);
		}
		default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return views(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends View> List<T> views(boolean greedy, Schema schema, String name, int types, int struct) {
			return views(greedy, null, schema, name, types, struct);
		}
		default <T extends View> List<T> views(boolean greedy, Schema schema, String name, int types, boolean struct) {
			return views(greedy, null, schema, name, types, struct);
		}
		default <T extends View> List<T> views(boolean greedy, String name, int types, boolean struct) {
			return views(greedy, null, null, name, types, struct);
		}
		default <T extends View> List<T> views(boolean greedy, String name, int types, int struct) {
			return views(greedy, null, null, name, types, struct);
		}
		default <T extends View> List<T> views(boolean greedy, int types, int struct) {
			return views(greedy, null, types, struct);
		}
		default <T extends View> List<T> views(boolean greedy, int types, boolean struct) {
			return views(greedy, null, types, struct);
		}
		default <T extends View> List<T> views(boolean greedy, boolean struct) {
			return views(greedy, View.TYPE.NORMAL.value, struct);
		}

		<T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, int struct){
			return views(catalog, schema, name, types, struct, null);
		}
		default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return views(catalog, schema, name, types, structs, configs);
		}
		default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return views(catalog, schema, name, types, struct, null);
		}

		default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types, int struct) {
			return views(null, schema, name, types, struct);
		}
		default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types, boolean struct) {
			return views(null, schema, name, types, struct);
		}
		default <T extends View> LinkedHashMap<String, T> views(String name, int types, int struct) {
			return views(null, null, name, types, struct);
		}
		default <T extends View> LinkedHashMap<String, T> views(String name, int types, boolean struct) {
			return views(null, null, name, types, struct);
		}
		default <T extends View> LinkedHashMap<String, T> views(int types, int struct) {
			return views(null, types, struct);
		}
		default <T extends View> LinkedHashMap<String, T> views(int types, boolean struct) {
			return views(null, types, struct);
		}
		default <T extends View> LinkedHashMap<String, T> views() {
			return views( View.TYPE.NORMAL.value, false);
		}

		default <T extends View> LinkedHashMap<String, T> views(int types, int struct, ConfigStore configs) {
			return views(null, null, null, types, struct, configs);
		}
		default <T extends View> LinkedHashMap<String, T> views(int types, boolean struct, ConfigStore configs) {
			return views(null, null, null,  types, struct, configs);
		}
		default <T extends View> LinkedHashMap<String, T> views(ConfigStore configs) {
			return views(View.TYPE.NORMAL.value, false, configs);
		}


		default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
			return views(greedy, catalog, schema, name, types, false, configs);
		}
		default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
			return views(greedy, catalog, schema, name, types, false);
		}
		default <T extends View> List<T> views(boolean greedy, Schema schema, String name, int types) {
			return views(greedy, null, schema, name, types, false);
		}
		default <T extends View> List<T> views(boolean greedy, String name, int types) {
			return views(greedy, null, null, name, types, false);
		}
		default <T extends View> List<T> views(boolean greedy, int types) {
			return views(greedy, null, types, false);
		}
		default <T extends View> List<T> views(boolean greedy) {
			return views(greedy, View.TYPE.NORMAL.value, false);
		}
		default <T extends View> List<T> views(boolean greedy, int types, ConfigStore configs) {
			return views(greedy, null, null, null, types, configs);
		}
		default <T extends View> List<T> views(boolean greedy, ConfigStore configs) {
			return views(greedy, null, null, null, View.TYPE.NORMAL.value, configs);
		}

		default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types) {
			return views(catalog, schema, name, types, false);
		}

		default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types) {
			return views(null, schema, name, types, false);
		}
		default <T extends View> LinkedHashMap<String, T> views(String name, int types) {
			return views(null, null, name, types, false);
		}
		default <T extends View> LinkedHashMap<String, T> views(int types) {
			return views(null, types, false);
		}
		default <T extends View> LinkedHashMap<String, T> views(int types, ConfigStore configs) {
			return views(null,null,null, types, false, configs);
		}

		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的视图的话, 可以直接设置为null, 如果设置为特定的视图名称, 则返回该视图的具体信息。
		 * @param struct 是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
		 * @return View
		 */
		View view(boolean greedy, Catalog catalog, Schema schema, String name, int struct);
		default View view(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return view(greedy, catalog, schema, name, structs);
		}
		default View view(boolean greedy, Schema schema, String name, int struct) {
			return view(greedy, null, schema, name, struct);
		}
		default View view(boolean greedy, Schema schema, String name, boolean struct) {
			return view(greedy, null, schema, name, struct);
		}
		default View view(boolean greedy, String name, int struct) {
			return view(greedy, null, null, name, struct);
		}
		default View view(boolean greedy, String name, boolean struct) {
			return view(greedy, null, null, name, struct);
		}

		View view(Catalog catalog, Schema schema, String name, int struct);
		default View view(Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return view(catalog, schema, name, structs);
		}
		default View view(Schema schema, String name, int struct) {
			return view(false, null, schema, name, struct);
		}
		default View view(Schema schema, String name, boolean struct) {
			return view(false, null, schema, name, struct);
		}
		default View view(String name, int struct) {
			return view(false, null, null, name, struct);
		}
		default View view(String name, boolean struct) {
			return view(false, null, null, name, struct);
		}
		default View view(boolean greedy, Catalog catalog, Schema schema, String name) {
			return view(greedy, catalog, schema, name, true);
		}
		default View view(boolean greedy, Schema schema, String name) {
			return view(greedy, null, schema, name, true);
		}
		default View view(boolean greedy, String name) {
			return view(greedy, null, null, name, true);
		}

		default View view(Catalog catalog, Schema schema, String name) {
			return view( catalog, schema, name, true);
		}
		default View view(Schema schema, String name) {
			return view(null, schema, name, true);
		}
		default View view(String name) {
			return view(null, null, name, true);
		}

		/**
		 * 视图的创建SQL
		 * @param view view
		 * @param init 是否还原初始状态 默认false
		 * @return ddl
		 */
		List<String> ddl(View view, boolean init);
		default List<String> ddl(View view) {
			return ddl(view, false);
		}



		/* *****************************************************************************************************************
		 * 													master table
		 ******************************************************************************************************************/

		/**
		 * 主表
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param table 表
		 * @return LinkedHashMap
		 */
		boolean exists(boolean greedy, MasterTable table);
		default boolean exists(MasterTable table) {
			return exists(false, table);
		}

		/**
		 * masterTables
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param types Metadata.TYPE
		 * @return masterTables
		 */
		<T extends MasterTable> List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
			return masterTables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return masterTables(greedy, catalog, schema, name, types, structs, configs);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return masterTables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Schema schema, String name, int types, int struct) {
			return masterTables(greedy, null, schema, name, types, struct);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Schema schema, String name, int types, boolean struct) {
			return masterTables(greedy, null, schema, name, types, struct);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, String name, int types, boolean struct) {
			return masterTables(greedy, null, null, name, types, struct);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, String name, int types, int struct) {
			return masterTables(greedy, null, null, name, types, struct);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, int types, int struct) {
			return masterTables(greedy, null, types, struct);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, int types, boolean struct) {
			return masterTables(greedy, null, types, struct);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, boolean struct) {
			return masterTables(greedy, MasterTable.TYPE.NORMAL.value, struct);
		}

		<T extends MasterTable> LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types, int struct){
			return masterTables(catalog, schema, name, types, struct, null);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return masterTables(catalog, schema, name, types, structs, configs);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return masterTables(catalog, schema, name, types, struct, null);
		}

		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Schema schema, String name, int types, int struct) {
			return masterTables(null, schema, name, types, struct);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Schema schema, String name, int types, boolean struct) {
			return masterTables(null, schema, name, types, struct);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(String name, int types, int struct) {
			return masterTables(null, null, name, types, struct);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(String name, int types, boolean struct) {
			return masterTables(null, null, name, types, struct);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types, int struct) {
			return masterTables(null, types, struct);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types, boolean struct) {
			return masterTables(null, types, struct);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables() {
			return masterTables( MasterTable.TYPE.NORMAL.value, false);
		}

		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types, int struct, ConfigStore configs) {
			return masterTables(null, null, null, types, struct, configs);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types, boolean struct, ConfigStore configs) {
			return masterTables(null, null, null,  types, struct, configs);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(ConfigStore configs) {
			return masterTables(MasterTable.TYPE.NORMAL.value, false, configs);
		}


		default <T extends MasterTable> List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
			return masterTables(greedy, catalog, schema, name, types, false, configs);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
			return masterTables(greedy, catalog, schema, name, types, false);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, Schema schema, String name, int types) {
			return masterTables(greedy, null, schema, name, types, false);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, String name, int types) {
			return masterTables(greedy, null, null, name, types, false);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, int types) {
			return masterTables(greedy, null, types, false);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy) {
			return masterTables(greedy, MasterTable.TYPE.NORMAL.value, false);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, int types, ConfigStore configs) {
			return masterTables(greedy, null, null, null, types, configs);
		}
		default <T extends MasterTable> List<T> masterTables(boolean greedy, ConfigStore configs) {
			return masterTables(greedy, null, null, null, MasterTable.TYPE.NORMAL.value, configs);
		}

		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types) {
			return masterTables(catalog, schema, name, types, false);
		}

		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Schema schema, String name, int types) {
			return masterTables(null, schema, name, types, false);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(String name, int types) {
			return masterTables(null, null, name, types, false);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types) {
			return masterTables(null, types, false);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types, ConfigStore configs) {
			return masterTables(null,null,null, types, false, configs);
		}

		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param struct 是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
		 * @return MasterTable
		 */
		MasterTable masterTable(boolean greedy, Catalog catalog, Schema schema, String name, int struct);
		default MasterTable masterTable(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return masterTable(greedy, catalog, schema, name, structs);
		}
		default MasterTable masterTable(boolean greedy, Schema schema, String name, int struct) {
			return masterTable(greedy, null, schema, name, struct);
		}
		default MasterTable masterTable(boolean greedy, Schema schema, String name, boolean struct) {
			return masterTable(greedy, null, schema, name, struct);
		}
		default MasterTable masterTable(boolean greedy, String name, int struct) {
			return masterTable(greedy, null, null, name, struct);
		}
		default MasterTable masterTable(boolean greedy, String name, boolean struct) {
			return masterTable(greedy, null, null, name, struct);
		}

		MasterTable masterTable(Catalog catalog, Schema schema, String name, int struct);
		default MasterTable masterTable(Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return masterTable(catalog, schema, name, structs);
		}
		default MasterTable masterTable(Schema schema, String name, int struct) {
			return masterTable(false, null, schema, name, struct);
		}
		default MasterTable masterTable(Schema schema, String name, boolean struct) {
			return masterTable(false, null, schema, name, struct);
		}
		default MasterTable masterTable(String name, int struct) {
			return masterTable(false, null, null, name, struct);
		}
		default MasterTable masterTable(String name, boolean struct) {
			return masterTable(false, null, null, name, struct);
		}
		default MasterTable masterTable(boolean greedy, Catalog catalog, Schema schema, String name) {
			return masterTable(greedy, catalog, schema, name, true);
		}
		default MasterTable masterTable(boolean greedy, Schema schema, String name) {
			return masterTable(greedy, null, schema, name, true);
		}
		default MasterTable masterTable(boolean greedy, String name) {
			return masterTable(greedy, null, null, name, true);
		}

		default MasterTable masterTable(Catalog catalog, Schema schema, String name) {
			return masterTable( catalog, schema, name, true);
		}
		default MasterTable masterTable(Schema schema, String name) {
			return masterTable(null, schema, name, true);
		}
		default MasterTable masterTable(String name) {
			return masterTable(null, null, name, true);
		}

		/**
		 * 表的创建SQL
		 * @param masterTable masterTable
		 * @param init 是否还原初始状态 默认false
		 * @return ddl
		 */
		List<String> ddl(MasterTable masterTable, boolean init);
		default List<String> ddl(MasterTable masterTable) {
			return ddl(masterTable, false);
		}
		/* *************************************************************************************************************
		 * 													partition table
		 **************************************************************************************************************/

		/**
		 * 子表
		 * @param table 表
		 * @return LinkedHashMap
		 */
		boolean exists(boolean greedy, PartitionTable table);
		default boolean exists(PartitionTable table) {
			return exists(false, table);
		}
		/**
		 * 根据主表与标签值查询分区表(子表)
		 * @param master 主表
		 * @param tags 标签值
		 * @param name 子表名
		 * @return PartitionTables
		 */
		<T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, Map<String,Object> tags, String name);
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master, Map<String,Object> tags, String name) {
			return partitionTables(false, master, tags, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, Map<String,Object> tags) {
			return partitionTables(greedy, master, tags, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master, Map<String,Object> tags) {
			return partitionTables(false, master, tags, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, Catalog catalog, Schema schema, String master, String name) {
			return partitionTables(greedy, new MasterTable(catalog, schema, master), null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, Schema schema, String master, String name) {
			return partitionTables(greedy, new MasterTable(schema, master), null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, String master, String name) {
			return partitionTables(greedy, new MasterTable(master), null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, String master) {
			return partitionTables(greedy, new MasterTable(master), null, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master) {
			return partitionTables(greedy, master, null, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, String name) {
			return partitionTables(greedy, master, null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(Catalog catalog, Schema schema, String master, String name) {
			return partitionTables(false, catalog, schema, master, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(Schema schema, String master, String name) {
			return partitionTables(false, schema, master, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(String master, String name) {
			return partitionTables(false, master, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(String master) {
			return partitionTables(false, master);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master) {
			return partitionTables(false, master);
		}

		PartitionTable partitionTable(boolean greedy, MasterTable master, String name);
		default PartitionTable partitionTable(boolean greedy, Catalog catalog, Schema schema, String master, String name) {
			return partitionTable(greedy, new MasterTable(catalog, schema, master), name);
		}
		default PartitionTable partitionTable(boolean greedy, Schema schema, String master, String name) {
			return partitionTable(greedy, new MasterTable(schema, master), name);
		}
		default PartitionTable partitionTable(boolean greedy, String master, String name) {
			return partitionTable(greedy, new MasterTable(master), name);
		}
		default PartitionTable partitionTable(Catalog catalog, Schema schema, String master, String name) {
			return partitionTable(false, catalog, schema, master, name);
		}
		default PartitionTable partitionTable(Schema schema, String master, String name) {
			return partitionTable(false, new MasterTable(schema, master), name);
		}
		default PartitionTable partitionTable(String master, String name) {
			return partitionTable(false, new MasterTable(master), name);
		}

		List<String> ddl(PartitionTable table);
		/* *************************************************************************************************************
		 * 													VertexTable
		 **************************************************************************************************************/

		/**
		 * 表是否存在
		 * @param vertexTable 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @return boolean
		 */
		boolean exists(boolean greedy, VertexTable vertexTable);
		default boolean exists(VertexTable vertexTable) {
			return exists(false, vertexTable);
		}
		/**
		 * vertexTables
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param types Metadata.TYPE
		 * @return vertexTables
		 */
		<T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
			return vertexTables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return vertexTables(greedy, catalog, schema, name, types, structs, configs);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return vertexTables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Schema schema, String name, int types, int struct) {
			return vertexTables(greedy, null, schema, name, types, struct);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Schema schema, String name, int types, boolean struct) {
			return vertexTables(greedy, null, schema, name, types, struct);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, String name, int types, boolean struct) {
			return vertexTables(greedy, null, null, name, types, struct);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, String name, int types, int struct) {
			return vertexTables(greedy, null, null, name, types, struct);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types, int struct) {
			return vertexTables(greedy, null, types, struct);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types, boolean struct) {
			return vertexTables(greedy, null, types, struct);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, boolean struct) {
			return vertexTables(greedy, VertexTable.TYPE.NORMAL.value, struct);
		}

		<T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, int struct){
			return vertexTables(catalog, schema, name, types, struct, null);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return vertexTables(catalog, schema, name, types, structs, configs);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return vertexTables(catalog, schema, name, types, struct, null);
		}

		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Schema schema, String name, int types, int struct) {
			return vertexTables(null, schema, name, types, struct);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Schema schema, String name, int types, boolean struct) {
			return vertexTables(null, schema, name, types, struct);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(String name, int types, int struct) {
			return vertexTables(null, null, name, types, struct);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(String name, int types, boolean struct) {
			return vertexTables(null, null, name, types, struct);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, int struct) {
			return vertexTables(null, types, struct);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, boolean struct) {
			return vertexTables(null, types, struct);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables() {
			return vertexTables( VertexTable.TYPE.NORMAL.value, false);
		}

		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, int struct, ConfigStore configs) {
			return vertexTables(null, null, null, types, struct, configs);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, boolean struct, ConfigStore configs) {
			return vertexTables(null, null, null,  types, struct, configs);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(ConfigStore configs) {
			return vertexTables(VertexTable.TYPE.NORMAL.value, false, configs);
		}


		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
			return vertexTables(greedy, catalog, schema, name, types, false, configs);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
			return vertexTables(greedy, catalog, schema, name, types, false);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, Schema schema, String name, int types) {
			return vertexTables(greedy, null, schema, name, types, false);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, String name, int types) {
			return vertexTables(greedy, null, null, name, types, false);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types) {
			return vertexTables(greedy, null, types, false);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy) {
			return vertexTables(greedy, VertexTable.TYPE.NORMAL.value, false);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types, ConfigStore configs) {
			return vertexTables(greedy, null, null, null, types, configs);
		}
		default <T extends VertexTable> List<T> vertexTables(boolean greedy, ConfigStore configs) {
			return vertexTables(greedy, null, null, null, VertexTable.TYPE.NORMAL.value, configs);
		}

		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types) {
			return vertexTables(catalog, schema, name, types, false);
		}

		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Schema schema, String name, int types) {
			return vertexTables(null, schema, name, types, false);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(String name, int types) {
			return vertexTables(null, null, name, types, false);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types) {
			return vertexTables(null, types, false);
		}
		default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, ConfigStore configs) {
			return vertexTables(null,null,null, types, false, configs);
		}

		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param struct 是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
		 * @return VertexTable
		 */
		VertexTable vertexTable(boolean greedy, Catalog catalog, Schema schema, String name, int struct);
		default VertexTable vertexTable(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return vertexTable(greedy, catalog, schema, name, structs);
		}
		default VertexTable vertexTable(boolean greedy, Schema schema, String name, int struct) {
			return vertexTable(greedy, null, schema, name, struct);
		}
		default VertexTable vertexTable(boolean greedy, Schema schema, String name, boolean struct) {
			return vertexTable(greedy, null, schema, name, struct);
		}
		default VertexTable vertexTable(boolean greedy, String name, int struct) {
			return vertexTable(greedy, null, null, name, struct);
		}
		default VertexTable vertexTable(boolean greedy, String name, boolean struct) {
			return vertexTable(greedy, null, null, name, struct);
		}

		VertexTable vertexTable(Catalog catalog, Schema schema, String name, int struct);
		default VertexTable vertexTable(Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return vertexTable(catalog, schema, name, structs);
		}
		default VertexTable vertexTable(Schema schema, String name, int struct) {
			return vertexTable(false, null, schema, name, struct);
		}
		default VertexTable vertexTable(Schema schema, String name, boolean struct) {
			return vertexTable(false, null, schema, name, struct);
		}
		default VertexTable vertexTable(String name, int struct) {
			return vertexTable(false, null, null, name, struct);
		}
		default VertexTable vertexTable(String name, boolean struct) {
			return vertexTable(false, null, null, name, struct);
		}
		default VertexTable vertexTable(boolean greedy, Catalog catalog, Schema schema, String name) {
			return vertexTable(greedy, catalog, schema, name, true);
		}
		default VertexTable vertexTable(boolean greedy, Schema schema, String name) {
			return vertexTable(greedy, null, schema, name, true);
		}
		default VertexTable vertexTable(boolean greedy, String name) {
			return vertexTable(greedy, null, null, name, true);
		}

		default VertexTable vertexTable(Catalog catalog, Schema schema, String name) {
			return vertexTable( catalog, schema, name, true);
		}
		default VertexTable vertexTable(Schema schema, String name) {
			return vertexTable(null, schema, name, true);
		}
		default VertexTable vertexTable(String name) {
			return vertexTable(null, null, name, true);
		}

		/**
		 * 表的创建SQL
		 * @param vertexTable vertexTable
		 * @param init 是否还原初始状态 默认false
		 * @return ddl
		 */
		List<String> ddl(VertexTable vertexTable, boolean init);
		default List<String> ddl(VertexTable vertexTable) {
			return ddl(vertexTable, false);
		}


		/* *************************************************************************************************************
		 * 													EdgeTable
		 **************************************************************************************************************/

		/**
		 * 表是否存在
		 * @param edgeTable 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @return boolean
		 */
		boolean exists(boolean greedy, EdgeTable edgeTable);
		default boolean exists(EdgeTable edgeTable) {
			return exists(false, edgeTable);
		}
		/**
		 * edgeTables
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param types Metadata.TYPE
		 * @return edgeTables
		 */
		<T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
			return edgeTables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return edgeTables(greedy, catalog, schema, name, types, structs, configs);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return edgeTables(greedy, catalog, schema, name, types, struct, null);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Schema schema, String name, int types, int struct) {
			return edgeTables(greedy, null, schema, name, types, struct);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Schema schema, String name, int types, boolean struct) {
			return edgeTables(greedy, null, schema, name, types, struct);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, String name, int types, boolean struct) {
			return edgeTables(greedy, null, null, name, types, struct);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, String name, int types, int struct) {
			return edgeTables(greedy, null, null, name, types, struct);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types, int struct) {
			return edgeTables(greedy, null, types, struct);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types, boolean struct) {
			return edgeTables(greedy, null, types, struct);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, boolean struct) {
			return edgeTables(greedy, EdgeTable.TYPE.NORMAL.value, struct);
		}

		<T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, int struct){
			return edgeTables(catalog, schema, name, types, struct, null);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return edgeTables(catalog, schema, name, types, structs, configs);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, boolean struct) {
			return edgeTables(catalog, schema, name, types, struct, null);
		}

		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Schema schema, String name, int types, int struct) {
			return edgeTables(null, schema, name, types, struct);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Schema schema, String name, int types, boolean struct) {
			return edgeTables(null, schema, name, types, struct);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(String name, int types, int struct) {
			return edgeTables(null, null, name, types, struct);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(String name, int types, boolean struct) {
			return edgeTables(null, null, name, types, struct);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, int struct) {
			return edgeTables(null, types, struct);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, boolean struct) {
			return edgeTables(null, types, struct);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables() {
			return edgeTables( EdgeTable.TYPE.NORMAL.value, false);
		}

		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, int struct, ConfigStore configs) {
			return edgeTables(null, null, null, types, struct, configs);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, boolean struct, ConfigStore configs) {
			return edgeTables(null, null, null,  types, struct, configs);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(ConfigStore configs) {
			return edgeTables(EdgeTable.TYPE.NORMAL.value, false, configs);
		}


		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
			return edgeTables(greedy, catalog, schema, name, types, false, configs);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
			return edgeTables(greedy, catalog, schema, name, types, false);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Schema schema, String name, int types) {
			return edgeTables(greedy, null, schema, name, types, false);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, String name, int types) {
			return edgeTables(greedy, null, null, name, types, false);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types) {
			return edgeTables(greedy, null, types, false);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy) {
			return edgeTables(greedy, EdgeTable.TYPE.NORMAL.value, false);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types, ConfigStore configs) {
			return edgeTables(greedy, null, null, null, types, configs);
		}
		default <T extends EdgeTable> List<T> edgeTables(boolean greedy, ConfigStore configs) {
			return edgeTables(greedy, null, null, null, EdgeTable.TYPE.NORMAL.value, configs);
		}

		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types) {
			return edgeTables(catalog, schema, name, types, false);
		}

		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Schema schema, String name, int types) {
			return edgeTables(null, schema, name, types, false);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(String name, int types) {
			return edgeTables(null, null, name, types, false);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types) {
			return edgeTables(null, types, false);
		}
		default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, ConfigStore configs) {
			return edgeTables(null,null,null, types, false, configs);
		}

		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
		 * @param struct 是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
		 * @return EdgeTable
		 */
		EdgeTable edgeTable(boolean greedy, Catalog catalog, Schema schema, String name, int struct);
		default EdgeTable edgeTable(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return edgeTable(greedy, catalog, schema, name, structs);
		}
		default EdgeTable edgeTable(boolean greedy, Schema schema, String name, int struct) {
			return edgeTable(greedy, null, schema, name, struct);
		}
		default EdgeTable edgeTable(boolean greedy, Schema schema, String name, boolean struct) {
			return edgeTable(greedy, null, schema, name, struct);
		}
		default EdgeTable edgeTable(boolean greedy, String name, int struct) {
			return edgeTable(greedy, null, null, name, struct);
		}
		default EdgeTable edgeTable(boolean greedy, String name, boolean struct) {
			return edgeTable(greedy, null, null, name, struct);
		}

		EdgeTable edgeTable(Catalog catalog, Schema schema, String name, int struct);
		default EdgeTable edgeTable(Catalog catalog, Schema schema, String name, boolean struct) {
			int structs = 0;
			if(struct) {
				structs = 32767;
			}
			return edgeTable(catalog, schema, name, structs);
		}
		default EdgeTable edgeTable(Schema schema, String name, int struct) {
			return edgeTable(false, null, schema, name, struct);
		}
		default EdgeTable edgeTable(Schema schema, String name, boolean struct) {
			return edgeTable(false, null, schema, name, struct);
		}
		default EdgeTable edgeTable(String name, int struct) {
			return edgeTable(false, null, null, name, struct);
		}
		default EdgeTable edgeTable(String name, boolean struct) {
			return edgeTable(false, null, null, name, struct);
		}
		default EdgeTable edgeTable(boolean greedy, Catalog catalog, Schema schema, String name) {
			return edgeTable(greedy, catalog, schema, name, true);
		}
		default EdgeTable edgeTable(boolean greedy, Schema schema, String name) {
			return edgeTable(greedy, null, schema, name, true);
		}
		default EdgeTable edgeTable(boolean greedy, String name) {
			return edgeTable(greedy, null, null, name, true);
		}

		default EdgeTable edgeTable(Catalog catalog, Schema schema, String name) {
			return edgeTable( catalog, schema, name, true);
		}
		default EdgeTable edgeTable(Schema schema, String name) {
			return edgeTable(null, schema, name, true);
		}
		default EdgeTable edgeTable(String name) {
			return edgeTable(null, null, name, true);
		}

		/**
		 * 表的创建SQL
		 * @param edgeTable edgeTable
		 * @param init 是否还原初始状态 默认false
		 * @return ddl
		 */
		List<String> ddl(EdgeTable edgeTable, boolean init);
		default List<String> ddl(EdgeTable edgeTable) {
			return ddl(edgeTable, false);
		}


		/* *****************************************************************************************************************
		 * 													column
		 ******************************************************************************************************************/
		/**
		 * 列是否存在
		 * @param column 列
		 * @return boolean
		 */
		boolean exists(boolean greedy, Table table, Column column);
		default boolean exists(boolean greedy, Column column) {
			return exists(greedy, null, column);
		}
		default boolean exists(boolean greedy, String table, String column) {
			return exists(greedy, new Table(table), new Column(column));
		}
		default boolean exists(boolean greedy, Catalog catalog, Schema schema, String table, String column) {
			return exists(greedy, new Table(catalog, schema, table), new Column(column));
		}
		default boolean exists(Column column) {
			return exists(false, null, column); 
		}
		default boolean exists(String table, String column) {
			return exists(false, new Table(table), new Column(column));
		}
		default boolean exists(Table table, String column) {
			return exists(false, table, new Column(column));
		}
		default boolean exists(Catalog catalog, Schema schema, String table, String column) {
			return exists(false, new Table(catalog, schema, table), new Column(column));
		}
		/**
		 * 查询表中所有的表,注意这里的map.KEY全部转大写
		 * @param table 表
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @return map
		 */
		<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table, ConfigStore configs);
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table){
			return columns(greedy, table, null);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table, ConfigStore configs) {
			return columns(greedy, new Table(table), configs);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table) {
			return columns(greedy, new Table(table), null);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table, ConfigStore configs) {
			return columns(greedy, new Table(catalog, schema, table), configs);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table) {
			return columns(greedy, new Table(catalog, schema, table), null);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(Table table, ConfigStore configs) {
			return columns(false, table, configs);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(Table table) {
			return columns(false, table, null);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(String table, ConfigStore configs) {
			return columns(false, new Table(table), configs);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(String table) {
			return columns(false, new Table(table));
		}
		default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table, ConfigStore configs) {
			return columns(false, new Table(catalog, schema, table), configs);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table) {
			return columns(false, new Table(catalog, schema, table), null);
		}

		/**
		 * 查询列
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog catalog
		 * @param schema schema
		 * @return List
		 */
		<T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema, ConfigStore configs);
		default <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema){
			return columns(greedy, catalog, schema, (ConfigStore) null);
		}
		default <T extends Column> List<T> columns(Catalog catalog, Schema schema, ConfigStore configs) {
			return columns(false, catalog, schema, configs);
		}
		default <T extends Column> List<T> columns(Catalog catalog, Schema schema) {
			return columns(false, catalog, schema, (ConfigStore) null);
		}
		default <T extends Column> List<T> columns(boolean greedy, ConfigStore configs) {
			return columns(greedy, (Catalog) null, (Schema) null, configs);
		}
		default <T extends Column> List<T> columns(boolean greedy) {
			return columns(greedy, (Catalog) null, (Schema) null, (ConfigStore) null);
		}
		default <T extends Column> List<T> columns(ConfigStore configs) {
			return columns(false, (Catalog) null, (Schema)null, configs);
		}
		default <T extends Column> List<T> columns() {
			return columns(false, (Catalog) null, (Schema)null, (ConfigStore) null);
		}

		/**
		 * 查询table中的column列
		 * @param table 表
		 * @param name 列名(不区分大小写)
		 * @return Column
		 */
		Column column(boolean greedy, Table table, String name);
		default Column column(boolean greedy, String table, String name) {
			return column(greedy, new Table(table), name);
		}
		default Column column(boolean greedy, Catalog catalog, Schema schema, String table, String name) {
			return column(greedy, new Table(catalog, schema, table), name);
		}
		default Column column(Table table, String name) {
			return column(false, table, name);
		}
		default Column column(String table, String name) {
			return column(false, new Table(table), name);
		}
		default Column column(Catalog catalog, Schema schema, String table, String name) {
			return column(false, new Table(catalog, schema, table), name);
		}

		/* *****************************************************************************************************************
		 * 													tag
		 ******************************************************************************************************************/
		<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table);
		default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table) {
			return tags(greedy, new Table(table));
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Catalog catalog, Schema schema, String table) {
			return tags(greedy, new Table(catalog, schema, table));
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(Table table) {
			return tags(false, table);
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(String table) {
			return tags(false, null, null, table);
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(Catalog catalog, Schema schema, String table) {
			return tags(false, new Table(catalog, schema, table));
		}

		/* *****************************************************************************************************************
		 * 													primary
		 ******************************************************************************************************************/

		PrimaryKey primary(boolean greedy, Table table);
		default PrimaryKey primary(boolean greedy, String table) {
			return primary(greedy, new Table(table));
		}
		default PrimaryKey primary(boolean greedy, Catalog catalog, Schema schema, String table) {
			return primary(greedy, new Table(catalog, schema, table));
		}
		default PrimaryKey primary(Table table) {
			return primary(false, table);
		}
		default PrimaryKey primary(String table) {
			return primary(false, new Table(table));
		}
		default PrimaryKey primary(Catalog catalog, Schema schema, String table) {
			return primary(false, new Table(catalog, schema, table));
		}

		/* *****************************************************************************************************************
		 * 													foreign
		 ******************************************************************************************************************/

		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table);
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, String table) {
			return foreigns(greedy, new Table(table));
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Catalog catalog, Schema schema, String table) {
			return foreigns(greedy, new Table(catalog, schema, table));
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(Table table) {
			return foreigns(false, table);
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(String table) {
			return foreigns(false, new Table(table));
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(Catalog catalog, Schema schema, String table) {
			return foreigns(false, new Table(catalog, schema, table));
		}
		ForeignKey foreign(boolean greedy, Table table, List<String> columns);
		default ForeignKey foreign(boolean greedy, Table table, String ... columns) {
			return foreign(greedy, table, BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(boolean greedy, String table, String ... columns) {
			return foreign(greedy, new Table(table), BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(boolean greedy, String table, List<String> columns) {
			return foreign(greedy, new Table(table), columns);
		}
		//与上面的foreign(boolean greedy, String table, String ... columns)冲突
		//ForeignKey foreign(boolean greedy, Catalog catalog, Schema schema, String table, String ... columns);
		default ForeignKey foreign(boolean greedy, Catalog catalog, Schema schema, String table, List<String> columns) {
			return foreign(greedy, new Table(catalog, schema, table), columns);
		}
		default ForeignKey foreign(Table table, String ... columns) {
			return foreign(false, table, BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(Table table, List<String> columns) {
			return foreign(false, table, columns);
		}
		default ForeignKey foreign(String table, String ... columns) {
			return foreign(false, new Table(table), BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(String table, List<String> columns) {
			return foreign(false, new Table(table), columns);
		}
		//与上面的foreign(String table, String ... columns)冲突
		//ForeignKey foreign(Catalog catalog, Schema schema, String table, String ... columns);
		default ForeignKey foreign(Catalog catalog, Schema schema, String table, List<String> columns) {
			return foreign(false, new Table(catalog, schema, table), columns);
		}
		/* *****************************************************************************************************************
		 * 													index
		 ******************************************************************************************************************/

		<T extends Index> List<T> indexes(boolean greedy, Table table);
		default <T extends Index> List<T> indexes(boolean greedy, String table) {
			return indexes(greedy, new Table(table));
		}
		default <T extends Index> List<T> indexes(boolean greedy) {
			return indexes(greedy, (Table)null);
		}
		default <T extends Index> List<T> indexes(boolean greedy, Catalog catalog, Schema schema, String table) {
			return indexes(greedy, new Table(catalog, schema, table));
		}

		<T extends Index> LinkedHashMap<String, T> indexes(Table table);
		default <T extends Index> LinkedHashMap<String, T> indexes(String table) {
			return indexes(new Table(table));
		}
		default <T extends Index> LinkedHashMap<String, T> indexes() {
			return indexes((Table)null);
		}
		default <T extends Index> LinkedHashMap<String, T> indexes(Catalog catalog, Schema schema, String table) {
			return indexes(new Table(catalog, schema, table));
		}

		Index index(boolean greedy, Table table, String name);
		default Index index(boolean greedy, String table, String name) {
			return index(greedy, new Table(table), name);
		}
		default Index index(boolean greedy, String name) {
			return index(greedy, (Table) null, name);
		}
		default Index index(Table table, String name) {
			return index(false, table, name);
		}
		default Index index(String table, String name) {
			return index(false, new Table(table), name);
		}
		default Index index(String name) {
			return index(false, name);
		}

		/* *****************************************************************************************************************
		 * 													constraint
		 ******************************************************************************************************************/

		<T extends Constraint> List<T> constraints(boolean greedy, Table table, String name);
		default <T extends Constraint> List<T> constraints(boolean greedy, Table table) {
			return constraints(greedy, table, null);
		}
		default <T extends Constraint> List<T> constraints(boolean greedy, String table) {
			return constraints(greedy, new Table(table));
		}
		default <T extends Constraint> List<T> constraints(boolean greedy, Catalog catalog, Schema schema, String table) {
			return constraints(greedy, new Table(catalog, schema, table));
		}

		<T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name);
		default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table) {
			return constraints(table, null);
		}
		default <T extends Constraint> LinkedHashMap<String, T> constraints(String table) {
			return constraints(new Table(table));
		}
		default <T extends Constraint> LinkedHashMap<String, T> constraints(Catalog catalog, Schema schema, String table) {
			return constraints( new Table(catalog, schema, table));
		}

		<T extends Constraint> LinkedHashMap<String, T> constraints(Column column, String name);
		default <T extends Constraint> LinkedHashMap<String, T> constraints(Column column) {
			return constraints(column, null);
		}

		Constraint constraint(boolean greedy, Table table, String name);
		default Constraint constraint(boolean greedy, String table, String name) {
			return constraint(greedy, new Table(table), name);
		}
		default Constraint constraint(boolean greedy, String name) {
			return constraint(greedy, (Table)null, name);
		}
		default Constraint constraint(Table table, String name) {
			return constraint(false, table, name);
		}
		default Constraint constraint(String table, String name) {
			return constraint(false, table, name);
		}
		default Constraint constraint(String name) {
			return constraint(false, name);
		}

		/* *****************************************************************************************************************
		 * 													trigger
		 ******************************************************************************************************************/
		<T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events);
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Catalog catalog, Schema schema, String table, List<Trigger.EVENT> events) {
			return triggers(greedy, new Table(catalog, schema, table), events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Schema schema, String table, List<Trigger.EVENT> events) {
			return triggers(greedy, new Table(schema, table), events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String table, List<Trigger.EVENT> events) {
			return triggers(greedy, new Table(table), events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, List<Trigger.EVENT> events) {
			return triggers(greedy, (Table) null, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy) {
			return triggers(greedy,(Table) null, null);
		}
 		default <T extends Trigger> LinkedHashMap<String, T> triggers(Catalog catalog, Schema schema, String table, List<Trigger.EVENT> events) {
			return triggers(false, new Table(catalog, schema, table), events);
		}

		default <T extends Trigger> LinkedHashMap<String, T> triggers(Schema schema, String table, List<Trigger.EVENT> events) {
			return triggers(false, schema, table, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(String table, List<Trigger.EVENT> events) {
			return triggers(false, table, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(List<Trigger.EVENT> events) {
			return triggers(false, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers() {
			return triggers(false);
		}

		Trigger trigger(boolean greedy, Catalog catalog, Schema schema, String name);
		default Trigger trigger(boolean greedy, Schema schema, String name) {
			return trigger(greedy, null, schema, name);
		}
		default Trigger trigger(boolean greedy, String name) {
			return trigger(greedy, null, null, name);
		}

		default Trigger trigger(Catalog catalog, Schema schema, String name) {
			return trigger(false, catalog, schema, name);
		}
		default Trigger trigger(Schema schema, String name) {
			return trigger(false, null, schema, name);
		}
		default Trigger trigger(String name) {
			return trigger(false, name);
		}

		/* *****************************************************************************************************************
		 * 													procedure
		 ******************************************************************************************************************/

		<T extends Procedure> List<T> procedures(boolean greedy, Catalog catalog, Schema schema, String name);
		default <T extends Procedure> List<T> procedures(boolean greedy, Schema schema, String name) {
			return procedures(greedy, null, schema, name);
		}
		default <T extends Procedure> List<T> procedures(boolean greedy, String name) {
			return procedures(greedy, null, null, name);
		}
		default <T extends Procedure> List<T> procedures(boolean greedy) {
			return procedures(greedy, null, null, null);
		}

		<T extends Procedure> LinkedHashMap<String, T> procedures(Catalog catalog, Schema schema, String name);
		default <T extends Procedure> LinkedHashMap<String, T> procedures(Schema schema, String name) {
			return procedures(null, schema, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures(String name) {
			return procedures(null, null, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures() {
			return procedures(null, null, null);
		}

		Procedure procedure(boolean greedy, Catalog catalog, Schema schema, String name);
		default Procedure procedure(boolean greedy, Schema schema, String name) {
			return procedure(greedy, null, schema, name);
		}
		default Procedure procedure(boolean greedy, String name) {
			return procedure(greedy, null, null, name);
		}
		default Procedure procedure(Catalog catalog, Schema schema, String name) {
			return procedure(false, catalog, schema, name);
		}
		default Procedure procedure(Schema schema, String name) {
			return procedure(false, schema, name);
		}
		default Procedure procedure(String name) {
			return procedure(false, name);
		}
		List<String> ddl(Procedure procedure);

		/* *****************************************************************************************************************
		 * 													function
		 ******************************************************************************************************************/

		<T extends Function> List<T> functions(boolean greedy, Catalog catalog, Schema schema, String name);
		default <T extends Function> List<T> functions(boolean greedy, Schema schema, String name) {
			return functions(greedy, null, schema, name);
		}
		default <T extends Function> List<T> functions(boolean greedy, String name) {
			return functions(greedy, null, null, name);
		}
		default <T extends Function> List<T> functions(boolean greedy) {
			return functions(greedy, null, null, null);
		}
		<T extends Function> LinkedHashMap<String, T> functions(Catalog catalog, Schema schema, String name);
		default <T extends Function> LinkedHashMap<String, T> functions(Schema schema, String name) {
			return functions(null, schema, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(String name) {
			return functions(null, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions() {
			return functions(null);
		}

		Function function(boolean greedy, Catalog catalog, Schema schema, String name);
		default Function function(boolean greedy, Schema schema, String name) {
			return function(greedy, null, schema, name);
		}
		default Function function(boolean greedy, String name) {
			return function(greedy, null, null, name);
		}
		default Function function(Catalog catalog, Schema schema, String name) {
			return function(false, catalog, schema, name);
		}
		default Function function(Schema schema, String name) {
			return function(false, schema, name);
		}
		default Function function(String name) {
			return function(false, name);
		}
		List<String> ddl(Function function);

		/* *****************************************************************************************************************
		 * 													sequence
		 ******************************************************************************************************************/

		<T extends Sequence> List<T> sequences(boolean greedy, Catalog catalog, Schema schema, String name);
		default <T extends Sequence> List<T> sequences(boolean greedy, Schema schema, String name) {
			return sequences(greedy, null, schema, name);
		}
		default <T extends Sequence> List<T> sequences(boolean greedy, String name) {
			return sequences(greedy, null, null, name);
		}
		default <T extends Sequence> List<T> sequences(boolean greedy) {
			return sequences(greedy, null, null, null);
		}
		<T extends Sequence> LinkedHashMap<String, T> sequences(Catalog catalog, Schema schema, String name);
		default <T extends Sequence> LinkedHashMap<String, T> sequences(Schema schema, String name) {
			return sequences(null, schema, name);
		}
		default <T extends Sequence> LinkedHashMap<String, T> sequences(String name) {
			return sequences(null, name);
		}
		default <T extends Sequence> LinkedHashMap<String, T> sequences() {
			return sequences(null);
		}

		Sequence sequence(boolean greedy, Catalog catalog, Schema schema, String name);
		default Sequence sequence(boolean greedy, Schema schema, String name) {
			return sequence(greedy, null, schema, name);
		}
		default Sequence sequence(boolean greedy, String name) {
			return sequence(greedy, null, null, name);
		}
		default Sequence sequence(Catalog catalog, Schema schema, String name) {
			return sequence(false, catalog, schema, name);
		}
		default Sequence sequence(Schema schema, String name) {
			return sequence(false, schema, name);
		}
		default Sequence sequence(String name) {
			return sequence(false, name);
		}
		List<String> ddl(Sequence sequence);
	}

	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/

	interface DDLService{

		/* *****************************************************************************************************************
		 * 													table
		 ******************************************************************************************************************/

		boolean save(Table table) throws Exception;
		boolean create(Table table) throws Exception;
		boolean alter(Table table) throws Exception;
		boolean drop(Table table) throws Exception;
		boolean rename(Table origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													view
		 ******************************************************************************************************************/

		boolean save(View view) throws Exception;
		boolean create(View view) throws Exception;
		boolean alter(View view) throws Exception;
		boolean drop(View view) throws Exception;
		boolean rename(View origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													master table
		 ******************************************************************************************************************/

		boolean save(MasterTable table) throws Exception;
		boolean create(MasterTable table) throws Exception;
		boolean alter(MasterTable table) throws Exception;
		boolean drop(MasterTable table) throws Exception;
		boolean rename(MasterTable origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													partition table
		 ******************************************************************************************************************/

		boolean save(PartitionTable table) throws Exception;
		boolean create(PartitionTable table) throws Exception;
		boolean alter(PartitionTable table) throws Exception;
		boolean drop(PartitionTable table) throws Exception;
		boolean rename(PartitionTable origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													column
		 ******************************************************************************************************************/
		/**
		 * 修改列  名称 数据类型 位置 默认值
		 * 执行save前先调用column.update()设置修改后的属性
		 * column.update().setName().setDefaultValue().setAfter()....
		 * @param column 列
		 * @throws Exception 异常 SQL异常
		 */
		boolean save(Column column) throws Exception;
		boolean add(Column column) throws Exception;
		boolean alter(Column column) throws Exception;
		boolean drop(Column column) throws Exception;
		boolean rename(Column origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													tag
		 ******************************************************************************************************************/

		boolean save(Tag tag) throws Exception;
		boolean add(Tag tag) throws Exception;
		boolean alter(Tag tag) throws Exception;
		boolean drop(Tag tag) throws Exception;
		boolean rename(Tag origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													primary
		 ******************************************************************************************************************/

		boolean add(PrimaryKey primary) throws Exception;
		boolean alter(PrimaryKey primary) throws Exception;
		boolean drop(PrimaryKey primary) throws Exception;
		boolean rename(PrimaryKey origin, String name) throws Exception;
		/* *****************************************************************************************************************
		 * 													foreign
		 ******************************************************************************************************************/

		boolean add(ForeignKey foreign) throws Exception;
		boolean alter(ForeignKey foreign) throws Exception;
		boolean drop(ForeignKey foreign) throws Exception;
		boolean rename(ForeignKey origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													index
		 ******************************************************************************************************************/

		boolean add(Index index) throws Exception;
		boolean alter(Index index) throws Exception;
		boolean drop(Index index) throws Exception;
		boolean rename(Index origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													constraint
		 ******************************************************************************************************************/
		/**
		 * 修改约束
		 * @param constraint 约束
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean add(Constraint constraint) throws Exception;
		boolean alter(Constraint constraint) throws Exception;
		boolean drop(Constraint constraint) throws Exception;
		boolean rename(Constraint origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													trigger
		 ******************************************************************************************************************/
		/**
		 * 触发器
		 * @param trigger 触发器
		 * @return trigger
		 * @throws Exception 异常 Exception
		 */
		boolean create(Trigger trigger) throws Exception;
		boolean alter(Trigger trigger) throws Exception;
		boolean drop(Trigger trigger) throws Exception;
		boolean rename(Trigger origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													procedure
		 ******************************************************************************************************************/
		/**
		 * 存储过程
		 * @param procedure 存储过程
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean create(Procedure procedure) throws Exception;
		boolean alter(Procedure procedure) throws Exception;
		boolean drop(Procedure procedure) throws Exception;
		boolean rename(Procedure origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													function
		 ******************************************************************************************************************/
		/**
		 * 函数
		 * @param function 函数
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean create(Function function) throws Exception;
		boolean alter(Function function) throws Exception;
		boolean drop(Function function) throws Exception;
		boolean rename(Function origin, String name) throws Exception;
		/* *****************************************************************************************************************
		 * 													sequence
		 ******************************************************************************************************************/
		/**
		 * 函数
		 * @param sequence 序列
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean create(Sequence sequence) throws Exception;
		boolean alter(Sequence sequence) throws Exception;
		boolean drop(Sequence sequence) throws Exception;
		boolean rename(Sequence origin, String name) throws Exception;
	}
	interface Authorize {
		/**
		 * 创建用户
		 * @param user 用户
		 * @return boolean
		 */
		boolean create(User user);
		boolean grant(User user, Privilege privilege);
	}

}
