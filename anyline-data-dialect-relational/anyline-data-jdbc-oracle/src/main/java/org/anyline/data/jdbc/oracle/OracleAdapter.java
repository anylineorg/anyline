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

 


package org.anyline.data.jdbc.oracle;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.OracleGenusAdapter;
import org.anyline.data.jdbc.adapter.init.alias.OracleGenusTypeMetadataAlias;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.exception.NotSupportException;
import org.anyline.metadata.*;
import org.anyline.metadata.adapter.ColumnMetadataAdapter;
import org.anyline.metadata.adapter.IndexMetadataAdapter;
import org.anyline.metadata.adapter.PrimaryMetadataAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.AbstractTypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 12及以上版本
 */
@Component("anyline.data.jdbc.adapter.oracle")
public class OracleAdapter extends OracleGenusAdapter implements JDBCAdapter {
	
	public static boolean IS_GET_SEQUENCE_VALUE_BEFORE_INSERT = false;

	public DatabaseType type() {
		return DatabaseType.ORACLE; 
	}
	public String version() {
		return "12";
	}

	private String delimiter;

	public OracleAdapter() {
		super();
		delimiterFr = "\"";
		delimiterTo = "\"";
		OracleConvert.reg();
		for (OracleGenusTypeMetadataAlias alias : OracleGenusTypeMetadataAlias.values()) {
			reg(alias);
			alias(alias.name(), alias.standard());
		}

		for(OracleWriter writer: OracleWriter.values()) {
			reg(writer.supports(), writer.writer());
		}
		for(OracleReader reader: OracleReader.values()) {
			reg(reader.supports(), reader.reader());
		}
		alias.put("BFILE", new AbstractTypeMetadata(TypeMetadata.CATEGORY.BYTES,"BFILE", oracle.sql.BFILE.class, 1, 1, 1, DatabaseType.ORACLE));
		alias.put("ROWID", new AbstractTypeMetadata(TypeMetadata.CATEGORY.TEXT, "ROWID",  oracle.sql.ROWID.class, 1, 1, 1, DatabaseType.ORACLE));
		alias.put("UROWID", new AbstractTypeMetadata(TypeMetadata.CATEGORY.TEXT, "UROWID", oracle.sql.ROWID.class, 1, 1, 1, DatabaseType.ORACLE));
		alias.put("DATE", new AbstractTypeMetadata(TypeMetadata.CATEGORY.DATE, "DATE",java.util.Date.class, java.sql.Timestamp.class, 1, 1, 1, DatabaseType.ORACLE));
		alias.put("TIMESTAMP", new AbstractTypeMetadata(TypeMetadata.CATEGORY.TIMESTAMP, "TIMESTAMP", java.sql.Timestamp.class, oracle.sql.TIMESTAMP.class, 1, 1, 1, DatabaseType.ORACLE));
		alias.put("TIMESTAMPTZ", new AbstractTypeMetadata(TypeMetadata.CATEGORY.TIMESTAMP, "TIMESTAMPTZ", java.sql.Timestamp.class, oracle.sql.TIMESTAMPTZ.class, 1, 1, 1, DatabaseType.ORACLE));
		alias.put("TIMESTAMPLTZ", new AbstractTypeMetadata(TypeMetadata.CATEGORY.TIMESTAMP, "TIMESTAMPLTZ", java.sql.Timestamp.class, oracle.sql.TIMESTAMPLTZ.class, 1, 1, 1, DatabaseType.ORACLE));

	}

	/**
	 * 验证运行环境与当前适配器是否匹配<br/>
	 * 默认不连接只根据连接参数<br/>
	 * 只有同一个库区分不同版本(如mmsql2000/mssql2005)或不同模式(如kingbase的oracle/pg模式)时才需要单独实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
	 * @return boolean
	 */
	@Override
	public boolean match(DataRuntime runtime, boolean compensate) {
		boolean chk = super.match(runtime, compensate);
		if(chk) {
			String version = runtime.getVersion();
			//Oracle Database 11g Enterprise Edition Release 11.2.0.1.0 - 64bit Production With the Partitioning, OLAP, Data Mining and Real Application Testing options*//*
			if(null != version ) {
				version = RegularUtil.cut(version, "release","-");
				if(null != version) {
					//11.2.0.1.0
					version = version.split("\\.")[0];
				}
				double v = BasicUtil.parseDouble(version, 0d);
				String key = null;
				log.debug("[adapter match][Oracle版本检测][result:{}][runtime version:{}][adapter:{}]", false, version, this.getClass());
				if(v >= 12.0) {
					return true;
				}
			}
		}
		return false;
	}

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * UPDATE			: 更新
	 * SAVE				: 根据情况插入或更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 *
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													INSERT
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
	 * public String batchInsertSeparator()
	 * public boolean supportInsertPlaceholder()
	 * protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns)
	 * public String generatedKey()
	 * [命令执行]
	 * long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);
	 ******************************************************************************************************************/

	/**
	 * insert [调用入口]<br/>
	 * 执行前根据主键生成器补充主键值, 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 需要插入入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return super.insert(runtime, random, batch, dest, data, configs, columns);
	}

	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 需要插入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		return super.buildInsertRun(runtime, batch, dest, obj, configs, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		super.fillInsertContent(runtime, run, dest, set, configs, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		super.fillInsertContent(runtime, run, dest, list, configs, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 确认需要插入的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj  Entity或DataRow
	 * @param batch  是否批量，批量时不检测值是否为空
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch) {
		return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 批量插入数据时, 多行数据之间分隔符
	 * @return String
	 */
	@Override
	public String batchInsertSeparator() {
		return ",";
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	@Override
	public boolean supportInsertPlaceholder() {
		return true;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 设置主键值
	 * @param obj obj
	 * @param value value
	 */
	@Override
	protected void setPrimaryValue(Object obj, Object value) {
		super.setPrimaryValue(obj, value);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 根据entity创建 INSERT RunPrepare由buildInsertRun调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		return super.createInsertRun(runtime, dest, obj, configs, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 根据collection创建 INSERT RunPrepare由buildInsertRun调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 对象集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns) {
		return super.createInsertRunFromCollection(runtime, batch, dest, list, configs, columns);
	}

	/**
	 * insert [after]<br/>
	 * 执行insert后返回自增主键的key
	 * @return String
	 */
	@Override
	public String generatedKey() {
		return super.generatedKey();
	}

	/**
	 * insert [命令执行]
	 * <br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param pks 需要返回的主键
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks) {
		return super.insert(runtime, random, data, configs, run, pks);
	}

	/* *****************************************************************************************************************
	 * 													UPDATE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns)
	 * Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns)
	 * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns)
	 * [命令执行]
	 * long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run)
	 ******************************************************************************************************************/
	/**
	 * UPDATE [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return super.update(runtime, random, batch, dest, data, configs, columns);
	}

	/**
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj Entity或DtaRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns) {
		return super.buildUpdateRun(runtime, batch, dest, obj, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, columns);
	}

	/**
	 * update [命令合成-子流程]<br/>
	 * 确认需要更新的列
	 * @param row DataRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns) {
		return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
	}
	@Override
	public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns) {
		return super.confirmUpdateColumns(runtime, dest, obj, configs, columns);
	}

	/**
	 * update [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run) {
		return super.update(runtime, random, dest, data, configs, run);
	}

	/**
	 * save [调用入口]<br/>
	 * <br/>
	 * 根据是否有主键值确认insert | update<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
		return super.save(runtime, random, dest, data, configs, columns);
	}

	@Override
	protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns) {
		return super.saveCollection(runtime, random, dest, data, configs, columns);
	}
	@Override
	protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return super.saveObject(runtime, random, dest, data, configs, columns);
	}
	@Override
	protected Boolean checkOverride(Object obj) {
		return super.checkOverride(obj);
	}
	@Override
	protected Map<String, Object> checkPv(Object obj) {
		return super.checkPv(obj);
	}

	/**
	 * 是否是可以接收数组类型的值
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param key key
	 * @return boolean
	 */
	@Override
	protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key) {
		return super.isMultipleValue(runtime, run, key);
	}
	@Override
	protected boolean isMultipleValue(Column column) {
		return super.isMultipleValue(column);
	}

	/**
	 * 过滤掉表结构中不存在的列
	 * @param table 表
	 * @param columns columns
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return super.checkMetadata(runtime, table, configs, columns);
	}

	/* *****************************************************************************************************************
	 * 													QUERY
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)
	 * <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions)
	 * List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * [命令合成]
	 * Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)
	 * Run fillQueryContent(DataRuntime runtime, Run run)
	 * String mergeFinalQuery(DataRuntime runtime, Run run)
	 * RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)
	 * Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder)
	 * StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)
	 * [命令执行]
	 * DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)
	 * List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 * Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) 
	 * DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)
	 * List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list)
	 ******************************************************************************************************************/

	/**
	 * query [调用入口]<br/>
	 * <br/>
	 * 返回DataSet中包含元数据信息，如果性能有要求换成maps
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return super.querys(runtime, random, prepare, configs, conditions);
	}

	/**
	 * query procedure [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
		return super.querys(runtime, random, procedure, navi);
	}

	/**
	 * query [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param clazz 类
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return EntitySet
	 * @param <T> Entity
	 */
	@Override
	public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions) {
		return super.selects(runtime, random, prepare, clazz, configs, conditions);
	}

	/**
	 * select [命令执行-子流程]<br/>
	 * DataRow转换成Entity
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param clazz entity class
	 * @param table table
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return EntitySet
	 * @param <T> entity.class
	 *
	 */
	@Override
	protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, Table table, ConfigStore configs, Run run) {
		return super.select(runtime, random, clazz, table, configs, run);
	}

	/**
	 * query [调用入口]<br/>
	 * <br/>
	 * 对性能有要求的场景调用，返回java原生map集合, 结果中不包含元数据信息
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return maps 返回map集合
	 */
	@Override
	public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return super.maps(runtime, random, prepare, configs, conditions);
	}

	/**
	 * select[命令合成]<br/> 最终可执行命令<br/>
	 * 创建查询SQL
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return super.buildQueryRun(runtime, prepare, configs, conditions);
	}

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	@Override
	public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names) {
		return super.buildQuerySequence(runtime, next, names);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public Run fillQueryContent(DataRuntime runtime, Run run) {
		return super.fillQueryContent(runtime, run);
	}
	@Override
	protected Run fillQueryContent(DataRuntime runtime, XMLRun run) {
		return super.fillQueryContent(runtime, run);
	}
	@Override
	protected Run fillQueryContent(DataRuntime runtime, TextRun run) {
		return super.fillQueryContent(runtime, run);
	}
	@Override
	protected Run fillQueryContent(DataRuntime runtime, TableRun run) {
		return super.fillQueryContent(runtime, run);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 合成最终 select 命令 包含分页 排序
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return String
	 */
	@Override
	public String mergeFinalQuery(DataRuntime runtime, Run run) {
		return super.mergeFinalQuery(runtime, run);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造 LIKE 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value 有占位符时返回占位值，没有占位符返回null
	 */
	@Override
	public RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder) {
		return super.createConditionLike(runtime, builder, compare, value, placeholder);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造 FIND_IN_SET 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value
	 */
	@Override
	public Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder) throws NotSupportException {
		return super.createConditionFindInSet(runtime, builder, column, compare, value, placeholder);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造(NOT) IN 查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return builder
	 */
	@Override
	public StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder) {
		return super.createConditionIn(runtime, builder, compare, value, placeholder);
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param system 系统表不检测列属性
	 * @param table 表
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return DataSet
	 */
	@Override
	public DataSet select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
		return super.select(runtime, random, system, table, configs, run);
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return maps
	 */
	@Override
	public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		return super.maps(runtime, random, configs, run);
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return map
	 */
	@Override
	public Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		return super.map(runtime, random, configs, run);
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next 是否查下一个序列值
	 * @param names 存储过程名称s
	 * @return DataRow 保存序列查询结果 以存储过程name作为key
	 */
	@Override
	public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names) {
		return super.sequence(runtime, random, next, names);
	}

	/**
	 * select [结果集封装-子流程]<br/>
	 * JDBC执行完成后的结果处理
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param list JDBC执行返回的结果集
	 * @return  maps
	 */
	@Override
	public List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list) {
		return super.process(runtime, list);
	}

	/* *****************************************************************************************************************
	 * 													COUNT
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * [命令合成]
	 * String mergeFinalTotal(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long count(DataRuntime runtime, String random, Run run)
	 ******************************************************************************************************************/
	/**
	 * count [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return super.count(runtime, random, prepare, configs, conditions);
	}

	/**
	 * count [命令合成]<br/>
	 * 合成最终 select count 命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return String
	 */
	@Override
	public String mergeFinalTotal(DataRuntime runtime, Run run) {
		return super.mergeFinalTotal(runtime, run);
	}

	/**
	 * count [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, Run run) {
		return super.count(runtime, random, run);
	}

	/* *****************************************************************************************************************
	 * 													EXISTS
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * String mergeFinalExists(DataRuntime runtime, Run run)
	 ******************************************************************************************************************/

	/**
	 * exists [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return boolean
	 */
	@Override
	public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return super.exists(runtime, random, prepare, configs, conditions);
	}
	@Override
	public String mergeFinalExists(DataRuntime runtime, Run run) {
		return super.mergeFinalExists(runtime, run);
	}

	/* *****************************************************************************************************************
	 * 													EXECUTE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values)
	 * boolean execute(DataRuntime runtime, String random, Procedure procedure)
	 * [命令合成]
	 * Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * void fillExecuteContent(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) 
	 ******************************************************************************************************************/

	/**
	 * execute [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)  {
		return super.execute(runtime, random, prepare, configs, conditions);
	}

	@Override
	public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values) {
		return super.execute(runtime, random, batch, configs, prepare, values);
	}

	/**
	 * procedure [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @param random  random
	 * @return 影响行数
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Procedure procedure) {
		return super.execute(runtime, random, procedure);
	}

	/**
	 * execute [命令合成]<br/>
	 * 创建执行SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return super.buildExecuteRun(runtime, prepare, configs, conditions);
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, XMLRun run) {
		super.fillExecuteContent(runtime, run);
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, TextRun run) {
		super.fillExecuteContent(runtime, run);
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, TableRun run) {
		super.fillExecuteContent(runtime, run);
	}

	/**
	 * execute [命令合成-子流程]<br/>
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public void fillExecuteContent(DataRuntime runtime, Run run) {
		super.fillExecuteContent(runtime, run);
	}

	/**
	 * execute [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		return super.execute(runtime, random, configs, run);
	}

	/* *****************************************************************************************************************
	 * 													DELETE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, Collection<T> values)
	 * long delete(DataRuntime runtime, String random, String table, ConfigStore configs, Object obj, String... columns)
	 * long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions)
	 * long truncate(DataRuntime runtime, String random, String table)
	 * [命令合成]
	 * List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns)
	 * List<Run> buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String column, Object values)
	 * List<Run> buildTruncateRun(DataRuntime runtime, String table)
	 * List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,String column, Object values)
	 * List<Run> buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns)
	 * void fillDeleteRunContent(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long delete(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 ******************************************************************************************************************/
	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String key, Collection<T> values) {
		return super.deletes(runtime, random, batch, table, configs, key, values);
	}

	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, String dest, ConfigStore configs, Object obj, String... columns) {
		return super.delete(runtime, random, dest, configs, obj, columns);
	}

	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 根据configs和conditions过滤条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions) {
		return super.delete(runtime, random, table, configs, conditions);
	}

	/**
	 * truncate [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return 1表示成功执行
	 */
	@Override
	public long truncate(DataRuntime runtime, String random, Table table) {
		return super.truncate(runtime, random, table);
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, String ... columns) {
		return super.buildDeleteRun(runtime, dest, configs, obj, columns);
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 根据属性解析出列
	 * @param values values
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String key, Object values) {
		return super.buildDeleteRun(runtime, batch, table, configs, key, values);
	}

	@Override
	public List<Run> buildTruncateRun(DataRuntime runtime, String table) {
		return super.buildTruncateRun(runtime, table);
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values) {
		return super.buildDeleteRunFromTable(runtime, batch, table, configs, column, values);
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String... columns) {
		return super.buildDeleteRunFromEntity(runtime, table, configs, obj, columns);
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public void fillDeleteRunContent(DataRuntime runtime, Run run) {
		super.fillDeleteRunContent(runtime, run);
	}

	/**
	 * delete[命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param configs 查询条件及相关设置
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		return super.delete(runtime, random, configs, run);
	}

	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库(catalog, schema)
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

	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
	 * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
	 * Database database(DataRuntime runtime, String random, String name)
	 * Database database(DataRuntime runtime, String random)
	 * String String product(DataRuntime runtime, String random);
	 * String String version(DataRuntime runtime, String random);
	 * [命令合成]
	 * List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryProductRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryVersionRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set)
	 * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet set)
	 * Database database(DataRuntime runtime, boolean create, Database dataase, DataSet set)
	 * Database database(DataRuntime runtime, boolean create, Database dataase)
	 * String product(DataRuntime runtime, boolean create, Database product, DataSet set)
	 * String product(DataRuntime runtime, boolean create, String product)
	 * String version(DataRuntime runtime, int index, boolean create, String version, DataSet set)
	 * String version(DataRuntime runtime, boolean create, String version)
	 * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog)
	 * Schema schema(DataRuntime runtime, boolean create, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, boolean create, Schema schema)
	 * Database database(DataRuntime runtime, boolean create, Database dataase)
	 ******************************************************************************************************************/

	/**
	 * database[调用入口]<br/>
	 * 当前数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Database
	 */
	@Override
	public Database database(DataRuntime runtime, String random) {
		return super.database(runtime, random);
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	public String product(DataRuntime runtime, String random) {
		return super.product(runtime, random);
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	public String version(DataRuntime runtime, String random) {
		return super.version(runtime, random);
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Database> databases(DataRuntime runtime, String random, boolean greedy, Database query) {
		return super.databases(runtime, random, greedy, query);
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, Database query) {
		return super.databases(runtime, random, query);
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库产品说明(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryProductRun(DataRuntime runtime) throws Exception {
		return super.buildQueryProductRun(runtime);
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryVersionRun(DataRuntime runtime) throws Exception {
		return super.buildQueryVersionRun(runtime);
	}

	/**
	 * database[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, Database query) throws Exception {
		return super.buildQueryDatabasesRun(runtime, greedy, query);
	}

	/**
	 * database[结果集封装]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> previous, Database query, DataSet set) throws Exception {
		return super.databases(runtime, index, create, previous, query, set);
	}
	@Override
	public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> previous, Database query, DataSet set) throws Exception {
		return super.databases(runtime, index, create, previous, query, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @param set 查询结果集
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, int index, boolean create, Database database, DataSet set) throws Exception {
		return super.database(runtime, index, create, database, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, boolean create, Database database) throws Exception {
		return super.database(runtime, create, database);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @param set 查询结果集
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, int index, boolean create, String product, DataSet set) {
		return super.product(runtime, index, create, product, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, boolean create, String product) {
		return super.product(runtime, create, product);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @param set 查询结果集
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, int index, boolean create, String version, DataSet set) {
		return super.version(runtime, index, create, version, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, boolean create, String version) {
		return super.version(runtime, create, version);
	}

	/* *****************************************************************************************************************
	 * 													catalog
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name)
	 * List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
	 * [命令合成]
	 * List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]<br/>
	 * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog)
	 ******************************************************************************************************************/
	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, Catalog query) {
		return super.catalogs(runtime, random, query);
	}

	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, Catalog query) {
		return super.catalogs(runtime, random, greedy, query);
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, Catalog query) throws Exception {
		return super.buildQueryCatalogsRun(runtime, greedy, query);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
		return super.catalogs(runtime, index, create, previous, catalog, schema, set);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> previous, Catalog query, DataSet set) throws Exception {
		return super.catalogs(runtime, index, create, previous, query, set);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> previous) throws Exception {
		return super.catalogs(runtime, create, previous);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> previous) throws Exception {
		return super.catalogs(runtime, create, previous);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previou 上一步查询结果
	 * @param set 查询结果集
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog previou, DataSet set) throws Exception {
		return super.catalog(runtime, index, create, previou, set);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previou 上一步查询结果
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, boolean create, Catalog previou) throws Exception {
		return super.catalog(runtime, create, previou);
	}

	/* *****************************************************************************************************************
	 * 													schema
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name)
	 * List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name)
	 * [命令合成]
	 * List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet set)
	 * List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema)
	 ******************************************************************************************************************/
	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Schema query) {
		return super.schemas(runtime, random, query);
	}

	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Schema query) {
		return super.schemas(runtime, random, greedy, query);
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Schema query) throws Exception {
		return super.buildQuerySchemasRun(runtime, greedy, query);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> previous, Schema query, DataSet set) throws Exception {
		return super.schemas(runtime, index, create, previous, query, set);
	}
	@Override
	public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> previous, Schema query, DataSet set) throws Exception {
		return super.schemas(runtime, index, create, previous, query, set);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQuerySchemaRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previou 上一步查询结果
	 * @param set 查询结果集
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, int index, boolean create, Schema previou, DataSet set) throws Exception {
		return super.schema(runtime, index, create, previou, set);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param previou 上一步查询结果
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, boolean create, Schema previou) throws Exception {
		return super.schema(runtime, create, previou);
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Table table, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
	 * @return List
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Table query, int types, int struct, ConfigStore configs) {
		return super.tables(runtime, random, greedy, query, types, struct, configs);
	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	@Override
	protected void tableMap(DataRuntime runtime, String random, boolean greedy, Table query, ConfigStore configs) {
		super.tableMap(runtime, random, greedy, query, configs);
	}
public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Table query, int types, int struct, ConfigStore configs) {
		return super.tables(runtime, random, query, types, struct, configs);
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Table query, int types, ConfigStore configs) throws Exception {
		List<Run> runs = super.buildQueryTablesRun(runtime, greedy, query, types, configs);
		for(Run run:runs) {
			run.getBuilder().append(" AND M.OWNER NOT IN('CTXSYS','EXFSYS','WMSYS','MDSYS','SYSTEM','OLAPSYS','SYSMAN','APEX_030200','SYS')");
		}
		return runs;
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Table query, int types) throws Exception {
		List<Run> runs = super.buildQueryTablesCommentRun(runtime, query, types);
		for(Run run:runs) {
			run.getBuilder().append(" AND OWNER NOT IN('CTXSYS','EXFSYS','WMSYS','MDSYS','SYSTEM','OLAPSYS','SYSMAN','APEX_030200','SYS')");
		}
		return runs;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception {
		return super.tables(runtime, index, create, previous, query, set);
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Table query, DataSet set) throws Exception {
		return super.tables(runtime, index, create, tables, query, set);
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Table query, int types) throws Exception {
		return super.tables(runtime, create, tables, query, types);
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Table query, int types) throws Exception {
		return super.tables(runtime, create, tables, query, types);
	}

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception {
		return super.comments(runtime, index, create, previous, query, set);
	}

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet set) throws Exception {
		return super.comments(runtime, index, create, previous, query, set);
	}

	/**
	 *
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init) {
		return super.ddl(runtime, random, table, init);
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table) throws Exception {
		return super.buildQueryDdlsRun(runtime, table);
	}

	/**
	 * table[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, table, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
	 * [命令合成]
	 * List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, View view)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, View view)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 * view[调用入口]<br/>
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @return List
	 * @param <T> View
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, int types, int struct, ConfigStore configs) {
		return super.views(runtime, random, query, types, struct, configs);
	}

	/**
	 * view[命令合成]<br/>
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
	 * @return List
	 */
	@Override
	public List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, View query, int types, ConfigStore configs) throws Exception {
		return super.buildQueryViewsRun(runtime, greedy, query, types, configs);
	}

	/**
	 * view[结果集封装]<br/>
	 *  根据查询结果集构造View
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, View query, DataSet set) throws Exception {
		return super.views(runtime, index, create, views, query, set);
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, View query, int types) throws Exception {
		return super.views(runtime, create, views, query, types);
	}

	/**
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param view 视图
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, View view, boolean init) {
		return super.ddl(runtime, random, view, init);
	}

	/**
	 * view[命令合成]<br/>
	 * 查询view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view view
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, View view) throws Exception {
		return super.buildQueryDdlsRun(runtime, view);
	}

	/**
	 * view[结果集封装]<br/>
	 * 查询 view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param view view
	 * @param previous 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, view, ddls, set);
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs)
	 * [命令合成]
	 * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * [结果集封装]<br/>
	 * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * [结果集封装]<br/>
	 * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables,Catalog catalog, Schema schema, String pattern, int types)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, MasterTable table)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 * master table[调用入口]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, int types, int struct, ConfigStore configs) {
		return super.masters(runtime, random, query, types, struct, configs);
	}

	/**
	 * master table[命令合成]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, MasterTable query, int types, ConfigStore configs) throws Exception {
		return super.buildQueryMasterTablesRun(runtime, greedy, query, types,  configs);
	}

	/**
	 * master table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, MasterTable query, DataSet set) throws Exception {
		return super.masters(runtime, index, create, previous, query, set);
	}

	/**
	 * master table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, MasterTable query, int types) throws Exception {
		return super.masters(runtime, create, previous, query, types);
	}

	/**
	 * master table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table MasterTable
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, MasterTable table, boolean init) {
		return super.ddl(runtime, random, table, init);
	}

	/**
	 * master table[命令合成]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table MasterTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table) throws Exception {
		return super.buildQueryDdlsRun(runtime, table);
	}

	/**
	 * master table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table MasterTable
	 * @param previous 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, table, ddls, set);
	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String, Tag> tags, String pattern)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String, Tag> tags)
	 * [结果集封装]<br/>
	 * <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, PartitionTable table)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * partition table[调用入口]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param master 主表
	 * @param pattern 名称统配符或正则
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, PartitionTable query) {
		return super.partitions(runtime, random, greedy, query);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, PartitionTable query, int types) throws Exception {
		return super.buildQueryPartitionTablesRun(runtime, query, types);
	}


	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, PartitionTable query, DataSet set) throws Exception {
		return super.partitions(runtime, total, index, create, query, set);
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param previous 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, PartitionTable query) throws Exception {
		return super.partitions(runtime, create, previous, query);
	}

	/**
	 * partition table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table) {
		return super.ddl(runtime, random, table);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 查询 PartitionTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table) throws Exception {
		return super.buildQueryDdlsRun(runtime, table);
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table MasterTable
	 * @param previous 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, table, ddls, set);
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);
	 * <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String table);
	 * [命令合成]
	 * List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
	 * [结果集封装]<br/>
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception;
	 * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * column[调用入口]<br/>
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Column query, boolean primary) {
		return super.columns(runtime, random, greedy, query, primary);
	}

	

	/**
	 * column[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryColumnsRun(DataRuntime runtime,  boolean metadata, Column query, ConfigStore configs) throws Exception {
		List<Run> runs = super.buildQueryColumnsRun(runtime, table, metadata, configs);
		if(!metadata) {
			for(Run run:runs) {
				run.getBuilder().append(" AND M.OWNER NOT IN('CTXSYS','EXFSYS','WMSYS','MDSYS','SYSTEM','OLAPSYS','SYSMAN','APEX_030200','SYS')");
			}
		}
		return runs;
	}

	/**
	 * column[命令合成]<br/>(方法1)<br/>
	 * 查询多个表的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryColumnsRun(DataRuntime runtime, boolean metadata, Collection<? extends Table> tables, Column query, ConfigStore configs) throws Exception {
		return super.buildQueryColumnsRun(runtime, metadata, tables, query, configs);
	}
	/**
	 * column[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception {
		return super.columns(runtime, index, create, table, columns, set);
	}
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception {
		return super.columns(runtime, index, create, table, columns, set);
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 * 根据查询结果集构造Column,并分配到各自的表中
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 表
	 * @param previous 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create,  List<T> previous, Collection<? extends Table> tables, Column query, DataSet set) throws Exception {
		return super.columns(runtime, index, create, tables, columns, set);
	}

	/**
	 * column[调用入口]<br/>(方法1)<br/>
	 * 查询多个表列，并分配到每个表中，需要保持所有表的catalog,schema相同
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 表
	 * @return List
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Collection<? extends Table> tables, ConfigStore configs) {
		return super.columns(runtime, random, greedy, catalog, schema, tables, configs);
	}

	/**
	 * column[结果集封装]<br/>
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @return columns 上一步查询结果
	 * @param pattern 名称
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception {
		return super.columns(runtime, create, columns, table, pattern);
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 列基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row 系统表查询SQL结果集
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) {
		return super.init(runtime, index, meta, table, row);
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Column
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return super.detail(runtime, index, meta, catalog, schema, row);
	}

	/**
	 * column[结构集封装-依据]<br/>
	 * 读取column元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ColumnMetadataAdapter
	 */
	@Override
	public ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime) {
		return super.columnMetadataAdapter(runtime);
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据数字有效位数列<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public String columnMetadataLengthRefer(DataRuntime runtime, TypeMetadata meta) {
		return super.columnMetadataLengthRefer(runtime, meta);
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据长度列<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public String columnMetadataPrecisionRefer(DataRuntime runtime, TypeMetadata meta) {
		return super.columnMetadataPrecisionRefer(runtime, meta);
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据数字有效位数列<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public String columnMetadataScaleRefer(DataRuntime runtime, TypeMetadata meta) {
		return super.columnMetadataScaleRefer(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table)
	 * [命令合成]
	 * List<Run> buildQueryTagsRun(DataRuntime runtime, Table table, boolean metadata)
	 * [结果集封装]<br/>
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set)
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return Tag
	 * @param <T>  Tag
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table) {
		return super.tags(runtime, random, greedy, table);
	}

	/**
	 * tag[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryTagsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception {
		return super.buildQueryTagsRun(runtime, table, metadata);
	}

	/**
	 * tag[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryTagsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception {
		return super.tags(runtime, index, create, table, tags, set);
	}

	/**
	 *
	 * tag[结果集封装]<br/>
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param pattern 名称统配符或正则
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception {
		return super.tags(runtime, create, tags, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
	 * [命令合成]
	 * List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception
	 * [结构集封装]
	 * <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * primary[调用入口]<br/>
	 * 查询主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return PrimaryKey
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, PrimaryKey query) {
		return super.primary(runtime, random, greedy, query);
	}

	/**
	 * primary[命令合成]<br/>
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, PrimaryKey query) throws Exception {
		return super.buildQueryPrimaryRun(runtime, query);
	}

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, PrimaryKey query, DataSet set) throws Exception {
		return super.init(runtime, index, primary, query, set);
	}

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T primary, PrimaryKey query, DataSet set) throws Exception {
		return super.detail(runtime, index, primary, query, set);
	}
	/**
	 * primary[结构集封装-依据]<br/>
	 * 读取primary key元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return PrimaryMetadataAdapter
	 */
	@Override
	public PrimaryMetadataAdapter primaryMetadataAdapter(DataRuntime runtime) {
		return super.primaryMetadataAdapter(runtime);
	}
	/**
	 * primary[结构集封装]<br/>
	 *  根据驱动内置接口补充PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @throws Exception 异常
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, PrimaryKey query) throws Exception {
		return super.primary(runtime, query);
	}
	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	 * [命令合成]
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception;
	 * [结构集封装]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 查询外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return PrimaryKey
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, ForeignKey query) {
		return super.foreigns(runtime, random, greedy, query);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryForeignsRun(DataRuntime runtime, ForeignKey query) throws Exception {
		return super.buildQueryForeignsRun(runtime, query);
	}

	/**
	 * foreign[结构集封装]<br/>
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> previous, ForeignKey query, DataSet set) throws Exception {
		return super.foreigns(runtime, index, table, previous, query, set);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
	 * <T extends Index> LinkedHashMap<T, Index> indexes(DataRuntime runtime, String random, Table table, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name)
	 * [结果集封装]<br/>
	 * <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set)
	 * <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate)
	 ******************************************************************************************************************/
	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Index query) {
		return super.indexes(runtime, random, greedy, query);
	}

	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Index query) {
		return super.indexes(runtime, random, query);
	}

	/**
	 * index[命令合成]<br/>
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param name 名称
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, Index query) {
		return super.buildQueryIndexesRun(runtime, query);
	}
	@Override
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, Collection<? extends Table> tables) {
		return super.buildQueryIndexesRun(runtime, tables);
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Index query, DataSet set) throws Exception {
		return super.indexes(runtime, index, create, previous, query, set);
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, List<T> previous, Index query, DataSet set) throws Exception {
		return super.indexes(runtime, index, create, previous, query, set);
	}

	/**
	 * index[结果集封装]<br/>
	 * 根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param unique 是否唯一
	 * @param approximate 索引允许结果反映近似值
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> previous, Index query) throws Exception {
		return super.indexes(runtime, create, previous, query);
	}

	/**
	 * index[结果集封装]<br/>
	 * 根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param unique 是否唯一
	 * @param approximate 索引允许结果反映近似值
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Index query) throws Exception {
		return super.indexes(runtime, create, previous, query);
	}

	/**
	 * index[结构集封装]<br/>
	 * 根据查询结果集构造index基础属性(name,table,schema,catalog)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> T init(DataRuntime runtime, int index, T meta, Index query, DataRow row) throws Exception {
		return super.init(runtime, index, meta, query, row);
	}

	/**
	 * index[结构集封装]<br/>
	 * 根据查询结果集构造index更多属性(column,order, position)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> T detail(DataRuntime runtime, int index, T meta, Index query, DataRow row) throws Exception {
		return super.detail(runtime, index, meta, query, row);
	}
	/**
	 * index[结构集封装-依据]<br/>
	 * 读取index元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	@Override
	public IndexMetadataAdapter indexMetadataAdapter(DataRuntime runtime) {
		return super.indexMetadataAdapter(runtime);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 *
	 * constraint[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Constraint query) {
		return super.constraints(runtime, random, greedy, query);
	}

	/**
	 *
	 * constraint[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param column 列
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern) {
		return super.constraints(runtime, random, table, column, pattern);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 查询表上的约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param pattern 名称通配符或正则
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern) {
		return super.buildQueryConstraintsRun(runtime, table, column, pattern);
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, List<T> previous, Constraint query, DataSet set) throws Exception {
		return super.constraints(runtime, index, create, previous, query, set);
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param column 列
	 * @param previous 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception {
		return super.constraints(runtime, index, create, table, column, constraints, set);
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)
	 * [命令合成]
	 * List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)
	 * [结果集封装]<br/>
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * trigger[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Trigger query) {
		return super.triggers(runtime, random, greedy, query);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	public List<Run> buildQueryTriggersRun(DataRuntime runtime, Trigger query) {
		return super.buildQueryTriggersRun(runtime, query);
	}

	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Trigger query, DataSet set) throws Exception {
		return super.triggers(runtime, index, create, previous, query, set);
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set);
	 ******************************************************************************************************************/
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Procedure query) {
		return super.procedures(runtime, random, greedy, query);
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Procedure query) {
		return super.procedures(runtime, random, query);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryProceduresRun(DataRuntime runtime, Procedure query) {
		return super.buildQueryProceduresRun(runtime, query);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Procedure query, DataSet set) throws Exception {
		return super.procedures(runtime, index, create, previous, query, set);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> previous, Procedure query) throws Exception {
		return super.procedures(runtime, create, previous, query);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Procedure query) throws Exception {
		return super.procedures(runtime, create, previous, query);
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure) {
		return super.ddl(runtime, random, procedure);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询存储DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception {
		return super.buildQueryDdlsRun(runtime, procedure);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param procedure Procedure
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, procedure, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
	 * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Function function);
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Function function) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Function query) {
		return super.functions(runtime, random, greedy, query);
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Function query) {
		return super.functions(runtime, random, query);
	}

	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param name 名称统配符或正则
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryFunctionsRun(DataRuntime runtime, Function query) {
		return super.buildQueryFunctionsRun(runtime, query);
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> previous, Function query, DataSet set) throws Exception {
		return super.functions(runtime, index, create, previous, query, set);
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Function query, DataSet set) throws Exception {
		return super.functions(runtime, index, create, previous, query, set);
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> previous, Function query) throws Exception {
		return super.functions(runtime, create, previous, query);
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Function
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Function meta) {
		return super.ddl(runtime, random, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 查询函数DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildQueryDdlsRun(runtime, meta);
	}

	/**
	 * function[结果集封装]<br/>
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param function Function
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, function, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence sequence) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Sequence query) {
		return super.sequences(runtime, random, greedy, query);
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Sequence query) {
		return super.sequences(runtime, random, query);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param name 名称统配符或正则
	 * @return runs
	 */
	@Override
	public List<Run> buildQuerySequencesRun(DataRuntime runtime, Sequence query) {
		return super.buildQuerySequencesRun(runtime, query);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> previous, Sequence query, DataSet set) throws Exception {
		return super.sequences(runtime, index, create, previous, query, set);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Sequence query, DataSet set) throws Exception {
		return super.sequences(runtime, index, create, previous, query, set);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences, Sequence query) throws Exception {
		return super.sequences(runtime, create, sequences, query);
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Sequence
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Sequence meta) {
		return super.ddl(runtime, random, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询序列DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildQueryDdlsRun(runtime, meta);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 查询 Sequence DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param sequence Sequence
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, sequence, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													common
	 * ----------------------------------------------------------------------------------------------------------------
	 */
	/**
	 *
	 * 根据 catalog, schema, name检测tables集合中是否存在
	 * @param metas metas
	 * @param catalog catalog
	 * @param schema schema
	 * @param name name
	 * @return 如果存在则返回Table 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Metadata> T search(List<T> metas, Catalog catalog, Schema schema, String name) {
		return super.search(metas, catalog, schema, name);
	}

	/**
	 *
	 * 根据 catalog, name检测schemas集合中是否存在
	 * @param schemas schemas
	 * @param catalog catalog
	 * @param name name
	 * @return 如果存在则返回 Schema 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name) {
		return super.schema(schemas, catalog, name);
	}

	/**
	 *
	 * 根据 name检测catalogs集合中是否存在
	 * @param catalogs catalogs
	 * @param name name
	 * @return 如果存在则返回 Catalog 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Catalog> T catalog(List<T> catalogs, String name) {
		return super.catalog(catalogs, name);
	}

	/**
	 *
	 * 根据 name检测databases集合中是否存在
	 * @param databases databases
	 * @param name name
	 * @return 如果存在则返回 Database 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Database> T database(List<T> databases, String name) {
		return super.database(databases, name);
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

	/**
	 * ddl [执行命令]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return boolean
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run) {
		return super.execute(runtime, random, meta, action, run);
	}
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Table meta)
	 * boolean alter(DataRuntime runtime, Table meta)
	 * boolean drop(DataRuntime runtime, Table meta)
	 * boolean rename(DataRuntime runtime, Table origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns)
	 * List<Run> buildRenameRun(DataRuntime runtime, Table meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Table meta)
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table table)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table)
	 * time runtime, StringBuilder builder, Table table)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table table)
	 ******************************************************************************************************************/
	/**
	 * table[调用入口]<br/>
	 * 创建表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Table meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Table meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * table[调用入口]<br/>
	 * 重命名表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	

	/**
	 * table[命令合成]<br/>
	 * 创建表<br/>
	 * 关于创建主键的几个环节<br/>
	 * 1.1.定义列时 标识 primary(DataRuntime runtime, StringBuilder builder, Column column)<br/>
	 * 1.2.定义表时 标识 primary(DataRuntime runtime, StringBuilder builder, Table table)<br/>
	 * 1.3.定义完表DDL后，单独创建 primary(DataRuntime runtime, PrimaryKey primary)根据三选一情况调用buildCreateRun<br/>
	 * 2.单独创建 buildCreateRun(DataRuntime runtime, PrimaryKey primary)<br/>
	 * 其中1.x三选一 不要重复
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return runs
	 * @throws Exception
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 修改表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param columns 列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns) throws Exception {
		return super.buildAlterRun(runtime, meta, columns);
	}

	/**
	 * table[命令合成]<br/>
	 * 重命名
	 * 子类实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 删除表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAppendColumnCommentRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		return super.checkTableExists(runtime, builder, exists);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 */
	@Override
	public void checkPrimary(DataRuntime runtime, Table table) {
		super.checkPrimary(runtime, table);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 定义表的主键标识,在创建表的DDL结尾部分(注意不要跟列定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.primary(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 engine
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.engine(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 body部分包含column index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.body(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 columns部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.columns(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 索引部分，与buildAppendIndexRun二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder indexes(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.indexes(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.charset(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 备注 创建表的完整DDL拼接COMMENT部分，与buildAppendCommentRun二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.comment(runtime, builder, meta);
	}
	
	/**
	 * table[命令合成-子流程]<br/>
	 * 数据模型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.keys(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 分桶方式
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.distribution(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 物化视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.materialize(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 扩展属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta) {
		return super.property(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 主表设置分区依据(根据哪几列分区)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		return super.partitionBy(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(相关主表)<br/>
	 * 如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		return super.partitionOf(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(分区依据值)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		return super.partitionFor(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 继承自table.getInherit
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		return super.inherit(runtime, builder, meta);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, View view) throws Exception;
	 * boolean alter(DataRuntime runtime, View view) throws Exception;
	 * boolean drop(DataRuntime runtime, View view) throws Exception;
	 * boolean rename(DataRuntime runtime, View origin, String name) throws Exception;
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception;
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception;
	 * StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view);
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 创建视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, View meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, View meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, View meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * view[调用入口]<br/>
	 * 重命名视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 视图
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, View origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * view[命令合成]<br/>
	 * 创建视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return Run
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图头部
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta) throws Exception {
		return super.buildCreateRunHead(runtime, builder, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图选项
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta) throws Exception {
		return super.buildCreateRunOption(runtime, builder, meta);
	}

	/**
	 * view[命令合成]<br/>
	 * 修改视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * view[命令合成]<br/>
	 * 重命名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * view[命令合成]<br/>
	 * 删除视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		return super.checkViewExists(runtime, builder, exists);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta) {
		return super.comment(runtime, builder, meta);
	}

	/* *****************************************************************************************************************
	 * 													MasterTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, MasterTable meta)
	 * boolean alter(DataRuntime runtime, MasterTable meta)
	 * boolean drop(DataRuntime runtime, MasterTable meta)
	 * boolean rename(DataRuntime runtime, MasterTable origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildDropRun(DataRuntime runtime, MasterTable table)
	 * [命令合成-子流程]
	 * List<Run> buildAlterRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildRenameRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table)
	 ******************************************************************************************************************/

	/**
	 * master table[调用入口]<br/>
	 * 创建主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * master table[调用入口]<br/>
	 * 重命名主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * master table[命令合成]<br/>
	 * 创建主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * master table[命令合成]<br/>
	 * 删除主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 主表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception;
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * [命令合成-子流程]
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 *
	 ******************************************************************************************************************/

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 创建分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 修改分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * partition table[命令合成-]<br/>
	 * 删除分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 分区表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * partition table[命令合成-子流程]<br/>
	 * 修改分区表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Column meta)
	 * boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger)
	 * boolean alter(DataRuntime runtime, Column meta)
	 * boolean drop(DataRuntime runtime, Column meta)
	 * boolean rename(DataRuntime runtime, Column origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column)
	 * List<Run> buildRenameRun(DataRuntime runtime, Column column)
	 * [命令合成-子流程]
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Column column)
	 * String alterColumnKeyword(DataRuntime runtime)
	 * StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column column)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column)
	 * StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column, String type, int ignorePrecision, boolean ignoreScale)
	 * int ignorePrecision(DataRuntime runtime, Column column)
	 * int ignoreScale(DataRuntime runtime, Column column)
	 * Boolean checkIgnorePrecision(DataRuntime runtime, String datatype)
	 * int checkIgnoreScale(DataRuntime runtime, String datatype)
	 * StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action)
	 * StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * column[调用入口]<br/>
	 * 添加列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Column meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param trigger 修改异常时，是否触发监听器
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception {
		return super.alter(runtime, table, meta, trigger);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Column meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Column meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * column[调用入口]<br/>
	 * 重命名列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 列
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Column origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * column[命令合成]<br/>
	 * 添加列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildAddRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildAlterRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成]<br/>
	 * 删除列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildDropRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildRenameRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta, boolean  slice) throws Exception {
		return super.buildChangeTypeRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	@Override
	public String alterColumnKeyword(DataRuntime runtime) {
		return super.alterColumnKeyword(runtime);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列引导<br/>
	 * alter table sso_user [add column] type_code int
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param meta 列
	 * @return String
	 */
	@Override
	public StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.addColumnGuide(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 删除列引导<br/>
	 * alter table sso_user [drop column] type_code
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param meta 列
	 * @return String
	 */
	@Override
	public StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.dropColumnGuide(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildChangeDefaultRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildChangeNullableRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildChangeCommentRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildAppendCommentRun(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		return super.buildDropAutoIncrement(runtime, meta, slice);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列，依次拼接下面几个属性注意不同数据库可能顺序不一样
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action) {
		return super.define(runtime, builder, meta, action);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		return super.checkColumnExists(runtime, builder, exists);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.type(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param type 数据类型(已经过转换)
	 * @param ignoreLength 是否忽略长度
	 * @param ignorePrecision 是否忽略有效位数
	 * @param ignoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale) {
		return super.type(runtime, builder, meta, type, ignoreLength, ignorePrecision, ignoreScale);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:聚合类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.aggregation(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:非空
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action) {
		return super.nullable(runtime, builder, meta, action);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.charset(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:默认值
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.defaultValue(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:定义列的主键标识(注意不要跟表定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.primary(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:唯一索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder unique(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.unique(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:递增列,需要通过serial实现递增的在type(DataRuntime runtime, StringBuilder builder, Column meta)中实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.increment(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:更新行事件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.onupdate(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:位置
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.position(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta) {
		return super.comment(runtime, builder, meta);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Tag meta)
	 * boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger)
	 * boolean alter(DataRuntime runtime, Tag meta)
	 * boolean drop(DataRuntime runtime, Tag meta)
	 * boolean rename(DataRuntime runtime, Tag origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Tag meta, boolean slice)
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag meta, boolean slice)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta)
	 * StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Tag meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @param trigger 修改异常时，是否触发监听器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception {
		return super.alter(runtime, table, meta, trigger);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Tag meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Tag meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * tag[调用入口]<br/>
	 * 重命名标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原标签
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * tag[命令合成]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Tag meta, boolean  slice) throws Exception {
		return super.buildAddRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildAlterRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildDropRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildRenameRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildChangeDefaultRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildChangeNullableRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildChangeCommentRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		return super.buildChangeTypeRun(runtime, meta, slice);
	}

	/**
	 * tag[命令合成]<br/>
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		return super.checkTagExists(runtime, builder, exists);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, PrimaryKey meta)
	 * boolean alter(DataRuntime runtime, PrimaryKey meta)
	 * boolean alter(DataRuntime runtime, Table table, PrimaryKey meta)
	 * boolean drop(DataRuntime runtime, PrimaryKey meta)
	 * boolean rename(DataRuntime runtime, PrimaryKey origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary)
	 ******************************************************************************************************************/

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception {
		return super.alter(runtime, table, origin, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 主键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * primary[命令合成]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta, boolean slice) throws Exception {
		return super.buildAddRun(runtime, meta, slice);
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta, boolean slice) throws Exception {
		return super.buildAlterRun(runtime, origin, meta, slice);
	}

	/**
	 * primary[命令合成]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta, boolean slice) throws Exception {
		return super.buildDropRun(runtime, meta, slice);
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, ForeignKey meta)
	 * boolean alter(DataRuntime runtime, ForeignKey meta)
	 * boolean alter(DataRuntime runtime, Table table, ForeignKey meta)
	 * boolean drop(DataRuntime runtime, ForeignKey meta)
	 * boolean rename(DataRuntime runtime, ForeignKey origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta)
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.buildAddRun(runtime, meta);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键
	 * @param meta 外键
	 * @return List
	 */

	/**
	 * 添加外键
	 * @param meta 外键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Index meta)
	 * boolean alter(DataRuntime runtime, Index meta)
	 * boolean alter(DataRuntime runtime, Table table, Index meta)
	 * boolean drop(DataRuntime runtime, Index meta)
	 * boolean rename(DataRuntime runtime, Index origin, String name)
	 * [命令合成]
	 * List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Index meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Index meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Index meta)
	 * [命令合成-子流程]
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta)
	 ******************************************************************************************************************/

	/**
	 * index[调用入口]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Index meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Index meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Index meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 重命名索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 索引
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Index origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * index[命令合成]<br/>
	 * 创建表过程添加索引,表创建完成后添加索引,于表内索引index(DataRuntime, StringBuilder, Table)二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	@Override
	public List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAppendIndexRun(runtime, meta);
	}

	/**
	 * index[命令合成]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Index meta) throws Exception {
		return super.buildAddRun(runtime, meta);
	}

	/**
	 * index[命令合成]<br/>
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Index meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * index[命令合成]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Index meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * index[命令合成]<br/>
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Index meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta) {
		return super.type(runtime, builder, meta);
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder property(DataRuntime runtime, StringBuilder builder, Index meta) {
		return super.property(runtime, builder, meta);
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta) {
		return super.comment(runtime, builder, meta);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Constraint meta)
	 * boolean alter(DataRuntime runtime, Constraint meta)
	 * boolean alter(DataRuntime runtime, Table table, Constraint meta)
	 * boolean drop(DataRuntime runtime, Constraint meta)
	 * boolean rename(DataRuntime runtime, Constraint origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Constraint meta)
	 ******************************************************************************************************************/

	/**
	 * constraint[调用入口]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Constraint meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Constraint meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Constraint meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 重命名约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 约束
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Constraint meta) throws Exception {
		return super.buildAddRun(runtime, meta);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Constraint meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * trigger[调用入口]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Trigger meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Trigger meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Trigger meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 重命名触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 触发器
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Trigger meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Trigger meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Trigger meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Trigger meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta) {
		return super.each(runtime, builder, meta);
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Procedure meta)
	 * boolean alter(DataRuntime runtime, Procedure meta)
	 * boolean drop(DataRuntime runtime, Procedure meta)
	 * boolean rename(DataRuntime runtime, Procedure origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Procedure meta)
	 * [命令合成-子流程]
	 * StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter)
	 ******************************************************************************************************************/

	/**
	 * procedure[调用入口]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Procedure meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Procedure meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Procedure meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 重命名存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 存储过程
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	@Override
	public StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter) {
		return super.parameter(runtime, builder, parameter);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Function meta)
	 * boolean alter(DataRuntime runtime, Function meta)
	 * boolean drop(DataRuntime runtime, Function meta)
	 * boolean rename(DataRuntime runtime, Function origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Function function)
	 * List<Run> buildAlterRun(DataRuntime runtime, Function function)
	 * List<Run> buildDropRun(DataRuntime runtime, Function function)
	 * List<Run> buildRenameRun(DataRuntime runtime, Function function)
	 ******************************************************************************************************************/

	/**
	 * function[调用入口]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Function meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * function[调用入口]<br/>
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Function meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * function[调用入口]<br/>
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Function meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * function[调用入口]<br/>
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Function origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * function[命令合成]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Sequence meta)
	 * boolean alter(DataRuntime runtime, Sequence meta)
	 * boolean drop(DataRuntime runtime, Sequence meta)
	 * boolean rename(DataRuntime runtime, Sequence origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildAlterRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildDropRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildRenameRun(DataRuntime runtime, Sequence sequence)
	 ******************************************************************************************************************/

	/**
	 * sequence[调用入口]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Sequence meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * sequence[调用入口]<br/>
	 * 修改序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Sequence meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * sequence[调用入口]<br/>
	 * 删除序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Sequence meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * sequence[调用入口]<br/>
	 * 重命名序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 序列
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Sequence origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 删除序列
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}
	/* *****************************************************************************************************************
	 *
	 * 														JDBC
	 *
	 *  ***************************************************************************************************************/

	@Override
	public <T extends Metadata> void checkSchema(DataRuntime runtime, DataSource datasource, T meta) {
		super.checkSchema(runtime, datasource,meta);
	}

	@Override
	public <T extends Metadata> void checkSchema(DataRuntime runtime, Connection con, T meta) {
		super.checkSchema(runtime, con, meta);
	}
    /**
     * 根据运行环境识别 catalog与schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Metadata
     * @param <T> Metadata
     */
	@Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {
        super.checkSchema(runtime, meta);
    }

	/**
	 * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta Metadata
	 * @param catalog catalog
	 * @param schema schema
     * @param overrideMeta 如果meta中有值，是否覆盖
     * @param overrideRuntime 如果runtime中有值，是否覆盖，注意结果集中可能跨多个schema，所以一般不要覆盖runtime,从con获取的可以覆盖ResultSet中获取的不要覆盖
	 * @param <T> Metadata
	 */
	@Override
    public <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta) {
        super.correctSchemaFromJDBC(runtime, meta, catalog, schema, overrideRuntime, overrideMeta);
    }

	/**
	 * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta Metadata
	 * @param catalog catalog
	 * @param schema schema
	 * @param <T> Metadata
	 */
	@Override
	public <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema) {
		correctSchemaFromJDBC(runtime, meta, catalog, schema, false, true);
	}

	/**
	 * 在调用jdbc接口前处理业务中的catalog,schema,部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
	 * @param catalog catalog
	 * @param schema schema
	 * @return String[]
	 */
	@Override
	public String[] correctSchemaFromJDBC(String catalog, String schema) {
		return super.correctSchemaFromJDBC(catalog, schema);
	}

	public String insertHead(ConfigStore configs) {
		return super.insertHead(configs);
	}
	public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return super.insertFoot(configs, columns);
	}

	@Override
	public String concat(DataRuntime runtime, String ... args) {
		return super.concat(runtime, args);
	}

	/**
	 * 内置函数 多种数据库兼容时需要
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
		return super.value(runtime, column, value);
	}
	/* *****************************************************************************************************************
	 *
	 * 														具体数据库
	 *
	 *  ***************************************************************************************************************/

}
