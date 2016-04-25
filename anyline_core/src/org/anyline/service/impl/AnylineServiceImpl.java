/* 
 * Copyright 2006-2015 www.anyline.org
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


package org.anyline.service.impl;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.anyline.config.db.Procedure;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLStore;
import org.anyline.config.db.impl.PageNaviImpl;
import org.anyline.config.db.impl.ProcedureImpl;
import org.anyline.config.db.impl.SQLStoreImpl;
import org.anyline.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.config.db.sql.auto.impl.TextSQLImpl;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigStoreImpl;
import org.anyline.dao.AnylineDao;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("anylineService")
public class AnylineServiceImpl implements AnylineService {
	protected Logger LOG = Logger.getLogger(this.getClass());
	@Autowired(required = false)
	@Qualifier("anylineDao")
	protected AnylineDao dao;

	public AnylineDao getDao() {
		return dao;
	}
	
	private String createCacheElementKey(DataSource ds, String src, ConfigStore configs, String ... conditions){
		String key = "";
		key += ds+"."+src;
		if(null != configs){
			key += "."+configs.toString();
		}
		if(null != conditions){
			for(String item:conditions){
				key += "."+item;
			}
		}
		return key;
	}
	/**
	 * 按条件查询
	 * @param ds 数据源
	 * @param src 表｜视图｜函数｜自定义SQL
	 * @param configs http参数封装
	 * @param conditions 固定查询条件
	 * @return
	 */
	@Override
	public DataSet query(DataSource ds, String src, ConfigStore configs, String... conditions) {
		DataSet set = null;
		conditions = BasicUtil.compressionSpace(conditions);
//		//是否启动缓存
//		if(!ConfigTable.getBoolean("IS_USE_CACHE")){
//			set = queryFromDao(ds,src, configs, conditions);
//			return set;
//		}
//		
//		String cacaheKey = "anyline.query.cache";
//		String elementKey = createCacheElementKey(ds, src, configs, conditions);
//		Element element = null;
//        synchronized (this) {
//        	String path = ConfigTable.getWebRoot()+"/WEB-INF/classes/ehcache.xml";
//        	CacheManager manager = CacheManager.newInstance(path);
//    		Cache cache = manager.getCache(cacaheKey);
//    		if(null == cache){
//    			manager.addCache(cacaheKey);
//    			cache = manager.getCache(cacaheKey);
//    		}
//            element = cache.get(elementKey);
//            if (element == null) {
//            	LOG.info(elementKey + "加入到缓存： " + cache.getName());
//                // 调用实际 的方法
//            	set = queryFromDao(ds,src, configs, conditions);
//            	set.setService(null);
//                element = new Element(elementKey, (Serializable)set);
//                cache.put(element);
//            } else {
//            	LOG.info(elementKey + "使用缓存： " + cache.getName());
//            	set = (DataSet)element.getObjectValue();
//            }
//            set.setService(this);
//            configs.copyPageNavi(set.getNavi());
//        }
		set = queryFromDao(ds,src, configs, conditions);
        return set;
		
	}
	private DataSet queryFromDao(DataSource ds, String src, ConfigStore configs, String... conditions){
		DataSet set = null;
		try {
			SQL sql = null;
			if (RegularUtil.match(src, SQL.XML_SQL_ID_STYLE)) {
				/* XML定义 */
				sql = SQLStoreImpl.parseSQL(src);
			} else if (src.toUpperCase().trim().startsWith("SELECT")) {
				sql = new TextSQLImpl(src);
			} else {
				/* 自动生成 */
				sql = new TableSQLImpl();
				sql.setDataSource(src);
			}
			set = dao.query(ds, sql, configs, conditions);
			set.setService(this);
		} catch (Exception e) {
			set = new DataSet();
			set.setException(e);
			e.printStackTrace();
			LOG.error(e);
		}
		return set;
	}
	@Override
	public DataSet query(String src, ConfigStore configs, String... conditions) {
		return query(null, src, configs, conditions);
	}

	@Override
	public DataSet query(DataSource ds, String src, String... conditions) {
		return query(ds, src, null, conditions);
	}

	@Override
	public DataSet query(String src, String... conditions) {
		return query(null, src, null, conditions);
	}

	@Override
	public DataSet query(DataSource ds, String src, int fr, int to, String... conditions) {
		conditions = BasicUtil.compressionSpace(conditions);
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		navi.setCalType(1);
		ConfigStore configs = new ConfigStoreImpl();
		configs.setPageNavi(navi);
		return query(ds, src, configs, conditions);
	}

	@Override
	public DataSet query(String src, int fr, int to, String... conditions) {
		return query(null, src, fr, to, conditions);
	}

	@Override
	public <T> List<T> query(DataSource ds, Class<T> clazz, ConfigStore configs, String... conditions) {
		String src = BeanUtil.checkTable(clazz);
		DataSet set = query(ds, src, configs, conditions);
		return set.entity(clazz);
	}

	@Override
	public <T> List<T> query(Class<T> clazz, ConfigStore configs, String... conditions) {
		return query(null, clazz, configs, conditions);
	}

	@Override
	public <T> List<T> query(DataSource ds, Class<T> clazz, String... conditions) {
		return query(ds, clazz, null, conditions);
	}

	@Override
	public <T> List<T> query(Class<T> clazz, String... conditions) {
		return query(null, clazz, null, conditions);
	}

	@Override
	public <T> List<T> query(DataSource ds, Class<T> clazz, int fr, int to, String... conditions) {
		String src = BeanUtil.checkTable(clazz);
		DataSet set = query(ds, src, fr, to, conditions);
		return set.entity(clazz);
	}

	@Override
	public <T> List<T> query(Class<T> clazz, int fr, int to, String... conditions) {
		return query(null, clazz, fr, to, conditions);
	}

	@Override
	public DataRow queryRow(DataSource ds, String src, ConfigStore store, String... conditions) {
		conditions = BasicUtil.compressionSpace(conditions);
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(0);
		navi.setLastRow(0);
		navi.setCalType(1);
		if (null == store) {
			store = new ConfigStoreImpl();
		}
		store.setPageNavi(navi);
		DataSet set = query(ds, src, store, conditions);
		if (null != set && set.size() > 0) {
			DataRow row = set.getRow(0);
			row.setService(this);
			return row;
		}
		return null;
	}

	@Override
	public DataRow queryRow(String src, ConfigStore configs, String... conditions) {
		return queryRow(null, src, configs, conditions);
	}

	@Override
	public DataRow queryRow(DataSource ds, String src, String... conditions) {
		return queryRow(ds, src, null, conditions);
	}

	@Override
	public DataRow queryRow(String src, String... conditions) {
		return queryRow(null, src, null, conditions);
	}

	@Override
	public <T> T queryEntity(DataSource ds, Class<T> clazz, ConfigStore configs, String... conditions) {
		String src = BeanUtil.checkTable(clazz);
		DataRow row = queryRow(ds, src, configs, conditions);
		return row.entity(clazz);
	}

	@Override
	public <T> T queryEntity(Class<T> clazz, ConfigStore configs, String... conditions) {
		return queryEntity(null, clazz, configs, conditions);
	}

	@Override
	public <T> T queryEntity(DataSource ds, Class<T> clazz, String... conditions) {
		return queryEntity(ds, clazz, null, conditions);
	}

	@Override
	public <T> T queryEntity(Class<T> clazz, String... conditions) {
		return queryEntity(null, clazz, null, conditions);
	}

	// /**
	// * 检查唯一性
	// * @param src
	// * @param configs
	// * @param conditions
	// * @return
	// */
	// @Override
	// public boolean exists(DataSource ds, String src, ConfigStore configs,
	// String ... conditions){
	// DataSet set = query(ds,src, configs, conditions);
	// if(set.size() > 0){
	// return true;
	// }
	// return false;
	// }
	// @Override
	// public boolean exists(String src, ConfigStore configs, String ...
	// conditions){
	// return exists(null, src, configs, conditions);
	// }
	//
	// @Override
	// public boolean exists(DataSource ds, String src, String ... conditions){
	// return exists(ds,src, null, conditions);
	// }
	// @Override
	// public boolean exists(String src, String ... conditions){
	// return exists(null, src, conditions);
	// }
	//
	// @Override
	// public boolean exists(Object entity){
	// if(null == entity){
	// return false;
	// }
	// String src = BeanUtil.checkTable(entity.getClass());
	// String pk = BeanUtil.getPrimaryKey(entity.getClass());
	// Object pkv = BeanUtil.getValueByColumn(entity, pk);
	// return exists(src, "+"+pk+":"+pkv);
	// }
	//
	/**
	 * 更新记录
	 * 
	 * @param row
	 *            需要更新的数据
	 * @param columns
	 *            需要更新的列
	 * @return
	 */
	@Override
	public int update(boolean sync, DataSource ds, String dest, Object data, String... columns) {
		final String cols[] = BasicUtil.compressionSpace(columns);
		final DataSource _ds = ds;
		final String _dest = dest;
		final Object _data = data;
		if(sync){
			new Thread(new Runnable(){
				@Override
				public void run() {
					dao.update(_ds, _dest, _data, cols);
				}
			}).start();
			return 0;
		}else{
			return dao.update(ds, dest, data, cols);
		}
	}
	@Override
	public int update(DataSource ds, String dest, Object data, String... columns) {
		columns = BasicUtil.compressionSpace(columns);
		return dao.update(ds, dest, data, columns);
	}

	@Override
	public int update(String dest, Object data, String... columns) {
		return update(null, dest, data, columns);
	}
	@Override
	public int update(boolean sync, String dest, Object data, String... columns) {
		return update(false,null, dest, data, columns);
	}

	@Override
	public int update(DataSource ds, Object data, String... columns) {
		return update(ds, null, data, columns);
	}
	@Override
	public int update(boolean sync, DataSource ds, Object data, String... columns) {
		return update(sync, ds, null, data, columns);
	}

	@Override
	public int update(Object data, String... columns) {
		return update(null, null, data, columns);
	}
	@Override
	public int update(boolean sync, Object data, String... columns) {
		return update(sync, null, null, data, columns);
	}
	@Override
	public int save(boolean sync, DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		if(sync){
			final DataSource _ds = ds;
			final String _dest = dest;
			final Object _data = data;
			final boolean _chk = checkPrimary;
			final String[] cols = columns;
			new Thread(new Runnable(){
				@Override
				public void run() {
					save(_ds, _dest, _data, _chk, cols);
				}
				
			}).start();
			return 0;
		}else{
			return save(ds, dest, data, checkPrimary, columns);
		}
		
	}
	@Override
	public int save(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		if (null == data) {
			return 0;
		}
		if (data instanceof Collection) {
			Collection datas = (Collection) data;
			int cnt = 0;
			for (Object obj : datas) {
				cnt += save(ds, dest, obj, checkPrimary, columns);
			}
			return cnt;
		}
		return saveObject(ds, dest, data, checkPrimary, columns);
	}

	@Override
	public int save(String dest, Object data, boolean checkPrimary, String... columns) {
		return save(null, dest, data, checkPrimary, columns);
	}
	@Override
	public int save(boolean sync, String dest, Object data, boolean checkPrimary, String... columns) {
		return save(sync,null, dest, data, checkPrimary, columns);
	}

	@Override
	public int save(DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return save(ds, null, data, checkPrimary, columns);
	}
	@Override
	public int save(boolean sync, DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return save(sync, ds, null, data, checkPrimary, columns);
	}

	@Override
	public int save(Object data, boolean checkPrimary, String... columns) {
		return save(null, null, data, checkPrimary, columns);
	}
	@Override
	public int save(boolean sync, Object data, boolean checkPrimary, String... columns) {
		return save(sync, null, null, data, checkPrimary, columns);
	}

	@Override
	public int save(DataSource ds, Object data, String... columns) {
		return save(ds, null, data, false, columns);
	}

	@Override
	public int save(boolean sync, DataSource ds, Object data, String... columns) {
		return save(sync, ds, null, data, false, columns);
	}

	@Override
	public int save(String dest, Object data, String... columns) {
		return save(null, dest, data, false, columns);
	}
	@Override
	public int save(boolean sync, String dest, Object data, String... columns) {
		return save(sync, null, dest, data, false, columns);
	}

	@Override
	public int save(Object data, String... columns) {
		return save(null, null, data, false, columns);
	}
	@Override
	public int save(boolean sync, Object data, String... columns) {
		return save(sync, null, null, data, false, columns);
	}

	@Override
	public int save(DataSource ds, String dest, Object data, String... columns) {
		return save(ds, dest, data, false, columns);
	}

	@Override
	public int save(boolean sync, DataSource ds, String dest, Object data, String... columns) {
		return save(sync, ds, dest, data, false, columns);
	}

	private int saveObject(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		try {
			return dao.save(ds, dest, data, checkPrimary, columns);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public int insert(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		return insert(ds, dest, data, checkPrimary, columns);
	}

	@Override
	public int insert(String dest, Object data, boolean checkPrimary, String... columns) {
		return insert(null, dest, data, checkPrimary, columns);
	}

	@Override
	public int insert(DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return insert(ds, null, data, checkPrimary, columns);
	}

	@Override
	public int insert(Object data, boolean checkPrimary, String... columns) {
		return insert(null, null, data, checkPrimary, columns);
	}

	@Override
	public int insert(DataSource ds, Object data, String... columns) {
		return insert(ds, null, data, false, columns);
	}

	@Override
	public int insert(String dest, Object data, String... columns) {
		return insert(null, dest, data, false, columns);
	}

	@Override
	public int insert(Object data, String... columns) {
		return insert(null, null, data, false, columns);
	}

	@Override
	public int insert(DataSource ds, String dest, Object data, String... columns) {
		return insert(ds, dest, data, false, columns);
	}

	@Override
	public List<Object> executeProcedure(DataSource ds, String procedure, String... inputs) {
		Procedure proc = new ProcedureImpl();
		proc.setName(procedure);
		for (String input : inputs) {
			proc.addInput(input);
		}
		return executeProcedure(ds, proc);
	}

	@Override
	public List<Object> executeProcedure(String procedure, String... inputs) {
		return executeProcedure(null, procedure, inputs);
	}

	@Override
	public List<Object> executeProcedure(DataSource ds, Procedure procedure) {
		List<Object> result = null;
		try {
			result = dao.executeProcedure(ds, procedure);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Object> executeProcedure(Procedure procedure) {
		return executeProcedure(null, procedure);
	}

	/**
	 * 根据存储过程查询
	 * 
	 * @param procedure
	 * @param inputs
	 * @return
	 */
	@Override
	public DataSet queryProcedure(DataSource ds, Procedure procedure) {
		DataSet set = null;
		try {
			set = dao.queryProcedure(ds, procedure);
		} catch (Exception e) {
			set = new DataSet();
			set.setException(e);
			LOG.error(e);
			e.printStackTrace();
		}
		return set;
	}

	@Override
	public DataSet queryProcedure(Procedure procedure) {
		return queryProcedure(null, procedure);
	}

	@Override
	public DataSet queryProcedure(DataSource ds, String procedure, String... inputs) {
		Procedure proc = new ProcedureImpl();
		proc.setName(procedure);
		for (String input : inputs) {
			proc.addInput(input);
		}
		return queryProcedure(ds, proc);
	}

	@Override
	public DataSet queryProcedure(String procedure, String... inputs) {
		return queryProcedure(null, procedure, inputs);
	}

	@Override
	public int execute(DataSource ds, String src, ConfigStore store, String... conditions) {
		int result = -1;
		conditions = BasicUtil.compressionSpace(conditions);
		SQL sql = null;
		if (RegularUtil.match(src, SQL.XML_SQL_ID_STYLE)) {
			sql = SQLStore.parseSQL(src);
		} else {
			sql = new TextSQLImpl(src);
		}
		if (null == sql) {
			return result;
		}
		result = dao.execute(ds, sql, store, conditions);
		return result;
	}

	@Override
	public int execute(String src, ConfigStore configs, String... conditions) {
		return execute(null, src, configs, conditions);
	}

	@Override
	public int execute(DataSource ds, String src, String... conditions) {
		return execute(ds, src, null, conditions);
	}

	@Override
	public int execute(String src, String... conditions) {
		return execute(null, src, null, conditions);
	}
	@Override
	public int delete(DataSource ds, String dest, Object data, String... columns) {
		if (null == data) {
			return 0;
		}
		if (data instanceof DataRow) {
			return deleteRow(ds, dest, (DataRow) data, columns);
		}
		if (data instanceof DataSet) {
			DataSet set = (DataSet) data;
			int cnt = 0;
			int size = set.size();
			for (int i = 0; i < size; i++) {
				cnt += deleteRow(ds, dest, set.getRow(i), columns);
			}
			return cnt;
		}
		if (data instanceof Collection) {
			Collection datas = (Collection) data;
			int cnt = 0;
			for (Object obj : datas) {
				cnt += delete(ds, dest, obj, columns);
			}
			return cnt;
		}
		return deleteObject(ds, dest, data, columns);
	}

	@Override
	public int delete(DataSource ds, Object data, String... columns) {
		return delete(ds, null, data, columns);
	}

	@Override
	public int delete(String dest, Object data, String... columns) {
		return delete(null, dest, data, columns);
	}

	@Override
	public int delete(Object data, String... columns) {
		return delete(null, null, data, columns);
	}

	private int deleteObject(DataSource ds, String dest, Object data, String... columns) {
		return 0;
	}

	private int deleteRow(DataSource ds, String dest, DataRow row, String... columns) {
		try {
			dao.delete(ds, dest, row, columns);
			return 1;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}