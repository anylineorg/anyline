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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;


public class FileUtil {
	private static final Logger log = Logger.getLogger(FileUtil.class);
	public final static int PATH_TYPE_JAR = 0;

	/**
	 * 合成path
	 * @param paths
	 * @return
	 */
	public static String mergePath(String ... paths){
		String result = null;
		String separator = getFileSeparator();
		if(null != paths){
			for(String path:paths){
				if(BasicUtil.isEmpty(path)){
					continue;
				}
				if(null == result){
					result = path;
				}else{
					if(result.endsWith("/") || result.endsWith("\\")){
						if(path.startsWith("/") || path.startsWith("\\")){
							// "root/" + "/sub" 
							result += path.substring(1);
						}else{
							// "root/" + "sub"
							result += path;
						}
					}else{
						if(path.startsWith("/") || path.startsWith("\\")){
							// "root" + "/sub" 
							result += path;
						}else{
							// "root" + "sub"
							result += separator + path;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 构建完整路径
	 * @param dir
	 * @param fileName
	 * @return
	 */
	public static String crateFullFilePath(String dir, String fileName, String separator){
		if(null==dir || null==fileName) {
			return null;
		}
		String path = null;
		if(FileUtil.endWithSeparator(dir)&& FileUtil.startWithSeparator(fileName)){
			path = dir.substring(0,dir.length()-1) + fileName;
		}else if(FileUtil.endWithSeparator(dir) || FileUtil.startWithSeparator(fileName)){
			path = dir + fileName;
		}else{
			path = dir + separator + fileName;
		}
		return path;
	}
	public static String createFullFilePath(String dir, String fileName){
		return crateFullFilePath(dir,fileName,File.separator);
	}
	public static String createFullHttpPath(String srcUrl, String dstUrl){
		//完整的目标URL
		if(dstUrl.startsWith("http:")) return dstUrl;
		String fullPath = null;

		if(dstUrl.startsWith("/")){//当前站点的绝对路径
			fullPath = getHostUrl(srcUrl) + dstUrl;
		}else if(dstUrl.startsWith("?")){//查询参数
			fullPath = fetchPathByUrl(srcUrl)+dstUrl;
		}else{//当前站点的相对路径
			srcUrl = fetchDirByUrl(srcUrl);
			if(srcUrl.endsWith("/")){
				//src是一个目录
				fullPath = srcUrl + dstUrl;
			}else{
				//src有可能是一个文件 : 需要判断是文件还是目录  文件比例多一些
				fullPath = srcUrl + "/" + dstUrl;
			}
		}
		return fullPath;
	}
	/**
	 * 目录分隔符
	 * @return
	 */
	public static String getFileSeparator(){
		return System.getProperty("file.separator");
	}
	
	/**
	 * 判断当前应用程序的目录类别 0-jar包形式
	 * @param dstClass
	 * @return
	 */
	public static int getPathType(Class dstClass){
		int type = -1;
		try{
			String path = dstClass.getResource("").getPath();
			//file:/D:/gather.jar!/com/sec/
			if(path.indexOf(".jar!") != -1){
				//jar 目录
				type = 0;
			}else{
				//其他目录
				type = 1;
			}
		}catch(Exception e){
			type = -1;
		}
		return type;
	}
	/**
	 * 读取输入流
	 * @param inputStream
	 * @param encode
	 * @return
	 */
	public static StringBuffer read(InputStream inputStream, String encode){
		StringBuffer buffer = new StringBuffer();
		int BUFFER_SIZE = 1024 * 8;
		
		BufferedInputStream in = null ;
        
         try  {
        	 if(inputStream.available() <=0){
        		 return buffer;
        	 }
        	 if(BUFFER_SIZE>inputStream.available()){
        		 BUFFER_SIZE = inputStream.available();
        	 }
             in = new BufferedInputStream(inputStream, BUFFER_SIZE);
             inputStream.available();
             byte [] by = new byte [BUFFER_SIZE];
             int size = 0;
             while ((size=in.read(by)) != -1 ){
            	if(null == encode){
            		buffer.append(new String(by,0,size));
            	}
            	else{
            		buffer.append(new String(by,0,size,encode));
            	}
            }
         }catch(Exception ex){
        	log.error(ex);
        	ex.printStackTrace();
         } finally  {
                try{
                	if(null != in) {
                		in.close();
                	}
                	if(null != inputStream){
                		inputStream.close();
                	}
                }catch(Exception e){
                	e.printStackTrace();
                	e.printStackTrace();
                }
        }  

		return buffer;
	}
	/**
	 * 读取文件
	 * @param file
	 * @return
	 */
	public static StringBuffer read(File file,String encode){
		StringBuffer buffer = new StringBuffer();
		if(null != file && file.exists()){
			try{
				buffer = read(new FileInputStream(file),encode);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return buffer;
	}
	public static StringBuffer read(File file){
		StringBuffer buffer = new StringBuffer();
		if(null != file && file.exists()){
			try{
				//String encode = getFileEncode(file);
				buffer = read(new FileInputStream(file),"UTF-8");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return buffer;
	}
//	/**
//	 * 写文件
//	 * @param content
//	 * @param file
//	 * @param append 追加内容
//	 */
//	public static void writeFile(String content, File file, boolean append){
//		if(null == file || null == content){
//			return;
//		}
//		File dir = file.getParentFile();
//		if(!dir.exists()){
//			dir.mkdirs();
//		}
//		try{
//			if(!file.exists()){
//				file.createNewFile();
//			}
//			FileWriter fw = new FileWriter(file,append);
//			fw.write(content);
//			fw.close();  
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	/**
	 * 
	 * @param content 写入内容
	 * @param file 文件
	 * @param encode 编码
	 * @param append 是否追加
	 */
	public static void write(String content, File file, String encode, boolean append) {
		if(null == file){
			return;
		}
		File dir = file.getParentFile();
		if(!dir.exists()){
			dir.mkdirs();
		}
		FileOutputStream fos = null; 
		OutputStreamWriter osw = null;
		try { 
			fos = new FileOutputStream(file, append);	
			osw = new OutputStreamWriter(fos, encode); 
			if(append){
				osw.append(content);
			}else{
				osw.write(content);
			}
			osw.flush(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		}finally{
			try{
				osw.close();
				fos.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void write(String content, File file, String encode) {
		write(content, file, encode, false);
	}

	public static void write(String content, File file, boolean append) {
		write(content, file, "UTF-8", append);
	}
	public static void write(String content, File file) {
		write(content, file, "UTF-8", false);
	}
	
	/**
	 * 创建文件
	 * @param fileDir
	 * @param fileName
	 * @param over 是否清空已存在的同名文件
	 * @return
	 */
	public static boolean createFile(String fileDir, String fileName, boolean over){
		String filePath = createFullFilePath(fileDir, fileName);
		return createFile(filePath,over);
	}
	public static boolean createFile(String file, boolean over){
		return createFile(new File(file), over);
	}
	public static boolean createFile(File file, boolean over){
		if(null == file){
			return false;
		}
		try{
			File dir = file.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}
			if(file.exists()){
				if(over){
					file.delete();
				}else{
					return true;
				}
			}
			file.createNewFile();
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * 从URL中提取文件目录(删除查询参数)
	 * @param url
	 * @return
	 */
	public static String fetchPathByUrl(String url){
		int to = url.indexOf("?");
		if(to != -1)
			url = url.substring(0,to);
		return url;
	}
	/**
	 * 提取一个URL所在的目录
	 * @param path
	 * @return
	 */
	public static String fetchDirByUrl(String url){
		String dir = null;
		if(url.endsWith("/")){
			dir = url;
		}else if(isHttpFile(url)){
			int to = url.lastIndexOf("/");
			dir = url.substring(0,to);
		}else{
			dir = url;
		}
		return dir;
	}
	/**
	 * path是否包含文件名
	 * @param path
	 * @return
	 */
	private static boolean isHttpFile(String path){

		if(path.endsWith("/")){
			return false;
		}
		String head = "http://";
		int fr = head.length();
		int l1 = path.lastIndexOf("/");
		int l2 = path.lastIndexOf(".");
		//int l3 = path.length();
		if(l1 == -1){
			return false;
		}else if(l2>l1 && l2>fr){
			return true;
		}
		return false;
	}
	/**
	 * 提取url根目录
	 * @param url
	 * @return
	 */
	public static String getHostUrl(String url){
		url = url.replaceAll("http://","");
		int idx = url.indexOf("/");
		if(idx != -1){
			url = url.substring(0,idx);
		}
		url = "http://"+url;
		return url;
	}
	private static boolean endWithSeparator(String path){
		if(path.endsWith("/") || path.endsWith("\\")) return true;
		else return false;
	}
	private static boolean startWithSeparator(String path){
		if(path.startsWith("/") || path.startsWith("\\")) return true;
		else return false;
	}
	
//	/**
//	 * 获取文件编码格式
//	 * 需要包antlr.jar cpdetector.jar chardet.jar
//	 * @param file
//	 * @return
//	 */
//	public static String getFileEncode(File file){
//		String encode = "UTF-8";
//
//		Charset charset = null;
//		try {
//			CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
//		//	detector.add(JChardetFacade.getInstance());
//			charset = detector.detectCodepage(file.toURI().toURL());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (charset != null) {
//			encode = charset.name();
//		}
//
//		return encode;
//	}
	/**
	 * 不含后缀的文件名
	 * @param file
	 * @return
	 */
	public static String getSimpleFileName(File file){
		String name = null;
		if(null == file) return null;
		name = file.getName();
		int idx = name.lastIndexOf(".");
		name = name.substring(0,idx);
		return name;
	}
	
	public static String getSimpleFileName(String file){
		return getSimpleFileName(new File(file));
	}
	public static String getFileName(String file){
		return getFileName(new File(file));
	}
	public static String getFileName(File file){
		if(null != file)
			return file.getName();
		return null;
	}
	public static String getSuffixFileName(File file){
		String name = null;
		if(null == file) return null;
		name = file.getName();
		int idx = name.lastIndexOf(".");
		name = name.substring(idx+1);
		return name;
	}
	public static String getSuffixFileName(String file){
		return getSuffixFileName(new File(file));
	}
	/**
	 * 复制文件  源文件  目标文件
	 * @param src
	 * @param dst
	 * @return
	 */
	public static boolean copy(File srcPath, File dstPath){
		boolean result = true;
		if (srcPath.isDirectory()){
			if (!dstPath.exists()){
				result = dstPath.mkdirs();
			}
			String[] files = srcPath.list();
			for(int i = 0; i < files.length; i++){
				copy(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		} else{
			if(!srcPath.exists()){
				result = false;
			}else{
				File dir = dstPath.getParentFile();
				if(!dir.exists()){
					dir.mkdirs();
				}
				InputStream in = null;
				OutputStream out = null;
				try{
					if(dstPath.isDirectory()){
						File dirs = dstPath.getParentFile();
						if(!dirs.exists()){
							dirs.mkdirs();
						}
					}
					dstPath.createNewFile();
					in = new FileInputStream(srcPath);
					out = new FileOutputStream(dstPath);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}catch(Exception e){
					e.printStackTrace();
					e.printStackTrace();
					result = false;
				}finally{
					if(null != in){
						try{
							in.close();
						}catch(Exception ex){
							log.error(ex);
						}
					}
					if(null != out){
						try{
							out.close();
						}catch(Exception ex){
							log.error(ex);
						}
					}
				}
			}
		}
		return result;
	}
    
	/**
	 * 读取当前目录及子目录下所有文件
	 * @param dir
	 * @return
	 */
	public static List<File> getAllChildrenFile(File dir, String ...subbfixs){
		List<File> list = new ArrayList<File>();
		if(dir.isFile()){
			if(filterByType(dir, subbfixs)){
				list.add(dir);
			}
			return list;
		}
		File[] children = dir.listFiles();
		if(null != children){
			int size = children.length;
			for(int i=0; i<size; i++){
				File child = children[i];
				if(child.isHidden()){
					continue;
				}
				if(child.isFile()){
					if(filterByType(child,subbfixs)){
						list.add(child);
					}
				}else{
					List<File> tmpList = getAllChildrenFile(child,subbfixs);
					list.addAll(tmpList);
				}
			}
		}
		return list;
	}
	/**
	 * 读取当前目录及子目录下所有子目录
	 * @param dir
	 * @return
	 */
	public static List<File> getAllChildrenDirectory(File dir){
		List<File> list = new ArrayList<File>();
		if(dir.isFile()){
			return list;
		}
		File[] children = dir.listFiles();
		if(null != children){
			int size = children.length;
			for(int i=0; i<size; i++){
				File child = children[i];
				if(child.isHidden()){
					continue;
				}
				if(child.isDirectory()){
					if(null == child.listFiles() || child.listFiles().length == 0){
						list.add(child);
					}else{
						list.addAll(getAllChildrenDirectory(child));
					}
				}
			}
		}
		return list;
	}
	/**
	 * 
	 * @param dir
	 * @param types
	 * @return
	 */
	public static List<File> getChildrenFile(File dir, String ...types){
		List<File> list = new ArrayList<File>();
		File files[] = dir.listFiles();
		if(null != files){
			for(File file:files){
				if(file.isFile() && filterByType(file,types))
					list.add(file);
			}
		}
		return list;
	}
	/**
	 * ZIP文件中的所有子文件
	 * @param zip
	 * @param types
	 * @return
	 */
	public static List<File> getZipAllChildrenFile(File zip, String ...types){
		List<File> list = new ArrayList<File>();
		ZipInputStream in = null;
		try{
			in = new ZipInputStream(new FileInputStream(zip));
			ZipEntry entry = null;
			while((entry=in.getNextEntry()) != null){
				String path = zip.getAbsolutePath() + "!/" + entry.getName();
				File file = new File(path);
				if(filterByType(file, types)){
					list.add(file);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	/**
	 * 按类型(后缀)过滤文件
	 * @param file
	 * @param types
	 * @return
	 */
	public static boolean filterByType(File file, String ... types){
		if(null == file){
			return false;
		}
		if(null == types || types.length == 0){
			return true;
		}
		for(String type:types){
			String fileName = file.getAbsolutePath().toUpperCase();
			type = type.toUpperCase();
			if(RegularUtil.match(fileName, type, Regular.MATCH_MODE.MATCH)){
				return true;
			}
			if(!type.startsWith(".")){
				type = "." + type;
			}
			if(fileName.endsWith(type)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 后缀名
	 * @param fileName
	 * @return
	 */
	public static String parseSubName(String fileName){
		String result = null;
		if(null == fileName) return null;
		int idx = fileName.lastIndexOf(".");
		if(idx >0){
			result = fileName.substring(idx+1);
		}
		return result;
	}
	public static boolean exists(File file){
		if(null == file){
			return false;
		}
		return file.exists();
	}
	public static boolean exists(String file){
		return exists(new File(file));
	} 
	public static boolean isFile(File file){
		if(null == file){
			return false;
		}
		return file.isFile();
	}
	public static boolean isFile(String file){
		return isFile(new File(file));
	}
	/**
	 * 识别文件类型(文件格式)
	 * @param file
	 * @return
	 */
	public static String parseSubName(File file) {    
        String filetype = null;     
        byte[] b = new byte[50];     
        try {     
            InputStream is = new FileInputStream(file);     
            is.read(b);     
            filetype = getFileTypeByStream(b);     
            is.close();    
        } catch (Exception e) {     
            e.printStackTrace();     
        }     
        return filetype;     
    } 
	public final static String getFileTypeByStream(byte[] b){     
        String filetypeHex = String.valueOf(getFileHexString(b));     
        Iterator<Entry<String, String>> entryiterator = Source.FILE_TYPE_MAP.entrySet().iterator();     
        while (entryiterator.hasNext()) {     
            Entry<String,String> entry =  entryiterator.next();     
            String fileTypeHexValue = entry.getValue();     
            if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {     
                return entry.getKey();     
            }     
        }     
        return null;     
    }     
	 public final static String getFileHexString(byte[] b)     
	    {     
	        StringBuilder stringBuilder = new StringBuilder();     
	        if (b == null || b.length <= 0)     
	        {     
	            return null;     
	        }     
	        for (int i = 0; i < b.length; i++)     
	        {     
	            int v = b[i] & 0xFF;     
	            String hv = Integer.toHexString(v);     
	            if (hv.length() < 2)     
	            {     
	                stringBuilder.append(0);     
	            }     
	            stringBuilder.append(hv);     
	        }     
	        return stringBuilder.toString();     
	    }   
	 /**
	  * 删除目录
	  * @param dir
	  * @return
	  */
	public static boolean deleteDir(File dir) {
		if(null == dir){
			return false;
		}
		if (dir.isDirectory()) {
			String[] children = dir.list();
			if(null != children){
				for (int i=0; i<children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		log.warn("[文件删除][file:"+dir.getAbsolutePath()+"]");
		return dir.delete();
	}
	/**
	 * 计算文件行数
	 * @param file
	 * @param subbfix 如果file是目录, 只统计其中subbfixs结尾的文件
	 * @return
	 */
	public static int calculateLine(File file, String ... subbfixs){
		int size = 0;
		if(null == file || !file.exists()){
			return size;
		}
		if(file.isDirectory()){
			List<File> files = FileUtil.getAllChildrenFile(file, subbfixs);
			for(File item:files){
				size += calculateLine(item);
			}
		}
		try{
			LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
			lineNumberReader.skip(file.length());
            size += lineNumberReader.getLineNumber();
            lineNumberReader.close();
        }catch(Exception e){
        	
        }
        return size;
	}
	/**
	 * 压缩文件
	 * @param zip
	 * @param srcs
	 * @return
	 */
	public static boolean zip(File zip, File... srcs) {
		List<File> files = new ArrayList<File>();
		for (File src:srcs) {
			files.add(src);
		}
		return zip(zip,files);
	}
	public static boolean zip(File zip, List<File> srcs) {
		boolean result = false;
		byte[] buf = new byte[1024];
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
			for (File src:srcs) {
				FileInputStream in = new FileInputStream(src);
				out.putNextEntry(new ZipEntry(src.getName()));
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.closeEntry();
				in.close();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
    /**
    * 获取单个文件的MD5值！
    * @param file
    * @return
    */
	public static String md5(File file){
		return MD5Util.getFileMD5(file);
	}
    /**
    * 获取文件夹中文件的MD5值
    * @param file
    * @param recursion ;true递归子目录中的文件
    * @return
    */
    public static Map<String, String> md5(File file, boolean recursion) {
    	return MD5Util.getDirMD5(file, recursion);
    }
}