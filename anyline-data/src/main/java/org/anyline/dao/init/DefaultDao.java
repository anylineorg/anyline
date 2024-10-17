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

package org.anyline.dao.init;

import org.anyline.adapter.PersistenceAdapter;
import org.anyline.annotation.Component;
import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.MetadataDiffer;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.persistence.ManyToMany;
import org.anyline.metadata.persistence.OneToMany;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

@Component("anyline.dao")
public class DefaultDao<E> implements AnylineDao<E> {
	protected static final Log log = LogProxy.get(DefaultDao.class);

	//默认环境, 如果没有值则根据当前线程动态获取
	//用于ServiceProxy中生成多个service/dao/jdbc
	protected DataRuntime runtime = null;

	public DataRuntime runtime() {
		if(null == runtime) {
			runtime = RuntimeHolder.runtime();
		}
		return runtime;
	}
	public DriverAdapter adapter() {
		return runtime().getAdapter();
	}

	public void setRuntime(DataRuntime runtime) {
		this.runtime = runtime;
	}

	/**
	 * 是否固定数据源
	 * @return boolean
	 */
	public boolean fix() {
		return true;
	}

	/**
	 * 根据差异生成SQL
	 * @param differ differ
	 * @return sql
	 */
	@Override
	public List<Run> ddl(DataRuntime runtime, MetadataDiffer differ) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, null, differ);
	}
	/**
	 * 根据差异生成SQL
	 * @param differs differs
	 * @return sql
	 */
	@Override
	public List<Run> ddl(DataRuntime runtime, List<MetadataDiffer> differs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, null, differs);
	}

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	/**
	 * 查询map列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return mpas
	 */
	@Override
	public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().maps(runtime, random, prepare, configs, conditions);
	}

	/**
	 * 查询<br/>
	 * 注意:如果设置了自动还原, querys会自动还原数据源(dao内部执行过程中不要调用除非是一些重载), 而select不会
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().querys(runtime, null, prepare, configs, conditions);

	}

	/**
	 * 查询<br/>
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return DataSet
	 */
	@Override
	public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(prepare != null && !prepare.disposable()){
			prepare = prepare.clone();
		}
		EntitySet set = runtime.getAdapter().selects(runtime, null, prepare, clazz, configs, conditions);
		int dependency = ConfigTable.ENTITY_FIELD_SELECT_DEPENDENCY;
		if(dependency > 0) {
			checkMany2ManyDependencyQuery(runtime, random, set, dependency);
			checkOne2ManyDependencyQuery(runtime, random, set, dependency);
		}
		return set;
	}

	/**
	 * 查询序列值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 可以是多个序列
	 * @return DataRow
	 */
	public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().sequence(runtime, null, next, names);

	}

	/**
	 * 统计总行数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().count(runtime, null, prepare, configs, conditions);

	}

	/**
	 * 检测是否存在
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return boolean
	 */
	@Override
	public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().exists(runtime, random, prepare, configs, conditions);

	}

	/**
	 * 更新
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data		需要更新的数据
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		if(null == runtime) {
			runtime = runtime();
		}
		long result = runtime.getAdapter().update(runtime, random, batch, dest, data, configs, columns);
		if(result > 0) {
			checkMany2ManyDependencySave(runtime, random, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
			checkOne2ManyDependencySave(runtime, random, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
		}
		return result;
	}

	@Override
	public long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}


	/**
	 * 多表关联更新
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 一般通过TableBuilder生成
	 * @param data K-VariableValue 更新值key:需要更新的列 value:通常是关联表的列用VariableValue表示，也可以是常量
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, RunPrepare prepare, DataRow data, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().update(runtime, random, prepare, data, configs, conditions);
	}
	/**
	 * 检测级联insert/update
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param obj obj
	 * @param dependency dependency
	 * @param mode 0:inser 1:update
	 * @return int
	 */
	private int checkMany2ManyDependencySave(DataRuntime runtime, String random, Object obj, int dependency, int mode) {
		int result = 0;
		//ManyToMany
		if(dependency <= 0) {
			return result;
		}
		if(obj instanceof DataSet || obj instanceof DataRow || obj instanceof Map) {
			return result;
		}
		if(obj instanceof EntitySet) {
			EntitySet set = (EntitySet) obj;
			for(Object entity:set) {
				checkMany2ManyDependencySave(runtime, random, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			Column pc = EntityAdapterProxy.primaryKey(clazz);
			String pk = null;
			if(null != pc) {
				pk = pc.getName();
			}
			List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
			for(Field field:fields) {
				try {
					ManyToMany join = PersistenceAdapter.manyToMany(field);
					//INSERT INTO HR_DEPLOYEE_DEPARTMENT(EMPLOYEE_ID, DEPARTMENT_ID) VALUES();
					Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(obj);
					Object pv = primaryValueMap.get(pk.toUpperCase());
					Object fv = BeanUtil.getFieldValue(obj, field);
					if(null == fv) {
						continue;
					}
					DataSet set = new DataSet();
					Collection fvs = new ArrayList();
					if (null == join.dependencyTable) {
						//只通过中间表查主键 List<Long> departmentIds
						fvs = BeanUtil.object2list(fv);
					} else {
						//通过子表完整查询 List<Department> departments
						Column joinpc = EntityAdapterProxy.primaryKey(clazz);
						String joinpk = null;
						if(null != joinpc) {
							joinpk = joinpc.getName();
						}
						if(fv.getClass().isArray()) {
							int len = Array.getLength(fv);
							for(int i=0; i<len; i++) {
								fvs.add(EntityAdapterProxy.primaryValue(Array.get(fv, i)).get(joinpk.toUpperCase()));
							}
						}else if(fv instanceof Collection) {
							Collection objs = (Collection) fv;
							for(Object item:objs) {
								fvs.add(EntityAdapterProxy.primaryValue(item).get(joinpk.toUpperCase()));
							}
						}
					}

					for(Object item:fvs) {
						DataRow row = new DataRow();
						row.put(join.joinColumn, pv);
						row.put(join.inverseJoinColumn, item);
						set.add(row);
					}
					if(mode == 1) {
						long qty = runtime.getAdapter().deletes(runtime, random, join.joinTable, join.joinColumn, pv);
						if(qty > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0) {
							if(!(obj instanceof DataRow)) {
								checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY);
								checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY);
							}
						}
					}
					runtime.getAdapter().save(runtime, random, join.joinTable, set);

				}catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						log.error("check dependency exception:", e);
					}else{
						log.error("[check Many2ManyDependency Save][result:fail][msg:{}]", e.toString());
					}
				}
			}
		}
		dependency --;
		return result;
	}

	private int checkOne2ManyDependencySave(DataRuntime runtime, String random, Object obj, int dependency, int mode) {
		int result = 0;
		//OneToMany
		if(dependency <= 0) {
			return result;
		}
		if(obj instanceof DataSet || obj instanceof DataRow || obj instanceof Map) {
			return result;
		}
		if(obj instanceof EntitySet) {
			EntitySet set = (EntitySet) obj;
			for(Object entity:set) {
				checkOne2ManyDependencySave(runtime, random, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			Column pc = EntityAdapterProxy.primaryKey(clazz);
			String pk = null;
			if(null != pc) {
				pk = pc.getName();
			}
			List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
			for(Field field:fields) {
				try {
					OneToMany join = PersistenceAdapter.oneToMany(field);
					Object pv = EntityAdapterProxy.primaryValue(obj).get(pk.toUpperCase());
					Object fv = BeanUtil.getFieldValue(obj, field);
					if(null == fv) {
						continue;
					}

					if(null == join.joinField) {
						throw new RuntimeException(field+"关联属性异常");
					}

					if(null == join.joinColumn) {
						throw new RuntimeException(field+"关联列异常");
					}

					if(null == join.dependencyTable) {
						throw new RuntimeException(field+"关联表异常");
					}
					if(mode == 1) {
						long qty = runtime.getAdapter().deletes(runtime, random, join.dependencyTable, join.joinColumn, pv);
						if(qty > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0) {
							if(!(obj instanceof Map)) {
								checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
								checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
							}
						}
					}
					Collection items = new ArrayList();
					if(fv.getClass().isArray()) {
						int len = Array.getLength(fv);
						for(int i=0; i<len; i++) {
							Object item = Array.get(fv, i);
							BeanUtil.setFieldValue(item, join.joinField, pv);
							items.add(item);
						}
					}else if(fv instanceof Collection) {
						Collection cols = (Collection) fv;
						for(Object item:cols) {
							BeanUtil.setFieldValue(item, join.joinField, pv);
							items.add(item);
						}
					}
					runtime.getAdapter().save(runtime, random, join.dependencyTable, items);

				}catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						log.error("check dependency exception:", e);
					}else{
						log.error("[check One2ManyDependency Save][result:fail][msg:{}]", e.toString());
					}
				}
			}
		}
		dependency --;
		return result;
	}

	protected <T> void checkMany2ManyDependencyQuery(DataRuntime runtime, String random, EntitySet<T> set, int dependency) {
		//ManyToMany
		if(set.isEmpty() || dependency <= 0) {
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc) {
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		Compare compare = ConfigTable.ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE;
		for(Field field:fields) {
			try {
				ManyToMany join = PersistenceAdapter.manyToMany(field);
				if(Compare.EQUAL == compare || set.size() == 1) {
					//逐行查询
					for (T entity : set) {
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						if (null == join.dependencyTable) {
							//只通过中间表查主键 List<Long> departmentIds
							//SELECT * FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
							DataSet items = runtime.getAdapter().querys(runtime, random, new DefaultTablePrepare(join.joinTable), new DefaultConfigStore(), "++" + join.joinColumn + ":" + primaryValueMap.get(pk.toUpperCase()));
							List<String> ids = items.getStrings(join.inverseJoinColumn);
							BeanUtil.setFieldValue(entity, field, ids);
						} else {
							//通过子表完整查询 List<Department> departments
							//SELECT * FROM HR_DEPARTMENT WHERE ID IN(SELECT DEPARTMENT_ID FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?)
							String sql = "SELECT * FROM " + join.dependencyTable + " WHERE " + join.dependencyPk + " IN (SELECT " + join.inverseJoinColumn + " FROM " + join.joinTable + " WHERE " + join.joinColumn + "= #{JOIN_VALUE}" + ")";
							ConfigStore configs = new DefaultConfigStore();
							configs.param("JOIN_VALUE", primaryValueMap.get(pk.toUpperCase()));
							EntitySet<T> dependencys = runtime.getAdapter().selects(runtime, random, new DefaultTextPrepare(sql), join.itemClass, configs);
							BeanUtil.setFieldValue(entity, field, dependencys);
						}
					}
				}else if(Compare.IN == compare) {
					//查出所有相关 再逐行分配
					List pvs = new ArrayList();
					Map<T, Object> idmap = new HashMap<>();
					for(T entity:set) {
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						Object pv = primaryValueMap.get(pk.toUpperCase());
						pvs.add(pv);
						idmap.put(entity, pv);
					}
					if (null == join.dependencyTable) {
						//只通过中间表查主键 List<Long> departmentIds
						//SELECT * FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID IN(?, ?, ?)
						ConfigStore conditions = new DefaultConfigStore();
						conditions.and(join.joinColumn, pvs);
						DataSet allItems = runtime.getAdapter().querys(runtime, random, new DefaultTablePrepare(join.joinTable), conditions);
						for(T entity:set) {
							DataSet items = allItems.getRows(join.joinColumn, idmap.get(entity)+"");
							List<String> ids = items.getStrings(join.inverseJoinColumn);
							BeanUtil.setFieldValue(entity, field, ids);
						}
					} else {
						//通过子表完整查询 List<Department> departments
						//SELECT M.*, F.EMPLOYEE_ID FROM hr_department AS M RIGHT JOIN hr_employee_department AS F ON M.ID = F.DEPARTMENT_ID WHERE F.EMPLOYEE_ID IN (1, 2)
						ConfigStore conditions = new DefaultConfigStore();
						conditions.param("JOIN_PVS", pvs);
						String sql = "SELECT M.*, F."+join.joinColumn+" FK_"+join.joinColumn+" FROM " + join.dependencyTable + " M RIGHT JOIN "+join.joinTable+" F ON M." + join.dependencyPk + " = "+join.inverseJoinColumn +" WHERE "+join.joinColumn+" IN(#{JOIN_PVS})";
						DataSet alls = runtime.getAdapter().querys(runtime, random, new DefaultTextPrepare(sql), conditions);
						for(T entity:set) {
							DataSet items = alls.getRows("FK_"+join.joinColumn, idmap.get(entity)+"");
							BeanUtil.setFieldValue(entity, field, items.entity(join.itemClass));
						}
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					log.error("check dependency exception:", e);
				}else{
					log.error("[check Many2ManyDependency query][result:fail][msg:{}]", e.toString());
				}
			}
		}
	}

	private int checkMany2ManyDependencyDelete(DataRuntime runtime, String random, Object entity, int dependency) {
		int result = 0;
		//ManyToMany
		if(dependency <= 0) {
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc) {
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		for(Field field:fields) {
			try {
				ManyToMany join = PersistenceAdapter.manyToMany(field);
				//DELETE FROM HR_DEPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
				runtime.getAdapter().deletes(runtime, random, join.joinTable, join.joinColumn, EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase()));

			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					log.error("check dependenty exception:", e);
				}else{
					log.error("[check Many2ManyDependency delete][result:fail][msg:{}]", e.toString());
				}
			}
		}
		return result;
	}
	private int checkOne2ManyDependencyDelete(DataRuntime runtime, String random, Object entity, int dependency) {
		int result = 0;
		//OneToMany
		if(dependency <= 0) {
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc) {
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		for(Field field:fields) {
			try {
				OneToMany join = PersistenceAdapter.oneToMany(field);
				//DELETE FROM HR_DEPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
				runtime.getAdapter().deletes(runtime, random, join.dependencyTable, join.joinColumn, EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase()));

			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					log.error("check dependency exception:", e);
				}else{
					log.error("[check One2ManyDependency delete][result:fail][msg:{}]", e.toString());
				}
			}
		}
		return result;
	}
	protected <T> void checkOne2ManyDependencyQuery(DataRuntime runtime, String random, EntitySet<T> set, int dependency) {
		//OneToMany
		if(set.isEmpty() || dependency <= 0) {
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc) {
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		Compare compare = ConfigTable.ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE;
		for(Field field:fields) {
			try {
				OneToMany join = PersistenceAdapter.oneToMany(field);
				if(Compare.EQUAL == compare || set.size() == 1) {
					//逐行查询
					for (T entity : set) {
						Object pv = EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase());
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						//通过子表完整查询 List<AttendanceRecord> records
						//SELECT * FROM HR_ATTENDANCE_RECORD WHERE EMPLOYEE_ID = ?)
						List<Object> params = new ArrayList<>();
						params.add(primaryValueMap.get(pk.toUpperCase()));
						EntitySet<T> dependencys = runtime.getAdapter().selects(runtime, random, null, join.dependencyClass, new DefaultConfigStore().and(join.joinColumn, pv));
						BeanUtil.setFieldValue(entity, field, dependencys);
					}
				}else if(Compare.IN == compare) {
					//查出所有相关 再逐行分配
					List pvs = new ArrayList();
					Map<T, Object> idmap = new HashMap<>();
					for(T entity:set) {
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						Object pv = primaryValueMap.get(pk.toUpperCase());
						pvs.add(pv);
						idmap.put(entity, pv);
					}
					//通过子表完整查询 List<Department> departments
					//SELECT M.*, F.EMPLOYEE_ID FROM hr_department AS M RIGHT JOIN hr_employee_department AS F ON M.ID = F.DEPARTMENT_ID WHERE F.EMPLOYEE_ID IN (1, 2)
					ConfigStore conditions = new DefaultConfigStore();
					conditions.and(join.joinColumn, pvs);
					EntitySet<T> alls = runtime.getAdapter().selects(runtime, random, null, join.dependencyClass, conditions);
					for(T entity:set) {
						EntitySet items = alls.gets(join.joinField, idmap.get(entity));
						BeanUtil.setFieldValue(entity, field, items);
					}

				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					log.error("check dependency exception:", e);
				}else{
					log.error("[check One2ManyDependency query][result:fail][msg:{}]", e.toString());
				}
			}

		}
	}

	/**
	 * 保存(insert|update)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data  data
	 * @param columns  columns
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String>  columns) {
		if(null == runtime) {
			runtime = runtime();
		}
		long result = runtime.getAdapter().save(runtime, random, dest, data, configs, columns);
		int ENTITY_FIELD_INSERT_DEPENDENCY = ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY;
		checkMany2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 1);
		checkOne2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 1);
		return result;

	}

	@Override
	public long save(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return save(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}

	/**
	 * 添加
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 需要插入的数据
	 * @param columns  需要插入的列
	 * @return int 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		if(null == runtime) {
			runtime = runtime();
		}
		long result =  runtime.getAdapter().insert(runtime, random, batch, dest, data, configs, columns);
		int ENTITY_FIELD_INSERT_DEPENDENCY = ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY;
		checkMany2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
		checkOne2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
		return result;
	}

	/**
	 * insert into table select * from table
	 * 与query参数一致
	 * @param dest 插入表
	 * @param prepare 查询表
	 * @param configs 查询条件及相关配置
	 * @param obj 查询条件
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare,  ConfigStore configs, Object obj, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().insert(runtime, random, dest, prepare, configs, obj, conditions);
	}
	/**
	 * 查询
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 查询表结构时使用
	 * @param system 系统表不查询表结构
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return DataSet
	 */
	protected DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().select(runtime, random, system, table, configs, run);
	}

	/**
	 * 执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs configs
	 * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().execute(runtime, random, prepare, configs, conditions);

	}

	@Override
	public long execute(DataRuntime runtime, String random, List<RunPrepare> prepares, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		List<RunPrepare> list = new ArrayList<>();
		for(RunPrepare prepare:prepares){
			if(!prepare.disposable()){
				list.add(prepare.clone());
			}else{
				list.add(prepare);
			}
		}
		return runtime.getAdapter().execute(runtime, random, list, configs);
	}

	@Override
	public long execute(DataRuntime runtime, String random, int batch, RunPrepare prepare, Collection<Object> values) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().execute(runtime, random, batch, null, prepare, values);
	}
	@Override
	public long execute(DataRuntime runtime, String random, int batch, int vol, RunPrepare prepare, Collection<Object> values) {
		if(null == runtime) {
			runtime = runtime();
		}
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().execute(runtime, random, batch, vol,null, prepare, values);
	}

	/**
	 * 执行存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure  存储过程
	 * @return 是否成功
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Procedure procedure) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().execute(runtime, random, procedure);
	}

	/**
	 * 根据存储过程查询(MSSQL AS 后必须加 SET NOCOUNT ON)<br/>
	 * @param procedure  procedure
	 * @param navi  navi
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().querys(runtime, random, procedure, navi);
	}

	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values 值集合
	 * @return long
	 * @param <T>
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String key, Collection<T> values) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().deletes(runtime, random, batch, table, key, values);

	}

	@Override
	public long delete(DataRuntime runtime, String random, Table dest, ConfigStore configs, Object obj, String... columns) {
		if(null == runtime) {
			runtime = runtime();
		}
		long qty = runtime.getAdapter().delete(runtime, random, dest, configs, obj, columns);
		if(qty > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0) {
			if(!(obj instanceof DataRow) && !(obj instanceof Collection)) {
				checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
				checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
			}
		}
		return qty;
	}
	@Override
	public long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().delete(runtime, random, table, configs, conditions);

	}
	@Override
	public long truncate(DataRuntime runtime, String random, Table table) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().truncate(runtime, random, table);
	}

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

	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Database> databases()
	 ******************************************************************************************************************/

	@Override
	public DatabaseType type() {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().type();
	}

	@Override
	public String version(DataRuntime runtime, String random) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().version(runtime, random);
	}

	@Override
	public String product(DataRuntime runtime, String random) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().product(runtime, random);
	}

	/**
	 * 根据结果集对象获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
	 * @param prepare RunPrepare
	 * @return LinkedHashMap
	 */
	public LinkedHashMap<String, Column> metadata(RunPrepare prepare, boolean comment) {
		DataRuntime runtime = runtime();
		if(null != prepare && !prepare.disposable()){
			prepare = prepare.clone();
		}
		return runtime.getAdapter().metadata(runtime, prepare, comment);
	}

	@Override
	public Database database(DataRuntime runtime, String random) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().database(runtime, random);
	}
	@Override
	public <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().databases(runtime, random, name);
	}
	@Override
	public <T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().databases(runtime, random, greedy, name);
	}
	@Override
	public Database database(DataRuntime runtime, String random, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().database(runtime, random, name);

	}

	@Override
	public Catalog catalog(DataRuntime runtime, String random) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().catalog(runtime, random);
	}
	@Override
	public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().catalogs(runtime, random, name);
	}
	@Override
	public <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().catalogs(runtime, random, greedy, name);
	}
	@Override
	public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Catalog catalog, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().schemas(runtime, random, catalog, name);
	}
	@Override
	public Schema schema(DataRuntime runtime, String random) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().schema(runtime, random);
	}
	@Override
	public <T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().schemas(runtime, random, greedy, catalog, name);
	}
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Table> tables(Catalog catalog, Schema schema, String name, int types)
	 * LinkedHashMap<String, Table> tables(Schema schema, String name, int types)
	 * LinkedHashMap<String, Table> tables(String name, int types)
	 * LinkedHashMap<String, Table> tables(int types)
	 * LinkedHashMap<String, Table> tables()
	 ******************************************************************************************************************/

	/**
	 * tables
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
	 * @return List
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Table query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().tables(runtime, random, greedy, query, types, struct, configs);
	}

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Table query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().tables(runtime, random, query, types, struct, configs);
	}

	/* *****************************************************************************************************************
	 * 													vertex
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, VertexTable> vertexs(Catalog catalog, Schema schema, String name, int types)
	 * LinkedHashMap<String, VertexTable> vertexs(Schema schema, String name, int types)
	 * LinkedHashMap<String, VertexTable> vertexs(String name, int types)
	 * LinkedHashMap<String, VertexTable> vertexs(int types)
	 * LinkedHashMap<String, VertexTable> vertexs()
	 ******************************************************************************************************************/

	/**
	 * vertexs
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
	 * @return List
	 */
	@Override
	public <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, VertexTable query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().vertexs(runtime, random, greedy, query, types, struct, configs);
	}

	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, VertexTable quer, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().vertexs(runtime, random, quer, types, struct, configs);
	}
	/* *****************************************************************************************************************
	 * 													edge
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, EdgeTable> edges(Catalog catalog, Schema schema, String name, int types)
	 * LinkedHashMap<String, EdgeTable> edges(Schema schema, String name, int types)
	 * LinkedHashMap<String, EdgeTable> edges(String name, int types)
	 * LinkedHashMap<String, EdgeTable> edges(int types)
	 * LinkedHashMap<String, EdgeTable> edges()
	 ******************************************************************************************************************/

	/**
	 * edges
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
	 * @return List
	 */
	@Override
	public <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, EdgeTable query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().edges(runtime, random, greedy, query, types, struct, configs);
	}

	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, EdgeTable query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().edges(runtime, random, query, types, struct, configs);
	}
	/**
	 * 查询表的创建SQL
	 * @param meta EdgeTable
	 * @param init 是否还原初始状态(如自增ID)
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, meta, init);
	}

	/**
	 * 查询表的创建SQL
	 * @param meta vertex
	 * @param init 是否还原初始状态(如自增ID)
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, VertexTable meta, boolean init) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, meta, init);
	}

	/**
	 * 查询表的创建SQL
	 * @param table table
	 * @param init 是否还原初始状态(如自增ID)
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, table, init);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, View> views(Catalog catalog, Schema schema, String name, int types)
	 * LinkedHashMap<String, View> views(Schema schema, String name, int types)
	 * LinkedHashMap<String, View> views(String name, int types)
	 * LinkedHashMap<String, View> views(int types)
	 * LinkedHashMap<String, View> views()
	 ******************************************************************************************************************/

	/**
	 * views
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
	 * @return List
	 */
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, View query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().views(runtime, random, greedy, query, types, struct, configs);
	}

	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().views(runtime, random, query, types, struct, configs);
	}

	@Override
	public List<String> ddl(DataRuntime runtime, String random, View view, boolean init) {
		return runtime.getAdapter().ddl(runtime, random, view, init);
	}

	/* *****************************************************************************************************************
	 * 													master
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, MasterTable> masters(Catalog catalog, Schema schema, String name, int types)
	 * LinkedHashMap<String, MasterTable> masters(Schema schema, String name, int types)
	 * LinkedHashMap<String, MasterTable> masters(String name, int types)
	 * LinkedHashMap<String, MasterTable> masters(int types)
	 * LinkedHashMap<String, MasterTable> masters()
	 ******************************************************************************************************************/

	/**
	 * masters
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
	 * @return List
	 */
	@Override
	public <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, MasterTable query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().masters(runtime, random, greedy, query, types, struct, configs);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, int types, int struct, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().masters(runtime, random, query, types, struct, configs);
	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, PartitionTable> partitions(Catalog catalog, Schema schema, String master, String name)
	 * LinkedHashMap<String, PartitionTable> partitions(Schema schema, String master, String name)
	 * LinkedHashMap<String, PartitionTable> partitions(String master, String name)
	 * LinkedHashMap<String, PartitionTable> partitions(String master)
	 * LinkedHashMap<String, PartitionTable> partitions(MasterTable table)
	 ******************************************************************************************************************/

	/**
	 * 表分区方式及分片
	 * @param table 主表
	 * @return Partition
	 */
	@Override
	public Table.Partition partition(DataRuntime runtime, String random, Table table) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().partition(runtime, random, table);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, String random, boolean greedy, PartitionTable query) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().partitions(runtime, random, greedy, query);
	}

	/**
	 * 查询 PartitionTable 创建SQL
	 * @param table PartitionTable
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, table);
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Column> columns(Table table)
	 * LinkedHashMap<String, Column> columns(String table)
	 * LinkedHashMap<String, Column> columns(Catalog catalog, Schema schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().columns(runtime, random, greedy, table, primary, configs);
	}

	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Column query, ConfigStore configs) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().columns(runtime, random, greedy, query, configs);

	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Tag> tags(Table table)
	 * LinkedHashMap<String, Tag> tags(String table)
	 * LinkedHashMap<String, Tag> tags(Catalog catalog, Schema schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().tags(runtime, random, greedy, table);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * PrimaryKey primary(Table table)
	 * PrimaryKey primary(String table)
	 * PrimaryKey primary(Catalog catalog, Schema schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return map
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().primary(runtime, random, greedy, table);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().foreigns(runtime, random, greedy, table);
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Index> indexes(Table table, String name)
	 * LinkedHashMap<String, Index> indexes(String table, String name)
	 * LinkedHashMap<String, Index> indexes(Table table)
	 * LinkedHashMap<String, Index> indexes(String table)
	 * LinkedHashMap<String, Index> indexes(Catalog catalog, Schema schema, String table)
	 ******************************************************************************************************************/

	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().indexes(runtime, random, greedy, table, name);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().indexes(runtime, random, table, name);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Constraint> constraints(Table table, String name)
	 * LinkedHashMap<String, Constraint> constraints(String table, String name)
	 * LinkedHashMap<String, Constraint> constraints(Table table)
	 * LinkedHashMap<String, Constraint> constraints(String table)
	 * LinkedHashMap<String, Constraint> constraints(Catalog catalog, Schema schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().constraints(runtime, random, greedy, table, name);
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().constraints(runtime, random, table, column, name);
	}
	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().triggers(runtime, random, greedy, table, events);

	}

	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().procedures(runtime, random, greedy, catalog, schema, name);
	}
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().procedures(runtime, random, catalog, schema, name);
	}

	/**
	 * 查询 Procedure 创建SQL
	 * @param procedure Procedure
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, procedure);

	}

	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().functions(runtime, random, greedy, catalog, schema, name);
	}
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().functions(runtime, random, catalog, schema, name);
	}

	/**
	 * 查询 Function 创建SQL
	 * @param function Function
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Function function) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, function);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 ******************************************************************************************************************/
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().sequences(runtime, random, greedy, catalog, schema, name);
	}
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().sequences(runtime, random, catalog, schema, name);
	}

	/**
	 * 查询 sequence 创建SQL
	 * @param sequence 序列
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Sequence sequence) {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().ddl(runtime, random, sequence);
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
	 * 解析DDL
	 * @param type Metadata类型
	 * @param ddl ddl
	 * @param configs 其他配置
	 * @return T
	 * @param <T> T
	 */
	@Override
	public <T extends Metadata> T parse(Class<T> type, String ddl, ConfigStore configs) {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().parse(runtime, type, ddl, configs);
	}
	/* *****************************************************************************************************************
	 * 													catalog
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Catalog catalog) throws Exception
	 * boolean alter(Catalog catalog) throws Exception
	 * boolean drop(Catalog catalog) throws Exception
	 * boolean rename(Catalog origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Catalog meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Catalog meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	@Override
	public boolean drop(Catalog meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	/**
	 * 重命名
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(Catalog origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}


	/* *****************************************************************************************************************
	 * 													schema
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Schema schema) throws Exception
	 * boolean alter(Schema schema) throws Exception
	 * boolean drop(Schema schema) throws Exception
	 * boolean rename(Schema origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Schema meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Schema meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	@Override
	public boolean drop(Schema meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	/**
	 * 重命名
	 * @param origin 原Schema
	 * @param name 新名称
	 * @return boolean
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(Schema origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}
	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Database database) throws Exception
	 * boolean alter(Database database) throws Exception
	 * boolean drop(Database database) throws Exception
	 * boolean rename(Database origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Database meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Database meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	@Override
	public boolean drop(Database meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	/**
	 * 重命名
	 * @param origin 原Database
	 * @param name 新名称
	 * @return boolean
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(Database origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}


	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Table table) throws Exception
	 * boolean alter(Table table) throws Exception
	 * boolean drop(Table table) throws Exception
	 * boolean rename(Table origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Table meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Table meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	@Override
	public boolean drop(Table meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	/**
	 * 重命名
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(Table origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(View view) throws Exception
	 * boolean alter(View view) throws Exception
	 * boolean drop(View view) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(View meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(View meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	@Override
	public boolean drop(View meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	@Override
	public boolean rename(View origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(MasterTable table) throws Exception
	 * boolean alter(MasterTable table) throws Exception
	 * boolean drop(MasterTable table) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(MasterTable meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}
	@Override
	public boolean alter(MasterTable meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean drop(MasterTable meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(MasterTable origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(PartitionTable table) throws Exception
	 * boolean alter(PartitionTable table) throws Exception
	 * boolean drop(PartitionTable table) throws Exception
	 ******************************************************************************************************************/

	@Override
	public boolean create(PartitionTable meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}
	@Override
	public boolean alter(PartitionTable meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}
	@Override
	public boolean drop(PartitionTable meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	@Override
	public boolean rename(PartitionTable origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Column column) throws Exception
	 * boolean alter(Table table, Column column) throws Exception
	 * boolean alter(Column column) throws Exception
	 * boolean drop(Column column) throws Exception
	 *
	 * private boolean alter(Table table, Column column, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Column meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}
	@Override
	public boolean drop(Column meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean alter(Table table, Column column) throws Exception {
		return alter(table, column, true);
	}
	@Override
	public boolean alter(Column meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	/**
	 * 修改列
	 * @param meta 列
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	public boolean alter(Table table, Column meta, boolean trigger) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, table, meta, trigger);
	}

	@Override
	public boolean rename(Column origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Tag tag) throws Exception
	 * boolean alter(Table table, Tag tag) throws Exception
	 * boolean alter(Tag tag) throws Exception
	 * boolean drop(Tag tag) throws Exception
	 *
	 * private boolean alter(Table table, Tag tag, boolean trigger) throws Exception
	 ******************************************************************************************************************/

	@Override
	public boolean add(Tag meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}
	@Override
	public boolean drop(Tag meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean alter(Table table, Tag column) throws Exception {
		return alter(table, column, true);
	}
	@Override
	public boolean alter(Tag meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}

	/**
	 * 修改标签
	 * @param meta 标签
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	public boolean alter(Table table, Tag meta, boolean trigger) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, table, meta, trigger);
	}

	@Override
	public boolean rename(Tag origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(PrimaryKey primary) throws Exception
	 * boolean alter(PrimaryKey primary) throws Exception
	 * boolean drop(PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(PrimaryKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}

	@Override
	public boolean alter(PrimaryKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean alter(Table table, PrimaryKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, table, meta);
	}
	@Override
	public boolean drop(PrimaryKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	@Override
	public boolean rename(PrimaryKey origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(ForeignKey foreign) throws Exception
	 * boolean alter(ForeignKey foreign) throws Exception
	 * boolean drop(ForeignKey foreign) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(ForeignKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}

	@Override
	public boolean alter(ForeignKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean alter(Table table, ForeignKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, table, meta);
	}
	@Override
	public boolean drop(ForeignKey meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}

	@Override
	public boolean rename(ForeignKey origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Index index) throws Exception
	 * boolean alter(Index index) throws Exception
	 * boolean drop(Index index) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Index meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}

	@Override
	public boolean alter(Index meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean alter(Table table, Index meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, table, meta);
	}
	@Override
	public boolean drop(Index meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(Index origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Constraint constraint) throws Exception
	 * boolean alter(Constraint constraint) throws Exception
	 * boolean drop(Constraint constraint) throws Exception
	 * boolean rename(Constraint origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Constraint meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}

	@Override
	public boolean alter(Constraint meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}
	@Override
	public boolean alter(Table table, Constraint meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, table, meta);
	}
	@Override
	public boolean drop(Constraint meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(Constraint origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Trigger meta) throws Exception
	 * boolean alter(Trigger meta) throws Exception
	 * boolean drop(Trigger meta) throws Exception
	 * boolean rename(Trigger origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Trigger meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().add(runtime, meta);
	}

	@Override
	public boolean alter(Trigger meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean drop(Trigger meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(Trigger origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Procedure procedure) throws Exception
	 * boolean alter(Procedure procedure) throws Exception
	 * boolean drop(Procedure procedure) throws Exception
	 * boolean rename(Procedure origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Procedure meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Procedure meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean drop(Procedure meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(Procedure origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Function meta) throws Exception
	 * boolean alter(Function meta) throws Exception
	 * boolean drop(Function meta) throws Exception
	 * boolean rename(Function origin, String name)  throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Function meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Function meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean drop(Function meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(Function origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Sequence meta) throws Exception
	 * boolean alter(Sequence meta) throws Exception
	 * boolean drop(Sequence meta) throws Exception
	 * boolean rename(Sequence origin, String name)  throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Sequence meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, meta);
	}

	@Override
	public boolean alter(Sequence meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().alter(runtime, meta);
	}
	@Override
	public boolean drop(Sequence meta) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, meta);
	}
	@Override
	public boolean rename(Sequence origin, String name) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, name);
	}

	/* *****************************************************************************************************************
	 *
	 * 													Authorize
	 *
	 * =================================================================================================================
	 * role			: 角色
	 * user			: 用户
	 * grant		: 授权
	 * privilege	: 权限
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													role
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Role role) throws Exception
	 * <T extends Role> List<T> roles(Role query) throws Exception
	 * boolean rename(Role origin, Role update) throws Exception
	 * boolean drop(Role role) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 创建角色
	 * @param role 角色
	 * @return boolean
	 */
	@Override
	public boolean create(Role role) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, role);
	}

	/**
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, Role query) throws Exception {
		if(null == runtime) {
			runtime = runtime();
		}
		return runtime.getAdapter().roles(runtime, random, greedy, query);
	}

	/**
	 * 角色重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	@Override
	public boolean rename(Role origin, Role update) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, update);
	}
	/**
	 * 删除角色
	 * @param role 角色
	 * @return boolean
	 */
	@Override
	public boolean drop(Role role) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, role);
	}

	/* *****************************************************************************************************************
	 * 													user
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(User user) throws Exception
	 * <T extends Role> List<T> roles(User query) throws Exception
	 * boolean rename(User origin, Role update) throws Exception
	 * boolean drop(User user) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 创建用户
	 * @param user 用户
	 * @return boolean
	 */
	@Override
	public boolean create(User user) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().create(runtime, user);
	}

	/**
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public List<User> users(DataRuntime runtime, String random, boolean greedy, User query) throws Exception{
		return runtime.getAdapter().users(runtime, random, greedy, query);
	}
	/**
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	@Override
	public boolean rename(User origin, User update) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().rename(runtime, origin, update);
	}

	/**
	 * 删除用户
	 * @param user 用户
	 * @return boolean
	 */
	@Override
	public boolean drop(User user) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().drop(runtime, user);
	}

	/* *****************************************************************************************************************
	 * 													grant
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean grant(User user, Privilege ... privileges) throws Exception
	 * boolean grant(String user, Privilege ... privileges) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 授权
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	@Override
	public boolean grant(User user, Privilege ... privileges) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().grant(runtime, user, privileges);
	}
	/**
	 * 授权
	 * @param role 角色
	 * @param privileges 权限
	 * @return boolean
	 */
	@Override
	public boolean grant(Role role, Privilege ... privileges) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().grant(runtime, role, privileges);
	}
	/**
	 * 授权
	 * @param user 用户
	 * @param roles 角色
	 * @return boolean
	 */
	@Override
	public boolean grant(User user, Role ... roles) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().grant(runtime, user, roles);
	}

	/* *****************************************************************************************************************
	 * 													privilege
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Privilege> privileges(Privilege query) throws Exception;
	 * List<Privilege> privileges(User user) throws Exception
	 * List<Privilege> privileges(String user) throws Exception
	 * boolean revoke(User user, Privilege ... privileges) throws Exception
	 * boolean revoke(String user, Privilege ... privileges) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询用户权限
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public List<Privilege> privileges(DataRuntime runtime, String random, boolean greedy,Privilege query) throws Exception {
		return runtime.getAdapter().privileges(runtime, random, greedy, query);
	}

	/**
	 * 撤销授权
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	@Override
	public boolean revoke(User user, Privilege ... privileges) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().revoke(runtime, user, privileges);
	}

	/**
	 * 撤销授权
	 * @param role 角色
	 * @param privileges 权限
	 * @return boolean
	 */
	@Override
	public boolean revoke(Role role, Privilege ... privileges) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().revoke(runtime, role, privileges);
	}

	/**
	 * 撤销授权
	 * @param user 用户
	 * @param roles 角色
	 * @return boolean
	 */
	@Override
	public boolean revoke(User user, Role ... roles) throws Exception {
		DataRuntime runtime = runtime();
		return runtime.getAdapter().revoke(runtime, user, roles);
	}

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 * -----------------------------------------------------------------------------------------------------------------
	 * void checkSchema(DataRuntime runtime, Table table)
	 * protected String LogUtil.param(List<?> params)
	 * protected String LogUtil.param(List<?> keys, List<?> values)
	 * private static String random()
	 ******************************************************************************************************************/

	private String random(DataRuntime runtime) {
		StringBuilder builder = new StringBuilder();
		builder.append("[cmd:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
				.append("][thread:")
				.append(Thread.currentThread().getId()).append("][ds:").append(runtime.datasource()).append("]");
		return builder.toString();
	}

}
