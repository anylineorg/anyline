
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
	private Logger log = Logger.getLogger(this.getClass());
	@Autowired(required=false)
	@Qualifier("anylineDao")
	protected AnylineDao dao;
	
	public AnylineDao getDao(){
		return dao;
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
		try {
			SQL sql = null;
			if (RegularUtil.match(src, SQL.XML_SQL_ID_STYLE)) {
				/* XML定义 */
				sql = SQLStoreImpl.parseSQL(src);
			}else if(src.toUpperCase().trim().startsWith("SELECT")){
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
			log.error(e);
		}
		return set;
	}

	@Override
	public DataSet query(String src, ConfigStore configs, String... conditions) {
		return query(null, src, configs, conditions);
	}
	 @Override
	 public DataSet query(DataSource ds, String src, String ... conditions){
	 return query(ds, src, null, conditions);
	 }
	 @Override
	 public DataSet query(String src, String ... conditions){
	 return query(null, src, null, conditions);
	 }

	 @Override
	 public DataSet query(DataSource ds, String src, int fr, int to, String ... conditions){
		 conditions = BasicUtil.compressionSpace(conditions);
		 PageNaviImpl navi = new PageNaviImpl();
		 navi.setFirstRow(fr);
		 navi.setLastRow(to);
		 navi.setCalType(1);
		 ConfigStore configs = new ConfigStoreImpl();
		 configs.setPageNavi(navi);
		 return query(ds,src, configs, conditions);
	 }
	 @Override
	 public DataSet query(String src, int fr, int to, String ... conditions){
		 return query(null, src, fr, to, conditions);
	 }
	 
	 @Override
	 public <T> List<T> query(DataSource ds, Class<T> clazz, ConfigStore configs, String ... conditions){
		 String src = BeanUtil.checkTable(clazz);
		 DataSet set = query(ds, src, configs, conditions);
		 return set.entity(clazz);
	 }
	 @Override
	 public <T> List<T> query(Class<T> clazz, ConfigStore configs, String ... conditions){
		 return query(null, clazz, configs, conditions);
	 }
	 @Override
	 public <T> List<T> query(DataSource ds, Class<T> clazz, String ... conditions){
		 return query(ds, clazz, null, conditions);
	 }
	 @Override
	 public <T> List<T> query(Class<T> clazz, String ... conditions){
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
		navi.setLastRow(1);
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
	 public <T> T queryEntity(DataSource ds, Class<T> clazz, ConfigStore configs, String ... conditions){
		 String src = BeanUtil.checkTable(clazz);
		 DataRow row = queryRow(ds, src, configs, conditions);
		 return row.entity(clazz);
	 }
	 @Override
	 public <T> T queryEntity(Class<T> clazz, ConfigStore configs, String ... conditions){
		 return queryEntity(null, clazz, configs, conditions);
	 }
	 @Override
	 public <T> T queryEntity(DataSource ds, Class<T> clazz, String ... conditions){
		 return queryEntity(ds, clazz, null, conditions);
	 }
	 @Override
	 public <T> T queryEntity(Class<T> clazz, String ... conditions){
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
	 * @param row 需要更新的数据
	 * @param columns 需要更新的列
	 * @return
	 */
	 @Override
	 public int update(DataSource ds, String dest, Object data, String ... columns){
		 columns = BasicUtil.compressionSpace(columns);
		 return dao.update(ds,dest, data, columns);
	 }
	 @Override
	 public int update(String dest, Object data, String ... columns){
		 return update(null, dest, data, columns);
	 }
	 @Override
	 public int update(DataSource ds, Object data, String ... columns){
		 return update(ds,null, data, columns);
	 }
	 @Override
	 public int update(Object data, String ... columns){
		 return update(null,null, data, columns);
	 }
	 @Override
	 public int save(DataSource ds, String dest, Object data, boolean checkPrimary, String ... columns){
		 if(null == data){
			 return 0;
		 }
		 if(data instanceof Collection){
			 Collection datas = (Collection)data;
			 int cnt = 0;
			 for(Object obj:datas){
				 cnt += save(ds, dest, obj, checkPrimary, columns);
			 }
			 return cnt;
		 }
		 return saveObject(ds, dest, data, checkPrimary, columns);
	 }
	 @Override
	 public int save(String dest, Object data, boolean checkPrimary, String ... columns){
		 return save(null, dest, data, checkPrimary, columns);
	 }
	 @Override
	 public int save(DataSource ds, Object data, boolean checkPrimary, String ... columns){
		 return save(ds,null, data, checkPrimary, columns);
	 }
	 @Override
	 public int save(Object data, boolean checkPrimary, String ... columns){
		 return save(null,null, data, checkPrimary, columns);
	 }
	 @Override
	 public int save(DataSource ds, Object data, String ... columns){
		 return save(ds,null, data, false, columns);
	 }
	 @Override
	 public int save(String dest, Object data, String ... columns){
		 return save(null, dest, data, false, columns);
	 }
	 @Override
	 public int save(Object data, String ... columns){
		 return save(null, null, data, false, columns);
	 }
	 @Override
	 public int save(DataSource ds, String dest, Object data, String ... columns){
		 return save(ds, dest, data, false, columns);
	 }
	 
	 
	 private int saveObject(DataSource ds, String dest, Object data, boolean checkPrimary, String ... columns){
		 try{
			return dao.save(ds, dest, data, checkPrimary, columns);
		 }catch(Exception e){
			 e.printStackTrace();
			 throw new RuntimeException(e);
		 }
	 }

	 @Override
	 public int insert(DataSource ds, String dest, Object data, boolean checkPrimary, String ... columns){
		 return insert(ds, dest, data, checkPrimary, columns);
	 }
	 @Override
	 public int insert(String dest, Object data, boolean checkPrimary, String ... columns){
		 return insert(null, dest, data, checkPrimary, columns);
	 }
	 @Override
	 public int insert(DataSource ds, Object data, boolean checkPrimary, String ... columns){
		 return insert(ds,null, data, checkPrimary, columns);
	 }
	 @Override
	 public int insert(Object data, boolean checkPrimary, String ... columns){
		 return insert(null,null, data, checkPrimary, columns);
	 }
	 @Override
	 public int insert(DataSource ds, Object data, String ... columns){
		 return insert(ds,null, data, false, columns);
	 }
	 @Override
	 public int insert(String dest, Object data, String ... columns){
		 return insert(null, dest, data, false, columns);
	 }
	 @Override
	 public int insert(Object data, String ... columns){
		 return insert(null, null, data, false, columns);
	 }
	 @Override
	 public int insert(DataSource ds, String dest, Object data, String ... columns){
		 return insert(ds, dest, data, false, columns);
	 }
	 


	 @Override
	 public List<Object> executeProcedure(DataSource ds, String procedure, String... inputs){
		 Procedure proc = new ProcedureImpl();
		 proc.setName(procedure);
		 for(String input : inputs){
			 proc.addInput(input);
		 }
	 return executeProcedure(ds, proc);
	 }
	 @Override
	 public List<Object> executeProcedure(String procedure, String... inputs){
		 return executeProcedure(null, procedure, inputs);
	 }
	
	 @Override
	 public List<Object> executeProcedure(DataSource ds, Procedure procedure) {
		 List<Object> result = null;
		 try{
			 result = dao.executeProcedure(ds,procedure);
		 }catch(Exception e){
		 log.error(e);
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
	 * @param procedure
	 * @param inputs
	 * @return
	 */
	 @Override
	 public DataSet queryProcedure(DataSource ds, Procedure procedure){
	 DataSet set = null;
	 try{
	 set = dao.queryProcedure(ds,procedure);
	 }catch(Exception e){
	 set = new DataSet();
	 set.setException(e);
	 log.error(e);
	 e.printStackTrace();
	 }
	 return set;
	 }
	 @Override
	 public DataSet queryProcedure(Procedure procedure){
	 return queryProcedure(null, procedure);
	 }
	 @Override
	 public DataSet queryProcedure(DataSource ds, String procedure, String ...
	 inputs){
	 Procedure proc = new ProcedureImpl();
	 proc.setName(procedure);
	 for(String input : inputs){
	 proc.addInput(input);
	 }
	 return queryProcedure(ds,proc);
	 }
	
	 @Override
	 public DataSet queryProcedure(String procedure, String ... inputs){
	 return queryProcedure(null, procedure, inputs);
	 }
	 @Override
	 public int execute(DataSource ds, String src, ConfigStore store, String... conditions) {
		 int result = -1;
		 conditions = BasicUtil.compressionSpace(conditions);
		 SQL sql = null;
		 if(RegularUtil.match(src, SQL.XML_SQL_ID_STYLE)){
			 sql = SQLStore.parseSQL(src);
		 }else{
			 sql = new TextSQLImpl(src);
		 }
		 if(null == sql){
			 return result;
		 }
		 result = dao.execute(ds, sql, store, conditions);
		 return result;
	 }
	 @Override
	 public int execute(String src, ConfigStore configs, String ... conditions){
		 return execute(null, src, configs, conditions);
	 }
	 @Override
	 public int execute(DataSource ds, String src, String ... conditions){
		 return execute(ds, src, null, conditions);
	 }
	 @Override
	 public int execute(String src, String ... conditions){
		 return execute(null, src, null, conditions);
	 }
	// @Override
	// public boolean delete(DataSource ds, DataRow row, String... cols) {
	// if(null == cols || cols.length == 0){
	// return delete(row, "CD");
	// }
	// try{
	// String sql = "DELETE FROM " + row.getDataSource() + " WHERE 1=1 ";
	// List<Object> values = new ArrayList<Object>();
	// for(String col:cols){
	// sql += "\n AND `" + col + "` = ?";
	// values.add(row.get(col));
	// }
	// return dao.execute(ds,sql,values);
	// }catch(Exception e){
	// e.printStackTrace();
	// return false;
	// }
	// }
	// @Override
	// public boolean delete(DataRow row, String... cols) {
	// return delete(null, row, cols);
	// }
	// @Override
	// public boolean delete(DataSource ds, DataSet set, String... cols) {
	// if(null == cols || cols.length == 0){
	// return delete(set, "CD");
	// }
	// boolean result = true;
	// int size = set.size();
	// for(int i=0; i<size; i++){
	// DataRow row = set.getRow(i);
	// result = delete(ds,row,cols) && result;
	// }
	// return result;
	// }
	// @Override
	// public boolean delete(DataSet set, String... cols) {
	// return delete(null, set, cols);
	// }
	// @Override
	// public boolean delete(DataSource ds, BasicEntity entity, String...
	// fields) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	// @Override
	// public boolean delete(BasicEntity entity, String... fields) {
	// // TODO Auto-generated method stub
	// return delete(null, entity, fields);
	// }
	// @Override
	// public boolean delete(DataSource ds, String table, List<String> cds) {
	// if(null == cds || cds.size() == 0 || BasicUtil.isEmpty(table)){
	// return false;
	// }
	// boolean result = false;
	// try{
	// table = parseKey(table);
	// StringBuilder sql = new StringBuilder();
	// List<Object> values = new ArrayList<Object>();
	// sql.append("DELETE FROM ").append(table).append(" WHERE 1=1 ");
	// for(int i=0;i<cds.size(); i++){
	// sql.append(" AND CD=?");
	// }
	// values.addAll(cds);
	// result = dao.execute(ds,sql.toString(), values);
	// }catch(Exception e){
	// result = false;
	// }
	// return result;
	// }
	// @Override
	// public boolean delete(String table, List<String> cds) {
	// return delete(null, table, cds);
	// }
	// @Override
	// public boolean delete(DataSource ds, String table, String... cds) {
	// if(null == cds || cds.length == 0 || BasicUtil.isEmpty(table)){
	// return false;
	// }
	// boolean result = false;
	// try{
	// table = parseKey(table);
	// StringBuilder sql = new StringBuilder();
	// sql.append("DELETE FROM ").append(table).append(" WHERE 1=1 ");
	// List<Object> values = new ArrayList<Object>();
	// for(int i=0;i<cds.length; i++){
	// sql.append(" AND `CD`=?");
	// values.add(cds[i]);
	// }
	// result = dao.execute(ds,sql.toString(), values);
	// }catch(Exception e){
	// result = false;
	// }
	// return result;
	// }
	// @Override
	// public boolean delete(String table, String... cds) {
	// return delete(null, table, cds);
	// }
	// private String parseKey(String src){
	// return src;
	// }
	//
	 @Override
	 public int delete(DataSource ds, String dest, Object data, String ... columns){
		 if(null == data){
			 return 0;
		 }
		 if(data instanceof DataRow){
			 return deleteRow(ds, dest, (DataRow)data, columns);
		 }
		 if(data instanceof DataSet){
			 DataSet set = (DataSet)data;
			 int cnt = 0;
			 int size = set.size();
			 for(int i=0; i<size; i++){
				 cnt += deleteRow(ds, dest, set.getRow(i), columns);
			 }
			 return cnt;
		 }
		 if(data instanceof Collection){
			 Collection datas = (Collection)data;
			 int cnt = 0;
			 for(Object obj:datas){
				 cnt += delete(ds, dest, obj, columns);
			 }
			 return cnt;
		 }
		 return deleteObject(ds, dest, data, columns);
	 }

	 @Override
	 public int delete(DataSource ds, Object data, String ... columns){
		 return delete(ds, null, data, columns);
	 }

	 @Override
	 public int delete(String dest, Object data, String ... columns){
		 return delete(null, dest, data, columns);
	 }

	 @Override
	 public int delete(Object data,  String ... columns){
		 return delete(null, null, data, columns);
	 }

	 private int deleteObject(DataSource ds, String dest, Object data,  String ... columns){
		 return 0;
	 }
	 private int deleteRow(DataSource ds, String dest, DataRow row,  String ... columns){
		 try{
			 dao.delete(ds, dest, row, columns);
			 return 1;
		 }catch(Exception e){
			 throw new RuntimeException(e);
		 }
	 }

}