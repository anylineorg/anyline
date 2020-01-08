package org.anyline.net; 
 
 
public interface DownloadProgress { 
	/** 
	 * 加载历史进度 
	 * @param url  url
	 * @param thread	线程号  thread	线程号
	 * @param total		本次需下载  total		本次需下载
	 * @param past		历史进度  past		历史进度
	 */ 
	public void init(String url, String thread, long total, long past); 
	/** 
	 * 每次下载长度 
	 * @param url  url
	 * @param thread	线程号  thread	线程号
	 * @param finish	本次循环下载长度  finish	本次循环下载长度
	 */ 
	public void step(String url, String thread, long finish); 
	/** 
	 * 下载完成 
	 * @param url  url
	 * @param thread  thread
	 */ 
	public void finish(String url, String thread); 
	/** 
	 * 下载异常 
	 * @param url  url
	 * @param thread  thread
	 * @param code  code
	 * @param message  message
	 */ 
	public void error(String url, String thread, int code, String message); 
 
	public void setErrorListener(DownloadListener listener); 
	public void setFinishListener(DownloadListener listener); 
	public int getAction(); 
	public void stop(); 
} 
