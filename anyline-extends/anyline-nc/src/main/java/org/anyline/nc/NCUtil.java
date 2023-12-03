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


package org.anyline.nc;
 
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.encrypt.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
 
public class NCUtil {
	private static Logger log = LoggerFactory.getLogger(NCUtil.class); 
	private String file; 
	private NetcdfFile nc; 
 
	private static Hashtable<String, NCUtil> instances = new Hashtable<String, NCUtil>(); 
	 
	public static NCUtil getInstance(String file) {
		String key = MD5Util.crypto(file); 
		NCUtil util = instances.get(key); 
		if (null == util) {
			util = new NCUtil(file); 
			instances.put(key, util); 
		} 
		return util; 
	} 
	 
	public NCUtil(String file){
		this.file = file; 
	} 
	/** 
	 * 打开源文件 
	 * @return boolean
	 */ 
	public boolean open(){
		try{
			long fr = System.currentTimeMillis(); 
			nc = NetcdfFile.open(file); 
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
				log.warn("[open file][耗时:{}][file:{}]",DateUtil.conversion(System.currentTimeMillis()-fr),file); 
			} 
			return true; 
		}catch(Exception e){
			e.printStackTrace(); 
			return false; 
		} 
	} 
	/** 
	 * 释放源文件 
	 * @return boolean
	 */ 
	public boolean close(){
		try{
			nc.close(); 
			return true; 
		}catch(Exception e){
			e.printStackTrace(); 
			return false; 
		} 
	} 
	/** 
	 * 变量列表 
	 * @return List
	 */ 
	public List<Variable> getVariables(){
		return nc.getVariables(); 
	} 
	/** 
	 * 变量名称列表 
	 * @return List
	 */ 
	public List<String> getVariableNames(){
		List<String> list = new ArrayList<>();
		List<Variable> variables = getVariables(); 
		for(Variable var:variables){
			list.add(var.getFullName()); 
		} 
		return list; 
	} 
	/** 
	 * 内容概要 
	 * @return String
	 */ 
	public String info(){
		return nc.getDetailInfo(); 
	} 
	/** 
	 * 查询变量 
	 * @param var  var
	 * @return Variable
	 */ 
	public Variable findVariable(String var){
		return nc.findVariable(var); 
	} 
	/** 
	 * 查询变量值 
	 * @param var  var
	 * @return Array
	 */ 
	public Array getVariableValues(String var){
		Array array = null; 
		try {
			Variable variable = findVariable(var); 
			if(null != variable){
				array = variable.read(); 
			} 
		} catch (Exception e) {
			e.printStackTrace(); 
		} 
		return array; 
	} 
	/** 
	 * 查询变量值 
	 * @param var 变量
	 * @param origin 起点
	 * @param shape 长度 
	 * @return Array
	 */ 
	public Array getVariableValues(String var, int[] origin, int[] shape){
		Array array = null; 
		try {
			Variable variable = findVariable(var); 
			if(null != variable){
				array = variable.read(origin, shape); 
			} 
		} catch (Exception e) {
			e.printStackTrace(); 
		} 
		return array; 
	} 
 
} 
