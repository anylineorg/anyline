/*
 * Copyright 2006-2022 www.anyline.org
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
 *
 *
 */
package org.anyline.poi.excel;

import org.anyline.entity.html.Table;
import org.anyline.entity.html.Td;
import org.anyline.entity.html.Tr;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ExcelUtil {

	public static final String OFFICE_EXCEL_XLS = "xls";
	public static final String OFFICE_EXCEL_XLSX = "xlsx";
	/**
	 * 读取指定Sheet也的内容
	 * @param file file 文件
	 * @param sheet sheet序号,从0开始
	 * @param rows 从第几行开始读取
	 * @return List
	 *
	 */
	public static List<List<String>> read(File file, int sheet, int rows) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(file);
			list = read(workbook.getSheetAt(sheet), rows);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 读取指定Sheet也的内容
	 * @param is 输入流
	 * @param sheet sheet序号,从0开始
	 * @param rows 从第几行开始读取
	 * @return List
	 *
	 */
	public static List<List<String>> read(InputStream is, int sheet, int rows) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(is);
			list = read(workbook.getSheetAt(sheet), rows);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 读取指定Sheet也的内容
	 * @param file file 文件
	 * @param sheet sheet序号,从0开始
	 * @return List
	 *
	 */
	public static List<List<String>> read(File file, int sheet) {
		return read(file, sheet, 0);
	}
	public static List<List<String>> read(InputStream is, int sheet) {
		return read(is, sheet, 0);
	}

	/**
	 * 读取excel
	 * @param file 文件 第0个sheet第0行开始读取
	 * @return list
	 */
	public static List<List<String>> read(File file){
		return read(file, 0, 0);
	}
	public static List<List<String>> read(InputStream is){
		return read(is, 0, 0);
	}

	/**
	 * 读取excel
	 * @param file 文件
	 * @param sheet sheet
	 * @param rows 从rows行开始读取
	 * @return list
	 */
	public static List<List<String>> read(File file, String sheet, int rows) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(file);
			list = read(workbook.getSheet(sheet), rows);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public static List<List<String>> read(InputStream is, String sheet, int rows) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(is);
			list = read(workbook.getSheet(sheet), rows);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 读取excel
	 * @param file 文件
	 * @param sheet sheet
	 * @return list
	 */
	public static List<List<String>> read(File file, String sheet) {
		return read(file, sheet, 0);
	}

	public static List<List<String>> read(InputStream is, String sheet) {
		return read(is, sheet, 0);
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


	public static Workbook getWorkbook(InputStream is) throws EncryptedDocumentException, InvalidFormatException, IOException {
		Workbook wb = null;
			try {
				wb = WorkbookFactory.create(is);
			} finally {
				if (is != null) {
					is.close();
				}
				if (wb != null) {
					wb.close();
				}
			}
		return wb;
	}

	public static List<List<String>> read(Sheet sheet, int rows) {
		List<List<String>> lists = new ArrayList<List<String>>();
		if(sheet != null){
			int rowNos = sheet.getLastRowNum();// 得到excel的总记录条数
			for (int i = rows; i <= rowNos; i++) {// 遍历行
				List<String> list = new ArrayList<>();
				Row row = sheet.getRow(i);
				if(row != null){
					int last = row.getLastCellNum();// 表头总共的列数
					for (int j = 0; j < last; j++) {
						Cell cell = row.getCell(j);
						if(cell != null){
							String value = null;
							if(isMerged(sheet, i, j)){
								value = getMergedRegionValue(sheet, i, j);
							}else {
								value = value(cell);
							}
							list.add(value);
						}
					}
					lists.add(list);
				}
			}
		}
		return lists;
	}
	/**
	 * 判断指定的单元格是否是合并单元格
	 * @param sheet sheet
	 * @param row 行下标
	 * @param col 列下标
	 * @return boolean
	 */
	public static boolean isMerged(Sheet sheet,int row ,int col) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstColumn = range.getFirstColumn();
			int lastColumn = range.getLastColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			if(row >= firstRow && row <= lastRow){
				if(col >= firstColumn && col <= lastColumn){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取合并单元格的值
	 * @param sheet sheet
	 * @param row row
	 * @param col col
	 * @return String
	 */
	public static String getMergedRegionValue(Sheet sheet ,int row , int col){
		int sheetMergeCount = sheet.getNumMergedRegions();
		for(int i = 0 ; i < sheetMergeCount ; i++){
			CellRangeAddress ca = sheet.getMergedRegion(i);
			int firstColumn = ca.getFirstColumn();
			int lastColumn = ca.getLastColumn();
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();
			if(row >= firstRow && row <= lastRow){
				if(col >= firstColumn && col <= lastColumn){
					Row fRow = sheet.getRow(firstRow);
					Cell fCell = fRow.getCell(firstColumn);
					return value(fCell) ;
				}
			}
		}

		return null ;
	}

	public static List<String> value(String path, int sheet, int row)  {
		File xlsx = new File(path);
		List<String> list = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(xlsx);
			Sheet sheet1 = workbook.getSheetAt(sheet);
			Row row1 = sheet1.getRow(row);
			int rowNum = row1.getLastCellNum();
			for (int i = 0; i < rowNum; i++) {
				list.add(value(row1.getCell(i)));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}


	public static String value(String path, int sheet, int row, int col) {
		File file = new File(path);
		return value(file, sheet, row, col);
	}
	public static String value(File file, int sheet, int row, int col) {
		String value = null;
		try {
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet1 = workbook.getSheetAt(sheet);
			Row row1 = sheet1.getRow(row);
			value = value(row1.getCell(col));
		}catch (Exception e){
			e.printStackTrace();
		}
		return value;
	}
	public static void value(String path, int sheet, int row, int col, String value) {
		File file = new File(path);
		value(file, sheet, row, col, value);
	}
	public static void value(File file, int sheet, int row, int col, String value) {
		OutputStream out = null;
		try {
			File tempFile = File.createTempFile(file.getName(), null);
			boolean renameOk = file.renameTo(tempFile);
			if(!renameOk){
				tempFile = new File(file.getParent(), "tmp_"+System.currentTimeMillis()+file.getName());
				renameOk = file.renameTo(tempFile);
			}
			if (!renameOk) {
				throw new Exception("重命名失败 "
						+ file.getAbsolutePath() + " > "
						+ tempFile.getAbsolutePath());
			}
			FileInputStream is = new FileInputStream(tempFile);

			Workbook workbook = new XSSFWorkbook(is);
			Sheet sheet1 = workbook.getSheetAt(sheet);
			value(sheet1, row, col, value);
			is.close();
			tempFile.delete();
			out = new FileOutputStream(file);
			workbook.write(out);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != out) {
				try {
					out.flush();
					out.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void value(String path, int sheet, int row, int col, double value) {
		File file = new File(path);
		value(file, sheet, row, col, value);
	}
	public static void value(File file, int sheet, int row, int col, double value) {
		try {
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet1 = workbook.getSheetAt(sheet);
			value(sheet1, row, col, value);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void value(String path, int sheet, int row, int col, int value) {
		File file = new File(path);
		value(file, sheet, row, col, value);
	}
	public static void value(File file, int sheet, int row, int col, int value) {
		try {
			Workbook workbook = WorkbookFactory.create(file);
			Sheet sheet1 = workbook.getSheetAt(sheet);
			value(sheet1, row, col, value);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public static String value(Sheet sheet, int row, int col){
		Row row1 = sheet.getRow(row);
		String value = value(row1.getCell(col));
		return value;
	}
	public static String value(Sheet sheet, int row, int col, String value){
		Row row1 = sheet.getRow(row);
		if(null == row1){
			row1 = sheet.createRow(row);
		}
		Cell cell = row1.getCell(col);
		if(null == cell){
			cell = row1.createCell(col);
		}
		cell.setCellValue(value);
		return value;
	}
	public static void value(Sheet sheet, int row, int col, double value){
		Row row1 = sheet.getRow(row);
		row1.getCell(col).setCellValue(value);
	}
	public static void value(Sheet sheet, int row, int col, int value){
		Row row1 = sheet.getRow(row);
		row1.getCell(col).setCellValue(value);
	}

	public static String value(Cell cell) {
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
	 * @param os 输出流
	 * @param headers	表头  headers	表头
	 * @param sheet 	sheet
	 * @param insert		导出的开始位置
	 * @param keys		对应列名属性名  keys		对应列名属性名
	 * @param set		数据源  set		数据源
	 * @return return
	 */
	public static boolean export(OutputStream os, String sheet, int insert, List<String>headers, List<String> keys, Collection<?> set){
		try{
			XSSFWorkbook  workbook = null;
			Sheet sht = null;
			workbook = new XSSFWorkbook();
			if(BasicUtil.isEmpty(sheet)){
				sheet = "sheet1";
			}
			sht = workbook.createSheet(sheet);
			write(sht, insert, headers, keys, set);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally {
			try{
				os.flush();
				os.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		return true;
	}

	private static void write(Sheet sheet, int insert, List<String>headers, List<String> keys, Collection<?> set){
		//表头
		if(null != headers && headers.size()>0) {
			Row row =sheet.createRow(insert++);
			int c= 0 ;
			for (String header : headers) {
				Cell cell = row.createCell(c++);
				cell.setCellType(CellType.STRING);
				cell.setCellValue(header);
			}
		}
		for(Object item:set){
			Row row = sheet.createRow(insert++);
			int c = 0;
			for(String key:keys){
				Cell cell=row.createCell(c++);
				cell.setCellType(CellType.STRING);
				cell.setCellValue(BeanUtil.parseFinalValue(item,key,""));
			}
		}
	}

	public static boolean export(File file, String sheet, int insert, Table table){
		FileOutputStream os = null;
		try{
			XSSFWorkbook  workbook = null;
			Sheet sht = null;
			if(file.exists()){
				File tempFile = File.createTempFile(file.getName(), null);
				boolean renameOk = file.renameTo(tempFile);
				if(!renameOk){
					tempFile = new File(file.getParent(), "tmp_"+System.currentTimeMillis()+file.getName());
					renameOk = file.renameTo(tempFile);
				}
				if (!renameOk) {
					throw new Exception("重命名失败 "
							+ file.getAbsolutePath() + " > "
							+ tempFile.getAbsolutePath());
				}
				FileInputStream is = new FileInputStream(tempFile);
				workbook = new XSSFWorkbook(is);
				if(BasicUtil.isEmpty(sheet)){
					sht = workbook.getSheetAt(0);
				}else {
					sht = workbook.getSheet(sheet);
					if(null == sht){
						sht = workbook.createSheet(sheet);
					}
				}
				is.close();
				tempFile.delete();
			}else {
				workbook = new XSSFWorkbook();
				if(BasicUtil.isEmpty(sheet)){
					sheet = "sheet1";
				}
				sht = workbook.createSheet(sheet);
			}

			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			if(!file.exists()){
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			write(workbook, os, sht, insert, table);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean export(File template, OutputStream os, String sheet, int insert, Table table){
		try{
			Sheet sht = null;
			FileInputStream is = new FileInputStream(template);
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			if(BasicUtil.isEmpty(sheet)){
				sht = workbook.getSheetAt(0);
			}else {
				sht = workbook.getSheet(sheet);
				if(null == sht){
					sht = workbook.createSheet(sheet);
				}
			}
			is.close();

			write(workbook, os, sht, insert, table);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean export(File template, File file, String sheet, int insert, Table table){
		try{
			Sheet sht = null;
			FileInputStream is = new FileInputStream(template);
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			if(BasicUtil.isEmpty(sheet)){
				sht = workbook.getSheetAt(0);
			}else {
				sht = workbook.getSheet(sheet);
				if(null == sht){
					sht = workbook.createSheet(sheet);
				}
			}
			is.close();
			FileUtil.create(file, false);
			write(workbook, new FileOutputStream(file), sht, insert, table);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean export(OutputStream os, String sheet, int insert, Table table){
		try{
			XSSFWorkbook workbook = new XSSFWorkbook();
			if(BasicUtil.isEmpty(sheet)){
				sheet = "sheet1";
			}
			Sheet sht = workbook.createSheet(sheet);
			write(workbook, os, sht, insert, table);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private static void write(XSSFWorkbook workbook, OutputStream os, Sheet sheet, int insert, Table table){
		try {
			int move = table.getTrs().size();
			int last = sheet.getLastRowNum();
			int footFr = insert;
			int footTo = last;
			if (last >= insert) {
				insert = last + 1;
			}
			List<Tr> trs = table.getTrs();
			for (Tr tr : trs) {
				Row row = sheet.createRow(insert);
				List<Td> tds = tr.getTds();
				for (Td td : tds) {
					int rowspan = td.getRowspan();
					int colspan = td.getColspan();

					int colIndex = td.getColIndex();
					int x = td.getColIndex();
					int y = td.getRowIndex();
					int offset = td.getOffset();
					Cell cell = row.createCell(colIndex + offset);

					cell.setCellType(CellType.STRING);
					cell.setCellValue(td.getTextTrim());
					if (rowspan > 1 || colspan > 1) {
						int firstRow = insert + y;
						int lastRow = firstRow + rowspan - 1;
						int firstCol = x + offset;
						int lastCol = firstCol + colspan - 1;
						CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
						sheet.addMergedRegion(region);
					}
				}
				insert++;
			}
			int footSize = footTo - footFr + 1; //foot行数
			if (move > 0 && footTo >= footFr) {
				sheet.shiftRows(footFr, footTo, move + footSize);//表头下移
				sheet.shiftRows(footTo + 1, footTo + footSize + move, -footSize);//数据上移
			}
			workbook.write(os);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try{
				os.flush();
				os.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param rows 开始写入的行数
	 * @param headers 表头
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, int rows, List<String>headers, List<String> keys, Collection<?> set){
		return export(file, "sheet1", rows, headers, keys, set);
	}
	public static boolean export(OutputStream os, int rows, List<String>headers, List<String> keys, Collection<?> set){
		return export(os, "sheet1", rows, headers, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, List<String> keys, Collection<?> set){
		return export(file,0, null, keys, set);
	}
	public static boolean export(OutputStream os, List<String> keys, Collection<?> set){
		return export(os,0, null, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param headers 表头
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, List<String> headers,List<String> keys, Collection<?> set){
		return export(file,0, headers, keys, set);
	}
	public static boolean export(OutputStream os, List<String> headers,List<String> keys, Collection<?> set){
		return export(os,0, headers, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param rows 从第几行开始写入
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File file, int rows, List<String> keys, Collection<?> set){
		return export(file, rows, null, keys, set);
	}
	public static boolean export(OutputStream os, int rows, List<String> keys, Collection<?> set){
		return export(os, rows, null, keys, set);
	}

	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param sheet sheet 如果文件存在 并且为空时 则取第0个sheet
	 * @param rows 行数
	 * @param set 数据
	 * @param configs 姓名:NAME或NAME
	 * @return boolean
	 */
	public static boolean export(File file, String sheet, int rows, Collection<?> set, String ... configs){
		List<String> headers = new ArrayList<>();
		List<String> keys = new ArrayList<>();
		if(null != configs){
			for(String config:configs){
				String tmps[] = config.split(":");
				if(tmps.length == 2){
					headers.add(tmps[0]);
					keys.add(tmps[1]);
				}else{
					keys.add(config);
				}
			}
			if(headers.size() != keys.size()){
				headers = new ArrayList<>();
			}
		}
		return export(file, sheet, rows, headers, keys, set);
	}

	public static boolean export(OutputStream os, String sheet, int rows, Collection<?> set, String ... configs){
		List<String> headers = new ArrayList<>();
		List<String> keys = new ArrayList<>();
		if(null != configs){
			for(String config:configs){
				String tmps[] = config.split(":");
				if(tmps.length == 2){
					headers.add(tmps[0]);
					keys.add(tmps[1]);
				}else{
					keys.add(config);
				}
			}
			if(headers.size() != keys.size()){
				headers = new ArrayList<>();
			}
		}
		return export(os, sheet, rows, headers, keys, set);
	}

	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param rows 行数
	 * @param set 数据
	 * @param configs 姓名:NAME或NAME
	 * @return boolean
	 */
	public static boolean export(File file, int rows, Collection<?> set, String ... configs){
		return export(file, "", rows, set, configs);
	}
	public static boolean export(OutputStream os, int rows, Collection<?> set, String ... configs){
		return export(os, "", rows, set, configs);
	}

	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param set 数据
	 * @param configs 姓名:NAME或NAME
	 * @return boolean
	 */
	public static boolean export(File file,  Collection<?> set, String ... configs){
		return export(file, 0, set, configs);
	}

	public static boolean export(OutputStream os,  Collection<?> set, String ... configs){
		return export(os, 0, set, configs);
	}


	/**
	 * 导出EXCEL
	 * @param template 模板
	 * @param file 		导致文件位置
	 * @param headers	表头  headers	表头
	 * @param sheet 	sheet
	 * @param insert	导出的开始位置
	 * @param keys		对应列名属性名  keys		对应列名属性名
	 * @param set		数据源  set		数据源
	 * @return return
	 */
	public static boolean export(File template, File file, String sheet, int insert, List<String>headers, List<String> keys, Collection<?> set){

		try{
			FileUtil.create(file, false);
			return export(template, new FileOutputStream(file), sheet, insert, headers, keys, set);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}


	public static boolean export(File file, String sheet, int insert, List<String>headers, List<String> keys, Collection<?> set){
		FileOutputStream os = null;
		try{
			XSSFWorkbook  workbook = null;
			Sheet sht = null;
			if(file.length() == 0){
				file.delete();
			}
			if(file.exists()){
				File tempFile = File.createTempFile(file.getName(), null);
				boolean renameOk = file.renameTo(tempFile);
				if(!renameOk){
					tempFile = new File(file.getParent(), "tmp_"+System.currentTimeMillis()+file.getName());
					renameOk = file.renameTo(tempFile);
				}
				if (!renameOk) {
					throw new Exception("重命名失败 "
							+ file.getAbsolutePath() + " > "
							+ tempFile.getAbsolutePath());
				}
				FileInputStream is = new FileInputStream(tempFile);
				workbook = new XSSFWorkbook(is);
				if(BasicUtil.isEmpty(sheet)){
					sht = workbook.getSheetAt(0);
				}else {
					sht = workbook.getSheet(sheet);
					if(null == sht){
						sht = workbook.createSheet(sheet);
					}
				}
				is.close();
				tempFile.delete();
			}else {

				FileUtil.create(file, false);
				workbook = new XSSFWorkbook();
				if(BasicUtil.isEmpty(sheet)){
					sheet = "sheet1";
				}
				sht = workbook.createSheet(sheet);
			}
			os = new FileOutputStream(file);
			write(workbook, os, sht, insert, headers, keys, set);

		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private static void write(XSSFWorkbook workbook, OutputStream os, Sheet sheet, int insert, List<String>headers, List<String> keys, Collection<?> set){
		try {
			int move = set.size();
			int footFr = insert;
			int last = sheet.getLastRowNum();
			int footTo = last;
			if (last >= insert) {
				insert = last + 1;
			}
			if (null != headers && headers.size() > 0) {
				move++;
			}
			write(sheet, insert, headers, keys, set);
			int footSize = footTo - footFr + 1; //foot行数
			if (move > 0 && footTo >= footFr && footFr >= insert) {
				sheet.shiftRows(footFr, footTo, move + footSize);
				sheet.shiftRows(footTo + 1, footTo + footSize + move, -footSize);//数据上移
			}
			workbook.write(os);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try{
				os.flush();
				os.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	public static boolean export(File template, OutputStream os, String sheet, int insert, List<String>headers, List<String> keys, Collection<?> set){
		try{
			XSSFWorkbook  workbook = null;
			Sheet sht = null;
			if(null != template && template.exists()){
				FileInputStream is = new FileInputStream(template);
				workbook = new XSSFWorkbook(is);
				if(BasicUtil.isNotEmpty(sheet)){
					workbook.setSheetName(0, sheet);
				}
				sht = workbook.getSheetAt(0);

				is.close();
			}else {
				workbook = new XSSFWorkbook();
				if(BasicUtil.isEmpty(sheet)){
					sheet = "sheet1";
				}
				sht = workbook.createSheet(sheet);
			}
			write(workbook, os, sht, insert, headers, keys, set);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param template template
	 * @param rows 开始写入的行数
	 * @param headers 表头
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File template, File file, int rows, List<String>headers, List<String> keys, Collection<?> set){
		return export(template, file, "sheet1", rows, headers, keys, set);
	}
	public static boolean export(File template, OutputStream os, int rows, List<String>headers, List<String> keys, Collection<?> set){
		return export(template, os, "sheet1", rows, headers, keys, set);
	}
	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param template template
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File template, File file, List<String> keys, Collection<?> set){
		return export(template, file,0, null, keys, set);
	}
	public static boolean export(File template, OutputStream os, List<String> keys, Collection<?> set){
		return export(template, os,0, null, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param template template
	 * @param headers 表头
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File template, File file, List<String> headers,List<String> keys, Collection<?> set){
		return export(template, file,0, headers, keys, set);
	}
	public static boolean export(File template, OutputStream os, List<String> headers,List<String> keys, Collection<?> set){
		return export(template, os,0, headers, keys, set);
	}

	/**
	 * 导出EXCEL
	 * @param template template
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param insert 从第几行开始写入
	 * @param keys 读取集合条目的属性
	 * @param set 数据集合
	 * @return boolean
	 */
	public static boolean export(File template, File file, int insert, List<String> keys, Collection<?> set){
		return export(template, file,insert, null, keys, set);
	}
	public static boolean export(File template, OutputStream os, int insert, List<String> keys, Collection<?> set){
		return export(template, os, insert, null, keys, set);
	}


	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param template template
	 * @param sheet sheet 如果文件存在 并且为空时 则取第0个sheet
	 * @param rows 行数
	 * @param set 数据
	 * @param configs 姓名:NAME或NAME
	 * @return boolean
	 */
	public static boolean export(File template, File file, String sheet, int rows, Collection<?> set, String ... configs){
		try {
			FileUtil.create(file, false);
			return export(template, new FileOutputStream(file), sheet, rows, set, configs);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public static boolean export(File template, OutputStream os, String sheet, int insert, Collection<?> set, String ... configs){
		List<String> headers = new ArrayList<>();
		List<String> keys = new ArrayList<>();
		if(null != configs){
			for(String config:configs){
				String tmps[] = config.split(":");
				if(tmps.length == 2){
					headers.add(tmps[0]);
					keys.add(tmps[1]);
				}else{
					keys.add(config);
				}
			}
			if(headers.size() != keys.size()){
				headers = new ArrayList<>();
			}
		}
		return export(template, os, sheet, insert, headers, keys, set);
	}

	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param template template
	 * @param insert 开始插入的位置
	 * @param set 数据
	 * @param configs 姓名:NAME或NAME
	 * @return boolean
	 */
	public static boolean export(File template, File file, int insert, Collection<?> set, String ... configs){
		return export(template, file, null, insert, set, configs);
	}
	public static boolean export(File template, File file, int insert, Table table){
		return export(template, file, null, insert, table);
	}
	public static boolean export(File template, OutputStream os, int insert, Collection<?> set, String ... configs){
		return export(template, os, null, insert, set, configs);
	}

	public static boolean export(File template, OutputStream os, int insert, Table table){
		return export(template, os, null, insert, table);
	}

	/**
	 * 导出excel
	 * @param file 导致文件位置，如果文件已存存，则以当前文件作为模板
	 * @param template template
	 * @param set 数据
	 * @param configs 姓名:NAME或NAME
	 * @return boolean
	 */
	public static boolean export(File template, File file,  Collection<?> set, String ... configs){
		return export(template, file, 0, set, configs);
	}
	public static boolean export(File template, File file,  Table table){
		return export(template, file,"", 0, table);
	}
	public static boolean export(File template, OutputStream os,  Collection<?> set, String ... configs){
		return export(template, os, 0, set, configs);
	}
	public static boolean export(File template, OutputStream os,  Table table){
		return export(template, os, "", 0, table);
	}
	public static boolean merge(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		try {
			sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
} 
