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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
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
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * Java utils 实现的Zip工具 不支持RAR格式
 * 
 * @author once
 */
public class ZipUtil {
	static final Logger log = Logger.getLogger(ZipUtil.class);
	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

	/**
	 * 批量压缩文件或文件夹
	 * 
	 * @param files
	 *            要压缩的文件或文件夹列表
	 * @param root
	 *            压缩后文件路径,解压到当前目录时,解压完成后的目录名
	 * @param zip
	 *            生成的压缩文件名
	 */
	public static boolean zip(Collection<File> files, File zip, String root) {
		boolean result = true;
		try {
			ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip), BUFF_SIZE));
			for (File file : files) {
				if(!zip(file, zipout, root)){
					result = false;
				}
			}
			zipout.close();
		} catch (Exception e) {
			result =false;
			e.printStackTrace();
		}
		return result;
	}
	public static boolean zip(Collection<File> files, File zip) {
		return zip(files, zip, "");
	}

	/**
	 * 批量压缩文件（夹）
	 * 如果zip已存在则会覆盖
	 * @param files
	 *            要压缩的文件（夹）列表
	 * @param zip
	 *            生成的压缩文件
	 * @param root
	 *            压缩后文件路径,解压到当前目录时,解压完成后的目录名
	 * @param comment
	 *            压缩文件的注释
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	public static boolean zip(Collection<File> files, File zip, String root, String comment) {
		boolean result = true;
		long fr = System.currentTimeMillis();
		if (ConfigTable.isDebug()) {
			log.warn("[压缩文件][file:" + zip.getAbsolutePath() + "][size:" + files.size() + "]");
		}
		try {
			ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip), BUFF_SIZE));
			for (File file : files) {
				if(!zip(file, zipout, root)){
					result = false;
				}
			}
			zipout.setComment(comment);
			zipout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ConfigTable.isDebug()) {
			log.warn("[压缩完成][time:" + (System.currentTimeMillis() - fr) + "][size:" + files.size() + "]");
		}
		return result;
	}

	/**
	 * 解压缩一个文件
	 * 
	 * @param zip
	 *            压缩文件
	 * @param folderPath
	 *            解压缩的目标目录
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */
	public static List<File> unZip(File zip, String dir) {
		return unZip(zip, new File(dir));
	}

	/**
	 * 解压缩一个文件
	 * 
	 * @param zip
	 *            压缩文件
	 * @param folderPath
	 *            解压缩的目标目录
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */
	public static List<File> unZip(File zip, File dir) {
		List<File> files = new ArrayList<File>();
		long fr = System.currentTimeMillis();
		if (ConfigTable.isDebug()) {
			log.warn("[解压文件][file:" + zip.getAbsolutePath() + "][dir:" + dir.getAbsolutePath() + "]");
		}
		int size = 0;
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
			ZipFile zf = new ZipFile(zip, Charset.forName("GBK"));
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
			}
			zf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ConfigTable.isDebug()) {
			log.warn("[解压完成][time:" + (System.currentTimeMillis() - fr) + "][dir:" + dir.getAbsolutePath() + "][size:" + size + "]");
		}
		return files;
	}

	/**
	 * 解压文件
	 * 
	 * @param zip
	 * @return
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
	 * @param zip
	 *            压缩文件
	 * @return 压缩文件内文件名称
	 * @throws ZipException
	 *             压缩文件格式有误时抛出
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */
	public static ArrayList<String> getEntriesNames(File zip) {
		ArrayList<String> entryNames = new ArrayList<String>();
		try {
			Enumeration<?> entries = getEntriesEnumeration(zip);
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				entryNames.add(new String(getEntryName(entry).getBytes("GB2312"), "8859_1"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entryNames;
	}

	/**
	 * 获得压缩文件内压缩文件对象以取得其属性
	 * 
	 * @param zip
	 *            压缩文件
	 * @return 返回一个压缩文件列表
	 * @throws ZipException
	 *             压缩文件格式有误时抛出
	 * @throws IOException
	 *             IO操作有误时抛出
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
	 * @param entry
	 *            压缩文件对象
	 * @return 压缩文件对象的注释
	 * @throws UnsupportedEncodingException
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
	 * @param entry
	 *            压缩文件对象
	 * @return 压缩文件对象的名称
	 * @throws UnsupportedEncodingException
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

	/**
	 * 压缩文件
	 * 
	 * @param src
	 *            需要压缩的文件或文件夹
	 * @param zipout
	 *            压缩的目的文件
	 * @param root
	 *            压缩后文件路径,解压到当前目录时,解压完成后的目录名
	 * @throws FileNotFoundException
	 *             找不到文件时抛出
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	private static boolean zip(File src, ZipOutputStream zipout, String root) {
		try {
			String path = src.getName();
			if(BasicUtil.isNotEmpty(root)){
				path = root +  File.separator + src.getName();
			}
			root = new String(root.getBytes("8859_1"), "GB2312");
			if (src.isDirectory()) {
				File[] fileList = src.listFiles();
				for (File file : fileList) {
					zip(file, zipout, path);
				}
			} else {
				byte buffer[] = new byte[BUFF_SIZE];
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(src), BUFF_SIZE);
				zipout.putNextEntry(new ZipEntry(path));
				int realLength;
				while ((realLength = in.read(buffer)) != -1) {
					zipout.write(buffer, 0, realLength);
				}
				in.close();
				zipout.flush();
				zipout.closeEntry();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
