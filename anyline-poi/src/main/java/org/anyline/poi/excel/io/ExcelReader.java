package org.anyline.poi.excel.io; 
 
import java.io.File; 
import java.util.LinkedHashMap; 
 
import org.anyline.entity.DataSet; 
 
public class ExcelReader { 
	public LinkedHashMap<String,DataSet> read(File file){ 
		LinkedHashMap<String,DataSet> list = new LinkedHashMap<String,DataSet>(); 
		return list; 
	} 
	public static class Builder{ 
		public ExcelReader build(){ 
			return new ExcelReader(); 
		} 
	} 
} 
