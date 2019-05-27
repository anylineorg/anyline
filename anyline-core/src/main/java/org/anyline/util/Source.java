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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Source {
	private static final Logger log = Logger.getLogger(Source.class);
	private String url;					//URL
	private String backFileCd;			//备份文件CD
	private int status;
	private String text;				//文本
	private String fileType;			//文件类型
	private String encode;				//编码
	private String contentType;			//
	private long lastModified;			//最后修改时间毫秒
	private String parser;				//解析器CD
	private String host;
	private Map<String,String> headers = new HashMap<String,String>();
	private Map<String,HttpCookie> cookies = new HashMap<String,HttpCookie>();

	private Map<String,String> seed;
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
		try{
			fileType = contentType.split(";")[0];
		}catch(Exception e){
			fileType = "text/html";
			log.error("setContentType$parse content type("+contentType+")");
		}
		try{
			String tmps[] = contentType.split("=");
			if(tmps.length>1){
				encode = tmps[1].trim();
			}
		}catch(Exception e){
			encode =null;
		}
		
	}
	/**
	 * 根据http文件头信息 解析文件类型
	 * @param contentType
	 * @return
	 */
	public static String parseHttpFileExtend(String contentType){
		String fileType = null;
		try{
			fileType = contentType.split(";")[0];
			fileType = fileType.toLowerCase();
			fileType = httpFileExtend.get(httpFileType.indexOf(fileType));
		}catch(Exception e){
			log.error("parseHttpFileType:\n"+e);
			fileType = "";
		}
		return fileType;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, HttpCookie> getCookies() {
		return cookies;
	}
	public void setCookies(Map<String, HttpCookie> cookies) {
		this.cookies = cookies;
	}
	public HttpCookie getCookie(String key){
		return cookies.get(key);
	}
	public void setCookie(HttpCookie cookie){
		if(null != cookie){
			cookies.put(cookie.getKey(), cookie);
		}
	}
	public String getCookieValue(String key){
		HttpCookie cookie = getCookie(key);
		if(null != cookie){
			return cookie.getValue();
		}
		return null;
	}
	public String getHeader(String key){
		return headers.get(key);
	}

	public static List<String> encodeList = new ArrayList<String>();
	//HTTP 文件类型
	public final static List<String> httpFileExtend = new ArrayList<String>();
	public final static List<String> httpFileType = new ArrayList<String>();
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
	public long getLastModified() {
		return lastModified;
	}
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	public String getBackFileCd() {
		return backFileCd;
	}
	public void setBackFileCd(String backFileCd) {
		this.backFileCd = backFileCd;
	}

	public Map<String,String> getSeed() {
		return seed;
	}
	public void setSeed(Map<String,String> seed) {
		this.seed = seed;
	}
	public String getParser() {
		return parser;
	}
	public void setParser(String parser) {
		this.parser = parser;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}