package org.anyline.qq.open.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class QQOpenConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	/**
	 * 服务号相关信息
	 */
	public String APP_ID = ""				; //AppID(应用ID)
	public String APP_KEY = ""				; //APPKEY(应用密钥)
	public String APP_SECRECT = ""			; //AppSecret(应用密钥)
	public String API_SECRECT = ""			; //商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	public String MCH_ID = ""				; //商家号
	public String SIGN_TYPE = ""			; //签名加密方式
	public String SERVER_TOKEN = ""			; //服务号的配置token
	public String CERT_PATH = ""			; //支付证书存放路径地址
	public String PAY_NOTIFY_URL = ""		; //支付统一接口的回调action
	public String PAY_CALLBACK_URL = ""		; //支付成功支付后跳转的地址

	public static final String TRADE_TYPE_JSAPI 		= "JSAPI"	;//公众号支付	
	public static final String TRADE_TYPE_NATIVE 		= "NATIVE"	;//原生扫码支付
	public static final String TRADE_TYPE_APP 			= "APP"		;//app支付
	public static final String TRADE_TYPE_MICROPAY 		= "MICROPAY";//刷卡支付
	/**
	 * 支付接口地址
	 */
	//支付统一接口(POST)
	public final static String UNIFIED_ORDER_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_unified_order.cgi";
	//订单查询接口(POST)
	public final static String QUERY_ORDER_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_order_query.cgi";
	//关闭订单接口(POST)
	public final static String CLOSE_ORDER_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_close_order.cgi";
	//退款接口(POST)
	public final static String REFUND_URL = "https://api.qpay.qq.com/cgi-bin/pay/qpay_refund.cgi";
	//退款查询接口(POST)
	public final static String QUERY_REFUND_URL = "https://qpay.qq.com/cgi-bin/pay/qpay_refund_query.cgi";
	//对账单接口(POST)
	public final static String DOWNLOAD_BILL_URL = "https://qpay.qq.com/cgi-bin/sp_download/qpay_mch_statement_down.cgi";

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
