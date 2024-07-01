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



package org.anyline.data.adapter;

import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.run.TextRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.NotSupportException;
import org.anyline.metadata.*;
import org.anyline.metadata.adapter.*;
import org.anyline.metadata.differ.*;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * DriverAdapter主要用来构造和执行不同数据库的命令,一般会分成3步,以insert为例<br/>
 * 1.insert[调用入口]<br/>提供为上一步调用的方法,方法内部再调用[命令合成]<br/>生成具体命令，最后调用[命令执行]执行命令<br/>
 * 2.insert[命令合成]<br/>根据不同的数据库生成具体的insert命令<br/>
 * 3.insert[命令执行]执行[命令合成]<br/>生成的命令<br/>
 * 其中[调用入口]<br/>,[命令执行]大部分通用，重点是[命令合成]<br/>需要由每个数据库的适配器各自生成<br/>
 * [命令执行]过程注意数据库是否支持占位符，是否支持返回自增值，是否支持批量量插入<br/>
 * 以上3步在子类中要全部实现，如果不实现，需要输出日志或调用super方法(用于异常堆栈输出)<br/>
 */
public interface DriverAdapter {
	Logger log = LoggerFactory.getLogger(DriverAdapter.class);

	// 内置VALUE
	 enum SQL_BUILD_IN_VALUE{
		CURRENT_DATETIME("CURRENT_DATETIME","当前日期时间"),
		CURRENT_DATE("CURRENT_DATE","当前日期"),
		CURRENT_TIME("CURRENT_TIME","当前时间"),
		CURRENT_TIMESTAMP("CURRENT_TIMESTAMP","当前时间戳");
		private final String code;
		private final String name;
		SQL_BUILD_IN_VALUE(String code, String name) {
			this.code = code;
			this.name = name;
		}
		String getCode() {
			return code;
		}
		String getName() {
			return name;
		}
	}

	/**
	 * 数据库类型
	 * @return DatabaseType
	 */
	DatabaseType type();
	default LinkedHashMap<String, TypeMetadata> types() {
		LinkedHashMap<String, TypeMetadata> types = new LinkedHashMap<>();
		for(TypeMetadata type:alias().values()) {
			types.put(type.getName().toUpperCase(), type);
		}
		return types;
	}

	/**
	 * 数据类型别名
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, TypeMetadata> alias();
	void setWorker(DriverWorker worker);
	DriverWorker getWorker();
	boolean supportCatalog();
	boolean supportSchema();
	void setListener(DDListener listener);
	DDListener getDDListener();
	void setListener(DMListener listener);
	DMListener getDMListener();
	void setGenerator(PrimaryGenerator generator);
	void setDelimiter(String delimiter);

	/**
	 * 根据catalog+schema+name 比较,过程中需要检测是否支持catalog,schema不支持的不判断
	 * @param m1 Metadata
	 * @param m2 Metadata
	 * @return boolean
	 */
	default boolean equals(Metadata m1, Metadata m2) {
		String c1 = null;
		String c2 = null;
		String s1 = null;
		String s2 = null;
		String n1 = null;
		String n2 = null;
		if(null != m1) {
			if(null == m2) {
				return false;
			}
			c1 = m1.getCatalogName();
			s1 = m1.getSchemaName();
			n1 = m1.getName();
		}
		if(null != m2) {
			if(null == m1) {
				return false;
			}
			c2 = m2.getCatalogName();
			s2 = m2.getSchemaName();
			n2 = m2.getName();
		}
		if(supportCatalog()) {
			if(!BasicUtil.equals(c1, c2, true)) {
				return false;
			}
		}
		if(supportSchema()) {
			if(!BasicUtil.equals(s1, s2, true)) {
				return false;
			}
		}
		return BasicUtil.equals(n1, n2, true);
	}
	default boolean empty(Metadata meta) {
		if(null == meta) {
			return true;
		}
		if(BasicUtil.isEmpty(meta.getName())) {
			return true;
		}
		return false;
	}
	default boolean empty(String meta) {
		return BasicUtil.isEmpty(meta);
	}
	default boolean equals(Catalog c1, Catalog c2) {
		if(!supportCatalog()) {
			//如果数据库不支持直接返回true
			return true;
		}
		String n1 = null;
		String n2 = null;
		if(null != c1) {
			if(null == c2) {
				return false;
			}
			n1 = c1.getName();
		}
		if(null != c2) {
			if(null == c1) {
				return false;
			}
			n2 = c2.getName();
		}
		return BasicUtil.equals(n1, n2, true);
	}
	default boolean equals(Schema s1, Schema s2) {
		//如果数据库不支持直接返回true
		if(!supportCatalog()) {
			return true;
		}
		String n1 = null;
		String n2 = null;
		if(null != s1) {
			if(null == s2) {
				return false;
			}
			n1 = s1.getName();
		}
		if(null != s2) {
			if(null == s1) {
				return false;
			}
			n2 = s2.getName();
		}
		return BasicUtil.equals(n1, n2, true);
	}
	/**
	 * 注册数据类型别名(包含对应的标准类型、length/precision/scale等配置)
	 * @param alias 数据类型别名
	 * @return Config
	 */
	TypeMetadata.Config reg(TypeMetadataAlias alias);
	/**
	 * 注册数据类型配置<br/>
	 * 要从配置项中取出每个属性检测合并,不要整个覆盖<br/>
	 * 数据类型 与 数据类型名称 的区别:如ORACLE_FLOAT,FLOAT 这两个对象的name都是float所以会相互覆盖
	 * @param type 数据类型名称
	 * @param config 配置项
	 * @return Config
	 */
	TypeMetadata.Config reg(String type, TypeMetadata.Config config);
	/**
	 * 注册数据类型配置<br/>
	 * 要从配置项中取出每个属性检测合并,不要整个覆盖<br/>
	 * 数据类型 与 数据类型名称 的区别:如ORACLE_FLOAT,FLOAT 这两个对象的name都是float所以会相互覆盖
	 * @param type 数据类型
	 * @param config 配置项
	 * @return Config
	 */
	TypeMetadata.Config reg(TypeMetadata type, TypeMetadata.Config config);
	/**
	 * 验证运行环境与当前适配器是否匹配<br/>
	 * 默认不连接只根据连接参数<br/>
	 * 只有同一个库区分不同版本(如mmsql2000/mssql2005)或不同模式(如kingbase的oracle/pg模式)时才需要单独实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
	 * @return boolean
	 */
	default boolean match(DataRuntime runtime, boolean compensate) {
		if(BasicUtil.isNotEmpty(runtime.getAdapterKey())) {
			return matchByAdapter(runtime);
		}
		String feature = runtime.getFeature();//数据源特征中包含上以任何一项都可以通过
		//获取特征时会重新解析 adapter参数,因为有些数据源是通过DataSource对象注册的，这时需要打开连接后才能拿到url
		if(BasicUtil.isNotEmpty(runtime.getAdapterKey())) {
			return matchByAdapter(runtime);
		}
		List<String> keywords = type().keywords(); //关键字+jdbc-url前缀+驱动类
		return match(feature, keywords, compensate);
	}
	default boolean matchByAdapter(DataRuntime runtime) {
		String config_adapter_key = runtime.getAdapterKey();
		if(BasicUtil.isNotEmpty(config_adapter_key)) {
			String type = type().name();
			//如果明确指定了adapter 不考虑其他特征
			boolean result = false;
			if(config_adapter_key.equalsIgnoreCase(type)) {
				result = true;
			}
			if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
				log.info("[adapter match][result:{}][config adapter:{}][match adapter:{}]", result, config_adapter_key, type);
			}
			return result;
		}
		return false;
	}
	/**
	 *
	 * @param feature 当前运行环境特征
	 * @param keywords 关键字+jdbc-url前缀+驱动类
	 * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
	 * @return 数据源特征中包含上以任何一项都可以通过
	 */
	default boolean match(String feature, List<String> keywords, boolean compensate) {
		if(null == feature) {
			return false;
		}
		feature = feature.toLowerCase();
		if(null != keywords) {
			for (String k:keywords) {
				if(BasicUtil.isEmpty(k)) {
					if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
						log.info("[adapter match][result:{}][feature:{}][key:{}][match adapter:{}]", false, feature, k, this.getClass());
					}
					continue;
				}
				if(feature.contains(k)) {
					if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
						log.info("[adapter match][result:{}][feature:{}][key:{}][match adapter:{}]", true, feature, k, this.getClass());
					}
					return true;
				}
			}
		}
		return false;
	}

	String TAB 		= "\t"		;
	String BR 		= "\n"		;
	String BR_TAB 	= "\n\t"	;

	/**
	 * 界定符(分隔符)
	 * @return String
	 */
	String getDelimiterFr();
	String getDelimiterTo();

	/**
	 * 对应的兼容模式，有些数据库会兼容oracle或pg,需要分别提供两个JDBCAdapter或者直接依赖oracle/pg的adapter
	 * 参考DefaultJDBCAdapterUtil定位adapter的方法
	 * @return DatabaseType
	 */
	DatabaseType compatible();

	/**
	 * 转换成相应数据库类型<br/>
	 * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型<br/>
	 * 同时解析长度、有效位数、精度<br/>
	 * 如有些数据库中用bigint有些数据库中有long
	 * @param meta 列
	 * @return 具体数据库中对应的数据类型
	 */
	TypeMetadata typeMetadata(DataRuntime runtime, Column meta);

	/**
	 * 转换成相应数据库类型<br/>
	 * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型，如有些数据库中用bigint有些数据库中有long
	 * @param type 编码时输入的类型(通常是java类)
	 * @return 具体数据库中对应的数据类型
	 */
	TypeMetadata typeMetadata(DataRuntime runtime, String type);
	/**
	 * 写入数据库前 类型转换
	 * @param supports 写入的原始类型 class ColumnType StringColumnType
	 * @param writer DataWriter
	 */
	default void reg(Object[] supports, DataWriter writer) {
		SystemDataWriterFactory.reg(type(), supports, writer);
	}

	/**
	 * 写入数据库时 类型转换 写入的原始类型需要writer中实现supports
	 * @param writer DataWriter
	 */
	default void reg(DataWriter writer) {
		SystemDataWriterFactory.reg(type(), null, writer);
	}

	/**
	 * 读取数据库入 类型转换
	 * @param supports 读取的原始类型 class ColumnType StringColumnType
	 * @param reader DataReader
	 */
	default void reg(Object[] supports, DataReader reader) {
		SystemDataReaderFactory.reg(type(), supports, reader);
	}

	/**
	 * 读取数据库入 类型转换 读取的原始类型需要reader中实现supports
	 * @param reader DataReader
	 */
	default void reg(DataReader reader) {
		SystemDataReaderFactory.reg(type(), null, reader);
	}

	/**
	 * 根据读出的数据类型 定位DataReader
	 * @param type class ColumnType StringColumnType
	 * @return DataReader
	 */
	default DataReader reader(Object type) {
		DataReader reader = DataReaderFactory.reader(type(), type);
		if(null == reader) {
			reader = SystemDataReaderFactory.reader(type(), type);
		}
		if(null == reader) {
			reader = DataReaderFactory.reader(DatabaseType.NONE, type);
		}
		if(null == reader) {
			reader = SystemDataReaderFactory.reader(DatabaseType.NONE, type);
		}
		return reader;
	}

	/**
	 * 根据写入的数据类型 定位DataWriter,只根据输入类型，输出类型在writer中判断
	 * @param type class(String.class) TypeMetadata,TypeMetadata.CATEGORY, StringColumnType("VARCHAR2")
	 * @return DataWriter
	 */
	default DataWriter writer(Object type) {
		DataWriter writer = DataWriterFactory.writer(type(), type);
		if(null == writer) {
			writer = SystemDataWriterFactory.writer(type(), type);
		}
		if(null == writer) {
			writer = DataWriterFactory.writer(DatabaseType.NONE, type);
		}
		if(null == writer) {
			writer = SystemDataWriterFactory.writer(DatabaseType.NONE, type);
		}
		return writer;
	}
	String name(Type type);
	default List<String> names(List<Type> types) {
		List<String> list = new ArrayList<>();
		for(Type type:types) {
			String name = name(type);
			if(null != name) {
				list.add(name);
			}
		}
		return list;
	}

	default void in(DataRuntime runtime, StringBuilder builder, String column, List<String> list) {
		if(!list.isEmpty()) {
			builder.append(" AND ").append(column);
			if(list.size() == 1) {
				builder.append(" = '").append(objectName(runtime, list.get(0))).append("'");
			}else{
				boolean first = true;
				builder.append(" IN(");
				for(String item:list) {
					if(!first) {
						builder.append(", ");
					}
					builder.append("'").append(objectName(runtime, item)).append("'");
					first = false;
				}
				builder.append(")");
			}
		}
	}
	/**
	 * 根据差异生成SQL
	 * @param differ differ 需要保证表中有列信息
	 * @return sqls
	 */
	default List<Run> ddls(DataRuntime runtime, String random, MetadataDiffer differ) {
		List<Run> list = new ArrayList<>();
		if(differ instanceof TablesDiffer) {
			TablesDiffer df = (TablesDiffer) differ;
			LinkedHashMap<String, Table> adds = df.getAdds();
			LinkedHashMap<String, Table> drops = df.getDrops();
			LinkedHashMap<String, Table> updates = df.getUpdates();//只统计哪些表需要修改
			LinkedHashMap<String, TableDiffer> diffs = df.getDiffers();//标记具体需要修改的内容
			//添加表
			for(Table add:adds.values()) {
				try {
					list.addAll(buildCreateRun(runtime, add));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			//修改表
			for(TableDiffer dif:diffs.values()) {
				try {
					Table dest = dif.getDest();
					Table origin = dif.getOrigin();
					Table update = origin.clone();
					if(null != update) {
						update.setUpdate(dest, false, false);
					}
					ColumnsDiffer columns_dif = dif.getColumnsDiffer();
					LinkedHashMap<String, Column> columns_adds = columns_dif.getAdds();
					LinkedHashMap<String, Column> columns_updates = columns_dif.getUpdates();
					LinkedHashMap<String, Column> columns_drops = columns_dif.getDrops();
					LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
					for(String key:columns_adds.keySet()) {
						Column column = columns_adds.get(key);
						column.setAction(ACTION.DDL.COLUMN_ADD);
						columns.put(key, column);
					}
					for(String key:columns_updates.keySet()) {
						Column column = columns_updates.get(key);
						column.setAction(ACTION.DDL.COLUMN_ALTER);
						columns.put(key, column);
					}
					for(String key:columns_drops.keySet()) {
						Column column = columns_drops.get(key);
						column.setAction(ACTION.DDL.COLUMN_DROP);
						columns.put(key, column);
					}
					update.setColumns(columns);
					list.addAll(buildAlterRun(runtime, update));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			//删除表
			for(Table drop:drops.values()) {
				try {
					list.addAll(buildDropRun(runtime, drop));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
		}else if(differ instanceof ViewsDiffer) {
			ViewsDiffer df = (ViewsDiffer) differ;
			LinkedHashMap<String, View> adds = df.getAdds();
			LinkedHashMap<String, View> drops = df.getDrops();
			LinkedHashMap<String, View> updates = df.getUpdates();
			for(View add:adds.values()) {
				try {
					list.addAll(buildCreateRun(runtime, add));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(View update:updates.values()) {
				try {
					list.addAll(buildAlterRun(runtime, update));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(View drop:drops.values()) {
				try {
					list.addAll(buildDropRun(runtime, drop));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
		}else if(differ instanceof TableDiffer) {
			TableDiffer df = (TableDiffer) differ;
			ColumnsDiffer columnsDiffer = df.getColumnsDiffer();
			IndexesDiffer indexesDiffer = df.getIndexesDiffer();
			list.addAll(ddls(runtime, random, columnsDiffer));
			list.addAll(ddls(runtime, random, indexesDiffer));
		}else if(differ instanceof ColumnsDiffer) {
			boolean slice = slice();
			ColumnsDiffer df = (ColumnsDiffer) differ;
			LinkedHashMap<String, Column> adds = df.getAdds();
			LinkedHashMap<String, Column> drops = df.getDrops();
			LinkedHashMap<String, Column> updates = df.getUpdates();
			for(Column add:adds.values()) {
				try {
					list.addAll(buildAddRun(runtime, add, slice));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Column update:updates.values()) {
				try {
					list.addAll(buildAlterRun(runtime, update, slice));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Column drop:drops.values()) {
				try {
					list.addAll(buildDropRun(runtime, drop, slice));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
		}else if(differ instanceof IndexesDiffer) {
			IndexesDiffer df = (IndexesDiffer) differ;
			LinkedHashMap<String, Index> adds = df.getAdds();
			LinkedHashMap<String, Index> drops = df.getDrops();
			LinkedHashMap<String, Index> updates = df.getUpdates();
			for(Index add:adds.values()) {
				try {
					list.addAll(buildAddRun(runtime, add));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Index update:updates.values()) {
				try {
					list.addAll(buildAlterRun(runtime, update));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Index drop:drops.values()) {
				try {
					list.addAll(buildDropRun(runtime, drop));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
		}else if(differ instanceof FunctionsDiffer) {
			FunctionsDiffer df = (FunctionsDiffer) differ;
			List<Function> adds = df.getAdds();
			List<Function> drops = df.getDrops();
			List<Function> updates = df.getUpdates();
			for(Function add:adds) {
				try {
					list.addAll(buildCreateRun(runtime, add));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Function update:updates) {
				try {
					list.addAll(buildAlterRun(runtime, update));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Function drop:drops) {
				try {
					list.addAll(buildDropRun(runtime, drop));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
		}else if(differ instanceof ProceduresDiffer) {
			ProceduresDiffer df = (ProceduresDiffer) differ;
			LinkedHashMap<String, Procedure> adds = df.getAdds();
			LinkedHashMap<String, Procedure> drops = df.getDrops();
			LinkedHashMap<String, Procedure> updates = df.getUpdates();
			for(Procedure add:adds.values()) {
				try {
					list.addAll(buildCreateRun(runtime, add));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Procedure update:updates.values()) {
				try {
					list.addAll(buildAlterRun(runtime, update));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
			for(Procedure drop:drops.values()) {
				try {
					list.addAll(buildDropRun(runtime, drop));
				}catch (Exception e) {
					log.error("build ddl exception:", e);
				}
			}
		}
		return list;
	}
	/**
	 * 根据差异生成SQL
	 * @param differs differs
	 * @return sqls
	 */
	default List<Run> ddls(DataRuntime runtime, String random, List<MetadataDiffer> differs) {
		List<Run> list = new ArrayList<>();
		for(MetadataDiffer differ:differs) {
			list.addAll(ddls(runtime, random, differ));
		}
		return list;
	}
	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * UPDATE			: 更新
	 * SAVE				: 插入或更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 * COMMON			：其他通用
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													INSERT
	 ******************************************************************************************************************/

	/**
	 * insert [调用入口]<br/>
	 * 执行前根据主键生成器补充主键值,执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 需要插入入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns) {
		return insert(runtime, random, batch, dest, data, null, columns);
	}
	default long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, String ... columns) {
		return insert(runtime, random, batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, int batch, Object data, String ... columns) {
		return insert(runtime, random, batch, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
		return insert(runtime, random, 0, dest, data, columns);
	}
	default long insert(DataRuntime runtime, String random, Table dest, Object data, String ... columns) {
		return insert(runtime, random, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, Object data, String ... columns) {
		return insert(runtime, random, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2list(columns));
	}

	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}
	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, List<String> columns) {
		return insert(runtime, random, batch, dest, data, null, columns);
	}
	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, String ... columns) {
		return insert(runtime, random, batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
		return insert(runtime, random, 0, dest, data, columns);
	}
	default long insert(DataRuntime runtime, String random, String dest, Object data, String ... columns) {
		return insert(runtime, random, dest, data, BeanUtil.array2list(columns));
	}

	/**
	 * insert into table select * from table
	 * 与query参数一致
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 插入表
	 * @param prepare 查询表
	 * @param configs 查询条件及相关配置
	 * @param obj 查询条件
	 * @param conditions 查询条件
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);

	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param prepare 查询
	 * @param configs 过滤条件及相关配置
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 需要插入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns);
	default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, List<String> columns) {
		return buildInsertRun(runtime, batch, dest, obj, null, columns);
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, String ... columns) {
		return buildInsertRun(runtime, batch, dest, obj, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore confgis, String ... columns) {
		return buildInsertRun(runtime, batch, dest, obj, confgis, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, Object obj, String ... columns) {
		return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, null), obj, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, Object obj, ConfigStore configs, String ... columns) {
		return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, configs), obj, configs, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, ConfigStore configs, Object obj, String ... columns) {
		return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, configs), obj, configs, BeanUtil.array2list(columns));
	}

	default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns) {
		return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, columns);
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, List<String> columns) {
		return buildInsertRun(runtime, batch, dest, obj, null, columns);
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, String ... columns) {
		return buildInsertRun(runtime, batch, dest, obj, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore confgis, String ... columns) {
		return buildInsertRun(runtime, batch, dest, obj, confgis, BeanUtil.array2list(columns));
	}
	
	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns);
	default void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, LinkedHashMap<String, Column> columns) {
		fillInsertContent(runtime, run, dest, list, null, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns);
	default void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, LinkedHashMap<String, Column> columns) {
		fillInsertContent(runtime, run, dest, set, null, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	default void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		fillInsertContent(runtime, run, DataSourceUtil.parseDest(dest, list, configs), list, configs, columns);
	}
	default void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, LinkedHashMap<String, Column> columns) {
		fillInsertContent(runtime, run, dest, list, null, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	default void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		fillInsertContent(runtime, run, DataSourceUtil.parseDest(dest, set, configs), set, configs, columns);
	}
	default void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, LinkedHashMap<String, Column> columns) {
		fillInsertContent(runtime, run, dest, set, null, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 确认需要插入的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data  Entity或DataRow
	 * @param batch  是否批量
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return List
	 */
	LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object data, ConfigStore configs, List<String> columns, boolean batch);
	default LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object data, ConfigStore configs, List<String> columns, boolean batch) {
		return confirmInsertColumns(runtime, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns, batch);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 批量插入数据时,多行数据之间分隔符
	 * @return String
	 */
	String batchInsertSeparator();
	/**
	 * insert [命令合成-子流程]<br/>
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	boolean supportInsertPlaceholder();
	/**
	 * insert [命令合成-子流程]<br/>
	 * 自增主键返回标识
	 * @return String
	 */
	String generatedKey();

	/**
	 * insert [命令执行]
	 * <br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks 需要返回的主键
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);

	/**
	 * 是否支持返回自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param configs configs中也可能禁用返回
	 * @return boolean
	 */
	boolean supportKeyHolder(DataRuntime runtime, ConfigStore configs);

	/**
	 * 自增主键值keys
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param configs configs中也可能禁用返回
	 * @return keys
	 */
	List<String> keyHolders(DataRuntime runtime, ConfigStore configs);
	/* *****************************************************************************************************************
	 * 													UPDATE
	 ******************************************************************************************************************/
	/**
	 * UPDATE [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return 影响行数
	 */
	long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long update(DataRuntime runtime, String random, int batch, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, configs), data, configs, columns);
	}
	default long update(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns) {
		return update(runtime, random, batch, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, int batch, Object data, List<String> columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, null), data, null, columns);
	}
	default long update(DataRuntime runtime, String random, int batch, Object data, ConfigStore configs) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, configs), data, configs);
	}
	default long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return update(runtime, random, batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, int batch, Object data, ConfigStore configs, String ... columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, configs), data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, int batch, Table dest, Object data, String ... columns) {
		return update(runtime, random, batch, dest, data, BeanUtil.array2string(columns));
	}
	default long update(DataRuntime runtime, String random, int batch, Object data, String ... columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2string(columns));
	}
	default long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, 0, dest, data, configs, columns);
	}
	default long update(DataRuntime runtime, String random, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, configs), data, configs, columns);
	}
	default long update(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
		return update(runtime, random, 0, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, Object data, List<String> columns) {
		return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, null), data, null, columns);
	}
	default long update(DataRuntime runtime, String random, Object data, ConfigStore configs) {
		return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, configs), data, configs);
	}
	default long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, String ... columns) {
		return update(runtime, random, 0, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, Object data, ConfigStore configs, String ... columns) {
		return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, configs), data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, Table dest, Object data, String ... columns) {
		return update(runtime, random, 0, dest, data, BeanUtil.array2string(columns));
	}
	default long update(DataRuntime runtime, String random, Object data, String ... columns) {
		return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2string(columns));
	}

	default long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}

	default long update(DataRuntime runtime, String random, int batch, String dest, Object data, List<String> columns) {
		return update(runtime, random, batch, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return update(runtime, random, batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, int batch, String dest, Object data, String ... columns) {
		return update(runtime, random, batch, dest, data, BeanUtil.array2string(columns));
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, 0, dest, data, configs, columns);
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
		return update(runtime, random, 0, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, String ... columns) {
		return update(runtime, random, 0, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, String ... columns) {
		return update(runtime, random, 0, dest, data, BeanUtil.array2string(columns));
	}

	/**
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj Entity或DtaRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildUpdateRun(DataRuntime runtime, int btch, Table dest, Object obj, ConfigStore configs, List<String> columns);
	default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		return buildUpdateRun(runtime, 0, dest, obj, configs, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, List<String> columns) {
		return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, configs), obj, configs, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, List<String> columns) {
		return buildUpdateRun(runtime, dest, obj, null, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, List<String> columns) {
		return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, null), obj, null, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, String ... columns) {
		return buildUpdateRun(runtime, dest, obj, configs, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, String ... columns) {
		return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, configs), obj, configs, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, String ... columns) {
		return buildUpdateRun(runtime, dest, obj, null, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, String ... columns) {
		return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, null), obj, null, BeanUtil.array2list(columns));
	}
	Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns);

	Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns);

	Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns);

	default Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns) {
		return buildUpdateRun(runtime, batch, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns) {
		return buildUpdateRun(runtime, 0, dest, obj, configs, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, List<String> columns) {
		return buildUpdateRun(runtime, dest, obj, null, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, String ... columns) {
		return buildUpdateRun(runtime, dest, obj, configs, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, String ... columns) {
		return buildUpdateRun(runtime, dest, obj, null, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return buildUpdateRunFromEntity(runtime, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, columns);
	}

	default Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns) {
		return buildUpdateRunFromDataRow(runtime, DataSourceUtil.parseDest(dest, row, configs), row, configs, columns);
	}

	default Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns) {
		return buildUpdateRunFromCollection(runtime, batch, DataSourceUtil.parseDest(dest, list, configs), list, configs, columns);
	}

	/**
	 * 确认需要更新的列
	 * @param row DataRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return List
	 */
	LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns);
	LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns);
	default LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns) {
		return confirmUpdateColumns(runtime, DataSourceUtil.parseDest(dest, row, configs), row, configs, columns);
	}
	default LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns) {
		return confirmUpdateColumns(runtime, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, columns);
	}

	/**
	 * update [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run);
	default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run) {
		return update(runtime, random, DataSourceUtil.parseDest(dest, data, configs), data, configs, run);
	}

	/**
	 * save [调用入口]<br/>
	 * <br/>
	 * 根据是否有主键值确认insert | update<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return 影响行数
	 */
	long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long save(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
		return save(runtime, random, dest, data, null, columns);
	}

	default long save(DataRuntime runtime, String random, Object data, List<String> columns) {
		return save(runtime, random, DataSourceUtil.parseDest(null, data, null), data, columns);
	}
	default long save(DataRuntime runtime, String random, Table dest, Object data, String ... columns) {
		return save(runtime, random, dest, data, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, Object data, String ... columns) {
		return save(runtime, random, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2list(columns));
	}

	default long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
		return save(runtime, random, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
		return save(runtime, random, dest, data, null, columns);
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, String ... columns) {
		return save(runtime, random, dest, data, BeanUtil.array2list(columns));
	}
	/* *****************************************************************************************************************
	 * 													QUERY
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
	DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * query procedure [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @return DataSet
	 */
	DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi);

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
	<T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) ;
	/**
	 * query [调用入口]<br/>
	 * <br/>
	 * 对性能有要求的场景调用，返回java原生map集合,结果中不包含元数据信息
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return maps 返回map集合
	 */
	List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * select[命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * 解析文本中的占位符
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run run
	 */
	void parseText(DataRuntime runtime, TextRun run);

	/**
	 * 是否支持SQL变量占位符扩展格式 :VAR,图数据库不要支持会与表冲突
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return boolean
	 */
	default boolean supportSqlVarPlaceholderRegexExt(DataRuntime runtime) {
		return true;
	}
	/**
	 * select[命令合成]<br/>
	 * 创建 select sequence 最终可执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 存储过程名称s
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names);
	/**
	 * select[命令合成-子流程] <br/>
	 * 填充 select 命令内容
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	void fillQueryContent(DataRuntime runtime, StringBuilder builder,  Run run);
	void fillQueryContent(DataRuntime runtime, Run run);

	/**
	 * select[命令合成-子流程] <br/>
	 * 合成最终 select 命令 包含分页 排序
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	String mergeFinalQuery(DataRuntime runtime, Run run);

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
	RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder);

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
	default Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder) throws NotSupportException{
		throw new NotSupportException("不支持");
	}
	/**
	 * select[命令合成-子流程] <br/>
	 * 构造 JSON_CONTAINS 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value
	 */
	default Object createConditionJsonContains(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder) throws NotSupportException{
		throw new NotSupportException("不支持");
	}
	/**
	 * select[命令合成-子流程] <br/>
	 * 构造 JSON_CONTAINS 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value
	 */
	default Object createConditionJsonContainsPath(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder) throws NotSupportException{
		throw new NotSupportException("不支持");
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
	StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder);

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param system 系统表不检测列属性
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return DataSet
	 */
	DataSet select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run);
	default DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run) {
		return select(runtime, random, system, new Table(table), configs, run);
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return maps
	 */
	List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run);
	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return map
	 */
	Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) ;

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next 是否查下一个序列值
	 * @param names 存储过程名称s
	 * @return DataRow 保存序列查询结果 以存储过程name作为key
	 */
	DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names);

	/**
	 * select [命令执行-子流程]<br/>
	 * JDBC执行完成后的结果处理
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param list JDBC执行返回的结果集
	 * @return  maps
	 */
	List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list);
	/* *****************************************************************************************************************
	 * 													COUNT
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
	long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * count [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return long
	 */
	long count(DataRuntime runtime, String random, Run run);

 
	/**
	 * 合成最终 select count 命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	String mergeFinalTotal(DataRuntime runtime, Run run);

	/* *****************************************************************************************************************
	 * 													EXISTS
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
	boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * exists [命令合成]<br/>
	 * 合成最终 exists 命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	String mergeFinalExists(DataRuntime runtime, Run run);

	/* *****************************************************************************************************************
	 * 													EXECUTE
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
	long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) ;

	long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values) ;
	long execute(DataRuntime runtime, String random, int batch, int vol, ConfigStore configs, RunPrepare prepare, Collection<Object> values) ;
	/**
	 * procedure [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @param random  random
	 * @return 影响行数
	 */
	boolean execute(DataRuntime runtime, String random, Procedure procedure) ;
	/**
	 * 创建执行SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	void fillExecuteContent(DataRuntime runtime, Run run);

	/**
	 * execute [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) ;
	/* *****************************************************************************************************************
	 * 													DELETE
	 ******************************************************************************************************************/

	/**
	 * delete [调用入口]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param column 列
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	<T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, Collection<T> values);
	default <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String column, Collection<T> values) {
		return deletes(runtime, random, batch, table, null, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, Table table, String column, Collection<T> values) {
		return deletes(runtime, random, 0, table, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, Table table, ConfigStore configs, String column, Collection<T> values) {
		return deletes(runtime, random, 0, table, configs, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String column, T ... values) {
		return deletes(runtime, random, batch, table, column, BeanUtil.array2list(values));
	}
	default <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, T ... values) {
		return deletes(runtime, random, batch, table, configs, column, BeanUtil.array2list(values));
	}
	default <T> long deletes(DataRuntime runtime, String random, Table table, ConfigStore configs, String column, T ... values) {
		return deletes(runtime, random, 0, table, configs, column, BeanUtil.array2list(values));
	}
	default <T> long deletes(DataRuntime runtime, String random, Table table, String column, T ... values) {
		return deletes(runtime, random, 0, table, column, BeanUtil.array2list(values));
	}

	default <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, Collection<T> values) {
		return deletes(runtime, random, batch, new Table(table), configs, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, int batch, String table, String column, Collection<T> values) {
		return deletes(runtime, random, batch, table, null, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, String table, String column, Collection<T> values) {
		return deletes(runtime, random, 0, table, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, String table, ConfigStore configs, String column, Collection<T> values) {
		return deletes(runtime, random, 0, table, configs, column, values);
	}
	default <T> long deletes(DataRuntime runtime, String random, int batch, String table, String column, T ... values) {
		return deletes(runtime, random, batch, table, column, BeanUtil.array2list(values));
	}
	default <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, T ... values) {
		return deletes(runtime, random, batch, table, configs, column, BeanUtil.array2list(values));
	}
	default <T> long deletes(DataRuntime runtime, String random, String table, ConfigStore configs, String column, T ... values) {
		return deletes(runtime, random, 0, table, configs, column, BeanUtil.array2list(values));
	}
	default <T> long deletes(DataRuntime runtime, String random, String table, String column, T ... values) {
		return deletes(runtime, random, 0, table, column, BeanUtil.array2list(values));
	}

	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return 影响行数
	 */
	long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, Object obj, String... columns);
	default long delete(DataRuntime runtime, String random, String table, ConfigStore configs, Object obj, String... columns) {
		return delete(runtime, random, new Table(table), configs, obj, columns);
	}

	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 根据configs和conditions过滤条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions);
	default long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions) {
		return delete(runtime, random, new Table(table), configs, conditions);
	}

	/**
	 * truncate [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return 1表示成功执行
	 */
	long truncate(DataRuntime runtime, String random, Table table);
	default long truncate(DataRuntime runtime, String random, String table) {
		return truncate(runtime, random, new Table(table));
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns);
	default Run buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns) {
		return buildDeleteRun(runtime, new Table(table), configs, obj, columns);
	}

	default Run buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs) {
		return buildDeleteRun(runtime,  table, configs, null, null);
	}
	default Run buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs) {
		return buildDeleteRun(runtime,  new Table(table), configs, null, null);
	}
	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values);
	default Run buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String column, Object values) {
		return buildDeleteRun(runtime, batch, new Table(table), configs, column, values);
	}

	/**
	 * truncate[命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	List<Run> buildTruncateRun(DataRuntime runtime, Table table);
	default List<Run> buildTruncateRun(DataRuntime runtime, String table) {
		return buildTruncateRun(runtime, new Table(table));
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values);
	default Run buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,String column, Object values) {
		return buildDeleteRunFromTable(runtime, batch, new Table(table), configs, column, values);
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns);
	default Run buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns) {
		return buildDeleteRunFromEntity(runtime, new Table(table), configs, obj, columns);
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	void fillDeleteRunContent(DataRuntime runtime, Run run);
	/**
	 *
	 * delete[命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param configs 查询条件及相关设置
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	long delete(DataRuntime runtime, String random, ConfigStore configs, Run run);
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

	/**
	 * 根据运行环境识别 catalog与schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta Metadata
	 * @param <T> Metadata
	 */
	<T extends Metadata> void checkSchema(DataRuntime runtime, T meta);

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
	default <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta) {
		if(supportCatalog()) {
			if (overrideMeta || empty(meta.getCatalog())) {
				meta.setCatalog(catalog);
			}
			if (overrideRuntime || BasicUtil.isEmpty(runtime.getCatalog())) {
				runtime.setCatalog(catalog);;
			}
		}else{
			meta.setCatalog((Catalog) null);
			runtime.setCatalog(null);
		}
		if(supportSchema()) {
			if (overrideMeta || empty(meta.getSchema())) {
				meta.setSchema(schema);
			}
			if (overrideRuntime || BasicUtil.isEmpty(runtime.getSchema())) {
				runtime.setSchema(schema);
			}
		}else{
			meta.setSchema((Schema) null);
			runtime.setSchema(null);
		}
	}

	/**
	 * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta Metadata
	 * @param catalog catalog
	 * @param schema schema
	 * @param <T> Metadata
	 */
	default <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema) {
		correctSchemaFromJDBC(runtime, meta, catalog, schema, false, true);
	}
	/**
	 * 在调用jdbc接口前处理业务中的catalog,schema,部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
	 * @param catalog catalog
	 * @param schema schema
	 * @return String[]
	 */
	default String[] correctSchemaFromJDBC(String catalog, String schema) {
		return new String[]{catalog, schema};
	}

	/**
	 * 根据结果集对象获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param comment 是否需要查询列注释
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String,Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment);
	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/

	/**
	 * database[调用入口]<br/>
	 * 当前数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Database
	 */
	Database database(DataRuntime runtime, String random);
	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	String product(DataRuntime runtime, String random);

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	String version(DataRuntime runtime, String random);

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name);
	List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name);
	default Database database(DataRuntime runtime, String random, String name) {
		List<Database> databases = databases(runtime, random, false, name);
		if(!databases.isEmpty()) {
			return databases.get(0);
		}
		return null;
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库产品说明(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryProductRun(DataRuntime runtime) throws Exception;
	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryVersionRun(DataRuntime runtime) throws Exception;
	/**
	 * database[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name) throws Exception;
	default List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy) throws Exception {
		return buildQueryDatabasesRun(runtime, false, null);
	}
	default List<Run> buildQueryDatabaseRun(DataRuntime runtime, String name) throws Exception {
		return buildQueryDatabasesRun(runtime, false, name);
	}
	default List<Run> buildQueryDatabaseRun(DataRuntime runtime) throws Exception {
		return buildQueryDatabasesRun(runtime, false, null);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @param set 查询结果集
	 * @return product
	 * @throws Exception 异常
	 */
	String product(DataRuntime runtime, int index, boolean create, String product, DataSet set);
	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @return product
	 * @throws Exception 异常
	 */
	String product(DataRuntime runtime, boolean create, String product);
	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @param set 查询结果集
	 * @return version
	 * @throws Exception 异常
	 */
	String version(DataRuntime runtime, int index, boolean create, String version, DataSet set);
	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @return version
	 * @throws Exception 异常
	 */
	String version(DataRuntime runtime, boolean create, String version);
	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception;
	List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @param set 查询结果集
	 * @return database
	 * @throws Exception 异常
	 */
	Database database(DataRuntime runtime, int index, boolean create, Database database, DataSet set) throws Exception;
	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @return database
	 * @throws Exception 异常
	 */
	Database database(DataRuntime runtime, boolean create, Database database) throws Exception;

	/* *****************************************************************************************************************
	 * 													catalog
	 ******************************************************************************************************************/
	/**
	 * catalog[调用入口]<br/>
	 * 当前catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Catalog
	 */
	Catalog catalog(DataRuntime runtime, String random);
	/**
	 * catalog[调用入口]<br/>
	 * 全部catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name);
	List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name);
	default Catalog catalog(DataRuntime runtime, String random, String name) {
		List<Catalog> catalogs = catalogs(runtime, random, false, name);
		if(!catalogs.isEmpty()) {
			return catalogs.get(0);
		}
		return null;
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询当前catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryCatalogRun(DataRuntime runtime, String random) throws Exception;
	/**
	 * catalog[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name) throws Exception;
	default List<Run> buildQueryCatalogsRun(DataRuntime runtime) throws Exception {
		return buildQueryCatalogsRun(runtime, false, null);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @param set 查询结果集
	 * @return catalogs
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, Catalog catalog, Schema schema, DataSet set) throws Exception;
	List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs) throws Exception;

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs) throws Exception;

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 上一步查询结果
	 * @param set 查询结果集
	 * @return Catalog
	 * @throws Exception 异常
	 */
	Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set) throws Exception;
	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 上一步查询结果
	 * @return Catalog
	 * @throws Exception 异常
	 */
	Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog) throws Exception;

	/* *****************************************************************************************************************
	 * 													schema
	 ******************************************************************************************************************/
	/**
	 * schema[调用入口]<br/>
	 * 当前schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return LinkedHashMap
	 */
	Schema schema(DataRuntime runtime, String random);
	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name);

	default LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, String name) {
		return schemas(runtime, random, null, name);
	}
	List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name);
	default List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, String name) {
		return schemas(runtime, random, greedy, null, name);
	}
	default Schema schema(DataRuntime runtime, String random, Catalog catalog, String name) {
		List<Schema> schemas = schemas(runtime, random, false, catalog, name);
		if(!schemas.isEmpty()) {
			return schemas.get(0);
		}
		return null;
	}

	/**
	 * schema[命令合成]<br/>
	 * 查询当前schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQuerySchemaRun(DataRuntime runtime, String random) throws Exception;
	/**
	 * schema[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception;
	default List<Run> buildQuerySchemasRun(DataRuntime runtime, String name) throws Exception {
		return buildQuerySchemasRun(runtime, false, null, name);
	}
	default List<Run> buildQuerySchemasRun(DataRuntime runtime, Catalog catalog) throws Exception {
		return buildQuerySchemasRun(runtime, false, catalog,null);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @param set 查询结果集
	 * @return schemas
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet set) throws Exception;
	List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @return schemas
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Schema> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, Schema> schemas) throws Exception;
	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @return schemas
	 * @throws Exception 异常
	 */
	List<Schema> schemas(DataRuntime runtime, boolean create, List<Schema> schemas) throws Exception;

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQuerySchemaRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schema 上一步查询结果
	 * @param set 查询结果集
	 * @return schema
	 * @throws Exception 异常
	 */
	Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set) throws Exception;
	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schema 上一步查询结果
	 * @return schema
	 * @throws Exception 异常
	 */
	Schema schema(DataRuntime runtime, boolean create, Schema schema) throws Exception;

	/**
	 * 检测name,name中可能包含catalog.schema.name<br/>
	 * 如果有一项或三项，在父类中解析<br/>
	 * 如果只有两项，需要根据不同数据库区分出最前一项是catalog还是schema，如果区分不出来的抛出异常
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表,视图等
	 * @return T
	 * @throws Exception 如果区分不出来的抛出异常
	 */
	<T extends Metadata> T checkName(DataRuntime runtime, String random, T meta) throws Exception;
	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	/**
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> Table
	 */
	<T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs);
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return tables(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return tables(runtime, random, greedy, catalog, schema, pattern, types, structs, configs);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
		return tables(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
		return tables(runtime, random, greedy, catalog, schema, pattern, types, false);
	}
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs);
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return tables(runtime, random, catalog, schema, pattern, types, struct, null);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return tables(runtime, random, catalog, schema, pattern, types, structs, configs);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
		return tables(runtime, random, catalog, schema, pattern, types, struct, null);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
		return tables(runtime, random, catalog, schema, pattern, types, false);
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
	List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception;
	<T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * 查询表创建SQL
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, Table table, boolean init);

	/**
	 * table[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table) throws Exception;

	/**
	 * table[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set);

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Table
	 */
	<T extends Table> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Table
	 */
	<T extends Table> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/**
	 * table[结构集封装-依据]<br/>
	 * 读取table元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return TableMetadataAdapter
	 */
	TableMetadataAdapter tableMetadataAdapter(DataRuntime runtime);

	/* *****************************************************************************************************************
	 * 													vertexTable
	 ******************************************************************************************************************/
	/**
	 * vertexTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> VertexTable
	 */
	<T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct);
	default <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return vertexTables(runtime, random, greedy, catalog, schema, pattern, types, structs);
	}
	default <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
		return vertexTables(runtime, random, greedy, catalog, schema, pattern, types, false);
	}
	<T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct);
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return vertexTables(runtime, random, catalog, schema, pattern, types, structs);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
		return vertexTables(runtime, random, catalog, schema, pattern, types, false);
	}

	/**
	 * vertexTable[命令合成]<br/>
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
	List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * vertexTable[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * vertexTable[结果集封装]<br/>
	 *  根据查询结果集构造VertexTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryVertexTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param vertexTables 上一步查询结果
	 * @param set 查询结果集
	 * @return vertexTables
	 * @throws Exception 异常
	 */
	<T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set) throws Exception;
	<T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param vertexTables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return vertexTables
	 * @throws Exception 异常
	 */
	<T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param vertexTables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return vertexTables
	 * @throws Exception 异常
	 */
	<T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * 查询表创建SQL
	 * vertexTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param vertexTable 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, VertexTable vertexTable, boolean init);

	/**
	 * vertexTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param vertexTable 表
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable) throws Exception;

	/**
	 * vertexTable[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param vertexTable 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set);

	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return VertexTable
	 */
	<T extends VertexTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据查询结果封装VertexTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return VertexTable
	 */
	<T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/* *****************************************************************************************************************
	 * 													edgeTable
	 ******************************************************************************************************************/
	/**
	 * edgeTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> EdgeTable
	 */
	<T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct);
	default <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return edgeTables(runtime, random, greedy, catalog, schema, pattern, types, structs);
	}
	default <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
		return edgeTables(runtime, random, greedy, catalog, schema, pattern, types, false);
	}
	<T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct);
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return edgeTables(runtime, random, catalog, schema, pattern, types, structs);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
		return edgeTables(runtime, random, catalog, schema, pattern, types, false);
	}

	/**
	 * edgeTable[命令合成]<br/>
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
	List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * edgeTable[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * edgeTable[结果集封装]<br/>
	 *  根据查询结果集构造EdgeTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryEdgeTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param edgeTables 上一步查询结果
	 * @param set 查询结果集
	 * @return edgeTables
	 * @throws Exception 异常
	 */
	<T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set) throws Exception;
	<T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param edgeTables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return edgeTables
	 * @throws Exception 异常
	 */
	<T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param edgeTables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return edgeTables
	 * @throws Exception 异常
	 */
	<T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * 查询表创建SQL
	 * edgeTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param edgeTable 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, EdgeTable edgeTable, boolean init);

	/**
	 * edgeTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param edgeTable 表
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable edgeTable) throws Exception;

	/**
	 * edgeTable[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param edgeTable 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, EdgeTable edgeTable, List<String> ddls, DataSet set);

	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return EdgeTable
	 */
	<T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return EdgeTable
	 */
	<T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  VIEW.TYPE.
	 * @return List
	 * @param <T> View
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs);
	default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types){
		return views(runtime, random, greedy, catalog, schema, pattern, types, null);
	}
	/**
	 * view[命令合成]<br/>
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types VIEW.TYPE.
	 * @return List
	 */
	List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception;
	default List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception{
		return buildQueryViewsRun(runtime, greedy, catalog, schema, pattern, types, null);
	}

	/**
	 * view[结果集封装]<br/>
	 *  根据查询结果集构造View
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param views 上一步查询结果
	 * @param set 查询结果集
	 * @return views
	 * @throws Exception 异常
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set) throws Exception;
	<T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * view[结果集封装]<br/>
	 * 根据根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types VIEW.TYPE.
	 * @return views
	 * @throws Exception 异常
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param view 视图
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, View view);

	/**
	 * view[命令合成]<br/>
	 * 查询view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view view
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[结果集封装]<br/>
	 * 查询 view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param view view
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */

	List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set);

	/**
	 * view[结果集封装]<br/>
	 * 根据查询结果封装view对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return View
	 */
	<T extends View> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * view[结果集封装]<br/>
	 * 根据查询结果封装view对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return View
	 */
	<T extends View> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/**
	 * view[结构集封装-依据]<br/>
	 * 读取view元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return TableMetadataAdapter
	 */
	ViewMetadataAdapter viewMetadataAdapter(DataRuntime runtime);
	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	/**
	 * master table[调用入口]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  MasterTable.TYPE.
	 * @return List
	 * @param <T> MasterTable
	 */
	<T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types);
	/**
	 * master table[命令合成]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types MasterTable.TYPE.
	 * @return String
	 */
	List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * master table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * master table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param types MasterTable.TYPE.
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables,Catalog catalog, Schema schema, String pattern, int types) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table MasterTable
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, MasterTable table);
	/**
	 * master table[命令合成]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table MasterTable
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table) throws Exception;
	/**
	 * master table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set);

	/**
	 * master table[结果集封装]<br/>
	 * 根据查询结果封装MasterTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return MasterTable
	 */
	<T extends MasterTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * master table[结果集封装]<br/>
	 * 根据查询结果封装MasterTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return MasterTable
	 */
	<T extends MasterTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/* *****************************************************************************************************************
	 * 													partition table
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
	<T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern);

	/**
	 * partition table[命令合成]<br/>
	 * 查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types yPartitionTable.TYPE.
	 * @return String
	 */
	List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception;
	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @param pattern 名称统配符或正则
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String pattern) throws Exception;
	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags) throws Exception;
	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master) throws Exception;

	/**
	 * partition table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * partition table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master) throws Exception;
	/**
	 * partition table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table PartitionTable
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, PartitionTable table);

	/**
	 * partition table[命令合成]<br/>
	 * 查询 PartitionTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table PartitionTable
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set);

	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果封装PartitionTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return PartitionTable
	 */
	<T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果封装PartitionTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return PartitionTable
	 */
	<T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/* *****************************************************************************************************************
	 * 													column
	 * 获取表的几种方法和场景
	 * (1)查询系统表
	 * (2)JDBC结果集自带的ResultSet
	 * (3)JDBC.DatabaseMetaData查询指定表的列
	 * (4)SPRING.queryForRowSet.SqlRowSetMetaData(与2类似)
	 ******************************************************************************************************************/
	/**
	 * column[调用入口]<br/>(多方法合成)<br/>
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);

	/**
	 * column[调用入口]<br/>(方法1)<br/>
	 * 查询列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 查询全部表时 输入null
	 * @return List
	 * @param <T> Column
	 */
	<T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table);
	default <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String table) {
		return columns(runtime, random, greedy, catalog, schema, new Table(table));
	}
	default <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema) {
		return columns(runtime, random, greedy, catalog, schema,(Table)null);
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
	<T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Collection<Table> tables);
	/**
	 * column[调用入口]<br/>(方法3)<br/>
	 * DatabaseMetaData
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @return columns 上一步查询结果
	 * @return pattern 列名称通配符
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;

	/**
	 * column[命令合成]<br/>(方法1)<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return sqls
	 */
	List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;

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
	List<Run> buildQueryColumnsRun(DataRuntime runtime, Catalog catalog, Schema schema, Collection<Table> tables, boolean metadata) throws Exception;
	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 *  根据查询结果集构造Column
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 * 根据查询结果集构造Column
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	<T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception;

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 * 根据查询结果集构造Column,并分配到各自的表中
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 表
	 * @param columns 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	<T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Collection<Table> tables, List<T> columns, DataSet set) throws Exception;

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 列基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row 系统表查询SQL结果集
	 * @param <T> Column
	 */
	<T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row);

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Column
	 * @param <T> Column
	 */
	<T extends Column> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/**
	 * column[结构集封装-依据]<br/>
	 * 读取column元数据结果集的依据，主要返回column属性与查询结集之间的对应关系
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ColumnMetadataAdapter
	 */
	ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime);
	/**
	 * column[结构集封装-依据]<br/>
	 * 读取column元数据结果集的依据，主要在columnMetadataAdapter(DataRuntime runtime)项目上补充length/precision/sacle相关
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 具体数据类型,length/precisoin/scale三个属性需要根据数据类型覆盖通用配置
	 * @return ColumnMetadataAdapter
	 */
	default ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta) {
		ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
		if(null == adapter) {
			adapter = new ColumnMetadataAdapter();
		}
		TypeMetadata.Config config = adapter.getTypeConfig();
		if(null == config) {
			config = new TypeMetadata.Config();
		}
		//长度列
		String columnMetadataLengthRefer = columnMetadataLengthRefer(runtime, meta);
		if(null != columnMetadataLengthRefer) {
			config.setLengthRefer(columnMetadataLengthRefer);
		}
		//有效位数列
		String columnMetadataPrecisionRefer = columnMetadataPrecisionRefer(runtime, meta);
		if(null != columnMetadataPrecisionRefer) {
			config.setPrecisionRefer(columnMetadataPrecisionRefer);
		}
		//小数位数列
		String columnMetadataScaleRefer = columnMetadataScaleRefer(runtime, meta);
		if(null != columnMetadataScaleRefer) {
			config.setScaleRefer(columnMetadataScaleRefer);
		}
		int ignoreLength = ignoreLength(runtime, meta);
		if(-1 != ignoreLength) {
			config.setIgnoreLength(ignoreLength);
		}
		int ignorePrecision = ignorePrecision(runtime, meta);
		if(-1 != ignorePrecision) {
			config.setIgnorePrecision(ignorePrecision);
		}
		int ignoreScale = ignoreScale(runtime, meta);
		if(-1 != ignoreScale) {
			config.setIgnoreScale(ignoreScale);
		}
		adapter.setTypeConfig(config);
		return adapter;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据长度列<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	String columnMetadataLengthRefer(DataRuntime runtime, TypeMetadata meta);

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据数字有效位数列<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	String columnMetadataPrecisionRefer(DataRuntime runtime, TypeMetadata meta);

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据数字小数位数列<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	String columnMetadataScaleRefer(DataRuntime runtime, TypeMetadata meta);

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略长度<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	int columnMetadataIgnoreLength(DataRuntime runtime, TypeMetadata meta);

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	int columnMetadataIgnorePrecision(DataRuntime runtime, TypeMetadata meta);

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略小数位数<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	int columnMetadataIgnoreScale(DataRuntime runtime, TypeMetadata meta);

	/* *****************************************************************************************************************
	 * 													tag
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
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table);
	/**
	 * tag[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	List<Run> buildQueryTagsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;

	/**
	 * tag[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryTagsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set 查询结果集
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception;

	/**
	 *
	 * tag[结果集封装]<br/>
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param pattern 名称统配符或正则
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception;

	/**
	 * tag[结果集封装]<br/>
	 * 列基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row 系统表查询SQL结果集
	 * @return Tag
	 * @param <T> Tag
	 */
	<T extends Tag> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row);

	/**
	 * tag[结果集封装]<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Tag
	 * @param <T> Tag
	 */
	<T extends Tag> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/* *****************************************************************************************************************
	 * 													primary
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
	PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table);

	/**
	 * primary[命令合成]<br/>
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception;

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	<T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception;

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	<T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception;
	/**
	 * primary[结构集封装-依据]<br/>
	 * 读取primary key元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return PrimaryMetadataAdapter
	 */
	PrimaryMetadataAdapter primaryMetadataAdapter(DataRuntime runtime);
	/**
	 * primary[结构集封装]<br/>
	 *  根据驱动内置接口补充PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @throws Exception 异常
	 */
	PrimaryKey primary(DataRuntime runtime, Table table) throws Exception;

	/* *****************************************************************************************************************
	 * 													foreign
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
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	/**
	 * foreign[命令合成]<br/>
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception;

	/**
	 * foreign[结构集封装]<br/>
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;

	/**
	 * foreign[结构集封装]<br/>
	 * 根据查询结果集构造ForeignKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	<T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception;

	/**
	 * foreign[结构集封装]<br/>
	 * 根据查询结果集构造ForeignKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	<T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception;
	/* *****************************************************************************************************************
	 * 													index
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
	<T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);

	<T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy,  Collection<Table> tables);
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
	<T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern);
	/**
	 * index[命令合成]<br/>
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param name 名称
	 * @return sqls
	 */
	List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name);
	List<Run> buildQueryIndexesRun(DataRuntime runtime, Collection<Table> tables);

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexes 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	<T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set) throws Exception;
	<T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set) throws Exception;
	<T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Collection<Table> tables, List<T> indexes, DataSet set) throws Exception;

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
	<T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate) throws Exception;

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
	<T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate) throws Exception;

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
	<T extends Index> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception;

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
	<T extends Index> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception;
	/**
	 * index[结构集封装-依据]<br/>
	 * 读取index元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	IndexMetadataAdapter indexMetadataAdapter(DataRuntime runtime);
	/* *****************************************************************************************************************
	 * 													constraint
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
	<T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);
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
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern);

	/**
	 * constraint[命令合成]<br/>
	 * 查询表上的约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param pattern 名称通配符或正则
	 * @return sqls
	 */
	List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern);
	/**
	 * constraint[结果集封装]<br/>
	 *  根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set 查询结果集
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception;

	/**
	 * constraint[结果集封装]<br/>
	 *  根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param column 列
	 * @param constraints 上一步查询结果
	 * @param set 查询结果集
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception;

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Constraint
	 */
	<T extends Constraint> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Constraint
	 */
	<T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/**
	 * catalog[结构集封装-依据]<br/>
	 * 读取catalog元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	ConstraintMetadataAdapter constraintMetadataAdapter(DataRuntime runtime);

	/* *****************************************************************************************************************
	 * 													trigger
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
	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events);
	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) ;
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception;

	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Trigger
	 */
	<T extends Trigger> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Trigger
	 */
	<T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);

	/**
	 * trigger[结构集封装-依据]<br/>
	 * 读取 trigger 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	TriggerMetadataAdapter triggerMetadataAdapter(DataRuntime runtime);

	/* *****************************************************************************************************************
	 * 													procedure
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
	<T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
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
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures) throws Exception;
	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception;
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	/**
	 * procedure[命令合成]<br/>
	 * 查询存储DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception;
	/**
	 * procedure[结果集封装]<br/>
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param procedure Procedure
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set);

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Procedure
	 */
	<T extends Procedure> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Procedure
	 */
	<T extends Procedure> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * procedure[结构集封装-依据]<br/>
	 * 读取 procedure 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ProcedureMetadataAdapter
	 */
	ProcedureMetadataAdapter procedureMetadataAdapter(DataRuntime runtime);
	/* *****************************************************************************************************************
	 * 													function
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
	<T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
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
	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions) throws Exception;

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions) throws Exception;
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param function Function
	 * @return ddl
	 */
	List<String> ddl(DataRuntime runtime, String random, Function function);
	/**
	 * function[命令合成]<br/>
	 * 查询函数DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param function 函数
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, Function function) throws Exception;
	/**
	 * function[结果集封装]<br/>
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param function Function
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set);

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Function
	 */
	<T extends Function> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Function
	 */
	<T extends Function> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * function[结构集封装-依据]<br/>
	 * 读取 function 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return FunctionMetadataAdapter
	 */
	FunctionMetadataAdapter functionMetadataAdapter(DataRuntime runtime);

	/* *****************************************************************************************************************
	 * 													sequence
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
	<T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
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
	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception;

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences) throws Exception;

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences) throws Exception;
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param sequence Sequence
	 * @return ddl
	 */
	List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
	/**
	 * sequence[命令合成]<br/>
	 * 查询序列DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param sequence 序列
	 * @return List
	 */
	List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence sequence) throws Exception;
	/**
	 * sequence[结果集封装]<br/>
	 * 查询 Sequence DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param sequence Sequence
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set);

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Sequence
	 */
	<T extends Sequence> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Sequence
	 */
	<T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
	/**
	 * sequence[结构集封装-依据]<br/>
	 * 读取 sequence 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return SequenceMetadataAdapter
	 */
	SequenceMetadataAdapter sequenceMetadataAdapter(DataRuntime runtime);

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
	 * 是否支持DDL合并
	 * @return boolean
	 */
	default boolean slice() {
		return false;
	}
	default boolean slice(boolean slice) {
		return slice && slice();
	}

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
	<T extends Metadata> T search(List<T> metas, Catalog catalog, Schema schema, String name);
	/**
	 * 执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return boolean
	 */
	boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run);
	default boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, List<Run> runs) {
		boolean result = true;
		int idx = 0;
		for(Run run:runs) {
			ACTION.SWITCH swt = InterceptorProxy.before(runtime, random, action, meta, runs);
			long cmt_fr = System.currentTimeMillis();
			if(swt == ACTION.SWITCH.CONTINUE){
				result = execute(runtime, random + "[index:" + idx++ + "]", meta, action, run) && result;
			}else if(swt == ACTION.SWITCH.SKIP){
				continue;
			}else if(swt == ACTION.SWITCH.BREAK){
				break;
			}
			swt = InterceptorProxy.after(runtime, random, action, meta, runs, result, System.currentTimeMillis()-cmt_fr);
			if(swt == ACTION.SWITCH.BREAK){
				break;
			}
		}
		return result;
	}
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Table table) throws Exception
	 * boolean alter(Table table) throws Exception
	 * boolean drop(Table table) throws Exception
	 * boolean rename(Table origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * table[调用入口]<br/>
	 * 创建表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, Table meta) throws Exception;
	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[调用入口]<br/>
	 * 重命名表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, Table origin, String name) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
	 * @param meta 表
	 * @return String
	 */
	default String keyword(Metadata meta) {
		return meta.getKeyword();
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
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 修改表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param columns 列
	 * @param slice 是否只生成片段(true:不含alter table部分，用于DDL合并)
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns, boolean slice) throws Exception;
	default List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns) throws Exception{
		return buildAlterRun(runtime, meta, columns, false);
	}

	/**
	 * table[命令合成]<br/>
	 * 重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 删除表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta) throws Exception;
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * table[命令合成-子流程]<br/>
	 * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 */
	void checkPrimary(DataRuntime runtime, Table table);

	/**
	 * table[命令合成-子流程]<br/>
	 * 定义表的主键标识,在创建表的DDL结尾部分(注意不要跟列定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 engine
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 body部分包含column index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 columns部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 索引部分，与buildAppendIndexRun二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder indexes(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 数据模型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 分桶方式
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 物化视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 扩展属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 主表设置分区依据(分区依据列)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(相关主表)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(分区依据值)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;
	/**
	 * table[命令合成-子流程]<br/>
	 * 继承自table.getInherit
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(View view) throws Exception
	 * boolean alter(View view) throws Exception
	 * boolean drop(View view) throws Exception
	 * boolean rename(View origin, String name) throws Exception
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 创建视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, View view) throws Exception;
	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[调用入口]<br/>
	 * 重命名视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 视图
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, View origin, String name) throws Exception;

	/**
	 * view[命令合成]<br/>
	 * 创建视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception;
	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图头部
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta) throws Exception;
	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图选项
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta) throws Exception;
	/**
	 * view[命令合成]<br/>
	 * 修改视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成]<br/>
	 * 重命名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成]<br/>
	 * 删除视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成-子流程]<br/>
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * view[命令合成-子流程]<br/>
	 * 视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param view 视图
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view);

	/* *****************************************************************************************************************
	 * 													MasterTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(MasterTable meta) throws Exception
	 * boolean alter(MasterTable meta) throws Exception
	 * boolean drop(MasterTable meta) throws Exception
	 * boolean rename(MasterTable origin, String name) throws Exception
	 ******************************************************************************************************************/
	/**
	 * master table[调用入口]<br/>
	 * 创建主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, MasterTable meta) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, MasterTable meta) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, MasterTable meta) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * 重命名主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception;

	/**
	 * master table[命令合成]<br/>
	 * 创建主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, MasterTable table) throws Exception;

	/**
	 * master table[命令合成]<br/>
	 * 删除主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, MasterTable table) throws Exception;
	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, MasterTable table) throws Exception;
	/**
	 * master table[命令合成-子流程]<br/>
	 * 主表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, MasterTable table) throws Exception;

	/**
	 * master table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table) throws Exception;

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table) throws Exception;

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, PartitionTable meta) throws Exception
	 * boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception
	 * boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception
	 * boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, PartitionTable meta) throws Exception;

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception;

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception;

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception;

	/**
	 * partition table[命令合成]<br/>
	 * 创建分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成]<br/>
	 * 修改分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成-]<br/>
	 * 删除分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception;
	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成-子流程]<br/>
	 * 修改分区表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Column meta) throws Exception
	 * boolean alter(DataRuntime runtime, Table table, Column meta) throws Exception
	 * boolean alter(DataRuntime runtime, Column meta) throws Exception
	 * boolean drop(DataRuntime runtime, Column meta) throws Exception
	 *
	 * private boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	/**
	 * column[调用入口]<br/>
	 * 添加列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean add(DataRuntime runtime, Column meta) throws Exception;

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param trigger 修改异常时，是否触发监听器
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception;
	default boolean alter(DataRuntime runtime, Table table, Column meta) throws Exception {
		return alter(runtime, table, meta, true);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, Column meta) throws Exception;

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, Column meta) throws Exception;

	/**
	 * column[调用入口]<br/>
	 * 重命名列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 列
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, Column origin, String name) throws Exception;

	/**
	 * column[命令合成]<br/>
	 * 添加列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice) throws Exception;


	/**
	 * column[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice) throws Exception;


	/**
	 * column[命令合成]<br/>
	 * 删除列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice) throws Exception;


	/**
	 * column[命令合成]<br/>
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeTypeRun(DataRuntime runtime, Column column, boolean slice) throws Exception;
	default List<Run> buildChangeTypeRun(DataRuntime runtime, Column column) throws Exception{
		return buildChangeTypeRun(runtime, column, false);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	String alterColumnKeyword(DataRuntime runtime);

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列引导<br/>
	 * alter table sso_user [add column] type_code int
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列
	 * @return String
	 */
	StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 删除列引导<br/>
	 * alter table sso_user [drop column] type_code
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列
	 * @return String
	 */
	StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeNullableRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列，依次拼接下面几个属性注意不同数据库可能顺序不一样
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param action 区分 创建与修改过程有区别  如有些数据库修改时不支持NULL NOT NULL(如clickhouse)
	 * @return StringBuilder
	 */
	StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:数据类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有长度<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type 数据类型
	 * @return boolean
	 */
	int ignoreLength(DataRuntime runtime, TypeMetadata type);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
	int ignorePrecision(DataRuntime runtime, TypeMetadata type);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:定义列:是否忽略小数位<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
	int ignoreScale(DataRuntime runtime, TypeMetadata type);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:聚合类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:列数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param type 数据类型(已经过转换)
	 * @param ignoreLength 是否忽略长度
	 * @param ignorePrecision 是否忽略有效位数
	 * @param ignoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:非空
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:默认值
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列的主键标识(注意不要跟表定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:唯一索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	StringBuilder unique(DataRuntime runtime, StringBuilder builder, Column meta);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:递增列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:更新行事件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:位置
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column);

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Tag meta) throws Exception
	 * boolean alter(DataRuntime runtime, Tag table, Column meta) throws Exception
	 * boolean alter(DataRuntime runtime, Tag meta) throws Exception
	 * boolean drop(DataRuntime runtime, Tag meta) throws Exception
	 *
	 * private boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Tag meta) throws Exception;

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @param trigger 修改异常时，是否触发监听器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception;
	default boolean alter(DataRuntime runtime, Table table, Tag meta) throws Exception {
		return alter(runtime, table, meta, true);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Tag meta) throws Exception;

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Tag meta) throws Exception;

	/**
	 * tag[调用入口]<br/>
	 * 重命名标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原标签
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;
	/**
	 * tag[命令合成]<br/>
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;
	/**
	 * tag[命令合成]<br/>
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;
	/**
	 * tag[命令合成]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * //TODO 放在下一级 metadata引用
	 * ddl过程 默认值 检测适配 内置函数 如mysql.CURRENT_TIMESTAMP 转换成 oracle.sysdate
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param def 默认值
	 * @return SQL_BUILD_IN_VALUE
	 */
	default SQL_BUILD_IN_VALUE checkDefaultBuildInValue(DataRuntime runtime, Object def) {
		SQL_BUILD_IN_VALUE result = null;
		if(null != def) {
			String chk = def.toString().toUpperCase().trim();
			if("CURRENT_TIMESTAMP".equals(chk)
				|| "CURRENT TIMESTAMP".equals(chk)
				|| "SYSDATE".equals(chk)
				|| "NOW()".equals(chk)
				|| "NOW".equals(chk)
				|| "SYSTIMESTAMP".equals(chk)
				|| "GETDATE()".equals(chk)
				|| chk.contains("DATETIME(")
				) {
				result = SQL_BUILD_IN_VALUE.CURRENT_DATETIME;
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception
	 * boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception
	 * boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception
	 * boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception
	 * boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	default boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception {
		return alter(runtime, table, table.getPrimaryKey(), meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 主键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception;
	/**
	 * primary[命令合成]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary, boolean slice) throws Exception;
	/**
	 * primary[命令合成]<br/>
	 * 创建完表后，添加主键，与列主键标识，表主键标识三选一<br/>
	 * 大部分情况调用buildAddRun<br/>
	 * 默认不调用，大部分数据库在创建列或表时可以直接标识出主键<br/>
	 * 只有在创建表过程中不支持创建主键的才需要实现这个方法
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	default List<Run> buildAppendPrimaryRun(DataRuntime runtime, Table meta) throws Exception {
		return new ArrayList<>();
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
	List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta, boolean slice) throws Exception;
	default List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta) throws Exception{
		return buildAlterRun(runtime, origin, meta, false);
	}
	default List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return buildAlterRun(runtime, null, meta);
	}

	/**
	 * primary[命令合成]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @param slice 是否只生成片段(true:不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary, boolean slice) throws Exception;
	default List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception {
		return buildDropRun(runtime, primary, false);
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception;

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add((DataRuntime runtime, ForeignKey meta) throws Exception
	 * boolean alter((DataRuntime runtime, ForeignKey meta) throws Exception
	 * boolean alter(Table table, ForeignKey meta) throws Exception;
	 * boolean drop((DataRuntime runtime, ForeignKey meta) throws Exception
	 * boolean rename((DataRuntime runtime, ForeignKey origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键
	 * @param meta 外键
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Index index) throws Exception
	 * boolean alter(DataRuntime runtime, Index index) throws Exception
	 * boolean drop(DataRuntime runtime, Index index) throws Exception
	 * boolean rename(DataRuntime runtime, Index origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * index[调用入口]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 重命名索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 索引
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Index origin, String name) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 创建表过程添加索引,表创建完成后添加索引,于表内索引index(DataRuntime, StringBuilder, Table)二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Index meta) throws Exception;
	/**
	 * index[命令合成]<br/>
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta);
	/**
	 * index[命令合成-子流程]<br/>
	 * 索引属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder property(DataRuntime runtime, StringBuilder builder, Index meta);
	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Constraint meta) throws Exception
	 * boolean alter(DataRuntime runtime, Constraint meta) throws Exception
	 * boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception;
	 * boolean drop(DataRuntime runtime, Constraint meta) throws Exception
	 * boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * constraint[调用入口]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 重命名约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 约束
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Trigger meta) throws Exception
	 * boolean alter(DataRuntime runtime, Trigger meta) throws Exception
	 * boolean drop(DataRuntime runtime, Trigger meta) throws Exception
	 * boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * trigger[调用入口]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[调用入口]<br/>
	 * 重命名触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 触发器
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildDropRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta);

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Procedure meta) throws Exception
	 * boolean alter(DataRuntime runtime, Procedure meta) throws Exception
	 * boolean drop(DataRuntime runtime, Procedure meta) throws Exception
	 * boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * procedure[调用入口]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean create(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[调用入口]<br/>
	 * 重命名存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 存储过程
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter);
	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Function meta) throws Exception
	 * boolean alter(DataRuntime runtime, Function meta) throws Exception
	 * boolean drop(DataRuntime runtime, Function meta) throws Exception
	 * boolean rename(DataRuntime runtime, Function origin, String name)  throws Exception
	 ******************************************************************************************************************/

	/**
	 * function[调用入口]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean create(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[调用入口]<br/>
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[调用入口]<br/>
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[调用入口]<br/>
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Function origin, String name) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Sequence meta) throws Exception
	 * boolean alter(DataRuntime runtime, Sequence meta) throws Exception
	 * boolean drop(DataRuntime runtime, Sequence meta) throws Exception
	 * boolean rename(DataRuntime runtime, Sequence origin, String name)  throws Exception
	 ******************************************************************************************************************/

	/**
	 * sequence[调用入口]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean create(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[调用入口]<br/>
	 * 修改序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[调用入口]<br/>
	 * 删除序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[调用入口]<br/>
	 * 重命名序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 序列
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Sequence origin, String name) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 删除序列
	 * @param meta 序列
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Sequence meta) throws Exception;
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 ******************************************************************************************************************/
	StringBuilder name(DataRuntime runtime, StringBuilder builder, Metadata meta);
	StringBuilder name(DataRuntime runtime, StringBuilder builder, Column meta);

/*
	*//**
	 * 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 表
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue run);
	boolean convert(DataRuntime runtime, Table table, Run run);
	boolean convert(DataRuntime runtime, ConfigStore configs, Run run);

	/**
	 * 数据类型转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param columns 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Map<String, Column> columns, RunValue run);

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Column column, RunValue run);
	Object convert(DataRuntime runtime, Column column, Object value);
	Object convert(DataRuntime runtime, TypeMetadata columnType, Object value);
	/**
	 * 在不检测数据库结构时才生效,否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param row DataRow 或 Entity
	 * @param key 列名
	 */
	void value(DataRuntime runtime, StringBuilder builder, Object row, String key);

	/**
	 * 根据数据类型生成SQL(如是否需要'',是否需要格式转换函数)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param value value
	 */
	//void format(StringBuilder builder, Object value);

	/**
	 * 从数据库中读取数据,常用的基本类型可以自动转换,不常用的如json/point/polygon/blob等转换成anyline对应的类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
	 * @return Object
	 */
	Object read(DataRuntime runtime, Column metadata, Object value, Class clazz);

	/**
	 * 通过占位符写入数据库前转换成数据库可接受的Java数据类型<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	Object write(DataRuntime runtime, Column metadata, Object value, boolean placeholder);
 	/**
	 * 拼接字符串
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param args args
	 * @return String
	 */
	String concat(DataRuntime runtime, String ... args);

	/**
	 * 是否是数字列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isNumberColumn(DataRuntime runtime, Column column);

	/**
	 * 是否是boolean列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isBooleanColumn(DataRuntime runtime, Column column);

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isCharColumn(DataRuntime runtime, Column column);

	/**
	 * 内置函数
	 * 如果需要引号,方法应该一块返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列属性,不同的数据类型解析出来的值可能不一样
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value);
	default String defaultValue(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
		return value(runtime, column, value);
	}
	void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value);
	/**
	 * 转换成相应数据库的数据类型包含精度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	//String type(Column column);

	/**
	 * 数据库类型转换成java类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type type
	 * @return String
	 */
	//String type2class(String type);

	/**
	 * 对象名称格式化(大小写转换)，在查询系统表时需要
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name name
	 * @return String
	 */
	String objectName(DataRuntime runtime, String name);

	default String compressCondition(DataRuntime runtime, String cmd) {
		String head = conditionHead();
		cmd = cmd.replaceAll(head + "\\s*1=1\\s*AND", head);
		return cmd;
	}
	default String conditionHead() {
		return "WHERE";
	}
	/**
	 * 比较运算符在不同数据库的区别
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列名
	 * @param compare Compare
	 * @param metadata 数据类型
	 * @param value 值
	 * @param placeholder 是否启用占位符
	 */
	default void formula(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Column metadata, Object value, boolean placeholder) {
		if(!placeholder) {
			//不用占位 需要引号的在这里加上
			value = write(runtime, metadata, value, placeholder);
		}
		builder.append(compare.formula(column, value, placeholder));
	}

}
