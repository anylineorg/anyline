package org.anyline.weixin.wap.util; 
 
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.Map; 
 
import org.anyline.entity.DataRow; 
import org.anyline.net.HttpUtil; 
import org.anyline.net.SimpleHttpUtil; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.BeanUtil; 
import org.anyline.util.ConfigTable; 
import org.anyline.weixin.util.WXConfig; 
import org.anyline.weixin.util.WXUtil; 
import org.anyline.weixin.wap.entity.WXWapPrePayOrder; 
import org.anyline.weixin.wap.entity.WXWapPrePayResult; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class WXWapUtil { 
	private static final Logger log = LoggerFactory.getLogger(WXWapUtil.class); 
	private static Hashtable<String,WXWapUtil> instances = new Hashtable<String,WXWapUtil>(); 
	private WXWapConfig config; 
	 
 
	public WXWapUtil(WXWapConfig config){ 
		this.config = config; 
	} 
	 
 
	public WXWapUtil(String key, DataRow config){ 
		WXWapConfig conf = WXWapConfig.parse(key, config); 
		this.config = conf; 
		instances.put(key, this); 
	} 
	public static WXWapUtil getInstance(){ 
		return getInstance("default"); 
	} 
	public static WXWapUtil getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		WXWapUtil util = instances.get(key); 
		if(null == util){ 
			WXWapConfig config = WXWapConfig.getInstance(key); 
			util = new WXWapUtil(config); 
			instances.put(key, util); 
		} 
		return util; 
	} 
	public WXWapConfig getConfig(){ 
		return config; 
	} 
	/** 
	 * 统一下单 
	 * @param order  order
	 * @return return
	 */ 
	public WXWapPrePayResult unifiedorder(WXWapPrePayOrder order) { 
		WXWapPrePayResult result = null; 
		order.setNonce_str(BasicUtil.getRandomLowerString(20)); 
		if(BasicUtil.isEmpty(order.getAppid())){ 
			order.setAppid(config.APP_ID); 
		} 
		if(BasicUtil.isEmpty(order.getMch_id())){ 
			order.setMch_id(config.PAY_MCH_ID); 
		} 
		if(BasicUtil.isEmpty(order.getNotify_url())){ 
			order.setNotify_url(config.PAY_NOTIFY_URL); 
		} 
		order.setTrade_type(WXConfig.TRADE_TYPE.MWEB); 
		 
		Map<String, Object> map = BeanUtil.toMap(order); 
		String sign = WXUtil.sign(config.PAY_API_SECRET,map); 
		map.put("sign", sign); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[统一下单][sign:{}]", sign); 
		} 
		String xml = BeanUtil.map2xml(map); 
 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[统一下单][xml:{}]", xml); 
		} 
		String rtn = SimpleHttpUtil.post(WXConfig.API_URL_UNIFIED_ORDER, xml); 
 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[统一下单][return:{}]", rtn); 
		} 
		result = BeanUtil.xml2object(rtn, WXWapPrePayResult.class); 
 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[统一下单][prepay id:{}]", result.getPrepay_id()); 
		} 
		return result; 
	} 
 
 
 
	/** 
	 * APP调起支付所需参数 
	 * @param prepayid 预支付id
	 * @return return
	 */ 
	public DataRow appParam(String prepayid){ 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("appid", config.APP_ID); 
		params.put("partnerid", config.PAY_MCH_ID); 
		params.put("prepayid", prepayid); 
		params.put("package", "Sign=WXPay"); 
		params.put("noncestr", BasicUtil.getRandomUpperString(32)); 
		params.put("timestamp", System.currentTimeMillis()/1000+""); 
		String sign = WXUtil.sign(config.PAY_API_SECRET,params); 
		params.put("sign", sign); 
		DataRow row = new DataRow(params); 
		row.put("packagevalue", row.get("package")); 
		row.remove("package"); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[APP调起微信支付][参数:{}]", row.toJSON()); 
		} 
		return row; 
	} 
	public DataRow getOpenId(String code){ 
		DataRow row = new DataRow(); 
		String url = WXConfig.API_URL_AUTH_ACCESS_TOKEN + "?appid="+config.APP_ID+"&secret="+config.APP_SECRET+"&code="+code+"&grant_type=authorization_code"; 
		String txt = HttpUtil.get(url).getText(); 
		row = DataRow.parseJson(txt); 
		return row; 
	} 
	public DataRow getUnionId(String code){ 
		return getOpenId(code); 
	} 
} 
