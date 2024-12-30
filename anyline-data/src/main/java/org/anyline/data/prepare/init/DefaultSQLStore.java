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

package org.anyline.data.prepare.init;

import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.SQLStore;
import org.anyline.data.prepare.text.init.DefaultTextCondition;
import org.anyline.data.prepare.text.init.DefaultTextPrepare;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.io.*;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

public class DefaultSQLStore extends SQLStore {

	private static DefaultSQLStore instance;
	private static Hashtable<String, RunPrepare> sqls = new Hashtable();
	private static final Log log = LogProxy.get(DefaultSQLStore.class);

	protected DefaultSQLStore() {
	}

	private static String root; //sql目录 多个以, 分隔 .表示项目当前目录 ${classpath}表示classes目录
	private static String[] cuts = "sql,classes".split(",");
	private static long lastLoadTime = 0;

	static {
		loadSQL();
	}
	public static synchronized void loadSQL() {
		root = ConfigTable.SQL_STORE_DIR;
		if (null == root) {
			root = "${classpath}/sql";
		}
		//${classpath} class path
		//. 项目根目录
		if(root.contains("{classpath}")) {
			root = root.replace("${classpath}", ConfigTable.getClassPath());
			root = root.replace("{classpath}", ConfigTable.getClassPath());
		}
		if(root.contains("{libpath}")) {
			root = root.replace("${libpath}", ConfigTable.getLibPath());
			root = root.replace("{libpath}", ConfigTable.getLibPath());
		}
		root = root.replace("/", FileUtil.getFileSeparator());
		root = root.replace("\\", FileUtil.getFileSeparator());
		root = root.replace("//", FileUtil.getFileSeparator());
		root = root.replace("\\\\", FileUtil.getFileSeparator());
		String[] dirs = root.split(",");
		for(String dir:dirs) {
			if(dir.startsWith(".")) {
				dir = FileUtil.merge(ConfigTable.getRoot(), dir.substring(1));
			}else if (dir.startsWith("/WEB-INF")) {
				dir = FileUtil.merge(ConfigTable.getWebRoot(), dir);
			}
			log.debug("[解析XML SQL][dir:{}]", dir);
			parse(dir);
		}
		lastLoadTime = System.currentTimeMillis();
	}
	private static synchronized void parse(String path) {
		//windows \D:\ \target\anyline-simple-data-jdbc-xml-8.6.5.jar!\BOOT-INF\classes!\sql
		//linux  /anyline-simple-data-jdbc-xml-8.7.1-SNAPSHOT.jar!/BOOT-INF/classes!/sql
		if(path.contains("jar!")) {
			//jar内部
			String separator = FileUtil.getFileSeparator();
			String sub = path.substring(path.indexOf("jar!")+4).replace("!"+separator,separator) + separator;
			sub = sub.toLowerCase();
 			//sub:  \BOOT-INF\classes!\sql\
			sub = sub.replace("!","");
			if(sub.startsWith(separator)) {
				sub = sub.substring(1);
			}
			sub = sub.replace("\\","/"); //子目录以/分隔(不区分操作系统)
			sub = sub.replace("//","/");
			try {
				File file = new File(System.getProperty("java.class.path"));
				parseJarFile(file, sub);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			parseFile(new File(path));
		}
	}

	private static synchronized void parseJarFile(File file, String sub) {
		//第一级jar
		try {
			FileInputStream fin = new FileInputStream(file);
			JarInputStream jis = new JarInputStream(fin);
			ZipEntry entry = null;
			while ((entry = jis.getNextEntry()) != null) {
				//第一级jar中的文件 过滤出xml与下一级jar
				// boot-inf/classes/mapper/hr/user.xml
				String path1 = entry.getName().toLowerCase();
				if(null != sub) {
					if(!path1.startsWith(sub)) {
						continue;
					}
				}
				if(path1.endsWith(".xml")) {
					FilterInputStream in = new FilterInputStream(jis) {
						public void close() throws IOException {}
					};
					parseXML(prefix(null, path1), FileUtil.read(in).toString());
				}else if(path1.endsWith(".jar")) {
					//第二级jar
					String name1 = FileUtil.getSimpleFileName(path1);
					JarInputStream jis2 = new JarInputStream(jis);
					ZipEntry entry2 = null;
					while ((entry2 = jis2.getNextEntry()) != null) {
						String path2 = entry2.getName();
						if(path2.endsWith("xml")) {
							FilterInputStream in2 = new FilterInputStream(jis2) {
								public void close() throws IOException {}
							};
							parseXML(prefix(name1, path2), FileUtil.read(in2).toString());
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析目录或文件或jar
	 * 遇到二级jar map中添加两个 一个带二级jar前缀 一个不带 遇到重名标记重复, 调用时抛出异常, 需要添加jar前缀才可以定位到
	 * @param file xml或目录或jar或jar/jar
	 */
	private static synchronized void parseFile(File file) {
		if (null == file) {
			return;
		}
		String name = file.getName().toLowerCase();
		if (file.isDirectory()) {
			//目录
			File[] files = file.listFiles();
			for (File item : files) {
				parseFile(item);
			}
		} else {
			//文件
			if (name.endsWith(".xml")) {
				String prefix = prefix(null, file.getPath());
				parseXML(prefix, FileUtil.read(file).toString());
			} else if (name.endsWith(".jar")) {
				parseJarFile(file, null);
			}
		}
	}

	/**
	 * 根据文件路径构造sql.id(xml, jar/xml, jar/jar/xml)
	 * @param parent 上一级jar.name
	 * @param path path或二级jar中的条目name
	 * @return String
	 */
	private static String prefix(String parent, String path) {
		String result = null;
		String prefix = path;
		if(null != root) {
			String[] dirs = root.split(",");
			for(String dir:dirs) {
				if(prefix.startsWith(dir)) {
					prefix = prefix.replace(dir, "");
				}
			}
		}
		for(String cut:cuts) {
			//分隔符 不一定与操作系统一致
			if(prefix.contains(cut + "/") || prefix.contains(cut + "\\")) {
				prefix = prefix.substring(prefix.indexOf(cut) + cut.length()+1);
			}
		}
		if(prefix.contains(".xml")) {
			prefix = prefix.substring(0, prefix.indexOf(".xml"));
		}
		prefix = prefix.replace("/",".")
				.replace("\\",".")
				.replace("src/main",".")
				.replace("src\\main",".");
		if(prefix.startsWith(".")) {
			prefix = prefix.substring(1);
		}
		result = prefix;
		if(null != parent) {
			result += "," + parent + "." + prefix;
		}
		return result;
	}
	private static List<RunPrepare> parseMyBatisXML(String prefix, Element root) {
		List<RunPrepare> result = new ArrayList<>();
		List<Element> elements = root.elements();
		for(Element element:elements) {
			String elementName = element.getName();
			if("sql".equalsIgnoreCase(elementName)
					|| "select".equalsIgnoreCase(elementName)
					|| "update".equalsIgnoreCase(elementName)
					|| "insert".equalsIgnoreCase(elementName)
					|| "delete".equalsIgnoreCase(elementName)
			) {
				String id = element.attributeValue("id");
				String sqlText = element.getText().trim();
				sqlText = RegularUtil.removeTag(sqlText);
				RunPrepare prepare = new DefaultTextPrepare();
				prepare.setText(sqlText);
				prepare.setId(id);

				if (ConfigTable.isSQLDebug()) {
					log.info("[解析SQL][id:{}]\n[text:{}]", id, sqlText);
				}
				result.add(prepare);

			}
		}
		return result;
	}
	private static List<RunPrepare> parseXML(String prefix, Element root) {
		List<RunPrepare> result = new ArrayList<>();

		// 全局条件分组
		Map<String, List<Condition>> gloableConditions = new HashMap<>();
		for (Iterator<?> itrCons = root.elementIterator("conditions"); itrCons.hasNext(); ) {
			Element conditionGroupElement = (Element) itrCons.next();
			String groupId = conditionGroupElement.attributeValue("id");
			List<Condition> conditions = new ArrayList<Condition>();
			gloableConditions.put(groupId, conditions);
			for (Iterator<?> itrParam = conditionGroupElement.elementIterator("condition"); itrParam.hasNext(); ) {
				conditions.add(parseCondition(null, null, (Element) itrParam.next()));
			}
		}
		for (Iterator<?> itrSql = root.elementIterator("sql"); itrSql.hasNext(); ) {
			Element sqlElement = (Element) itrSql.next();
			String id = sqlElement.attributeValue("id");
			boolean strict = BasicUtil.parseBoolean(sqlElement.attributeValue("strict"), false);    // 是否严格格式  true:java中不允许添加XML定义之外的临时条件
			String sqlText = sqlElement.elementText("text").trim();                                    // RunPrepare 文本
			RunPrepare prepare = new DefaultTextPrepare();
			prepare.setText(sqlText);
			prepare.setStrict(strict);
			for (Iterator<?> itrParam = sqlElement.elementIterator("condition"); itrParam.hasNext(); ) {
				parseCondition(prepare, gloableConditions, (Element) itrParam.next());
			}
			String group = sqlElement.elementText("group");
			String order = sqlElement.elementText("order");
			prepare.group(group);
			prepare.order(order);
			prepare.setId(id);
			if (ConfigTable.isSQLDebug()) {
				log.debug("[解析SQL][id:{}]\n[text:{}]", id, sqlText);
			}
			result.add(prepare);

		}
		return result;
	}

	/**
     * 解析SQL
	 * @param prefix 用来标记SQL.ID(需要多个前缀的以, 分隔)如 crm.hr.user.f1:USER_LIST  prefix = crm.hr.user.f1
	 * @param content 文件内容
	 */
	private static void parseXML(String prefix, String content) {
		List<RunPrepare> prepares = new ArrayList<>();
		Document document = createDocument(content);
		if (null == document) {
			return;
		}
		Element root = document.getRootElement();
		String rootName = root.getName();
		if("mapper".equalsIgnoreCase(rootName)) {
			prepares = parseMyBatisXML(prefix, root);
		}else{
			prepares = parseXML(prefix, root);
		}
		for(RunPrepare prepare:prepares) {
			String[] prefixs = prefix.split(",");
			for(String _prefix:prefixs) {
				String sqlId = _prefix + ":" + prepare.getId();
				prepare.setDest(sqlId);
				if(sqls.containsKey(sqlId)) {
					sqls.get(sqlId).setMultiple(true);
					log.warn("[SQL Prepare 重名][调用时请注意添加前缀][id:{}]", sqlId);
				}else {
					sqls.put(sqlId, prepare);
					log.debug("[创建SQL Prepare][id:{}]", sqlId);
				}
			}
		}
	}

	private static Condition parseCondition(RunPrepare prepare, Map<String, List<Condition>> map, Element element) {
		Condition condition = null;
		String id = element.attributeValue("id");    // 查询条件id
		Compare.EMPTY_VALUE_SWITCH swt = Compare.EMPTY_VALUE_SWITCH.IGNORE;
		String _swt = element.attributeValue("switch");
		if(BasicUtil.isNotEmpty(_swt)) {
			swt = Compare.EMPTY_VALUE_SWITCH.valueOf(_swt.toUpperCase());
		}else {
			boolean required = BasicUtil.parseBoolean(element.attributeValue("required"), false);
			boolean strictRequired = BasicUtil.parseBoolean(element.attributeValue("strictRequired"), false);
			if(strictRequired) {
				swt = Compare.EMPTY_VALUE_SWITCH.BREAK;
			}else if(required) {
				swt = Compare.EMPTY_VALUE_SWITCH.NULL;
			}
		}
		if (null != id) {
			boolean isStatic = BasicUtil.parseBoolean(element.attributeValue("static"), false);    // 是否是静态文本
			String text = element.getText().trim();            // 查询条件文本
			if (!text.toUpperCase().startsWith("AND ") && !text.toUpperCase().startsWith("$") && !text.toUpperCase().startsWith("OR ")) {
				text = "\nAND " + text;
			}
			condition = new DefaultTextCondition(id, text, isStatic);
			String test = element.attributeValue("test");
			condition.setTest(test);
			if (null != prepare) {
				prepare.addCondition(condition);
			}
		} else {
			String ref = element.attributeValue("ref");//ref对应conditions.id
			if (null != ref && null != prepare && null != map) {
				List<Condition> conditions = map.get(ref);
				if (null != conditions) {
					for (Condition c : conditions) {
						prepare.addCondition(c);
					}
				}
			}
		}
		if(null != condition) {
			condition.setSwt(swt);
		}
		return condition;
	}

	private static Document createDocument(File file) {
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			document = reader.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return document;
	}

	private static Document createDocument(InputStream is) {
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			document = reader.read(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return document;
	}
	private static Document createDocument(String content) {
		Document document = null;
		try {
			document = DocumentHelper.parseText(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return document;
	}

	public static synchronized DefaultSQLStore getInstance() {
		if (instance == null) {
			instance = new DefaultSQLStore();
		}
		return instance;
	}

	public static RunPrepare parseSQL(String id) {
		RunPrepare prepare = null;
		if (ConfigTable.getReload() > 0 && (System.currentTimeMillis() - lastLoadTime) / 1000 > ConfigTable.getReload()) {
			loadSQL();
		}
		try {
			if (ConfigTable.isSQLDebug()) {
				log.debug("[提取SQL][id:{}]", id);
			}
			log.debug("sqlId:{}", id);
			prepare = sqls.get(id);
			if (null == prepare) {
				log.error("[SQL提取失败][id:{}][所有可用sql:{}]", id, BeanUtil.concat(BeanUtil.getMapKeys(sqls)));
			}else{
				if(prepare.isMultiple()) {
					log.error("[SQL提取失败][有多个重名SQL使用完整ID调用][id:{}]", id);
				}
			}
		} catch (Exception e) {
			log.error("[SQL提取失败][id:{}]", id);
		}
		return prepare;
	}
}
