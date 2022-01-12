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


package org.anyline.util; 
 
 
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
 
public class FileUtil { 
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class); 
	public final static int PATH_TYPE_JAR = 0; 

	/**
	 * 合成path
	 * @param paths paths
	 * @return String
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
	 * 目录分隔符 
	 * @return String
	 */ 
	public static String getFileSeparator(){ 
		return System.getProperty("file.separator"); 
	} 
	 
	/** 
	 * 判断当前应用程序的目录类别 0-jar包形式 
	 * @param dst  dst
	 * @return int
	 */ 
	@SuppressWarnings("rawtypes")
	public static int getPathType(Class dst){ 
		int type = -1; 
		try{ 
			String path = dst.getResource("").getPath(); 
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
	public static int getPathType(String path){
		int type = -1;
		if(path.indexOf(".jar!") != -1){
			//jar 目录
			type = 0;
		}else{
			//其他目录
			type = 1;
		}
		return type;
	}
	/** 
	 * 读取输入流 
	 * @param input  input
	 * @param encode  encode
	 * @return StringBuffer
	 */ 
	public static StringBuffer read(InputStream input, String encode){ 
		StringBuffer buffer = new StringBuffer(); 
		int BUFFER_SIZE = 1024 * 8; 
		 
		BufferedInputStream in = null ;
         
         try  { 
        	 if(input.available() <=0){ 
        		 return buffer; 
        	 } 
        	 if(BUFFER_SIZE>input.available()){ 
        		 BUFFER_SIZE = input.available(); 
        	 } 
             in = new BufferedInputStream(input, BUFFER_SIZE);
             input.available(); 
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
        	log.error(ex.getMessage());
        	ex.printStackTrace(); 
         } finally  { 
                try{ 
                	if(null != in) {
                		in.close();
                	}
                	if(null != input){
                		input.close();
                	} 
                }catch(Exception e){ 
                	e.printStackTrace();
                } 
        }   
 
		return buffer; 
	}

	public static StringBuffer read(InputStream input){
		StringBuffer buffer = new StringBuffer();
		try  {
			if(input.available() <=0){
				return buffer;
			}
			input.available();
			BufferedReader br=new BufferedReader(new InputStreamReader(input));
			String line=null;
			while((line=br.readLine())!=null){
				buffer.append(line);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		} finally  {
			try{
				if(null != input){
					input.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return buffer;
	}
	public static byte[] file2byte(File file){
		byte[] buffer = null;
		try{
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1)
			{
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		}catch (Exception e){
			e.printStackTrace();
		}
		return buffer;
	}
	/** 
	 * 读取文件 
	 * @param file  file
	 * @param encode  encode
	 * @return StringBuffer
	 */ 
	public static StringBuffer read(File file,String encode){
		StringBuffer buffer = new StringBuffer();
		if(null != file && file.exists()){
			try{
				if(file.getAbsolutePath().contains(".jar!")){
					buffer = readJar(file.getAbsolutePath());
				}else {
					buffer = read(new FileInputStream(file), encode);
				}
			}catch(Exception e){ 
				e.printStackTrace(); 
			}
		} 
		return buffer; 
	}

	public static StringBuffer readJar(String path )throws IOException {
		InputStream in=FileUtil.class.getResourceAsStream(path);
		Reader f = new InputStreamReader(in);
		BufferedReader fb = new BufferedReader(f);
		StringBuffer builder = new StringBuffer();
		String s = "";
		while((s = fb.readLine()) != null) {
			builder.append(s);
		}
		return builder;
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

	public static boolean write(InputStream is, File file){
		if(null == file || null == is){
			return false;
		}
		try {
			if(!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			OutputStream os = new FileOutputStream(file);
			return write(is, os, true);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public static boolean write(InputStream is, OutputStream os){
		return write(is,os,true);
	}
	public static boolean write(InputStream is, OutputStream os, boolean close){
		BufferedInputStream bis = new BufferedInputStream(is);
		int len;
		byte[] arr = new byte[1024];
		try {
			while ((len = bis.read(arr)) != -1) {
				os.write(arr, 0, len);
				os.flush();
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally {
			if(close){
				try {
					os.close();
				}catch (Exception ex){

				}
				try {
					is.close();
				}catch (Exception ex){

				}
			}
		}
		return true;
	}
	/** 
	 * 创建文件 
	 * @param fileDir  fileDir
	 * @param fileName  fileName
	 * @param over 是否清空已存在的同名文件 
	 * @return boolean
	 */ 
	public static boolean create(String fileDir, String fileName, boolean over){ 
		String filePath = mergePath(fileDir, fileName); 
		return create(filePath,over); 
	} 
	public static boolean create(String file, boolean over){ 
		return create(new File(file), over); 
	} 
	public static boolean create(File file, boolean over){ 
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
	 * @param url  url
	 * @return String
	 */ 
	public static String fetchPathByUrl(String url){ 
		int to = url.indexOf("?"); 
		if(to != -1) 
			url = url.substring(0,to); 
		return url; 
	} 
	/** 
	 * 提取一个URL所在的目录 
	 * @param url  url
	 * @return String
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
	 * @param path  path
	 * @return boolean
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
	 * @param url  url
	 * @return String
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

	/**
	 * 不含后缀的文件名
	 * @param file file
	 * @return String
	 */ 
	public static String getSimpleFileName(File file){ 
		String name = null; 
		if(null == file) return null; 
		name = file.getName(); 
		int idx = name.lastIndexOf(".");
		if(idx != -1){ 
			name = name.substring(0,idx);
		} 
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
		if(idx != -1){
			name = name.substring(idx+1);
		}
		return name;
	}
	public static String getSuffixFileName(String file){
		return getSuffixFileName(new File(file));
	}
	/** 
	 * 复制文件  源文件  目标文件 
	 * @param src  src
	 * @param dst  dst
	 * @return boolean
	 */ 
	public static boolean copy(File src, File dst){ 
		boolean result = true; 
		if (src.isDirectory()){ 
			if (!dst.exists()){ 
				result = dst.mkdirs(); 
			} 
			String[] files = src.list(); 
			for(int i = 0; i < files.length; i++){ 
				copy(new File(src, files[i]), new File(dst, files[i])); 
			} 
		} else{ 
			if(!src.exists()){ 
				result = false; 
			}else{
				File dir = dst.getParentFile();
				if(!dir.exists()){
					dir.mkdirs();
				} 
				InputStream in = null; 
				OutputStream out = null; 
				try{ 
					if(!dst.isDirectory()){ 
						File dirs = dst.getParentFile(); 
						if(!dirs.exists()){ 
							dirs.mkdirs(); 
						} 
					}else{
						dst = new File(dst, src.getName());
					} 
					dst.createNewFile(); 
					in = new FileInputStream(src); 
					out = new FileOutputStream(dst); 
					byte[] buf = new byte[1024]; 
					int len; 
					while ((len = in.read(buf)) > 0) { 
						out.write(buf, 0, len); 
					} 
				}catch(Exception e){ 
					e.printStackTrace();
					result = false; 
				}finally{ 
					if(null != in){ 
						try{ 
							in.close(); 
						}catch(Exception ex){ 
							log.error(ex.getMessage()); 
						} 
					} 
					if(null != out){ 
						try{ 
							out.close(); 
						}catch(Exception ex){ 
							log.error(ex.getMessage()); 
						} 
					} 
				} 
			} 
		} 
		return result; 
	} 
     
	/** 
	 * 读取当前目录及子目录下所有文件 
	 * @param dir  dir
	 * @param subbfixs  subbfixs
	 * @return List
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
	 * @param dir  dir
	 * @return List
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
	 * @param dir  dir
	 * @param types  types
	 * @return List
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
	 * @param zip  zip
	 * @param types  types
	 * @return List
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
	 * @param file  file
	 * @param types  types
	 * @return boolean
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
	 * @param fileName  fileName
	 * @return String
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
	 * @param file  file
	 * @return return
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
        Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();      
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
	  * @param file  file
	  * @return boolean
	  */ 
	public static boolean delete(File file) {
		if(null == file){
			return false;
		}
		boolean result = true; 
		if (file.isDirectory()) { 
			File[] children = file.listFiles();
			if(null != children){ 
				for (File child:children) {
						delete(child); 
				}
			}
			result = file.delete();
			log.warn("[目录删除][result:"+result+"][file:"+file.getAbsolutePath()+"]"); 
		}else{
			result = file.delete();
			log.warn("[文件删除][result:"+result+"][file:"+file.getAbsolutePath()+"]");
		}
		return result;
	} 
	/** 
	 * 计算文件行数 
	 * @param file file
	 * @param subbfixs 如果file是目录, 只统计其中subbfixs结尾的文件 
	 * @return return
	 */ 
	public static int lines(File file, String ... subbfixs){
		int size = 0; 
		if(null == file || !file.exists()){ 
			return size; 
		}
		if(file.isDirectory()){
			List<File> files = FileUtil.getAllChildrenFile(file, subbfixs);
			for(File item:files){
				size += lines(item);
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
	 * @param zip  zip
	 * @param srcs  srcs
	 * @return return
	 */ 
	public static boolean zip(File zip, File... srcs) { 
		List<File> files = new ArrayList<File>(); 
		for (File src:srcs) { 
			files.add(src); 
		} 
		return zip(zip,files); 
	} 
	public static boolean zip(File zip, List<File> srcs) { 
		return ZipUtil.zip(srcs, zip); 
	}
    /**
    * 获取单个文件的MD5值！
    * @param file file
    * @return return
    */
	public static String md5(File file){
		return MD5Util.getFileMD5(file);
	}
    /**
    * 获取文件夹中文件的MD5值
    * @param file file
    * @param recursion  true递归子目录中的文件
    * @return return
    */
    public static Map<String, String> md5(File file, boolean recursion) {
    	return MD5Util.getDirMD5(file, recursion);
    }

    /**  
     * 读取输入流中的数据保存至指定目录  
     * @param is 输入流  
     * @param file 文件名  
	 * @return return
     */  
    public static boolean save(InputStream is, File file) {
    	if (BasicUtil.isEmpty(file)) {
			return false;
		}
    	long fr = System.currentTimeMillis();
    	File dir = file.getParentFile();
    	if(!dir.exists()){
    		dir.mkdirs();
    	}
        BufferedInputStream bis = null;
        BufferedOutputStream bos =null;
		try {
			bis = new BufferedInputStream(is);  
			bos = new BufferedOutputStream(new FileOutputStream(file));
	        int len = -1;  
	        while ((len = bis.read()) != -1) {  
	            bos.write(len);  
	            bos.flush();  
	        }  
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(null != bos) {
					bos.close();  
				}
			}catch(Exception e){}
			try{
				if(null != bis) {
					bis.close();  
				}
			}catch(Exception e){}
		}
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[save file][file:"+file.getAbsolutePath()+"][耗时:"+(System.currentTimeMillis()-fr)+"]");
		}
        return true;
    }  

    /**  
     * 读取输入流中的数据保存至指定目录  
     * @param is 输入流  
     * @param path 文件存储目录  
	 * @return return
     */  
    public static boolean save(InputStream is, String path) {
    	return save(is, new File(path));
    }

	/**
	 * @param total 全部
	 * @param finish 已完成
	 * @return return
	 */
	public static String progress(long total, long finish){
		String title = "";
		double rate = finish*100.00/total;
		String rateTitle = NumberUtil.format(rate, "0.00");
		if(finish>=total){
			rateTitle = "100";
		}
		title = length(finish) + "/" + length(total) + "("+rateTitle+"%)";
		return title;
	}
	/**
	 * 文件大小格式化
	 * @param b b
	 * @return return
	 */
	public static String length(long b){
		String result = "";
		if(b<1024){
			result = b+ "byte";
		}else if(b<1024L*1024){
			result = NumberUtil.format(b/1024.00,"0.00") + "kb";
		}else if(b<1024L*1024*1024){
			result = NumberUtil.format(b/1024.00/1024,"0.00") + "mb";
		}else if(b<1024L*1024*1024*1024){
			result = NumberUtil.format(b/1024.00/1024/1024,"0.00") + "gb";
		}else if(b<1024L*1024*1024*1024*1024){
			result = NumberUtil.format(b/1024.00/1024/1024/1024,"0.00") + "tb";
		}else if(b<1024L*1024*1024*1024*1024*1024){
			result = NumberUtil.format(b/1024.00/1024/1024/1024/1024,"0.00") + "pb";
		}
		return result;
	}
	/**
	 * 合并文件
	 * @param dst dst
	 * @param items items
	 */
	public static void merge(File dst, List<File> items){
		FileOutputStream os =null;
		FileInputStream is = null;
        try {
        	File dir = dst.getParentFile();
        	if(!dir.exists()){
        		dir.mkdirs();
        	}
            os = new FileOutputStream(dst);
            byte[] bytes = new byte[1024];
            int length = 0;
            for(File item:items){
                if(!item.exists()){
                    continue;
                }
                long fr = System.currentTimeMillis();
                is = new FileInputStream(item);
                while ((length = is.read(bytes)) != -1) {
                    os.write(bytes, 0, length);
                }
                log.warn("[合并文件][耗时:"+DateUtil.conversion(System.currentTimeMillis()-fr)+"][file:"+dst.getAbsolutePath()+"][item:"+item.getAbsolutePath()+"]");
            }
        }catch (Exception e){
        	e.printStackTrace();
        }finally{
        	try{
        		os.close();
        	}catch(Exception e){
        		
        	}
        }
    	try{
    		is.close();
    	}catch(Exception e){
    		
    	}
    }
	/**
	 * 合并文件
	 * @param dst dst
	 * @param dir dir
	 */
	public static void merge(File dst, File dir){
		FileOutputStream os =null;
		FileInputStream is = null;
        try {
        	File root = dst.getParentFile();
        	if(!root.exists()){
        		root.mkdirs();
        	}
            os = new FileOutputStream(dst);
            byte[] bytes = new byte[1024];
            int length = 0;
            List<File> items = getAllChildrenFile(dir);
            for(File item:items){
                if(!item.exists()){
                    continue;
                }
                long fr = System.currentTimeMillis();
                is = new FileInputStream(item);
                while ((length = is.read(bytes)) != -1) {
                    os.write(bytes, 0, length);
                }
                log.warn("[合并文件][耗时:"+DateUtil.conversion(System.currentTimeMillis()-fr)+"][file:"+dst.getAbsolutePath()+"][item:"+item.getAbsolutePath()+"]");
            }
        }catch (Exception e){
        	e.printStackTrace();
        }finally{
        	try{
        		os.flush();
        		os.close();
        	}catch(Exception e){
        		
        	}
        	try{
        		is.close();
        	}catch(Exception e){
        		
        	}
        }
    	try{
    		is.close();
    	}catch(Exception e){
    		
    	}
    }

	/**
	 * 文件拆分
	 * @param file file
	 * @param count count
	 */
	public static void split(File file, int count) {
	    FileInputStream fis = null;
	    FileOutputStream fos = null;
	    FileChannel input = null;
	    FileChannel out = null;
		try {
			fis = new FileInputStream(file);
			input = fis.getChannel();
			String fileName =  FileUtil.getSimpleFileName(file);
			String subName = FileUtil.getSuffixFileName(file);
			if(BasicUtil.isNotEmpty(subName)){
				subName = "."+subName;
			}
		    final long fileSize = input.size();
		    long average = fileSize / count;//平均值
		    long bufferSize = 1024; //缓存块大小
		    ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.valueOf(bufferSize + "")); // 申请一个缓存区
		    long startPosition = 0; //子文件开始位置
		    long endPosition = average < bufferSize ? 0 : average - bufferSize;//子文件结束位置
		    for (int i = 0; i < count; i++) {
		    	long fr = System.currentTimeMillis();
		        if (i + 1 != count) {
		            int read = input.read(byteBuffer, endPosition);// 读取数据
		            readW:
		            while (read != -1) {
		                byteBuffer.flip();//切换读模式
		                byte[] array = byteBuffer.array();
		                for (int j = 0; j < array.length; j++) {
		                    byte b = array[j];
		                    if (b == 10 || b == 13) { //判断\n\r
		                        endPosition += j;
		                        break readW;
		                    }
		                }
		                endPosition += bufferSize;
		                byteBuffer.clear(); //重置缓存块指针
		                read = input.read(byteBuffer, endPosition);
		            }
		        }else{
		            endPosition = fileSize; //最后一个文件直接指向文件末尾
		        }
		        File item = new File(file.getParent(), fileName+"_"+(i+1)+subName);
		        try{
			        fos = new FileOutputStream(item);
			        out = fos.getChannel();
			        input.transferTo(startPosition, endPosition - startPosition, out);//通道传输文件数据
		        }catch(Exception e){
		        	e.printStackTrace();
		        }finally{
		        	try{
				        out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
		        	try{
				        fos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
		        }
		        startPosition = endPosition + 1;
		        endPosition += average;
		        log.warn("[文件分割]["+(i+1)+"/"+count+"][耗时:"+DateUtil.conversion(System.currentTimeMillis()-fr)+"][src:"+file.getAbsolutePath()+"][item:"+item.getAbsolutePath()+"]");
		    }
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
		    try {
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		    try {
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	//HTTP 文件类型
		public final static List<String> httpFileExtend = new ArrayList<>();
		public final static List<String> httpFileType = new ArrayList<>();
		public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();   
		static{
			FILE_TYPE_MAP.put("jpg", "FFD8FF"); 	//JPEG (jpg)     
	        FILE_TYPE_MAP.put("png", "89504E47");   //PNG (png)     
	        FILE_TYPE_MAP.put("gif", "47494638");   //GIF (gif)     
	        FILE_TYPE_MAP.put("tif", "49492A00");   //TIFF (tif)     
	        FILE_TYPE_MAP.put("bmp", "424D"); 		//Windows Bitmap (bmp)     
	        FILE_TYPE_MAP.put("dwg", "41433130");   //CAD (dwg)     
	        FILE_TYPE_MAP.put("html", "68746D6C3E");//HTML (html)     
	        FILE_TYPE_MAP.put("rtf", "7B5C727466"); //Rich Text Format (rtf)     
	        FILE_TYPE_MAP.put("xml", "3C3F786D6C");     
	        FILE_TYPE_MAP.put("zip", "504B0304");     
	        FILE_TYPE_MAP.put("rar", "52617221");     
	        FILE_TYPE_MAP.put("psd", "38425053");   //Photoshop (psd)     
	        FILE_TYPE_MAP.put("eml", "44656C69766572792D646174653A");  //Email [thorough only] (eml)     
	        FILE_TYPE_MAP.put("dbx", "CFAD12FEC5FD746F");  //Outlook Express (dbx)     
	        FILE_TYPE_MAP.put("pst", "2142444E");  //Outlook (pst)     
	        FILE_TYPE_MAP.put("xls", "D0CF11E0");  //MS Word     
	        FILE_TYPE_MAP.put("doc", "D0CF11E0");  //MS Excel 注意：word 和 excel的文件头一样     
	        FILE_TYPE_MAP.put("mdb", "5374616E64617264204A");  //MS Access (mdb)     
	        FILE_TYPE_MAP.put("wpd", "FF575043"); //WordPerfect (wpd)      
	        FILE_TYPE_MAP.put("eps", "252150532D41646F6265");     
	        FILE_TYPE_MAP.put("ps", "252150532D41646F6265");     
	        FILE_TYPE_MAP.put("pdf", "255044462D312E");  //Adobe Acrobat (pdf)     
	        FILE_TYPE_MAP.put("qdf", "AC9EBD8F");  //Quicken (qdf)     
	        FILE_TYPE_MAP.put("pwl", "E3828596");  //Windows Password (pwl)     
	        FILE_TYPE_MAP.put("wav", "57415645");  //Wave (wav)     
	        FILE_TYPE_MAP.put("avi", "41564920");     
	        FILE_TYPE_MAP.put("ram", "2E7261FD");  //Real Audio (ram)     
	        FILE_TYPE_MAP.put("rm", "2E524D46");  //Real Media (rm)     
	        FILE_TYPE_MAP.put("mpg", "000001BA");  //     
	        FILE_TYPE_MAP.put("mov", "6D6F6F76");  //Quicktime (mov)     
	        FILE_TYPE_MAP.put("asf", "3026B2758E66CF11"); //Windows Media (asf)     
	        FILE_TYPE_MAP.put("mid", "4D546864");  //MIDI (mid)     
	        
			//文件编码
			//文件类型
			httpFileExtend.add("ai");
			httpFileType.add("application/postscript");
			httpFileExtend.add("aif");
			httpFileType.add("audio/x-aiff");                                                                       
			httpFileExtend.add("aifc");
			httpFileType.add("audio/x-aiff");                                                                       
			httpFileExtend.add("aiff");
			httpFileType.add("audio/x-aiff");                                                                       
			httpFileExtend.add("asc");
			httpFileType.add("text/plain");                                                                         
			httpFileExtend.add("au");
			httpFileType.add("audio/basic");                                                                        
			httpFileExtend.add("avi");
			httpFileType.add("video/x-msvideo");                                                                    
			httpFileExtend.add("bcpio");
			httpFileType.add("application/x-bcpio");                                                                
			httpFileExtend.add("bin");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("bmp");
			httpFileType.add("image/bmp");                                                                          
			httpFileExtend.add("cdf");
			httpFileType.add("application/x-netcdf");                                                               
			httpFileExtend.add("class");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("cpio");
			httpFileType.add("application/x-cpio");                                                                 
			httpFileExtend.add("cpt");
			httpFileType.add("application/mac-compactpro");                                                         
			httpFileExtend.add("csh");
			httpFileType.add("application/x-csh");                                                                  
			httpFileExtend.add("css");
			httpFileType.add("text/css");                                                                           
			httpFileExtend.add("dcr");
			httpFileType.add("application/x-director");                                                             
			httpFileExtend.add("dir");
			httpFileType.add("application/x-director");                                                             
			httpFileExtend.add("djv");
			httpFileType.add("image/vnd.djvu");                                                                     
			httpFileExtend.add("djvu");
			httpFileType.add("image/vnd.djvu");                                                                     
			httpFileExtend.add("dll");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("dms");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("doc");
			httpFileType.add("application/msword");                                                                 
			httpFileExtend.add("dvi");
			httpFileType.add("application/x-dvi");                                                                  
			httpFileExtend.add("dxr");
			httpFileType.add("application/x-director");                                                             
			httpFileExtend.add("eps");
			httpFileType.add("application/postscript");                                                             
			httpFileExtend.add("etx");
			httpFileType.add("text/x-setext");                                                                      
			httpFileExtend.add("exe");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("ez");
			httpFileType.add("application/andrew-inset");                                                           
			httpFileExtend.add("gif");
			httpFileType.add("image/gif");                                                                          
			httpFileExtend.add("gtar");
			httpFileType.add("application/x-gtar");                                                                 
			httpFileExtend.add("hdf");
			httpFileType.add("application/x-hdf");                                                                  
			httpFileExtend.add("hqx");
			httpFileType.add("application/mac-binhex40");                                                           
			httpFileExtend.add("htm");
			httpFileType.add("text/html");                                                                          
			httpFileExtend.add("html");
			httpFileType.add("text/html");                                                                          
			httpFileExtend.add("ice");
			httpFileType.add("x-conference/x-cooltalk");                                                            
			httpFileExtend.add("ief");
			httpFileType.add("image/ief");                                                                          
			httpFileExtend.add("iges");
			httpFileType.add("model/iges");                                                                         
			httpFileExtend.add("igs");
			httpFileType.add("model/iges");                                                                         
			httpFileExtend.add("jpe");
			httpFileType.add("image/jpeg");                                                                         
			httpFileExtend.add("jpeg");
			httpFileType.add("image/jpeg");                                                                         
			httpFileExtend.add("jpg");
			httpFileType.add("image/jpeg");                                                                         
			httpFileExtend.add("js");
			httpFileType.add("application/x-javascript");                                                           
			httpFileExtend.add("kar");
			httpFileType.add("audio/midi");                                                                         
			httpFileExtend.add("latex");
			httpFileType.add("application/x-latex");                                                                
			httpFileExtend.add("lha");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("lzh");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("m3u");
			httpFileType.add("audio/x-mpegurl");                                                                    
			httpFileExtend.add("man");
			httpFileType.add("application/x-troff-man");                                                            
			httpFileExtend.add("me");
			httpFileType.add("application/x-troff-me");                                                             
			httpFileExtend.add("mesh");
			httpFileType.add("model/mesh");                                                                         
			httpFileExtend.add("mid");
			httpFileType.add("audio/midi");                                                                         
			httpFileExtend.add("midi");
			httpFileType.add("audio/midi");                                                                         
			httpFileExtend.add("mif");
			httpFileType.add("application/vnd.mif");                                                                
			httpFileExtend.add("mov");
			httpFileType.add("video/quicktime");                                                                    
			httpFileExtend.add("movie");
			httpFileType.add("video/x-sgi-movie");                                                                  
			httpFileExtend.add("mp2");
			httpFileType.add("audio/mpeg");                                                                         
			httpFileExtend.add("mp3");
			httpFileType.add("audio/mpeg");                                                                         
			httpFileExtend.add("mpe");
			httpFileType.add("video/mpeg");                                                                         
			httpFileExtend.add("mpeg");
			httpFileType.add("video/mpeg");                                                                         
			httpFileExtend.add("mpg");
			httpFileType.add("video/mpeg");                                                                         
			httpFileExtend.add("mpga");
			httpFileType.add("audio/mpeg");                                                                         
			httpFileExtend.add("ms");
			httpFileType.add("application/x-troff-ms");                                                             
			httpFileExtend.add("msh");
			httpFileType.add("model/mesh");                                                                         
			httpFileExtend.add("mxu");
			httpFileType.add("video/vnd.mpegurl");                                                                  
			httpFileExtend.add("nc");
			httpFileType.add("application/x-netcdf");                                                               
			httpFileExtend.add("oda");
			httpFileType.add("application/oda");                                                                    
			httpFileExtend.add("pbm");
			httpFileType.add("image/x-portable-bitmap");                                                            
			httpFileExtend.add("pdb");
			httpFileType.add("chemical/x-pdb");                                                                     
			httpFileExtend.add("pdf");
			httpFileType.add("application/pdf");                                                                    
			httpFileExtend.add("pgm");
			httpFileType.add("image/x-portable-graymap");                                                           
			httpFileExtend.add("pgn");
			httpFileType.add("application/x-chess-pgn");                                                            
			httpFileExtend.add("png");
			httpFileType.add("image/png");                                                                          
			httpFileExtend.add("pnm");
			httpFileType.add("image/x-portable-anymap");                                                            
			httpFileExtend.add("ppm");
			httpFileType.add("image/x-portable-pixmap");                                                            
			httpFileExtend.add("ppt");
			httpFileType.add("application/vnd.ms-powerpoint");                                                      
			httpFileExtend.add("ps");
			httpFileType.add("application/postscript");                                                             
			httpFileExtend.add("qt");
			httpFileType.add("video/quicktime");                                                                    
			httpFileExtend.add("ra");
			httpFileType.add("audio/x-realaudio");                                                                  
			httpFileExtend.add("ram");
			httpFileType.add("audio/x-pn-realaudio");                                                               
			httpFileExtend.add("ras");
			httpFileType.add("image/x-cmu-raster");                                                                 
			httpFileExtend.add("rgb");
			httpFileType.add("image/x-rgb");                                                                        
			httpFileExtend.add("rm");
			httpFileType.add("audio/x-pn-realaudio");                                                               
			httpFileExtend.add("roff");
			httpFileType.add("application/x-troff");                                                                
			httpFileExtend.add("rpm");
			httpFileType.add("audio/x-pn-realaudio-plugin");                                                        
			httpFileExtend.add("rtf");
			httpFileType.add("text/rtf");                                                                           
			httpFileExtend.add("rtx");
			httpFileType.add("text/richtext");                                                                      
			httpFileExtend.add("sgm");
			httpFileType.add("text/sgml");                                                                          
			httpFileExtend.add("sgml");
			httpFileType.add("text/sgml");                                                                          
			httpFileExtend.add("sh");
			httpFileType.add("application/x-sh");                                                                   
			httpFileExtend.add("shar");
			httpFileType.add("application/x-shar");                                                                 
			httpFileExtend.add("silo");
			httpFileType.add("model/mesh");                                                                         
			httpFileExtend.add("sit");
			httpFileType.add("application/x-stuffit");                                                              
			httpFileExtend.add("skd");
			httpFileType.add("application/x-koan");                                                                 
			httpFileExtend.add("skm");
			httpFileType.add("application/x-koan");                                                                 
			httpFileExtend.add("skp");
			httpFileType.add("application/x-koan");                                                                 
			httpFileExtend.add("skt");
			httpFileType.add("application/x-koan");                                                                 
			httpFileExtend.add("smi");
			httpFileType.add("application/smil");                                                                   
			httpFileExtend.add("smil");
			httpFileType.add("application/smil");                                                                   
			httpFileExtend.add("snd");
			httpFileType.add("audio/basic");                                                                        
			httpFileExtend.add("so");
			httpFileType.add("application/octet-stream");                                                           
			httpFileExtend.add("spl");
			httpFileType.add("application/x-futuresplash");                                                         
			httpFileExtend.add("src");
			httpFileType.add("application/x-wais-source");                                                          
			httpFileExtend.add("sv4cpio");
			httpFileType.add("application/x-sv4cpio");                                                              
			httpFileExtend.add("sv4crc");
			httpFileType.add("application/x-sv4crc");                                                               
			httpFileExtend.add("swf");
			httpFileType.add("application/x-shockwave-flash");                                                      
			httpFileExtend.add("t");
			httpFileType.add("application/x-troff");                                                                
			httpFileExtend.add("tar");
			httpFileType.add("application/x-tar");                                                                  
			httpFileExtend.add("tcl");
			httpFileType.add("application/x-tcl");                                                                  
			httpFileExtend.add("tex");
			httpFileType.add("application/x-tex");                                                                  
			httpFileExtend.add("texi");
			httpFileType.add("application/x-texinfo");                                                              
			httpFileExtend.add("texinfo");
			httpFileType.add("application/x-texinfo");                                                              
			httpFileExtend.add("tif");
			httpFileType.add("image/tiff");                                                                         
			httpFileExtend.add("tiff");
			httpFileType.add("image/tiff");                                                                         
			httpFileExtend.add("tr");
			httpFileType.add("application/x-troff");                                                                
			httpFileExtend.add("tsv");
			httpFileType.add("text/tab-separated-values");                                                          
			httpFileExtend.add("txt");
			httpFileType.add("text/plain");                                                                         
			httpFileExtend.add("ustar");
			httpFileType.add("application/x-ustar");                                                                
			httpFileExtend.add("vcd");
			httpFileType.add("application/x-cdlink");                                                               
			httpFileExtend.add("vrml");
			httpFileType.add("model/vrml");                                                                         
			httpFileExtend.add("wav");
			httpFileType.add("audio/x-wav");                                                                        
			httpFileExtend.add("wbmp");
			httpFileType.add("image/vnd.wap.wbmp");                                                                 
			httpFileExtend.add("wbxml");
			httpFileType.add("application/vnd.wap.wbxml");                                                          
			httpFileExtend.add("wml");
			httpFileType.add("text/vnd.wap.wml");                                                                   
			httpFileExtend.add("wmlc");
			httpFileType.add("application/vnd.wap.wmlc");                                                           
			httpFileExtend.add("wmls");
			httpFileType.add("text/vnd.wap.wmlscript");                                                             
			httpFileExtend.add("wmlsc");
			httpFileType.add("application/vnd.wap.wmlscriptc");                                                     
			httpFileExtend.add("wrl");
			httpFileType.add("model/vrml");                                                                         
			httpFileExtend.add("xbm");
			httpFileType.add("image/x-xbitmap");                                                                    
			httpFileExtend.add("xht");
			httpFileType.add("application/xhtml+xml");                                                              
			httpFileExtend.add("xhtml");
			httpFileType.add("application/xhtml+xml");                                                              
			httpFileExtend.add("xls");
			httpFileType.add("application/vnd.ms-excel");                                                           
			httpFileExtend.add("xml");
			httpFileType.add("text/xml");                                                                           
			httpFileExtend.add("xpm");
			httpFileType.add("image/x-xpixmap");                                                                    
			httpFileExtend.add("xsl");
			httpFileType.add("text/xml");                                                                           
			httpFileExtend.add("xwd");
			httpFileType.add("image/x-xwindowdump");                                                                
			httpFileExtend.add("xyz");
			httpFileType.add("chemical/x-xyz");                                                                     
			httpFileExtend.add("zip");                           
			httpFileType.add("application/zip");
		}
}
