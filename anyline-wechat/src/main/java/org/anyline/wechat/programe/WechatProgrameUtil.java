package org.anyline.wechat.programe;


import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.AESUtil;
import org.anyline.net.HttpUtil;
import org.anyline.util.*;
import org.anyline.wechat.util.WechatUtil;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class WechatProgrameUtil extends WechatUtil {
	private static DataSet jsapiTickets = new DataSet();

	private WechatProgrameConfig config = null;


	private static Hashtable<String, WechatProgrameUtil> instances = new Hashtable<String,WechatProgrameUtil>();
	public static WechatProgrameUtil getInstance(){
		return getInstance("default");
	}
	public WechatProgrameUtil(WechatProgrameConfig config){
		this.config = config;
	}
	public WechatProgrameUtil(String key, DataRow config){
		WechatProgrameConfig conf = WechatProgrameConfig.parse(key, config);
		this.config = conf;
		instances.put(key, this);
	}
	public static WechatProgrameUtil reg(String key, DataRow config){
		WechatProgrameConfig conf = WechatProgrameConfig.reg(key, config);
		WechatProgrameUtil util = new WechatProgrameUtil(conf);
		instances.put(key, util);
		return util;
	}
	public static WechatProgrameUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WechatProgrameUtil util = instances.get(key);
		if(null == util){
			WechatProgrameConfig config = WechatProgrameConfig.getInstance(key);
			util = new WechatProgrameUtil(config);
			instances.put(key, util);
		}
		return util;
	}

	public WechatProgrameConfig getConfig() {
		return config;
	}


	public String sessionKey(String code){
		DataRow session = jscode2session(code);
		return session.getString("session_key");
	}
	public String openid(String code){
		DataRow session = jscode2session(code);
		return session.getString("openid");
	}
	public String unionid(String code){
		DataRow session = jscode2session(code);
		return session.getString("unionid");
	}
	public DataRow jscode2session(String code){
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+config.APP_ID+"&secret="+config.APP_SECRET+"&js_code="+code+"&grant_type=authorization_code";
		String json = HttpUtil.get(url).getText();
		DataRow session = DataRow.parseJson(json);
		if(session.isEmpty("session_key")){
			log.warn("[jscode2session][result:fail][json:{}]",json);
		}
		return session;
	}
	public String getAccessToken(){
		return getAccessToken(config);
	}
	/**
	 * 生成二维码
	 * @param path path
	 * @param width width
	 * @return InputStream
	 * @throws Exception Exception
	 */
	public InputStream createQRCode(String path, int width) throws Exception{
		String access_token = getAccessToken(config);
		String url = "https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token="+access_token;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("path",path);
		if(width>0){
			params.put("width", width);
		}
		String json = BeanUtil.map2json(params);
		InputStream is = HttpUtil.postStream(url,"UTF-8", new StringEntity(json, "UTF-8")).getInputStream();
		return is;
	}
	public boolean createQRCode(String path, int width, File file){
		try {
			InputStream is = createQRCode(path, width);
			FileUtil.write(is, file);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean createQRCode(String path, int width, OutputStream os){
		try {
			InputStream is = createQRCode(path, width);
			FileUtil.write(is, os);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean createQRCode(String path, File file){
		return createQRCode(path, 0, file);
	}
	public boolean createQRCode(String path, OutputStream os){
		return createQRCode(path, 0, os);
	}
	public InputStream createQRCode(String path) throws Exception{
		return createQRCode(path, 0);
	}

	/**
	 * 创建二维码
	 * @param path path
	 * @param width 宽度自动写0
	 * @param autoColor 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false
	 * @param color auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
	 * @param hyaline 是否需要透明底色，为 true 时，生成透明底色的小程序
	 * @return InputStream
	 */
	public InputStream createWXCode(String path, int width, boolean autoColor, String color, boolean hyaline){
		String access_token = getAccessToken(config);
		String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token="+access_token;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("path",path);
		if(width>0){
			params.put("width", width);
		}
		params.put("auto_color", autoColor);
		if(BasicUtil.isNotEmpty(color)) {
			params.put("line_color", color);
		}
		params.put("is_hyaline", hyaline);
		String json = BeanUtil.map2json(params);
		InputStream is = HttpUtil.postStream(url,"UTF-8", new StringEntity(json, "UTF-8")).getInputStream();
		return is;
	}

	/**
	 * 创建二维码
	 * @param path path
	 * @param width 宽度自动写0
	 * @param autoColor 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false
	 * @param color auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
	 * @param hyaline 是否需要透明底色，为 true 时，生成透明底色的小程序
	 * @param file 写入文件
	 * @return boolean
	 */
	public boolean createWXCode(String path, int width, boolean autoColor, String color, boolean hyaline, File file){
		try{
			InputStream is = createWXCode(path, width, autoColor, color, hyaline);
			FileUtil.write(is, file);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean createWXCode(String path, int width, boolean autoColor, String color, boolean hyaline, OutputStream os){
		try{
			InputStream is = createWXCode(path, width, autoColor, color, hyaline);
			FileUtil.write(is, os);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean createWXCode(String path, File file){
		return createWXCode(path, 0, true, null, false, file);
	}
	public boolean createWXCode(String path, OutputStream os){
		return createWXCode(path, 0, true, null, false, os);
	}

	/**
	 * 创建二维码
	 * @param path path
	 * @param scene scene 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
	 * @param width 宽度自动写0
	 * @param autoColor 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false
	 * @param color auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
	 * @param hyaline 是否需要透明底色，为 true 时，生成透明底色的小程序
	 * @return InputStream
	 */
	public InputStream createWXCodeUnlimit(String path, String scene, int width, boolean autoColor, String color, boolean hyaline){
		String access_token = getAccessToken(config);
		String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token="+access_token;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("path",path);
		if(width>0){
			params.put("width", width);
		}

		params.put("auto_color", autoColor);
		if(BasicUtil.isNotEmpty(scene)) {
			params.put("scene", scene);
		}
		if(BasicUtil.isNotEmpty(color)) {
			params.put("line_color", color);
		}
		params.put("is_hyaline", hyaline);
		String json = BeanUtil.map2json(params);
		InputStream is = HttpUtil.postStream(url,"UTF-8", new StringEntity(json, "UTF-8")).getInputStream();
		return is;
	}
	/**
	 * 创建二维码
	 * @param path path
	 * @param scene scene 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
	 * @param width 宽度自动写0
	 * @param autoColor 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false
	 * @param color auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
	 * @param hyaline 是否需要透明底色，为 true 时，生成透明底色的小程序
	 * @param file 写入文件
	 * @return  boolean
	 */
	public boolean createWXCodeUnlimit(String path, String scene, int width, boolean autoColor, String color, boolean hyaline, File file){
		try{
			InputStream is = createWXCodeUnlimit(path, scene, width, autoColor, color, hyaline);
			FileUtil.write(is, file);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean createWXCodeUnlimit(String path, String scene, int width, boolean autoColor, String color, boolean hyaline, OutputStream os){
		try{
			InputStream is = createWXCodeUnlimit(path, scene, width, autoColor, color, hyaline);
			FileUtil.write(is, os);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean createWXCodeUnlimit(String path, String scene, File file){
		return createWXCodeUnlimit(path, scene, 0, true, null, false, file);
	}
	public boolean createWXCodeUnlimit(String path, String scene, OutputStream os){
		return createWXCodeUnlimit(path, scene, 0, true, null, false, os);
	}

	/**
	 * 解密数据
	 * @param session 会话key
	 * @param vector 加密初始向理
	 * @param data 加密数据
	 * @return String
	 */
	public static String decrypt(String session, String vector, String data) {
		if(!HAS_SECURITY_PROVIDER) {
			HAS_SECURITY_PROVIDER = true;
			Security.addProvider(new BouncyCastleProvider());
		}
		return AESUtil.decrypt(AESUtil.CIPHER.PKCS7, session, vector, data);
	}
	private static boolean HAS_SECURITY_PROVIDER = false;

} 
