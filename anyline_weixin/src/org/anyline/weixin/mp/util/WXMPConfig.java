package org.anyline.weixin.mp.util;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;


public class WXMPConfig extends BasicConfig{
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
	public String NOTIFY_URL = ""			; //微信支付统一接口的回调action
	public String CALLBACK_URL = ""			; //微信支付成功支付后跳转的地址
	public String OAUTH2_REDIRECT_URI = ""	; //oauth2授权时回调action
	
	public String WEB_SERVER = ""			;
	
	public static final String TRADE_TYPE_JSAPI 		= "JSAPI"	;//公众号支付	
	public static final String TRADE_TYPE_NATIVE 		= "NATIVE"	;//原生扫码支付
	public static final String TRADE_TYPE_APP 			= "APP"		;//app支付
	public static final String TRADE_TYPE_MICROPAY 		= "MICROPAY";//刷卡支付
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

	public static WXMPConfig getInstance(){
		return getInstance("default");
	}
	public static WXMPConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (WXMPConfig)instances.get(key);
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
				if("anyline-weixin-mp.xml".equals(file.getName())){
					parseFile(WXMPConfig.class, file, instances);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
