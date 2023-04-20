package org.anyline.poi.excel.io;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.poi.excel.ExcelUtil;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class ExcelReader {
	private File file = null;
	private int head = -1;		// 表头所在行
	private int data = -1;		// 数据开始行
	private int foot = 0;		// 表头行数
	private int sheet = -1;
	private String sheetName = null;
	private InputStream is;

	public static ExcelReader init(){
		return new ExcelReader();
	}
	public DataSet read(){
		DataSet set = new DataSet();
		if(null == is &&(null == file || !file.exists())){
			return set;
		}
		int fr = 0;
		if(head != -1){
			fr = head;
			if(data == -1){
				data = head + 1;
			}
		}else{
			if(data == -1){
				data = 0;
			}
			fr = data;
		}

		List<List<String>> list = null;
		if(null != is){
			if (sheet != -1) {
				list = ExcelUtil.read(is, sheet, fr, foot);
			} else {
				if (null != sheetName) {
					list = ExcelUtil.read(is, sheetName, fr, foot);
				} else {
					list = ExcelUtil.read(is, 0, fr, foot);
				}
			}

		}else {
			if (sheet != -1) {
				list = ExcelUtil.read(file, sheet, fr, foot);
			} else {
				if (null != sheetName) {
					list = ExcelUtil.read(file, sheetName, fr, foot);
				} else {
					list = ExcelUtil.read(file, 0, fr, foot);
				}
			}
		}
		if(list.size()>0) {
			if (head != -1) {
				// 取第head行作为表头
				List<String> headers = list.get(head);
				int size = headers.size();
				int rows = 0;
				for(List<String> item:list){
					if(rows ++ < (data-head)){
						continue;
					}
					DataRow row = new DataRow();
					for(int i=0; i<size; i++){
						String key = headers.get(i).trim();
						String value = item.get(i);
						row.put(key, value);
					}
					set.add(row);
				}
			}else{
				for(List<String> item:list){
					DataRow row = new DataRow();
					int size = item.size();
					for(int i=0; i<size; i++){
						String value = item.get(i);
						row.put(i+"", value);
					}
					set.add(row);
				}
			}
		}

		return set;
	}

	/**
	 * 表头所在行
	 * @param head 表头所在行
	 * @return ExcelReader
	 */
	public ExcelReader setHead(int head){
		this.head = head;
		return this;
	}

	public ExcelReader setFile(File file) {
		this.file = file;
		return this;
	}

	public ExcelReader setData(int data) {
		this.data = data;
		return this;
	}

	public ExcelReader setFoot(int foot) {
		this.foot = foot;
		return this;
	}

	public ExcelReader setSheet(int sheet) {
		this.sheet = sheet;
		return this;
	}
	public ExcelReader setSheet(String sheet) {
		this.sheetName = sheet;
		return this;
	}

	public InputStream getInputStream() {
		return this.is;
	}

	public ExcelReader setInputStream(final InputStream is) {
		this.is = is;
		return this;
	}
}
