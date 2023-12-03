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


package org.anyline.poi.excel;
import org.anyline.entity.html.Table;
import org.anyline.entity.html.Td;
import org.anyline.entity.html.Tr;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelUtil {

	public static final String OFFICE_EXCEL_XLS = "xls";
	public static final String OFFICE_EXCEL_XLSX = "xlsx";
	/**
	 * 读取指定Sheet也的内容
	 * @param file file 文件
	 * @param sheet sheet序号,从0开始
	 * @param rows 从第几行开始读取
	 * @param foot 到第几行结束(如果负数表示 表尾有多少行不需要读取)
	 * @return List
	 *
	 */
	public static List<List<String>> read(File file, int sheet, int rows, int foot) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(file);
			list = read(workbook.getSheetAt(sheet), rows, foot);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

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
			list = read(workbook.getSheetAt(sheet), rows, 0);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public static int[] position(Sheet sheet, int rows, int cols, String regex){
		int row = -1;
		int col = -1;
		int last = sheet.getLastRowNum();
		for (int r = rows; r <= last; r++) {// 遍历行
			List<String> list = new ArrayList<>();
			Row line = sheet.getRow(r);
			if(line != null){
				int cells = line.getLastCellNum();// 表头总共的列数
				for (int c = cols; c < cells; c++) {
					Cell cell = line.getCell(c);
					String value = null;
					if(cell != null){
						if(isMerged(sheet, r, c)){
							value = getMergedRegionValue(sheet, r, c);
						}else {
							value = value(cell);
						}
					}else{
						value = getMergedRegionValue(sheet, r, c);
					}
					if(null != value){
						if(RegularUtil.match(value, regex)){
							row = r;
							col = c;
							break;
						}
					}
				}
			}
			if(row != -1){
				break;
			}
		}
		return new int[]{row, col};
	}
	/**
	 * 根据内容(正则)定位单元格
	 * @param is 文件
	 * @param sheet sheet
	 * @param rows 开始行
	 * @param cols 开始列
	 * @param regex 匹配内容
	 * @return int[]
	 */
	public static int[] position(InputStream is, int sheet, int rows, int cols, String regex){
		try{
			Workbook workbook = getWorkbook(is);
			return position(workbook.getSheetAt(sheet), rows, cols, regex);
		}catch (Exception e){
			e.printStackTrace();
		}
		return new int[]{-1,-1};
	}
	public static int[] position(InputStream is, String sheet, int rows, int cols, String regex){
		try{
			Workbook workbook = getWorkbook(is);
			return position(workbook.getSheet(sheet), rows, cols, regex);
		}catch (Exception e){
			e.printStackTrace();
		}
		return new int[]{-1,-1};
	}
	public static int[] position(InputStream is, int sheet, String regex){
		return position(is, sheet, 0, 0, regex);
	}
	public static int[] position(InputStream is, String sheet, String regex){
		return position(is, sheet, 0, 0, regex);
	}
	public static int[] position(InputStream is, String regex){
		return position(is, 0, 0, 0, regex);
	}

	public static int[] position(File file, int sheet, int rows, int cols, String regex){
		try{
			Workbook workbook = getWorkbook(file);
			return position(workbook.getSheetAt(sheet), rows, cols, regex);
		}catch (Exception e){
			e.printStackTrace();
		}
		return new int[]{-1,-1};
	}
	public static int[] position(File file, String sheet, int rows, int cols, String regex){
		try{
			Workbook workbook = getWorkbook(file);
			return position(workbook.getSheet(sheet), rows, cols, regex);
		}catch (Exception e){
			e.printStackTrace();
		}
		return new int[]{-1,-1};
	}
	public static int[] position(File file, int sheet, String regex){
		return position(file, sheet, 0, 0, regex);
	}
	public static int[] position(File file, String sheet, String regex){
		return position(file, sheet, 0, 0, regex);
	}
	public static int[] position(File file, String regex){
		return position(file, 0, 0, 0, regex);
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
		return read(is, sheet, rows, 0);
	}
	public static List<List<String>> read(InputStream is, int sheet, int rows, int foot) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(is);
			list = read(workbook.getSheetAt(sheet), rows, foot);
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
			list = read(workbook.getSheet(sheet), rows, 0);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 读取excel
	 * @param file 文件
	 * @param sheet sheet
	 * @param rows 从rows行开始读取
	 * @param foot 到第几行结束(如果负数表示 表尾有多少行不需要读取)
	 * @return list
	 */
	public static List<List<String>> read(File file, String sheet, int rows, int foot) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(file);
			list = read(workbook.getSheet(sheet), rows, foot);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public static List<List<String>> read(InputStream is, String sheet, int rows, int foot) {
		List<List<String>> list = null;
		try {
			Workbook workbook = getWorkbook(is);
			list = read(workbook.getSheet(sheet), rows, foot);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	public static List<List<String>> read(InputStream is, String sheet, int rows) {
		return read(is, sheet, rows, 0);
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

	/**
	 * 读取sheet
	 * @param sheet sheet
	 * @param start 从第rows行开始
	 * @param end 到第几行结束(如果负数表示 表尾有多少行不需要读取)
	 * @return List
	 */
	public static List<List<String>> read(Sheet sheet, int start, int end) {
		List<List<String>> lists = new ArrayList<List<String>>();
		if(sheet != null){
			int max = sheet.getLastRowNum();// 得到excel的总记录条数
			int last = max;
			if(end < 0){
				last = max + end;
			}else if(end > 0){
				last = end;
			}
			for (int r = start; r <= last; r++) {// 遍历行
				List<String> list = new ArrayList<>();
				Row row = sheet.getRow(r);
				if(row != null){
					int cells = row.getLastCellNum();// 表头总共的列数
					for (int c = 0; c < cells; c++) {
						Cell cell = row.getCell(c);
						String value = null;
						if(cell != null){
							if(isMerged(sheet, r, c)){
								value = getMergedRegionValue(sheet, r, c);
							}else {
								value = value(cell);
							}
						}else{
							value = getMergedRegionValue(sheet, r, c);
						}
						list.add(value);
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

	/**
	 * 读取1行数据
	 * @param file 文件
	 * @param sheet sheet
	 * @param row 行
	 * @return List
	 */
	public static List<String> values(File file, int sheet, int row)  {
		List<String> list = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(file);
			return values(workbook.getSheetAt(sheet), row);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	public static List<String> values(InputStream is, int sheet, int row)  {
		List<String> list = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(is);
			return values(workbook.getSheetAt(sheet), row);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	public static List<String> values(InputStream is, int row)  {
		return values(is, 0, row);
	}
	public static List<String> values(File file, int row)  {
		return values(file, 0, row);
	}
	public static List<String> values(File file, String sheet, int row)  {
		List<String> list = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(file);
			return values(workbook.getSheet(sheet), row);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	public static List<String> values(InputStream is, String sheet, int row)  {
		List<String> list = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(is);
			return values(workbook.getSheet(sheet), row);
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	public static List<String> values(Sheet sheet, int row){
		List<String> list = new ArrayList<>();
		Row r = sheet.getRow(row);
		int rowNum = r.getLastCellNum();
		for (int i = 0; i < rowNum; i++) {
			list.add(value(r.getCell(i)));
		}
		return list;
	}

	public static List<String> values(String path, int sheet, int row)  {
		return values(new File(path), sheet, row);
	}
	public static List<String> values(String path, String sheet, int row)  {
		return values(new File(path), sheet, row);
	}
	public static List<String> values(String path,  int row)  {
		return values(new File(path), 0, row);
	}

	public static String value(Sheet sheet, int row, int col){
		return value(sheet.getRow(row).getCell(col));
	}

	public static String value(String path, int sheet, int row, int col) {
		File file = new File(path);
		return value(file, sheet, row, col);
	}
	public static String value(Workbook workbook, int sheet, int row, int col){
		return value(workbook.getSheetAt(sheet), row, col);
	}
	public static String value(Workbook workbook, String sheet, int row, int col){
		return value(workbook.getSheet(sheet), row, col);
	}
	public static String value(File file, int sheet, int row, int col) {
		try {
			Workbook workbook = WorkbookFactory.create(file);
			return value(workbook, sheet, row, col);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public static String value(File file, String sheet, int row, int col) {
		try {
			Workbook workbook = WorkbookFactory.create(file);
			return value(workbook, sheet, row, col);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public static String value(InputStream is, int sheet, int row, int col) {
		try {
			Workbook workbook = WorkbookFactory.create(is);
			return value(workbook, sheet, row, col);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public static String value(InputStream is, String sheet, int row, int col) {
		try {
			Workbook workbook = WorkbookFactory.create(is);
			return value(workbook, sheet, row, col);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String value(InputStream is, int row, int col) {
		return value(is, 0, row, col);
	}
	public static void value(String path, int sheet, int row, int col, String value) {
		value( new File(path), sheet, row, col, value);
	}
	public static void value(String path, int row, int col, String value) {
		value( new File(path), 0, row, col, value);
	}
	public static void value(String path, String sheet, int row, int col, String value) {
		value( new File(path), sheet, row, col, value);
	}
	public static void value(File file, int sheet, int row, int col, String value) {
		OutputStream os = null;
		try {
			File tempFile = FileUtil.createTempFile(file);
			FileInputStream is = new FileInputStream(tempFile);
			Workbook workbook = new XSSFWorkbook(is);
			value(workbook.getSheetAt(sheet), row, col, value);
			is.close();
			tempFile.delete();
			os = new FileOutputStream(file);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != os) {
				try {
					os.flush();
					os.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void value(File file, String sheet, int row, int col, String value) {
		OutputStream os = null;
		try {
			File tempFile = FileUtil.createTempFile(file);
			FileInputStream is = new FileInputStream(tempFile);
			Workbook workbook = new XSSFWorkbook(is);
			value(workbook.getSheet(sheet), row, col, value);
			is.close();
			tempFile.delete();
			os = new FileOutputStream(file);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != os) {
				try {
					os.flush();
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void value(String path, int sheet, int row, int col, double value) {
		value( new File(path), sheet, row, col, value);
	}
	public static void value(String path, int row, int col, double value) {
		value( new File(path), 0, row, col, value);
	}
	public static void value(String path, String sheet, int row, int col, double value) {
		value( new File(path), sheet, row, col, value);
	}
	public static void value(File file, int sheet, int row, int col, double value) {
		OutputStream os = null;
		try {
			File tempFile = FileUtil.createTempFile(file);
			FileInputStream is = new FileInputStream(tempFile);
			Workbook workbook = new XSSFWorkbook(is);
			value(workbook.getSheetAt(sheet), row, col, value);
			is.close();
			tempFile.delete();
			os = new FileOutputStream(file);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != os) {
				try {
					os.flush();
					os.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void value(File file, String sheet, int row, int col, double value) {
		OutputStream os = null;
		try {
			File tempFile = FileUtil.createTempFile(file);
			FileInputStream is = new FileInputStream(tempFile);
			Workbook workbook = new XSSFWorkbook(is);
			value(workbook.getSheet(sheet), row, col, value);
			is.close();
			tempFile.delete();
			os = new FileOutputStream(file);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != os) {
				try {
					os.flush();
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}


	public static void value(String path, int sheet, int row, int col, int value) {
		value( new File(path), sheet, row, col, value);
	}
	public static void value(String path, String sheet, int row, int col, int value) {
		value( new File(path), sheet, row, col, value);
	}
	public static void value(File file, int sheet, int row, int col, int value) {
		OutputStream os = null;
		try {
			File tempFile = FileUtil.createTempFile(file);
			FileInputStream is = new FileInputStream(tempFile);
			Workbook workbook = new XSSFWorkbook(is);
			value(workbook.getSheetAt(sheet), row, col, value);
			is.close();
			tempFile.delete();
			os = new FileOutputStream(file);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != os) {
				try {
					os.flush();
					os.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void value(File file, String sheet, int row, int col, int value) {
		OutputStream os = null;
		try {
			File tempFile = FileUtil.createTempFile(file);
			FileInputStream is = new FileInputStream(tempFile);
			Workbook workbook = new XSSFWorkbook(is);
			value(workbook.getSheet(sheet), row, col, value);
			is.close();
			tempFile.delete();
			os = new FileOutputStream(file);
			workbook.write(os);
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != os) {
				try {
					os.flush();
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void value(Sheet sheet, int row, int col, String value){
		Row r = sheet.getRow(row);
		if(null == r){
			r = sheet.createRow(row);
		}
		Cell cell = r.getCell(col);
		if(null == cell){
			cell = r.createCell(col);
		}
		cell.setCellValue(value);
	}
	public static void value(Sheet sheet, int row, int col, double value){
		Row r = sheet.getRow(row);
		if(null == r){
			r = sheet.createRow(row);
		}
		Cell cell = r.getCell(col);
		if(null == cell){
			cell = r.createCell(col);
		}
		cell.setCellValue(value);
	}
	public static void value(Sheet sheet, int row, int col, int value){
		Row r = sheet.getRow(row);
		if(null == r){
			r = sheet.createRow(row);
		}
		Cell cell = r.getCell(col);
		if(null == cell){
			cell = r.createCell(col);
		}
		cell.setCellValue(value);
	}

	public static String value(Cell cell) {
		// 判断是否为null或空串
		if (cell == null || cell.toString().trim().equals("")) {
			return "";
		}
		String value = "";
		// 5.0.0 getCellType()
		// 3.x getCellTypeEnum
		switch (cell.getCellType()) {
			case NUMERIC: // 数字
				if (DateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					value = formater.format(date);
				}else{
					// 如果查数字,修改成String
					cell.setCellType(CellType.STRING);
					value = cell.getStringCellValue();
					// value = cell.getNumericCellValue() + "";
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
				value = "";
				break;
			default:
				value = "";
				break;
		}
		return value;
	}

	/** 
	 * 导出EXCEL
	 * @param os 输出流
	 * @param headers	表头  headers	表头
	 * @param sheet 	sheet
	 * @param insert	导出的开始位置
	 * @param keys		对应列名属性名  keys		对应列名属性名
	 * @param set		数据源  set		数据源
	 * @return boolean
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
		// 表头
		if(null != headers && headers.size()>0) {
			Row row =sheet.createRow(insert++);
			int c= 0 ;
			for (String header : headers) {
				Cell cell = row.createCell(c++);
				cell.setCellType(CellType.STRING);
				cell.setCellValue(header);
			}
		}
		int num = 1;
		for(Object item:set){
			Row row = sheet.createRow(insert++);
			int c = 0;
			for(String key:keys){
				Cell cell=row.createCell(c++);
				cell.setCellType(CellType.STRING);
				if(key.equals("${num}")){
					cell.setCellValue(num);
				}else {
					cell.setCellValue(BeanUtil.parseFinalValue(item, key, ""));
				}
			}
			num ++;
		}
	}
	public static boolean export(File file, String sheet, int insert, Table table){
		FileOutputStream os = null;
		try{
			XSSFWorkbook  workbook = null;
			Sheet sht = null;
			if(file.exists()){
				File tempFile = FileUtil.createTempFile(file);
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
			File dir = file.getParentFile();
			if(null != dir && !dir.exists()){
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

	public static boolean export(File file, Table table){
		String sheet = "sheet1";
		return export(file, sheet,0, table);
	}
	public static boolean export(File file, String sheet, Table table){
		return export(file, sheet,0, table);
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
	public static boolean export(File template, File file, int sheet, int insert, Table table){
		try{
			Sheet sht = null;
			FileInputStream is = new FileInputStream(template);
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			sht = workbook.getSheetAt(sheet);
			is.close();
			FileUtil.create(file, false);
			write(workbook, new FileOutputStream(file), sht, insert, table);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean export(File template, OutputStream os,  Table table){
		return export(template, os, "", 0, table);
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
	private static Row row(Sheet sheet, int r){
		Row row = null;
		int last = sheet.getLastRowNum();
		if(r<=last){
			row = sheet.getRow(r);
			if(null == row){
				row = sheet.createRow(r);
			}
		}else{
			row = sheet.createRow(r);
		}
		return row;
	}
	private static void write(XSSFWorkbook workbook, OutputStream os, Sheet sheet, int insert, Table table){
		try {
			int size = table.getTrs().size();
			int last = sheet.getLastRowNum();
			if(last > 0 && last>=insert && size>0) {
				sheet.shiftRows(insert, last, size);//表尾下移
			}
			List<Tr> trs = table.getTrs();
			for (Tr tr : trs) {
				Row row = row(sheet,insert);
				List<Td> tds = tr.getTds();
				for (Td td : tds) {
					int rowspan = td.getRowspan();
					int colspan = td.getColspan();

					int colIndex = td.getColIndex();
					int x = td.getColIndex();
					int y = td.getRowIndex();
					int offset = td.getOffset();

					CellStyle style = null;
					if (insert - 1 > 0) {
						Row prevRow = sheet.getRow(insert - 1);
						int lastCellIndex = prevRow.getLastCellNum();
						if (colIndex + offset >= lastCellIndex) {
							Cell prevCell = prevRow.getCell(colIndex + offset);
							if (null != prevCell) {
								style = prevCell.getCellStyle();
							}
						}
					}
					Map<String, String> styles = td.getStyles();
					if(null != styles || !styles.isEmpty()){
						if(null == style){
							style = sheet.getWorkbook().createCellStyle();
						}
						parseStyle(style, styles);
						Font font = sheet.getWorkbook().createFont();
						font = parseFont(font, styles);
						if(null != font){
							style.setFont(font);
						}
					}
					if (rowspan > 1 || colspan > 1) {
						int firstRow = insert;
						int lastRow = firstRow + rowspan - 1;
						int firstCol = x + offset;
						int lastCol = firstCol + colspan - 1;
						CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
						sheet.addMergedRegion(region);
						// 补齐其他单元格(否则边框设置不上)
						for(int rr=1; rr<rowspan; rr++){
							Row mergeRow = row(sheet, insert+rr);
							for(int cc=0; cc<colspan; cc++){
								Cell mergeCell = mergeRow.createCell(colIndex + offset + cc);
								mergeCell.setCellStyle(style);
							}
						}
						if(colspan>1){
							for(int cc=1; cc<colspan; cc++){
								Cell mergeCell = row.createCell(colIndex + offset + cc);
								if(null != style) {
									mergeCell.setCellStyle(style);
								}
							}
						}
					}

					Cell cell = row.createCell(colIndex + offset);
					if(null != style) {
						cell.setCellStyle(style);
					}
					cell.setCellType(CellType.STRING);
					cell.setCellValue(td.getTextTrim());
				}
				insert++;
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

	private static BorderStyle parseBorderStyle(Map<String, String> styles, String side){
		BorderStyle result = BorderStyle.NONE;
		String style = styles.get("border-"+side+"-style");
		if(BasicUtil.isEmpty(style)){
			style = styles.get("border-style");
		}
		if(BasicUtil.isNotEmpty(style)){
			if("solid".equalsIgnoreCase(style)){
				result = BorderStyle.THIN;
			}else if("double".equalsIgnoreCase(style)){
				result = BorderStyle.DOUBLE;
			}else if("dashed".equalsIgnoreCase(style)){
				result = BorderStyle.DASHED;
			}else if("dotted".equalsIgnoreCase(style)){
				result = BorderStyle.DOTTED;
			}else if("thick".equalsIgnoreCase(style)){
				result = BorderStyle.THICK;
			}else{
				result = BorderStyle.THIN;
			}
		}
		return result;
	}
	/**
	 * 根据css样式解析excel样式
	 * @param style excel样式
	 * @param styles css样式
	 * @return CellStyle
	 */
	private static CellStyle parseStyle(CellStyle style, Map<String, String> styles){
		if(null != styles) {
			// 边框
			style.setBorderTop(parseBorderStyle(styles, "top"));
			style.setBorderBottom(parseBorderStyle(styles, "bottom"));
			style.setBorderRight(parseBorderStyle(styles, "right"));
			style.setBorderLeft(parseBorderStyle(styles, "left"));

			// 水平对齐
			String textAlign = styles.get("text-align");
			if(BasicUtil.isNotEmpty(textAlign)){
				if("center".equals(textAlign)){
					style.setAlignment(HorizontalAlignment.CENTER);
				}else if("left".equals(textAlign)){
					style.setAlignment(HorizontalAlignment.LEFT);
				}else if("right".equals(textAlign)){
					style.setAlignment(HorizontalAlignment.RIGHT);
				}
			}
			// 垂直对齐
			String verticalAlign = styles.get("vertical-align");
			if(BasicUtil.isNotEmpty(verticalAlign)){
				if("center".equals(verticalAlign) || "middle".equals(verticalAlign)){
					style.setVerticalAlignment(VerticalAlignment.CENTER);
				}else if("top".equals(verticalAlign)){
					style.setVerticalAlignment(VerticalAlignment.TOP);
				}else if("bottom".equals(verticalAlign)){
					style.setVerticalAlignment(VerticalAlignment.BOTTOM);
				}
			}

		}
		return style;
	}
	private static Font parseFont(Font font, Map<String, String> styles){
		if(null != styles) {
			String fontSize = styles.get("font-size");
			if(null != fontSize){
				short pt = 0;
				if(fontSize.endsWith("px")){
					int px = BasicUtil.parseInt(fontSize.replace("px",""),0);
					pt = (short) DocxUtil.px2pt(px);
				}else if(fontSize.endsWith("pt")){
					pt = BasicUtil.parseInt(fontSize.replace("pt",""),0).shortValue();
				}
				if(pt>0){
					font.setFontHeightInPoints(pt);
				}
			}

			String fontFamily = styles.get("font-family");
			if(BasicUtil.isNotEmpty(fontFamily)) {
				font.setFontName(fontFamily);
			}

			// 删除线
			String strike = styles.get("strike");
			if(null != strike){
				if(strike.equalsIgnoreCase("true")){
					font.setStrikeout(true);
				}
			}
			// 斜体
			String italics = styles.get("italic");
			if(null != italics){
				if(italics.equalsIgnoreCase("true")){
					font.setItalic(true);
				}
			}
			// 加粗
			String fontWeight = styles.get("font-weight");
			if(null != fontWeight && fontWeight.length()>0){
				int weight = BasicUtil.parseInt(fontWeight,0);
				if(weight >=700){
					font.setBold(true);
				}
			}

			// 下划线
			String underline = styles.get("underline");
			if(null != underline){
				font.setUnderline((byte) 1);
			}
			return font;
		}
		return null;
	}
	/**
	 * 导出EXCEL
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @return boolean
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
				File tempFile = FileUtil.createTempFile(file);
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

			int size = set.size();
			int last = sheet.getLastRowNum();
			if(last > 0 && last>=insert && size>0) {
				sheet.shiftRows(insert, last, size);//表尾下移
			}
			write(sheet, insert, headers, keys, set);
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
	 * @param file 导致文件位置,如果文件已存存,则以当前文件作为模板
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
