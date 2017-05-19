package org.anyline.qq.pay;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class QQPayConfig {
	private static Logger log = Logger.getLogger(QQPayConfig.class);
	private static Hashtable<String, QQPayConfig> instances = new Hashtable<String,QQPayConfig>();
	private Map<String,String> kvs = new HashMap<String,String>();
	/**
	 * 服务号相关信息
	 */
	public String APP_ID = ""				; //AppID(应用ID)
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

	public static QQPayConfig getInstance(){
		return instances.get("default");
	}
	public static QQPayConfig getInstance(String key){
		return instances.get(key);
	}
	public String getString(String key){
		return kvs.get(key);
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
				if("anyline-qqpay.xml".equals(file.getName())){
					loadConfig(file);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载配置文件] [file:" + file.getName() + "]");
			}

			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrConfig=root.elementIterator("config"); itrConfig.hasNext();){
				QQPayConfig config = new QQPayConfig();
				Element configElement = itrConfig.next();
				String configKey = configElement.attributeValue("key");
				if(BasicUtil.isEmpty(configKey)){
					configKey = "default";
				}
				Map<String,String> kvs = new HashMap<String,String>();
				for(Iterator<Element> itrProperty=configElement.elementIterator("property"); itrProperty.hasNext();){
					Element propertyElement = itrProperty.next();
					String key = propertyElement.attributeValue("key");
					String value = propertyElement.getTextTrim();
					if(ConfigTable.isDebug()){
						log.info("[解析QQ支付配置文件][key = "  + key + "][" + key + " = " + value+"]");
					}
					kvs.put(key, value);
				}
				config.kvs = kvs;
				config.setFieldValue();
				instances.put(configKey, config);
			}
			
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}

	private void setFieldValue(){
		Field[] fields = this.getClass().getDeclaredFields();
		for(Field field:fields){
			if(field.getType().getName().equals("java.lang.String")){
				String name = field.getName();
				try {
					String value = kvs.get(name);
					if(BasicUtil.isNotEmpty(value)){
						field.set(this, kvs.get(name));
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static void debug(){
	}
}
