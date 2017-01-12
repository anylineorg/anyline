package org.anyline.easemob;

import java.io.File;
import java.util.Iterator;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class EasemobConfig {
	private static Logger log = Logger.getLogger(EasemobConfig.class);

	/**
	 * 服务号相关信息
	 */
	public static String APP_ID = ""				; //AppID(应用ID)
	public static String APP_SECRECT = ""			; //AppSecret(应用密钥)
	public static String API_SECRECT = ""			; //微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	public static String MCH_ID = ""				; //商家号
	public static String SIGN_TYPE = ""				; //签名加密方式
	public static String SERVER_TOKEN = ""			; //服务号的配置token
	public static String CERT_PATH = ""				; //微信支付证书存放路径地址
	public static String PAY_NOTIFY_URL = ""		; //微信支付统一接口的回调action
	public static String PAY_CALLBACK_URL = ""		; //微信支付成功支付后跳转的地址
	public static String OAUTH2_REDIRECT_URI = ""	; //oauth2授权时回调action

	public static final String TRADE_TYPE_JSAPI 		= "JSAPI"	;//公众号支付	
	public static final String TRADE_TYPE_NATIVE 		= "NATIVE"	;//原生扫码支付
	public static final String TRADE_TYPE_APP 			= "APP"		;//app支付
	public static final String TRADE_TYPE_MICROPAY 		= "MICROPAY";//刷卡支付
	 /**
	 * 微信基础接口地址
	 */
	 //获取token接口(GET)
	 public final static String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	 //oauth2授权接口(GET)
	 public final static String OAUTH2_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	 //刷新access_token接口（GET）
	 public final static String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
	// 菜单创建接口（POST）
	 public final static String MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	// 菜单查询（GET）
	 public final static String MENU_GET_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";
	// 菜单删除（GET）
	public final static String MENU_DELETE_URL = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";
	/**
	 * 微信支付接口地址
	 */
	//微信支付统一接口(POST)
	public final static String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	//微信退款接口(POST)
	public final static String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	//订单查询接口(POST)
	public final static String CHECK_ORDER_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
	//关闭订单接口(POST)
	public final static String CLOSE_ORDER_URL = "https://api.mch.weixin.qq.com/pay/closeorder";
	//退款查询接口(POST)
	public final static String CHECK_REFUND_URL = "https://api.mch.weixin.qq.com/pay/refundquery";
	//对账单接口(POST)
	public final static String DOWNLOAD_BILL_URL = "https://api.mch.weixin.qq.com/pay/downloadbill";
	//短链接转换接口(POST)
	public final static String SHORT_URL = "https://api.mch.weixin.qq.com/tools/shorturl";
	//接口调用上报接口(POST)
	public final static String REPORT_URL = "https://api.mch.weixin.qq.com/payitil/report";
		
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

			File file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline-weixin.xml");
			loadConfig(file);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载微信配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("APP_ID".equalsIgnoreCase(key)){
					EasemobConfig.APP_ID = value;
				}else if("APP_SECRECT".equalsIgnoreCase(key)){
					EasemobConfig.APP_SECRECT = value;
				}else if("API_SECRECT".equalsIgnoreCase(key)){
					EasemobConfig.API_SECRECT = value;
				}else if("MCH_ID".equalsIgnoreCase(key)){
					EasemobConfig.MCH_ID = value;
				}else if("SIGN_TYPE".equalsIgnoreCase(key)){
					EasemobConfig.SIGN_TYPE = value;
				}else if("SERVER_TOKEN".equalsIgnoreCase(key)){
					EasemobConfig.SERVER_TOKEN = value;
				}else if("CERT_PATH".equalsIgnoreCase(key)){
					EasemobConfig.CERT_PATH = value;
				}else if("PAY_NOTIFY_URL".equalsIgnoreCase(key)){
					EasemobConfig.PAY_NOTIFY_URL = value;
				}else if("PAY_CALLBACK_URL".equalsIgnoreCase(key)){
					EasemobConfig.PAY_CALLBACK_URL = value;
				}else if("OAUTH2_REDIRECT_URI".equalsIgnoreCase(key)){
					EasemobConfig.OAUTH2_REDIRECT_URI = value;
				}
				
				if(ConfigTable.isDebug()){
					log.info("[解析微信配置文件] [" + key + " = " + value+"]");
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
