package org.anyline.wechat.entity;

/**
 * 退款
 */
public class WechatRefund {
	private String appid			; //公众号ID(config)		是		String(32)	wx8888888888888888	微信分配的公众账号ID（企业号corpid即为此appId） 
	private String mch_id			; //商户号(config)		是		String(32)	1900000109	微信支付分配的商户号 
	private String nonce_str		; //随机字符串(auto)		是		String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位。推荐随机数生成算法 
	private String sign				; //签名(auto)			是		String(32)	C380BEC2BFD727A4B6845133519F3AD6	签名，详见签名生成算法 
	private String sign_type		; //签名类型(config)		否		String(32)	HMAC-SHA256	签名类型，目前支持HMAC-SHA256和MD5，默认为MD5 
	private String transaction_id	; //微信订单号				是		String(32)	1217752501201407033233368018	微信生成的订单号，在支付通知中有返回 
	private String out_trade_no		; //商户订单号(evl)		是		String(32)	1217752501201407033233368018	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 
	private String out_refund_no	; //商户退款单号			是		String(64)	1217752501201407033233368018	商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。 
	private int total_fee			; //订单金额				是		Int	100		订单总金额，单位为分，只能为整数，详见支付金额 
	private int refund_fee			; //退款金额				是		Int	100		退款总金额，订单总金额，单位为分，只能为整数，详见支付金额 
	private String refund_fee_type	; //货币种类(def)			是		String(8)	CNY	货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型 
	private String refund_desc		; //退款原因				否		String(80)	商品已售完	若商户传入，会在下发给用户的退款消息中体现退款原因 
	private String refund_account	; //退款资金来源			否		String(30)	REFUND_SOURCE_RECHARGE_FUNDS	REFUND_SOURCE_UNSETTLED_FUNDS---未结算资金退款（默认使用未结算资金退款）REFUND_SOURCE_RECHARGE_FUNDS---可用余额退款 
	private String notify_url		; //退款结果通知url		否		String(256)	https://weixin.qq.com/notify/	异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数 如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。 
	public String getAppid() { 
		return appid; 
	} 
	public void setAppid(String appid) { 
		this.appid = appid; 
	} 
	public String getMch_id() { 
		return mch_id; 
	} 
	public void setMch_id(String mch_id) { 
		this.mch_id = mch_id; 
	} 
	public String getNonce_str() { 
		return nonce_str; 
	} 
	public void setNonce_str(String nonce_str) { 
		this.nonce_str = nonce_str; 
	} 
	public String getSign() { 
		return sign; 
	} 
	public void setSign(String sign) { 
		this.sign = sign; 
	} 
	public String getSign_type() { 
		return sign_type; 
	} 
	public void setSign_type(String sign_type) { 
		this.sign_type = sign_type; 
	} 
	public String getTransaction_id() { 
		return transaction_id; 
	} 
	public void setTransaction_id(String transaction_id) { 
		this.transaction_id = transaction_id; 
	} 
	public String getOut_trade_no() { 
		return out_trade_no; 
	} 
	public void setOut_trade_no(String out_trade_no) { 
		this.out_trade_no = out_trade_no; 
	} 
	public String getOut_refund_no() { 
		return out_refund_no; 
	} 
	public void setOut_refund_no(String out_refund_no) { 
		this.out_refund_no = out_refund_no; 
	} 
	public String getRefund_fee_type() { 
		return refund_fee_type; 
	} 
	 
	public int getTotal_fee() { 
		return total_fee; 
	} 
	public void setTotal_fee(int total_fee) { 
		this.total_fee = total_fee; 
	} 
	public int getRefund_fee() { 
		return refund_fee; 
	} 
	public void setRefund_fee(int refund_fee) { 
		this.refund_fee = refund_fee; 
	} 
	public void setRefund_fee_type(String refund_fee_type) { 
		this.refund_fee_type = refund_fee_type; 
	} 
	public String getRefund_desc() { 
		return refund_desc; 
	} 
	public void setRefund_desc(String refund_desc) { 
		this.refund_desc = refund_desc; 
	} 
	public String getRefund_account() { 
		return refund_account; 
	} 
	public void setRefund_account(String refund_account) { 
		this.refund_account = refund_account; 
	} 
	public String getNotify_url() { 
		return notify_url; 
	} 
	public void setNotify_url(String notify_url) { 
		this.notify_url = notify_url; 
	} 
	 
} 
