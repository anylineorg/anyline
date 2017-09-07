package org.anyline.weixin.wap.util;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;


public class WXWapConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	/**
	 * 服务号相关信息
	 */
	public String APP_ID = ""				; //AppID(应用ID)
	public String APP_SECRECT = ""			; //AppSecret(应用密钥)
	public String API_SECRECT = ""			; //微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	public String MCH_ID = ""				; //商家号
	public String SIGN_TYPE = ""			; //签名加密方式
	public String SERVER_TOKEN = ""			; //服务号的配置token
	public String CERT_PATH = ""			; //微信支付证书存放路径地址
	public String PAY_NOTIFY_URL = ""			; //微信支付统一接口的回调action
	public String PAY_CALLBACK_URL = ""			; //微信支付成功支付后跳转的地址
	public String OAUTH2_REDIRECT_URI = ""	; //oauth2授权时回调action

	
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static WXWapConfig getInstance(){
		return getInstance("default");
	}
	public static WXWapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (WXWapConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		try {
			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-weixin-wap.xml".equals(file.getName())){
					parseFile(WXWapConfig.class, file, instances);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
