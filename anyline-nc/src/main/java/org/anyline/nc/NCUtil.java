package org.anyline.nc;

import ucar.nc2.NetcdfFile;

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
}
