package org.anyline.alipay.util;

import java.io.File;
import java.util.Iterator;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class AliPayConfig {
	private static Logger log = Logger.getLogger(AliPayConfig.class);
	// 合作身份者ID，签约账号，以2088开头由16位纯数字组成的字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
	public static String PARTNER = "";
	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	public static String SELLER_ID = "";
	// MD5密钥，安全检验码，由数字和字母组成的32位字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
    public static String KEY = "";
	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String NOTIFY_URL = "";
	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	public static String CALLBACK_URL = "";
	// 签名方式
	public static String SIGN_TYPE = "MD5";
	// 调试用，创建TXT日志文件夹路径，见AlipayCore.java类中的logResult(String sWord)打印方法。
	public static String LOG_PATH = "d:\\log";
	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String INPUT_CHARSET = "utf-8";
	// 支付类型 ，无需修改
	public static String PAYMENT_TYPE = "1";
	// 调用的接口名，无需修改
	public static String SERVICE_WEB = "create_direct_pay_by_user";
	public static String SERVICE_WAP = "alipay.wap.create.direct.pay.by.user";
	//应用公钥
	public static String APP_PUBLIC_KEY = "";
	//应用私钥
	public static String APP_PRIVATE_KEY = "";
	//支付宝公钥
	public static String ALIPAY_PUBLIC_KEY= "";
//↓↓↓↓↓↓↓↓↓↓ 请在这里配置防钓鱼信息，如果没开通防钓鱼功能，为空即可 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	public static String ANTI_PHISHING_KEY = "";
	// 客户端的IP地址 非局域网的外网IP地址，如：221.0.0.1
	public static String EXTER_INVOKE_IP = "";
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		try {

			File file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline-alipay.xml");
			loadConfig(file);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载支付宝配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("PARTNER".equalsIgnoreCase(key)){
					AliPayConfig.PARTNER = value;
				}else if("SELLER_ID".equalsIgnoreCase(key)){
					AliPayConfig.SELLER_ID = value;
				}else if("KEY".equalsIgnoreCase(key)){
					AliPayConfig.KEY = value;
				}else if("APP_PUBLIC_KEY".equalsIgnoreCase(key)){
					AliPayConfig.APP_PUBLIC_KEY = value;
				}else if("APP_PRIVATE_KEY".equalsIgnoreCase(key)){
					AliPayConfig.APP_PRIVATE_KEY = value;
				}else if("ALIPAY_PUBLIC_KEY".equalsIgnoreCase(key)){
					AliPayConfig.ALIPAY_PUBLIC_KEY = value;
				}else if("NOTIFY_URL".equalsIgnoreCase(key)){
					AliPayConfig.NOTIFY_URL = value;
				}else if("CALLBACK_URL".equalsIgnoreCase(key)){
					AliPayConfig.CALLBACK_URL = value;
				}else if("SIGN_TYPE".equalsIgnoreCase(key)){
					AliPayConfig.SIGN_TYPE = value;
				}else if("LOG_PATH".equalsIgnoreCase(key)){
					AliPayConfig.LOG_PATH = value;
				}else if("INPUT_CHARSET".equalsIgnoreCase(key)){
					AliPayConfig.INPUT_CHARSET = value;
				}else if("ANTI_PHISHING_KEY".equalsIgnoreCase(key)){
					AliPayConfig.ANTI_PHISHING_KEY = value;
				}else if("EXTER_INVOKE_IP".equalsIgnoreCase(key)){
					AliPayConfig.EXTER_INVOKE_IP = value;
				}
				
				if(ConfigTable.isDebug()){
					log.info("[解析支付宝配置文件] [" + key + " = " + value+"]");
				}
			}
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
	public static void main(String args[]){
		debug();
	}
}
