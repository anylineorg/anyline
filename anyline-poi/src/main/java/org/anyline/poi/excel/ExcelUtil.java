package org.anyline.poi.excel; 
 
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

	public static final String OFFICE_EXCEL_XLS = "xls";
	public static final String OFFICE_EXCEL_XLSX = "xlsx";
	/**
	 * 读取指定Sheet也的内容
	 * @param file file 文件
	 * @param sheet sheet序号,从0开始,如果读取全文sheetNo设置null
	 * @param rows 从第几行开始读取
	 * @return List
	 * @throws EncryptedDocumentException EncryptedDocumentException
	 * @throws InvalidFormatException InvalidFormatException
	 * @throws IOException IOException
	 */
	public static List<List<String>> read(File file, int sheet, int rows) throws EncryptedDocumentException, InvalidFormatException, IOException {
		Workbook workbook = getWorkbook(file);
		return read(workbook.getSheetAt(sheet), rows);
	}

	public static List<List<String>> read(File file, int sheet) throws EncryptedDocumentException, InvalidFormatException, IOException {
		return read(file, sheet, 0);
	}
	public static List<List<String>> read(File file) throws EncryptedDocumentException, InvalidFormatException, IOException {
		return read(file, 0, 0);
	}
	public static List<List<String>> read(File file, String sheet, int rows) throws EncryptedDocumentException, InvalidFormatException, IOException {
		Workbook workbook = getWorkbook(file);
		return read(workbook.getSheet(sheet), rows);
	}
	public static List<List<String>> read(File file, String sheet) throws EncryptedDocumentException, InvalidFormatException, IOException {
		return read(file, sheet, 0);
	}
	public static Workbook getWorkbook(File file) throws EncryptedDocumentException, InvalidFormatException, IOException {
		InputStream is = null;
		Workbook wb = null;

		String suffix = FileUtil.getSuffixFileName(file);
		if (OFFICE_EXCEL_XLS.equals(suffix) || OFFICE_EXCEL_XLSX.equals(suffix)) {
			try {
				is = new FileInputStream(file);
				wb = WorkbookFactory.create(is);
			} finally {
				if (is != null) {
					is.close();
				}
				if (wb != null) {
					wb.close();
				}
			}
		} else {
			throw new IllegalArgumentException("非Excel文件");
		}

		return wb;
	}


	private static List<List<String>> read(Sheet sheet, int rows) {
		List<List<String>> lists = new ArrayList<List<String>>();
		if(sheet != null){
			int rowNos = sheet.getLastRowNum();// 得到excel的总记录条数
			for (int i = rows; i <= rowNos; i++) {// 遍历行
				List<String> list = new ArrayList<>();
				Row row = sheet.getRow(i);
				if(row != null){
					int columNos = row.getLastCellNum();// 表头总共的列数
					for (int j = 0; j < columNos; j++) {
						Cell cell = row.getCell(j);
						if(cell != null){
							list.add(value(cell));
						}
					}
					lists.add(list);
				}
			}
		}
		return lists;
	}

	public String value(String path, String row, String col) throws Exception {
		return value(path, 0, Integer.valueOf(row), Integer.valueOf(col));
	}

	public String value(String path, String row) throws Exception {
		return value(path, 0, Integer.valueOf(row));
	}

	public String value(String path, int sheet, int row) throws Exception {
		File xlsx = new File(path);
		Workbook workbook = WorkbookFactory.create(xlsx);
		Sheet sheet1 = workbook.getSheetAt(sheet);
		Row row1 = sheet1.getRow(row);
		List<String> list = new ArrayList<>();
		int rowNum = row1.getLastCellNum();
		for (int i = 0; i < rowNum; i++) {
			list.add(value(row1.getCell(i)));
		}
		return list.toString();
	}


	public String value(String path, int sheet, int row, int col) throws Exception {
		File xlsx = new File(path);
		Workbook workbook = WorkbookFactory.create(xlsx);
		Sheet sheet1 = workbook.getSheetAt(sheet);
		Row row1 = sheet1.getRow(row);
		String value = value(row1.getCell(col));
		return value;
	}

	private static String value(Cell cell) {
		//判断是否为null或空串
		if (cell == null || cell.toString().trim().equals("")) {
			return "";
		}
		String value = "";


			switch (cell.getCellTypeEnum()) {
				case NUMERIC: // 数字
					if (HSSFDateUtil.isCellDateFormatted(cell)) {
						Date date = cell.getDateCellValue();
						DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						value = formater.format(date);
					}else{
						value = cell.getNumericCellValue() + "";
					}
					break;
				case STRING: // 字符串
					value = cell.getStringCellValue();
					break;
				case BOOLEAN: // Boolean
					value = cell.getBooleanCellValue() + "";
					break;
				case FORMULA: // 公式
					value = cell.getCellFormula() + "";
					break;
				case BLANK: // 空值
					value = "";
					break;
				case ERROR: // 故障
					value = "非法字符";
					break;
				default:
					value = "未知类型";
					break;
			}
		return value;
	}

	/** 
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param headers	表头  headers	表头
	 * @param sheet 	sheet
	 * @param rows		导出的开始位置
	 * @param keys		对应列名属性名  keys		对应列名属性名
	 * @param set		数据源  set		数据源
	 * @return return
	 */ 
	public static boolean export(File file, String sheet, int rows, List<String>headers, List<String> keys, DataSet set){
		FileOutputStream out = null;
		try{

			SXSSFWorkbook  workbook = null;
			Sheet sht = null;
			if(file.exists()){

				File tempFile = File.createTempFile(file.getName(), null);
				tempFile.delete();
				boolean renameOk = file.renameTo(tempFile);
				if(!renameOk){
					tempFile = new File(file.getParent(), "tmp_"+System.currentTimeMillis()+file.getName());
					tempFile.delete();
					renameOk = file.renameTo(tempFile);
				}
				if (!renameOk) {
					throw new Exception("重命名失败 "
							+ file.getAbsolutePath() + " > "
							+ tempFile.getAbsolutePath());
				}

				XSSFWorkbook wb = new XSSFWorkbook(tempFile);
				workbook = new SXSSFWorkbook(wb);
				if(BasicUtil.isEmpty(sheet)){
					sht = workbook.getSheetAt(0);
				}else {
					sht = workbook.getSheet(sheet);
				}
			}else {
				workbook = new SXSSFWorkbook();
				if(BasicUtil.isEmpty(sheet)){
					sheet = "sheet1";
				}
				sht = workbook.createSheet(sheet);
			}
			//表头
			if(null != headers) {
				Row row =sht.createRow(rows++);
				int c= 0 ;
				for (String header : headers) {
					Cell cell = row.createCell(c++);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(header);
				}
			}
			for(DataRow item:set){
				Row row = sht.createRow(rows++);
				int c = 0;
				for(String key:keys){
					Cell cell=row.createCell(c++);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(item.getStringNvl(key,""));
					BeanUtil.parseFinalValue(item,key);
				}
			}
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			if(!file.exists()){
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			workbook.write(out);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally {
			try{
				out.flush();
				out.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 *
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param rows 开始写入的行数
	 * @param headers 表头
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, int rows, List<String>headers, List<String> keys, DataSet set){
		return export(file, "sheet1", rows, headers, keys, set);
	}
	public static boolean export(File file, List<String> keys, DataSet set){
		return export(file,0, null, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param headers 表头
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, List<String> headers,List<String> keys, DataSet set){
		return export(file,0, headers, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param rows 从第几行开始写入
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, int rows, List<String> keys, DataSet set){
		return export(file,rows, null, keys, set);
	}

	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param sheet sheet 如果文件存在 并且为空时 则取第0个sheet
	 * @param rows 行数
	 * @param set 数据
	 * @param configs 姓名:NAME
	 * @return boolean
	 */
	public static boolean export(File file, String sheet, int rows, DataSet set, String ... configs){
		List<String> headers = new ArrayList<>();
		List<String> keys = new ArrayList<>();
		if(null != configs){
			for(String config:configs){
				String tmps[] = config.split(":");
				if(tmps.length == 2){
					headers.add(tmps[0]);
					keys.add(tmps[1]);
				}
			}
			if(headers.size() != keys.size()){
				headers = new ArrayList<>();
			}
		}
		return export(file, sheet, rows, headers, keys, set);
	}

	public static boolean export(File file, int rows, DataSet set, String ... configs){
		return export(file, null, rows, set, configs);
	}
	public static boolean export(File file,  DataSet set, String ... configs){
		return export(file, 0, set, configs);
	}
} 
