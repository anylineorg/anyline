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
 *
 *          
 */


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
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class SQLStoreImpl extends SQLStore{ 
 
	private static SQLStoreImpl instance; 
	private static Hashtable<String,SQL> sqls = new Hashtable<String,SQL>(); 
	private static final Logger log = LoggerFactory.getLogger(SQLStoreImpl.class); 
	protected SQLStoreImpl() {} 
	private static String sqlDir;
	private static long lastLoadTime = 0;
	static{ 
		loadSQL(); 
	} 
	public static synchronized void loadSQL(){ 
		sqlDir = ConfigTable.getString("SQL_STORE_DIR"); 
		List<File> files = FileUtil.getAllChildrenFile(new File(ConfigTable.getWebRoot(),sqlDir),"xml"); 
		for(File file:files){
			if(ConfigTable.isSQLDebug()){
				log.warn("[解析SQL] [FILE:{}]",file.getAbsolutePath());
			} 
			sqls.putAll(parseSQLFile(file)); 
		}
		lastLoadTime = System.currentTimeMillis(); 
	} 
	 
 
	/** 
	 * 解析sql.xml文件 
	 * @param file  file
	 * @return return
	 */ 
	private static Hashtable<String,SQL> parseSQLFile(File file){
		Hashtable<String,SQL> result = new Hashtable<String,SQL>(); 
		String fileName = file.getPath(); 
		String dirName = new File(ConfigTable.getWebRoot(), sqlDir).getPath()+FileUtil.getFileSeparator(); 
		fileName = fileName.replace(dirName, "").replace(".xml", "").replace(FileUtil.getFileSeparator(), "."); 
		 
		Document document = createDocument(file); 
		if(null == document) {
			return result;
		} 
		Element root = document.getRootElement(); 
		//全局条件分组 
		Map<String,List<Condition>> gloableConditions = new HashMap<String,List<Condition>>(); 
		for(Iterator<?> itrCons = root.elementIterator("conditions"); itrCons.hasNext();){ 
			Element conditionGroupElement = (Element)itrCons.next(); 
			String groupId = conditionGroupElement.attributeValue("id"); 
			List<Condition> conditions = new ArrayList<Condition>(); 
			gloableConditions.put(groupId, conditions); 
			for(Iterator<?> itrParam = conditionGroupElement.elementIterator("condition"); itrParam.hasNext();){ 
				conditions.add(parseCondition(null,null,(Element)itrParam.next())); 
			} 
		} 
		for(Iterator<?> itrSql = root.elementIterator("sql"); itrSql.hasNext();){ 
			Element sqlElement = (Element)itrSql.next(); 
			String sqlId = fileName +":" +sqlElement.attributeValue("id");						//SQL 主键
			boolean strict = BasicUtil.parseBoolean(sqlElement.attributeValue("strict"), false);	//是否严格格式  true:java中不允许添加XML定义之外的临时条件
			String sqlText = sqlElement.elementText("text");									//SQL 文本 
			SQL sql = new XMLSQLImpl(); 
			sql.setDataSource(fileName+":"+sqlId); 
			sql.setText(sqlText);
			sql.setStrict(strict); 
			for(Iterator<?> itrParam = sqlElement.elementIterator("condition"); itrParam.hasNext();){ 
				parseCondition(sql,gloableConditions,(Element)itrParam.next());
			} 
			String group = sqlElement.elementText("group"); 
			String order = sqlElement.elementText("order"); 
			sql.group(group); 
			sql.order(order);
			if(ConfigTable.isSQLDebug()){
				log.warn("[解析SQL][id:{}]\n[text:{}]",sqlId, sqlText);
			}
			result.put(sqlId, sql); 
		}
		return result; 
	}
	private static Condition parseCondition(SQL sql, Map<String,List<Condition>> map, Element element){
		Condition condition = null;
		String id = element.attributeValue("id");	//查询条件id
		boolean required = BasicUtil.parseBoolean(element.attributeValue("required"), false);
		boolean strictRequired = BasicUtil.parseBoolean(element.attributeValue("strictRequired"), false);
		if(null != id){
			boolean isStatic = BasicUtil.parseBoolean(element.attributeValue("static"),false);	//是否是静态文本
			String text = element.getText().trim();			//查询条件文本
			if(!text.toUpperCase().startsWith("AND")){
				text =  "\nAND " + text;
			}
			condition = new XMLConditionImpl(id, text, isStatic);
			condition.setRequired(required);
			condition.setStrictRequired(strictRequired);
			String test = element.attributeValue("test");
			condition.setTest(test);
			if(null != sql){
				sql.addCondition(condition);
			}
		}else{
			String ref = element.attributeValue("ref");//ref对应conditions.id
			if(null != ref && null != sql && null != map){
				List<Condition> conditions = map.get(ref);
				if(null != conditions){
					for(Condition c:conditions){
						sql.addCondition(c);
					}
				}
			}
		}
		return condition;
	} 
	private static Document createDocument(File file){ 
		Document document = null; 
		try{ 
			SAXReader reader = new SAXReader(); 
			document = reader.read(file); 
		}catch(Exception e){ 
			e.printStackTrace(); 
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
		if(ConfigTable.getReload()>0 && (System.currentTimeMillis()-lastLoadTime)/1000 > ConfigTable.getReload()){
			loadSQL();
		} 
		try{
			if(ConfigTable.isSQLDebug()){
				log.warn("[提取SQL][id:{}]",id);
			} 
			sql = sqls.get(id); 
		}catch(Exception e){ 
			log.error("[SQL提取失败][id:{}]",id); 
		}  
		return sql; 
	} 
}
