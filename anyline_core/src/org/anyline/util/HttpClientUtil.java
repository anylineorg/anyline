package org.anyline.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {
	public static Source post(CloseableHttpClient client, String url, String encode,  String ... params){
		return post(client, null, url, encode, params);
	}
	public static Source post(CloseableHttpClient client, Map<String,String> headers, String url, String encode, String ... params){
		Map<String,String> map = paramToMap(params);
		return post(client, headers, url, encode, map);
	}
	public static Source post(CloseableHttpClient client, String url, String encode, Map<String,String> params){
		return post(client, null, url, encode, params);
	}
	public static Source post(CloseableHttpClient client, Map<String,String> headers, String url, String encode, Map<String,String> params){
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if(null != params){
			Iterator<String> keys = params.keySet().iterator();
			while(keys.hasNext()){
				String key = keys.next();
				String value = params.get(key);
				pairs.add(new BasicNameValuePair(key, value));
			}
		}
		return post(client, headers, url, encode, pairs);
	}

	public static Source post(CloseableHttpClient client, String url, String encode, List<NameValuePair> pairs){
		return post(client, null, url, encode, pairs);
	}
	public static Source post(CloseableHttpClient client, Map<String,String> headers, String url, String encode, List<NameValuePair> pairs){
		Source result = new Source();
		HttpPost method = new HttpPost(url);
		CloseableHttpResponse response = null;
		try{
			if(null != pairs){  
				method.setEntity(new UrlEncodedFormEntity(pairs));
			}
			if(null != headers){
				Iterator<String> keys = headers.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					String value = headers.get(key);
					method.setHeader(key, value);
				}
			}
			
			response = client.execute(method);
			if(null == headers){
				headers = new HashMap<String,String>();
			}
			Header[] all = response.getAllHeaders();
			for(Header header:all){
				String key = header.getName();
				String value = header.getValue();
				headers.put(key, value);
				if("Set-Cookie".equalsIgnoreCase(key)){
					HttpCookie c = new HttpCookie(value);
					result.setCookie(c);
				}
			}
			result.setHeaders(headers);
			
	        HttpEntity entity = response.getEntity();
	        
	        if(null != entity){
		        String text = EntityUtils.toString(entity, encode);
		        result.setText(text);
	        }
		}catch(Exception e){
			e.printStackTrace();
		}finally {  
			try{
				response.close();
			}catch(Exception e){
				e.printStackTrace();
			}
        }
		return result;
	}
	private static Map<String,String> paramToMap(String ... params){
		Map<String,String> result = new HashMap<String,String>();
		if(null != params){
			int size = params.length;
			for(int i=0; i<size-1; i+=2){
				String key = params[i];
				String value = params[i+1];
				if(null == value){
					value = "";
				}
				result.put(key.toString(), value);
			}
		}
		return result;
	}
}
