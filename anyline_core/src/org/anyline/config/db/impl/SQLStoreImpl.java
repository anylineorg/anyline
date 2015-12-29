
package org.anyline.config.db.impl;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.config.db.Condition;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLStore;
import org.anyline.config.db.sql.xml.impl.XMLConditionImpl;
import org.anyline.config.db.sql.xml.impl.XMLSQLImpl;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class SQLStoreImpl extends SQLStore{

	private static SQLStoreImpl instance;
	private static Hashtable<String,SQL> sqls = new Hashtable<String,SQL>();
	private static Logger log = Logger.getLogger(SQLStoreImpl.class);

	private SQLStoreImpl() {}
	private static String sqlDir;
	static{;
		loadSQL();
	}
	public static void loadSQL(){
		sqlDir = ConfigTable.getString("SQL_STORE_DIR");
		List<File> files = FileUtil.getAllChildrenFile(new File(ConfigTable.getWebRoot(),sqlDir),"xml");
		for(File file:files){
			sqls.putAll(parseSQLFile(file));
		}
	}
	

	/**
	 * 解析sql.xml文件
	 * @param file
	 * @return
	 */
	private static Hashtable<String,SQL> parseSQLFile(File file){
		Hashtable<String,SQL> result = new Hashtable<String,SQL>();
		String fileName = file.getPath();
		String dirName = new File(ConfigTable.getWebRoot(), sqlDir).getPath()+FileUtil.getFileSeparator();
		fileName = fileName.replace(dirName, "").replace(".xml", "").replace(FileUtil.getFileSeparator(), ".");
		
		Document document = createDocument(file);
		if(null == document) return result;
		Element root = document.getRootElement();
		//条件分组
		Map<String,List<Condition>> conditionMap = new HashMap<String,List<Condition>>();
		for(Iterator<?> itrSql = root.elementIterator("conditions"); itrSql.hasNext();){
			Element conditionGroupElement = (Element)itrSql.next();
			String groupId = conditionGroupElement.attributeValue("id");
			List<Condition> conditions = new ArrayList<Condition>();
			conditionMap.put(groupId, conditions);
			for(Iterator<?> itrParam = conditionGroupElement.elementIterator("condition"); itrParam.hasNext();){
				Element conditionElement = (Element)itrParam.next();
				String paramId = conditionElement.attributeValue("id");	//参数主键
				boolean isStatic = BasicUtil.parseBoolean(conditionElement.attributeValue("static"),false);	//是否是静态文本
				String paramText = " AND " + conditionElement.getText();			//参数文本
				Condition condition = new XMLConditionImpl(paramId, paramText, isStatic);
				conditions.add(condition);
			}
		}
		for(Iterator<?> itrSql = root.elementIterator("sql"); itrSql.hasNext();){
			Element sqlElement = (Element)itrSql.next();
			String sqlId = fileName +":" +sqlElement.attributeValue("id");			//SQL 主键
			String sqlText = sqlElement.elementText("text");						//SQL 文本
			SQL sql = new XMLSQLImpl();
			sql.setDataSource(fileName+":"+sqlId);
			sql.setText(sqlText);
			for(Iterator<?> itrParam = sqlElement.elementIterator("condition"); itrParam.hasNext();){
				Element conditionElement = (Element)itrParam.next();
				String paramId = conditionElement.attributeValue("id");	//参数主键
				if(null != paramId){
					boolean isStatic = BasicUtil.parseBoolean(conditionElement.attributeValue("static"),false);	//是否是静态文本
					String paramText = " AND " + conditionElement.getText();			//参数文本
					Condition condition = new XMLConditionImpl(paramId, paramText, isStatic);
					sql.addCondition(condition);
				}
				//引用全局条件
				String ref = conditionElement.attributeValue("ref");
				if(null != ref){
					List<Condition> conditions = conditionMap.get(ref);
					if(null != conditions){
						for(Condition c:conditions){
							sql.addCondition(c);
						}
					}
				}
			}
			String group = sqlElement.elementText("group");
			String order = sqlElement.elementText("order");
			sql.group(group);
			sql.order(order);
			
			result.put(sqlId, sql);
		}
		return result;
	}
	private static Document createDocument(File file){
		Document document = null;
		try{
			SAXReader reader = new SAXReader();
			document = reader.read(file);
		}catch(Exception e){
			log.error(e);
			log.error(e);
		}
		return document;
	}
	public static synchronized SQLStoreImpl getInstance() {
		if (instance == null) {
			instance = new SQLStoreImpl();
		}
		return instance;
	}

	public static SQL parseSQL(String id){
		SQL sql = null;
		try{
			sql = sqls.get(id);
		}catch(Exception e){
			log.error("SQL提取失败:"+id);
			log.error(e);
		} 
		return sql;
	}
}