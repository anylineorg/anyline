package org.anyline.poi.excel;

import java.io.File;
import java.util.List;

import org.anyline.entity.DataSet;

public class ExcelUtil {
	public DataSet read(File file){
		DataSet set = null;
		return set;
	}
	public DataSet read(String file){
		return read(new File(file));
	}
	/**
	 * 
	 * @param file		文件
	 * @param sheet		sheet下标
	 * @param headers	表头
	 * @param keys		对应列名属性名
	 * @param set		数据源
	 * @return
	 */
	public boolean write(File file, int sheet, List<String>headers, List<String> keys, DataSet set){
		return false;
	}
	public boolean write(File file, int sheet, List<String>headers, DataSet set){
		return false;
	}
	public boolean write(File file, List<String>headers, DataSet set){
		return false;
	}
	public boolean write(File file, DataSet set){
		return false;
	}
}
