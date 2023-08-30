
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


package org.anyline.dao.init.springjdbc;
import org.anyline.adapter.PersistenceAdapter;
import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.util.ClientHolder;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.metadata.*;
import org.anyline.metadata.ACTION.DDL;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.persistence.ManyToMany;
import org.anyline.metadata.persistence.OneToMany;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.*;

@Primary
@Repository("anyline.dao")
public class DefaultDao<E> implements AnylineDao<E> {
	protected static final Logger log = LoggerFactory.getLogger(DefaultDao.class);

	protected static DMListener dmListener;
	protected static DDListener ddListener;

	protected static boolean isBatchInsertRun = false;

	//默认环境,如果没有值则根据当前线程动态获取
	//用于ServiceProxy中生成多个service/dao/jdbc
	protected DataRuntime runtime = null;

	public DataRuntime runtime(){
		if(null != runtime){
			//固定数据源
			//runtime.setDao(this);
			return runtime;
		}
		//可切换数据源
		DataRuntime r = RuntimeHolder.getRuntime();
		if(null != r){
			//r.setDao(this);
		}
		return r;
	}


	/**
	 * 是否固定数据源
	 * @return boolean
	 */
	public boolean isFix(){
		return false;
	}
	public DMListener getListener() {
		return dmListener;
	}

	@Autowired(required=false)
	public void setDMListener(DMListener listener) {
		DefaultDao.dmListener = listener;
	}
	@Autowired(required=false)
	public void setDDListener(DDListener listener) {
		DefaultDao.ddListener = listener;
	}

	public DataRuntime getRuntime() {
		return runtime;
	}


	public void setRuntime(DataRuntime runtime) {
		this.runtime = runtime;
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
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().maps(runtime, random, prepare, configs, conditions);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 查询<br/>
	 * 注意:如果设置了自动还原,querys会自动还原数据源(dao内部执行过程中不要调用除非是一些重载),而select不会
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		if(null == runtime) {
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().querys(runtime, null, prepare, configs, conditions);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}
	/**
	 * 查询<br/>
	 * 注意:如果设置了自动还原,querys会自动还原数据源(dao内部执行过程中不要调用除非是一些重载),而select不会
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	@Override
	public <T> EntitySet<T> selects(DataRuntime runtime, String random,  boolean recover, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) {
		if(null == runtime){
			runtime = runtime();
		}
		try {
			EntitySet set = runtime.getAdapter().selects(runtime, null, prepare, clazz, configs, conditions);
			int dependency = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_SELECT_DEPENDENCY();
			if(dependency > 0) {
				checkMany2ManyDependencyQuery(runtime, random, set, dependency);
				checkOne2ManyDependencyQuery(runtime, random, set, dependency);
			}
			return set;
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}


	/**
	 * 查询序列值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 可以是多个序列
	 * @return DataRow
	 */
	public DataRow sequence(DataRuntime runtime, String random,  boolean recover, boolean next, String ... names) {
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().sequence(runtime, null, next, names);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 统计总行数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random,  boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().count(runtime, null, prepare, configs, conditions);
		}finally{
			if(!isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 检测是否存在
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return boolean
	 */
	@Override
	public boolean exists(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		if(null == runtime){
			runtime = runtime();
		}
		try {
 			return runtime.getAdapter().exists(runtime, random, prepare, configs, conditions);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 更新
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param dest		需要更新的表,如果没有提供则根据data解析
	 * @param data		需要更新的数据
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, boolean recover, int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			long result = runtime.getAdapter().update(runtime, random, dest, data, configs, columns);
			checkMany2ManyDependencySave(runtime, random, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
			checkOne2ManyDependencySave(runtime, random, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
			return result;
		}finally{
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 检测级联insert/update
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param obj obj
	 * @param dependency dependency
	 * @param mode 0:inser 1:update
	 * @return int
	 */
	private int checkMany2ManyDependencySave(DataRuntime runtime, String random, Object obj, int dependency, int mode){
		int result = 0;
		//ManyToMany
		if(dependency <= 0){
			return result;
		}
		if(obj instanceof DataSet || obj instanceof DataRow || obj instanceof Map){
			return result;
		}
		if(obj instanceof EntitySet){
			EntitySet set = (EntitySet) obj;
			for(Object entity:set){
				checkMany2ManyDependencySave(runtime, random, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			Column pc = EntityAdapterProxy.primaryKey(clazz);
			String pk = null;
			if(null != pc){
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
					if(null == fv){
						continue;
					}
					DataSet set = new DataSet();
					Collection fvs = new ArrayList();
					if (null == join.dependencyTable) {
						//只通过中间表查主键 List<Long> departmentIds
						if(fv.getClass().isArray()){
							fvs = BeanUtil.array2collection(fv);
						}else if(fv instanceof Collection){
							fvs = (Collection) fv;
						}
					} else {
						//通过子表完整查询 List<Department> departments
						Column joinpc = EntityAdapterProxy.primaryKey(clazz);
						String joinpk = null;
						if(null != joinpc){
							joinpk = joinpc.getName();
						}
						if(fv.getClass().isArray()){
							Object[] objs = (Object[])fv;
							for(Object item:objs){
								fvs.add(EntityAdapterProxy.primaryValue(item).get(joinpk.toUpperCase()));
							}
						}else if(fv instanceof Collection){
							Collection objs = (Collection) fv;
							for(Object item:objs){
								fvs.add(EntityAdapterProxy.primaryValue(item).get(joinpk.toUpperCase()));
							}
						}
					}

					for(Object item:fvs){
						DataRow row = new DataRow();
						row.put(join.joinColumn, pv);
						row.put(join.inverseJoinColumn, item);
						set.add(row);
					}
					if(mode == 1) {
						long qty = runtime.getAdapter().delete(runtime, null,  join.joinTable, join.joinColumn, pv + "");
						if(qty > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0){
							if(!(obj instanceof DataRow)){
								checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
								checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
							}
						}
					}
					runtime.getAdapter().save(runtime, random, join.joinTable, set, false);

				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else{
						log.error("[check Many2ManyDependency Save][result:fail][msg:{}]", e.toString());
					}
				}
			}
		}
		dependency --;
		return result;
	}

	private int checkOne2ManyDependencySave(DataRuntime runtime, String random, Object obj, int dependency, int mode){
		int result = 0;
		//OneToMany
		if(dependency <= 0){
			return result;
		}
		if(obj instanceof DataSet || obj instanceof DataRow || obj instanceof Map){
			return result;
		}
		if(obj instanceof EntitySet){
			EntitySet set = (EntitySet) obj;
			for(Object entity:set){
				checkOne2ManyDependencySave(runtime, random, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			Column pc = EntityAdapterProxy.primaryKey(clazz);
			String pk = null;
			if(null != pc){
				pk = pc.getName();
			}
			List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
			for(Field field:fields) {
				try {
					OneToMany join = PersistenceAdapter.oneToMany(field);
					Object pv = EntityAdapterProxy.primaryValue(obj).get(pk.toUpperCase());
					Object fv = BeanUtil.getFieldValue(obj, field);
					if(null == fv){
						continue;
					}

					if(null == join.joinField){
						throw new RuntimeException(field+"关联属性异常");
					}

					if(null == join.joinColumn){
						throw new RuntimeException(field+"关联列异常");
					}

					if(null == join.dependencyTable){
						throw new RuntimeException(field+"关联表异常");
					}
					if(mode == 1) {
						long qty = runtime.getAdapter().delete(runtime, random, join.dependencyTable, join.joinColumn, pv + "");
						if(qty > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0){
							if(!(obj instanceof DataRow)){
								checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
								checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
							}
						}
					}
					Collection items = new ArrayList();
					if(fv.getClass().isArray()){
						Object[] objs = (Object[])fv;
						for(Object item:objs){
							BeanUtil.setFieldValue(item, join.joinField, pv);
							items.add(item);
						}
					}else if(fv instanceof Collection){
						Collection cols = (Collection) fv;
						for(Object item:cols){
							BeanUtil.setFieldValue(item, join.joinField, pv);
							items.add(item);
						}
					}
					runtime.getAdapter().save(runtime, random, join.dependencyTable, items, false);

				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
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
		if(set.size()==0 || dependency <= 0){
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		Compare compare = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE();
		for(Field field:fields){
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
				}else if(Compare.IN == compare){
					//查出所有相关 再逐行分配
					List pvs = new ArrayList();
					Map<T,Object> idmap = new HashMap<>();
					for(T entity:set){
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						Object pv = primaryValueMap.get(pk.toUpperCase());
						pvs.add(pv);
						idmap.put(entity, pv);
					}
					if (null == join.dependencyTable) {
						//只通过中间表查主键 List<Long> departmentIds
						//SELECT * FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID IN(?,?,?)
						ConfigStore conditions = new DefaultConfigStore();
						conditions.and(join.joinColumn, pvs);
						DataSet allItems = runtime.getAdapter().querys(runtime, random,   new DefaultTablePrepare(join.joinTable), conditions);
						for(T entity:set){
							DataSet items = allItems.getRows(join.joinColumn, idmap.get(entity)+"");
							List<String> ids = items.getStrings(join.inverseJoinColumn);
							BeanUtil.setFieldValue(entity, field, ids);
						}
					} else {
						//通过子表完整查询 List<Department> departments
						//SELECT M.*, F.EMPLOYEE_ID FROM hr_department AS M RIGHT JOIN hr_employee_department AS F ON M.ID = F.DEPARTMENT_ID WHERE F.EMPLOYEE_ID IN (1,2)
						ConfigStore conditions = new DefaultConfigStore();
						conditions.param("JOIN_PVS", pvs);
						String sql = "SELECT M.*, F."+join.joinColumn+" FK_"+join.joinColumn+" FROM " + join.dependencyTable + " M RIGHT JOIN "+join.joinTable+" F ON M." + join.dependencyPk + " = "+join.inverseJoinColumn +" WHERE "+join.joinColumn+" IN(#{JOIN_PVS})";
						DataSet alls = runtime.getAdapter().querys(runtime, random,   new DefaultTextPrepare(sql), conditions);
						for(T entity:set){
							DataSet items = alls.getRows("FK_"+join.joinColumn, idmap.get(entity)+"");
							BeanUtil.setFieldValue(entity, field, items.entity(join.itemClass));
						}
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check Many2ManyDependency query][result:fail][msg:{}]", e.toString());
				}
			}
		}
	}

	private int checkMany2ManyDependencyDelete(DataRuntime runtime, String random, Object entity, int dependency){
		int result = 0;
		//ManyToMany
		if(dependency <= 0){
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		for(Field field:fields) {
			try {
				ManyToMany join = PersistenceAdapter.manyToMany(field);
				//DELETE FROM HR_DEPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
				runtime.getAdapter().delete(runtime, random,  join.joinTable, join.joinColumn, EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase())+"");

			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check Many2ManyDependency delete][result:fail][msg:{}]", e.toString());
				}
			}
		}
		return result;
	}
	private int checkOne2ManyDependencyDelete(DataRuntime runtime, String random, Object entity, int dependency){
		int result = 0;
		//OneToMany
		if(dependency <= 0){
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		for(Field field:fields) {
			try {
				OneToMany join = PersistenceAdapter.oneToMany(field);
				//DELETE FROM HR_DEPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
				runtime.getAdapter().delete(runtime, random, join.dependencyTable, join.joinColumn, EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase())+"");

			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check One2ManyDependency delete][result:fail][msg:{}]", e.toString());
				}
			}
		}
		return result;
	}
	protected <T> void checkOne2ManyDependencyQuery(DataRuntime runtime, String random, EntitySet<T> set, int dependency) {
		//OneToMany
		if(set.size()==0 || dependency <= 0){
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		Compare compare = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE();
		for(Field field:fields){
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
				}else if(Compare.IN == compare){
					//查出所有相关 再逐行分配
					List pvs = new ArrayList();
					Map<T,Object> idmap = new HashMap<>();
					for(T entity:set){
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						Object pv = primaryValueMap.get(pk.toUpperCase());
						pvs.add(pv);
						idmap.put(entity, pv);
					}
					//通过子表完整查询 List<Department> departments
					//SELECT M.*, F.EMPLOYEE_ID FROM hr_department AS M RIGHT JOIN hr_employee_department AS F ON M.ID = F.DEPARTMENT_ID WHERE F.EMPLOYEE_ID IN (1,2)
					ConfigStore conditions = new DefaultConfigStore();
					conditions.and(join.joinColumn, pvs);
					EntitySet<T> alls = runtime.getAdapter().selects(runtime, random, null, join.dependencyClass, conditions);
					for(T entity:set){
						EntitySet items = alls.gets(join.joinField, idmap.get(entity));
						BeanUtil.setFieldValue(entity, field, items);
					}

				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check One2ManyDependency query][result:fail][msg:{}]", e.toString());
				}
			}

		}
	}

	/**
	 * 保存(insert|upate)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param dest  表
	 * @param data  data
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns  columns
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, boolean recover, int batch, String dest, Object data, boolean checkPrimary, List<String>  columns){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().save(runtime, random, dest, data, checkPrimary, columns);
		}finally{
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 添加
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param dest 表
	 * @param data 需要插入的数据
	 * @param checkPrimary   是否需要检查重复主键,默认不检查
	 * @param columns  需要插入的列
	 * @return int 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, boolean recover, int batch, String dest, Object data, boolean checkPrimary, List<String> columns) {
		if(null == runtime){
			runtime = runtime();
		}
		try{
			long result =  runtime.getAdapter().insert(runtime, random, dest,data, checkPrimary, columns);
			int ENTITY_FIELD_INSERT_DEPENDENCY = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_INSERT_DEPENDENCY();
			checkMany2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
			checkOne2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
			return result;
		}finally{
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}


	/**
	 * 查询
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param table 查询表结构时使用
	 * @param system 系统表不查询表结构
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return DataSet
	 */
	protected DataSet select(DataRuntime runtime, String random, boolean recover, boolean system, String table, ConfigStore configs, Run run){
		if(null == runtime){
			runtime = runtime();
		}try{
			return runtime.getAdapter().select(runtime, random, system, table, configs, run);
		}finally{
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs configs
	 * @param conditions conditions
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().execute(runtime, random, prepare, configs, conditions);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 执行存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param procedure  存储过程
	 * @return 是否成功
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, boolean recover, Procedure procedure){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().execute(runtime, random, procedure);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 根据存储过程查询(MSSQL AS 后必须加 SET NOCOUNT ON)<br/>
	 * @param procedure  procedure
	 * @param navi  navi
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, boolean recover, Procedure procedure, PageNavi navi){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().querys(runtime, random, procedure, navi);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 *
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random
	 * @param recover
	 * @param table 表
	 * @param key 列
	 * @param values 值集合
	 * @return
	 * @param <T>
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, boolean recover, int batch, String table, String key, Collection<T> values){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().deletes(runtime, random, table, key, values);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	@Override
	public long delete(DataRuntime runtime, String random, boolean recover, String dest, Object obj, String... columns) {
		if(null == runtime){
			runtime = runtime();
		}
		try {
			long qty = runtime.getAdapter().delete(runtime, random, dest, obj, columns);

			if(qty > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0){
				if(!(obj instanceof DataRow)){
					checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
					checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
				}
			}
			return qty;
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}
	@Override
	public long delete(DataRuntime runtime, String random, boolean recover, String table, ConfigStore configs, String... conditions) {
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().delete(runtime, random, table, configs, conditions);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}
	@Override
	public int truncate(DataRuntime runtime, String random, boolean recover, String table){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().truncate(runtime, random, table);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
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
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, boolean recover){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().databases(runtime, random);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}
	@Override
	public Database database(DataRuntime runtime, String random, boolean recover, String name){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().database(runtime, random, name);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Table> tables(String catalog, String schema, String name, String types)
	 * LinkedHashMap<String, Table> tables(String schema, String name, String types)
	 * LinkedHashMap<String, Table> tables(String name, String types)
	 * LinkedHashMap<String, Table> tables(String types)
	 * LinkedHashMap<String, Table> tables()
	 ******************************************************************************************************************/

	/**
	 * tables
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
	 * @param pattern 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
	 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
	 * @return List
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String pattern, String types){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().tables(runtime, random, greedy, catalog, schema, pattern, types);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, boolean recover, String catalog, String schema, String pattern, String types){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().tables(runtime, random, catalog, schema, pattern, types);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}


	/**
	 * 查询表的创建SQL
	 * @param table table
	 * @param init 是否还原初始状态(如自增ID)
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, boolean recover, Table table, boolean init){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ddl(runtime, random, table, init);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, View> views(String catalog, String schema, String name, String types)
	 * LinkedHashMap<String, View> views(String schema, String name, String types)
	 * LinkedHashMap<String, View> views(String name, String types)
	 * LinkedHashMap<String, View> views(String types)
	 * LinkedHashMap<String, View> views()
	 ******************************************************************************************************************/

	/**
	 * views
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
	 * @param pattern 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
	 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
	 * @return List
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String pattern, String types){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().views(runtime, random, greedy, catalog, schema, pattern, types);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}


	/**
	 * 查询view的创建SQL
	 * @param view view
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, boolean recover, View view){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ddl(runtime, random, view);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types)
	 * LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types)
	 * LinkedHashMap<String, MasterTable> mtables(String name, String types)
	 * LinkedHashMap<String, MasterTable> mtables(String types)
	 * LinkedHashMap<String, MasterTable> mtables()
	 ******************************************************************************************************************/
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String pattern, String types) {
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().mtables(runtime, random, greedy,  catalog, schema, pattern, types);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 查询MasterTable创建SQL
	 * @param table MasterTable
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, boolean recover, MasterTable table){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ddl(runtime, random, table);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String master, String name)
	 * LinkedHashMap<String, PartitionTable> ptables(String schema, String master, String name)
	 * LinkedHashMap<String, PartitionTable> ptables(String master, String name)
	 * LinkedHashMap<String, PartitionTable> ptables(String master)
	 * LinkedHashMap<String, PartitionTable> ptables(MasterTable table)
	 ******************************************************************************************************************/


	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean recover, boolean greedy, MasterTable master, Map<String, Object> tags, String name){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ptables(runtime, random, greedy, master, tags, name);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}


	/**
	 * 查询 PartitionTable 创建SQL
	 * @param table PartitionTable
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, boolean recover, PartitionTable table){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ddl(runtime, random, table);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Column> columns(Table table)
	 * LinkedHashMap<String, Column> columns(String table)
	 * LinkedHashMap<String, Column> columns(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, boolean primary){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().columns(runtime, random, greedy, table, primary);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}
	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Tag> tags(Table table)
	 * LinkedHashMap<String, Tag> tags(String table)
	 * LinkedHashMap<String, Tag> tags(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table) {
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().tags(runtime, random, greedy, table);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * PrimaryKey primary(Table table)
	 * PrimaryKey primary(String table)
	 * PrimaryKey primary(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表
	 * @return map
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().primary(runtime, random, greedy, table);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().foreigns(runtime, random, greedy, table);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Index> indexs(Table table, String name)
	 * LinkedHashMap<String, Index> indexs(String table, String name)
	 * LinkedHashMap<String, Index> indexs(Table table)
	 * LinkedHashMap<String, Index> indexs(String table)
	 * LinkedHashMap<String, Index> indexs(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表
	 * @param name name
	 * @return map
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, String name){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().indexs(runtime, random, greedy, table, name);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}

	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Constraint> constraints(Table table, String name)
	 * LinkedHashMap<String, Constraint> constraints(String table, String name)
	 * LinkedHashMap<String, Constraint> constraints(Table table)
	 * LinkedHashMap<String, Constraint> constraints(String table)
	 * LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, String name) {
		return null;
	}
	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, List<Trigger.EVENT> events){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().triggers(runtime, random, greedy, table, events);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String name){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().procedures(runtime, random, greedy, catalog, schema, name);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 查询 Procedure 创建SQL
	 * @param procedure Procedure
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, boolean recover, Procedure procedure){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ddl(runtime, random, procedure);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String name){
		if(null == runtime){
			runtime = runtime();
		}
		try{
			return runtime.getAdapter().functions(runtime, random, greedy, catalog, schema, name);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
	}

	/**
	 * 查询 Function 创建SQL
	 * @param function Function
	 * @return list
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, boolean recover, Function function){
		if(null == runtime){
			runtime = runtime();
		}
		try {
			return runtime.getAdapter().ddl(runtime, random, function);
		}finally {
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
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
		boolean result = false;
		DDL action = DDL.TABLE_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Table table) throws Exception {
		boolean result = true;
		List<Run> runs = new ArrayList<>();
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();


		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		SWITCH swt = InterceptorProxy.prepare(runtime, random, DDL.TABLE_ALTER, table);
		if(null != ddListener && swt == SWITCH.CONTINUE) {
			swt = ddListener.parepareAlter(runtime, random, table);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, table);
		checkSchema(runtime, update);

		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, random, table, uname);
			table.setName(uname);
		}
		if(!result){
			return result;
		}

		//修改表备注
		String comment = update.getComment()+"";
		if(!comment.equals(table.getComment())){
			swt = InterceptorProxy.prepare(runtime, random, DDL.TABLE_COMMENT, table);
			if(swt != SWITCH.BREAK) {
				if(BasicUtil.isNotEmpty(table.getComment())) {
					runs.addAll(adapter.buildChangeCommentRun(runtime, update));
				}else{
					runs.addAll(adapter.buildAppendCommentRun(runtime, update));
				}
				swt = InterceptorProxy.before(runtime, random, DDL.TABLE_COMMENT, table, runs);
				if(swt != SWITCH.BREAK) {
					long cmt_fr = System.currentTimeMillis();
					result = execute(runtime, random, DDL.TABLE_COMMENT, runs) && result;
					InterceptorProxy.after(runtime, random, DDL.TABLE_COMMENT, table, runs, result, System.currentTimeMillis()-cmt_fr);
				}
			}
		}

		Map<String, Column> cols = new LinkedHashMap<>();

		// 更新列
		for (Column ucolumn : ucolumns.values()) {
			//先根据原列名 找到数据库中定义的列
			Column column = columns.get(ucolumn.getName().toUpperCase());
			//再检测update(如果name不一样需要rename)
			if(null != ucolumn.getUpdate()){
				ucolumn = ucolumn.getUpdate();
			}
			if (null != column) {
				// 修改列
				if (!column.equals(ucolumn)) {
					column.setTable(update);
					column.setUpdate(ucolumn, false, false);
					/*
					alter(column);
					result = true;*/
					column.setAction("alter");
					cols.put(column.getName().toUpperCase(), column);
				}
			} else {
				// 添加列
				ucolumn.setTable(update);
				/*
				add(ucolumn);
				result = true;*/
				ucolumn.setAction("add");
				cols.put(ucolumn.getName().toUpperCase(), ucolumn);
			}
		}
		List<String> deletes = new ArrayList<>();
		// 删除列(根据删除标记)
		for (Column column : ucolumns.values()) {
			if (column.isDrop()) {
				/*drop(column);*/
				deletes.add(column.getName().toUpperCase());
				column.setAction("drop");
				cols.put(column.getName().toUpperCase(), column);
			}
		}
		// 删除列(根据新旧对比)
		if (table.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if (column instanceof Tag) {
					continue;
				}
				if (column.isDrop() || deletes.contains(column.getName().toUpperCase()) || "drop".equals(column.getAction())) {
					//上一步已删除
					continue;
				}

				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					/*
					drop(column);
					result = true;*/
					column.setAction("drop");
					cols.put(column.getName().toUpperCase(), column);
				}
			}
		}

		//主键
		PrimaryKey src_primary = primary(table);
		PrimaryKey cur_primary = update.getPrimaryKey();
		String src_define = "";
		String cur_define = "";
		if(null != src_primary){
			src_define= BeanUtil.concat(src_primary.getColumns().values(),"name", ",");
		}
		if(null != cur_primary){
			cur_define= BeanUtil.concat(cur_primary.getColumns().values(),"name", ",");
		}
		boolean change_pk = !cur_define.equalsIgnoreCase(src_define);
		//如果主键有更新 先删除主键 避免alters中把原主键列的非空取消时与主键约束冲突
		if(change_pk){
			LinkedHashMap<String,Column> pks = src_primary.getColumns();
			LinkedHashMap<String,Column> npks = cur_primary.getColumns();
			for(String k:pks.keySet()){
				Column auto = columns.get(k.toUpperCase());
				if(null != auto && auto.isAutoIncrement() == 1){//原主键科自增
					if(!npks.containsKey(auto.getName().toUpperCase())){ //当前不是主键
						auto.setPrimaryKey(false);
						result = execute(runtime, random, DDL.TABLE_ALTER, adapter.buildDropAutoIncrement(runtime, auto)) && result;
					}
				}
			}
			//删除主键
			if(null != src_primary){
				drop(src_primary);
			}
		}
		List<Run> alters = adapter.buildAlterRun(runtime, table, cols.values());
		if(null != alters && alters.size()>0){
			result = execute(runtime, random, DDL.COLUMN_ALTER, alters) && result;
		}
		//在alters执行完成后 添加主键 避免主键中存在alerts新添加的列
		if(change_pk){
			//添加主键
			if(null != cur_primary) {
				add(cur_primary);
			}
		}
		CacheProxy.clear();
		return result;
	}

	@Override
	public boolean drop(Table meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TABLE_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	private List<Run> runs(String sql){
		List<Run> runs = new ArrayList<>();
		runs.add(new SimpleRun(sql));
		return runs;
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
		return rename(null, null, origin, name);
	}

	protected boolean rename(DataRuntime runtime, String random, Table origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.TABLE_RENAME;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		boolean result = false;
		DDL action = DDL.VIEW_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(View meta) throws Exception {
		boolean result = false;
		DDL action = DDL.VIEW_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}

	@Override
	public boolean drop(View meta) throws Exception{
		boolean result = false;
		DDL action = DDL.VIEW_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(View origin, String name) throws Exception {
		return rename(runtime(), origin, name);
	}

	protected boolean rename(DataRuntime runtime, View origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.VIEW_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		if(null == runtime){
			runtime = runtime();
		}
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(MasterTable table) throws Exception
	 * boolean alter(MasterTable table) throws Exception
	 * boolean drop(MasterTable table) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(MasterTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.MASTER_TABLE_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean alter(MasterTable table) throws Exception{
		SWITCH swt = SWITCH.CONTINUE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		if (null != ddListener) {
			//swt = ddListener.prepareAlter(runtime, random, table);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		boolean result = true;

		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		LinkedHashMap<String, Tag> tags = table.getTags();
		LinkedHashMap<String, Tag> utags = update.getTags();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();

		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, random, table, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
				result = true;
			}
		}
		// 删除列
		if(table.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
					result = true;
				}
			}
		}
		// 更新标签
		for(Tag utag : utags.values()){
			Tag tag = tags.get(utag.getName().toUpperCase());
			if(null != tag){
				// 修改列
				tag.setTable(update);
				tag.setUpdate(utag, false, false);
				alter(tag);
				result = true;
			}else{
				// 添加列
				utag.setTable(update);
				add(utag);
				result = true;
			}
		}
		// 删除标签
		if(table.isAutoDropColumn()) {
			for (Tag tag : tags.values()) {
				Tag utag = utags.get(tag.getName().toUpperCase());
				if (null == utag) {
					tag.setTable(update);
					drop(tag);
					result = true;
				}
			}
		}
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[alter master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(MasterTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.MASTER_TABLE_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(MasterTable origin, String name) throws Exception {
		return rename(runtime(), origin, name);
	}

	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.MASTER_TABLE_RENAME;
		if(null == runtime){
			runtime = runtime();
		}
		String random = random(runtime);
		origin.setNewName(name);

		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				swt = InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(PartitionTable table) throws Exception
	 * boolean alter(PartitionTable table) throws Exception
	 * boolean drop(PartitionTable table) throws Exception
	 ******************************************************************************************************************/

	@Override
	public boolean create(PartitionTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PARTITION_TABLE_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getMasterName(), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean alter(PartitionTable table) throws Exception{
		SWITCH swt = SWITCH.CONTINUE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		if (null != ddListener) {
			//swt = ddListener.prepareAlter(runtime, random, table);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		boolean result = true;

		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, random, table, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
				result = true;
			}
		}
		// 删除列
		if(table.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
					result = true;
				}
			}
		}
		if (null != ddListener) {

		}
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[alter partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(PartitionTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PARTITION_TABLE_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getMasterName(), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(PartitionTable origin, String name) throws Exception {
		return rename(null, null, origin, name);
	}

	protected boolean rename(DataRuntime runtime, String random, PartitionTable origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.PARTITION_TABLE_RENAME;
		origin.setNewName(name);
		if(null == random){
			random = random(runtime);
		}
		if(null == runtime){
			runtime = runtime();
		}
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getMasterName(), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
	public boolean add(Column meta) throws Exception{
		boolean result = false;
		DDL action = DDL.COLUMN_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Table table, Column column) throws Exception{
		return alter(table, column, true);
	}
	@Override
	public boolean alter(Column column) throws Exception{
		Table table = column.getTable(true);
		if(null == table){
			LinkedHashMap<String, Table> tables = tables(column.getCatalog(), column.getSchema(), column.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + column.getTableName(true));
				}else{
					log.error("表不存在:" + column.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, column, true);
	}
	@Override
	public boolean drop(Column meta) throws Exception{
		boolean result = false;
		DDL action = DDL.COLUMN_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * 修改列
	 * @param meta 列
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	private boolean alter(Table table, Column meta, boolean trigger) throws Exception{
		boolean result = false;
		DDL action = DDL.COLUMN_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta, false);

		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改Column执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改Column执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == SWITCH.CONTINUE) {
					result = alter(table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean rename(Column origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.COLUMN_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
	public boolean add(Tag meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TAG_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Table table, Tag tag) throws Exception{
		return alter(table, tag, true);
	}
	@Override
	public boolean alter(Tag tag) throws Exception{
		Table table = tag.getTable(true);
		if(null == table){
			List<Table> tables = tables(false, tag.getCatalog(), tag.getSchema(), tag.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + tag.getTableName(true));
				}else {
					log.error("表不存在:" + tag.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(table, tag, true);
	}
	@Override
	public boolean drop(Tag meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TAG_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * 修改标签
	 * @param meta 标签
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	private boolean alter(Table table, Tag meta, boolean trigger) throws Exception{
		boolean result = false;
		DDL action = DDL.TAG_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta, false);
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}catch (Exception e){
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
				if (swt == SWITCH.CONTINUE) {
					result = alter(table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(Tag origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.TAG_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		boolean result = false;
		DDL action = DDL.PRIMARY_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(PrimaryKey primary) throws Exception {
		Table table = primary.getTable(true);
		if(null == table){
			List<Table> tables = tables(false, primary.getCatalog(), primary.getSchema(), primary.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + primary.getTableName(true));
				}else{
					log.error("表不存在:" + primary.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(table, primary);
	}
	@Override
	public boolean alter(Table table, PrimaryKey meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PRIMARY_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}
	@Override
	public boolean drop(PrimaryKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.PRIMARY_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(PrimaryKey origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.PRIMARY_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(ForeignKey foreign) throws Exception
	 * boolean alter(ForeignKey foreign) throws Exception
	 * boolean drop(PrimaryKey foreign) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(ForeignKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.FOREIGN_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(ForeignKey meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() == 0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(table, meta);
	}
	@Override
	public boolean alter(Table table, ForeignKey meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TRIGGER_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}
	@Override
	public boolean drop(ForeignKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.FOREIGN_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(ForeignKey origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.FOREIGN_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		boolean result = false;
		DDL action = DDL.INDEX_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Index meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(table, meta);
	}
	@Override
	public boolean alter(Table table, Index index) throws Exception{
		boolean result = false;
		DDL action = DDL.INDEX_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, index);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, index);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, index);
		List<Run> runs = adapter.buildAlterRun(runtime, index);
		swt = InterceptorProxy.before(runtime, random, action, index, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, index, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, index.getTableName(true), index.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, index, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, index, runs, result, millis);
			}
		}

		return result;
	}
	@Override
	public boolean drop(Index meta) throws Exception {
		boolean result = false;
		DDL action = DDL.INDEX_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Index origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.INDEX_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Constraint constraint) throws Exception
	 * boolean alter(Constraint constraint) throws Exception
	 * boolean drop(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Constraint meta) throws Exception {
		boolean result = false;
		DDL action = DDL.CONSTRAINT_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Constraint meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(table, meta);
	}
	@Override
	public boolean alter(Table table, Constraint meta) throws Exception{
		boolean result = false;
		DDL action = DDL.CONSTRAINT_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Constraint meta) throws Exception {
		boolean result = false;
		DDL action = DDL.CONSTRAINT_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Constraint origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.CONSTRAINT_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);

		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Trigger trigger) throws Exception
	 * boolean alter(Trigger trigger) throws Exception
	 * boolean drop(Trigger trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Trigger meta) throws Exception {
		boolean result = false;
		DDL action = DDL.TRIGGER_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean alter(Trigger meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TRIGGER_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Trigger meta) throws Exception {
		boolean result = false;
		DDL action = DDL.TRIGGER_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Trigger origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.TRIGGER_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Procedure procedure) throws Exception
	 * boolean alter(Procedure procedure) throws Exception
	 * boolean drop(Procedure procedure) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Procedure meta) throws Exception {
		boolean result = false;
		DataRuntime runtime = runtime();
		DDL action = DDL.PRIMARY_ADD;
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random,  action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result,  millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean alter(Procedure meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PROCEDURE_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Procedure meta) throws Exception {
		boolean result = true;
		DataRuntime runtime = runtime();
		DDL action = DDL.PROCEDURE_DROP;
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Procedure origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.PROCEDURE_RENAME;
		DataRuntime runtime = runtime();
		origin.setNewName(name);
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null == ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, origin.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Function function) throws Exception
	 * boolean alter(Function function) throws Exception
	 * boolean drop(Function function) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Function meta) throws Exception {
		boolean result = true;
		DDL action = DDL.FUNCTION_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			swt = SWITCH.CONTINUE;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}

			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean alter(Function meta) throws Exception{
		boolean result = false;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DDL action = DDL.FUNCTION_ALTER;
		SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt =  ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Function meta) throws Exception {
		boolean result = false;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DDL action = DDL.FUNCTION_DROP;
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == SWITCH.BREAK){
			return false;
		}
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);

		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random , action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE) {
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Function origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.FUNCTION_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == SWITCH.BREAK){
			return false;
		}
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);

		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random , action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE) {
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}
	public boolean execute(DataRuntime runtime, String random, ACTION.DDL action, Run run){
		if(null == run){
			return false;
		}
		boolean result = false;
		String sql = run.getFinalUpdate();
		if(BasicUtil.isNotEmpty(sql)) {
			Long fr = System.currentTimeMillis();
			/*if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				random = random(runtime);
				log.info("{}[action:{}][ds:{}][sql:\n{}\n]", random, action, runtime.datasource(), sql);
			}*/
			//runtime.getTemplate().update(sql);
			runtime.getAdapter().update(runtime, random, null, null, run);
			result = true;
			/*if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][ds:{}][result:{}][执行耗时:{}ms]", random, action, runtime.datasource(), result, System.currentTimeMillis() - fr);
			}*/
		}
		return result;
	}
	public boolean execute(DataRuntime runtime, String random, ACTION.DDL action, List<Run> runs){
		boolean result = true;
		int idx = 0;
		for(Run run:runs){
			result = execute(runtime, random + "[index:" + idx+++"]", action, run) && result;
		}
		return result;
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

	public void checkSchema(DataRuntime runtime, Table table){
		if(null != table){
			DriverAdapter adapter = runtime.getAdapter();
			adapter.checkSchema(runtime, table);
		}
	}
	public void checkSchema(DataRuntime runtime, Column column){
		Table table = column.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
			column.setCatalog(table.getCatalog());
			column.setSchema(table.getSchema());
		}
	}
	public void checkSchema(DataRuntime runtime, Index index){
		Table table = index.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
			index.setCatalog(table.getCatalog());
			index.setSchema(table.getSchema());
		}
	}
	public void checkSchema(DataRuntime runtime, Constraint constraint){
		Table table = constraint.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
			constraint.setCatalog(table.getCatalog());
			constraint.setSchema(table.getSchema());
		}
	}
	public void checkSchema(DataRuntime runtime, Trigger trigger){
		Table table = (Table)trigger.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
		}
	}

	public void checkSchema(DataRuntime runtime, Procedure procedure){
		Table table = new Table(procedure.getCatalog(), procedure.getSchema());
		checkSchema(runtime, table);
		procedure.setCatalog(table.getCatalog());
		procedure.setSchema(table.getSchema());
	}

	public void checkSchema(DataRuntime runtime, Function function){
		Table table = new Table(function.getCatalog(), function.getSchema());
		checkSchema(runtime, table);
		function.setCatalog(table.getCatalog());
		function.setSchema(table.getSchema());
	}



	private String random(DataRuntime runtime){
		StringBuilder builder = new StringBuilder();
		builder.append("[SQL:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
				.append("][thread:")
				.append(Thread.currentThread().getId()).append("][ds:").append(runtime.datasource()).append("]");
		return builder.toString();
	}

}
