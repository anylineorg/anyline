/*  
 * Copyright 2006-2015 www.anyline.org 
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
 
import java.io.BufferedInputStream; 
import java.io.BufferedOutputStream; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.UnsupportedEncodingException; 
import java.nio.charset.Charset; 
import java.util.ArrayList; 
import java.util.Collection; 
import java.util.Enumeration; 
import java.util.List; 
import java.util.zip.ZipEntry; 
import java.util.zip.ZipException; 
import java.util.zip.ZipFile; 
import java.util.zip.ZipInputStream; 
import java.util.zip.ZipOutputStream; 
 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
/** 
 * Java utils 实现的Zip工具 不支持RAR格式 
 *  
 * @author  zh
 */ 
public class ZipUtil { 
	static final Logger log = LoggerFactory.getLogger(ZipUtil.class); 
	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte 
 
	/** 
	 * 批量压缩文件或文件夹 
	 * @param srcs  要压缩的文件或文件夹列表 
	 * @param root  压缩后文件路径,解压到当前目录时,解压完成后的目录名 
	 * @param zip  生成的压缩文件名 
	 * @return return
	 */ 
	public static boolean zip(Collection<File> srcs, File zip, String root) { 
		return zip(srcs, zip, root, null); 
	} 
	public static boolean append(Collection<File> srcs, File zip, String root) { 
		return append(srcs, zip, root, null); 
	} 
 
	public static boolean zip(File src, File zip, String root) { 
		List<File> files = new ArrayList<File>(); 
		files.add(src); 
		return zip(files, zip, root); 
	} 
	public static boolean append(File src, File zip, String root) { 
		List<File> files = new ArrayList<File>(); 
		files.add(src); 
		return append(files, zip, root); 
	} 
 
	public static boolean zip(Collection<File> srcs, File zip) { 
		return zip(srcs, zip, ""); 
	} 
	public static boolean append(Collection<File> srcs, File zip) { 
		return append(srcs, zip, ""); 
	} 
 
	public static boolean zip(File src, File zip) { 
		List<File> files = new ArrayList<File>(); 
		files.add(src); 
		return zip(files, zip); 
	} 
	public static boolean append(File src, File zip) { 
		List<File> files = new ArrayList<File>(); 
		files.add(src); 
		return append(files, zip); 
	} 
 
	/** 
	 * 批量压缩文件（夹） 如果zip已存在则会覆盖 
	 *  
	 * @param files   要压缩的文件（夹）列表 
	 * @param zip  生成的压缩文件 
	 * @param root 压缩后文件路径,解压到当前目录时,解压完成后的目录名 
	 * @param comment   压缩文件的注释 
	 * @param append   是否追加
	 * @return return
	 */ 
	public static boolean zip(Collection<File> files, File zip, String root, String comment, boolean append) { 
		boolean result = true; 
		long fr = System.currentTimeMillis(); 
		if (ConfigTable.isDebug()) { 
			log.warn("[压缩文件][file:{}][size:{}]", zip.getAbsolutePath(), files.size()); 
		} 
		try { 
			File dir = zip.getParentFile(); 
			if (!dir.exists()) { 
				dir.mkdirs(); 
			} 
			ZipOutputStream zipout = null; 
			if (append && zip.exists()) { 
				//追加文件 
				File tempFile = File.createTempFile(zip.getName(), null); 
				tempFile.delete(); 
				boolean renameOk = zip.renameTo(tempFile); 
				if (!renameOk) { 
					throw new RuntimeException("重命名失败 " 
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
					for (File f : files) { 
						if (f.getName().equals(name)) { 
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
			}else{ 
				zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip),BUFF_SIZE)); 
			} 
			for (File file : files) { 
				if (!zip(file, zipout, root)) { 
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
 
	public static boolean zip(Collection<File> files, File zip, String root, String comment) { 
		return zip(files, zip, root, comment, false); 
	} 
 
	public static boolean append(Collection<File> files, File zip, String root, String comment){ 
		return zip(files, zip, root, comment, true); 
	} 
	 
	public static boolean zip(File src, File zip, String root, String comment) { 
		List<File> files = new ArrayList<File>(); 
		files.add(src); 
		return zip(files, zip, root, comment); 
	} 
	public static boolean append(File src, File zip, String root, String comment) { 
		List<File> files = new ArrayList<File>(); 
		files.add(src); 
		return append(files, zip, root, comment); 
	} 
 
 
	/** 
	 * 压缩文件 
	 *  
	 * @param src  需要压缩的文件或文件夹 
	 * @param zipout 压缩的目的文件 
	 * @param dir   压缩后文件路径,解压到当前目录时,解压完成后的目录名 
	 */ 
	private static boolean zip(File src, ZipOutputStream zipout, String dir) { 
		try { 
			String path = src.getName(); 
			if (BasicUtil.isNotEmpty(dir)) { 
				path = dir + File.separator + src.getName(); 
			} 
			dir = new String(dir.getBytes("8859_1"), "GB2312"); 
			if (src.isDirectory()) { 
				File[] fileList = src.listFiles(); 
				for (File file : fileList) { 
					zip(file, zipout, path); 
				} 
			} else { 
				long fr = System.currentTimeMillis(); 
				byte buffer[] = new byte[BUFF_SIZE]; 
				BufferedInputStream in = new BufferedInputStream( 
						new FileInputStream(src), BUFF_SIZE); 
				zipout.putNextEntry(new ZipEntry(path)); 
				int realLength; 
				while ((realLength = in.read(buffer)) != -1) { 
					zipout.write(buffer, 0, realLength); 
				} 
				in.close(); 
				zipout.flush(); 
				zipout.closeEntry(); 
				if (ConfigTable.isDebug()) { 
					log.warn("[压缩文件][添加文件][耗时:{}][file:{}]",DateUtil.conversion(System.currentTimeMillis()- fr), src.getAbsolutePath()); 
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
			for (Enumeration<?> entries = zf.entries(); entries 
					.hasMoreElements();) { 
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
		try { 
			Enumeration<?> entries = getEntriesEnumeration(zip); 
			while (entries.hasMoreElements()) { 
				ZipEntry entry = ((ZipEntry) entries.nextElement()); 
				entryNames.add(new String(getEntryName(entry) 
						.getBytes("GB2312"), "8859_1")); 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return entryNames; 
	} 
 
	/** 
	 * 获得压缩文件内压缩文件对象以取得其属性 
	 *  
	 * @param zip  压缩文件 
	 * @return 返回一个压缩文件列表 
	 */ 
	public static Enumeration<?> getEntriesEnumeration(File zip) { 
		ZipFile zf = null; 
		try { 
			zf = new ZipFile(zip); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally { 
			try { 
				zf.close(); 
			} catch (IOException e) { 
				e.printStackTrace(); 
			} 
		} 
		return zf.entries(); 
 
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
			result = new String(entry.getComment().getBytes("GB2312"), "8859_1"); 
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
			result = new String(entry.getName().getBytes("GB2312"), "8859_1"); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return result; 
	} 
} 
