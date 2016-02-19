

package org.anyline.config.db;

import org.anyline.config.db.run.RunSQL;
import org.anyline.config.http.ConfigStore;

public interface SQLCreater {

	public static final String TAB = "\t";
	public static final String BR = "\n";
	public static final String BR_TAB = "\n\t";
	
	/**
	 * 创建查询SQL
	 * @param sql
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public RunSQL createQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions);
	
	public RunSQL createDeleteRunSQL(String dest, Object obj, String ... columns);
	
	public RunSQL createExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions);
	
	public String parseBaseQueryTxt(RunSQL run);
	/**
	 * 求总数SQL
	 * @param txt
	 * @return
	 */
	public String parseTotalQueryTxt(RunSQL run);
	
	
	/**
	 * 查询SQL
	 * @param baseTxt
	 * @return
	 */
	public String parseFinalQueryTxt(RunSQL run);
	public RunSQL createInsertTxt(String dest, Object obj, boolean checkPrimary, String ... columns);
	public RunSQL createUpdateTxt(String dest, Object obj, boolean checkPrimary, String ... columns);
	public String getDisKeyFr();
	public String getDisKeyTo();
	public String getDataSource(Object obj);
	public String getPrimaryKey(Object obj);
	public Object getPrimaryValue(Object obj);
}
