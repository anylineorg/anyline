package org.anyline.aliyun.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.anyline.util.BasicUtil;
import org.anyline.util.HttpUtil;
import org.apache.log4j.Logger;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;

public class OSSUtil {
	private static Logger log = Logger.getLogger(OSSUtil.class);
	private OSSClient client = null;
	private OSSConfig config = null;
	private static Hashtable<String, OSSUtil> instances = new Hashtable<String, OSSUtil>();

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
			util.client = new OSSClient(config.ENDPOINT, config.KEY_ID, config.KEY_SECRET);
			instances.put(key, util);
		}
		return util;
	}
	public String upload(File file, String bucket, String path){
		client.putObject(bucket, path, file);
		client.shutdown();
		return createUrl(bucket, path);
	}
	public String upload(URL url, String bucket, String path){
		try {
			client.putObject(bucket, path, url.openStream());
			client.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createUrl(bucket, path);
	}
	public String upload(InputStream in, String bucket, String path){
		client.putObject(bucket, path, in);
		client.shutdown();
		return createUrl(bucket, path);
	}
	private String createUrl(String bucket, String path){
		String result = "";
		result = config.ENDPOINT.replace("//", bucket+".");
		result = HttpUtil.mergeUrlParam(result, path);
		return result;
	}
}
