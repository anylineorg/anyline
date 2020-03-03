package org.anyline.wechat.open.util;
 
import java.io.File; 
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.Map; 
 
import org.anyline.entity.DataRow; 
import org.anyline.net.HttpUtil; 
import org.anyline.net.SimpleHttpUtil; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.BeanUtil; 
import org.anyline.util.ConfigTable;
import org.anyline.wechat.entity.*;
import org.anyline.wechat.util.WechatConfig;
import org.anyline.wechat.util.WechatUtil;
import org.apache.http.entity.StringEntity; 
import org.apache.http.impl.client.CloseableHttpClient; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class WechatOpenUtil {
	private static final Logger log = LoggerFactory.getLogger(WechatOpenUtil.class);
	private static Hashtable<String, WechatOpenUtil> instances = new Hashtable<String, WechatOpenUtil>();
	private WechatOpenConfig config;
 
	public WechatOpenUtil(WechatOpenConfig config){
		this.config = config; 
	} 
 
	public WechatOpenUtil(String key, DataRow config){
		WechatOpenConfig conf = WechatOpenConfig.parse(key, config);
		this.config = conf; 
		instances.put(key, this); 
	} 
 
	public static WechatOpenUtil reg(String key, DataRow config){
		WechatOpenConfig conf = WechatOpenConfig.parse(key, config);
		WechatOpenUtil util = new WechatOpenUtil(conf);
		instances.put(key, util); 
		return util; 
	} 
	 
	public static WechatOpenUtil getInstance(){
		return getInstance("default"); 
	} 
	public static WechatOpenUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		WechatOpenUtil util = instances.get(key);
		if(null == util){ 
			WechatOpenConfig config = WechatOpenConfig.getInstance(key);
			util = new WechatOpenUtil(config);
			instances.put(key, util); 
		} 
		return util; 
	} 
	public WechatOpenConfig getConfig(){
		return config; 
	} 
	/** 
	 * 统一下单
	 * @param order order
	 * @return WechatPrePayResult
	 * @throws Exception Exception
	 */
	public WechatPrePayResult unifiedorder(WechatPrePayOrder order) throws Exception{
		return WechatUtil.unifiedorder(config,WechatConfig.TRADE_TYPE.APP,order);
	} 
 
 
	/** 
	 * 退款申请 
	 * @param refund  refund
	 * @return return
	 */ 
	public WechatRefundResult refund(WechatRefund refund){
		return WechatUtil.refund(config, refund);
	}

	/**
	 * 发送红包
	 * @param pack  pack
	 * @return return
	 */
	public WechatRedpackResult sendRedpack(WechatRedpack pack){
		return WechatUtil.sendRedpack(config, pack);
	}

	/**
	 * 发送裂变红包
	 * @param pack  pack
	 * @return return
	 */
	public WechatFissionRedpackResult sendRedpack(WechatFissionRedpack pack){
		return WechatUtil.sendRedpack(config,pack);
	}
	/**
	 * 企业付款
	 * @param transfer  transfer
	 * @return return
	 */
	public WechatEnterpriseTransferResult transfer(WechatEnterpriseTransfer transfer){
		return WechatUtil.transfer(config, transfer);
	}
	/**
	 * 企业付款到银行卡
	 * @param transfer  transfer
	 * @return return
	 */
	public WechatEnterpriseTransferBankResult transfer(WechatEnterpriseTransferBank transfer){
		return WechatUtil.transfer(config, transfer);
	}

	/** 
	 * APP调起支付所需参数 
	 * @param prepayid 预支付id(由统一下单接口返回)
	 * @return return
	 */ 
	public DataRow pay(String prepayid){
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("appid", config.APP_ID); 
		params.put("partnerid", config.PAY_MCH_ID); 
		params.put("prepayid", prepayid); 
		params.put("package", "Sign=WXPay"); 
		params.put("noncestr", BasicUtil.getRandomUpperString(32)); 
		params.put("timestamp", System.currentTimeMillis()/1000+""); 
		String sign = WechatUtil.sign(config.PAY_API_SECRET,params);
		params.put("sign", sign); 
		DataRow row = new DataRow(params); 
		row.put("packagevalue", row.get("package")); 
		row.remove("package"); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[APP调起微信支付][参数:{}]", row.toJSON()); 
		} 
		return row; 
	}
	public WechatAuthInfo getAuthInfo(String code){
		return WechatUtil.getAuthInfo(config, code);
	}
	public String getOpenId(String code){
		WechatAuthInfo info = getAuthInfo(code);
		if(null != info && info.isResult()){
			return info.getOpenid();
		}
		return null;
	}
	public WechatUserInfo getUserInfo(String openid){
		return WechatUtil.getUserInfo(config,openid);
	}
	public String getUnionId(String openid) {
		WechatUserInfo info = getUserInfo(openid);
		if (null != info && info.isResult()) {
			return info.getUnionid();
		}
		return null;
	}
} 
