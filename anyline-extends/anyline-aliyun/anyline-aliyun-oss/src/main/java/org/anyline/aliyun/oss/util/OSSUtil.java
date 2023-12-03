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


package org.anyline.aliyun.oss.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import org.anyline.net.HttpUtil;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class OSSUtil {
	private static final Logger log = LoggerFactory.getLogger(OSSUtil.class);
	private OSSClient client = null;
	private OSSConfig config = null;
	private static Hashtable<String, OSSUtil> instances = new Hashtable<String, OSSUtil>();


	static {
		Hashtable<String, AnylineConfig> configs = OSSConfig.getInstances();
		for(String key:configs.keySet()){
			instances.put(key, getInstance(key));
		}
	}
	public static Hashtable<String, OSSUtil> getInstances(){
		return instances;
	}

	public OSSUtil(){}
	public OSSUtil(String endpoint, String bucket, String account, String password){
        OSSConfig config = new OSSConfig();
        config.ENDPOINT = endpoint;
        config.ACCESS_ID = account;
        config.ACCESS_SECRET = password;
        config.BUCKET = bucket;
        this.config = config;
        client = new OSSClient(config.ENDPOINT, config.ACCESS_ID, config.ACCESS_SECRET);
    }

	public static OSSUtil getInstance() {
		return getInstance(OSSConfig.DEFAULT_INSTANCE_KEY);
	}

	public OSSClient getClient() {
		return client;
	}
	public void setClient(OSSClient client) {
		this.client = client;
	}
    public OSSConfig getConfig(){
        return config;
    }
    public void setConfig(OSSConfig config){
        this.config = config;
    }
	@SuppressWarnings("deprecation")
	public static OSSUtil getInstance(String key) {
		if (BasicUtil.isEmpty(key)) {
			key = OSSConfig.DEFAULT_INSTANCE_KEY;
		}
		OSSUtil util = instances.get(key);
		if (null == util) {
			OSSConfig config = OSSConfig.getInstance(key);
			if(null != config) {
				util = new OSSUtil();
				util.config = config;
				util.client = new OSSClient(config.ENDPOINT, config.ACCESS_ID, config.ACCESS_SECRET);
				instances.put(key, util);
			}
		}
		return util;
	}
	/**
	 * 上传文件或目录
	 * @param file  file
	 * @param path  path
	 * @return String
	 */
	public String upload(File file, String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		String result = null;
		if(null != file && file.exists() && file.isDirectory()){
			List<File> files = FileUtil.getAllChildrenFile(file);
			for(File item:files){
				String itemPath = FileUtil.merge(path, item.getAbsolutePath().replace(file.getAbsolutePath(), "")).replace("\\", "/");
				String url = upload(item, itemPath);
				if(null == result){
					result = url;
				}else{
					result += "," + url;
				}
			}
		}else{
			result = createUrl(path);
			client.putObject(config.BUCKET, path, file);
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
				log.info("[oss upload file][result:true][file:{}][url:{}]",file.getAbsolutePath(), result);
			}
		}
		return result;
	}
	public String upload(URL url, String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		try {
			client.putObject(config.BUCKET, path, url.openStream());
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
				log.info("[oss upload file][result:true][file:{}]",path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createUrl(path);
	}
	public String upload(InputStream in, String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		client.putObject(config.BUCKET, path, in);
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
			log.info("[oss upload file][result:true][file:{}]",path);
		}
		return createUrl(path);
	}
	public boolean download(File dir){
		return download(dir,"");
	}
	/**
	 * 文件列表
	 * @param prefix  前缀
	 * @return List
	 */
	public List<String> list(String prefix){
		List<String> list = new ArrayList<>();
		if(null == prefix){
			prefix = "";
		}
		if(prefix.startsWith("/")){
			prefix = prefix.substring(1);
		}
		final int maxKeys = 200;
		String nextMarker = null;
		ObjectListing objectListing;
		do {
		    objectListing = client.listObjects(new ListObjectsRequest(config.BUCKET).withPrefix(prefix).withMarker(nextMarker).withMaxKeys(maxKeys));
		    List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
		    for (OSSObjectSummary s : sums) {
		    	String key = s.getKey();
		    	if(key.endsWith("/")){
		    		continue;
		    	}
		    	list.add(key);
		    }
		    nextMarker = objectListing.getNextMarker();

		} while (objectListing.isTruncated());
		return list;
	}
	public List<String> list(){
		return list("");
	}
	/**
	 * 下载prefix目录下的所有文件到本地dir目录
	 * @param dir  目录
	 * @param prefix  前缀
	 * @return boolean
	 */
	public boolean download(File dir, String prefix){
		if(null == prefix){
			prefix = "";
		}
		if(prefix.startsWith("/")){
			prefix = prefix.substring(1);
		}
		final int maxKeys = 200;
		String nextMarker = null;
		ObjectListing objectListing;
		do {
		    objectListing = client.listObjects(new ListObjectsRequest(config.BUCKET)
		    .withPrefix(prefix).withMarker(nextMarker).withMaxKeys(maxKeys));
		    List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
		    for (OSSObjectSummary s : sums) {
		    	String key = s.getKey();
		    	if(key.endsWith("/")){
		    		continue;
		    	}
		        File file = new File(dir, key);
		        File parent = file.getParentFile();
		        if(null != parent && !parent.exists()){
		        	parent.mkdirs();
		        }
		        try{
		        	client.getObject(new GetObjectRequest(config.BUCKET, key), file);
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
		        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
		        	log.info("[oss download file][local:{}][remote:{}]",file.getAbsolutePath(),key);
		        }
		    }
		    nextMarker = objectListing.getNextMarker();

		} while (objectListing.isTruncated());

		return true;
	}
	/**
	 * 文件是否存在
	 * @param path  文件路径
	 * @return boolean
	 */
	public boolean exists(String path){
		boolean result = false;
		if(null == path){
			path = "";
		}
		String key = key(path);
		try{
			result = client.doesObjectExist(config.BUCKET,key);
		}catch(Exception e){}
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
			log.info("[check exists][path:{}][key:{}]", path, key);
		}
		return result;
	}
	public boolean delete(String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		boolean result = false;
		try{
			String key = key(path);
			client.deleteObject(config.BUCKET, key);
			log.info("[oss delete file][result:true][file:{}]", path);
			result = true;
		}catch(Exception e){
			log.warn("[oss delete file][result:true][file:{}]",path);
			result = false;
		}
		return result;
	}
	public OSSObject get(String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		try{
			path = path.replace("http://"+config.BUCKET+"."+config.ENDPOINT+"/", "");
			return client.getObject(config.BUCKET, path);
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * 最后修改时间
	 * @param path  文件路径
	 * @return Date
	 */
	public Date getLastModified(String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		try{
			path = path.replace("http://"+config.BUCKET+"."+config.ENDPOINT+"/", "");
			OSSObject obj = client.getObject(config.BUCKET, path);
			if(null == obj){
				return null;
			}else{
				return obj.getObjectMetadata().getLastModified();
			}
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * 最后修改时间
	 * @param path  path
	 * @param format 日期格式
	 * @return String
	 */
	public String getLastModified(String path, String format){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		Date date = getLastModified(path);
		if(null == date){
			return "";
		}
		return DateUtil.format(date, format);
	}
	/**
	 * 是否过期
	 * @param path  path
	 * @param millisecond  millisecond
	 * @return boolean
	 */
	public boolean isExpire(String path, long millisecond){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		Date date = getLastModified(path);
		if(null == date){
			return false;
		}
		return DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date) > millisecond;
	}
	/**
	 * 创建完整url
	 * @param path  path
	 * @return String
	 */
	private String createUrl(String path){
		if(null == path){
			path = "";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		String result = "";
		result = "http://"+config.BUCKET+"."+config.ENDPOINT;
		result = HttpUtil.mergePath(result, path);
		return result;
	}
	public Map<String, String> signature(String dir) {
		return signature(dir, config.EXPIRE_SECOND);
	}
	public Map<String, String> signature(String dir, int second) {
		if(second == 0){
			second = config.EXPIRE_SECOND;
		}
		String host = "";
		if(config.BUCKET.startsWith("http")){
			host = config.BUCKET + "." + config.ENDPOINT;
		}else{
			host = "https://" + config.BUCKET + "." + config.ENDPOINT;
		}
		Map<String, String> result = new HashMap<String, String>();
		try {
	        String postPolicy = policy(dir,second);
	        byte[] binaryData = postPolicy.getBytes("utf-8");
	        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
	        String postSignature = client.calculatePostSignature(postPolicy);
	        result.put("accessid", config.ACCESS_ID);
	        result.put("policy", encodedPolicy);
	        result.put("signature", postSignature);
	        result.put("dir", dir);
	        result.put("host", host);
	        result.put("expire", String.valueOf((System.currentTimeMillis() + second * 1000)/1000));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public String policy(String dir,long second){
		String result = null;
		PolicyConditions policyConds = new PolicyConditions();
		policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
	    long expireEndTime = System.currentTimeMillis() + second * 1000;
	    Date expiration = new Date(expireEndTime);
	    result = client.generatePostPolicy(expiration, policyConds);
		return result;
	}
	public String key(String key){
		if(null != key){
			if(key.contains(config.ENDPOINT)){
				key = key.substring(key.indexOf(config.ENDPOINT)+config.ENDPOINT.length()+1);
			}
		}
		return key;
	}
}
