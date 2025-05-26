/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.net;
 
 
public interface DownloadProgress {
	/** 
	 * 加载历史进度 
	 * @param url  url
	 * @param thread	线程号
	 * @param total		本次需下载
	 * @param past		历史进度
	 */ 
	public void init(String url, String thread, long total, long past);
	/** 
	 * 每次下载长度 
	 * @param url  url
	 * @param thread	线程号
	 * @param finish	本次循环下载长度
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
