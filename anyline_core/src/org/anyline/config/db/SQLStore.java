
package org.anyline.config.db;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.config.db.Condition;
import org.anyline.config.db.SQL;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public abstract class SQLStore{

	public static void loadSQL(){}

	/**
	 * 解析sql.xml文件
	 * @param file
	 * @return
	 */
	public static synchronized SQLStore getInstance() {return null; }
	public static SQL parseSQL(String id){return null;}
}