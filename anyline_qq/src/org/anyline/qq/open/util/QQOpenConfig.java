package org.anyline.qq.open.util;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.qq.util.QQConfig;
import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;


public class QQOpenConfig extends QQConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	/**
	 * 服务号相关信息
	 */
	public String APP_ID = ""				; //AppID(应用ID)
	public String APP_KEY = ""				; //APPKEY(应用密钥)
	public String APP_SECRET = ""			; //AppSecret(应用密钥)
	public String SIGN_TYPE = ""			; //签名加密方式
	public String SERVER_TOKEN = ""			; //服务号的配置token
	
	public String PAY_API_SECRET = ""		; //商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	public String PAY_MCH_ID = ""			; //商家号
	public String PAY_NOTIFY_URL = ""		; //支付统一接口的回调action
	public String PAY_CALLBACK_URL = ""		; //支付成功支付后跳转的地址
	public String PAY_KEY_STORE_FILE = ""	; //支付证书存放路径地址

	
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static QQOpenConfig getInstance(){
		return getInstance("default");
	}
	public static QQOpenConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (QQOpenConfig)instances.get(key);
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
				if("anyline-qq-open.xml".equals(file.getName())){
					parseFile(QQOpenConfig.class, file, instances);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
