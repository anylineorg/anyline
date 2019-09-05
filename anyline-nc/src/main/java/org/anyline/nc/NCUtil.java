package org.anyline.nc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anyline.util.BeanUtil;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NCUtil {
	private String file;
	private NetcdfFile nc;
	
	public NCUtil(String file){
		this.file = file;
	}
	public boolean open(){
		try{
			nc = NetcdfFile.open(file);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public boolean close(){
		try{
			nc.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public List<Variable> getVariables(){
		return nc.getVariables();
	}
	public List<String> getVariableNames(){
		List<String> list = new ArrayList<String>();
		List<Variable> variables = getVariables();
		for(Variable var:variables){
			list.add(var.getFullName());
		}
		return list;
	}
	
}
