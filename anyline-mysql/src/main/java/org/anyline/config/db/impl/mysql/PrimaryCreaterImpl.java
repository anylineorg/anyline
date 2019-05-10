
package org.anyline.config.db.impl.mysql;

import java.util.List;

import org.anyline.config.db.Procedure;
import org.anyline.config.db.SQLCreater.DB_TYPE;
import org.anyline.config.db.impl.ProcedureImpl;
import org.anyline.dao.AnylineDao;
import org.anyline.dao.PrimaryCreater;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
@Repository("mysql.primaryCreaterImpl")
public class PrimaryCreaterImpl implements PrimaryCreater {

	@Autowired(required=false)
	@Qualifier("anylineDao")
	private AnylineDao dao;

	public DB_TYPE type(){
		return DB_TYPE.MYSQL;
	}
	public synchronized Object createPrimary(String table, String column, String other) {
		String primary = null;
		if(null == column){
			column = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
		}
		if(null == column){
			column = "ID";
		}
		if(null == table || null == column){
			return null;
		}
		Procedure proc = new ProcedureImpl();

		proc.setName(ConfigTable.getString("CREATE_PRIMARY_KEY_PROCEDURE"));
		proc.addInput(table);
		proc.addInput(column);
		proc.addInput(other);
		proc.regOutput();
		try{

			boolean result = dao.executeProcedure(proc);
			if(result){
				List<Object> list = proc.getResult();
				if(null != list && list.size()>0){
					primary = list.get(0).toString();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(null == primary || "-1".equals(primary) || "null".equalsIgnoreCase(primary)){
			primary = BasicUtil.getRandomUpperString(10);
		}
		return primary;
	}

}
