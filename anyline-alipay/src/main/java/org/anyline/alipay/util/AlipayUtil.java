package org.anyline.alipay.util; 
 
import java.net.URLEncoder;
import java.util.Hashtable;

import com.alipay.api.request.*;
import com.alipay.api.response.*;
import org.anyline.alipay.entity.AlipayTradeOrder;
import org.anyline.alipay.entity.AlipayTradeQuery; 
import org.anyline.alipay.entity.AlipayTradeQueryResult; 
import org.anyline.alipay.entity.AlipayTransfer; 
import org.anyline.alipay.entity.AlipayTransferQuery; 
import org.anyline.alipay.entity.AlipayTransferQueryResult; 
import org.anyline.alipay.entity.AlipayTransferResult;
import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
import com.alipay.api.AlipayApiException; 
import com.alipay.api.AlipayClient; 
import com.alipay.api.DefaultAlipayClient; 
import com.alipay.api.domain.AlipayTradeAppPayModel;

public class AlipayUtil { 
	private static final Logger log = LoggerFactory.getLogger(AlipayUtil.class); 
 
	private AlipayClient client = null; 
	private AlipayConfig config = null;
	public AlipayUtil(){
	}
	public AlipayUtil(AlipayConfig config){
		this.config = config;
		client = new DefaultAlipayClient(
				"https://openapi.alipay.com/gateway.do",
				config.getString("APP_ID"),
				config.getString("APP_PRIVATE_KEY"),
				config.getString("DATA_FORMAT"),
				config.getString("ENCODE"),
				config.getString("ALIPAY_PUBLIC_KEY"),
				config.getString("SIGN_TYPE"));
	}
	private static Hashtable<String, AlipayUtil> instances = new Hashtable<String, AlipayUtil>(); 


	public static AlipayUtil getInstance() { 
		return getInstance("default"); 
	} 
 
	public static AlipayUtil getInstance(String key) { 
		if (BasicUtil.isEmpty(key)) { 
			key = "default"; 
		} 
		AlipayUtil util = instances.get(key); 
		if (null == util) {
			AlipayConfig config = AlipayConfig.getInstance(key);

			util = new AlipayUtil(config);

			instances.put(key, util); 
		} 
 
		return util; 
	}

	public static AlipayUtil reg(String key, DataRow config){
		AlipayConfig conf = AlipayConfig.reg(key, config);
		AlipayUtil util = new AlipayUtil(conf);
		instances.put(key, util);
		return util;
	}
	/** 
	 * app支付 
	 *  
	 * @param subject 支付标题 
	 * @param body  支付明细 
	 * @param price  支付价格(元) 
	 * @param order  系统订单号 
	 * @return String
	 */ 
	public String createAppOrder(String subject, String body, String price, String order) { 
		String result = ""; 
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest(); 
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel(); 
		model.setBody(body); 
		model.setSubject(subject); 
		model.setOutTradeNo(order); 
		model.setTimeoutExpress("30m"); 
		model.setTotalAmount(price); 
		request.setBizModel(model); 
		request.setNotifyUrl(config.getString("NOTIFY_URL")); 
		try { 
			AlipayTradeAppPayResponse response = client.sdkExecute(request); 
			result = response.getBody(); 
		} catch (AlipayApiException e) { 
			e.printStackTrace(); 
		} 
 
		return result; 
	} 
 
	/** 
	 * html支付 
	 *  
	 * @param subject  支付标题 
	 * @param body  支付明细
	 * @param price  支付金额(元)
	 * @param order  系统订单号
	 * @return String
	 */ 
	public String createWebOrder(String subject, String body, String price, String order) { 
		String result = ""; 
		return result; 
	} 
	public String createWapOrder(AlipayTradeOrder order, String callback){
		String result = ""; 
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
		if(BasicUtil.isEmpty(callback)){
			callback = config.RETURN_URL;
		}
	    alipayRequest.setReturnUrl(callback);
	    alipayRequest.setNotifyUrl(config.NOTIFY_URL); 
	    alipayRequest.setBizContent(BeanUtil.object2json(order));//填充业务参数 
	    try { 
	        result = client.pageExecute(alipayRequest).getBody(); //调用SDK生成表单 
	    } catch (AlipayApiException e) { 
	        e.printStackTrace(); 
	    } 
		return result; 
	} 
	public String createWapOrder(String subject, String body, String price, String order, String callback){
		AlipayTradeOrder tradeOrder = new AlipayTradeOrder(); 
		tradeOrder.setSubject(subject); 
		tradeOrder.setBody(body); 
		tradeOrder.setTotal_amount(price); 
		tradeOrder.setOut_trade_no(order); 
		return createWapOrder(tradeOrder, callback);
	} 
	/** 
	 * 交易状态查询 
	 * @param query 查询参数
	 * @return return
	 */ 
	public AlipayTradeQueryResult tradeQuery(AlipayTradeQuery query){ 
		AlipayTradeQueryResult result = null;  
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest(); 
		String json = BeanUtil.object2json(query); 
		request.setBizContent(json); 
		try { 
			AlipayTradeQueryResponse res = client.execute(request); 
			result = new AlipayTradeQueryResult(res); 
		} catch (AlipayApiException e) { 
			result = new AlipayTradeQueryResult(); 
			e.printStackTrace(); 
		}finally{ 
			log.warn("[单笔转账到支付宝账户][data:{}][result:{}]", json,BeanUtil.object2json(result)); 
		} 
		return result; 
	} 
	/** 
	 * 单笔转账到支付宝账户 
	 * @param transfer  转帐参数
	 * @return return
	 */ 
	public AlipayTransferResult transfer(AlipayTransfer transfer) { 
		AlipayTransferResult result = null;  
		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest(); 
		String json = BeanUtil.object2json(transfer); 
		request.setBizContent(json); 
		try { 
			AlipayFundTransToaccountTransferResponse res = client.execute(request); 
			result = new AlipayTransferResult(res); 
		} catch (AlipayApiException e) { 
			result = new AlipayTransferResult(); 
			e.printStackTrace(); 
		}finally{ 
			log.warn("[单笔转账到支付宝账户][data:{}][result:{}]", json,BeanUtil.object2json(result)); 
		} 
		return result; 
	} 
	/** 
	 * 单笔转账到支付宝账户  结果查询 
	 * @param query  查询参数
	 * @return return
	 */ 
	public AlipayTransferQueryResult transferQuery(AlipayTransferQuery query) { 
		AlipayTransferQueryResult result = null; 
		AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest(); 
		String json = BeanUtil.object2json(query); 
		request.setBizContent(json); 
		try { 
			AlipayFundTransOrderQueryResponse res = client.execute(request); 
			result = new AlipayTransferQueryResult(res); 
		} catch (AlipayApiException e) { 
			result = new AlipayTransferQueryResult(); 
			e.printStackTrace(); 
		}finally{ 
			log.warn("[单笔转账到支付宝账户查询][data:{}][result:{}]", json,BeanUtil.object2json(result)); 
		} 
		return result; 
	}

	/**
	 * 创建登录连接
	 * @param redirect 回调地址
	 * @param state 状态保持
	 * @param scope 获取信息范围
	 * @return String
	 */
	public String ceateAuthUrl(String redirect, String scope, String state){
		try {
			redirect = URLEncoder.encode(redirect, "UTF-8");
		}catch (Exception e){
			e.printStackTrace();
		}
		String url = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id="+config.APP_ID+"&scope="+scope+"&redirect_uri="+redirect+"&state="+state;
		return url;
	}

	/**
	 * 用户信息
	 * @param code 回调参数auth_code
	 * @return
	 */
	public DataRow getUserInfo(String code){
		DataRow user = null;
		AlipaySystemOauthTokenRequest req = new AlipaySystemOauthTokenRequest();
		req.setCode(code);
		req.setGrantType("authorization_code");
		try {
			AlipaySystemOauthTokenResponse oauthTokenResponse = client.execute(req);
			String token = oauthTokenResponse.getAccessToken();
			String userId = oauthTokenResponse.getAlipayUserId();
			user = new DataRow();
			user.put("USER_ID", userId);
			//详细信息
			AlipayUserInfoShareRequest infoReq = new AlipayUserInfoShareRequest();
			AlipayUserInfoShareResponse infoRes = client.execute(infoReq, token);
			if (infoRes.isSuccess()) {
				user = DataRow.parseJson(infoRes.getBody()).getRow("alipay_user_info_share_response");
			} else {
				log.warn("[获取详细调用失败][code:{}][msg:{}]",infoRes.getSubCode(),infoRes.getSubMsg());
			}
		} catch (AlipayApiException e) {
			//处理异常
			e.printStackTrace();
		}
		return user;
	}

} 
