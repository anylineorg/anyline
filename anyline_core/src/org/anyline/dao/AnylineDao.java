
package org.anyline.dao;

import java.util.List;

import javax.sql.DataSource;

import org.anyline.config.db.Procedure;
import org.anyline.config.db.SQL;
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.DataSet;

public interface AnylineDao {
	/**
	 * 查询
	 * @param sql
	 * @return
	 */
	public DataSet query(DataSource ds, SQL sql, ConfigStore configs, String ... conditions);
	public DataSet query(SQL sql, ConfigStore configs, String ... conditions);
	public DataSet query(DataSource ds, SQL sql, String ... conditions);
	public DataSet query(SQL sql, String ... conditions);
	/**
	 * 更新
	 * @param	row
	 * 			需要更新的数据
	 * @param	columns
	 * 			需要更新的列
	 * @param	dst
	 * 			表
	 * @return
	 */
	public int update(DataSource ds, String dst, Object data, String ... columns);
	public int update(String dst, Object data, String ... columns);
	public int update(DataSource ds, Object data, String ... columns);
	public int update(Object data, String ... columns);
	
	/**
	 * 添加
	 * @param data
	 * 			需要插入的数据
	 * @param checkParimary
	 * 			是否需要检查重复主键,默认不检查
	 * @param columns
	 * 			需要插入的列
	 * @param dst
	 * 			表
	 * @return
	 */
	public int insert(DataSource ds, String dst, Object data, boolean checkPrimary, String ... columns);
	public int insert(String dst, Object data, boolean checkPrimary, String ... columns);
	public int insert(DataSource ds, Object data, boolean checkPrimary, String ... columns);
	public int insert(Object data, boolean checkPrimary, String ... columns);
	public int insert(DataSource ds, String dst, Object data, String ... columns);
	public int insert(String dst, Object data, String ... columns);
	public int insert(DataSource ds, Object data, String ... columns);
	public int insert(Object data, String ... columns);
	/**
	 * 保存(insert|update)
	 * @param data
	 * @param checkPrimary 是否检查主键
	 * @param columns
	 * @return
	 */
	public int save(DataSource ds, String dst, Object data, boolean checkPrimary, String ... columns);
	public int save(String dst, Object data, boolean checkPrimary, String ... columns);
	public int save(DataSource ds, Object data, boolean checkPrimary, String ... columns);
	public int save(Object data, boolean checkPrimary, String ... columns);
	public int save(DataSource ds, String dst, Object data, String ... columns);
	public int save(String dst, Object data, String ... columns);
	public int save(DataSource ds, Object data, String ... columns);
	public int save(Object data, String ... columns);


	public int execute(DataSource ds, SQL sql, ConfigStore configs, String ... conditions);
	public int execute(DataSource ds, SQL sql, String ... conditions);
	
	public int execute(SQL sql, ConfigStore configs, String ... conditions);
	public int execute(SQL sql, String ... conditions);
	


	/**
	 * 执行存储过程
	 * @param procedure
	 * @param inputs
	 * @param outputs
	 * @return
	 */
	public List<Object> executeProcedure(DataSource ds, Procedure procedure);
	public List<Object> executeProcedure(Procedure procedure);
//	/**
//	 * 根据存储过程查询
//	 * @param procedure
//	 * @param inputs
//	 * @return
//	 */
	public DataSet queryProcedure(DataSource ds, Procedure procedure);
	public DataSet queryProcedure(Procedure procedure);
	public int delete(DataSource ds, String dest, Object data, String ... columns);
}
