package org.anyline.aliyun.oss.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;

public class OSSUtil {
	private static Logger log = Logger.getLogger(OSSUtil.class);
	private OSSClient client = null;
	private OSSConfig config = null;
	private static Hashtable<String, OSSUtil> instances = new Hashtable<String, OSSUtil>();
	
	
	public OSSConfig getConfig(){
		return config;
	}
	public static OSSUtil getInstance() {
		return getInstance("default");
	}

	public static OSSUtil getInstance(String key) {
		if (BasicUtil.isEmpty(key)) {
			key = "default";
		}
		OSSUtil util = instances.get(key);
		if (null == util) {
			util = new OSSUtil();
			OSSConfig config = OSSConfig.getInstance(key);
			util.config = config;
			util.client = new OSSClient(config.ENDPOINT, config.ACCESS_ID, config.ACCESS_SECRET);
			instances.put(key, util);
		}
		return util;
	}
	public String upload(File file, String bucket, String path){
		client.putObject(bucket, path, file);
		return createUrl(bucket, path);
	}
	public String upload(URL url, String bucket, String path){
		try {
			client.putObject(bucket, path, url.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createUrl(bucket, path);
	}
	public String upload(InputStream in, String bucket, String path){
		client.putObject(bucket, path, in);
		return createUrl(bucket, path);
	}
	private String createUrl(String bucket, String path){
		String result = "";
		result = "http://"+bucket+"."+config.ENDPOINT;
		result = FileUtil.mergePath(result, path);
		return result;
	}
	public Map<String,String> signature(String dir) {
		return signature(dir, config.EXPIRE_SECOND);
	}
	public Map<String,String> signature(String dir, int second) {
		if(second == 0){
			second = config.EXPIRE_SECOND;
		}
		String host = "http://" + config.BUCKET + "." + config.ENDPOINT;
		Map<String,String> result = BeanUtil.craeteMap();
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
}
