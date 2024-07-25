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



package org.anyline.data.adapter.init;

import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.KeyAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.SyntaxHelper;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.TextPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.init.DefaultVariable;
import org.anyline.data.prepare.xml.XMLPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.GeneratorConfig;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.AnylineException;
import org.anyline.exception.CommandException;
import org.anyline.exception.CommandQueryException;
import org.anyline.exception.CommandUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.adapter.*;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.ConvertProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class AbstractDriverAdapter implements DriverAdapter {

	protected DMListener dmListener;
	protected DDListener ddListener;
	protected PrimaryGenerator primaryGenerator;
	protected DriverActuator actuator;

	protected DMListener getListener() {
		return dmListener;
	}

	public void setListener(DMListener listener) {
		this.dmListener = listener;
	}
	public DMListener getDMListener() {
		return this.dmListener;
	}
	public void setListener(DDListener listener) {
		this.ddListener = listener;
	}
	public DDListener getDDListener() {
		return this.ddListener;
	}
	public void setGenerator(PrimaryGenerator generator) {
		this.primaryGenerator = generator;
	}
	public PrimaryGenerator getPrimaryGenerator() {
		return primaryGenerator;
	}
	protected String delimiterFr = "";
	protected String delimiterTo = "";
	//拼写兼容 下划线空格兼容
	protected static Map<String, String> spells = new HashMap<>();

	//根据名称定位数据类型
	protected LinkedHashMap<String, TypeMetadata> alias = new LinkedHashMap();


	static {
		for(StandardTypeMetadata type: StandardTypeMetadata.values()) {
			String name = type.name().toUpperCase();//变量名
			String standard = type.getName().toUpperCase(); //标准SQL类型名
			spells.put(name, standard);
			if(name.contains(" ")) {
				spells.put(name.replace(" ", "_"), standard);
			}
			if(name.contains("_")) {
				spells.put(name.replace("_", " "), standard);
			}
			if(standard.contains(" ")) {
				spells.put(standard.replace(" ", "_"), standard);
			}
			if(standard.contains("_")) {
				spells.put(standard.replace("_", " "), standard);
			}
		}
	}

	@Override
	public void setActuator(DriverActuator actuator) {
		this.actuator = actuator;
	}
	@Override
	public DriverActuator getActuator() {
		return actuator;
	}
	@Override
	public String getDelimiterFr() {
		return this.delimiterFr;
	}
	@Override
	public String getDelimiterTo() {
		return this.delimiterTo;
	}
	public void setDelimiter(String delimiter) {
		if(BasicUtil.isNotEmpty(delimiter)) {
			delimiter = delimiter.replaceAll("\\s","");
			if(delimiter.length() == 1) {
				this.delimiterFr = delimiter;
				this.delimiterTo = delimiter;
			}else{
				this.delimiterFr = delimiter.substring(0,1);
				this.delimiterTo = delimiter.substring(1,2);
			}
		}
	}
	/**
	 * 对应的兼容模式，有些数据库会兼容oracle或pg,需要分别提供两个JDBCAdapter或者直接依赖oracle/pg的adapter
	 * 参考DefaultJDBCAdapterUtil定位adapter的方法
	 * @return DatabaseType
	 */
	public DatabaseType compatible() {
		return null;
	}
	public AbstractDriverAdapter() {
		//当前数据库支持的数据类型,子类根据情况覆盖
		for(StandardTypeMetadata type: StandardTypeMetadata.values()) {
			reg(type, type.config());
			List<DatabaseType> dbs = type.databaseTypes();
			for(DatabaseType db:dbs) {
				if(db == this.type()) {
					//column type支持当前db
					alias(type.getName(), type);
					alias(type.name(), type);
					break;
				}
			}
		}
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Config(0, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Config( 1,1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.BYTES, new TypeMetadata.Config(0, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Config(1,1,1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.INT, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Config(1, 0, 0));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.DATE, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.TIME, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.DATETIME, new TypeMetadata.Config( 1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.COLLECTION, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.GEOMETRY, new TypeMetadata.Config(1, 1, 1));
		MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Config( 1, 1, 1));
	}

	public LinkedHashMap<String, TypeMetadata> alias() {
		return alias;
	}

	/**
	 * 注册数据类型别名(包含对应的标准类型、length/precision/scale等配置)
	 * @param alias 数据类型别名
	 * @return Config
	 */
	@Override
	public TypeMetadata.Config reg(TypeMetadataAlias alias) {
		TypeMetadata standard = alias.standard();
		if(standard == StandardTypeMetadata.NONE) {
			return null;
		}
		alias(alias.input(), standard);										//根据别名
		alias(standard.getName(), standard);										//根据实现SQL数据类型名称
		TypeMetadata.Config config = alias.config();
		reg(alias.input(), config);
		reg(alias.standard(), config);
		return config;
	}
	protected void alias(String key, TypeMetadata value) {
		if(null != key && null != value && TypeMetadata.NONE != value) {
			this.alias.put(key, value);
			this.alias.put(key.replace("_", " "), value);
			this.alias.put(key.replace(" ", "_"), value);
		}
	}
	/**
	 * 注册数据类型配置
	 * 要从配置项中取出每个属性检测合并,不要整个覆盖
	 * @param type 数据类型
	 * @param config 配置项
	 * @return Config
	 */
	@Override
	public TypeMetadata.Config reg(TypeMetadata type, TypeMetadata.Config config) {
		return MetadataAdapterHolder.reg(type(), type, config);
	}
	/**
	 * 注册数据类型配置
	 * 要从配置项中取出每个属性检测合并,不要整个覆盖
	 * @param type 类型名称或别名
	 * @param config 配置项
	 * @return TypeMetadata.Config
	 */
	@Override
	public TypeMetadata.Config reg(String type, TypeMetadata.Config config) {
		return MetadataAdapterHolder.reg(type(), type, config);
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
	 * long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
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
	@Override
	public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		if(null == random) {
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		swt = InterceptorProxy.prepareInsert(runtime, random, batch, dest, data, columns);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.prepareInsert(runtime, random, batch, dest, data, columns);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != data && data instanceof DataSet) {
			DataSet set = (DataSet)data;
			Map<String, Object> tags = set.getTags();
			if(null != tags && tags.size()>0) {
				LinkedHashMap<String, PartitionTable> partitionTables = partitionTables(runtime, random, false, new MasterTable(dest), tags, null);
				if(partitionTables.size() != 1) {
					String msg = "分区表定位异常,主表:" + dest + ",标签:" + BeanUtil.map2json(tags) + ",分区表:" + BeanUtil.object2json(partitionTables.keySet());
					if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
						throw new CommandUpdateException(msg);
					}else{
						log.error(msg);
						return -1;
					}
				}
				dest = partitionTables.values().iterator().next();
			}
		}
		Run run = buildInsertRun(runtime, batch, dest, data, configs, columns);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
			dest.setColumns(columns(runtime, random, false, dest, false));
		}
		if(null == run) {
			return 0;
		}

		long cnt = 0;
		long fr = System.currentTimeMillis();
		long millis = -1;

		swt = InterceptorProxy.beforeInsert(runtime, random, run, dest, data, columns);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.beforeInsert(runtime, random, run, dest, data, columns);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		cnt = insert(runtime, random, data, configs, run, null);
		if (null != dmListener) {
			dmListener.afterInsert(runtime, random, run, cnt, dest, data, columns, cmd_success, cnt, millis);
		}
		InterceptorProxy.afterInsert(runtime, random, run, dest, data, columns, cmd_success, cnt, System.currentTimeMillis() - fr);
		return cnt;
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
	@Override
	public long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions)", 37));
		}
		return 0;
	}

	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 需要插入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		Run run = null;
		if(null == obj) {
			return null;
		}
		if(null == dest) {
			dest = DataSourceUtil.parseDest(null, obj, configs);
		}

		if(obj instanceof Collection) {
			Collection list = (Collection) obj;
			if(!list.isEmpty()) {
				run = createInsertRunFromCollection(runtime, batch, dest, list, configs, columns);
			}
		}else {
			run = createInsertRun(runtime, dest, obj, configs, columns);
		}
		convert(runtime, configs, run);
		return run;
	}


	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param prepare 查询
	 * @param configs 过滤条件及相关配置
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String... conditions) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String... conditions)", 37));
		}
		return null;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)", 37));
		}
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)", 37));
		}
	}


	/**
	 * 插入子表前 检测并创建子表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param dest 表
	 * @param configs ConfigStore
	 */
	@Override
	public void fillInsertCreateTemplate(DataRuntime runtime, Run run, PartitionTable dest, ConfigStore configs){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertCreateTemplate(DataRuntime runtime, Run run, PartitionTable dest, ConfigStore configs)", 37));
		}
	}
	/**
	 * insert [命令合成-子流程]<br/>
	 * 确认需要插入的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj  Entity或DataRow
	 * @param batch  是否批量，批量时不检测值是否为空
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
	@Override
	public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch) {
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();/*确定需要插入的列*/
		if(null == obj) {
			return new LinkedHashMap<>();
		}
		if(obj instanceof Map && !(obj instanceof DataRow)) {
			obj = new DataRow(KeyAdapter.KEY_CASE.SRC, (Map)obj);
		}
		LinkedHashMap<String, Column> mastKeys = new LinkedHashMap<>();		// 必须插入列
		List<String> ignores = new ArrayList<>();		// 必须不插入列
		List<String> factKeys = new ArrayList<>();		// 根据是否空值

		boolean each = true;//是否需要从row中查找列
		if(null != columns && columns.size()>0) {
			each = false;
			cols = new LinkedHashMap<>();
			for(String column:columns) {
				if(BasicUtil.isEmpty(column)) {
					continue;
				}
				if(column.startsWith("+")) {
					column = column.substring(1);
					mastKeys.put(column.toUpperCase(), new Column(column));
					each = true;
				}else if(column.startsWith("-")) {
					column = column.substring(1);
					ignores.add(column);
					each = true;
				}else if(column.startsWith("?")) {
					column = column.substring(1);
					factKeys.add(column);
					each = true;
				}
				cols.put(column.toUpperCase(), new Column(column));
			}
		}
		if(each) {
			// 是否插入null及""列
			boolean isInsertNullColumn =  false;
			boolean isInsertEmptyColumn = false;
			DataRow row = null;
			if(obj instanceof DataRow) {
				row = (DataRow)obj;
				mastKeys.putAll(row.getUpdateColumns(true));

				ignores.addAll(row.getIgnoreUpdateColumns());
				cols = row.getColumns();

				isInsertNullColumn = row.isInsertNullColumn();
				isInsertEmptyColumn = row.isInsertEmptyColumn();

			}else{
				isInsertNullColumn = ConfigStore.IS_INSERT_NULL_FIELD(configs);
				isInsertEmptyColumn = ConfigStore.IS_INSERT_EMPTY_FIELD(configs);
				if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
					cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.INSERT));
				}else {
					cols = new LinkedHashMap<>();
					List<Field> fields = ClassUtil.getFields(obj.getClass(), false, false);
					for (Field field : fields) {
						Class clazz = field.getType();
						if (clazz == String.class || clazz == Date.class || ClassUtil.isPrimitiveClass(clazz)) {
							cols.put(field.getName().toUpperCase(), new Column(field.getName()));
						}
					}
				}
			}
			if(batch) {
				isInsertNullColumn = true;
				isInsertEmptyColumn = true;
			}

			if(log.isDebugEnabled()) {
				log.debug("[confirm insert columns][columns:{}]", cols);
			}
			BeanUtil.removeAll(ignores, columns);
			for(String ignore:ignores) {
				cols.remove(ignore.toUpperCase());
			}
			if(log.isDebugEnabled()) {
				log.debug("[confirm insert columns][ignores:{}]", ignores);
			}
			List<String> keys = Column.names(cols);
			for(String key:keys) {
				if(mastKeys.containsKey(key.toUpperCase())) {
					// 必须插入
					continue;
				}
				Object value = null;
				if(!(obj instanceof Map) && EntityAdapterProxy.hasAdapter(obj.getClass())) {
					value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
				}else{
					value = BeanUtil.getFieldValue(obj, key);
				}

				if(null == value) {
					if(factKeys.contains(key)) {
						cols.remove(key);
						continue;
					}
					if(!isInsertNullColumn) {
						cols.remove(key);
						continue;
					}
				}else if(BasicUtil.isEmpty(true, value)) {
					if(factKeys.contains(key)) {
						cols.remove(key);
						continue;
					}
					if(!isInsertEmptyColumn) {
						cols.remove(key);
						continue;
					}
				}

			}
		}
		if(log.isDebugEnabled()) {
			log.debug("[confirm insert columns][result:{}]", cols);
		}
		cols = checkMetadata(runtime, dest, configs, cols);
 		return cols;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 批量插入数据时,多行数据之间分隔符
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
	protected void setPrimaryValue(Object obj, Object value) {
		if(null == obj) {
			return;
		}
		if(obj instanceof DataRow) {
			DataRow row = (DataRow)obj;
			row.setPrimaryValue(value);
		}else{
			Column key = EntityAdapterProxy.primaryKey(obj.getClass());
			Field field = EntityAdapterProxy.field(obj.getClass(), key);
			BeanUtil.setFieldValue(obj, field, value);
		}
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 根据entity创建 INSERT RunPrepare由buildInsertRun调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run createInsertRun(DataRuntime runtime, Table dest, Object obj, List<String> columns)", 37));
		}
		return null;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 根据collection创建 INSERT RunPrepare由buildInsertRun调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 对象集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns) {
		Run run = new TableRun(runtime, dest);
		run.setBatch(batch);
		if(null == list || list.isEmpty()) {
			throw new CommandException("空数据");
		}
		Object first = list.iterator().next();

		if(BasicUtil.isEmpty(dest)) {
			throw new CommandException("未指定表");
		}
		/*确定需要插入的列*/
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
		for(Object item:list) {
			cols.putAll(confirmInsertColumns(runtime, dest, item, configs, columns, true));
			if(!ConfigTable.IS_CHECK_ALL_INSERT_COLUMN) {
				break;
			}
		}
		if(null == cols || cols.isEmpty()) {
			throw new CommandException("未指定列(DataRow或Entity中没有需要插入的属性值)["+first.getClass().getName()+":"+BeanUtil.object2json(first)+"]");
		}
		run.setInsertColumns(cols);
		run.setVol(cols.size());
		fillInsertContent(runtime, run, dest, list, configs, cols);

		return run;
	}

	/**
	 * insert [after]<br/>
	 * 执行insert后返回自增主键的key
	 * @return String
	 */
	@Override
	public String generatedKey() {
		return null;
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
	public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks) {
		long cnt = 0;
		int batch = run.getBatch();
		String action = "insert";
		if(batch > 1) {
			action = "batch insert";
		}
		if(!run.isValid()) {
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
				log.warn("[valid:false][action:{}][table:{}][不具备执行条件]", action, run.getTableName());
			}
			return -1;
		}
		String cmd = run.getFinalInsert();
		if(run.isEmpty()) {
			log.warn("[不具备执行条件][action:{}][table:{}]", action, run.getTable());
			return -1;
		}
		if(null != configs) {
			configs.add(run);
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
			if(batch > 1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
				log.info("{}[action:{}][table:{}][cmd:\n{}\n]\n[param size:{}]", random, action, run.getTable(), cmd, values.size());
			}else {
				log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
		}
		long millis = -1;

		boolean exe = true;
		if(null != configs) {
			exe = configs.execute();
		}
		if(!exe) {
			return -1;
		}
		try {
			cnt = actuator.insert(this, runtime, random, data, configs, run, generatedKey(), pks);
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
				if(millis > SLOW_SQL_MILLIS) {
					slow = true;
					log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}]{}", random, action, run.getTable(), DateUtil.format(millis), run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener) {
						dmListener.slow(runtime, random, ACTION.DML.INSERT, run, cmd, values, null, true, cnt, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
				String qty = LogUtil.format(cnt, 34);
				if(batch > 1) {
					qty = LogUtil.format("约"+cnt, 34);
				}
				log.info("{}[action:{}][table:{}][执行耗时:{}][影响行数:{}]", random, action, run.getTable(), DateUtil.format(millis), qty);
			}
		}catch(Exception e) {
			if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				log.error("insert 异常:", e);
			}
			if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.error("{}[{}][action:{}][table:{}]{}", random, LogUtil.format("插入异常:", 33)+e, action, run.getTable(), run.log(ACTION.DML.INSERT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				CommandUpdateException ex = new CommandUpdateException("insert异常:"+e.toString(), e);
				ex.setCmd(cmd);
				ex.setValues(values);
				throw ex;
			}

		}
		return cnt;
	}

	/**
	 * 是否支持返回自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param configs configs中也可能禁用返回
	 * @return boolean
	 */
	public boolean supportKeyHolder(DataRuntime runtime, ConfigStore configs) {
		if(null != configs && !configs.supportKeyHolder()) {
			return false;
		}
		return true;
	}

	/**
	 * 自增主键值keys
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param configs configs中也可能禁用返回
	 * @return keys
	 */
	public List<String> keyHolders(DataRuntime runtime, ConfigStore configs) {
		if(null != configs) {
			return configs.keyHolders();
		}
		return new ArrayList<>();
	}
	/* *****************************************************************************************************************
	 * 													UPDATE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns)
	 * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * [命令执行]
	 * long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run)
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
	@Override
	public long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		if(null == random) {
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareUpdate(runtime, random, batch, dest, data, configs, columns);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.prepareUpdate(runtime, random, batch, dest, data, configs, columns);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null == data) {
			if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				throw new CommandUpdateException("更新空数据");
			}else{
				log.error("更新空数据");
			}
		}
		long result = 0;
		if(data instanceof Collection) {
			if(batch <= 1) {
				Collection list = (Collection) data;
				for (Object item : list) {
					ConfigStore cfg = new DefaultConfigStore();
					cfg.copyProperty(configs);
					cfg.and(configs);
					result += update(runtime, random, 0, dest, item, cfg, columns);
				}
				return result;
			}
		}

		Run run = buildUpdateRun(runtime, batch, dest, data, configs, columns);

		if(run.isEmptyCondition()) {
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
				log.warn("[valid:false][没有更新条件][dest:"+dest+"]");
			}
			return -1;
		}

		//提前设置好columns,到了adapter中需要手动检测缓存
		if(ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
			dest.setColumns(columns(runtime, null, false, dest, false));
		}
		if(!run.isValid()) {
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		//String sql = run.getFinalUpdate();
		/*if(BasicUtil.isEmpty(sql)) {
			log.warn("[不具备更新条件][dest:{}]", dest);
			return -1;
		}
		List<Object> values = run.getValues();*/
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		/*if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[cmd:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}*/
		long millis = -1;
		swt = InterceptorProxy.beforeUpdate(runtime, random, run, dest, data, configs, columns);
		if (swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.beforeUpdate(runtime, random, run, dest, data, columns);
		}
		if (swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		result = update(runtime, random, dest, data, configs, run);
		cmd_success = true;
		millis = System.currentTimeMillis() - fr;
		if (null != dmListener) {
			dmListener.afterUpdate(runtime, random, run, result, dest, data, columns, cmd_success, result, millis);
		}
		InterceptorProxy.afterUpdate(runtime, random, run, dest, data, configs, columns, cmd_success, result, System.currentTimeMillis() - fr);
		return result;
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
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		Run run = null;
		if(null == obj) {
			return null;
		}
		if(null == dest) {
			dest = DataSourceUtil.parseDest(null, obj, configs);
		}
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
		if(null != columns) {
			for(String column:columns) {
				cols.put(column.toUpperCase(), new Column(column));
			}
		}
		if(obj instanceof DataRow) {
		}else if(obj instanceof Map) {
			obj = new DataRow((Map)obj);
		}
		if(obj instanceof Collection) {
			run = buildUpdateRunFromCollection(runtime, batch, dest, (Collection)obj, configs, cols);
		}else if(obj instanceof DataRow) {
			run = buildUpdateRunFromDataRow(runtime, dest, (DataRow)obj, configs, cols);
		}else{
			run = buildUpdateRunFromEntity(runtime, dest, obj, configs, cols);
		}
		convert(runtime, configs, run);
		buildUpdateRunLimit(runtime, run);
		return run;
	}

	/**
	 *
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return Run
	 */
	public Run buildUpdateRunLimit(DataRuntime runtime, Run run){
		return run;
	}

	/**
	 *
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj Entity或DtaRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run
	 */
	@Override
	public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		TableRun run = new TableRun(runtime, dest);
		run.setFrom(2);
		StringBuilder builder = run.getBuilder();
		// List<Object> values = new ArrayList<Object>();
		List<String> list = new ArrayList<>();
		for(Column column:columns.values()) {
			list.add(column.getName());
		}
		LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, obj, configs, list);

		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		List<String> primaryKeys = configs.keys();
		if(primaryKeys.isEmpty()) {
			if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
				primaryKeys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
			} else {
				primaryKeys = new ArrayList<>();
				primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
			}
		}
		/*if(primaryKeys.isEmpty()) {
			throw new SQLUpdateException("[更新异常][更新条件为空, update方法不支持更新整表操作]");
		}*/
		// 不更新主键 除非显示指定
		LinkedHashMap<String, Column> configColumns = configs.getColumns();
		for(String pk:primaryKeys) {
			pk = pk.toUpperCase();
			if(!columns.containsKey(pk) && !columns.containsKey("+"+pk) && !configColumns.containsKey(pk)) {
				cols.remove(pk.toUpperCase());
			}
		}
		//不更新默认主键  除非显示指定
		String defaultPk = DataRow.DEFAULT_PRIMARY_KEY.toUpperCase();
		if(!columns.containsKey(defaultPk) && !columns.containsKey("+"+defaultPk) && !configColumns.containsKey(defaultPk)) {
			cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
		}
		boolean isReplaceEmptyNull = ConfigStore.IS_REPLACE_EMPTY_NULL(configs);
		cols = checkMetadata(runtime, dest, configs, cols);

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/
		if(!cols.isEmpty()) {
			builder.append("UPDATE ");
			name(runtime, builder, dest);
			builder.append(" SET").append(BR_TAB);
			boolean first = true;
			for(Column column:cols.values()) {
				String key = column.getName();
				Object value = null;
				if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
					Field field = EntityAdapterProxy.field(obj.getClass(), key);
					value = BeanUtil.getFieldValue(obj, field);
				}else {
					value = BeanUtil.getFieldValue(obj, key);
				}
				//if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}")) {
				if(BasicUtil.checkEl(value+"")) {
					String str = value.toString();
					value = str.substring(2, str.length()-1);

					if(!first) {
						builder.append(",");
					}
					delimiter(builder, key).append(" = ").append(value).append(BR_TAB);
					first = false;
				}else{
					if("NULL".equals(value)) {
						value = null;
					}else if("".equals(value) && isReplaceEmptyNull) {
						value = null;
					}
					boolean chk = true;
                   /* if(null == value) {
                        if(!IS_UPDATE_NULL_FIELD(configs)) {
                            chk = false;
                        }
                    }else */if("".equals(value)) {
						if(!ConfigStore.IS_UPDATE_EMPTY_FIELD(configs)) {
							chk = false;
						}
					}
					if(chk) {
						if(!first) {
							builder.append(",");
						}
						first = false;
						delimiter(builder, key).append(" = ?").append(BR_TAB);
						updateColumns.add(key);
						Compare compare = Compare.EQUAL;
						if(isMultipleValue(runtime, run, key)) {
							compare = Compare.IN;
						}
						addRunValue(runtime, run, compare, column, value);
					}
				}
			}
			builder.append(BR);
			if(configs.isEmptyCondition()) {
				for (String pk : primaryKeys) {
					if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
						Field field = EntityAdapterProxy.field(obj.getClass(), pk);
						configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, pk, BeanUtil.getFieldValue(obj, field));
					} else {
						configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, pk, BeanUtil.getFieldValue(obj, pk));
					}
				}
			}

			//builder.append("\nWHERE 1=1").append(BR_TAB);
			run.setConfigStore(configs);
			run.init();
			run.appendCondition(this, true, true);
		}
		run.setUpdateColumns(updateColumns);

		return run;
	}

	/**
	 *
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param row DtaRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run
	 */
	@Override
	public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		//注意columns中可能含 +-号
		TableRun run = new TableRun(runtime, dest);
		run.setFrom(1);
		StringBuilder builder = run.getBuilder();

		// List<Object> values = new ArrayList<Object>();
		/*确定需要更新的列*/
		LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, row, configs, Column.names(columns));

		if(null == configs) {
			configs = new DefaultConfigStore();
		}
		List<String> primaryKeys = configs.keys();
		if(primaryKeys.isEmpty()) {
			primaryKeys.addAll(row.getPrimaryKeys());
		}
		if(primaryKeys.isEmpty()) {
			throw new CommandUpdateException("[更新异常][更新条件为空, update方法不支持更新整表操作]");
		}
		if(configs.isEmptyCondition()) {
			//没有其他条件时添加 主键作条件
			for (String pk : primaryKeys) {
				Object pv = row.get(pk);
				pv = convert(runtime, cols.get(pk.toUpperCase()), pv); //统一调用
				if (null != pv) {
					configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, pk, pv);
				}
                /*builder.append(" AND ");
                delimiter(builder, pk).append(" = ?");
                updateColumns.add(pk);
                addRunValue(runtime, run, Compare.EQUAL, new Column(pk), row.get(pk));*/
			}
		}

		// 不更新主键 除非显示指定
		LinkedHashMap<String, Column> configColumns = configs.getColumns();
		for(String pk:primaryKeys) {
			pk = pk.toUpperCase();
			if(!columns.containsKey(pk) && !columns.containsKey("+"+pk) && !configColumns.containsKey(pk)) {
				cols.remove(pk.toUpperCase());
			}
		}
		//不更新默认主键  除非显示指定
		String defaultPk = DataRow.DEFAULT_PRIMARY_KEY.toUpperCase();
		if(!columns.containsKey(defaultPk) && !columns.containsKey("+"+defaultPk) && !configColumns.containsKey(defaultPk)) {
			cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
		}

		boolean replaceEmptyNull = row.isReplaceEmptyNull();

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/

		if(!cols.isEmpty()) {
			builder.append("UPDATE ");
			name(runtime, builder, dest);
			builder.append(" SET").append(BR_TAB);
			boolean first = true;
			for(Column col:cols.values()) {
				String key = col.getName();
				Object value = row.get(key);
				if(!first) {
					builder.append(",");
				}
				first = false;
				//if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ) {
				if(BasicUtil.checkEl(value+"")) {
					String str = value.toString();
					value = str.substring(2, str.length()-1);
					delimiter(builder, key).append(" = ").append(value).append(BR_TAB);
				}else{
					delimiter(builder, key).append(" = ?").append(BR_TAB);
					if("NULL".equals(value)) {
						value = null;
					}else if("".equals(value) && replaceEmptyNull) {
						value = null;
					}
					updateColumns.add(key);
					Compare compare = Compare.EQUAL;
					addRunValue(runtime, run, compare, col, value);
				}
			}
			builder.append(BR);
			//builder.append("\nWHERE 1=1").append(BR_TAB);
			run.setConfigStore(configs);
			run.init();
			run.appendCondition(this, true, true);
		}
		run.setUpdateColumns(updateColumns);
		return run;
	}

	/**
	 *
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list Collection
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run
	 */
	@Override
	public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		TableRun run = new TableRun(runtime, dest);
		run.setFrom(1);
		if (null == list || list.isEmpty()) {
			return run;
		}
		if(null == configs) {
			configs = new DefaultConfigStore();
		}

		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
		List<String> primaryKeys = new ArrayList<>();

		boolean replaceEmptyNull = false;
		for(Object item:list) {
			if(item instanceof DataRow) {
				DataRow row = (DataRow)item;
				primaryKeys = row.getPrimaryKeys();
				cols.putAll(confirmUpdateColumns(runtime, dest, row, configs, Column.names(columns)));
				replaceEmptyNull = row.isReplaceEmptyNull();
			}else{
				List<String> ll = new ArrayList<>();
				for(Column column:columns.values()) {
					ll.add(column.getName());
				}
				cols.putAll(confirmUpdateColumns(runtime, dest, item, configs, ll));
				if(EntityAdapterProxy.hasAdapter(item.getClass())) {
					primaryKeys.addAll(EntityAdapterProxy.primaryKeys(item.getClass()).keySet());
				}else{
					primaryKeys = new ArrayList<>();
					primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
				}
				replaceEmptyNull = ConfigStore.IS_REPLACE_EMPTY_NULL(configs);
			}
			if(!ConfigTable.IS_CHECK_ALL_UPDATE_COLUMN) {
				break;
			}
		}
		cols = checkMetadata(runtime, dest, configs, cols);
		StringBuilder builder = run.getBuilder();
		List<String> keys = configs.keys();
		if(!keys.isEmpty()) {
			primaryKeys = keys;
		}
		if(primaryKeys.isEmpty()) {
			throw new CommandUpdateException("[更新异常][更新条件为空, update方法不支持更新整表操作]");
		}
		LinkedHashMap<String, Column> configColumns = configs.getColumns();
		// 不更新主键 除非显示指定
		for(String pk:primaryKeys) {
			pk = pk.toUpperCase();
			if(!columns.containsKey(pk) && !columns.containsKey("+"+pk) && !configColumns.containsKey(pk)) {
				cols.remove(pk.toUpperCase());
			}
		}
		//不更新默认主键  除非显示指定
		String defaultPk = DataRow.DEFAULT_PRIMARY_KEY.toUpperCase();
		if(!columns.containsKey(defaultPk) && !columns.containsKey("+"+defaultPk)&& !configColumns.containsKey(defaultPk)) {
			cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
		}
		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/

		if(!cols.isEmpty()) {
			builder.append("UPDATE ");
			name(runtime, builder, dest);
			builder.append(" SET ");
			boolean start = true;
			for(Column col:cols.values()) {
				String key = col.getName();
				if(!start) {
					builder.append(",");
				}
				start = false;
				builder.append(key);
				builder.append(" = ?");
			}
			start = true;
			for (String pk : primaryKeys) {
				if(start) {
					builder.append(" WHERE ");
				}else {
					builder.append(" AND ");
				}
				delimiter(builder, pk).append(" = ?");
				start = false;
			}
		}
		run.setUpdateColumns(updateColumns);
		List<RunValue> values = new ArrayList<>();
		for(Object item:list) {
			for(Column col:cols.values()) {
				String key = col.getName();
				Object value = BeanUtil.getFieldValue(item, key);
				//if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ) {
				if(BasicUtil.checkEl(value+"")) {
					String str = value.toString();
					value = str.substring(2, str.length()-1);
				}else{
					if("NULL".equals(value)) {
						value = null;
					}else if("".equals(value) && replaceEmptyNull) {
						value = null;
					}
				}
				values.add(new RunValue(key, value));
			}

			for (String pk : primaryKeys) {
				values.add(new RunValue(pk, BeanUtil.getFieldValue(item, pk)));
			}
		}
		run.setBatch(batch);
		run.setVol(cols.size() + primaryKeys.size());
		run.setRunValues(values);
		return run;
	}

	/**
	 * 确认需要更新的列
	 * @param dest 表
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
	@Override
	public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns) {
		LinkedHashMap<String, Column> cols = null;/*确定需要更新的列*/
		if(null == row) {
			return new LinkedHashMap<>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> conditions = new ArrayList<>()							; // 更新条件
		if(null == columns || columns.isEmpty()) {
			if(null != configs) {
				columns = configs.columns();
			}
		}
		LinkedHashMap<String, Column> masters = row.getUpdateColumns(true)		; // 必须更新列
		List<String> ignores = BeanUtil.copy(row.getIgnoreUpdateColumns())	; // 必须不更新列
		List<String> factKeys = new ArrayList<>()							; // 根据是否空值
		BeanUtil.removeAll(ignores, columns);
		if(null != configs) {
			BeanUtil.removeAll(configs.excludes(), columns);
		}
		if(null != columns && columns.size()>0) {
			each = false;
			cols = new LinkedHashMap<>();
			for(String column:columns) {
				if(BasicUtil.isEmpty(column)) {
					continue;
				}
				if(column.startsWith("+")) {
					column = column.substring(1);
					masters.put(column.toUpperCase(), new Column(column));
					each = true;
				}else if(column.startsWith("-")) {
					column = column.substring(1);
					ignores.add(column);
					each = true;
				}else if(column.startsWith("?")) {
					column = column.substring(1);
					factKeys.add(column);
					each = true;
				}
				cols.put(column.toUpperCase(), new Column(column));
			}
		}else if(null != masters && masters.size()>0) {
			each = false;
			cols = masters;
		}
		if(each) {
			cols = row.getUpdateColumns(true);
			cols.putAll(masters);
			// 是否更新null及""列
			boolean isUpdateNullColumn = row.isUpdateNullColumn();
			boolean isUpdateEmptyColumn = row.isUpdateEmptyColumn();
			List<String> keys = Column.names(cols);
			for(String key:keys) {
				if(masters.containsKey(key)) {
					// 必须更新
					continue;
				}

				Object value = row.get(key);
				if(null == value) {
					if(factKeys.contains(key)) {
						cols.remove(key.toUpperCase());
						continue;
					}
					if(!isUpdateNullColumn) {
						cols.remove(key.toUpperCase());
						continue;
					}
				}else if("".equals(value.toString().trim())) {
					if(factKeys.contains(key)) {
						cols.remove(key.toUpperCase());
						continue;
					}
					if(!isUpdateEmptyColumn) {
						cols.remove(key.toUpperCase());
						continue;
					}
				}

			}
		}
		if(null != ignores) {
			for(String ignore:ignores) {
				cols.remove(ignore.toUpperCase());
			}
		}
		cols = checkMetadata(runtime, dest, configs, cols);
		return cols;
	}

	/**
	 * 确认需要更新的列
	 * @param dest 表
	 * @param obj Object
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
	public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		LinkedHashMap<String, Column> cols = null;/*确定需要更新的列*/
		if(null == obj) {
			return new LinkedHashMap<>();
		}

		if(obj instanceof Map && !(obj instanceof DataRow)) {
			obj = new DataRow(KeyAdapter.KEY_CASE.SRC, (Map)obj);
		}
		boolean each = true;//是否需要从row中查找列
		List<String> conditions = new ArrayList<>()							; // 更新条件
		LinkedHashMap<String, Column> masters = new LinkedHashMap<>()		; // 必须更新列
		List<String> ignores = new ArrayList<>()	; // 必须不更新列
		List<String> factKeys = new ArrayList<>()							; // 根据是否空值
		BeanUtil.removeAll(ignores, columns);

		if(null != columns && columns.size()>0) {
			each = false;
			cols = new LinkedHashMap<>();
			for(String column:columns) {
				if(BasicUtil.isEmpty(column)) {
					continue;
				}
				if(column.startsWith("+")) {
					column = column.substring(1);
					masters.put(column.toUpperCase(), new Column(column));
					each = true;
				}else if(column.startsWith("-")) {
					column = column.substring(1);
					ignores.add(column);
					each = true;
				}else if(column.startsWith("?")) {
					column = column.substring(1);
					factKeys.add(column);
					each = true;
				}
				cols.put(column.toUpperCase(), new Column(column));
			}
		}else if(null != masters && masters.size()>0) {
			each = false;
			cols = masters;
		}
		if(each) {
			// 是否更新null及""列
			boolean isUpdateNullColumn = ConfigStore.IS_UPDATE_NULL_FIELD(configs);
			boolean isUpdateEmptyColumn = ConfigStore.IS_UPDATE_EMPTY_FIELD(configs);
			cols = new LinkedHashMap<>();
			if(obj instanceof DataRow) {
				DataRow row = (DataRow)obj;
				masters.putAll(row.getUpdateColumns(true));
				ignores.addAll(row.getIgnoreUpdateColumns());
				cols = row.getColumns();
				isUpdateNullColumn = row.isUpdateNullColumn();
				isUpdateEmptyColumn = row.isUpdateEmptyColumn();
			} else {
				cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.UPDATE)); ;
			}
			cols.putAll(masters);
			List<String> keys = Column.names(cols);
			for(String key:keys) {
				if(masters.containsKey(key)) {
					// 必须更新
					continue;
				}
				Object value = BeanUtil.getFieldValue(obj, key);
				if(null == value) {
					if(factKeys.contains(key)) {
						cols.remove(key.toUpperCase());
						continue;
					}
					if(!isUpdateNullColumn) {
						cols.remove(key.toUpperCase());
						continue;
					}
				}else if("".equals(value.toString().trim())) {
					if(factKeys.contains(key)) {
						cols.remove(key.toUpperCase());
						continue;
					}
					if(!isUpdateEmptyColumn) {
						cols.remove(key.toUpperCase());
						continue;
					}
				}

			}
		}
		if(null != ignores) {
			for(String ignore:ignores) {
				cols.remove(ignore.toUpperCase());
			}
		}
		cols = checkMetadata(runtime, dest, configs, cols);
		return cols;
	}

	/**
	 * update [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return 影响行数
	 */
	public long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) {
		long result = 0;
		if(!run.isValid()) {
			if(log.isWarnEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		if(run.isEmpty()) {
			log.warn("[不具备执行条件][dest:{}]", dest);
			return -1;
		}
		if(null != configs) {
			configs.add(run);
		}
		List<Object> values = run.getValues();
		int batch = run.getBatch();
		String action = null;
		if(null != run.action()){
			action = run.action().toString();
		}
		if(batch > 1) {
			action = "batch " + action;
		}
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		if (log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
			log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.UPDATE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
		}

		boolean exe = true;
		if(null != configs) {
			exe = configs.execute();
		}
		if(!exe) {
			return -1;
		}
		long millis = -1;
		try{
			result = actuator.update(this, runtime, random, dest, data, configs, run);
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
				if(millis > SLOW_SQL_MILLIS) {
					slow = true;
					log.warn("{}[slow cmd][action:{}][{}][执行耗时:{}]{}", random, action, run.getTable(), DateUtil.format(millis), run.log(ACTION.DML.UPDATE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener) {
						dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, run.getFinalUpdate(), values, null, true, result, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
				String qty = result+"";
				if(batch>1) {
					qty = "约"+result;
				}
				log.info("{}[action:{}][{}][执行耗时:{}][影响行数:{}]", random, action, run.getTable(), DateUtil.format(millis), LogUtil.format(qty, 34));
			}

		}catch(Exception e) {
			if (ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				log.error("update 异常:", e);
			}
			if (ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				CommandUpdateException ex = new CommandUpdateException("update异常:" + e.toString(), e);
				ex.setValues(values);
				throw ex;
			}
			if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.error("{}[{}][action:][{}]{}", random, action, run.getTable(), LogUtil.format("更新异常:", 33) + e.toString(), run.log(ACTION.DML.UPDATE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}

		}
		return result;
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
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
		if(null == random) {
			random = random(runtime);
		}
		if(null == data) {
			if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				throw new CommandUpdateException("save空数据");
			}else {
				log.error("save空数据");
				return -1;
			}
		}
		if(data instanceof Collection) {
			Collection<?> items = (Collection<?>)data;
			long cnt = 0;
			for (Object item : items) {
				cnt += save(runtime, random, dest, item, configs, columns);
			}
			return cnt;
		}
		return saveObject(runtime, random, dest, data, configs, columns);
	}

	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 保存的数据
	 * @param configs ConfigStore
	 * @param columns 指定列
	 * @return long 影响行数
	 */
	protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns) {
		long cnt = 0;
		//List<Run> runs = buildInsertRun(runtime, random, batch, dest, data, columns);
		return cnt;
	}
	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 保存的数据
	 * @param configs ConfigStore
	 * @param columns 指定列
	 * @return long 影响行数
	 */
	protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
		if(null == data) {
			return 0;
		}
		boolean isNew = BeanUtil.checkIsNew(data);
		if(isNew) {
			return insert(runtime, random, 0, dest, data, configs, columns);
		}else{
			//是否覆盖(null:不检测直接执行update有可能影响行数=0)
			Boolean override = checkOverride(data);
			ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
			if(null != override) {
				RunPrepare prepare = new DefaultTablePrepare(dest);
				Map<String, Object> pvs = checkPv(data);
				ConfigStore stores = new DefaultConfigStore();
				for(String k:pvs.keySet()) {
					stores.and(k, pvs.get(k));
				}
				boolean exists = exists(runtime, random, prepare, stores);
				if(exists) {
					if(override) {
						return update(runtime, random, dest, data, configs, columns);
					}else{
						log.warn("[跳过更新][数据已存在:{}({})]", dest, BeanUtil.map2json(pvs));
					}
				}else{
					return insert(runtime, random, 0, dest, data, configs, columns);
				}
			}else{
				return update(runtime, random, dest, data, configs, columns);
			}
		}
		return 0;
	}

	/**
	 * 有主键值的情况下 检测覆盖
	 * @param obj Object
	 * @return boolean
	 * null:正常执行update<br/>
	 * true或false:检测数据库中是否存在<br/>
	 * 如果数据库中存在匹配的数据<br/>
	 * true:执行更新<br/>
	 * false:跳过更新<br/>
	 */
	protected Boolean checkOverride(Object obj) {
		Boolean result = null;
		if(null != obj && obj instanceof DataRow) {
			result = ((DataRow)obj).getOverride();
		}
		return result;
	}

	/**
	 * 检测主键值
	 * @param obj Object
	 * @return Map
	 */
	protected Map<String, Object> checkPv(Object obj) {
		Map<String, Object> pvs = new HashMap<>();
		if(null != obj && obj instanceof DataRow) {
			DataRow row = (DataRow) obj;
			List<String> ks = row.getPrimaryKeys();
			for(String k:ks) {
				pvs.put(k, row.get(k));
			}
		}
		return pvs;
	}

	/**
	 * 是否是可以接收数组类型的值
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param key key
	 * @return boolean
	 */
	protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key) {
		Table table = run.getTable();
		if (null != table) {
			LinkedHashMap<String, Column> columns = columns(runtime, null, false, table, false);
			if(null != columns) {
				Column column = columns.get(key.toUpperCase());
				return isMultipleValue(column);
			}
		}
		return false;
	}

	/**
	 * 是否支持集合值
	 * @param column Column 根据column的数据类型
	 * @return boolean
	 */
	protected boolean isMultipleValue(Column column) {
		if(null != column) {
			String type = column.getTypeName().toUpperCase();
			if(type.contains("POINT") || type.contains("GEOMETRY") || type.contains("POLYGON")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 过滤掉表结构中不存在的列
	 * @param table 表
	 * @param columns columns
	 * @return List
	 */
	public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		if(!ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
			return columns;
		}
		LinkedHashMap<String, Column> result = new LinkedHashMap<>();
		LinkedHashMap<String, Column> metadatas = columns(runtime, null, false, table, false);
		try {
			LinkedHashMap<String, Tag> tags = tags(runtime, null,false, table);
			metadatas.putAll(tags);
		}catch (Exception e){}
		//tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)
		if(!metadatas.isEmpty()) {
			for (String key:columns.keySet()) {
				if (metadatas.containsKey(key)) {
					result.put(key, metadatas.get(key));
				} else {
					if(ConfigStore.IS_LOG_SQL_WARN(configs)) {
						log.warn("[{}][column:{}.{}][insert/update忽略当前列名]", LogUtil.format("列名检测不存在", 33), table, key);
					}
				}
			}
		}else{
			if(ConfigStore.IS_LOG_SQL_WARN(configs)) {
				log.warn("[{}][table:{}][忽略列名检测]", LogUtil.format("表结构检测失败(检查表名是否存在)", 33), table);
			}
		}
		if(ConfigStore.IS_LOG_SQL_WARN(configs)) {
			log.info("[check column metadata][origin:{}][result:{}]", columns.size(), result.size());
		}
		return result;
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
		DataSet set = null;
		Long fr = 0L;
		boolean cmd_success = false;
		Run run = null;
		PageNavi navi = null;

		if(null == random) {
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return new DataSet().setTable(prepare.getTable());
		}
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK) {
			return new DataSet().setTable(prepare.getTable());
		}

		run = buildQueryRun(runtime, prepare, configs, conditions);

		if (log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs) && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			String src = "";
			if (prepare instanceof TablePrepare) {
				src = prepare.getTableName();
			} else {
				src = prepare.getText();
			}
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
			log.warn(tmp);
		}
		navi = run.getPageNavi();
		if(null == navi && null != configs){
			navi = configs.getPageNavi();
		}
		long total = 0;
		Boolean autoCount = false;
		if (run.isValid()) {
			if (null != navi) {
				autoCount = navi.autoCount();//未设置或true时查询总数
				if(null == autoCount) {
					autoCount = true;
				}
				if(autoCount) {
					if (null != dmListener) {
						dmListener.beforeTotal(runtime, random, run);
					}
					fr = System.currentTimeMillis();
					if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
						// 第一条 query中设置的标识(只查一行)
						total = 1;
					} else {
						// 未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = count(runtime, random, run);
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
					if (null != dmListener) {
						dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
					}
					if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
						log.info("[查询记录总数][行数:{}]", total);
					}
				}
			}
		}
		fr = System.currentTimeMillis();
		if (run.isValid()) {
			if(null == navi || total > 0 || !autoCount) {
				if(null != dmListener) {
					dmListener.beforeQuery(runtime, random, run, total);
				}
				swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
				if(swt == ACTION.SWITCH.BREAK) {
					return new DataSet().setTable(prepare.getTable());
				}
				set = select(runtime, random, false, prepare.getTable(), configs, run);
				cmd_success = true;
			}else{
				if(null != configs) {
					configs.add(run);
				}
				set = new DataSet().setTable(prepare.getTable());
				if(ConfigStore.IS_CHECK_EMPTY_SET_METADATA(configs)) {
					set.setMetadata(metadata(runtime, prepare, false));
				}
			}
		} else {
			set = new DataSet().setTable(prepare.getTable());
		}

		set.setDest(prepare.getDest());
		set.setNavi(navi);
		if (null != navi && navi.isLazy()) {
			navi.setDataSize(set.size());
			PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
		}

		if(null != dmListener) {
			dmListener.afterQuery(runtime, random, run, cmd_success, set, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, cmd_success, set, navi, System.currentTimeMillis() - fr);
		return set;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)", 37));
		}
		return new DataSet();
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
		if(null == prepare) {
			prepare = new DefaultTablePrepare();
		}
		EntitySet<T> list = null;
		Long fr = System.currentTimeMillis();
		Run run = null;
		boolean cmd_success = false;
		PageNavi navi = null;

		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return new EntitySet();
		}
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK) {
			return new EntitySet();
		}

		if(BasicUtil.isEmpty(prepare.getDest())) {
			//text xml格式的 不检测表名，避免一下步根据表名检测表结构
			if(prepare instanceof TextPrepare || prepare instanceof XMLPrepare) {
			}else {
				prepare.setDest(EntityAdapterProxy.table(clazz, true));
			}
		}

		run = buildQueryRun(runtime, prepare, configs, conditions);
		if (log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs) && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
			log.warn(tmp);
		}
		navi = run.getPageNavi();
		long total = 0;
		Boolean autoCount = false;
		if (run.isValid()) {
			if (null != navi) {
				autoCount = navi.autoCount();//未设置或true时查询总数
				if(null == autoCount) {
					autoCount = true;
				}
				if(autoCount) {
					if (null != dmListener) {
						dmListener.beforeTotal(runtime, random, run);
					}
					fr = System.currentTimeMillis();
					if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
						// 第一条 query中设置的标识(只查一行)
						total = 1;
					}  else {
						// 未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = count(runtime, random, run);
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
					if (null != dmListener) {
						dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
					}
				}
			}
			if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
				log.info("[查询记录总数][行数:{}]", total);
			}
		}
		fr = System.currentTimeMillis();
		if (run.isValid()) {
			if(null == navi || !autoCount || total > 0) {
				swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
				if(swt == ACTION.SWITCH.BREAK) {
					return new EntitySet();
				}
				if (null != dmListener) {
					dmListener.beforeQuery(runtime, random, run, total);
				}
				fr = System.currentTimeMillis();
				list = select(runtime, random, clazz, run.getTable(), configs, run);
				cmd_success = false;
			}else{
				list = new EntitySet<>();
			}
		} else {
			list = new EntitySet<>();
		}
		list.setNavi(navi);
		if (null != navi && navi.isLazy()) {
			PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
		}

		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, cmd_success, list, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, cmd_success, list, navi, System.currentTimeMillis() - fr);
		return list;
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
	protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, Table table, ConfigStore configs, Run run) {
		EntitySet<T> set = new EntitySet<>();
		if(null == random) {
			random = random(runtime);
		}
		if(null != configs) {
			configs.entityClass(clazz);
		}
		DataSet rows = select(runtime, random, false, table, configs, run);
		for(DataRow row:rows) {
			T entity = null;
			if(EntityAdapterProxy.hasAdapter(clazz)) {
				//jdbc adapter需要参与 或者metadata里添加colun type
				entity = EntityAdapterProxy.entity(clazz, row, null);
			}else{
				entity = row.entity(clazz);
			}
			set.add(entity);
		}

		return set;
	}

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
	@Override
	public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		List<Map<String, Object>> maps = null;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		Run run = null;
		if(null == random) {
			random = random(runtime);
		}
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK) {
			return new ArrayList<>();
		}

		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return new ArrayList<>();
		}
		run = buildQueryRun(runtime, prepare, configs, conditions);
		Long fr = System.currentTimeMillis();
		if (log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs) && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			String src = "";
			if (prepare instanceof TablePrepare) {
				src = prepare.getTableName();
			} else {
				src = prepare.getText();
			}
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
			log.warn(tmp);
		}
		if (run.isValid()) {
			swt = InterceptorProxy.beforeQuery(runtime, random, run, null);
			if(swt == ACTION.SWITCH.BREAK) {
				return new ArrayList<>();
			}
			if (null != dmListener) {
				dmListener.beforeQuery(runtime, random, run, -1);
			}
			if(null != configs) {
				PageNavi navi = configs.getPageNavi();
				if(null != navi) {
					Boolean autoCount = navi.autoCount();
					if (null != autoCount && autoCount) {
						long total = count(runtime, random, run);
						navi.setTotalRow(total);
					}
				}

			}

			maps = maps(runtime, random, configs, run);
			cmd_success = true;
		} else {
			maps = new ArrayList<>();
		}

		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, cmd_success, maps, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, cmd_success, maps, null, System.currentTimeMillis() - fr);
		return maps;
	}

	/**
	 * select[命令合成]<br/> 最终可执行命令<br/>
	 * 创建查询SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		Run run = initQueryRun(runtime, prepare);
		init(runtime, run, configs, conditions);
		List<Run> unions = run.getUnions();
		if(null != unions) {
			for(Run union:unions) {
				init(runtime, union, configs, conditions);
			}
		}
		if(run.checkValid()) {
			//构造最终的查询SQL
			run = fillQueryContent(runtime, run);
		}

		return run;
	}

	/**
	 * query run初始化,检测占位符、忽略不存在的列等
	 * select[命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 */
	public void init(DataRuntime runtime, Run run, ConfigStore configs, String ... conditions) {
		if(null != run) {
			RunPrepare prepare = run.getPrepare();
			if(prepare instanceof TablePrepare) {
				likes(runtime, prepare.getTable(), configs);
			}
			//如果是text类型 将解析文本并抽取出变量
			if(run instanceof TextRun) {
				parseText(runtime, (TextRun)run);
			}
			run.addConfigStore(configs);
			configs = run.getConfigs();
			//先把configs中的占位值取出
			if(null != configs) {
				List<Object> statics = configs.getStaticValues();
				for (Object item : statics) {
					run.addValue(new RunValue("none", item));
				}
			}

			run.addCondition(conditions);

			if(run.checkValid()) {
				//为变量赋值 run.condition赋值
				run.init();
				//检测不存在的列
				if(ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
					List<Join> joins = prepare.getJoins();
					Table table = run.getTable();
					if(null != table && (null == joins || joins.isEmpty())) {//TODO 单表时再检测
						LinkedHashMap<String, Column> metadatas = columns(runtime, null, false, table, false);
						//检测不存在的列
						OrderStore orders = run.getOrderStore();
						if (null != orders) {
							orders.filter(metadatas);
						}
						if(null != prepare) {
							prepare.filter(metadatas);
						}
						ConditionChain chain = run.getConditionChain();
						if(null != chain) {
							chain.filter(metadatas);
						}
						if(null != configs) {
							configs.filter(metadatas);
						}
					}
				}
			}
		}
	}
	/**
	 * 解析文本中的占位符
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	public void parseText(DataRuntime runtime, TextRun run) {
		String text = run.getPrepare().getText();
		if(null == text) {
			return;
		}
		try{
			int varType = -1;
			Compare compare = Compare.EQUAL;

			List<List<String>> keys = null;
			int type = 0;
			// AND CD = {CD} || CD LIKE '%{CD}%' || CD IN ({CD}) || CD = ${CD} || CD = #{CD}
			//{CD} 用来兼容旧版本，新版本中不要用，避免与josn格式冲突
			keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX, Regular.MATCH_MODE.CONTAIN);
			type = Variable.KEY_TYPE_SIGN_V2 ;
			//::KEY 格式的占位符解析,在PG环境中会与 ::INT8 格式冲突 需要禁用
			if(keys.isEmpty() && ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT && supportSqlVarPlaceholderRegexExt(runtime)) {
				// AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD
				keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX_EXT, Regular.MATCH_MODE.CONTAIN);
				type = Variable.KEY_TYPE_SIGN_V1 ;
			}
			if(BasicUtil.isNotEmpty(true, keys)) {
				// AND CD = :CD
				for(int i=0; i<keys.size();i++) {
					List<String> keyItem = keys.get(i);

					Variable var = SyntaxHelper.buildVariable(type, keyItem.get(0), keyItem.get(1), keyItem.get(2), keyItem.get(3));
					if(null == var) {
						continue;
					}
					var.setSwt(Compare.EMPTY_VALUE_SWITCH.NULL);
					run.addVariable(var);
				}// end for
			}else{
				// AND CD = ?
				int qty = SQLUtil.countPlaceholder(text);
				if(qty > 0) {
					for(int i=0; i<qty; i++) {
						Variable var = new DefaultVariable();
						var.setType(Variable.VAR_TYPE_INDEX);
						var.setSwt(Compare.EMPTY_VALUE_SWITCH.NULL);
						run.addVariable(var);
					}
				}
			}
		}catch(Exception e) {
			log.error("parse text exception:", e);
		}
	}
	private void likes(DataRuntime runtime, Table table, ConfigStore configs) {
		if(null == table || null == configs) {
			return;
		}
		List<Config> list = configs.getConfigChain().getConfigs();
		for(Config config:list) {
			if(config.getCompare() == Compare.LIKES) {
				LinkedHashMap<String, Column> colums = columns(runtime, null, false, table, false);
				list.remove(config);
				ConfigStore ors = new DefaultConfigStore();
				List<Object> values = config.getValues();
				Object value = null;
				if(null != values && !values.isEmpty()) {
					value = values.get(0);
				}
				for(Column column:colums.values()) {
					TypeMetadata tm = column.getTypeMetadata();
					if(null != tm && tm.getCategoryGroup() == TypeMetadata.CATEGORY_GROUP.STRING) {
						ors.or(Compare.LIKE, column.getName(), value);
					}
				}
				configs.and(ors);
				break;
			}
		}
	}

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体, 中间过程有可能转换类型
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public Run fillQueryContent(DataRuntime runtime, Run run) {
		return fillQueryContent(runtime, run.getBuilder(), run);
	}
	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder 有可能合个run合成一个 所以提供一个共用builder
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public Run fillQueryContent(DataRuntime runtime, StringBuilder builder, Run run) {
		if(null != run) {
			if(run instanceof TableRun) {
				run = fillQueryContent(runtime, builder, (TableRun) run);
			}else if(run instanceof XMLRun) {
				run = fillQueryContent(runtime, builder, (XMLRun) run);
			}else if(run instanceof TextRun) {
				run = fillQueryContent(runtime, builder, (TextRun) run);
			}
			convert(runtime, run.getConfigs(), run);
		}
		return run;
	}
	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run XMLRun
	 */
	protected Run fillQueryContent(DataRuntime runtime, XMLRun run) {
		return fillQueryContent(runtime, run.getBuilder(), run);
	}
	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder 有可能合个run合成一个 所以提供一个共用builder
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, XMLRun run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillQueryContent(DataRuntime runtime, XMLRun run)", 37));
		}
		return run;
	}
	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run TextRun
	 */
	protected Run fillQueryContent(DataRuntime runtime, TextRun run) {
		return fillQueryContent(runtime, run.getBuilder(), run);
	}

	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder 有可能合个run合成一个 所以提供一个共用builder
	 * @param run TextRun
	 */
	protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TextRun run) {
		replaceVariable(runtime, run);
		run.appendCondition(true);
		run.appendGroup();
		// appendOrderStore();
		run.checkValid();
		return run;
	}

	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run TextRun
	 */
	protected Run fillQueryContent(DataRuntime runtime, TableRun run) {
		return fillQueryContent(runtime, run.getBuilder(), run);
	}
	/**
	 * 有些非JDBC环境也需要用到SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder 有可能合个run合成一个 所以提供一个共用builder
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TableRun run) {
		TablePrepare sql = (TablePrepare)run.getPrepare();
		builder.append("SELECT ");
		if(null != sql.getDistinct()) {
			builder.append(sql.getDistinct());
		}
		builder.append(BR_TAB);
		LinkedHashMap<String,Column> columns = sql.getColumns();
		if(null == columns || columns.isEmpty()) {
			ConfigStore configs = run.getConfigs();
			if(null != configs) {
				List<String> cols = configs.columns();
				columns = new LinkedHashMap<>();
				for(String col:cols) {
					columns.put(col.toUpperCase(), new Column(col));
				}
			}
		}
		if(null != columns && !columns.isEmpty()) {
			// 指定查询列
			boolean first = true;
			for(Column column:columns.values()) {
				if(BasicUtil.isEmpty(column) || BasicUtil.isEmpty(column.getName())) {
					continue;
				}
				if(!first) {
					builder.append(",");
				}
				first = false;
				String name = column.getName();
				//if (column.startsWith("${") && column.endsWith("}")) {
				if (BasicUtil.checkEl(name)) {
					name = name.substring(2, name.length()-1);
					builder.append(name);
				}else{
					if(name.contains("(") || name.contains(",")) {
						builder.append(name);
					}else if(name.toUpperCase().contains(" AS ")) {
						int split = name.toUpperCase().indexOf(" AS ");
						String tmp = name.substring(0, split).trim();
						delimiter(builder, tmp);
						builder.append(" ");
						tmp = name.substring(split+4).trim();
						delimiter(builder, tmp);
					}else if("*".equals(name)) {
						builder.append("*");
					}else{
						delimiter(builder, name);
					}
				}
			}
			builder.append(BR);
		}else{
			// 全部查询
			builder.append("*");
			builder.append(BR);
		}
		Table table = run.getTable();
		builder.append("FROM").append(BR_TAB);
		name(runtime, builder, table);
		String alias = table.getAlias();
		if(BasicUtil.isNotEmpty(alias)) {
			builder.append(" ");
			delimiter(builder, alias);
		}
		builder.append(BR);
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(BR_TAB).append(join.getType().getCode()).append(" ");
				Table joinTable = join.getTable();
				String joinTableAlias = joinTable.getAlias();
				name(runtime, builder, joinTable);
				if(BasicUtil.isNotEmpty(joinTableAlias)) {
					builder.append("  ");
					delimiter(builder, joinTableAlias);
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		//builder.append("\nWHERE 1=1\n\t");
		/*添加查询条件*/
		// appendConfigStore();
		run.appendCondition(builder, this, true, true);
		run.appendGroup(builder);
		return run;
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
		return run.getBaseQuery();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)", 37));
		}
		return new DataSet().setTable(table);
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
		List<Map<String, Object>> maps = null;
		if(null == random) {
			random = random(runtime);
		}
		if(null != configs) {
			configs.add(run);
		}
		String sql = run.getFinalQuery();
		List<Object> values = run.getValues();
		if(BasicUtil.isEmpty(sql)) {
			if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw new CommandQueryException("未指定命令");
			}else{
				log.error("未指定命令");
				return new ArrayList<>();
			}
		}
		if(log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
			log.info("{}[action:select]{}", random, run.log(ACTION.DML.SELECT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
		}
		boolean exe = true;
		if(null != configs) {
			exe = configs.execute();
		}
		if(!exe) {
			return new ArrayList<>();
		}
		try{
			maps = actuator.maps(this, runtime, random, configs, run);
			maps = process(runtime, maps);
		}catch(Exception e) {
			if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				log.error("maps 异常:", e);
			}
			if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e.toString(), run.log(ACTION.DML.SELECT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				CommandQueryException ex = new CommandQueryException("query异常:"+e.toString(), e);
				ex.setCmd(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return maps;
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
		Map<String, Object> map = null;
		String sql = run.getFinalExists();
		List<Object> values = run.getValues();
		if(null != configs) {
			configs.add(run);
		}
		long fr = System.currentTimeMillis();
		if (log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
			log.info("{}[action:select]{}", random, run.log(ACTION.DML.EXISTS,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
		}
		boolean exe = true;
		if(null != configs) {
			exe = configs.execute();
		}
		if(!exe) {
			return new HashMap<>();
		}
		try {
			map = actuator.map(this, runtime, random, configs, run);
		}catch (Exception e) {
			if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw new CommandQueryException("查询异常", e);
			}
			if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
					e.printStackTrace();
				}
				log.error("{}[{}][action:select][cmd:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e, sql, LogUtil.param(values));
			}
		}
		//}
		Long millis = System.currentTimeMillis() - fr;
		boolean slow = false;
		long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
		if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
			if(millis > SLOW_SQL_MILLIS) {
				slow = true;
				log.warn("{}[slow cmd][action:exists][执行耗时:{}][cmd:\n{}\n]\n[param:{}]", random, DateUtil.format(millis), sql, LogUtil.param(values));
				if(null != dmListener) {
					dmListener.slow(runtime, random, ACTION.DML.EXISTS, run, sql, values, null, true, map, millis);
				}
			}
		}
		if (!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
			log.info("{}[action:select][执行耗时:{}][封装行数:{}]", random, DateUtil.format(millis), LogUtil.format(map == null ?0:1, 34));
		}
		return map;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)", 37));
		}
		return null;
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
		return list;
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
	public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		long count = -1;
		Long fr = System.currentTimeMillis();
		Run run = null;
		if(null == random) {
			random = random(runtime);
		}

		boolean cmd_success = false;

		ACTION.SWITCH swt = InterceptorProxy.prepareCount(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		run = buildQueryRun(runtime, prepare, configs, conditions);
		if(!run.isValid()) {
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTableName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		if (null != dmListener) {
			dmListener.beforeCount(runtime, random, run);
		}
		swt = InterceptorProxy.beforeCount(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		fr = System.currentTimeMillis();
		count = count(runtime, random, run);
		cmd_success = true;

		if(null != dmListener) {
			dmListener.afterCount(runtime, random, run, cmd_success, count, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterCount(runtime, random, run, cmd_success, count, System.currentTimeMillis() - fr);
		return count;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String mergeFinalTotal(DataRuntime runtime, Run run)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long count(DataRuntime runtime, String random, Run run)", 37));
		}
		return -1;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现  exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)", 37));
		}
		return false;
	}
	/**
	 * exists [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return String
	 */
	@Override
	public String mergeFinalExists(DataRuntime runtime, Run run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String mergeFinalExists(DataRuntime runtime, Run run)", 37));
		}
		return null;
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
	public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		long result = -1;
		boolean cmd_success = false;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if(null == random) {
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareExecute(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}

		Run run = buildExecuteRun(runtime,  prepare, configs, conditions);
		if(!run.isValid()) {
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTableName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long fr = System.currentTimeMillis();

		long millis = -1;
		swt = InterceptorProxy.beforeExecute(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.beforeExecute(runtime, random, run);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		result = execute(runtime, random, configs, run);
		cmd_success = true;
		if (null != dmListener) {
			dmListener.afterExecute(runtime, random, run, cmd_success, result, millis);
		}
		InterceptorProxy.afterExecute(runtime, random, run, cmd_success, result, System.currentTimeMillis()-fr);
		return result;
	}

	/**
	 * execute [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param batch 批量执行每批最多数量
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param values  values
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values) {
		if(null == random) {
			random = random(runtime);
		}
		prepare.setBatch(batch);
		Run run = buildExecuteRun(runtime, prepare, configs);
		if(null != values && !values.isEmpty()) {
			Object first = values.iterator().next();
			if (first instanceof Collection) {
				//?下标占位
				List<Object> list = new ArrayList<>();
				int vol = 0;
				for (Object item : values) {
					Collection col = (Collection) item;
					list.addAll(col);
					vol = col.size();
				}
				run.setValues(null, list);
				run.setVol(vol);
			} else {
				//${} #{}占位
				List<Object> list = new ArrayList<>();
				List<Variable> vars = run.getVariables();
				List<String> keys = new ArrayList<>();
				run.setVol(vars.size());
				for (Variable var : vars) {
					keys.add(var.getKey());
				}
				for (Object item : values) {
					for (String key : keys) {
						Object value = BeanUtil.getFieldValue(item, key);
						list.add(value);
					}
				}
				run.setValues(null, list);
			}
		}
		return execute(runtime, random, configs, run);
	}

	/**
	 * execute [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param batch 批量执行每批最多数量
	 * @param vol 批量执行每行参数数量
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param values  values
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, int batch, int vol, ConfigStore configs, RunPrepare prepare, Collection<Object> values) {
		if(null == random) {
			random = random(runtime);
		}
		prepare.setBatch(batch);
		Run run = buildExecuteRun(runtime, prepare, configs);
		run.setVol(vol);
		run.setValues(null, values);
        return execute(runtime, random, configs, run);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, Procedure procedure)", 37));
		}
		return false;
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
		Run run = null;
		if(prepare instanceof XMLPrepare) {
			run = new XMLRun();
		}else if(prepare instanceof TextPrepare) {
			run = new TextRun();
		}
		if(null != run) {
			run.setBatch(prepare.getBatch());
			run.setRuntime(runtime);
			run.setPrepare(prepare);
			if(run instanceof TextRun) {
				parseText(runtime, (TextRun)run);
			}
			run.setConfigStore(configs);
			run.addCondition(conditions);
			run.init(); //
			//构造最终的执行SQL
			//fillQueryContent(runtime, run);
			fillExecuteContent(runtime, run);
		}
		return run;
	}
	/**
	 * execute [命令合成]<br/>
	 * 填充execute命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run XMLRun
	 */
	protected void fillExecuteContent(DataRuntime runtime, XMLRun run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, XMLRun run)", 37));
		}
	}
	/**
	 * execute [命令合成]<br/>
	 * 填充execute命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run XMLRun
	 */
	protected void fillExecuteContent(DataRuntime runtime, TextRun run) {
		replaceVariable(runtime, run);
		run.appendCondition(true);
		run.appendGroup();
		run.checkValid();
	}
	/**
	 * execute [命令合成]<br/>
	 * 填充execute命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run XMLRun
	 */
	protected void fillExecuteContent(DataRuntime runtime, TableRun run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, TextRun run)", 37));
		}
	}

	/**
	 * execute [命令合成-子流程]<br/>
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public void fillExecuteContent(DataRuntime runtime, Run run) {
		if(null != run) {
			if(run instanceof TableRun) {
				TableRun r = (TableRun) run;
				fillExecuteContent(runtime, r);
			}else if(run instanceof XMLRun) {
				XMLRun r = (XMLRun) run;
				fillExecuteContent(runtime, r);
			}else if(run instanceof TextRun) {
				TextRun r = (TextRun) run;
				fillExecuteContent(runtime, r);
			}
		}
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
		long result = -1;
		if(null == random) {
			random = random(runtime);
		}
		String sql = run.getFinalExecute();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		int batch = run.getBatch();
		String action = "execute";
		if(batch > 1) {
			action = "batch execute";
		}
		if(log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
			if(batch >1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
				log.info("{}[action:{}][cmd:\n{}\n]\n[param size:{}]", random, action, sql, values.size());
			}else {
				log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.EXECUTE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
		}
		if(null != configs) {
			configs.add(run);
		}
		boolean exe = true;
		if(null != configs) {
			exe = configs.execute();
		}
		if(!exe) {
			return -1;
		}
		long millis = -1;
		try{
			result = actuator.execute(this, runtime, random, configs, run);
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
				if(millis > SLOW_SQL_MILLIS) {
					slow = true;
					log.warn("{}[slow cmd][action:{}][执行耗时:{}][cmd:\n{}\n]\n[param:{}]", random, action, DateUtil.format(millis), sql, LogUtil.param(values));
					if(null != dmListener) {
						dmListener.slow(runtime, random, ACTION.DML.EXECUTE, run, sql, values, null, true, result, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
				String qty = ""+result;
				if(batch>1) {
					qty = "约"+result;
				}
				log.info("{}[action:{}][执行耗时:{}][影响行数:{}]", random, action, DateUtil.format(millis), LogUtil.format(qty, 34));
			}
		}catch(Exception e) {
			if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				log.error("execute exception:",e);
			}
			if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.error("{}[{}][action:{}]{}", random, LogUtil.format("命令执行异常:", 33)+e, action, run.log(ACTION.DML.EXECUTE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				throw new CommandUpdateException("命令执行异常", e);
			}

		}
		return result;
	}

	/**
	 * query [命令合成]<br/>
	 * 替换占位符
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return 影响行数
	 */
	protected void replaceVariable(DataRuntime runtime, TextRun run) {
		StringBuilder builder = run.getBuilder();
		List<Variable> variables = run.getVariables();
		String result = run.getText();
		if(supportPlaceholder() && null != variables) {
			for(Variable var:variables) {
				if(null == var) {
					continue;
				}
				if(var.getType() == Variable.VAR_TYPE_REPLACE) {
					// CD = ::CD
					List<Object> values = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true, values)) {
						if(var.getCompare() == Compare.IN) {
							value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
						}else {
							value = values.get(0).toString();
						}
					}
					if(null != value) {
						result = result.replace(var.getFullKey(), value);
					}else{
						result = result.replace(var.getFullKey(), "NULL");
					}
				}
			}
			for(Variable var:variables) {
				if(null == var) {
					continue;
				}
				if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE) {
					// CD = ':CD'
					List<Object> values = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true, values)) {
						if(var.getCompare() == Compare.IN) {
							value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
						}else {
							value = values.get(0).toString();
						}
					}
					if(null != value) {
						result = result.replace(var.getFullKey(), value);
					}else{
						result = result.replace(var.getFullKey(), "");
					}
				}
			}
			for(Variable var:variables) {
				if(null == var) {
					continue;
				}
				if(var.getType() == Variable.VAR_TYPE_KEY) {
					// CD = :CD
					List<Object> varValues = var.getValues();
					if(run.getBatch() >1) {//批量执行时在下一步提供值
						result = result.replace(var.getFullKey(), "?");
					}else if(BasicUtil.isNotEmpty(true, varValues)) {
						if(var.getCompare() == Compare.IN) {
							// 多个值IN
							String replaceDst = "";
							for(Object tmp:varValues) {
								replaceDst += " ?";
							}
							addRunValue(runtime, run, Compare.IN, new Column(var.getKey()), varValues);
							replaceDst = replaceDst.trim().replace(" ",",");
							result = result.replace(var.getFullKey(), replaceDst);
						}else{
							// 单个值
							result = result.replace(var.getFullKey(), "?");
							addRunValue(runtime, run, Compare.EQUAL, new Column(var.getKey()), varValues.get(0));
						}
					}else{
						//没有提供参数值
						result = result.replace(var.getFullKey(), "NULL");
					}
				}
			}
			// 添加其他变量值
			for(Variable var:variables) {
				if(null == var) {
					continue;
				}
				// CD = ?
				if(var.getType() == Variable.VAR_TYPE_INDEX) {
					List<Object> varValues = var.getValues();
					Object value = null;
					if(BasicUtil.isNotEmpty(true, varValues)) {
						value = varValues.get(0);
					}
					addRunValue(runtime, run, Compare.EQUAL, new Column(var.getKey()), value);
				}
			}
		}

		builder.append(result);
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
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String key, Collection<T> values) {
		long result = -1;
		if(null == random) {
			random = random(runtime);
		}
		ACTION.SWITCH swt = InterceptorProxy.prepareDelete(runtime, random, batch, table, key, values);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.prepareDelete(runtime, random, batch, table, key, values);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		List<Run> runs = buildDeleteRun(runtime, batch, table, configs, key, values);
		for(Run run:runs){

			if(!run.isValid()) {
				if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
					log.warn("[valid:false][不具备执行条件][table:" +table+ "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
				}
				continue;
			}
			if(result == -1){
				result = 1;
			}
			result += delete(runtime, random, configs, run);
		}
		return result;
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
	public long delete(DataRuntime runtime, String random, Table dest, ConfigStore configs, Object obj, String... columns) {
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		long size = -1;
		if(null != obj) {
			swt = InterceptorProxy.prepareDelete(runtime, random, 0, dest, obj, columns);
			if(swt == ACTION.SWITCH.BREAK) {
				return -1;
			}
			if(null != dmListener) {
				swt = dmListener.prepareDelete(runtime, random, 0, dest, obj, columns);
			}
			if(swt == ACTION.SWITCH.BREAK) {
				return -1;
			}
			List<Run> runs = buildDeleteRun(runtime, dest, configs, obj, columns);
			for(Run run:runs){
				if(!run.isValid()) {
					if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
						log.warn("[valid:false][不具备执行条件][dest:" + dest + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
					}
					continue;
				}
				if(size == -1){
					size = 0;
				}
				size += delete(runtime, random, configs, run);
			}
		}
		return size;
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
	public long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions) {
		long result = -1;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		swt = InterceptorProxy.prepareDelete(runtime, random, 0, table, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.prepareDelete(runtime, random, 0, table, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		List<Run> runs = buildDeleteRun(runtime, table, configs, conditions);
		for(Run run:runs){
			if(!run.isValid()) {
				if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
					log.warn("[valid:false][不具备执行条件][table:" + table + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
				}
				continue;
			}
			if(result == -1){
				result = 0;
			}
			result += delete(  runtime, random, configs, run);
		}
		return result;
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
		List<Run> runs = buildTruncateRun(runtime, table);
		if(null != runs && runs.size()>0) {
			RunPrepare prepare = new DefaultTextPrepare(runs.get(0).getFinalUpdate());
			return (int)execute(runtime, random, prepare, null);
		}
		return -1;
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, String ... columns) {
		List<Run> runs = new ArrayList<>();
		if(null == obj && (null == configs || configs.isEmptyCondition())) {
			return null;
		}
		if(obj instanceof Collection){
			Collection list = (Collection) obj;
			for(Object item:list){
				runs.addAll(buildDeleteRun(runtime, dest, configs, item, columns));
			}
			return runs;
		}
		if(null == dest) {
			dest = DataSourceUtil.parseDest(null, obj, configs);
		}
		if(null == dest) {
			Object entity = obj;
			if(obj instanceof Collection) {
				entity = ((Collection)obj).iterator().next();
			}
			Table table = EntityAdapterProxy.table(entity.getClass());
			if(null != table) {
				dest = table;
			}
		}
		if(null == dest || BasicUtil.isEmpty(dest.getName())){
			runs = buildDeleteRunFromConfig(runtime, configs);
		}else if(obj instanceof ConfigStore) {
			Run run = new TableRun(runtime, dest);
			RunPrepare prepare = new DefaultTablePrepare();
			prepare.setDest(dest);
			run.setPrepare(prepare);
			run.setConfigStore((ConfigStore)obj);
			run.addCondition(columns);
			run.init();
			fillDeleteRunContent(runtime, run);
			runs.add(run);
		}else{
			runs = buildDeleteRunFromEntity(runtime, dest, configs, obj, columns);
		}
		convert(runtime, new DefaultConfigStore(), runs);

		return runs;
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values values
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String key, Object values) {
		List<Run> runs = buildDeleteRunFromTable(runtime, batch, table, configs, key, values);
		convert(runtime, new DefaultConfigStore(), runs);
		return runs;
	}


	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildTruncateRun(DataRuntime runtime, Table table) {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("TRUNCATE TABLE ");
		name(runtime, builder, table);
		return runs;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,String column, Object values)", 37));
		}
		return null;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String... columns) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String... columns)", 37));
		}
		return null;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public void fillDeleteRunContent(DataRuntime runtime, Run run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillDeleteRunContent(DataRuntime runtime, Run run)", 37));
		}
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
		long result = -1;
		boolean cmd_success = false;
		if(null == random) {
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		long fr = System.currentTimeMillis();
		swt = InterceptorProxy.beforeDelete(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if(null != dmListener) {
			swt = dmListener.beforeDelete(runtime, random, run);
		}
		if(swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		long millis = -1;

		result = execute(runtime, random, configs, run);
		cmd_success = true;
		millis = System.currentTimeMillis() - fr;

		if(null != dmListener) {
			dmListener.afterDelete(runtime, random, run, cmd_success, result, millis);
		}
		InterceptorProxy.afterDelete(runtime, random, run, cmd_success, result, millis);
		return result;
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
	 * String version(DataRuntime runtime, String random)
	 * String product(DataRuntime runtime, String random)
	 * Database database(DataRuntime runtime, String random)
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
	 * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
	 * Database database(DataRuntime runtime, String random, String name)
	 * Database database(DataRuntime runtime, String random)
	 * String String product(DataRuntime runtime, String random);
	 * String String version(DataRuntime runtime, String random);
	 * [命令合成]
	 * List<Run> buildQueryProductRun(DataRuntime runtime)
	 * List<Run> buildQueryVersionRun(DataRuntime runtime)
	 * List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryProductRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryVersionRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime)
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
	 * String product(DataRuntime runtime, int index, boolean create, String product, DataSet set)
	 * String product(DataRuntime runtime, boolean create, String product)
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, String random)", 37));
		}
		return null;
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	public String product(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		String product = null;
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryProductRun(runtime);
				if (null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						product = product(runtime, idx++, true, product, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[product][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if(null == product) {
				product = product(runtime, false, product);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[product][result:{}][执行耗时:{}]", random, product, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[product][result:fail][msg:{}]", e.toString());
			}
		}
		return product;
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	public String version(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		String version = null;
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryProductRun(runtime);
				if (null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						version = version(runtime, idx++, true, version, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[version][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if(null == version) {
				version = version(runtime, false, version);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[version][result:{}][执行耗时:{}]", random, version, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[version][result:fail][msg:{}]", e.toString());
			}
		}
		return version;
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
	public List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name) {
		if(null == random) {
			random = random(runtime);
		}
		List<Database> databases = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabasesRun(runtime, greedy, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						databases = databases(runtime, idx++, true, databases, null, null, set);
					}
				}
				if(databases.isEmpty()){
					databases = getActuator().databases(this, runtime);
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}]", random, databases.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name) {
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, Database> databases = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabasesRun(runtime, false, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						databases = databases(runtime, idx++, true, databases, null, null, set);
					}
				}
				if(databases.isEmpty()){
					List<Database> list = getActuator().databases(this, runtime);
					for(Database item:list){
						databases.put(item.getName().toUpperCase(), item);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}]", random, databases.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryDatabaseRun(DataRuntime runtime) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDatabaseRun(DataRuntime runtime)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryProductRun(DataRuntime runtime)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryVersionRun(DataRuntime runtime)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * database[结果集封装]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * database[结果集封装]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception
	 */
	@Override
	public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public Database database(DataRuntime runtime, int index, boolean create, Database database, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, int index, boolean create, Database database,DataSet set)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, boolean create, Database database) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, boolean create, Database database)", 37));
		}
		return null;
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
	@Override
	public String product(DataRuntime runtime, int index, boolean create, String product, DataSet set) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String product(DataRuntime runtime, int index, boolean create, String product, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, boolean create, String product) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String product(DataRuntime runtime, boolean create, String product)", 37));
		}
		return null;
	}

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
	@Override
	public String version(DataRuntime runtime, int index, boolean create, String version, DataSet set) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String version(DataRuntime runtime, int index, boolean create, String version, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, boolean create, String version) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String version(DataRuntime runtime, boolean create, String version)", 37));
		}
		return null;
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
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs)
	 * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs)
	 *
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog)
	 ******************************************************************************************************************/

	/**
	 * catalog[调用入口]<br/>
	 * 当前Catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Catalog
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		Catalog catalog = null;
		try{
			long fr = System.currentTimeMillis();
			//根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogRun(runtime, random);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						catalog = catalog(runtime, idx++, true, catalog, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalog][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			//根据JDBC接口补充
			try{
				catalog = catalog(runtime, true, catalog);
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalog][{}][msg:{}]", random, LogUtil.format("根据JDBC接口补充失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalog][result:{}][执行耗时:{}]", random, catalog, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalog][result:fail][msg:{}]", e.toString());
			}
		}
		return catalog;
	}

	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name) {
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, Catalog> catalogs = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogsRun(runtime, false, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						catalogs = catalogs(runtime, idx++, true, catalogs, null, null, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalogs][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalogs][result:{}][执行耗时:{}]", random, catalogs.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalogs][result:fail][msg:{}]", e.toString());
			}
		}
		return catalogs;
	}

	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name) {
		if(null == random) {
			random = random(runtime);
		}
		List<Catalog> catalogs = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogsRun(runtime, greedy, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						catalogs = catalogs(runtime, idx++, true, catalogs, null, null, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalogs][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalogs][result:{}][执行耗时:{}]", random, catalogs.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalogs][result:fail][msg:{}]", e.toString());
			}
		}
		return catalogs;
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询当前catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogRun(DataRuntime runtime, String random) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryCatalogRun(DataRuntime runtime, String random)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 上一步查询结果
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog)", 37));
		}
		return null;
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
	 * 当前Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Schema
	 */
	@Override
	public Schema schema(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		Schema schema = null;
		try{
			long fr = System.currentTimeMillis();
			//根据系统表查询
			try{
				List<Run> runs = buildQuerySchemaRun(runtime, random);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						schema = schema(runtime, idx++, true, schema, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schema][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			//根据JDBC接口补充
			try{
				schema = schema(runtime, true, schema);
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schema][{}][msg:{}]", random, LogUtil.format("根据JDBC接口补充失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schema][result:{}][执行耗时:{}]", random, schema, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schema][result:fail][msg:{}]", e.toString());
			}
		}
		return schema;
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
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name) {
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, Schema> schemas = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySchemasRun(runtime, false, catalog, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						schemas = schemas(runtime, idx++, true, schemas, null, null, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schemas][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schemas][result:{}][执行耗时:{}]", random, schemas.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schemas][result:fail][msg:{}]", e.toString());
			}
		}
		return schemas;
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
	public List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name) {
		if(null == random) {
			random = random(runtime);
		}
		List<Schema> schemas = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySchemasRun(runtime, greedy, catalog, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						schemas = schemas(runtime, idx++, true, schemas, null, null, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schemas][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schemas][result:{}][执行耗时:{}]", random, schemas.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schemas][result:fail][msg:{}]", e.toString());
			}
		}
		return schemas;
	}

	/**
	 * schema[命令合成]<br/>
	 * 查询当前schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQuerySchemaRun(DataRuntime runtime, String random) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySchemaRun(DataRuntime runtime, String random)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, Schema> schemas) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Schema> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, Schema> schemas)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	public List<Schema> schemas(DataRuntime runtime, boolean create, List<Schema> schemas) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Schema> schemas(DataRuntime runtime, boolean create, List<Schema> schemas)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schema 上一步查询结果
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, boolean create, Schema schema) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Schema schema(DataRuntime runtime, boolean create, Schema schema)", 37));
		}
		return null;
	}

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
	public <T extends Metadata> T checkName(DataRuntime runtime, String random, T meta) throws RuntimeException{
		if(null == meta) {
			return null;
		}
		String name = meta.getName();
		if(null != name && name.contains(".")) {
			String[] ks = name.split("\\.");
			if(ks.length == 3) {
				meta.setCatalog(ks[0]);
				meta.setSchema(ks[1]);
				meta.setName(ks[2]);
			}else{
				throw new RuntimeException("无法实别schema或catalog(子类未" + this.getClass().getSimpleName() + "实现)");
			}
		}
		return meta;
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
	 * @param types  Metadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}

		try{
			long fr = System.currentTimeMillis();
			Table search = new Table();
			if(
				(supportCatalog() && empty(catalog))    //支持catalog 但catalog为空
				|| (supportSchema() && empty(schema))	//支持schema 但schema为空
			) {
				Table tmp = new Table();
				if(!greedy) { //非贪婪模式下 检测当前catalog schema
					checkSchema(runtime, tmp);
				}
				if(supportCatalog() && empty(catalog)) {
					catalog = tmp.getCatalog();
				}
				if(supportSchema() && empty(schema)) {
					schema = tmp.getSchema();
				}
			}
			String caches_key = CacheProxy.key(runtime, "tables", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.tables(caches_key);
			if(null != list && !list.isEmpty()){
				return list;
			}
			String cache_key = CacheProxy.key(runtime, "table", greedy, catalog, schema, pattern);
			String origin = CacheProxy.name(cache_key);
			if(null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
				//先查出所有key并以大写缓存 用来实现忽略大小写
				tableMap(runtime, random, greedy, catalog, schema, null);
				origin = CacheProxy.name(cache_key);
			}
			if(null == origin) {
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);
			PageNavi navi = null;
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			navi = configs.getPageNavi();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryTablesRun(runtime, greedy, catalog, schema, origin, types, configs);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						if(null != navi){
							run.setPageNavi(navi);
							mergeFinalQuery(runtime, run);
						}
						DataSet set = select(runtime, random, true, (String)null, configs.keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						list = tables(runtime, idx++, true, list, catalog, schema, set);
						if(null != navi){
							//分页只查一次
							break;
						}
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统表查询失败后根据驱动内置接口补充
			if(list.isEmpty()) {
				try {
					list = tables(runtime, true, list, catalog, schema, origin, types);
					//删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
					if(!greedy) {
						int size = list.size();
						for(int i=size-1;i>=0; i--) {
							Table item = list.get(i);
							if(!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
								list.remove(i);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[tables][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(Table table:list) {
				if(BasicUtil.isNotEmpty(table.getComment())) {
					comment = true;
					break;
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryTablesCommentRun(runtime, catalog, schema, origin, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							if(null != navi){
								run.setPageNavi(navi);
								//mergeFinalQuery(runtime, run);
							}
							DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
							list = comments(runtime, idx++, true, list, catalog, schema, set);
							if(null != navi){
								break;
							}
							//merge(list, maps);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.info("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
			if(BasicUtil.isNotEmpty(origin)) {
				origin = origin.replace("%",".*");
				//有表名的，根据表名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
				for(T item:list) {
					String name = item.getName(greedy)+"";
					if(RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
						if(equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
							tmp.add(item);
						}
					}
				}
				list = tmp;
			}
			if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
				//查询全部表结构 columns()内部已经给table.columns赋值
				List<Column> columns = columns(runtime, random, greedy, catalog, schema, (List<Table>)list);
			}
			if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
				//查询全部表结构
				indexes(runtime, random, greedy, (List<Table>)list);
			}
			CacheProxy.tables(caches_key, list);
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[tables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	protected void tableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
			if(null == random) {
				random = random(runtime);
			}
			DriverAdapter adapter = runtime.getAdapter();
			List<Table> tables = null;
			boolean sys = false; //根据系统表查询
			if(greedy) {
				catalog = null;
				schema = null;
			}
			try {
				//缓存 不需要configs条件及分页
				List<Run> runs =buildQueryTablesRun(runtime, greedy, catalog, schema, null, Table.TYPE.NORMAL.value, null);
				if (null != runs && !runs.isEmpty()) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						tables = tables(runtime, idx++, true, tables, catalog, schema, set);
						for(Table table:tables){
							String cache_key = CacheProxy.key(runtime, "table", greedy, catalog, schema, table.getName());
							CacheProxy.name(cache_key, table.getName());
						}
						sys = true;
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			if(!sys) {
				try {
					tables = tables(runtime, true, tables, catalog, schema, null, Table.TYPE.NORMAL.value);
					for(Table table:tables){
						String cache_key = CacheProxy.key(runtime, "table", greedy, catalog, schema, table.getName());
						CacheProxy.name(cache_key, table.getName());
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		//}

	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		List<T> list = tables(runtime, random, false, catalog, schema, pattern, types, struct, configs);
		for(T table:list) {
			tables.put(table.getName().toUpperCase(), table);
		}
		return tables;
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
	public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

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
	public List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == tables) {
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

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
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(null == tables) {
			tables = new ArrayList<>();
		}
		for(DataRow row:set) {
			T table = null;
			table = init(runtime, index, table, catalog, schema, row);
			if(null == search(tables, table.getCatalog(), table.getSchema(), table.getName())) {
				tables.add(table);
			}
			detail(runtime, index, table, catalog, schema, row);
		}
		return tables;
	}

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
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == tables) {
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

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
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == tables) {
			tables = new ArrayList<>();
		}
		return tables;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Table
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Table
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * table[结构集封装-依据]<br/>
	 * 读取table元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return TableMetadataAdapter
	 */
	@Override
	public TableMetadataAdapter tableMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 TableMetadataAdapter tableMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new TableMetadataAdapter();
	}
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
	public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(null == tables) {
			tables = new LinkedHashMap<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return tables;
	}

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
	@Override
	public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(null == tables) {
			tables = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现  <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return tables;
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
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, table);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, table, list, set);
				}
				table.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = table.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, table, true);
					table.setColumns(columns);
					table.setTags(tags(runtime, random, false, table));
				}
				PrimaryKey pk = table.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, table);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				table.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = table.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					table.setIndexes(indexes(runtime, random, table, null));
				}
				runs = buildCreateRun(runtime, table);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					table.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[table ddl][table:{}][result:{}][执行耗时:{}]", random, table.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
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
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}


	/* *****************************************************************************************************************
	 * 													vertexTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends VertexTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, VertexTable vertexTable, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
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
	@Override
	public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}

		try{
			long fr = System.currentTimeMillis();
			VertexTable search = new VertexTable();
			if(
					(supportCatalog() && empty(catalog))    //支持catalog 但catalog为空
							|| (supportSchema() && empty(schema))	//支持schema 但schema为空
			) {
				VertexTable tmp = new VertexTable();
				if(!greedy) { //非贪婪模式下 检测当前catalog schema
					checkSchema(runtime, tmp);
				}
				if(supportCatalog() && empty(catalog)) {
					catalog = tmp.getCatalog();
				}
				if(supportSchema() && empty(schema)) {
					schema = tmp.getSchema();
				}
			}
			String caches_key = CacheProxy.key(runtime, "vertexTables", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.vertexTables(caches_key);
			if(null != list && !list.isEmpty()){
				return list;
			}
			String cache_key = CacheProxy.key(runtime, "vertexTable", greedy, catalog, schema, pattern);
			String origin = CacheProxy.name(cache_key);
			if(null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
				//先查出所有key并以大写缓存 用来实现忽略大小写
				vertexTableMap(runtime, random, greedy, catalog, schema, null);
				origin = CacheProxy.name(cache_key);
			}
			if(null == origin) {
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);
			PageNavi navi = null;
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			navi = configs.getPageNavi();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryVertexTablesRun(runtime, greedy, catalog, schema, origin, types, configs);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						if(null != navi){
							run.setPageNavi(navi);
							mergeFinalQuery(runtime, run);
						}
						DataSet set = select(runtime, random, true, (String)null, configs.keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						list = vertexTables(runtime, idx++, true, list, catalog, schema, set);
						if(null != navi){
							//分页只查一次
							break;
						}
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[vertexTables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统表查询失败后根据驱动内置接口补充
			if(list.isEmpty()) {
				try {
					list = vertexTables(runtime, true, list, catalog, schema, origin, types);
					//删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
					if(!greedy) {
						int size = list.size();
						for(int i=size-1;i>=0; i--) {
							VertexTable item = list.get(i);
							if(!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
								list.remove(i);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[vertexTables][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(VertexTable vertexTable:list) {
				if(BasicUtil.isNotEmpty(vertexTable.getComment())) {
					comment = true;
					break;
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryVertexTablesCommentRun(runtime, catalog, schema, origin, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							if(null != navi){
								run.setPageNavi(navi);
								//mergeFinalQuery(runtime, run);
							}
							DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
							list = comments(runtime, idx++, true, list, catalog, schema, set);
							if(null != navi){
								break;
							}
							//merge(list, maps);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.info("{}[vertexTables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[vertexTables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
			if(BasicUtil.isNotEmpty(origin)) {
				origin = origin.replace("%",".*");
				//有表名的，根据表名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
				for(T item:list) {
					String name = item.getName(greedy)+"";
					if(RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
						if(equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
							tmp.add(item);
						}
					}
				}
				list = tmp;
			}
			if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
				//查询全部表结构 columns()内部已经给vertexTable.columns赋值
				List<Column> columns = columns(runtime, random, greedy, catalog, schema, list);
			}
			if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
				//查询全部表结构
				//indexes(runtime, random, greedy, (List<VertexTable>)list);
			}
			CacheProxy.tables(caches_key, list);
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[vertexTables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * vertexTable[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	protected void vertexTableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<VertexTable> vertexTables = null;
		boolean sys = false; //根据系统表查询
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryVertexTablesRun(runtime, greedy, catalog, schema, null, VertexTable.TYPE.NORMAL.value, null);
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					vertexTables = vertexTables(runtime, idx++, true, vertexTables, catalog, schema, set);
					for(VertexTable vertexTable:vertexTables){
						String cache_key = CacheProxy.key(runtime, "vertexTable", greedy, catalog, schema, vertexTable.getName());
						CacheProxy.name(cache_key, vertexTable.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				vertexTables = vertexTables(runtime, true, vertexTables, catalog, schema, null, VertexTable.TYPE.NORMAL.value);
				for(VertexTable vertexTable:vertexTables){
					String cache_key = CacheProxy.key(runtime, "vertexTable", greedy, catalog, schema, vertexTable.getName());
					CacheProxy.name(cache_key, vertexTable.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}


	/**
	 * vertexTable[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> vertexTables = new LinkedHashMap<>();
		List<T> list = vertexTables(runtime, random, false, catalog, schema, pattern, types, struct, configs);
		for(T vertexTable:list) {
			vertexTables.put(vertexTable.getName().toUpperCase(), vertexTable);
		}
		return vertexTables;
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
	@Override
	public List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryVertexTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

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
	public List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == vertexTables) {
			vertexTables = new LinkedHashMap<>();
		}
		return vertexTables;
	}

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
	@Override
	public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == vertexTables) {
			vertexTables = new ArrayList<>();
		}
		return vertexTables;
	}

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
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == vertexTables) {
			vertexTables = new LinkedHashMap<>();
		}
		return vertexTables;
	}

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
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == vertexTables) {
			vertexTables = new ArrayList<>();
		}
		return vertexTables;
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return VertexTable
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据查询结果封装VertexTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return VertexTable
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * vertexTable[结构集封装-依据]<br/>
	 * 读取vertexTable元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return VertexTableMetadataAdapter
	 */
	@Override
	public VertexTableMetadataAdapter vertexTableMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 VertexTableMetadataAdapter vertexTableMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new VertexTableMetadataAdapter();
	}

	/**
	 *
	 * vertexTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param vertexTable 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, VertexTable vertexTable, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, vertexTable);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传vertexTable,这里的vertexTable用来查询表结构
					DataSet set = select(runtime, random, true, (VertexTable)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, vertexTable, list, set);
				}
				vertexTable.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = vertexTable.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, vertexTable, true);
					vertexTable.setColumns(columns);
					vertexTable.setTags(tags(runtime, random, false, vertexTable));
				}
				PrimaryKey pk = vertexTable.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, vertexTable);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				vertexTable.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = vertexTable.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					vertexTable.setIndexes(indexes(runtime, random, vertexTable, null));
				}
				runs = buildCreateRun(runtime, vertexTable);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					vertexTable.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[vertexTable ddl][vertexTable:{}][result:{}][执行耗时:{}]", random, vertexTable.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[vertexTable ddl][{}][vertexTable:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), vertexTable.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * vertexTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param vertexTable 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}


	/* *****************************************************************************************************************
	 * 													EdgeTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends EdgeTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable meta)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
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
	@Override
	public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}

		try{
			long fr = System.currentTimeMillis();
			EdgeTable search = new EdgeTable();
			if(
					(supportCatalog() && empty(catalog))    //支持catalog 但catalog为空
							|| (supportSchema() && empty(schema))	//支持schema 但schema为空
			) {
				EdgeTable tmp = new EdgeTable();
				if(!greedy) { //非贪婪模式下 检测当前catalog schema
					checkSchema(runtime, tmp);
				}
				if(supportCatalog() && empty(catalog)) {
					catalog = tmp.getCatalog();
				}
				if(supportSchema() && empty(schema)) {
					schema = tmp.getSchema();
				}
			}
			String caches_key = CacheProxy.key(runtime, "edgeTables", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.edgeTables(caches_key);
			if(null != list && !list.isEmpty()){
				return list;
			}
			String cache_key = CacheProxy.key(runtime, "edgeTable", greedy, catalog, schema, pattern);
			String origin = CacheProxy.name(cache_key);
			if(null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
				//先查出所有key并以大写缓存 用来实现忽略大小写
				edgeTableMap(runtime, random, greedy, catalog, schema, null);
				origin = CacheProxy.name(cache_key);
			}
			if(null == origin) {
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);
			PageNavi navi = null;
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			navi = configs.getPageNavi();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryEdgeTablesRun(runtime, greedy, catalog, schema, origin, types, configs);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						if(null != navi){
							run.setPageNavi(navi);
							mergeFinalQuery(runtime, run);
						}
						DataSet set = select(runtime, random, true, (String)null, configs.keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						list = edgeTables(runtime, idx++, true, list, catalog, schema, set);
						if(null != navi){
							//分页只查一次
							break;
						}
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[edgeTables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统表查询失败后根据驱动内置接口补充
			if(list.isEmpty()) {
				try {
					list = edgeTables(runtime, true, list, catalog, schema, origin, types);
					//删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
					if(!greedy) {
						int size = list.size();
						for(int i=size-1;i>=0; i--) {
							EdgeTable item = list.get(i);
							if(!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
								list.remove(i);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[edgeTables][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(EdgeTable item:list) {
				if(BasicUtil.isNotEmpty(item.getComment())) {
					comment = true;
					break;
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryEdgeTablesCommentRun(runtime, catalog, schema, origin, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							if(null != navi){
								run.setPageNavi(navi);
								//mergeFinalQuery(runtime, run);
							}
							DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
							list = comments(runtime, idx++, true, list, catalog, schema, set);
							if(null != navi){
								break;
							}
							//merge(list, maps);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.info("{}[edgeTables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[edgeTables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
			if(BasicUtil.isNotEmpty(origin)) {
				origin = origin.replace("%",".*");
				//有表名的，根据表名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
				for(T item:list) {
					String name = item.getName(greedy)+"";
					if(RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
						if(equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
							tmp.add(item);
						}
					}
				}
				list = tmp;
			}
			if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
				//查询全部表结构 columns()内部已经给edgeTable.columns赋值
				List<Column> columns = columns(runtime, random, greedy, catalog, schema, (List<EdgeTable>)list);
			}
			if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
				//查询全部表结构
				indexes(runtime, random, greedy, (List<EdgeTable>)list);
			}
			CacheProxy.tables(caches_key, list);
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[edgeTables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * edgeTable[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	protected void edgeTableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<EdgeTable> edgeTables = null;
		boolean sys = false; //根据系统表查询
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryEdgeTablesRun(runtime, greedy, catalog, schema, null, EdgeTable.TYPE.NORMAL.value, null);
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					edgeTables = edgeTables(runtime, idx++, true, edgeTables, catalog, schema, set);
					for(EdgeTable item:edgeTables){
						String cache_key = CacheProxy.key(runtime, "edgeTable", greedy, catalog, schema, item.getName());
						CacheProxy.name(cache_key, item.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				edgeTables = edgeTables(runtime, true, edgeTables, catalog, schema, null, EdgeTable.TYPE.NORMAL.value);
				for(EdgeTable item:edgeTables){
					String cache_key = CacheProxy.key(runtime, "edgeTable", greedy, catalog, schema, item.getName());
					CacheProxy.name(cache_key, item.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * edgeTable[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> edgeTables = new LinkedHashMap<>();
		List<T> list = edgeTables(runtime, random, false, catalog, schema, pattern, types, struct, configs);
		for(T edgeTable:list) {
			edgeTables.put(edgeTable.getName().toUpperCase(), edgeTable);
		}
		return edgeTables;
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
	@Override
	public List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

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
	public List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == edgeTables) {
			edgeTables = new LinkedHashMap<>();
		}
		return edgeTables;
	}

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
	@Override
	public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == edgeTables) {
			edgeTables = new ArrayList<>();
		}
		return edgeTables;
	}

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
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == edgeTables) {
			edgeTables = new LinkedHashMap<>();
		}
		return edgeTables;
	}

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
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == edgeTables) {
			edgeTables = new ArrayList<>();
		}
		return edgeTables;
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return EdgeTable
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return EdgeTable
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * edgeTable[结构集封装-依据]<br/>
	 * 读取edgeTable元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return EdgeTableMetadataAdapter
	 */
	@Override
	public EdgeTableMetadataAdapter edgeTableMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 EdgeTableMetadataAdapter edgeTableMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new EdgeTableMetadataAdapter();
	}


	/**
	 *
	 * edgeTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, meta);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传edgeTable,这里的edgeTable用来查询表结构
					DataSet set = select(runtime, random, true, (EdgeTable)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, meta, list, set);
				}
				meta.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = meta.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, meta, true);
					meta.setColumns(columns);
					meta.setTags(tags(runtime, random, false, meta));
				}
				PrimaryKey pk = meta.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, meta);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				meta.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = meta.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					meta.setIndexes(indexes(runtime, random, meta, null));
				}
				runs = buildCreateRun(runtime, meta);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					meta.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[edgeTable ddl][edgeTable:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[edgeTable ddl][{}][edgeTable:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * edgeTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable meta) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param meta 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}


	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryViewsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
	 * <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends View> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, View view, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, View view)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @param struct 是否查询视图结构
	 * @return List
	 * @param <T> View
	 */
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}

		try{
			long fr = System.currentTimeMillis();
			View search = new View();
			if(
					(supportCatalog() && empty(catalog))    //支持catalog 但catalog为空
							|| (supportSchema() && empty(schema))	//支持schema 但schema为空
			) {
				View tmp = new View();
				if(!greedy) { //非贪婪模式下 检测当前catalog schema
					checkSchema(runtime, tmp);
				}
				if(supportCatalog() && empty(catalog)) {
					catalog = tmp.getCatalog();
				}
				if(supportSchema() && empty(schema)) {
					schema = tmp.getSchema();
				}
			}
			String caches_key = CacheProxy.key(runtime, "views", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.views(caches_key);
			if(null != list && !list.isEmpty()){
				return list;
			}
			String cache_key = CacheProxy.key(runtime, "view", greedy, catalog, schema, pattern);
			String origin = CacheProxy.name(cache_key);
			if(null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
				//先查出所有key并以大写缓存 用来实现忽略大小写
				viewMap(runtime, random, greedy, catalog, schema, null);
				origin = CacheProxy.name(cache_key);
			}
			if(null == origin) {
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);
			PageNavi navi = null;
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			navi = configs.getPageNavi();
			// 根据系统视图查询
			try{
				List<Run> runs = buildQueryViewsRun(runtime, greedy, catalog, schema, origin, types, configs);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						if(null != navi){
							run.setPageNavi(navi);
							mergeFinalQuery(runtime, run);
						}
						DataSet set = select(runtime, random, true, (String)null, configs.keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						list = views(runtime, idx++, true, list, catalog, schema, set);
						if(null != navi){
							//分页只查一次
							break;
						}
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统视图查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统视图查询失败后根据驱动内置接口补充
			if(list.isEmpty()) {
				try {
					list = views(runtime, true, list, catalog, schema, origin, types);
					//删除跨库视图，JDBC驱动内置接口补充可能会返回跨库视图
					if(!greedy) {
						int size = list.size();
						for(int i=size-1;i>=0; i--) {
							View item = list.get(i);
							if(!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
								list.remove(i);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[views][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(View view:list) {
				if(BasicUtil.isNotEmpty(view.getComment())) {
					comment = true;
					break;
				}
			}
			//视图备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryViewsCommentRun(runtime, catalog, schema, origin, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							if(null != navi){
								run.setPageNavi(navi);
								//mergeFinalQuery(runtime, run);
							}
							DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
							list = comments(runtime, idx++, true, list, catalog, schema, set);
							if(null != navi){
								break;
							}
							//merge(list, maps);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.info("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统视图查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[views][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
			if(BasicUtil.isNotEmpty(origin)) {
				origin = origin.replace("%",".*");
				//有视图名的，根据视图名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
				for(T item:list) {
					String name = item.getName(greedy)+"";
					if(RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
						if(equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
							tmp.add(item);
						}
					}
				}
				list = tmp;
			}
			if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
				//查询全部视图结构 columns()内部已经给view.columns赋值
				List<Column> columns = columns(runtime, random, greedy, catalog, schema, (List<View>)list);
			}
			if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
				//查询全部视图结构
				indexes(runtime, random, greedy, (List<View>)list);
			}
			CacheProxy.views(caches_key, list);
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[views][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * view[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	protected void viewMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<View> views = null;
		boolean sys = false; //根据系统视图查询
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryViewsRun(runtime, greedy, catalog, schema, null, View.TYPE.NORMAL.value, null);
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					views = views(runtime, idx++, true, views, catalog, schema, set);
					for(View view:views){
						String cache_key = CacheProxy.key(runtime, "view", greedy, catalog, schema, view.getName());
						CacheProxy.name(cache_key, view.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				views = views(runtime, true, views, catalog, schema, null, View.TYPE.NORMAL.value);
				for(View view:views){
					String cache_key = CacheProxy.key(runtime, "view", greedy, catalog, schema, view.getName());
					CacheProxy.name(cache_key, view.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * view[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> views = new LinkedHashMap<>();
		List<T> list = views(runtime, random, false, catalog, schema, pattern, types, struct, configs);
		for(T view:list) {
			views.put(view.getName().toUpperCase(), view);
		}
		return views;
	}

	/**
	 * view[命令合成]<br/>
	 * 查询视图,不是查视图中的数据
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
	public List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryViewsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成]<br/>
	 * 查询视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	public List<Run> buildQueryViewsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryViewsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
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
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == views) {
			views = new LinkedHashMap<>();
		}
		return views;
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
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == views) {
			views = new ArrayList<>();
		}
		return views;
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == views) {
			views = new LinkedHashMap<>();
		}
		return views;
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return views
	 * @throws Exception 异常
	 * @param <T> View
	 */
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == views) {
			views = new ArrayList<>();
		}
		return views;
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据查询结果封装View对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return View
	 * @param <T> View
	 */
	@Override
	public <T extends View> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * view[结果集封装]<br/>
	 * 根据查询结果封装View对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return View
	 * @param <T> View
	 */
	@Override
	public <T extends View> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * view[结构集封装-依据]<br/>
	 * 读取view元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ViewMetadataAdapter
	 */
	@Override
	public ViewMetadataAdapter viewMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 ViewMetadataAdapter viewMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new ViewMetadataAdapter();
	}

	/**
	 *
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param view 视图
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, View view, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, view);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传view,这里的view用来查询视图结构
					DataSet set = select(runtime, random, true, (View)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, view, list, set);
				}
				view.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = view.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, view, true);
					view.setColumns(columns);
					view.setTags(tags(runtime, random, false, view));
				}
				PrimaryKey pk = view.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, view);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				view.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = view.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					view.setIndexes(indexes(runtime, random, view, null));
				}
				runs = buildCreateRun(runtime, view);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					view.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[view ddl][view:{}][result:{}][执行耗时:{}]", random, view.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[view ddl][{}][view:{}][msg:{}]", random, LogUtil.format("查询视图的创建DDL失败", 33), view.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * view[命令合成]<br/>
	 * 查询视图DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, View view) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, View view)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[结果集封装]<br/>
	 * 查询视图DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param view 视图
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}


	/* *****************************************************************************************************************
	 * 													masterTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends MasterTable> List<T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends MasterTable> List<T> masterTables(DataRuntime runtime, int index, boolean create, List<T> masterTables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends MasterTable> List<T> masterTables(DataRuntime runtime, boolean create, List<T> masterTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends MasterTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, MasterTable masterTable, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable masterTable)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, MasterTable masterTable, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * masterTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  Metadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> List<T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}

		try{
			long fr = System.currentTimeMillis();
			MasterTable search = new MasterTable();
			if(
					(supportCatalog() && empty(catalog))    //支持catalog 但catalog为空
							|| (supportSchema() && empty(schema))	//支持schema 但schema为空
			) {
				MasterTable tmp = new MasterTable();
				if(!greedy) { //非贪婪模式下 检测当前catalog schema
					checkSchema(runtime, tmp);
				}
				if(supportCatalog() && empty(catalog)) {
					catalog = tmp.getCatalog();
				}
				if(supportSchema() && empty(schema)) {
					schema = tmp.getSchema();
				}
			}
			String caches_key = CacheProxy.key(runtime, "masterTables", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.masterTables(caches_key);
			if(null != list && !list.isEmpty()){
				return list;
			}
			String cache_key = CacheProxy.key(runtime, "masterTable", greedy, catalog, schema, pattern);
			String origin = CacheProxy.name(cache_key);
			if(null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
				//先查出所有key并以大写缓存 用来实现忽略大小写
				masterTableMap(runtime, random, greedy, catalog, schema, null);
				origin = CacheProxy.name(cache_key);
			}
			if(null == origin) {
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);
			PageNavi navi = null;
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			navi = configs.getPageNavi();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryMasterTablesRun(runtime, greedy, catalog, schema, origin, types, configs);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						if(null != navi){
							run.setPageNavi(navi);
							mergeFinalQuery(runtime, run);
						}
						DataSet set = select(runtime, random, true, (String)null, configs.keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						list = masterTables(runtime, idx++, true, list, catalog, schema, set);
						if(null != navi){
							//分页只查一次
							break;
						}
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[masterTables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统表查询失败后根据驱动内置接口补充
			if(list.isEmpty()) {
				try {
					list = masterTables(runtime, true, list, catalog, schema, origin, types);
					//删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
					if(!greedy) {
						int size = list.size();
						for(int i=size-1;i>=0; i--) {
							MasterTable item = list.get(i);
							if(!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
								list.remove(i);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[masterTables][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(MasterTable masterTable:list) {
				if(BasicUtil.isNotEmpty(masterTable.getComment())) {
					comment = true;
					break;
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryMasterTablesCommentRun(runtime, catalog, schema, origin, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							if(null != navi){
								run.setPageNavi(navi);
								//mergeFinalQuery(runtime, run);
							}
							DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
							list = comments(runtime, idx++, true, list, catalog, schema, set);
							if(null != navi){
								break;
							}
							//merge(list, maps);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.info("{}[masterTables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[masterTables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
			if(BasicUtil.isNotEmpty(origin)) {
				origin = origin.replace("%",".*");
				//有表名的，根据表名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
				for(T item:list) {
					String name = item.getName(greedy)+"";
					if(RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
						if(equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
							tmp.add(item);
						}
					}
				}
				list = tmp;
			}
			if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
				//查询全部表结构 columns()内部已经给masterTable.columns赋值
				List<Column> columns = columns(runtime, random, greedy, catalog, schema, (List<MasterTable>)list);
			}
			if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
				//查询全部表结构
				indexes(runtime, random, greedy, (List<MasterTable>)list);
			}
			CacheProxy.tables(caches_key, list);
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[masterTables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * masterTable[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	protected void masterTableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<MasterTable> masterTables = null;
		boolean sys = false; //根据系统表查询
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryMasterTablesRun(runtime, greedy, catalog, schema, null, MasterTable.TYPE.NORMAL.value, null);
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					masterTables = masterTables(runtime, idx++, true, masterTables, catalog, schema, set);
					for(MasterTable masterTable:masterTables){
						String cache_key = CacheProxy.key(runtime, "masterTable", greedy, catalog, schema, masterTable.getName());
						CacheProxy.name(cache_key, masterTable.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				masterTables = masterTables(runtime, true, masterTables, catalog, schema, null, MasterTable.TYPE.NORMAL.value);
				for(MasterTable masterTable:masterTables){
					String cache_key = CacheProxy.key(runtime, "masterTable", greedy, catalog, schema, masterTable.getName());
					CacheProxy.name(cache_key, masterTable.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * masterTable[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> masterTables = new LinkedHashMap<>();
		List<T> list = masterTables(runtime, random, false, catalog, schema, pattern, types, struct, configs);
		for(T masterTable:list) {
			masterTables.put(masterTable.getName().toUpperCase(), masterTable);
		}
		return masterTables;
	}

	/**
	 * masterTable[命令合成]<br/>
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
	public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * masterTable[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	public List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * masterTable[结果集封装]<br/>
	 *  根据查询结果集构造MasterTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param masterTables 上一步查询结果
	 * @param set 查询结果集
	 * @return masterTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == masterTables) {
			masterTables = new LinkedHashMap<>();
		}
		return masterTables;
	}

	/**
	 * masterTable[结果集封装]<br/>
	 *  根据查询结果集构造MasterTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param masterTables 上一步查询结果
	 * @param set 查询结果集
	 * @return masterTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> List<T> masterTables(DataRuntime runtime, int index, boolean create, List<T> masterTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> List<T> masterTables(DataRuntime runtime, int index, boolean create, List<T> masterTables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == masterTables) {
			masterTables = new ArrayList<>();
		}
		return masterTables;
	}

	/**
	 * masterTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param masterTables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return masterTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == masterTables) {
			masterTables = new LinkedHashMap<>();
		}
		return masterTables;
	}

	/**
	 * masterTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param masterTables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types Metadata.TYPE.
	 * @return masterTables
	 * @throws Exception 异常
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> List<T> masterTables(DataRuntime runtime, boolean create, List<T> masterTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> List<T> masterTables(DataRuntime runtime, boolean create, List<T> masterTables, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		if(null == masterTables) {
			masterTables = new ArrayList<>();
		}
		return masterTables;
	}

	/**
	 * masterTable[结果集封装]<br/>
	 * 根据查询结果封装MasterTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return MasterTable
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * masterTable[结果集封装]<br/>
	 * 根据查询结果封装MasterTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return MasterTable
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return null;
	}
	/**
	 * masterTable[结构集封装-依据]<br/>
	 * 读取masterTable元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return MasterTableMetadataAdapter
	 */
	@Override
	public MasterTableMetadataAdapter masterTableMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 MasterTableMetadataAdapter masterTableMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new MasterTableMetadataAdapter();
	}


	/**
	 *
	 * masterTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, MasterTable meta, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, meta);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传masterTable,这里的masterTable用来查询表结构
					DataSet set = select(runtime, random, true, (MasterTable)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, meta, list, set);
				}
				meta.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = meta.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, meta, true);
					meta.setColumns(columns);
					meta.setTags(tags(runtime, random, false, meta));
				}
				PrimaryKey pk = meta.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, meta);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				meta.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = meta.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					meta.setIndexes(indexes(runtime, random, meta, null));
				}
				runs = buildCreateRun(runtime, meta);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					meta.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[masterTable ddl][masterTable:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[masterTable ddl][{}][masterTable:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * masterTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param masterTable 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable masterTable) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable masterTable)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * masterTable[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param masterTable 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, MasterTable masterTable, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, MasterTable masterTable, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String pattern)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags)
	 * [结果集封装]<br/>
	 * <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
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
	public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern) {
		LinkedHashMap<String,T> tables = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryPartitionTablesRun(runtime, master, tags, pattern);
				if(null != runs) {
					int idx = 0;
					int total = runs.size();
					for(Run run:runs) {
						DataSet set = select(runtime, random, false, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						tables = partitionTables(runtime, total, idx++, true, master, tables, master.getCatalog(), master.getSchema(), set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][stable:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), master.getName(), e.toString());
				}
			}

			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[tables][stable:{}][result:{}][执行耗时:{}]", random, master.getName(), tables.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[partitionTables][result:fail][msg:{}]", e.toString());
			}
		}
		return tables;
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
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @param name 名称统配符或正则
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String name) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String name)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master)", 37));
		}
		return new ArrayList<>();
	}

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
	public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, PartitionTable> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable table, Catalog catalog, Schema schema, LinkedHashMap<String, PartitionTable> tables, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		if(null == tables) {
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

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
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, PartitionTable> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)", 37));
		}
		if(null == tables) {
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

	/**
	 * partition table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table PartitionTable
	 * @return List
	 */
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, table, list, set);
				}
				table.setDdls(list);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[partition table ddl][table:{}][result:{}][执行耗时:{}]", random, table.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[partition table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询子表创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table)", 37));
		}
		return runs;
	}

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
	@Override
	public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

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
	@Override
	public <T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}
	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果封装PartitionTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return PartitionTable
	 */
	@Override
	public <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
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
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
	 * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * column[调用入口]<br/>
	 * 查询表结构(多方法合成)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary, ConfigStore configs) {
		if (!greedy) {
			checkSchema(runtime, table);
		}
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		String key = CacheProxy.key(runtime, "table_columns", greedy, table);
		LinkedHashMap<String,T> columns = CacheProxy.columns(key);
		if(null != columns && !columns.isEmpty()) {
			return columns;
		}
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		try {

			int qty_total = 0;
			int qty_dialect = 0; //优先根据系统表查询
			int qty_metadata = 0; //再根据metadata解析
			int qty_jdbc = 0; //根据驱动内置接口补充

			// 1.优先根据系统表查询
			try {
				List<Run> runs = buildQueryColumnsRun(runtime, table, false, configs);
				if (null != runs) {
					int idx = 0;
					for (Run run: runs) {
						DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run);
						columns = columns(runtime, idx, true, table, columns, set);
						idx++;
					}
				}
				if(null != columns) {
					qty_dialect = columns.size();
					qty_total=columns.size();
				}
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
				if(primary) {
					e.printStackTrace();
				} if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			// 2.根据驱动内置接口补充
			// 再根据metadata解析 SELECT * FROM T WHERE 1=0
			if (null == columns || columns.isEmpty()) {
				try {
					List<Run> runs = buildQueryColumnsRun(runtime, table, true);
					if (null != runs) {
						for (Run run  : runs) {
							String sql = run.getFinalQuery();
							if(BasicUtil.isNotEmpty(sql)) {
								columns = actuator.columns(this, runtime, true, columns, table, sql);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						log.error("columns exception:", e);
					}else {
						if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
							log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.toString());
						}
					}
				}
				if(null != columns) {
					qty_metadata = columns.size() - qty_dialect;
					qty_total = columns.size();
				}
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据驱动内置接口补充:{}][执行耗时:{}]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, DateUtil.format(System.currentTimeMillis() - fr));
			}

			// 方法(3)根据根据驱动内置接口补充
			if (null == columns || columns.isEmpty()) {
				columns = actuator.metadata(this, runtime, true, columns, table, null);

				if(null != columns) {
					qty_total = columns.size();
					qty_jdbc = columns.size() - qty_metadata - qty_dialect;
				}
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据根据驱动内置接口补充:{}][执行耗时:{}]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, DateUtil.format(System.currentTimeMillis() - fr));
			}
			//检测主键
			if(ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY) {
				if (null != columns || !columns.isEmpty()) {
					boolean exists = false;
					for(Column column:columns.values()) {
						if(column.isPrimaryKey() != -1) {
							exists = true;
							break;
						}
					}
					if(!exists) {
						PrimaryKey pk = primary(runtime, random, false, table);
						if(null != pk) {
							LinkedHashMap<String,Column> pks = pk.getColumns();
							if(null != pks) {
								for(String k:pks.keySet()) {
									Column column = columns.get(k);
									if(null != column) {
										column.primary(true);
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				log.error("columns exception:", e);
			}else{
				log.error("{}[columns][result:fail][table:{}][msg:{}]", random, table, e.toString());
			}
		}
		if(null != columns) {
			CacheProxy.cache(key, columns);
		}else{
			columns = new LinkedHashMap<>();
		}
		int index = 0;
		for(Column column:columns.values()) {
			if(null == column.getPosition() || -1 == column.getPosition()) {
				column.setPosition(index++);
			}
			if(column.isAutoIncrement() != 1) {
				column.autoIncrement(false);
			}
			if(column.isPrimaryKey() != 1) {
				column.setPrimary(false);
			}
			if(null == column.getTable() && !greedy) {
				column.setTable(table);
			}
		}
		return columns;
	}

	/**
	 * column[调用入口]<br/>
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
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table, ConfigStore configs) {
		List<Table> tables = new ArrayList<>();
		tables.add(table);
		return columns(runtime, random, greedy, catalog, schema, tables, configs);
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
		List<T> columns = new ArrayList<>();
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		Table tab = null;
		if(null != tables && !tables.isEmpty()) {
			tab = tables.iterator().next();
		}

		if(null!= tab) {
			tab.setCatalog(catalog);
			tab.setSchema(schema);
			if (BasicUtil.isEmpty(catalog) && BasicUtil.isEmpty(schema) && !greedy) {
				checkSchema(runtime, tab);
			}
		}
		//根据系统表查询
		try {
			List<Run> runs = buildQueryColumnsRun(runtime, catalog, schema, tables, false, configs);
			if (null != runs) {
				int idx = 0;
				for (Run run: runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run);
					columns = columns(runtime, idx, true, (Table)null, columns, set);
					idx++;
				}
			}

			for(Table table:tables) {
				Long tObjectId = table.getObjectId();
				LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
				table.setColumns(cols);
				for(Column column:columns) {
					if(table.equals(column.getTable())) {
						Catalog cCatalog = column.getCatalog();
						Schema cSchema = column.getSchema();
						Long cObjectId = column.getObjectId();
						if(null != tObjectId && null != cObjectId && tObjectId == cObjectId) {
							cols.put(column.getName().toUpperCase(), column);
						}else{
							if(equals(cCatalog, column.getCatalog())
									&& equals(cSchema, column.getSchema())
									&& BasicUtil.equals(table.getName(), column.getTableName(), true)
							) {
								cols.put(column.getName().toUpperCase(), column);
							}
						}
					}
				}
				table.setColumns(cols);
				columns.removeAll(cols.values());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return columns;
	}
	/**
	 * column[调用入口]<br/>
	 * DatabaseMetaData(方法3)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @return columns 上一步查询结果
	 * @return pattern 列名称通配符
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern)", 37));
		}
		return new LinkedHashMap<>();
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
	public List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildQueryColumnsRun(DataRuntime runtime, Catalog catalog, Schema schema, Collection<? extends Table> tables, boolean metadata, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryColumnsRun(DataRuntime runtime, Catalog catalog, Schema schema, Collection<? extends Table> tables, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}
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
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

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
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set)", 37));
		}
		return new ArrayList<>();
	}
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
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> columns, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> columns, DataSet set)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * column[结果集封装]<br/>
	 * (方法1)
	 * <br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row 系统表查询SQL结果集
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row)", 37));
		}
		return meta;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 * column[结构集封装-依据]<br/>
	 * 读取column元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ColumnMetadataAdapter
	 */
	@Override
	public ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime)", 37));
		}
		return null;
	}
	/**
	 * column[结构集封装-依据]<br/>
	 * 读取column元数据结果集的依据(需要区分数据类型)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 具体数据类型,length/precisoin/scale三个属性需要根据数据类型覆盖通用配置
	 * @return ColumnMetadataAdapter
	 */
	@Override
	public ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta) {
		return DriverAdapter.super.columnMetadataAdapter(runtime, meta);
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
		if(null == meta) {
			return null;
		}
		String result = null;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Config config = MetadataAdapterHolder.get(type(), meta);
		if(null != config) {
			result = config.getLengthRefer();
		}

		//2.配置类-数据类型名称
		if(null == result) {
			//根据数据类型名称
			config = MetadataAdapterHolder.get(type(), meta.getName());
			if(null != config) {
				result = config.getLengthRefer();
			}
		}
		//3.数据类型自带(length/precision/scale)

		//4.配置类-数据类型大类
		if(null == result) {
			config = MetadataAdapterHolder.get(type(), meta.getCategory());
			if(null != config) {
				result = config.getLengthRefer();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(null == result) {
			ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
			if(null != adapter) {
				config = adapter.getTypeConfig();
				if(null != config) {
					result = config.getLengthRefer();
				}
			}
		}
		return result;
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
		if(null == meta) {
			return null;
		}
		String result = null;

		//1.配置类 数据类型
		TypeMetadata.Config config = MetadataAdapterHolder.get(type(), meta);
		if(null != config) {
			result = config.getPrecisionRefer();
		}

		//2.配置类-数据类型名称
		if(null == result) {
			//根据数据类型名称
			config = MetadataAdapterHolder.get(type(), meta.getName());
			if(null != config) {
				result = config.getPrecisionRefer();
			}
		}
		//3.数据类型自带(length/precision/scale)

		//4.配置类-数据类型大类
		if(null == result) {
			config = MetadataAdapterHolder.get(type(), meta.getCategory());
			if(null != config) {
				result = config.getPrecisionRefer();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(null == result) {
			ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
			if(null != adapter) {
				config = adapter.getTypeConfig();
				if(null != config) {
					result = config.getPrecisionRefer();
				}
			}
		}
		return result;
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
		if(null == meta) {
			return null;
		}
		String result = null;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Config config = MetadataAdapterHolder.get(type(), meta);
		if(null != config) {
			result = config.getScaleRefer();
		}

		//2.配置类-数据类型名称
		if(null == result) {
			//根据数据类型名称
			config = MetadataAdapterHolder.get(type(), meta.getName());
			if(null != config) {
				result = config.getScaleRefer();
			}
		}
		//3.数据类型自带(length/precision/scale)

		//4.配置类-数据类型大类
		if(null == result) {
			config = MetadataAdapterHolder.get(type(), meta.getCategory());
			if(null != config) {
				result = config.getScaleRefer();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(null == result) {
			ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
			if(null != adapter) {
				config = adapter.getTypeConfig();
				if(null != config) {
					result = config.getScaleRefer();
				}
			}
		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略长度<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public int columnMetadataIgnoreLength(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return -1;
		}
		int result = -1;

		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Config config = MetadataAdapterHolder.get(type(), meta);
		if(null != config) {
			result = config.ignoreLength();
		}

		//2.配置类-数据类型名称
		if(-1 == result) {
			//根据数据类型名称
			config = MetadataAdapterHolder.get(type(), meta.getName());
			if(null != config) {
				result = config.ignoreLength();
			}
		}
		//3.数据类型自带(length/precision/scale)
		if(-1 == result) {
			result = meta.ignoreLength();
		}
		//4.配置类-数据类型大类
		if(-1 == result) {
			config = MetadataAdapterHolder.get(type(), meta.getCategory());
			if(null != config) {
				result = config.ignoreLength();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(-1 == result) {
			ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
			if(null != adapter) {
				config = adapter.getTypeConfig();
				if(null != config) {
					result = config.ignoreLength();
				}
			}
		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public int columnMetadataIgnorePrecision(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return -1;
		}
		int result = -1;

		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Config config = MetadataAdapterHolder.get(type(), meta);
		if(null != config) {
			result = config.ignorePrecision();
		}

		//2.配置类-数据类型名称
		if(-1 == result) {
			//根据数据类型名称
			config = MetadataAdapterHolder.get(type(), meta.getName());
			if(null != config) {
				result = config.ignorePrecision();
			}
		}
		//3.数据类型自带(length/precision/scale)
		if(-1 == result) {
			result = meta.ignorePrecision();
		}
		//4.配置类-数据类型大类
		if(-1 == result) {
			config = MetadataAdapterHolder.get(type(), meta.getCategory());
			if(null != config) {
				result = config.ignorePrecision();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(-1 == result) {
			ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
			if(null != adapter) {
				config = adapter.getTypeConfig();
				if(null != config) {
					result = config.ignorePrecision();
				}
			}
		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略小数位<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public int columnMetadataIgnoreScale(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return -1;
		}
		int result = -1;

		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Config config = MetadataAdapterHolder.get(type(), meta);
		if(null != config) {
			result = config.ignoreScale();
		}

		//2.配置类-数据类型名称
		if(-1 == result) {
			//根据数据类型名称
			config = MetadataAdapterHolder.get(type(), meta.getName());
			if(null != config) {
				result = config.ignoreScale();
			}
		}
		//3.数据类型自带(length/precision/scale)
		if(-1 == result) {
			result = meta.ignoreScale();
		}
		//4.配置类-数据类型大类
		if(-1 == result) {
			config = MetadataAdapterHolder.get(type(), meta.getCategory());
			if(null != config) {
				result = config.ignoreScale();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(-1 == result) {
			ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime);
			if(null != adapter) {
				config = adapter.getTypeConfig();
				if(null != config) {
					result = config.ignoreScale();
				}
			}
		}
		return result;
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
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTagsRun(DataRuntime runtime, Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set)", 37));
		}
		if(null == tags) {
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

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
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)", 37));
		}
		if(null == tags) {
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

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
	@Override
	public <T extends Tag> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 * tag[结果集封装]<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Tag
	 * @param <T> Tag
	 */
	@Override
	public <T extends Tag> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
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
	 * PrimaryKey primary(DataRuntime runtime, Table table)
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
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table) {
		PrimaryKey primary = null;
		if(!greedy) {
			checkSchema(runtime, table);
		}
		String tab = table.getName();
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		if(null == random) {
			random = random(runtime);
		}
		try{
			List<Run> runs = buildQueryPrimaryRun(runtime, table);
			if(null != runs) {
				int idx = 0;
				for(Run run:runs) {
					DataSet set = select(runtime, random, false, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					primary = init(runtime, idx, primary, table, set);
					primary = detail(runtime, idx, primary, table, set);
					if(null != primary) {
						primary.setTable(table);
					}
					idx ++;
				}
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.warn("{}[primary][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), catalog, schema, table, e.toString());
			}
		}
		table.setPrimaryKey(primary);
		return primary;
	}

	/**
	 * primary[命令合成]<br/>
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, Table table, DataSet set)", 37));
		}
		return meta;
	}

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataSet set)", 37));
		}
		return meta;
	}

	/**
	 * primary[结构集封装-依据]<br/>
	 * primary元数据名称依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return column name
	 */
	@Override
	public PrimaryMetadataAdapter primaryMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String primaryMetadataName(DataRuntime runtime)", 37));
		}
		return null;
	}
	/**
	 * primary[结构集封装]<br/>
	 *  根据驱动内置接口补充PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @throws Exception 异常
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, Table table) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 PrimaryKey primary(DataRuntime runtime, Table table)", 37));
		}
		return null;
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
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table) {
		LinkedHashMap<String, T> foreigns = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		try {
			List<Run> runs = buildQueryForeignsRun(runtime, table);
			if(null != runs) {
				int idx = 0;
				for(Run run:runs) {
					DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					foreigns = foreigns(runtime, idx, table, foreigns, set);
					idx++;
				}
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
		}
		return foreigns;
	}

	/**
	 * foreign[命令合成]<br/>
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

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
	@Override
	public <T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row)", 37));
		}
		return meta;
	}

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
	@Override
	public <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name)
	 * [结果集封装]<br/>
	 * <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set)
	 * <T extends Index> List< T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate)
	 ******************************************************************************************************************/
	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param tables 表
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Collection<? extends Table> tables) {
		List<T> indexes = null;
		if(null == random) {
			random = random(runtime);
		}
		//根据系统表查询
		try {
			List<Run> runs = buildQueryIndexesRun(runtime, tables);
			if (null != runs) {
				int idx = 0;
				for (Run run: runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run);
					indexes = indexes(runtime, idx, true, tables, indexes, set);
					idx++;
				}
			}
			if(null != indexes) {
				for (Table table : tables) {
					Long tObjectId = table.getObjectId();
					LinkedHashMap<String, Index> idxs = new LinkedHashMap<>();
					table.setIndexes(idxs);
					for (Index index : indexes) {
						if (table.equals(index.getTable())) {
							Catalog cCatalog = index.getCatalog();
							Schema cSchema = index.getSchema();
							Long cObjectId = index.getObjectId();
							if (null != tObjectId && null != cObjectId && tObjectId == cObjectId) {
								idxs.put(index.getName().toUpperCase(), index);
							} else {
								if (equals(cCatalog, index.getCatalog())
										&& equals(cSchema, index.getSchema())
										&& BasicUtil.equals(table.getName(), index.getTableName(), true)
								) {
									idxs.put(index.getName().toUpperCase(), index);
								}
							}
						}
					}
					indexes.removeAll(idxs.values());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null == indexes) {
			indexes = new ArrayList<>();
		}
		return indexes;
	}
	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param pattern 索引名称
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern) {
		List<T> indexes = null;
		if(null == table) {
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		List<Run> runs = buildQueryIndexesRun(runtime, table, pattern);
		if(null != runs) {
			int idx = 0;
			for(Run run:runs) {
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					indexes = indexes(runtime, idx, true, table, indexes, set);
				}catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		if(null == indexes || indexes.isEmpty()) {
			if(null != table.getName()) {
				try {
					LinkedHashMap<String,T> maps = indexes(runtime, true, new LinkedHashMap<>(), table, false, false);
					table.setIndexes(maps);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
			}
		}
		if(null == indexes) {
			indexes = new ArrayList<>();
		}
		return indexes;
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
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern) {
		LinkedHashMap<String,T> indexes = null;
		if(null == table) {
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}

		checkSchema(runtime, table);

		List<Run> runs = buildQueryIndexesRun(runtime, table, pattern);

		if(null != runs) {
			int idx = 0;
			for(Run run:runs) {
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					indexes = indexes(runtime, idx, true, table, indexes, set);
				}catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		if(null == indexes || indexes.isEmpty()) {
			if(null != table.getName()) {
				try {
					indexes = indexes(runtime, true, indexes, table, false, false);
					table.setIndexes(indexes);
				} catch (Exception e) {
					log.info("{}[{}][table:{}][msg:{}]", random, LogUtil.format("JDBC方式获取索引失败", 33), table, e.toString());
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
					indexes = new LinkedHashMap<>();
				}
				if(BasicUtil.isNotEmpty(pattern)) {
					T index = indexes.get(pattern.toUpperCase());
					indexes = new LinkedHashMap<>();
					indexes.put(pattern.toUpperCase(), index);
				}
			}
		}
		Index pk = null;
		if(null != indexes) {
			for (Index index : indexes.values()) {
				if (index.isPrimary()) {
					pk = index;
					break;
				}
			}
		}
		if(null == pk) {
			//识别主键索引
			pk = table.getPrimaryKey();
			if (null == pk) {
				pk = primary(runtime, random, false, table);
			}
			if (null != pk) {
				Index index = indexes.get(pk.getName().toUpperCase());
				if (null != index) {
					index.setPrimary(true);
				} else {
					indexes.put(pk.getName().toUpperCase(), (T) pk);
				}
			}
		}
		if(null == indexes) {
			indexes = new LinkedHashMap<>();
		}
		return indexes;
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
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * index[命令合成]<br/>
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tables 表
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, Collection<? extends Table> tables) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryIndexesRun(DataRuntime runtime, Collection<? extends Table> tables)", 37));
		}
		return new ArrayList<>();
	}
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
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set) throws Exception {
		if(null == indexes) {
			indexes = new LinkedHashMap<>();
		}
		IndexMetadataAdapter config = indexMetadataAdapter(runtime);
		for(DataRow row:set) {
			String name = row.getString(config.getNameRefers());
			if(null == name) {
				continue;
			}
			T meta = indexes.get(name.toUpperCase());
			meta = init(runtime, index, meta, table, row);
			if(null != table) {
				if (!table.getName().equalsIgnoreCase(meta.getTableName())) {
					continue;
				}
			}
			meta = detail(runtime, index, meta, table, row);
			if(null != meta) {
				indexes.put(meta.getName().toUpperCase(), meta);
			}
		}
		return indexes;
	}

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
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set) throws Exception {
		if(null == indexes) {
			indexes = new ArrayList<>();
		}
		IndexMetadataAdapter config = indexMetadataAdapter(runtime);
		for(DataRow row:set) {
			String name = row.getString(config.getNameRefers());
			if(null == name) {
				continue;
			}
			T meta = (T)new Index(name.toUpperCase());
			meta = init(runtime, index, meta, table, row);
			if(null != table) {
				if (!table.getName().equalsIgnoreCase(meta.getTableName())) {
					continue;
				}
			}
			meta = detail(runtime, index, meta, table, row);
			if(null != meta) {
				indexes.add(meta);
			}
		}
		return indexes;
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 表
	 * @param indexes 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> indexes, DataSet set) throws Exception {
		if(null == indexes) {
			indexes = new ArrayList<>();
		}
		Map<String,Table> tbls = new HashMap<>();
		for(Table table:tables) {
			tbls.put(table.getName().toUpperCase(), table);
		}
		for(DataRow row:set) {
			T meta = null;
			meta = init(runtime, index, meta, null, row);
			if(null == Metadata.match(meta, indexes)) {
				indexes.add(meta);
			}
			detail(runtime, index, meta, null, row);
			String tableName = meta.getTableName();
			if(null != tableName) {
				Table table = tbls.get(tableName.toUpperCase());
				if(null != table) {
					table.add(meta);
				}
			}
		}
		return indexes;
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
	public <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate)", 37));
		}
		if(null == indexes) {
			indexes = new ArrayList<>();
		}
		return indexes;
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
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate)", 37));
		}
		if(null == indexes) {
			indexes = new LinkedHashMap<>();
		}
		return indexes;
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
	public <T extends Index> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception{
		IndexMetadataAdapter config = indexMetadataAdapter(runtime);
		String name = row.getString(config.getNameRefers());
		if(null == meta) {
			meta = (T)new Index();
			meta.setName(name);
			Catalog catalog = null;
			Schema schema = null;
			String catalogName = row.getString(config.getCatalogRefers());
			if(BasicUtil.isNotEmpty(catalogName)) {
				catalog = new Catalog(catalogName);
			}else{
				if(null != table) {
					catalog = table.getCatalog();
				}
			}
			String schemaName = row.getString(config.getSchemaRefers());
			if(BasicUtil.isNotEmpty(schemaName)) {
				schema = new Schema(schemaName);
			}else{
				if(null != table) {
					schema = table.getSchema();
				}
			}

			if(null == table) {
				String tableName = row.getString(config.getTableRefers());
				table = new Table(catalog, schema, tableName);
			}
			meta.setCatalog(catalog);
			meta.setSchema(schema);
			meta.setTable(table);
			meta.setMetadata(row);

			//是否主键
			String[] chks = config.getCheckPrimaryRefers();
			String[] vals = config.getCheckPrimaryValues();
			Boolean bol = parseBoolean(row, chks, vals);
			if(null != bol){
				meta.setPrimary(bol);
			}
			//是否唯一
			chks = config.getCheckUniqueRefers();
			vals = config.getCheckUniqueValues();
			bol = parseBoolean(row, chks, vals);
			if(null != bol){
				meta.setUnique(bol);
			}
		}
		return meta;
	}

	/**
	 * parse boolean
	 * @param row 结果集
	 * @param cols 检测的我
	 * @param vals 匹配true的值S(只要一项匹配就返回true)
	 * @return boolean
	 */
	protected Boolean parseBoolean(DataRow row, String[] cols, String[] vals){
		Boolean bol = null;
		if(null != cols){
			for(String col:cols){
				Object value = row.get(col);
				if(null == value){
					continue;
				}
				if(value instanceof Boolean){
					bol = BasicUtil.parseBoolean(value);
				}else if(null != vals) {
					String str = value.toString();
					for (String val : vals) {
						if (str.equalsIgnoreCase(val)) {
							bol = true;
							break;
						}
					}
				}
				if(null != bol){
					break;
				}
			}
			if(null == bol) {
				bol = false;
			}
		}
		return bol;
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
	public <T extends Index> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception{
		IndexMetadataAdapter config = indexMetadataAdapter(runtime);
		//oracle中取了两列COLUMN_EXPRESSION,COLUMN_NAME("NAME",SYS_NC00009$)
		String columnName = row.getStringWithoutEmpty(config.getColumnRefers());
		if(null == columnName) {
			return meta;
		}
		columnName = columnName.replace("\"", "");
		Column column = meta.getColumn(columnName.toUpperCase());
		if(null == column) {
			column = new Column();
		}
		column.setName(columnName);
		meta.addColumn(column);
		Integer position = row.getInt(config.getColumnPositionRefers());
		if(null == position) {
			position = 0;
		}
		column.setPosition(position);
		meta.setPosition(column, position);
		String order = row.getString(config.getColumnOrderRefers());
		if(null != order) {
			order = order.toUpperCase();
			Order.TYPE type = Order.TYPE.ASC;
			if(order.contains("DESC")) {
				type = Order.TYPE.DESC;
			}
			meta.setOrder(column, type);
		}
		return meta;
	}
	/**
	 * index[结构集封装-依据]<br/>
	 * 读取index元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	@Override
	public IndexMetadataAdapter indexMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 IndexMetadataAdapter indexMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new IndexMetadataAdapter();
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
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set)", 37));
		}
		if(null == constraints) {
			constraints = new ArrayList<>();
		}
		return constraints;
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param column 列
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Constraint>  constraints(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set)", 37));
		}
		if(null == constraints) {
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}

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
	@Override
	public <T extends Constraint> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Constraint
	 */
	@Override
	public <T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 * catalog[结构集封装-依据]<br/>
	 * 读取catalog元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ConstraintMetadataAdapter
	 */
	@Override
	public ConstraintMetadataAdapter constraintMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 ConstraintMetadataAdapter constraintMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new ConstraintMetadataAdapter();
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
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)", 37));
		}
		if(null == triggers) {
			triggers = new LinkedHashMap<>();
		}
		return triggers;
	}

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
	@Override
	public <T extends Trigger> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Trigger
	 */
	@Override
	public <T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 * trigger[结构集封装-依据]<br/>
	 * 读取 trigger 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	@Override
	public TriggerMetadataAdapter triggerMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 TriggerMetadataAdapter triggerMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new TriggerMetadataAdapter();
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
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures)
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception;
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
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> ArrayList<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */

	public List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, procedure);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, procedure, list, set);
				}
				if(list.size()>0) {
					procedure.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装

			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[procedure ddl][procedure:{}][result:{}][执行耗时:{}]", random, procedure.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[procedure ddl][{}][procedure:{}][msg:{}]", random, LogUtil.format("查询存储过程的创建DDL失败", 33), procedure.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure)", 37));
		}
		return runs;
	}

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
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

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
	@Override
	public <T extends Procedure> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set)", 37));
		}
		return meta;
	}
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Procedure
	 */
	@Override
	public <T extends Procedure> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set)", 37));
		}
		return meta;
	}

	/**
	 * procedure[结构集封装-依据]<br/>
	 * 读取 procedure 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return ProcedureMetadataAdapter
	 */
	@Override
	public ProcedureMetadataAdapter procedureMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 ProcedureMetadataAdapter procedureMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new ProcedureMetadataAdapter();
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
	 * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions)
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)
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
	public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	public List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
	}

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
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

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
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Function
	 * @return ddl
	 */
	public List<String> ddl(DataRuntime runtime, String random, Function meta) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, meta);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, meta, list, set);
				}
				if(list.size()>0) {
					meta.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[function ddl][function:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[function ddl][{}][function:{}][msg:{}]", random, LogUtil.format("查询函数的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, Function meta)", 37));
		}
		return runs;
	}

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
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

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
	@Override
	public <T extends Function> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		FunctionMetadataAdapter config = functionMetadataAdapter(runtime);
		if(null == meta) {
			meta = (T)new Function();
		}
		if(null != config) {
			meta.setName(row.getString(config.getNameRefers()));
			meta.setSchema(row.getString(config.getSchemaRefers()));
			meta.setComment(row.getString(config.getCommentRefers()));
			meta.setDefinition(row.getString(config.getDefineRefers()));
		}
		return meta;
	}
	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Function
	 */
	@Override
	public <T extends Function> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}
	/**
	 * function[结构集封装-依据]<br/>
	 * 读取 function 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return FunctionMetadataAdapter
	 */
	@Override
	public FunctionMetadataAdapter functionMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 FunctionMetadataAdapter functionMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new FunctionMetadataAdapter();
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
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences)
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences)
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
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {

		if(null == random) {
			random = random(runtime);
		}
		List<T> sequences = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySequencesRun(runtime, catalog, schema, pattern);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						sequences = sequences(runtime, idx++, true, sequences, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[sequences][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[sequences][result:{}][执行耗时:{}]", random, sequences.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[sequences][result:fail][msg:{}]", e.toString());
			}
		}
		return sequences;
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
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {

		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, T> sequences = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySequencesRun(runtime, catalog, schema, pattern);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						sequences = sequences(runtime, idx++, true, sequences, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[sequences][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[sequences][result:{}][执行耗时:{}]", random, sequences.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[sequences][result:fail][msg:{}]", e.toString());
			}
		}
		return sequences;
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	public List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
	}

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
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

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
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Sequence
	 * @return ddl
	 */
	public List<String> ddl(DataRuntime runtime, String random, Sequence meta) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlsRun(runtime, meta);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, (Table)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, meta, list, set);
				}
				if(list.size()>0) {
					meta.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[sequence ddl][sequence:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[sequence ddl][{}][sequence:{}][msg:{}]", random, LogUtil.format("查询序列的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return runs;
	}

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
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

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
	@Override
	public <T extends Sequence> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}
	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Sequence
	 */
	@Override
	public <T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}


	/**
	 * sequence[结构集封装-依据]<br/>
	 * 读取 sequence 元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return SequenceMetadataAdapter
	 */
	@Override
	public SequenceMetadataAdapter sequenceMetadataAdapter(DataRuntime runtime) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 SequenceMetadataAdapter sequenceMetadataAdapter(DataRuntime runtime)", 37));
		}
		return new SequenceMetadataAdapter();
	}

	/* *****************************************************************************************************************
	 * 													common
	 * ----------------------------------------------------------------------------------------------------------------
	 */
	/**
	 *
	 * 根据 catalog, name检测schemas集合中是否存在
	 * @param schemas schemas
	 * @param catalog catalog
	 * @param name name
	 * @return 如果存在则返回 Schema 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name) {
		if(null != schemas) {
			for(T schema:schemas) {
				if(BasicUtil.equalsIgnoreCase(catalog, schema.getCatalog())
						&& schema.getName().equalsIgnoreCase(name)
				) {
					return schema;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * 根据 name检测catalogs集合中是否存在
	 * @param catalogs catalogs
	 * @param name name
	 * @return 如果存在则返回 Catalog 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Catalog> T catalog(List<T> catalogs, String name) {
		if(null != catalogs) {
			for(T catalog:catalogs) {
				if(catalog.getName().equalsIgnoreCase(name)) {
					return catalog;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * 根据 name检测databases集合中是否存在
	 * @param databases databases
	 * @param name name
	 * @return 如果存在则返回 Database 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Database> T database(List<T> databases, String name) {
		if(null != databases) {
			for(T database:databases) {
				if(database.getName().equalsIgnoreCase(name)) {
					return database;
				}
			}
		}
		return null;
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
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run)", 37));
		}
		return false;
	}
	/**
	 * 执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param runs 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return boolean
	 */
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, List<Run> runs) {
		boolean result = true;
		int idx = 0;
		long frs = System.currentTimeMillis();
		ACTION.SWITCH swt = meta.swt();
		if(swt == ACTION.SWITCH.CONTINUE) {//上一步执行状态保存在meta中
			swt = InterceptorProxy.before(runtime, random, action, meta, runs);
			if (swt == ACTION.SWITCH.CONTINUE) {
				for (Run run : runs) {
					swt = InterceptorProxy.before(runtime, random, action, meta, run, runs);
					long fr = System.currentTimeMillis();
					if (swt == ACTION.SWITCH.CONTINUE) {
						result = execute(runtime, random + "[index:" + idx++ + "]", meta, action, run) && result;
					} else if (swt == ACTION.SWITCH.SKIP) {//跳过after
						continue;
					} else if (swt == ACTION.SWITCH.BREAK) {//中断整组命令
						break;
					}
					swt = InterceptorProxy.after(runtime, random, action, meta, run, runs, result, System.currentTimeMillis() - fr);
					if (swt == ACTION.SWITCH.BREAK) {
						break;
					}
				}
				long millis = System.currentTimeMillis() - frs;
				if(runs.size()>1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
					//如果有多个命令执行，再统计一次 合计时间
					log.info("{}[action:{}][meta:{}][cmds:{}][result:{}][执行耗时:{}]", random, action, meta.getName(), runs.size(), result, DateUtil.format(millis));
				}
				swt = InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean create(DataRuntime runtime, Table meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		//检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
		checkPrimary(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * 检测列的执行命令,all drop alter等
	 * @param meta 表
	 * @return cols
	 */
	protected LinkedHashMap<String, Column> checkColumnAction(DataRuntime runtime, Table meta) {
		Table update = (Table)meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		for(Column col:columns.values()) {
			typeMetadata(runtime, col);
		}
		for(Column col:ucolumns.values()) {
			typeMetadata(runtime, col);
		}
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
		// 更新列
		for (Column ucolumn : ucolumns.values()) {
			//先根据原列名 找到数据库中定义的列
			Column column = columns.get(ucolumn.getName().toUpperCase());
			//再检测update(如果name不一样需要rename)
			if(null != ucolumn.getUpdate()) {
				ucolumn = ucolumn.getUpdate();
			}
			if (null != column) {
				// 修改列
				if (!column.equals(ucolumn)) {
					column.setTable(update);
					column.setUpdate(ucolumn, false, false);
					column.setAction(ACTION.DDL.COLUMN_ALTER);
					ucolumn.setAction(ACTION.DDL.COLUMN_ALTER);
					cols.put(column.getName().toUpperCase(), column);
				}
			} else {
				// 添加列
				ucolumn.setTable(update);
				ucolumn.setAction(ACTION.DDL.COLUMN_ADD);
				cols.put(ucolumn.getName().toUpperCase(), ucolumn);
			}
		}
		List<String> deletes = new ArrayList<>();
		// 删除列(根据删除标记)
		for (Column column : ucolumns.values()) {
			if (column.isDrop()) {
				/*drop(column);*/
				deletes.add(column.getName().toUpperCase());
				column.setAction(ACTION.DDL.COLUMN_DROP);
				cols.put(column.getName().toUpperCase(), column);
			}
		}
		// 删除列(根据新旧对比)
		if (meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if (column instanceof Tag) {
					continue;
				}
				if (column.isDrop() || deletes.contains(column.getName().toUpperCase()) || ACTION.DDL.COLUMN_DROP == column.getAction()) {
					//上一步已删除
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					column.setAction(ACTION.DDL.COLUMN_DROP);
					cols.put(column.getName().toUpperCase(), column);
				}
			}
		}
		//忽略 删除不存的的列(原表中本来就没有的 还执行删除)
		for(Column column:cols.values()) {
			if(!columns.containsKey(column.getName().toUpperCase())) {
				if(column.getAction() == ACTION.DDL.COLUMN_DROP) {
					column.setAction(ACTION.DDL.IGNORE);
				}
			}
		}
		return cols;
	}

	/**
	 * 修改主键前先 根据主键检测自增 如果数据库要求自增必须在主键上时才需要执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return boolean
	 * @throws Exception 异常
	 */
	protected List<Run> checkAutoIncrement(DataRuntime runtime, String random, Table table, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Table update = (Table)table.getUpdate();
		if(!table.primaryEquals(update)) {
			LinkedHashMap<String, Column> pks = table.getPrimaryKeyColumns();
			LinkedHashMap<String, Column> npks = update.getPrimaryKeyColumns();
			LinkedHashMap<String, Column> columns = table.getColumns();
			if (null != pks) {
				for (String k : pks.keySet()) {
					Column auto = columns.get(k.toUpperCase());
					if (null != auto && auto.isAutoIncrement() == 1) {//原来是自增
						if (null != npks && !npks.containsKey(auto.getName().toUpperCase())) { //当前不是主键
							auto.primary(false);
							//取消自增
							runs = buildDropAutoIncrement(runtime, auto, slice);
						}
					}
				}
			}
		}
		return runs;
	}

	/**
	 * 合关DDL片段
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param slices slices
	 * @return list
	 */
	@Override
	public List<Run> merge(DataRuntime runtime, Table meta, List<Run> slices){
		List<Run> runs = new ArrayList<>();
		Run merge = null;
		if(null != slices && !slices.isEmpty()){
			StringBuilder builder = null;
			boolean first = true;
			for(Run item:slices) {
				if(BasicUtil.isNotEmpty(item)) {
					String line = item.getFinalUpdate().trim();
					if(BasicUtil.isEmpty(line)) {
						continue;
					}
					if(!item.slice()){
						//不支持合并的(不是片段的)
						runs.add(item);
						continue;
					}
					if(null == merge){
						merge = new SimpleRun(runtime);
						builder = merge.getBuilder();
						builder.append("ALTER ").append(keyword(meta)).append(" ");
						name(runtime, builder, meta);
					}
					builder.append("\n");
					if(!first) {
						builder.append(",");
					}
					first = false;
					builder.append(line);
				}
			}
		}
		if(null != merge){
			//合并的最后执行(pg rename不支持合并 先安排)
			runs.add(merge);
		}
		return runs;
	}

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, Table meta) throws Exception {
		boolean result = true;
		List<Run> runs = new ArrayList<>();
		Table update = (Table)meta.getUpdate();
		//检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
		checkPrimary(runtime, update);
		String name = meta.getName();
		String uname = update.getName();
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, ACTION.DDL.TABLE_ALTER, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		checkSchema(runtime, update);

		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)) {
			//先修改表名，后续在新表名基础上执行
			result = rename(runtime, meta, uname);
			meta.setName(uname);
		}
		if(!result) {
			return result;
		}
		//修改表备注
		String ucomment = update.getComment();
		String comment = meta.getComment();
		if(BasicUtil.isEmpty(ucomment) && BasicUtil.isEmpty(comment)){
			//都为空时不更新
		}else {
			if (!BasicUtil.equals(comment, ucomment)) {
				swt = InterceptorProxy.prepare(runtime, random, ACTION.DDL.TABLE_COMMENT, meta);
				if (swt == ACTION.SWITCH.BREAK) {
					return false;
				}
				if (BasicUtil.isNotEmpty(meta.getComment())) {
					runs.addAll(buildChangeCommentRun(runtime, update));
				} else {
					runs.addAll(buildAddCommentRun(runtime, update));
				}
				result = execute(runtime, random, meta, ACTION.DDL.TABLE_COMMENT, runs) && result;
				if (meta.swt() == ACTION.SWITCH.BREAK) {
					return result;
				}
			}
		}

		boolean slice  = slice();
		List<Run> slices = new ArrayList<>();
		//List<Run> merges = new ArrayList<>();

		LinkedHashMap<String, Column> cols = checkColumnAction(runtime, meta);
		//主键
		PrimaryKey src_primary = primary(runtime, random, false, meta);
		PrimaryKey cur_primary = update.getPrimaryKey();
		boolean change_pk = !meta.primaryEquals(update);
		if(change_pk) {
			meta.setChangePrimary(1);
		}
		//主动标记删除状态
		if(null != cur_primary && cur_primary.isDrop()){
			cur_primary.execute(meta.execute());
			if(slice){
				slices.addAll(buildDropRun(runtime, cur_primary, slice));
			}else {
				drop(runtime, cur_primary);
			}
			cur_primary = null;
			change_pk = false;
		}
		//如果主键有更新 先删除主键 避免alters中把原主键列的非空取消时与主键约束冲突
		try {
			List<Run> autos = checkAutoIncrement(runtime, null, meta, slice);
			if(slice) {
				slices.addAll(autos);
			}else{
				result = execute(runtime, random, meta, ACTION.DDL.TABLE_ALTER, autos) && result;
				if(meta.swt() == ACTION.SWITCH.BREAK) {
					return result;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		if(change_pk && null != src_primary) {
			src_primary.execute(meta.execute());
			if(slice){
				slices.addAll(buildDropRun(runtime, src_primary, slice));
			}else {
				drop(runtime, src_primary);
			}
			src_primary = null;
		}

		//更新列
		List<Run> alters = buildAlterRun(runtime, meta, cols.values(), slice);
		if(slice){
			slices.addAll(alters);
		}else{
			if(null != alters && alters.size()>0) {
				result = execute(runtime, random, meta, ACTION.DDL.COLUMN_ALTER, alters) && result;
				if(meta.swt() == ACTION.SWITCH.BREAK) {
					return result;
				}
			}
		}

		//在alters执行完成后 添加主键 避免主键中存在alerts新添加的列
		if(null != cur_primary) {//复合主键的单独添加
			if(change_pk){
				if(slice){
					slices.addAll(buildAddRun(runtime, cur_primary, slice));
				}else {
					add(runtime, cur_primary);
				}
			}
		}
		List<Run> merges = merge(runtime, meta, slices);
		result = execute(runtime, random, meta, ACTION.DDL.TABLE_ALTER, merges) && result;
		if(meta.swt() == ACTION.SWITCH.BREAK) {
			return result;
		}
		/*
		修改索引
		在索引上标记删除的才删除,没有明确标记删除的不删除(因为许多情况会生成索引，比如唯一约束也会生成个索引，但并不在uindexes中)
		*/
		LinkedHashMap<String, Index> oindexes = indexes(runtime, random, meta, null);		//原索引
		LinkedHashMap<String, Index> indexes = update.getIndexes();		//新索引
		for(Index index:indexes.values()) {
			if(index.isPrimary()) {
				continue;
			}
			index.execute(meta.execute());
			if(index.isDrop()) {
				//项目中调用drop()明确要删除的
				drop(runtime, index);
			}else{
				if(null != index.getUpdate()) {
					//改名或设置过update的
					alter(runtime, index);
				}else {
					Index oindex = oindexes.get(index.getName().toUpperCase());
					if (null == oindex) {
						//名称不存在的
						add(runtime, index);
					}else{
						if(!index.equals(oindex)) {
							if(oindex.isPrimary()){
								continue;
							}
							oindex.execute(meta.execute());
							oindex.setUpdate(index, false, false);
							alter(runtime, oindex);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, Table meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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

	public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.TABLE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
	 * @param meta 表
	 * @return String
	 */
	public String keyword(Metadata meta) {
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
	 * @return runs
	 * @throws Exception
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成]<br/>
	 * 修改表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception {
		if(null != meta) {
			return buildAlterRun(runtime, meta, meta.columns());
		}
		return new ArrayList<>();
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
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		for(Column column:columns) {
			ACTION.DDL action = column.getAction();
			CMD cmd = null;
			if(null != action) {
				cmd = action.getCmd();
			}
			if(CMD.IGNORE == cmd) {
				continue;
			}
			if(CMD.CREATE == cmd) {
				runs.addAll(buildAddRun(runtime, column, slice));
			}else if(CMD.ALTER == cmd) {
				runs.addAll(buildAlterRun(runtime, column, slice));
			}else if(CMD.DROP == cmd) {
				runs.addAll(buildDropRun(runtime, column, slice));
			}
		}
		return runs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 */
	@Override
	public void checkPrimary(DataRuntime runtime, Table table) {
		if(null != table) {
			table.checkColumnPrimary();
		}
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 columns部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table table) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder indexes(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
	public boolean create(DataRuntime runtime, View meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, View meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, View meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, View origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
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
	public boolean create(DataRuntime runtime, MasterTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, MasterTable meta) throws Exception {
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		String random = random(runtime);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		boolean result = true;
		checkSchema(runtime, meta);
		Table update = meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		LinkedHashMap<String, Tag> tags = meta.getTags();
		LinkedHashMap<String, Tag> utags = update.getTags();
		String name = meta.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();

		if(!name.equalsIgnoreCase(uname)) {
			result = rename(runtime, meta, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()) {
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column) {
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(runtime, column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(runtime, ucolumn);
				result = true;
			}
		}
		// 删除列
		if(meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag) {
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(runtime, column);
					result = true;
				}
			}
		}
		// 更新标签
		for(Tag utag : utags.values()) {
			Tag tag = tags.get(utag.getName().toUpperCase());
			if(null != tag) {
				// 修改列
				tag.setTable(update);
				tag.setUpdate(utag, false, false);
				alter(runtime, tag);
				result = true;
			}else{
				// 添加列
				utag.setTable(update);
				add(runtime, utag);
				result = true;
			}
		}
		// 删除标签
		if(meta.isAutoDropColumn()) {
			for (Tag tag : tags.values()) {
				Tag utag = utags.get(tag.getName().toUpperCase());
				if (null == utag) {
					tag.setTable(update);
					drop(runtime, tag);
					result = true;
				}
			}
		}
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[alter master table][table:{}][result:{}][执行耗时:{}]", random, meta.getName(), result, DateUtil.format(System.currentTimeMillis() - fr));
		}
		return result;
	}

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, MasterTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);

		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean create(DataRuntime runtime, PartitionTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception {
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		String random = random(runtime);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		boolean result = true;

		checkSchema(runtime, meta);
		Table update = meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = meta.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)) {
			result = rename(runtime, meta, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()) {
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column) {
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(runtime, column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(runtime, ucolumn);
				result = true;
			}
		}
		// 删除列
		if(meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag) {
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(runtime, column);
					result = true;
				}
			}
		}
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[alter partition table][table:{}][result:{}][执行耗时:{}]", random, meta.getName(), result, DateUtil.format(System.currentTimeMillis() - fr));
		}
		return result;
	}

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */

	public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return  execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_RENAME;
		origin.setNewName(name);
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, Column meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.COLUMN_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
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
	public boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta, false);

		try{
			result = execute(runtime, random, meta, action, runs);
			if(meta.swt() == ACTION.SWITCH.BREAK) {
				return result;
			}
		}catch (Exception e) {
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改Column执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				//DDL修改列异常后 -1:抛出异常 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION == -1) {
					throw e;
				}else if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改Column执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == ACTION.SWITCH.CONTINUE) {
					result = alter(runtime, table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}
		return result;
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, Column meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			LinkedHashMap<String, Table> tables = tables(runtime, null, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(runtime, table, meta, true);
	}

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, Column meta) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, Column origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.COLUMN_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin, false);
		return  execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	@Override
	public String alterColumnKeyword(DataRuntime runtime) {
		return "";
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
	public StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
	public StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
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
		String define = meta.getDefinition();
		if(BasicUtil.isNotEmpty(define)) {
			builder.append(" ").append(define);
			return builder;
		}
		// 数据类型
		type(runtime, builder, meta);
		//聚合
		aggregation(runtime, builder, meta);
		// 编码
		charset(runtime, builder, meta);
		// 默认值
		defaultValue(runtime, builder, meta);
		// 非空
		nullable(runtime, builder, meta, action);
		//主键
		primary(runtime, builder, meta);
		//唯一索引
		unique(runtime, builder, meta);

		// 递增(注意有些数据库不需要是主键)
		increment(runtime, builder, meta);
		// 更新行事件
		onupdate(runtime, builder, meta);
		// 备注
		comment(runtime, builder, meta);
		// 位置
		position(runtime, builder, meta);

		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(null == builder) {
			builder = new StringBuilder();
		}
		String finalType = meta.getFinalType();
		if(BasicUtil.isNotEmpty(finalType)) {
			builder.append(finalType);
			return builder;
		}
		meta.ignoreLength(ignoreLength);
		meta.ignorePrecision(ignorePrecision);
		meta.ignoreScale(ignoreScale);
		meta.parseType(2);
		builder.append(meta.getFullType(type()));
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有长度<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type 数据类型
	 * @return boolean
	 */
	@Override
	public int ignoreLength(DataRuntime runtime, TypeMetadata type) {
		return MetadataAdapterHolder.ignoreLength(type(), type);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
	@Override
	public int ignorePrecision(DataRuntime runtime, TypeMetadata type) {
		return MetadataAdapterHolder.ignorePrecision(type(), type);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:定义列:是否忽略小数位<br/>
	 * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
    @Override
    public int ignoreScale(DataRuntime runtime, TypeMetadata type) {
		return MetadataAdapterHolder.ignoreScale(type(), type);
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
		if(meta.isPrimaryKey() == 1) {
			builder.append(" NOT NULL");
			return builder;
		}
		if(null == meta.getDefaultValue()) {
			int nullable = meta.isNullable();
			if(nullable != -1) {
				if (nullable == 0) {
					builder.append(" NOT");
				}
				builder.append(" NULL");
			}
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		Object def = null;
		boolean defaultCurrentDateTime = false;
		Column update = meta.getUpdate();
		if(null != update) {
			//自增序列不要默认值nextval('crm_user_id_seq'::regclass)
			if(update.isAutoIncrement() == 1) {
				return builder;
			}
			def = update.getDefaultValue();
			defaultCurrentDateTime = update.isDefaultCurrentDateTime();
		}else {
			if(meta.isAutoIncrement() == 1) {
				return builder;
			}
			def = meta.getDefaultValue();
			defaultCurrentDateTime = meta.isDefaultCurrentDateTime();
		}
		if(null == def && defaultCurrentDateTime) {
			String type = meta.getFullType(type()).toLowerCase();
			if (type.contains("timestamp")) {
				def = SQL_BUILD_IN_VALUE.CURRENT_TIMESTAMP;
			}else{
				def = SQL_BUILD_IN_VALUE.CURRENT_DATETIME;
			}
		}
		if(null != def) {
			String str = def.toString().trim();
			builder.append(" DEFAULT ");
			//boolean isCharColumn = isCharColumn(runtime, column);
			SQL_BUILD_IN_VALUE val = checkDefaultBuildInValue(runtime, def);
			if(null != val) {
				def = val;
			}
			if(def instanceof SQL_BUILD_IN_VALUE) {
				String value = value(runtime, meta, (SQL_BUILD_IN_VALUE)def);
				if(null != value) {
					builder.append(value);
				}else{
					throw new RuntimeException("当前adapter没有解析"+def+",可以用${原生SQL}代替,如column.setDefaultValue(\"${now()}\");");
				}
			}else if(str.startsWith("${") && str.endsWith("}")) {
				builder.append(str.substring(2, str.length()-1));
			}else {
				//nextval('crm_user_id_seq'::regclass)
				//DEFAULT NULL::timestamp with time zone,
				if(null != def && def.toString().contains("::")) {
					def = def.toString().split("::")[0];
				}
				def = write(runtime, meta, def, false);
				if(null == def) {
					def = meta.getDefaultValue();
				}
				//format(builder, def);
				builder.append(def);
			}
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder unique(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
	 * List<Run> buildAddRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildDropRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag)
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
	public boolean add(DataRuntime runtime, Tag meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TAG_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);

		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
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
	public boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TAG_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta, false);
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}catch (Exception e) {
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改TAG执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改TAG执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == ACTION.SWITCH.CONTINUE) {
					result = alter(runtime, table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}
		return result;
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Tag meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), 1);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else {
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta, true);
	}

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Tag meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TAG_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.TAG_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin, slice());
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * tag[命令合成]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Tag meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Tag meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
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
	public boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables( runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改Table最后修改主键,注意不要与列上的主键标识重复,如果列上支持标识主键，这里不需要实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, origin, meta);
		return execute(runtime, random, table, action, runs);
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta, boolean slice)", 37));
		}
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
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta, boolean slice)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
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
	 * foreign[调用入口]
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean add(DataRuntime runtime, ForeignKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.FOREIGN_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * foreign[调用入口]
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.FOREIGN_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * foreign[调用入口]
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.FOREIGN_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return List
	 */

	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) ", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) ", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, Index meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_ADD;
		String random = random(runtime);
		Index update = (Index)meta.getUpdate();
		if(null != update){
			//如果是修改 调用过来的  添加update
			meta = update;
		}
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Index meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Index meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, Index origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		List<Run> runs = new ArrayList<>();
		if(null != meta) {
			LinkedHashMap<String, Index> indexes = meta.getIndexes();
			if(null != indexes) {
				for(Index index:indexes.values()) {
					if(index.isPrimary()) {
						continue;
					}
					runs.addAll(buildAddRun(runtime, index));
				}
			}
		}
		return runs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta)", 37));
		}
		return builder;
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder property(DataRuntime runtime, StringBuilder builder, Index meta) {
		return builder;
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta)", 37));
		}
		return builder;
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
	public StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
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
	public boolean add(DataRuntime runtime, Constraint meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Constraint meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Constraint meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_RENAME;
		String random = random(runtime);
		origin.setNewName(name);

		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, Trigger meta) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Trigger meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Trigger meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta)", 37));
		}
		return builder;
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
	public boolean create(DataRuntime runtime, Procedure meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Procedure meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PROCEDURE_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Procedure meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PROCEDURE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
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
	public boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.PROCEDURE_RENAME;
		origin.setNewName(name);
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	public StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter)", 37));
		}
		return builder;
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
	 * function[调用入口]
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean create(DataRuntime runtime, Function meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.FUNCTION_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * function[调用入口]
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Function meta) throws Exception {
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.FUNCTION_ALTER;
		ACTION.SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * function[调用入口]
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Function meta) throws Exception {
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.FUNCTION_DROP;
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);

		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * function[调用入口]
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean rename(DataRuntime runtime, Function origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.FUNCTION_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);

		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * function[命令合成]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
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
	 * sequence[调用入口]
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean create(DataRuntime runtime, Sequence meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.SEQUENCE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * sequence[调用入口]
	 * 修改序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Sequence meta) throws Exception {
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.SEQUENCE_ALTER;
		ACTION.SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * sequence[调用入口]
	 * 删除序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Sequence meta) throws Exception {
		boolean result = false;
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.SEQUENCE_DROP;
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);

		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * sequence[调用入口]
	 * 重命名序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 序列
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean rename(DataRuntime runtime, Sequence origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.SEQUENCE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);

		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[命令合成]<br/>
	 * 删除序列
	 * @param meta 序列
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(DataRuntime runtime, Column column)
	 *  boolean isNumberColumn(DataRuntime runtime, Column column)
	 * boolean isCharColumn(DataRuntime runtime, Column column)
	 * String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value)
	 * String type(String type)
	 * String type2class(String type)
	 *
	 * protected String string(List<String> keys, String key, ResultSet set, String def) throws Exception
	 * protected String string(List<String> keys, String key, ResultSet set) throws Exception
	 * protected Integer integer(List<String> keys, String key, ResultSet set, Integer def) throws Exception
	 * protected Boolean bool(List<String> keys, String key, ResultSet set, Boolean def) throws Exception
	 * protected Boolean bool(List<String> keys, String key, ResultSet set, int def) throws Exception
	 * protected Object value(List<String> keys, String key, ResultSet set, Object def) throws Exception
	 * protected Object value(List<String> keys, String key, ResultSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 检测针对表的主键生成器
	 * @param type 数据库类型
	 * @param table 表
	 * @return PrimaryGenerator
	 */
	protected PrimaryGenerator checkPrimaryGenerator(DatabaseType type, String table) {
		table = table.replace(getDelimiterFr(), "").replace(getDelimiterTo(), "");
		//针对当前表的生成器
		PrimaryGenerator generator = GeneratorConfig.get(table);
		if(null != generator) {
			if(generator != PrimaryGenerator.GENERATOR.DISABLE && generator != PrimaryGenerator.GENERATOR.AUTO) {
				return generator;
			}
		}
		//全局配置
		if(null == primaryGenerator) {
			if(null == primaryGenerator) {
				primaryGenerator = GeneratorConfig.get();
			}
			if(null == primaryGenerator) {
				//全局配置
				if (ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.SNOWFLAKE;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.RANDOM;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.UUID;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.TIME;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.TIMESTAMP;
				}
			}
		}
		if(null != primaryGenerator) {
			return primaryGenerator;
		}else{
			return null;
		}
	}

	/**
	 * 转换成相应数据库类型<br/>
	 * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型<br/>
	 * 同时解析长度、有效位数、精度<br/>
	 * 如有些数据库中用bigint有些数据库中有long
	 * @param meta 列
	 * @return 具体数据库中对应的数据类型
	 */
	@Override
	public TypeMetadata typeMetadata(DataRuntime runtime, Column meta) {
		TypeMetadata typeMetadata = meta.getTypeMetadata();
		if(null == typeMetadata || TypeMetadata.NONE == typeMetadata || meta.getParseLvl() < 2 || type() != meta.getDatabase()) {
			typeMetadata = TypeMetadata.parse(type(), meta, alias, spells);
			meta.setDatabase(type());
			meta.setParseLvl(2);
		}
		return typeMetadata;
	}

	/**
	 * 数据类型拼写兼容
	 * @param name name
	 * @return spell
	 */
	public TypeMetadata spell(String name) {
		TypeMetadata typeMetadata = alias.get(name.toUpperCase());
		if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {//拼写兼容  下划线空格兼容
			typeMetadata = alias.get(spells.get(name.toUpperCase()));
		}
		return typeMetadata;
	}
	/**
	 * 转换成相应数据库类型<br/>
	 * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型，如有些数据库中用bigint有些数据库中有long
	 * @param type 编码时输入的类型
	 * @return 具体数据库中对应的数据类型
	 */
	@Override
	public TypeMetadata typeMetadata(DataRuntime runtime, String type) {
		if(null == type) {
			return null;
		}
		Column tmp = new Column();
		tmp.setTypeName(type, false);
		return typeMetadata(runtime, tmp);
	}

	/**
	 * 合成完整名称
	 * @param meta 合成完整名称
	 * @return String
	 */
	public String name(Metadata meta) {
		StringBuilder builder = new StringBuilder();
		String catalog = meta.getCatalogName();
		String schema = meta.getSchemaName();
		String name = meta.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			builder.append(catalog).append(".");
		}
		if(!empty(schema)) {
			builder.append(schema).append(".");
		}
		builder.append(name);
		return builder.toString();
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param meta Metadata
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, Metadata meta) {
		checkName(runtime, null, meta);
		String catalog = meta.getCatalogName();
		String schema = meta.getSchemaName();
		String name = meta.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog).append(".");
		}
		if(!empty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, name);
		return builder;
	}
	/**
	 * 拼接完整列名
	 * @param builder builder
	 * @param meta Column
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(null != meta) {
			delimiter(builder, meta.getName());
		}
		return builder;
	}

	/**
	 * 拼接界定符
	 * @param builder StringBuilder
	 * @param src 原文
	 * @return StringBuilder
	 */
	public StringBuilder delimiter(StringBuilder builder, String src) {
		return SQLUtil.delimiter(builder, src, getDelimiterFr(), getDelimiterTo());
	}
	/**
	 * 拼接界定符
	 * @param builder StringBuilder
	 * @param list 原文
	 * @return StringBuilder
	 */
	public StringBuilder delimiter(StringBuilder builder, List<String> list) {
		String fr = getDelimiterFr();
		String to = getDelimiterTo();
		boolean first = true;
		for(String item:list) {
			if(!first) {
				builder.append(", ");
			}
			first = false;
			SQLUtil.delimiter(builder, item, fr, to);
		}
		return builder;
	}

	/**
	 * 是否是boolean列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isBooleanColumn(DataRuntime runtime, Column column) {
		String clazz = column.getClassName();
		if(null != clazz) {
			clazz = clazz.toLowerCase();
			if(clazz.contains("boolean")) {
				return true;
			}
		}else{
			// 如果没有同步法数据库,直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type) {
				type = type.toLowerCase();
				if(type.equals("bit") || type.equals("bool")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isNumberColumn(DataRuntime runtime, Column column) {
		String clazz = column.getClassName();
		if(null != clazz) {
			clazz = clazz.toLowerCase();
			if(
					clazz.startsWith("int")
							|| clazz.contains("integer")
							|| clazz.contains("long")
							|| clazz.contains("decimal")
							|| clazz.contains("float")
							|| clazz.contains("double")
							|| clazz.contains("timestamp")
							// || clazz.contains("bit")
							|| clazz.contains("short")
			) {
				return true;
			}
		}else{
			// 如果没有同步法数据库,直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type) {
				type = type.toLowerCase();
				if(type.startsWith("int")
						||type.contains("float")
						||type.contains("double")
						||type.contains("short")
						||type.contains("long")
						||type.contains("decimal")
						||type.contains("numeric")
						||type.contains("timestamp")
				) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isCharColumn(DataRuntime runtime, Column column) {
		return !isNumberColumn(runtime, column) && !isBooleanColumn(runtime, column);
	}

	/**
	 * 内置函数
	 * @param column 列属性
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
		return null;
	}


	/**
	 * 写入数据库前类型转换<br/>
	 *
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	@Override
	public Object write(DataRuntime runtime, Column metadata, Object value, boolean placeholder) {
		if(null == value || "NULL".equals(value)) {
			return null;
		}
		Object result = null;
		TypeMetadata columnType = null;
		DataWriter writer = null;
		boolean isArray = false;
		//根据元数据类型
		if(null != metadata) {
			isArray = metadata.isArray();
			//根据列类型
			columnType = metadata.getTypeMetadata();
			if(null != columnType) {
				writer = writer(columnType);
			}
			if(null == writer) {
				//根据列类型名称
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					writer = writer(typeName);
					if(null != columnType) {
						//类型名称 转 成标准类型
						writer = writer(typeMetadata(runtime, metadata));
					}
				}
			}
		}
		if(null == columnType || TypeMetadata.NONE == columnType) {
			//根据值的Java class
			columnType = typeMetadata(runtime, value.getClass().getSimpleName());
		}
		if(null != columnType && TypeMetadata.NONE != columnType) {
			Class writeClass = columnType.compatible();
			if(null != writeClass) {
				value = ConvertProxy.convert(value, writeClass, isArray);
			}
		}

		if(null != columnType && TypeMetadata.NONE != columnType) {//根据列类型定位writer
			writer = writer(columnType);
			if(null == writer) {
				writer = writer(columnType.getCategory());
			}
		}
		if(null == writer && null != value) {//根据值类型定位writer
			writer = writer(value.getClass());
		}
		if(null != writer) {
			result = writer.write(value, placeholder, columnType);
		}
		if(null != result) {
			return result;
		}
		if(null != columnType && TypeMetadata.NONE != columnType) {
			result = columnType.write(value, null, false);
		}
		if(null != result) {
			return result;
		}
		//根据值类型
		if(!placeholder) {
			if (null == value || BasicUtil.isNumber(value) || "NULL".equals(value)) {
				result = value;
			} else {
				result = "'" + value + "'";
			}
		}

		return result;
	}

	/**
	 * 从数据库中读取数据<br/>
	 * 先由子类根据metadata.typeName(CHAR,INT)定位到具体的数据库类型ColumnType<br/>
	 * 如果定位成功由CoumnType根据class转换(class可不提供)<br/>
	 * 如果没有定位到ColumnType再根据className(String,BigDecimal)定位到JavaType<br/>
	 * 如果定位失败或转换失败(返回null)再由父类转换<br/>
	 * 如果没有提供metadata和class则根据value.class<br/>
	 * 常用类型jdbc可以自动转换直接返回就可以(一般子类DataType返回null父类原样返回)<br/>
	 * 不常用的如json/point/polygon/blob等转换成anyline对应的类型<br/>
	 *
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时应该指定属性class, DataRow赋值时可以通过JDBChandler指定class)
	 * @return Object
	 */
	@Override
	public Object read(DataRuntime runtime, Column metadata, Object value, Class clazz) {
		Object result = value;
		if(null == value) {
			return null;
		}
		DataReader reader = null;
		TypeMetadata ctype = null;
		if (null != metadata) {
			ctype = metadata.getTypeMetadata();
			if(null != ctype) {
				reader = reader(ctype);
			}
			if(null == reader) {
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					reader = reader(typeName);
					if(null == reader) {
						reader = reader(typeMetadata(runtime, metadata));
					}
				}
			}
		}
		if(null == reader) {
			reader = reader(value.getClass());
		}
		if(null != reader) {
			result = reader.read(value);
		}
		if(null == reader || null == result) {
			if(null != ctype) {
				result = ctype.read(value, null, clazz);
			}
		}
		return result;
	}
	/**
	 * 在不检测数据库结构时才生效,否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param obj Object
	 * @param key 列名
	 */
	@Override
	public void value(DataRuntime runtime, StringBuilder builder, Object obj, String key) {
		Object value = null;
		if(obj instanceof DataRow) {
			value = ((DataRow)obj).get(key);
		}else {
			if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
				Field field = EntityAdapterProxy.field(obj.getClass(), key);
				value = BeanUtil.getFieldValue(obj, field);
			} else {
				value = BeanUtil.getFieldValue(obj, key);
			}
		}
		if(null != value) {
			if(value instanceof SQL_BUILD_IN_VALUE) {
				builder.append(value(runtime, null, (SQL_BUILD_IN_VALUE)value));
			}else {
				TypeMetadata type = typeMetadata(runtime, value.getClass().getName());
				if (null != type) {
					value = type.write(value, null, false);
				}
				builder.append(value);

			}
		}else{
			builder.append("null");
		}
	}
	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 表
	 * @param value  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue value) {
		boolean result = false;
		if(ConfigTable.IS_AUTO_CHECK_METADATA) {
			LinkedHashMap<String, Column> columns = columns(runtime,null, false, new Table(catalog, schema, table), false);
			result = convert(runtime, columns, value);
		}else{
			result = convert(runtime,(Column)null, value);
		}
		return result;
	}

	/**
	 * 设置参数值,主要根据数据类型格执行式化，如对象,list,map等插入json列
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param column 列
	 * @param value value
	 */
	@Override
	public void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value) {
		boolean split = ConfigTable.IS_AUTO_SPLIT_ARRAY;
		if(ConfigTable.IS_AUTO_CHECK_METADATA) {
			String type = null;
			if(null != column) {
				type = column.getTypeName();
				if(null == type && BasicUtil.isNotEmpty(run.getTable())) {
					LinkedHashMap<String,Column> columns = columns(runtime,null, false, run.getTable(), false);
					column = columns.get(column.getName().toUpperCase());
					if(null != column) {
						type = column.getTypeName();
					}
				}
			}
		}
		run.addValues(compare, column, value, split);
		if(null != column) {
			//value = convert(runtime, column, rv); //统一调用
		}
	}

	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param configs ConfigStore
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, ConfigStore configs, Run run) {
		boolean result = false;
		if(null != run) {
			result = convert(runtime, run.getTable(), run);
		}
		return result;
	}

	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table Table
	 * @param run  run
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, Table table, Run run) {
		boolean result = false;
		if(null != table) {
			LinkedHashMap<String, Column> columns = table.getColumns();

			if (ConfigTable.IS_AUTO_CHECK_METADATA) {
				//if (null == columns || columns.isEmpty()) {
				//有可能是通过class解析解析的columns以数据库为准
					columns = columns(runtime, null, false, table, false);
				//}
			}
			List<RunValue> values = run.getRunValues();
			if (null != values) {
				for (RunValue value : values) {
					if (ConfigTable.IS_AUTO_CHECK_METADATA) {
						result = convert(runtime, columns, value);
					} else {
						result = convert(runtime, (Column) null, value);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 数据类型转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param columns 列
	 * @param value 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, Map<String,Column> columns, RunValue value) {
		boolean result = false;
		if(null != columns && null != value) {
			String key = value.getKey();
			if(null != key) {
				Column meta = columns.get(key.toUpperCase());
				result = convert(runtime, meta, value);
			}
		}
		return result;
	}

	/**
	 * 根据数据库列属性 类型转换(一般是在更新数据库时调用)
	 * 子类先解析(有些同名的类型以子类为准)、失败后再到这里解析
	 * @param metadata 列
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)Value
	 * @return boolean 是否完成类型转换,决定下一步是否继续
	 */
	@Override
	public boolean convert(DataRuntime runtime, Column metadata, RunValue run) {
		if(null == run) {
			return true;
		}
		Object value = run.getValue();
		if(null == value) {
			return true;
		}
		try {
			if(null != metadata) {
				//根据列属性转换(最终也是根据java类型转换)
				value = convert(runtime, metadata, value);
			}else{
				DataWriter writer = writer(value.getClass());
				if(null != writer) {
					value = writer.write(value,true, metadata.getTypeMetadata());
				}
			}
			run.setValue(value);

		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata 列
	 * @param value 值
	 * @return Object
	 */
	@Override
	public Object convert(DataRuntime runtime, Column metadata, Object value) {
		if(null == value) {
			return value;
		}
		try {
			if(null != metadata) {
				TypeMetadata columnType = metadata.getTypeMetadata();
				if(null == columnType) {
					columnType = typeMetadata(runtime, metadata);
					if(null != columnType) {
						columnType.setArray(metadata.isArray());
						metadata.setTypeMetadata(columnType);
					}
				}
				value = convert(runtime, columnType, value);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 数据类型
	 * @param value 值
	 * @return Object
	 */
	@Override
	public Object convert(DataRuntime runtime, TypeMetadata meta, Object value) {
		if(null == meta) {
			return value;
		}
		String typeName = meta.getName();

		boolean parseJson = false;
		if(null != typeName && !(value instanceof String)) {
			if(typeName.contains("JSON")) {
				//对象转换成json string
				value = BeanUtil.object2json(value);
				parseJson = true;
			}else if(typeName.contains("XML")) {
				value = BeanUtil.object2xml(value);
				parseJson = true;
			}
		}
		if(!parseJson) {
			if (null != meta) {
				DataWriter writer = writer(meta);
				if(null == writer) {
					writer = writer(meta.getCategory());
				}
				if(null != writer) {
					value = writer.write(value, true, meta);
				}else {
					Class transfer = meta.transfer();
					Class compatible = meta.compatible();
					if (null != transfer) {
						value = ConvertProxy.convert(value, transfer, meta.isArray());
					}
					if (null != compatible) {
						value = ConvertProxy.convert(value, compatible, meta.isArray());
					}
				}
			}
		}
		return value;
	}

	/**
	 * 对象名称格式化(大小写转换)，在查询系统表时需要
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name name
	 * @return String
	 */
	@Override
	public String objectName(DataRuntime runtime, String name) {
		KeyAdapter.KEY_CASE keyCase = type().nameCase();
		if(null != keyCase) {
			return keyCase.convert(name);
		}
		return name;
	}

	/**
	 *
	 * 根据 catalog, schema, name检测tables集合中是否存在
	 * @param list metas
	 * @param catalog catalog
	 * @param schema schema
	 * @param name name
	 * @return 如果存在则返回Table 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Metadata> T search(List<T> list, Catalog catalog, Schema schema, String name) {
		if(null != list) {
			for(T meta:list) {
				if(equals(catalog, meta.getCatalog())
						&& equals(schema, meta.getSchema())
						&& BasicUtil.equalsIgnoreCase(meta.getName(),name)
				) {
					return meta;
				}
			}
		}
		return Metadata.match(list, catalog, schema, name);
	}

	public <T extends Metadata> T search(List<T> list, String catalog, String schema, String name) {
		return Metadata.match(list, catalog, schema, name);
	}

	public <T extends Metadata> T search(List<T> list, String catalog, String name) {
		return Metadata.match(list, catalog, name);
	}

	public <T extends Metadata> T search(List<T> list, String name) {
		return Metadata.match(list, name);
	}

	//A.ID,A.COOE,A.NAME
	protected String concat(String prefix, String split, List<String> columns) {
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(prefix)) {
			prefix = "";
		}else{
			if(!prefix.endsWith(".")) {
				prefix += ".";
			}
		}

		boolean first = true;
		for(String column:columns) {
			if(!first) {
				builder.append(split);
			}
			first = false;
			builder.append(prefix);
			//.append(column);
			delimiter(builder, column);
		}
		return builder.toString();
	}
	//master.column = data.column
	protected String concatEqual(String master, String data, String split, List<String> columns) {
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(master)) {
			master = "";
		}else{
			if(!master.endsWith(".")) {
				master += ".";
			}
		}
		if(BasicUtil.isEmpty(data)) {
			data = "";
		}else{
			if(!data.endsWith(".")) {
				data += ".";
			}
		}

		boolean first = true;
		for(String column:columns) {
			if(!first) {
				builder.append(split);
			}
			first = false;
			builder.append(master).append(column).append(" = ").append(data).append(column);
		}
		return builder.toString();
	}


	protected String random(DataRuntime runtime) {
		StringBuilder builder = new StringBuilder();
		builder.append("[cmd:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
			.append("][thread:")
			.append(Thread.currentThread().getId()).append("][ds:").append(runtime.datasource()).append("]");
		return builder.toString();
	}

}