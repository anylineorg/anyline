package org.anyline.nc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NCUtil {
	private String file;
	private NetcdfFile nc;
	
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
			array = variable.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;
	}

}
