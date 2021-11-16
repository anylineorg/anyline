/*  
 * Copyright 2006-2020 www.anyline.org
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
 
package org.anyline.util; 
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
 
/** 
 * Java utils 实现的Zip工具 不支持RAR格式
 */ 
public class ZipUtil { 
	static final Logger log = LoggerFactory.getLogger(ZipUtil.class); 
	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte 


	/**
	 * 从压缩文件中删除条目
	 * @param zip 压缩文件
	 * @param item 需要删除的条目(含目录)
	 * @return boolean
	 */
	public static boolean remove(File zip, String item){
		Map properties = new HashMap<>();
		properties.put("create", "false");
		URI zip_disk = URI.create("jar:file:/"+zip.getAbsolutePath().replace("\\","/"));
		try (FileSystem fs = FileSystems.newFileSystem(zip_disk, properties)) {
			Path path = fs.getPath(item);
			log.warn("[删除压缩文件条目][zip:{}][item:{}]", zip.getAbsolutePath(), path.toUri());
			Files.delete(path);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 读取压缩文件条目
	 * @param zip 压缩文件
	 * @param item 条目
	 * @return InputStream
	 * @throws Exception Exception
	 */
	public static InputStream read(File zip, String item) throws Exception{
		ZipFile _zip = new ZipFile(zip);
		ZipEntry _item = _zip.getEntry(item);
		return _zip.getInputStream(_item);
	}
	public static String read(File zip, String item, String encode){
		InputStream in = null;
		try {
			in = read(zip, item);
			String str = FileUtil.read(in, "UTF-8").toString();
			return str;
		}catch (Exception e){
			return null;
		}finally {
			try{
				in.close();
			}catch (Exception e){

			}
		}
	}
	/**
	 * 替换内容
	 * @param zip 源文件
	 * @param content 替换内容
	 * @param item 被替换条目(含目录)
	 * @throws Exception Exception
	 */
	public static void replace(File zip, File content, String item) throws Exception {
		replace(zip, new FileInputStream(content), item);
	}

	public static void replace(File zip, String content, String item) throws Exception {
		replace(zip, new ByteArrayInputStream(content.getBytes()), item);
	}

	public static void replace(File src, InputStream in, String item) throws Exception {

		File tempFile = File.createTempFile(src.getName(), null);
		tempFile.delete();
		boolean renameOk = src.renameTo(tempFile);
		if (!renameOk) {
			throw new Exception("重命名失败 "
					+ src.getAbsolutePath() + " > "
					+ tempFile.getAbsolutePath());
		}
		ZipFile zip = new ZipFile(tempFile);
		Enumeration<? extends ZipEntry> entrys = zip.entries();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(src));
		int len = -1;
		byte[] buffer = new byte[1024];
		while (entrys.hasMoreElements()) {
			ZipEntry entity = entrys.nextElement();
			InputStream is = zip.getInputStream(entity);
			out.putNextEntry(new ZipEntry(entity.toString()));
			if (item.equals(entity.toString())) {
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				in.close();
			} else {
				while ((len = is.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				is.close();
			}
		}
		out.close();
	}
	public static boolean zip(Map<String,File> files, File zip, String dir, String comment, boolean append) {
		boolean result = true;
		long fr = System.currentTimeMillis();
		if (ConfigTable.isDebug()) {
			log.warn("[压缩文件][file:{}][size:{}]", zip.getAbsolutePath(), files.size());
		}
		try {

			List<String> keys = BeanUtil.getMapKeys(files);
			File parent = zip.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			ZipOutputStream zipout = null;
			if (append && zip.exists()) {
				//追加文件
				File tempFile = File.createTempFile(zip.getName(), null);
				tempFile.delete();
				boolean renameOk = zip.renameTo(tempFile);
				if (!renameOk) {
					throw new Exception("重命名失败 "
							+ zip.getAbsolutePath() + " > "
							+ tempFile.getAbsolutePath());
				}
				byte[] buf = new byte[1024];
				ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
				zipout = new ZipOutputStream(new FileOutputStream(zip));
				ZipEntry entry = zin.getNextEntry();
				while (entry != null) {
					String name = entry.getName();
					boolean notInFiles = true;
					for(String key:keys){
						if (key.equals(name)) {
							notInFiles = false;
							break;
						}
					}
					if (notInFiles) {
						zipout.putNextEntry(new ZipEntry(name));
						int len;
						while ((len = zin.read(buf)) > 0) {
							zipout.write(buf, 0, len);
						}
					}
					entry = zin.getNextEntry();
				}
				zin.close();
			}else{//end 追加
				zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip),BUFF_SIZE));
			}
			for(String key:keys){
				File file = files.get(key);
				if (!zip(file, key, zipout, dir)) {
					result = false;
				}
			}

			if(null !=comment){
				zipout.setComment(comment);
			}
			zipout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ConfigTable.isDebug()) {
			log.warn("[压缩完成][time:{}][size:{}]",(System.currentTimeMillis() - fr), files.size());
		}
		return result;
	}

	/**
	 * 批量压缩文件（夹） 如果zip已存在则会覆盖
	 *
	 * @param files   要压缩的文件（夹）列表
	 * @param zip  生成的压缩文件
	 * @param dir 压缩后文件路径,解压到当前目录时,解压完成后的目录名
	 * @param comment   压缩文件的注释
	 * @param append   是否追加
	 * @return return
	 */
	public static boolean zip(Collection<File> files, File zip, String dir, String comment, boolean append) {
		Map<String,File> map = new HashMap<String,File>();
		for(File file:files){
			map.put(file.getName(), file);
		}
		return zip(map,zip,dir,comment,append);
	}
	public static boolean zip(Collection<File> files, File zip, String dir, String comment) {
		return zip(files, zip, dir, comment, false);
	}
	public static boolean zip(Map<String,File> files, File zip, String dir, String comment) {
		return zip(files, zip, dir, comment, false);
	}

	public static boolean append(Collection<File> files, File zip, String dir, String comment){
		return zip(files, zip, dir, comment, true);
	}
	public static boolean append(Map<String,File> files, File zip, String dir, String comment){
		return zip(files, zip, dir, comment, true);
	}

	public static boolean zip(File item, File zip, String dir, String comment) {
		List<File> files = new ArrayList<File>();
		files.add(item);
		return zip(files, zip, dir, comment);
	}
	public static boolean append(File item, File zip, String dir, String comment) {
		List<File> files = new ArrayList<File>();
		files.add(item);
		return append(files, zip, dir, comment);
	}

	/**
	 * 批量压缩文件或文件夹
	 * @param items  要压缩的文件或文件夹列表
	 * @param dir  压缩后文件路径,解压到当前目录时,解压完成后的目录名
	 * @param zip  生成的压缩文件名
	 * @return return
	 */
	public static boolean zip(Collection<File> items, File zip, String dir) {
		return zip(items, zip, dir, null);
	}
	public static boolean zip(Map<String,File> items, File zip, String dir) {
		return zip(items, zip, dir, null);
	}
	public static boolean append(Collection<File> items, File zip, String dir) {
		return append(items, zip, dir, null);
	}
	public static boolean append(Map<String,File> items, File zip, String dir) {
		return append(items, zip, dir, null);
	}

	public static boolean zip(File item, File zip, String dir) {
		List<File> files = new ArrayList<File>();
		files.add(item);
		return zip(files, zip, dir);
	}
	public static boolean append(File item, File zip, String dir) {
		List<File> files = new ArrayList<File>();
		files.add(item);
		return append(files, zip, dir);
	}

	public static boolean zip(Collection<File> items, File zip) {
		return zip(items, zip, "");
	}
	public static boolean zip(Map<String,File> items, File zip) {
		return zip(items, zip, "");
	}
	public static boolean append(Collection<File> items, File zip) {
		return append(items, zip, "");
	}

	public static boolean append(Map<String,File> items, File zip) {
		return append(items, zip, "");
	}

	public static boolean zip(File item, File zip) {
		if(item.exists() && item.isDirectory()){
			String dir = item.getAbsolutePath();
			List<File> files = FileUtil.getAllChildrenFile(item);
			Map<String,File> map = new HashMap<String,File>();
			for(File file: files){
				String path = file.getAbsolutePath();
				String key = path.replace(dir,"");
				map.put(key, file);
			}
			return zip(map, zip, "", null, false);
		}
		List<File> files = new ArrayList<File>();
		files.add(item);
		return zip(files, zip);
	}
	public static boolean append(File item, File zip) {
		List<File> files = new ArrayList<File>();
		files.add(item);
		return append(files, zip);
	}
	/** 
	 * 压缩文件 
	 *  
	 * @param item  需要压缩的文件或文件夹
	 * @param zipout 压缩的目的文件 
	 * @param dir   压缩后文件路径,解压到当前目录时,解压完成后的目录名 
	 */ 
	private static boolean zip(File item, String rename, ZipOutputStream zipout, String dir) {
		try { 
			String path = item.getName();
			if(BasicUtil.isNotEmpty(rename)){
				path = rename;
			}
			if (BasicUtil.isNotEmpty(dir)) { 
				path = dir + File.separator + item.getName();
			} 
			dir = new String(dir.getBytes("8859_1"), "GB2312"); 
			if (item.isDirectory()) {
				File[] fileList = item.listFiles();
				for (File file : fileList) { 
					zip(file, file.getName(),zipout, path);
				} 
			} else { 
				long fr = System.currentTimeMillis(); 
				byte buffer[] = new byte[BUFF_SIZE]; 
				BufferedInputStream in = new BufferedInputStream( 
						new FileInputStream(item), BUFF_SIZE);
				zipout.putNextEntry(new ZipEntry(path)); 
				int realLength; 
				while ((realLength = in.read(buffer)) != -1) { 
					zipout.write(buffer, 0, realLength); 
				} 
				in.close(); 
				zipout.flush(); 
				zipout.closeEntry(); 
				if (ConfigTable.isDebug()) { 
					log.warn("[压缩文件][添加文件][耗时:{}][file:{}]",DateUtil.conversion(System.currentTimeMillis()- fr), item.getAbsolutePath());
				} 
			} 
			return true; 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return false; 
		} 
	}

	/** 
	 * 解压缩一个文件 
	 *  
	 * @param zip 压缩文件 
	 * @param dir 解压缩的目标目录 
	 * @return return
	 */ 
	public static List<File> unZip(File zip, String dir) { 
		return unZip(zip, new File(dir)); 
	} 
 
	/** 
	 * 解压缩一个文件 
	 *  
	 * @param zip 压缩文件 
	 * @param dir 解压缩的目标目录 
	 * @return return
	 */ 
	public static List<File> unZip(File zip, File dir) { 
		List<File> files = new ArrayList<File>(); 
		long fr = System.currentTimeMillis(); 
		if (ConfigTable.isDebug()) { 
			log.warn("[解压文件][file:{}][dir:{}]", zip.getAbsolutePath(), dir.getAbsolutePath()); 
		} 
		int size = 0; 
		try { 
			if (!dir.exists()) { 
				dir.mkdirs(); 
			} 
			ZipFile zf = new ZipFile(zip, Charset.forName("GBK")); 
			int total = zf.size(); 
			for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
				ZipEntry entry = ((ZipEntry) entries.nextElement()); 
				if (entry.isDirectory()) { 
					continue; 
				} 
				size++; 
				InputStream in = zf.getInputStream(entry); 
				File desFile = new File(dir, entry.getName()); 
				if (!desFile.exists()) { 
					File fileParentDir = desFile.getParentFile(); 
					if (!fileParentDir.exists()) { 
						fileParentDir.mkdirs(); 
					} 
					desFile.createNewFile(); 
				} 
				files.add(desFile); 
				OutputStream out = new FileOutputStream(desFile); 
				byte buffer[] = new byte[BUFF_SIZE]; 
				int realLength; 
				while ((realLength = in.read(buffer)) > 0) { 
					out.write(buffer, 0, realLength); 
				} 
				in.close(); 
				out.close(); 
				if (ConfigTable.isDebug()) { 
					log.warn("[解压完成][进度:{}/{}][耗时:{}][file:{}]", size,total,DateUtil.conversion(System.currentTimeMillis()- fr),desFile.getAbsolutePath()); 
				} 
			} 
			zf.close(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		if (ConfigTable.isDebug()) { 
			log.warn("[解压完成][共耗时:{}][dir:{}][size:{}]",DateUtil.conversion(System.currentTimeMillis() - fr), dir.getAbsolutePath(), size); 
		} 
		return files; 
	}

	/** 
	 * 解压文件 
	 *  
	 * @param zip  zip
	 * @return return
	 */ 
	public static List<File> unZip(File zip) { 
		if (null == zip) { 
			return new ArrayList<File>(); 
		} 
		return unZip(zip, zip.getParentFile()); 
	} 
 
	/** 
	 * 获得压缩文件内文件列表 
	 *  
	 * @param zip 压缩文件 
	 * @return 压缩文件内文件名称 
	 */ 
	public static ArrayList<String> getEntriesNames(File zip) { 
		ArrayList<String> entryNames = new ArrayList<String>();
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zip);
			Enumeration<?> entries = zipFile.entries();
			while (entries.hasMoreElements()) { 
				ZipEntry entry = ((ZipEntry) entries.nextElement()); 
				entryNames.add(new String(getEntryName(entry) 
						.getBytes("GB2312"), "8859_1")); 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally {
			if(null != zipFile) {
				try {
					zipFile.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return entryNames; 
	} 

	/** 
	 * 取得压缩文件对象的注释 
	 *  
	 * @param entry 压缩文件对象 
	 * @return 压缩文件对象的注释 
	 */ 
	public static String getEntryComment(ZipEntry entry) { 
		String result = ""; 
		try {

            result = entry.getComment();
            if(null != result) {
                result = new String(result.getBytes("GB2312"), "8859_1");
            }

		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return result; 
	} 
 
	/** 
	 * 取得压缩文件对象的名称 
	 *  
	 * @param entry 压缩文件对象 
	 * @return 压缩文件对象的名称 
	 */ 
	public static String getEntryName(ZipEntry entry) { 
		String result = ""; 
		try {
			result = entry.getName();
			if(null != result) {
				result = new String(result.getBytes("GB2312"), "8859_1");
			}
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return result; 
	} 
} 
