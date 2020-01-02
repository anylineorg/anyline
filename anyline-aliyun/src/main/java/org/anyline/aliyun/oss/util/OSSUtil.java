package org.anyline.aliyun.oss.util; 
 
import java.io.File; 
import java.io.InputStream; 
import java.net.URL; 
import java.util.ArrayList; 
import java.util.Date; 
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.List; 
import java.util.Map; 

import org.anyline.net.HttpUtil; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
import org.anyline.util.DateUtil; 
import org.anyline.util.FileUtil; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 

import com.aliyun.oss.OSSClient; 
import com.aliyun.oss.common.utils.BinaryUtil; 
import com.aliyun.oss.model.GetObjectRequest; 
import com.aliyun.oss.model.ListObjectsRequest; 
import com.aliyun.oss.model.MatchMode; 
import com.aliyun.oss.model.OSSObject; 
import com.aliyun.oss.model.OSSObjectSummary; 
import com.aliyun.oss.model.ObjectListing; 
import com.aliyun.oss.model.PolicyConditions; 
 
public class OSSUtil { 
	private static final Logger log = LoggerFactory.getLogger(OSSUtil.class); 
	private OSSClient client = null; 
	private OSSConfig config = null; 
	private static Hashtable<String, OSSUtil> instances = new Hashtable<String, OSSUtil>(); 
	 
	 
	public OSSConfig getConfig(){ 
		return config; 
	} 
	public static OSSUtil getInstance() { 
		return getInstance("default"); 
	} 
 
	public OSSClient getClient() { 
		return client; 
	} 
	public void setClient(OSSClient client) { 
		this.client = client; 
	} 
	@SuppressWarnings("deprecation")
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
			instances.put(key, util);//get_object_to_file 
		} 
		return util; 
	} 
	/** 
	 * 上传文件或目录 
	 * @param file  file
	 * @param path  path
	 * @return return
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
				String itemPath = FileUtil.mergePath(path, item.getAbsolutePath().replace(file.getAbsolutePath(), "")).replace("\\", "/"); 
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
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[oss upload file][result:true][file:{}][url:{}]",file.getAbsolutePath(), result); 
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
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[oss upload file][result:true][file:{}]",path); 
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
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[oss upload file][result:true][file:{}]",path); 
		} 
		return createUrl(path); 
	} 
	public boolean download(File dir){ 
		return download(dir,""); 
	} 
	/** 
	 * 文件列表 
	 * @param prefix  prefix
	 * @return return
	 */ 
	public List<String> list(String prefix){ 
		List<String> list = new ArrayList<String>(); 
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
	 * @param dir  dir
	 * @param prefix  prefix
	 * @return return
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
		        if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
		        	log.warn("[oss download file][local:{}][remote:{}]",file.getAbsolutePath(),key); 
		        } 
		    } 
		    nextMarker = objectListing.getNextMarker(); 
		     
		} while (objectListing.isTruncated()); 
 
		return true; 
	} 
	/** 
	 * 文件是否存在 
	 * @param path  path
	 * @return return
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
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[check exists][path:{}][key:{}]", path, key); 
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
			log.warn("[oss delete file][result:true][file:{}]", path); 
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
	 * @param path  path
	 * @return return
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
	 * @return return
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
	 * @return return
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
	 * @return return
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
	public Map<String,String> signature(String dir) { 
		return signature(dir, config.EXPIRE_SECOND); 
	} 
	public Map<String,String> signature(String dir, int second) { 
		if(second == 0){ 
			second = config.EXPIRE_SECOND; 
		} 
		String host = "http://" + config.BUCKET + "." + config.ENDPOINT; 
		Map<String,String> result = new HashMap<String,String>(); 
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
