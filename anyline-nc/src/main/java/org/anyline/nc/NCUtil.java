package org.anyline.nc;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.anyline.util.MD5Util;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NCUtil {
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
	 * @return
	 */
	public boolean open(){
		try{
			nc = NetcdfFile.open(file);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 释放源文件
	 * @return
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
	 * @return
	 */
	public List<Variable> getVariables(){
		return nc.getVariables();
	}
	/**
	 * 变量名称列表
	 * @return
	 */
	public List<String> getVariableNames(){
		List<String> list = new ArrayList<String>();
		List<Variable> variables = getVariables();
		for(Variable var:variables){
			list.add(var.getFullName());
		}
		return list;
	}
	/**
	 * 内容概要
	 * @return
	 */
	public String info(){
		return nc.getDetailInfo();
	}
	/**
	 * 查询变量
	 * @param var
	 * @return
	 */
	public Variable findVariable(String var){
		return nc.findVariable(var);
	}
	/**
	 * 查询变量值
	 * @param var
	 * @return
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
	 * @param origin 起点
	 * @param shape 长度
	 * @return
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
