package org.anyline.qq.open.util;

import java.util.Base64;
import java.util.Hashtable;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.anyline.entity.DataRow;
import org.anyline.net.HttpUtil;
import org.anyline.net.SimpleHttpUtil;
import org.anyline.qq.open.entity.QQPayTradeOrder;
import org.anyline.qq.open.entity.QQPayTradeResult;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QQOpenUtil{
	private static final Logger log = LoggerFactory.getLogger(QQOpenUtil.class);
	private static Hashtable<String,QQOpenUtil> instances = new Hashtable<String,QQOpenUtil>();
	private QQOpenConfig config = null;
	public static QQOpenUtil getInstance(){
		return getInstance("default");
	}
	public static QQOpenUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		QQOpenUtil util = instances.get(key);
		if(null == util){
			util = new QQOpenUtil();
			util.config = QQOpenConfig.getInstance(key);
			instances.put(key, util);
		}
		return util;
	}
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public QQPayTradeResult unifiedorder(QQPayTradeOrder order) {
		QQPayTradeResult result = null;
		order.setNonce_str(BasicUtil.getRandomString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(config.PAY_NOTIFY_URL);
		}
		
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = sign(map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("\n\t[统一下单][sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("\n\t[统一下单][xml:{}]", xml);
		}
		String rtn = SimpleHttpUtil.post(QQOpenConfig.UNIFIED_ORDER_URL, xml);

		if(ConfigTable.isDebug()){
			log.warn("\n\t[统一下单][return:{}]", rtn);
		}
		result = BeanUtil.xml2object(rtn, QQPayTradeResult.class);

		if(ConfigTable.isDebug()){
			log.warn("\n\t[统一下单][prepay id:{}]", result.getPrepay_id());
		}
		return result;
	}
	/**
	 * 签名
	 * 统一下单签名
	 * @param params
	 * @return
	 */
	public String sign(Map<String, Object> params) {
		String sign = "";
		sign = BasicUtil.joinBySort(params);
		sign += "&key=" + config.PAY_API_SECRET;
		sign = MD5Util.crypto(sign).toUpperCase();
		return sign;
	}

	/**
	 * APP 调用起支付签名
	 * @param nonce	随机串
	 * @param prepayid 预支付ID
	 * @return
	 */
	public String appSign(String prepayid, String nonce){
		String result = "";
        StringBuilder builder = new StringBuilder();
        builder.append("appId=").append(config.APP_ID);
        builder.append("&bargainorId=").append(config.PAY_MCH_ID);
        builder.append("&nonce=").append(nonce);
        builder.append("&pubAcc=").append("");
        builder.append("&tokenId=").append(prepayid);
        try {
			byte[] byteKey = (QQOpenConfig.getInstance().APP_KEY+"&").getBytes("UTF-8");
        // 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(byteKey, "HmacSHA1");
        // 生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance("HmacSHA1");
        // 用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] byteSrc = builder.toString().getBytes("UTF-8");
        // 完成 Mac 操作
        byte[] dst = mac.doFinal(byteSrc);
        // Base64
        result = Base64.getEncoder().encodeToString(dst);
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(ConfigTable.isDebug()){
			log.warn("\n\t[APP调起QQ支付签名][sign:{}][app key:{}][sign src:{}]", result, QQOpenConfig.getInstance().APP_KEY,builder.toString());
        }
		return result;
	}

	/**
	 * APP调起支付所需参数
	 * @param hint	关注手Q公众帐号提示语
	 * @param prepayid 预支付ID
	 * @return
	 */
	public DataRow appParam(String prepayid, String hint){
		DataRow row = new DataRow();
		String nonce = BasicUtil.getRandomLowerString(32);
		row.put("APPID", QQOpenConfig.getInstance().APP_ID);
		row.put("nonce", nonce);
		row.put("timeStamp", System.currentTimeMillis()/1000+"");
		row.put("tokenId", prepayid);
		row.put("pubAcc", "");
		row.put("pubAccHint", hint);
		row.put("bargainorId", QQOpenConfig.getInstance().PAY_MCH_ID);
		row.put("sigType", "HMAC-SHA1");
		String sign = appSign(prepayid, nonce);
		row.put("SIG", sign);
		if(ConfigTable.isDebug()){
			log.warn("\n\t[APP调起QQ支付][返回参数:{}]",row.toJSON());
		}
		return row;
	}

	public DataRow getOpenId(String accessToken){
		DataRow row = new DataRow();
		String url = "https://graph.qq.com/oauth2.0/me?access_token="+accessToken+"&unionid=1";
		String txt = HttpUtil.get(url).getText();
		if(ConfigTable.isDebug()){
			log.warn("\n\t[QQ登录][get openid][txt:"+txt+"]");
		}
		//callback( {"client_id":"1106186286","openid":"0B6D8FD1AF2B686CDF78AC34E981D9C4","unionid":"UID_8687A0501C64D5AD725C283107C5BB83"} );
		String openid= RegularUtil.cut(txt, "openid",":","\"","\"");
		String unionid = RegularUtil.cut(txt, "unionid",":","\"","\"");
		row.put("OPENID", openid);
		row.put("UNIONID", unionid);
		return row;
	} 
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
	
}
